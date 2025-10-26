package com.alphapay.payEngine.controller.account;

import com.alphapay.payEngine.account.management.dto.request.ResetPasswordRequest;
import com.alphapay.payEngine.account.management.dto.response.ForgotPasswordResponse;
import com.alphapay.payEngine.account.management.dto.response.ResetPasswordResponse;
import com.alphapay.payEngine.account.management.service.ResetPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/merchant")
public class ResetPasswordController {

    @Autowired
    private ResetPasswordService resetPasswordService;

    @PostMapping("/adminResetPassword")
    ResetPasswordResponse adminResetPassword(@RequestBody ResetPasswordRequest request) {
        return resetPasswordService.resetPasswordWithRandom(request);
    }

    @PostMapping("/settingResetPassword")
    ResetPasswordResponse settingResetPassword(@RequestBody ResetPasswordRequest request) {
        return resetPasswordService.settingResetPassword(request);
    }

    @PostMapping("/forgotPassword")
    ForgotPasswordResponse forgetPassword(@RequestBody ResetPasswordRequest request) {
        return resetPasswordService.forgotPassword(request);
    }

    @PostMapping("/emailResetPassword")
    ResetPasswordResponse emailResetPassword(@RequestBody ResetPasswordRequest request) {
        return resetPasswordService.emailResetPassword(request);
    }
}
