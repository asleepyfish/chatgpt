package io.github.asleepyfish.util;

import com.google.common.cache.Cache;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.ModelType;
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
import com.theokanning.openai.moderation.ModerationRequest;
import com.theokanning.openai.moderation.ModerationResult;
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

    private static OpenAiProxyService openAiProxyService;


    public OpenAiUtils(OpenAiProxyService openAiProxyService) {
        OpenAiUtils.openAiProxyService = openAiProxyService;
    }

    /**
     * create stream chat completion
     *
     * @param content content
     */
    public static void createStreamChatCompletion(String content) {
        createStreamChatCompletion(content, "DEFAULT USER", System.out);
    }

    /**
     * create stream chat completion
     *
     * @param content content
     * @param os      os
     */
    public static void createStreamChatCompletion(String content, OutputStream os) {
        createStreamChatCompletion(content, "DEFAULT USER", os);
    }

    /**
     * create stream chat completion
     *
     * @param content content
     * @param user    user
     */
    public static void createStreamChatCompletion(String content, String user, OutputStream os) {
        openAiProxyService.createStreamChatCompletion(content, user, os);
    }

    /**
     * create stream chat completion
     *
     * @param content content
     * @param user    user
     * @param model   model
     * @param os      os
     */
    public static void createStreamChatCompletion(String content, String user, String model, OutputStream os) {
        createStreamChatCompletion(RoleEnum.USER.getRoleName(), content, user, model, 1.0D, 1.0D, os);
    }

    /**
     * create stream chat completion
     *
     * @param role        role
     * @param content     content
     * @param user        user
     * @param model       model
     * @param os          os
     * @param temperature temperature
     * @param topP        topP
     */
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

    /**
     * create stream chat completion
     *
     * @param chatCompletionRequest chatCompletionRequest
     * @param os                    os
     */
    public static void createStreamChatCompletion(ChatCompletionRequest chatCompletionRequest, OutputStream os) {
        openAiProxyService.createStreamChatCompletion(chatCompletionRequest, os);
    }

    /**
     * create chat completion
     *
     * @param content content
     * @return List<String>
     */
    public static List<String> createChatCompletion(String content) {
        return createChatCompletion(content, "DEFAULT USER");
    }

    /**
     * create chat completion
     *
     * @param content content
     * @param user    user
     * @return List<String>
     */
    public static List<String> createChatCompletion(String content, String user) {
        return openAiProxyService.chatCompletion(content, user);
    }

    /**
     * create chat completion
     *
     * @param content content
     * @param user    user
     * @param model   model
     * @return List<String>
     */
    public static List<String> createChatCompletion(String content, String user, String model) {
        return createChatCompletion(RoleEnum.USER.getRoleName(), content, user, model, 1.0D, 1.0D);
    }

    /**
     * create chat completion
     *
     * @param role        role
     * @param content     content
     * @param user        user
     * @param model       model
     * @param temperature temperature
     * @param topP        topP
     * @return List<String>
     */
    public static List<String> createChatCompletion(String role, String content, String user, String model, Double temperature, Double topP) {
        return createChatCompletion(ChatCompletionRequest.builder()
                .model(model)
                .messages(Collections.singletonList(new ChatMessage(role, content)))
                .user(user)
                .temperature(temperature)
                .topP(topP)
                .build());
    }

    /**
     * create chat completion
     *
     * @param chatCompletionRequest chatCompletionRequest
     * @return List<String>
     */
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

    /**
     * create image
     *
     * @param prompt prompt
     * @return List<String>
     */
    public static List<String> createImage(String prompt) {
        return createImage(prompt, "DEFAULT USER");
    }

    /**
     * create image
     *
     * @param prompt prompt
     * @param user   user
     * @return List<String>
     */
    public static List<String> createImage(String prompt, String user) {
        return openAiProxyService.createImages(prompt, user);
    }

    /**
     * create image
     *
     * @param prompt         prompt
     * @param user           user
     * @param responseFormat responseFormat
     * @return List<String>
     */
    public static List<String> createImage(String prompt, String user, ImageResponseFormatEnum responseFormat) {
        return openAiProxyService.createImages(prompt, user, responseFormat);
    }

    /**
     * create image
     *
     * @param prompt         prompt
     * @param user           user
     * @param responseFormat responseFormat
     * @param imageSizeEnum  imageSizeEnum
     * @return List<String>
     */
    public static List<String> createImage(String prompt, String user, ImageResponseFormatEnum responseFormat, ImageSizeEnum imageSizeEnum) {
        return openAiProxyService.createImages(prompt, user, responseFormat, imageSizeEnum);
    }

    /**
     * create image
     *
     * @param createImageRequest createImageRequest
     * @return List<String>
     */
    public static ImageResult createImage(CreateImageRequest createImageRequest) {
        return openAiProxyService.createImages(createImageRequest);
    }

    /**
     * download image
     *
     * @param prompt prompt
     * @param os     os
     */
    public static void downloadImage(String prompt, OutputStream os) {
        downloadImage(prompt, ImageSizeEnum.S1024x1024.getSize(), os);
    }

    /**
     * download image
     *
     * @param prompt prompt
     * @param n      n
     * @param os     os
     */
    public static void downloadImage(String prompt, Integer n, OutputStream os) {
        downloadImage(prompt, n, ImageSizeEnum.S1024x1024.getSize(), os);
    }

    /**
     * download image
     *
     * @param prompt prompt
     * @param size   size
     * @param os     os
     */
    public static void downloadImage(String prompt, String size, OutputStream os) {
        downloadImage(prompt, 1, size, os);
    }

    /**
     * download image
     *
     * @param prompt prompt
     * @param n      n
     * @param size   size
     * @param os     os
     */
    public static void downloadImage(String prompt, Integer n, String size, OutputStream os) {
        downloadImage(CreateImageRequest.builder()
                .prompt(prompt)
                .n(n)
                .size(size)
                .user("DEFAULT USER").build(), os);
    }

    /**
     * download image
     *
     * @param createImageRequest createImageRequest
     * @param os                 os
     */
    public static void downloadImage(CreateImageRequest createImageRequest, OutputStream os) {
        openAiProxyService.downloadImage(createImageRequest, os);
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

    /**
     * edit
     *
     * @param input       input
     * @param instruction instruction
     * @return results
     */
    public static String edit(String input, String instruction) {
        return openAiProxyService.edit(input, instruction);
    }

    /**
     * edit
     *
     * @param input         input
     * @param instruction   instruction
     * @param editModelEnum editModelEnum
     * @return results
     */
    public static String edit(String input, String instruction, EditModelEnum editModelEnum) {
        return openAiProxyService.edit(input, instruction, editModelEnum);
    }

    /**
     * edit
     *
     * @param input         input
     * @param instruction   instruction
     * @param temperature   temperature
     * @param topP          topP
     * @param editModelEnum editModelEnum
     * @return results
     */
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

    /**
     * createFineTune
     *
     * @param FineTuneRequest FineTuneRequest
     * @return FineTuneResult
     */
    public static FineTuneResult createFineTune(FineTuneRequest FineTuneRequest) {
        return openAiProxyService.createFineTune(FineTuneRequest);
    }

    /**
     * createFineTuneCompletion
     *
     * @param completionRequest completionRequest
     * @return CompletionResult
     */
    public static CompletionResult createFineTuneCompletion(CompletionRequest completionRequest) {
        return openAiProxyService.createFineTuneCompletion(completionRequest);
    }

    /**
     * listFineTunes
     *
     * @return List
     */
    public static List<FineTuneResult> listFineTunes() {
        return openAiProxyService.listFineTunes();
    }

    /**
     * retrieveFineTune
     *
     * @param fineTuneId fineTuneId
     * @return FineTuneResult
     */
    public static FineTuneResult retrieveFineTune(String fineTuneId) {
        return openAiProxyService.retrieveFineTune(fineTuneId);
    }

    /**
     * cancelFineTune
     *
     * @param fineTuneId fineTuneId
     * @return FineTuneResult
     */
    public static FineTuneResult cancelFineTune(String fineTuneId) {
        return openAiProxyService.cancelFineTune(fineTuneId);
    }

    /**
     * listFineTuneEvents
     *
     * @param fineTuneId fineTuneId
     * @return List
     */
    public static List<FineTuneEvent> listFineTuneEvents(String fineTuneId) {
        return openAiProxyService.listFineTuneEvents(fineTuneId);
    }

    /**
     * deleteFineTune
     *
     * @param fineTuneId fineTuneId
     * @return DeleteResult
     */
    public static DeleteResult deleteFineTune(String fineTuneId) {
        return openAiProxyService.deleteFineTune(fineTuneId);
    }

    /**
     * createModeration
     *
     * @param moderationRequest moderationRequest
     * @return ModerationResult
     */
    public static ModerationResult createModeration(ModerationRequest moderationRequest) {
        return openAiProxyService.createModeration(moderationRequest);
    }

    /**
     * force clear cache
     *
     * @param cacheName cacheName
     */
    public static void forceClearCache(String cacheName) {
        openAiProxyService.forceClearCache(cacheName);
    }

    /**
     * retrieve cache
     *
     * @return cache
     */
    public static Cache<String, LinkedList<ChatMessage>> retrieveCache() {
        return openAiProxyService.retrieveCache();
    }

    /**
     * retrieve chat message
     *
     * @param key key
     * @return chat message
     */
    public static LinkedList<ChatMessage> retrieveChatMessage(String key) {
        return openAiProxyService.retrieveChatMessage(key);
    }

    /**
     * set cache
     *
     * @param cache cache
     */
    public static void setCache(Cache<String, LinkedList<ChatMessage>> cache) {
        openAiProxyService.setCache(cache);
    }

    /**
     * add cache
     *
     * @param key          key
     * @param chatMessages chatMessages
     */
    public static void addCache(String key, LinkedList<ChatMessage> chatMessages) {
        openAiProxyService.addCache(key, chatMessages);
    }

    /**
     * Set System Prompt
     *
     * @param systemPrompt systemPrompt
     */
    public static void setSystemPrompt(String systemPrompt) {
        openAiProxyService.setSystemPrompt(systemPrompt);
    }

    /**
     * Get System Prompt
     *
     * @return systemPrompt
     */
    public static String getSystemPrompt() {
        return openAiProxyService.getSystemPrompt();
    }

    /**
     * clean up system prompt
     */
    public static void cleanUpSystemPrompt() {
        openAiProxyService.cleanUpSystemPrompt();
    }

    /**
     * 计数Token
     *
     * @param text 文本
     * @return int
     */
    public static int countTokens(String text) {
        return openAiProxyService.countTokens(text);
    }

    /**
     * 计数Token
     *
     * @param text      文本
     * @param modelType 型号类型
     * @return int
     */
    public static int countTokens(String text, ModelType modelType) {
        return openAiProxyService.countTokens(text, modelType);
    }

    /**
     * 获取模型编码
     *
     * @param modelType 型号类型
     * @return {@link Encoding}
     */
    public static Encoding getEncodingForModel(ModelType modelType) {
        return openAiProxyService.getEncodingForModel(modelType);
    }
}
