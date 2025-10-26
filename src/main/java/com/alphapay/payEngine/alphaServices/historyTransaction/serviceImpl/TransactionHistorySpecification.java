package com.alphapay.payEngine.alphaServices.historyTransaction.serviceImpl;

import com.alphapay.payEngine.alphaServices.historyTransaction.dto.request.TransactionHistoryRequest;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class TransactionHistorySpecification implements Specification<FinancialTransaction> {

    private static final Logger logger = LoggerFactory.getLogger(TransactionHistorySpecification.class);

    private final TransactionHistoryRequest filter;

    public TransactionHistorySpecification(TransactionHistoryRequest filter) {
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(Root<FinancialTransaction> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        final List<Predicate> predicates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
            List<Predicate> typePredicates = new ArrayList<>();

            for (String type : filter.getStatus().split(",")) {
                typePredicates.add(criteriaBuilder.like(root.get("status"), "%" + type + "%"));
            }

            predicates.add(criteriaBuilder.or(typePredicates.toArray(new Predicate[0])));
        }

        if (filter.getPaymentMethod() != null && !filter.getPaymentMethod().isEmpty()) {
            List<Predicate> typePredicates = new ArrayList<>();

            for (String type : filter.getPaymentMethod().split(",")) {
                typePredicates.add(criteriaBuilder.like(root.get("paymentMethod"), "%" + type + "%"));
            }
            predicates.add(criteriaBuilder.or(typePredicates.toArray(new Predicate[0])));
        }

        if (filter.getCurrency() != null && !filter.getCurrency().isEmpty()) {
            List<Predicate> typePredicates = new ArrayList<>();

            for (String type : filter.getCurrency().split(",")) {
                typePredicates.add(criteriaBuilder.like(root.get("currency"), "%" + type + "%"));
            }
            predicates.add(criteriaBuilder.or(typePredicates.toArray(new Predicate[0])));
        }


        if (filter.getAmount() != null) {
            predicates.add(criteriaBuilder.equal(root.get("amount"), filter.getAmount()));
        }

        if (filter.getPaidCurrencyValue() != null) {
            predicates.add(criteriaBuilder.equal(root.get("paidCurrencyValue"), filter.getPaidCurrencyValue()));
        }

        if (filter.getTransactionNumber() != null && !filter.getTransactionNumber().isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("transactionNumber"), "%" + filter.getTransactionNumber() + "%"));
        }

        if (filter.getExternalInvoiceId() != null && !filter.getExternalInvoiceId().isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("externalInvoiceId"), "%" + filter.getExternalInvoiceId() + "%"));
        }

        if (filter.getExternalPaymentId() != null && !filter.getExternalPaymentId().isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("externalPaymentId"), "%" + filter.getExternalPaymentId() + "%"));
        }

        if (filter.getInvoiceLink() != null) {
            predicates.add(criteriaBuilder.equal(root.<String>get("invoiceLink"), filter.getInvoiceLink()));
        }

        if (filter.getMaskedCard() != null) {
            predicates.add(criteriaBuilder.equal(root.<String>get("cardNumber"), "%" +filter.getMaskedCard()+ "%"));
        }

        if (filter.getProcessorId() != null) {
            predicates.add(criteriaBuilder.equal(root.<Long>get("processorId"), filter.getProcessorId()));
        }

        if (filter.getPaymentId() != null && !filter.getPaymentId().isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("paymentId"), "%" + filter.getPaymentId() + "%"));
        }

        if (filter.getTransactionStatus() != null && !filter.getTransactionStatus().isEmpty()) {
            List<Predicate> typePredicates = new ArrayList<>();

            for (String type : filter.getTransactionStatus().split(",")) {
                typePredicates.add(criteriaBuilder.like(root.get("transactionStatus"), "%" + type + "%"));
            }

            predicates.add(criteriaBuilder.or(typePredicates.toArray(new Predicate[0])));
        }


        if (filter.getInvoiceStatus() != null && !filter.getInvoiceStatus().isEmpty()) {
            List<Predicate> typePredicates = new ArrayList<>();

            for (String type : filter.getInvoiceStatus().split(",")) {
                typePredicates.add(criteriaBuilder.like(root.get("invoiceStatus"), "%" + type + "%"));
            }

            predicates.add(criteriaBuilder.or(typePredicates.toArray(new Predicate[0])));
        }


        if (filter.getSubMerchantId() != null) {
            logger.debug("filter.getSubMerchantId------------>{}",filter.getSubMerchantId());
            predicates.add(criteriaBuilder.equal(root.get("invoice").get("merchantUserAccount").get("id"), filter.getSubMerchantId()));
        } else if (filter.getMerchantId() != null) {
            if (filter.getSubMerchantIds() != null && !filter.getSubMerchantIds().isEmpty()) {
                List<Long> filterdMerchantId = filter.getSubMerchantIds();
                filterdMerchantId.add(filter.getMerchantId());
                predicates.add(root.<String>get("merchantId").in(filterdMerchantId));
            } else {
                //use invoice id to get merchant transaction, rely on that every invoice id start with merchantId follow with dash
                predicates.add(criteriaBuilder.like(root.get("invoiceLink"), filter.getMerchantId() + "-" + "%"));
            }
        }

        if (filter.getTransactionType() != null && !filter.getTransactionType().isEmpty()) {
            List<Predicate> typePredicates = new ArrayList<>();

            for (String type : filter.getTransactionType().split(",")) {
                typePredicates.add(criteriaBuilder.like(root.get("transactionType"), "%" + type + "%"));
            }

            predicates.add(criteriaBuilder.or(typePredicates.toArray(new Predicate[0])));
        }

//        if (filter.getInvoiceLink() != null || filter.getMerchantId() != null || filter.getSubMerchantId() != null) {
            predicates.add(criteriaBuilder.isNotNull(root.get("invoice")));
//        }

        if (filter.getExternalPaymentId() != null && !filter.getExternalPaymentId().isEmpty()) {
            predicates.add(criteriaBuilder.isNotNull(root.get("externalPaymentId")));
        }

        if (filter.getFromDate() == null && filter.getToDate() == null) {
            calendar.setTime(today);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            Date defaultToDate = calendar.getTime();

            calendar.setTime(today);
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            Date defaultFromDate = calendar.getTime();

            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("creationTime"), defaultFromDate));
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("creationTime"), defaultToDate));
        } else {

            if (filter.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("creationTime"), filter.getFromDate()));
            }

            if (filter.getToDate() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(filter.getToDate());
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                Date endOfDay = cal.getTime();
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("creationTime"), endOfDay));
            }
        }

        return andTogether(predicates, criteriaBuilder);
    }

    private Predicate andTogether(List<Predicate> predicates, CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}



