package com.alphapay.payEngine.integration.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class PaymentStepMissingException extends BaseWebApplicationException {

    public PaymentStepMissingException() {
        super(
                404,
                "7219",
                "ex.7219.payment.step.missing",
                "Previous payment step is missing please start again",
                "payment step is missing"
        );


    }
}
