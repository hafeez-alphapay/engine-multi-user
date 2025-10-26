package com.alphapay.payEngine.integration.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogoFile {
    private String fileName;
    private String mediaType;
    private String buffer;
}
