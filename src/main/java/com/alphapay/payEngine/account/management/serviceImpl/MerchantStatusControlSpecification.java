package com.alphapay.payEngine.account.management.serviceImpl;

import com.alphapay.payEngine.account.management.dto.request.GetMerchantStatusChanges;
import com.alphapay.payEngine.account.management.model.MerchantStatusHistoryEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;


public class MerchantStatusControlSpecification implements Specification<MerchantStatusHistoryEntity> {

    private static final Logger logger = LoggerFactory.getLogger(MerchantStatusControlSpecification.class);

    private final GetMerchantStatusChanges filter;

    public MerchantStatusControlSpecification(GetMerchantStatusChanges filter) {
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(Root<MerchantStatusHistoryEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        final List<Predicate> predicates = new ArrayList<>();

        if (filter.getMerchantId() != null) {
            predicates.add(criteriaBuilder.equal(root.get("merchantId"), filter.getMerchantId()));
        }
        if (filter.getAssignedUserId() != null) {
            predicates.add(criteriaBuilder.equal(root.get("assignedUserId"), filter.getAssignedUserId()));
        }

        if (filter.getPerformByUserId() != null) {
            predicates.add(criteriaBuilder.equal(root.get("performedByUserId"), filter.getPerformByUserId()));
        }


        if (filter.getPerformedAt() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("creationTime"), filter.getPerformedAt()));
        }

        return (Predicate) andTogether(predicates, criteriaBuilder);
    }

    private Predicate andTogether(List<Predicate> predicates, CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}



