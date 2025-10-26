package com.alphapay.payEngine.account.management.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "registration_countries")
@Getter
@Setter
@NoArgsConstructor
public class CountriesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID
    private Long id;

    @Column(name = "name_en", nullable = false, unique = true) // English Name
    private String nameEn;

    @Column(name = "name_ar", nullable = false, unique = true) // Arabic Name
    private String nameAr;

    @Column(name = "ios_code", nullable = false, unique = true)
    private String iosCode;


    @Enumerated(EnumType.STRING)
    @Column(name = "risk", nullable = true)
    private RiskLevel risk;


}
