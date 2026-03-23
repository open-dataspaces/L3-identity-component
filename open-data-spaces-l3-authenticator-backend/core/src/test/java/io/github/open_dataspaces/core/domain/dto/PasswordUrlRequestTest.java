/*
 * PasswordUrlRequestTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This package contains unit tests for the PasswordUrlRequest class.
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.open_dataspaces.core.common.consts.Const;

/**
 * Unit tests for PasswordUrlRequest.
 */
class PasswordUrlRequestTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    /**
     * Provides test cases for validation.
     */
    static Stream<TestCase> validationTestCases() {
        return Stream.of(
            // Valid input
            new TestCase("valid-input", "valid-client-id", "http://valid.redirect.uri", "abc123#", 0, ""),
            new TestCase("clientId-too-short", "a", "http://valid.redirect.uri", "abc123#", 0, ""),
            new TestCase("redirectUri-too-short", "valid-client-id", "a", "abc123#", 0, ""),
            new TestCase("clientId-too-long", "a".repeat(Const.CLIENT_ID_LENGTH_MAX), "http://valid.redirect.uri", "abc123#", 0, ""),
            new TestCase("redirectUri-too-long", "valid-client-id", "a".repeat(Const.CLIENT_ID_LENGTH_MAX), "abc123#", 0, ""),

            // Invalid clientId
            new TestCase("clientId-blank", "", "http://valid.redirect.uri", "abc123#", 2, "%s: cannot be blank"),
            new TestCase("clientId-null", null, "http://valid.redirect.uri", "abc123#", 1, "%s: cannot be blank"),
            new TestCase("clientId-too-long", "a".repeat(Const.CLIENT_ID_LENGTH_MAX + 1), "http://valid.redirect.uri", "abc123#", 1, "%s: the length must be between " + Const.CLIENT_ID_LENGTH_MIN + " and " + Const.CLIENT_ID_LENGTH_MAX),

            // Invalid redirectUri
            new TestCase("redirectUri-blank", "valid-client-id", "", "abc123#", 2, "%s: cannot be blank"),
            new TestCase("redirectUri-null", "valid-client-id", null, "abc123#", 1, "%s: cannot be blank"),
            new TestCase("redirectUri-too-long", "valid-client-id", "a".repeat(Const.REDIRECT_URI_LENGTH_MAX + 1), "abc123#", 1, "%s: the length must be between " + Const.REDIRECT_URI_LENGTH_MIN + " and " + Const.REDIRECT_URI_LENGTH_MAX),

            // Invalid codeChallenge
            new TestCase("codeChallenge-blank", "valid-client-id", "http://valid.redirect.uri", "", 1, "%s: cannot be blank"),
            new TestCase("codeChallenge-blank", "valid-client-id", "http://valid.redirect.uri", null, 1, "%s: cannot be blank")
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
        PasswordUrlRequest request = new PasswordUrlRequest(tc.clientId, tc.redirectUri, tc.codeChallenge);

        // Act
        Set<ConstraintViolation<PasswordUrlRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertEquals(tc.expectedViolationCount, violations.size(), "Unexpected number of validation violations.");
        if (tc.expectedViolationCount > 0) {
            assertTrue(violations.stream().allMatch(v -> v.getMessage() != null), "Validation messages should not be null.");
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains(tc.expectedMessage)), "Expected " + tc.expectedMessage + "message.");
        } else {
            assertEquals(tc.clientId, request.getClientId(), "The clientId should match the input value.");
            assertEquals(tc.redirectUri, request.getRedirectUri(), "The redirectUrl should match the input value.");
        }
    }

    /**
     * Test setter method.
     */
    @Test
    @DisplayName("Test Setter method")
    void testSetter() {
        PasswordUrlRequest request = new PasswordUrlRequest("client-id", "http://redirect.uri", "abc123#");
        request.setClientId("new-client-id");
        request.setRedirectUri("http://new.redirect.uri");
        request.setCodeChallenge("newCodeChallenge");

        assertEquals("new-client-id", request.getClientId(), "The clientId should match the input value.");
        assertEquals("http://new.redirect.uri", request.getRedirectUri(), "The redirectUrl should match the input value.");
        assertEquals("newCodeChallenge", request.getCodeChallenge(), "The codeChallenge should match the input value.");
    }

    /**
     * Tests whether the @JsonProperty annotation of TokenRefreshRequest is set correctly.
     */
    @Test
    void testJsonPropertyNames() throws Exception {
        Field fieldClientId = PasswordUrlRequest.class.getDeclaredField("clientId");
        Field fieldRedirectUrl = PasswordUrlRequest.class.getDeclaredField("redirectUri");
        Field fieldCodeChallenge = PasswordUrlRequest.class.getDeclaredField("codeChallenge");
        JsonProperty annotationClientId = fieldClientId.getAnnotation(JsonProperty.class);
        JsonProperty annotationRedirectUrl = fieldRedirectUrl.getAnnotation(JsonProperty.class);
        JsonProperty annotationCodeChallenge = fieldCodeChallenge.getAnnotation(JsonProperty.class);
        assertNotNull(annotationClientId);
        assertEquals(Const.JSON_PROPERTY_CLIENT_ID, annotationClientId.value());
        assertEquals(Const.JSON_PROPERTY_REDIRECT_URI, annotationRedirectUrl.value());
        assertEquals(Const.JSON_PROPERTY_CODE_CHALLENGE, annotationCodeChallenge.value());
    }

    /**
     * Test toString method.
     */
    @Test
    @DisplayName("Test toString method")
    void testToString() {
        PasswordUrlRequest request = new PasswordUrlRequest("client-id", "http://redirect.uri", "abc123#");

        String toStringResult = request.toString();

        assertNotNull(toStringResult, "toString should not return null.");
        assertTrue(toStringResult.contains("client-id"), "toString should include clientId.");
        assertTrue(toStringResult.contains("http://redirect.uri"), "toString should include redirectUri.");
        assertTrue(toStringResult.contains("abc123#"), "toString should include codeChallenge.");
    }

    /**
     * Test equals and hashCode methods.
     */
    @Test
    @DisplayName("Test equals and hashCode methods")
    void testEqualsAndHashCode() {
        PasswordUrlRequest request1 = new PasswordUrlRequest("client-id", "http://redirect.uri", "abc123#");

        PasswordUrlRequest request2 = new PasswordUrlRequest("client-id", "http://redirect.uri", "abc123#");

        PasswordUrlRequest request3 = new PasswordUrlRequest("different-client-id", "http://redirect.uri", "abc123#");

        PasswordUrlRequest request4 = new PasswordUrlRequest("client-id", "http://other_redirect.uri", "abc123#");

        PasswordUrlRequest request5 = new PasswordUrlRequest("client-id", "http://other_redirect.uri", "code-challenge");

        assertEquals(request1, request2, "Objects with the same fields should be equal.");
        assertEquals(request1.hashCode(), request2.hashCode(), "Hash codes should match for equal objects.");
        assertNotEquals(request1, request3, "Objects with different fields should not be equal.");
        assertNotEquals(request1, request4, "Objects with different fields should not be equal.");
        assertNotEquals(request1, request5, "Objects with different fields should not be equal.");
    }

    /**
     * Simple holder for parameterized test data.
     */
    private static class TestCase {
        final String name;
        final String clientId;
        final String redirectUri;
        final String codeChallenge;
        final int expectedViolationCount;
        final String expectedMessage;

        TestCase(String name, String clientId, String redirectUri, String codeChallenge, int expectedViolationCount, String expectedMessage) {
            this.name = name;
            this.clientId = clientId;
            this.redirectUri = redirectUri;
            this.codeChallenge = codeChallenge;
            this.expectedViolationCount = expectedViolationCount;
            this.expectedMessage = expectedMessage;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
