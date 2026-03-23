/*
 * DateFormatValidator.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class provides utility methods for validating date formats.
 *
 * Date: 2025/09/30
 */

package io.github.open_dataspaces.core.common.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import io.github.open_dataspaces.core.common.annotations.DateFormat;

/**
 * Validator for the @DateFormat annotation.
 */
public class DateFormatValidator implements ConstraintValidator<DateFormat, String> {
    private String pattern;

    @Override
    public void initialize(DateFormat constraintAnnotation) {
        this.pattern = constraintAnnotation.pattern();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        try {
            if (hasTimeComponent(pattern)) {
                LocalDateTime parsed = LocalDateTime.parse(value, DateTimeFormatter.ofPattern(pattern));
                return value.equals(parsed.format(DateTimeFormatter.ofPattern(pattern)));
            } else {
                LocalDate parsed = LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern));
                return value.equals(parsed.format(DateTimeFormatter.ofPattern(pattern)));
            }
        } catch (DateTimeParseException | IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean hasTimeComponent(String pattern) {
        // Pattern characters for time components
        return pattern.contains("H") || pattern.contains("h")
                || pattern.contains("m") || pattern.contains("s") || pattern.contains("S");
    }
}