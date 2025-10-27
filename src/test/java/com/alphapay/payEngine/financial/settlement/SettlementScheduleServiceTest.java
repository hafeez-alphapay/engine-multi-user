package com.alphapay.payEngine.financial.settlement;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SettlementScheduleServiceTest {

    private final SettlementScheduleService service = new SettlementScheduleService(mock(SettlementScheduleConfigurationRepository.class));

    @Test
    void calculatesDailyWindowUsingCutoff() {
        SettlementScheduleConfiguration configuration = buildConfiguration(SettlementCycle.DAILY);
        configuration.setCutoffTime(LocalTime.of(12, 30));

        SettlementScheduleService.SettlementWindow window = service.calculateWindow(configuration,
                LocalDateTime.of(2024, 6, 11, 13, 0));

        assertThat(window.periodStart()).isEqualTo(LocalDateTime.of(2024, 6, 10, 12, 30));
        assertThat(window.periodEnd()).isEqualTo(LocalDateTime.of(2024, 6, 11, 12, 30));
    }

    @Test
    void calculatesWeeklyWindowForConfiguredDay() {
        SettlementScheduleConfiguration configuration = buildConfiguration(SettlementCycle.WEEKLY);
        configuration.setCutoffTime(LocalTime.of(12, 30));
        configuration.setPrimaryDayOfWeek(DayOfWeek.MONDAY);

        SettlementScheduleService.SettlementWindow window = service.calculateWindow(configuration,
                LocalDateTime.of(2024, 6, 12, 13, 0));

        assertThat(window.periodEnd()).isEqualTo(LocalDateTime.of(2024, 6, 10, 12, 30));
        assertThat(window.periodStart()).isEqualTo(LocalDateTime.of(2024, 6, 3, 12, 30));
    }

    @Test
    void calculatesTwiceWeeklyWindowBetweenConfiguredDays() {
        SettlementScheduleConfiguration configuration = buildConfiguration(SettlementCycle.TWICE_WEEKLY);
        configuration.setCutoffTime(LocalTime.of(18, 0));
        configuration.setPrimaryDayOfWeek(DayOfWeek.MONDAY);
        configuration.setSecondaryDayOfWeek(DayOfWeek.THURSDAY);

        SettlementScheduleService.SettlementWindow window = service.calculateWindow(configuration,
                LocalDateTime.of(2024, 6, 14, 19, 0));

        assertThat(window.periodEnd()).isEqualTo(LocalDateTime.of(2024, 6, 13, 18, 0));
        assertThat(window.periodStart()).isEqualTo(LocalDateTime.of(2024, 6, 10, 18, 0));
    }

    @Test
    void calculatesMonthlyWindowAdjustingForShortMonths() {
        SettlementScheduleConfiguration configuration = buildConfiguration(SettlementCycle.MONTHLY);
        configuration.setCutoffTime(LocalTime.of(9, 0));
        configuration.setPrimaryDayOfMonth(31);

        SettlementScheduleService.SettlementWindow window = service.calculateWindow(configuration,
                LocalDateTime.of(2024, 4, 2, 10, 0));

        assertThat(window.periodEnd()).isEqualTo(LocalDateTime.of(2024, 3, 31, 9, 0));
        assertThat(window.periodStart()).isEqualTo(LocalDateTime.of(2024, 2, 29, 9, 0));
    }

    private SettlementScheduleConfiguration buildConfiguration(SettlementCycle cycle) {
        SettlementScheduleConfiguration configuration = new SettlementScheduleConfiguration();
        configuration.setProcessorId(1L);
        configuration.setCycle(cycle);
        configuration.setCutoffTime(LocalTime.NOON);
        configuration.setTimeZone("UTC");
        return configuration;
    }
}

