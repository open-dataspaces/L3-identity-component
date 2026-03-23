/*
 * TokenClientRequestTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies the behavior and validation logic
 * of the TokenClientRequest DTO.
 *
 * Date: 2025-06-30
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/*
 * AuthClientResponseTest is a test class for the AuthClientResponse.
 */
class TokenClientRequestTest {

    /**
     * case#1:
     * testDefaultConstructor tests the default constructor of AuthClientResponse.
     */
    @Test
    void testDefaultConstructor() {
        TokenClientResponse response = new TokenClientResponse("access123", 3600, "tokenType", 3600, "scope");
        // Assert
        assertEquals("access123", response.getAccessToken());
        assertEquals(3600, response.getExpiresIn());
        assertEquals("tokenType", response.getTokenType());
        assertEquals(3600, response.getNotBeforePolicy());
        assertEquals("scope", response.getScope());
    }

    /**
     * case#3:
     * testGetterSetter tests the getter and setter methods of AuthClientResponse.
     */
    @Test
    void testGetterSetter() {
        TokenClientResponse response = new TokenClientResponse("access123", 3600, "tokenType", 3600, "scope");
        String newToken = "another-token";
        // Set value
        response.setAccessToken(newToken);

        // Assert
        assertEquals("another-token", response.getAccessToken());
    }
}