package com.alphapay.payEngine.integration.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class AmountIsNotValidException extends BaseWebApplicationException {
    public AmountIsNotValidException(String applicationMessage) {
        super(
                401, "105", "ex.105.refund.amount.not.valid", applicationMessage,applicationMessage
        );
    }
}
