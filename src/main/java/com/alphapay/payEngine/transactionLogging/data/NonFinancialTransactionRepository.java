package com.alphapay.payEngine.transactionLogging.data;

import com.alphapay.payEngine.transactionLogging.data.NonFinancialTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NonFinancialTransactionRepository extends JpaRepository<NonFinancialTransaction, Long> {
    NonFinancialTransaction findByRequestId(String requestId);
}
