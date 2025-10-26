package com.alphapay.payEngine.integration.repository;

import com.alphapay.payEngine.integration.model.CustomMerchantCommissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomMerchantCommissionRepository extends JpaRepository<CustomMerchantCommissionEntity, Long> {

}
