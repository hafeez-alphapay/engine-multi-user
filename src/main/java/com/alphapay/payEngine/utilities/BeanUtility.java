package com.alphapay.payEngine.utilities;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class BeanUtility {
    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        emptyNames.add("id");
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    public static void copyProperties(Object source, Object dest) {
        BeanUtils.copyProperties(source, dest, getNullPropertyNames(source));
    }

    public static String maskPan(String panNumber) {
        //Avoid Null Pointer exception if any
        if (org.apache.commons.lang3.StringUtils.isBlank(panNumber) || panNumber.length() < 12)
            return panNumber;
        return panNumber.substring(0, 6)
                + StringUtils.repeat("*", 5)
                + panNumber.substring(11);
    }

    public static String maskDigits(String input) {
        if (input == null || input.length() != 16) {
            log.error("Input string must be exactly 16 digits long");
            return "";
        }

        // Ensure that the string contains only digits
        if (!input.matches("\\d{16}")) {
            log.error("Input string must contain only digits");
            return "";
        }

        // Extract parts and create the masked string
        String firstSixDigits = input.substring(0, 6);
        String lastFourDigits = input.substring(12);
        String maskedString = firstSixDigits + "******" + lastFourDigits;

        return maskedString;
    }

    public static void copyNonNullProperties(Object source, Object target) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }

        String[] result = new String[emptyNames.size()];
        BeanUtils.copyProperties(source, target, emptyNames.toArray(result));
    }

    public static boolean isExpired(Date expiryDateTime) {
        // Get the current date and time
        Date now = new Date();
        // Compare the expiry date with the current date
        return expiryDateTime.before(now);
    }

    public static boolean isExpired(LocalDateTime expiryTime) {
        LocalDateTime now =LocalDateTime.now();
        return expiryTime != null && expiryTime.isBefore(LocalDateTime.now());
    }

    private static final SecureRandom random = new SecureRandom();

    public static String generateToken(int length) {
        // Generate random bytes of the required length
        byte[] randomBytes = new byte[length];
        random.nextBytes(randomBytes);

        // Encode the random bytes to URL-safe Base64
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        // Optionally, if you want to ensure the token length matches the one you provided,
        // you could trim or pad it here, although the example token is quite long
        // (256 characters or more). You can adjust the length parameter accordingly.
        return token;
    }

}
