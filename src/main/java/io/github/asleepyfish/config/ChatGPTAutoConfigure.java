package io.github.asleepyfish.config;

import com.theokanning.openai.OpenAiService;
import io.github.asleepyfish.service.OpenAiUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @Author: asleepyfish
 * @Date: 2023-02-07 23:48
 * @Description: support for springboot
 */
@Configuration
@EnableConfigurationProperties(ChatGPTProperties.class)
@ConditionalOnProperty(prefix = "chatgpt", value = "enable", havingValue = "true")
public class ChatGPTAutoConfigure {
    @Autowired
    private ChatGPTProperties properties;

    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(properties.getToken(), Duration.ofSeconds(properties.getTimeout()));
    }

    @Bean
    public OpenAiUtils openAiUtils(OpenAiService openAiService) {
        return new OpenAiUtils(openAiService);
    }
}
