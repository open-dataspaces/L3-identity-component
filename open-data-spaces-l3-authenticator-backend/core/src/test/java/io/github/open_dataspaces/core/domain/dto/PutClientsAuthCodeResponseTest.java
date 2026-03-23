/*
 * PutClientsAuthCodeResponseTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * Unit tests for the PutClientsAuthCodeResponse DTO, covering construction, property access, equality, and JSON serialization.
 *
 * Date: 2026/2/25
 */

package io.github.open_dataspaces.core.domain.dto;

import java.lang.reflect.Field;
import java.util.List;

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

class PutClientsAuthCodeResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    // Common test data
    private final String name = "Sample Client";
    private final String description = "Sample Description";
    private final String flowType = "authorization_code";
    private final List<String> redirectUris = List.of("https://example.com/callback");

    /**
     * Test for the default constructor.
     */
    @Test
    @DisplayName("Default constructor initializes all fields to null")
    void testDefaultConstructor() {
        PutClientsAuthCodeResponse dto = new PutClientsAuthCodeResponse();
        assertNull(dto.getName());
        assertNull(dto.getDescription());
        assertNull(dto.getFlowType());
        assertNull(dto.getRedirectUris());
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
        PutClientsAuthCodeResponse dto = new PutClientsAuthCodeResponse(
                argName, argDescription, flowType, redirectUris
        );
        assertEquals(argName, dto.getName());
        assertEquals(argDescription, dto.getDescription());
        assertEquals(flowType, dto.getFlowType());
        assertEquals(redirectUris, dto.getRedirectUris());
    }

    /**
     * Test for setters and getters of PutClientsAuthCodeResponse.
     */
    @Test
    @DisplayName("Setters and getters work as expected")
    void testSettersAndGetters() {
        PutClientsAuthCodeResponse dto = new PutClientsAuthCodeResponse();
        dto.setName(name);
        dto.setDescription(description);
        dto.setFlowType(flowType);
        dto.setRedirectUris(redirectUris);

        assertEquals(name, dto.getName());
        assertEquals(description, dto.getDescription());
        assertEquals(flowType, dto.getFlowType());
        assertEquals(redirectUris, dto.getRedirectUris());
    }

    /**
     * Test toString() method of PutClientsAuthCodeResponse.
     */
    @Test
    @DisplayName("toString includes all property values")
    void testToString() {
        PutClientsAuthCodeResponse dto = new PutClientsAuthCodeResponse(
                name, description, flowType, redirectUris
        );
        String str = dto.toString();
        assertTrue(str.contains(name));
        assertTrue(str.contains(description));
        assertTrue(str.contains(flowType));
        assertTrue(str.contains(redirectUris.get(0)));
    }

    /**
     * Test for equals and hashCode methods.
     */
    @Test
    @DisplayName("Equals and hashCode are consistent for identical and different objects")
    void testEqualsAndHashCode() {
        PutClientsAuthCodeResponse dto1 = new PutClientsAuthCodeResponse(
                name, description, flowType, redirectUris
        );
        PutClientsAuthCodeResponse dto2 = new PutClientsAuthCodeResponse(
                name, description, flowType, redirectUris
        );
        PutClientsAuthCodeResponse dto3 = new PutClientsAuthCodeResponse(
                "other", description, flowType, redirectUris
        );
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    /**
     * Tests whether the @JsonProperty annotation of PutClientsAuthCodeResponse is set correctly.
     */
    @ParameterizedTest
    @CsvSource({
        "name, " + Const.JSON_PROPERTY_CLIENT_NAME,
        "description, " + Const.JSON_PROPERTY_CLIENT_DESCRIPTION,
        "flowType, " + Const.JSON_PROPERTY_FLOW_TYPE,
        "redirectUris, " + Const.JSON_PROPERTY_REDIRECT_URIS
    })
    @DisplayName("JsonProperty annotation values are correct")
    void testJsonPropertyNames(String fieldName, String jsonPropertyName) throws Exception {
        Field field = PutClientsAuthCodeResponse.class.getDeclaredField(fieldName);
        JsonProperty annotation = field.getAnnotation(JsonProperty.class);
        assertNotNull(annotation, "JsonProperty annotation should be present for field: " + fieldName);
        assertEquals(jsonPropertyName, annotation.value(), "JsonProperty value mismatch for field: " + fieldName);
    }

    /**
     * Test JSON serialization of PutClientsAuthCodeResponse.
     */
    @Test
    @DisplayName("JSON serialization produces expected property names and values")
    void testJsonSerialization() throws Exception {
        PutClientsAuthCodeResponse dto = new PutClientsAuthCodeResponse(
                name, description, flowType, redirectUris
        );
        String json = objectMapper.writeValueAsString(dto);
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_CLIENT_NAME + "\":\"" + name + "\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_CLIENT_DESCRIPTION + "\":\"" + description + "\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_FLOW_TYPE + "\":\"" + flowType + "\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_REDIRECT_URIS + "\":[")); // List serialization
        assertTrue(json.contains(redirectUris.get(0)));
    }
}