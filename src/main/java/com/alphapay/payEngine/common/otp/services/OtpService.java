package com.alphapay.payEngine.common.otp.services;

import com.alphapay.payEngine.common.otp.exceptions.GenerationAttemptsExceededException;
import com.alphapay.payEngine.common.otp.exceptions.MinimumTimeToRegenerateIsNotPassedException;
import com.alphapay.payEngine.common.otp.exceptions.OTPNotFoundException;
import com.alphapay.payEngine.common.otp.models.OtpDetails;
import com.alphapay.payEngine.common.otp.repositories.OtpRepository;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

@Service
public class OtpService implements IOtpService {

    Logger logger = LoggerFactory.getLogger(IOtpService.class);

    @Value("${otpDetails.digits.count}")
    private int otpDigitsCount;

    @Value("${otpDetails.minimum.time.to.regenerate.in.seconds}")
    private int otpCoolDownSeconds;

    @Value("${otpDetails.validity.period.in.seconds}")
    private int otpValidityPeriodSeconds;

    @Value("${otpDetails.max.generation.attempts}")
    private int otpMaxGenerationAttempts;

    @Value("${otpDetails.max.validation.attempts}")
    private int otpMaxValidationAttempts;

    @Value("${spring.profiles.active}")
    private String springActiveProfile;

    @Value("${otpDetails.config.useDefaultOTP:false}")
    private boolean useDefaultOTP;

    @Autowired
    OtpRepository otpRepository;

    @Override
    public String generateOTP(String requestId, String tranType, String cif) throws GenerationAttemptsExceededException, MinimumTimeToRegenerateIsNotPassedException {
        logger.debug("generating otp {requestId: " + requestId + ", TranType: " + tranType + ", CIF: " + cif + "}");

        Date currentTime = Calendar.getInstance().getTime();
        Date expiryTime = DateUtils.addSeconds(currentTime, otpValidityPeriodSeconds);
        String otpStr = this.generateRandomInt(otpDigitsCount);

        if (!otpRepository.existsByRequestId(requestId)) {
            logger.debug("otp doesn't exists generating a new one ..." + otpStr);
            OtpDetails otpDetailsObject = new OtpDetails(
                    requestId,
                    BCrypt.hashpw(otpStr, BCrypt.gensalt()),
                    tranType,
                    currentTime,
                    expiryTime,
                    1,
                    0
            );
            otpRepository.save(otpDetailsObject);
        } else {
            logger.debug("regenerating otp");

            OtpDetails otpDetailsObject = otpRepository.findByRequestId(requestId);
            if (otpDetailsObject.getGenerationAttempts() >= otpMaxGenerationAttempts) {
                logger.debug("otp regeneration attempts limit exceeded");
                throw new GenerationAttemptsExceededException();
            }
            if (DateUtils.addSeconds(otpDetailsObject.getLastGenerated(), otpCoolDownSeconds).after(currentTime)) {
                logger.debug("otp minimum time to regenerate has not passed");
                throw new MinimumTimeToRegenerateIsNotPassedException();
            }

            otpDetailsObject.setLastGenerated(currentTime);
            otpDetailsObject.setExpiryTime(expiryTime);
            otpDetailsObject.setGenerationAttempts(otpDetailsObject.getGenerationAttempts() + 1);
            otpDetailsObject.setOtp(BCrypt.hashpw(otpStr, BCrypt.gensalt()));
            otpDetailsObject.setValidationAttempts(0);
            otpRepository.save(otpDetailsObject);
        }

        logger.debug("otp generated successfully ");
        return otpStr;
    }

    private String generateRandomInt(int otpDigitsCount) {
        //On dev profile return static otp
        if (useDefaultOTP) {
            return "12345";
        }
        Random random = new Random();
        int min = (int) Math.pow(10, otpDigitsCount - 1);
        int max = (int) Math.pow(10, otpDigitsCount) - 1;
        return String.valueOf(random.nextInt(max - min + 1) + min);
    }


    private OtpDetails getOtpToValidate(String requestId) {
        logger.debug("validating otp { requestId: " + requestId + " }");

        OtpDetails otpDetailsObject = otpRepository.findByRequestId(requestId);

        if (otpDetailsObject == null) {
            logger.debug("otp with request id = " + requestId + " not found");
            throw new OTPNotFoundException();
        }

        otpDetailsObject.setValidationAttempts(otpDetailsObject.getValidationAttempts() + 1);
        long id = saveOTP(otpDetailsObject);
        otpDetailsObject.setId(id);
        logger.debug("OTP is {}", otpDetailsObject);
        return otpDetailsObject;
    }

    @Override
    public int validate(String requestId, String OtpValue) {
        logger.debug("validating otp { requestId: " + requestId + " }");
        return validate(requestId, OtpValue, "");
    }

    @Override
    public int validate(String requestId, String OtpValue, String transType) {
        OtpDetails otpDetailsObject = getOtpToValidate(requestId);
        Date currentTime = Calendar.getInstance().getTime();
        if (!otpDetailsObject.getTranType().equals(transType)) {
            return 0;
        }
        if (otpDetailsObject.isValidated()) {
            return -2;
        }
        if (otpDetailsObject.getValidationAttempts() > otpMaxValidationAttempts) {
            logger.debug("otp validation failed max attempts exceeded");
            return -1;
        }

        if (currentTime.after(otpDetailsObject.getExpiryTime())) {
            logger.debug("otp validation failed expired");
            return -1;
        }

        if (!BCrypt.checkpw(OtpValue, otpDetailsObject.getOtp())) {
            logger.debug("otp validation failed incorrect otp");
            return 0;
        }

        logger.debug("otp validated successfully");
        otpDetailsObject.setValidated(true);
        saveOTP(otpDetailsObject);
        return 1;
    }

    @Transactional(dontRollbackOn = {Exception.class})
    private Long saveOTP(OtpDetails otpDetails) {
        try {
            otpDetails = otpRepository.saveAndFlush(otpDetails);
            logger.debug("saved with Id {}", otpDetails.getId());
            return otpDetails.getId();
        } catch (Throwable ex) {
            logger.error("Unable to save request in db due to {}", ex);
        }
        return 0l;
    }
}
