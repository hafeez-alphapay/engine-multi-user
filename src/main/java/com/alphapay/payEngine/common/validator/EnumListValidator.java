package com.alphapay.payEngine.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumListValidator implements ConstraintValidator<ValidEnumList, List<String>> {

    private Set<String> validEnumNames;

    @Override
    public void initialize(ValidEnumList constraintAnnotation) {
        Class<? extends Enum<?>> enumClass = constraintAnnotation.enumClass();
        validEnumNames = Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(List<String> values, ConstraintValidatorContext context) {
        if (values == null || values.isEmpty()) {
            return true;
        }
        return validEnumNames.containsAll(values);
    }
}