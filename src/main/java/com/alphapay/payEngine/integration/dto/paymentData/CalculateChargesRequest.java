package com.alphapay.payEngine.integration.dto.paymentData;

import com.alphapay.payEngine.service.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalculateChargesRequest extends BaseRequest {
    private String paymentId;
}
