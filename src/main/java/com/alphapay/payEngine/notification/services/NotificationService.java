package com.alphapay.payEngine.notification.services;

import com.alphapay.payEngine.account.management.repository.UserRepository;
import com.alphapay.payEngine.common.httpclient.service.RestClientService;
import com.alphapay.payEngine.config.AppSetting;
import com.alphapay.payEngine.config.EmailConfig;
import com.alphapay.payEngine.config.EmailSenderConfiguration;
import com.alphapay.payEngine.notification.exceptions.NotificationConfigDoesNotExistException;
import com.alphapay.payEngine.notification.exceptions.SMSConfigIsNullException;
import com.alphapay.payEngine.notification.models.Notification;
import com.alphapay.payEngine.notification.models.NotificationConfig;
import com.alphapay.payEngine.notification.repositories.NotificationConfigRepository;
import com.alphapay.payEngine.notification.repositories.NotificationRepository;
import com.alphapay.payEngine.storage.model.MerchantDocuments;
import com.alphapay.payEngine.storage.serviceImpl.RemoteFileSystemStorageService;
import com.alphapay.payEngine.utilities.UtilHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;

/**
 * NotificationService is responsible for sending different types of notifications.
 */
@Service
@Slf4j
public class NotificationService implements INotificationService {

    public static final String FORMATTED_MESSAGE_IN_HTML_KEY = "{HTML_FORMATTED_MESSAGE}";
    public static final String HTML_MAIL_TEMPLATES_LOCATION = "mailtemplates/";
    @Autowired
    HttpServletRequest httpServletRequest;
    @Value("${sms.request.body.url.params:false}")
    private boolean bodyInQueryParams;
    @Autowired
    private Environment environment;
    @Autowired
    private EmailConfig emailConfig;
    @Autowired
    private NotificationConfigRepository notificationConfigRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private RestClientService restClientService;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private AppSetting appSetting;
    @Autowired
    private UserRepository userRepository;
    @Value("${aws.sns.sms.senderId}")
    private String AWS_SNS_SMS_SenderID;

    @Value("${aws.sns.sms.maxPrice}")
    private String AWS_SNS_SMS_MaxPrice;

    @Value("${aws.sns.sms.smsType}")
    private String AWS_SNS_SMS_SMSType;

    @Autowired
    private RemoteFileSystemStorageService sftpService;

    public static String convertPhoneNumber(String mobile) {
        if (mobile.startsWith("0")) {
            return "249" + mobile.substring(1);
        }
        return mobile;
    }

    /**
     * Sends an email notification.
     *
     * @param requestId       The request ID for the email notification.
     * @param templateId      The template ID to use for the email.
     * @param keys            The keys used in the template.
     * @param email           The recipient email address.
     * @param lang            The language of the email.
     * @param applicationId   The ID of the application sending the email.
     * @param emailTemplateId
     */
    @Override
    public void sendEmailNotification(String requestId, String templateId, String[] keys, String email, String businessName, Locale lang, String applicationId, String emailTemplateId) {
        logRequest("email", requestId, templateId, email, applicationId);

        if (!isValidNotificationConfig(applicationId)) {
            log.trace("Notification Config Does Not Exist");
            throw new NotificationConfigDoesNotExistException();
        }


            CompletableFuture.runAsync(() -> {
                try {
                    String message = getLocalizedMessage(templateId, keys, lang);
                    EmailSenderConfiguration senderConfig = emailConfig.getJavaMailSender(applicationId);
                    log.debug("senderConfig.getTemplate()::{}", senderConfig.getTemplate());
                    if (StringUtils.isNotBlank(senderConfig.getTemplate())) {
                        sendEmailHtml(applicationId, email, message, null, null, emailTemplateId);
                    } else {
                        sendEmail(applicationId, email, message);
                    }
                    saveNotification(requestId, message, templateId, businessName, lang, email, null);
                    log.debug("Async EMAIL sent successfully [requestId={}, templateId={}, email={}, applicationId={}]", requestId, templateId, email, applicationId);
                } catch (Exception ex) {
                    // Log with rich context and rethrow so it propagates to the future.
                    log.error("Async EMAIL send failed [requestId={}, templateId={}, email={}, applicationId={}]: {}", requestId, templateId, email, applicationId, ex.getMessage(), ex);
                    throw new RuntimeException(ex);
                }
            }).whenComplete((ignored, ex) -> {
                if (ex != null) {
                    // Completion-stage logging to ensure visibility even if exceptions are wrapped.
                    log.warn("Async EMAIL completion had an exception [requestId={}, templateId={}, email={}, applicationId={}]", requestId, templateId, email, applicationId, ex);
                }
            });
    }

