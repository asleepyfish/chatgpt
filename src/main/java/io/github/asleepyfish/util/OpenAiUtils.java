package io.github.asleepyfish.util;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import io.github.asleepyfish.config.ChatGPTProperties;
import io.github.asleepyfish.enums.ChatGPTErrorEnum;
import io.github.asleepyfish.enums.FinishReasonEnum;
import io.github.asleepyfish.enums.ModelEnum;
import io.github.asleepyfish.exception.ChatGPTException;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: asleepyfish
 * @Date: 2023/2/8 17:14
 * @Description: provide external call-related interfaces
 */
public class OpenAiUtils {
    private static OpenAiService openAiService;

    private static ChatGPTProperties chatGPTProperties;

    public OpenAiUtils(OpenAiService openAiService, ChatGPTProperties chatGPTProperties) {
        OpenAiUtils.openAiService = openAiService;
        OpenAiUtils.chatGPTProperties = chatGPTProperties;
    }

    public static List<String> createCompletion(String prompt) {
        return createCompletion(prompt, "DEFAULT USER");
    }

    public static List<String> createCompletion(String prompt, String user) {
        return createCompletion(prompt, user, chatGPTProperties.getModel());
    }

    public static List<String> createCompletion(String prompt, String user, String model) {
        return createCompletion(prompt, user, model, 0.9D, 1D);
    }

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

    public static List<String> createCompletion(CompletionRequest completionRequest) {
        List<CompletionChoice> choices = openAiService.createCompletion(completionRequest).getChoices();
        if (CollectionUtils.isEmpty(choices)) {
            throw new ChatGPTException(ChatGPTErrorEnum.FAILED_TO_GENERATE_ANSWER, completionRequest.getPrompt());
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
}
