package com.alphapay.payEngine.account.roles.service;

import com.alphapay.payEngine.account.management.dto.request.GetAllUsersRequestFilter;
import com.alphapay.payEngine.account.management.dto.response.BasicUserDetails;
import com.alphapay.payEngine.account.management.dto.response.PaginatedResponse;
import com.alphapay.payEngine.account.roles.dto.request.*;
import com.alphapay.payEngine.account.roles.dto.response.AllPermissionResponse;
import com.alphapay.payEngine.account.roles.dto.response.AllRoleResponse;
import com.alphapay.payEngine.account.roles.dto.response.RoleResponse;
import jakarta.validation.Valid;

import java.util.Locale;

public interface RoleUserManagementService {
    AllPermissionResponse getAllPermissions(@Valid GetPermissionsRequest request, Locale locale);

    RoleResponse createRole(@Valid CreateRoleRequest request, Locale locale);

    RoleResponse assignUserRole(@Valid UserRoleRequest request, Locale locale);

    AllRoleResponse getAllRoles(@Valid GetRolesRequest request, Locale locale);

    AllRoleResponse getUserRoles(UserRoleRequest request, Locale locale);

    RoleResponse updateRole(@Valid UpdateRoleRequest request, Locale locale);

    RoleResponse deleteRole(@Valid DeleteRoleRequest request, Locale locale);

    RoleResponse deleteUserRole(@Valid UserRoleRequest request, Locale locale);

    PaginatedResponse<BasicUserDetails> getAllUserBasicInfo(@Valid GetAllUsersRequestFilter request);
}
