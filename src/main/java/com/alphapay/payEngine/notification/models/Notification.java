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
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY,generator="native")
    @GenericGenerator(name = "native",strategy = "native")
    private Long id;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "sent_date")
    private Date sentDate;

    @Column(name = "message")
    private String message;

    @Column(name = "template_id")
    private String templateId;

    @Column(name = "lang")
    private String language;


    @Column(name = "email")
    private String email;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "application_id")
    private String applicationId;
    //TODO compare this approach with spring jpa auditing; ZonedDateTime
    @PrePersist
    public void prePersist() {
        this.sentDate = new Date();
    }

}