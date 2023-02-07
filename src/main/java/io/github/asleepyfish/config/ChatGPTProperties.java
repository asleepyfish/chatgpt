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
     * Timeout time (unit: s)
     * http read timeout in seconds, 0 means no timeout
     */
    private Long timeout;
}
