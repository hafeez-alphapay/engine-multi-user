package com.alphapay.payEngine.account.management.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class MerchantKycResponse {
    private Map<String, String> data;
    private Pagination pagination;
}
