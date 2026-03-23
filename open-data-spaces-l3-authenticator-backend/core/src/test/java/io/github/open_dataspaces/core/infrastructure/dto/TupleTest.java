/*
 * TupleTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the Tuple DTO.
 *
 * Date: 2026/02/20
 */

package io.github.open_dataspaces.core.infrastructure.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Unit tests for Tuple DTO.
 */
public class TupleTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    // Common test data
    private final String commonUser = "user-123";
    private final String commonRelation = "member";
    private final String commonObject = "resource-123";

    @Test
    @DisplayName("AllArgsConstructor Test")
    void testAllArgsConstructor() {
        Tuple dtoObject = new Tuple(commonUser, commonRelation, commonObject);
        assertEquals(commonUser, dtoObject.getUser());
        assertEquals(commonRelation, dtoObject.getRelation());
        assertEquals(commonObject, dtoObject.getObject());
    }

    @Test
    @DisplayName("Setters and Getters Test")
    void testSettersAndGetters() {
        Tuple dtoObject = new Tuple("dummy-user", "dummy-relation", "dummy-object");
        dtoObject.setUser(commonUser);
        dtoObject.setRelation(commonRelation);
        dtoObject.setObject(commonObject);
        assertEquals(commonUser, dtoObject.getUser());
        assertEquals(commonRelation, dtoObject.getRelation());
        assertEquals(commonObject, dtoObject.getObject());
    }

    @Test
    @DisplayName("toString Test")
    void testToString() {
        Tuple dtoObject = new Tuple(commonUser, commonRelation, commonObject);
        String result = dtoObject.toString();
        assertTrue(result.contains(commonUser), "toString should include user");
        assertTrue(result.contains(commonRelation), "toString should include relation");
        assertTrue(result.contains(commonObject), "toString should include object");
    }

    @Test
    @DisplayName("Equals and HashCode Test")
    void testEqualsAndHashCode() {
        Tuple dtoObject = new Tuple(commonUser, commonRelation, commonObject);
        Tuple dtoObjectSame = new Tuple(commonUser, commonRelation, commonObject);
        Tuple dtoObjectDiff = new Tuple("other-user", commonRelation, commonObject);
        assertEquals(dtoObject, dtoObjectSame, "Objects with same values should be equal");
        assertNotEquals(dtoObject, dtoObjectDiff, "Objects with different values should not be equal");
        assertEquals(dtoObject.hashCode(), dtoObjectSame.hashCode(), "Hash codes should be equal for same objects");
        assertNotEquals(dtoObject.hashCode(), dtoObjectDiff.hashCode(), "Hash codes should differ for different objects");
    }

    @Test
    @DisplayName("JSON serialization")
    void testJsonSerialization() throws Exception {
        Tuple dtoObject = new Tuple(commonUser, commonRelation, commonObject);
        String json = objectMapper.writeValueAsString(dtoObject);
        assertTrue(json.contains("\"user\":\"" + commonUser + "\""), "JSON should contain user: " + json);
        assertTrue(json.contains("\"relation\":\"" + commonRelation + "\""), "JSON should contain relation: " + json);
        assertTrue(json.contains("\"object\":\"" + commonObject + "\""), "JSON should contain object: " + json);
    }
}
