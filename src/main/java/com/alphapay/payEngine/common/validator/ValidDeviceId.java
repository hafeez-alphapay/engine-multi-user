package com.alphapay.payEngine.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target( { METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = {DeviceIdValidator.class})
@Documented
public @interface ValidDeviceId {
	
	String message() default "Invalid Device ID";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
