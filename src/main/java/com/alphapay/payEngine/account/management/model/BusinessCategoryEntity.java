package com.alphapay.payEngine.account.management.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "business_categories")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class BusinessCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID
    private Long id;

    @Column(name = "name_en", nullable = false) // English Name
    private String nameEn;

    @Column(name = "name_ar", nullable = false) // Arabic Name
    private String nameAr;

    @ManyToOne(fetch = FetchType.LAZY) // Many categories belong to one business type
    @JoinColumn(name = "business_type_id", nullable = false) // Foreign key to `business_types`
    private BusinessTypeEntity businessType;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk", nullable = true)
    private RiskLevel risk;
}