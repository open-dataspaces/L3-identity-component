/*
 * APIJSONRequestTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class tests the APIJSONRequest functionality
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for APIJSONRequest.
 */
class APIJSONRequestTest {
    /**
     * Test no-args constructor.
     */
    @Test
    @DisplayName("Test no-args constructor")
    void testNoArgsConstructor() {
        APIJSONRequest req = new APIJSONRequest();
        assertNotNull(req);
        assertEquals(null, req.getRawJson());
        assertNotNull(req.getJsonProperties());
        assertEquals(0, req.getJsonProperties().size());
    }

    /**
     * Test setters and getters.
     */
    @Test
    @DisplayName("APIJSONRequest - Setter and Getter Test")
    void testSettersAndGetters() {
        APIJSONRequest req = new APIJSONRequest();
        req.setRawJson("{\"key1\":\"value1\",\"key2\":\"value2\"}");
        assertEquals("{\"key1\":\"value1\",\"key2\":\"value2\"}", req.getRawJson());
        assertNotNull(req.getJsonProperties());
        assertEquals(2, req.getJsonProperties().size());
    }

    /**
     * Test setRawJson with valid JSON string.
     */
    @Test
    @DisplayName("Test setRawJson Success")
    void testSetRawJson_success() {
        String rowJson = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
        APIJSONRequest apiJsonRequest = new APIJSONRequest();
        // Arrange
        apiJsonRequest.setRawJson(rowJson);
        // Assert
        assertNotNull(apiJsonRequest.getJsonProperties());
        assertEquals(2, apiJsonRequest.getJsonProperties().size());
        assertTrue(apiJsonRequest.getJsonProperties().contains("key1"));
        assertTrue(apiJsonRequest.getJsonProperties().contains("key2"));
    }

    /**
     * Test setRawJson with null, empty, or invalid JSON strings.
     */
    @Test
    @DisplayName("Test setRawJson null")
    void testSetRawJson_null() {
        String rowJson = null;
        APIJSONRequest apiJsonRequest = new APIJSONRequest();
        // Arrange
        apiJsonRequest.setRawJson(rowJson);
        // Assert
        assertNotNull(apiJsonRequest.getJsonProperties());
        assertEquals(0, apiJsonRequest.getJsonProperties().size());
    }

    /**
     * Test setRawJson with empty string.
     */
    @Test
    @DisplayName("Test setRawJson empty")
    void testSetRawJson_emptyRawJson() {
        String rowJson = "";
        APIJSONRequest apiJsonRequest = new APIJSONRequest();
        // Arrange
        apiJsonRequest.setRawJson(rowJson);
        // Assert
        assertNotNull(apiJsonRequest.getJsonProperties());
        assertEquals(0, apiJsonRequest.getJsonProperties().size());
    }

    /**
     * Test setRawJson with invalid JSON format.
     */
    @Test
    @DisplayName("Test setRawJson not json format")
    void testSetRawJson_notJsonFormat() {

        // Missing ending brace }
        String rowJson = "{\"key1\":\"value1\",\"key2\":\"value2\"";
        APIJSONRequest apiJsonRequest = new APIJSONRequest();
        // Arrange
        apiJsonRequest.setRawJson(rowJson);
        // Assert
        assertNotNull(apiJsonRequest.getJsonProperties());
        assertEquals(0, apiJsonRequest.getJsonProperties().size());
    }

    /**
    * Test hasProperty with existing and non-existing properties.
    */
    @Test
    @DisplayName("Test hasProperty Success")
    void testHasProperty_success() {
        APIJSONRequest apiJsonRequest = new APIJSONRequest();
        apiJsonRequest.setRawJson("{\"key1\":\"value1\",\"key2\":\"value2\"}");
        // Arrange & Assert
        assertTrue(apiJsonRequest.hasProperty("key1"));
        assertTrue(apiJsonRequest.hasProperty("key2"));
        assertFalse(apiJsonRequest.hasProperty("notExistKey"));
    }

    /**
     * Test hasProperty with null jsonProperties.
     */
    @Test
    @DisplayName("Test hasProperty null")
    void testHasProperty_null()throws Exception {
        APIJSONRequest req = new APIJSONRequest();
        var field = APIJSONRequest.class.getDeclaredField("jsonProperties");
        field.setAccessible(true);
        field.set(req, null);
        assertFalse(req.hasProperty("anyKey"));
    }

    /**
     * Test buildStateStringForProperty with existing property.
     */
    @Test
    @DisplayName("Test buildStateStringForProperty Success")
    void testBuildStateStringForProperty_success() {
        APIJSONRequest req = new APIJSONRequest();
        req.setRawJson("{\"key1\":\"value1\",\"key2\":\"value2\"}");
        StateString expected = StateString.of("value1");
        // Arrange
        StateString stateString = req.buildStateStringForProperty("key1", "value1");
        // Assert
        assertNotNull(stateString);
        assertEquals(expected, stateString);
    }

    /**
     * Test buildStateStringForProperty with non-existing property.
     */
    @Test
    @DisplayName("Test buildStateStringForProperty Success")
    void testBuildStateStringForProperty_emptyStateString() {
        APIJSONRequest req = new APIJSONRequest();
        StateString expected = new StateString();
        // Arrange
        StateString stateString = req.buildStateStringForProperty("key1", "value1");
        // Assert
        assertEquals(expected, stateString);
    }
}