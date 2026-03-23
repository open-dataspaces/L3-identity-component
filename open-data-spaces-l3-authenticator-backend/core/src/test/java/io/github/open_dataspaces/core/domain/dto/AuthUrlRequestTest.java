/*
 * AuthUrlRequestTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies the behavior and validation logic
 * of the AuthUrlRequest DTO.
 *
 * Date: 2025-08-31
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
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
 * AuthUrlRequestTest is a test class for the AuthUrlRequest.
 */
public class AuthUrlRequestTest {

    private static Validator validator;

    /**
     * setUpValidator initializes the Validator instance before all tests.
     */
    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * case#1:
     * testGetterSetter tests the getter and setter methods of AuthUrlRequest.
     */
    @Test
    void testGetterSetter() {
        AuthUrlRequest request = new AuthUrlRequest();
        String clientId = "client123";
        String redirectUri = "https://example.com/callback";
        String codeChallenge = "challenge123";

        request.setClientId(clientId);
        request.setRedirectUri(redirectUri);
        request.setCodeChallenge(codeChallenge);

        assertEquals(clientId, request.getClientId());
        assertEquals(redirectUri, request.getRedirectUri());
        assertEquals(codeChallenge, request.getCodeChallenge());
    }

    /**
     * case#2:
     * testJsonProperty tests the JSON serialization and deserialization of AuthUrlRequest.
     */
    @Test
    void testJsonProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        AuthUrlRequest request = new AuthUrlRequest();
        request.setClientId("client123");
        request.setRedirectUri("https://example.com/callback");
        request.setCodeChallenge("challenge123");

        String inputJson = "{"
                + "\"" + Const.JSON_PROPERTY_CLIENT_ID + "\":\"client123\","
                + "\"" + Const.JSON_PROPERTY_REDIRECT_URI + "\":\"https://example.com/callback\","
                + "\"" + Const.JSON_PROPERTY_CODE_CHALLENGE + "\":\"challenge123\""
                + "}";

        // Serialize
        String json = mapper.writeValueAsString(request);
        assertEquals(mapper.readTree(inputJson), mapper.readTree(json));

        // Deserialize
        AuthUrlRequest input = mapper.readValue(inputJson, AuthUrlRequest.class);

