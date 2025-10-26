package com.alphapay.payEngine.common.otp.services;

import com.alphapay.payEngine.common.otp.exceptions.GenerationAttemptsExceededException;
import com.alphapay.payEngine.common.otp.exceptions.MinimumTimeToRegenerateIsNotPassedException;

public interface IOtpService {
    String generateOTP(String requestId, String tranType, String cif) throws GenerationAttemptsExceededException, MinimumTimeToRegenerateIsNotPassedException;
    int validate(String requestId, String OtpValue);
    int validate(String requestId, String OtpValue,String transType);
}
