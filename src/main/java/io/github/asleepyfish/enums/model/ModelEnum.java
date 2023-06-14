package io.github.asleepyfish.enums.model;

import io.github.asleepyfish.enums.exception.ChatGPTErrorEnum;
import io.github.asleepyfish.exception.ChatGPTException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @Author: asleepyfish
 * @Date: 2023-02-08 22:19
 * @Description: ModelEnum
 */
@Getter
@AllArgsConstructor
public enum ModelEnum {

    /**
     * GPT4
     */
    GPT_4("gpt-4", 8192),

    /**
     * GPT4-0314
     */
    GPT_4_0314("gpt-4-0314", 8192),

    /**
     * GPT4-32k
     */
    GPT_4_32K("gpt-4-32k", 32768),

    /**
     * GPT4-32k-0314
     */
    GPT_4_32K_0314("gpt-4-32k-0314", 32768),

    /**
     * GPT-3.5-turbo-16k
     */
    GPT_35_TURBO_16K("gpt-3.5-turbo-16k", 16384),

    /**
     * GPT-3.5-turbo-0613
     */
    GPT_35_TURBO_0613("gpt-3.5-turbo-0613", 4096),

    /**
     * gpt-3.5-turbo-16k-0613
     */
    GPT_35_TURBO_16K_0613("gpt-3.5-turbo-16k-0613", 16384),

    /**
     * gpt-3.5-turbo
     */
    GPT_35_TURBO("gpt-3.5-turbo", 4096),

    /**
     * GPT-3.5-TURBO-0301
     */
    GPT_35_TURBO_0301("gpt-3.5-turbo-0301", 4096),

    /**
     * TEXT_DAVINCI_003
     */
    TEXT_DAVINCI_003("text-davinci-003", 4000),
    TEXT_CURIE_001("text-curie-001", 2048),
    TEXT_BABBAGE_001("text-babbage-001", 2048),
    TEXT_ADA_001("text-ada-001", 2048);
    private final String modelName;

    private final Integer maxTokens;

    public static Integer getMaxTokens(String modelName) {
        for (ModelEnum modelEnum : values()) {
            if (Objects.equals(modelEnum.getModelName(), modelName)) {
                return modelEnum.getMaxTokens();
            }
        }
        throw new ChatGPTException(ChatGPTErrorEnum.MODEL_SELECTION_ERROR);
    }
}
