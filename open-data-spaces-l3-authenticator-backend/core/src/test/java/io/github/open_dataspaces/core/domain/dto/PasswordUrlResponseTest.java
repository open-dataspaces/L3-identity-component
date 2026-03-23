/*
 * PasswordUrlResponseTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This package contains unit tests for the PasswordUrlResponse class.
 *
 * Date: 2025/08/31
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.open_dataspaces.core.common.consts.Const;

/**
 * Unit tests for PasswordUrlResponse.
 */
class PasswordUrlResponseTest {

    /**
     * Test constructor and getter for valid input.
     */
    @Test
    @DisplayName("Test constructor and getter with valid input")
    void testConstructorAndGetter_validInput() {
        // Arrange
        String validUrl = "http://example.com";

        // Act
        PasswordUrlResponse response = new PasswordUrlResponse(validUrl);

        // Assert
        assertNotNull(response, "The response object should not be null.");
        assertEquals(validUrl, response.getUrl(), "The URL should match the input value.");
    }

    /**
     * getter/setter for valid input.
     */
    @Test
    @DisplayName("Test constructor and getter with valid input")
    void testSetterAndGetter_validInput() {
        // Arrange
        String validUrl = "http://example.com";

        // Act
        PasswordUrlResponse response = new PasswordUrlResponse("");
        response.setUrl(validUrl);

        // Assert
        assertNotNull(response, "The response object should not be null.");
        assertEquals(validUrl, response.getUrl(), "The URL should match the input value.");
    }

    /**
     * Tests whether the @JsonProperty annotation of TokenRefreshRequest is set correctly.
     */
    @Test
    void testJsonPropertyNames() throws Exception {
        Field field = PasswordUrlResponse.class.getDeclaredField("url");
        JsonProperty annotation = field.getAnnotation(JsonProperty.class);
        assertNotNull(annotation);
        assertEquals(Const.JSON_PROPERTY_URL, annotation.value());
    }

    /**
     * Test toString method.
     */
    @Test
    @DisplayName("Test toString method")
    void testToString() {
        // Arrange
        String validUrl = "http://example.com";
        PasswordUrlResponse response = new PasswordUrlResponse(validUrl);

        // Act
        String toStringResult = response.toString();

        // Assert
        assertNotNull(toStringResult, "The toString result should not be null.");
        assertEquals("PasswordUrlResponse(url=http://example.com)", toStringResult, "The toString result should match the expected format.");
    }

    /**
     * Test equals and hashCode methods.
     */
    @Test
    @DisplayName("Test equals and hashCode methods")
    void testEqualsAndHashCode() {
        // Arrange
        String url1 = "http://example.com";
        String url2 = "http://another-example.com";

        PasswordUrlResponse response1 = new PasswordUrlResponse(url1);
        PasswordUrlResponse response2 = new PasswordUrlResponse(url1);
        PasswordUrlResponse response3 = new PasswordUrlResponse(url2);

        // Act & Assert
        assertEquals(response1, response2, "Objects with the same URL should be equal.");
        assertEquals(response1.hashCode(), response2.hashCode(), "Hash codes should match for equal objects.");
        assertNotNull(response1.toString(), "toString should not return null.");
        assertNotNull(response3.toString(), "toString should not return null.");
    }
}
