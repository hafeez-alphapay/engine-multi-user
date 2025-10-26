package com.alphapay.payEngine.account.merchantKyc.repository;

import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.merchantKyc.model.MerchantEntity;
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
public interface MerchantRepository extends JpaRepository<MerchantEntity, Long> {

    @Query("from MerchantEntity u where u.billerClientId = :billerClientId")
    Optional<MerchantEntity> findByBillerClientId(Long billerClientId);

    List<MerchantEntity> findByOwnerUserId(Long ownerUserId);

    boolean existsByOwnerUserId(Long ownerUserId);

    Optional<MerchantEntity> findByOwnerUser(UserEntity ownerUser);

    List<MerchantEntity> findAllByOwnerUser(UserEntity ownerUser);

    Optional<MerchantEntity> findByOwnerUser_Id(Long ownerUserId);

    Page<MerchantEntity> findAll(Specification<MerchantEntity> specification, Pageable pageable);

//    @Query("SELECT COUNT(u) FROM MerchantEntity u WHERE u.lastUpdated >= :since AND u.parentUser = :parentId")
//    long countActiveVendorUsersSince(@Param("since") LocalDateTime since, UserEntity parentId);
//
//    @Query("SELECT COUNT(u) FROM MerchantEntity u WHERE u.lastUpdated >= :since")
//    long countActiveUsersSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(u) FROM MerchantEntity u WHERE u.mbmeApproveStatus = 'Approved'")
    long countApprovedMbme();

    @Query("SELECT COUNT(u) FROM MerchantEntity u WHERE u.myfattoraApproveStatus = 'Approved'")
    long countApprovedMyfattora();

    @Query("SELECT COUNT(u) FROM MerchantEntity u WHERE u.mbmeApproveStatus = 'Approved' AND u.parentMerchant = :parentMerchant")
    long countVendorApprovedMbme(MerchantEntity parentMerchant);

    @Query("SELECT COUNT(u) FROM MerchantEntity u WHERE u.myfattoraApproveStatus = 'Approved' AND u.parentMerchant = :parentMerchant")
    long countVendorApprovedMyfattora(MerchantEntity parentMerchant);

    @Query("SELECT m FROM MerchantEntity m WHERE m.parentMerchant IS NOT NULL")
    List<MerchantEntity> findAllWithParentMerchant();

}
