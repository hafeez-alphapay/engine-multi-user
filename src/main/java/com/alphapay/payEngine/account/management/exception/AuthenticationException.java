package com.alphapay.payEngine.account.management.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class AuthenticationException extends BaseWebApplicationException {

    public AuthenticationException() {
        super(401, "7000", "ex.7000.authentication.error", "Authentication error", "Authentication error");
    }
    
    public AuthenticationException(String message) {
        super(401, "7000", "ex.7000.authentication.error", "Authentication error", message);
    }

    public AuthenticationException(String errorCode, String errorMessageKey) {
        super(401, errorCode, errorMessageKey, "Authentication error", null);
    }

    public AuthenticationException(String errorCode, String errorMessageKey, Object[] vars){
        super(401, errorCode, errorMessageKey, "Authentication error", null, vars);
    }
}
