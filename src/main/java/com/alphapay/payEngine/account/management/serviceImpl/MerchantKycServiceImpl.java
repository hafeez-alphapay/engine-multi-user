package com.alphapay.payEngine.account.management.serviceImpl;

import com.alphapay.payEngine.account.management.dto.request.*;
import com.alphapay.payEngine.account.management.dto.response.*;
import com.alphapay.payEngine.account.management.exception.AccountNotFoundException;
import com.alphapay.payEngine.account.management.exception.MessageResolverService;
import com.alphapay.payEngine.account.management.model.MerchantComment;
import com.alphapay.payEngine.account.management.model.MerchantProviders;
import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.management.repository.MerchantCommentRepository;
import com.alphapay.payEngine.account.management.repository.UserRepository;
import com.alphapay.payEngine.account.management.service.BaseUserService;
import com.alphapay.payEngine.account.management.service.MerchantKycService;
import com.alphapay.payEngine.account.management.service.MerchantMapperService;
import com.alphapay.payEngine.account.management.service.MerchantService;
import com.alphapay.payEngine.account.merchantKyc.model.MerchantEntity;
import com.alphapay.payEngine.account.merchantKyc.repository.MerchantRepository;
import com.alphapay.payEngine.account.roles.exception.UserNotFoundException;
import com.alphapay.payEngine.alphaServices.model.MerchantAlphaPayServicesEntity;
import com.alphapay.payEngine.alphaServices.model.MerchantServiceConfigEntity;
import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import com.alphapay.payEngine.alphaServices.service.MerchantAlphaPayServicesService;
import com.alphapay.payEngine.integration.model.orchast.ServiceProvider;
import com.alphapay.payEngine.integration.repository.MerchantProviderRegistrationRepository;
import com.alphapay.payEngine.integration.repository.ServiceProviderRepository;
import com.alphapay.payEngine.storage.dto.DocumentDescription;
import com.alphapay.payEngine.storage.dto.MerchantDocumentResponse;
import com.alphapay.payEngine.storage.dto.RequiredDocumentDto;
import com.alphapay.payEngine.storage.service.DocumentService;
import com.alphapay.payEngine.utilities.MessageService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MerchantKycServiceImpl implements MerchantKycService {

    @Autowired
    private BaseUserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MerchantMapperService merchantMapper;

    @Autowired
    private MessageResolverService resolverService;

    @Autowired
    private MerchantCommentRepository merchantCommentRepository;

    @Autowired
    private MerchantAlphaPayServicesService merchantAlphaPayServicesService;

    @Autowired
    private MerchantProviderRegistrationRepository merchantProviderRegistrationRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private MerchantService merchantService;

    @Override
    public PaginatedMerchantResponse<MerchantResponse> getAllMerchants(GetAllUsersRequestFilter request) {
        Page<MerchantEntity> merchantEntities = null;
        if (request.getIsAdmin()) {
            merchantEntities =  merchantService.getAllMerchants(request);
        }else {
            request.setParentId(request.getAuditInfo().getUserId());
            merchantEntities = merchantService.getMultiVendorUsers(request);
        }

        Page<MerchantResponse> userPage = merchantEntities.map(merchantMapper::convertToDto);

        List<MerchantResponse> enrichedMerchants = userPage.getContent().stream().toList();
        log.debug("Enriched merchants: {}", enrichedMerchants);

        long countActiveUsersLast30Days;
        long countApprovedMbme;
        long countApprovedMyfattora;

        if (request.getIsAdmin()) {
            countActiveUsersLast30Days = 0L;// merchantRepository.countActiveUsersSince(LocalDateTime.now().minusDays(30));
            countApprovedMbme =  merchantRepository.countApprovedMbme();
            countApprovedMyfattora =  merchantRepository.countApprovedMyfattora();
        } else {
            MerchantEntity userEntity = merchantService.getMerchant(request.getParentId());
            countActiveUsersLast30Days = 0L;//merchantRepository.countActiveVendorUsersSince(LocalDateTime.now().minusDays(30), userEntity);
            countApprovedMbme = merchantRepository.countVendorApprovedMbme(userEntity);
            countApprovedMyfattora = merchantRepository.countVendorApprovedMyfattora(userEntity);
        }

        return new PaginatedMerchantResponse<>(
                enrichedMerchants,
                userPage.getNumber() + 1, // Convert zero-based page index to one-based
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isLast(),

                countActiveUsersLast30Days,
                countApprovedMbme,
                countApprovedMyfattora
        );
    }


    @Override
    public MerchantDocumentResponse uploadDocument(Long documentCategoryId, Long merchantId, MultipartFile file, String requestId, String acceptLanguage, Long userId) throws Exception {
        return documentService.uploadDocument(documentCategoryId, merchantId, file, requestId, userId);
    }

    @Override
    public PaymentLinkEntity uploadSignDocument(String invoiceId, MultipartFile file, String requestId) throws Exception {
        String documentType = "Signature";
        return documentService.uploadCustomerDoc(documentType, invoiceId, file, requestId);
    }

    @Override
    @Transactional
    public MerchantStatusResponse changeMerchantAccountStatus(MerchantAccountStatusRequest request) {
        Optional<MerchantEntity> merchant = merchantRepository.findById(request.getMerchantId());
        if (merchant.isEmpty()) {
            throw new AccountNotFoundException();
        }

        if (request.getRole().toLowerCase().contains("manager")) {
            if (request.getNewStatus().equals("ACTIVE") || request.getNewStatus().equals("INACTIVE")) { //change merchant account status to active by manager role
                merchant.get().setStatus(request.getNewStatus());
            } else { //change status of merchant data approve or declined by manager role
                merchant.get().setManagerApproveStatus(request.getNewStatus());
            }
        } else if (request.getRole().toLowerCase().contains("admin") ||
                request.getRole().toLowerCase().contains("sales")) {
            merchant.get().setAdminApproveStatus(request.getNewStatus()); //merchant data approve review by admin role
        }
        Map<String, String> data = new HashMap<>();
        data.put("merchantId", String.valueOf(request.getMerchantId()));
        data.put("accountStatus", request.getNewStatus());
        MerchantStatusResponse response = new MerchantStatusResponse();
        response.setStatus("Success");
        response.setResponseCode(200);
        response.setResponseMessage("Merchant Account Status has been changed Successfully");
        response.setData(data);
        return response;
    }

    @Override
    public MerchantDetailsResponse getMerchantDetails(MerchantDetailsRequest request) {

        MerchantEntity merchantEntity = merchantService.getMerchant(request.getMerchantId());
        MerchantResponse merchantResponse = merchantMapper.convertToDto(merchantEntity);
        MerchantDetailsResponse response = new MerchantDetailsResponse();
        BeanUtils.copyProperties(request, response);
        BeanUtils.copyProperties(merchantResponse, response);
        resolverService.setAsSuccess(response);
        return response;
    }

    @Override
    public MerchantDetailsResponse getMerchantFullDetails(MerchantDetailsRequest request) {
        log.debug("MerchantDetailsRequest------>{}", request);
        if (request.getIsVendor()) {
            List<Long> subsMerchants = new ArrayList<>();
            MerchantEntity superMerchant = merchantRepository.findById(request.getVendorMerchantId()).orElse(null);
            if (superMerchant != null && superMerchant.getSubMerchants() != null) {
                subsMerchants = superMerchant.getSubMerchants().stream()
                        .map(MerchantEntity::getId)
                        .toList();
            }
            if (!subsMerchants.contains(request.getMerchantId()) &&
                    !request.getVendorMerchantId().equals(request.getMerchantId())) {
                throw new UserNotFoundException();
            }
        }
        MerchantResponse merchantResponse = getMerchantDetails(request);
        //get merchant all services
        List<MerchantAlphaPayServicesEntity> merchantAlphaPayServicesEntityList = merchantAlphaPayServicesService.getMerchantServices(request.getMerchantId());

        List<DocumentDescription> requiredDocuments = documentService.getActiveRequireDocuments();

        List<MerchantDocumentResponse> merchantDocuments = documentService.findMerchantDocByMerchantId(request.getMerchantId()).stream()
                .filter(doc -> doc.getMerchantId().equals(request.getMerchantId()))
                .toList();

        for (DocumentDescription documentDescription : requiredDocuments) {
            List<RequiredDocumentDto> requiredDocs = documentDescription.getDocuments();

            // Map uploadedDocName in RequiredDocumentDto
            for (RequiredDocumentDto requiredDoc : requiredDocs) {
                // Find matching MerchantDocumentResponse by documentCategoryId
                Optional<MerchantDocumentResponse> matchingMerchantDoc = merchantDocuments.stream()
                        .filter(md -> md.getDocumentCategoryId().equals(requiredDoc.getDocumentCategoryId()))
                        .findFirst();

                // Set the uploadedDocName if a match is found
                matchingMerchantDoc.ifPresent(merchantDoc -> requiredDoc.setUploadedDocName(merchantDoc.getDocumentName()));
            }
        }
        //convert merchant service list to dto
        List<MerchantServiceRequest> merchantServices = merchantAlphaPayServicesEntityList.stream()
                .map(entity -> {
                    MerchantServiceRequest serviceRequest = new MerchantServiceRequest();
                    serviceRequest.setServiceId(entity.getAlphaPayService().getServiceId());
                    serviceRequest.setStatus(entity.getStatus());
                    return serviceRequest;
                })
                .collect(Collectors.toList());

        MerchantDetailsResponse response = new MerchantDetailsResponse();
        BeanUtils.copyProperties(request, response);
        BeanUtils.copyProperties(merchantResponse, response);
        response.setDocumentsCategory(requiredDocuments);
        response.setMerchantServices(merchantServices);
        MerchantServiceConfigEntity serviceConfigEntity = merchantAlphaPayServicesService.getConfigEntityByMerchantId(request.getMerchantId());
        if (serviceConfigEntity != null) {
            response.setMerchantConfig(new MerchantServiceConfigDTO(serviceConfigEntity));
        }
        resolverService.setAsSuccess(response);
        return response;
    }


    @Override
    public List<MerchantProviders> getMerchantProvider(MerchantDetailsRequest request) {
        ServiceProvider serviceProvider = serviceProviderRepository.findById(request.getProviderId()).get();
        return  merchantProviderRegistrationRepository.findUsersAndRegistrationsByServiceProviderId(request.getProviderId());
    }

    @Override
    public List<VendorMerchants> getParentMerchantWithSubMerchant(@Valid MerchantDetailsRequest request) {
        List<MerchantEntity> vendorMerchants = new ArrayList<>();

        if (request.getMerchantId() == null) {
            vendorMerchants = merchantRepository.findAllWithParentMerchant();
        } else {
            vendorMerchants = (List<MerchantEntity>) merchantRepository.findById(request.getMerchantId()).get().getSubMerchants();
        }

        List<VendorMerchants> result = vendorMerchants.stream().map(vendor -> {
            VendorMerchants dto = new VendorMerchants();
            dto.setId(vendor.getId());
            dto.setTradeNameEnglish(vendor.getTradeNameEn());
            if (vendor.getSubMerchants() != null && !vendor.getSubMerchants().isEmpty()) {
                List<SubMerchant> subMerchantList = vendor.getSubMerchants()
                        .stream()
                        .map(sub -> {
                            SubMerchant sm = new SubMerchant();
                            sm.setId(sub.getId());
                            sm.setTradeNameEnglish(sub.getTradeNameEn());
                            return sm;
                        })
                        .collect(Collectors.toList());
                dto.setSubMerchants(subMerchantList);
            }
            return dto;
        }).collect(Collectors.toList());

        return result;
    }

    @Override
    @Transactional
    public void assignMerchant(AssignMerchantRequest request) {
        log.debug("assignMerchant::{}", request);
        Optional<UserEntity> userEntity = userRepository.findById(request.getMerchantId());
//        userEntity.ifPresent(user -> user.setAssignTo(request.getSelectedId()));
        log.debug("assignMerchant::{}", userEntity.get());
    }

    @Override
    public List<MerchantComment> addCommentMerchant(AddCommentMerchantRequest request) {
        MerchantComment comment = new MerchantComment();
        comment.setMerchantId(request.getMerchantId());
        comment.setCommentBy(request.getCommentBy());
        comment.setTypeOfComment(request.getTypeOfComment());
        comment.setComment(request.getComment());
        merchantCommentRepository.save(comment);

        return merchantCommentRepository.findByMerchantId(request.getMerchantId());
    }

    @Override
    public MerchantCommentResponse AllCommentMerchant(AddCommentMerchantRequest request) {
        List<MerchantComment> comments = merchantCommentRepository.findByMerchantId(request.getMerchantId());
        MerchantCommentResponse response = new MerchantCommentResponse();
        response.setComments(comments);
        return response;
    }

    @Override
    public void assignMerchantAggreagtorId(MerchantAggregatorLinkRequest request) {
        Optional<MerchantEntity> userEntity = merchantRepository.findById(request.getMerchantId());
        userEntity.ifPresent(user -> {
            user.setBillerClientId(request.getAggregatorId());
            user.setBillerClientStatus("ACTIVE");
        });
        log.debug("assignMerchant to aggregator::{}", userEntity.get());

    }


    @Override
    public void updateClientAggregatorStatus(MerchantAggregatorLinkRequest request) {
        Optional<MerchantEntity> userEntity = merchantRepository.findByBillerClientId(request.getAggregatorId());
        userEntity.ifPresent(user -> user.setBillerClientStatus(request.getStatus()));
        log.debug("assignMerchant to aggregator::{}", userEntity.get());
    }
}