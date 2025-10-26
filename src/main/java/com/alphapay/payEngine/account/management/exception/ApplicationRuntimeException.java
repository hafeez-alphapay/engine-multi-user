package com.alphapay.payEngine.account.management.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class ApplicationRuntimeException extends BaseWebApplicationException {

    public ApplicationRuntimeException(int httpStatus, String errorCode, String errorMessageKey, String errorMessage, String applicationMessage) {
        super(httpStatus, errorCode, errorMessageKey, errorMessage, applicationMessage);
    }

    public ApplicationRuntimeException(String applicationMessage) {
        super(500, "5200", "ex.5200.internal.system.error", "Internal System Error", applicationMessage);
    }
}
