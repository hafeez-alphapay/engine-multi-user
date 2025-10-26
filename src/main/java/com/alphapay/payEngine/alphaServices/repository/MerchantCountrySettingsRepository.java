package com.alphapay.payEngine.alphaServices.repository;

import com.alphapay.payEngine.alphaServices.model.MerchantCountrySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantCountrySettingsRepository extends JpaRepository<MerchantCountrySettings, Long> {

    Optional<MerchantCountrySettings> findByMerchantIdAndIsoCountryCode(Long merchantId, String isoCountryCode);

    boolean existsByMerchantIdAndIsoCountryCodeAndStatus(Long merchantId, String isoCountryCode, String status);
}
