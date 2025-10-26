package com.alphapay.payEngine.account.merchantKyc.service.impl;

import com.alphapay.payEngine.account.management.exception.PasswordException;
import com.alphapay.payEngine.account.management.exception.ValidationException;
import com.alphapay.payEngine.account.management.model.*;
import com.alphapay.payEngine.account.management.repository.*;
import com.alphapay.payEngine.account.merchantKyc.dto.KycOnboardingRequest;
import com.alphapay.payEngine.account.merchantKyc.dto.KycOnboardingResponse;
import com.alphapay.payEngine.account.merchantKyc.model.MerchantEntity;
import com.alphapay.payEngine.account.merchantKyc.model.MerchantManagersKyc;
import com.alphapay.payEngine.account.merchantKyc.repository.CompanyKycManagerRepository;
import com.alphapay.payEngine.account.merchantKyc.repository.MerchantRepository;
import com.alphapay.payEngine.account.merchantKyc.service.KycOnboardingService;
import com.alphapay.payEngine.integration.model.BankEntity;
import com.alphapay.payEngine.integration.repository.BankRepository;
import com.alphapay.payEngine.utilities.BeanUtility;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of {@link KycOnboardingService} responsible for persisting KYB profiles.
 */
@Service
@RequiredArgsConstructor
public class KycOnboardingServiceImpl implements KycOnboardingService {

    private final MerchantRepository profileRepository;
    private final CompanyKycManagerRepository managerRepository;
    private final MerchantRegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;
    private final CountryRepository countryRepository;

    private final BusinessCategoryRepository businessCategoryRepository;
    private final BusinessTypeRepository businessTypeRepository;

