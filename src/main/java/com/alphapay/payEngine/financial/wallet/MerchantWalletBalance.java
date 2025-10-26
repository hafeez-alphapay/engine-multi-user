package com.alphapay.payEngine.financial.wallet;

import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "merchant_wallet_balances",
        uniqueConstraints = @UniqueConstraint(name = "uk_wallet_merchant_currency",
                columnNames = {"merchant_id", "currency"}))
public class MerchantWalletBalance extends CommonBean {

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(nullable = false)
    private String currency;

    @Column(name = "total_credits", nullable = false)
    private BigDecimal totalCredits = BigDecimal.ZERO;

    @Column(name = "total_debits", nullable = false)
    private BigDecimal totalDebits = BigDecimal.ZERO;

    @Column(name = "rolling_reserve_held", nullable = false)
    private BigDecimal rollingReserveHeld = BigDecimal.ZERO;

    @Column(name = "current_balance", nullable = false)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MerchantWalletStatus status = MerchantWalletStatus.ACTIVE;
}
