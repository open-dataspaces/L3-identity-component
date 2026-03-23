/*
 * AuthTokenResponseTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies the behavior and validation logic
 * of the AuthTokenResponse DTO.
 *
 * Date: 2025/08/15
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.open_dataspaces.core.common.annotations.Masked;
import io.github.open_dataspaces.core.common.consts.Const;

/*
 * AuthTokenResponseTest is a test class for the AuthTokenResponse.
 */
class AuthTokenResponseTest {

    /**
     * case#1:
     * testAllArgsConstructorAndGetters tests the all-args constructor and getters of AuthTokenResponse.
     */
    @Test
    void testAllArgsConstructorAndGetters() {
        AuthTokenResponse response = new AuthTokenResponse(
                "access-token",
                3600L,
                "Bearer",
                0,
                "openid",
                "refresh-token",
                7200L,
                "id-token");

        assertEquals("access-token", response.getAccessToken());
        assertEquals(3600L, response.getExpiresIn());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(0, response.getNotBeforePolicy());
        assertEquals("openid", response.getScope());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(7200L, response.getRefreshExpiresIn());
        assertEquals("id-token", response.getIdToken());
    }

    /**
     * case#2:
     * testSetters tests the setter methods of AuthTokenResponse.
     */
    @Test
    void testSetters() {
        AuthTokenResponse response = new AuthTokenResponse(
                "initial-access", 1800L, "Bearer", 0, "openid",
                "initial-refresh", 3600L, "initial-id");

        // Test setting new values
        response.setAccessToken("new-access-token");
        response.setExpiresIn(7200L);
        response.setTokenType("JWT");
        response.setNotBeforePolicy(1);
        response.setScope("openid profile email");
        response.setRefreshToken("new-refresh-token");
        response.setRefreshExpiresIn(14400L);
        response.setIdToken("new-id-token");

        // Verify new values
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals(7200L, response.getExpiresIn());
        assertEquals("JWT", response.getTokenType());
        assertEquals(1, response.getNotBeforePolicy());
        assertEquals("openid profile email", response.getScope());
        assertEquals("new-refresh-token", response.getRefreshToken());
        assertEquals(14400L, response.getRefreshExpiresIn());
        assertEquals("new-id-token", response.getIdToken());
    }

    /**
     * case#3:
     * testEqualsAndHashCode tests the equals and hashCode methods of AuthTokenResponse.
     */
    @Test
    void testEqualsAndHashCode() {
        AuthTokenResponse response1 = new AuthTokenResponse(
                "access1", 3600L, "Bearer", 0, "openid",
                "refresh1", 7200L, "id1");

        AuthTokenResponse response2 = new AuthTokenResponse(
                "access1", 3600L, "Bearer", 0, "openid",
                "refresh1", 7200L, "id1");

        AuthTokenResponse response3 = new AuthTokenResponse(
                "access2", 3600L, "Bearer", 0, "openid",
                "refresh1", 7200L, "id1");

        // Test equals
        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
        assertNotEquals(response2, response3);

        // Test hashCode
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    /**
     * case#4:
     * testToString tests the toString method of AuthTokenResponse.
     */
    @Test
    void testToString() {
        AuthTokenResponse response = new AuthTokenResponse(
                "sample-access-token", 3600L, "Bearer", 0, "openid profile",
                "sample-refresh-token", 7200L, "sample-id-token");

        String toStringResult = response.toString();

        // Check that toString contains key information
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("AuthTokenResponse"));
        assertTrue(toStringResult.contains("Bearer"));
        assertTrue(toStringResult.contains("3600"));
        assertTrue(toStringResult.contains("openid profile"));
        assertTrue(toStringResult.contains("7200"));
    }

    /**
     * case#5:
     * testJsonProperty tests the JSON deserialization of AuthTokenResponse.
     */
    @Test
    void testJsonProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        AuthTokenResponse response = new AuthTokenResponse(
                "test-access-token", 1800L, "Bearer", 0, "openid",
                "test-refresh-token", 3600L, "test-id-token");

        String json = mapper.writeValueAsString(response);

