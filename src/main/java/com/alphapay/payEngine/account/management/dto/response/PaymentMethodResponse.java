package com.alphapay.payEngine.account.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodResponse {
    private Integer paymentMethodId;
    private String providerId;
    private String paymentMethodNameAr;
    private String paymentMethodNameEn;
    private String paymentMethodCode;
}

