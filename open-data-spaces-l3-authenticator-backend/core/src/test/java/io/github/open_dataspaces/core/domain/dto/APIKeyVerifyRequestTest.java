/*
 * APIKeyVerifyRequestTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * Unit tests for the APIKeyVerifyResponse class.
 *
 * Date: 2025-11-30
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Field;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.open_dataspaces.core.common.consts.Const;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;

/**
 * Unit tests for the APIKeyVerifyRequestTest class.
 */
public class APIKeyVerifyRequestTest {
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // Register JavaTimeModule
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // Use ISO-8601 format

    // Common input parameters
    private final String commonVerifyAPIKey = "Sample-API-Key";

    /**
     * Test for the default constructor.
     */
    @Test
    @DisplayName("APIKeyVerifyRequest - Default Constructor")
    void testDefaultConstructor() {
        APIKeyVerifyRequest dtoObject = new APIKeyVerifyRequest();

        assertNotNull(dtoObject);
        assertNull(dtoObject.getVerifyAPIKey());
    }

    /**
     * Test for the All args constructor.
     */
    @Test
    @DisplayName("APIKeyVerifyRequest - All Args Constructor")
    void testAllArgsConstructor() {
        APIKeyVerifyRequest dtoObject = new APIKeyVerifyRequest(
                commonVerifyAPIKey
        );

        assertNotNull(dtoObject);
        assertEquals(commonVerifyAPIKey, dtoObject.getVerifyAPIKey());
    }

    /**
     * Test for setters and getters of APIKeyVerifyRequest.
     */
    @Test
    @DisplayName("APIKeyVerifyRequest - Setter and Getter Test")
    void testSettersAndGetters() {
        // Arrange
        APIKeyVerifyRequest dtoObject = new APIKeyVerifyRequest();

        // Act
        dtoObject.setVerifyAPIKey(commonVerifyAPIKey);

        // Assert
        assertNotNull(dtoObject);
        assertEquals(commonVerifyAPIKey, dtoObject.getVerifyAPIKey());
    }

    /**
     * Test for valid APIKeyVerifyRequest.
     */
    @ParameterizedTest
    @CsvSource({
        "VALUE",
        "a",
        "LONG"
    })
    void testValidParameters(String argVerifyAPIKey) {
        String verifyAPIKey =
                "VALUE".equals(argVerifyAPIKey) ? commonVerifyAPIKey :
                "LONG".equals(argVerifyAPIKey) ? "E".repeat(Const.VERIFY_API_KEY_LENGTH_MAX) :
                argVerifyAPIKey;
        // Arrange
        APIKeyVerifyRequest request = new APIKeyVerifyRequest();
        request.setVerifyAPIKey(verifyAPIKey);
        // Act
        Set<ConstraintViolation<APIKeyVerifyRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertTrue(violations.isEmpty(), "Expected no validation errors, but found: " + violations);
    }

    /**
     * Test for invalid APIKeyVerifyRequest.
     */
    @ParameterizedTest
    @CsvSource({
        "EMPTY",
        "''",
        "OVER"
    })
    void testInvalidParameters(String argVerifyAPIKey) {
        String verifyAPIKey =
                "EMPTY".equals(argVerifyAPIKey) ? null :
                "OVER".equals(argVerifyAPIKey) ? "E".repeat(Const.VERIFY_API_KEY_LENGTH_MAX + 1) : argVerifyAPIKey;
        // Arrange
        APIKeyVerifyRequest request = new APIKeyVerifyRequest();
        request.setVerifyAPIKey(verifyAPIKey);
        // Act
        Set<ConstraintViolation<APIKeyVerifyRequest>> violations = VALIDATOR.validate(request);
        // Assert
        assertFalse(violations.isEmpty(),
                "Expected no validation errors, but found: " + violations.toString());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().contains("verifyAPIKey")),
                "validation property should contain '" + "verifyAPIKey" + "' but was: " + violations.toString());
    }

    /**
     * Test for the toString method of APIKeyVerifyRequest.
     */
    @Test
    @DisplayName("APIKeyVerifyRequest - toString Test")
    void testToString() {
        // Arrange
        APIKeyVerifyRequest dtoObject = new APIKeyVerifyRequest(
                commonVerifyAPIKey
        );

        // Act
        String result = dtoObject.toString();

        // Assert
        assertTrue(result.contains("verifyAPIKey"), "toString should include verifyAPIKey");
    }

    /**
     * Test for equals and hashCode methods.
     */
    @Test
    @DisplayName("APIKeyVerifyRequest - Equals and HashCode Test")
    void testEqualsAndHashCode() {
        // Arrange
        APIKeyVerifyRequest dtoObjectBase = new APIKeyVerifyRequest(
                commonVerifyAPIKey
        );
        // Arrange
        APIKeyVerifyRequest dtoObjectEquals = new APIKeyVerifyRequest(
                commonVerifyAPIKey
        );
        // Arrange
        APIKeyVerifyRequest dtoObjectNotEquals = new APIKeyVerifyRequest(
                "invalid-APIKey"
        );

        // Act & Assert
        // Equals tests
        assertEquals(dtoObjectBase, dtoObjectEquals, "Objects with the same values should be equal.");
        assertNotEquals(dtoObjectBase, dtoObjectNotEquals, "Objects with different values should not be equal.");

        // HashCode tests
        assertEquals(dtoObjectBase.hashCode(), dtoObjectEquals.hashCode(),
                "Objects with the same values should have the same hash code.");
        assertNotEquals(dtoObjectBase.hashCode(), dtoObjectNotEquals.hashCode(),
                "Objects with different values should have different hash codes.");
    }

    /**
     * Test to verify that @Column annotations are correctly set on fields.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "verifyAPIKey, " + Const.JSON_PROPERTY_VERIFY_APIKEY,
    })
    @DisplayName("APIKeyVerifyRequest - @JsonProperty Annotation Test")
    void testJsonPropertyNames(String fieldName, String jsonPropertyName) throws Exception {
        Field field = APIKeyVerifyRequest.class.getDeclaredField(fieldName);
        JsonProperty annotation = field.getAnnotation(JsonProperty.class);
        assertNotNull(annotation);
        assertEquals(jsonPropertyName, annotation.value());
    }

    /**
     * Test JSON serialization of APIKeyVerifyRequest.
     */
    @Test
    @DisplayName("APIKeyVerifyRequest - JSON Serialization Test")
    void testJsonSerialization() throws Exception {
        // Arrange
        APIKeyVerifyRequest dtoObject = new APIKeyVerifyRequest(
                commonVerifyAPIKey
        );

        // Act
        String json = objectMapper.writeValueAsString(dtoObject);

        // Assert
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_VERIFY_APIKEY + "\":\"Sample-API-Key\""));
    }
}
