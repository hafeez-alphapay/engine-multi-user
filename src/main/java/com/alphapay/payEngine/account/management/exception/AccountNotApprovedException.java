package com.alphapay.payEngine.account.management.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class AccountNotApprovedException extends BaseWebApplicationException {

    public AccountNotApprovedException() {
        super(423, "7218", "ex.7218.account.not.approved", "Account Not Approved", "Account doesn't approved yet please wait until approved");
    }
}