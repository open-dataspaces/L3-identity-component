/*
 * TokenPasswordResponseTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies the behavior and validation logic
 * of the TokenPasswordResponse DTO.
 *
 * Date: 2025-06-30
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/*
 * TokenPasswordResponseTest is a test class for the TokenPasswordResponse.
 */
class TokenPasswordResponseTest {

    /**
     * case#2:
     * testParameterizedConstructor tests the parameterized constructor of TokenPasswordResponse.
     */
    @Test
    void testParameterizedConstructor() {
        TokenPasswordResponse response = new TokenPasswordResponse("access123", 3600, "tokenType", 3600, "scope", "refresh456", 3600);
        // Assert
        assertEquals("access123", response.getAccessToken());
        assertEquals(3600, response.getExpiresIn());
        assertEquals("tokenType", response.getTokenType());
        assertEquals(3600, response.getRefreshExpiredIn());
        assertEquals("scope", response.getScope());
        assertEquals("refresh456", response.getRefreshToken());
    }

    /**
     * case#3:
     * testGetterSetter tests the getter and setter methods of TokenPasswordResponse.
     */
    @Test
    void testGetterSetter() {
        TokenPasswordResponse response = new TokenPasswordResponse("access123", 3600, "tokenType", 3600, "scope", "refresh456", 3600);
        // Set value
        response.setAccessToken("tokenABC");
        response.setExpiresIn(7200);
        response.setTokenType("newTokenType");
        response.setRefreshExpiredIn(7200);
        response.setScope("newScope");
        response.setRefreshToken("refreshXYZ");

        // Assert
        assertEquals("tokenABC", response.getAccessToken());
        assertEquals(7200, response.getExpiresIn());
        assertEquals("newTokenType", response.getTokenType());
        assertEquals(7200, response.getRefreshExpiredIn());
        assertEquals("newScope", response.getScope());
        assertEquals("refreshXYZ", response.getRefreshToken());
    }

}