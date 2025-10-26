package com.alphapay.payEngine.integration.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HttpFile {
    private String fileName;
    private String mediaType;
    private String buffer;
}