    /**
     * Sends an email notification.
     *
     * @param requestId     The request ID for the email notification.
     * @param templateId    The template ID to use for the email.
     * @param keys          The keys used in the template.
     * @param email         The recipient email address.
     * @param lang          The language of the email.
     * @param applicationId The ID of the application sending the email.
     */
    @Override
    public void sendEmailNotification(String requestId, String templateId, String[] keys, String email, String businessName, Locale lang, String applicationId, String title,  byte[] attachment) {
        logRequest("email", requestId, templateId, email, applicationId);

        if (!isValidNotificationConfig(applicationId)) {
            log.trace("Notification Config Does Not Exist");
            throw new NotificationConfigDoesNotExistException();
        }

        try {
            String message = getLocalizedMessage(templateId, keys, lang);
            EmailSenderConfiguration senderConfig = emailConfig.getJavaMailSender(applicationId);
            if (StringUtils.isNotBlank(senderConfig.getTemplate())) {
                sendEmailHtml(applicationId, email, message, title, attachment, null);
            } else {
                sendEmail(applicationId, email, message);

            }
            saveNotification(requestId, message, templateId, businessName, lang, email, null);
        } catch (Exception e) {
            log.error("Email notification not sent", e);
        }
    }

    /**
     * Sends a mobile notification, either via SMS or WhatsApp.
     *
     * @param requestId     The request ID for the mobile notification.
     * @param templateId    The template ID to use for the mobile message.
     * @param keys          The keys used in the template.
     * @param mobile        The recipient mobile number.
     * @param lang          The language of the mobile message.
     * @param applicationId The ID of the application sending the message.
     * @param isWhatsApp    Flag indicating whether to send via WhatsApp.
     */
    @Override
    public void sendMobileNotification(String requestId, String templateId, String[] keys, String mobile, String businessName, Locale lang, String applicationId, boolean isWhatsApp) {
        String internationalMobile = mobile;
        logRequest("mobile", requestId, templateId, internationalMobile, applicationId);

        if (internationalMobile.length() <= 10) {
            internationalMobile = UtilHelper.toInternationalUAEMobile(internationalMobile);
        }

        if (!isValidNotificationConfig(applicationId)) {
            log.trace("Notification Config Does Not Exist");
            throw new NotificationConfigDoesNotExistException();
        }

        String message = getLocalizedMessage(templateId, keys, lang);
        NotificationConfig config = notificationConfigRepository.findByApplicationId(applicationId);

        if (isInvalidSMSGatewayConfig(config)) {
            log.trace("SMS Config Is Null");
            throw new SMSConfigIsNullException();
        }
            CompletableFuture.runAsync(() -> {
                try {
                    sendSMSMessage(mobile, message, config);
                    saveNotification(requestId, message, templateId, businessName, lang, null, mobile);
                    log.debug("Async SMS sent successfully [requestId={}, templateId={}, mobile={}, applicationId={}]", requestId, templateId, mobile, applicationId);
                } catch (Exception ex) {
                    // Log with rich context and rethrow so it propagates to the future.
                    log.error("Async SMS send failed [requestId={}, templateId={}, mobile={}, applicationId={}]: {}", requestId, templateId, mobile, applicationId, ex.getMessage(), ex);
                    throw ex;
                }
            }).whenComplete((ignored, ex) -> {
                if (ex != null) {
                    // Completion-stage logging to ensure visibility even if exceptions are wrapped.
                    log.warn("Async SMS completion had an exception [requestId={}, templateId={}, mobile={}, applicationId={}]", requestId, templateId, mobile, applicationId, ex);
                }
            });
    }

