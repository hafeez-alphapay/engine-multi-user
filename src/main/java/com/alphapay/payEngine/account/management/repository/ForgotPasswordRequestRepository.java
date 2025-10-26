package com.alphapay.payEngine.account.management.repository;

import com.alphapay.payEngine.account.management.model.ForgotPasswordRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForgotPasswordRequestRepository extends JpaRepository<ForgotPasswordRequestEntity, Long> {

    ForgotPasswordRequestEntity findBySessionId(String resetPasswordId);
}
