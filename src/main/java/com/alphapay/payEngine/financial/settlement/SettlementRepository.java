package com.alphapay.payEngine.financial.settlement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    boolean existsByProcessorIdAndPeriodEnd(Long processorId, LocalDateTime periodEnd);

}
