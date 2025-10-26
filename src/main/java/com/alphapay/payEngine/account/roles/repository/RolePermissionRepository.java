package com.alphapay.payEngine.account.roles.repository;

import com.alphapay.payEngine.account.roles.model.RoleEntity;
import com.alphapay.payEngine.account.roles.model.RolePermissionEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, Long> {
    List<RolePermissionEntity> findByRoleEntity(RoleEntity role);

    @Transactional
    @Modifying
    @Query("DELETE FROM RolePermissionEntity rp WHERE rp.roleEntity = :roleEntity")
    void deleteByRoleEntity(@Param("roleEntity") RoleEntity roleEntity);
}
