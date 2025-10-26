package com.alphapay.payEngine.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class RequiredDocumentDto {
    private Long documentCategoryId;
    private String docName;
    private String allowedType;
    private String allowedSize;
    private boolean isRequired;
    private String uploadedDocName;
}
