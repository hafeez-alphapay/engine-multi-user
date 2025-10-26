package com.alphapay.payEngine.integration.model;

import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "currency_exchange_rate")
@Getter
@Setter
@ToString
public class CurrencyExchangeRate extends CommonBean {

    @Column(name = "currency", length = 250)
    private String currency;

    @Column(name = "rate", precision = 10, scale = 8)
    private BigDecimal rate;
}