package io.github.asleepyfish.service.openai;

import com.alibaba.fastjson2.JSONObject;
import io.github.asleepyfish.config.ChatGPTProperties;
import io.github.asleepyfish.enums.exception.ChatGPTErrorEnum;
import io.github.asleepyfish.exception.ChatGPTException;
import io.github.asleepyfish.strategy.DefaultTokenStrategy;
import io.github.asleepyfish.strategy.RandomTokenStrategy;
import io.github.asleepyfish.strategy.TokenStrategy;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @Author: asleepyfish
 * @Date: 2023/7/4 14:24
 * @Description: OkHttp Interceptor that adds an authorization token header
 */
@Slf4j
public class AuthenticationInterceptor implements Interceptor {

    private final List<String> tokens;

    private final TokenStrategy tokenStrategy;

    public AuthenticationInterceptor(String token) {
        Objects.requireNonNull(token, "OpenAI token required");
        this.tokens = Collections.synchronizedList(Collections.singletonList(token));
        this.tokenStrategy = new DefaultTokenStrategy();
    }

    public AuthenticationInterceptor(ChatGPTProperties properties) {
        TokenStrategy strategy = new RandomTokenStrategy();
        Class<? extends TokenStrategy> strategyClass = properties.getTokenStrategyImpl();
        if (strategyClass != null) {
            try {
                strategy = strategyClass.newInstance();
            } catch (Exception e) {
                log.error("Failed to instantiate token strategy class: {}", strategyClass, e);
            }
        }
        this.tokenStrategy = strategy;
        List<String> tokens = Collections.synchronizedList(new ArrayList<>());
        String token = properties.getToken();
        List<String> alterTokens = properties.getAlterTokens();
        Objects.requireNonNull(token, "OpenAI token required");
        tokens.add(token);
        if (!CollectionUtils.isEmpty(alterTokens)) {
            tokens.addAll(alterTokens);
        }
        this.tokens = tokens;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = tokenStrategy.getToken(tokens);

        Request request = chain.request()
                .newBuilder()
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .build();
        Response response = chain.proceed(request);

        if (!response.isSuccessful() && response.body() != null) {
            String errorMsg = response.body().string();
            log.error("OpenAI request error: {}", errorMsg);
            String code = JSONObject.parseObject(errorMsg).getJSONObject("error").getString("code");
            String message = JSONObject.parseObject(errorMsg).getJSONObject("error").getString("message");
            if ("account_deactivated".equals(code) || "invalid_api_key".equals(code)) {
                tokens.removeIf(t -> t.equals(token));
                log.error("OpenAI token {} is invalid, removed from token list", token);
                throw new ChatGPTException(ChatGPTErrorEnum.TOKEN_IS_INVALID_ERROR, message);
            }
        }
        return response;
    }
}
