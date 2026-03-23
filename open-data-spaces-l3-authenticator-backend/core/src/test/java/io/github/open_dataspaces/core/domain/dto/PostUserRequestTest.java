/*
 * PostUserRequest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for creating a personal user.
 *
 * Date: 2025/11/30
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.open_dataspaces.core.common.consts.Const;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Unit tests for PostUserRequest.
 */
class PostUserRequestTest {

    private static Validator validator;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // Register JavaTimeModule
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // Use ISO-8601 format

    // Common input parameters
    private final String commonLoginUserId = "login-user-id";
    private final String commonCreatePasswordFlagTrue = "true";
    private final String commonCreatePasswordFlagFalse = "false";
    private final String commonPasswordTemporaryFlagTrue = "true";
    private final String commonPasswordTemporaryFlagFalse = "false";

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * Test for the default constructor.
     */
    @Test
    @DisplayName("PostUserRequest - Default Constructor")
    void testDefaultConstructor() {
        PostUserRequest dtoObject = new PostUserRequest();

        // Assert
        assertNotNull(dtoObject);
        assertNull(dtoObject.getLoginUserId());
        assertEquals(true, dtoObject.getCreatePasswordFlag());
        assertEquals(false, dtoObject.getPasswordTemporaryFlag());
    }

    /**
     * Test for the all args constructor.
     */
    @Test
    @DisplayName("PostUserRequest - All Args Constructor")
    void testAllArgsConstructor() {
        PostUserRequest dtoObject = new PostUserRequest(commonLoginUserId, commonCreatePasswordFlagFalse, commonPasswordTemporaryFlagTrue);

        // Assert
        assertEquals(commonLoginUserId, dtoObject.getLoginUserId());
        assertEquals(Boolean.valueOf(commonCreatePasswordFlagFalse), dtoObject.getCreatePasswordFlag());
        assertEquals(Boolean.valueOf(commonPasswordTemporaryFlagTrue), dtoObject.getPasswordTemporaryFlag());
    }

    /**
     * Test for valid PostUserRequest.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // Length boundary cases
        "LONG, TRUE, FALSE",
        "SMALL, FALSE, TRUE",
        // Pattern boundary cases
        "VALUE, true, EMPTY",
        "VALUE, True, EMPTY",
        "VALUE, false, EMPTY",
        "VALUE, False, EMPTY",
        "VALUE, EMPTY, true",
        "VALUE, EMPTY, True",
        "VALUE, EMPTY, false",
        "VALUE, EMPTY, False",
        // -- no test case --
        // Nullable cases
        "VALUE, EMPTY, EMPTY",
    }, nullValues = "EMPTY")
    @DisplayName("PostUserRequest - Valid Input")
    void testValidParameters(
            String argLoginUserId,
            String argCreatePasswordFlag,
            String argPasswordTemporaryFlag
    ) {
        // Convert special input values
        String loginUserId =
                "LONG".equals(argLoginUserId) ? "a".repeat(Const.LOGIN_USER_ID_LENGTH_MAX) :
                "SMALL".equals(argLoginUserId) ? "n".repeat(Const.LOGIN_USER_ID_LENGTH_MIN) :
                "VALUE".equals(argLoginUserId) ? commonLoginUserId : argLoginUserId;
        // Arrange
        PostUserRequest dtoObject = new PostUserRequest();
        dtoObject.setLoginUserId(loginUserId);
        if (argCreatePasswordFlag != null) {
            dtoObject.setCreatePasswordFlag(argCreatePasswordFlag);
        }
        if (argPasswordTemporaryFlag != null) {
            dtoObject.setPasswordTemporaryFlag(argPasswordTemporaryFlag);
        }

        // Act
        Set<ConstraintViolation<PostUserRequest>> violations = validator.validate(dtoObject);

        // Assert
        assertTrue(violations.isEmpty(), "Expected no validation errors, but found: " + violations);
        if (argCreatePasswordFlag == null) {
            assertEquals(true, dtoObject.getCreatePasswordFlag());
        }
        if (argPasswordTemporaryFlag == null) {
            assertEquals(false, dtoObject.getPasswordTemporaryFlag());
        }
    }

    /**
     * Test for invalid PostUserRequest.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // Required violations
        "EMPTY, true, false, loginUserId",
        // Size low violations
        "LOW, true, false, loginUserId",
        // Size Over violations
        "OVER, true, false, loginUserId",
        // Pattern violations
        "~~~, true, false, loginUserId",
        "VALUE, '', false, createPasswordFlag",
        "VALUE, true,'', passwordTemporaryFlag",
    }, nullValues = "EMPTY")
    @DisplayName("PostUserRequest - Invalid Input")
    void testInvalidParameters(
            String argLoginUserId,
            String argCreatePasswordFlag,
            String argPasswordTemporaryFlag,
            String argInvalidProperty
    ) {
        // Convert special input values
        String loginUserId =
                "OVER".equals(argLoginUserId) ? "a".repeat(Const.LOGIN_USER_ID_LENGTH_MAX + 1) :
                "LOW".equals(argLoginUserId) ? "n".repeat(Const.LOGIN_USER_ID_LENGTH_MIN - 1) :
                "VALUE".equals(argLoginUserId) ? commonLoginUserId : argLoginUserId;
        String createPasswordFlag =
                "TRUE".equals(argCreatePasswordFlag) ? "true" :
                "FALSE".equals(argCreatePasswordFlag) ? "false" : argCreatePasswordFlag;
        String passwordTemporaryFlag =
                "TRUE".equals(argPasswordTemporaryFlag) ? "true" :
                "FALSE".equals(argPasswordTemporaryFlag) ? "false" : argPasswordTemporaryFlag;

        // Arrange
        PostUserRequest dtoObject = new PostUserRequest();
        dtoObject.setLoginUserId(loginUserId);
        dtoObject.setCreatePasswordFlag(createPasswordFlag);
        dtoObject.setPasswordTemporaryFlag(passwordTemporaryFlag);

        // Act
        Set<ConstraintViolation<PostUserRequest>> violations = validator.validate(dtoObject);

        // Assert
        assertFalse(violations.isEmpty(),
                "Expected no validation errors, but found: " + violations.toString());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(argInvalidProperty)),
                "validation property should contain '" + argInvalidProperty + "' but was: " + violations.toString());
    }

    /**
     * Test for the setters and getters.
     */
    @Test
    @DisplayName("PostUserRequest - Setter and Getter Test")
    void testGettersAndSetters() {
        // Arrange
        PostUserRequest dtoObject = new PostUserRequest();

        // Act
        dtoObject.setLoginUserId(commonLoginUserId);
        dtoObject.setCreatePasswordFlag("true");
        dtoObject.setPasswordTemporaryFlag("true");
        // Assert
        assertEquals(commonLoginUserId, dtoObject.getLoginUserId());
        assertTrue(dtoObject.getCreatePasswordFlag());
        assertTrue(dtoObject.getPasswordTemporaryFlag());
    }

