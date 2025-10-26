package com.alphapay.payEngine.integration.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class ServiceNotAllowedToMerchantException extends BaseWebApplicationException {

    public ServiceNotAllowedToMerchantException() {
        super(
                400,
                "7511",
                "ex.7511.service.not.allowed",
                "Service not allowed for this merchant",
                "Service not allowed for this merchant"
        );
    }
}
