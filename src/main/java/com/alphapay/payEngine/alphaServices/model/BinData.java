package com.alphapay.payEngine.alphaServices.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bin_data")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BinData {

    @Id
    @Column(length = 6)
    private String bin;

    @Column(length = 50)
    private String brand;

    @Column(length = 20)
    private String type;

    @Column(length = 50)
    private String category;

    @Column(length = 100)
    private String issuer;

    @Column(length = 100, name = "issuer_phone")
    private String issuerPhone;

    @Column(length = 255, name = "issuer_url")
    private String issuerUrl;

    @Column(length = 2, name = "iso_code_2", nullable = false)
    private String isoCode2;

    @Column(length = 3, name = "iso_code_3")
    private String isoCode3;

    @Column(length = 100, name = "country_name", nullable = false)
    private String countryName;
}
