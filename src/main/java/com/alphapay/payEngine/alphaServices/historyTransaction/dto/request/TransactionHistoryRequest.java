package com.alphapay.payEngine.alphaServices.historyTransaction.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TransactionHistoryRequest extends BaseRequest {
    private Date fromDate;
    private Date toDate;
    private Integer pageSize;
    private Integer pageNumber;
    private Long merchantId;
    private List<Long> subMerchantIds;
    private Long subMerchantId;
    private String status;
    private String paymentId;
    private String transactionStatus;
    private String transactionType;
    private String invoiceStatus;
    private BigDecimal amount;
    private BigDecimal paidCurrencyValue;
    private String paymentMethod;
    private String currency;
    private String transactionNumber;
    private String externalInvoiceId;
    private String externalPaymentId;
    private String invoiceLink;
    private String maskedCard;
    private Long processorId;
}
