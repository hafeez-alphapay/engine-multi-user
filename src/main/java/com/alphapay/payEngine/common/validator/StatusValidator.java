package com.alphapay.payEngine.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class StatusValidator implements ConstraintValidator<ValidStatus, String> {

    private static final List<String> VALID_STATUSES = Arrays.asList("APPROVED", "DECLINED", "PENDING","ACTIVE","INACTIVE");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false; // Null is not valid
        }
        return VALID_STATUSES.contains(value.toUpperCase());
    }
}
