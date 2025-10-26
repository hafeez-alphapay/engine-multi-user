package com.alphapay.payEngine.account.management.service;

import com.alphapay.payEngine.account.management.dto.response.BasicUserDetails;
import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.roles.model.RoleEntity;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class BasicUserMapperService {

    public BasicUserDetails toBasicUserDetails(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }

        BasicUserDetails basicUserDetails = new BasicUserDetails();
        basicUserDetails.setUserId(userEntity.getId());
        basicUserDetails.setFullName(userEntity.getUserDetails().getFullName());
        basicUserDetails.setMobileNo(userEntity.getUserDetails().getMobileNo());
        basicUserDetails.setEmail(userEntity.getUserDetails().getEmail());
        basicUserDetails.setEnabled(userEntity.isEnabled());
        basicUserDetails.setLocked(userEntity.isLocked());
        basicUserDetails.setLoginTryCount(userEntity.getLoginTryCount());
        basicUserDetails.setActivationDate(userEntity.getActivationDate());
        basicUserDetails.setDisabledDate(userEntity.getDisabledDate());
        basicUserDetails.setLastLogin(userEntity.getLastLogin());

        if (userEntity.getRoles() != null) {
            String roles = userEntity.getRoles().stream()
                    .map(RoleEntity::getName)
                    .collect(Collectors.joining(", "));
            basicUserDetails.setRoleName(roles);
        }

        return basicUserDetails;
    }
}
