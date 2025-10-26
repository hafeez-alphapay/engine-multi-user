package com.alphapay.payEngine.account.roles.repository;

import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.roles.model.RoleEntity;
import com.alphapay.payEngine.account.roles.model.UserRoleEntity;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {
    Optional<UserRoleEntity> findByUserAndRoleEntity(UserEntity user, RoleEntity roleEntity);
//    List<UserRoleEntity> findByUser(UserEntity user);
    boolean existsByRoleEntity(RoleEntity roleEntity);

    @EntityGraph(attributePaths = {"roleEntity.rolePermissions.permissionEntity"})
    List<UserRoleEntity> findByUser(UserEntity user);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserRoleEntity ur WHERE ur.user = :user AND ur.roleEntity = :roleEntity")
    void deleteByUserAndRoleEntity(@Param("user") UserEntity user, @Param("roleEntity") RoleEntity roleEntity);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserRoleEntity ur WHERE ur.user = :user ")
    void deleteByUser(@Param("user") UserEntity user );
}
