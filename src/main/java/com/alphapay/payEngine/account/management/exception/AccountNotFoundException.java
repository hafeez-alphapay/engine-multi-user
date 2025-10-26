package com.alphapay.payEngine.account.management.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class AccountNotFoundException extends BaseWebApplicationException {

    public AccountNotFoundException() {
        super(409, "1201", "ex.1201.account.notFound", "Invalid account", "Account not found");
    }

    public AccountNotFoundException(String errorCode, String errorMessageKey) {
        super(409, errorCode, errorMessageKey, "Invalid account", null);
    }

}
