/*
 * PutPlantRequestTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This package contains unit tests for the PutPlantRequest class,
 *
 * Date: 2025/09/27
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.open_dataspaces.core.common.consts.Const;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Unit tests for PutPlantRequest.
 */
public class PutPlantRequestTest {
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    /**
     * Test the no-args constructor.
     * Ensures that the object is created with default values.
     */
    @Test
    @DisplayName("Test no-args constructor")
    void testNoArgsConstructor() {
        // Act
        PutPlantRequest response = new PutPlantRequest();

        // Assert
        assertNotNull(response);
    }

    /**
     * Test for valid PutOperatorRequest (normal case).
     */
    @ParameterizedTest
    @CsvSource({
        // Normal cases
        "2025-08-30T12:00:00.000Z, user1,Address1,OpenId123456,GlobalId1",
        "2025-09-01T15:30:00.000Z, user2,Address2,OpenId654321,GlobalId2"
    })
    @DisplayName("PutPlantRequest - Valid Input")
    void testValidPutPlantRequest(
            String updatedAt,
            String plantName,
            String plantAddress,
            String openPlantId,
            String globalPlantId) {

        // Arrange
        PutPlantRequest request = new PutPlantRequest(
                updatedAt,
                plantName,
                plantAddress,
                openPlantId,
                globalPlantId
        );

        // Act
        Set<ConstraintViolation<PutPlantRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertNotNull(violations);
        assertTrue(violations.isEmpty());
        assertEquals(updatedAt, request.getUpdatedAt());
        assertEquals(plantName, request.getPlantName());
        assertEquals(plantAddress, request.getPlantAddress());
        assertEquals(openPlantId, request.getOpenPlantId());
        assertEquals(globalPlantId, request.getGlobalPlantId());
    }

    /**
     * Test for valid PutPlantRequest (normal values and boundary cases).
     */
    @ParameterizedTest
    @CsvSource({
        // Normal cases
        "2025-08-30T12:00:00.000Z, user1,Address1,OpenId123456,GlobalId1",
        // Boundary values
        "2025-08-30T12:00:00.000Z, 1,Address1,OpenId123456,GlobalId1",
        "2025-08-30T12:00:00.000Z, LONG,Address1,OpenId123456,GlobalId1",
        "2025-08-30T12:00:00.000Z, user1,1,OpenId123456,GlobalId1",
        "2025-08-30T12:00:00.000Z, user1,LONG,OpenId123456,GlobalId1",
        "2025-08-30T12:00:00.000Z, user1,Address1,123456,GlobalId1",
        "2025-08-30T12:00:00.000Z, user1,Address1,LONG,GlobalId1",
        "2025-08-30T12:00:00.000Z, user1,Address1,OpenId123456,''",
        "2025-08-30T12:00:00.000Z, user1,Address1,OpenId123456,LONG",
        // Null cases (no change)
        "2025-08-30T12:00:00.000Z, NULL,NULL,NULL,NULL",
    })
    @DisplayName("PutPlantRequest - Valid Input")
    void testValidPutPlantRequest_variousCases(
            String updatedAt,
            String plantName,
            String plantAddress,
            String openPlantId,
            String globalPlantId) {

        // Replace "LONG"/"NULL" with boundary values
        plantName = "LONG".equals(plantName) ? "a".repeat(Const.PLANT_NAME_LENGTH_MAX) : plantName;
        plantAddress = "LONG".equals(plantAddress) ? "a".repeat(Const.PLANT_ADDRESS_LENGTH_MAX) : plantAddress;
        openPlantId = "LONG".equals(openPlantId) ? "e".repeat(Const.OPEN_PLANT_ID_LENGTH_MAX - 6) + "1".repeat(6)  : openPlantId;
        globalPlantId = "LONG".equals(globalPlantId) ? "a".repeat(Const.GLOBAL_PLANT_ID_LENGTH_MAX) : globalPlantId;
        plantName = "NULL".equals(plantName) ? null : plantName;
        plantAddress = "NULL".equals(plantAddress) ? null : plantAddress;
        openPlantId = "NULL".equals(openPlantId) ? null : openPlantId;
        globalPlantId = "NULL".equals(globalPlantId) ? null : globalPlantId;

        // Arrange
        PutPlantRequest request = new PutPlantRequest(
                updatedAt,
                plantName,
                plantAddress,
                openPlantId,
                globalPlantId
        );

        // Act
        Set<ConstraintViolation<PutPlantRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertNotNull(violations);
        assertTrue(violations.isEmpty());
        assertEquals(updatedAt, request.getUpdatedAt());
        assertEquals(plantName, request.getPlantName());
        assertEquals(plantAddress, request.getPlantAddress());
        assertEquals(openPlantId, request.getOpenPlantId());
        assertEquals(globalPlantId, request.getGlobalPlantId());
    }

