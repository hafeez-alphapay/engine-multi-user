package com.alphapay.payEngine.account.management.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "merchant_comments")
@Getter
@Setter
public class MerchantComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "comment_by", nullable = false)
    private String commentBy;

    @Column(name = "type_of_comment", nullable = false)
    private String typeOfComment;

    @Column(name = "comment", nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
