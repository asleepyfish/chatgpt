package io.github.asleepyfish.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.Image;
import com.theokanning.openai.service.OpenAiService;
import io.github.asleepyfish.config.ChatGPTProperties;
import io.github.asleepyfish.enums.*;
import io.github.asleepyfish.exception.ChatGPTException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Author: asleepyfish
 * @Date: 2023/2/8 17:14
 * @Description: provide external call-related interfaces
 */
public class OpenAiUtils {
    private static final Log LOG = LogFactory.getLog(OpenAiUtils.class);

    private static OpenAiService openAiService;

    private static ChatGPTProperties chatGPTProperties;

    private static final Random RANDOM = new Random();

    private static Cache<String, LinkedList<ChatMessage>> cache;

    public OpenAiUtils(OpenAiService openAiService, ChatGPTProperties chatGPTProperties) {
        OpenAiUtils.openAiService = openAiService;
        OpenAiUtils.chatGPTProperties = chatGPTProperties;
        cache = chatGPTProperties.getSessionExpirationTime() == null ?
                CacheBuilder.newBuilder()
                        .build() :
                CacheBuilder.newBuilder()
                        .expireAfterAccess(chatGPTProperties.getSessionExpirationTime(), TimeUnit.MINUTES)
                        .build();
    }

    public static List<String> createChatCompletion(String content) {
        return createChatCompletion(content, "DEFAULT USER");
    }

    public static List<String> createChatCompletion(String content, String user) {
        return createChatCompletion(content, user, chatGPTProperties.getChatModel());
    }

    public static List<String> createChatCompletion(String content, String user, String model) {
        return createChatCompletion(RoleEnum.USER.getRoleName(), content, user, model, 1.0D, 1.0D);
    }

    public static List<String> createChatCompletion(String role, String content, String user, String model, Double temperature, Double topP) {
        return createChatCompletion(ChatCompletionRequest.builder()
                .model(model)
                .messages(Collections.singletonList(new ChatMessage(role, content)))
                .user(user)
                .temperature(temperature)
                .topP(topP)
                .build());
    }

