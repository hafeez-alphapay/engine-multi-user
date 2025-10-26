package com.alphapay.payEngine.alphaServices.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "redirect_3ds_url")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Redirect3DSUrl {

    @Id
    @Column(length = 50)
    private String uuid;

    @Column(length = 100)
    private String paymentId;


    @Column(length = 100)
    private String externalPaymentId;

    @Column(length = 300)
    private String providerUrl;

}