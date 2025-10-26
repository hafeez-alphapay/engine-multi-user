package com.alphapay.payEngine.storage.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DocumentDescription {
    private String description;
    private List<RequiredDocumentDto> documents;
}