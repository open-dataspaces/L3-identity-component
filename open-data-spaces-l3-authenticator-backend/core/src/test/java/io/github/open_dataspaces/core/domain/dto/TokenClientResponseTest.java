/*
 * TokenClientResponseTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies the behavior and validation logic
 * of the TokenClientResponseTest DTO.
 *
 * Date: 2025-06-30
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/*
 * TokenClientResponseTest is a test class for the TokenClientResponse.
 */
class TokenClientResponseTest {

    /**
     * case#2:
     * testParameterizedConstructor tests the parameterized constructor of TokenClientResponse.
     */
    @Test
    void testParameterizedConstructor() {
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
     * testGetterSetter tests the getter and setter methods of TokenClientResponse.
     */
    @Test
    void testGetterSetter() {
        TokenClientResponse response = new TokenClientResponse("access123", 3600, "tokenType", 3600, "scope");
        // Set value
        response.setAccessToken("tokenABC");
        response.setExpiresIn(7200);
        response.setTokenType("newTokenType");
        response.setNotBeforePolicy(7200);
        response.setScope("newScope");

        // Assert
        assertEquals("tokenABC", response.getAccessToken());
        assertEquals(7200, response.getExpiresIn());
        assertEquals("newTokenType", response.getTokenType());
        assertEquals(7200, response.getNotBeforePolicy());
        assertEquals("newScope", response.getScope());
    }
}