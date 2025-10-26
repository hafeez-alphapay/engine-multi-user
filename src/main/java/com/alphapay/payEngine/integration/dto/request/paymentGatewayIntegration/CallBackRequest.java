package com.alphapay.payEngine.integration.dto.request.paymentGatewayIntegration;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CallBackRequest extends GatewayConfigurationBaseRequest {

    @NotEmpty
    private String callbackUrl;

}
