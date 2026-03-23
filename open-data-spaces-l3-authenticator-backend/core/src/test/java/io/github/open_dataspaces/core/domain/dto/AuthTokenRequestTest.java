/*
 * AuthTokenRequestTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies the behavior and validation logic
 * of the AuthTokenRequest DTO.
 *
 * Date: 2025-08-31
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.open_dataspaces.core.common.consts.Const;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * AuthTokenRequestTest is a test class for the AuthTokenRequest.
 */
public class AuthTokenRequestTest {

    private Validator validator;

    /**
     * setUpValidator initializes the Validator instance before all tests.
     */
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * case#1:
     * testJsonProperty tests the JSON serialization and deserialization of
     * AuthTokenRequest.
     */
    @Test
    void testJsonProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        AuthTokenRequest request = new AuthTokenRequest();
        request.setClientId("client123");
        request.setRedirectUri("https://example.com/callback");
        request.setCodeVerifier("verifier123");
        request.setCode("authcode123");
        request.setClientSecret("secret123");

        String inputJson = "{"
                + "\"" + Const.JSON_PROPERTY_CODE + "\":\"authcode123\","
                + "\"" + Const.JSON_PROPERTY_CLIENT_ID + "\":\"client123\","
                + "\"" + Const.JSON_PROPERTY_CLIENT_SECRET + "\":\"secret123\","
                + "\"" + Const.JSON_PROPERTY_REDIRECT_URI + "\":\"https://example.com/callback\","
                + "\"" + Const.JSON_PROPERTY_CODE_VERIFIER + "\":\"verifier123\""
                + "}";

        // Serialize
        String json = mapper.writeValueAsString(request);
        assertEquals(mapper.readTree(inputJson), mapper.readTree(json));

        // Deserialize
        AuthTokenRequest input = mapper.readValue(inputJson, AuthTokenRequest.class);

