package com.alphapay.payEngine.integration.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class FileUploadRequest {
    private HttpFile fileUpload;
    private Integer fileType;
    private LocalDateTime expireDate;
    private Integer supplierCode;
}
