package com.alphapay.payEngine.account.merchantKyc.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardPaymentMethodsDto {

    @NotNull(message = "Ecommerce flag must be specified")
    private Boolean ecommerce;

    @NotNull(message = "Payment link flag must be specified")
    private Boolean paymentLink;

    @NotNull(message = "POS machine flag must be specified")
    private Boolean posMachine;
}
