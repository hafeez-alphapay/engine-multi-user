package com.alphapay.payEngine.integration.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterMerchantGatewayRequest extends BaseRequest {
    @NotNull
    private Long merchantId;

    @NotEmpty
    private String serviceId;

    @NotEmpty
    private String callbackUrl;

    private String webhookUrl;
}
