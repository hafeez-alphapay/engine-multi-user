package com.alphapay.payEngine.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Profile(value="staging")
@PropertySource({ "classpath:application-staging.properties" })
public class AppStagingConfig {

}
