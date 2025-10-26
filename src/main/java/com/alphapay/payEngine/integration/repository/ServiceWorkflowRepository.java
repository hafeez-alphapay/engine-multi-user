package com.alphapay.payEngine.integration.repository;

import com.alphapay.payEngine.integration.model.orchast.ServiceWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceWorkflowRepository extends JpaRepository<ServiceWorkflow, Long> {
    // Find a workflow by name
    Optional<ServiceWorkflow> findByName(String name);
}