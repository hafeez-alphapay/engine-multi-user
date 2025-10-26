package com.alphapay.payEngine.storage.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
public class MerchantDocumentResponse {
    @JsonIgnore
    private Long id;
    @JsonIgnore
    private String documentLocation;
    private String documentName;
    private String documentType;
    private Date uploadedOn;
    private String merchantUserName;
    private String uploadedBy;
    @JsonIgnore
    private Long merchantId;
    private Long documentCategoryId;
}
