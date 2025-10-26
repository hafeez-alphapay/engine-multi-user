package com.alphapay.payEngine.account.management.dto.response;


import com.alphapay.payEngine.alphaServices.model.MerchantServiceConfigEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
public class MerchantServiceConfigDTO  {
    //private Long merchantId;
    private String webhookUrl;
    private String webhookSecretKey;
    //private String apiKey;
    private String callbackUrl;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime expirationDate;


    public MerchantServiceConfigDTO(MerchantServiceConfigEntity entity) {
        // Default constructor
        this.webhookUrl = entity.getWebhookUrl();
        this.webhookSecretKey = entity.getWebhookSecretKey();
        this.callbackUrl= entity.getCallbackUrl();
        this.expirationDate = entity.getExpirationDate();
    }
}
