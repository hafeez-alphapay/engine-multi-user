package com.alphapay.payEngine.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Profile(value="production")
@PropertySource({ "classpath:application-prod.properties" })
public class AppProdConfig {

}
