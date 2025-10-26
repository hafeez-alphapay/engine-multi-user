package com.alphapay.payEngine.integration.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "back_end_response_code_mapping")
public class BackEndResponseCodeMapping implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_response_code", length = 255)
    private String appResponseCode;

    @Column(name = "app_response_message", length = 255)
    private String appResponseMessage;

    @Column(name = "app_response_message_ar", length = 255)
    private String appResponseMessageAr;

    @Column(name = "external_response_code", length = 255)
    private String externalResponseCode;

    @ManyToOne
    @JoinColumn(name = "category", nullable = false)
    private BackEndResponseCodeCategory category;


    @Override
    public String toString() {
        return "BackEndResponseCodeMapping{" +
                "id=" + id +
                ", appResponseCode='" + appResponseCode + '\'' +
                ", appResponseMessage='" + appResponseMessage + '\'' +
                ", appResponseMessageAr='" + appResponseMessageAr + '\'' +
                ", externalResponseCode='" + externalResponseCode + '\'' +
                ", category=" + category +
                '}';
    }
}