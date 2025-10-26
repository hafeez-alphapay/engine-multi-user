package com.alphapay.payEngine.account.management.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "registration_emirates")
@Getter
@Setter
@NoArgsConstructor
public class EmiratesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID
    private Long id;

    @Column(name = "name_en", nullable = false, unique = true) // English Name
    private String nameEn;

    @Column(name = "name_ar", nullable = false, unique = true) // Arabic Name
    private String nameAr;
}
