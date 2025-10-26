package com.alphapay.payEngine.account.customerKyc.serviceImpl;

import com.alphapay.payEngine.account.customerKyc.dto.response.CustomerIdDataResponse;
import com.alphapay.payEngine.account.customerKyc.dto.response.CustomerIdVerificationResponse;
import com.alphapay.payEngine.account.customerKyc.dto.response.MrzData;
import com.alphapay.payEngine.account.customerKyc.model.VerifiedCustomerKycEntity;
import com.alphapay.payEngine.account.customerKyc.repository.VerifiedCustomerKycRepository;
import com.alphapay.payEngine.account.customerKyc.service.CustomerKycVerificationService;
import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import com.alphapay.payEngine.alphaServices.repository.PaymentLinkEntityRepository;
import com.alphapay.payEngine.integration.exception.InvoiceLinkExpiredOrNotFoundException;
import com.alphapay.payEngine.storage.service.DocumentService;
import com.alphapay.payEngine.utilities.BeanUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Optional;

@Service
public class CustomerKycVerificationServiceImpl implements CustomerKycVerificationService {

    private final RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private VerifiedCustomerKycRepository verifiedCustomerKycRepository;
    @Autowired
    private PaymentLinkEntityRepository paymentLinkEntityRepository;
    @Autowired
    private DocumentService documentService;
    /**
     * Verifies the customer's identification document by processing the provided file.
     *
     * @param idType         The type of identification document (e.g., passport, national ID, driver license).
     * @param invoiceId      The invoice ID associated with the customer's verification request.
     * @param file           The uploaded image or PDF file of the identification document.
     * @param requestId      A unique identifier for the current verification request, used for tracking and logging.
     * @param acceptLanguage The preferred language for the response (e.g., "en", "ar").
     * @return A response object indicating the verification status of the customer ID.
     */

    @Override
    public CustomerIdVerificationResponse verifyCustomerIdDocument(String idType, String invoiceId, MultipartFile file, String requestId, String acceptLanguage) {
        Optional<PaymentLinkEntity> paymentLinkEntity = paymentLinkEntityRepository.findByInvoiceId(invoiceId);
        if (paymentLinkEntity.isEmpty()) {
            throw new InvoiceLinkExpiredOrNotFoundException();
        }

        String url = "http://13.53.62.167/scanner/passport/mrz";

        try {
            Resource imageResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("passport_image", imageResource);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.add("Api-Key", "e9hj3Sdf82hdj29Ujd29dkdjf82hsjdK");
            headers.add("Api-Secret", "JKjs73Kdjf78shdkf739SDFjkdf92hf9HFks82jsfkdhsfjd93hsdkfj82HF");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<CustomerIdDataResponse> response = restTemplate.postForEntity(url, requestEntity, CustomerIdDataResponse.class);

            VerifiedCustomerKycEntity entity = new VerifiedCustomerKycEntity();

            entity.setInvoiceId(invoiceId);
            entity.setPassportNumber(Objects.requireNonNull(response.getBody()).getPassport().getNumber());
            entity.setPassportImageUrl(response.getBody().getPassport().getImage());

            MrzData mrz = response.getBody().getMrz_data();

            entity.setValidScore(mrz.getValid_score());
            entity.setDocumentType(mrz.getType());
            entity.setIssuingCountry(mrz.getCountry());
            entity.setDocumentNumber(mrz.getNumber());
            entity.setDateOfBirth(mrz.getDate_of_birth());
            entity.setExpirationDate(mrz.getExpiration_date());
            entity.setNationality(mrz.getNationality());
            entity.setSex(mrz.getSex());
            entity.setNames(mrz.getNames());
            entity.setSurname(mrz.getSurname());
            entity.setIsValidNumber(mrz.isValid_number());
            entity.setValidDateOfBirth(mrz.isValid_date_of_birth());
            entity.setValidExpirationDate(mrz.isValid_expiration_date());
            entity.setValidComposite(mrz.isValid_composite());
            entity.setValidPersonalNumber(mrz.isValid_personal_number());
            entity.setUuid(response.getBody().getUuid());

            verifiedCustomerKycRepository.save(entity);

            documentService.uploadCustomerDoc(idType,invoiceId, file, requestId);

            CustomerIdVerificationResponse verificationResponse = new CustomerIdVerificationResponse();
            verificationResponse.setStatus("SUCCESS");
            BeanUtility.copyNonNullProperties((response.getBody()).getMrz_data(), verificationResponse);

            return verificationResponse;

        } catch (Exception e) {
            CustomerIdVerificationResponse errorResponse = new CustomerIdVerificationResponse();
            errorResponse.setStatus("FAILED");
            errorResponse.setResponseCode(1);
            errorResponse.setResponseMessage("Error processing image: " + e.getMessage());
            return errorResponse;
        }
    }
}
