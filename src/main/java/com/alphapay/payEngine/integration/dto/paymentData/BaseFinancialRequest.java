package com.alphapay.payEngine.integration.dto.paymentData;

import com.alphapay.payEngine.model.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Setter
@Getter
@ToString
public class BaseFinancialRequest extends BaseResponse {
    private Map<String, String> paymentAttributes;
}
