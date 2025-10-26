package com.alphapay.payEngine.financial.reserve;

import com.alphapay.payEngine.common.bean.CommonBean;
import com.alphapay.payEngine.financial.settlement.Settlement;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "rolling_reserves")
public class RollingReserve extends CommonBean {

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(nullable = false)
    private String currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

    @Column(name = "reserve_percentage", nullable = false)
    private BigDecimal reservePercentage;

    @Column(name = "reserve_amount", nullable = false)
    private BigDecimal reserveAmount;

    @Column(name = "hold_period_days", nullable = false)
    private Integer holdPeriodDays;

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RollingReserveStatus status = RollingReserveStatus.HELD;

    @Column(name = "released_at")
    private LocalDate releasedAt;

    private String notes;
}
