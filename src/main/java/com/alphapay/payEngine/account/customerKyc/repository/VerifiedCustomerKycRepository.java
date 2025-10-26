package com.alphapay.payEngine.account.customerKyc.repository;

import com.alphapay.payEngine.account.customerKyc.model.VerifiedCustomerKycEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerifiedCustomerKycRepository extends JpaRepository<VerifiedCustomerKycEntity, Long> {
}
