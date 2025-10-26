package com.alphapay.payEngine.integration.model;

import com.alphapay.payEngine.common.bean.CommonBean;
import com.alphapay.payEngine.integration.model.orchast.ServiceProvider;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "merchant_payment_provider_registration")
@ToString(exclude = {"user", "customCommissions"})

public class MerchantPaymentProviderRegistration extends CommonBean {

    private Long merchantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_provider_id", nullable = false, foreignKey = @ForeignKey(name = "fk_service_provider"))
    private ServiceProvider serviceProvider;

    private Integer supplierCode;

    @Column(name = "is_percentage_of_net_value", nullable = false)
    private boolean isPercentageOfNetValue;

    @Column(name = "commission_value", precision = 10, scale = 2)
    private BigDecimal commissionValue;

    @Column(name = "commission_percentage", precision = 5, scale = 2)
    private BigDecimal commissionPercentage;

    @Column(name = "deposit_terms", nullable = false, length = 255)
    private String depositTerms;

    @Column(name = "deposit_day", nullable = false, length = 50)
    private String depositDay;

    @Column(name = "display_supplier_details", nullable = false)
    private boolean displaySupplierDetails;

    @Enumerated(EnumType.STRING)
    private FeeChargedBy feeChargedBy;

    private String kycStatus;

    private String kycFeedback;

    private Boolean isDefault;

    private String merchantExternalId;

    private String merchantExternalKey;

    private String merchantApiKey;

    private String merchantApiKeyId;

    @JsonManagedReference
    @OneToMany(mappedBy = "merchantPaymentProviderRegistration", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CustomMerchantCommissionEntity> customCommissions;

    public enum FeeChargedBy {
        Customer, Merchant, Split
    }
}
