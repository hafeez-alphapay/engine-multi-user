package com.alphapay.payEngine.financial.wallet;

import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "merchant_wallet_ledger")
public class MerchantWalletLedgerEntry extends CommonBean {

    @Column(name = "wallet_id")
    private Long walletId;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private MerchantWalletLedgerEntryType entryType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MerchantWalletLedgerDirection direction;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "balance_before", nullable = false)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", nullable = false)
    private BigDecimal balanceAfter;

    @Column(name = "reserve_before")
    private BigDecimal reserveBefore;

    @Column(name = "reserve_after")
    private BigDecimal reserveAfter;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "statement_description")
    private String statementDescription;
}
