/*
 * EvaluateResponseTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the EvaluateResponse DTO.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.infrastructure.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Unit tests for EvaluateResponse DTO.
 */
public class EvaluateResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // Register JavaTimeModule
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // Use ISO-8601 format

    // Validator for bean validation tests
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    // Common test data
    private final Boolean commonDecision = Boolean.TRUE;

    @Test
    @DisplayName("Default Constructor")
    void testDefaultConstructor() {
        EvaluateResponse dtoObject = new EvaluateResponse();

        assertNotNull(dtoObject);
        assertNull(dtoObject.getDecision());
    }

    @ParameterizedTest
    @CsvSource(value = {
        // decision
        "true, true",
        "true, false",
        "false, NULL",
    })
    @DisplayName("Validate - valid parameters")
    void testValidParameters(
            boolean validatorResult,
            String argDecision
    ) {
        EvaluateResponse dtoObject = new EvaluateResponse();
        dtoObject.setDecision(
                "NULL".equals(argDecision) ? null : Boolean.valueOf(argDecision)
        );

        Set<ConstraintViolation<EvaluateResponse>> violations = VALIDATOR.validate(dtoObject);
        assertEquals(validatorResult, violations.isEmpty());
    }

    @Test
    @DisplayName("Setters and Getters Test")
    void testSettersAndGetters() {
        EvaluateResponse dtoObject = new EvaluateResponse();
        dtoObject.setDecision(commonDecision);

        assertEquals(dtoObject.getDecision(), commonDecision);
    }

    @Test
    @DisplayName("toString Test")
    void testToString() {
        EvaluateResponse dtoObject = new EvaluateResponse();
        dtoObject.setDecision(commonDecision);

        // Act
        String result = dtoObject.toString();

        // Assert
        assertTrue(result.contains(commonDecision.toString()), "toString should include decision");
    }

    @Test
    @DisplayName("Equals and HashCode Test")
    void testEqualsAndHashCode() {
        EvaluateResponse dtoObject = new EvaluateResponse();
        dtoObject.setDecision(commonDecision);

        EvaluateResponse dtoObjectSame = new EvaluateResponse();
        dtoObjectSame.setDecision(commonDecision);

        EvaluateResponse dtoObjectDiff = new EvaluateResponse();
        dtoObjectDiff.setDecision(false);

        // Equals tests
        assertEquals(dtoObject, dtoObjectSame, "Objects with same values should be equal");
        assertNotEquals(dtoObject, dtoObjectDiff, "Objects with different values should not be equal");

        // HashCode tests
        assertEquals(dtoObject.hashCode(), dtoObjectSame.hashCode(), "Hash codes should be equal for same objects");
        assertNotEquals(dtoObject.hashCode(), dtoObjectDiff.hashCode(), "Hash codes should differ for different objects");
    }

    @Test
    @DisplayName("JSON serialization")
    void testJsonSerialization() throws Exception {
        EvaluateResponse dtoObject = new EvaluateResponse();
        dtoObject.setDecision(commonDecision);

        String json = objectMapper.writeValueAsString(dtoObject);

        // Assert
        assertTrue(json.contains("\"decision\":" + commonDecision), "JSON should contain decision: " + json);
    }
}
