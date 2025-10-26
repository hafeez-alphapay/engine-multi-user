package com.alphapay.payEngine.notification.services;

import com.alphapay.payEngine.storage.model.MerchantDocuments;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Locale;

public interface INotificationService {
    void sendEmailNotification(String requestId, String messageTemplateId, String[] keys, String email, String businessName, Locale lang, String applicationId, String emailTemplateId);
    void sendEmailNotification(String requestId, String messageTemplateId, String[] keys, String email,String businessName,   Locale lang, String applicationId,String title,  byte[] merchantDocuments);

    @Async
    void sendMobileNotification(String requestId, String messageTemplateId, String[] keys, String mobile,String businessName,  Locale lang, String applicationId, boolean isWhatsapp);

    @Async
    void sendMobileNotification(String requestId, String messageTemplateId, String[] keys, String mobile,String businessName,  Locale lang, String applicationId);

    String getLocalizedMessage(String messageTemplateId, String[] keys, Locale lang);
}
