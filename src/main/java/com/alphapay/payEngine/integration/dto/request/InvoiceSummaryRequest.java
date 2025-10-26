package com.alphapay.payEngine.integration.dto.request;

import com.alphapay.payEngine.integration.dto.paymentData.BaseFinancialRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class InvoiceSummaryRequest extends BaseFinancialRequest {
    @NotBlank
    private String invoiceId;
}
