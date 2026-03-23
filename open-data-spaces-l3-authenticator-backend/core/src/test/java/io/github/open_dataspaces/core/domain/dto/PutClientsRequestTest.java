/*
 * PutClientsRequestTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies the behavior and validation logic of the PutClientsRequest DTO.
 *
 * Date: 2026/2/25
 */

package io.github.open_dataspaces.core.domain.dto;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import io.github.open_dataspaces.core.common.consts.Const;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * PutClientsRequestTest.java.
 */
public class PutClientsRequestTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();
    // Common input parameters
    private final String commonFlowType = "authorization_code";
    private final String commonName = "Test Client";
    private final String commonDescription = "Test Description";
    private final String commonOperatorId = "123e4567-e89b-12d3-a456-426614174000";
    private final String commonOpenSystemId = "openSystemId";
    private final List<String> commonRedirectUris = List.of("https://example.com/callback");

    /**
     * Test for the default constructor.
     */
    @Test
    @DisplayName("PutClientsRequest - Default Constructor")
    void testDefaultConstructor() {
        PutClientsRequest request = new PutClientsRequest();
        assertNotNull(request);
        assertNull(request.getFlowType());
        assertNull(request.getName());
        assertNull(request.getDescription());
        assertNull(request.getOperatorId());
        assertNull(request.getOpenSystemId());
        assertNull(request.getRedirectUris());
    }

    /**
     * Test for the all args constructor.
     */
    @Test
    @DisplayName("PutClientsRequest - All Args Constructor")
    void testAllArgsConstructor() {
        PutClientsRequest request = new PutClientsRequest(
                commonFlowType,
                commonName,
                commonDescription,
                commonOperatorId,
                commonOpenSystemId,
                commonRedirectUris
        );
        assertEquals(commonFlowType, request.getFlowType());
        assertEquals(commonName, request.getName());
        assertEquals(commonDescription, request.getDescription());
        assertEquals(commonOperatorId, request.getOperatorId());
        assertEquals(commonOpenSystemId, request.getOpenSystemId());
        assertEquals(commonRedirectUris, request.getRedirectUris());
    }

    /**
     * Test for the no-args constructor.
     */
    @Test
    @DisplayName("PutClientsRequest - No Args Constructor")
    void testNoArgsConstructor() {
        PutClientsRequest request = new PutClientsRequest();
        assertNotNull(request);
        assertNull(request.getFlowType());
        assertNull(request.getName());
        assertNull(request.getDescription());
        assertNull(request.getOperatorId());
        assertNull(request.getOpenSystemId());
        assertNull(request.getRedirectUris());
    }

    /**
     * Test for setters and getters of PutClientsRequest.
     */
    @Test
    @DisplayName("PutClientsRequest - Setter and Getter Test")
    void testSettersAndGetters() {
        PutClientsRequest request = new PutClientsRequest();
        request.setFlowType(commonFlowType);
        request.setName(commonName);
        request.setDescription(commonDescription);
        request.setOperatorId(commonOperatorId);
        request.setOpenSystemId(commonOpenSystemId);
        request.setRedirectUris(commonRedirectUris);

        assertEquals(commonFlowType, request.getFlowType());
        assertEquals(commonName, request.getName());
        assertEquals(commonDescription, request.getDescription());
        assertEquals(commonOperatorId, request.getOperatorId());
        assertEquals(commonOpenSystemId, request.getOpenSystemId());
        assertEquals(commonRedirectUris, request.getRedirectUris());
    }

    /**
     * Test toString() method of PutClientsRequest.
     */
    @Test
    @DisplayName("PutClientsRequest - toString Test")
    void testToString() {
        PutClientsRequest request = new PutClientsRequest(
                commonFlowType,
                commonName,
                commonDescription,
                commonOperatorId,
                commonOpenSystemId,
                commonRedirectUris
        );
        String str = request.toString();
        assertTrue(str.contains(commonFlowType));
        assertTrue(str.contains(commonName));
        assertTrue(str.contains(commonDescription));
        assertTrue(str.contains(commonOperatorId));
        assertTrue(str.contains(commonOpenSystemId));
        assertTrue(str.contains(commonRedirectUris.get(0)));
    }

    /**
     * Test for equals and hashCode methods.
     */
    @Test
    @DisplayName("PutClientsRequest - Equals and HashCode Test")
    void testEqualsAndHashCode() {
        PutClientsRequest req1 = new PutClientsRequest(
                commonFlowType,
                commonName,
                commonDescription,
                commonOperatorId,
                commonOpenSystemId,
                commonRedirectUris
        );
        PutClientsRequest req2 = new PutClientsRequest(
                commonFlowType,
                commonName,
                commonDescription,
                commonOperatorId,
                commonOpenSystemId,
                commonRedirectUris
        );
        // Different values for negative test
        PutClientsRequest req3 = new PutClientsRequest(
                commonFlowType + "_diff",
                commonName + "_diff",
                commonDescription + "_diff",
                commonOperatorId + "_diff",
                commonOpenSystemId + "_diff",
                List.of("https://diff.com")
        );

        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());
        assertNotEquals(req1, req3);
        assertNotEquals(req1.hashCode(), req3.hashCode());
    }

    /**
     * Tests whether the @JsonProperty annotation of PutClientsRequest is set correctly.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "flowType, " + Const.JSON_PROPERTY_FLOW_TYPE,
        "name, " + Const.JSON_PROPERTY_CLIENT_NAME,
        "description, " + Const.JSON_PROPERTY_CLIENT_DESCRIPTION,
        "operatorId, " + Const.JSON_PROPERTY_OPERATOR_ID,
        "openSystemId, " + Const.JSON_PROPERTY_OPEN_SYSTEM_ID,
        "redirectUris, " + Const.JSON_PROPERTY_REDIRECT_URIS
    })
    @DisplayName("PutClientsRequest - @JsonProperty Annotation Test")
    void testJsonPropertyNames(String fieldName, String jsonPropertyName) throws Exception {
        Field field = PutClientsRequest.class.getDeclaredField(fieldName);
        JsonProperty annotation = field.getAnnotation(JsonProperty.class);
        assertNotNull(annotation, "JsonProperty annotation should be present for field: " + fieldName);
        assertEquals(jsonPropertyName, annotation.value(), "JsonProperty value mismatch for field: " + fieldName);
    }

    /**
     * Test JSON serialization of PutClientsRequest.
     */
    @Test
    @DisplayName("PutClientsRequest - JSON Serialization Test")
    void testJsonSerialization() throws Exception {
        PutClientsRequest request = new PutClientsRequest(
                commonFlowType,
                commonName,
                commonDescription,
                commonOperatorId,
                commonOpenSystemId,
                commonRedirectUris
        );

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        assertTrue(json.contains(Const.JSON_PROPERTY_FLOW_TYPE));
        assertTrue(json.contains(Const.JSON_PROPERTY_CLIENT_NAME));
        assertTrue(json.contains(Const.JSON_PROPERTY_CLIENT_DESCRIPTION));
        assertTrue(json.contains(Const.JSON_PROPERTY_OPERATOR_ID));
        assertTrue(json.contains(Const.JSON_PROPERTY_OPEN_SYSTEM_ID));
        assertTrue(json.contains(Const.JSON_PROPERTY_REDIRECT_URIS));
        assertTrue(json.contains(commonRedirectUris.get(0)));
    }

    /**
     * Parameterized test for valid PutClientsRequest parameters.
     */
    @ParameterizedTest
    @CsvSource({
        // flowType, clientId, name, description, operatorId, openSystemId, redirectUris
        // Minimum length test
        "VALUE, EMPTY, EMPTY, EMPTY, SMALL, SMALL",
        // Maximum length test
        "VALUE, LONG, LONG, UUID, LONG, LONG",
        // Not required values test
        "client_credentials, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY"
    })
    @DisplayName("PutClientsRequest - Valid Input")
    void testValidParameters(
            String argFlowType,
            String argName,
            String argDescription,
            String argOperatorId,
            String argOpenSystemId,
            String argRedirectUris
    ) {
        // Arrange
        String flowType =
                "VALUE".equals(argFlowType) ? commonFlowType : "client_credentials";
        String name =
                "LONG".equals(argName) ? "N".repeat(Const.CLIENT_NAME_LENGTH_MAX) :
                "EMPTY".equals(argName) ? null :
                "VALUE".equals(argName) ? commonName : argName;
        String description =
                "LONG".equals(argDescription) ? "N".repeat(Const.CLIENT_DESCRIPTION_LENGTH_MAX) :
                "EMPTY".equals(argDescription) ? null :
                "VALUE".equals(argDescription) ? commonDescription : argDescription;
        String operatorId =
                "UUID".equals(argOperatorId) ? commonOperatorId :
                "EMPTY".equals(argOperatorId) ? null :
                "VALUE".equals(argOperatorId) ? commonOperatorId : argOperatorId;
        String openSystemId =
                "LONG".equals(argOpenSystemId) ? "N".repeat(Const.CLIENT_OPEN_SYSTEM_ID_LENGTH_MAX) :
                "SMALL".equals(argOpenSystemId) ? "a".repeat(Const.CLIENT_OPEN_SYSTEM_ID_LENGTH_MIN) :
                "EMPTY".equals(argOpenSystemId) ? null :
                "VALUE".equals(argOpenSystemId) ? commonOpenSystemId : argOpenSystemId;
        String stringRedirectUris =
                "EMPTY".equals(argRedirectUris) ? null :
                "LONG".equals(argRedirectUris) ? "N".repeat(Const.REDIRECT_URI_LENGTH_MAX) :
                "SMALL".equals(argRedirectUris) ? "a".repeat(Const.REDIRECT_URI_LENGTH_MIN) :
                "VALUE".equals(argRedirectUris) ? argRedirectUris : argRedirectUris;
        List<String> redirectUris = (stringRedirectUris == null) ? null : List.of(stringRedirectUris);
        PutClientsRequest request = new PutClientsRequest();
        request.setFlowType(flowType);
        request.setName(name);
        request.setDescription(description);
        request.setOperatorId(operatorId);
        request.setOpenSystemId(openSystemId);
        request.setRedirectUris(redirectUris);
        // Act
        Set<ConstraintViolation<PutClientsRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertTrue(violations.isEmpty(), "Expected no validation errors, but found: " + violations);
    }

    /**
     * Test for invalid PutClientsRequest.
     */
    @ParameterizedTest
    @CsvSource({
        // flowType, clientId, name, description, operatorId, openSystemId, redirectUris, invalidProperty
        // flowType
        "EMPTY, VALUE, VALUE, VALUE, VALUE, VALUE, flowType", // empty
        "BLANK, VALUE, VALUE, VALUE, VALUE, VALUE, flowType", // blank
        "a, VALUE, VALUE, VALUE, VALUE, VALUE, flowType", // not in enum
        // name
        "VALUE, OVER, VALUE, VALUE, VALUE, VALUE, name", // over
        // description
        "VALUE, VALUE, OVER, VALUE, VALUE, VALUE, description", // over
        // operatorId
        "VALUE, VALUE, VALUE, 123e456G-e89b-12d3-a456-426614174000, VALUE, BLANK, operatorId", // not UUID
        "VALUE, VALUE, VALUE, 123e456G-e89b-12d3-a456-426614174000, VALUE, VALUE, operatorId", // not UUID
        // openSystemId
        "VALUE, VALUE, VALUE, VALUE, OVER, VALUE, openSystemId", // over
        // redirectUris
        "VALUE, VALUE, VALUE, VALUE, VALUE, BLANK, redirectUris", // blank
        "VALUE, VALUE, VALUE, VALUE, VALUE, OVER, redirectUris", // over
        "VALUE, VALUE, VALUE, VALUE, VALUE, *, redirectUris", // not matching pattern
        "VALUE, VALUE, VALUE, VALUE, VALUE, %, redirectUris", // not matching pattern
        "VALUE, VALUE, VALUE, VALUE, VALUE, {, redirectUris", // not matching pattern
        "VALUE, VALUE, VALUE, VALUE, VALUE, }, redirectUris", // not matching pattern
        "VALUE, VALUE, VALUE, VALUE, VALUE, ' ', redirectUris", // not matching pattern
        "VALUE, VALUE, VALUE, VALUE, VALUE, \\\\, redirectUris", // not matching pattern
        "VALUE, VALUE, VALUE, VALUE, VALUE, \\t, redirectUris", // not matching pattern
        "VALUE, VALUE, VALUE, VALUE, VALUE, \\n, redirectUris", // not matching pattern
        "VALUE, VALUE, VALUE, VALUE, VALUE, \\r, redirectUris", // not matching pattern
        "VALUE, VALUE, VALUE, VALUE, VALUE, \\u000B, redirectUris", // not matching pattern
        "VALUE, VALUE, VALUE, VALUE, VALUE, \\f, redirectUris", // not matching pattern
        "VALUE, VALUE, VALUE, VALUE, VALUE, 　, redirectUris", // not matching pattern
        "VALUE, VALUE, VALUE, VALUE, VALUE, \u00A0, redirectUris", // not matching pattern
    })
    @DisplayName("PutClientsRequest - Invalid Input")
    void testInvalidParameters(
            String argFlowType,
            String argName,
            String argDescription,
            String argOperatorId,
            String argOpenSystemId,
            String argRedirectUris,
            String argInvalidProperty
    ) {
        // Arrange
        String flowType =
                "BLANK".equals(argFlowType) ? "" :
                "EMPTY".equals(argFlowType) ? null :
                "VALUE".equals(argFlowType) ? commonFlowType : argFlowType;
        String name =
                "OVER".equals(argName) ? "N".repeat(Const.CLIENT_NAME_LENGTH_MAX + 1) :
                "EMPTY".equals(argName) ? null :
                "VALUE".equals(argName) ? commonName : argName;
        String description =
                "OVER".equals(argDescription) ? "N".repeat(Const.CLIENT_DESCRIPTION_LENGTH_MAX + 1) :
                "EMPTY".equals(argDescription) ? null :
                "VALUE".equals(argDescription) ? commonDescription : argDescription;
        String operatorId =
                "UUID".equals(argOperatorId) ? commonOperatorId :
                "EMPTY".equals(argOperatorId) ? null :
                "VALUE".equals(argOperatorId) ? commonOperatorId : argOperatorId;
        String openSystemId =
                "BLANK".equals(argOpenSystemId) ? "" :
                "OVER".equals(argOpenSystemId) ? "N".repeat(Const.CLIENT_OPEN_SYSTEM_ID_LENGTH_MAX + 1) :
                "EMPTY".equals(argOpenSystemId) ? null :
                "VALUE".equals(argOpenSystemId) ? commonOpenSystemId : argOpenSystemId;
        String stringRedirectUris =
                "BLANK".equals(argRedirectUris) ? "" :
                "EMPTY".equals(argRedirectUris) ? null :
                "OVER".equals(argRedirectUris) ? "N".repeat(Const.REDIRECT_URI_LENGTH_MAX + 1) :
                "LOW".equals(argRedirectUris) ? "a".repeat(Const.REDIRECT_URI_LENGTH_MIN - 1) :
                "VALUE".equals(argRedirectUris) ? argRedirectUris : argRedirectUris;
        List<String> redirectUris = (stringRedirectUris == null) ? null : List.of(stringRedirectUris);
        PutClientsRequest request = new PutClientsRequest();
        request.setFlowType((flowType == null) ? null : flowType);
        request.setName((name == null) ? null : name);
        request.setDescription((description == null) ? null : description);
        request.setOperatorId((operatorId == null) ? null : operatorId);
        request.setOpenSystemId((openSystemId == null) ? null : openSystemId);
        request.setRedirectUris(redirectUris);

        Set<ConstraintViolation<PutClientsRequest>> violations = VALIDATOR.validate(request);

        assertFalse(violations.isEmpty(), "Expected validation errors but found none.");
        boolean found = violations.stream().anyMatch(v -> v.getPropertyPath().toString().contains(argInvalidProperty));
        assertTrue(found, "Expected validation error for property: " + argInvalidProperty + ", but got: " + violations);
    }
}