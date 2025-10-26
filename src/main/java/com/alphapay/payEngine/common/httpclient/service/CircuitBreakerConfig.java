package com.alphapay.payEngine.common.httpclient.service;


import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;
/*
    SPRING CIRCUIT BREAKER CONFIGURATION FOR ALL EXTERNAL REST CALLS.
    EACH ENTITY (INTEGRATION) SHOULD HAVE ITS OWN CircuitBreakerConfig WHICH TRACKS FALIURE
    IF FALIURE EXCEEDS THREASHOLD CIRCUIT WILL BE HALF OPEN THEN OPEN FOR SPECIFIC WIAT
    IN TIME
    ALL PARAMETERS ARE INJECTED
 */
@Configuration
@Slf4j
public class CircuitBreakerConfig {
    @Value("${circuit.bk.sliding.core.size}")
    int CORE_CIRCUIT_SLIDINGW_SIZE;
    @Value("${circuit.core.open.wait}")
    int CORE_WAIT_IN_OPEN;
    @Value("${circuit.core.min.calls}")
    int CORE_MIN_NO_CALLS;
    @Value("${circuit.core.failure.threshold}")
    float CORE_FALIURE_THREASHOLD;



    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        log.debug("Initializing Circuit Breakers - paymentCircuitBreaker ", CORE_CIRCUIT_SLIDINGW_SIZE, CORE_WAIT_IN_OPEN, CORE_MIN_NO_CALLS, CORE_FALIURE_THREASHOLD);

        io.github.resilience4j.circuitbreaker.CircuitBreakerConfig paymentCircuitBreakerConfig = io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .slidingWindowSize(CORE_CIRCUIT_SLIDINGW_SIZE)
                .slidingWindowType(COUNT_BASED)
                .waitDurationInOpenState(Duration.ofSeconds(CORE_WAIT_IN_OPEN))
                .minimumNumberOfCalls(CORE_MIN_NO_CALLS)
                .failureRateThreshold(CORE_FALIURE_THREASHOLD)
                .build();

        Map cicuitBreakerMap = Map.of(  "paymentCircuitBreaker", paymentCircuitBreakerConfig );
        return CircuitBreakerRegistry.of(cicuitBreakerMap);
    }
}