package io.github.asleepyfish.config;

import io.github.asleepyfish.enums.model.ModelEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Author: asleepyfish
 * @Date: 2023-02-07 22:15
 * @Description: ChatGPT request parameters
 */
@Data
@ConfigurationProperties(prefix = "chatgpt")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGPTProperties {
    /**
     * OpenAi token string "sk-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
     */
    private String token;

    /**
     * The name of the model to use.
     * Required if specifying a fine-tuned model or if using the new v1/completions endpoint.
     */
    @Builder.Default
    private String model = ModelEnum.TEXT_DAVINCI_003.getModelName();

    /**
     * chatModel which use by createChatCompletion
     */
    @Builder.Default
    private String chatModel = ModelEnum.GPT_35_TURBO.getModelName();

    /**
     * Timeout retries
     */
    @Builder.Default
    private int retries = 5;

    /**
     * proxyHost
     */
    private String proxyHost;

    /**
     * proxyPort
     */
    private int proxyPort;

    /**
     * sessionExpirationTime
     */
    private Integer sessionExpirationTime;

    /**
     * baseUrl
     */
    @Builder.Default
    private String baseUrl = "https://api.openai.com/";
}
