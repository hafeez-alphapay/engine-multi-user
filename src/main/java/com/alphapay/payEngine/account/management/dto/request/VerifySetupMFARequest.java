package com.alphapay.payEngine.account.management.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerifySetupMFARequest extends BaseRequest {
    @NotNull
    private String initialLoginRequestId;
    @NotNull
    private String token;

    @Override
    public String toString() {
        return "VerifySetupMFARequest{" +
                "initialLoginRequestId='" + initialLoginRequestId + '\'' +
                ", token='" + "******" + '\'' +
                '}';
    }
}
