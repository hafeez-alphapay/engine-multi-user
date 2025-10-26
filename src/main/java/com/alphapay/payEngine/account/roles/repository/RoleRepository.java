package com.alphapay.payEngine.account.roles.repository;

import com.alphapay.payEngine.account.roles.model.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    RoleEntity findByName(String name);
    boolean existsByName(String name);
}