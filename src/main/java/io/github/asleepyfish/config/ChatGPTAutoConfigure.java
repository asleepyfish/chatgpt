package io.github.asleepyfish.config;

import io.github.asleepyfish.service.OpenAiProxyService;
import io.github.asleepyfish.util.OpenAiUtils;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
    @ConditionalOnMissingBean(OkHttpClient.class)
    public OkHttpClient okHttpClient() {
        return OpenAiProxyService.defaultClient(properties, Duration.ZERO);
    }

    @Bean
    public OpenAiProxyService openAiProxyService(OkHttpClient okHttpClient) {
        return new OpenAiProxyService(properties, okHttpClient);
    }

    @Bean
    @ConditionalOnBean(OpenAiProxyService.class)
    public OpenAiUtils openAiUtils(OpenAiProxyService openAiProxyService) {
        return new OpenAiUtils(openAiProxyService);
    }
}
