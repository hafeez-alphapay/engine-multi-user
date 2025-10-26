package com.alphapay.payEngine.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target( { METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = RoleForRequestValidator.class)
@Documented
public @interface ValidRoleForRequest {

    String message() default "Invalid role for the specified type of request.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}