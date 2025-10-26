package com.alphapay.payEngine.account.management.service;

import com.alphapay.payEngine.account.management.dto.request.CompleteLoginRequest;
import com.alphapay.payEngine.account.management.dto.request.LoginRequest;
import com.alphapay.payEngine.account.management.dto.request.VerifySetupMFARequest;
import com.alphapay.payEngine.account.management.dto.response.LoginResponse;
import com.alphapay.payEngine.model.response.BaseResponse;

public interface LoginService {
    LoginResponse login(LoginRequest loginRequest);

    LoginResponse loginWithTOTP(LoginRequest loginRequest) ;

    BaseResponse setupAndVerifyMFA(VerifySetupMFARequest verifySetupMFARequest);

    LoginResponse completeLogin(CompleteLoginRequest completeLoginRequest);
}
