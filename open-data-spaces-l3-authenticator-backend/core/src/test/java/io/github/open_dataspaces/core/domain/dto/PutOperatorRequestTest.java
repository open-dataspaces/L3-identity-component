/*
 * PutOperatorRequestTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for PutOperatorRequest DTO.
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
 * Unit tests for PutOperatorRequest.
 */
class PutOperatorRequestTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    /**
     * Test for valid PutOperatorRequest (normal case).
     */
    @ParameterizedTest
    @CsvSource({
        // Normal cases
        "2025-08-30T12:00:00.000Z, user1, Operator1, Address1, OpenId1, GlobalId1",
        "2025-09-01T15:30:00.000Z, user2, Operator2, Address2, OpenId2, GlobalId2"
    })
    @DisplayName("PutOperatorRequest - Valid Input")
    void testValidPutOperatorRequest(
            String updatedAt,
            String loginUserId,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId) {

        // Arrange
        PutOperatorRequest request = new PutOperatorRequest(
                updatedAt,
                loginUserId,
                operatorName,
                operatorAddress,
                openOperatorId,
                globalOperatorId
        );

        // Act
        Set<ConstraintViolation<PutOperatorRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertNotNull(violations);
        assertTrue(violations.isEmpty());
        assertEquals(updatedAt, request.getUpdatedAt());
        assertEquals(loginUserId, request.getLoginUserId());
        assertEquals(operatorName, request.getOperatorName());
        assertEquals(operatorAddress, request.getOperatorAddress());
        assertEquals(openOperatorId, request.getOpenOperatorId());
        assertEquals(globalOperatorId, request.getGlobalOperatorId());
    }

    /**
     * Test for valid PutOperatorRequest (normal values and boundary cases).
     */
    @ParameterizedTest
    @CsvSource({
        // Boundary values
        "2025-08-30T12:00:00.000Z, us1, Operator1, Address1, OpenId1, GlobalId1",
        "2025-08-30T12:00:00.000Z, LONG, Operator1, Address1, OpenId1, GlobalId1",
        "2025-08-30T12:00:00.000Z, user1, O, Address1, OpenId1, GlobalId1",
        "2025-08-30T12:00:00.000Z, user1, LONG, Address1, OpenId1, GlobalId1",
        "2025-08-30T12:00:00.000Z, user1, Operator1, A, OpenId1, GlobalId1",
        "2025-08-30T12:00:00.000Z, user1, Operator1, LONG, OpenId1, GlobalId1",
        "2025-08-30T12:00:00.000Z, user1, Operator1, Address1, O, GlobalId1",
        "2025-08-30T12:00:00.000Z, user1, Operator1, Address1, LONG, GlobalId1",
        "2025-08-30T12:00:00.000Z, user1, Operator1, Address1, OpenId1, LONG",
        // Null cases, 0 length cases
        "2025-08-30T12:00:00.000Z, NULL, NULL, NULL, NULL, NULL",
        "2025-08-30T12:00:00.000Z, NULL, NULL, NULL, NULL, ''",
        // Pattern cases
        "2025-08-30T12:00:00.000Z, az09_.-@, Operator1, Address1, OpenId1, GlobalId1",
        "2025-08-30T12:00:00.000Z, user1, Operator1, Address1, AZaz09, GlobalId1"
    })
    @DisplayName("PutOperatorRequest - Valid Input")
    void testValidPutOperatorRequest_variousCaases(
            String updatedAt,
            String loginUserId,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId) {

        // Replace "LONG"/"NULL" with boundary values
        loginUserId = "LONG".equals(loginUserId) ? "a".repeat(Const.LOGIN_USER_ID_LENGTH_MAX) : loginUserId;
        operatorName = "LONG".equals(operatorName) ? "a".repeat(Const.OPERATOR_NAME_LENGTH_MAX) : operatorName;
        operatorAddress = "LONG".equals(operatorAddress) ? "a".repeat(Const.OPERATOR_ADDRESS_LENGTH_MAX) : operatorAddress;
        openOperatorId = "LONG".equals(openOperatorId) ? "a".repeat(Const.OPEN_OPERATOR_ID_LENGTH_MAX) : openOperatorId;
        globalOperatorId = "LONG".equals(globalOperatorId) ? "a".repeat(Const.GLOBAL_OPERATOR_ID_LENGTH_MAX) : globalOperatorId;
        loginUserId = "NULL".equals(loginUserId) ? null : loginUserId;
        operatorName = "NULL".equals(operatorName) ? null : operatorName;
        operatorAddress = "NULL".equals(operatorAddress) ? null : operatorAddress;
        openOperatorId = "NULL".equals(openOperatorId) ? null : openOperatorId;
        globalOperatorId = "NULL".equals(globalOperatorId) ? null : globalOperatorId;

        // Arrange
        PutOperatorRequest request = new PutOperatorRequest(
                updatedAt,
                loginUserId,
                operatorName,
                operatorAddress,
                openOperatorId,
                globalOperatorId
        );

        // Act
        Set<ConstraintViolation<PutOperatorRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertNotNull(violations);
        assertTrue(violations.isEmpty());
        assertEquals(updatedAt, request.getUpdatedAt());
        assertEquals(loginUserId, request.getLoginUserId());
        assertEquals(operatorName, request.getOperatorName());
        assertEquals(operatorAddress, request.getOperatorAddress());
        assertEquals(openOperatorId, request.getOpenOperatorId());
        assertEquals(globalOperatorId, request.getGlobalOperatorId());
    }

    /**
     * Test validation for abnormal cases.
     */
    @ParameterizedTest
    @CsvSource({
        // required field is null
        "NULL, user1, Operator1, Address1, OpenId1, GlobalId1, %s: cannot be blank",
        // length boundary violations
        "2025-08-30T12:00:00.000Z, us, Operator1, Address1, OpenId1, GlobalId1, %s: the length must be between 3 and 255",
        "2025-08-30T12:00:00.000Z, LONG, Operator1, Address1, OpenId1, GlobalId1, %s: the length must be between 3 and 255",
        "2025-08-30T12:00:00.000Z, user1, '', Address1, OpenId1, GlobalId1, %s: the length must be between 1 and 255",
        "2025-08-30T12:00:00.000Z, user1, LONG, Address1, OpenId1, GlobalId1, %s: the length must be between 1 and 255",
        "2025-08-30T12:00:00.000Z, user1, Operator1, '', OpenId1, GlobalId1, %s: the length must be between 1 and 256",
        "2025-08-30T12:00:00.000Z, user1, Operator1, LONG, OpenId1, GlobalId1, %s: the length must be between 1 and 256",
        "2025-08-30T12:00:00.000Z, user1, Operator1, Address1, '', GlobalId1, %s: the length must be between 1 and 20",
        "2025-08-30T12:00:00.000Z, user1, Operator1, Address1, LONG, GlobalId1, %s: the length must be between 1 and 20",
        "2025-08-30T12:00:00.000Z, user1, Operator1, Address1, OpenId1, LONG, %s: the length must be between 0 and 256",
        // pattern violations
        "2025-08-30T12:00:00.000Z, invalid_user!, Operator1, Address1, OpenId1, GlobalId1, %s: the length or characters are invalid",
        "2025-08-30T12:00:00.000Z, invalidUser1, Operator1, Address1, OpenId1, GlobalId1, %s: the length or characters are invalid",
        "2025-08-30T12:00:00.000Z, user1, Operator1, Address1, invalid_open_id, GlobalId1, %s: the length or characters are invalid",
        // date is invalid format
        "'', user1, Operator1, Address1, OpenId1, GlobalId1, '%s: invalid date format, expected pattern yyyy-MM-dd''T''HH:mm:ss.SSS''Z'''",
        "invalid-date, user1, Operator1, Address1, OpenId1, GlobalId1, '%s: invalid date format, expected pattern yyyy-MM-dd''T''HH:mm:ss.SSS''Z'''",
        "2025/08/30 12:00:00.000, user1, Operator1, Address1, OpenId1, GlobalId1, '%s: invalid date format, expected pattern yyyy-MM-dd''T''HH:mm:ss.SSS''Z'''"
    })
    @DisplayName("PutOperatorRequest - Validation Errors")
    void testValidationErrors(
            String updatedAt,
            String loginUserId,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId,
            String expectedErrorMessage) {

        // Replace "LONG"/"NULL" with boundary values
        updatedAt = "NULL".equals(updatedAt) ? null : updatedAt;
        loginUserId = "LONG".equals(loginUserId) ? "a".repeat(Const.LOGIN_USER_ID_LENGTH_MAX + 1) : loginUserId;
        operatorName = "LONG".equals(operatorName) ? "a".repeat(Const.OPERATOR_NAME_LENGTH_MAX + 1) : operatorName;
        operatorAddress = "LONG".equals(operatorAddress) ? "a".repeat(Const.OPERATOR_ADDRESS_LENGTH_MAX + 1) : operatorAddress;
        openOperatorId = "LONG".equals(openOperatorId) ? "a".repeat(Const.OPEN_OPERATOR_ID_LENGTH_MAX + 1) : openOperatorId;
        globalOperatorId = "LONG".equals(globalOperatorId) ? "a".repeat(Const.GLOBAL_OPERATOR_ID_LENGTH_MAX + 1) : globalOperatorId;

        // Arrange
        PutOperatorRequest request = new PutOperatorRequest(
                updatedAt,
                loginUserId,
                operatorName,
                operatorAddress,
                openOperatorId,
                globalOperatorId
        );

        // Act
        Set<ConstraintViolation<PutOperatorRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "Expected validation errors, but none were found.");
        assertTrue(violations.stream().anyMatch(v -> expectedErrorMessage.equals(v.getMessage())),
                "Expected validation message: " + expectedErrorMessage + ", but found: " + violations);
    }

    /**
     * Test for PutOperatorRequest with Setter and Getter.
     */
    @Test
    @DisplayName("PutOperatorRequest - Setter and Getter Test")
    void testPutOperatorRequest_setterGetter() {

        // Arrange
        PutOperatorRequest request = new PutOperatorRequest(
                null,
                null,
                null,
                null,
                null,
                null
        );
        request.setUpdatedAt("2025-08-30T12:00:00.000Z");
        request.setLoginUserId("user1");
        request.setOperatorName("Operator1");
        request.setOperatorAddress("Address1");
        request.setOpenOperatorId("OpenId1");
        request.setGlobalOperatorId("GlobalId1");

        // Act
        Set<ConstraintViolation<PutOperatorRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertNotNull(violations);
        assertTrue(violations.isEmpty());
        assertEquals("2025-08-30T12:00:00.000Z", request.getUpdatedAt());
        assertEquals("user1", request.getLoginUserId());
        assertEquals("Operator1", request.getOperatorName());
        assertEquals("Address1", request.getOperatorAddress());
        assertEquals("OpenId1", request.getOpenOperatorId());
        assertEquals("GlobalId1", request.getGlobalOperatorId());
    }

    /**
     * Tests whether the @JsonProperty annotation of PutOperatorRequest is set correctly.
     */
    @Test
    void testJsonPropertyNames() throws Exception {
        // Retrieve fields from the PutOperatorRequest class
        Field fieldUpdatedAt = PutOperatorRequest.class.getDeclaredField("updatedAt");
        Field fieldLoginUserId = PutOperatorRequest.class.getDeclaredField("loginUserId");
        Field fieldOperatorName = PutOperatorRequest.class.getDeclaredField("operatorName");
        Field fieldOperatorAddress = PutOperatorRequest.class.getDeclaredField("operatorAddress");
        Field fieldOpenOperatorId = PutOperatorRequest.class.getDeclaredField("openOperatorId");
        Field fieldGlobalOperatorId = PutOperatorRequest.class.getDeclaredField("globalOperatorId");

        // Retrieve @JsonProperty annotations
        JsonProperty annotationUpdatedAt = fieldUpdatedAt.getAnnotation(JsonProperty.class);
        JsonProperty annotationLoginUserId = fieldLoginUserId.getAnnotation(JsonProperty.class);
        JsonProperty annotationOperatorName = fieldOperatorName.getAnnotation(JsonProperty.class);
        JsonProperty annotationOperatorAddress = fieldOperatorAddress.getAnnotation(JsonProperty.class);
        JsonProperty annotationOpenOperatorId = fieldOpenOperatorId.getAnnotation(JsonProperty.class);
        JsonProperty annotationGlobalOperatorId = fieldGlobalOperatorId.getAnnotation(JsonProperty.class);

        // Assert that annotations are not null
        assertNotNull(annotationUpdatedAt, "JsonProperty annotation is missing for updatedAt");
        assertNotNull(annotationLoginUserId, "JsonProperty annotation is missing for loginUserId");
        assertNotNull(annotationOperatorName, "JsonProperty annotation is missing for operatorName");
        assertNotNull(annotationOperatorAddress, "JsonProperty annotation is missing for operatorAddress");
        assertNotNull(annotationOpenOperatorId, "JsonProperty annotation is missing for openOperatorId");
        assertNotNull(annotationGlobalOperatorId, "JsonProperty annotation is missing for globalOperatorId");

        // Assert that annotation values match the expected constants
        assertEquals(Const.JSON_PROPERTY_UPDATED_AT, annotationUpdatedAt.value(), "JsonProperty value mismatch for updatedAt");
        assertEquals(Const.JSON_PROPERTY_LOGIN_USER_ID, annotationLoginUserId.value(), "JsonProperty value mismatch for loginUserId");
        assertEquals(Const.JSON_PROPERTY_OPERATOR_NAME, annotationOperatorName.value(), "JsonProperty value mismatch for operatorName");
        assertEquals(Const.JSON_PROPERTY_OPERATOR_ADDRESS, annotationOperatorAddress.value(), "JsonProperty value mismatch for operatorAddress");
        assertEquals(Const.JSON_PROPERTY_OPEN_OPERATOR_ID, annotationOpenOperatorId.value(), "JsonProperty value mismatch for openOperatorId");
        assertEquals(Const.JSON_PROPERTY_GLOBAL_OPERATOR_ID, annotationGlobalOperatorId.value(), "JsonProperty value mismatch for globalOperatorId");
    }

    /**
     * Test for the toString method of PutOperatorRequest.
     */
    @Test
    @DisplayName("PutOperatorRequest - toString Test")
    void testToString() {
        // Arrange
        PutOperatorRequest request = new PutOperatorRequest(
                "2025-08-30T12:00:00.000Z",
                "user123",
                "Operator Name",
                "123 Main St",
                "open123",
                "global123"
        );

        // Act
        String result = request.toString();

        // Assert
        assertTrue(result.contains("2025-08-30T12:00:00.000Z"), "toString should include updatedAt");
        assertTrue(result.contains("user123"), "toString should include loginUserId");
        assertTrue(result.contains("Operator Name"), "toString should include operatorName");
        assertTrue(result.contains("123 Main St"), "toString should include operatorAddress");
        assertTrue(result.contains("open123"), "toString should include openOperatorId");
        assertTrue(result.contains("global123"), "toString should include globalOperatorId");
    }

    /**
     * Test for equals and hashCode methods.
     */
    @Test
    @DisplayName("PutOperatorRequest - Equals and HashCode Test")
    void testEqualsAndHashCode() {
        // Arrange
        PutOperatorRequest request1 = new PutOperatorRequest(
                "2025-08-30T12:00:00.000Z",
                "user1",
                "Operator1",
                "Address1",
                "OpenId1",
                "GlobalId1"
        );

        PutOperatorRequest request2 = new PutOperatorRequest(
                "2025-08-30T12:00:00.000Z",
                "user1",
                "Operator1",
                "Address1",
                "OpenId1",
                "GlobalId1"
        );

        PutOperatorRequest request3 = new PutOperatorRequest(
                "2025-09-01T15:30:00.000Z",
                "user2",
                "Operator2",
                "Address2",
                "OpenId2",
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
     * Test JSON serialization of PutOperatorRequest.
     */
    @Test
    @DisplayName("PutOperatorRequest - JSON Serialization Test")
    void testJsonSerialization() throws Exception {
        // Arrange
        PutOperatorRequest request = new PutOperatorRequest(
                "2025-08-30T12:00:00.000Z",
                "user123",
                "Operator Name",
                "123 Main St",
                "open123",
                "global123"
        );

        ObjectMapper objectMapper = new ObjectMapper();

        // Act
        String json = objectMapper.writeValueAsString(request);

        // Assert
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_UPDATED_AT + "\":\"2025-08-30T12:00:00.000Z\""), "JSON should contain updatedAt");
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_LOGIN_USER_ID + "\":\"user123\""), "JSON should contain loginUserId");
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_OPERATOR_NAME + "\":\"Operator Name\""), "JSON should contain operatorName");
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_OPERATOR_ADDRESS + "\":\"123 Main St\""), "JSON should contain operatorAddress");
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_OPEN_OPERATOR_ID + "\":\"open123\""), "JSON should contain openOperatorId");
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_GLOBAL_OPERATOR_ID + "\":\"global123\""), "JSON should contain globalOperatorId");
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
                + "\"" + Const.JSON_PROPERTY_LOGIN_USER_ID + "\":\"user123\","
                + "\"" + Const.JSON_PROPERTY_OPERATOR_NAME + "\":\"Operator Name\","
                + "\"" + Const.JSON_PROPERTY_OPERATOR_ADDRESS + "\":\"123 Main St\","
                + "\"" + Const.JSON_PROPERTY_OPEN_OPERATOR_ID + "\":\"open123\","
                + "\"" + Const.JSON_PROPERTY_GLOBAL_OPERATOR_ID + "\":\"global123\","
                + "\"extraField\":\"shouldBeIgnored\""
                + "}";

        ObjectMapper objectMapper = new ObjectMapper();

        // Act
        PutOperatorRequest request = objectMapper.readValue(json, PutOperatorRequest.class);

        // Assert
        assertEquals("2025-08-30T12:00:00.000Z", request.getUpdatedAt());
        assertEquals("user123", request.getLoginUserId());
        assertEquals("Operator Name", request.getOperatorName());
        assertEquals("123 Main St", request.getOperatorAddress());
        assertEquals("open123", request.getOpenOperatorId());
        assertEquals("global123", request.getGlobalOperatorId());
        // extraField is not present in the DTO, so it cannot be accessed
    }
}
