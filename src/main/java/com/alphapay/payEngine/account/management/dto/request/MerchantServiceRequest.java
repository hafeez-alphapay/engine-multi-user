package com.alphapay.payEngine.account.management.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MerchantServiceRequest {
    private String serviceId;
    private String status;
}
