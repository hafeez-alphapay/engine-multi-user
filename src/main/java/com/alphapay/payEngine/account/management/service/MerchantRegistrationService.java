package com.alphapay.payEngine.account.management.service;


import com.alphapay.payEngine.account.management.dto.request.*;
import com.alphapay.payEngine.account.management.dto.response.*;
import com.alphapay.payEngine.account.management.model.MerchantRegistration;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Locale;

public interface MerchantRegistrationService {

    MerchantRegistrationResponse registerMerchant(MerchantRegistration request, Locale acceptLanguage);

    OtpResponse validateOTP(OTPValidationRequest request);

    OtpResponse resendOtp(@Valid ResendOtpRequest otpRequest);

    List<BusinessTypeResponse> getBusinessTypes(BusinessTypeRequest request);

    List<CountriesResponse> getCountries(CountriesRequest request);

    List<BusinessCategoryResponse> getAllBusinessCategories(BusinessCategoryRequest request);

    List<BusinessCategoryResponse> getCategoriesByBusinessTypeId(Long businessTypeId);

    List<EmiratesResponse> getEmirates(@Valid EmiratesRequest request);

    MerchantRegistrationResponse updateMerchantInfo( UpdateMerchantInfoRequest request);

    OtpResponse generateOtp(@Valid GenerateOtpRequest request);
}
