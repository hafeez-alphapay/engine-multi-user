package com.alphapay.payEngine.integration.repository;

import com.alphapay.payEngine.integration.model.BackEndResponseCodeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BackEndResponseCodeCategoryRepository extends JpaRepository<BackEndResponseCodeCategory, Long> {

    BackEndResponseCodeCategory findByCategoryName(String categoryName);
}
