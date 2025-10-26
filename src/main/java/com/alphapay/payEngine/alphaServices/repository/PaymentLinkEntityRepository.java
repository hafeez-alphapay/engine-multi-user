package com.alphapay.payEngine.alphaServices.repository;

import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentLinkEntityRepository extends JpaRepository<PaymentLinkEntity,Long> {
    Page<PaymentLinkEntity> findAll(Specification<PaymentLinkEntity> specification, Pageable pageable);
    List<PaymentLinkEntity> findAll(Specification<PaymentLinkEntity> specification, Sort sort);
    Optional<PaymentLinkEntity> findByInvoiceId(String invoiceId);

    @Query("SELECT p FROM PaymentLinkEntity p LEFT JOIN FETCH p.invoiceItems WHERE p.invoiceId = :invoiceId")
    Optional<PaymentLinkEntity> findByInvoiceIdWithInvoiceItems(@Param("invoiceId") String invoiceId);

    @Query("SELECT p FROM PaymentLinkEntity p LEFT JOIN FETCH p.invoiceItems WHERE p.paymentId = :paymentId")
    Optional<PaymentLinkEntity> findByPaymentIdWithInvoiceItems(@Param("paymentId") String paymentId);

    List<PaymentLinkEntity> findByInvoiceStatus(String active);

    Optional<PaymentLinkEntity> findByExternalPaymentId(String paymentId);

    long countByInvoiceStatus(String invoiceStatus);
    long countByInvoiceStatusAndCreatedBy(String invoiceStatus,Long createdBy);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM PaymentLinkEntity i WHERE i.invoiceStatus = :invoiceStatus")
    BigDecimal sumAmountByStatus(@Param("invoiceStatus") String invoiceStatus);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM PaymentLinkEntity i WHERE i.invoiceStatus = :invoiceStatus AND i.createdBy = :createdBy")
    BigDecimal sumAmountByStatusAndCreatedBy(@Param("invoiceStatus") String invoiceStatus,@Param("createdBy") Long createdBy);
}
