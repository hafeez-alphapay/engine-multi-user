package com.alphapay.payEngine.alphaServices.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "delivery_service_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DeliveryServiceInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "service_type", nullable = false, length = 50)
    private String serviceType;

    @Column(name = "coverage_area", nullable = false, length = 100)
    private String coverageArea;

    @Column(name = "contact_number", nullable = false, length = 20)
    private String contactNumber;

    @Column(name = "email_address", length = 100)
    private String emailAddress;

    @Column(name = "working_hours", nullable = false, length = 50)
    private String workingHours;

    @Column(name = "provider_api_url", nullable = false, length = 255)
    private String providerApiUrl;

    @Column(name = "provider_api_key", nullable = false, length = 255)
    private String providerApiKey;

    @Column(name = "tracking_url_template", length = 255)
    private String trackingUrlTemplate;

    @Column(name = "integration_status", nullable = false)
    private Boolean integrationStatus = true;

    @Column(name = "last_synced", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp lastSynced;

    @Column(name = "created_on", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdOn;

    @Column(name = "updated_on", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp updatedOn;
}