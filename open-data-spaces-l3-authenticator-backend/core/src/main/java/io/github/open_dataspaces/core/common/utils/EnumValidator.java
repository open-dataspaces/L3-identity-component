/*
 * EnumValidator.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class provides a custom validator for checking if a string value matches an enum constant.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.common.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

/**
 * Custom validator for enum types.
 *
 * <p>This validator checks if a given string value matches any constant of the specified enum class.
 * Used in conjunction with the {@link ValidEnum} annotation.</p>
 */
public class EnumValidator implements ConstraintValidator<ValidEnum, String> {

    private Class<? extends Enum<?>> enumClass;

    /**
     * Initializes the validator with the target enum class.
     *
     * @param constraintAnnotation the annotation instance for a given constraint declaration
     */
    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
    }

    /**
     * Checks if the provided value is a valid enum constant.
     *
     * @param value the value to validate
     * @param context the context in which the constraint is evaluated
     * @return true if the value is null or matches an enum constant, false otherwise
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return Arrays.stream(enumClass.getEnumConstants()).anyMatch(e -> e.name().toString().equals(value));
    }
}
