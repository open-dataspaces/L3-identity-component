/*
 * TokenRefreshResponseTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies the behavior and validation logic
 * of the TokenRefreshResponse DTO.
 *
 * Date: 2025-06-30
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.open_dataspaces.core.common.consts.Const;

/*
 * TokenRefreshResponseTest is a test class for the TokenRefreshResponse.
 */
class TokenRefreshResponseTest {

    /**
     * setUpValidator initializes the Validator instance before all tests.
     */
    @BeforeAll
    static void setUpValidator() {
    }

    /**
     * case#2:
     * testParameterizedConstructor tests the parameterized constructor of TokenRefreshResponse.
     */
    @Test
    void testParameterizedConstructor() {
        String accessToken = "access-token-123";
        long expiresIn = 123L;
        String tokenType = "Bearer";
        int notBeforePolicy = 0;
        String scope = "openid";
        String refreshToken = "refresh-token-123";
        long refreshExpiresIn = 456L;
        String idToken = "id-token-123";

        TokenRefreshResponse response = new TokenRefreshResponse(
                accessToken, expiresIn, tokenType, notBeforePolicy, scope, refreshToken, refreshExpiresIn, idToken);

        // Assert
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(expiresIn, response.getExpiresIn());
        assertEquals(tokenType, response.getTokenType());
        assertEquals(notBeforePolicy, response.getNotBeforePolicy());
        assertEquals(scope, response.getScope());
        assertEquals(refreshToken, response.getRefreshToken());
        assertEquals(refreshExpiresIn, response.getRefreshExpiresIn());
        assertEquals(idToken, response.getIdToken());

    }

    /**
     * case#1:
     * testGetterSetter tests the getter and setter methods of TokenRefreshRequest.
     */
    @Test
    void testAllPropertiesHaveGetterAndSetter() {
        Field[] fields = TokenRefreshResponse.class.getDeclaredFields();
        Class<?> clazz = TokenRefreshResponse.class;

        for (Field field : fields) {
            String prop = field.getName();

            switch (field.getType().getSimpleName()) {
                case "boolean":
                    // isXXX()
                    String isName = "is" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
                    try {
                        Method method = clazz.getMethod(isName);
                        assertNotNull(method);
                    } catch (NoSuchMethodException e) {
                        fail(isName + " Not Found");
                    }
                    break;
                default:
                    // Getter
                    StringBuilder sb = new StringBuilder();
                    String getterName = sb.append("get")
                            .append(prop.substring(0, 1).toUpperCase())
                            .append(prop.substring(1)).toString();
                    try {
                        Method method = clazz.getMethod(getterName);
                        assertNotNull(method);
                    } catch (NoSuchMethodException e) {
                        fail(getterName + " Not Found");
                    }

                    // Setter
                    sb = new StringBuilder();
                    String setterName = sb.append("set")
                            .append(prop.substring(0, 1).toUpperCase())
                            .append(prop.substring(1)).toString();
                    try {
                        Method method = clazz.getMethod(setterName, field.getType());
                        assertNotNull(method);
                    } catch (NoSuchMethodException e) {
                        fail(setterName + " Not Found");
                    }
                    break;
            }
        }
    }

    /**
     * Test that the @JsonProperty annotation of TokenRefreshRequest is set correctly.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "accessToken, " + Const.JSON_PROPERTY_ACCESS_TOKEN,
        "expiresIn, " + Const.JSON_PROPERTY_EXPIRES_IN,
        "tokenType, " + Const.JSON_PROPERTY_TOKEN_TYPE,
        "notBeforePolicy, " + Const.JSON_PROPERTY_NOT_BEFORE_POLICY,
        "scope, " + Const.JSON_PROPERTY_SCOPE,
        "refreshToken, " + Const.JSON_PROPERTY_REFRESH_TOKEN,
        "refreshExpiresIn, " + Const.JSON_PROPERTY_REFRESH_EXPIRES_IN,
        "idToken, " + Const.JSON_PROPERTY_ID_TOKEN
    })
    void testJsonPropertyNames(String propertyName, String jsonPropertyName) throws Exception {
        Field field = TokenRefreshResponse.class.getDeclaredField(propertyName);
        JsonProperty annotation = field.getAnnotation(JsonProperty.class);
        assertNotNull(annotation);
        assertEquals(jsonPropertyName, annotation.value());
    }

    /**
     * Tests the behavior when idToken is null.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "idToken"
    })
    void testIsNull_thenNotIncludedInJson(String propertyName) throws Exception {

        TokenRefreshResponse response = new TokenRefreshResponse(
                null, // accessToken
                0L, // expiresIn
                null, // tokenType
                0, // notBeforePolicy
                null, // scope
                null, // refreshToken
                0L, // refreshExpiresIn
                null // idToken
        );

        // check JsonInclude annotation
        Field field = TokenRefreshResponse.class.getDeclaredField(propertyName);
        JsonInclude annotationJsonInclude = field.getAnnotation(JsonInclude.class);
        assertNotNull(annotationJsonInclude);
        assertEquals(JsonInclude.Include.NON_NULL, annotationJsonInclude.value());

        // check JsonProperty annotation
        JsonProperty annotationJsonProperty = field.getAnnotation(JsonProperty.class);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(response);
        assertFalse(json.contains(annotationJsonProperty.value()));
    }
}