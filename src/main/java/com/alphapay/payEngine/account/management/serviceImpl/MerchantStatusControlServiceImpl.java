package com.alphapay.payEngine.account.management.serviceImpl;

import com.alphapay.payEngine.account.management.dto.request.GetMerchantStatusChanges;
import com.alphapay.payEngine.account.management.dto.request.UpdateMerchantStatusRequest;
import com.alphapay.payEngine.account.management.dto.response.PaginatedResponse;
import com.alphapay.payEngine.account.management.model.MerchantStatusHistoryEntity;
import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.management.repository.MerchantStatusHistoryRepository;
import com.alphapay.payEngine.account.management.repository.UserRepository;
import com.alphapay.payEngine.account.management.service.BaseUserService;
import com.alphapay.payEngine.account.management.service.MerchantStatusControlService;
import com.alphapay.payEngine.account.roles.exception.UserNotFoundException;
import com.alphapay.payEngine.integration.model.MerchantPaymentProviderRegistration;
import com.alphapay.payEngine.integration.model.orchast.ServiceProvider;
import com.alphapay.payEngine.integration.repository.MerchantProviderRegistrationRepository;
import com.alphapay.payEngine.integration.repository.ServiceProviderRepository;
import com.alphapay.payEngine.utilities.ApprovalStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.alphapay.payEngine.utilities.UserAccountStatus.*;

