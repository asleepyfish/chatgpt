package io.github.asleepyfish.service.openai;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;

/**
 * @Author: asleepyfish
 * @Date: 2023/7/4 14:24
 * @Description: OkHttp Interceptor that adds an authorization token header
 */
@Slf4j
public class AuthenticationInterceptor implements Interceptor {

    private final String token;

    public AuthenticationInterceptor(String token) {
        Objects.requireNonNull(token, "OpenAI token required");
        this.token = token;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request()
                .newBuilder()
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .build();
        Response response = chain.proceed(request);
        if (!response.isSuccessful() && response.body() != null) {
            String errorMsg = response.body().string();
            log.error("OpenAI request error: {}", errorMsg);

        }
        return response;
    }
}
