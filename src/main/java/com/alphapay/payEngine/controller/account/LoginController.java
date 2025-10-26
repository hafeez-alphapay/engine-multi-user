package com.alphapay.payEngine.controller.account;

import com.alphapay.payEngine.account.management.dto.request.CompleteLoginRequest;
import com.alphapay.payEngine.account.management.dto.request.LoginRequest;
import com.alphapay.payEngine.account.management.dto.request.VerifySetupMFARequest;
import com.alphapay.payEngine.account.management.dto.response.LoginResponse;
import com.alphapay.payEngine.account.management.service.LoginService;
import com.alphapay.payEngine.account.management.service.TOTPService;
import com.alphapay.payEngine.model.response.BaseResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {


    Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    LoginService loginService;

    @Autowired
    TOTPService totpService;

    @PostMapping("/login")
    LoginResponse login(@RequestBody LoginRequest loginRequest) {
        logger.info("Login request received for user with email {}", loginRequest.getEmail());
        return loginService.login(loginRequest);
    }

    @PostMapping("/v2/login")
    LoginResponse loginV2(@RequestBody LoginRequest loginRequest) {
        logger.info("Login request received for user with email {}", loginRequest.getEmail());
        return loginService.loginWithTOTP(loginRequest);
    }

    @PostMapping("/v2/setupTOTPMFA")

    public BaseResponse setupTOTPMFA(@Valid @RequestBody VerifySetupMFARequest verifySetupMFARequest) {
        // This method can be used to handle TOTP MFA setup requests.
        // You can implement the logic to set up TOTP MFA here.
        return loginService.setupAndVerifyMFA(verifySetupMFARequest);
    }

    @PostMapping("/v2/loginCompletion")

    public LoginResponse completeLogin(@Valid @RequestBody CompleteLoginRequest completeLoginRequest) {
        // This method can be used to handle TOTP MFA setup requests.
        // You can implement the logic to set up TOTP MFA here.
        return loginService.completeLogin(completeLoginRequest);
    }

}
