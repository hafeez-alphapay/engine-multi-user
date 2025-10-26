package com.alphapay.payEngine.integration.repository;

import com.alphapay.payEngine.integration.model.PaymentMethodEntity;
import com.alphapay.payEngine.integration.model.orchast.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethodEntity, Long> {
    List<PaymentMethodEntity> findByStatus(String status);
    List<PaymentMethodEntity> findByStatusAndServiceProvider(String status, ServiceProvider serviceProvider);

    Optional<PaymentMethodEntity> findByPaymentMethodId(Integer paymentMethodId);

    Optional<PaymentMethodEntity> findByPaymentMethodEnAndServiceProvider(String paymentMethod, ServiceProvider serviceProvider);
}
