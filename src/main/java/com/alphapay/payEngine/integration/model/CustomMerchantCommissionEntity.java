package com.alphapay.payEngine.integration.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@Entity
@Table(name = "custom_merchant_commission")
public class CustomMerchantCommissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethodEntity paymentMethod;

    @Column(name = "commission_value", precision = 18, scale = 2, nullable = false)
    private BigDecimal commissionValue;

    @Column(name = "commission_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal commissionPercentage;

    @Column(name = "is_percentage_of_net_value", nullable = false)
    private boolean isPercentageOfNetValue;

    @Column(name = "is_local", nullable = false)
    private boolean isLocal;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "merchant_provider_registration_id", nullable = false)
    private MerchantPaymentProviderRegistration merchantPaymentProviderRegistration;
}
