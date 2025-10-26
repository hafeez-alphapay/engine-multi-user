package com.alphapay.payEngine.common.otp.exceptions;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class MinimumTimeToRegenerateIsNotPassedException extends BaseWebApplicationException {

    public MinimumTimeToRegenerateIsNotPassedException() {
        super(400, "7205", "ex.7205.otp.minimumTimeToRegenerate.notPassed", "minimum time to regenerate is not passed", "minimum time to regenerate is not passed");
    }
}
