package com.alphapay.payEngine.account.management.serviceImpl;

import com.alphapay.payEngine.account.management.dto.request.LoginRequest;
import com.alphapay.payEngine.account.management.dto.response.LoginResponse;
import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.management.repository.UserRepository;
import com.alphapay.payEngine.account.management.service.InitializUserDataService;
import com.alphapay.payEngine.account.merchantKyc.model.MerchantEntity;
import com.alphapay.payEngine.account.merchantKyc.repository.MerchantRepository;
import com.alphapay.payEngine.account.roles.model.RoleEntity;
import com.alphapay.payEngine.account.roles.model.RolePermissionEntity;
import com.alphapay.payEngine.account.roles.model.UserRoleEntity;
import com.alphapay.payEngine.account.roles.repository.UserRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class InitializUserDataServiceImpl implements InitializUserDataService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;


    @Override
    public LoginResponse getInitializationData(LoginRequest request) {
        LoginResponse response = new LoginResponse();
        response.setEmail(request.getEmail());
        UserEntity user = userRepository.findByEmail(response.getEmail());
        List<UserRoleEntity> userRoleEntityList = userRoleRepository.findByUser(user);
        List<String> permissionResponseList = new ArrayList<>();
        List<String> roleResponse = new ArrayList<>();
        for (UserRoleEntity userRoleEntity : userRoleEntityList) {
            RoleEntity role = userRoleEntity.getRoleEntity();
            roleResponse.add(role.getName());
            for (RolePermissionEntity rolePermission : role.getRolePermissions()) {
                if (rolePermission.getStatus().equals("Active")) {
                    String permissionName = rolePermission.getPermissionEntity().getName();
                    if (!permissionResponseList.contains(permissionName)) {
                        permissionResponseList.add(permissionName);
                    }
                }
            }
        }
        response.setPermissions(permissionResponseList);
        response.setRoles(roleResponse);
        Map<String, String> userParams = new HashMap<>();
        Optional<MerchantEntity> merchantEntity;
        Set<MerchantEntity> subMerchants;
        Long merchantId;
        log.debug("Fetching merchant and sub-merchants for userId={}", user.getId());
        if (user.getParentUser() != null) {
            merchantEntity = merchantRepository.findByOwnerUser(user.getParentUser());
            merchantId = merchantEntity.get().getId();
            subMerchants =  merchantEntity.get().getSubMerchants();
        } else {
            merchantEntity = merchantRepository.findByOwnerUser(user);
            merchantId = merchantEntity.get().getId();
            subMerchants =  merchantEntity.get().getSubMerchants();
        }

        if (subMerchants != null && !subMerchants.isEmpty()) {
            Set<Long> subMerchantIds = new HashSet<>();
            for (MerchantEntity sub : subMerchants) {
                subMerchantIds.add(sub.getId());
            }
            userParams.put("subMerchantIds", subMerchantIds.toString());
        } else {
            userParams.put("subMerchantIds", "[]");
        }

//        Fetching merchant and sub-merchants for userId=3
//        Parent user found: parentUserId=2, merchantId=2, subMerchantCount=0
//        No sub-merchants found for merchantId=2

//        Fetching merchant and sub-merchants for userId=3
//        Parent user found: parentUserId=2, merchantId=2, subMerchantCount=5
//        Sub-merchant IDs for merchantId=2: [16, 20, 22, 23, 28]

//        Fetching merchant and sub-merchants for userId=2
//        No parent user found: userId=2, merchantId=2, subMerchantCount=5
//        Sub-merchant IDs for merchantId=2: [16, 20, 22, 23, 28]


        userParams.put("merchantId", String.valueOf(merchantId));
        userParams.put("userId", String.valueOf(user.getId()));
        userParams.put("full_name", user.getUserDetails().getFullName());
        userParams.put("email", user.getUserDetails().getEmail());
        userParams.put("mobile_no", user.getUserDetails().getMobileNo());
//        userParams.put("nationality", user.getUserDetails().getNationality());
//        userParams.put("logo",user.getLogo());
        userParams.put("parentUserId", (user.getParentUser() != null && user.getParentUser().getId() != null && user.getParentUser().getId() != 0) ? user.getParentUser().getId() + "" : "");
        response.setUserData(userParams);

        return response;
    }
}