    private final BankRepository bankRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public KycOnboardingResponse onboardMerchant(KycOnboardingRequest request) {

        Optional<BankEntity> bank = bankRepository.findById(request.getBankId());//.orElseThrow(() -> new ValidationException("Invalid businessTpe: " + request.getBusinessType()));

        MerchantEntity profile = new MerchantEntity();

        profile.setCommercialLicenseNumber(request.getCommercialLicenseNumber());
        profile.setCommercialLicenseExpiry(request.getCommercialLicenseExpiryDate());
        profile.setBusinessLegalAddress(request.getBusinessLegalAddress());
        profile.setWebsiteUrls(toJson(request.getWebsiteUrls()));
        profile.setSocialMediaUrls(toJson(request.getSocialMediaUrls()));
        profile.setRequiredServices(toJson(request.getRequiredServices()));
        profile.setCurrentPaymentGateway(request.getCurrentPaymentGateway());
        profile.setAcceptedCardTypes(request.getAcceptedCardTypes());
        profile.setCardPaymentMethods(toJson(request.getCardPaymentMethods()));
        profile.setProcessingCurrencies(toJson(filterTrueKeys(request.getProcessingCurrencies())));
        profile.setSettlementCurrencies(toJson(filterTrueKeys(request.getSettlementCurrencies())));
        profile.setAvgOrderPrice(request.getAvgOrderPrice());
        profile.setMaxOrderPrice(request.getMaxOrderPrice());
        profile.setNoOfOrdersMonthly(request.getNoOfOrdersMonthly());
        profile.setVolumeOfOrdersMonthly(request.getVolumeOfOrdersMonthly());
        profile.setAnnualIncome(request.getAnnualIncome());
        profile.setEstimatedCardTransVolume(request.getEstimatedCardTransVolume());
        profile.setAvgTurnoverValue(request.getAvgTurnoverValue());
        profile.setAvgTurnoverCount(request.getAvgTurnoverCount());
        profile.setRefundValue(request.getRefundValue());
        profile.setRefundCount(request.getRefundCount());
        profile.setCashbackValue(request.getCashbackValue());
        profile.setCashbackCount(request.getCashbackCount());
        profile.setUaeTarget(request.getUaeTarget());
        profile.setEuTarget(request.getEuTarget());
        profile.setUkTarget(request.getUkTarget());
        profile.setUsTarget(request.getUsTarget());
        profile.setRowTarget(request.getRowTarget());

        // Extended fields
        profile.setTradeNameAr(request.getTradeNameAr());
        profile.setTradeNameEn(request.getTradeNameEn());
        profile.setLegalName(request.getLegalName());
        BusinessTypeEntity type1 = businessTypeRepository.findByNameEn(request.getBusinessType())
                .orElseThrow(() -> new ValidationException("Invalid businessTpe: " + request.getBusinessType()));
        BusinessCategoryEntity category1 = businessCategoryRepository.findByNameEn(request.getBusinessCategory())
                .orElseThrow(() -> new ValidationException("Invalid businessCategory: " + request.getBusinessCategory()));

        profile.setBusinessType(type1);
        profile.setBusinessCategory(category1);
        profile.setBusinessActivity(request.getBusinessActivity());
        profile.setOtherBusinessActivity(request.getOtherbusinessActivity());
        profile.setBusinessPhysicalAddress(request.getBusinessPhysicalAddress());
        profile.setEmirate(request.getEmirate());
        profile.setBusinessPhoneNumber(request.getBusinessPhoneNumber());
        profile.setOfficeEmailAddress(request.getOfficeEmailAddress());
        bank.ifPresent(bankEntity -> profile.setBankName(bankEntity.getBankName()));
        profile.setBankAccountName(request.getBankAccountName());
        profile.setBankAccount(request.getBankAccount());
        profile.setBankIban(request.getBankIban());
        profile.setCurrentlyAcceptCardPayments(request.getCurrentlyAcceptCardPayments());

        MerchantEntity savedProfile = profileRepository.save(profile);

        registerMerchant(request, savedProfile.getId());

        List<MerchantManagersKyc> managers = request.getManagersList().stream()
                .map(dto -> {
                    MerchantManagersKyc manager = new MerchantManagersKyc();
                    manager.setMerchantEntity(savedProfile);
                    manager.setFullName(dto.getFullName());
                    manager.setIdNumber(dto.getIdNumber());
                    manager.setIdExpiry(dto.getIdExpiry());
                    manager.setDob(dto.getDob());
                    CountriesEntity country = countryRepository.findByNameEn(dto.getNationality())
                            .orElseThrow(() -> new ValidationException("Invalid nationality: " + dto.getNationality()));
                    manager.setNationality(country);
                    manager.setAddress(dto.getAddress());
                    manager.setPersonType(dto.getPersonType());
                    manager.setIsShareholder(dto.getIsShareholder());
                    manager.setOwnershipType(dto.getOwnershipType());
                    manager.setOwnershipPercentage(dto.getOwnershipPercentage());
                    manager.setPosition(dto.getPosition());
                    manager.setIncomeSource(dto.getIncomeSource());
                    manager.setPhone(dto.getPhone());
                    manager.setEmail(dto.getEmail());
                    return manager;
                }).collect(Collectors.toList());

        managerRepository.saveAll(managers);

        KycOnboardingResponse baseResponse = new KycOnboardingResponse();
        BeanUtility.copyProperties(request, baseResponse);
//        baseResponse.setMerchantId(savedProfile.getUser().getId());
        baseResponse.setResponseCode(0);
        baseResponse.setResponseMessage("Merchant onboarding successful");
        baseResponse.setStatus("success");
        return baseResponse;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing to JSON", e);
        }
    }


    private List<String> filterTrueKeys(Map<String, Boolean> map) {
        return map.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .toList();
    }

    UserEntity registerMerchant(KycOnboardingRequest request, Long merchantProfileId) {
        MerchantRegistration registration = new MerchantRegistration();
        UserDetails details = new UserDetails();
        registration.setRequestId(request.getRequestId());
        registration.setRegistrationId(request.getRequestId());
//        details.setBankAccountName(request.getBankAccountName());
//        details.setBankName(request.getBankId());
//        details.setIban(request.getBankIban() != null ? request.getBankIban() : "");
//        details.setTradeNameEnglish(request.getTradeNameEn() != null ? request.getTradeNameEn() : "");
//        details.setBusinessCategory(request.getBusinessCategory() != null ? request.getBusinessCategory() : "");
//        details.setBusinessType(
//                request.getBusinessType() != null ? request.getBusinessType() : ""
//        );
        details.setEmail(request.getUserEmail());
        details.setFullName(request.getLegalName());
//        details.setOwnerEmail(request.getOfficeEmailAddress());
//        details.setNationality(request.getManagersList().get(0).getNationality());
//        details.setOwnerMobileNumber(request.getManagersList().get(0).getPhone());
//        details.setTradeNameEnglish(request.getTradeNameEn());


        if (!request.getUserPassword().equals(request.getConfirmUserPassword())) {
            throw new PasswordException();
        }
        BeanUtility.copyProperties(request, details);
        String encryptedPassword = passwordEncoder.encode(request.getUserPassword());
        details.setPassword(encryptedPassword);
//        details.setEmirate(request.getUserResidenceEmirate() != null ? request.getUserResidenceEmirate() : "TO BE DETERMINED");
//        details.setBusinessCategory(request.getBusinessCategory() != null ? request.getBusinessCategory() : "");
//        details.setBusinessAddress(request.getBusinessPhysicalAddress() != null ? request.getBusinessPhysicalAddress() : "");
        registration.setUserDetails(details);
        registration.setEnabled(false);
        registration.setStatus("Active");
        registration = registrationRepository.save(registration);


        UserEntity user = new UserEntity();
        user.setStatus("Active");
        user.setEnabled(true);
        BeanUtils.copyProperties(registration, user, "id", "version");
        user.setUserDetails(registration.getUserDetails());
        user.setRegistrationId(registration.getId());
        user.setActivationDate(new Date());
        user.setEnabled(true);
        user.setRequestId(request.getRequestId());
//        user.setAdminApproveStatus("PENDING");
//        user.setManagerApproveStatus("PENDING");
        //user.setMbmeApproveStatus("PENDING");
        //user.setMyfattoraApproveStatus("PENDING");
        if (request.getMerchantId() != null)
            user.setParentUser(
                    userRepository.findById(request.getMerchantId())
                            .orElseThrow(() -> new ValidationException("Parent merchant not found"))
            );
        UserEntity savedUser = userRepository.save(user);
        return savedUser;
    }
}