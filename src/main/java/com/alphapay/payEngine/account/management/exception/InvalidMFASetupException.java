package com.alphapay.payEngine.account.management.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class InvalidMFASetupException extends BaseWebApplicationException {

    public InvalidMFASetupException() {
        super(409, "7225", "ex.7225.invalid.mfa.setup", "Invalid Token Setup - Login Transaction ID Not In valid status", "Invalid Token Setup - Login Transaction ID Not In valid status");
    }

    public InvalidMFASetupException(String errorCode, String errorMessageKey) {
        super(409, errorCode, errorMessageKey, "Invalid Token Setup - Login Transaction ID Not In valid status", null);
    }

}
