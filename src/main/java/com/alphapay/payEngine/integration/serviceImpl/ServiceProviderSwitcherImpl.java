package com.alphapay.payEngine.integration.serviceImpl;

import com.alphapay.payEngine.integration.dto.ProviderStatus;
import com.alphapay.payEngine.integration.model.MerchantPaymentProviderRegistration;
import com.alphapay.payEngine.integration.service.ServiceProviderSwitcher;
import com.alphapay.payEngine.utilities.CircuitBreakerState;
import com.mysql.cj.log.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ServiceProviderSwitcherImpl implements ServiceProviderSwitcher {
    @Autowired
    ProviderStatusService providerStatusService;

    @Override
    public MerchantPaymentProviderRegistration determineBestProvider(List<MerchantPaymentProviderRegistration> providers , List<MerchantPaymentProviderRegistration> staticPreferredProviders) {
        MerchantPaymentProviderRegistration bestProvider = null;
        double bestScore = Double.MIN_VALUE;

        boolean isAllHaveSameSuccessRate=true;



        for (MerchantPaymentProviderRegistration provider : providers) {
            ProviderStatus status = providerStatusService.getProviderStatus(provider.getServiceProvider().getId());
            if (status == null) {
                log.debug("No Status found for provider in CB with ID  {}",provider.getServiceProvider().getId());
                // No status found for this provider; skip or handle error
                isAllHaveSameSuccessRate=false;
                continue;
            }

            ProviderCircuitBreaker cb = status.getCircuitBreaker();
            // If the circuit is not available (i.e., fully OPEN and not done cooling off), skip it
            if (!cb.isAvailable()) {
                log.debug("CB is seems to be fully open for provider with id {}",provider.getServiceProvider().getId());
                isAllHaveSameSuccessRate=false;
                continue;
            }

            // For providers in CLOSED or HALF_OPEN, consider their success rate
            double successRate = status.getMetrics().getSuccessRate();
            if(successRate != 1.0)
            {
                isAllHaveSameSuccessRate=false;
                log.debug("Circuit Breaker has some values");

            }
            // You can use more advanced scoring here (e.g., combine success rate and latency)
            if (successRate > bestScore) {
                bestScore = successRate;
                bestProvider = provider;
            }
        }
        if(isAllHaveSameSuccessRate && staticPreferredProviders!=null && staticPreferredProviders.size()>0 )
        {
            //Fall back to static provider initially ...
            log.debug("Defaulting to prefered default provider with ID {}",staticPreferredProviders.get(0).getServiceProvider().getId());
            return staticPreferredProviders.get(0);
        }

        // If we still have null after iterating, it means no circuit was available
        // => fallback logic: forcibly "half-open" one or pick the soonest to recover
        if (bestProvider == null) {
            log.debug("Akk providers are open");
            bestProvider = handleAllOpenProviders(providers,staticPreferredProviders );
        }
        log.debug("Found the best provider is {}", bestProvider.getServiceProvider().getId());

        return bestProvider;
    }

    /**
     * Handle the scenario where all providers are OPEN.
     * A common approach:
     *  - Find the provider whose circuit is likely to recover the soonest
     *  - Forcibly move that circuit to HALF_OPEN to test it
     *
     *  If none can be forcibly tested, return null (or throw exception).
     */
    private MerchantPaymentProviderRegistration handleAllOpenProviders(List<MerchantPaymentProviderRegistration> providers,List<MerchantPaymentProviderRegistration> defaultProviders) {

        long earliestRecoveryTime = Long.MAX_VALUE;
        MerchantPaymentProviderRegistration fallbackProvider = null;
        if(defaultProviders!=null && defaultProviders.size()>0){
            fallbackProvider=defaultProviders.get(0);}
        else {

            for (MerchantPaymentProviderRegistration provider : providers) {
                ProviderStatus status = providerStatusService.getProviderStatus(provider.getId());
                if (status == null) continue;

                ProviderCircuitBreaker cb = status.getCircuitBreaker();
                // We're only dealing with providers in OPEN state here, presumably
                if (cb.getState() == CircuitBreakerState.OPEN) {
                    // Calculate next test time: (when did we open + openTimeout)
                    long nextTestTime = cb.getOpenTimestamp() + cb.getOpenTimeoutMillis();
                    if (nextTestTime < earliestRecoveryTime) {
                        earliestRecoveryTime = nextTestTime;
                        fallbackProvider = provider;
                    }
                }
            }
        }

        // If we found a provider, forcibly move it to HALF_OPEN so we can test it
        if (fallbackProvider != null) {
            ProviderStatus fallbackStatus = providerStatusService.getProviderStatus(fallbackProvider.getId());
            if (fallbackStatus != null) {
                ProviderCircuitBreaker cb = fallbackStatus.getCircuitBreaker();
                // forcibly half-open if you want to test it *now*
                cb.forceHalfOpen();
                return fallbackProvider;
            }
        }

        // If truly none can be tested, we return null or throw an exception
        return null;
    }
}
