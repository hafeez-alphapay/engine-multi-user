package com.alphapay.payEngine.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Profile(value={"local","default"})
@PropertySource({ "classpath:application-local.properties" })
public class AppLocalConfig {

}
