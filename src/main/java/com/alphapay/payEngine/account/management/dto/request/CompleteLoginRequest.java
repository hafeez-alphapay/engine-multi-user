package com.alphapay.payEngine.account.management.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompleteLoginRequest extends BaseRequest {
    @NotNull
    private String initialLoginRequestId;
    @NotNull
    private String token;

    @Override
    public String toString() {
        return "CompleteLoginRequest{" +
                "initialLoginRequestId='" + initialLoginRequestId + '\'' +
                ", token='" + "******" + '\'' +
                '}';
    }
}
