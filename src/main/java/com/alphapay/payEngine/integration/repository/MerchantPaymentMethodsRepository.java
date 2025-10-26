package com.alphapay.payEngine.integration.repository;

import com.alphapay.payEngine.integration.model.MerchantPaymentMethodsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantPaymentMethodsRepository extends JpaRepository<MerchantPaymentMethodsEntity, Long> {
    List<MerchantPaymentMethodsEntity> findByUserIdAndStatus(Long userId, String status);
    List<MerchantPaymentMethodsEntity> findByUserId(Long userId);
    @Query("""
        SELECT m FROM MerchantPaymentMethodsEntity m
        WHERE m.userId = :userId
          AND m.status = :status
          AND m.paymentMethod.serviceProvider.id = :defaultProviderId
          AND m.paymentMethod.paymentMethodCode IN :paymentMethodsCode
    """)
    List<MerchantPaymentMethodsEntity> findByUserIdAndStatusOrderByDefaultProviderFirst(
            @Param("userId") Long userId,
            @Param("status") String status,
            @Param("defaultProviderId") Long defaultProviderId,
            @Param("paymentMethodsCode") String[] paymentMethodsCode);
}

