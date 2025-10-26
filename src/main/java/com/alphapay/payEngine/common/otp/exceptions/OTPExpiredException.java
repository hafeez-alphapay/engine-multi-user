package com.alphapay.payEngine.common.otp.exceptions;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class OTPExpiredException extends BaseWebApplicationException {
    public OTPExpiredException() {
        super(400, "1204", "ex.1204.otpDetails.expired", "Otp Expired or exceeded attempts", "Otp Expired or exceeded attempts");
    }
}
