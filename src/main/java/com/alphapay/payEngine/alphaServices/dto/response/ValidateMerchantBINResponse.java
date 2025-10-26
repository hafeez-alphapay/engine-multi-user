package com.alphapay.payEngine.alphaServices.dto.response;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ValidateMerchantBINResponse {
    private Boolean valid;
    private String message;
    private String messageAr;
}
