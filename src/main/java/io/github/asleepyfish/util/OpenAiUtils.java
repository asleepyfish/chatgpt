package io.github.asleepyfish.util;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.Image;
import io.github.asleepyfish.config.ChatGPTProperties;
import io.github.asleepyfish.enums.ChatGPTErrorEnum;
import io.github.asleepyfish.enums.FinishReasonEnum;
import io.github.asleepyfish.enums.ImageResponseFormatEnum;
import io.github.asleepyfish.enums.ModelEnum;
import io.github.asleepyfish.exception.ChatGPTException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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
        List<CompletionChoice> choices = new ArrayList<>();

        for (int i = 0; i < chatGPTProperties.getRetries(); i++) {
            try {
                // avoid frequently request, random sleep 0.5s~1s
                if (i > 0) {
                    Thread.sleep(500 + RANDOM.nextInt(500));
                }
                choices = openAiService.createCompletion(completionRequest).getChoices();
                // if the last line code is correct, we can simply break the circle
                break;
            } catch (Exception e) {
                LOG.error("answer failed " + (i + 1) + " times, the error message is: " + e.getMessage());
                if (i == chatGPTProperties.getRetries() - 1) {
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
                    Thread.sleep(500 + RANDOM.nextInt(500));
                }
                imageList = openAiService.createImage(createImageRequest).getData();
                break;
            } catch (Exception e) {
                LOG.error("image generate failed " + (i + 1) + " times, the error message is: " + e.getMessage());
                if (i == chatGPTProperties.getRetries() - 1) {
                    throw new ChatGPTException(ChatGPTErrorEnum.FAILED_TO_GENERATE_IMAGE, e.getMessage());
                }
            }
        }
        return ImageResponseFormatEnum.URL.getResponseFormat().equals(createImageRequest.getResponseFormat()) ?
                imageList.stream().map(Image::getUrl).collect(Collectors.toList()) :
                imageList.stream().map(Image::getB64Json).collect(Collectors.toList());
    }
}
