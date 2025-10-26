package com.alphapay.payEngine.financial.wallet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantWalletBalanceRepository extends JpaRepository<MerchantWalletBalance, Long> {

    Optional<MerchantWalletBalance> findByMerchantIdAndCurrency(Long merchantId, String currency);
}
