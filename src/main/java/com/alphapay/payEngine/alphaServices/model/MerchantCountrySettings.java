package com.alphapay.payEngine.alphaServices.model;

import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "merchant_country_settings")
@Getter
@Setter
public class MerchantCountrySettings extends CommonBean {
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;
    @Column(name = "country", nullable = false)
    private String isoCountryCode; // ISO 3166-1 alpha-2 country code
}

