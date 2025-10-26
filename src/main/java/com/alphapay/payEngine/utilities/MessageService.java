package com.alphapay.payEngine.utilities;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MessageService {

    private final MessageSource messageSource;

    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Get localized message from the resource bundle.
     *
     * @param key    the key for the message in the resource bundle
     * @param locale the locale to use for localization
     * @return the localized message
     */
    public String getLocalizedMessage(String key, Locale locale) {
        try {
            // Fetch the message from the resource bundle
            return messageSource.getMessage(key, null, locale);
        } catch (Exception e) {
            // Fallback if the key is not found
            return "Message not found for key: " + key;
        }
    }
}
