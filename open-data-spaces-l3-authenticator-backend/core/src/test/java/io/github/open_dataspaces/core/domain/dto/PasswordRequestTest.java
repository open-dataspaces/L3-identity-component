/*
 * PasswordRequestTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This package contains unit tests for the PasswordRequest class.
 *
 * Date: 2025/08/31
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.open_dataspaces.core.common.consts.Const;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for PasswordRequest.
 */
class PasswordRequestTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    /**
     * Provides test cases for validation.
     */
    static Stream<TestCase> validationTestCases() {
        return Stream.of(
            // Valid inputs
            new TestCase("valid-input", "OldPassword1!", "NewPassword1!", 0),
            new TestCase("oldPassword-short", "OldPa!50", "NewPassword1!", 0),
            new TestCase("oldPassword-long", "OldPassword12345678$", "NewPassword1!", 0),
            new TestCase("newPassword-short", "OldPassword1!", "NewPa!50", 0),
            new TestCase("newPassword-long", "OldPassword1!", "!NewPassword12345678", 0),
            new TestCase("oldPassword-special", "Pass1!@#$%^&*()", "NewPassword1!", 0),
            new TestCase("newPassword-special", "OldPassword1!", "Pass1!@#$%^&*()", 0),
            new TestCase("oldPassword-blank", "", "NewPassword1!", 0),

            // Invalid oldPassword
            new TestCase("oldPassword-null", null, "NewPassword1!", 1),
            new TestCase("oldPassword-too-long", "Long1Long2Long3Long%4", "NewPassword1!", 1),
            new TestCase("oldPassword-invalid-pattern1", "password(1)", "NewPassword1!", 1),
            new TestCase("oldPassword-invalid-pattern2", "PASSWORD(9)", "NewPassword1!", 1),
            new TestCase("oldPassword-invalid-pattern3", "PASSwORD(&)", "NewPassword1!", 1),
            new TestCase("oldPassword-invalid-pattern4", "PASSwORD0", "NewPassword1!", 1),
            new TestCase("oldPassword-invalid-special", "Pass1!@#$%^&*_)", "NewPassword1!", 1),

            // Invalid newPassword
            new TestCase("newPassword-blank", "OldPassword1!", "", 3),
            new TestCase("newPassword-null", "OldPassword1!", null, 1),
            new TestCase("newPassword-too-short", "OldPassword1!", "Short1!", 1),
            new TestCase("newPassword-too-long", "OldPassword1!", "Long1Long2Long3Long%4", 1),
            new TestCase("newPassword-invalid-pattern1", "OldPassword1!", "password(1)", 1),
            new TestCase("newPassword-invalid-pattern2", "OldPassword1!", "PASSWORD(9)", 1),
            new TestCase("newPassword-invalid-pattern3", "OldPassword1!", "PASSwORD(&)", 1),
            new TestCase("newPassword-invalid-pattern4", "OldPassword1!", "PASSwORD0", 1),
            new TestCase("newPassword-invalid-special", "OldPassword1!", "Pass1!@#$%^&*?)", 1)
        );
    }

    /**
     * Parameterized test for validation.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("validationTestCases")
    @DisplayName("Validation handles various scenarios")
    void validation_handlesVariousScenarios(TestCase tc) {
        // Arrange
        PasswordRequest request = new PasswordRequest(tc.oldPassword, tc.newPassword);

        // Act
        Set<ConstraintViolation<PasswordRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertEquals(tc.expectedViolationCount, violations.size(), "Unexpected number of validation violations.");
        if (tc.expectedViolationCount > 0) {
            assertTrue(violations.stream().allMatch(v -> v.getMessage() != null), "Validation messages should not be null.");
        } else {
            assertEquals(tc.newPassword, request.getNewPassword(), "New password should match the input.");
            assertEquals(tc.oldPassword, request.getOldPassword(), "Old password should match the input.");
        }
    }

    /**
     * Test setter method.
     */
    @Test
    @DisplayName("Test Setter method")
    void testSetter() {
        PasswordRequest request = new PasswordRequest("^oldPassword0", "#newPassword9");
        request.setOldPassword("^newOldPassword0");
        request.setNewPassword("#newNewPassword9");

        assertEquals("^newOldPassword0", request.getOldPassword(), "The oldPassword should match the input value.");
        assertEquals("#newNewPassword9", request.getNewPassword(), "The newPassword should match the input value.");
    }

    /**
     * Tests whether the @JsonProperty annotation of PasswordRequest is set correctly.
     */
    @Test
    @DisplayName("Test JsonProperty annotations")
    void testJsonPropertyNames() throws Exception {
        Field fieldOldPassword = PasswordRequest.class.getDeclaredField("oldPassword");
        Field fieldNewPassword = PasswordRequest.class.getDeclaredField("newPassword");
        JsonProperty annotationOldPassword = fieldOldPassword.getAnnotation(JsonProperty.class);
        JsonProperty annotationNewPassword = fieldNewPassword.getAnnotation(JsonProperty.class);
        assertNotNull(annotationOldPassword);
        assertEquals(Const.JSON_PROPERTY_OLD_PASSWORD, annotationOldPassword.value());
        assertEquals(Const.JSON_PROPERTY_NEW_PASSWORD, annotationNewPassword.value());
    }

    /**
     * Test toString method.
     */
    @Test
    @DisplayName("Test toString method")
    void testToString() {
        PasswordRequest request = new PasswordRequest("^oldPassword0", "#newPassword9");

        String toStringResult = request.toString();

        assertNotNull(toStringResult, "toString should not return null.");
        assertTrue(toStringResult.contains("^oldPassword0"), "toString should include oldPassword.");
        assertTrue(toStringResult.contains("#newPassword9"), "toString should include newPassword.");
    }

    /**
     * Test equals and hashCode methods.
     */
    @Test
    @DisplayName("Test equals and hashCode methods")
    void testEqualsAndHashCode() {
        PasswordRequest request1 = new PasswordRequest("^oldPassword0", "#newPassword9");

        PasswordRequest request2 = new PasswordRequest("^oldPassword0", "#newPassword9");

        PasswordRequest request3 = new PasswordRequest("!oldPassword0", "#newPassword9");

        PasswordRequest request4 = new PasswordRequest("^oldPassword0", "*newPassword9");

        assertEquals(request1, request2, "Objects with the same fields should be equal.");
        assertEquals(request1.hashCode(), request2.hashCode(), "Hash codes should match for equal objects.");
        assertNotEquals(request1, request3, "Objects with different fields should not be equal.");
        assertNotEquals(request1, request4, "Objects with different fields should not be equal.");
    }

    /**
     * Simple holder for parameterized test data.
     */
    private static class TestCase {
        final String name;
        final String oldPassword;
        final String newPassword;
        final int expectedViolationCount;

        TestCase(String name, String oldPassword, String newPassword, int expectedViolationCount) {
            this.name = name;
            this.oldPassword = oldPassword;
            this.newPassword = newPassword;
            this.expectedViolationCount = expectedViolationCount;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
