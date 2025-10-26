package com.alphapay.payEngine.account.management.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "business_types")
@Getter
@Setter
@NoArgsConstructor
public class BusinessTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID
    private Long id;

    @Column(name = "name_en", nullable = false, unique = true) // English Name
    private String nameEn;

    @Column(name = "name_ar", nullable = false, unique = true) // Arabic Name
    private String nameAr;

    @OneToMany(mappedBy = "businessType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BusinessCategoryEntity> categories; // List of categories associated with this type
}