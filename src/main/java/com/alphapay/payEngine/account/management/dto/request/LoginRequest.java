package com.alphapay.payEngine.account.management.dto.request;

import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.service.bean.BaseRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest extends BaseRequest {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    UserEntity user;

    private String email;

    private String password;

    private String pushNotificationId;

    @Override
    public String toString() {
        return "LoginRequest{" +
                ", email='" + email + '\'' +
                ", password='" + "******" + '\'' +
                ", auditInfo=" + getAuditInfo() +
                ", pushNotificationId=" + pushNotificationId +
                '}';
    }
}
