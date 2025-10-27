package com.alphapay.payEngine.financial.settlement;

import com.alphapay.payEngine.financial.reserve.RollingReserve;
import com.alphapay.payEngine.financial.reserve.RollingReserveRepository;
import com.alphapay.payEngine.financial.wallet.MerchantWalletBalanceService;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.alphapay.payEngine.financial.settlement.SettlementCycle.ON_DEMAND;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private static final String PAYMENT_TRANSACTION_TYPE = "ExecutePaymentRequest";
    private static final List<String> REFUND_TRANSACTION_TYPES = List.of(
            "DirectPaymentRefundRequest",
            "PortalRefundRequest"
    );
    private static final DateTimeFormatter REFERENCE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final SettlementScheduleService scheduleService;
    private final SettlementRepository settlementRepository;
    private final FinancialTransactionRepository transactionRepository;
    private final MerchantWalletBalanceService walletBalanceService;
    private final RollingReserveRepository rollingReserveRepository;

    @Transactional
    public void processScheduledSettlements(LocalDateTime referenceTime) {
        scheduleService.findAll().stream()
                .filter(configuration -> configuration.getCycle() != ON_DEMAND)
                .sorted(Comparator.comparing(SettlementScheduleConfiguration::getProcessorId))
                .forEach(configuration -> processConfiguration(configuration, referenceTime));
    }

    @Transactional
    public void processOnDemandSettlement(Long processorId, LocalDateTime periodStart, LocalDateTime periodEnd) {
        SettlementScheduleConfiguration configuration = scheduleService.getRequiredConfiguration(processorId);
        SettlementScheduleService.SettlementWindow window =
                new SettlementScheduleService.SettlementWindow(periodStart, periodEnd, java.time.ZoneId.of(configuration.getTimeZone()));
        generateSettlements(configuration, window);
    }

    private void processConfiguration(SettlementScheduleConfiguration configuration, LocalDateTime referenceTime) {
        SettlementScheduleService.SettlementWindow window;
        try {
            window = scheduleService.calculateWindow(configuration, referenceTime);
        } catch (IllegalStateException ex) {
            log.error("Unable to determine settlement window for processor {}: {}", configuration.getProcessorId(), ex.getMessage());
            return;
        }

        if (settlementRepository.existsByProcessorIdAndPeriodEnd(configuration.getProcessorId(), window.periodEnd())) {
            return;
        }

        generateSettlements(configuration, window);
    }

    private void generateSettlements(SettlementScheduleConfiguration configuration,
                                     SettlementScheduleService.SettlementWindow window) {
        List<FinancialTransaction> ledgerEntries = transactionRepository
                .findByProcessorIdAndSettlementIsNullAndCreationTimeGreaterThanEqualAndCreationTimeLessThan(
                        configuration.getProcessorId(),
                        window.periodStart(),
                        window.periodEnd());

        Map<SettlementKey, List<FinancialTransaction>> transactionsByMerchant = ledgerEntries.stream()
                .filter(this::isEligibleForSettlement)
                .collect(Collectors.groupingBy(this::groupingKey));

        if (transactionsByMerchant.isEmpty()) {
            log.debug("No eligible transactions found for processor {} between {} and {}", configuration.getProcessorId(), window.periodStart(), window.periodEnd());
            return;
        }

        transactionsByMerchant.forEach((key, transactions) ->
                createSettlement(configuration, window, key, transactions));
    }

    private void createSettlement(SettlementScheduleConfiguration configuration,
                                  SettlementScheduleService.SettlementWindow window,
                                  SettlementKey key,
                                  List<FinancialTransaction> transactions) {
        if (key.merchantId() == null || !StringUtils.hasText(key.currency())) {
            log.warn("Skipping settlement creation due to missing merchant or currency for processor {}", configuration.getProcessorId());
            return;
        }

        BigDecimal totalSales = sumAmounts(transactions, this::isPayment);
        BigDecimal totalRefunds = sumAmounts(transactions, this::isRefund);
        BigDecimal totalCommission = sumAmounts(transactions, tx -> true, FinancialTransaction::getTotalCharges);
        BigDecimal vat = sumAmounts(transactions, tx -> true, FinancialTransaction::getVat);
        BigDecimal settlementFees = BigDecimal.ZERO;

        BigDecimal reservePercentage = resolveReservePercentage(configuration);
        BigDecimal rollingReserve = totalSales.multiply(reservePercentage).setScale(2, RoundingMode.HALF_UP);
        if (rollingReserve.compareTo(BigDecimal.ZERO) < 0) {
            rollingReserve = BigDecimal.ZERO;
        }

        BigDecimal netPayout = totalSales
                .subtract(totalRefunds)
                .subtract(totalCommission)
                .subtract(settlementFees)
                .subtract(rollingReserve)
                .add(vat);

        Settlement settlement = new Settlement();
        settlement.setSettlementReference(generateReference(configuration.getProcessorId(), key.merchantId(), key.currency(), window.periodEnd()));
        settlement.setProcessorId(configuration.getProcessorId());
        settlement.setMerchantId(key.merchantId());
        settlement.setCurrency(key.currency());
        settlement.setCycle(configuration.getCycle());
        settlement.setPeriodStart(window.periodStart());
        settlement.setPeriodEnd(window.periodEnd());
        settlement.setTotalSales(totalSales);
        settlement.setTotalRefunds(totalRefunds);
        settlement.setTotalCommission(totalCommission);
        settlement.setSettlementFees(settlementFees);
        settlement.setRollingReserve(rollingReserve);
        settlement.setVat(vat);
        settlement.setNetPayout(netPayout);
        settlement.setStatus(SettlementStatus.PENDING);
        settlement.setSettledAt(window.periodEnd());

        Settlement saved = settlementRepository.save(settlement);

        transactions.forEach(transaction -> {
            transaction.setSettlement(saved);
            transaction.setSettledAt(window.periodEnd());
        });
        transactionRepository.saveAll(transactions);

        walletBalanceService.applySettlement(saved);

        persistRollingReserve(configuration, saved, rollingReserve);

        log.info("Generated settlement {} for merchant {} currency {} covering {} - {} (transactions: {})",
                saved.getSettlementReference(), key.merchantId(), key.currency(), window.periodStart(), window.periodEnd(), transactions.size());
    }

    private void persistRollingReserve(SettlementScheduleConfiguration configuration,
                                       Settlement settlement,
                                       BigDecimal rollingReserve) {
        if (rollingReserve == null || rollingReserve.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        Integer holdDays = Optional.ofNullable(configuration.getReserveHoldDays()).orElse(0);
        LocalDate releaseDate = Optional.ofNullable(settlement.getSettledAt())
                .map(LocalDateTime::toLocalDate)
                .orElse(LocalDate.now())
                .plusDays(holdDays.longValue());

        RollingReserve reserve = new RollingReserve();
        reserve.setMerchantId(settlement.getMerchantId());
        reserve.setCurrency(settlement.getCurrency());
        reserve.setSettlement(settlement);
        reserve.setReservePercentage(resolveReservePercentage(configuration));
        reserve.setReserveAmount(rollingReserve);
        reserve.setHoldPeriodDays(holdDays);
        reserve.setReleaseDate(releaseDate);

        rollingReserveRepository.save(reserve);
    }

    private SettlementKey groupingKey(FinancialTransaction transaction) {
        Long merchantId = transaction.getMerchantId();
        String currency = resolveCurrency(transaction);
        return new SettlementKey(merchantId, currency);
    }

    private boolean isEligibleForSettlement(FinancialTransaction transaction) {
        if (transaction == null) {
            return false;
        }
        if (transaction.getSettlement() != null) {
            return false;
        }
        if (transaction.getMerchantId() == null) {
            return false;
        }
        String currency = resolveCurrency(transaction);
        if (!StringUtils.hasText(currency)) {
            return false;
        }
        if (!StringUtils.hasText(transaction.getTransactionType())) {
            return false;
        }
        if (isPayment(transaction)) {
            return "Paid".equalsIgnoreCase(transaction.getInvoiceStatus())
                    && "Success".equalsIgnoreCase(transaction.getTransactionStatus());
        }
        if (isRefund(transaction)) {
            return "Success".equalsIgnoreCase(transaction.getTransactionStatus());
        }
        return false;
    }

    private boolean isPayment(FinancialTransaction transaction) {
        return PAYMENT_TRANSACTION_TYPE.equalsIgnoreCase(transaction.getTransactionType());
    }

    private boolean isRefund(FinancialTransaction transaction) {
        if (!StringUtils.hasText(transaction.getTransactionType())) {
            return false;
        }
        return REFUND_TRANSACTION_TYPES.stream()
                .anyMatch(type -> type.equalsIgnoreCase(transaction.getTransactionType()));
    }

    private BigDecimal sumAmounts(List<FinancialTransaction> transactions,
                                  java.util.function.Predicate<FinancialTransaction> filter) {
        return sumAmounts(transactions, filter, this::resolveAmount);
    }

    private BigDecimal sumAmounts(List<FinancialTransaction> transactions,
                                  java.util.function.Predicate<FinancialTransaction> filter,
                                  java.util.function.Function<FinancialTransaction, BigDecimal> extractor) {
        return transactions.stream()
                .filter(filter)
                .map(extractor)
                .filter(Objects::nonNull)
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal resolveAmount(FinancialTransaction transaction) {
        if (transaction.getPaidCurrencyValue() != null) {
            return transaction.getPaidCurrencyValue();
        }
        return transaction.getAmount();
    }

    private String resolveCurrency(FinancialTransaction transaction) {
        String currency = StringUtils.hasText(transaction.getPaidCurrency())
                ? transaction.getPaidCurrency()
                : transaction.getCurrency();
        return StringUtils.hasText(currency) ? currency.toUpperCase(Locale.ROOT) : null;
    }

    private String generateReference(Long processorId, Long merchantId, String currency, LocalDateTime periodEnd) {
        return String.format("STL-%d-%d-%s-%s",
                processorId,
                merchantId,
                currency,
                REFERENCE_FORMATTER.format(periodEnd));
    }

    private record SettlementKey(Long merchantId, String currency) {
    }

    private BigDecimal resolveReservePercentage(SettlementScheduleConfiguration configuration) {
        BigDecimal percentage = Optional.ofNullable(configuration.getReservePercentage()).orElse(BigDecimal.ZERO);
        if (percentage.compareTo(BigDecimal.ONE) > 0) {
            percentage = percentage.movePointLeft(2);
        }
        if (percentage.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return percentage;
    }
}
