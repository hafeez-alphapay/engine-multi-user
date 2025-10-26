package com.alphapay.payEngine.account.management.service;

import com.alphapay.payEngine.account.management.dto.request.ResetPasswordRequest;
import com.alphapay.payEngine.account.management.dto.response.ForgotPasswordResponse;
import com.alphapay.payEngine.account.management.dto.response.ResetPasswordResponse;

public interface ResetPasswordService {
    ResetPasswordResponse resetPasswordWithRandom(ResetPasswordRequest loginRequest);

    ResetPasswordResponse settingResetPassword(ResetPasswordRequest request);

    ForgotPasswordResponse forgotPassword(ResetPasswordRequest request);

    ResetPasswordResponse emailResetPassword(ResetPasswordRequest request);
}
