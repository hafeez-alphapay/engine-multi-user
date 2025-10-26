package com.alphapay.payEngine.account.management.repository;

import com.alphapay.payEngine.account.management.model.EmiratesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmirateRepository extends JpaRepository<EmiratesEntity, Long> {
}
