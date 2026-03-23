/*
 * TokenIntrospectionResponseTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies the behavior and validation logic
 * of the TokenIntrospectionResponse DTO.
 *
 * Date: 2025-06-30
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.open_dataspaces.core.common.consts.Const;

/*
 * TokenIntrospectionResponseTest is a test class for the TokenIntrospectionResponse.
 */
class TokenIntrospectionResponseTest {

    /**
     * setUpValidator initializes the Validator instance before all tests.
     */
    @BeforeAll
    static void setUpValidator() {
    }

    /**
     * case#1:
     * testDefaultConstructor tests the default constructor of TokenIntrospectionResponse.
     */
    @Test
    void testDefaultConstructor() {
        TokenIntrospectionResponse response = new TokenIntrospectionResponse();
        // Assert
        assertFalse(response.isActive());
        assertNull(response.getTokenInfo());

        Object result = response.getTokenInfoForJson();
        assertTrue(result instanceof HashMap);
        assertTrue(((HashMap<?, ?>) result).isEmpty());
    }

    /**
     * case#2:
     * testParameterizedConstructor tests the parameterized constructor of TokenIntrospectionResponse.
     */
    @Test
    void testParameterizedConstructor() {
        long exp = 123L;
        long iat = 456L;
        String operatorId = "123e4567-e89b-12d3-a456-426614174000";
        String openSystemId = "OPEN_SYS_01";
        String scope = "openid";
        String clientId = "client-id-123";
        String tokenType = "Bearer";

        TokenIntrospectionResponse response = new TokenIntrospectionResponse(
                exp, iat, operatorId, openSystemId, scope, clientId, tokenType);

        // Assert
        assertTrue(response.isActive());
        assertNotNull(response.getTokenInfo());
        assertEquals(exp, response.getTokenInfo().getExp());
        assertEquals(iat, response.getTokenInfo().getIat());
        assertEquals(operatorId, response.getTokenInfo().getOperatorId());
        assertEquals(openSystemId, response.getTokenInfo().getOpenSystemId());
        assertEquals(scope, response.getTokenInfo().getScope());
        assertEquals(clientId, response.getTokenInfo().getClientId());
        assertEquals(tokenType, response.getTokenInfo().getTokenType());

        Object result = response.getTokenInfoForJson();
        assertNotNull(result);
        assertTrue(result instanceof TokenIntrospectionResponse.TokenInfo);

        TokenIntrospectionResponse.TokenInfo info = (TokenIntrospectionResponse.TokenInfo) result;
        assertEquals(exp, info.getExp());
        assertEquals(iat, info.getIat());
        assertEquals(operatorId, info.getOperatorId());
        assertEquals(openSystemId, info.getOpenSystemId());
        assertEquals(scope, info.getScope());
        assertEquals(clientId, info.getClientId());
        assertEquals(tokenType, info.getTokenType());

    }

    /**
     * case#1:
     * testGetterSetter tests the getter and setter methods of TokenIntrospectionRequest.
     */
    @Test
    void testAllPropertiesHaveGetterAndSetter() {
        Field[] fields = TokenIntrospectionResponse.class.getDeclaredFields();
        Class<?> clazz = TokenIntrospectionResponse.class;

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
     * Test that the @JsonProperty annotation of TokenIntrospectionRequest is set correctly.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "active, " + Const.JSON_PROPERTY_ACTIVE
    })
    void testJsonPropertyNames(String propertyName, String jsonPropertyName) throws Exception {
        Field field = TokenIntrospectionResponse.class.getDeclaredField(propertyName);
        JsonProperty annotation = field.getAnnotation(JsonProperty.class);
        assertNotNull(annotation);
        assertEquals(jsonPropertyName, annotation.value());
    }

    /**
     * Tests whether the @JsonProperty annotation of TokenIntrospectionResponse is set correctly.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "getTokenInfoForJson, " + Const.JSON_PROPERTY_TOKEN_INFO
    })
    void testMethod_jsonPropertyNames(String propertyName, String jsonPropertyName) throws Exception {
        Method method = TokenIntrospectionResponse.class.getMethod(propertyName);
        JsonProperty annotation = method.getAnnotation(JsonProperty.class);
        assertNotNull(annotation);
        assertEquals(jsonPropertyName, annotation.value());
    }

    /**
     * Tests whether the @JsonProperty annotation of TokenIntrospectionRequest.TokenInfo is set correctly.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "exp, " + Const.JSON_PROPERTY_EXP,
        "iat, " + Const.JSON_PROPERTY_IAT,
        "operatorId, " + Const.JSON_PROPERTY_OPERATOR_ID,
        "openSystemId, " + Const.JSON_PROPERTY_OPEN_SYSTEM_ID,
        "scope, " + Const.JSON_PROPERTY_SCOPE,
        "clientId, " + Const.JSON_PROPERTY_CLIENT_ID,
        "tokenType, " + Const.JSON_PROPERTY_TOKEN_TYPE
    })
    void testTokenInfo_jsonPropertyNames(String propertyName, String jsonPropertyName) throws Exception {
        Field field = TokenIntrospectionResponse.TokenInfo.class.getDeclaredField(propertyName);
        JsonProperty annotation = field.getAnnotation(JsonProperty.class);
        assertNotNull(annotation);
        assertEquals(jsonPropertyName, annotation.value());
    }

    /**
     * Tests the behavior when a property of tokenInfo is null.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "operatorId",
        "openSystemId"
    })
    void testIsNull_thenNotIncludedInJson(String propertyName) throws Exception {

        TokenIntrospectionResponse response = new TokenIntrospectionResponse();

        // check JsonInclude annotation
        Field field = TokenIntrospectionResponse.TokenInfo.class.getDeclaredField(propertyName);
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