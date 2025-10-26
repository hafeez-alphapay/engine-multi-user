package com.alphapay.payEngine.integration.repository;

import com.alphapay.payEngine.integration.model.BackEndResponseCodeCategory;
import com.alphapay.payEngine.integration.model.BackEndResponseCodeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BackEndResponseCodeMappingRepository extends JpaRepository<BackEndResponseCodeMapping, Long> {

    List<BackEndResponseCodeMapping> findByCategory(BackEndResponseCodeCategory category);

    List<BackEndResponseCodeMapping> findByAppResponseCode(String appResponseCode);

    List<BackEndResponseCodeMapping> findByExternalResponseCode(String externalResponseCode);
}
