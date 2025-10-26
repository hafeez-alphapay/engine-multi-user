package com.alphapay.payEngine.integration.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

/**
 * Thrown when the provided payment method is not valid for the current merchant or step.
 */
public class PaymentMethodNotValidException extends BaseWebApplicationException {

    public PaymentMethodNotValidException() {
        super(
                400,
                "7224",
                "ex.7224.payment.method.invalid",
                "The selected payment method is invalid or not allowed. Please choose a valid one.",
                "Invalid or unauthorized payment method."
        );
    }
}