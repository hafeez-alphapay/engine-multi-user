package com.alphapay.payEngine.account.management.service;

import com.alphapay.payEngine.account.management.dto.response.MerchantManagersResponse;
import com.alphapay.payEngine.account.management.dto.response.MerchantResponse;
import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.merchantKyc.model.MerchantEntity;
import com.alphapay.payEngine.account.merchantKyc.model.MerchantManagersKyc;
import com.alphapay.payEngine.utilities.BeanUtility;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MerchantMapperService {
    public MerchantResponse convertToDto(MerchantEntity merchant) {
        MerchantResponse dto = new MerchantResponse();

        // Basic IDs
        dto.setId(merchant.getId());
        dto.setBillerClientId(merchant.getBillerClientId());
        dto.setBillerClientStatus(merchant.getBillerClientStatus());
        dto.setLogo(merchant.getLogo());

        List<MerchantManagersResponse> merchantManagersResponse = new ArrayList<>();
        for (MerchantManagersKyc manager : merchant.getManagers()) {
            MerchantManagersResponse merchantManager = new MerchantManagersResponse();
            BeanUtility.copyProperties(manager, merchantManager);
            merchantManager.setNationality(manager.getNationality().getNameEn());
            merchantManagersResponse.add(merchantManager);
        }
        dto.setMerchantManagersResponse(merchantManagersResponse);
        UserEntity owner = merchant.getOwnerUser();
        if (owner != null) {
            dto.setFullName(owner.getUserDetails().getFullName());
            dto.setMobileNo(owner.getUserDetails().getMobileNo());
            dto.setEmail(owner.getUserDetails().getEmail());
            dto.setEnabled(Boolean.TRUE.equals(owner.isEnabled()));
            dto.setLocked(Boolean.TRUE.equals(owner.isLocked()));
            dto.setActivationDate(owner.getActivationDate());
            dto.setDisabledDate(owner.getDisabledDate());

            // Heuristic: if user has a parent, treat as sub-user
            if (owner.getParentUser() != null) {
                dto.setSubUserId(owner.getId());
            }
        }

        // Names & legal
        dto.setLegalName(merchant.getLegalName());
        dto.setTradeNameEn(merchant.getTradeNameEn());
        dto.setTradeNameAr(merchant.getTradeNameAr());

        // Business classification -> flattened names
        if (merchant.getBusinessType() != null) {
            dto.setBusinessType(safeNameEn(merchant.getBusinessType().getNameEn()));
        }
        if (merchant.getOtherBusinessType() != null) {
            dto.setOtherBusinessType(safeNameEn(merchant.getOtherBusinessType().getNameEn()));
        }
        if (merchant.getBusinessCategory() != null) {
            dto.setBusinessCategory(safeNameEn(merchant.getBusinessCategory().getNameEn()));
        }
        if (merchant.getOtherBusinessCategory() != null) {
            dto.setOtherBusinessCategory(safeNameEn(merchant.getOtherBusinessCategory().getNameEn()));
        }

        // Activities
        dto.setBusinessActivity(merchant.getBusinessActivity());
        dto.setOtherBusinessActivity(merchant.getOtherBusinessActivity());

        // Licensing
        dto.setCommercialLicenseNumber(merchant.getCommercialLicenseNumber());
        dto.setCommercialLicenseExpiry(merchant.getCommercialLicenseExpiry());

        // Addresses & contact
        dto.setBusinessLegalAddress(merchant.getBusinessLegalAddress());
        dto.setBusinessPhysicalAddress(merchant.getBusinessPhysicalAddress());
        dto.setEmirate(merchant.getEmirate());
        dto.setBusinessPhoneNumber(merchant.getBusinessPhoneNumber());

        dto.setOfficeEmailAddress(merchant.getOfficeEmailAddress());


        dto.setBankAccountName(merchant.getBankAccountName());
        dto.setAccountNumber(merchant.getBankAccount());
        dto.setBankAccount(merchant.getBankAccount()); // keep both for compatibility
        dto.setIban(merchant.getBankIban());
        dto.setBankIban(merchant.getBankIban()); // keep entity-style naming as well
        // `bankName` is not present on entity; if bankId contains name, mirror it
        dto.setBankName(merchant.getBankName());

        // Websites / socials (entity stores JSON strings; pass-through as-is)
        dto.setWebsiteUrls(toList(merchant.getWebsiteUrls()));
        dto.setSocialMediaUrls(toList(merchant.getSocialMediaUrls()));

        dto.setRequiredServices(merchant.getRequiredServices());

        // Payments config
        dto.setCurrentlyAcceptCardPayments(merchant.getCurrentlyAcceptCardPayments());
        dto.setCurrentPaymentGateway(merchant.getCurrentPaymentGateway());
        dto.setAcceptedCardTypes(merchant.getAcceptedCardTypes());
        dto.setCardPaymentMethods(merchant.getCardPaymentMethods());
        dto.setProcessingCurrencies(merchant.getProcessingCurrencies());
        dto.setSettlementCurrencies(merchant.getSettlementCurrencies());

        // Volumes & stats
        dto.setAvgOrderPrice(merchant.getAvgOrderPrice());
        dto.setMaxOrderPrice(merchant.getMaxOrderPrice());
        dto.setNoOfOrdersMonthly(merchant.getNoOfOrdersMonthly());
        dto.setVolumeOfOrdersMonthly(merchant.getVolumeOfOrdersMonthly());
        dto.setAnnualIncome(merchant.getAnnualIncome());
        dto.setEstimatedCardTransVolume(merchant.getEstimatedCardTransVolume());
        dto.setAvgTurnoverValue(merchant.getAvgTurnoverValue());
        dto.setAvgTurnoverCount(merchant.getAvgTurnoverCount());
        dto.setRefundValue(merchant.getRefundValue());
        dto.setRefundCount(merchant.getRefundCount());
        dto.setCashbackValue(merchant.getCashbackValue());
        dto.setCashbackCount(merchant.getCashbackCount());


        // Approvals & flags
        dto.setIsRestrictedCountries(merchant.getIsRestrictedCountries());
        dto.setAdminApproveStatus(merchant.getAdminApproveStatus());
        dto.setManagerApproveStatus(merchant.getManagerApproveStatus());
        dto.setMbmeApproveStatus(merchant.getMbmeApproveStatus());
        dto.setMyfattoraApproveStatus(merchant.getMyfattoraApproveStatus());

        // Audit
        dto.setCreatedAt(merchant.getCreatedAt());
        dto.setUpdatedAt(merchant.getUpdatedAt());


        return dto;
    }

    private List<String> toList(String json) {
        if (json == null) {
            return Collections.emptyList();
        }

        String trimmed = json.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) {
            return Collections.emptyList();
        }

        try {
            ObjectMapper mapper = new ObjectMapper();

            // Case 1: JSON array ["a","b"]
            if (trimmed.startsWith("[")) {
                return mapper.readValue(trimmed, new TypeReference<List<String>>() {
                });
            }

            // Case 2: single quoted string "a"
            if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                return List.of(mapper.readValue(trimmed, String.class));
            }

            // Case 3: fallback — comma-separated values a,b,c
            return Arrays.stream(trimmed.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JSON string to list: " + json, e);
        }
    }

    private String safeNameEn(String value) {
        return value == null ? null : value.trim();
    }
}
