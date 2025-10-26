package com.alphapay.payEngine.integration.repository;

import com.alphapay.payEngine.integration.model.BankEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankRepository extends JpaRepository<BankEntity, Long> {
}
