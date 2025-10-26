package com.alphapay.payEngine.account.management.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class UserAccountOnboardingDisabledException extends BaseWebApplicationException {

    public UserAccountOnboardingDisabledException() {
        super(451, "7211", "ex.7211.user.account.onboarding.disabled", "Onboarding disabled", "Onboarding not allowed");
    }

    public UserAccountOnboardingDisabledException(String errorCode, String errorMessageKey) {
        super(451, errorCode, errorMessageKey, "Onboarding disabled", null);
    }

}
