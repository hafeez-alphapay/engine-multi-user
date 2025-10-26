package com.alphapay.payEngine.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Getter
@Setter
@AllArgsConstructor
public class EmailSenderConfiguration {
   private JavaMailSenderImpl javaMailSender;
   private String senderEmail;
   private String emailSubject;
   private String template;
   private String emailCcList;
   private String emailToList;
}
