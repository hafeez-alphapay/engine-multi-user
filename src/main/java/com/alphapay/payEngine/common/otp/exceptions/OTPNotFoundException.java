package com.alphapay.payEngine.common.otp.exceptions;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class OTPNotFoundException extends BaseWebApplicationException {
    public OTPNotFoundException() {
        super(400, "1205", "ex.1205.otp.not.found", "No request initiated with requestId", "No request initiated with requestId");
    }
}
