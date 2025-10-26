package com.alphapay.payEngine.account.management.service;

import com.alphapay.payEngine.account.management.dto.request.LoginRequest;
import com.alphapay.payEngine.account.management.dto.response.LoginResponse;

public interface InitializUserDataService {
    LoginResponse getInitializationData(LoginRequest loginRequest);
}
