/*
 * TokenRevokeRequestTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies the behavior and validation logic
 * of the TokenRevokeRequest DTO.
 *
 * Date: 2026/02/10
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.open_dataspaces.core.common.consts.Const;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Unit tests for the TokenRevokeRequest class.
 */
public class TokenRevokeRequestTest {
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // Use ISO-8601 format

    private static Validator validator;

    private final String commonClientId = "testClientId";
    private final String commonClientSecret = "testClientSecret";
    private final String commonRefreshToken = "testRefreshToken";

    /**
     * Sets up the Validator instance before all tests.
     */
    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * Tests the default constructor of TokenRevokeRequest.
     */
    @Test
    void testDefaultConstructor() {
        TokenRevokeRequest request = new TokenRevokeRequest();

        // Verify that fields are initialized to null
        assertNull(request.getClientId());
        assertNull(request.getClientSecret());
        assertNull(request.getRefreshToken());

        Set<ConstraintViolation<TokenRevokeRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    /**
     * Tests the parameterized constructor of TokenRevokeRequest.
     */
    @Test
    void testSettersAndGetters() {
        TokenRevokeRequest entity = new TokenRevokeRequest();
        entity.setClientId(commonClientId);
        entity.setClientSecret(commonClientSecret);
        entity.setRefreshToken(commonRefreshToken);

        assertEquals(entity.getClientId(), commonClientId);
        assertEquals(entity.getClientSecret(), commonClientSecret);
        assertEquals(entity.getRefreshToken(), commonRefreshToken);
    }

    /**
     * Tests toString method of TokenRevokeRequest.
     */
    @Test
    void testToString() {
        TokenRevokeRequest dtoObject = new TokenRevokeRequest();
        String result = dtoObject.toString();

        assertTrue(result.contains("clientId"), "toString should include clientId");
        assertTrue(result.contains("clientSecret"), "toString should include clientSecret");
        assertTrue(result.contains("refreshToken"), "toString should include refreshToken");
    }

    /**
     * Tests equals and hashCode methods of TokenRevokeRequest.
     */
    @Test
    void testEqualsAndHashCode() {
        TokenRevokeRequest dtoObject1 = new TokenRevokeRequest();
        TokenRevokeRequest dtoObject2 = new TokenRevokeRequest();
        TokenRevokeRequest dtoObject3 = new TokenRevokeRequest();
        dtoObject3.setClientId(commonClientId);
        dtoObject3.setClientSecret(commonClientSecret);
        dtoObject3.setRefreshToken(commonRefreshToken);

        assertEquals(dtoObject1, dtoObject2, "Objects with same values should be equal");
        assertEquals(dtoObject1.hashCode(), dtoObject2.hashCode(), "Hash codes should be equal for equal objects");

        assertNotEquals(dtoObject1, dtoObject3, "Objects with different values should not be equal");
        assertNotEquals(dtoObject1.hashCode(), dtoObject3.hashCode(), "Hash codes should differ for non-equal objects");
    }

    /**
     * Tests JSON serialization of TokenRevokeRequest.
     *
     * @throws Exception if serialization fails
     */
    @Test
    void testJsonSerialization() throws Exception {
        TokenRevokeRequest dtoObject = new TokenRevokeRequest();
        // Act
        String json = objectMapper.writeValueAsString(dtoObject);
        // Assert
        assertTrue(json.contains(Const.JSON_PROPERTY_CLIENT_ID));
        assertTrue(json.contains(Const.JSON_PROPERTY_CLIENT_SECRET));
        assertTrue(json.contains(Const.JSON_PROPERTY_REFRESH_TOKEN));
    }

    /**
     * Tests valid parameters for TokenRevokeRequest using parameterized tests.
     *
     * @param propertyName The name of the property being tested (e.g., "clientId", "clientSecret", "refreshToken").
     * @param clientId The clientId value to test (e.g., "min", "max", "default").
     * @param clientSecret The clientSecret value to test (e.g., "min", "max", "default").
     * @param refreshToken The refreshToken value to test (e.g., "default").
     */
    @ParameterizedTest
    @CsvSource({
        "all,           default,   default,    default",
        "clientId,      min,       default,    default",
        "clientId,      max,       default,    default",
        "clientSecret,  default,   min,        default",
        "clientSecret,  default,   max,        default",
        // "refreshToken,  default,   default,    min",    There is no minimum length requirement for this string.
        // "refreshToken,  default,   default,    max",    There is no maximum length limit defined for this string.
    })
    void testValidParameters(String propertyName, String clientId, String clientSecret, String refreshToken) {
        TokenRevokeRequest request = new TokenRevokeRequest();
        request.setClientId(getTestString(clientId, commonClientId, Const.CLIENT_ID_LENGTH_MIN, Const.CLIENT_ID_LENGTH_MAX));
        request.setClientSecret(getTestString(clientSecret, commonClientSecret, Const.CLIENT_SECRET_LENGTH_MIN, Const.CLIENT_SECRET_LENGTH_MAX));
        request.setRefreshToken(getTestString(refreshToken, commonRefreshToken, null, null));

        Set<ConstraintViolation<TokenRevokeRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertTrue(violations.isEmpty(), "Expected no validation errors, but found: " + violations);
    }

    /**
     * Tests invalid parameters for TokenRevokeRequest using parameterized tests.
     *
     * @param propertyName The name of the property being tested (e.g., "clientId", "clientSecret", "refreshToken").
     * @param clientId The clientId value to test (e.g., "min", "max", "blank", "empty").
     * @param clientSecret The clientSecret value to test (e.g., "min", "max", "blank", "empty").
     * @param refreshToken The refreshToken value to test (e.g., "blank", "empty").
     */
    @ParameterizedTest
    @CsvSource({
        "clientId,      min-1,      default,    default",
        "clientId,      max+1,      default,    default",
        "clientId,      blank,      default,    default",
        "clientId,      empty,      default,    default",
        "clientSecret,  default,    min-1,      default",
        "clientSecret,  default,    max+1,      default",
        "clientSecret,  default,    blank,      default",
        "clientSecret,  default,    empty,      default",
        // "refreshToken,  default,    default,    min-1",  There is no minimum length requirement for this string.
        // "refreshToken,  default,    default,    max+1",  There is no minimum length requirement for this string.
        "refreshToken,  default,    default,    blank",
        "refreshToken,  default,    default,    empty",
    })
    void testInvalidParameters(String propertyName, String clientId, String clientSecret, String refreshToken) {
        TokenRevokeRequest request = new TokenRevokeRequest();
        request.setClientId(getTestString(clientId, commonClientId, Const.CLIENT_ID_LENGTH_MIN, Const.CLIENT_ID_LENGTH_MAX));
        request.setClientSecret(getTestString(clientSecret, commonClientSecret, Const.CLIENT_SECRET_LENGTH_MIN, Const.CLIENT_SECRET_LENGTH_MAX));
        request.setRefreshToken(getTestString(refreshToken, commonRefreshToken, null, null));

        Set<ConstraintViolation<TokenRevokeRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "Expected validation errors but found none.");
        boolean found = violations.stream().anyMatch(v -> v.getPropertyPath().toString().contains(propertyName));
        assertTrue(found, "Expected validation error for property: " + propertyName + ", but got: " + violations);
    }

    /**
     * Helper method to generate test strings based on the specified value type and constraints.
     *
     * @param valueType The type of value to generate (e.g., "default", "blank", "empty", "min-1", "min", "max", "max+1").
     * @param defaultValue The default value to use when valueType is "default".
     * @param lengthMin The minimum length constraint for the string (used for "min" and "min-1" value types).
     * @param lengthMax The maximum length constraint for the string (used for "max" and "max+1" value types).
     * @return The generated test string based on the specified parameters.
     */
    private String getTestString(String valueType, String defaultValue, Integer lengthMin, Integer lengthMax) {
        String value = null;
        switch (valueType) {
            case "default":
                value = defaultValue;
                break;
            case "blank":
                value = "";
                break;
            case "empty":
                value = null;
                break;
            case "min-1":
                value = (lengthMin - 1 < 0) ? null : "a".repeat(lengthMin - 1);
                break;
            case "min":
                value = "a".repeat(lengthMin);
                break;
            case "max":
                value = "a".repeat(lengthMax);
                break;
            case "max+1":
                value = "a".repeat(lengthMax + 1);
                break;
            default:
                throw new IllegalArgumentException("Unknown valueType: " + valueType);
        }
        return value;
    }

}
