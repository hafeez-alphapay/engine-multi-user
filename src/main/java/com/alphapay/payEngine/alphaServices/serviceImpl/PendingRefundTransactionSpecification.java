package com.alphapay.payEngine.alphaServices.serviceImpl;

import com.alphapay.payEngine.alphaServices.model.PendingRefundProcess;
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

public class PendingRefundTransactionSpecification implements Specification<PendingRefundProcess> {

    private static final Logger logger = LoggerFactory.getLogger(com.alphapay.payEngine.alphaServices.historyTransaction.serviceImpl.TransactionHistorySpecification.class);

    private final String status;

    public PendingRefundTransactionSpecification(String status) {
        this.status = status;
    }

    @Override
    public Predicate toPredicate(Root<PendingRefundProcess> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        final List<Predicate> predicates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        if (status != null && !status.isEmpty()) {
            List<Predicate> typePredicates = new ArrayList<>();

            for (String type : status.split(",")) {
                typePredicates.add(criteriaBuilder.like(root.get("status"), "%" + type + "%"));
            }

            predicates.add(criteriaBuilder.or(typePredicates.toArray(new Predicate[0])));
        }


        return andTogether(predicates, criteriaBuilder);
    }

    private Predicate andTogether(List<Predicate> predicates, CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}



