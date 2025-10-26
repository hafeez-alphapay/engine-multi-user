package com.alphapay.payEngine.common.otp.exceptions;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class InvalidOTPException extends BaseWebApplicationException {

    public InvalidOTPException() {
        super(400, "4201", "ex.4210.otpDetails.validation.failed", "Otp Validation Failed", "Otp Validation Failed");
    }
}
