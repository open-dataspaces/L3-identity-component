/*
 * TokenIntrospectionResultTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * Unit tests for the TokenIntrospectionResult class.
 *
 * <p>This test class verifies the correct behavior of the TokenIntrospectionResult class.
 * </p>
 *
 * @author btmurayamakohei
 * @since 2025/06/17
 */

package io.github.open_dataspaces.core.domain.entities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.open_dataspaces.core.common.consts.Const;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
/**
 * Unit tests for the CidrsKey class.
 *
 * <p>This test class verifies the correct behavior of the CidrsKey class,
 * including its getter, setter, equals, and hashCode methods.
 * </p>
 *
 * <p>The tests ensure that:
 * <ul>
 *   <li>Property values can be set and retrieved correctly</li>
 *   <li>The parameterized constructor initializes fields as expected</li>
 *   <li>Equals and hashCode methods work as intended for various cases</li>
 *   <li>Comparison with null and different types returns false</li>
 *   <li>Objects with the same cidr and apiKey are considered equal</li>
 *   <li>Objects with different cidr or apiKey are not considered equal</li>
 * </ul>
 * </p>
 *
 * @author btmurayamakohei
 * @since 2025/06/17
 */

class TokenIntrospectionResultTest {

    /**
     * case#1:
     * testDefaultConstructor tests the default constructor of TokenIntrospectionResponse.
     */
    @Test
    void testDefaultConstructor() {
        TokenIntrospectionResult result = new TokenIntrospectionResult();
        // Assert
        assertFalse(result.isActive());
        assertNull(result.getUserName());
        assertNull(result.getSub());
        assertNull(result.getClientId());
        assertNull(result.getScope());
        assertEquals(0L, result.getExp());
        assertNull(result.getTyp());
        assertNull(result.getOperatorId());
        assertNull(result.getOpenSystemId());
        assertEquals(0L, result.getIat());
        assertNull(result.getIss());
    }

    /**
     * case#1:
     * testGetterSetter tests the getter and setter methods of TokenIntrospectionRequest.
     */
    @Test
    void testAllPropertiesHaveGetterAndSetter() {
        Field[] fields = TokenIntrospectionResult.class.getDeclaredFields();
        Class<?> clazz = TokenIntrospectionResult.class;

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
     * Tests whether the @JsonProperty annotation is correctly set on TokenIntrospectionResult fields.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "active, " + Const.JSON_PROPERTY_ACTIVE,
        "userName, " + Const.JSON_PROPERTY_USERNAME,
        "sub, " + Const.JSON_PROPERTY_SUB,
        "clientId, " + Const.JSON_PROPERTY_CLIENT_ID,
        "scope, " + Const.JSON_PROPERTY_SCOPE,
        "exp, " + Const.JSON_PROPERTY_EXP,
        "typ, " + Const.JSON_PROPERTY_TYP,
        "operatorId, " + Const.JSON_PROPERTY_OPERATOR_ID,
        "openSystemId, " + Const.JSON_PROPERTY_OPEN_SYSTEM_ID,
        "iat, " + Const.JSON_PROPERTY_IAT,
        "iss, " + Const.JSON_PROPERTY_ISS
    })
    void testJsonPropertyNames(String propertyName, String jsonPropertyName) throws Exception {
        Field field = TokenIntrospectionResult.class.getDeclaredField(propertyName);
        JsonProperty annotation = field.getAnnotation(JsonProperty.class);
        assertNotNull(annotation);
        assertEquals(jsonPropertyName, annotation.value());
    }

}