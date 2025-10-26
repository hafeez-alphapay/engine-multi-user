package com.alphapay.payEngine.integration.repository;

import com.alphapay.payEngine.integration.model.orchast.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {
    Optional<ServiceProvider> findByServiceId(String name);
    Optional<ServiceProvider> findById(Long id);
}
