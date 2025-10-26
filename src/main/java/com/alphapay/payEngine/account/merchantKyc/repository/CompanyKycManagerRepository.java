package com.alphapay.payEngine.account.merchantKyc.repository;


import com.alphapay.payEngine.account.merchantKyc.model.MerchantManagersKyc;
import com.alphapay.payEngine.account.merchantKyc.model.MerchantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyKycManagerRepository extends JpaRepository<MerchantManagersKyc, Long> {

    List<MerchantManagersKyc> findByMerchantEntity(MerchantEntity merchantEntity);

    void deleteAllByMerchantEntity(MerchantEntity merchantEntity);
}