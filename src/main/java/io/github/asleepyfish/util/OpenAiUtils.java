package io.github.asleepyfish.util;

import com.google.common.cache.Cache;
import com.theokanning.openai.DeleteResult;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.edit.EditRequest;
import com.theokanning.openai.edit.EditResult;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.finetune.FineTuneEvent;
import com.theokanning.openai.finetune.FineTuneRequest;
import com.theokanning.openai.finetune.FineTuneResult;
import com.theokanning.openai.image.CreateImageEditRequest;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.CreateImageVariationRequest;
import com.theokanning.openai.image.ImageResult;
import com.theokanning.openai.model.Model;
import io.github.asleepyfish.entity.audio.TranscriptionRequest;
import io.github.asleepyfish.entity.audio.TranslationRequest;
import io.github.asleepyfish.entity.billing.Billing;
import io.github.asleepyfish.entity.billing.Subscription;
import io.github.asleepyfish.enums.audio.AudioResponseFormatEnum;
import io.github.asleepyfish.enums.chat.RoleEnum;
import io.github.asleepyfish.enums.edit.EditModelEnum;
import io.github.asleepyfish.enums.embedding.EmbeddingModelEnum;
import io.github.asleepyfish.enums.image.ImageResponseFormatEnum;
import io.github.asleepyfish.enums.image.ImageSizeEnum;
import io.github.asleepyfish.enums.model.ModelEnum;
import io.github.asleepyfish.service.OpenAiProxyService;
import lombok.NonNull;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: asleepyfish
 * @Date: 2023/2/8 17:14
 * @Description: provide external call-related interfaces
 */
public class OpenAiUtils {

    private static final Log LOG = LogFactory.getLog(OpenAiUtils.class);

    private static OpenAiProxyService openAiProxyService;


    public OpenAiUtils(OpenAiProxyService openAiProxyService) {
        OpenAiUtils.openAiProxyService = openAiProxyService;
    }

    public static void createStreamChatCompletion(String content) {
        createStreamChatCompletion(content, "DEFAULT USER", System.out);
    }

    public static void createStreamChatCompletion(String content, OutputStream os) {
        createStreamChatCompletion(content, "DEFAULT USER", os);
    }

    public static void createStreamChatCompletion(String content, String user, OutputStream os) {
        openAiProxyService.createStreamChatCompletion(content, user, os);
    }

    public static void createStreamChatCompletion(String content, String user, String model, OutputStream os) {
        createStreamChatCompletion(RoleEnum.USER.getRoleName(), content, user, model, 1.0D, 1.0D, os);
    }

    public static void createStreamChatCompletion(String role, String content, String user, String model, Double temperature, Double topP, OutputStream os) {
        createStreamChatCompletion(ChatCompletionRequest.builder()
                .model(model)
                .messages(Collections.singletonList(new ChatMessage(role, content)))
                .user(user)
                .temperature(temperature)
                .topP(topP)
                .stream(true)
                .build(), os);
    }

    public static void createStreamChatCompletion(ChatCompletionRequest chatCompletionRequest, OutputStream os) {
        openAiProxyService.createStreamChatCompletion(chatCompletionRequest, os);
    }

    public static List<String> createChatCompletion(String content) {
        return createChatCompletion(content, "DEFAULT USER");
    }

    public static List<String> createChatCompletion(String content, String user) {
        return openAiProxyService.chatCompletion(content, user);
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
        return openAiProxyService.chatCompletion(chatCompletionRequest);
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
        return openAiProxyService.completion(prompt, user);
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
        return openAiProxyService.completion(completionRequest);
    }

    public static List<String> createImage(String prompt) {
        return createImage(prompt, "DEFAULT USER");
    }

    public static List<String> createImage(String prompt, String user) {
        return openAiProxyService.createImages(prompt, user);
    }

    public static List<String> createImages(String prompt, String user, ImageResponseFormatEnum responseFormat) {
        return openAiProxyService.createImages(prompt, user, responseFormat);
    }

    public static ImageResult createImage(CreateImageRequest createImageRequest) {
        return openAiProxyService.createImages(createImageRequest);
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
        openAiProxyService.downloadImage(createImageRequest, response);
    }

    /**
     * Get Bill Since startDate
     *
     * @param startDate startDate (yyyy-MM-dd)
     * @return bill
     */
    public static String billingUsage(String... startDate) {
        return openAiProxyService.billingUsage(startDate);
    }

