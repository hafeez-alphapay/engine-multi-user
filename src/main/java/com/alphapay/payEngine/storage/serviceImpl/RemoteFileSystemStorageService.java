package com.alphapay.payEngine.storage.serviceImpl;

import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.merchantKyc.model.MerchantEntity;
import com.alphapay.payEngine.storage.model.MerchantDocuments;
import com.alphapay.payEngine.storage.service.S3Service;
import com.alphapay.payEngine.storage.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Service
public class RemoteFileSystemStorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(RemoteFileSystemStorageService.class);

    @Value("${sftp.config.port}")
    int sftpConfigPort;

    @Value("${sftp.config.host}")
    String sftpConfigHost;

    @Value("${sftp.config.user}")
    String sftpConfigUserName;

    @Value("${sftp.config.password}")
    String sftpConfigPassword;

    @Value("${sftp.config.timeout}")
    int sftpConfigTimeout;

    @Value("${sftp.config.dir}")
    String sftpConfigDir;

    @Autowired
    private S3Service s3Service;


    @Override
    public MerchantDocuments store(MultipartFile file, MerchantEntity merchantUser, UserEntity uploadedBy, String type, Long documentCategoryId) throws Exception {
        logger.info("storing to s3 bucket ..");

        String keyName = merchantUser.getId().toString() + "-" +
                uploadedBy.getId().toString() + "-" +
                new Date().getTime() + "-" +
                type +
                file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")).toLowerCase();

        logger.debug("documentName= " + keyName);

        if (file.isEmpty()) {
            throw new Exception("failed to store empty file " + keyName);
        }

        // Security check to prevent path traversal attacks
        if (keyName.contains("..")) {
            throw new Exception("cannot store file with relative path outside current directory " + keyName);
        }
        String fileUrl;
        try {
            fileUrl = s3Service.uploadFile(file, keyName); // Pass the file and the updated keyName
        } catch (Exception e) {
            logger.error("Error uploading file to S3", e);
            throw new Exception("Error uploading file to S3: " + e.getMessage());
        }


        return new MerchantDocuments(
                fileUrl,                // Image location (can be updated if needed)
                keyName,                // The document name used for storage
                type,                   // Type of the document
                documentCategoryId,     // The document category ID
                new Date(),             // Current date for timestamp
                merchantUser,           // The user the document is associated with
                uploadedBy              // The user who uploaded the document
        );
    }


    @Override
    public String storeCustomerDoc(String documentType, MultipartFile file, String invoiceId) throws Exception {
        String keyName = documentType + "-" + invoiceId + "-" + new Date().getTime();
        if (file.isEmpty()) {
            throw new Exception("failed to store empty file " + keyName);
        }
        if (keyName.contains("..")) {
            throw new Exception("cannot store file with relative path outside current directory " + keyName);
        }
        String fileUrl;
        try {
            fileUrl = s3Service.uploadFile(file, keyName);
        } catch (Exception e) {
            logger.error("Error uploading file to S3", e);
            throw new Exception("Error uploading file to S3: " + e.getMessage());
        }

        return fileUrl;
    }

    public byte[] getFileFromS3(String keyName) throws Exception {
        return s3Service.downloadFileAsBytes(keyName);
    }

    public String getFileMediaType(String keyName) throws Exception {
        return s3Service.getFileMediaType(keyName);
    }
}
