/*
 * EnumValidatorTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the EnumValidator class.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import io.github.open_dataspaces.core.common.enums.EnumAPIKeyAttributes;

import jakarta.validation.ConstraintValidatorContext;

/**
 * Unit tests for the {@link EnumValidator} class.
 */
class EnumValidatorTest {

    private EnumValidator validator;
    private ConstraintValidatorContext context;
    private ValidEnum annotation;

    @BeforeEach
    void setUp() {
        validator = new EnumValidator();
        context = Mockito.mock(ConstraintValidatorContext.class);
        annotation = new ValidEnum() {
            @Override
            public Class<? extends Enum<?>> enumClass() {
                return EnumAPIKeyAttributes.class;
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return ValidEnum.class;
            }

            @Override
            public String message() {
                return "Invalid enum value";
            }

            @Override
            public Class<?>[] groups() {
                return new Class<?>[0];
            }

            @SuppressWarnings("unchecked")
            @Override
            public Class<? extends jakarta.validation.Payload>[] payload() {
                return new Class[0];
            }
        };
    }

    /**
     * (Case#1) Tests the initialize method.
     */
    @Test
    void testInitialize() {
        // Act
        validator.initialize(annotation);

        // Assert
        assertNotNull(validator, "Validator should be initialized");
    }

    /**
     * (Case#2) Tests the isValid method with valid enum values.
     */
    @Test
    void testIsValid_withValidEnumValues() {
        // Act
        validator.initialize(annotation);

        // Assert
        assertTrue(validator.isValid("DATASPACE", context), "DATASPACE should be valid");
        assertTrue(validator.isValid("APPLICATION", context), "APPLICATION should be valid");
        assertTrue(validator.isValid("TRACEABILITY", context), "TRACEABILITY should be valid");
    }

    /**
     * (Case#3) Tests the isValid method with invalid enum values.
     */
    @Test
    void testIsValid_withInvalidEnumValues() {
        // Act
        validator.initialize(annotation);

        // Assert
        assertFalse(validator.isValid("INVALID", context), "INVALID should not be valid");
        assertFalse(validator.isValid("UNKNOWN", context), "UNKNOWN should not be valid");
    }

    /**
     * (Case#4) Tests the isValid method with null values.
     */
    @Test
    void testIsValid_withNullValue() {
        // Act
        validator.initialize(annotation);

        // Assert
        assertTrue(validator.isValid(null, context), "Null value should be valid");
    }
}