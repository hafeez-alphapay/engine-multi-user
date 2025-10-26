package com.alphapay.payEngine.integration.serviceImpl;

import com.alphapay.payEngine.integration.service.SimplifiedPaymentGatewayService;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefundProcessingJob {

    private final FinancialTransactionRepository financialTransactionRepository;

    private final SimplifiedPaymentGatewayService paymentGatewayService;

    /**
     * This job runs every x minutes to check for pending DirectPaymentRefundRequest transactions
     * and perform operations on their paymentIds.
     */
    //@Scheduled(fixedRate = 5 * 60 * 1000) // every 2 minutes (in milliseconds)
    @Scheduled(fixedRateString = "${refund.job.fixedRate.ms}")
    public void processPendingRefunds() {
        log.info("🔁 Starting scheduled refund processing job...");

        List<String> paymentIds = financialTransactionRepository.findRefundPendingTransactions();

        if (paymentIds.isEmpty()) {
            log.info("✅ No stale pending refunds found.");
            return;
        }

        log.info("🔍 Found {} pending refund transactions older than 7 minutes.", paymentIds.size());

        for (String paymentId : paymentIds) {
            try {
                log.info("➡️ Processing refund logic for paymentId: {}", paymentId);
                paymentGatewayService.executeRefundJob(paymentId);



            } catch (Exception e) {
                log.error("❌ Error processing refund for paymentId {}: {}", paymentId, e.getMessage(), e);
            }
        }

        log.info("✅ Refund processing job completed.");
    }

}
