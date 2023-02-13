package io.github.asleepyfish.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Author: asleepyfish
 * @Date: 2023-02-07 22:15
 * @Description: ChatGPT request parameters
 */
@Data
@ConfigurationProperties(prefix = "chatgpt")
public class ChatGPTProperties {
    /**
     * OpenAi token string "sk-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
     */
    private String token;

    /**
     * The name of the model to use.
     * Required if specifying a fine tuned model or if using the new v1/completions endpoint.
     */
    private String model = "text-davinci-003";

    /**
     * Timeout retries
     */
    private int retries = 5;
}