    @Override
    public void sendMobileNotification(String requestId, String templateId, String[] keys, String mobile, String businessName, Locale lang, String applicationId) {
        sendMobileNotification(requestId, templateId, keys, mobile, businessName, lang, applicationId, false);
    }

    /**
     * Checks if the SMS Gateway configuration is invalid.
     *
     * @param config The NotificationConfig object.
     * @return True if invalid, otherwise false.
     */
    private boolean isInvalidSMSGatewayConfig(NotificationConfig config) {
        return config.getSmsGatewayURL() == null || config.getSmsGatewayUsername() == null || config.getSmsGatewayPassword() == null;
    }

    /**
     * Sends an SMS message.
     *
     * @param mobile  The recipient mobile number.
     * @param message The message content.
     * @param config  The NotificationConfig object.
     */

    private void sendSMSMessage(String mobile, String message, NotificationConfig config) {
        // Logic to send SMS.
        String internationalMobile = UtilHelper.toInternationalUAEMobile(mobile);
        String internationalMobileWithoutPlus = UtilHelper.toInternationalSDGMobileWithoutPlus(mobile);

        try {
            //TODO update the http request after knowing the sms gateway
            String stringRequest = "";
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setContentType(new MediaType("application", "json"));
            ResponseEntity<String> responseEntity = null;
            mobile = convertPhoneNumber(mobile);
            log.debug("SMS Gateway Sending Message To :<<< {}", mobile);
            String requestFormat = config.getSmscRequestMapper();
            VelocityContext context = new VelocityContext();
            context.put("username", config.getSmsGatewayUsername());
            context.put("password", config.getSmsGatewayPassword());
            context.put("mobile", mobile);
            context.put("internationalMobile", internationalMobile);
            context.put("internationalMobileWithoutPlus", internationalMobileWithoutPlus);
            context.put("textEncode", "utf-8");
            context.put("text", message);
            //Context Loaded now do transformation
            StringWriter transformWriter = new StringWriter();


            //  * Merge data and template
            //
            Velocity.evaluate(context, transformWriter, "Transforming to Format", requestFormat);
            log.debug("Transformed request {} ", transformWriter.toString());
            stringRequest = transformWriter.toString();
            HttpEntity<String> requestEntity = new HttpEntity<>(stringRequest, requestHeaders);
            log.debug("Outgoing Request >> {}", stringRequest);

            String url = config.getSmsGatewayURL();
            if (bodyInQueryParams) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, String> queryParams = objectMapper.readValue(stringRequest, Map.class);
                    StringBuilder queryStringBuilder = new StringBuilder();
                    for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                        if (queryStringBuilder.length() > 0) {
                            queryStringBuilder.append("&");
                        }
                        queryStringBuilder.append(entry.getKey()).append("=").append(entry.getValue());
                    }

                    url = url.concat("?").concat(queryStringBuilder.toString());
                    log.debug("Outgoing Request >> {}", url);
                } catch (Exception e) {
                    log.error(e.getMessage());
                    log.error("Error in build uri from request");
                }
            }

            try {
                responseEntity = restClientService.invokeRemoteService(url, org.springframework.http.HttpMethod.POST, requestEntity, String.class, null, restClientService.getGenericRestTemplate());
                log.trace("response status code : {}", responseEntity.getStatusCode());
                String stringResponse = responseEntity.getBody();
                log.trace("SMS Gateway Response: :<<< {}", stringResponse);
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("SMS notification not sent", e);
        }
    }

    /**
     * Checks if a NotificationConfig exists for the given channel ID.
     *
     * @param channelId The channel ID.
     * @return True if exists, otherwise false.
     */
    private boolean isValidNotificationConfig(String channelId) {
        return notificationConfigRepository.existsByApplicationId(channelId);
    }

    /**
     * Logs the details of the request.
     *
     * @param type       The type of notification.
     * @param requestId  The request ID.
     * @param templateId The template ID.
     * @param address    The address (could be email or mobile number).
     * @param channelId  The channel ID.
     */
    private void logRequest(String type, String requestId, String templateId, String address, String channelId) {
        log.trace(String.format("Sending %s {requestId: %s, templateId: %s, address: %s, applicationId: %s}", type, requestId, templateId, address, channelId));
    }

    /**
     * Sends an email.
     *
     * @param channelId      The channel ID.
     * @param to             The recipient email.
     * @param messageContent The content of the email.
     */
    private void sendEmail(String channelId, String to, String messageContent) {
        EmailSenderConfiguration senderConfig = emailConfig.getJavaMailSender(channelId);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(senderConfig.getSenderEmail());
        message.setSubject(senderConfig.getEmailSubject());
        message.setText(messageContent);
        senderConfig.getJavaMailSender().send(message);
    }

    private void sendEmailHtml(String applicationId, String to, String messageContent, String title, byte[] attachments, String emailTemplateId) throws
            MessagingException, IOException {
        EmailSenderConfiguration senderConfig = emailConfig.getJavaMailSender(applicationId);
        String senderTemplate =  senderConfig.getTemplate();
        if (emailTemplateId != null){
            senderTemplate = emailTemplateId;
        }
        MimeMessage message = senderConfig.getJavaMailSender().createMimeMessage();
        String htmlContent = readHtmlFromFile(HTML_MAIL_TEMPLATES_LOCATION + senderTemplate).replace(FORMATTED_MESSAGE_IN_HTML_KEY, messageContent);

        MimeMessageHelper helper;

        if (attachments != null ) {
            helper = new MimeMessageHelper(message, true, "utf-8");
        } else {
            helper = new MimeMessageHelper(message, "utf-8");
        }

        helper.setFrom(senderConfig.getSenderEmail());

        if (StringUtils.isNotBlank(senderConfig.getEmailToList())) {
            helper.setTo(senderConfig.getEmailToList().split(","));
        } else {
            helper.setTo(to);
        }

        if (title != null) {
            helper.setSubject(title);
        } else {
            helper.setSubject(senderConfig.getEmailSubject());
        }
        helper.setText(htmlContent, true);
        if (attachments != null) {
            try {
                DataSource ds = new ByteArrayDataSource(attachments, "application/pdf");
                String fileName = "Daily_Payment_Merchant_Report.pdf";
                helper.addAttachment(fileName, ds);
            } catch (Exception e) {
                throw new RuntimeException("Error attaching PDF: " + e.getMessage(), e);
            }
        }
        if (StringUtils.isNotBlank(senderConfig.getEmailCcList())) {
            String[] emailCcList = senderConfig.getEmailCcList().split(",");
            for (String email : emailCcList) {
                helper.addCc(email.trim());
            }
        }
        senderConfig.getJavaMailSender().send(message);

    }


    /**
     * Retrieves a localized message based on the template ID and keys.
     *
     * @param templateId The template ID.
     * @param keys       The keys.
     * @param lang       The language.
     * @return The localized message.
     */
    @Override
    public String getLocalizedMessage(String templateId, String[] keys, Locale lang) {
        return messageSource.getMessage(templateId, keys, lang);
    }

    /**
     * Initializes the ResourceBundleMessageSource for localized messages.
     */
    private void initializeMessageSource() {
        messageSource = new ResourceBundleMessageSource();
//        messageSource.setBasename("i18n/notifications");
    }

    /**
     * Saves the notification to the database.
     *
     * @param requestId  The request ID.
     * @param message    The message content.
     * @param templateId The template ID.
     * @param lang       The language.
     * @param email      The recipient email.
     * @param mobile     The recipient mobile number.
     */
    private void saveNotification(String requestId, String message, String templateId, String businessName, Locale
            lang, String email, String mobile) {
        Notification notification = new Notification();
        notification.setRequestId(requestId);
        notification.setMessage(message);
        notification.setTemplateId(templateId);
        notification.setLanguage(lang.getLanguage());
        notification.setBusinessName(businessName);
        notification.setEmail(email);
        notification.setMobile(mobile);
        notificationRepository.save(notification);
    }

    private String readHtmlFromFile(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

}
