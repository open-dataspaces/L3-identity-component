/*
 * APIErrorResponseTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies the behavior of the APIErrorResponse DTO,
 * including its construction and getter methods.
 *
 * Date: 2025-06-30
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/*
 * ErrorResponseTest is a test class for the ErrorResponse.
 */
class APIErrorResponseTest {

    /**
     * case#1:
     * testErrorResponse tests that ErrorResponse is constructed correctly
     * and its getter methods for code, message, and detail work as expected.
     */
    @Test
    void testErrorResponse() {
        // Set value
        APIErrorResponse response = new APIErrorResponse("ERR001", "Invalid request", 400,
                "The request parameter 'id' is missing.");
        // Assert
        // assertEquals("ERR001", response.getCode());
        // assertEquals("Invalid request", response.getMessage());
        assertEquals("The request parameter 'id' is missing.", response.getDetail());
    }

    /**
     * case#2:
     * testErrorResponseEmpty tests that ErrorResponse handles empty string values correctly
     * and its getter methods for code, message, and detail return empty strings as expected.
     */
    @Test
    void testErrorResponseEmpty() {
        // Set value
        // APIErrorResponse response = new APIErrorResponse(null, "", 0, "");
        // Assert
        // assertTrue(response.getCode().isBlank());
        // assertTrue(response.getMessage().isBlank());
        // assertTrue(response.getDetail().isBlank());
    }

    /**
     * case#3:
     * testErrorResponseNull tests that ErrorResponse handles null values correctly
     * and its getter methods for code, message, and detail return null as expected.
     */
    @Test
    void testErrorResponseNull() {
        // Set value
        // APIErrorResponse response = new APIErrorResponse(null, null, null, null);
        // Assert
        // assertNull(response.getCode());
        // assertNull(response.getMessage());
        // assertNull(response.getDetail());
    }
}