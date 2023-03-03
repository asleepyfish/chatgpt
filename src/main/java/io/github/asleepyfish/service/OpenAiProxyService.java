package io.github.asleepyfish.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.service.OpenAiService;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;

/**
 * @Author: asleepyfish
 * @Date: 2023/3/3 14:00
 * @Description: OpenAiProxyService
 */
public class OpenAiProxyService extends OpenAiService {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10L);

    public OpenAiProxyService(String token, String proxyHost, int proxyPort) {
        this(token, DEFAULT_TIMEOUT, proxyHost, proxyPort);
    }

    public OpenAiProxyService(String token, Duration timeout, String proxyHost, int proxyPort) {
        this(buildApi(token, timeout, proxyHost, proxyPort));
    }

    public OpenAiProxyService(OpenAiApi api) {
        super(api);
    }

    public static OpenAiApi buildApi(String token, Duration timeout, String proxyHost, int proxyPort) {
        ObjectMapper mapper = defaultObjectMapper();
        // Create proxy object
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        OkHttpClient client = defaultClient(token, timeout).newBuilder().proxy(proxy).build();
        Retrofit retrofit = defaultRetrofit(client, mapper);
        return retrofit.create(OpenAiApi.class);
    }
}
