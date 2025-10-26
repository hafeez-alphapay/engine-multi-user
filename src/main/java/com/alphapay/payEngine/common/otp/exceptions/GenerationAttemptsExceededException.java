package com.alphapay.payEngine.common.otp.exceptions;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class GenerationAttemptsExceededException extends BaseWebApplicationException {
    public GenerationAttemptsExceededException() {
        super(400, "7206", "ex.7206.otpDetails.max.regeneration.reached", "Otp generation attempts exceeded", "Otp generation attempts exceeded");
    }
}
