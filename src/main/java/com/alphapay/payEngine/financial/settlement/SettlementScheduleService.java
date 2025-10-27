package com.alphapay.payEngine.financial.settlement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

@Service
public class SettlementScheduleService {

    private final SettlementScheduleConfigurationRepository repository;

    public SettlementScheduleService(SettlementScheduleConfigurationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<SettlementScheduleConfiguration> findByProcessorId(Long processorId) {
        return repository.findByProcessorId(processorId);
    }

    @Transactional(readOnly = true)
    public SettlementScheduleConfiguration getRequiredConfiguration(Long processorId) {
        return findByProcessorId(processorId)
                .orElseThrow(() -> new IllegalStateException("Missing settlement schedule for processor " + processorId));
    }

    public SettlementWindow calculateWindow(SettlementScheduleConfiguration configuration, LocalDateTime referenceTime) {
        ZoneId zoneId = ZoneId.of(configuration.getTimeZone());
        ZonedDateTime reference = referenceTime.atZone(zoneId);
        LocalTime cutoff = configuration.getCutoffTime();

        ZonedDateTime periodEnd = switch (configuration.getCycle()) {
            case DAILY -> alignToCutoff(reference, cutoff);
            case WEEKLY -> resolveWeeklyCutoff(reference, cutoff, configuration.getPrimaryDayOfWeek());
            case TWICE_WEEKLY -> resolveMultipleWeeklyCutoff(reference, cutoff,
                    configuration.getPrimaryDayOfWeek(), configuration.getSecondaryDayOfWeek());
            case MONTHLY -> resolveMonthlyCutoff(reference, cutoff, configuration.getPrimaryDayOfMonth());
            case TWICE_MONTHLY -> resolveMultipleMonthlyCutoff(reference, cutoff,
                    configuration.getPrimaryDayOfMonth(), configuration.getSecondaryDayOfMonth());
            case ON_DEMAND -> throw new IllegalStateException("On-demand cycles do not have automatic windows");
        };

        ZonedDateTime periodStart = switch (configuration.getCycle()) {
            case DAILY -> periodEnd.minusDays(1);
            case WEEKLY -> periodEnd.minusWeeks(1);
            case TWICE_WEEKLY -> determineWeeklyStart(periodEnd, configuration);
            case MONTHLY -> periodEnd.minusMonths(1);
            case TWICE_MONTHLY -> determineMonthlyStart(periodEnd, configuration);
            case ON_DEMAND -> throw new IllegalStateException("On-demand cycles do not have automatic windows");
        };

        return new SettlementWindow(periodStart.toLocalDateTime(), periodEnd.toLocalDateTime(), zoneId);
    }

    private ZonedDateTime alignToCutoff(ZonedDateTime reference, LocalTime cutoff) {
        ZonedDateTime candidate = reference.with(cutoff);
        if (reference.toLocalTime().isBefore(cutoff)) {
            candidate = candidate.minusDays(1);
        }
        return candidate;
    }

    private ZonedDateTime resolveWeeklyCutoff(ZonedDateTime reference, LocalTime cutoff, DayOfWeek targetDay) {
        if (targetDay == null) {
            throw new IllegalStateException("Weekly cycles require at least one day of week");
        }
        ZonedDateTime candidate = reference.with(TemporalAdjusters.previousOrSame(targetDay)).with(cutoff);
        if (candidate.isAfter(reference)) {
            candidate = candidate.minusWeeks(1);
        }
        return candidate;
    }

    private ZonedDateTime resolveMultipleWeeklyCutoff(ZonedDateTime reference, LocalTime cutoff,
                                                      DayOfWeek first, DayOfWeek second) {
        ZonedDateTime candidate = resolveOptionalWeekly(reference, cutoff, first).orElse(null);
        ZonedDateTime other = resolveOptionalWeekly(reference, cutoff, second).orElse(null);

        if (candidate == null && other == null) {
            throw new IllegalStateException("Multiple weekly cycles require configured days of week");
        }
        if (candidate == null) {
            return other;
        }
        if (other == null) {
            return candidate;
        }
        return candidate.isAfter(other) ? candidate : other;
    }

    private Optional<ZonedDateTime> resolveOptionalWeekly(ZonedDateTime reference, LocalTime cutoff, DayOfWeek dayOfWeek) {
        if (dayOfWeek == null) {
            return Optional.empty();
        }
        ZonedDateTime candidate = reference.with(TemporalAdjusters.previousOrSame(dayOfWeek)).with(cutoff);
        if (candidate.isAfter(reference)) {
            candidate = candidate.minusWeeks(1);
        }
        return Optional.of(candidate);
    }

    private ZonedDateTime resolveMonthlyCutoff(ZonedDateTime reference, LocalTime cutoff, Integer dayOfMonth) {
        if (dayOfMonth == null) {
            throw new IllegalStateException("Monthly cycles require at least one day of month");
        }
        ZonedDateTime candidate = alignMonthly(reference, cutoff, dayOfMonth);
        if (candidate.isAfter(reference)) {
            candidate = alignMonthly(reference.minusMonths(1), cutoff, dayOfMonth);
        }
        return candidate;
    }

    private ZonedDateTime resolveMultipleMonthlyCutoff(ZonedDateTime reference, LocalTime cutoff,
                                                       Integer first, Integer second) {
        ZonedDateTime candidate = resolveOptionalMonthly(reference, cutoff, first).orElse(null);
        ZonedDateTime other = resolveOptionalMonthly(reference, cutoff, second).orElse(null);

        if (candidate == null && other == null) {
            throw new IllegalStateException("Multiple monthly cycles require configured days of month");
        }
        if (candidate == null) {
            return other;
        }
        if (other == null) {
            return candidate;
        }
        return candidate.isAfter(other) ? candidate : other;
    }

    private Optional<ZonedDateTime> resolveOptionalMonthly(ZonedDateTime reference, LocalTime cutoff, Integer dayOfMonth) {
        if (dayOfMonth == null) {
            return Optional.empty();
        }
        ZonedDateTime candidate = alignMonthly(reference, cutoff, dayOfMonth);
        if (candidate.isAfter(reference)) {
            candidate = alignMonthly(reference.minusMonths(1), cutoff, dayOfMonth);
        }
        return Optional.of(candidate);
    }

    private ZonedDateTime alignMonthly(ZonedDateTime reference, LocalTime cutoff, int dayOfMonth) {
        LocalDate date = reference.toLocalDate();
        int adjustedDay = Math.min(dayOfMonth, date.lengthOfMonth());
        ZonedDateTime candidate = reference.withDayOfMonth(adjustedDay).with(cutoff);
        return candidate;
    }

    private ZonedDateTime determineWeeklyStart(ZonedDateTime periodEnd, SettlementScheduleConfiguration configuration) {
        ZonedDateTime latestCutoff = resolveMultipleWeeklyCutoff(periodEnd.minusSeconds(1), configuration.getCutoffTime(),
                configuration.getPrimaryDayOfWeek(), configuration.getSecondaryDayOfWeek());
        return latestCutoff;
    }

    private ZonedDateTime determineMonthlyStart(ZonedDateTime periodEnd, SettlementScheduleConfiguration configuration) {
        ZonedDateTime latestCutoff = resolveMultipleMonthlyCutoff(periodEnd.minusSeconds(1), configuration.getCutoffTime(),
                configuration.getPrimaryDayOfMonth(), configuration.getSecondaryDayOfMonth());
        return latestCutoff;
    }

    public record SettlementWindow(LocalDateTime periodStart, LocalDateTime periodEnd, ZoneId zoneId) {
    }
}

