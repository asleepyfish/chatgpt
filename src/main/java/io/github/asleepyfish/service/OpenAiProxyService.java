package io.github.asleepyfish.service;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.theokanning.openai.client.OpenAiApi;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.edit.EditRequest;
import com.theokanning.openai.edit.EditResult;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.image.*;
import com.theokanning.openai.image.Image;
import com.theokanning.openai.service.OpenAiService;
import io.github.asleepyfish.config.ChatGPTProperties;
import io.github.asleepyfish.entity.audio.TranscriptionRequest;
import io.github.asleepyfish.entity.audio.TranslationRequest;
import io.github.asleepyfish.entity.billing.Billing;
import io.github.asleepyfish.entity.billing.Subscription;
import io.github.asleepyfish.enums.audio.AudioModelEnum;
import io.github.asleepyfish.enums.audio.AudioResponseFormatEnum;
import io.github.asleepyfish.enums.chat.FinishReasonEnum;
import io.github.asleepyfish.enums.chat.RoleEnum;
import io.github.asleepyfish.enums.edit.EditModelEnum;
import io.github.asleepyfish.enums.embedding.EmbeddingModelEnum;
import io.github.asleepyfish.enums.exception.ChatGPTErrorEnum;
import io.github.asleepyfish.enums.image.ImageResponseFormatEnum;
import io.github.asleepyfish.enums.image.ImageSizeEnum;
import io.github.asleepyfish.enums.model.ModelEnum;
import io.github.asleepyfish.exception.ChatGPTException;
import okhttp3.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import retrofit2.Retrofit;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Author: asleepyfish
 * @Date: 2023/3/3 14:00
 * @Description: OpenAiProxyService
 */
public class OpenAiProxyService extends OpenAiService {

    private static final Log LOG = LogFactory.getLog(OpenAiProxyService.class);

    private static final String BASE_URL = "https://api.openai.com";

    private static final Random RANDOM = new Random();

    private final ChatGPTProperties chatGPTProperties;

    private final OkHttpClient client;

    private Cache<String, LinkedList<ChatMessage>> cache;

    public OpenAiProxyService(ChatGPTProperties chatGPTProperties, Duration timeout) {
        super(buildApi(chatGPTProperties.getToken(), timeout, chatGPTProperties.getProxyHost(), chatGPTProperties.getProxyPort()),
                defaultClient(chatGPTProperties.getToken(), timeout, chatGPTProperties.getProxyHost(), chatGPTProperties.getProxyPort()).dispatcher().executorService());
        this.chatGPTProperties = chatGPTProperties;
        this.cache = chatGPTProperties.getSessionExpirationTime() == null ? CacheBuilder.newBuilder().build() :
                CacheBuilder.newBuilder().expireAfterAccess(chatGPTProperties.getSessionExpirationTime(), TimeUnit.MINUTES).build();
        this.client = OpenAiProxyService.defaultClient(chatGPTProperties.getToken(), timeout, chatGPTProperties.getProxyHost(), chatGPTProperties.getProxyPort());
    }

    public OpenAiProxyService(ChatGPTProperties chatGPTProperties) {
        this(chatGPTProperties, Duration.ZERO);
    }

    public static OpenAiApi buildApi(String token, Duration timeout, String proxyHost, int proxyPort) {
        ObjectMapper mapper = defaultObjectMapper();
        OkHttpClient client = defaultClient(token, timeout, proxyHost, proxyPort);
        Retrofit retrofit = defaultRetrofit(client, mapper);
        return retrofit.create(OpenAiApi.class);
    }

    public static OkHttpClient defaultClient(String token, Duration timeout, String proxyHost, int proxyPort) {
        if (Strings.isNullOrEmpty(proxyHost)) {
            return OpenAiService.defaultClient(token, timeout);
        }
        // Create proxy object
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        return OpenAiService.defaultClient(token, timeout).newBuilder().proxy(proxy).build();
    }

    public void createStreamChatCompletion(String content) {
        createStreamChatCompletion(content, "DEFAULT USER", System.out);
    }

    public void createStreamChatCompletion(String content, OutputStream os) {
        createStreamChatCompletion(content, "DEFAULT USER", os);
    }

    public void createStreamChatCompletion(String content, String user, OutputStream os) {
        createStreamChatCompletion(content, user, chatGPTProperties.getChatModel(), os);
    }

    public void createStreamChatCompletion(String content, String user, String model, OutputStream os) {
        createStreamChatCompletion(RoleEnum.USER.getRoleName(), content, user, model, 1.0D, 1.0D, os);
    }

