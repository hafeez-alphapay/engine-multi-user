package com.alphapay.payEngine.account.management.serviceImpl;

import com.alphapay.payEngine.account.management.dto.request.ResetPasswordRequest;
import com.alphapay.payEngine.account.management.dto.response.ForgotPasswordResponse;
import com.alphapay.payEngine.account.management.dto.response.ResetPasswordResponse;
import com.alphapay.payEngine.account.management.exception.ExpiredResetPasswordLink;
import com.alphapay.payEngine.account.management.exception.PasswordUsedException;
import com.alphapay.payEngine.account.management.model.ForgotPasswordRequestEntity;
import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.management.repository.ForgotPasswordRequestRepository;
import com.alphapay.payEngine.account.management.repository.UserRepository;
import com.alphapay.payEngine.account.management.service.ResetPasswordService;
import com.alphapay.payEngine.account.roles.exception.UserNotFoundException;
import com.alphapay.payEngine.notification.services.INotificationService;
import com.alphapay.payEngine.utilities.BeanUtility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static com.alphapay.payEngine.utilities.UtilHelper.generateStrongPassword;

@Slf4j
@Service
public class ResetPasswordServiceImpl implements ResetPasswordService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    HttpServletRequest httpServletRequest;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private INotificationService notificationService;

    @Autowired
    private ForgotPasswordRequestRepository forgotPasswordRequestRepository;

    @Value("${email.reset.password.link.url}")
    private String resetPasswordLinkUrl;

    @Value("${default.reset.password.expiry.time}")
    private int expiryPasswordTime;

    //875690
    @Override
    @Transactional
    public ResetPasswordResponse resetPasswordWithRandom(ResetPasswordRequest request) {
        Optional<UserEntity> merchantUser = userRepository.findById(request.getUserId());
        if (merchantUser.isEmpty()) {
            throw new UserNotFoundException();
        }
        String newPassword = generateStrongPassword(8);
        String encryptedNewPassword = passwordEncoder.encode(newPassword);
        // Load existing password history safely (comma-separated hashed passwords)
        String historyStr = Optional.ofNullable(merchantUser.get().getPasswordHistory()).orElse("").trim();
        List<String> history = new ArrayList<>();
        if (!historyStr.isBlank()) {
            for (String h : historyStr.split(",")) {
                String v = h.trim();
                if (!v.isEmpty()) history.add(v);
            }
        }

        // Check if the new plain-text password matches any stored hash in history
        boolean existsInHistory = history.stream().anyMatch(h -> {
            try {
                return passwordEncoder.matches(request.getNewPassword(), h);
            } catch (Exception ex) {

                return false;
            }
        });

        if (existsInHistory) {
            throw new PasswordUsedException();
        }

        // If not found, append the new hashed password, keeping only the last 4 entries
        while (history.size() >= 4) {
            history.remove(0);
        }
        history.add(encryptedNewPassword);
        merchantUser.get().setPasswordHistory(String.join(",", history));
        merchantUser.get().getUserDetails().setPassword(encryptedNewPassword);
        merchantUser.get().setLoginTryCount(0);
        merchantUser.get().setLocked(false);


        try {
            String[] msgKeys = {merchantUser.get().getUserDetails().getFullName(), newPassword};
            notificationService.sendEmailNotification(request.getRequestId(), "ADMIN_RESET_PASSWORD_EMAIL", msgKeys, merchantUser.get().getUserDetails().getEmail(), "", Locale.ENGLISH, httpServletRequest.getAttribute("applicationId") + "", null);
        } catch (Throwable e) {
            log.debug("Unable to send reset password email notification");
        }
        ResetPasswordResponse response = new ResetPasswordResponse();
        response.setResponseCode(200);
        response.setResponseMessage("Password reset successfully");
        response.setStatus("Success");
        return response;
    }

    @Override
    @Transactional
    public ResetPasswordResponse settingResetPassword(ResetPasswordRequest request) {
        Optional<UserEntity> merchantUser = userRepository.findById(request.getUserId());
        if (merchantUser.isEmpty()) {
            throw new UserNotFoundException();
        }

        if (!passwordEncoder.matches(request.getOldPassword(), merchantUser.get().getUserDetails().getPassword())) {
            try {
                String[] msgKeys = {merchantUser.get().getUserDetails().getFullName()};
                notificationService.sendEmailNotification(request.getRequestId(), "USER_CHANGE_EMAIL_WRONG_PASSWORD", msgKeys, merchantUser.get().getUserDetails().getEmail(), "", Locale.ENGLISH, httpServletRequest.getAttribute("applicationId") + "", null);
            } catch (Throwable e) {
                log.debug("Unable to send failed change password email notification");
            }
            throw new PasswordUsedException();
        }

        String encryptedNewPassword = passwordEncoder.encode(request.getNewPassword());

        // Load existing password history safely (comma-separated hashed passwords)
        String historyStr = Optional.ofNullable(merchantUser.get().getPasswordHistory()).orElse("").trim();
        List<String> history = new ArrayList<>();
        if (!historyStr.isBlank()) {
            for (String h : historyStr.split(",")) {
                String v = h.trim();
                if (!v.isEmpty()) history.add(v);
            }
        }

        // Check if the new plain-text password matches any stored hash in history
        boolean existsInHistory = history.stream().anyMatch(h -> {
            try {
                return passwordEncoder.matches(request.getNewPassword(), h);
            } catch (Exception ex) {

                return false;
            }
        });

        if (existsInHistory) {
            throw new PasswordUsedException();
        }

        // If not found, append the new hashed password, keeping only the last 4 entries
        while (history.size() >= 4) {
            history.remove(0);
        }
        history.add(encryptedNewPassword);
        merchantUser.get().setPasswordHistory(String.join(",", history));

        merchantUser.get().getUserDetails().setPassword(encryptedNewPassword);
        try {
            String[] msgKeys = {merchantUser.get().getUserDetails().getFullName()};
            notificationService.sendEmailNotification(request.getRequestId(), "USER_CHANGE_EMAIL", msgKeys, merchantUser.get().getUserDetails().getEmail(), "", Locale.ENGLISH, httpServletRequest.getAttribute("applicationId") + "", null);
        } catch (Throwable e) {
            log.debug("Unable to send change password email notification");
        }
        ResetPasswordResponse response = new ResetPasswordResponse();
        response.setResponseCode(200);
        response.setResponseMessage("Password reset successfully");
        response.setStatus("Success");
        return response;
    }


    @Override
    public ForgotPasswordResponse forgotPassword(ResetPasswordRequest request) {

        UserEntity merchantUser = userRepository.findByEmail(request.getEmail());
        if (merchantUser != null) {
            String resetPasswordSession = UUID.randomUUID().toString();
            ForgotPasswordRequestEntity entity = new ForgotPasswordRequestEntity();
            entity.setPasswordChangedStatus("Active");
            entity.setSessionId(resetPasswordSession);

            entity.setExpiryTime(LocalDateTime.now().plusHours(expiryPasswordTime));
            entity.setUserId(merchantUser.getId());

            forgotPasswordRequestRepository.save(entity);
            String emailTemplateId = "email_resetpassword.html";
            try {
                String[] msgKeys = {resetPasswordLinkUrl + resetPasswordSession};
                notificationService.sendEmailNotification(request.getRequestId(), "FORGOT_PASSWORD_EMAIL", msgKeys, merchantUser.getUserDetails().getEmail(), "", Locale.ENGLISH, httpServletRequest.getAttribute("applicationId") + "", emailTemplateId);
            } catch (Throwable e) {
                log.debug("Unable to send forgot password email notification", e);
            }
        }
        ForgotPasswordResponse response = new ForgotPasswordResponse();
        response.setResponseCode(200);
        response.setResponseMessage("If your email exists in our records, you will receive a password reset link shortly. Please check your inbox and spam folder.");
        response.setStatus("Success");
        return response;
    }

    @Override
    @Transactional
    public ResetPasswordResponse emailResetPassword(ResetPasswordRequest request) {
        ForgotPasswordRequestEntity forgotPasswordRequest = forgotPasswordRequestRepository.findBySessionId(request.getResetPasswordId());
        if (forgotPasswordRequest == null) {
            throw new ExpiredResetPasswordLink();
        }
        if (BeanUtility.isExpired(forgotPasswordRequest.getExpiryTime())) {
            forgotPasswordRequest.setPasswordChangedStatus("Expired");
            throw new ExpiredResetPasswordLink();
        }

        Optional<UserEntity> merchantUser = userRepository.findById(forgotPasswordRequest.getUserId());

        if (merchantUser.isEmpty()) {
            throw new UserNotFoundException();
        }

        String encryptedNewPassword = passwordEncoder.encode(request.getNewPassword());

        // Load existing password history safely (comma-separated hashed passwords)
        String historyStr = Optional.ofNullable(merchantUser.get().getPasswordHistory()).orElse("").trim();
        List<String> history = new ArrayList<>();
        if (!historyStr.isBlank()) {
            for (String h : historyStr.split(",")) {
                String v = h.trim();
                if (!v.isEmpty()) history.add(v);
            }
        }

        // Check if the new plain-text password matches any stored hash in history
        boolean existsInHistory = history.stream().anyMatch(h -> {
            try {
                return passwordEncoder.matches(request.getNewPassword(), h);
            } catch (Exception ex) {

                return false;
            }
        });

        if (existsInHistory) {
            throw new PasswordUsedException();
        }

        // If not found, append the new hashed password, keeping only the last 4 entries
        while (history.size() >= 4) {
            history.remove(0);
        }
        history.add(encryptedNewPassword);
        merchantUser.get().setPasswordHistory(String.join(",", history));

        merchantUser.get().getUserDetails().setPassword(encryptedNewPassword);
        merchantUser.get().setLoginTryCount(0);
        merchantUser.get().setLocked(false);

        ResetPasswordResponse response = new ResetPasswordResponse();
        response.setResponseCode(200);
        response.setResponseMessage("Password reset successfully");
        response.setStatus("Success");

        forgotPasswordRequest.setPasswordChangedStatus("Successful");
        return response;
    }

}
