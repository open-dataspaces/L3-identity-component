/*
 * CreateStoreResponseTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the CreateStoreResponse DTO.
 *
 * Date: 2026/02/16
 */

package io.github.open_dataspaces.core.infrastructure.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
 * Unit tests for CreateStoreResponse DTO.
 */
public class CreateStoreResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    // Common test data
    private final String commonId = "store-123";
    private final String commonName = "Test Store";
    private final String commonCreatedAt = "2026-02-16T12:00:00Z";
    private final String commonUpdatedAt = "2026-02-16T13:00:00Z";

    @Test
    @DisplayName("Default Constructor")
    void testDefaultConstructor() {
        CreateStoreResponse dtoObject = new CreateStoreResponse();
        assertNotNull(dtoObject);
        assertNull(dtoObject.getId());
        assertNull(dtoObject.getName());
        assertNull(dtoObject.getCreatedAt());
        assertNull(dtoObject.getUpdatedAt());
    }

    @Test
    @DisplayName("AllArgsConstructor Test")
    void testAllArgsConstructor() {
        CreateStoreResponse dtoObject = new CreateStoreResponse(commonId, commonName, commonCreatedAt, commonUpdatedAt);
        assertEquals(commonId, dtoObject.getId());
        assertEquals(commonName, dtoObject.getName());
        assertEquals(commonCreatedAt, dtoObject.getCreatedAt());
        assertEquals(commonUpdatedAt, dtoObject.getUpdatedAt());
    }

    @Test
    @DisplayName("Setters and Getters Test")
    void testSettersAndGetters() {
        CreateStoreResponse dtoObject = new CreateStoreResponse();
        dtoObject.setId(commonId);
        dtoObject.setName(commonName);
        dtoObject.setCreatedAt(commonCreatedAt);
        dtoObject.setUpdatedAt(commonUpdatedAt);
        assertEquals(commonId, dtoObject.getId());
        assertEquals(commonName, dtoObject.getName());
        assertEquals(commonCreatedAt, dtoObject.getCreatedAt());
        assertEquals(commonUpdatedAt, dtoObject.getUpdatedAt());
    }

    @Test
    @DisplayName("toString Test")
    void testToString() {
        CreateStoreResponse dtoObject = new CreateStoreResponse(commonId, commonName, commonCreatedAt, commonUpdatedAt);
        String result = dtoObject.toString();
        assertTrue(result.contains(commonId), "toString should include id");
        assertTrue(result.contains(commonName), "toString should include name");
        assertTrue(result.contains(commonCreatedAt), "toString should include createdAt");
        assertTrue(result.contains(commonUpdatedAt), "toString should include updatedAt");
    }

    @Test
    @DisplayName("Equals and HashCode Test")
    void testEqualsAndHashCode() {
        CreateStoreResponse dtoObject = new CreateStoreResponse(commonId, commonName, commonCreatedAt, commonUpdatedAt);
        CreateStoreResponse dtoObjectSame = new CreateStoreResponse(commonId, commonName, commonCreatedAt, commonUpdatedAt);
        CreateStoreResponse dtoObjectDiff = new CreateStoreResponse("other-id", commonName, commonCreatedAt, commonUpdatedAt);
        assertEquals(dtoObject, dtoObjectSame, "Objects with same values should be equal");
        assertNotEquals(dtoObject, dtoObjectDiff, "Objects with different values should not be equal");
        assertEquals(dtoObject.hashCode(), dtoObjectSame.hashCode(), "Hash codes should be equal for same objects");
        assertNotEquals(dtoObject.hashCode(), dtoObjectDiff.hashCode(), "Hash codes should differ for different objects");
    }

    @ParameterizedTest
    @CsvSource(value = {
        "id, " + Const.JSON_PROPERTY_STORE_ID,
        "name, " + Const.JSON_PROPERTY_STORE_NAME,
        "createdAt, " + Const.JSON_PROPERTY_CREATED_AT,
        "updatedAt, " + Const.JSON_PROPERTY_UPDATED_AT
    })
    @DisplayName("CreateStoreResponse - @JsonProperty Annotation Test")
    void testJsonPropertyNames(String fieldName, String jsonPropertyName) throws Exception {
        Field field = CreateStoreResponse.class.getDeclaredField(fieldName);
        JsonProperty annotation = field.getAnnotation(JsonProperty.class);
        assertNotNull(annotation);
        assertEquals(jsonPropertyName, annotation.value());
    }

    @Test
    @DisplayName("JSON serialization")
    void testJsonSerialization() throws Exception {
        CreateStoreResponse dtoObject = new CreateStoreResponse(commonId, commonName, commonCreatedAt, commonUpdatedAt);
        String json = objectMapper.writeValueAsString(dtoObject);
        assertTrue(json.contains("\"id\":\"" + commonId + "\""), "JSON should contain id: " + json);
        assertTrue(json.contains("\"name\":\"" + commonName + "\""), "JSON should contain name: " + json);
        assertTrue(json.contains("\"created_at\":\"" + commonCreatedAt + "\""), "JSON should contain created_at: " + json);
        assertTrue(json.contains("\"updated_at\":\"" + commonUpdatedAt + "\""), "JSON should contain updated_at: " + json);
    }
}
