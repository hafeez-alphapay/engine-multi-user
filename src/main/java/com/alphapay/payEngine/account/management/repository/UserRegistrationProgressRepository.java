package com.alphapay.payEngine.account.management.repository;

import com.alphapay.payEngine.account.management.model.UserRegistrationProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRegistrationProgressRepository extends JpaRepository<UserRegistrationProgress, Long> {
}
