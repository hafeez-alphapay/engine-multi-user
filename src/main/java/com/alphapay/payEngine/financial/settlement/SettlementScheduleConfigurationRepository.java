package com.alphapay.payEngine.financial.settlement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementScheduleConfigurationRepository extends JpaRepository<SettlementScheduleConfiguration, Long> {

    Optional<SettlementScheduleConfiguration> findByProcessorId(Long processorId);
}

