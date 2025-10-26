package com.alphapay.payEngine.common.otp.repositories;

import com.alphapay.payEngine.common.otp.models.OtpDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpRepository extends JpaRepository<OtpDetails,Long> {

    boolean existsByRequestId(String foo);
    OtpDetails findByRequestId(String requestId);
}
