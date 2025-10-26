package com.alphapay.payEngine.account.management.repository;

import com.alphapay.payEngine.account.management.model.BusinessTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessTypeRepository extends JpaRepository<BusinessTypeEntity, Long> {
    @Query("SELECT DISTINCT bt FROM BusinessTypeEntity bt LEFT JOIN FETCH bt.categories")
    List<BusinessTypeEntity> findAllWithCategories();

    Optional<BusinessTypeEntity> findByNameEn(String nameEn);
}
