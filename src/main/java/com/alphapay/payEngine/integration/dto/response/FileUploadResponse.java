package com.alphapay.payEngine.integration.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileUploadResponse {
    private int status_code;

    private String status_message;

    private Result result;

    @Setter
    @Getter
    public class Result {
        private FileInfo file_info;
        private boolean success;

    }

    @Setter
    @Getter
    public class FileInfo {
        private long size;
        private String mimetype;
        private String extension;
        private String upload_at_utc;

    }
}