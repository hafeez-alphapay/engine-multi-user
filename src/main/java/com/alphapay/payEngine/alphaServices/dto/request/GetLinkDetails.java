package com.alphapay.payEngine.alphaServices.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetLinkDetails extends BaseRequest {
    private String invoiceId;
    private Long merchantId;
}