    /**
     * You can query bills for up to 100 days at a time.
     *
     * @param startDate startDate
     * @param endDate   endDate
     * @return Unit: (USD)
     */
    public static String billingUsage(String startDate, String endDate) {
        return openAiProxyService.billingUsage(startDate, endDate);
    }

    /**
     * You can query all the available billing for a given date range.
     *
     * @param startDate startDate
     * @return billing
     */
    public static Billing billing(String... startDate) {
        return openAiProxyService.billing(startDate);
    }

    /**
     * Obtain subscription information
     *
     * @return subscription information
     */
    public static Subscription subscription() {
        return openAiProxyService.subscription();
    }

    /**
     * listModels
     *
     * @return list of models
     */
    public static List<Model> listModels() {
        return openAiProxyService.listModels();
    }

    /**
     * getModel
     *
     * @param model model
     * @return model
     */
    public static Model getModel(String model) {
        return openAiProxyService.getModel(model);
    }

    public static String edit(String input, String instruction) {
        return openAiProxyService.edit(input, instruction);
    }

    public static String edit(String input, String instruction, EditModelEnum editModelEnum) {
        return openAiProxyService.edit(input, instruction, editModelEnum);
    }

    public static String edit(String input, String instruction, Double temperature, Double topP, EditModelEnum editModelEnum) {
        return openAiProxyService.edit(input, instruction, temperature, topP, editModelEnum);
    }

    /**
     * edit
     *
     * @param editRequest editRequest
     * @return results
     */
    public static EditResult edit(EditRequest editRequest) {
        return openAiProxyService.edit(editRequest);
    }

    /**
     * embeddings
     *
     * @param text text
     * @return results
     */
    public static List<Double> embeddings(String text) {
        return openAiProxyService.embeddings(text);
    }

    /**
     * embeddings
     *
     * @param text               text
     * @param embeddingModelEnum embeddingModelEnum
     * @return results
     */
    public static List<Double> embeddings(String text, EmbeddingModelEnum embeddingModelEnum) {
        return openAiProxyService.embeddings(text, embeddingModelEnum);
    }

    /**
     * embeddings
     *
     * @param embeddingRequest embeddingRequest
     * @return results
     */
    public static EmbeddingResult embeddings(EmbeddingRequest embeddingRequest) {
        return openAiProxyService.embeddings(embeddingRequest);
    }

    /**
     * Transcribes audio into the input language.
     *
     * @param filePath                filePath
     * @param audioResponseFormatEnum audioResponseFormatEnum
     * @return text
     */
    public static String transcription(String filePath, AudioResponseFormatEnum audioResponseFormatEnum) {
        return openAiProxyService.transcription(filePath, audioResponseFormatEnum);
    }

    /**
     * Transcribes audio into the input language.
     *
     * @param file                    file
     * @param audioResponseFormatEnum audioResponseFormatEnum
     * @return text
     */
    public static String transcription(File file, AudioResponseFormatEnum audioResponseFormatEnum) {
        return openAiProxyService.transcription(file, audioResponseFormatEnum);
    }

    /**
     * Transcribes audio into the input language.
     *
     * @param transcriptionRequest transcriptionRequest
     * @return text
     */
    public static String transcription(TranscriptionRequest transcriptionRequest) {
        return openAiProxyService.transcription(transcriptionRequest);
    }

    /**
     * Translates audio into English.
     *
     * @param filePath                filePath
     * @param audioResponseFormatEnum audioResponseFormatEnum
     * @return text
     */
    public static String translation(String filePath, AudioResponseFormatEnum audioResponseFormatEnum) {
        return openAiProxyService.translation(filePath, audioResponseFormatEnum);
    }

    /**
     * Translates audio into English.
     *
     * @param file                    file
     * @param audioResponseFormatEnum audioResponseFormatEnum
     * @return text
     */
    public static String translation(File file, AudioResponseFormatEnum audioResponseFormatEnum) {
        return openAiProxyService.translation(file, audioResponseFormatEnum);
    }

    /**
     * Translates audio into English.
     *
     * @param translationRequest translationRequest
     * @return text
     */
    public static String translation(TranslationRequest translationRequest) {
        return openAiProxyService.translation(translationRequest);
    }

