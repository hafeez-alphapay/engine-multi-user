package com.alphapay.payEngine.alphaServices.model;

import com.alphapay.payEngine.common.bean.CommonBean;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.codehaus.commons.nullanalysis.NotNull;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "pending_refunds")
@Getter
@Setter
@ToString
public class PendingRefundProcess extends CommonBean {

    // in PendingRefundProcess
    @Column(name = "refund_key", nullable = false)
    private String key;

    @Column(name = "key_type", length = 100)
    private String keyType = "PaymentId";

    @Column(name = "request_id", nullable = false, unique = true)
    @Pattern(regexp = "^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$",
            message = "Invalid UUID Format")
    private String requestId;

    @Column(name = "alpha_refund_id", nullable = false, unique = true)
    private String alphaRefundId;

    @Column(name = "supplier_deducted_amount", precision = 19, scale = 4)
    private BigDecimal supplierDeductedAmount;

    private String comment;

    private Long merchantId;
    String applicationId;

    String approvedBy;
    Long approvedById;

    String approvalComments;
    private String merchantName;
}

