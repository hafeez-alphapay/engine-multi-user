package com.alphapay.payEngine.controller.account;

import com.alphapay.payEngine.account.management.dto.request.GetAllUsersRequestFilter;
import com.alphapay.payEngine.account.management.dto.response.BasicUserDetails;
import com.alphapay.payEngine.account.management.dto.response.PaginatedResponse;
import com.alphapay.payEngine.account.roles.dto.request.*;
import com.alphapay.payEngine.account.roles.dto.response.AllPermissionResponse;
import com.alphapay.payEngine.account.roles.dto.response.AllRoleResponse;
import com.alphapay.payEngine.account.roles.dto.response.RoleResponse;
import com.alphapay.payEngine.account.roles.service.RoleUserManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.alphapay.payEngine.utilities.UtilHelper.getLocale;

@RestController
@Slf4j
@RequestMapping("/superadmin")
public class RoleUserManagementController {

    @Autowired
    private RoleUserManagementService roleUserManagementService;

    @RequestMapping(value = "/permissions", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public @ResponseBody AllPermissionResponse getAllPermissions(@Valid @RequestBody GetPermissionsRequest request, HttpServletRequest httpServletRequest) {
        return roleUserManagementService.getAllPermissions(request,getLocale(request)  );
    }

    @RequestMapping(value = "/roles/create", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public @ResponseBody RoleResponse createRole(@Valid @RequestBody CreateRoleRequest request) {
        return roleUserManagementService.createRole(request,getLocale(request));
    }

    @RequestMapping(value = "/users/assignRole", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public @ResponseBody RoleResponse assignUserRole(@Valid @RequestBody UserRoleRequest request) {
        return roleUserManagementService.assignUserRole(request,getLocale(request));
    }

    @RequestMapping(value = "/roles", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public @ResponseBody AllRoleResponse getAllRoles(@Valid @RequestBody GetRolesRequest request) {
        return roleUserManagementService.getAllRoles(request,getLocale(request));
    }

    @RequestMapping(value = "/users/getRole", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public @ResponseBody AllRoleResponse getUserRole(@Valid @RequestBody UserRoleRequest request) {
        return roleUserManagementService.getUserRoles(request,getLocale(request));
    }

    @RequestMapping(value = "/users/deleteRole", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public @ResponseBody RoleResponse deleteUserRole(@Valid @RequestBody UserRoleRequest request) {
        return roleUserManagementService.deleteUserRole(request,getLocale(request));
    }

    @RequestMapping(value = "/roles/update", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public @ResponseBody RoleResponse updateRole(@Valid @RequestBody UpdateRoleRequest request) {
        return roleUserManagementService.updateRole(request,getLocale(request));
    }

    @RequestMapping(value = "/roles/delete", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public @ResponseBody RoleResponse deleteRole(@RequestBody @Valid DeleteRoleRequest request) {
        return roleUserManagementService.deleteRole(request,getLocale(request));
    }


    @RequestMapping(value = "/users/all", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity<PaginatedResponse<BasicUserDetails>> getAllMerchantRegistrations(@Valid @RequestBody GetAllUsersRequestFilter request) {

        PaginatedResponse<BasicUserDetails> response = roleUserManagementService.getAllUserBasicInfo(request);
        return ResponseEntity.ok(response);
    }
}
