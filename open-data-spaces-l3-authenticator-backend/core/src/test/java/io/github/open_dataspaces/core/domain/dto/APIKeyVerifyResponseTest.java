/*
 * APIKeyVerifyResponseTest.java
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Field;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.open_dataspaces.core.common.consts.Const;

/**
 * Unit tests for the APIKeyVerifyResponseTest class.
 */
public class APIKeyVerifyResponseTest {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // Register JavaTimeModule
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // Use ISO-8601 format

    // Common input parameters
    private final boolean commonVerifyResult = false;

    /**
     * Test for the default constructor.
     */
    @Test
    @DisplayName("APIKeyVerifyResponse - Default Constructor")
    void testDefaultConstructor() {
        APIKeyVerifyResponse dtoObject = new APIKeyVerifyResponse();

        assertNotNull(dtoObject);
        assertFalse(dtoObject.isVerifyResult());
    }

    /**
     * Test for the All args constructor.
     */
    @Test
    @DisplayName("APIKeyVerifyResponse - All Args Constructor")
    void testAllArgsConstructor() {
        APIKeyVerifyResponse dtoObject = new APIKeyVerifyResponse(
                commonVerifyResult
        );

        assertNotNull(dtoObject);
        assertEquals(commonVerifyResult, dtoObject.isVerifyResult());
    }

    /**
     * Test for setters and getters of APIKeyVerifyResponse.
     */
    @Test
    @DisplayName("APIKeyVerifyResponse - Setter and Getter Test")
    void testSettersAndGetters() {
        // Arrange
        APIKeyVerifyResponse dtoObject = new APIKeyVerifyResponse();

        // Act
        dtoObject.setVerifyResult(commonVerifyResult);

        // Assert
        assertNotNull(dtoObject);
        assertEquals(commonVerifyResult, dtoObject.isVerifyResult());
    }

    /**
     * Test for the toString method of APIKeyVerifyResponse.
     */
    @Test
    @DisplayName("APIKeyVerifyResponse - toString Test")
    void testToString() {
        // Arrange
        APIKeyVerifyResponse dtoObject = new APIKeyVerifyResponse(
                commonVerifyResult
        );

        // Act
        String result = dtoObject.toString();

        // Assert
        assertTrue(result.contains("verifyResult"), "toString should include verifyResult");
    }

    /**
     * Test for equals and hashCode methods.
     */
    @Test
    @DisplayName("APIKeyVerifyResponse - Equals and HashCode Test")
    void testEqualsAndHashCode() {
        // Arrange
        APIKeyVerifyResponse dtoObjectBase = new APIKeyVerifyResponse(
                commonVerifyResult
        );

        // Arrange
        APIKeyVerifyResponse dtoObjectEquals = new APIKeyVerifyResponse(
                commonVerifyResult
        );

        // Arrange
        APIKeyVerifyResponse dtoObjectNotEquals = new APIKeyVerifyResponse(
                true
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
        "verifyResult, " + Const.JSON_PROPERTY_VERIFY_RESULT,
    })
    @DisplayName("APIKeyVerifyResponse - @JsonProperty Annotation Test")
    void testJsonPropertyNames(String fieldName, String jsonPropertyName) throws Exception {
        Field field = APIKeyVerifyResponse.class.getDeclaredField(fieldName);
        JsonProperty annotation = field.getAnnotation(JsonProperty.class);
        assertNotNull(annotation);
        assertEquals(jsonPropertyName, annotation.value());
    }

    /**
     * Test JSON serialization of APIKeyVerifyResponse.
     */
    @Test
    @DisplayName("APIKeyVerifyResponse - JSON Serialization Test")
    void testJsonSerialization() throws Exception {
        // Arrange
        APIKeyVerifyResponse dtoObject = new APIKeyVerifyResponse(
                commonVerifyResult
        );

        // Act
        String json = objectMapper.writeValueAsString(dtoObject);

        // Assert
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_VERIFY_RESULT + "\":false"));
    }
}
