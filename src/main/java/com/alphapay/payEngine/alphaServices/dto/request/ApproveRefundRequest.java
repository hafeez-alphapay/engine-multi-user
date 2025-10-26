package com.alphapay.payEngine.alphaServices.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.codehaus.commons.nullanalysis.NotNull;
@Data
public class ApproveRefundRequest extends BaseRequest {
    @NotBlank
    String approvedBy;
    @NotNull
    Long approvedById;

    @NotBlank
    String approvalComments;
    @NotBlank
    String refundId;
}
