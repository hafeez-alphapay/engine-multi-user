package com.alphapay.payEngine.alphaServices.model;


import com.alphapay.payEngine.account.management.model.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "store_products", schema = "sybersupermerchant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StoreProductsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "title_ar", length = 255)
    private String titleAr;

    @Column(name = "description_ar", length = 500)
    private String descriptionAr;

    @Column(name = "product_condition", length = 100)
    private String productCondition;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "sku", nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "weight", precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(name = "dimensions", length = 255)
    private String dimensions;

    @Column(name = "brand", length = 255)
    private String brand;

    @Column(name = "category", length = 255)
    private String category;

    @Column(name = "currency", length = 50)
    private String currency;

    @Column(name = "product_type", length = 100)
    private String productType;

    @Column(name = "product_attributes", length = 500)
    private String productAttributes;

    @Column(name = "product_attributes_ar", length = 500)
    private String productAttributesAr;

    @Column(name = "discount", precision = 19, scale = 2)
    private BigDecimal discount;

    @Column(name = "use_delivery_service")
    private Boolean useDeliveryService;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_on", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdOn;

    @Column(name = "status", length = 100)
    private String status;

    // Relationships
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory", referencedColumnName = "id", nullable = false)
    private ProductInventoryEntity inventory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_user_account", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private UserEntity merchantUserAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_service", referencedColumnName = "id")
    private DeliveryServiceInfo deliveryService;
}