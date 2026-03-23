/*
 * GetOperatorResponseTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * Unit tests for the GetOperatorResponse class.
 *
 * Date: 2025-06-30
 */

package io.github.open_dataspaces.core.domain.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.open_dataspaces.core.common.consts.Const;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Unit tests for GetOperatorResponse.
 */
public class GetOperatorResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // Register JavaTimeModule
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // Use ISO-8601 format

    /**
     * case#1:
     * testNoArgsConstructor tests the no-args constructor of GetOperatorResponse.
     */
    @Test
    void testNoArgsConstructor() {
        GetOperatorResponse response = new GetOperatorResponse();
        assertNull(response.getOperatorId());
        assertNull(response.getOperatorName());
        assertNull(response.getOperatorAddress());
        assertNull(response.getOpenOperatorId());
        assertNull(response.getGlobalOperatorId());
        assertNull(response.getEffectiveStartDate());
        assertNull(response.getEffectiveEndDate());
        assertNull(response.getDeletedFlag());
        assertNull(response.getCreatedAt());
        assertNull(response.getUpdatedAt());
    }

    /**
     * case#2:
     * testParameterizedConstructor tests the all-args constructor of GetOperatorResponse.
     */
    @Test
    void testParameterizedConstructor() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2030, 12, 31);
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2025, 1, 2, 11, 0);

        GetOperatorResponse response = new GetOperatorResponse(
                "id123",
                "テスト事業者",
                "テスト県テスト市テストビル1F",
                "openId123",
                "globalId123",
                startDate,
                endDate,
                true,
                createdAt,
                updatedAt
        );

        assertEquals("id123", response.getOperatorId());
        assertEquals("テスト事業者", response.getOperatorName());
        assertEquals("テスト県テスト市テストビル1F", response.getOperatorAddress());
        assertEquals("openId123", response.getOpenOperatorId());
        assertEquals("globalId123", response.getGlobalOperatorId());
        assertEquals(startDate, response.getEffectiveStartDate());
        assertEquals(endDate, response.getEffectiveEndDate());
        assertTrue(response.getDeletedFlag());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }

    /**
     * case#3:
     * testSettersAndGetters tests the setter and getter methods of GetOperatorResponse.
     */
    @Test
    void testSettersAndGetters() {
        GetOperatorResponse response = new GetOperatorResponse();

        response.setOperatorId("id999");
        response.setOperatorName("事業者B");
        response.setOperatorAddress("大阪府大阪市1-2-3");
        response.setOpenOperatorId("openId999");
        response.setGlobalOperatorId("globalId999");
        response.setEffectiveStartDate(LocalDate.of(2026, 5, 1));
        response.setEffectiveEndDate(LocalDate.of(2031, 4, 30));
        response.setDeletedFlag(false);
        response.setCreatedAt(LocalDateTime.of(2026, 5, 1, 8, 0));
        response.setUpdatedAt(LocalDateTime.of(2026, 5, 2, 9, 0));

        assertEquals("id999", response.getOperatorId());
        assertEquals("事業者B", response.getOperatorName());
        assertEquals("大阪府大阪市1-2-3", response.getOperatorAddress());
        assertEquals("openId999", response.getOpenOperatorId());
        assertEquals("globalId999", response.getGlobalOperatorId());
        assertEquals(LocalDate.of(2026, 5, 1), response.getEffectiveStartDate());
        assertEquals(LocalDate.of(2031, 4, 30), response.getEffectiveEndDate());
        assertFalse(response.getDeletedFlag());
        assertEquals(LocalDateTime.of(2026, 5, 1, 8, 0), response.getCreatedAt());
        assertEquals(LocalDateTime.of(2026, 5, 2, 9, 0), response.getUpdatedAt());
    }

    /**
     * case#4:
     * testToString tests the toString method of GetOperatorResponse.
     */
    @Test
    void testToString() {
        GetOperatorResponse response = new GetOperatorResponse();
        response.setOperatorId("id123");
        response.setOperatorName("テスト事業者");
        response.setOperatorAddress("テスト県テスト市テストビル1F");
        response.setOpenOperatorId("openId123");
        response.setGlobalOperatorId("globalId123");
        response.setEffectiveStartDate(LocalDate.of(2025, 1, 1));
        response.setEffectiveEndDate(LocalDate.of(2025, 12, 31));
        response.setDeletedFlag(true);
        response.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        response.setUpdatedAt(LocalDateTime.of(2025, 1, 2, 11, 0));

        String str = response.toString();
        assertTrue(str.contains("id123"));
        assertTrue(str.contains("テスト事業者"));
        assertTrue(str.contains("テスト県テスト市テストビル1F"));
        assertTrue(str.contains("openId123"));
        assertTrue(str.contains("globalId123"));
        assertTrue(str.contains("2025-01-01"));
        assertTrue(str.contains("2025-12-31"));
        assertTrue(str.contains("true"));
        assertTrue(str.contains("2025-01-01T10:00"));
        assertTrue(str.contains("2025-01-02T11:00"));
    }

    /**
     * case#5:
     * testEqualsAndHashCode tests the equals and hashCode methods of GetOperatorResponse.
     */
    @Test
    void testEqualsAndHashCode() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2030, 12, 31);
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2025, 1, 2, 11, 0);
        GetOperatorResponse response1 =  new GetOperatorResponse(
                "id123",
                "テスト事業者",
                "テスト県テスト市テストビル1F",
                "openId123",
                "globalId123",
                startDate,
                endDate,
                true,
                createdAt,
                updatedAt
        );

        GetOperatorResponse response2 =  new GetOperatorResponse(
                "id123",
                "テスト事業者",
                "テスト県テスト市テストビル1F",
                "openId123",
                "globalId123",
                startDate,
                endDate,
                true,
                createdAt,
                updatedAt
        );

        GetOperatorResponse response3 = new GetOperatorResponse(
                "id789",
                "テスト事業者3",
                "テスト県テスト市テストビル3F",
                "openId789",
                "globalId789",
                startDate,
                endDate,
                true,
                createdAt,
                updatedAt
        );

        // Test equals
        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
        assertNotEquals(response2, response3);

        // Test hashCode
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    /**
     * Tests whether the @JsonProperty annotation of OperatorResponse is set correctly.
     */
    @Test
    @DisplayName("Test @JsonProperty annotations")
    void testJsonPropertyNames() throws Exception {
        // Retrieve fields from the OperatorResponse class
        Field fieldOperatorId = GetOperatorResponse.class.getDeclaredField("operatorId");
        Field fieldOperatorName = GetOperatorResponse.class.getDeclaredField("operatorName");
        Field fieldOperatorAddress = GetOperatorResponse.class.getDeclaredField("operatorAddress");
        Field fieldOpenOperatorId = GetOperatorResponse.class.getDeclaredField("openOperatorId");
        Field fieldGlobalOperatorId = GetOperatorResponse.class.getDeclaredField("globalOperatorId");
        Field fieldEffectiveStartDate = GetOperatorResponse.class.getDeclaredField("effectiveStartDate");
        Field fieldEffectiveEndDate = GetOperatorResponse.class.getDeclaredField("effectiveEndDate");
        Field fieldIsDeleted = GetOperatorResponse.class.getDeclaredField("deletedFlag");
        Field fieldCreatedAt = GetOperatorResponse.class.getDeclaredField("createdAt");
        Field fieldUpdatedAt = GetOperatorResponse.class.getDeclaredField("updatedAt");

        // Retrieve @JsonProperty annotations
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
     * case#6:
     * testJsonSerialization tests JSON serialization of GetOperatorResponse.
     */
    @Test
    @DisplayName("GetOperatorResponse - JSON Serialization Test")
    void testJsonSerialization() throws Exception {
        // Arrange
        GetOperatorResponse response = new GetOperatorResponse();
        response.setOperatorId("id123");
        response.setOperatorName("Operator Name");
        response.setOperatorAddress("123 Main St");
        response.setOpenOperatorId("open123");
        response.setGlobalOperatorId("global123");
        response.setEffectiveStartDate(LocalDate.of(2025, 8, 30));
        response.setEffectiveEndDate(LocalDate.of(2025, 12, 31));
        response.setDeletedFlag(false);
        response.setCreatedAt(LocalDateTime.of(2025, 8, 30, 10, 0, 0));
        response.setUpdatedAt(LocalDateTime.of(2025, 8, 30, 12, 0, 0));

        // Act
        String json = objectMapper.writeValueAsString(response);
        System.out.println("JSON: " + json);

        // Assert
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_OPERATOR_ID + "\":\"id123\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_OPERATOR_NAME + "\":\"Operator Name\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_OPERATOR_ADDRESS + "\":\"123 Main St\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_OPEN_OPERATOR_ID + "\":\"open123\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_GLOBAL_OPERATOR_ID + "\":\"global123\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_EFFECTIVE_START_DATE + "\":\"2025-08-30\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_EFFECTIVE_END_DATE + "\":\"2025-12-31\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_DELETED_FLAG + "\":false"));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_CREATED_AT + "\":\"2025-08-30T10:00:00.000Z\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_UPDATED_AT + "\":\"2025-08-30T12:00:00.000Z\""));
    }
}