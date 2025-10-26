package com.alphapay.payEngine.integration.serviceImpl;

import com.alphapay.payEngine.integration.dto.ProviderStatus;
import com.alphapay.payEngine.integration.dto.TimeWindowMetrics;
import com.alphapay.payEngine.integration.model.orchast.ServiceProvider;
import com.alphapay.payEngine.integration.repository.ServiceProviderRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ProviderStatusService {

    @Autowired
    ServiceProviderRepository serviceProviderRepository;

    /**
     * Number of consecutive failures required to transition from CLOSED/HALF_OPEN to OPEN.
     * For example, if this is 5, the breaker opens after 5 consecutive failures.
     */
    @Value("${provider.circuit.breaker.failure-threshold}")
    private int failureThreshold;
    /**
     * How long (in milliseconds) the circuit remains in OPEN state before it attempts to transition to HALF_OPEN.
     * For example, if this is 30,000 ms, the circuit stays OPEN for 30 seconds once triggered.
     */
    @Value("${provider.circuit.breaker.open-timeout-millis}")
    private long openTimeoutMillis;
    /**
     * Reserved for future logic (e.g., controlling how frequently to test in HALF_OPEN).
     * In a basic implementation, you might not use this directly.
     */
    @Value("${provider.circuit.breaker.half-open-test-interval-millis}")
    private long halfOpenTestIntervalMillis;


    private final Map<Long, ProviderStatus> providerStatusMap = new ConcurrentHashMap<>();

    /**
     * Initialize the status for each provider. In a real application,
     * you might load these settings from configuration or DB.
     */
    @PostConstruct
    private void initProviderStatuses() {

        List<ServiceProvider> providers=serviceProviderRepository.findAll();
        if(providers==null || providers.isEmpty()){
            log.debug("No providers found in the database.");
            return;
        }
        for (ServiceProvider provider : providers) {
            long providerId = provider.getId();
            // Assuming you have a way to get the threshold and window size from the provider object

            ProviderStatus providerStatus = new ProviderStatus(
                    new ProviderCircuitBreaker(failureThreshold, openTimeoutMillis, halfOpenTestIntervalMillis),
                    new TimeWindowMetrics(openTimeoutMillis)
            );
            providerStatusMap.put(providerId, providerStatus);
        }
    }

    public ProviderStatus getProviderStatus(long providerId) {
        return providerStatusMap.get(providerId);
    }

    public Collection<ProviderStatus> getAllProviderStatuses() {
        return providerStatusMap.values();
    }

    // If you need to add or remove providers dynamically:
    public void addProviderStatus(Long providerId, ProviderStatus status) {
        providerStatusMap.put(providerId, status);
    }

    public void removeProviderStatus(String providerId) {
        providerStatusMap.remove(providerId);
    }
}
