/*
 * EvaluateRequestTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the EvaluateRequest DTO.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.infrastructure.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.open_dataspaces.core.common.consts.Const;

/**
 * Unit tests for EvaluateRequest DTO.
 */
public class EvaluateRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // Register JavaTimeModule
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // Use ISO-8601 format

    // Common test data
    private final String commonSubjectType = "User";
    private final String commonSubjectId = "subject-123";
    private final String commonResourceType = "api";
    private final String commonResourceId = "/v1/resource/123";
    private final String commonActionName = "GET";
    private final LocalDateTime commonCurrentTime = LocalDateTime.now(ZoneOffset.UTC);

    @Test
    @DisplayName("parameterized constructor with all parameters")
    void testParameterizedConstructor() {
        EvaluateRequest dtoObject = new EvaluateRequest(
                commonSubjectType,
                commonSubjectId,
                commonResourceType,
                commonResourceId,
                commonActionName,
                commonCurrentTime
        );

        assertNotNull(dtoObject);
        assertEquals(dtoObject.getSubject().getType(), commonSubjectType);
        assertEquals(dtoObject.getSubject().getId(), commonSubjectId);
        assertEquals(dtoObject.getResource().getType(), commonResourceType);
        assertEquals(dtoObject.getResource().getId(), commonResourceId);
        assertEquals(dtoObject.getAction().getName(), commonActionName);
        assertEquals(dtoObject.getContext().getCurrentTime(), commonCurrentTime);
    }

    @Test
    @DisplayName("setters and getters")
    void testSettersAndGetters() {
        EvaluateRequest dtoObject = new EvaluateRequest(
                null,
                null,
                null,
                null,
                null,
                null
        );

        // Set values
        dtoObject.getSubject().setType(commonSubjectType);
        dtoObject.getSubject().setId(commonSubjectId);
        dtoObject.getResource().setType(commonResourceType);
        dtoObject.getResource().setId(commonResourceId);
        dtoObject.getAction().setName(commonActionName);
        dtoObject.getContext().setCurrentTime(commonCurrentTime);

        // Assert values
        assertEquals(dtoObject.getSubject().getType(), commonSubjectType);
        assertEquals(dtoObject.getSubject().getId(), commonSubjectId);
        assertEquals(dtoObject.getResource().getType(), commonResourceType);
        assertEquals(dtoObject.getResource().getId(), commonResourceId);
        assertEquals(dtoObject.getAction().getName(), commonActionName);
        assertEquals(dtoObject.getContext().getCurrentTime(), commonCurrentTime);
    }

    @Test
    @DisplayName("toString method")
    void testToString() {
        EvaluateRequest dtoObject = new EvaluateRequest(
                commonSubjectType,
                commonSubjectId,
                commonResourceType,
                commonResourceId,
                commonActionName,
                commonCurrentTime
        );

        String result = dtoObject.toString();

        assertTrue(result.contains(commonSubjectType), "toString should include subject type");
        assertTrue(result.contains(commonSubjectId), "toString should include subject id");
        assertTrue(result.contains(commonResourceType), "toString should include resource type");
        assertTrue(result.contains(commonResourceId), "toString should include resource id");
        assertTrue(result.contains(commonActionName), "toString should include action name");
        assertTrue(result.contains(commonCurrentTime.toString()), "toString should include current time");
    }

    @Test
    @DisplayName("Equals and HashCode")
    void testEqualsAndHashCode() {
        EvaluateRequest dtoObject = new EvaluateRequest(
                commonSubjectType,
                commonSubjectId,
                commonResourceType,
                commonResourceId,
                commonActionName,
                commonCurrentTime
        );

        EvaluateRequest dtoObjectEquals = new EvaluateRequest(
                commonSubjectType,
                commonSubjectId,
                commonResourceType,
                commonResourceId,
                commonActionName,
                commonCurrentTime
        );

        EvaluateRequest dtoObjectDiff = new EvaluateRequest(
                commonSubjectType + "Diff",
                commonSubjectId + "Diff",
                commonResourceType + "Diff",
                commonResourceId + "Diff",
                commonActionName + "Diff",
                commonCurrentTime.plusDays(1)
        );

        // Assert equality
        assertEquals(dtoObject, dtoObjectEquals, "DTO objects with same values should be equal");
        assertNotEquals(dtoObject, dtoObjectDiff, "DTO objects with different values should not be equal");
        // Assert hash codes
        assertEquals(dtoObject.hashCode(), dtoObjectEquals.hashCode(), "Hash codes should match for equal objects");
        assertNotEquals(dtoObject.hashCode(), dtoObjectDiff.hashCode(), "Hash codes should differ for different objects");
    }

    @Test
    @DisplayName("JSON serialization")
    void testJsonSerialization() throws Exception {
        EvaluateRequest dtoObject = new EvaluateRequest(
                commonSubjectType,
                commonSubjectId,
                commonResourceType,
                commonResourceId,
                commonActionName,
                commonCurrentTime
        );
        // Act
        String json = objectMapper.writeValueAsString(dtoObject);

        // Assert
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_CURRENT_TIME + "\":\"" + dtoObject.getContext().getCurrentTimeAsString() + "\""), "JSON should contain currentTime: " + json);
        assertFalse(json.contains("currentTime"), "JSON should not contain field named currentTime");   // Because of @JsonIgnore

        dtoObject.getContext().setCurrentTime(null);
        json = objectMapper.writeValueAsString(dtoObject);
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_CURRENT_TIME + "\":null"), "JSON should contain currentTime: " + json);
    }
}
