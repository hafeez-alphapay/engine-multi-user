package com.alphapay.payEngine.account.roles.exception;
import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class RoleAlreadyAssignedException extends BaseWebApplicationException {

    public RoleAlreadyAssignedException() {
        super(
                400,
                "7211",
                "ex.7211.role.already.assigned",
                "Role Already Assigned",
                "Role Already Assigned to user"
        );
    }
}
