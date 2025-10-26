package com.alphapay.payEngine.integration.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Setter
@Getter
@ToString
public class MbmeMerchantRegistration extends BaseRequest {
    private Map<String,String> responseData;
}
