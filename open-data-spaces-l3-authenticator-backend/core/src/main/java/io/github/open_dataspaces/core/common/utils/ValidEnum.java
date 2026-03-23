/*
 * ValidEnum.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is part of the user authentication module. ValidEnum defines a custom annotation for
 * validating enum values.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.common.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.github.open_dataspaces.core.common.consts.ConstError;

/**
 * Custom annotation for validating that a string value matches a constant in the specified enum
 * class.
 *
 * <p>Used in conjunction with {@link EnumValidator} to ensure that a field or parameter value is a
 * valid enum constant.</p>
 */
@Documented
@Constraint(validatedBy = EnumValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnum {

    /**
     * The default validation error message.
     */
    String message() default ConstError.ERR_VALIDATION_ENUM_DEFAULT;

    /**
     * Allows specification of validation groups.
     */
    Class<?>[] groups() default {};

    /**
     * Allows specification of custom payload objects.
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * Specifies the enum class to validate against.
     */
    Class<? extends Enum<?>> enumClass();
}
