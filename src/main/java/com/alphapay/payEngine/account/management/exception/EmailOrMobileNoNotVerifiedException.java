package com.alphapay.payEngine.account.management.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class EmailOrMobileNoNotVerifiedException extends BaseWebApplicationException {

    public EmailOrMobileNoNotVerifiedException() {
        super(409, "7215", "ex.7215.email.mobileno.not.verified", "You Mobile No or Email Not verified yet ", "Mobile or email not verified");
    }
}
