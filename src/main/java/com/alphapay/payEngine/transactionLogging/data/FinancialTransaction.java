package com.alphapay.payEngine.transactionLogging.data;

import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import com.alphapay.payEngine.common.bean.CommonBean;
import com.alphapay.payEngine.financial.settlement.Settlement;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Entity
@Setter
@Getter
@Table(name = "FinancialTransactions")
public class FinancialTransaction extends CommonBean {
    //TODO add all common financial tran attributes
    @Column(nullable = false, unique = true)
    private String requestId;
    private String applicationId;
    @Column(name = "gw_application_id")
    private Long applicationChannel;
    private String sessionId;
    private String deviceId;
    private String ip;
    private String transactionType;
    private BigDecimal amount;
    private BigDecimal depositShare;
    private BigDecimal totalCharges;
    private BigDecimal vat;
    private BigDecimal customerServiceCharge;
    private String paymentMethod;
    private String currency;
    private String transactionNumber;
    private String transactionId;

    @ElementCollection
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "additional_attributes_financial_transaction_log", joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> paymentResponse;

    @ElementCollection
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "additional_incoming_attributes_financial_transaction_log", joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> incomingPaymentAttributes;

    private String customerReference;

    private String transactionServiceId;

    private String serviceId;

    private String responseMessage;
    private String httpResponseCode;
    private String appResponseCode;
    private String comments;
    private String transactionStatus;
    private String paymentId;
    @OneToOne(fetch = FetchType.EAGER)
    private PaymentLinkEntity invoice;
    private String externalInvoiceId;
    private String externalPaymentId;
    private String invoiceLink;
    private String invoiceStatus;
    private String paidCurrency;
    private BigDecimal paidCurrencyValue;
    private String country;
    private Long merchantId;
    private Long userId;

    @Column
    private Date transactionTime;

    private Long processorId;
    private String cardNumber;
    @Transient
    private Map<String,String> customerInfo;
    private BigDecimal exchangeRate;

    @Transient
    private BigDecimal refundAmount;

    @Transient
    private String refundCurrency;

    private Boolean refunded=Boolean.FALSE;

    private String refundId;

    private String refundWebhookUrl;

    private String externalRefundId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id")
    @JsonIgnore
    private Settlement settlement;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Column(name = "posted_to_wallet")
    private Boolean postedToWallet = Boolean.FALSE;
}
