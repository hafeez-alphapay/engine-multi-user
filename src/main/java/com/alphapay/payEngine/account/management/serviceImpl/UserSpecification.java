package com.alphapay.payEngine.account.management.serviceImpl;

import com.alphapay.payEngine.account.management.dto.request.GetAllUsersRequestFilter;
import com.alphapay.payEngine.account.management.model.UserEntity;
import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class UserSpecification implements Specification<UserEntity> {

    private static final Logger logger = LoggerFactory.getLogger(UserSpecification.class);

    private final GetAllUsersRequestFilter filter;

    public UserSpecification(GetAllUsersRequestFilter filter) {
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(Root<UserEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        final List<Predicate> predicates = new ArrayList<>();



        if (filter.getFullName() != null) {
            List<Predicate> orPredicates = new ArrayList<>();
            for (String name : filter.getFullName().split(",")) {
                orPredicates.add(criteriaBuilder.like(root.get("userDetails").get("fullName"), "%" + name.trim() + "%"));
            }
            predicates.add(criteriaBuilder.or(orPredicates.toArray(new Predicate[0])));
        }

        if (filter.getMobileNo() != null) {
            List<Predicate> orPredicates = new ArrayList<>();
            for (String name : filter.getMobileNo().split(",")) {
                orPredicates.add(criteriaBuilder.like(root.get("userDetails").get("mobileNo"), "%" + name.trim() + "%"));
            }
            predicates.add(criteriaBuilder.or(orPredicates.toArray(new Predicate[0])));
        }

        if (filter.getEmail() != null) {

            List<Predicate> orPredicates = new ArrayList<>();
            for (String email : filter.getEmail().split(",")) {
                orPredicates.add(criteriaBuilder.like(root.get("userDetails").get("email"), "%" + email.trim() + "%"));
            }
            predicates.add(criteriaBuilder.or(orPredicates.toArray(new Predicate[0])));
        }


        if (filter.getUserId() != null) {
            predicates.add(criteriaBuilder.equal(root.<String>get("id"), filter.getUserId()));
        }
        else {
            //NO User ID .. either list all or specific (in List of subusers)
            if(filter.getSubUserIds() != null && !filter.getSubUserIds().isEmpty()) {
                predicates.add(root.<String>get("id").in(filter.getSubUserIds()));
            }

        }

        if (filter.getFromDate() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("creationTime"), filter.getFromDate()));
        }

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
        if (filter.getRoleId() != null) {
            List<Predicate> orPredicates = new ArrayList<>();
            Join<Object, Object> rolesJoin = root.join("roles", JoinType.INNER);
            for (String roleId : filter.getRoleId().split(",")) {
                orPredicates.add(criteriaBuilder.equal(rolesJoin.get("id"),  roleId ));
            }
            predicates.add(criteriaBuilder.or(orPredicates.toArray(new Predicate[0])));
        }


        if (filter.getAssignTo() != null) {
            predicates.add(criteriaBuilder.equal(root.<String>get("assignTo"), filter.getAssignTo()));
        }


        return (Predicate) andTogether(predicates, criteriaBuilder);
    }

    private Predicate andTogether(List<Predicate> predicates, CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}



