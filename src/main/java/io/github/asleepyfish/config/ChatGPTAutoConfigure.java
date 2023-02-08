package io.github.asleepyfish.config;

import com.theokanning.openai.OpenAiService;
import io.github.asleepyfish.util.OpenAiUtils;
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
public class ChatGPTAutoConfigure {
    @Autowired
    private ChatGPTProperties properties;

    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(properties.getToken(), Duration.ZERO);
    }

    @Bean
    public OpenAiUtils openAiUtils(OpenAiService openAiService, ChatGPTProperties properties) {
        return new OpenAiUtils(openAiService, properties);
    }
}
