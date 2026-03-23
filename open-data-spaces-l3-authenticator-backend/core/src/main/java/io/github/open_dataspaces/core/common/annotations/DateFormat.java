/*
 * DateFormat.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is used to validate date formats in request parameters.
 *
 * Date: 2025/08/30
 */

package io.github.open_dataspaces.core.common.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.validations.DateFormatValidator;

/**
 * DateFormat annotation is used to validate that a string field matches a specified date format pattern.
 * It uses the DateFormatValidator class to perform the actual validation logic.
 */
@Documented
@Constraint(validatedBy = DateFormatValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DateFormat {
    /**
     * validation error message.
     */
    String message() default ConstError.ERR_400;

    /**
     * validation date format pattern.
     * example: yyyy-MM-dd
     */
    String pattern() default Const.DATE_FORMAT;

    /**
     * validation groups
     * Used to switch the scope of validation.
     */
    Class<?>[] groups() default {};

    /**
     * validation payload
     * Used to provide additional information (metadata) when validation fails.
     */
    Class<? extends Payload>[] payload() default {};

}