package com.alphapay.payEngine.management.data.repository;

import com.alphapay.payEngine.management.data.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApplicationRepository extends JpaRepository<Application, Long> {


    @Query("from Application a where a.applicationId = :applicationId")
    Application getByApplicationId(@Param("applicationId") String applicationId);

    @Query("from Application a where a.authorizationKey = :authorizationKey")
    Application getByAuthorizationKey(@Param("authorizationKey") String authorizationKey);

}
