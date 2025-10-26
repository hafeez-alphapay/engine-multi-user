package com.alphapay.payEngine.account.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KycApprovalRequest extends com.alphapay.payEngine.service.bean.BaseRequest {
    @NotBlank(message = "Merchant ID is required")
    private String merchantId;

    @NotBlank(message = "Approval status is required")
    private String approvalStatus; // Values: "approved" or "rejected"

    private String comments; // Optional field
}
