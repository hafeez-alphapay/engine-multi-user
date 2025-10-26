package com.alphapay.payEngine.alphaServices.repository;


import com.alphapay.payEngine.alphaServices.model.Redirect3DSUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Redirect3DSUrlRepository extends JpaRepository<Redirect3DSUrl, String> {

    // Example custom method to find by paymentId
    Redirect3DSUrl findByPaymentId(String paymentId);
}
