package com.alphapay.payEngine.alphaServices.repository;

import com.alphapay.payEngine.alphaServices.model.MerchantLinkSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantLinkSettingsRepository  extends JpaRepository<MerchantLinkSettings, Long> {
    MerchantLinkSettings findByMerchantIdAndCurrency(Long merchantId, String currency);
}
