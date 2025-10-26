package com.alphapay.payEngine.alphaServices.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import lombok.Data;

@Data
public class GetPendingRefundRequest extends BaseRequest {
    private Integer pageSize;
    private Integer pageNumber;
}
