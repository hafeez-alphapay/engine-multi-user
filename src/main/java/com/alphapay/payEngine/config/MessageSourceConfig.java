package com.alphapay.payEngine.config;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageSourceConfig {

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("i18n/messages", "i18n/notifications");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}