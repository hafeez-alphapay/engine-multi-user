package com.alphapay.payEngine.config;

import org.springframework.context.annotation.*;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@ComponentScan(basePackages = {"com.alphapay.payEngine"}, excludeFilters = {
        @ComponentScan.Filter(value = Controller.class, type = FilterType.ANNOTATION),
        @ComponentScan.Filter(value = Configuration.class, type = FilterType.ANNOTATION),
        @ComponentScan.Filter(value = EnableWebMvc.class, type = FilterType.ANNOTATION)})
@EnableAsync
@EnableSpringConfigured
@EnableRetry
@PropertySource(value = {"classpath:application.properties"})
public class AppConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
