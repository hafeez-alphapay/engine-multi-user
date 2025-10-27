package com.alphapay.payEngine.financial.service;

import com.alphapay.payEngine.financial.wallet.MerchantWalletBalanceService;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialTransactionLedgerService {

    private final FinancialTransactionRepository financialTransactionRepository;
    private final MerchantWalletBalanceService merchantWalletBalanceService;

    @Transactional
    public FinancialTransaction save(FinancialTransaction transaction) {
        postToWalletIfNeeded(transaction);
        return financialTransactionRepository.save(transaction);
    }

    @Transactional
    public FinancialTransaction saveAndFlush(FinancialTransaction transaction) {
        postToWalletIfNeeded(transaction);
        return financialTransactionRepository.saveAndFlush(transaction);
    }

    private void postToWalletIfNeeded(FinancialTransaction transaction) {
        if (!merchantWalletBalanceService.isEligibleForWalletPosting(transaction)) {
            return;
        }

        try {
            boolean posted = merchantWalletBalanceService.applyTransaction(transaction);
            if (posted) {
                transaction.setPostedToWallet(Boolean.TRUE);
            }
        } catch (Exception ex) {
            log.error("Failed to update merchant wallet for transaction {}", transaction.getId(), ex);
            throw ex;
        }
    }
}
