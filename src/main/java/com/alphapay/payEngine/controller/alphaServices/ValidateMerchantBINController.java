package com.alphapay.payEngine.controller.alphaServices;

import com.alphapay.payEngine.account.management.exception.MessageResolverService;
import com.alphapay.payEngine.alphaServices.dto.request.BINInfoRequest;
import com.alphapay.payEngine.alphaServices.dto.request.ValidateMerchantBINRequest;
import com.alphapay.payEngine.alphaServices.dto.response.CustomerCardBINInfoResponse;
import com.alphapay.payEngine.alphaServices.dto.response.ValidateMerchantBINResponse;
import com.alphapay.payEngine.alphaServices.serviceImpl.BINServiceImpl;
import com.alphapay.payEngine.alphaServices.serviceImpl.MerchantCountryPermissionCheckerImpl;
import com.alphapay.payEngine.integration.exception.BINNotAllowedToMerchantException;
import com.alphapay.payEngine.integration.exception.CustomerCardBINInfoNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

/**
 * REST controller responsible for validating whether a given BIN is allowed for a merchant
 * based on the merchant's allowed country settings.
 * <p>
 * Endpoint: POST /validate/card/checkPIN
 */
@RestController
@RequestMapping("/validate/card")
public class ValidateMerchantBINController {

    private static final Logger logger = LoggerFactory.getLogger(ValidateMerchantBINController.class);

    @Autowired
    private MerchantCountryPermissionCheckerImpl merchantCountryPermissionChecker;

    @Autowired
    private BINServiceImpl binService;

    @Autowired
    private MessageResolverService messageResolverService;

    /**
     * Validates whether a given BIN is allowed for a specific merchant.
     *
     * @param request the request containing merchantId and BIN
     * @return a response indicating whether the BIN is allowed, along with localized messages
     */
    @PostMapping(value = "/checkPIN", produces = "application/json", consumes = "application/json")
    public ValidateMerchantBINResponse validateBIN(@Valid @RequestBody ValidateMerchantBINRequest request) {
        logger.info("Received BIN validation request for merchantId={} and BIN={}", request.getMerchantId(), request.getBin());

        boolean valid = merchantCountryPermissionChecker.validateBINCodeForMerchant(request.getBin(), request.getMerchantId());

        String message;
        String messageAr;

        if (valid) {
            message = "BIN is valid for the merchant";
            messageAr = "BIN صالح للتاجر";
            logger.info("BIN {} is valid for merchant {}", request.getBin(), request.getMerchantId());
        } else {
            BINNotAllowedToMerchantException ex = new BINNotAllowedToMerchantException();
            message = messageResolverService.resolveLocalizedErrorMessage(ex);
            messageAr = messageResolverService.resolveLocalizedErrorMessage(ex, new Locale("ar"));
            logger.warn("BIN {} is not allowed for merchant {}", request.getBin(), request.getMerchantId());
        }

        return new ValidateMerchantBINResponse(valid, message, messageAr);
    }

    /**
     * Retrieves BIN information including card type, scheme, and country.
     *
     * @param binRequest the BIN to look up
     * @return a BinData representing BIN details
     */
    @PostMapping(value = "/binInfo", produces = "application/json")
    public CustomerCardBINInfoResponse getBINInfo(@Valid @RequestBody BINInfoRequest binRequest) {
        CustomerCardBINInfoResponse binData = binService.getBinInfo(binRequest.getBin());
        if (binData == null) {
            throw new CustomerCardBINInfoNotFoundException();
        }
        messageResolverService.setAsSuccess(binData);
        return binData;
    }
}
