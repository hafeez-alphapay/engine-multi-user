package com.alphapay.payEngine.alphaServices.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
@Setter
@Getter
@ToString
public class InvoiceItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "invoice_id", referencedColumnName = "id", nullable = false)
    private PaymentLinkEntity paymentLink;

    private String name;

    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity = 1;

}