package com.alphapay.payEngine.account.management.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class AuthorizationException extends BaseWebApplicationException {

    public AuthorizationException(String applicationMessage) {
        super(403, "7200", "ex.7200.forbidden", "Forbidden", applicationMessage);
    }

}
