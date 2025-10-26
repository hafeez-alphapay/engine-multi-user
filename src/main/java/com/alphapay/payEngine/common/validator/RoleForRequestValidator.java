package com.alphapay.payEngine.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class RoleForRequestValidator implements ConstraintValidator<ValidRoleForRequest, String> {

    @Override
    public boolean isValid(String role, ConstraintValidatorContext context) {
        if (role == null) {
            return true; // Skip validation if values are null
        }

        return List.of("SALES", "MANAGER", "ADMIN").contains(role.toUpperCase());
    }
}