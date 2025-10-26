package com.alphapay.payEngine.account.management.repository;

import com.alphapay.payEngine.account.management.model.MerchantComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantCommentRepository extends JpaRepository<MerchantComment, Long> {
    List<MerchantComment> findByMerchantId(Long merchantId);
}