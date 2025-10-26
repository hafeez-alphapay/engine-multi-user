package com.alphapay.payEngine.account.management.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class AccountLockedException extends BaseWebApplicationException {

    public AccountLockedException() {
        super(423, "7202", "ex.7202.account.locked", "Account locked", "Account is locked");
    }
}
