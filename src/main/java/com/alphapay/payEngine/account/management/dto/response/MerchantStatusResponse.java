package com.alphapay.payEngine.account.management.dto.response;

import com.alphapay.payEngine.model.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class MerchantStatusResponse extends BaseResponse {
    private Map<String, String> data; // Contains merchantId and accountStatus
}