    /**
     * Test for the toString method.
     */
    @Test
    @DisplayName("PostUserRequest - toString Test")
    void testToString() {
        // Arrange
        PostUserRequest dtoObject = new PostUserRequest();
        dtoObject.setLoginUserId(commonLoginUserId);
        dtoObject.setCreatePasswordFlag(commonCreatePasswordFlagFalse);
        dtoObject.setPasswordTemporaryFlag(commonPasswordTemporaryFlagTrue);

        // Act
        String result = dtoObject.toString();

        // Assert
        assertTrue(result.contains(commonLoginUserId), "toString should include loginUserId");
        assertTrue(result.contains(String.valueOf(commonCreatePasswordFlagFalse)), "toString should include createPasswordFlag");
        assertTrue(result.contains(String.valueOf(commonPasswordTemporaryFlagTrue)), "toString should include passwordTemporaryFlag");
    }

    /**
     * Test for equals and hashCode methods.
     */
    @Test
    @DisplayName("PostUserRequest - Equals and HashCode Test")
    void testEqualsAndHashCode() {
        // Arrange
        PostUserRequest dtoObjectBase = new PostUserRequest();
        dtoObjectBase.setLoginUserId(commonLoginUserId);
        dtoObjectBase.setCreatePasswordFlag(commonCreatePasswordFlagFalse);
        dtoObjectBase.setPasswordTemporaryFlag(commonPasswordTemporaryFlagTrue);

        PostUserRequest dtoObjectEquals = new PostUserRequest();
        dtoObjectEquals.setLoginUserId(commonLoginUserId);
        dtoObjectEquals.setCreatePasswordFlag(commonCreatePasswordFlagFalse);
        dtoObjectEquals.setPasswordTemporaryFlag(commonPasswordTemporaryFlagTrue);

        PostUserRequest dtoObjectNotEquals = new PostUserRequest();
        dtoObjectNotEquals.setLoginUserId(commonLoginUserId + "-diff");
        dtoObjectNotEquals.setCreatePasswordFlag(commonCreatePasswordFlagTrue);
        dtoObjectNotEquals.setPasswordTemporaryFlag(commonPasswordTemporaryFlagFalse);

        // Act & Assert
        // Equals tests
        assertEquals(dtoObjectBase, dtoObjectEquals, "Objects with the same values should be equal.");
        assertNotEquals(dtoObjectBase, dtoObjectNotEquals, "Objects with different values should not be equal.");

        // HashCode tests
        assertEquals(dtoObjectBase.hashCode(), dtoObjectEquals.hashCode(),
                "Objects with the same values should have the same hash code.");
        assertNotEquals(dtoObjectBase.hashCode(), dtoObjectNotEquals.hashCode(),
                "Objects with different values should have different hash codes.");
    }

