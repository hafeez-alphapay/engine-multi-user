package com.alphapay.payEngine.controller.account;

import com.alphapay.payEngine.account.management.dto.request.*;
import com.alphapay.payEngine.account.management.dto.response.*;
import com.alphapay.payEngine.account.management.model.MerchantRegistration;
import com.alphapay.payEngine.account.management.service.MerchantRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.alphapay.payEngine.utilities.UtilHelper.getLocale;

@RestController
@Slf4j
@RequestMapping("/merchant")
public class MerchantRegistrationController {

    @Autowired
    private MerchantRegistrationService merchantRegistrationService;

    @RequestMapping(value = "/register", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public @ResponseBody MerchantRegistrationResponse registerUser(@Valid @RequestBody MerchantRegistrationRequest request, HttpServletRequest httpServletRequest) {
        MerchantRegistration registration = request.convertToEntity();
        registration.setApplicationId((String) httpServletRequest.getAttribute("applicationId"));
        return merchantRegistrationService.registerMerchant(registration, getLocale(request));
    }

    @RequestMapping(value = "/otpValidation", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public @ResponseBody OtpResponse otpValidation(@Valid @RequestBody OTPValidationRequest request) {
        return merchantRegistrationService.validateOTP(request);
    }

    @RequestMapping(value = "/resendOtp", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public @ResponseBody OtpResponse resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        return merchantRegistrationService.resendOtp(request);
    }


    @RequestMapping(value = "/generateOtp", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public @ResponseBody OtpResponse generateOtp(@Valid @RequestBody GenerateOtpRequest request) {
        return merchantRegistrationService.generateOtp(request);
    }

    @RequestMapping(value = "/businessType", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public @ResponseBody List<BusinessTypeResponse> getBusinessType(@Valid @RequestBody BusinessTypeRequest request) {
        return merchantRegistrationService.getBusinessTypes(request);
    }

    @RequestMapping(value = "/businessCategory", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public @ResponseBody List<BusinessCategoryResponse> getBusinessCategory(@Valid @RequestBody BusinessCategoryRequest request) {
        return merchantRegistrationService.getAllBusinessCategories(request);
    }

    @RequestMapping(value = "/getCountries", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public @ResponseBody List<CountriesResponse> getCountries(@Valid @RequestBody CountriesRequest request) {
        return merchantRegistrationService.getCountries(request);
    }

    @RequestMapping(value = "/getEmirates", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public @ResponseBody List<EmiratesResponse> getEmirates(@Valid @RequestBody EmiratesRequest request) {
        return merchantRegistrationService.getEmirates(request);
    }

    @RequestMapping(value = "/categoriesByBusinessTypeId", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public @ResponseBody List<BusinessCategoryResponse> getBusinessCategoryByBusinessTypeId(@Valid @RequestBody BusinessCategoryRequest request) {
        return merchantRegistrationService.getCategoriesByBusinessTypeId(request.getBusinessTypeId());
    }

    @PostMapping("/update")
    public @ResponseBody MerchantRegistrationResponse updateMerchantInfo(@Valid @RequestBody UpdateMerchantInfoRequest request) {
        return merchantRegistrationService.updateMerchantInfo(request);
    }
}
