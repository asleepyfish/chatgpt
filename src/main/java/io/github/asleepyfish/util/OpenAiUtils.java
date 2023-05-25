package io.github.asleepyfish.util;

import com.google.common.cache.Cache;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.image.CreateImageRequest;
import io.github.asleepyfish.entity.billing.Billing;
import io.github.asleepyfish.entity.billing.Subscription;
import io.github.asleepyfish.enums.ImageSizeEnum;
import io.github.asleepyfish.enums.ModelEnum;
import io.github.asleepyfish.enums.RoleEnum;
import io.github.asleepyfish.service.OpenAiProxyService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletResponse;
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
        return createImage(CreateImageRequest.builder()
                .prompt(prompt)
                .user(user)
                .build());
    }

    public static List<String> createImage(CreateImageRequest createImageRequest) {
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
