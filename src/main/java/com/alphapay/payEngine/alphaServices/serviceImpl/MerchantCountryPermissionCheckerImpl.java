package com.alphapay.payEngine.alphaServices.serviceImpl;

import com.alphapay.payEngine.account.management.exception.AccountNotFoundException;
import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.management.repository.UserRepository;
import com.alphapay.payEngine.account.management.service.MerchantService;
import com.alphapay.payEngine.account.merchantKyc.model.MerchantEntity;
import com.alphapay.payEngine.alphaServices.model.BinData;
import com.alphapay.payEngine.alphaServices.repository.BinDataRepository;
import com.alphapay.payEngine.alphaServices.repository.MerchantCountrySettingsRepository;
import com.alphapay.payEngine.common.httpclient.service.RestClientService;
import com.alphapay.payEngine.integration.model.BinListExternalResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MerchantCountryPermissionCheckerImpl {

    @Autowired
    MerchantCountrySettingsRepository merchantCountrySettingsRepository;

    @Autowired
    BINServiceImpl binService;

    @Autowired
    BinDataRepository binDataRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RestClientService restClientService;

    String url = "https://lookup.binlist.net";
    @Autowired
    private MerchantService merchantService;
    /**
     * Validates if a given BIN code is allowed for a specific merchant.
     *
     * @param bin The BIN code to validate.
     * @param merchantId The ID of the merchant.
     * @return true if the BIN is allowed for the merchant, false otherwise.
     */

    public boolean validateBINCodeForMerchant(String bin, Long merchantId) {
        // Retrieve the country code from the BIN
        log.debug("Validating BIN code: {} for merchant ID: {}", bin, merchantId);
        MerchantEntity merchantEntity =  merchantService.getMerchant(merchantId);
//        UserEntity user = userRepository.findById(merchantId).orElseThrow(() -> new AccountNotFoundException());
        if(merchantEntity.getIsRestrictedCountries()) {
            String binCountryCode = binService.getCountryISO2FromBin(bin);
            if (binCountryCode == null || binCountryCode.isEmpty()) {
                log.debug("Invalid BIN or country not found for BIN: {}", bin);
                binCountryCode=getCountryISO2FromBinThroughAPIIntegration(bin);

                if (binCountryCode == null || binCountryCode.isEmpty())
                return true; // Invalid BIN or country not found
            }
            log.debug("BIN: {}, Country Code: {}", bin, binCountryCode);
            // Check if the merchant is allowed for this country
            return isMerchantAllowed(merchantId, binCountryCode);
        }
        else {
            log.debug("Merchant with ID {} is not restricted to specific countries.", merchantId);
            return true; // Merchant is not restricted to specific countries
        }
    }
    public boolean isMerchantAllowed(Long merchantId, String binCountryCode) {
        return merchantCountrySettingsRepository.existsByMerchantIdAndIsoCountryCodeAndStatus(
                merchantId, binCountryCode, "ACTIVE"
        );
    }

    String getCountryISO2FromBinThroughAPIIntegration(String bin) {
        try{
            BinListExternalResponseModel responseEntity =null;
            String uri=url+"/"+bin;

            //responseEntity = restClientService.invokeRemoteService(uri, org.springframework.http.HttpMethod.GET, null, BinListExternalResponseModel.class, null, restClientService.getGenericRestTemplate());
            responseEntity = restClientService.invokeRemoteService(uri, HttpMethod.GET, null, BinListExternalResponseModel.class, null, restClientService.getGenericRestTemplate()).getBody();
            log.debug("Response from BIN API for BIN {}: {}", bin, responseEntity);
            if (responseEntity != null && responseEntity.getCountry() != null) {
                String countryIso2 = responseEntity.getCountry().getAlpha2();
                if(countryIso2!=null && !countryIso2.isEmpty() ) {
                    BinData newBinData = new BinData();
                    newBinData.setBin(bin);
                    newBinData.setBrand(responseEntity.getBrand().toUpperCase());
                    newBinData.setType(responseEntity.getType().toUpperCase());
                    //newBinData.setCategory(responseEntity.getType());
                    newBinData.setIssuer(responseEntity.getBank().getName().toUpperCase());
                    newBinData.setIssuerPhone(responseEntity.getBank().getPhone());
                    newBinData.setIssuerUrl(responseEntity.getBank().getUrl());
                    newBinData.setCountryName(responseEntity.getCountry().getName().toUpperCase());
                    newBinData.setIsoCode2(countryIso2.toUpperCase());
                    //newBinData.set
                    log.debug("Saving new BIN data: {}", newBinData);
                    binDataRepository.save(newBinData);

                    log.debug("Retrieved country ISO2 from BIN: {} is {}", bin, countryIso2);
                    return countryIso2.toUpperCase();
                }
            } else {
                log.warn("No country information found for BIN: {}", bin);
            }

        }
        catch (Throwable ex) {
            log.error("Error retrieving country ISO2 from BIN: {}", bin, ex);
        }
        return null;
    }

}
