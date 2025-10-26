package com.alphapay.payEngine.service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Optimus App Financial Services , configuration parameters for service flow
 */
@Entity
@Setter
@Getter
@ToString
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "serviceName", "applicationId" }) })

public class OptimusServiceConfig implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "service_id",unique = true)
    private String serviceId;
    private boolean syberbiller = false;
    private boolean purchase = true;
    private String serviceName;
    private String applicationId;
    private String serviceNameAR;
    private String serviceNameEN;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id", nullable = false)
    @OrderBy("orderNo asc")
    private Set<PaymentInfoField> paymentInfoFields;
    @Column(name = "status")
    private String status;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id", nullable = false)
    @OrderBy("stepId asc")
    private List<ExternalExecutionStep> externalSteps;
    //This will not be used (Maybe in future if some cases can't be handled by Optimus Generic Service)
    private String optimusServiceSpecialHandlerClass;
    private String adminPortalDisplayLogo;

    private BigDecimal minimumAmount;
    private BigDecimal maximumLimitAmount;
}
