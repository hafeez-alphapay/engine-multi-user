package com.alphapay.payEngine.storage.serviceImpl;

import com.alphapay.payEngine.storage.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
public class S3ServiceImpl implements S3Service {
    @Value("${s3.document.bucketName}")
    private String bucketName;
    @Value("${aws.region}")
    private String REGION;
    @Value("${s3.aws.access-key-id}")
    private String ACCESS_KEY;
    @Value("${s3.aws.secret-access-key}")
    private String SECRET_KEY;

    public S3Client createS3Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY);
        return S3Client.builder()
                .region(Region.of(REGION))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    @Override
    public String uploadFile(MultipartFile file, String keyName) throws IOException {
        S3Client s3Client = createS3Client();
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));


        String fileUrl = "https://" + bucketName + ".s3." + Region.ME_CENTRAL_1.toString().toLowerCase() + ".amazonaws.com/" + keyName;

        log.trace("File uploaded successfully! File URL:{}", fileUrl);

        return fileUrl;
    }


    @Override
    public byte[] downloadFileAsBytes(String keyName) {
        S3Client s3Client =  createS3Client();

        InputStream inputStream = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error downloading file from S3", e);
        }
    }

    /**
     * @param keyName
     * @return
     */
    @Override
    public String getFileMediaType(String keyName) {
        S3Client s3Client = createS3Client();
        String mediaType;
        HeadObjectResponse headResponse = s3Client.headObject(HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build());

        mediaType = headResponse.contentType();
        return mediaType;
    }
}
