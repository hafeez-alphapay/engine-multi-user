package com.alphapay.payEngine.alphaServices.dto.request;

import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class PayLinkSpecification implements Specification<PaymentLinkEntity> {

    private static final Logger logger = LoggerFactory.getLogger(PayLinkSpecification.class);

    private final LinkHistoryRequest filter;

    public PayLinkSpecification(LinkHistoryRequest filter) {
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(Root<PaymentLinkEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        final List<Predicate> predicates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();

        if (filter.getInvoiceStatus() != null && !filter.getInvoiceStatus().isEmpty()) {
            List<Predicate> typePredicates = new ArrayList<>();

            for (String type : filter.getInvoiceStatus().split(",")) {
                typePredicates.add(criteriaBuilder.like(root.get("invoiceStatus"), "%" + type + "%"));
            }

            predicates.add(criteriaBuilder.or(typePredicates.toArray(new Predicate[0])));
        }

        if (filter.getLinkType() != null && !filter.getLinkType().isEmpty()) {
            List<Predicate> typePredicates = new ArrayList<>();

            for (String type : filter.getLinkType().split(",")) {
                typePredicates.add(criteriaBuilder.like(root.get("type"), "%" + type + "%"));
            }

            predicates.add(criteriaBuilder.or(typePredicates.toArray(new Predicate[0])));
        }

        if (filter.getCurrency() != null) {
            predicates.add(criteriaBuilder.equal(root.<String>get("currency"), filter.getCurrency()));
        }

        if (filter.getAmount() != null) {
            predicates.add(criteriaBuilder.equal(root.<Double>get("amount"), filter.getAmount()));
        }

        if (filter.getCustomerName() != null) {
            predicates.add(criteriaBuilder.equal(root.<String>get("customerName"), filter.getCustomerName()));
        }
        if (filter.getPaymentLinkTitle() != null) {
            predicates.add(criteriaBuilder.equal(root.<String>get("paymentLinkTitle"), filter.getPaymentLinkTitle()));
        }
        if (filter.getInvoiceId() != null) {
            predicates.add(criteriaBuilder.equal(root.<String>get("invoiceId"), filter.getInvoiceId()));
        }

        if (filter.getExpiryDate() != null) {
            predicates.add(criteriaBuilder.equal(root.get("expiryDate"), filter.getExpiryDate()));
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

            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdOn"), defaultFromDate));
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdOn"), defaultToDate));
        } else {

            if (filter.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdOn"), filter.getFromDate()));
            }

            if (filter.getToDate() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(filter.getToDate());
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                Date endOfDay = cal.getTime();
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdOn"), endOfDay));
            }
        }

        if (filter.getMerchantId() != null) {
            Join<PayLinkSpecification, UserEntity> merchantUserAccountJoin = root.join("merchantUserAccount");
            predicates.add(criteriaBuilder.equal(merchantUserAccountJoin.get("id"), filter.getMerchantId()));
        }

        return (Predicate) andTogether(predicates, criteriaBuilder);
    }

    private Predicate andTogether(List<Predicate> predicates, CriteriaBuilder cb) {
        return cb.and(predicates.toArray(new Predicate[0]));
    }
}



