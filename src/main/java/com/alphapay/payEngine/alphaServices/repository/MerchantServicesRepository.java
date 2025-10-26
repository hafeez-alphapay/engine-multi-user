package com.alphapay.payEngine.alphaServices.repository;


import com.alphapay.payEngine.alphaServices.model.AlphaPayServicesEntity;
import com.alphapay.payEngine.alphaServices.model.MerchantAlphaPayServicesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantServicesRepository extends JpaRepository<MerchantAlphaPayServicesEntity, Long> {

    List<MerchantAlphaPayServicesEntity> findByMerchantId(Long merchantId);

    MerchantAlphaPayServicesEntity findByMerchantIdAndAlphaPayServiceAndStatus(Long merchantId, AlphaPayServicesEntity service, String status);

    List<MerchantAlphaPayServicesEntity> findByMerchantIdAndStatus(Long merchantId, String status);

}
