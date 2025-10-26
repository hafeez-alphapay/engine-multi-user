package com.alphapay.payEngine.cache.config;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayEngineCachingConfiguration extends CachingConfigurerSupport {
    @Override
    public CacheErrorHandler errorHandler() {
        return new PayEnginesCacheErrorHandler();
    }
}