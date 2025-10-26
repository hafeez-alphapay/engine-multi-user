package com.alphapay.payEngine.storage.serviceImpl;

import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.management.service.BaseUserService;
import com.alphapay.payEngine.account.management.service.MerchantService;
import com.alphapay.payEngine.account.merchantKyc.model.MerchantEntity;
import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import com.alphapay.payEngine.alphaServices.repository.PaymentLinkEntityRepository;
import com.alphapay.payEngine.integration.exception.InvoiceLinkExpiredOrNotFoundException;
import com.alphapay.payEngine.storage.dto.DocumentDescription;
import com.alphapay.payEngine.storage.dto.MerchantDocumentResponse;
import com.alphapay.payEngine.storage.dto.RequiredDocumentDto;
import com.alphapay.payEngine.storage.model.MerchantDocuments;
import com.alphapay.payEngine.storage.model.RequiredDocumentsCategory;
import com.alphapay.payEngine.storage.repository.MerchantDocumentsRepository;
import com.alphapay.payEngine.storage.repository.RequiredDocumentsRepository;
import com.alphapay.payEngine.storage.service.DocumentService;
import com.alphapay.payEngine.utilities.MessageService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DocumentServiceImpl implements DocumentService {
    @Autowired
    private RequiredDocumentsRepository requiredDocumentsRepository;

    @Autowired
    private BaseUserService userService;

    @Autowired
    private RemoteFileSystemStorageService storageService;

    @Autowired
    private MerchantDocumentsRepository merchantDocumentsRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private PaymentLinkEntityRepository paymentLinkEntityRepository;

    @Autowired
    private MerchantService merchantService;
    @Override
    public List<DocumentDescription> getActiveRequireDocuments() {
        List<String> uniqueDescriptions = requiredDocumentsRepository.findDistinctDescriptions();
        List<DocumentDescription> documentDescriptions = new ArrayList<>();
        for (String description : uniqueDescriptions) {
            List<RequiredDocumentsCategory> requiredDocs = requiredDocumentsRepository.findByStatusAndDescription("Active", description);
            List<RequiredDocumentDto> documentDtos = new ArrayList<>();
            for (RequiredDocumentsCategory doc : requiredDocs) {
                documentDtos.add(new RequiredDocumentDto(doc.getId(), doc.getDocName(), doc.getAllowedType(), doc.getAllowedSize(), doc.getIsRequired(), ""));
            }
            DocumentDescription documentDescription = new DocumentDescription();
            documentDescription.setDescription(description);
            documentDescription.setDocuments(documentDtos);
            documentDescriptions.add(documentDescription);
        }

        return documentDescriptions;
    }

    @Override
    @Transactional
    public MerchantDocumentResponse uploadDocument(Long documentCategoryId, Long merchantId, MultipartFile file, String requestId, Long userId) throws Exception {
        MerchantEntity merchant = merchantService.getMerchant(merchantId);
        UserEntity user = userService.getLoggedUser(userId);
        RequiredDocumentsCategory requiredDocumentsCategory = requiredDocumentsRepository.findById(documentCategoryId);

        MerchantDocuments merchantDocuments = storageService.store(file, merchant, user, requiredDocumentsCategory.getDocName().replace(" ", ""), documentCategoryId);
        Optional<MerchantDocuments> uploadedDoc = merchantDocumentsRepository.findByDocumentCategoryIdAndMerchantUserAccount(documentCategoryId, merchant);
        merchantDocuments.setRequestId(requestId);

        if (uploadedDoc.isPresent()) {
            MerchantDocuments updatedDoc = uploadedDoc.get();
            updatedDoc.setDocumentName(merchantDocuments.getDocumentName());
            updatedDoc.setDocumentType(merchantDocuments.getDocumentType());
            updatedDoc.setDocumentLocation(merchantDocuments.getDocumentLocation());
            updatedDoc.setUploadedOn(merchantDocuments.getUploadedOn());
            updatedDoc.setUploadedBy(merchantDocuments.getUploadedBy());
            updatedDoc.setDocumentCategoryId(merchantDocuments.getDocumentCategoryId());
        } else {
            merchantDocumentsRepository.save(merchantDocuments);
        }

        if (merchantDocuments.getDocumentCategoryId() == 25) {
            merchant.setLogo(merchantDocuments.getDocumentLocation());
        }

        return new MerchantDocumentResponse(merchantDocuments.getId(), merchantDocuments.getDocumentLocation(), merchantDocuments.getDocumentName(), merchantDocuments.getDocumentType(), merchantDocuments.getUploadedOn(), merchantDocuments.getMerchantUserAccount().getLegalName(), merchantDocuments.getUploadedBy().getUserDetails().getFullName(), merchantDocuments.getMerchantUserAccount().getId(), merchantDocuments.getDocumentCategoryId());
    }

    @Override
    public List<MerchantDocumentResponse> findMerchantDocByMerchantId(Long merchantId) {
        return mapToDTO(merchantDocumentsRepository.findByMerchantUserAccount(merchantService.getMerchant(merchantId)));
    }

    /**
     * @param invoiceId
     * @param file
     * @param requestId
     * @return
     */
    @Override
    public PaymentLinkEntity uploadCustomerDoc(String documentType, String invoiceId, MultipartFile file, String requestId) throws Exception {
        Optional<PaymentLinkEntity> paymentLinkEntity = paymentLinkEntityRepository.findByInvoiceId(invoiceId);
        if (paymentLinkEntity.isEmpty()) {
            throw new InvoiceLinkExpiredOrNotFoundException();
        }
        String fileUrl = storageService.storeCustomerDoc(documentType, file, invoiceId);
        if (documentType.equals("Signature"))
            paymentLinkEntity.get().setSignatureUrl(fileUrl);
        else
            paymentLinkEntity.get().setCustomerKycDocument(fileUrl);

        paymentLinkEntityRepository.save(paymentLinkEntity.get());
        return paymentLinkEntity.get();
    }

    public List<MerchantDocumentResponse> mapToDTO(List<MerchantDocuments> merchantDocuments) {
        return merchantDocuments.stream().map(doc -> new MerchantDocumentResponse(doc.getId(), doc.getDocumentLocation(), doc.getDocumentName(), doc.getDocumentType(), doc.getUploadedOn(), doc.getMerchantUserAccount().getLegalName(), doc.getUploadedBy().getUserDetails().getFullName(), doc.getMerchantUserAccount().getId(), doc.getDocumentCategoryId())).collect(Collectors.toList());
    }

}
