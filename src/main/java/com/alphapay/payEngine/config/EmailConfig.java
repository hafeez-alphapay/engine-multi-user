package com.alphapay.payEngine.config;

import com.alphapay.payEngine.notification.models.NotificationConfig;
import com.alphapay.payEngine.notification.repositories.NotificationConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration class for setting up email capabilities.
 */
@Configuration
@Slf4j
public class EmailConfig {

    private final ConcurrentHashMap<String, EmailSenderConfiguration> mailSenderMap = new ConcurrentHashMap<>();
    @Autowired
    private NotificationConfigRepository notificationConfigRepository;

    @PostConstruct
    public void init() {
        loadMailSenderConfigs();
    }

    /**
     * Load or reload JavaMailSender configurations from the database.
     */
    public void loadMailSenderConfigs() {
        log.info("Initializing/Refreshing JavaMailSender instances");

        List<NotificationConfig> configs = notificationConfigRepository.findAll();
        for (NotificationConfig config : configs) {
            try {
                updateMailSender(config);
            } catch (Throwable ex) {
                log.error("Unable to intialize mail config for {}", config);
                log.error("Unable to intialize mail config for {}", ex.getMessage());
            }
        }
    }

    /**
     * Update a specific JavaMailSender configuration.
     *
     * @param config the new configuration
     */
    public void updateMailSender(NotificationConfig config) {
        JavaMailSender mailSender = new JavaMailSenderImpl();
        ((JavaMailSenderImpl) mailSender).setHost(config.getSmtpHostname());
        ((JavaMailSenderImpl) mailSender).setPort(config.getSmtpPort());
        ((JavaMailSenderImpl) mailSender).setUsername(config.getSmtpUserName());
        if (config.getSmtpPassword() != null)
            ((JavaMailSenderImpl) mailSender).setPassword(config.getSmtpPassword());

        Properties props = ((JavaMailSenderImpl) mailSender).getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        if (config.getSmtpAuth() != null)
            props.put("mail.smtp.auth", config.getSmtpAuth().toString());
        if (config.getSmtpEnableStarttls() != null)
            props.put("mail.smtp.ssl.enable", config.getSmtpEnableStarttls().toString());
        props.put("mail.debug", "true");  // You may want to make this configurable

        mailSenderMap.put(config.getApplicationId(), new EmailSenderConfiguration((JavaMailSenderImpl) mailSender, config.getSmtpUserName(), config.getEmailHeader(), config.getMailHtmlTemplateFile(), config.getEmailCcList(),config.getEmailToList()));
    }

    /**
     * Fetch JavaMailSender by applicationId.
     *
     * @param applicationId Application ID
     * @return JavaMailSender instance
     */
    public EmailSenderConfiguration getJavaMailSender(String applicationId) {
        return mailSenderMap.get(applicationId);
    }
}
