package com.alphapay.payEngine.utilities;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@Slf4j
public class NotificationMaskingUtil {

    public static final String OTP_RESEND_MSG = "otp.registration.resend.msg";

    public enum MaskType {
        MOBILE,
        EMAIL,
        ACCOUNT
    }

    @Autowired
    private MessageSource messageSource;

    public String generateMaskedMessage(String identifier, MaskType identifierType, String locale) {

        String localizedMessage = messageSource.getMessage(OTP_RESEND_MSG, null, new Locale(locale)) + " " + maskInfo(identifier, identifierType);
        return localizedMessage;

    }

    public String generateMaskedMessage(String identifier, MaskType identifierType) {
        return maskInfo(identifier, identifierType);
    }

    public static String maskInfo(String info, MaskType type) {
        log.debug("Generating message for {} : typ {}", info, type.name());
        StringBuilder maskedInfo = new StringBuilder();
        if (info == null || info.isEmpty()) {
            return "Invalid input";
        }

        switch (type) {
            case MOBILE:
                int length = info.length();
                maskedInfo.append(info.substring(0, Math.min(3, length))); // First 3 digits
                maskedInfo.append("***-***");
                maskedInfo.append(info.substring(Math.max(length - 4, 0), length)); // Last 4 digits
                break;

            case EMAIL:
                String[] emailParts = info.split("@");
                if (emailParts.length != 2) {
                    return "Invalid email";
                }

                String localPart = emailParts[0];
                length = localPart.length();
                maskedInfo.append(localPart.substring(0, Math.min(2, length))); // First 2 characters
                maskedInfo.append("***");
                maskedInfo.append(localPart.substring(Math.max(length - 2, 0), length)); // Last 2 characters
                maskedInfo.append("@");
                maskedInfo.append(emailParts[1]);
                break;
            case ACCOUNT:
                length = info.length();
                maskedInfo.append("***-***");
                maskedInfo.append(info.substring(Math.max(length - 3, 0), length)); // Last 3 digits
                break;

            default:
                return "Invalid type";
        }

        return maskedInfo.toString();
    }
}