    /**
     * create Image Edit
     *
     * @param createImageEditRequest createImageEditRequest
     * @param imagePath              imagePath
     * @param maskPath               maskPath
     * @return imageResult
     */
    public static ImageResult createImageEdit(CreateImageEditRequest createImageEditRequest, String imagePath, String maskPath) {
        return openAiProxyService.createImageEdit(createImageEditRequest, imagePath, maskPath);
    }

    /**
     * create Image Edit
     *
     * @param createImageEditRequest createImageEditRequest
     * @param image                  image
     * @param mask                   mask
     * @return imageResult
     */
    public static ImageResult createImageEdit(CreateImageEditRequest createImageEditRequest, File image, File mask) {
        return openAiProxyService.createImageEdit(createImageEditRequest, image, mask);
    }

    /**
     * create Image Edit
     *
     * @param createImageVariationRequest createImageVariationRequest
     * @param imagePath                   imagePath
     * @return imageResult
     */
    public static ImageResult createImageVariation(CreateImageVariationRequest createImageVariationRequest, String imagePath) {
        return openAiProxyService.createImageVariation(createImageVariationRequest, imagePath);
    }

    /**
     * create Image Edit
     *
     * @param createImageVariationRequest createImageVariationRequest
     * @param image                       image
     * @return imageResult
     */
    public static ImageResult createImageVariation(CreateImageVariationRequest createImageVariationRequest, File image) {
        return openAiProxyService.createImageVariation(createImageVariationRequest, image);
    }

    /**
     * listFiles
     *
     * @return files
     */
    public static List<com.theokanning.openai.file.File> listFiles() {
        return openAiProxyService.listFiles();
    }

    /**
     * uploadFile
     *
     * @param purpose  purpose
     * @param filepath filepath
     * @return file
     */
    public static com.theokanning.openai.file.File uploadFile(@NonNull String purpose, @NonNull String filepath) {
        return openAiProxyService.uploadFile(purpose, filepath);
    }

    /**
     * deleteFile
     *
     * @param fileId fileId
     * @return DeleteResult
     */
    public static DeleteResult deleteFile(@NonNull String fileId) {
        return openAiProxyService.deleteFile(fileId);
    }

    /**
     * retrieveFile
     *
     * @param fileId fileId
     * @return file
     */
    public static com.theokanning.openai.file.File retrieveFile(@NonNull String fileId) {
        return openAiProxyService.retrieveFile(fileId);
    }

    /**
     * retrieveFileContent
     *
     * @param fileId fileId
     * @return file
     */
    public static String retrieveFileContent(@NonNull String fileId) {
        return openAiProxyService.retrieveFileContent(fileId);
    }

    public static FineTuneResult createFineTune(FineTuneRequest request) {
        return openAiProxyService.createFineTune(request);
    }

    public static CompletionResult createFineTuneCompletion(CompletionRequest request) {
        return openAiProxyService.createFineTuneCompletion(request);
    }

    public static List<FineTuneResult> listFineTunes() {
        return openAiProxyService.listFineTunes();
    }

    public static FineTuneResult retrieveFineTune(String fineTuneId) {
        return openAiProxyService.retrieveFineTune(fineTuneId);
    }

    public static FineTuneResult cancelFineTune(String fineTuneId) {
        return openAiProxyService.cancelFineTune(fineTuneId);
    }

    public static List<FineTuneEvent> listFineTuneEvents(String fineTuneId) {
        return openAiProxyService.listFineTuneEvents(fineTuneId);
    }

    public static DeleteResult deleteFineTune(String fineTuneId) {
        return openAiProxyService.deleteFineTune(fineTuneId);
    }

    public static void forceClearCache(String cacheName) {
        openAiProxyService.forceClearCache(cacheName);
    }

    public static Cache<String, LinkedList<ChatMessage>> retrieveCache() {
        return openAiProxyService.retrieveCache();
    }

    public static LinkedList<ChatMessage> retrieveChatMessage(String key) {
        return openAiProxyService.retrieveChatMessage(key);
    }

    public static void setCache(Cache<String, LinkedList<ChatMessage>> cache) {
        openAiProxyService.setCache(cache);
    }

    public static void addCache(String key, LinkedList<ChatMessage> chatMessages) {
        openAiProxyService.addCache(key, chatMessages);
    }
}
