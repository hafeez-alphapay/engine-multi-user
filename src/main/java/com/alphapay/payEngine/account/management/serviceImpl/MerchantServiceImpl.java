package com.alphapay.payEngine.account.management.serviceImpl;

import com.alphapay.payEngine.account.management.exception.AccountNotFoundException;
import com.alphapay.payEngine.account.management.dto.request.GetAllUsersRequestFilter;
import com.alphapay.payEngine.account.management.exception.DateMismatchException;
import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.management.service.BaseUserService;
import com.alphapay.payEngine.account.management.service.MerchantService;
import com.alphapay.payEngine.account.merchantKyc.model.MerchantEntity;
import com.alphapay.payEngine.account.merchantKyc.repository.MerchantRepository;
import com.alphapay.payEngine.account.roles.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    MerchantRepository merchantRepository;
    @Value("${trans.history.page.size}")
    private String historyPageSize;
    @Value("${trans.history.duration}")
    private String transHistoryDuration;
    @Value("${default.unfiltered.max-results}")
    private String defaultUnfilteredMaxResults;

    @Autowired
    private BaseUserService userService;

    @Override
    public Page<MerchantEntity> getAllMerchants(GetAllUsersRequestFilter request) {
        if (request.getToDate() != null) if (request.getToDate().before(request.getFromDate())) {
            log.debug("Date mismatch: To date {} is before From date {}", request.getToDate(), request.getFromDate());
            throw new DateMismatchException();
        }

        if (request.getToDate() != null) if (request.getToDate().after(new Date())) {
            request.setToDate(new Date());
        }
        if (request.getFromDate() != null && request.getToDate() != null) {
            Duration duration = Duration.between(request.getFromDate().toInstant(), request.getToDate().toInstant());
            log.debug("Duration between dates: {} days", duration.toDays());
            Long daysBetween = duration.toDays();
            if (daysBetween > Long.parseLong(transHistoryDuration)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(request.getToDate());
                cal.add(Calendar.DATE, -Integer.parseInt(transHistoryDuration));
                request.setFromDate(cal.getTime());
            }
        }
        Specification<MerchantEntity> transactionSpec = new MerchantSpecification(request);

        // Setup pagination
        int pageIndex = request.getPageNumber() == null ? 0 : request.getPageNumber() - 1;
        int pageSize = request.getPageSize() == null ? Integer.parseInt(historyPageSize) : request.getPageSize();
        PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by("createdAt").descending());
        Page<MerchantEntity> merchantPage;
        try {
            merchantPage = merchantRepository.findAll(transactionSpec, pageRequest);
        } catch (Exception ex) {
            log.error("Error fetching user data", ex);
            throw new RuntimeException("Error fetching user data");
        }

        return merchantPage;
    }

    @Override
    public Page<MerchantEntity> getMultiVendorUsers(GetAllUsersRequestFilter request) {
        // Validate date range
        if (request.getToDate() != null) if (request.getToDate().before(request.getFromDate())) {
            log.debug("Date mismatch: To date {} is before From date {}", request.getToDate(), request.getFromDate());
            throw new DateMismatchException();
        }

        if (request.getToDate() != null) if (request.getToDate().after(new Date())) {
            request.setToDate(new Date());
        }
        if (request.getFromDate() != null && request.getToDate() != null) {
            Duration duration = Duration.between(request.getFromDate().toInstant(), request.getToDate().toInstant());
            log.debug("Duration between dates: {} days", duration.toDays());
            Long daysBetween = duration.toDays();
            if (daysBetween > Long.parseLong(transHistoryDuration)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(request.getToDate());
                cal.add(Calendar.DATE, -Integer.parseInt(transHistoryDuration));
                request.setFromDate(cal.getTime());
            }
        }
        Long parentId = request.getParentId();
        MerchantEntity superUser = merchantRepository.findById(parentId).orElseThrow(UserNotFoundException::new); // Ensure the parent user exists, throws UserNotFoundException if not found
        List<Long> subsMerchants = new ArrayList<>();
        if (superUser.getSubMerchants() != null) {
            // Collect sub-merchant IDs from the super user
            if (!superUser.getSubMerchants().isEmpty() && superUser.getSubMerchants().size() > 0) {
                subsMerchants = superUser.getSubMerchants().stream().map(MerchantEntity::getId).collect(Collectors.toList());
            }
            subsMerchants.add(request.getParentId());
        }
        request.setSubUserIds(subsMerchants);
        Specification<MerchantEntity> transactionSpec = new MerchantSpecification(request);

        // Setup pagination
        int pageIndex = request.getPageNumber() == null ? 0 : request.getPageNumber() - 1;
        int pageSize = request.getPageSize() == null ? Integer.parseInt(historyPageSize) : request.getPageSize();
        PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by("creationTime").descending());
        Page<MerchantEntity> merchantPage;
        try {
            merchantPage = merchantRepository.findAll(transactionSpec, pageRequest);
        } catch (Exception ex) {
            log.error("Error fetching user data", ex);
            throw new RuntimeException("Error fetching user data");
        }

        return merchantPage;
    }

    /**
     * Retrieves a {@link MerchantEntity} using a flexible lookup strategy.
     * <p>
     * Primary path: attempts to load the merchant by its primary key via
     * {@link MerchantRepository#findById(Object)} using the provided {@code merchantId}.
     * </p>
     * <p>
     * Fallback path: if no merchant exists with that ID, the same identifier is treated as a
     * user identifier. The method resolves the {@link UserEntity} via
     * {@link BaseUserService#getLoggedUser(Long)} and then fetches the merchant owned by:
     * </p>
     * <ul>
     *   <li>the resolved user, when the user has no parent (i.e., a direct owner), or</li>
     *   <li>the user's parent account, when the resolved user is a sub-user.</li>
     * </ul>
     * <p>
     * This allows API callers to pass either a merchant ID or (when no merchant is found)
     * a user ID (for an owner or a sub-user) and still obtain the correct merchant record.
     * </p>
     *
     * @param merchantId the merchant primary key; if not found, this value is interpreted as a user ID
     * @return the resolved {@link MerchantEntity}
     * @throws UserNotFoundException if the fallback user-based resolution path is taken and no merchant
     *                               is associated with the resolved owner user
     */
    @Override
    public MerchantEntity getMerchant(Long merchantId) {
        Optional<MerchantEntity> merchantEntity;
        merchantEntity = merchantRepository.findById(merchantId);
        // TODO: Remove the fallback below — this case should never occur. In production, a merchant ID should not be treated as a user ID.
        if (merchantEntity.isEmpty()){
            UserEntity userEntity = userService.getLoggedUser(merchantId);
            if (userEntity.getParentUser() == null) {
                merchantEntity = merchantRepository.findByOwnerUser(userEntity);
            }else{
                merchantEntity = Optional.ofNullable(merchantRepository.findByOwnerUser(userEntity.getParentUser()).orElseThrow(AccountNotFoundException::new));
            }
        }
        return merchantEntity.get();
    }

    /**
     * Returns the merchant associated with the given user ID.
     * <p>
     * This method first loads the {@link UserEntity} using {@link BaseUserService#getLoggedUser(Long)}.
     * If the resolved user is a sub-user (i.e., {@code getParentUser() != null}), the method looks up
     * the merchant owned by the parent user. Otherwise, it looks up the merchant owned by the user
     * themself. The merchant is retrieved via {@link MerchantRepository#findByOwnerUser(UserEntity)}.
     * </p>
     *
     * @param userId the ID of the user (or sub-user) whose associated merchant should be returned
     * @return the {@link MerchantEntity} owned by the parent user (for sub-users) or by the user directly
     * @throws UserNotFoundException if the user cannot be found or if no merchant is associated with the
     *                               resolved owner user
     */
    @Override
    public MerchantEntity getMerchantByUserId(Long userId) {
        UserEntity userEntity = userService.getLoggedUser(userId);
        MerchantEntity merchantEntity;
        if (userEntity.getParentUser() != null) {
            merchantEntity = merchantRepository.findByOwnerUser(userEntity.getParentUser()).orElseThrow(UserNotFoundException::new);
        } else {
            merchantEntity = merchantRepository.findByOwnerUser(userEntity).orElseThrow(UserNotFoundException::new);
        }
        return merchantEntity;
    }
}
