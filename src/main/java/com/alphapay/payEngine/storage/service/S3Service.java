package com.alphapay.payEngine.storage.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface S3Service {
    String uploadFile(MultipartFile file, String keyName)throws IOException;

    byte[] downloadFileAsBytes(String keyName) ;
    String getFileMediaType(String keyName) ;
}
