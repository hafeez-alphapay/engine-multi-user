package com.alphapay.payEngine.account.management.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class AccountDisabledException extends BaseWebApplicationException {

    public AccountDisabledException() {
        super(403, "7204", "ex.7204.account.disabled", "Account disabled", "Account is disabled");
    }
}
