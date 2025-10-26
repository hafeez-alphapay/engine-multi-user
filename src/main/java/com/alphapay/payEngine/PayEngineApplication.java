package com.alphapay.payEngine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PayEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(PayEngineApplication.class, args);
	}

}
