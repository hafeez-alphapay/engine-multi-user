package com.alphapay.payEngine.alphaServices.model;

import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "merchant_link_settings")
@Setter
@Getter
public class MerchantLinkSettings extends CommonBean {

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "max_transaction_amount", precision = 38, scale = 2)
    private BigDecimal maxTransactionAmount;

    private String currency;
}
