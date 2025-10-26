package com.alphapay.payEngine.account.roles.serviceImpl;

import com.alphapay.payEngine.account.management.dto.request.GetAllUsersRequestFilter;
import com.alphapay.payEngine.account.management.dto.response.BasicUserDetails;
import com.alphapay.payEngine.account.management.dto.response.PaginatedResponse;
import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.management.repository.UserRepository;
import com.alphapay.payEngine.account.management.service.BaseUserService;
import com.alphapay.payEngine.account.roles.dto.request.*;
import com.alphapay.payEngine.account.roles.dto.response.*;
import com.alphapay.payEngine.account.roles.exception.*;
import com.alphapay.payEngine.account.roles.model.PermissionEntity;
import com.alphapay.payEngine.account.roles.model.RoleEntity;
import com.alphapay.payEngine.account.roles.model.RolePermissionEntity;
import com.alphapay.payEngine.account.roles.model.UserRoleEntity;
import com.alphapay.payEngine.account.roles.repository.PermissionRepository;
import com.alphapay.payEngine.account.roles.repository.RolePermissionRepository;
import com.alphapay.payEngine.account.roles.repository.RoleRepository;
import com.alphapay.payEngine.account.roles.repository.UserRoleRepository;
import com.alphapay.payEngine.account.roles.service.RoleUserManagementService;
import com.alphapay.payEngine.utilities.MessageService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class RoleUserUserManagementServiceImpl implements RoleUserManagementService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private BaseUserService baseUserService;

    @Override
    public AllPermissionResponse getAllPermissions(GetPermissionsRequest request, Locale locale) {
        // Fetch all permissions from the database
        List<PermissionEntity> permissionEntities = permissionRepository.findAll();

        // Map permissions to response objects
        List<PermissionResponse> permissionResponses = permissionEntities.stream()
                .map(this::mapToPermissionResponse)
                .collect(Collectors.toList());

        // Create and return the response
        AllPermissionResponse response = new AllPermissionResponse();
        BeanUtils.copyProperties(request, response);
        response.setPermissions(permissionResponses);
        response.setStatus("Success");
        response.setResponseCode(200);
        response.setResponseMessage(messageService.getLocalizedMessage("permissions.fetched.success", locale));
        return response;
    }

    private PermissionResponse mapToPermissionResponse(PermissionEntity permissionEntity) {
        PermissionResponse permissionResponse = new PermissionResponse();

        BeanUtils.copyProperties(permissionEntity, permissionResponse);

        return permissionResponse;
    }

    @Override
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request, Locale locale) {
        if (roleRepository.existsByName(request.getName())) {
            throw new DuplicateRoleNameException();
        }
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName(request.getName());
        roleEntity.setStatus("Active");
        roleEntity = roleRepository.save(roleEntity);

        List<PermissionEntity> permissionEntities = permissionRepository.findAllById(request.getPermissions());
        if (permissionEntities.size() != request.getPermissions().size()) {
            throw new PermissionDoesNotExistException();
        }

        RoleEntity finalRoleEntity = roleEntity;
        List<RolePermissionEntity> rolePermissionEntities = permissionEntities.stream()
                .map(permissionEntity -> {
                    RolePermissionEntity rolePermissionEntity = new RolePermissionEntity();
                    rolePermissionEntity.setRoleEntity(finalRoleEntity);
                    rolePermissionEntity.setPermissionEntity(permissionEntity);
                    rolePermissionEntity.setStatus("Active");
                    return rolePermissionEntity;
                })
                .collect(Collectors.toList());
        rolePermissionRepository.saveAll(rolePermissionEntities);

        List<PermissionResponse> permissionResponses = permissionEntities.stream()
                .map(this::mapToPermissionResponse)
                .collect(Collectors.toList());

        RoleResponse response = new RoleResponse();
        Role roleResponse = new Role();
        roleResponse.setRoleId(finalRoleEntity.getId());
        BeanUtils.copyProperties(roleEntity, roleResponse);
        roleResponse.setPermissions(permissionResponses);
        BeanUtils.copyProperties(request, response);
        response.setStatus("Success");
        response.setResponseCode(200);
        response.setRole(roleResponse);
        response.setResponseMessage(messageService.getLocalizedMessage("role.created.success", locale));
        return response;
    }

    @Override
    public RoleResponse assignUserRole(UserRoleRequest request, Locale locale) {

        RoleEntity roleEntity = roleRepository.findById(request.getRoleId())
                .orElseThrow(RoleNotFoundException::new);

        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(UserNotFoundException::new);

        if (userRoleRepository.findByUserAndRoleEntity(user, roleEntity)
                .isPresent()) {
            throw new RoleAlreadyAssignedException();
        }
        userRoleRepository.deleteByUser(user);
        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setUser(user);
        userRoleEntity.setRoleEntity(roleEntity);
        userRoleEntity.setStatus("Active");
        userRoleRepository.save(userRoleEntity);

        RoleResponse response = new RoleResponse();
        Role role = new Role();
        role.setName(roleEntity.getName());
        role.setRoleId(roleEntity.getId());
        BeanUtils.copyProperties(request, response);
        response.setRole(role);
        response.setStatus("Success");
        response.setResponseCode(201);
        response.setResponseMessage(messageService.getLocalizedMessage("role.assigned.success", locale));

        return response;
    }

    @Override
    public AllRoleResponse getAllRoles(GetRolesRequest request, Locale locale) {

        List<RoleEntity> roleEntities = roleRepository.findAll();

        List<Role> roleResponses = roleEntities.stream().map(roleEntity -> {
            List<PermissionEntity> permissionEntities = rolePermissionRepository.findByRoleEntity(roleEntity)
                    .stream()
                    .map(RolePermissionEntity::getPermissionEntity)
                    .toList();

            List<PermissionResponse> permissionResponses = permissionEntities.stream()
                    .map(this::mapToPermissionResponse)
                    .collect(Collectors.toList());

            Role roleResponse = new Role();
            roleResponse.setRoleId(roleEntity.getId());
            roleResponse.setName(roleEntity.getName());
            roleResponse.setPermissions(permissionResponses);
            return roleResponse;
        }).toList();

        AllRoleResponse response = new AllRoleResponse();
        response.setStatus("Success");
        response.setRequestId(request.getRequestId());
        response.setResponseCode(200);
        response.setResponseMessage(messageService.getLocalizedMessage("roles.fetched.success", locale));
        response.setRoles(roleResponses);

        return response;
    }

    @Override
    public AllRoleResponse getUserRoles(UserRoleRequest request, Locale locale) {

        UserEntity userEntity = userRepository.findById(request.getUserId())
                .orElseThrow(UserNotFoundException::new);

        List<UserRoleEntity> userRoles = userRoleRepository.findByUser(userEntity);
        if (userRoles.isEmpty()) {
            throw new UserRoleNotFoundException();
        }

        List<Role> roleResponses = userRoles.stream().map(userRole -> {
            RoleEntity roleEntity = roleRepository.findById(userRole.getRoleEntity().getId())
                    .orElseThrow(RoleNotFoundException::new);

            List<PermissionEntity> permissions = rolePermissionRepository.findByRoleEntity(roleEntity)
                    .stream()
                    .map(RolePermissionEntity::getPermissionEntity)
                    .toList();
            List<PermissionResponse> permissionResponses = permissions.stream().map(permission -> {
                PermissionResponse permissionResponse = new PermissionResponse();
                permissionResponse.setId(permission.getId());
                permissionResponse.setName(permission.getName());
                return permissionResponse;
            }).toList();

            Role role = new Role();
            role.setName(roleEntity.getName());
            role.setRoleId(roleEntity.getId());
            role.setPermissions(permissionResponses);

            return role;
        }).toList();

        AllRoleResponse response = new AllRoleResponse();
        response.setStatus("Success");
        response.setRequestId(request.getRequestId());
        response.setResponseCode(200);
        response.setResponseMessage(messageService.getLocalizedMessage("user.roles.fetched.success", locale));
        response.setRoles(roleResponses);

        return response;
    }

    @Override
    public RoleResponse updateRole(UpdateRoleRequest request, Locale locale) {
        RoleEntity roleEntity = roleRepository.findById(request.getRoleId())
                .orElseThrow(RoleNotFoundException::new);
        roleEntity.setName(request.getName());
        rolePermissionRepository.deleteByRoleEntity(roleEntity);
        List<PermissionEntity> permissions = new ArrayList<>();
        for (Long permissionId : request.getPermissions()) {
            PermissionEntity permissionEntity = permissionRepository.findById(permissionId).orElseThrow(PermissionDoesNotExistException::new);
            permissions.add(permissionEntity);
            RolePermissionEntity rolePermission = new RolePermissionEntity();
            rolePermission.setStatus("Active");
            rolePermission.setRoleEntity(roleEntity);
            rolePermission.setPermissionEntity(permissionEntity);
            rolePermissionRepository.save(rolePermission);
        }

        List<PermissionResponse> permissionResponses = permissions.stream()
                .map(this::mapToPermissionResponse)
                .collect(Collectors.toList());
        RoleResponse response = new RoleResponse();

        Role role = new Role();
        role.setRoleId(roleEntity.getId());
        role.setName(roleEntity.getName());
        role.setPermissions(permissionResponses);
        response.setRole(role);

        response.setStatus("Success");
        response.setRequestId(request.getRequestId());
        response.setResponseCode(200);
        response.setResponseMessage(messageService.getLocalizedMessage("role.updated.success", locale));
        return response;
    }

    @Override
    public RoleResponse deleteRole(@Valid DeleteRoleRequest request, Locale locale) {
        RoleEntity roleEntity = roleRepository.findById(request.getRoleId())
                .orElseThrow(RoleNotFoundException::new);
        if (userRoleRepository.existsByRoleEntity(roleEntity)) {
            throw new RoleAlreadyAssignedException();
        }
        rolePermissionRepository.deleteByRoleEntity(roleEntity);
        roleRepository.deleteById(roleEntity.getId());
        RoleResponse response = new RoleResponse();

        response.setStatus("Success");
        response.setRequestId(request.getRequestId());
        response.setResponseCode(200);
        response.setResponseMessage(messageService.getLocalizedMessage("role.deleted.success", locale));
        return null;
    }

    @Override
    public RoleResponse deleteUserRole(UserRoleRequest request, Locale locale) {
        UserEntity userEntity = userRepository.findById(request.getUserId())
                .orElseThrow(UserNotFoundException::new);

        RoleEntity roleEntity = roleRepository.findById(request.getRoleId())
                .orElseThrow(RoleNotFoundException::new);

        userRoleRepository.deleteByUserAndRoleEntity(userEntity, roleEntity);

        RoleResponse response = new RoleResponse();
        response.setStatus("Success");
        response.setRequestId(request.getRequestId());
        response.setResponseCode(200);
        response.setResponseMessage(messageService.getLocalizedMessage("role.deleted.success", locale));

        return response;
    }

    @Override
    public PaginatedResponse<BasicUserDetails> getAllUserBasicInfo(GetAllUsersRequestFilter request) {
        return baseUserService.getBasicUserDetails(request);
    }

}
