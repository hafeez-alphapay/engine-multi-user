package com.alphapay.payEngine.common.httpclient.service;


import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * - Supports both HTTP and HTTPS
 * - Uses a connection pool to re-use connections and save overhead of creating connections.
 * - Has a custom connection keep-alive strategy (to apply a default keep-alive if one isn't specified)
 * - Starts an idle connection monitor to continuously clean up stale connections.
 */
@Configuration
@EnableScheduling
@Slf4j
public class HttpClientConfig {

    // Determines the timeout in milliseconds until a connection is established.
    @Value("${payengine.connect.timeout}")
    private int CONNECT_TIMEOUT;
    @Value("${payengine.request.timeout}")
    private int REQUEST_TIMEOUT;
    @Value("${payengine.socket.timeout}")
    private  int SOCKET_TIMEOUT;
    @Value("${payengine.max.total.connections}")
    private  int MAX_TOTAL_CONNECTIONS;
    @Value("${payengine.idle.connections.wait}")
    private  int CLOSE_IDLE_CONNECTION_WAIT_TIME_SECS;

    private String EBS_KEYSTORE_PASS;
    @Primary
    @Bean(name="coreBankPoolingConnectionManager")
    public PoolingHttpClientConnectionManager poolingConnectionManager() {
        log.debug("Initiating HttpClientConfig with params {},{},{},{},{}",CONNECT_TIMEOUT,REQUEST_TIMEOUT,SOCKET_TIMEOUT
        ,MAX_TOTAL_CONNECTIONS,CLOSE_IDLE_CONNECTION_WAIT_TIME_SECS);
        SSLContextBuilder builder = new SSLContextBuilder();
        try {
            builder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            });
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            log.error("Pooling Connection Manager Initialisation failure because of " + e.getMessage(), e);
        }

        SSLConnectionSocketFactory sslsf = null;
        try {
            sslsf = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);

        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            log.error("Pooling Connection Manager Initialisation failure because of " + e.getMessage(), e);
        }

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslsf).register("http", new PlainConnectionSocketFactory()).build();

        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        poolingConnectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        return poolingConnectionManager;
    }




    @Primary
    @Bean(name="coreBankHttpClient")
    public CloseableHttpClient httpClient() {

        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(Timeout.ofMilliseconds(CONNECT_TIMEOUT)).setConnectionRequestTimeout(Timeout.ofMilliseconds(REQUEST_TIMEOUT)).setResponseTimeout(Timeout.ofMilliseconds(SOCKET_TIMEOUT)).build();
        return HttpClients.custom().setDefaultRequestConfig(requestConfig).setConnectionManager(poolingConnectionManager()).build();
    }


    @Bean
    public Runnable idleConnectionMonitor(final PoolingHttpClientConnectionManager connectionManager) {
        return new Runnable() {
            @Override
            @Scheduled(fixedDelay = 10000)
            public void run() {
                try {
                    if (connectionManager != null) {
                        //  log.trace("run IdleConnectionMonitor - Closing expired and idle connections...");
                        connectionManager.closeExpired();
                        connectionManager.closeIdle(TimeValue.ofSeconds(CLOSE_IDLE_CONNECTION_WAIT_TIME_SECS));
                    } else {
                        log.trace("run IdleConnectionMonitor - Http Client Connection manager is not initialised");
                    }
                } catch (Exception e) {
                    log.error("run IdleConnectionMonitor - Exception occurred. msg={}, e={}", e.getMessage(), e);
                }
            }
        };
    }

}

