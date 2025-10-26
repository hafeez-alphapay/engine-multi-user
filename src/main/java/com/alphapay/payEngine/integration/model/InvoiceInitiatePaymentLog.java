package com.alphapay.payEngine.integration.model;

import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Map;

@Entity
@Table(name = "invoice_initiate_payment_log")
@Getter
@Setter
@ToString
public class InvoiceInitiatePaymentLog extends CommonBean {

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "payment_id", nullable = false, length = 250)
    private String paymentId;

    @Column(name = "external_payment_id", nullable = false, length = 250)
    private String externalPaymentId;

    private String externalInvoiceId;

    @Column(name = "trans_type", nullable = false, length = 250)
    private String transType;

    @Column(name = "invoice_value", nullable = false, length = 250)
    private BigDecimal invoiceValue;

    @Column(name = "invoice_currency")
    private String invoiceCurrency;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "response_message")
    private String responseMessage;


    @Column(name = "response_code")
    private Integer responseCode;

    @Column(name = "payment_status")
    private String paymentStatus;

    @ElementCollection
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "response_data_attributes_log", joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> responseData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", referencedColumnName = "id", insertable = false, updatable = false)
    private PaymentLinkEntity paymentLinks;
}