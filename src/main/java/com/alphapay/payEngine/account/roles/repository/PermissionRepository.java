package com.alphapay.payEngine.account.roles.repository;

import com.alphapay.payEngine.account.roles.model.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {
    PermissionEntity findByName(String name);
}