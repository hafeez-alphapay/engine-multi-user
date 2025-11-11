package com.alphapay.payEngine.financial.wallet;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantWalletLedgerRepository extends JpaRepository<MerchantWalletLedgerEntry, Long> {
}