        // Assert
        assertEquals(request.getClientId(), input.getClientId());
        assertEquals(request.getRedirectUri(), input.getRedirectUri());
        assertEquals(request.getCodeChallenge(), input.getCodeChallenge());
    }

    /**
     * case#3:
     * testEqualsAndHashCode tests the equals and hashCode methods of AuthUrlRequest.
     */
    @Test
    void testEqualsAndHashCode() {
        // Test basic equality
        AuthUrlRequest req1 = new AuthUrlRequest();
        req1.setClientId("client123");
        req1.setRedirectUri("http://example.com");
        req1.setCodeChallenge("challenge123");

        AuthUrlRequest req2 = new AuthUrlRequest();
        req2.setClientId("client123");
        req2.setRedirectUri("http://example.com");
        req2.setCodeChallenge("challenge123");

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

        // Test inequality - different codeChallenge
        req2.setRedirectUri("http://example.com");
        req2.setCodeChallenge("differentChallenge");
        assertNotEquals(req1, req2);

        // Test null comparison
        assertNotEquals(req1, null);

        // Test different class comparison
        assertNotEquals(req1, "not an AuthUrlRequest");

        // Test self comparison
        assertEquals(req1, req1);
    }

    /**
     * case#4:
     * testToString tests the toString method of AuthUrlRequest.
     */
    @Test
    void testToString() {
        AuthUrlRequest request = new AuthUrlRequest();
        request.setClientId("testClient");
        request.setRedirectUri("http://test.com");
        request.setCodeChallenge("testChallenge");

        String str = request.toString();

        // Verify toString contains field values
        assertTrue(str.contains("clientId=testClient"));
        assertTrue(str.contains("redirectUri=http://test.com"));
        assertTrue(str.contains("codeChallenge=testChallenge"));

        // Test with null values
        request.setClientId(null);
        request.setRedirectUri(null);
        request.setCodeChallenge(null);

        String strWithNulls = request.toString();
        assertTrue(strWithNulls.contains("clientId=null"));
        assertTrue(strWithNulls.contains("redirectUri=null"));
        assertTrue(strWithNulls.contains("codeChallenge=null"));
    }

    /**
     * testClientIdValidation tests the validation logic
     * for various clientId values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
            // clientId, expectErrorMessage, isValid
            "client123,, true", // case#5: valid clientId
            "A,, true", // case#6: clientId is min length 1 character
            "clientmaxt1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789,, true", // case#7: clientId is max length 239 characters
            "'  ', %s: cannot be blank, false", // case#8: clientId is blank spaces
            "NULL, %s: cannot be blank, false", // case#9: clientId is null
            "'', %s: cannot be blank, false", // case#10: clientId is empty
            "clientmaxt12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890, %s: the length must be between 1 and 239, false" // case#11: clientId is more than max length 240 characters
    }, nullValues = "NULL")
    void testClientIdValidation(String clientId, String errStr, boolean isValid) {
        AuthUrlRequest request = new AuthUrlRequest();
        // Set value
        request.setClientId(clientId); // test item
        request.setRedirectUri("http://test.com");
        request.setCodeChallenge("testChallenge");

        // Assert
        Set<ConstraintViolation<AuthUrlRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty(), "Expected no violations for valid clientId: " + clientId);
        } else {
            assertFalse(violations.isEmpty(), "Expected violations for invalid clientId: " + clientId);
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
            "https://example.com/callback,, true", // case#12: valid redirectUri
            "http://localhost:8080/callback,, true", // case#13: valid localhost
            "A,, true", // case#14: redirectUri is min length 1 character
            "redirectUri0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123,, true", // case#15: redirectUri is max length 255 character
            "'  ', %s: cannot be blank, false", // case#16: redirectUri is blank spaces
            "NULL, %s: cannot be blank, false", // case#17: redirectUri is null
            "'', %s: cannot be blank, false", // case#18: redirectUri is empty
            "redirectUri01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234, %s: the length must be between 1 and 255, false" // case#19: redirectUri is more than max length 256 characters
    }, nullValues = "NULL")
    void testRedirectUriValidation(String redirectUri, String errStr, boolean isValid) {
        AuthUrlRequest request = new AuthUrlRequest();
        // Set value
        request.setClientId("client123");
        request.setRedirectUri(redirectUri); // test item
        request.setCodeChallenge("testChallenge");

        // Assert
        Set<ConstraintViolation<AuthUrlRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty(), "Expected no violations for valid redirectUri: " + redirectUri);
        } else {
            assertFalse(violations.isEmpty(), "Expected violations for invalid redirectUri: " + redirectUri);
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * testCodeChallengeValidation tests the validation logic
     * for various codeChallenge values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
            // codeChallenge, expectErrorMessage, isValid
            "challenge123,, true", // case#20: valid codeChallenge
            "A,, true", // case#21: codeChallenge is 1 character
            "very-long-code-challenge-with-many-characters-123456789012345678901234567890,, true", // case#22: long value (no size constraint)
            "'  ', %s: cannot be blank, false", // case#23: codeChallenge is blank spaces
            "NULL, %s: cannot be blank, false", // case#24: codeChallenge is null
            "'', %s: cannot be blank, false" // case#25: codeChallenge is empty
    }, nullValues = "NULL")
    void testCodeChallengeValidation(String codeChallenge, String errStr, boolean isValid) {
        AuthUrlRequest request = new AuthUrlRequest();
        // Set value
        request.setClientId("client123");
        request.setRedirectUri("http://test.com");
        request.setCodeChallenge(codeChallenge); // test item

        // Assert
        Set<ConstraintViolation<AuthUrlRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty(), "Expected no violations for valid codeChallenge: " + codeChallenge);
        } else {
            assertFalse(violations.isEmpty(), "Expected violations for invalid codeChallenge: " + codeChallenge);
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }
}