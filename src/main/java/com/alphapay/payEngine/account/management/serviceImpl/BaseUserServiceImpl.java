package com.alphapay.payEngine.account.management.serviceImpl;

import com.alphapay.payEngine.account.management.dto.request.GetAllUsersRequestFilter;
import com.alphapay.payEngine.account.management.dto.response.BasicUserDetails;
import com.alphapay.payEngine.account.management.dto.response.PaginatedResponse;
import com.alphapay.payEngine.account.management.exception.AccountNotFoundException;
import com.alphapay.payEngine.account.management.exception.DateMismatchException;
import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.management.repository.UserRepository;
import com.alphapay.payEngine.account.management.service.BaseUserService;
import com.alphapay.payEngine.account.management.service.BasicUserMapperService;
import com.alphapay.payEngine.account.roles.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class BaseUserServiceImpl implements BaseUserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    HttpServletRequest httpServletRequest;


    @Value("${trans.history.page.size}")
    private String historyPageSize;

    @Value("${trans.history.duration}")
    private String transHistoryDuration;

    @Value("${default.unfiltered.max-results}")
    private String defaultUnfilteredMaxResults;

    @Autowired
    private BasicUserMapperService basicUserMapperService;

    @Override
    public UserEntity getLoggedUser(String email, String applicationId) {
        if (applicationId == null)
            applicationId = (String) httpServletRequest.getAttribute("applicationId");
        log.debug("Finding {} for channel {}", email, applicationId);
        UserEntity loggedUser = userRepository.findByEmail(email);
        log.debug("loggedUser {}  ", loggedUser);

        if (loggedUser == null)
            throw new AccountNotFoundException();

        return loggedUser;
    }

    @Override
    public PaginatedResponse<BasicUserDetails> getBasicUserDetails(GetAllUsersRequestFilter request) {
        Page<UserEntity> userEntities = null;
        if(request.getIsAdmin())
            userEntities= getAllUsers(request);
        else
        {
            request.setParentId(request.getAuditInfo().getUserId());
            userEntities = getMultiVendorUsers(request);
        }

        Page<BasicUserDetails> basicUserDetails = userEntities.map(basicUserMapperService::toBasicUserDetails);
        List<BasicUserDetails> enrichedMerchants = basicUserDetails.getContent().stream().toList();

        return new PaginatedResponse<>(
                enrichedMerchants,
                basicUserDetails.getNumber() + 1, // Convert zero-based page index to one-based
                basicUserDetails.getSize(),
                basicUserDetails.getTotalElements(),
                basicUserDetails.getTotalPages(),
                basicUserDetails.isLast());
    }

    @Override
    public Page<UserEntity> getAllUsers(GetAllUsersRequestFilter request) {
        // Validate date range
        if (request.getToDate() != null)
            if (request.getToDate().before(request.getFromDate())) {
                log.debug("Date mismatch: To date {} is before From date {}", request.getToDate(), request.getFromDate());
                throw new DateMismatchException();
            }

        if (request.getToDate() != null)
            if (request.getToDate().after(new Date())) {
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
        Specification<UserEntity> transactionSpec = new UserSpecification(request);

        // Setup pagination
        int pageIndex = request.getPageNumber() == null ? 0 : request.getPageNumber() - 1;
        int pageSize = request.getPageSize() == null ? Integer.parseInt(historyPageSize) : request.getPageSize();
        PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by("creationTime").descending());
        Page<UserEntity> merchantPage;
        try {
            merchantPage = userRepository.findAll(transactionSpec, pageRequest);
        } catch (Exception ex) {
            log.error("Error fetching user data", ex);
            throw new RuntimeException("Error fetching user data");
        }

        return merchantPage;
    }

    @Override
    public Page<UserEntity> getMultiVendorUsers(GetAllUsersRequestFilter request) {
        // Validate date range
        if (request.getToDate() != null)
            if (request.getToDate().before(request.getFromDate())) {
                log.debug("Date mismatch: To date {} is before From date {}", request.getToDate(), request.getFromDate());
                throw new DateMismatchException();
            }

        if (request.getToDate() != null)
            if (request.getToDate().after(new Date())) {
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
        UserEntity superUser=getLoggedUser(parentId); // Ensure the parent user exists, throws UserNotFoundException if not found
        List<Long> subUsers= new ArrayList<>();
        if (superUser.getSubUsers() != null) {
            // Collect sub-merchant IDs from the super user
            if(!superUser.getSubUsers().isEmpty() && superUser.getSubUsers().size() > 0) {
                subUsers = superUser.getSubUsers().stream()
                        .map(UserEntity::getId)
                        .collect(Collectors.toList());
            }
            subUsers.add(request.getParentId());
        }
        request.setSubUserIds(subUsers);
        Specification<UserEntity> transactionSpec = new UserSpecification(request);

        // Setup pagination
        int pageIndex = request.getPageNumber() == null ? 0 : request.getPageNumber() - 1;
        int pageSize = request.getPageSize() == null ? Integer.parseInt(historyPageSize) : request.getPageSize();
        PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by("creationTime").descending());
        Page<UserEntity> merchantPage;
        try {
            merchantPage = userRepository.findAll(transactionSpec, pageRequest);
        } catch (Exception ex) {
            log.error("Error fetching user data", ex);
            throw new RuntimeException("Error fetching user data");
        }

        return merchantPage;
    }
    @Override
    public UserEntity getLoggedUser(Long merchantId) {
        return userRepository.findById(merchantId).orElseThrow(UserNotFoundException::new);
    }
}
