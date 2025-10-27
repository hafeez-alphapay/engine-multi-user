package com.alphapay.payEngine.financial.wallet;

import com.alphapay.payEngine.financial.exception.MerchantWalletStatusException;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantWalletBalanceService {

    private static final String PAYMENT_TRANSACTION_TYPE = "ExecutePaymentRequest";
    private static final List<String> REFUND_TRANSACTION_TYPES = List.of(
            "DirectPaymentRefundRequest",
            "PortalRefundRequest"
    );

    private final MerchantWalletBalanceRepository walletRepository;

    @Transactional
    public boolean applyTransaction(FinancialTransaction transaction) {
        if (!isEligibleForWalletPosting(transaction)) {
            return false;
        }

        Long merchantId = transaction.getMerchantId();
        String currency = resolveCurrency(transaction);
        if (merchantId == null || !StringUtils.hasText(currency)) {
            log.warn("Skipping wallet update for transaction {} due to missing merchant or currency", transaction.getId());
            return false;
        }

        MerchantWalletBalance wallet = walletRepository
                .findByMerchantIdAndCurrency(merchantId, currency)
                .orElseGet(() -> newWallet(merchantId, currency));

        if (MerchantWalletStatus.CLOSED.equals(wallet.getStatus())) {
            throw new MerchantWalletStatusException(merchantId, currency);
        }

        BigDecimal amount = resolveAmount(transaction);
        if (amount == null || BigDecimal.ZERO.compareTo(amount) == 0) {
            log.warn("Skipping wallet update for transaction {} due to zero amount", transaction.getId());
            return false;
        }

        if (isCreditTransaction(transaction)) {
            wallet.setTotalCredits(safe(wallet.getTotalCredits()).add(amount));
        } else {
            wallet.setTotalDebits(safe(wallet.getTotalDebits()).add(amount));
        }

        BigDecimal reserve = safe(wallet.getRollingReserveHeld());
        wallet.setCurrentBalance(safe(wallet.getTotalCredits())
                .subtract(safe(wallet.getTotalDebits()))
                .subtract(reserve));

        walletRepository.save(wallet);
        return true;
    }

    public boolean isEligibleForWalletPosting(FinancialTransaction transaction) {
        if (transaction == null) {
            return false;
        }
        if (Boolean.TRUE.equals(transaction.getPostedToWallet())) {
            return false;
        }
        if (!StringUtils.hasText(transaction.getTransactionType())) {
            return false;
        }

        if (PAYMENT_TRANSACTION_TYPE.equalsIgnoreCase(transaction.getTransactionType())) {
            return "Paid".equalsIgnoreCase(transaction.getInvoiceStatus())
                    && "Success".equalsIgnoreCase(transaction.getTransactionStatus());
        }

        if (REFUND_TRANSACTION_TYPES.stream()
                .anyMatch(type -> type.equalsIgnoreCase(transaction.getTransactionType()))) {
            return "Success".equalsIgnoreCase(transaction.getTransactionStatus());
        }

        return false;
    }

    private MerchantWalletBalance newWallet(Long merchantId, String currency) {
        MerchantWalletBalance wallet = new MerchantWalletBalance();
        wallet.setMerchantId(merchantId);
        wallet.setCurrency(currency);
        wallet.setTotalCredits(BigDecimal.ZERO);
        wallet.setTotalDebits(BigDecimal.ZERO);
        wallet.setRollingReserveHeld(BigDecimal.ZERO);
        wallet.setCurrentBalance(BigDecimal.ZERO);
        wallet.setStatus(MerchantWalletStatus.ACTIVE);
        return wallet;
    }

    private boolean isCreditTransaction(FinancialTransaction transaction) {
        return PAYMENT_TRANSACTION_TYPE.equalsIgnoreCase(transaction.getTransactionType());
    }

    private BigDecimal resolveAmount(FinancialTransaction transaction) {
        BigDecimal amount = transaction.getPaidCurrencyValue() != null
                ? transaction.getPaidCurrencyValue()
                : transaction.getAmount();
        if (amount == null) {
            return null;
        }
        return amount.abs();
    }

    private String resolveCurrency(FinancialTransaction transaction) {
        String currency = StringUtils.hasText(transaction.getPaidCurrency())
                ? transaction.getPaidCurrency()
                : transaction.getCurrency();
        return StringUtils.hasText(currency) ? currency.toUpperCase() : null;
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
