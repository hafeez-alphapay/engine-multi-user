package com.alphapay.payEngine.transactionLogging.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {
    FinancialTransaction findByRequestId(String requestId);

    Page<FinancialTransaction> findAll(Specification<FinancialTransaction> specification, Pageable pageable);

    List<FinancialTransaction> findAll(Specification<FinancialTransaction> specification);

    Optional<FinancialTransaction> findByPaymentIdAndTransactionType(String paymentId, String transactionType);
    List<FinancialTransaction> findAllByPaymentIdAndTransactionType(String paymentId, String transactionType);
    List<FinancialTransaction> findAllByPaymentIdAndTransactionTypeAndTransactionStatus(String paymentId, String transactionType,String transactionStatus);
    /**
     * Fetches the most recent transaction matching a given paymentId and transactionType,
     * ordered by lastUpdated descending.
     */
    Optional<FinancialTransaction> findFirstByPaymentIdAndTransactionTypeOrderByLastUpdatedDesc(String paymentId, String transactionType);

    Optional<FinancialTransaction> findByExternalPaymentIdAndTransactionType(String paymentId,String transactionType);
    Optional<FinancialTransaction> findByExternalPaymentIdAndTransactionTypeAndInvoiceStatus(String paymentId,String transactionType, String invoiceStatus);
    Optional<FinancialTransaction> findByPaymentIdAndTransactionTypeAndInvoiceStatusAndCurrency(String paymentId,String transactionType, String invoiceStatus,String currency);
    Optional<FinancialTransaction> findByPaymentIdAndTransactionTypeAndInvoiceStatus(String paymentId,String transactionType, String invoiceStatus);


    Optional<FinancialTransaction> findByExternalInvoiceIdAndTransactionType(String externalInvoiceId,String transactionType);

    Optional<FinancialTransaction> findFirstByPaymentIdAndTransactionTypeAndHttpResponseCodeOrderByLastUpdatedDesc(String paymentId, String transactionType,String httpResponseCode);
    @Query(value = "SELECT * FROM financial_transactions " +
            "WHERE transaction_type = 'ExecutePaymentRequest' " +
            "AND transaction_status = 'InProgress' " +
            "ORDER BY creation_time DESC "  ,
            nativeQuery = true)
    List<FinancialTransaction> findLastFiveExecutePaymentTransactions();

    @Query(value =
            "SELECT * FROM financial_transactions " +
                    "WHERE transaction_type = 'ExecutePaymentRequest' " +
                    "  AND transaction_status = 'InProgress' " +
                    "  AND creation_time <  (CONVERT_TZ(UTC_TIMESTAMP(), 'UTC', '+04:00') - INTERVAL 10 MINUTE) " +
                    "  AND creation_time >  (CONVERT_TZ(UTC_TIMESTAMP(), 'UTC', '+04:00') - INTERVAL 3 DAY) " +
                    "ORDER BY creation_time ASC",
            nativeQuery = true)
    List<FinancialTransaction> findExecutePaymentTransactionsOlderThan10MinutesAndNewerThan3Days();

    List<FinancialTransaction> findByTransactionTimeBetween(Date fromDate, Date toDate);

    @Query(value = """
    SELECT DISTINCT payment_id 
    FROM financial_transactions 
    WHERE (transaction_type = 'DirectPaymentRefundRequest' 
           OR transaction_type = 'PortalRefundRequest') 
      AND invoice_status = 'Pending' 
      AND last_updated <= NOW() - INTERVAL 1 MINUTE
    """, nativeQuery = true)
    List<String> findRefundPendingTransactions();

    List<FinancialTransaction> findByCreationTimeBetweenAndTransactionType(LocalDateTime yesterday, LocalDateTime now,String transactionType);

    List<FinancialTransaction> findAllByPaymentIdAndTransactionTypeIn(
            String paymentId, List<String> transactionTypes
    );


    List<FinancialTransaction> findAllByPaymentIdAndTransactionTypeInAndTransactionStatus(String paymentId,
                                                                                          List<String> transactionTypes,String transactionStatus);
}
