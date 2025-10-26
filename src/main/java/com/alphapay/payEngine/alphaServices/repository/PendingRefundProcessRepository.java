package com.alphapay.payEngine.alphaServices.repository;


import com.alphapay.payEngine.alphaServices.model.PendingRefundProcess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PendingRefundProcessRepository extends JpaRepository<PendingRefundProcess, Long> {
    List<PendingRefundProcess> findByStatus(String status);

    Page<PendingRefundProcess> findAll(Specification<PendingRefundProcess> specification, Pageable pageable);

    Optional<PendingRefundProcess> findByAlphaRefundId(String alphaRefundId);
}
