package com.alphapay.payEngine.storage.model;

import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.merchantKyc.model.MerchantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "merchant_documents")
public class MerchantDocuments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", length = 255)
    private String requestId;

    @Column(name = "document_location", length = 255)
    private String documentLocation;

    @Column(name = "document_name", length = 255)
    private String documentName;

    @Column(name = "document_type", length = 255)
    private String documentType;

    @Column(name = "require_document_category_id", length = 255)
    private Long documentCategoryId;

    @Column(name = "uploaded_on")
    private Date uploadedOn;

    @ManyToOne
    @JoinColumn(name = "merchant_user_account", nullable = false, foreignKey = @ForeignKey(name = "FKmerchant_documentsn4rbpw759iuri"))
    private MerchantEntity merchantUserAccount;


    @ManyToOne
    @JoinColumn(name = "uploaded_by", nullable = false, foreignKey = @ForeignKey(name = "FKupload_byn4rbpw759iuri"))
    private UserEntity uploadedBy;

    public MerchantDocuments(String documentLocation, String documentName, String documentType, Long documentCategoryId, Date uploadedOn, MerchantEntity merchantUserAccount, UserEntity uploadedBy) {
        this.documentLocation = documentLocation;
        this.documentName = documentName;
        this.documentType = documentType;
        this.documentCategoryId = documentCategoryId;
        this.uploadedOn = uploadedOn;
        this.uploadedBy = uploadedBy;
        this.merchantUserAccount = merchantUserAccount;
    }
}