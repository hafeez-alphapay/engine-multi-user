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
import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Defines the settlement cadence for a payment processor/provider. The schedule determines when
 * the batch job should aggregate financial transactions and produce settlements.
 */
@Getter
@Setter
@Entity
@Table(name = "settlement_schedule_configuration",
        uniqueConstraints = @UniqueConstraint(name = "uk_schedule_processor", columnNames = "processor_id"))
public class SettlementScheduleConfiguration extends CommonBean {

    @Column(name = "processor_id", nullable = false)
    private Long processorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "cycle", nullable = false)
    private SettlementCycle cycle;

    @Column(name = "cutoff_time", nullable = false)
    private LocalTime cutoffTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_day_of_week")
    private DayOfWeek primaryDayOfWeek;

    @Enumerated(EnumType.STRING)
    @Column(name = "secondary_day_of_week")
    private DayOfWeek secondaryDayOfWeek;

    @Column(name = "primary_day_of_month")
    private Integer primaryDayOfMonth;

    @Column(name = "secondary_day_of_month")
    private Integer secondaryDayOfMonth;

    @Column(name = "time_zone", nullable = false)
    private String timeZone = "UTC";

    @Column(name = "reserve_percentage")
    private BigDecimal reservePercentage;

    @Column(name = "reserve_hold_days")
    private Integer reserveHoldDays;

}

