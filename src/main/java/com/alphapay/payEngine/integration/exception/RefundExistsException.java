package com.alphapay.payEngine.integration.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class RefundExistsException extends BaseWebApplicationException {
    public RefundExistsException() {
        super(
                409,
                "7221",
                "ex.7221.refund.exists",
                "The refund request already exists, you have created it before.",
                "The refund request already exists, you have created it before."
        );


    }
}
