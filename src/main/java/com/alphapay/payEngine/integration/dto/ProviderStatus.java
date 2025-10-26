package com.alphapay.payEngine.integration.dto;

import com.alphapay.payEngine.integration.serviceImpl.ProviderCircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProviderStatus {
    private final ProviderCircuitBreaker circuitBreaker;
    private final TimeWindowMetrics metrics;

}
