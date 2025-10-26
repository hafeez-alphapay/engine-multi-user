package com.alphapay.payEngine.account.management.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class PasswordUsedException extends BaseWebApplicationException {

    public PasswordUsedException() {
        super(401, "7225", "ex.7225.history.password.match", "Password already used before, please use another one");
    }
}
