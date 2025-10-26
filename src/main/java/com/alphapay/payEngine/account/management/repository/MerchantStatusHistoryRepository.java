package com.alphapay.payEngine.account.management.repository;

import com.alphapay.payEngine.account.management.model.MerchantStatusHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantStatusHistoryRepository extends JpaRepository<MerchantStatusHistoryEntity, Long> {
    Page<MerchantStatusHistoryEntity> findAll(Specification<MerchantStatusHistoryEntity> specification, Pageable pageable);
}
