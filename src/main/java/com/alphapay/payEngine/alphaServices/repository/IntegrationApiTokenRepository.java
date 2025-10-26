package com.alphapay.payEngine.alphaServices.repository;

import com.alphapay.payEngine.alphaServices.model.IntegrationApiToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface IntegrationApiTokenRepository extends JpaRepository<IntegrationApiToken, String> {

    // Pull tokens that will expire before a given threshold (UTC)
    @Query("""
         select t from IntegrationApiToken t
         where t.expiresAtUtc <= :threshold
         """)
    List<IntegrationApiToken> findExpiringBefore(Instant threshold);

    @Query("""
         select t from IntegrationApiToken t
         where t.workFlowId = :workFlowId and t.tokenName = :tokenName
         """)
    IntegrationApiToken findOne(String workFlowId, String tokenName);
}