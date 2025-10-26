package com.alphapay.payEngine.account.management.repository;

import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.management.model.UserMfaConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserMfaConfigRepository extends JpaRepository<UserMfaConfig, Long> {

    Optional<UserMfaConfig> findByUserAndMfaType(UserEntity user, String mfaType);

    List<UserMfaConfig> findAllByUser(UserEntity user);

    boolean existsByUserAndMfaTypeAndEnabledTrue(UserEntity user, String mfaType);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserMfaConfig m WHERE m.user = :user AND m.mfaType = :mfaType")
    void deleteByUserAndMfaType(@Param("user") UserEntity user, @Param("mfaType") String mfaType);
}
