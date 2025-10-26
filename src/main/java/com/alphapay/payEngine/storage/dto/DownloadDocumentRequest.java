package com.alphapay.payEngine.storage.dto;

import com.alphapay.payEngine.service.bean.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DownloadDocumentRequest extends BaseRequest {
    @NotBlank
    private String documentName;
}
