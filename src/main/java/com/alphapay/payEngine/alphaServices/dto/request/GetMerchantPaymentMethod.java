package com.alphapay.payEngine.alphaServices.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetMerchantPaymentMethod extends BaseRequest {
    private Long merchantId;
}
