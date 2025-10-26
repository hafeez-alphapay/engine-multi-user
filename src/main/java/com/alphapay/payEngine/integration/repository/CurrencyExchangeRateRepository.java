package com.alphapay.payEngine.integration.repository;

import com.alphapay.payEngine.integration.model.CurrencyExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyExchangeRateRepository extends JpaRepository<CurrencyExchangeRate, Long> {
    Optional<CurrencyExchangeRate> findByCurrencyAndStatus(String currency,String status);
    List<CurrencyExchangeRate> findByStatus(String status);
}