    /**
     * Test to verify that @Column annotations are correctly set on fields.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "loginUserId, " + Const.JSON_PROPERTY_LOGIN_USER_ID,
        "createPasswordFlag, " + Const.JSON_PROPERTY_CREATE_PASSWORD_FLAG,
        "passwordTemporaryFlag, " + Const.JSON_PROPERTY_PASSWORD_TEMPORARY_FLAG
    })
    @DisplayName("PostUserRequest - @JsonProperty Annotation Test")
    void testJsonPropertyNames(String fieldName, String jsonPropertyName) throws Exception {
        Field field = PostUserRequest.class.getDeclaredField(fieldName);
        JsonProperty annotation = field.getAnnotation(JsonProperty.class);
        assertNotNull(annotation);
        assertEquals(jsonPropertyName, annotation.value());
    }

    /**
     * Test JSON serialization.
     */
    @Test
    @DisplayName("PostUserRequest - JSON Serialization Test")
    void testJsonSerialization() throws Exception {
        // Arrange
        PostUserRequest dtoObject = new PostUserRequest();
        dtoObject.setLoginUserId(commonLoginUserId);
        dtoObject.setCreatePasswordFlag(commonCreatePasswordFlagFalse);
        dtoObject.setPasswordTemporaryFlag(commonPasswordTemporaryFlagTrue);

        // Act
        String json = objectMapper.writeValueAsString(dtoObject);

        // Assert
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_LOGIN_USER_ID + "\":\"" + commonLoginUserId + "\""), "JSON should contain loginUserId: " + json);
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_CREATE_PASSWORD_FLAG + "\":" + commonCreatePasswordFlagFalse), "JSON should contain createPasswordFlag: " + json);
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_PASSWORD_TEMPORARY_FLAG + "\":" + commonPasswordTemporaryFlagTrue), "JSON should contain passwordTemporaryFlag: " + json);
    }

    /**
     * Test JSON deserialization.
     */
    @Test
    @DisplayName("PostUserRequest - JSON Deserialization Test")
    void testJsonDeserialization() throws Exception {
        // Arrange
        String json = "{"
                + "\"" + Const.JSON_PROPERTY_LOGIN_USER_ID + "\":\"" + commonLoginUserId + "\","
                + "\"" + Const.JSON_PROPERTY_CREATE_PASSWORD_FLAG + "\":" + commonCreatePasswordFlagFalse + ","
                + "\"" + Const.JSON_PROPERTY_PASSWORD_TEMPORARY_FLAG + "\":" + commonPasswordTemporaryFlagTrue
                + "}";

        // Act
        PostUserRequest request = objectMapper.readValue(json, PostUserRequest.class);

        // Assert
        assertEquals(commonLoginUserId, request.getLoginUserId());
        assertEquals(Boolean.valueOf(commonCreatePasswordFlagFalse), request.getCreatePasswordFlag());
        assertEquals(Boolean.valueOf(commonPasswordTemporaryFlagTrue), request.getPasswordTemporaryFlag());
    }

    /**
     * Test JSON deserialization with extra (unexpected) fields.
     */
    @Test
    @DisplayName("PostUserRequest - JSON Deserialization Ignores Unknown Fields")
    void testJsonDeserializationWithUnknownFields() throws Exception {
        // Arrange
        String json = "{"
                + "\"" + Const.JSON_PROPERTY_LOGIN_USER_ID + "\":\"" + commonLoginUserId + "\","
                + "\"" + Const.JSON_PROPERTY_CREATE_PASSWORD_FLAG + "\":" + commonCreatePasswordFlagFalse + ","
                + "\"" + Const.JSON_PROPERTY_PASSWORD_TEMPORARY_FLAG + "\":" + commonPasswordTemporaryFlagTrue + ","
                + "\"extraField\":\"shouldBeIgnored\""
                + "}";

        // Act
        PostUserRequest request = objectMapper.readValue(json, PostUserRequest.class);

        // Assert
        assertEquals(commonLoginUserId, request.getLoginUserId());
        assertEquals(Boolean.valueOf(commonCreatePasswordFlagFalse), request.getCreatePasswordFlag());
        assertEquals(Boolean.valueOf(commonPasswordTemporaryFlagTrue), request.getPasswordTemporaryFlag());
        // extraField is not present in the DTO, so it cannot be accessed
    }

    /**
     * Test to verify not required values don't raise validation errors.
     */
    @Test
    @DisplayName("PostUserRequest - Not required Values Test")
    void testNotRequiredValues() {
        // Arrange
        PostUserRequest request = new PostUserRequest();
        request.setLoginUserId("user1");
        // Act
        Set<ConstraintViolation<PostUserRequest>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty(), "Expected no validation errors, but found: " + violations);
        assertEquals(true, request.getCreatePasswordFlag());
        assertEquals(false, request.getPasswordTemporaryFlag());
    }
}