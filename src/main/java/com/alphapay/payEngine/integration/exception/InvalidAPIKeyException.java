package com.alphapay.payEngine.integration.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class InvalidAPIKeyException extends BaseWebApplicationException {

    public InvalidAPIKeyException() {
        super(
                400,
                "7511",
                "ex.7511.invalid.api.key",
                "Invalid Api Key",
                "Invalid Api Key"
        );
    }
}