@Slf4j
@Service
public class MerchantStatusControlServiceImpl implements MerchantStatusControlService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    MerchantProviderRegistrationRepository merchantProviderRegistrationRepository;

    @Autowired
    private MerchantStatusHistoryRepository merchantStatusHistoryRepository;

    @Autowired
    private BaseUserService userService;
    @Value("${mbme.provider.service.id}")
    private String mbmeProviderServiceId;

    @Value("${mf.provider.service.id}")
    private String mfProviderServiceId;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Override
    public MerchantStatusHistoryEntity handleStatusChange(UpdateMerchantStatusRequest request) {
        UserEntity performByUserEntity = userRepository.findById(request.getPerformByUserId()).orElseThrow(UserNotFoundException::new);
        String assignUserName;
        if (request.getAssignedUserId() != null) {
            Optional<UserEntity> assignedEntity = userRepository.findById(request.getAssignedUserId());
            assignUserName = assignedEntity.get().getUserDetails().getFullName();
        } else {
            assignUserName = performByUserEntity.getUserDetails().getFullName();
        }

        switch (request.getEntityType()) {
            case USER_ACTIVATE_ACCOUNT:
                updateUserActivateAccountStatus(request);
                break;
            case USER_LOCK_ACCOUNT:
                updateUserLockAccountStatus(request);
                break;
            case MANAGER_APPROVAL:
                updateManagerApprovalStatus(request);
                break;
            case ADMIN_APPROVAL:
                updateAdminApprovalStatus(request);
                break;
            case MBME_PAYMENT_PROVIDER:
                updateMbmePaymentProviderStatus(request);
                break;
            case MF_PAYMENT_PROVIDER:
                updateMFPaymentProviderStatus(request);
                break;
            default:
                throw new IllegalArgumentException("Unsupported entity type");
        }


        return saveStatusHistory(request, performByUserEntity.getUserDetails().getFullName(), assignUserName);
    }

    private void updateMbmePaymentProviderStatus(UpdateMerchantStatusRequest request) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByServiceId(mbmeProviderServiceId).get();

        Optional<MerchantPaymentProviderRegistration> merchantPaymentProviderRegistrations = merchantProviderRegistrationRepository.findByServiceProviderAndMerchantId(serviceProvider, request.getMerchantId());
        if (merchantPaymentProviderRegistrations.isPresent()) {
            merchantPaymentProviderRegistrations.get().setStatus(request.getNewStatus());
            merchantProviderRegistrationRepository.save(merchantPaymentProviderRegistrations.get());
        }
    }


    private void updateMFPaymentProviderStatus(UpdateMerchantStatusRequest request) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByServiceId(mfProviderServiceId).get();

        Optional<MerchantPaymentProviderRegistration> merchantPaymentProviderRegistrations = merchantProviderRegistrationRepository.findByServiceProviderAndMerchantId(serviceProvider, request.getMerchantId());
        if (merchantPaymentProviderRegistrations.isPresent()) {
            merchantPaymentProviderRegistrations.get().setStatus(request.getNewStatus());
            merchantProviderRegistrationRepository.save(merchantPaymentProviderRegistrations.get());
        }
    }

    private void updateAdminApprovalStatus(UpdateMerchantStatusRequest request) {
        UserEntity merchantEntity = userRepository.findById(request.getMerchantId()).orElseThrow(UserNotFoundException::new);

//        if (request.getNewStatus().equals(ApprovalStatus.APPROVED.getName())) {
//            merchantEntity.setAdminApproveStatus(ApprovalStatus.APPROVED.getName());
//        } else if (request.getNewStatus().equals(ApprovalStatus.REJECTED.getName())) {
//            merchantEntity.setAdminApproveStatus(ApprovalStatus.REJECTED.getName());
//        }
        userRepository.save(merchantEntity);
    }

    private void updateManagerApprovalStatus(UpdateMerchantStatusRequest request) {
        UserEntity merchantEntity = userRepository.findById(request.getMerchantId()).orElseThrow(UserNotFoundException::new);
        ;

//        if (request.getNewStatus().equals(ApprovalStatus.APPROVED.getName())) {
//            merchantEntity.setManagerApproveStatus(ApprovalStatus.APPROVED.name());
//        } else if (request.getNewStatus().equals(ApprovalStatus.REJECTED.getName())) {
//            merchantEntity.setManagerApproveStatus(ApprovalStatus.REJECTED.name());
//        }

        userRepository.save(merchantEntity);
    }

    private void updateUserActivateAccountStatus(UpdateMerchantStatusRequest request) {
        UserEntity merchantEntity = userRepository.findById(request.getMerchantId()).orElseThrow(UserNotFoundException::new);

        if (request.getNewStatus().equals(ENABLED.getName())) {
            merchantEntity.setEnabled(true);
        } else if (request.getNewStatus().equals(DISABLED.getName())) {
            merchantEntity.setEnabled(false);
        }
        userRepository.save(merchantEntity);
    }


    private void updateUserLockAccountStatus(UpdateMerchantStatusRequest request) {
        UserEntity merchantEntity = userRepository.findById(request.getMerchantId()).orElseThrow(UserNotFoundException::new);

        if (request.getNewStatus().equals(LOCKED.getName())) {
            merchantEntity.setLocked(true);
        } else if (request.getNewStatus().equals(UNLOCKED.getName())) {
            merchantEntity.setLocked(false);
            merchantEntity.setLoginTryCount(0);
        }

        userRepository.save(merchantEntity);
    }

    private MerchantStatusHistoryEntity saveStatusHistory(UpdateMerchantStatusRequest request, String performBy, String assignedToUser) {
        UserEntity merchantEntity = userRepository.findById(request.getMerchantId()).orElseThrow(UserNotFoundException::new);
        if (assignedToUser == null) {
            assignedToUser = performBy;
        }
        MerchantStatusHistoryEntity history = new MerchantStatusHistoryEntity();
        history.setMerchantId(request.getMerchantId());
//        history.setMerchantName(merchantEntity.getUserDetails().getTradeNameEnglish());
        history.setEntityType(request.getEntityType().name());
        history.setNewStatus(request.getNewStatus());
        history.setComment(request.getComment());
        history.setAssignedUserId(request.getAssignedUserId());
        history.setAssignedUser(assignedToUser);
        history.setPerformedByUserId(request.getPerformByUserId());
        history.setPerformedByUser(performBy);
        merchantStatusHistoryRepository.save(history);
        return history;
    }


    /**
     * @param request
     * @return
     */
    @Override
    public PaginatedResponse<MerchantStatusHistoryEntity> filterStatusChanges(GetMerchantStatusChanges request) {

        Specification<MerchantStatusHistoryEntity> transactionSpec = new MerchantStatusControlSpecification(request);
        int pageIndex = request.getPageNumber() == null ? 0 : request.getPageNumber() - 1;
        int pageSize = request.getPageSize() == null ? Integer.parseInt("100") : request.getPageSize();
        PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by("creationTime").descending());
        Page<MerchantStatusHistoryEntity> merchantStatusHistories;
        try {
            merchantStatusHistories = merchantStatusHistoryRepository.findAll(transactionSpec, pageRequest);
        } catch (Exception ex) {
            log.error("Error fetching user data", ex);
            throw new RuntimeException("Error fetching user data");
        }

        List<MerchantStatusHistoryEntity> merchantStatusHistoriesList = merchantStatusHistories.getContent().stream().toList();

        return new PaginatedResponse<MerchantStatusHistoryEntity>(
                merchantStatusHistoriesList,
                merchantStatusHistories.getNumber() + 1, // Convert zero-based page index to one-based
                merchantStatusHistories.getSize(),
                merchantStatusHistories.getTotalElements(),
                merchantStatusHistories.getTotalPages(),
                merchantStatusHistories.isLast()
        );

    }
}
