package com.alphapay.payEngine.common.httpclient.service;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

/**
 * - Supports both HTTP and HTTPS
 * - Uses a connection pool to re-use connections and save overhead of creating connections.
 * - Has a custom connection keep-alive strategy (to apply a default keep-alive if one isn't specified)
 * - Starts an idle connection monitor to continuously clean up stale connections.
 */
@Configuration
public class RestClientConfig {

    private static final int CONNECT_TIMEOUT = 50000;  // 5 seconds
    private static final int REQUEST_TIMEOUT = 50000;  // 5 seconds
    private static final int SOCKET_TIMEOUT = 50000;   // 5 seconds

    @Bean
    public CloseableHttpClient httpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(CONNECT_TIMEOUT))
                .setResponseTimeout(Timeout.ofMilliseconds(SOCKET_TIMEOUT))
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(REQUEST_TIMEOUT))
                .build();

        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofSeconds(30))  // Cleanup stale connections
                .build();
    }

    @Primary
    @Bean
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient(httpClient());
        clientHttpRequestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        clientHttpRequestFactory.setReadTimeout(SOCKET_TIMEOUT);
        return clientHttpRequestFactory;
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("poolScheduler");
        scheduler.setPoolSize(50);
        return scheduler;
    }

    @Primary
    @Bean(name = "genericRestTemplate")
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
        return restTemplate;
    }
}