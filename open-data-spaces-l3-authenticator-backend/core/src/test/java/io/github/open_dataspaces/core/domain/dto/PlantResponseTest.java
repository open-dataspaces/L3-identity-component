/*
 * PlantResponseTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for PlantResponse DTO.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
 * Unit tests for the PlantResponse class.
 */
class PlantResponseTest {

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
        PlantResponse response = new PlantResponse();

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
        PlantResponse response = new PlantResponse(
                "plantId1",
                "operatorId",
                "plantName",
                "address1",
                "openId1",
                "globalId1",
                startDate,
                endDate,
                false,
                createdAt,
                updatedAt
        );

        // Assert
        assertNotNull(response);
        assertEquals("plantId1", response.getPlantId());
        assertEquals("operatorId", response.getOperatorId());
        assertEquals("plantName", response.getPlantName());
        assertEquals("address1", response.getPlantAddress());
        assertEquals("openId1", response.getOpenPlantId());
        assertEquals("globalId1", response.getGlobalPlantId());
        assertEquals(false, response.isDeletedFlag());
        assertEquals(startDate, response.getEffectiveStartDate());
        assertEquals(endDate, response.getEffectiveEndDate());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }

    /**
     * Test the constructor that initializes the response from an PlantResult.
     */
    @Test
    @DisplayName("Test PlantResponse Constructor with PlantResult")
    void testConstructorWithPlantResult() {
        // Arrange
        PlantResult plantResult = new PlantResult(
                "plantId123",
                "operatorId123",
                "Plant Name",
                "123 Main St",
                "openId123",
                "globalId123",
                false,
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2025-12-31"),
                LocalDateTime.parse("2025-01-01T10:15:30"),
                "creator123",
                LocalDateTime.parse("2025-01-02T10:15:30"),
                "updater123"
        );

        // Act
        PlantResponse response = new PlantResponse(plantResult);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals("plantId123", response.getPlantId(), "plantId should match");
        assertEquals("operatorId123", response.getOperatorId(), "operatorId should match");
        assertEquals("Plant Name", response.getPlantName(), "plantName should match");
        assertEquals("123 Main St", response.getPlantAddress(), "plantAddress should match");
        assertEquals("openId123", response.getOpenPlantId(), "openPlantId should match");
        assertEquals("globalId123", response.getGlobalPlantId(), "globalPlantId should match");
        assertEquals(false, response.isDeletedFlag(), "deletedFlag should match");
        assertEquals(LocalDate.parse("2025-01-01"), response.getEffectiveStartDate(), "effectiveStartDate should match");
        assertEquals(LocalDate.parse("2025-12-31"), response.getEffectiveEndDate(), "effectiveEndDate should match");
        assertEquals(LocalDateTime.parse("2025-01-01T10:15:30"), response.getCreatedAt(), "createdAt should match");
        assertEquals(LocalDateTime.parse("2025-01-02T10:15:30"), response.getUpdatedAt(), "updatedAt should match");
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

        PlantResponse response = new PlantResponse();
        response.setPlantId("plantId1");
        response.setOperatorId("operatorId");
        response.setPlantName("plantName");
        response.setPlantAddress("address1");
        response.setOpenPlantId("openId1");
        response.setGlobalPlantId("globalId1");
        response.setDeletedFlag(false);
        response.setEffectiveStartDate(startDate);
        response.setEffectiveEndDate(endDate);
        response.setCreatedAt(createdAt);
        response.setUpdatedAt(updatedAt);

        // Assert
        assertEquals("plantId1", response.getPlantId());
        assertEquals("operatorId", response.getOperatorId());
        assertEquals("plantName", response.getPlantName());
        assertEquals("address1", response.getPlantAddress());
        assertEquals("openId1", response.getOpenPlantId());
        assertEquals("globalId1", response.getGlobalPlantId());
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
        PlantResponse response = new PlantResponse(
                "plantId",
                "operatorId",
                "plantName",
                "plantAddress",
                "openId",
                "globalId",
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
        assertTrue(result.contains("plantId"));
        assertTrue(result.contains("operatorId"));
        assertTrue(result.contains("plantName"));
        assertTrue(result.contains("plantAddress"));
        assertTrue(result.contains("openId"));
        assertTrue(result.contains("globalId"));
        assertTrue(result.contains("2025-01-01"));
        assertTrue(result.contains("2025-12-31"));
        assertTrue(result.contains("false"));
        assertTrue(result.contains("2025-01-01T10:15:30"));
        assertTrue(result.contains("2025-01-02T10:15:30"));
    }

    /**
     * Test the equals and hashCode methods.
     * Ensures that objects with the same values are considered equal.
     */
    @Test
    @DisplayName("Test equals and hashCode methods")
    void testEqualsAndHashCode() {
        // Arrange
        PlantResponse response1 = new PlantResponse(
                "plantId",
                "operatorId",
                "plantName",
                "plantAddress",
                "openId",
                "globalId",
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2025-12-31"),
                false,
                LocalDateTime.parse("2025-01-01T10:15:30"),
                LocalDateTime.parse("2025-01-02T10:15:30")
        );

        PlantResponse response2 = new PlantResponse(
                "plantId",
                "operatorId",
                "plantName",
                "plantAddress",
                "openId",
                "globalId",
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
     * Tests whether the @JsonProperty annotation of PlantResponse is set correctly.
     */
    @Test
    @DisplayName("Test @JsonProperty annotations")
    void testJsonPropertyNames() throws Exception {
        // Retrieve fields from the PlantResponse class
        Field fieldPlantId = PlantResponse.class.getDeclaredField("plantId");
        Field fieldOperatorId = PlantResponse.class.getDeclaredField("operatorId");
        Field fieldPlantName = PlantResponse.class.getDeclaredField("plantName");
        Field fieldPlantAddress = PlantResponse.class.getDeclaredField("plantAddress");
        Field fieldOpenPlantId = PlantResponse.class.getDeclaredField("openPlantId");
        Field fieldGlobalPlantId = PlantResponse.class.getDeclaredField("globalPlantId");
        Field fieldEffectiveStartDate = PlantResponse.class.getDeclaredField("effectiveStartDate");
        Field fieldEffectiveEndDate = PlantResponse.class.getDeclaredField("effectiveEndDate");
        Field fieldIsDeleted = PlantResponse.class.getDeclaredField("deletedFlag");
        Field fieldCreatedAt = PlantResponse.class.getDeclaredField("createdAt");
        Field fieldUpdatedAt = PlantResponse.class.getDeclaredField("updatedAt");

        // Retrieve @JsonProperty annotations
        JsonProperty annotationPlantId = fieldPlantId.getAnnotation(JsonProperty.class);
        JsonProperty annotationOperatorId = fieldOperatorId.getAnnotation(JsonProperty.class);
        JsonProperty annotationPlantName = fieldPlantName.getAnnotation(JsonProperty.class);
        JsonProperty annotationPlantAddress = fieldPlantAddress.getAnnotation(JsonProperty.class);
        JsonProperty annotationOpenPlantId = fieldOpenPlantId.getAnnotation(JsonProperty.class);
        JsonProperty annotationGlobalPlantId = fieldGlobalPlantId.getAnnotation(JsonProperty.class);
        JsonProperty annotationEffectiveStartDate = fieldEffectiveStartDate.getAnnotation(JsonProperty.class);
        JsonProperty annotationEffectiveEndDate = fieldEffectiveEndDate.getAnnotation(JsonProperty.class);
        JsonProperty annotationIsDeleted = fieldIsDeleted.getAnnotation(JsonProperty.class);
        JsonProperty annotationCreatedAt = fieldCreatedAt.getAnnotation(JsonProperty.class);
        JsonProperty annotationUpdatedAt = fieldUpdatedAt.getAnnotation(JsonProperty.class);

        // Assert
        assertNotNull(annotationPlantId);
        assertEquals(Const.JSON_PROPERTY_PLANT_ID, annotationPlantId.value());
        assertNotNull(annotationOperatorId);
        assertEquals(Const.JSON_PROPERTY_OPERATOR_ID, annotationOperatorId.value());
        assertNotNull(annotationPlantName);
        assertEquals(Const.JSON_PROPERTY_PLANT_NAME, annotationPlantName.value());
        assertNotNull(annotationPlantAddress);
        assertEquals(Const.JSON_PROPERTY_PLANT_ADDRESS, annotationPlantAddress.value());
        assertNotNull(annotationOpenPlantId);
        assertEquals(Const.JSON_PROPERTY_OPEN_PLANT_ID, annotationOpenPlantId.value());
        assertNotNull(annotationGlobalPlantId);
        assertEquals(Const.JSON_PROPERTY_GLOBAL_PLANT_ID, annotationGlobalPlantId.value());
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
     * Test JSON serialization of PlantResponse.
     */
    @Test
    @DisplayName("Test JSON serialization")
    void testJsonSerialization() throws JsonProcessingException {
        // Arrange
        PlantResponse response = new PlantResponse(
                "plantId",
                "operatorId",
                "plantName",
                "plantAddress",
                "openId",
                "globalId",
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2025-12-31"),
                false,
                LocalDateTime.parse("2025-01-01T10:15:30"),
                LocalDateTime.parse("2025-01-02T10:15:30")
        );

        // Act
        String json = objectMapper.writeValueAsString(response);

        // Assert
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_PLANT_ID + "\":\"plantId\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_OPERATOR_ID + "\":\"operatorId\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_PLANT_NAME + "\":\"plantName\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_PLANT_ADDRESS + "\":\"plantAddress\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_OPEN_PLANT_ID + "\":\"openId\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_GLOBAL_PLANT_ID + "\":\"globalId\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_EFFECTIVE_START_DATE + "\":\"2025-01-01\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_EFFECTIVE_END_DATE + "\":\"2025-12-31\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_DELETED_FLAG + "\":false"));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_CREATED_AT + "\":\"2025-01-01T10:15:30"));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_UPDATED_AT + "\":\"2025-01-02T10:15:30"));
    }

    /**
     * Test that globalPlantId field does not have @JsonInclude annotation.
     */
    @Test
    @DisplayName("Test globalPlantId field not have @JsonInclude annotation")
    void testJsonIncludeNotForGlobalPlantId() throws JsonProcessingException {
        // Arrange
        PlantResponse responseWithGlobalPlantId = new PlantResponse(
                "plantId",
                "operatorId",
                "plantName",
                "plantAddress",
                "openId",
                "globalId",
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2025-12-31"),
                false,
                LocalDateTime.parse("2025-01-01T10:15:30"),
                LocalDateTime.parse("2025-01-02T10:15:30")
        );

        PlantResponse responseWithEmptyGlobalPlantId = new PlantResponse(
                "plantId",
                "operatorId",
                "plantName",
                "plantAddress",
                "openId",
                "", // globalOperatorId is empty string
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2025-12-31"),
                false,
                LocalDateTime.parse("2025-01-01T10:15:30"),
                LocalDateTime.parse("2025-01-02T10:15:30")
        );

        PlantResponse responseWithNullGlobalPlantId = new PlantResponse(
                "plantId",
                "operatorId",
                "plantName",
                "plantAddress",
                "openId",
                null, // globalOperatorId is null
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2025-12-31"),
                false,
                LocalDateTime.parse("2025-01-01T10:15:30"),
                LocalDateTime.parse("2025-01-02T10:15:30")
        );

        // Act
        String jsonWithGlobalPlantId = objectMapper.writeValueAsString(responseWithGlobalPlantId);
        String jsonWithEmptyGlobalPlantId = objectMapper.writeValueAsString(responseWithEmptyGlobalPlantId);
        String jsonWithNullGlobalPlantId = objectMapper.writeValueAsString(responseWithNullGlobalPlantId);

        // Assert
        // globalOperatorId should always be included in the JSON regardless of its value
        assertTrue(jsonWithGlobalPlantId.contains("\"global_plant_id\":\"globalId\""), "global_operator_id should be included in JSON when not null");
        assertTrue(jsonWithEmptyGlobalPlantId.contains("\"global_plant_id\":\"\""), "global_operator_id be included in JSON when empty");
        assertTrue(jsonWithNullGlobalPlantId.contains("\"global_plant_id\":null"), "global_operator_id be included in JSON when null");
    }
}
