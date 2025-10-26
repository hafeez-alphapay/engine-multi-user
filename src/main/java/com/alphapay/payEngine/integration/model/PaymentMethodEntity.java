package com.alphapay.payEngine.integration.model;

import com.alphapay.payEngine.integration.model.orchast.ServiceProvider;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_methods")
@Getter
@Setter
@ToString
public class PaymentMethodEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "payment_method_id", length = 255)
    private Integer paymentMethodId; // Optional field for a unique identifier

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_provider_id", nullable = false, foreignKey = @ForeignKey(name = "fk_service_provider"))
    private ServiceProvider serviceProvider;

    @Column(name = "payment_method_ar", length = 255)
    private String paymentMethodAr; // Arabic name of the payment method

    @Column(name = "payment_method_en", length = 255)
    private String paymentMethodEn; // English name of the payment method

    @Column(name = "payment_method_code", length = 50)
    private String paymentMethodCode; // Code representing the payment method

    @Column(name = "is_direct_payment")
    private Boolean isDirectPayment; // Whether direct payment is supported (TinyInt in DB)

    @Column(name = "service_charge", precision = 10, scale = 2)
    private BigDecimal serviceCharge; // Service charge for the payment method

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount; // Total amount associated with the payment method

    @Column(name = "currency_iso", length = 10)
    private String currencyIso; // Currency ISO code for the payment method (optional)

    @Column(name = "is_embedded_supported")
    private Boolean isEmbeddedSupported; // Whether embedded payment is supported (TinyInt in DB)

    @Column(name = "payment_currency_iso", length = 10)
    private String paymentCurrencyIso;

    @Column(name = "status")
    private String status;

    @Column(name = "fixed_comm")
    private BigDecimal fixedComm;

    @Column(name = "local_percentage_comm")
    private BigDecimal localPercentageComm;

    @Column(name = "international_percentage_comm")
    private BigDecimal internationalPercentageComm;

}