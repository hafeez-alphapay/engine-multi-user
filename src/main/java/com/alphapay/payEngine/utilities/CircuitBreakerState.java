package com.alphapay.payEngine.utilities;

public enum CircuitBreakerState {
    CLOSED,     // Normal operation
    OPEN,       // Provider fails too frequently; do not call
    HALF_OPEN   // Testing provider to see if it has recovered
}