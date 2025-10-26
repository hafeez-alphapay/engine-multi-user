package com.alphapay.payEngine.integration.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "banks")
@Getter
@Setter
public class BankEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "value", nullable = false)
    private Long bankId;

    @Column(name = "text", nullable = false)
    private String bankName;
}
