package com.alphapay.payEngine.integration.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BankResponse {
    private Long bankId;
    private String bankName;
}
