package com.alphapay.payEngine.account.management.repository;

import com.alphapay.payEngine.account.management.model.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query("from UserEntity u where u.userDetails.email = :email")
    UserEntity findByEmail(@Param("email") String email);

    @Query("from UserEntity u where u.userDetails.mobileNo = :mobileNo")
    UserEntity findByMobileNo(@Param("mobileNo") String mobileNo);


    Page<UserEntity> findAll(Specification<UserEntity> specification, Pageable pageable);

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.lastUpdated >= :since")
    long countActiveUsersSince(@Param("since") LocalDateTime since);





    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.lastUpdated >= :since AND u.parentUser = :parentId")
    long countActiveVendorUsersSince(@Param("since") LocalDateTime since, UserEntity parentId);

    //    @Query("from UserEntity u where u.userDetails.cif = :cif and u.applicationId = :applicationId")
//    UserEntity findByCifAndApplicationId(@Param("cif") String cif, @Param("applicationId") String applicationId);
//
//    @Query("from UserEntity u where u.deviceID = :deviceID and u.applicationId = :applicationId")
//    List<UserEntity> findByDeviceIdAndApplication(@Param("deviceID") String cif, @Param("applicationId") String applicationId);
//
//    @Query("from UserEntity u where u.userDetails.mobileNo = :name OR  u.userDetails.email = :name")
//    UserEntity findByMobileNoOrEmail(@Param("name") String name);
//
//    List<UserEntity> findAll(Specification<UserEntity> specification);

    @Query("SELECT u FROM UserEntity u JOIN u.roles r WHERE r.name = :roleName AND u.id = :userId")
    List<UserEntity> findByIdAndRoleName(@Param("roleName") String roleName,Long userId);

    @Query("SELECT u FROM UserEntity u JOIN u.roles r WHERE r.name = :roleName")
    List<UserEntity> findByRoleName(@Param("roleName") String roleName);

    List<UserEntity> findByCreationTimeBetween(LocalDateTime yesterday, LocalDateTime now);
}
