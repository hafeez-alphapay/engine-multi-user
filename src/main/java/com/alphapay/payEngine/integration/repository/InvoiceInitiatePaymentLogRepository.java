package com.alphapay.payEngine.integration.repository;

import com.alphapay.payEngine.integration.model.InvoiceInitiatePaymentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceInitiatePaymentLogRepository extends JpaRepository<InvoiceInitiatePaymentLog, Long> {
    Optional<InvoiceInitiatePaymentLog> findByPaymentIdAndTransType(String paymentId, String transType);

    Optional<InvoiceInitiatePaymentLog> findByExternalPaymentId(String paymentId);
}
