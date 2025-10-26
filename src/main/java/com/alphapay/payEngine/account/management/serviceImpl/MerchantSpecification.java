package com.alphapay.payEngine.account.management.serviceImpl;

import com.alphapay.payEngine.account.management.dto.request.GetAllUsersRequestFilter;
import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.merchantKyc.model.MerchantEntity;
import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MerchantSpecification implements Specification<MerchantEntity> {

    private static final Logger logger = LoggerFactory.getLogger(MerchantSpecification.class);

    private final GetAllUsersRequestFilter filter;

    public MerchantSpecification(GetAllUsersRequestFilter filter) {
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(Root<MerchantEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        final List<Predicate> predicates = new ArrayList<>();

        if (filter.getTradeNameEnglish() != null) {
            List<Predicate> orPredicates = new ArrayList<>();
            for (String name : filter.getTradeNameEnglish().split(",")) {
                orPredicates.add(criteriaBuilder.like(root.get("tradeNameEn"), "%" + name.trim() + "%"));
            }
            predicates.add(criteriaBuilder.or(orPredicates.toArray(new Predicate[0])));
        }

        if (filter.getAddress() != null) {
            List<Predicate> orPredicates = new ArrayList<>();
            for (String name : filter.getAddress().split(",")) {
                orPredicates.add(criteriaBuilder.like(root.get("businessAddress"), "%" + name.trim() + "%"));
            }
            predicates.add(criteriaBuilder.or(orPredicates.toArray(new Predicate[0])));
        }


        if (filter.getMerchantId() != null) {
            predicates.add(criteriaBuilder.equal(root.<String>get("id"), filter.getMerchantId()));
        }


        if (filter.getFromDate() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.getFromDate()));
        }


        if (filter.getToDate() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(filter.getToDate());
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            Date endOfDay = cal.getTime();
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endOfDay));
        }


        return (Predicate) andTogether(predicates, criteriaBuilder);
    }

    private Predicate andTogether(List<Predicate> predicates, CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}



