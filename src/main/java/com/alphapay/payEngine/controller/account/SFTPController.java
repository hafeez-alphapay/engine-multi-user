package com.alphapay.payEngine.controller.account;

import com.alphapay.payEngine.account.management.service.MerchantKycService;
import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import com.alphapay.payEngine.storage.dto.DocumentDescription;
import com.alphapay.payEngine.storage.dto.DownloadDocumentRequest;
import com.alphapay.payEngine.storage.dto.MerchantDocumentResponse;
import com.alphapay.payEngine.storage.service.DocumentService;
import com.alphapay.payEngine.storage.serviceImpl.RemoteFileSystemStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/documents")
public class SFTPController {

    @Autowired
    private RemoteFileSystemStorageService sftpService;

    @Autowired
    private MerchantKycService merchantKycService;

    @Autowired
    private DocumentService documentService;

    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@Valid @RequestBody DownloadDocumentRequest request) {
        try {
            byte[] fileData = sftpService.getFileFromS3(request.getDocumentName());

            return new ResponseEntity<>(fileData, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MerchantDocumentResponse uploadDocument(@RequestParam("requestId") String requestId,
                                                   @RequestParam("acceptLanguage") String acceptLanguage,
                                                   @RequestParam("merchantId") Long merchantId,
                                                   @RequestParam("userId") Long userId,
                                                   @RequestParam("documentCategoryId") Long documentCategoryId,
                                                   @RequestParam("file") MultipartFile file,
                                                   HttpServletRequest httpServletRequest) throws Exception {
        return merchantKycService.uploadDocument(documentCategoryId,merchantId, file, requestId, acceptLanguage,userId);
    }


    @PostMapping(value = "/uploadSignature", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PaymentLinkEntity uploadSignDocument(@RequestParam("requestId") String requestId,
                                                @RequestParam("invoiceId") String invoiceId,
                                                @RequestParam("file") MultipartFile file,
                                                HttpServletRequest httpServletRequest) throws Exception {
        return merchantKycService.uploadSignDocument(invoiceId, file, requestId);
    }

    @GetMapping("/requiredDocumentsCategory")
    public List<DocumentDescription> getRequiredDocuments() {
        return documentService.getActiveRequireDocuments();
    }
}
