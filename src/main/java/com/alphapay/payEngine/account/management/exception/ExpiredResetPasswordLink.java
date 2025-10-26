package com.alphapay.payEngine.account.management.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class ExpiredResetPasswordLink extends BaseWebApplicationException {

    public ExpiredResetPasswordLink() {
        super(403, "7223", "ex.7223.reset.password.link.expired", "Your password reset link has expired.", "Your password reset link has expired.");
    }
}