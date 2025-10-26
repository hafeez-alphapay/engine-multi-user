package com.alphapay.payEngine.alphaServices.repository;

import com.alphapay.payEngine.alphaServices.model.AlphaPayServicesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlphaPayServicesRepository extends JpaRepository<AlphaPayServicesEntity, Long> {

    Optional<AlphaPayServicesEntity> findByServiceId(String serviceId);

    Optional<AlphaPayServicesEntity> findByStatus(String status);

    @Query("SELECT a FROM AlphaPayServicesEntity a WHERE a.serviceId IN :serviceIds")
    List<AlphaPayServicesEntity> findAllByServiceId(@Param("serviceIds") List<String> serviceIds);
}