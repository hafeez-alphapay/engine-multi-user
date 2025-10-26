package com.alphapay.payEngine.alphaServices.serviceImpl;

import com.alphapay.payEngine.account.management.dto.request.MerchantServiceRequest;
import com.alphapay.payEngine.account.management.exception.MessageResolverService;
import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.management.repository.UserRepository;
import com.alphapay.payEngine.alphaServices.dto.request.GetServiceRequest;
import com.alphapay.payEngine.alphaServices.dto.response.MerchantStats;
import com.alphapay.payEngine.alphaServices.dto.response.ServiceResponse;
import com.alphapay.payEngine.alphaServices.model.AlphaPayServicesEntity;
import com.alphapay.payEngine.alphaServices.model.MerchantAlphaPayServicesEntity;
import com.alphapay.payEngine.alphaServices.model.MerchantServiceConfigEntity;
import com.alphapay.payEngine.alphaServices.repository.AlphaPayServicesRepository;
import com.alphapay.payEngine.alphaServices.repository.MerchantServiceConfigRepository;
import com.alphapay.payEngine.alphaServices.repository.MerchantServicesRepository;
import com.alphapay.payEngine.alphaServices.service.MerchantAlphaPayServicesService;
import com.alphapay.payEngine.integration.dto.request.paymentGatewayIntegration.*;
import com.alphapay.payEngine.integration.dto.response.paymentGatewayIntegration.MerchantGatewayConfigurationResponse;
import com.alphapay.payEngine.integration.exception.InvalidAPIKeyException;
import com.alphapay.payEngine.integration.exception.ServiceNotAllowedToMerchantException;
import com.alphapay.payEngine.utilities.BeanUtility;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MerchantAlphaPayServicesServiceImpl implements MerchantAlphaPayServicesService {

    @Autowired
    private MerchantServicesRepository merchantServicesRepository;

    @Autowired
    private AlphaPayServicesRepository alphaPayServicesRepository;

    @Autowired
    private MerchantServiceConfigRepository merchantServiceConfigRepository;

    @Autowired
    private MessageResolverService resolverService;

    @Autowired
    private UserRepository merchantRepo;

    @Override
    public List<ServiceResponse> getAllServices(GetServiceRequest request) {
        List<AlphaPayServicesEntity> servicesEntities = alphaPayServicesRepository.findAll();
        List<ServiceResponse> serviceResponses = new ArrayList<>();
        for (AlphaPayServicesEntity alphaPayServicesEntity : servicesEntities) {
            ServiceResponse serviceResponse = new ServiceResponse();
            BeanUtils.copyProperties(alphaPayServicesEntity, serviceResponse);
            serviceResponses.add(serviceResponse);
        }
        return serviceResponses;
    }

    @Override
    public List<MerchantAlphaPayServicesEntity> getMerchantServices(Long merchantId) {
        return merchantServicesRepository.findByMerchantId(merchantId);
    }

    @Override
    public List<MerchantAlphaPayServicesEntity> getMerchantActiveServices(Long merchantId) {
        return merchantServicesRepository.findByMerchantIdAndStatus(merchantId, "Active");
    }

    @Override
    public MerchantAlphaPayServicesEntity checkMerchantService(Long merchantId, String serviceId) {
        Optional<AlphaPayServicesEntity> serviceEntity = alphaPayServicesRepository.findByServiceId(serviceId);
        MerchantAlphaPayServicesEntity merchantAlphaPayServices = merchantServicesRepository.findByMerchantIdAndAlphaPayServiceAndStatus(merchantId, serviceEntity.get(), "Active");
        if (merchantAlphaPayServices == null) {
            throw new ServiceNotAllowedToMerchantException();
        }
        return merchantAlphaPayServices;
    }

    @Override
    public void addMerchantService(Long merchantId, List<MerchantServiceRequest> newMerchantServices) {
        List<MerchantAlphaPayServicesEntity> existingServices = merchantServicesRepository.findByMerchantId(merchantId);

        // Create map for quick lookup: serviceId -> existing entity
        Map<String, MerchantAlphaPayServicesEntity> existingMap = existingServices.stream()
                .collect(Collectors.toMap(
                        s -> s.getAlphaPayService().getServiceId(),
                        s -> s
                ));

        List<AlphaPayServicesEntity> serviceEntities = alphaPayServicesRepository.findAll();

        // Map serviceId -> AlphaPayServicesEntity
        Map<String, AlphaPayServicesEntity> serviceEntityMap = serviceEntities.stream()
                .collect(Collectors.toMap(AlphaPayServicesEntity::getServiceId, s -> s));


        for (MerchantServiceRequest requestService : newMerchantServices) {
            String serviceId = requestService.getServiceId();
            String status = requestService.getStatus();

            if (existingMap.containsKey(serviceId)) {
                // Update status
                existingMap.get(serviceId).setStatus(status);
            } else {
                // Add new
                log.debug("requestService:::{}", requestService);
                AlphaPayServicesEntity alphaPayService = serviceEntityMap.get(serviceId);
                if (alphaPayService != null) {
                    MerchantAlphaPayServicesEntity newEntity = new MerchantAlphaPayServicesEntity();
                    newEntity.setMerchantId(merchantId);
                    newEntity.setAlphaPayService(alphaPayService);
                    newEntity.setServiceId(alphaPayService.getServiceId());
                    newEntity.setStatus(status);
                    existingServices.add(newEntity);
                    log.debug("newEntity:::{}", newEntity);
                }
            }
        }
        log.debug("existingServices:::{}", existingServices);
        merchantServicesRepository.saveAll(existingServices);
    }

    private void checkIfMerchantHasActiveService(Long merchantId, String serviceId) {
        List<MerchantAlphaPayServicesEntity> merchantServices = getMerchantActiveServices(merchantId);
        if (merchantServices == null || merchantServices.isEmpty()) {
            throw new ServiceNotAllowedToMerchantException();
        }
    }

    @Override
    @Transactional
    public MerchantGatewayConfigurationResponse generateApiKey(GenerateApiKeyRequest request) {
        checkIfMerchantHasActiveService(request.getMerchantId(), request.getServiceId());
        MerchantServiceConfigEntity merchantServiceConfig;
        String token = BeanUtility.generateToken(256);
        Optional<MerchantServiceConfigEntity> merchantServiceConfigEntity = merchantServiceConfigRepository.findByMerchantId(request.getMerchantId());
        if (merchantServiceConfigEntity.isPresent()) {
            merchantServiceConfigEntity.get().setExpirationDate(setExpirationDate(24));
            merchantServiceConfigEntity.get().setApiKey(token);
            merchantServiceConfigRepository.save(merchantServiceConfigEntity.get());
            merchantServiceConfig = merchantServiceConfigEntity.get();
        } else {
            MerchantServiceConfigEntity newMerchantServiceConfigEntity = new MerchantServiceConfigEntity();
            newMerchantServiceConfigEntity.setApiKey(token);
            newMerchantServiceConfigEntity.setMerchantId(request.getMerchantId());
            newMerchantServiceConfigEntity.setExpirationDate(setExpirationDate(24));
            merchantServiceConfigRepository.save(newMerchantServiceConfigEntity);
            merchantServiceConfig = newMerchantServiceConfigEntity;
        }

        return createConfigurationResponse(merchantServiceConfig, request);
    }

    public LocalDateTime setExpirationDate(Integer validationInMonth) {
        LocalDateTime currentDate = LocalDateTime.now();
        return currentDate.plusMonths(validationInMonth);
    }

    @Override
    public MerchantGatewayConfigurationResponse configureWebhook(WebhookRequest request) {
        checkIfMerchantHasActiveService(request.getMerchantId(), request.getServiceId());
        Optional<MerchantServiceConfigEntity> merchantServiceConfigEntity = merchantServiceConfigRepository.findByMerchantId(request.getMerchantId());
        MerchantServiceConfigEntity newMerchantServiceConfig;
        if (merchantServiceConfigEntity.isPresent()) {
            merchantServiceConfigEntity.get().setWebhookUrl(request.getWebhookUrl());
            merchantServiceConfigEntity.get().setWebhookSecretKey(request.getWebhookSecretKey());
            merchantServiceConfigRepository.save(merchantServiceConfigEntity.get());
            newMerchantServiceConfig = merchantServiceConfigEntity.get();
        } else {
            MerchantServiceConfigEntity merchantServiceConfig = new MerchantServiceConfigEntity();
            merchantServiceConfig.setWebhookUrl(request.getWebhookUrl());
            merchantServiceConfig.setWebhookSecretKey(request.getWebhookSecretKey());
            merchantServiceConfigRepository.save(merchantServiceConfig);
            newMerchantServiceConfig = merchantServiceConfig;
        }
        return createConfigurationResponse(newMerchantServiceConfig, request);
    }

    @Override
    @Transactional
    public MerchantGatewayConfigurationResponse configureCallBack(CallBackRequest request) {
        checkIfMerchantHasActiveService(request.getMerchantId(), request.getServiceId());
        Optional<MerchantServiceConfigEntity> merchantServiceConfigEntity = merchantServiceConfigRepository.findByMerchantId(request.getMerchantId());
        MerchantServiceConfigEntity newMerchantServiceConfig;
        if (merchantServiceConfigEntity.isPresent()) {
            merchantServiceConfigEntity.get().setCallbackUrl(request.getCallbackUrl());
            merchantServiceConfigRepository.save(merchantServiceConfigEntity.get());
            newMerchantServiceConfig = merchantServiceConfigEntity.get();
        } else {
            MerchantServiceConfigEntity merchantServiceConfig = new MerchantServiceConfigEntity();
            merchantServiceConfig.setCallbackUrl(request.getCallbackUrl());
            merchantServiceConfigRepository.save(merchantServiceConfig);
            newMerchantServiceConfig = merchantServiceConfig;
        }
        return createConfigurationResponse(newMerchantServiceConfig, request);
    }

    @Override
    public MerchantGatewayConfigurationResponse getGatewayConfiguration(GetGatewayConfigurationRequest request) {
        checkIfMerchantHasActiveService(request.getMerchantId(), request.getServiceId());
        Optional<MerchantServiceConfigEntity> merchantServiceConfigEntity = merchantServiceConfigRepository.findByMerchantId(request.getMerchantId());

        return createConfigurationResponse(merchantServiceConfigEntity.get(), request);
    }

    private MerchantGatewayConfigurationResponse createConfigurationResponse(MerchantServiceConfigEntity merchantServices, GatewayConfigurationBaseRequest request) {
        MerchantGatewayConfigurationResponse response = new MerchantGatewayConfigurationResponse();
        BeanUtility.copyProperties(request, response);
        BeanUtility.copyProperties(merchantServices, response);
        resolverService.setAsSuccess(response);
        return response;
    }


    @Override
    public MerchantServiceConfigEntity validatedMerchantApiKey(String apiKey) {
        MerchantServiceConfigEntity merchantAlphaPayServicesEntity = merchantServiceConfigRepository.findByApiKey(apiKey);
        if (merchantAlphaPayServicesEntity == null) {
            throw new InvalidAPIKeyException();
        }
        return merchantAlphaPayServicesEntity;
    }

    @Override
    public MerchantServiceConfigEntity getConfigEntityByMerchantId(Long merchantId) {
        Optional<MerchantServiceConfigEntity> merchantAlphaPayServicesEntity = merchantServiceConfigRepository.findByMerchantId(merchantId);
        return merchantAlphaPayServicesEntity.orElseGet(MerchantServiceConfigEntity::new);
    }

    @Override
    public MerchantStats getLast24hStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusHours(24);

        // ✅ Fetch last 24 hours merchants
        List<UserEntity> merchantsLast24h = merchantRepo.findByCreationTimeBetween(yesterday, now);

        int newMerchantsToday = merchantsLast24h.size();
        int approvedToday = (int) merchantsLast24h.stream()
                .filter(m -> "Approved".equalsIgnoreCase(m.getStatus()))
                .count();
        int rejectedToday = (int) merchantsLast24h.stream()
                .filter(m -> "Rejected".equalsIgnoreCase(m.getStatus()))
                .count();

        int lastLoginCount = (int) merchantsLast24h.stream()
                .filter(m -> m.getLastLogin() != null)
                .count();

        int lockedCount = (int) merchantsLast24h.stream()
                .filter(UserEntity::isLocked)
                .count();

        int disabledCount = (int) merchantsLast24h.stream()
                .filter(m -> m.getDisabledDate() != null)
                .count();

        double avgApprovalHours = merchantsLast24h.stream()
                .filter(m -> "Approved".equalsIgnoreCase(m.getStatus()) && m.getActivationDate() != null)
                .mapToDouble(m -> Duration.between(m.getCreationTime(),
                        m.getActivationDate().toInstant()).toHours())
                .average()
                .orElse(0);


        // ✅ Approval status breakdowns using Merchant table fields
        Map<String, Long> managerApprovalBreakdown = null;
        //merchantsLast24h.stream()
//                .collect(Collectors.groupingBy(UserEntity::getManagerApproveStatus, Collectors.counting()));

        Map<String, Long> adminApprovalBreakdown =null;
        //merchantsLast24h.stream()
//                .collect(Collectors.groupingBy(UserEntity::getAdminApproveStatus, Collectors.counting()));

        Map<String, Long> mbmeApprovalBreakdown = null;
        //merchantsLast24h.stream()
//                .collect(Collectors.groupingBy(UserEntity::getMbmeApproveStatus, Collectors.counting()));

        Map<String, Long> myfattoraApprovalBreakdown =null;
        //merchantsLast24h.stream()
//                .collect(Collectors.groupingBy(UserEntity::getMyfattoraApproveStatus, Collectors.counting()));

        return new MerchantStats(
                newMerchantsToday,
                approvedToday,
                lastLoginCount,
                rejectedToday,
                avgApprovalHours,
                managerApprovalBreakdown,
                adminApprovalBreakdown,
                mbmeApprovalBreakdown,
                myfattoraApprovalBreakdown,
                lockedCount,
                disabledCount
        );
    }
}
