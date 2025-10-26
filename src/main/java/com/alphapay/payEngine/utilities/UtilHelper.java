package com.alphapay.payEngine.utilities;

import com.alphapay.payEngine.integration.dto.response.AlphaWebhookResponse;
import com.alphapay.payEngine.service.bean.BaseRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UtilHelper {

    private static final String HEADER_X_FORWARDED_FOR = "X-FORWARDED-FOR";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * The allowed currencies, injected from the property 'allowed.currencies'.
     * Defaults to 'AED' if the property is not specified.
     */
    @Value("${allowed.currencies:AED}")
    private String allowedCurrencies;
    @Value("${default.link.expiry.in.days}")
    private int defaultExpiryInDays;

    public static boolean isValidEmail(String email) {
        boolean isValid = false;

        if (email == null)
            return isValid;

        String emailRegex = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        isValid = EmailValidator.getInstance().isValid(email);

        return isValid;
    }

    public static boolean isValidMobileNumber(String mobile) {
        boolean isValid = false;

        if (mobile == null)
            return isValid;

        String mobileRegex = "^0\\d{9}$";
        isValid = mobile.matches(mobileRegex);

        return isValid;
    }

    public static boolean isValidCardNumber(String pan) {
        boolean isValid = false;

        return isValid;
    }

    public static String generateRandomToken(int length) {
        SecureRandom random = new SecureRandom();
        return String.format("%05d", random.nextInt((int) Math.pow(10, length)));
        //return new BigInteger(length * 5, random).toString(32);
    }

    public static String generateRandomUUID() {
        return UUID.randomUUID().toString();
    }

    public static String getMobileNumberInternationalFormat(String mobile) {
        //TODO different countries
        if (isValidMobileNumber(mobile))
            return mobile.replaceFirst("0", "249");

        return mobile;
    }

    public static String getFullMobileNumber(String customerContact, String countryCode) {
        if (customerContact == null || customerContact.isEmpty() || countryCode == null || countryCode.isEmpty()) {
            return null;
        }

        customerContact = customerContact.replaceAll("[\\s\\-()]", "");

        if (customerContact.startsWith("0")) {
            customerContact = customerContact.substring(1);
        }

        if (customerContact.startsWith(countryCode)) {
            customerContact = customerContact.substring(countryCode.length());
        }

        return countryCode + customerContact;
    }

    public static String getMobileNumberLocalFormat(String mobile) {
        //TODO different countries
        if (isValidInternationalMobileNumber(mobile))
            return mobile.replaceFirst("249", "0");

        return mobile;
    }

    private static boolean isValidInternationalMobileNumber(String mobile) {
        String mobileRegex = "^249\\d{9}$";
        return mobile != null && mobile.matches(mobileRegex);
    }

    public static String sha256Digest(String clearText) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(clearText.getBytes("UTF-8"));

            byte byteData[] = md.digest();

            //convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return clearText;
        } catch (UnsupportedEncodingException e) {
            return clearText;
        }
    }

    public static <T extends Enum<T>> T getEnumFromString(Class<T> enumClass, String value) {
        if (enumClass == null) {
            throw new IllegalArgumentException("EnumClass value can't be null.");
        }

        for (Enum<?> enumValue : enumClass.getEnumConstants()) {
            if (enumValue.toString().equalsIgnoreCase(value)) {
                return (T) enumValue;
            }
        }

        //Construct an error message that indicates all possible values for the enum.
        StringBuilder errorMessage = new StringBuilder();
        boolean bFirstTime = true;
        for (Enum<?> enumValue : enumClass.getEnumConstants()) {
            errorMessage.append(bFirstTime ? "" : ", ").append(enumValue);
            bFirstTime = false;
        }
        throw new IllegalArgumentException(value + " is invalid value. Supported values are " + errorMessage);
    }

    public static String toInternationalUAEMobile(String mobile) {
        String countryCode = "+971"; // UAE country code
        String reExp = "(^\\+?" + countryCode + "|^00" + countryCode + "|^" + countryCode + ")(\\d{9})";
        mobile = mobile.replaceFirst(reExp, "$2");
        if (mobile.startsWith("0")) {
            mobile = mobile.substring(1);

        }
        return countryCode + mobile;
    }

    public static String toInternationalSDGMobileWithoutPlus(String mobile) {
        String countryCode = "971"; // UAE country code
        String reExp = "(^\\+?" + countryCode + "|^00" + countryCode + "|^" + countryCode + ")(\\d{9})";
        mobile = mobile.replaceFirst(reExp, "$2");
        if (mobile.startsWith("0")) {
            mobile = mobile.substring(1);
        }
        return countryCode + mobile;
    }

    public static String calculateCreateInvoiceHash(String configHashKey, String configHashSalt, String requestId, Double amount, String serviceId, String currency, String invoiceReference, String description,
                                                    String customerName, String paymentLinkUrl) {
        String hash = "";

        String message = configHashKey + "|" +
                (requestId != null ? requestId : "") + "|" +
                (amount != null ? amount.toString() : "") + "|" +
                (serviceId != null ? serviceId : "") + "|" +
                (description != null ? description : "") + "|" +
                (currency != null ? currency : "") + "|" +
                (invoiceReference != null ? invoiceReference : "") + "|" +
                (customerName != null ? customerName : "") + "|" +
                (paymentLinkUrl != null ? paymentLinkUrl : "") + "|" +
                configHashSalt;

        hash = DigestUtils.sha256Hex(message);

        return hash;
    }

    public static String calculateCreateInvoiceRequestHash(String configHashKey, String configHashSalt, String requestId, Double amount, String serviceId, String currency, String invoiceReference, String description, String customerName) {
        String hash = "";

        String message = configHashKey + "|" +
                (requestId != null ? requestId : "") + "|" +
                (amount != null ? amount.toString() : "") + "|" +
                (serviceId != null ? serviceId : "") + "|" +
                (description != null ? description : "") + "|" +
                (currency != null ? currency : "") + "|" +
                (invoiceReference != null ? invoiceReference : "") + "|" +
                (customerName != null ? customerName : "") + "|" +
                configHashSalt;

        hash = DigestUtils.sha256Hex(message);

        return hash;
    }

    public static String calculateRequestHash(
            String configHashKey,
            String configHashSalt,
            String requestId,
            Double amount,
            Double discountedPrice,
            String paymentLinkTitle,
            String currency,
            String invoiceId,
            Long merchantId
    ) {
        String hash = "";

        String message = configHashKey + "|" +
                (requestId != null ? requestId : "") + "|" +
                (amount != null ? amount.toString() : "") + "|" +
                (discountedPrice != null ? discountedPrice.toString() : "") + "|" +
                (paymentLinkTitle != null ? paymentLinkTitle : "") + "|" +
                (currency != null ? currency : "") + "|" +
                (invoiceId != null ? invoiceId : "") + "|" +
                (merchantId != null ? merchantId : "") + "|" +
                configHashSalt;

        // Calculate the SHA-256 hash
        hash = DigestUtils.sha256Hex(message);

        return hash;
    }

    public static Locale getLocale(BaseRequest request) {
        return "en".equalsIgnoreCase(request.getAuditInfo().getAcceptLanguage()) ? Locale.ENGLISH : new Locale("ar");
    }

    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 10) {
            // Not enough digits to mask properly
            return cardNumber;
        }

        int totalLength = cardNumber.length();
        String firstSix = cardNumber.substring(0, 6);
        String lastFour = cardNumber.substring(totalLength - 4);

        // Mask the middle digits
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < totalLength - 10; i++) {
            masked.append('*');
        }

        return firstSix + masked + lastFour;
    }

    /**
     * Extracts the local number from an international phone number.
     *
     * @param internationalNumber the full international number (e.g., "+971547029323")
     * @return the local number (e.g., "547029323") or null if the input is invalid
     */
    public static String extractLocalNumber(String internationalNumber) {
        if (internationalNumber == null || !internationalNumber.startsWith("+971")) {
            return internationalNumber;
        }

        // Remove the country code "+971" and return the rest
        return internationalNumber.substring(4);
    }

    public static String getMediaType(byte[] fileBytes) {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new IllegalArgumentException("File bytes cannot be null or empty");
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes)) {
            String mimeType = URLConnection.guessContentTypeFromStream(bais);
            log.debug("mimeTypemimeTypemimeType:{}",mimeType);
            return mimeType != null ? mimeType : "application/octet-stream"; // Default to binary if unknown
        } catch (Exception e) {
            throw new RuntimeException("Failed to determine media type", e);
        }
    }

    public static String generateStrongPassword(int length) {
        // Define character sets for the password
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digits = "0123456789";
        String specialChars = "!@#$%^&*()-_=+[{]}\\|;:'\",<.>/?";

        // Combine all character sets into one
        String allCharacters = lowercase + uppercase + digits + specialChars;

        // Use SecureRandom for better randomness
        SecureRandom random = new SecureRandom();

        StringBuilder password = new StringBuilder();

        // Ensure the password contains at least one character from each set
        password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        // Fill the rest of the password with random characters from the combined set
        for (int i = password.length(); i < length; i++) {
            password.append(allCharacters.charAt(random.nextInt(allCharacters.length())));
        }

        // Convert the StringBuilder to a String and return
        return password.toString();
    }

    public static String generateSignature(AlphaWebhookResponse response, String secretKey) throws Exception {
        // Convert object fields to a sorted key-value map
        Map<String, String> sortedDataMap = extractFieldsAsMap(response);

        // Convert map to formatted key=value string
        String sortedKeyValueString = sortedDataMap.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(","));

        System.out.println("🔹 Ordered Data String: " + sortedKeyValueString);

        // Encode the secret key in UTF-8
        byte[] secretKeyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

        // Generate HMAC SHA-256 signature
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyBytes, "HmacSHA256");
        sha256Hmac.init(secretKeySpec);
        byte[] hashedBytes = sha256Hmac.doFinal(sortedKeyValueString.getBytes(StandardCharsets.UTF_8));

        // Encode the result in Base64
        return Base64.getEncoder().encodeToString(hashedBytes);
    }

    private static Map<String, String> extractFieldsAsMap(AlphaWebhookResponse response) throws IllegalAccessException {
        Map<String, String> dataMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        // Use reflection to iterate over all fields in the class
        for (Field field : AlphaWebhookResponse.class.getDeclaredFields()) {
            field.setAccessible(true); // Allow access to private fields
            Object value = field.get(response);

            // Convert null values to empty strings
            String valueStr = (value == null) ? "" : value.toString();
            dataMap.put(field.getName(), valueStr);
        }

        return dataMap;
    }

    public static JsonNode mergeData(Map<String, Object> dataMap) {
        ObjectNode mergedData = objectMapper.createObjectNode();
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            mergedData.set(key, objectMapper.valueToTree(value));
        }

        return mergedData;
    }

    /**
     * Checks if the specified currency is supported.
     *
     * @param currency the currency to check.
     * @return true if the currency is supported, false otherwise.
     */
    public boolean isSupportedCurrency(String currency) {
        String[] currencies = allowedCurrencies.split(",");
        for (String cr : currencies) {
            if (cr.trim().equalsIgnoreCase(currency))
                return true;
        }
        return false;
    }

    /**
     * Determines the expiry date for a payment link.
     *
     * @param expiry      The provided expiry date.
     * @param currentDate The current date.
     * @return The calculated expiry date.
     */
    public Date getExpiryDate(Date expiry, Date currentDate) {
        if (expiry != null) {
            return expiry;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            calendar.add(Calendar.DATE, defaultExpiryInDays);
            return calendar.getTime();
        }
    }

}
