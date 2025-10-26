package com.alphapay.payEngine.integration.dto.paymentData;

import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Map;

@ToString(callSuper = true)
@Setter
@Getter
public class InitiatePaymentResponse extends BaseFinancialRequest {
    private String currency;
    private String paymentId;
    private Long invoiceId;
    private PaymentLinkEntity invoice;
    private String invoiceLink;
    private Long merchantId;
    private BigDecimal amount;
    private String paymentLinkUrl;

    private Map<String, Object> responseData;
}
