package com.alphapay.payEngine.alphaServices.model;

import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.merchantKyc.model.MerchantEntity;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "payment_links")
@Getter
@Setter
@ToString
public class PaymentLinkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;
    private String invoiceReference;
    private String type;
    private String description;
    private BigDecimal amount;

    @Transient
    private String amountString;

    @Column(name = "fixed_discount", nullable = true)
    private BigDecimal fixedDiscount;

    @Column(name = "percentage_discount", nullable = true)
    private Integer percentageDiscount;

    @Column(name = "remind_after", nullable = true)
    private int remindAfter;

    @Column(name = "payment_link_title", nullable = false)
    private String paymentLinkTitle;
    private String currency;

    @Column(name = "invoice_id", nullable = true)
    private String invoiceId;

    @Column(name = "invoice_status", nullable = true)
    private String invoiceStatus;

    private String status;

    @Column(name = "created_on", nullable = false)
    private Date createdOn;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "expiry_date", nullable = false)
    private Date expiryDateTime;

    private long quantity;

    @Column(name = "total_payment_attempts", nullable = true)
    private int totalPaymentAttempts;

    @Column(name = "successfulAttempts", nullable = true)
    private int successfulAttempts;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_contact")
    private String customerContact;

    @Column(name = "customer_email")
    private String customerEmail;

    @ManyToOne
    @JoinColumn(name = "delivery_service", referencedColumnName = "id", nullable = true)
    private DeliveryServiceInfo deliveryService;

    @Column(name = "use_delivery_service", nullable = true)
    private Boolean useDeliveryService;

    @Column(name = "customer_kyc_required")
    private boolean customerKycRequired;

    @Column(name = "signature_required")
    private boolean signatureRequired;

    @Column(name = "signature_url")
    private String signatureUrl;

    @Column(name = "customer_kyc_document")
    private String customerKycDocument;

    @JsonIgnore
    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "merchant_user_account", referencedColumnName = "id", nullable = false)
    private MerchantEntity merchantUserAccount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product")
    @JsonIgnore
    @Transient
    private StoreProductsEntity product;

    private boolean openAmount;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    private String comment;

    private boolean requiredTerms;

    private String termsCondition;

    private String language = "en";
    private String externalPaymentId;
    private String paymentId;
    private String callBackUrl;
    private String webhookUrl;
    private String webhookSecretKey;

    @ToString.Exclude
    @JsonManagedReference
    @OneToMany(mappedBy = "paymentLink", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItemEntity> invoiceItems = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonBackReference
    private List<FinancialTransaction> transactions;

    private String countryCode = "+971";

    @Column(name = "payment_methods_code")
    private String paymentMethodsCode;

    @Column(name = "business_name")
    private String businessName;


    /* ---------------------------------------------------------------------
     *  Key/value table mapping
     * ------------------------------------------------------------------- */
    @ElementCollection
    @CollectionTable(
            name = "payment_link_additional_inputs",
            joinColumns = @JoinColumn(name = "payment_link_id"))
    @MapKeyColumn(name = "name")          // the map key
    @AttributeOverrides({                 // columns defined in KvPair
            @AttributeOverride(name = "value", column = @Column(name = "value")),
            @AttributeOverride(name = "type", column = @Column(name = "type"))
    })
    private Map<String, KvPair> additionalInputs = new LinkedHashMap<>();

    @ElementCollection
    @CollectionTable(
            name = "payment_link_additional_outputs",
            joinColumns = @JoinColumn(name = "payment_link_id"))
    @MapKeyColumn(name = "name")          // the map key
    @AttributeOverrides({                 // columns defined in KvPair
            @AttributeOverride(name = "value", column = @Column(name = "value")),
            @AttributeOverride(name = "type", column = @Column(name = "type"))
    })
    private Map<String, KvPair> additionalOutputs = new LinkedHashMap<>();


    @Transient
    public Map<String, Object> getAdditionalInputs() {
        return additionalInputs.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().toObject(),
                        (a, b) -> b,
                        LinkedHashMap::new));
    }

    public void putAdditionalInput(String key, Object value) {
        additionalInputs.put(key, KvPair.of(value));
    }


    public void putAdditionalOutput(String key, Object value) {
        additionalOutputs.put(key, KvPair.of(value));
    }

    @Transient
    public Map<String, Object> getAdditionalOutputs() {
        return additionalOutputs.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().toObject(),
                        (a, b) -> b,
                        LinkedHashMap::new));
    }


}
