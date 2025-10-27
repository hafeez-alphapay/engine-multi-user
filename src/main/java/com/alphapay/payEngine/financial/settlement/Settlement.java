package com.alphapay.payEngine.financial.settlement;

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
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "settlements",
        uniqueConstraints = @UniqueConstraint(name = "uk_settlement_reference",
                columnNames = {"settlement_reference"}))
public class Settlement extends CommonBean {

    @Column(name = "settlement_reference", nullable = false)
    private String settlementReference;

    @Column(name = "processor_id", nullable = false)
    private Long processorId;


    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementCycle cycle;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @Column(name = "total_sales", nullable = false)
    private BigDecimal totalSales = BigDecimal.ZERO;

    @Column(name = "total_refunds", nullable = false)
    private BigDecimal totalRefunds = BigDecimal.ZERO;

    @Column(name = "total_commission", nullable = false)
    private BigDecimal totalCommission = BigDecimal.ZERO;

    @Column(name = "settlement_fees", nullable = false)
    private BigDecimal settlementFees = BigDecimal.ZERO;

    @Column(name = "rolling_reserve", nullable = false)
    private BigDecimal rollingReserve = BigDecimal.ZERO;

    @Column(name = "vat", nullable = false)
    private BigDecimal vat = BigDecimal.ZERO;

    @Column(name = "net_payout", nullable = false)
    private BigDecimal netPayout = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status = SettlementStatus.PENDING;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Column(name = "payout_reference")
    private String payoutReference;
}
