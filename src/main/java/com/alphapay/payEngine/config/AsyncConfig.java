package com.alphapay.payEngine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

// @Configuration
// Put in a config package
@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "webhookExecutor")
    public Executor webhookExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setThreadNamePrefix("webhook-");
        ex.setCorePoolSize(4);
        ex.setMaxPoolSize(16);
        ex.setQueueCapacity(500);
        ex.initialize();
        return ex;
    }
}

