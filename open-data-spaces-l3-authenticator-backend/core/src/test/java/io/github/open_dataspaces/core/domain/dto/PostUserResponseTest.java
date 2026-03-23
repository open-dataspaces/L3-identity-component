/*
 * PostUserResponseTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for user information responses.
 *
 * Date: 2025/11/30
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.github.open_dataspaces.core.common.consts.Const;

class PostUserResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // Register JavaTimeModule
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // Use ISO-8601 format

    // Common input parameters
    private final String commonLoginUserId = "login-user-id";
    private final String commonPassword = "password123";
    private final String commonUserId = UUID.randomUUID().toString();

    /**
     * Test for the default constructor.
     */
    @Test
    @DisplayName("PostUserResponse - Default Constructor")
    public void testDefaultConstructor() {
        PostUserResponse dtoObject = new PostUserResponse();

        // Assert
        assertNotNull(dtoObject);
        assertNull(dtoObject.getLoginUserId());
        assertNull(dtoObject.getPassword());
        assertNull(dtoObject.getOperatorId());
    }

    /**
     * Test for the all args constructor.
     */
    @Test
    @DisplayName("PostUserResponse - All Args Constructor")
    public void testAllArgsConstructor() {
        PostUserResponse dtoObject = new PostUserResponse(commonLoginUserId, commonUserId, commonPassword);

        // Assert
        assertEquals(commonLoginUserId, dtoObject.getLoginUserId());
        assertEquals(commonUserId, dtoObject.getOperatorId());
        assertEquals(commonPassword, dtoObject.getPassword());
    }

    /**
     * Test for the setters and getters.
     */
    @Test
    @DisplayName("PostUserResponse - Setter and Getter Test")
    public void testSettersAndGetters() {
        // Arrange
        PostUserResponse dtoObject = new PostUserResponse();

        // Act
        dtoObject.setLoginUserId(commonLoginUserId);
        dtoObject.setPassword(commonPassword);
        dtoObject.setOperatorId(commonUserId);

        // Assert
        assertEquals(commonLoginUserId, dtoObject.getLoginUserId());
        assertEquals(commonPassword, dtoObject.getPassword());
        assertEquals(commonUserId, dtoObject.getOperatorId());
    }

    /**
     * Test for the toString method.
     */
    @Test
    @DisplayName("PostUserResponse - toString Test")
    void testToString() {
        // Arrange
        PostUserResponse dtoObject = new PostUserResponse();
        dtoObject.setLoginUserId(commonLoginUserId);
        dtoObject.setPassword(commonPassword);
        dtoObject.setOperatorId(commonUserId);

        // Act
        String result = dtoObject.toString();

        // Assert
        assertTrue(result.contains(commonLoginUserId), "toString should include loginUserId");
        assertTrue(result.contains(commonPassword), "toString should include password");
        assertTrue(result.contains(commonUserId), "toString should include operatorId");
    }

    /**
     * Test for equals and hashCode methods.
     */
    @Test
    @DisplayName("PostUserResponse - Equals and HashCode Test")
    void testEqualsAndHashCode() {
        // Arrange
        PostUserResponse dtoObjectBase = new PostUserResponse();
        dtoObjectBase.setLoginUserId(commonLoginUserId);
        dtoObjectBase.setPassword(commonPassword);
        dtoObjectBase.setOperatorId(commonUserId);

        PostUserResponse dtoObjectEquals = new PostUserResponse();
        dtoObjectEquals.setLoginUserId(commonLoginUserId);
        dtoObjectEquals.setPassword(commonPassword);
        dtoObjectEquals.setOperatorId(commonUserId);

        PostUserResponse dtoObjectNotEquals = new PostUserResponse();
        dtoObjectNotEquals.setLoginUserId(commonLoginUserId + "-diff");
        dtoObjectNotEquals.setPassword(commonPassword + "-diff");
        dtoObjectNotEquals.setOperatorId(commonUserId + "-diff");

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
        "password, " + Const.JSON_PROPERTY_LOGIN_USER_PASSWORD,
        "operatorId, " + Const.JSON_PROPERTY_OPERATOR_ID
    })
    @DisplayName("PostUserResponse - @JsonProperty Annotation Test")
    void testJsonPropertyNames(String fieldName, String jsonPropertyName) throws Exception {
        Field field = PostUserResponse.class.getDeclaredField(fieldName);
        JsonProperty annotation = field.getAnnotation(JsonProperty.class);
        assertNotNull(annotation);
        assertEquals(jsonPropertyName, annotation.value());
    }

    /**
     * Test JSON serialization.
     */
    @Test
    @DisplayName("PostUserResponse - JSON Serialization Test")
    void testJsonSerialization() throws Exception {
        // Arrange
        PostUserResponse dtoObject = new PostUserResponse();
        dtoObject.setLoginUserId(commonLoginUserId);
        dtoObject.setPassword(commonPassword);
        dtoObject.setOperatorId(commonUserId);

        // Act
        String json = objectMapper.writeValueAsString(dtoObject);

        // Assert
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_LOGIN_USER_ID + "\":\"" + commonLoginUserId + "\""), "JSON should contain loginUserId: " + json);
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_LOGIN_USER_PASSWORD + "\":\"" + commonPassword + "\""), "JSON should contain password: " + json);
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_OPERATOR_ID + "\":\"" + commonUserId + "\""), "JSON should contain operatorId: " + json);
    }
}