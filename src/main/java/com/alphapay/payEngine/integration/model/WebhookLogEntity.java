package com.alphapay.payEngine.integration.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64)
    private String invoiceId;

    @Column(length = 64)
    private String paymentId;

    private String webhookUrl;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String requestPayload;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String responseBody;

    private int responseCode;

    private LocalDateTime timestamp;

    private String status; // e.g. SUCCESS, FAILED

    private String signature;
    @Column(name="flat_payload_prior_sign" , columnDefinition = "TEXT")
    private String flatPayloadPriorSign;
}
