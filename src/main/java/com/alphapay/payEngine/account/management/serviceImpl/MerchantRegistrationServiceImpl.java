package com.alphapay.payEngine.account.management.serviceImpl;


import com.alphapay.payEngine.account.management.dto.request.*;
import com.alphapay.payEngine.account.management.dto.response.*;
import com.alphapay.payEngine.account.management.exception.*;
import com.alphapay.payEngine.account.management.model.*;
import com.alphapay.payEngine.account.management.repository.*;
import com.alphapay.payEngine.account.management.service.MerchantRegistrationService;
import com.alphapay.payEngine.account.roles.exception.UserNotFoundException;
import com.alphapay.payEngine.alphaServices.model.AlphaPayServicesEntity;
import com.alphapay.payEngine.alphaServices.repository.AlphaPayServicesRepository;
import com.alphapay.payEngine.alphaServices.service.MerchantAlphaPayServicesService;
import com.alphapay.payEngine.common.encryption.EncryptionService;
import com.alphapay.payEngine.common.otp.exceptions.InvalidOTPException;
import com.alphapay.payEngine.common.otp.exceptions.OTPExpiredException;
import com.alphapay.payEngine.common.otp.services.IOtpService;
import com.alphapay.payEngine.notification.services.INotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MerchantRegistrationServiceImpl implements MerchantRegistrationService {
    private static final Logger logger = LoggerFactory.getLogger(MerchantRegistrationServiceImpl.class);

    @Autowired
    HttpServletRequest httpServletRequest;
    @Autowired
    private EncryptionService encryptionService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MerchantRegistrationRepository merchantRegistrationRepository;
    @Autowired
    private IOtpService otpService;
    @Autowired
    private INotificationService notificationService;
    @Autowired
    private BusinessCategoryRepository businessCategoryRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private BusinessTypeRepository businessTypeRepository;
    @Autowired
    private EmirateRepository emirateRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MerchantAlphaPayServicesService merchantAlphaPayServicesService;
    @Autowired
    private UserRegistrationProgressRepository userRegistrationProgressRepository;
    @Autowired
    private MessageResolverService resolverService;
    @Autowired
    private AlphaPayServicesRepository alphaPayServicesRepository;

    @Override
    @Transactional
    public MerchantRegistrationResponse registerMerchant(MerchantRegistration request, Locale acceptLanguage) {
        String confirmPassword = request.getConfirmPassword();// encryptionService.decrypt(request.getConfirmPassword());
        String rawPassword = request.getUserDetails().getPassword();// encryptionService.decrypt(request.getUserDetails().getPassword());
        if (!rawPassword.equals(confirmPassword)) {
            throw new PasswordException();
        }
        String encryptedPassword = passwordEncoder.encode(rawPassword);
        request.getUserDetails().setPassword(encryptedPassword);
        request.setStatus("InActive");
        log.debug("MerchantRegistration:::{}", request);
        Optional<MerchantRegistration> registration = merchantRegistrationRepository.findByRegistrationId(request.getRegistrationId());
        if (registration.isEmpty()) {
            throw new NoRegistrationInitiatedException();
        }

        request.getUserDetails().setMobileNo(registration.get().getUserDetails().getMobileNo());
        request.getUserDetails().setEmail(registration.get().getUserDetails().getEmail());

        registration.get().setUserDetails(request.getUserDetails());
        registration.get().setRequestId(request.getRequestId());
        request = registration.get();
//        merchantRegistrationRepository.save(request);
        if (!registration.get().isEmailVerified() || !registration.get().isMobileVerified()) {
            throw new EmailOrMobileNoNotVerifiedException();
        }

        UserEntity userEmail = userRepository.findByEmail(registration.get().getUserDetails().getEmail());
        if (userEmail != null) {
            if (Objects.equals(userEmail.getUserDetails().getMobileNo(), registration.get().getUserDetails().getMobileNo())) {
                throw new DuplicateUserException();
            } else {
                throw new DuplicateUserException();
            }
        }

        UserEntity user = new UserEntity();
        registration.get().setStatus("Active");
        registration.get().setEnabled(true);
        BeanUtils.copyProperties(registration.get(), user, "id", "version");
        user.setUserDetails(registration.get().getUserDetails());
        user.setRegistrationId(registration.get().getId());
        user.setActivationDate(new Date());
        user.setEnabled(true);
        user.setRequestId(request.getRequestId());

        UserEntity savedUser = userRepository.save(user);

        //add alphaPay services to user with default status InActive.
        Long merchantId = savedUser.getId();
        List<AlphaPayServicesEntity> serviceEntities = alphaPayServicesRepository.findAll();
        log.debug("alphapay_services:::{}",serviceEntities);
        List<MerchantServiceRequest> merchantAlphaPayServices = new ArrayList<>();
        for (AlphaPayServicesEntity service : serviceEntities) {
            MerchantServiceRequest merchantAlphaPayService = new MerchantServiceRequest();
            merchantAlphaPayService.setServiceId(service.getServiceId());
            merchantAlphaPayService.setStatus("InActive");
            merchantAlphaPayServices.add(merchantAlphaPayService);
        }
        log.debug("merchantAlphaPayServices:::{}",merchantAlphaPayServices);
        log.debug("merchantId:::{}",merchantId);
        merchantAlphaPayServicesService.addMerchantService(merchantId, merchantAlphaPayServices);

        /***
         UserRegistrationProgress progress = new UserRegistrationProgress();
         progress.setUserId(savedUser.getId());
         progress.setAddressProvided(true);
         progress.setBankInfoAdded(true);
         progress.setPhoneVerified(true);
         progress.setAdminApproveStatus("PENDING");
         progress.setManagerApproveStatus("PENDING");
         userRegistrationProgressRepository.save(progress);
         */
        Map<String, Object> data = new HashMap<>();
        data.put("merchantId", request.getId().toString());
        data.put("registrationId", request.getRequestId());
//        MerchantRegistrationResponse response = new MerchantRegistrationResponse(request.getRequestId(), "Success", 200, "Merchant registered successfully. Your account will be activated upon KYC approval by the sales team.", data);
        MerchantRegistrationResponse response = new MerchantRegistrationResponse();
        BeanUtils.copyProperties(request, response);
        response.setData(data);
        resolverService.setAsSuccess(response);
        return response;
    }


    @Override
    @Transactional(dontRollbackOn = {InvalidOTPException.class, OTPExpiredException.class})
    public OtpResponse validateOTP(OTPValidationRequest request) {
        Optional<MerchantRegistration> registration = merchantRegistrationRepository.findByRegistrationId(request.getRegistrationId());
        if (registration.isEmpty()) {
            throw new NoRegistrationInitiatedException();
        }
        String tranType = "";
        if (request.getType().equals("mobileNo")) {
            tranType = "MERCHANT_REGISTRATION_MOBILE_NO";
        } else if (request.getType().equals("email")) {
            tranType = "MERCHANT_REGISTRATION_EMAIL";
        }
        int validation = otpService.validate(request.getGeneratedOtpId(), request.getOtp(), tranType);
        if (validation != 1) {
            if (validation == 0)
                throw new InvalidOTPException();
            else
                throw new OTPExpiredException();
        }
        if (request.getType().equals("email")) {
            registration.get().setEmailVerified(true);
            request.setType("Email");
        } else if (request.getType().equals("mobileNo")) {
            registration.get().setMobileVerified(true);
            request.setType("Mobile Number");
        }
        OtpResponse response = new OtpResponse();
        BeanUtils.copyProperties(request, response);
        response.setGeneratedOtpId(request.getRequestId());
        resolverService.setAsSuccess(response);
        return response;
    }

    @Override
    public OtpResponse resendOtp(@Valid ResendOtpRequest otpRequest) {
        String otp = null;
        if (otpRequest.getType().equals("mobileNo")) {
            otp = otpService.generateOTP(otpRequest.getRequestId(), "MERCHANT_REGISTRATION_MOBILE_NO", "");
        } else if (otpRequest.getType().equals("email")) {
            otp = otpService.generateOTP(otpRequest.getRequestId(), "MERCHANT_REGISTRATION_EMAIL", "");
        }

        Optional<MerchantRegistration> registration = merchantRegistrationRepository.findByRegistrationId(otpRequest.getRegistrationId());
        if (registration.isPresent()) {
            if (otpRequest.getType().equals("mobileNo")) {
                try {
                    String[] msgKeys = {otp};
                    notificationService.sendMobileNotification(otpRequest.getRequestId(), "MERCHANT_ONBOARDING_REGISTRATION_OTP_MESSAGE", msgKeys, registration.get().getUserDetails().getMobileNo(),
                            "", Locale.ENGLISH, httpServletRequest.getAttribute("applicationId") + "", false);
                } catch (Throwable e) {
                    logger.debug("Unable to send sms notification");
                }
            } else if (otpRequest.getType().equals("email")) {
                try {
                    String[] msgKeys = {otp};
                    notificationService.sendEmailNotification(otpRequest.getRequestId(), "MERCHANT_ONBOARDING_REGISTRATION_OTP_EMAIL", msgKeys, registration.get().getUserDetails().getEmail(), "", Locale.ENGLISH, httpServletRequest.getAttribute("applicationId") + "", null);
                } catch (Throwable e) {
                    logger.debug("Unable to send email notification");
                }
            }
        } else {
            throw new NoRegistrationInitiatedException();

        }
        OtpResponse response = new OtpResponse();
        BeanUtils.copyProperties(otpRequest, response);
        response.setGeneratedOtpId(otpRequest.getRequestId());
        resolverService.setAsSuccess(response);
        return response;
    }

    @Override
    public List<BusinessTypeResponse> getBusinessTypes(BusinessTypeRequest request) {
        List<BusinessTypeResponse> responseList = new ArrayList<>();
        List<BusinessTypeEntity> types = businessTypeRepository.findAllWithCategories();
        for (BusinessTypeEntity type : types) {
            BusinessTypeResponse response = new BusinessTypeResponse();
            response.setId(type.getId());
            response.setNameEn(type.getNameEn());
            response.setNameAr(type.getNameAr());
            if (type.getCategories() != null) {
                List<BusinessCategoryResponse> categoryResponses = type.getCategories().stream()
                        .map(category -> new BusinessCategoryResponse(
                                category.getId(),
                                category.getNameEn(),
                                category.getNameAr()
                        ))
                        .collect(Collectors.toList());
                response.setCategories(categoryResponses);
            }
            responseList.add(response);
        }
        return responseList;
    }

    @Override
    public List<BusinessCategoryResponse> getAllBusinessCategories(BusinessCategoryRequest request) {
        return businessCategoryRepository.findAll().stream()
                .map(type -> {
                    BusinessCategoryResponse response = new BusinessCategoryResponse();
                    BeanUtils.copyProperties(type, response);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CountriesResponse> getCountries(CountriesRequest request) {
        return countryRepository.findAll().stream()
                .map(type -> {
                    CountriesResponse response = new CountriesResponse();
                    BeanUtils.copyProperties(type, response);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<BusinessCategoryResponse> getCategoriesByBusinessTypeId(Long businessTypeId) {
        List<BusinessCategoryEntity> categories = businessCategoryRepository.findByBusinessTypeId(businessTypeId);

        return categories.stream()
                .map(category -> new BusinessCategoryResponse(
                        category.getId(),
                        category.getNameEn(),
                        category.getNameAr()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<EmiratesResponse> getEmirates(EmiratesRequest request) {
        return emirateRepository.findAll().stream()
                .map(type -> {
                    EmiratesResponse response = new EmiratesResponse();
                    BeanUtils.copyProperties(type, response);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MerchantRegistrationResponse updateMerchantInfo(UpdateMerchantInfoRequest request) {
        Optional<UserEntity> optionalMerchant = userRepository.findById(request.getMerchantId());
        if (optionalMerchant.isEmpty()) {
            throw new UserNotFoundException();
        }

        UserEntity merchant = optionalMerchant.get();

//        merchant.getUserDetails().setBusinessCategory(request.getBusinessCategory());
//        merchant.getUserDetails().setBusinessType(request.getBusinessType());
//        merchant.getUserDetails().setLegalName(request.getLegalName());
//        merchant.getUserDetails().setTradeNameEnglish(request.getTradeNameEnglish());
//        merchant.getUserDetails().setEmirate(request.getEmirate());
//        merchant.getUserDetails().setBusinessAddress(request.getBusinessAddress());
//        merchant.getUserDetails().setWebsiteUrl(request.getWebsiteUrl());
//        merchant.getUserDetails().setSocialMediaUrl(request.getSocialMediaUrl());
//        merchant.getUserDetails().setOwnerMobileNumber(request.getOwnerMobileNumber());
//        merchant.getUserDetails().setOwnerEmail(request.getOwnerEmail());
//        merchant.getUserDetails().setNationality(request.getNationality());


        userRepository.save(merchant);
        merchantAlphaPayServicesService.addMerchantService(request.getMerchantId(), request.getMerchantServices());
        Map<String, Object> data = new HashMap<>();
        data.put("merchantId", merchant.getId().toString());

        MerchantRegistrationResponse response = new MerchantRegistrationResponse();
        BeanUtils.copyProperties(request, response);
        resolverService.setAsSuccess(response);
        return response;
    }

    @Override
    public OtpResponse generateOtp(GenerateOtpRequest otpRequest) {
        MerchantRegistration newMerchant;

        UserDetails userDetails = new UserDetails();
        Optional<MerchantRegistration> registration = merchantRegistrationRepository.findByRegistrationId(otpRequest.getRegistrationId());
        newMerchant = registration.orElseGet(MerchantRegistration::new);
        newMerchant.setRequestId(otpRequest.getRequestId());
        newMerchant.setStatus("InActive");
        String otp = null;
        if (otpRequest.getType().equals("mobileNo")) {
            otp = otpService.generateOTP(otpRequest.getRequestId(), "MERCHANT_REGISTRATION_MOBILE_NO", "");
        } else if (otpRequest.getType().equals("email")) {
            otp = otpService.generateOTP(otpRequest.getRequestId(), "MERCHANT_REGISTRATION_EMAIL", "");
        } else {
            //TODO:: change exception to invalid type
            throw new InvalidOTPException();
        }

        if (otpRequest.getType().equals("mobileNo")) {
            if (newMerchant.getUserDetails() != null) {
                BeanUtils.copyProperties(newMerchant.getUserDetails(), userDetails);
            }
            userDetails.setMobileNo(otpRequest.getContact());
            userDetails.setCountryCode(otpRequest.getCountryCode());
            newMerchant.setUserDetails(userDetails);


            try {
                String[] msgKeys = {otp};
                notificationService.sendMobileNotification(otpRequest.getRequestId(), "MERCHANT_ONBOARDING_REGISTRATION_OTP_MESSAGE", msgKeys, otpRequest.getContact(),
                        "", Locale.ENGLISH, httpServletRequest.getAttribute("applicationId") + "", false);
            } catch (Throwable e) {
                logger.debug("Unable to send sms notification");
            }
            otpRequest.setType("Mobile Number");
        } else if (otpRequest.getType().equals("email")) {
            if (newMerchant.getUserDetails() != null) {
                BeanUtils.copyProperties(newMerchant.getUserDetails(), userDetails);
            }
            userDetails.setEmail(otpRequest.getContact());
            newMerchant.setUserDetails(userDetails);

            try {
                String[] msgKeys = {otp};
                notificationService.sendEmailNotification(otpRequest.getRequestId(), "MERCHANT_ONBOARDING_REGISTRATION_OTP_EMAIL", msgKeys, otpRequest.getContact(), "", Locale.ENGLISH, httpServletRequest.getAttribute("applicationId") + "", null);
            } catch (Throwable e) {
                logger.debug("Unable to send email notification");
            }
            otpRequest.setType("Email");
        } else {
            //thow new exception not valid type
        }
        log.debug("newMerchant::{}", newMerchant);
        newMerchant.setRegistrationId(otpRequest.getRegistrationId());
        merchantRegistrationRepository.save(newMerchant);

        OtpResponse response = new OtpResponse();
        BeanUtils.copyProperties(otpRequest, response);
        response.setGeneratedOtpId(otpRequest.getRequestId());
        resolverService.setAsSuccess(response);

        return response;
    }
}
