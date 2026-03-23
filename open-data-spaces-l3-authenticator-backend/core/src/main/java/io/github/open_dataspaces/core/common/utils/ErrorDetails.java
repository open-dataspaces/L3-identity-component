/*
 * ErrorDetails.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class provides utility methods to create error detail messages from validation errors.
 *
 * Date: 2025-06-30
*/

package io.github.open_dataspaces.core.common.utils;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import io.github.open_dataspaces.core.common.consts.ConstError;

import jakarta.validation.ConstraintViolation;

/**
 * Utility class for creating error details from validation errors.
 */
public class ErrorDetails {

    // Private constructor to prevent instantiation. It is for testing purposes only.
    private ErrorDetails() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    /**
     * Creates a string containing all error messages from the given BindingResult.
     *
     * @param bindingResult the BindingResult containing validation errors
     * @return a string with all error messages joined by a delimiter
     */
    @SuppressWarnings("null")
    public static String createErrorDetails(BindingResult bindingResult) {
        // return bindingResult.getAllErrors().stream().map(ObjectError::getDefaultMessage)
        //             .collect(Collectors.joining(ConstError.VALIDATION_ERROR_DELIMITER));

        return bindingResult.getAllErrors().stream()
            .map(error -> {
                if (error instanceof FieldError fieldError) {
                    return String.format(
                            fieldError.getDefaultMessage(),
                            JSONUtil.getJsonPropertyName(bindingResult.getTarget().getClass(), fieldError.getField()));     // Use JSONUtil to get the correct property name
                } else {
                    return error.getDefaultMessage();
                }
            })
            .collect(Collectors.joining(ConstError.VALIDATION_ERROR_DELIMITER));

    }

    /**
     * Creates a string containing all error messages from the given BindingResult.
     *
     * @param violations the set of ConstraintViolations containing validation errors
     * @return a string with all error messages joined by a delimiter
     */
    public static String createErrorDetails(Set<? extends ConstraintViolation<?>> violations) {

        return violations.stream()
            .map(violation -> {
                String property = violation.getPropertyPath().toString();
                String message = violation.getMessage();
                String jsonProperty = JSONUtil.getJsonPropertyName(violation.getRootBeanClass(), property);
                return String.format(message, jsonProperty);
            })
            .collect(Collectors.joining(ConstError.VALIDATION_ERROR_DELIMITER));

    }

}
