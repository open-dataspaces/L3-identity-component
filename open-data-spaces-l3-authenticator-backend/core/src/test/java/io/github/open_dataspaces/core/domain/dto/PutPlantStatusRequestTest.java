/*
 * PutPlantStatusRequestTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for PutPlantStatusRequest DTO.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;
import java.lang.reflect.Field;

/**
 * Unit tests for PutPlantStatusRequest.
 */
class PutPlantStatusRequestTest {

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
        PutPlantStatusRequest request = new PutPlantStatusRequest();

        // Assert
        assertNotNull(request);
    }

    /**
     * Test for valid PutPlantStatusRequestTest (normal case).
     */
    @ParameterizedTest
    @CsvSource({
        // Normal cases
        "2025-08-30T12:00:00.000Z, 2025-08-30, 2025-12-31, true",
        "2025-09-01T15:30:00.000Z, 2025-09-01, 2025-12-31, false"
    })
    @DisplayName("PutPlantStatusRequest - Valid Input")
    void testParameterizedConstructor(
            String updatedAt,
            String effectiveStartDate,
            String effectiveEndDate,
            String deletedFlag) {

        // Arrange
        PutPlantStatusRequest request = new PutPlantStatusRequest(
                updatedAt,
                effectiveStartDate,
                effectiveEndDate,
                deletedFlag
        );

        // Act
        Set<ConstraintViolation<PutPlantStatusRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertNotNull(violations);
        assertTrue(violations.isEmpty());
        assertEquals(updatedAt, request.getUpdatedAt());
        assertEquals(effectiveStartDate, request.getEffectiveStartDate());
        assertEquals(effectiveEndDate, request.getEffectiveEndDate());
        assertEquals(Boolean.valueOf(deletedFlag), request.getDeletedFlag());
    }

    /**
     * Test for valid PutPlantStatusRequest (normal values and boundary cases).
     */
    @ParameterizedTest
    @CsvSource({
        // Null cases, 0 length cases
        "2025-08-30T12:00:00.000Z, NULL, NULL, NULL",
        // Pattern cases
        "2025-08-30T12:00:00.000Z, 2025-08-30, 2025-12-31, true",
        "2025-08-30T12:00:00.000Z, 2025-08-30, 2025-12-31, false"
    })
    @DisplayName("PutPlantStatusRequest - Valid Input")
    void testValidParameterizedConstructor(
            String updatedAt,
            String effectiveStartDate,
            String effectiveEndDate,
            String deletedFlag) {

        // Replace "LONG"/"NULL" with boundary values
        effectiveStartDate = "NULL".equals(effectiveStartDate) ? null : effectiveStartDate;
        effectiveEndDate = "NULL".equals(effectiveEndDate) ? null : effectiveEndDate;
        deletedFlag = "NULL".equals(deletedFlag) ? null : deletedFlag;

        // Arrange
        PutPlantStatusRequest request = new PutPlantStatusRequest(
                updatedAt,
                effectiveStartDate,
                effectiveEndDate,
                deletedFlag
        );

        // Act
        Set<ConstraintViolation<PutPlantStatusRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertNotNull(violations);
        assertTrue(violations.isEmpty());
        assertEquals(updatedAt, request.getUpdatedAt());
        assertEquals(effectiveStartDate, request.getEffectiveStartDate());
        assertEquals(effectiveEndDate, request.getEffectiveEndDate());
        if (deletedFlag == null) {
            assertEquals(null, request.getDeletedFlag());
        } else {
            assertEquals(Boolean.valueOf(deletedFlag), request.getDeletedFlag());
        }
    }

    /**
     * Test validation for abnormal cases.
     */
    @ParameterizedTest
    @CsvSource({
        // required field is null
        "NULL, 2025-08-30, 2025-12-31, false, NOT_BLANK",
        // length boundary violations
        // -- No applicable pattern for this DTO --
        // pattern violations
        "2025-08-30T12:00:00.000Z, 2025-08-30, 2025-12-31, invalidBoolean, PATTERN_BOOLEAN",
        // date is invalid format
        "'', 2025-08-30, 2025-12-31, false, PATTERN_DATE",
        "invalid-date, 2025-08-30, 2025-12-31, false, PATTERN_DATE",
        "2025/08/30 12:00:00.000, 2025-08-30, 2025-12-31, false, PATTERN_DATE",
        "2025-08-30T12:00:00.000Z, '', 2025-12-31, false, PATTERN_DATE",
        "2025-08-30T12:00:00.000Z, invalid-date, 2025-12-31, false, PATTERN_DATE",
        "2025-08-30T12:00:00.000Z, 2025/08/30, 2025-12-31, false, PATTERN_DATE",
        "2025-08-30T12:00:00.000Z, 2025-08-30, '', false, PATTERN_DATE",
        "2025-08-30T12:00:00.000Z, 2025-08-30, invalid-date, false, PATTERN_DATE",
        "2025-08-30T12:00:00.000Z, 2025-08-30, 2025/08/30, false, PATTERN_DATE",
    })
    @DisplayName("PutPlantStatusRequest - Validation Errors")
    void testInvalidParameterizedConstructor(
            String updatedAt,
            String effectiveStartDate,
            String effectiveEndDate,
            String deletedFlag,
            String expectedErrorMessage) {

        // Replace "LONG"/"NULL" with boundary values
        updatedAt = "NULL".equals(updatedAt) ? null : updatedAt;

        String invalidMessage;
        switch (expectedErrorMessage) {
            case "NOT_BLANK":
                invalidMessage = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK;
                break;
            case "PATTERN_DATE":
                invalidMessage = ConstError.ERR_VALIDATION_COMMON_PATTERN_DATE;
                break;
            case "PATTERN_BOOLEAN":
                invalidMessage = ConstError.ERR_VALIDATION_COMMON_PATTERN_BOOLEAN;
                break;
            default:
                throw new IllegalArgumentException("Invalid expectedErrorMessage: " + expectedErrorMessage);
        }

        // Arrange
        PutPlantStatusRequest request = new PutPlantStatusRequest(
                updatedAt,
                effectiveStartDate,
                effectiveEndDate,
                deletedFlag
        );

        // Act
        Set<ConstraintViolation<PutPlantStatusRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "Expected validation errors, but none were found.");
        assertTrue(violations.stream().anyMatch(v -> invalidMessage.equals(v.getMessageTemplate())),
                "Expected validation message: " + invalidMessage + ", but found: " + violations);
    }

    /**
     * Test for PutOperatorRequest with Setter and Getter.
     */
    @Test
    @DisplayName("PutPlantStatusRequest - Setter and Getter Test")
    void testSettersAndGetters() {

        // Arrange
        PutPlantStatusRequest request = new PutPlantStatusRequest(
                null,
                null,
                null,
                null
        );
        request.setUpdatedAt("2025-08-30T12:00:00.000Z");
        request.setEffectiveStartDate("2025-08-30");
        request.setEffectiveEndDate("2025-12-31");
        request.setDeletedFlag("true");

        // Act
        Set<ConstraintViolation<PutPlantStatusRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertNotNull(violations);
        assertTrue(violations.isEmpty());
        assertEquals("2025-08-30T12:00:00.000Z", request.getUpdatedAt());
        assertEquals("2025-08-30", request.getEffectiveStartDate());
        assertEquals("2025-12-31", request.getEffectiveEndDate());
        assertEquals(Boolean.TRUE, request.getDeletedFlag());
    }

    /**
     * Test for the toString method of PutOperatorRequest.
     */
    @Test
    @DisplayName("PutPlantStatusRequest - toString Test")
    void testToString() {
        // Arrange
        PutPlantStatusRequest request = new PutPlantStatusRequest(
                "2025-08-30T12:00:00.000Z",
                "2025-08-30",
                "2025-12-31",
                "true"
        );

        // Act
        String result = request.toString();

        // Assert
        assertTrue(result.contains("2025-08-30T12:00:00.000Z"), "toString should include updatedAt");
        assertTrue(result.contains("2025-08-30"), "toString should include effectiveStartDate");
        assertTrue(result.contains("2025-12-31"), "toString should include effectiveEndDate");
        assertTrue(result.contains("true"), "toString should include deletedFlag");
    }

    /**
     * Test for equals and hashCode methods.
     */
    @Test
    @DisplayName("PutPlantStatusRequest - Equals and HashCode Test")
    void testEqualsAndHashCode() {
        // Arrange
        PutPlantStatusRequest request = new PutPlantStatusRequest(
                "2025-08-30T12:00:00.000Z",
                "2025-08-30",
                "2025-12-31",
                "true"
        );

        PutPlantStatusRequest requestEquales = new PutPlantStatusRequest(
                "2025-08-30T12:00:00.000Z",
                "2025-08-30",
                "2025-12-31",
                "true"
        );

        PutPlantStatusRequest requestNotEquales = new PutPlantStatusRequest(
                "2025-09-01T15:30:00.000Z",
                "2025-06-01",
                "2025-10-30",
                "false"
        );

        // Act & Assert
        // Equals tests
        assertEquals(request, requestEquales, "Objects with the same values should be equal.");
        assertNotEquals(request, requestNotEquales, "Objects with different values should not be equal.");

        // HashCode tests
        assertEquals(request.hashCode(), requestEquales.hashCode(),
                "Objects with the same values should have the same hash code.");
        assertNotEquals(request.hashCode(), requestNotEquales.hashCode(),
                "Objects with different values should have different hash codes.");
    }

    /**
     * Tests whether the @JsonProperty annotation of PutOperatorRequest is set correctly.
     */
    @Test
    void testJsonPropertyNames() throws Exception {
        // Retrieve fields from the PutOperatorRequest class
        Field fieldUpdatedAt = PutPlantStatusRequest.class.getDeclaredField("updatedAt");
        Field fieldEffectiveStartDate = PutPlantStatusRequest.class.getDeclaredField("effectiveStartDate");
        Field fieldEffectiveEndDate = PutPlantStatusRequest.class.getDeclaredField("effectiveEndDate");
        Field fieldDeletedFlag = PutPlantStatusRequest.class.getDeclaredField("deletedFlag");

        // Retrieve @JsonProperty annotations
        JsonProperty annotationUpdatedAt = fieldUpdatedAt.getAnnotation(JsonProperty.class);
        JsonProperty annotationEffectiveStartDate = fieldEffectiveStartDate.getAnnotation(JsonProperty.class);
        JsonProperty annotationEffectiveEndDate = fieldEffectiveEndDate.getAnnotation(JsonProperty.class);
        JsonProperty annotationDeletedFlag = fieldDeletedFlag.getAnnotation(JsonProperty.class);

        // Assert that annotations are not null
        assertNotNull(annotationUpdatedAt, "JsonProperty annotation is missing for updatedAt");
        assertNotNull(annotationEffectiveStartDate, "JsonProperty annotation is missing for effectiveStartDate");
        assertNotNull(annotationEffectiveEndDate, "JsonProperty annotation is missing for effectiveEndDate");
        assertNotNull(annotationDeletedFlag, "JsonProperty annotation is missing for deletedFlag");

        // Assert that annotation values match the expected constants
        assertEquals(Const.JSON_PROPERTY_UPDATED_AT, annotationUpdatedAt.value(), "JsonProperty value mismatch for updatedAt");
        assertEquals(Const.JSON_PROPERTY_EFFECTIVE_START_DATE, annotationEffectiveStartDate.value(), "JsonProperty value mismatch for effectiveStartDate");
        assertEquals(Const.JSON_PROPERTY_EFFECTIVE_END_DATE, annotationEffectiveEndDate.value(), "JsonProperty value mismatch for effectiveEndDate");
        assertEquals(Const.JSON_PROPERTY_DELETED_FLAG, annotationDeletedFlag.value(), "JsonProperty value mismatch for deletedFlag");
    }

    /**
     * Test JSON serialization of PutOperatorRequest.
     */
    @Test
    @DisplayName("PutPlantStatusRequest - JSON Serialization Test")
    void testJsonSerialization() throws Exception {
        // Arrange
        PutPlantStatusRequest request = new PutPlantStatusRequest(
                "2025-08-30T12:00:00.000Z",
                "2025-08-30",
                "2025-12-31",
                "true"
        );

        ObjectMapper objectMapper = new ObjectMapper();

        // Act
        String json = objectMapper.writeValueAsString(request);

        // Assert
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_UPDATED_AT + "\":\"2025-08-30T12:00:00.000Z\""), "JSON should contain updatedAt");
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_EFFECTIVE_START_DATE + "\":\"2025-08-30\""), "JSON should contain effectiveStartDate");
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_EFFECTIVE_END_DATE + "\":\"2025-12-31\""), "JSON should contain effectiveEndDate");
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_DELETED_FLAG + "\":true"), "JSON should contain deletedFlag");
    }
}