    public void createStreamChatCompletion(String role, String content, String user, String model, Double temperature, Double topP, OutputStream os) {
        createStreamChatCompletion(ChatCompletionRequest.builder()
                .model(model)
                .messages(Collections.singletonList(new ChatMessage(role, content)))
                .user(user)
                .temperature(temperature)
                .topP(topP)
                .stream(true)
                .build(), os);
    }

    public void createStreamChatCompletion(ChatCompletionRequest chatCompletionRequest, OutputStream os) {
        chatCompletionRequest.setStream(true);
        chatCompletionRequest.setN(1);
        String user = chatCompletionRequest.getUser();
        LinkedList<ChatMessage> contextInfo = new LinkedList<>();
        try {
            contextInfo = cache.get(user, LinkedList::new);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        contextInfo.addAll(chatCompletionRequest.getMessages());
        chatCompletionRequest.setMessages(contextInfo);
        List<ChatCompletionChunk> chunks = new ArrayList<>();
        for (int i = 0; i < chatGPTProperties.getRetries(); i++) {
            try {
                // avoid frequently request, random sleep 0.5s~0.7s
                if (i > 0) {
                    randomSleep();
                }
                super.streamChatCompletion(chatCompletionRequest).doOnError(Throwable::printStackTrace).blockingForEach(chunk -> {
                    chunk.getChoices().stream().map(choice -> choice.getMessage().getContent())
                            .filter(Objects::nonNull).findFirst().ifPresent(o -> {
                                try {
                                    os.write(o.getBytes(Charset.defaultCharset()));
                                    os.flush();
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
                    chunks.add(chunk);
                });
                os.close();
                // if the last line code is correct, we can simply break the circle
                break;
            } catch (Exception e) {
                String message = e.getMessage();
                boolean overload = checkTokenUsage(message);
                if (overload) {
                    int size = Objects.requireNonNull(cache.getIfPresent(user)).size();
                    for (int j = 0; j < size / 2; j++) {
                        Objects.requireNonNull(cache.getIfPresent(user)).removeFirst();
                    }
                    chatCompletionRequest.setMessages(cache.getIfPresent(user));
                }
                LOG.error("answer failed " + (i + 1) + " times, the error message is: " + message);
                if (i == chatGPTProperties.getRetries() - 1) {
                    e.printStackTrace();
                    // when the call fails, remove the last item in the list
                    Objects.requireNonNull(cache.getIfPresent(user)).removeLast();
                    throw new ChatGPTException(ChatGPTErrorEnum.FAILED_TO_GENERATE_ANSWER, message);
                }
            }
        }
        LinkedList<ChatMessage> chatMessages = new LinkedList<>();
        try {
            chatMessages = cache.get(user, LinkedList::new);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        chatMessages.add(new ChatMessage(RoleEnum.ASSISTANT.getRoleName(), chunks.stream()
                .flatMap(chunk -> chunk.getChoices().stream())
                .map(ChatCompletionChoice::getMessage)
                .map(ChatMessage::getContent)
                .filter(Objects::nonNull)
                .collect(Collectors.joining())));
    }

    public List<String> chatCompletion(String content) {
        return chatCompletion(content, "DEFAULT USER");
    }

    public List<String> chatCompletion(String content, String user) {
        return chatCompletion(content, user, chatGPTProperties.getChatModel());
    }

    public List<String> chatCompletion(String content, String user, String model) {
        return chatCompletion(RoleEnum.USER.getRoleName(), content, user, model, 1.0D, 1.0D);
    }

    public List<String> chatCompletion(String role, String content, String user, String model, Double temperature, Double topP) {
        return chatCompletion(ChatCompletionRequest.builder()
                .model(model)
                .messages(Collections.singletonList(new ChatMessage(role, content)))
                .user(user)
                .temperature(temperature)
                .topP(topP)
                .build());
    }

    public List<String> chatCompletion(ChatCompletionRequest chatCompletionRequest) {
        String user = chatCompletionRequest.getUser();
        LinkedList<ChatMessage> contextInfo = new LinkedList<>();
        try {
            contextInfo = cache.get(user, LinkedList::new);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        contextInfo.addAll(chatCompletionRequest.getMessages());
        chatCompletionRequest.setMessages(contextInfo);
        List<ChatCompletionChoice> choices = new ArrayList<>();
        for (int i = 0; i < chatGPTProperties.getRetries(); i++) {
            try {
                // avoid frequently request, random sleep 0.5s~0.7s
                if (i > 0) {
                    randomSleep();
                }
                choices = super.createChatCompletion(chatCompletionRequest).getChoices();
                // if the last line code is correct, we can simply break the circle
                break;
            } catch (Exception e) {
                String message = e.getMessage();
                boolean overload = checkTokenUsage(message);
                if (overload) {
                    int size = Objects.requireNonNull(cache.getIfPresent(user)).size();
                    for (int j = 0; j < size / 2; j++) {
                        Objects.requireNonNull(cache.getIfPresent(user)).removeFirst();
                    }
                    chatCompletionRequest.setMessages(cache.getIfPresent(user));
                }
                LOG.error("answer failed " + (i + 1) + " times, the error message is: " + message);
                if (i == chatGPTProperties.getRetries() - 1) {
                    e.printStackTrace();
                    // when the call fails, remove the last item in the list
                    Objects.requireNonNull(cache.getIfPresent(user)).removeLast();
                    throw new ChatGPTException(ChatGPTErrorEnum.FAILED_TO_GENERATE_ANSWER, message);
                }
            }
        }
        List<String> results = new ArrayList<>();
        LinkedList<ChatMessage> chatMessages = new LinkedList<>();
        try {
            chatMessages = cache.get(user, LinkedList::new);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        for (ChatCompletionChoice choice : choices) {
            String text = choice.getMessage().getContent();
            results.add(text);
            if (FinishReasonEnum.LENGTH.getMessage().equals(choice.getFinishReason())) {
                results.add("答案过长，请输入继续~");
            }
            chatMessages.add(choice.getMessage());
        }
        return results;
    }

    /**
     * please use ChatCompletion instead
     *
     * @param prompt prompt
     * @return List<String>
     */
    @Deprecated
    public List<String> completion(String prompt) {
        return completion(prompt, "DEFAULT USER");
    }

    @Deprecated
    public List<String> completion(String prompt, String user) {
        return completion(prompt, user, chatGPTProperties.getModel());
    }

    @Deprecated
    public List<String> completion(String prompt, String user, String model) {
        return completion(prompt, user, model, 0D, 1D);
    }

    @Deprecated
    public List<String> completion(String prompt, String user, String model, Double temperature, Double topP) {
        return completion(CompletionRequest.builder()
                .model(model)
                .prompt(prompt)
                .user(user)
                .temperature(temperature)
                .topP(topP)
                .maxTokens(ModelEnum.getMaxTokens(model))
                .build());
    }

    @Deprecated
    public List<String> completion(CompletionRequest completionRequest) {
        List<CompletionChoice> choices = new ArrayList<>();

        for (int i = 0; i < chatGPTProperties.getRetries(); i++) {
            try {
                // avoid frequently request, random sleep 0.5s~0.7s
                if (i > 0) {
                    randomSleep();
                }
                choices = super.createCompletion(completionRequest).getChoices();
                // if the last line code is correct, we can simply break the circle
                break;
            } catch (Exception e) {
                LOG.error("answer failed " + (i + 1) + " times, the error message is: " + e.getMessage());
                if (i == chatGPTProperties.getRetries() - 1) {
                    e.printStackTrace();
                    throw new ChatGPTException(ChatGPTErrorEnum.FAILED_TO_GENERATE_ANSWER, e.getMessage());
                }
            }
        }
        List<String> results = new ArrayList<>();
        choices.forEach(choice -> {
            String text = choice.getText();
            if (FinishReasonEnum.LENGTH.getMessage().equals(choice.getFinish_reason())) {
                text = text + System.lineSeparator() + "The answer is too long, Please disassemble the above problems into several minor problems.";
            }
            results.add(text);
        });
        return results;
    }

    public List<String> createImages(String prompt) {
        return createImages(prompt, "DEFAULT USER");
    }

    public List<String> createImages(String prompt, String user) {
        return createImages(prompt, user, ImageResponseFormatEnum.URL);
    }

    public List<String> createImages(String prompt, String user, ImageResponseFormatEnum responseFormat) {
        ImageResult imageResult = createImages(CreateImageRequest.builder()
                .prompt(prompt)
                .user(user)
                .responseFormat(responseFormat.getResponseFormat())
                .build());
        String format = responseFormat.getResponseFormat();
        return imageResult.getData().stream().map(image -> format == null ||
                ImageResponseFormatEnum.URL.getResponseFormat().equals(format) ?
                image.getUrl() : image.getB64Json()).collect(Collectors.toList());
    }

    public ImageResult createImages(CreateImageRequest createImageRequest) {
        ImageResult imageResult = new ImageResult();
        for (int i = 0; i < chatGPTProperties.getRetries(); i++) {
            try {
                if (i > 0) {
                    randomSleep();
                }
                imageResult = super.createImage(createImageRequest);
                break;
            } catch (Exception e) {
                LOG.error("image generate failed " + (i + 1) + " times, the error message is: " + e.getMessage());
                if (i == chatGPTProperties.getRetries() - 1) {
                    e.printStackTrace();
                    throw new ChatGPTException(ChatGPTErrorEnum.FAILED_TO_GENERATE_IMAGE, e.getMessage());
                }
            }
        }
        return imageResult;
    }

    public void downloadImage(String prompt, HttpServletResponse response) {
        downloadImage(prompt, ImageSizeEnum.S1024x1024.getSize(), response);
    }

    public void downloadImage(String prompt, Integer n, HttpServletResponse response) {
        downloadImage(prompt, n, ImageSizeEnum.S1024x1024.getSize(), response);
    }

    public void downloadImage(String prompt, String size, HttpServletResponse response) {
        downloadImage(prompt, 1, size, response);
    }

    public void downloadImage(String prompt, Integer n, String size, HttpServletResponse response) {
        downloadImage(CreateImageRequest.builder()
                .prompt(prompt)
                .n(n)
                .size(size)
                .user("DEFAULT USER").build(), response);
    }

    public void downloadImage(CreateImageRequest createImageRequest, HttpServletResponse response) {
        createImageRequest.setResponseFormat(ImageResponseFormatEnum.B64_JSON.getResponseFormat());
        if (!ImageResponseFormatEnum.B64_JSON.getResponseFormat().equals(createImageRequest.getResponseFormat())) {
            throw new ChatGPTException(ChatGPTErrorEnum.ERROR_RESPONSE_FORMAT);
        }
        List<String> imageList = createImages(createImageRequest).getData().stream()
                .map(Image::getB64Json).collect(Collectors.toList());
        try (OutputStream os = response.getOutputStream()) {
            if (imageList.size() == 1) {
                response.setContentType("image/png");
                response.setHeader("Content-Disposition", "attachment; filename=generated.png");
                BufferedImage bufferedImage = getImageFromBase64(imageList.get(0));
                ImageIO.write(bufferedImage, "png", os);
            } else {
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition", "attachment; filename=images.zip");
                try (ZipOutputStream zipOut = new ZipOutputStream(os)) {
                    for (int i = 0; i < imageList.size(); i++) {
                        BufferedImage bufferedImage = getImageFromBase64(imageList.get(i));
                        ZipEntry zipEntry = new ZipEntry("image" + (i + 1) + ".png");
                        zipOut.putNextEntry(zipEntry);
                        ImageIO.write(bufferedImage, "png", zipOut);
                        zipOut.closeEntry();
                    }
                }
            }
        } catch (Exception e) {
            throw new ChatGPTException(ChatGPTErrorEnum.DOWNLOAD_IMAGE_ERROR);
        }
    }

    /**
     * Get Bill Since startDate
     *
     * @param startDate startDate (yyyy-MM-dd)
     * @return bill
     */
    public String billingUsage(String... startDate) {
        String start = startDate.length == 0 ? "2023-01-01" : startDate[0];
        BigDecimal totalUsage = BigDecimal.ZERO;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            LocalDate endDate = LocalDate.now();
            // the max query bills scope up to 100 days. The interval for each query is defined as 3 months.
            Period threeMonth = Period.ofMonths(3);
            LocalDate nextDate = LocalDate.parse(start, formatter);
            while (nextDate.isBefore(endDate)) {
                String left = nextDate.format(formatter);
                nextDate = nextDate.plus(threeMonth);
                String right = nextDate.format(formatter);
                totalUsage = totalUsage.add(new BigDecimal(billingUsage(left, right)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return totalUsage.toPlainString();
    }

    /**
     * You can query bills for up to 100 days at a time.
     *
     * @param startDate startDate (yyyy-MM-dd)
     * @param endDate   endDate  (yyyy-MM-dd)
     * @return Unit: (USD)
     */
    public String billingUsage(String startDate, String endDate) {
        HttpUrl.Builder urlBuildr = HttpUrl.parse(BASE_URL + "/v1/dashboard/billing/usage").newBuilder();
        urlBuildr.addQueryParameter("start_date", startDate);
        urlBuildr.addQueryParameter("end_date", endDate);
        String url = urlBuildr.build().toString();
        Request request = new Request.Builder()
                .url(url)
                .build();
        String billingUsage = "0";
        for (int i = 0; i < chatGPTProperties.getRetries(); i++) {
            try (Response response = client.newCall(request).execute()) {
                if (i > 0) {
                    randomSleep();
                }
                String resStr = response.body().string();
                JSONObject resJson = JSONObject.parseObject(resStr);
                String cents = resJson.get("total_usage").toString();
                billingUsage = new BigDecimal(cents).divide(new BigDecimal("100")).toPlainString();
                break;
            } catch (Exception e) {
                LOG.error("query billingUsage failed " + (i + 1) + " times, the error message is: " + e.getMessage());
                if (i == chatGPTProperties.getRetries() - 1) {
                    e.printStackTrace();
                    throw new ChatGPTException(ChatGPTErrorEnum.QUERY_BILLINGUSAGE_ERROR, e.getMessage());
                }
            }
        }
        return billingUsage;
    }

    /**
     * You can query all the available billing for a given date range.
     *
     * @param startDate startDate
     * @return billing
     */
    public Billing billing(String... startDate) {
        String start = startDate.length == 0 ? "2023-01-01" : startDate[0];
        Subscription subscription = subscription();
        String usage = billingUsage(start);
        String dueDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(subscription.getAccessUntil() * 1000));
        String total = subscription.getSystemHardLimitUsd();
        Billing billing = new Billing();
        billing.setDueDate(dueDate);
        billing.setTotal(total);
        billing.setUsage(usage);
        billing.setBalance(new BigDecimal(total).subtract(new BigDecimal(usage)).toPlainString());
        return billing;
    }

    /**
     * Obtain subscription information
     *
     * @return subscription information
     */
    public Subscription subscription() {
        Request request = new Request.Builder()
                .url(BASE_URL + "/v1/dashboard/billing/subscription")
                .build();
        Subscription subscription = null;
        for (int i = 0; i < chatGPTProperties.getRetries(); i++) {
            try (Response response = client.newCall(request).execute()) {
                if (i > 0) {
                    randomSleep();
                }
                String resStr = response.body().string();
                subscription = JSONObject.parseObject(resStr, Subscription.class);
                break;
            } catch (Exception e) {
                LOG.error("query billingUsage failed " + (i + 1) + " times, the error message is: " + e.getMessage());
                if (i == chatGPTProperties.getRetries() - 1) {
                    e.printStackTrace();
                    throw new ChatGPTException(ChatGPTErrorEnum.QUERY_BILLINGUSAGE_ERROR, e.getMessage());
                }
            }
        }
        return subscription;
    }

    public String edit(String input, String instruction) {
        return edit(input, instruction, EditModelEnum.TEXT_DAVINCI_EDIT_001);
    }

    public String edit(String input, String instruction, EditModelEnum editModelEnum) {
        return edit(input, instruction, 1D, 1D, editModelEnum);
    }

    public String edit(String input, String instruction, Double temperature, Double topP, EditModelEnum editModelEnum) {
        EditResult editResult = edit(EditRequest.builder()
                .model(editModelEnum.getModelName())
                .input(input)
                .instruction(instruction)
                .temperature(temperature)
                .topP(topP)
                .build());
        List<String> results = Lists.newArrayList();
        editResult.getChoices().forEach(choice -> results.add(choice.getText()));
        return results.get(0);
    }

    /**
     * edit
     *
     * @param editRequest editRequest
     * @return results
     */
    public EditResult edit(EditRequest editRequest) {
        EditResult editResult = new EditResult();
        for (int i = 0; i < chatGPTProperties.getRetries(); i++) {
            try {
                if (i > 0) {
                    randomSleep();
                }
                editResult = super.createEdit(editRequest);
                break;
            } catch (Exception e) {
                LOG.error("edit failed " + (i + 1) + " times, the error message is: " + e.getMessage());
                if (i == chatGPTProperties.getRetries() - 1) {
                    e.printStackTrace();
                    throw new ChatGPTException(ChatGPTErrorEnum.EDIT_ERROR, e.getMessage());
                }
            }
        }
        return editResult;
    }

    /**
     * embeddings
     *
     * @param input input
     * @return results
     */
    public List<Double> embeddings(String input) {
        return embeddings(input, EmbeddingModelEnum.TEXT_EMBEDDING_ADA_002);
    }

    /**
     * embeddings
     *
     * @param input              input
     * @param embeddingModelEnum embeddingModelEnum
     * @return results
     */
    public List<Double> embeddings(String input, EmbeddingModelEnum embeddingModelEnum) {
        return embeddings(EmbeddingRequest.builder()
                .input(Collections.singletonList(input))
                .model(embeddingModelEnum.getModelName())
                .build()).getData().get(0).getEmbedding();
    }

    /**
     * edit
     *
     * @param embeddingRequest embeddingRequest
     * @return results
     */
    public EmbeddingResult embeddings(EmbeddingRequest embeddingRequest) {
        EmbeddingResult embeddingResult = new EmbeddingResult();
        for (int i = 0; i < chatGPTProperties.getRetries(); i++) {
            try {
                if (i > 0) {
                    randomSleep();
                }
                embeddingResult = super.createEmbeddings(embeddingRequest);
            } catch (Exception e) {
                LOG.error("embeddings failed " + (i + 1) + " times, the error message is: " + e.getMessage());
                if (i == chatGPTProperties.getRetries() - 1) {
                    e.printStackTrace();
                    throw new ChatGPTException(ChatGPTErrorEnum.EMBEDDINGS_ERROR, e.getMessage());
                }
            }
        }
        return embeddingResult;
    }

    /**
     * Transcribes audio into the input language.
     *
     * @param filePath                filePath
     * @param audioResponseFormatEnum audioResponseFormatEnum
     * @return text
     */
    public String transcription(String filePath, AudioResponseFormatEnum audioResponseFormatEnum) {
        File file = new File(filePath);
        return transcription(file, audioResponseFormatEnum);
    }

    /**
     * Transcribes audio into the input language.
     *
     * @param file                    file
     * @param audioResponseFormatEnum audioResponseFormatEnum
     * @return text
     */
    public String transcription(File file, AudioResponseFormatEnum audioResponseFormatEnum) {
        TranscriptionRequest transcriptionRequest = TranscriptionRequest.builder()
                .file(file).model(AudioModelEnum.WHISPER_1.getModelName())
                .responseFormat(audioResponseFormatEnum.getFormat()).build();
        return transcription(transcriptionRequest);
    }

    /**
     * Transcribes audio into the input language.
     *
     * @param transcriptionRequest transcriptionRequest
     * @return text
     */
    public String transcription(TranscriptionRequest transcriptionRequest) {
        // Create Request Body
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model", transcriptionRequest.getModel())
                .addFormDataPart("file", transcriptionRequest.getFile().getName(),
                        RequestBody.Companion.create(transcriptionRequest.getFile(), MediaType.parse("application/octet-stream")));
        if (transcriptionRequest.getPrompt() != null) {
            builder.addFormDataPart("prompt", transcriptionRequest.getPrompt());
        }
        if (transcriptionRequest.getResponseFormat() != null) {
            builder.addFormDataPart("response_format", transcriptionRequest.getResponseFormat());
        }
        if (transcriptionRequest.getTemperature() != null) {
            builder.addFormDataPart("temperature", String.valueOf(transcriptionRequest.getTemperature()));
        }
        if (transcriptionRequest.getLanguage() != null) {
            builder.addFormDataPart("language", transcriptionRequest.getLanguage());
        }
        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .post(requestBody)
                .url(BASE_URL + "/v1/audio/transcriptions")
                .build();
        String text = null;
        for (int i = 0; i < chatGPTProperties.getRetries(); i++) {
            try (Response response = client.newCall(request).execute()) {
                if (i > 0) {
                    randomSleep();
                }
                text = response.body().string();
                break;
            } catch (Exception e) {
                LOG.error("transcription failed " + (i + 1) + " times, the error message is: " + e.getMessage());
                if (i == chatGPTProperties.getRetries() - 1) {
                    e.printStackTrace();
                    throw new ChatGPTException(ChatGPTErrorEnum.TRANSCRIPTION_ERROR, e.getMessage());
                }
            }
        }
        return text;
    }

    /**
     * Translates audio into English.
     *
     * @param filePath                filePath
     * @param audioResponseFormatEnum audioResponseFormatEnum
     * @return text
     */
    public String translation(String filePath, AudioResponseFormatEnum audioResponseFormatEnum) {
        File file = new File(filePath);
        return translation(file, audioResponseFormatEnum);
    }

    /**
     * Translates audio into English.
     *
     * @param file                    file
     * @param audioResponseFormatEnum audioResponseFormatEnum
     * @return text
     */
    public String translation(File file, AudioResponseFormatEnum audioResponseFormatEnum) {
        TranslationRequest translationRequest = TranslationRequest.builder()
                .file(file).model(AudioModelEnum.WHISPER_1.getModelName())
                .responseFormat(audioResponseFormatEnum.getFormat()).build();
        return translation(translationRequest);
    }

    /**
     * Translates audio into English.
     *
     * @param translationRequest translationRequest
     * @return text
     */
    public String translation(TranslationRequest translationRequest) {
        // Create Request Body
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model", translationRequest.getModel())
                .addFormDataPart("file", translationRequest.getFile().getName(),
                        RequestBody.Companion.create(translationRequest.getFile(), MediaType.parse("application/octet-stream")));
        if (translationRequest.getPrompt() != null) {
            builder.addFormDataPart("prompt", translationRequest.getPrompt());
        }
        if (translationRequest.getResponseFormat() != null) {
            builder.addFormDataPart("response_format", translationRequest.getResponseFormat());
        }
        if (translationRequest.getTemperature() != null) {
            builder.addFormDataPart("temperature", String.valueOf(translationRequest.getTemperature()));
        }
        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .post(requestBody)
                .url(BASE_URL + "/v1/audio/translations")
                .build();
        String text = null;
        for (int i = 0; i < chatGPTProperties.getRetries(); i++) {
            try (Response response = client.newCall(request).execute()) {
                if (i > 0) {
                    randomSleep();
                }
                text = response.body().string();
                break;
            } catch (Exception e) {
                LOG.error("translation failed " + (i + 1) + " times, the error message is: " + e.getMessage());
                if (i == chatGPTProperties.getRetries() - 1) {
                    e.printStackTrace();
                    throw new ChatGPTException(ChatGPTErrorEnum.TRANSLATION_ERROR, e.getMessage());
                }
            }
        }
        return text;
    }

    /**
     * create Image Edit
     *
     * @param createImageEditRequest createImageEditRequest
     * @param imagePath              imagePath
     * @param maskPath               maskPath
     * @return imageResult
     */
    public ImageResult createImageEdit(CreateImageEditRequest createImageEditRequest, String imagePath, String maskPath) {
        File image = new File(imagePath);
        File mask = null;
        if (maskPath != null) {
            mask = new File(maskPath);
        }
        return createImageEdit(createImageEditRequest, image, mask);
    }

    /**
     * create Image Edit
     *
     * @param createImageEditRequest createImageEditRequest
     * @param image                  image
     * @param mask                   mask
     * @return imageResult
     */
    public ImageResult createImageEdit(CreateImageEditRequest createImageEditRequest, File image, File mask) {
        try {
            convertColorFormats(image);
            if (mask != null) {
                convertColorFormats(mask);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }

        // Create Request Body
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("prompt", createImageEditRequest.getPrompt())
                .addFormDataPart("image", image.getName(),
                        RequestBody.Companion.create(image, MediaType.parse("application/octet-stream")));
        if (mask != null) {
            builder.addFormDataPart("mask", mask.getName(),
                    RequestBody.Companion.create(mask, MediaType.parse("application/octet-stream")));
        }
        if (createImageEditRequest.getN() != null) {
            builder.addFormDataPart("n", String.valueOf(createImageEditRequest.getN()));
        }
        if (createImageEditRequest.getResponseFormat() != null) {
            builder.addFormDataPart("response_format", createImageEditRequest.getResponseFormat());
        }
        if (createImageEditRequest.getSize() != null) {
            builder.addFormDataPart("size", createImageEditRequest.getSize());
        }
        if (createImageEditRequest.getUser() != null) {
            builder.addFormDataPart("user", createImageEditRequest.getUser());
        }
        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .post(requestBody)
                .url(BASE_URL + "/v1/images/edits")
                .build();
        ImageResult imageResult = new ImageResult();
        for (int i = 0; i < chatGPTProperties.getRetries(); i++) {
            try (Response response = client.newCall(request).execute()) {
                if (i > 0) {
                    randomSleep();
                }
                String res = response.body().string();
                imageResult = JSONObject.parseObject(res, ImageResult.class);
                break;
            } catch (Exception e) {
                LOG.error("image generate failed " + (i + 1) + " times, the error message is: " + e.getMessage());
                if (i == chatGPTProperties.getRetries() - 1) {
                    e.printStackTrace();
                    throw new ChatGPTException(ChatGPTErrorEnum.CREATE_IMAGE_EDIT_ERROR, e.getMessage());
                }
            }
        }
        return imageResult;
    }

    /**
     * create Image Edit
     *
     * @param createImageVariationRequest createImageVariationRequest
     * @param imagePath                   imagePath
     * @return imageResult
     */
    public ImageResult createImageVariation(CreateImageVariationRequest createImageVariationRequest, String imagePath) {
        File image = new File(imagePath);
        return createImageVariation(createImageVariationRequest, image);
    }

    /**
     * create Image Variation
     *
     * @param createImageVariationRequest createImageVariationRequest
     * @param image                       image
     * @return imageResult
     */
    public ImageResult createImageVariation(CreateImageVariationRequest createImageVariationRequest, File image) {
        try {
            convertColorFormats(image);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }

        // Create Request Body
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", image.getName(),
                        RequestBody.Companion.create(image, MediaType.parse("application/octet-stream")));
        if (createImageVariationRequest.getN() != null) {
            builder.addFormDataPart("n", String.valueOf(createImageVariationRequest.getN()));
        }
        if (createImageVariationRequest.getResponseFormat() != null) {
            builder.addFormDataPart("response_format", createImageVariationRequest.getResponseFormat());
        }
        if (createImageVariationRequest.getSize() != null) {
            builder.addFormDataPart("size", createImageVariationRequest.getSize());
        }
        if (createImageVariationRequest.getUser() != null) {
            builder.addFormDataPart("user", createImageVariationRequest.getUser());
        }
        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .post(requestBody)
                .url(BASE_URL + "/v1/images/variations")
                .build();
        ImageResult imageResult = new ImageResult();
        for (int i = 0; i < chatGPTProperties.getRetries(); i++) {
            try (Response response = client.newCall(request).execute()) {
                if (i > 0) {
                    randomSleep();
                }
                String res = response.body().string();
                imageResult = JSONObject.parseObject(res, ImageResult.class);
                break;
            } catch (Exception e) {
                LOG.error("image generate failed " + (i + 1) + " times, the error message is: " + e.getMessage());
                if (i == chatGPTProperties.getRetries() - 1) {
                    e.printStackTrace();
                    throw new ChatGPTException(ChatGPTErrorEnum.CREATE_IMAGE_VARIATION_ERROR, e.getMessage());
                }
            }
        }
        return imageResult;
    }

    public void forceClearCache(String cacheName) {
        this.cache.invalidate(cacheName);
    }

    public Cache<String, LinkedList<ChatMessage>> retrieveCache() {
        return this.cache;
    }

    public LinkedList<ChatMessage> retrieveChatMessage(String key) {
        return this.cache.getIfPresent(key);
    }

    public void setCache(Cache<String, LinkedList<ChatMessage>> cache) {
        this.cache = cache;
    }

    public void addCache(String key, LinkedList<ChatMessage> chatMessages) {
        this.cache.put(key, chatMessages);
    }

    private void randomSleep() throws InterruptedException {
        Thread.sleep(500 + RANDOM.nextInt(200));
    }

    private static boolean checkTokenUsage(String message) {
        return message != null && message.contains("This model's maximum context length is");
    }

    private BufferedImage getImageFromBase64(String base64) throws IOException {
        byte[] imageBytes = Base64.getDecoder().decode(base64.getBytes());
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
            return ImageIO.read(bis);
        }
    }

    /**
     * convert color formats
     *
     * @param image image
     * @throws IOException IOException
     */
    private void convertColorFormats(File image) throws IOException {
        BufferedImage inputImage = ImageIO.read(image);

        // Get the color model of the image
        ColorModel colorModel = inputImage.getColorModel();
        // Check the mode of the image
        ComponentColorModel componentColorModel = (ComponentColorModel) colorModel;

        // Check the pixel format of the image
        int pixelSize = componentColorModel.getPixelSize();
        int numComponents = componentColorModel.getNumComponents();
        boolean isRGBA = pixelSize == 32 && numComponents == 4;
        boolean isL = pixelSize == 8 && numComponents == 1;
        boolean isLA = pixelSize == 16 && numComponents == 2;

        if (!isRGBA && !isL && !isLA) {
            // Create a new RGBA image
            BufferedImage outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);

            // Draw the original image to the new image
            Graphics2D g2d = outputImage.createGraphics();
            g2d.drawImage(inputImage, 0, 0, null);
            g2d.dispose();

            // Save New Image
            ImageIO.write(outputImage, "png", image);
        }

    }
}
