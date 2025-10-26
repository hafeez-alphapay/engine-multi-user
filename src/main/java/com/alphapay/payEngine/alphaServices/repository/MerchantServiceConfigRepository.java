package com.alphapay.payEngine.alphaServices.repository;

import com.alphapay.payEngine.alphaServices.model.MerchantServiceConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantServiceConfigRepository extends JpaRepository<MerchantServiceConfigEntity, Long> {
    Optional<MerchantServiceConfigEntity> findByMerchantId(Long merchantId);
    MerchantServiceConfigEntity findByApiKey(String apiKey);

}
