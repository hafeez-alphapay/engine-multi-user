package com.alphapay.payEngine.integration.repository;

import com.alphapay.payEngine.account.management.model.MerchantProviders;
import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.integration.model.MerchantPaymentProviderRegistration;
import com.alphapay.payEngine.integration.model.orchast.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantProviderRegistrationRepository extends JpaRepository<MerchantPaymentProviderRegistration, Long> {
    List<MerchantPaymentProviderRegistration> findByMerchantIdAndIsDefault(Long merchantId,Boolean isDefault);
    List<MerchantPaymentProviderRegistration> findByMerchantIdAndStatusAndIsDefault(Long merchantId,String status,Boolean isDefault);

    List<MerchantPaymentProviderRegistration> findByMerchantIdAndStatus(Long merchantId,String status);
    List<MerchantPaymentProviderRegistration> findByMerchantId(Long merchantId);

    List<MerchantPaymentProviderRegistration> findByMerchantExternalIdAndStatus(String merchantExternalId,String status);

    Optional<MerchantPaymentProviderRegistration> findByServiceProviderAndMerchantId(ServiceProvider serviceProvider,Long merchantId);
    Optional<MerchantPaymentProviderRegistration> findBySupplierCode(Integer supplierCode);
    Optional<MerchantPaymentProviderRegistration> findFirstByMerchantIdAndIsDefaultTrue(Long merchantId);

    @Query("""
               select u.tradeNameEn as tradeNameEn, mpr.merchantId as id
               from MerchantPaymentProviderRegistration mpr
               join MerchantEntity u on u.id = mpr.merchantId
               where mpr.serviceProvider.id = :serviceProviderId
            """)
    List<MerchantProviders> findUsersAndRegistrationsByServiceProviderId(@Param("serviceProviderId") Long serviceProviderId);
}