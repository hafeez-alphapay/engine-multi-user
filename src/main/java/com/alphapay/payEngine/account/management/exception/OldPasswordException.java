package com.alphapay.payEngine.account.management.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class OldPasswordException extends BaseWebApplicationException {

    public OldPasswordException() {
        super(401, "7222", "ex.7222.old.password.mismatchh", "Old password is wrong", "Old password is wrong");
    }
}
