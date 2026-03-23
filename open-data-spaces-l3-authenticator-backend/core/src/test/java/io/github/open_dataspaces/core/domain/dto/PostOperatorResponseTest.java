/*
 * PostOperatorResponseTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for the OperatorServiceImpl class, focusing on the addOperator method.
 *
 * Date: 2025/08/30
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.open_dataspaces.core.common.consts.Const;

/**
 * Unit tests for the PostOperatorResponse class.
 */
class PostOperatorResponseTest {

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
        PostOperatorResponse response = PostOperatorResponse.builder().build();

        // Assert
        assertNotNull(response);
        assertNull(response.getPassword());
    }

    /**
     * Test the parameterized constructor with valid inputs.
     * Ensures that the object is correctly populated.
     */
    @ParameterizedTest
    @CsvSource({
        "password123"
    })
    @NullAndEmptySource
    @DisplayName("Test parameterized constructor with valid inputs")
    void testParameterizedConstructor(String password) {
        // Arrange
        OperatorResult operatorResult = new OperatorResult(
                "operatorId", "operatorName", "address1", "openOperatorId1", "globalOperatorId1", false,
                LocalDate.parse("2025-01-01"), LocalDate.parse("2025-12-31"),
                LocalDateTime.now(), "creator1", LocalDateTime.now(), "updater1"
        );

        // Act
        PostOperatorResponse response = new PostOperatorResponse(operatorResult, "loginUserId", password);

        // Assert
        assertNotNull(response);
        if (StringUtils.hasText(password)) {
            assertEquals(password, response.getPassword());
        } else {
            assertNull(response.getPassword());
        }
    }

    /**
     * Test to ensure PostOperatorResponse is a subclass of OperatorResponse.
     */
    @Test
    @DisplayName("Ensure PostOperatorResponse extends OperatorResponse")
    void testInheritance() {
        // Arrange
        PostOperatorResponse response = new PostOperatorResponse();

        // Act & Assert
        assertTrue(response instanceof OperatorResponse, "PostOperatorResponse should extend OperatorResponse");
    }

    /**
     * Test that the password field is included or excluded in the JSON based on its value.
     */
    @ParameterizedTest
    @CsvSource({
        "password123, true", // Password is not null or empty
        "'', false",         // Password is empty
        "null, false"        // Password is null
    })
    @DisplayName("Test @JsonInclude for password field")
    void testJsonIncludeForPassword(String password, boolean isPasswordIncluded) throws JsonProcessingException {
        // Arrange
        password = "null".equals(password) ? null : password;
        OperatorResult operatorResult = new OperatorResult(
                "operatorId1", "operatorName", "address1", "openOperatorId1", "globalOperatorId1", false,
                LocalDate.parse("2025-01-01"), LocalDate.parse("2025-12-31"),
                LocalDateTime.now(), "creator1", LocalDateTime.now(), "updater1"
        );
        PostOperatorResponse response = new PostOperatorResponse(operatorResult, "loginUserId123", password);

        // Act
        String json = objectMapper.writeValueAsString(response);

        // Assert
        if (isPasswordIncluded) {
            assertTrue(json.contains("\"password\":\"" + password + "\""), "Password should be included in JSON");
        } else {
            assertFalse(json.contains("\"password\""), "Password should not be included in JSON");
        }
    }

    /**
     * Test the setter methods.
     * Ensures that the fields are correctly updated.
     */
    @ParameterizedTest
    @CsvSource({
        "password123, updatedPassword",
        "null, newPassword",
        "oldPassword, null"
    })
    @DisplayName("Test setter methods")
    void testSettersAndGetters(String initialPassword, String updatedPassword) {
        // Arrange
        initialPassword = "null".equals(initialPassword) ? null : initialPassword;
        updatedPassword = "null".equals(updatedPassword) ? null : updatedPassword;
        PostOperatorResponse response = PostOperatorResponse.builder().build();
        response.setPassword(initialPassword);

        // Act
        response.setPassword(updatedPassword);

        // Assert
        assertEquals(updatedPassword, response.getPassword());
    }

    /**
     * Test the toString method.
     * Ensures that the string representation includes all fields, including cases where password is null or empty.
     */
    @ParameterizedTest
    @CsvSource({
        "password123", // Normal case
    })
    @NullAndEmptySource
    @DisplayName("Test toString method with various password values")
    void testToString(String password) {
        // Arrange
        OperatorResult operatorResult = new OperatorResult(
                "operatorId1", "operatorName", "address1", "openOperatorId1", "globalOperatorId1", false,
                LocalDate.parse("2025-01-01"), LocalDate.parse("2025-12-31"),
                LocalDateTime.now(), "creator1", LocalDateTime.now(), "updater1"
        );
        PostOperatorResponse response = new PostOperatorResponse(operatorResult, "loginUserId123", password);

        // Act
        String result = response.toString();

        // Assert
        assertNotNull(result);
        assertEquals(true, result.contains("password"));
        if (password != null && !password.isEmpty()) {
            assertEquals(true, result.contains(password));
        }
    }

    /**
     * Test the equals and hashCode methods.
     * Ensures that objects with the same values are considered equal.
     */
    @Test
    @DisplayName("Test equals and hashCode methods")
    void testEqualsAndHashCode() {
        // Arrange
        PostOperatorResponse response1 = new PostOperatorResponse();
        response1.setPassword("password123");
        PostOperatorResponse response2 = new PostOperatorResponse();
        response2.setPassword("password123");

        // Act & Assert
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    /**
     * Tests whether the @JsonProperty annotation of PostOperatorRequest is set correctly.
     */
    @Test
    void testJsonPropertyNames() throws Exception {
        Field fieldPassword = PostOperatorResponse.class.getDeclaredField("password");
        JsonProperty annotationPassword = fieldPassword.getAnnotation(JsonProperty.class);
        assertNotNull(annotationPassword);
        assertEquals(Const.JSON_PROPERTY_LOGIN_USER_PASSWORD, annotationPassword.value());
    }

    /**
     * Test JSON serialization of PostOperatorResponse.
     */
    @Test
    @DisplayName("PostOperatorResponse - JSON Serialization Test")
    void testJsonSerialization() throws JsonProcessingException {
        // Arrange
        PostOperatorResponse response = new PostOperatorResponse(
                new OperatorResult(
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
                "updater123"),
                "loginUserId123",
                "password123"
        );

        // Act
        String json = objectMapper.writeValueAsString(response);

        // Assert
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_LOGIN_USER_ID + "\":\"loginUserId123\""), "JSON should contain loginUserId");
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_LOGIN_USER_PASSWORD + "\":\"password123\""), "JSON should contain password");
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_OPERATOR_NAME + "\":\"Operator Name\""), "JSON should contain operatorName");
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_OPERATOR_ADDRESS + "\":\"123 Main St\""), "JSON should contain operatorAddress");
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_OPEN_OPERATOR_ID + "\":\"openOperatorId123\""), "JSON should contain openOperatorId");
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_GLOBAL_OPERATOR_ID + "\":\"globalOperatorId123\""), "JSON should contain globalOperatorId");
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_EFFECTIVE_START_DATE + "\":\"2025-01-01\""), "JSON should contain effectiveStartDate");
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_EFFECTIVE_END_DATE + "\":\"2025-12-31\""), "JSON should contain effectiveEndDate");
    }
}
