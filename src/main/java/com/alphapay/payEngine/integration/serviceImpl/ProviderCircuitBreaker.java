package com.alphapay.payEngine.integration.serviceImpl;

import com.alphapay.payEngine.utilities.CircuitBreakerState;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * A provider-specific circuit breaker that controls how requests are routed based on recent failures.
 * <p>
 *     The circuit can be in one of three states:
 *     <ul>
 *         <li>{@link CircuitBreakerState#CLOSED}: Normal operation.</li>
 *         <li>{@link CircuitBreakerState#OPEN}: The provider has failed too frequently; requests are blocked until a timeout expires.</li>
 *         <li>{@link CircuitBreakerState#HALF_OPEN}: Testing state after a timeout. A single request is allowed; if it succeeds, the breaker closes; if it fails, it opens again.</li>
 *     </ul>
 * </p>
 * <p>
 *     This class is thread-safe: all mutating operations are synchronized, ensuring consistent state transitions
 *     even under concurrent usage.
 * </p>
 */
@Getter
@Setter
@Slf4j
public class ProviderCircuitBreaker {

    /**
     * The current state of this circuit breaker (CLOSED, OPEN, or HALF_OPEN).
     */
    private CircuitBreakerState state = CircuitBreakerState.CLOSED;

    /**
     * The timestamp (in millis) when the circuit last transitioned to OPEN.
     * Used to determine when it's permissible to move from OPEN to HALF_OPEN.
     */
    private long openTimestamp = 0L;

    /**
     * The current count of consecutive failures. When this reaches {@link #failureThreshold},
     * the circuit transitions to OPEN.
     */
    private int failureCount = 0;


    /**
     * Number of consecutive failures required to transition from CLOSED/HALF_OPEN to OPEN.
     * For example, if this is 5, the breaker opens after 5 consecutive failures.
     */
    private final int failureThreshold;

    /**
     * How long (in milliseconds) the circuit remains in OPEN state before it attempts to transition to HALF_OPEN.
     * For example, if this is 30,000 ms, the circuit stays OPEN for 30 seconds once triggered.
     */
    private final long openTimeoutMillis;

    /**
     * Reserved for future logic (e.g., controlling how frequently to test in HALF_OPEN).
     * In a basic implementation, you might not use this directly.
     */
    private final long halfOpenTestIntervalMillis;

    /**
     * Constructs a circuit breaker with thresholds and timeouts.
     *
     * @param failureThreshold         Number of consecutive failures to trigger OPEN state.
     * @param openTimeoutMillis        Time in milliseconds the circuit remains OPEN before moving to HALF_OPEN.
     * @param halfOpenTestIntervalMillis Time in milliseconds between test calls in HALF_OPEN (optional usage).
     */
    public ProviderCircuitBreaker(int failureThreshold, long openTimeoutMillis, long halfOpenTestIntervalMillis) {
        this.failureThreshold = failureThreshold;
        this.openTimeoutMillis = openTimeoutMillis;
        this.halfOpenTestIntervalMillis = halfOpenTestIntervalMillis;
    }

    /**
     * Indicates whether this circuit breaker is currently available for routing requests.
     * <p>
     *     If the state is {@link CircuitBreakerState#CLOSED} or {@link CircuitBreakerState#HALF_OPEN}, it returns true.
     *     If the state is {@link CircuitBreakerState#OPEN} but the timeout has elapsed, it transitions to HALF_OPEN
     *     and returns true. Otherwise, it remains false.
     * </p>
     *
     * @return {@code true} if requests can be routed; {@code false} if it is in OPEN state and not yet ready to test.
     */
    public synchronized boolean isAvailable() {
        if (state == CircuitBreakerState.OPEN) {
            long now = System.currentTimeMillis();
            // Check if we've exceeded the open timeout
            if ((now - openTimestamp) >= openTimeoutMillis) {
                CircuitBreakerState oldState = state;
                state = CircuitBreakerState.HALF_OPEN;
                failureCount = 0;
                log.info("Circuit breaker transitioning from {} to HALF_OPEN (timeout elapsed).", oldState);
            } else {
                // Still OPEN, not yet time to go HALF_OPEN
                return false;
            }
        }
        // If CLOSED or now HALF_OPEN, it's considered "available" for a test request
        return (state == CircuitBreakerState.CLOSED || state == CircuitBreakerState.HALF_OPEN);
    }

    /**
     * Records a successful request to the provider. If the circuit is in HALF_OPEN, a success triggers
     * a transition to CLOSED. Consecutive failure count is reset.
     */
    public synchronized void recordSuccess() {
        if (state == CircuitBreakerState.HALF_OPEN) {
            CircuitBreakerState oldState = state;
            state = CircuitBreakerState.CLOSED;
            failureCount = 0;
            log.info("Circuit breaker transitioning from {} to CLOSED (success in HALF_OPEN).", oldState);
        }
        // If already CLOSED, just ignore (but the success may be relevant to other metrics).
    }

    /**
     * Records a failed request to the provider. If the consecutive failure count exceeds {@link #failureThreshold}
     * and the circuit is in CLOSED or HALF_OPEN, it transitions to OPEN.
     */
    public synchronized void recordFailure() {
        failureCount++;
        if ((state == CircuitBreakerState.CLOSED || state == CircuitBreakerState.HALF_OPEN)
                && failureCount >= failureThreshold) {

            CircuitBreakerState oldState = state;
            state = CircuitBreakerState.OPEN;
            openTimestamp = System.currentTimeMillis();
            log.warn("Circuit breaker transitioning from {} to OPEN ({} consecutive failures).",
                    oldState, failureCount);
        }
    }

    /**
     * Force the circuit breaker into HALF_OPEN state, typically used in fallback logic when all providers
     * are in OPEN state and you want to test one immediately.
     * <p>
     *     This also resets {@link #failureCount} to 0.
     * </p>
     */
    public synchronized void forceHalfOpen() {
        if (state == CircuitBreakerState.OPEN) {
            CircuitBreakerState oldState = state;
            state = CircuitBreakerState.HALF_OPEN;
            failureCount = 0;
            log.info("Circuit breaker forcibly transitioning from {} to HALF_OPEN.", oldState);
        }
    }
}
