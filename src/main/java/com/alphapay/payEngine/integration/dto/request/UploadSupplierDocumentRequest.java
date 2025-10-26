package com.alphapay.payEngine.integration.dto.request;

import com.alphapay.payEngine.model.response.BaseResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadSupplierDocumentRequest extends BaseResponse {
    private Long merchantId;
}