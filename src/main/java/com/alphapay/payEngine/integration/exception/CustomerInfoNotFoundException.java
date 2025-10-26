package com.alphapay.payEngine.integration.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class CustomerInfoNotFoundException extends BaseWebApplicationException {
    public CustomerInfoNotFoundException() {
        super(
                404,
                "7217",
                "ex.7217.customer.info.not.found",
                "Customer info not found.",
                "Customer info not found"
        );
    }
}