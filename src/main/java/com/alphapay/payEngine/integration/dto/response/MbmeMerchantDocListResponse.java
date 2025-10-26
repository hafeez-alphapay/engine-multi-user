package com.alphapay.payEngine.integration.dto.response;

import com.alphapay.payEngine.model.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MbmeMerchantDocListResponse extends BaseResponse {
    private List<Document> documents;

    @Getter
    @Setter
    public static class Document {
        private String documentType;
        private String documentTypeName;
        private boolean mandatory;
        private String status;
        private Boolean mergeMultipleFiles;
        private String message;
    }
}
