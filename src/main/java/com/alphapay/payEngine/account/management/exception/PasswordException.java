package com.alphapay.payEngine.account.management.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class PasswordException extends BaseWebApplicationException {

    public PasswordException() {
        super(401, "7201", "ex.7201.password.mismatch", "Wrong Password", "Wrong Password");
    }
}
