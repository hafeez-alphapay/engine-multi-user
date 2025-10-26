package com.alphapay.payEngine.account.management.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResetPasswordRequest extends BaseRequest {
    private String oldPassword;
    private String newPassword;
    private String resetPasswordId;
    private String email;
    private Long userId;
}