        // Assert
        assertEquals(request.getClientId(), input.getClientId());
        assertEquals(request.getRedirectUri(), input.getRedirectUri());
        assertEquals(request.getCodeVerifier(), input.getCodeVerifier());
        assertEquals(request.getCode(), input.getCode());
        assertEquals(request.getClientSecret(), input.getClientSecret());
    }

    /**
     * case#2:
     * testEqualsAndHashCode tests the equals and hashCode methods of
     * AuthTokenRequest.
     */
    @Test
    void testEqualsAndHashCode() {
        // Test basic equality
        AuthTokenRequest req1 = new AuthTokenRequest();
        req1.setClientId("client123");
        req1.setRedirectUri("http://example.com");
        req1.setCodeVerifier("verifier123");
        req1.setCode("code123");
        req1.setClientSecret("secret123");

        AuthTokenRequest req2 = new AuthTokenRequest();
        req2.setClientId("client123");
        req2.setRedirectUri("http://example.com");
        req2.setCodeVerifier("verifier123");
        req2.setCode("code123");
        req2.setClientSecret("secret123");

        // Test equality
        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());

        // Test inequality - different clientId
        req2.setClientId("differentClient");
        assertNotEquals(req1, req2);

        // Test inequality - different redirectUri
        req2.setClientId("client123");
        req2.setRedirectUri("http://different.com");
        assertNotEquals(req1, req2);

        // Test inequality - different codeVerifier
        req2.setRedirectUri("http://example.com");
        req2.setCodeVerifier("differentVerifier");
        assertNotEquals(req1, req2);

        // Test inequality - different code
        req2.setCodeVerifier("verifier123");
        req2.setCode("differentCode");
        assertNotEquals(req1, req2);

        // Test inequality - different clientSecret
        req2.setCode("code123");
        req2.setClientSecret("differentSecret");
        assertNotEquals(req1, req2);

        // Test null comparison
        assertNotEquals(req1, null);

        // Test different class comparison
        assertNotEquals(req1, "not an AuthTokenRequest");

        // Test self comparison
        assertEquals(req1, req1);
    }

    /**
     * case#3:
     * testToString tests the toString method of AuthTokenRequest.
     */
    @Test
    void testToString() {
        AuthTokenRequest request = new AuthTokenRequest();
        request.setClientId("testClient");
        request.setRedirectUri("http://test.com");
        request.setCodeVerifier("testVerifier");
        request.setCode("testCode");
        request.setClientSecret("testSecret");

        String str = request.toString();

        // Verify toString contains field values
        assertTrue(str.contains("clientId=testClient"));
        assertTrue(str.contains("redirectUri=http://test.com"));
        assertTrue(str.contains("codeVerifier=testVerifier"));
        assertTrue(str.contains("code=testCode"));
        assertTrue(str.contains("clientSecret=testSecret"));

        // Test with null values
        request.setClientId(null);
        request.setRedirectUri(null);
        request.setCodeVerifier(null);
        request.setCode(null);
        request.setClientSecret(null);

        String strWithNulls = request.toString();
        assertTrue(strWithNulls.contains("clientId=null"));
        assertTrue(strWithNulls.contains("redirectUri=null"));
        assertTrue(strWithNulls.contains("codeVerifier=null"));
        assertTrue(strWithNulls.contains("code=null"));
        assertTrue(strWithNulls.contains("clientSecret=null"));
    }

    /**
     * testCodeValidation tests the validation logic
     * for various code values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
            // code, expectErrorMessage, isValid
            "authcode123,, true", // case#4: valid code
            "A,, true", // case#5: code is min length 1 character
            "'  ', %s: cannot be blank, false", // case#6: code is blank spaces
            "NULL, %s: cannot be blank, false", // case#7: code is null
            "'', %s: cannot be blank, false" // case#8: code is empty
    }, nullValues = "NULL")
    void testCodeValidation(String code, String errStr, boolean isValid) {
        AuthTokenRequest request = new AuthTokenRequest();
        // Set value
        request.setClientId("client123");
        request.setRedirectUri("http://test.com");
        request.setCodeVerifier("testVerifier");
        request.setCode(code); // test item
        request.setClientSecret("testSecret");

        // Assert
        Set<ConstraintViolation<AuthTokenRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty(), "Expected no violations for valid code: " + code);
        } else {
            assertFalse(violations.isEmpty(), "Expected violations for invalid code: " + code);
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * testClientIdValidation tests the validation logic
     * for various clientId values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
            // clientId, expectErrorMessage, isValid
            "client123,, true", // case#9: valid clientId
            "A,, true", // case#10: clientId is min length 1 character
            "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789,, true", // case#11: clientId is max length 239 characters
            "'  ', %s: cannot be blank, false", // case#12: clientId is blank spaces
            "NULL, %s: cannot be blank, false", // case#13: clientId is null
            "'', %s: cannot be blank, false", // case#14: clientId is empty
            "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890, %s: the length must be between 1 and 239, false" // case#15: clientId is too long
    }, nullValues = "NULL")
    void testClientIdValidation(String clientId, String errStr, boolean isValid) {
        AuthTokenRequest request = new AuthTokenRequest();
        // Set value
        request.setClientId(clientId); // test item
        request.setRedirectUri("http://test.com");
        request.setCodeVerifier("testVerifier");
        request.setCode("testCode");
        request.setClientSecret("testSecret");

        // Assert
        Set<ConstraintViolation<AuthTokenRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty(), "Expected no violations for valid clientId: " + clientId);
        } else {
            assertFalse(violations.isEmpty(), "Expected violations for invalid clientId: " + clientId);
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * testClientSecretValidation tests the validation logic
     * for various clientSecret values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
            // clientSecret, expectErrorMessage, isValid
            "secret123,, true", // case#16: valid clientSecret
            "A,, true", // case#17: clientSecret is min length 1 character
            "clientSecret012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012,, true", // case#18: clientSecret is max length 255 characters
            "'  ', %s: cannot be blank, false", // case#19: clientSecret is blank spaces
            "NULL, %s: cannot be blank, false", // case#20: clientSecret is null
            "'', %s: cannot be blank, false", // case#21: clientSecret is empty
            "clientSecret0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123, %s: the length must be between 1 and 255, false" // case#22: clientSecret is more characters
    }, nullValues = "NULL")
    void testClientSecretValidation(String clientSecret, String errStr, boolean isValid) {
        AuthTokenRequest request = new AuthTokenRequest();
        // Set value
        request.setClientId("client123");
        request.setRedirectUri("http://test.com");
        request.setCodeVerifier("testVerifier");
        request.setCode("testCode");
        request.setClientSecret(clientSecret); // test item

        // Assert
        Set<ConstraintViolation<AuthTokenRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty(), "Expected no violations for valid clientSecret: " + clientSecret);
        } else {
            assertFalse(violations.isEmpty(), "Expected violations for invalid clientSecret: " + clientSecret);
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * testRedirectUriValidation tests the validation logic
     * for various redirectUri values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
            // redirectUri, expectErrorMessage, isValid
            "https://example.com/callback,, true", // case#23: valid redirectUri
            "http://localhost:8080/callback,, true", // case#24: valid localhost
            "A,, true", // case#25: redirectUri is min length 1 character
            "redirectUri0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123,, true", // case#26: redirectUri is max length 255 characters
            "'  ', %s: cannot be blank, false", // case#27: redirectUri is blank spaces
            "NULL, %s: cannot be blank, false", // case#28: redirectUri is null
            "'', %s: cannot be blank, false", // case#29: redirectUri is empty
            "redirectUri01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234, %s: the length must be between 1 and 255, false" // case#30: redirectUri is more characters
    }, nullValues = "NULL")
    void testRedirectUriValidation(String redirectUri, String errStr, boolean isValid) {
        AuthTokenRequest request = new AuthTokenRequest();
        // Set value
        request.setClientId("testClient");
        request.setRedirectUri(redirectUri); // test item
        request.setCodeVerifier("testVerifier");
        request.setCode("testCode");
        request.setClientSecret("testSecret");

        // Assert
        Set<ConstraintViolation<AuthTokenRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty(), "Expected no violations for valid redirectUri: " + redirectUri);
        } else {
            assertFalse(violations.isEmpty(), "Expected violations for invalid redirectUri: " + redirectUri);
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * testCodeVerifierValidation tests the validation logic
     * for various codeVerifier values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
            // codeVerifier, expectErrorMessage, isValid
            "verifier123,, true", // case#31: valid codeVerifier
            "A,, true", // case#32: codeVerifier is min length 1 character
            "'  ', %s: cannot be blank, false", // case#33: codeVerifier is blank spaces
            "NULL, %s: cannot be blank, false", // case#34: codeVerifier is null
            "'', %s: cannot be blank, false" // case#35: codeVerifier is empty characters
    }, nullValues = "NULL")
    void testCodeVerifierValidation(String codeVerifier, String errStr, boolean isValid) {
        AuthTokenRequest request = new AuthTokenRequest();
        // Set value
        request.setClientId("client123");
        request.setRedirectUri("http://test.com");
        request.setCodeVerifier(codeVerifier); // test item
        request.setCode("testCode");
        request.setClientSecret("secret123");

        // Assert
        Set<ConstraintViolation<AuthTokenRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty(), "Expected no violations for valid codeVerifier: " + codeVerifier);
        } else {
            assertFalse(violations.isEmpty(), "Expected violations for invalid codeVerifier: " + codeVerifier);
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }
}