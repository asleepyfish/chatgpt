package io.github.asleepyfish.service;

import com.theokanning.openai.OpenAiService;

/**
 * @Author: asleepyfish
 * @Date: 2023/2/8 17:14
 * @Description: provide external call-related interfaces
 */
public class OpenAiUtils {
    private static OpenAiService openAiService;

    public OpenAiUtils(OpenAiService openAiService) {
        OpenAiUtils.openAiService = openAiService;
    }


    public static String createCompletion(String model, String prompt, String user) {
        return null;
    }
}
