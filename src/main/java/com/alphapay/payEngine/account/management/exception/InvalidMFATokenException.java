package com.alphapay.payEngine.account.management.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class InvalidMFATokenException extends BaseWebApplicationException {

    public InvalidMFATokenException() {
        super(409, "7226", "ex.7226.invalid.mfa.code", "Invalid Token", "Invalid Token");
    }

    public InvalidMFATokenException(String errorCode, String errorMessageKey) {
        super(409, errorCode, errorMessageKey, "Invalid Token", null);
    }

}
