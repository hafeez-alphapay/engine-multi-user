package com.alphapay.payEngine.account.management.repository;


import com.alphapay.payEngine.account.management.model.BusinessCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessCategoryRepository extends JpaRepository<BusinessCategoryEntity, Long> {
    List<BusinessCategoryEntity> findByBusinessTypeId(Long businessTypeId);

    Optional<BusinessCategoryEntity> findByNameEn(String nameEn);

}