package com.alphapay.payEngine.account.management.dto.response;

import com.alphapay.payEngine.common.otp.models.TOTPKey;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class LoginResponse {
    private String email;
    private Map<String, String> userData;
    private List<String> permissions;
    private List<String> roles;
    private TOTPKey totpKey;
    private String Status;
    private Long userId;
    private String requestId;
}
