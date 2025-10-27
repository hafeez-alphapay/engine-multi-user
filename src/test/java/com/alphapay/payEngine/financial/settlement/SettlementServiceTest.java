package com.alphapay.payEngine.financial.settlement;

import com.alphapay.payEngine.financial.reserve.RollingReserve;
import com.alphapay.payEngine.financial.reserve.RollingReserveRepository;
import com.alphapay.payEngine.financial.wallet.MerchantWalletBalanceService;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransactionRepository;
import com.alphapay.payEngine.financial.settlement.Settlement;
import com.alphapay.payEngine.financial.settlement.SettlementCycle;
import com.alphapay.payEngine.financial.settlement.SettlementRepository;
import com.alphapay.payEngine.financial.settlement.SettlementScheduleConfiguration;
import com.alphapay.payEngine.financial.settlement.SettlementScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private SettlementScheduleService scheduleService;
    @Mock
    private SettlementRepository settlementRepository;
    @Mock
    private FinancialTransactionRepository financialTransactionRepository;
    @Mock
    private MerchantWalletBalanceService walletBalanceService;
    @Mock
    private RollingReserveRepository rollingReserveRepository;

    @InjectMocks
    private SettlementService settlementService;

    private SettlementScheduleConfiguration configuration;
    private SettlementScheduleService.SettlementWindow window;

    @BeforeEach
    void setUp() {
        configuration = new SettlementScheduleConfiguration();
        configuration.setProcessorId(10L);
        configuration.setCycle(SettlementCycle.DAILY);
        configuration.setCutoffTime(LocalTime.NOON);
        configuration.setTimeZone("UTC");
        configuration.setReservePercentage(new BigDecimal("0.10"));
        configuration.setReserveHoldDays(30);

        window = new SettlementScheduleService.SettlementWindow(
                LocalDateTime.of(2024, 1, 1, 12, 0),
                LocalDateTime.of(2024, 1, 2, 12, 0),
                ZoneId.of("UTC"));
    }

    @Test
    void generatesSettlementForScheduledProcessor() {
        FinancialTransaction payment = buildTransaction(99L, "USD", "ExecutePaymentRequest",
                new BigDecimal("100.00"), "Success", "Paid");
        payment.setTotalCharges(new BigDecimal("2.00"));
        payment.setVat(new BigDecimal("5.00"));

        FinancialTransaction refund = buildTransaction(99L, "USD", "DirectPaymentRefundRequest",
                new BigDecimal("20.00"), "Success", "Paid");

        when(scheduleService.findAll()).thenReturn(List.of(configuration));
        when(scheduleService.calculateWindow(eq(configuration), any(LocalDateTime.class))).thenReturn(window);
        when(settlementRepository.existsByProcessorIdAndPeriodEnd(configuration.getProcessorId(), window.periodEnd())).thenReturn(false);
        when(financialTransactionRepository.findByProcessorIdAndSettlementIsNullAndCreationTimeGreaterThanEqualAndCreationTimeLessThan(
                configuration.getProcessorId(), window.periodStart(), window.periodEnd()))
                .thenReturn(List.of(payment, refund));

        ArgumentCaptor<Settlement> settlementCaptor = ArgumentCaptor.forClass(Settlement.class);
        when(settlementRepository.save(settlementCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(financialTransactionRepository.saveAll(any())).thenReturn(List.of(payment, refund));

        settlementService.processScheduledSettlements(window.periodEnd().plusMinutes(5));

        Settlement saved = settlementCaptor.getValue();
        assertThat(saved.getMerchantId()).isEqualTo(99L);
        assertThat(saved.getProcessorId()).isEqualTo(10L);
        assertThat(saved.getCurrency()).isEqualTo("USD");
        assertThat(saved.getTotalSales()).isEqualByComparingTo("100.00");
        assertThat(saved.getTotalRefunds()).isEqualByComparingTo("20.00");
        assertThat(saved.getTotalCommission()).isEqualByComparingTo("2.00");
        assertThat(saved.getVat()).isEqualByComparingTo("5.00");
        assertThat(saved.getRollingReserve()).isEqualByComparingTo("10.00");
        assertThat(saved.getNetPayout()).isEqualByComparingTo("73.00");
        assertThat(saved.getSettledAt()).isEqualTo(window.periodEnd());

        verify(walletBalanceService).applySettlement(saved);

        ArgumentCaptor<RollingReserve> reserveCaptor = ArgumentCaptor.forClass(RollingReserve.class);
        verify(rollingReserveRepository).save(reserveCaptor.capture());
        assertThat(reserveCaptor.getValue().getReserveAmount()).isEqualByComparingTo("10.00");
        assertThat(reserveCaptor.getValue().getHoldPeriodDays()).isEqualTo(30);

        verify(financialTransactionRepository).saveAll(any());
    }

    @Test
    void skipsWhenNoEligibleTransactions() {
        when(scheduleService.findAll()).thenReturn(List.of(configuration));
        when(scheduleService.calculateWindow(eq(configuration), any(LocalDateTime.class))).thenReturn(window);
        when(settlementRepository.existsByProcessorIdAndPeriodEnd(configuration.getProcessorId(), window.periodEnd())).thenReturn(false);
        when(financialTransactionRepository.findByProcessorIdAndSettlementIsNullAndCreationTimeGreaterThanEqualAndCreationTimeLessThan(
                configuration.getProcessorId(), window.periodStart(), window.periodEnd()))
                .thenReturn(List.of());

        settlementService.processScheduledSettlements(window.periodEnd().plusMinutes(5));

        verify(settlementRepository, never()).save(any());
        verify(walletBalanceService, never()).applySettlement(any());
        verify(rollingReserveRepository, never()).save(any());
    }

    private FinancialTransaction buildTransaction(Long merchantId,
                                                   String currency,
                                                   String type,
                                                   BigDecimal amount,
                                                   String status,
                                                   String invoiceStatus) {
        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setMerchantId(merchantId);
        transaction.setPaidCurrency(currency);
        transaction.setTransactionType(type);
        transaction.setPaidCurrencyValue(amount);
        transaction.setTransactionStatus(status);
        transaction.setInvoiceStatus(invoiceStatus);
        transaction.setCreationTime(window.periodStart().plusHours(1));
        return transaction;
    }
}
