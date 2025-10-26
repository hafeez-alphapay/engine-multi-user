package com.alphapay.payEngine.integration.repository;

import com.alphapay.payEngine.integration.model.WebhookLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface WebhookLogRepository extends JpaRepository<WebhookLogEntity, Long> {

    long countByFlatPayloadPriorSignAndResponseCodeIn(
            String flatPayloadPriorSign, Collection<Integer> responseCodes);
}
