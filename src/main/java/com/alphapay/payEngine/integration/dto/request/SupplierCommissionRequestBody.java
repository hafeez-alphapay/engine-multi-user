package com.alphapay.payEngine.integration.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class SupplierCommissionRequestBody {
    private Integer supplierCode;
    private List<CustomSupplierCommissionRequest> supplierCommissions;
}
