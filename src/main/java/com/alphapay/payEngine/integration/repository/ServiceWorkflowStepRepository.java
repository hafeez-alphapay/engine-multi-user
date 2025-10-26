package com.alphapay.payEngine.integration.repository;

import com.alphapay.payEngine.integration.model.orchast.ServiceEntity;
import com.alphapay.payEngine.integration.model.orchast.ServiceWorkflow;
import com.alphapay.payEngine.integration.model.orchast.ServiceWorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceWorkflowStepRepository extends JpaRepository<ServiceWorkflowStep, Long> {
    Optional<ServiceWorkflowStep> findByServiceWorkflowAndService(ServiceWorkflow serviceWorkflow, ServiceEntity service);
}