    public static List<String> createChatCompletion(ChatCompletionRequest chatCompletionRequest) {
        String user = chatCompletionRequest.getUser();
        LinkedList<ChatMessage> contextInfo = null;
        try {
            contextInfo = cache.get(user, LinkedList::new);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        assert contextInfo != null;
        contextInfo.addAll(chatCompletionRequest.getMessages());
        cache.put(user, contextInfo);
        chatCompletionRequest.setMessages(contextInfo);
        List<ChatCompletionChoice> choices = new ArrayList<>();
        for (int i = 0; i < chatGPTProperties.getRetries(); i++) {
            try {
                // avoid frequently request, random sleep 0.5s~1s
                if (i > 0) {
                    randomSleep();
                }
                choices = openAiService.createChatCompletion(chatCompletionRequest).getChoices();
                // if the last line code is correct, we can simply break the circle
                break;
            } catch (Exception e) {
                String message = e.getMessage();
                boolean overload = checkTokenUsage(message);
                if (overload) {
                    Objects.requireNonNull(cache.getIfPresent(user)).removeFirst();
                    chatCompletionRequest.getMessages().remove(0);
                }
                LOG.error("answer failed " + (i + 1) + " times, the error message is: " + message);
                if (i == chatGPTProperties.getRetries() - 1) {
                    e.printStackTrace();
                    throw new ChatGPTException(ChatGPTErrorEnum.FAILED_TO_GENERATE_ANSWER, message);
                }
            }
        }
        List<String> results = new ArrayList<>();
        choices.forEach(choice -> {
            String text = choice.getMessage().getContent();
            if (FinishReasonEnum.LENGTH.getMessage().equals(choice.getFinishReason())) {
                text = text + System.lineSeparator() + "The answer is too long, Please disassemble the above problems into several minor problems.";
            }
            results.add(text);
            try {
                LinkedList<ChatMessage> chatMessages = cache.get(user, LinkedList::new);
                chatMessages.add(choice.getMessage());
                cache.put(user, chatMessages);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
        return results;
    }

    private static void randomSleep() throws InterruptedException {
        Thread.sleep(500 + RANDOM.nextInt(500));
    }

    private static boolean checkTokenUsage(String message) {
        return message.contains("This model's maximum context length is 4096 tokens.");
    }

    /**
     * please use ChatCompletion instead
     *
     * @param prompt prompt
     * @return List<String>
     */
    @Deprecated
    public static List<String> createCompletion(String prompt) {
        return createCompletion(prompt, "DEFAULT USER");
    }

    @Deprecated
    public static List<String> createCompletion(String prompt, String user) {
        return createCompletion(prompt, user, chatGPTProperties.getModel());
    }

    @Deprecated
    public static List<String> createCompletion(String prompt, String user, String model) {
        return createCompletion(prompt, user, model, 0D, 1D);
    }

    @Deprecated
    public static List<String> createCompletion(String prompt, String user, String model, Double temperature, Double topP) {
        return createCompletion(CompletionRequest.builder()
                .model(model)
                .prompt(prompt)
                .user(user)
                .temperature(temperature)
                .topP(topP)
                .maxTokens(ModelEnum.getMaxTokens(model))
                .build());
    }

    @Deprecated
    public static List<String> createCompletion(CompletionRequest completionRequest) {
        List<CompletionChoice> choices = new ArrayList<>();

        for (int i = 0; i < chatGPTProperties.getRetries(); i++) {
            try {
                // avoid frequently request, random sleep 0.5s~1s
                if (i > 0) {
                    randomSleep();
                }
                choices = openAiService.createCompletion(completionRequest).getChoices();
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

    public static List<String> createImage(String prompt) {
        return createImage(prompt, "DEFAULT USER");
    }

    public static List<String> createImage(String prompt, String user) {
        return createImage(CreateImageRequest.builder()
                .prompt(prompt)
                .user(user)
                .build());
    }

    public static List<String> createImage(CreateImageRequest createImageRequest) {
        List<Image> imageList = new ArrayList<>();
        for (int i = 0; i < chatGPTProperties.getRetries(); i++) {
            try {
                if (i > 0) {
                    randomSleep();
                }
                imageList = openAiService.createImage(createImageRequest).getData();
                break;
            } catch (Exception e) {
                LOG.error("image generate failed " + (i + 1) + " times, the error message is: " + e.getMessage());
                if (i == chatGPTProperties.getRetries() - 1) {
                    e.printStackTrace();
                    throw new ChatGPTException(ChatGPTErrorEnum.FAILED_TO_GENERATE_IMAGE, e.getMessage());
                }
            }
        }
        String responseFormat = createImageRequest.getResponseFormat();
        // default response_format is url
        return imageList.stream().map(image -> responseFormat == null ||
                ImageResponseFormatEnum.URL.getResponseFormat().equals(responseFormat) ?
                image.getUrl() : image.getB64Json()).collect(Collectors.toList());
    }

    public static void downloadImage(String prompt, HttpServletResponse response) {
        downloadImage(prompt, ImageSizeEnum.S1024x1024.getSize(), response);
    }

    public static void downloadImage(String prompt, Integer n, HttpServletResponse response) {
        downloadImage(prompt, n, ImageSizeEnum.S1024x1024.getSize(), response);
    }

    public static void downloadImage(String prompt, String size, HttpServletResponse response) {
        downloadImage(prompt, 1, size, response);
    }

    public static void downloadImage(String prompt, Integer n, String size, HttpServletResponse response) {
        downloadImage(CreateImageRequest.builder()
                .prompt(prompt)
                .n(n)
                .size(size)
                .user("DEFAULT USER").build(), response);
    }

    public static void downloadImage(CreateImageRequest createImageRequest, HttpServletResponse response) {
        createImageRequest.setResponseFormat(ImageResponseFormatEnum.B64_JSON.getResponseFormat());
        if (!ImageResponseFormatEnum.B64_JSON.getResponseFormat().equals(createImageRequest.getResponseFormat())) {
            throw new ChatGPTException(ChatGPTErrorEnum.ERROR_RESPONSE_FORMAT);
        }
        List<String> imageList = createImage(createImageRequest);
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

    public static void forceClearCache(String cacheName) {
        cache.invalidate(cacheName);
    }

    private static BufferedImage getImageFromBase64(String base64) throws IOException {
        byte[] imageBytes = Base64.getDecoder().decode(base64.getBytes());
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
            return ImageIO.read(bis);
        }
    }
}
