package com.alphapay.payEngine.integration.repository;

import com.alphapay.payEngine.integration.model.orchast.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    Optional<ServiceEntity> findByServiceId(String name);
}
