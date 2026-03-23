/*
 * OperatorResponseTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for OperatorResponse DTO.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.open_dataspaces.core.common.consts.Const;

/**
 * Unit tests for the OperatorResponse class.
 */
class OperatorResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // Register JavaTimeModule
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // Use ISO-8601 format

    /**
     * Test the no-args constructor.
     * Ensures that the object is created with default values.
     */
    @Test
    @DisplayName("Test no-args constructor")
    void testNoArgsConstructor() {
        // Act
        OperatorResponse response = new OperatorResponse();

        // Assert
        assertNotNull(response);
    }

    /**
     * Test the parameterized constructor with valid inputs.
     * Ensures that the object is correctly populated.
     */
    @Test
    @DisplayName("Test parameterized constructor with valid inputs")
    void testParameterizedConstructor() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2030, 12, 31);
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2025, 1, 2, 11, 0);

        // Act
        OperatorResponse response = new OperatorResponse(
                "loginUserId1",
                "operatorId",
                "operatorName",
                "address1",
                "openOperatorId1",
                "globalOperatorId1",
                startDate,
                endDate,
                false,
                createdAt,
                updatedAt
        );

        // Assert
        assertNotNull(response);
        assertEquals("loginUserId1", response.getLoginUserId());
        assertEquals("operatorId", response.getOperatorId());
        assertEquals("operatorName", response.getOperatorName());
        assertEquals("address1", response.getOperatorAddress());
        assertEquals("openOperatorId1", response.getOpenOperatorId());
        assertEquals("globalOperatorId1", response.getGlobalOperatorId());
        assertEquals(false, response.isDeletedFlag());
        assertEquals(startDate, response.getEffectiveStartDate());
        assertEquals(endDate, response.getEffectiveEndDate());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }

    /**
     * Test the constructor that initializes the response from an OperatorResult.
     */
    @Test
    @DisplayName("Test OperatorResponse Constructor with OperatorResult")
    void testConstructorWithOperatorResult() {
        // Arrange
        OperatorResult operatorResult = new OperatorResult(
                "operatorId123",
                "Operator Name",
                "123 Main St",
                "openOperatorId123",
                "globalOperatorId123",
                false,
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2025-12-31"),
                LocalDateTime.parse("2025-01-01T10:15:30"),
                "creator123",
                LocalDateTime.parse("2025-01-02T10:15:30"),
                "updater123"
        );

        // Act
        OperatorResponse response = new OperatorResponse(operatorResult);

        // Assert
        assertEquals("operatorId123", response.getOperatorId(), "operatorId should match");
        assertEquals("Operator Name", response.getOperatorName(), "operatorName should match");
        assertEquals("123 Main St", response.getOperatorAddress(), "operatorAddress should match");
        assertEquals("openOperatorId123", response.getOpenOperatorId(), "openOperatorId should match");
        assertEquals("globalOperatorId123", response.getGlobalOperatorId(), "globalOperatorId should match");
        assertEquals(false, response.isDeletedFlag(), "deletedFlag should match");
        assertEquals(LocalDate.parse("2025-01-01"), response.getEffectiveStartDate(), "effectiveStartDate should match");
        assertEquals(LocalDate.parse("2025-12-31"), response.getEffectiveEndDate(), "effectiveEndDate should match");
        assertEquals(LocalDateTime.parse("2025-01-01T10:15:30"), response.getCreatedAt(), "createdAt should match");
        assertEquals(LocalDateTime.parse("2025-01-02T10:15:30"), response.getUpdatedAt(), "updatedAt should match");

        // Fields not included in OperatorResult should be null
        assertNull(response.getLoginUserId(), "loginUserId should be null");
    }

    /**
     * Test the setters and getters.
     */
    @Test
    @DisplayName("Test parameterized constructor - setters and getters")
    void testSettersAndGetters() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2030, 12, 31);
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2025, 1, 2, 11, 0);

        OperatorResponse response = new OperatorResponse();

        response.setLoginUserId("loginUserId");
        response.setOperatorId("operatorId");
        response.setOperatorName("operatorName");
        response.setOperatorAddress("address1");
        response.setOpenOperatorId("openOperatorId1");
        response.setGlobalOperatorId("globalOperatorId1");
        response.setDeletedFlag(false);
        response.setEffectiveStartDate(startDate);
        response.setEffectiveEndDate(endDate);
        response.setCreatedAt(createdAt);
        response.setUpdatedAt(updatedAt);

        // Assert
        assertEquals("loginUserId", response.getLoginUserId());
        assertEquals("operatorId", response.getOperatorId());
        assertEquals("operatorName", response.getOperatorName());
        assertEquals("address1", response.getOperatorAddress());
        assertEquals("openOperatorId1", response.getOpenOperatorId());
        assertEquals("globalOperatorId1", response.getGlobalOperatorId());
        assertEquals(false, response.isDeletedFlag());
        assertEquals(startDate, response.getEffectiveStartDate());
        assertEquals(endDate, response.getEffectiveEndDate());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }

    /**
     * Test the toString method.
     * Ensures that the string representation includes all fields.
     */
    @Test
    @DisplayName("Test toString method")
    void testToString() {
        // Arrange
        OperatorResponse response = new OperatorResponse(
                "loginUserId",
                "operatorId",
                "operatorName",
                "operatorAddress",
                "openOperatorId",
                "globalOperatorId",
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2025-12-31"),
                false,
                LocalDateTime.parse("2025-01-01T10:15:30"),
                LocalDateTime.parse("2025-01-02T10:15:30")
        );

        // Act
        String result = response.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("loginUserId"));
        assertTrue(result.contains("operatorId"));
        assertTrue(result.contains("operatorName"));
        assertTrue(result.contains("operatorAddress"));
        assertTrue(result.contains("openOperatorId"));
        assertTrue(result.contains("globalOperatorId"));
        assertTrue(result.contains("2025-01-01"));
        assertTrue(result.contains("2025-12-31"));
    }

    /**
     * Test the equals and hashCode methods.
     * Ensures that objects with the same values are considered equal.
     */
    @Test
    @DisplayName("Test equals and hashCode methods")
    void testEqualsAndHashCode() {
        // Arrange
        OperatorResponse response1 = new OperatorResponse(
                "loginUserId",
                "operatorId",
                "operatorName",
                "operatorAddress",
                "openOperatorId",
                "globalOperatorId",
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2025-12-31"),
                false,
                LocalDateTime.parse("2025-01-01T10:15:30"),
                LocalDateTime.parse("2025-01-02T10:15:30")
        );

        OperatorResponse response2 = new OperatorResponse(
                "loginUserId",
                "operatorId",
                "operatorName",
                "operatorAddress",
                "openOperatorId",
                "globalOperatorId",
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2025-12-31"),
                false,
                LocalDateTime.parse("2025-01-01T10:15:30"),
                LocalDateTime.parse("2025-01-02T10:15:30")
        );

        // Act & Assert
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    /**
     * Tests whether the @JsonProperty annotation of OperatorResponse is set correctly.
     */
    @Test
    @DisplayName("Test @JsonProperty annotations")
    void testJsonPropertyNames() throws Exception {
        // Retrieve fields from the OperatorResponse class
        Field fieldLoginUserId = OperatorResponse.class.getDeclaredField("loginUserId");
        Field fieldOperatorId = OperatorResponse.class.getDeclaredField("operatorId");
        Field fieldOperatorName = OperatorResponse.class.getDeclaredField("operatorName");
        Field fieldOperatorAddress = OperatorResponse.class.getDeclaredField("operatorAddress");
        Field fieldOpenOperatorId = OperatorResponse.class.getDeclaredField("openOperatorId");
        Field fieldGlobalOperatorId = OperatorResponse.class.getDeclaredField("globalOperatorId");
        Field fieldEffectiveStartDate = OperatorResponse.class.getDeclaredField("effectiveStartDate");
        Field fieldEffectiveEndDate = OperatorResponse.class.getDeclaredField("effectiveEndDate");
        Field fieldIsDeleted = OperatorResponse.class.getDeclaredField("deletedFlag");
        Field fieldCreatedAt = OperatorResponse.class.getDeclaredField("createdAt");
        Field fieldUpdatedAt = OperatorResponse.class.getDeclaredField("updatedAt");

        // Retrieve @JsonProperty annotations
        JsonProperty annotationLoginUserId = fieldLoginUserId.getAnnotation(JsonProperty.class);
        JsonProperty annotationOperatorId = fieldOperatorId.getAnnotation(JsonProperty.class);
        JsonProperty annotationOperatorName = fieldOperatorName.getAnnotation(JsonProperty.class);
        JsonProperty annotationOperatorAddress = fieldOperatorAddress.getAnnotation(JsonProperty.class);
        JsonProperty annotationOpenOperatorId = fieldOpenOperatorId.getAnnotation(JsonProperty.class);
        JsonProperty annotationGlobalOperatorId = fieldGlobalOperatorId.getAnnotation(JsonProperty.class);
        JsonProperty annotationEffectiveStartDate = fieldEffectiveStartDate.getAnnotation(JsonProperty.class);
        JsonProperty annotationEffectiveEndDate = fieldEffectiveEndDate.getAnnotation(JsonProperty.class);
        JsonProperty annotationIsDeleted = fieldIsDeleted.getAnnotation(JsonProperty.class);
        JsonProperty annotationCreatedAt = fieldCreatedAt.getAnnotation(JsonProperty.class);
        JsonProperty annotationUpdatedAt = fieldUpdatedAt.getAnnotation(JsonProperty.class);

        // Assert
        assertNotNull(annotationLoginUserId);
        assertEquals(Const.JSON_PROPERTY_LOGIN_USER_ID, annotationLoginUserId.value());
        assertNotNull(annotationOperatorId);
        assertEquals(Const.JSON_PROPERTY_OPERATOR_ID, annotationOperatorId.value());
        assertNotNull(annotationOperatorName);
        assertEquals(Const.JSON_PROPERTY_OPERATOR_NAME, annotationOperatorName.value());
        assertNotNull(annotationOperatorAddress);
        assertEquals(Const.JSON_PROPERTY_OPERATOR_ADDRESS, annotationOperatorAddress.value());
        assertNotNull(annotationOpenOperatorId);
        assertEquals(Const.JSON_PROPERTY_OPEN_OPERATOR_ID, annotationOpenOperatorId.value());
        assertNotNull(annotationGlobalOperatorId);
        assertEquals(Const.JSON_PROPERTY_GLOBAL_OPERATOR_ID, annotationGlobalOperatorId.value());
        assertNotNull(annotationEffectiveStartDate);
        assertEquals(Const.JSON_PROPERTY_EFFECTIVE_START_DATE, annotationEffectiveStartDate.value());
        assertNotNull(annotationEffectiveEndDate);
        assertEquals(Const.JSON_PROPERTY_EFFECTIVE_END_DATE, annotationEffectiveEndDate.value());
        assertNotNull(annotationIsDeleted);
        assertEquals(Const.JSON_PROPERTY_DELETED_FLAG, annotationIsDeleted.value());
        assertNotNull(annotationCreatedAt);
        assertEquals(Const.JSON_PROPERTY_CREATED_AT, annotationCreatedAt.value());
        assertNotNull(annotationUpdatedAt);
        assertEquals(Const.JSON_PROPERTY_UPDATED_AT, annotationUpdatedAt.value());
    }

    /**
     * Test JSON serialization of OperatorResponse.
     */
    @Test
    @DisplayName("Test JSON serialization")
    void testJsonSerialization() throws JsonProcessingException {
        // Arrange
        OperatorResponse response = new OperatorResponse(
                "loginUserId",
                "operatorId",
                "operatorName",
                "operatorAddress",
                "openOperatorId",
                "globalOperatorId",
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2025-12-31"),
                false,
                LocalDateTime.parse("2025-01-01T10:15:30"),
                LocalDateTime.parse("2025-01-02T10:15:30")
        );

        // Act
        String json = objectMapper.writeValueAsString(response);

        // Assert
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_LOGIN_USER_ID + "\":\"loginUserId\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_OPERATOR_ID + "\":\"operatorId\""));
    }

    /**
     * Test that globalOperatorId field does not have @JsonInclude annotation.
     */
    @Test
    @DisplayName("Test globalOperatorId field not have @JsonInclude annotation")
    void testJsonIncludeNotForGlobalOperatorId() throws JsonProcessingException {
        // Arrange
        OperatorResponse responseWithGlobalOperatorId = new OperatorResponse(
                "loginUserId",
                "operatorId",
                "operatorName",
                "operatorAddress",
                "openOperatorId",
                "globalOperatorId", // globalOperatorId is not null
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2025-12-31"),
                false,
                LocalDateTime.parse("2025-01-01T10:15:30"),
                LocalDateTime.parse("2025-01-02T10:15:30")
        );

        OperatorResponse responseWithEmptyGlobalOperatorId = new OperatorResponse(
                "loginUserId",
                "operatorId",
                "operatorName",
                "operatorAddress",
                "openOperatorId",
                "", // globalOperatorId is empty string
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2025-12-31"),
                false,
                LocalDateTime.parse("2025-01-01T10:15:30"),
                LocalDateTime.parse("2025-01-02T10:15:30")
        );

        OperatorResponse responseWithNullGlobalOperatorId = new OperatorResponse(
                "loginUserId",
                "operatorId",
                "operatorName",
                "operatorAddress",
                "openOperatorId",
                null, // globalOperatorId is null
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2025-12-31"),
                false,
                LocalDateTime.parse("2025-01-01T10:15:30"),
                LocalDateTime.parse("2025-01-02T10:15:30")
        );

        // Act
        String jsonWithGlobalOperatorId = objectMapper.writeValueAsString(responseWithGlobalOperatorId);
        String jsonWithEmptyGlobalOperatorId = objectMapper.writeValueAsString(responseWithEmptyGlobalOperatorId);
        String jsonWithNullGlobalOperatorId = objectMapper.writeValueAsString(responseWithNullGlobalOperatorId);

        // Assert
        // globalOperatorId should always be included in the JSON regardless of its value
        assertTrue(jsonWithGlobalOperatorId.contains("\"global_operator_id\":\"globalOperatorId\""), "global_operator_id should be included in JSON when not null");
        assertTrue(jsonWithEmptyGlobalOperatorId.contains("\"global_operator_id\":\"\""), "global_operator_id be included in JSON when empty");
        assertTrue(jsonWithNullGlobalOperatorId.contains("\"global_operator_id\":null"), "global_operator_id be included in JSON when null");
    }
}
