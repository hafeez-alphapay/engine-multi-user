package com.alphapay.payEngine.notification.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@Table(name = "notification_config")
public class NotificationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    @Column(name = "application_id", nullable = false)
    private String applicationId;

    @Column(name = "smtp_subject")
    private String smtpSubject;

    @Column(name = "smtp_hostname")
    private String smtpHostname;

    @Column(name = "smtp_mail")
    private String smtpMail;

    @Column(name = "smtp_port")
    private Integer smtpPort;

    @Column(name = "smtp_username")
    private String smtpUserName;

    @Column(name = "smtp_password")
    private String smtpPassword;

    @Column(name = "smtp_auth")
    private Boolean smtpAuth;

    @Column(name = "smtp_enable_starttls")
    private Boolean smtpEnableStarttls;

    @Column(name = "sms_gateway_url")
    private String smsGatewayURL;

    @Column(name = "sms_gateway_username")
    private String smsGatewayUsername;

    @Column(name = "sms_gateway_password")
    private String smsGatewayPassword;

    @Column(name = "email_header")
    private String emailHeader;

    @Column(name = "last_updated")
    private Date lastUpdated;

    @Column(name = "creation_time")
    private Date creationTime;

    @Column(name = "smscRequestMapper", length = 1000)
    private String smscRequestMapper;

    @Column(name = "mail_html_template_file")
    private String mailHtmlTemplateFile;

    @Column(name = "email_cc_list")
    private String emailCcList;

    @Column(name = "email_to_list")
    private String emailToList;

    @Column(name = "fire_base_service_account_path")
    private String fireBaseServiceAccountPath;

    //TODO compare this approach with spring jpa auditing; ZonedDateTime
    @PrePersist
    public void prePersist() {
        this.creationTime = new Date();
        this.lastUpdated = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = new Date();
    }
}
