package com.alphapay.payEngine.transactionLogging.data;

import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
@Entity
@Setter
@Getter
@ToString
@Table(name="NonFinancialTransactions",uniqueConstraints = { @UniqueConstraint(columnNames = { "requestId", "applicationId" }) })
public class NonFinancialTransaction extends CommonBean {
    @Column(nullable = false,unique = true)
    private String requestId;
    private String sessionId;
    private String applicationId;
    @Column(name = "gw_application_id")
    private Long applicationApplication;
    private String ip;
    private String transactionType;
    private String responseMessage;
    private String httpResponseCode;
    private String httpResponse;
    private BigDecimal amount;
    private String registrationId;
    private String appResponseCode;
    private Long userId;
}
