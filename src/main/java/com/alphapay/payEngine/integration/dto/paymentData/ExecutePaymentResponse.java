package com.alphapay.payEngine.integration.dto.paymentData;

import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class ExecutePaymentResponse extends ApiResponse {
    private Long invoiceId;
    private PaymentLinkEntity invoice;
    private BigDecimal amount;
    private BigDecimal exchangeRate;
    private BigDecimal paidCurrencyValue;
    private String paidCurrency;
    private Long merchantId;
    private String currency;
    private String invoiceLink;
    private String externalInvoiceId;
    private String paymentURL;
    private String paymentHTML;

    private String externalPaymentId;
    private String transactionStatus;
    private String cardNumber;

    @Override
    public String toString() {
        return "ExecutePaymentResponse{" +
                "invoiceId=" + invoiceId +
                ", invoice=" + invoice +
                ", amount=" + amount +
                ", merchantId=" + merchantId +
                ", currency='" + currency + '\'' +
                ", invoiceLink='" + invoiceLink + '\'' +
                ", externalInvoiceId='" + externalInvoiceId + '\'' +
                ", paymentURL='" + paymentURL + '\'' +
                ", paymentHTML='" + paymentHTML + '\'' +
                ", externalPaymentId='" + externalPaymentId + '\'' +
                ", transactionStatus='" + transactionStatus + '\'' +
                '}';
    }
}
