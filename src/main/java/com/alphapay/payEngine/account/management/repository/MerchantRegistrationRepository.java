package com.alphapay.payEngine.account.management.repository;

import com.alphapay.payEngine.account.management.model.MerchantRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MerchantRegistrationRepository extends JpaRepository<MerchantRegistration, Long> {
    @Query("from MerchantRegistration u where u.status=:status and u.userDetails.email = :email and u.userDetails.mobileNo = :mobileNo ")
    List<MerchantRegistration> findUserByStatusAndEmailAndMobileNo(String status, String email,String mobileNo);

    @Query("from MerchantRegistration u where u.requestId=:requestId")
    MerchantRegistration findByRequestId(String requestId);

    @Query("from MerchantRegistration u where u.registrationId=:registrationId")
    Optional<MerchantRegistration> findByRegistrationId(String registrationId);
}
