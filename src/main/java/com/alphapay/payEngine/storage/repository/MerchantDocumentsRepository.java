package com.alphapay.payEngine.storage.repository;

import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.merchantKyc.model.MerchantEntity;
import com.alphapay.payEngine.storage.model.MerchantDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantDocumentsRepository extends JpaRepository<MerchantDocuments, Long> {
    Optional<MerchantDocuments> findByDocumentCategoryIdAndMerchantUserAccount(Long documentCategoryId, MerchantEntity merchantUserAccount);

    @Query("SELECT m FROM MerchantDocuments m WHERE m.documentCategoryId IN :categoryIds")
    List<MerchantDocuments> findByDocumentCategoryIds(List<Long> categoryIds);

    List<MerchantDocuments> findByMerchantUserAccount(MerchantEntity merchantUserAccount);
}