        // Verify JSON contains correct property names as defined in Const
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_ACCESS_TOKEN + "\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_EXPIRES_IN + "\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_TOKEN_TYPE + "\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_NOT_BEFORE_POLICY + "\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_SCOPE + "\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_REFRESH_TOKEN + "\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_REFRESH_EXPIRES_IN + "\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_ID_TOKEN + "\""));

        // Verify values are present
        assertTrue(json.contains("test-access-token"));
        assertTrue(json.contains("1800"));
        assertTrue(json.contains("Bearer"));
        assertTrue(json.contains("openid"));
        assertTrue(json.contains("test-refresh-token"));
        assertTrue(json.contains("3600"));
        assertTrue(json.contains("test-id-token"));
    }

    /**
     * testTokenBoundaryValues tests boundary values for numeric fields.
     */
    @ParameterizedTest
    @CsvSource({
        //expiresIn, refreshExpiresIn
        "-9223372036854775808, -9223372036854775808", // case#6: Minimum long values
        "0, 0",           // case#7: Default values (zero)
        "1, 1",           // case#8: Small positive values
        "3600, 7200",     // case#9: Typical values (1 hour, 2 hours)
        "86400, 604800",  // case#10: Large values (1 day, 1 week)
        "9223372036854775807, 9223372036854775807"  // case#11: Maximum long values
    })
    void testTokenBoundaryValues(long expiresIn, long refreshExpiresIn) {
        AuthTokenResponse response = new AuthTokenResponse(
                "access-token", expiresIn, "Bearer", 0, "openid",
                "refresh-token", refreshExpiresIn, "id-token");

        assertEquals(expiresIn, response.getExpiresIn());
        assertEquals(refreshExpiresIn, response.getRefreshExpiresIn());
    }

    /**
     * testDifferentTokenTypesAndScopes tests different token types.
     */
    @ParameterizedTest
    @CsvSource({
        //tokenType, scope
        "'Bearer', 'openid'", // case#12: Bearer
        "'JWT', 'openid profile'", // case#13: JWT
        "'MAC', 'openid profile email'", // case#14: MAC
        "'Bearer', 'read write'" // case#15: Bearer
    })
    void testDifferentTokenTypesAndScopes(String tokenType, String scope) {
        AuthTokenResponse response = new AuthTokenResponse(
                "access-token", 3600L, tokenType, 0, scope,
                "refresh-token", 7200L, "id-token");

        assertEquals(tokenType, response.getTokenType());
        assertEquals(scope, response.getScope());
    }

    /**
     * testNotBeforePolicyValues tests notBeforePolicy values.
     */
    @ParameterizedTest
    @CsvSource({
        "0",      // case#16: Default/immediate validity
        "1",      // case#17: 1 second delay
        "300",    // case#18: 5 minutes delay
        "3600",   // case#19: 1 hour delay
        "86400"   // case#20: 1 day delay
    })
    void testNotBeforePolicyValues(int notBeforePolicy) {
        AuthTokenResponse response = new AuthTokenResponse(
                "access-token", 3600L, "Bearer", notBeforePolicy, "openid",
                "refresh-token", 7200L, "id-token");

        assertEquals(notBeforePolicy, response.getNotBeforePolicy());
    }

    /**
     * testMaskedAnnotationPresence tests that @Masked annotation is present on sensitive fields.
     */
    @ParameterizedTest
    @CsvSource({
        // fieldName, expectedPrefix, expectedSuffix
        "accessToken, 10, 5", // case#21: accessToken is masked
        "refreshToken, 10, 5", // case#22: refreshToken is masked
        "idToken, 10, 5" // case#23: idToken is masked
    })
    void testMaskedAnnotationPresence(String fieldName, int expectedPrefix, int expectedSuffix) throws Exception {
        Field field = AuthTokenResponse.class.getDeclaredField(fieldName);
        Masked masked = field.getAnnotation(Masked.class);

        assertNotNull(masked, "Field " + fieldName + " should have @Masked annotation");
        assertEquals(expectedPrefix, masked.unmaskedPrefixLength(),
                "Unmasked prefix length should be " + expectedPrefix + " for field " + fieldName);
        assertEquals(expectedSuffix, masked.unmaskedSuffixLength(),
                "Unmasked suffix length should be " + expectedSuffix + " for field " + fieldName);
    }

    /**
     * testNonSensitiveFieldsNotMasked tests that non-sensitive fields are not masked.
     */
    @ParameterizedTest
    @CsvSource({
        "tokenType", // case#24: tokenType is not masked
        "scope", // case#25: scope is not masked
        "expiresIn", // case#26: expiresIn is not masked
        "refreshExpiresIn", // case#27: refreshExpiresIn is not masked
        "notBeforePolicy" // case#28: notBeforePolicy is not masked
    })
    void testNonSensitiveFieldsNotMasked(String fieldName) throws Exception {
        Field field = AuthTokenResponse.class.getDeclaredField(fieldName);
        Masked masked = field.getAnnotation(Masked.class);

        // These fields should not have @Masked annotation
        assertEquals(null, masked, "Field " + fieldName + " should not have @Masked annotation");
    }
}