    /**
     * Test validation for abnormal cases.
     */
    @ParameterizedTest
    @CsvSource({
        // length boundary violations
        "2025-08-30T12:00:00.000Z, '',Address1,OpenId123456,GlobalId1,%s: the length must be between 1 and 256",
        "2025-08-30T12:00:00.000Z, LONG,Address1,OpenId123456,GlobalId1,%s: the length must be between 1 and 256",
        "2025-08-30T12:00:00.000Z, user1,'',OpenId123456,GlobalId1,%s: the length must be between 1 and 256",
        "2025-08-30T12:00:00.000Z, user1,LONG,OpenId123456,GlobalId1,%s: the length must be between 1 and 256",
        "2025-08-30T12:00:00.000Z, user1,Address1,12345,GlobalId1,%s: the length must be between 6 and 26",
        "2025-08-30T12:00:00.000Z, user1,Address1,LONG,GlobalId1,%s: the length must be between 6 and 26",
        "2025-08-30T12:00:00.000Z, user1,Address1,OpenId123456,LONG,%s: the length must be between 0 and 256",
        // required field is null
        "NULL, user1,Address1,OpenId123456,GlobalId1,%s: cannot be blank",
        // date is invalid format
        "'', user1,Address1,OpenId123456,GlobalId1, '%s: invalid date format, expected pattern yyyy-MM-dd''T''HH:mm:ss.SSS''Z'''",
        "testDay, user1,Address1,OpenId123456,GlobalId1, '%s: invalid date format, expected pattern yyyy-MM-dd''T''HH:mm:ss.SSS''Z'''",
        "2025-08-30T12:00:00.000, user1,Address1,OpenId123456,GlobalId1, '%s: invalid date format, expected pattern yyyy-MM-dd''T''HH:mm:ss.SSS''Z'''",
        "2025-08-30T12:00:00.000Z, user1,Address1,OpenId12345,GlobalId1,'%s: the length or characters are invalid'",
    })
    @DisplayName("PutPlantRequest - Validation Errors")
    void testValidationErrors(
            String updatedAt,
            String plantName,
            String plantAddress,
            String openPlantId,
            String globalPlantId,
            String expectedErrorMessage) {

        // Replace "LONG"/"NULL" with boundary values
        updatedAt = "NULL".equals(updatedAt) ? null : updatedAt;
        plantName = "LONG".equals(plantName) ? "a".repeat(Const.PLANT_NAME_LENGTH_MAX + 1) : plantName;
        plantAddress = "LONG".equals(plantAddress) ? "a".repeat(Const.PLANT_ADDRESS_LENGTH_MAX + 1) : plantAddress;
        openPlantId = "LONG".equals(openPlantId) ? "e".repeat(Const.OPEN_PLANT_ID_LENGTH_MAX - 6) + "1".repeat(6 + 1)  : openPlantId;
        globalPlantId = "LONG".equals(globalPlantId) ? "a".repeat(Const.GLOBAL_PLANT_ID_LENGTH_MAX + 1) : globalPlantId;

        // Arrange
        PutPlantRequest request = new PutPlantRequest(
                updatedAt,
                plantName,
                plantAddress,
                openPlantId,
                globalPlantId
        );

        // Act
        Set<ConstraintViolation<PutPlantRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "Expected validation errors, but none were found.");
        assertTrue(violations.stream().anyMatch(v -> expectedErrorMessage.equals(v.getMessage())),
                "Expected validation message: " + expectedErrorMessage + ", but found: " + violations);
    }

    /**
     * Test for the setter and getter methods of PutPlantRequest.
     */
    @Test
    @DisplayName("PutPlantRequest - Setter and Getter Test")
    void testPutPlantRequest_setterGetter() {

        // Arrange
        PutPlantRequest request = new PutPlantRequest(
                null,
                null,
                null,
                null,
                null
        );
        request.setUpdatedAt("2025-08-30T12:00:00.000Z");
        request.setPlantName("user1");
        request.setPlantAddress("Address1");
        request.setOpenPlantId("OpenId123456");
        request.setGlobalPlantId("GlobalId1");

        // Act
        Set<ConstraintViolation<PutPlantRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertNotNull(violations);
        assertTrue(violations.isEmpty());
        assertEquals("2025-08-30T12:00:00.000Z", request.getUpdatedAt());
        assertEquals("user1", request.getPlantName());
        assertEquals("Address1", request.getPlantAddress());
        assertEquals("OpenId123456", request.getOpenPlantId());
        assertEquals("GlobalId1", request.getGlobalPlantId());
    }

    /**
     * Test for the toString method of PutPlantRequest.
     */
    @Test
    @DisplayName("PutPlantRequest - toString Test")
    void testToString() {
        // Arrange
        PutPlantRequest request = new PutPlantRequest(
                "2025-08-30T12:00:00.000Z",
                "user1",
                "Address1",
                "OpenId123456",
                "GlobalId1"
        );

        // Act
        String result = request.toString();

        // Assert
        assertTrue(result.contains("2025-08-30T12:00:00.000Z"), "toString should include updatedAt");
        assertTrue(result.contains("user1"), "toString should include plantName");
        assertTrue(result.contains("Address1"), "toString should include plantAddress");
        assertTrue(result.contains("OpenId123456"), "toString should include openPlantId");
        assertTrue(result.contains("GlobalId1"), "toString should include globalPlantId");
    }

    /**
     * Test for the toString method of PutPlantRequest.
     */
    @Test
    @DisplayName("PutPlantRequest - Equals and HashCode Test")
    void testEqualsAndHashCode() {
        // Arrange
        PutPlantRequest request1 = new PutPlantRequest(
                "2025-08-30T12:00:00.000Z",
                "user1",
                "Address1",
                "OpenId123456",
                "GlobalId1"
        );

        PutPlantRequest request2 = new PutPlantRequest(
                "2025-08-30T12:00:00.000Z",
                "user1",
                "Address1",
                "OpenId123456",
                "GlobalId1"
        );

        PutPlantRequest request3 = new PutPlantRequest(
                "2025-08-30T12:00:00.000Z",
                "user2",
                "Address2",
                "OpenId654321",
                "GlobalId2"
        );
        // Act & Assert
        // Equals tests
        assertEquals(request1, request2, "Objects with the same values should be equal.");
        assertNotEquals(request1, request3, "Objects with different values should not be equal.");

        // HashCode tests
        assertEquals(request1.hashCode(), request2.hashCode(),
                "Objects with the same values should have the same hash code.");
        assertNotEquals(request1.hashCode(), request3.hashCode(),
                "Objects with different values should have different hash codes.");
    }

    /**
     * Tests whether the @JsonProperty annotation of PutOperatorRequest is set correctly.
     */
    @Test
    void testJsonPropertyNames() throws Exception {
        // Retrieve fields from the PutPlantRequest class
        Field fieldUpdatedAt = PutPlantRequest.class.getDeclaredField("updatedAt");
        Field fieldPlantName = PutPlantRequest.class.getDeclaredField("plantName");
        Field fieldPlantAddress = PutPlantRequest.class.getDeclaredField("plantAddress");
        Field fieldOpenPlantId = PutPlantRequest.class.getDeclaredField("openPlantId");
        Field fieldGlobalPlantId = PutPlantRequest.class.getDeclaredField("globalPlantId");

        // Retrieve @JsonProperty annotations
        JsonProperty annotationUpdatedAt = fieldUpdatedAt.getAnnotation(JsonProperty.class);
        JsonProperty annotationPlantName = fieldPlantName.getAnnotation(JsonProperty.class);
        JsonProperty annotationPlantAddress = fieldPlantAddress.getAnnotation(JsonProperty.class);
        JsonProperty annotationOpenPlantId = fieldOpenPlantId.getAnnotation(JsonProperty.class);
        JsonProperty annotationGlobalPlantId = fieldGlobalPlantId.getAnnotation(JsonProperty.class);

        // Assert that annotations are not null
        assertNotNull(annotationUpdatedAt, "JsonProperty annotation is missing for updatedAt");
        assertNotNull(annotationPlantName, "JsonProperty annotation is missing for plantName");
        assertNotNull(annotationPlantAddress, "JsonProperty annotation is missing for plantAddress");
        assertNotNull(annotationOpenPlantId, "JsonProperty annotation is missing for openPlantId");
        assertNotNull(annotationGlobalPlantId, "JsonProperty annotation is missing for globalPlantId");

        // Assert that annotation values match the expected constants
        assertEquals(Const.JSON_PROPERTY_UPDATED_AT, annotationUpdatedAt.value(), "JsonProperty value mismatch for updatedAt");
        assertEquals(Const.JSON_PROPERTY_PLANT_NAME, annotationPlantName.value(), "JsonProperty value mismatch for plantName");
        assertEquals(Const.JSON_PROPERTY_PLANT_ADDRESS, annotationPlantAddress.value(), "JsonProperty valuemismatch for plantAddress");
        assertEquals(Const.JSON_PROPERTY_OPEN_PLANT_ID, annotationOpenPlantId.value(), "JsonProperty value mismatch for openPlantId");
        assertEquals(Const.JSON_PROPERTY_GLOBAL_PLANT_ID, annotationGlobalPlantId.value(), "JsonProperty value mismatch for globalPlantId");
    }

    /**
     * Test JSON serialization of PutOperatorRequest.
     */
    @Test
    @DisplayName("PutPlantRequest - JSON Serialization Test")
    void testJsonSerialization() throws Exception {
        // Arrange
        PutPlantRequest request = new PutPlantRequest(
                "2025-08-30T12:00:00.000Z",
                "user1",
                "Address1",
                "OpenId123456",
                "GlobalId1"
        );

        ObjectMapper objectMapper = new ObjectMapper();

        // Act
        String json = objectMapper.writeValueAsString(request);

        // Assert
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_UPDATED_AT + "\":\"2025-08-30T12:00:00.000Z\""), "JSON should contain updatedAt");
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_PLANT_NAME + "\":\"user1\""), "JSON should contain plantName");
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_PLANT_ADDRESS + "\":\"Address1\""), "JSON should contain plantAddress");
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_OPEN_PLANT_ID + "\":\"OpenId123456\""), "JSON should contain openPlantId");
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_GLOBAL_PLANT_ID + "\":\"GlobalId1\""), "JSON should contain globalPlantId");
    }

    /**
     * Test JSON deserialization with extra (unexpected) fields.
     */
    @Test
    @DisplayName("PutOperatorRequest - JSON Deserialization Ignores Unknown Fields")
    void testJsonDeserializationWithUnknownFields() throws Exception {
        // Arrange
        String json = "{"
                + "\"" + Const.JSON_PROPERTY_UPDATED_AT + "\":\"2025-08-30T12:00:00.000Z\","
                + "\"" + Const.JSON_PROPERTY_PLANT_NAME + "\":\"user1\","
                + "\"" + Const.JSON_PROPERTY_PLANT_ADDRESS + "\":\"Address1\","
                + "\"" + Const.JSON_PROPERTY_OPEN_PLANT_ID + "\":\"OpenId123456\","
                + "\"" + Const.JSON_PROPERTY_GLOBAL_PLANT_ID + "\":\"GlobalId1\","
                + "\"extraField\":\"shouldBeIgnored\""
                + "}";

        ObjectMapper objectMapper = new ObjectMapper();

        // Act
        PutPlantRequest request = objectMapper.readValue(json, PutPlantRequest.class);
        Exception exception = null;
        try {
            request = objectMapper.readValue(json, PutPlantRequest.class);
        } catch (Exception ex) {
            exception = ex;
        }
        // Assert
        assertNull(exception, "Deserialization should not throw an exception even if unknown fields exist.");
        assertEquals("2025-08-30T12:00:00.000Z", request.getUpdatedAt());
        assertEquals("user1", request.getPlantName());
        assertEquals("Address1", request.getPlantAddress());
        assertEquals("OpenId123456", request.getOpenPlantId());
        assertEquals("GlobalId1", request.getGlobalPlantId());
    }
}
