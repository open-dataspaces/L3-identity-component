/*
 * AuthUrlResponseTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies the behavior and validation logic
 * of the AuthUrlResponse DTO.
 *
 * Date: 2025-08-31
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * AuthUrlResponseTest is a test class for the AuthUrlResponse.
 */
public class AuthUrlResponseTest {

    /**
     * case#1:
     * testAllArgsConstructorAndGetters tests the all-args constructor and getter
     * method of AuthUrlResponse.
     */
    @Test
    void testAllArgsConstructorAndGetters() {
        String testUrl = "https://example.com/auth";
        AuthUrlResponse response = new AuthUrlResponse(testUrl);
        assertEquals(testUrl, response.getUrl());
    }

    /**
     * case#2:
     * testSetUrl tests the setUrl method of AuthUrlResponse.
     */
    @Test
    void testSetUrl() {
        AuthUrlResponse response = new AuthUrlResponse("initial");
        String newUrl = "https://new-url.com";
        response.setUrl(newUrl);
        assertEquals(newUrl, response.getUrl());
    }

    /**
     * case#3:
     * testEqualsAndHashCode tests the equals and hashCode methods of AuthUrlResponse.
     */
    @Test
    void testEqualsAndHashCode() {
        AuthUrlResponse resp1 = new AuthUrlResponse("url1");
        AuthUrlResponse resp2 = new AuthUrlResponse("url1");
        AuthUrlResponse resp3 = new AuthUrlResponse("url2");

        assertEquals(resp1, resp2);
        assertEquals(resp1.hashCode(), resp2.hashCode());
        assertNotEquals(resp1, resp3);
    }

    /**
     * case#4:
     * testToString tests the toString method of AuthUrlResponse.
     */
    @Test
    void testToString() {
        AuthUrlResponse response = new AuthUrlResponse("https://example.com");
        String str = response.toString();

        assertTrue(str.contains("https://example.com"));
    }
}