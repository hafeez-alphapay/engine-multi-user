package com.alphapay.payEngine.integration.dto.response.paymentGatewayIntegration;

import com.alphapay.payEngine.model.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class MerchantGatewayConfigurationResponse extends BaseResponse {
    private String apiKey;
    private String webhookUrl;
    private String webhookSecretKey;
    private String callbackUrl;
    private LocalDateTime expirationDate;
}
