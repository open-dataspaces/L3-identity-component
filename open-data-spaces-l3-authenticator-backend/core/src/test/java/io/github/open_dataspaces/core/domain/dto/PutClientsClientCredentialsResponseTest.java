/*
 * PutClientsClientCredentialsResponseTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * Unit tests for the PutClientsClientCredentialsResponse DTO, covering construction, property access, equality, and JSON serialization.
 *
 * Date: 2026/2/25
 */

package io.github.open_dataspaces.core.domain.dto;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.open_dataspaces.core.common.consts.Const;

class PutClientsClientCredentialsResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    // Common test data
    private final String name = "Sample Client";
    private final String description = "Sample Description";
    private final String flowType = "authorization_code";
    private final String openSystemId = "open-system";
    private final String operatorId = "operator-xyz";

    /**
     * Test for the default constructor.
     */
    @Test
    @DisplayName("Default constructor initializes all fields to null")
    void testDefaultConstructor() {
        PutClientsClientCredentialsResponse dto = new PutClientsClientCredentialsResponse();
        assertNull(dto.getName());
        assertNull(dto.getDescription());
        assertNull(dto.getFlowType());
        assertNull(dto.getOpenSystemId());
        assertNull(dto.getOperatorId());
    }

    /**
     * Test for the all-args constructor.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // name description
        "name, description",
        "NULL, NULL",
    }, nullValues = "NULL")
    @DisplayName("All-args constructor sets all fields")
    void testAllArgsConstructor(String argName, String argDescription) {
        PutClientsClientCredentialsResponse dto = new PutClientsClientCredentialsResponse(
                argName, argDescription, flowType, openSystemId, operatorId
        );
        assertEquals(argName, dto.getName());
        assertEquals(argDescription, dto.getDescription());
        assertEquals(flowType, dto.getFlowType());
        assertEquals(openSystemId, dto.getOpenSystemId());
        assertEquals(operatorId, dto.getOperatorId());
    }

    /**
     * Test for setters and getters of PostClientsRequest.
     */
    @Test
    @DisplayName("Setters and getters work as expected")
    void testSettersAndGetters() {
        PutClientsClientCredentialsResponse dto = new PutClientsClientCredentialsResponse();
        dto.setName(name);
        dto.setDescription(description);
        dto.setFlowType(flowType);
        dto.setOpenSystemId(openSystemId);
        dto.setOperatorId(operatorId);

        assertEquals(name, dto.getName());
        assertEquals(description, dto.getDescription());
        assertEquals(flowType, dto.getFlowType());
        assertEquals(openSystemId, dto.getOpenSystemId());
        assertEquals(operatorId, dto.getOperatorId());
    }

    /**
     * Test toString() method of PostClientsRequest.
     */
    @Test
    @DisplayName("toString includes all property values")
    void testToString() {
        PutClientsClientCredentialsResponse dto = new PutClientsClientCredentialsResponse(
                name, description, flowType, openSystemId, operatorId
        );
        String str = dto.toString();
        assertTrue(str.contains(name));
        assertTrue(str.contains(description));
        assertTrue(str.contains(flowType));
        assertTrue(str.contains(openSystemId));
        assertTrue(str.contains(operatorId));
    }

    /**
     * Test for equals and hashCode methods.
     */
    @Test
    @DisplayName("Equals and hashCode are consistent for identical and different objects")
    void testEqualsAndHashCode() {
        PutClientsClientCredentialsResponse dto1 = new PutClientsClientCredentialsResponse(
                name, description, flowType, openSystemId, operatorId
        );
        PutClientsClientCredentialsResponse dto2 = new PutClientsClientCredentialsResponse(
                name, description, flowType, openSystemId, operatorId
        );
        PutClientsClientCredentialsResponse dto3 = new PutClientsClientCredentialsResponse(
                "name1", description, flowType, openSystemId, operatorId
        );
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    /**
     * Tests whether the @JsonProperty annotation of PostClientsRequest is set correctly.
     */
    @ParameterizedTest
    @CsvSource({
        "name, " + Const.JSON_PROPERTY_CLIENT_NAME,
        "description, " + Const.JSON_PROPERTY_CLIENT_DESCRIPTION,
        "flowType, " + Const.JSON_PROPERTY_FLOW_TYPE,
        "openSystemId, " + Const.JSON_PROPERTY_OPEN_SYSTEM_ID,
        "operatorId, " + Const.JSON_PROPERTY_OPERATOR_ID,
    })
    @DisplayName("JsonProperty annotation values are correct")
    void testJsonPropertyNames(String fieldName, String jsonPropertyName) throws Exception {
        Field field = PutClientsClientCredentialsResponse.class.getDeclaredField(fieldName);
        JsonProperty annotation = field.getAnnotation(JsonProperty.class);
        assertNotNull(annotation, "JsonProperty annotation should be present for field: " + fieldName);
        assertEquals(jsonPropertyName, annotation.value(), "JsonProperty value mismatch for field: " + fieldName);
    }

    /**
     * Test JSON serialization of PostClientsRequest.
     */
    @Test
    @DisplayName("JSON serialization produces expected property names and values")
    void testJsonSerialization() throws Exception {
        PutClientsClientCredentialsResponse dto = new PutClientsClientCredentialsResponse(
                name, description, flowType, openSystemId, operatorId
        );
        String json = objectMapper.writeValueAsString(dto);
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_CLIENT_NAME + "\":\"" + name + "\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_CLIENT_DESCRIPTION + "\":\"" + description + "\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_FLOW_TYPE + "\":\"" + flowType + "\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_OPEN_SYSTEM_ID + "\":\"" + openSystemId + "\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_OPERATOR_ID + "\":\"" + operatorId + "\""));
    }
}