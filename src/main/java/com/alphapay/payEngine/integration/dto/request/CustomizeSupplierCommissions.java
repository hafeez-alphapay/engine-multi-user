package com.alphapay.payEngine.integration.dto.request;

import com.alphapay.payEngine.model.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CustomizeSupplierCommissions extends BaseResponse {
    private Long merchantId;
    private List<SupplierCommission> supplierCommissions;
}
