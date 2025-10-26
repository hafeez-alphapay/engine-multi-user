package com.alphapay.payEngine.account.management.service;

import com.alphapay.payEngine.account.management.dto.request.GetAllUsersRequestFilter;
import com.alphapay.payEngine.account.management.dto.response.BasicUserDetails;
import com.alphapay.payEngine.account.management.dto.response.PaginatedResponse;
import com.alphapay.payEngine.account.management.model.UserEntity;
import org.springframework.data.domain.Page;

public interface BaseUserService {

    UserEntity getLoggedUser(String email, String channelId);
    Page<UserEntity> getAllUsers(GetAllUsersRequestFilter request);
    Page<UserEntity> getMultiVendorUsers(GetAllUsersRequestFilter request);
    PaginatedResponse<BasicUserDetails> getBasicUserDetails(GetAllUsersRequestFilter request);
    UserEntity getLoggedUser(Long merchantId);
}
