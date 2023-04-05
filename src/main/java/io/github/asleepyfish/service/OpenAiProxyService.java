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
    public OpenAiProxyService(String token, Duration timeout, String proxyHost, int proxyPort) {
        super(buildApi(token, timeout, proxyHost, proxyPort), defaultClient(token, timeout, proxyHost, proxyPort).dispatcher().executorService());
    }

    public static OpenAiApi buildApi(String token, Duration timeout, String proxyHost, int proxyPort) {
        ObjectMapper mapper = defaultObjectMapper();
        OkHttpClient client = defaultClient(token, timeout, proxyHost, proxyPort);
        Retrofit retrofit = defaultRetrofit(client, mapper);
        return retrofit.create(OpenAiApi.class);
    }

    public static OkHttpClient defaultClient(String token, Duration timeout, String proxyHost, int proxyPort) {
        // Create proxy object
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        return OpenAiService.defaultClient(token, timeout).newBuilder().proxy(proxy).build();
    }
}
