package com.alphapay.payEngine.account.management.serviceImpl;

import com.alphapay.payEngine.account.management.dto.request.CompleteLoginRequest;
import com.alphapay.payEngine.account.management.dto.request.LoginRequest;
import com.alphapay.payEngine.account.management.dto.request.VerifySetupMFARequest;
import com.alphapay.payEngine.account.management.dto.response.LoginResponse;
import com.alphapay.payEngine.account.management.exception.*;
import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.management.model.UserMfaConfig;
import com.alphapay.payEngine.account.management.repository.UserMfaConfigRepository;
import com.alphapay.payEngine.account.management.repository.UserRepository;
import com.alphapay.payEngine.account.management.service.BaseUserService;
import com.alphapay.payEngine.account.management.service.InitializUserDataService;
import com.alphapay.payEngine.account.management.service.LoginService;
import com.alphapay.payEngine.account.management.service.TOTPService;
import com.alphapay.payEngine.account.roles.exception.UserNotFoundException;
import com.alphapay.payEngine.common.encryption.EncryptionService;
import com.alphapay.payEngine.common.otp.models.TOTPKey;
import com.alphapay.payEngine.config.AppSetting;
import com.alphapay.payEngine.model.response.BaseResponse;
import com.alphapay.payEngine.notification.services.INotificationService;
import com.alphapay.payEngine.transactionLogging.data.NonFinancialTransaction;
import com.alphapay.payEngine.transactionLogging.data.NonFinancialTransactionRepository;
import com.alphapay.payEngine.utilities.BeanUtility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class LoginServiceImpl implements LoginService {

    Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);

    @Value("${login.mfa.totp.enabled:false}")
    private boolean isTOTPEnabled;
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;
    @Autowired
    UserRepository usersRepository;

    @Autowired
    UserMfaConfigRepository userMfaConfigRepository;

    @Autowired
    AppSetting appSetting;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private BaseUserService baseUserService;

    @Value("${login.tryCount}")
    private int loginTryCount;

    @Autowired
    INotificationService notificationService;

    @Autowired
    InitializUserDataService initializUserDataService;

    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    TOTPService totpService;

    @Autowired
    NonFinancialTransactionRepository nonFinancialTransactionRepository;


    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        commonLoginActivities(loginRequest);
        LoginResponse response = initializUserDataService.getInitializationData(loginRequest);
        return response;
    }

    @Override
    public LoginResponse loginWithTOTP(LoginRequest loginRequest) {
        LoginResponse response = new LoginResponse();
        BeanUtility.copyProperties(loginRequest, response);


        if(!isTOTPEnabled)
        {
            LoginResponse res= login(loginRequest);
            res.setStatus("LOGIN_SUCCESS");
        }
        else
        {
            //Login Without Initialization
            List<UserMfaConfig> configs=commonLoginActivities(loginRequest);

            UserMfaConfig totpConfig = configs.stream()
                    .filter(config -> "TOTP".equalsIgnoreCase(config.getMfaType()))
                    .findFirst()
                    .orElse(null);


            if(totpConfig == null || !totpConfig.isEnabled()) {
               // throw new MfaNotEnabledException("TOTP is not enabled for this user");
                if(totpConfig == null)
                {
                    TOTPKey key = totpService.generateTOTPKey(loginRequest.getRequestId(), "ALPHAPAY FZE - MERCHANT", loginRequest.getEmail());
                    totpConfig = updateMFAConfig(key, loginRequest.getEmail(),loginRequest.getApplicationId());
                }
                String provider=activeProfile.contains("dev") ? "ALPHAPAY DEV FZE - MERCHANT" : "ALPHAPAY FZE - MERCHANT";
               response.setTotpKey(                new TOTPKey(
                        totpConfig.getSecret(),
                        totpService.generateQRCode(
                                "otpauth://totp/"+provider+":" + loginRequest.getEmail() + "?secret=" + totpConfig.getSecret() + "&issuer=ALPHAPAY FZE - MERCHANT",
                                provider
                        ),
                        provider,
                         loginRequest.getEmail()
                ));
                response.setStatus("TOTP_INIT_REQUIRED");
                response.setUserId(totpConfig.getUser().getId());

            }
            else
            {
                response.setUserId(totpConfig.getUser().getId());

                // BeanUtility.copyProperties(loginRequest, response);
                response.setStatus("TOTP_REQUIRED");
            }



        }
        return response;
    }
    @Transactional
    @Override
    public BaseResponse setupAndVerifyMFA(VerifySetupMFARequest verifySetupMFARequest)
        {
            NonFinancialTransaction initialLogin = nonFinancialTransactionRepository.findByRequestId(verifySetupMFARequest.getInitialLoginRequestId());
            if (initialLogin == null || !"LoginRequest".equals(initialLogin.getTransactionType())) {
                throw new UserNotFoundException();
            }
            UserEntity user = baseUserService.getLoggedUser(initialLogin.getUserId());
            if (user == null) {
                throw new UserNotFoundException();
            }
            UserMfaConfig existingConfig = userMfaConfigRepository.findByUserAndMfaType(user, "TOTP").orElse(null);
            boolean valid=totpService.validateTOTP(verifySetupMFARequest.getRequestId(), existingConfig.getSecret(), verifySetupMFARequest.getToken());
            if(!valid)
            {
                throw new InvalidMFATokenException();
            }
            else
            {
                existingConfig.setEnabled(true);
                existingConfig.setVerifiedAt(LocalDateTime.now());
                userMfaConfigRepository.save(existingConfig);
                // initialLogin.setStatus("MFA_SETUP_SUCCESS");
                //nonFinancialTransactionRepository.save(initialLogin);
                return new BaseResponse("Success",0,"MFA setup successful");
            }

        }
    @Transactional(dontRollbackOn = {PasswordException.class,InvalidMFATokenException.class, ExceededLoginTriesException.class})
    @Override
    public LoginResponse completeLogin(CompleteLoginRequest completeLoginRequest) {
        NonFinancialTransaction initialLogin = nonFinancialTransactionRepository.findByRequestId(completeLoginRequest.getInitialLoginRequestId());
        if (initialLogin == null || !"LoginRequest".equals(initialLogin.getTransactionType())) {
            throw new UserNotFoundException();
        }
        if(!initialLogin.getStatus().equals("TOTP_INIT_REQUIRED") && !initialLogin.getStatus().equals("TOTP_REQUIRED"))
        {
            throw new InvalidMFASetupException();
        }
        UserEntity user = baseUserService.getLoggedUser(initialLogin.getUserId());
        if (user == null) {
            throw new UserNotFoundException();
        }
        UserMfaConfig existingConfig = userMfaConfigRepository.findByUserAndMfaType(user, "TOTP").orElse(null);
        if (existingConfig == null || !existingConfig.isEnabled()) {
            throw new InvalidMFASetupException();
        }
        boolean valid = totpService.validateTOTP(completeLoginRequest.getRequestId(), existingConfig.getSecret(), completeLoginRequest.getToken());
        if (!valid) {
            user.setLoginTryCount(user.getLoginTryCount() + 1);
            if (user.getLoginTryCount() > loginTryCount) {
                user.setLocked(true);
                try {
                    String[] msgKeys = {user.getUserDetails().getEmail(), loginTryCount + ""};
                    notificationService.sendMobileNotification(completeLoginRequest.getRequestId(), "ACCOUNT_LOCKED_NOTIFICATION", msgKeys, user.getUserDetails().getMobileNo(),
                            user.getUserDetails().getEmail(), Locale.ENGLISH, httpServletRequest.getAttribute("applicationId") + "");
                } catch (Throwable e) {
                    logger.debug("Unable to send notification");
                }
                throw new ExceededLoginTriesException();
            }
            throw new InvalidMFATokenException();
        }
        user.setLoginTryCount(0);
        user.setLastLogin(new Date());
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(user.getUserDetails().getEmail());
        //loginRequest.setPassword(completeLoginRequest.getPassword());
        loginRequest.setApplicationId(completeLoginRequest.getApplicationId());
        //loginRequest.setPushNotificationId(completeLoginRequest.getPushNotificationId());
        loginRequest.setRequestId(completeLoginRequest.getRequestId());
        LoginResponse response = initializUserDataService.getInitializationData(loginRequest);
        response.setStatus("LOGIN_SUCCESS");
        initialLogin.setStatus("SUCCESS");
        nonFinancialTransactionRepository.save(initialLogin);
        return response;

    }


    @Transactional(dontRollbackOn = {PasswordException.class, ExceededLoginTriesException.class})
    List<UserMfaConfig> commonLoginActivities(LoginRequest loginRequest)
    {
        UserEntity user = baseUserService.getLoggedUser(loginRequest.getEmail(), loginRequest.getApplicationId());

        if (user.isLocked())
            throw new AccountLockedException();

        if (!user.isEnabled())
            throw new AccountDisabledException();

        user.setLoginTryCount(user.getLoginTryCount() + 1);
        if (user.getLoginTryCount() > loginTryCount) {
            user.setLocked(true);
            try {
                String[] msgKeys = {user.getUserDetails().getEmail(), loginTryCount + ""};
                notificationService.sendMobileNotification(loginRequest.getRequestId(), "ACCOUNT_LOCKED_NOTIFICATION", msgKeys, user.getUserDetails().getMobileNo(),
                        user.getUserDetails().getEmail(), Locale.ENGLISH, httpServletRequest.getAttribute("applicationId") + "");
            } catch (Throwable e) {
                logger.debug("Unable to send notification");
            }
            throw new ExceededLoginTriesException();
        }

        String plainPassword;
        plainPassword =  loginRequest.getPassword();//encryptionService.decrypt(loginRequest.getPassword());

        if (!passwordEncoder.matches(plainPassword, user.getUserDetails().getPassword()))
            throw new PasswordException();

        user.setLoginTryCount(0);
        user.setLastLogin(new Date());
        user.setPushNotificationId(loginRequest.getPushNotificationId());
        return user.getMfaConfigs();
    }

    @Transactional
    UserMfaConfig updateMFAConfig(TOTPKey totpToken, String email,String appId) {
        UserEntity user = baseUserService.getLoggedUser(email, appId);
        if (user == null) {
            throw new UserNotFoundException();
        }
        UserMfaConfig newConfig = new UserMfaConfig();
        newConfig.setUser(user);
        newConfig.setMfaType("TOTP");
        newConfig.setSecret(totpToken.getKey());
        newConfig.setCreatedAt(LocalDateTime.now());
        newConfig.setEnabled(false);
        newConfig=userMfaConfigRepository.save(newConfig);
        return newConfig;

    }


}
