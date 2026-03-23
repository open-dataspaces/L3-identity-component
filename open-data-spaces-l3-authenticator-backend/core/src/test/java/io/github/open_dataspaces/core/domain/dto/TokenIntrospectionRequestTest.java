/*
 * TokenIntrospectionRequestTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies the behavior and validation logic
 * of the TokenIntrospectionRequest DTO.
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
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.open_dataspaces.core.common.annotations.Masked;
import io.github.open_dataspaces.core.common.consts.Const;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/*
 * TokenIntrospectionRequestTest is a test class for the TokenIntrospectionRequest.
 */
class TokenIntrospectionRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * case#1:
     * testDefaultConstructor tests the default constructor of TokenIntrospectionResponse.
     */
    @Test
    void testDefaultConstructor() {
        TokenIntrospectionRequest request = new TokenIntrospectionRequest();

        // Verify that fields are initialized to null or default values
        assertNull(request.getAccessToken());
        assertNull(request.getClientId());
        assertNull(request.getClientSecret());
        // Check other fields as needed

        // Check that validation violations occur (if there are required fields)
        Set<ConstraintViolation<TokenIntrospectionRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    /**
     * case#1:
     * testGetterSetter tests the getter and setter methods of TokenIntrospectionRequest.
     */
    @Test
    void testAllPropertiesHaveGetterAndSetter() {
        Field[] fields = TokenIntrospectionRequest.class.getDeclaredFields();
        Class<?> clazz = TokenIntrospectionRequest.class;

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
                    // getter
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

                    // setter
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
     * Tests whether the @JsonProperty annotation of TokenIntrospectionRequest is set correctly.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "accessToken, " + Const.JSON_PROPERTY_ACCESS_TOKEN,
        "clientId, " + Const.JSON_PROPERTY_CLIENT_ID,
        "clientSecret, " + Const.JSON_PROPERTY_CLIENT_SECRET
    })
    void testJsonPropertyNames(String propertyName, String jsonPropertyName) throws Exception {
        Field field = TokenIntrospectionRequest.class.getDeclaredField(propertyName);
        JsonProperty annotation = field.getAnnotation(JsonProperty.class);
        assertNotNull(annotation);
        assertEquals(jsonPropertyName, annotation.value());
    }

    /**
     * Tests whether the @Masked annotation of TokenIntrospectionRequest is set correctly.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "accessToken, 10, 5",
        "clientSecret, 0, 0",
    })
    void testMaskedAnnotation(String propertyName, long unmaskedPrefixLength, long unmaskedSuffixLength) throws Exception {
        Field field = TokenIntrospectionRequest.class.getDeclaredField(propertyName);
        Masked masked = field.getAnnotation(Masked.class);
        assertNotNull(masked);

        assertEquals(unmaskedPrefixLength, masked.unmaskedPrefixLength());
        assertEquals(unmaskedSuffixLength, masked.unmaskedSuffixLength());
    }

    /**
     * testIdTokenValidation tests the idToken validation logic
     * for various input values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "true,access-token-123,client-id-123,client-secret-123",
        "false,NULL,client-id-123,client-secret-123",
        "true,access-token-123,MIN,client-secret-123",
        "true,access-token-123,MAX,client-secret-123",
        "false,access-token-123,NULL,client-secret-123",
        "false,access-token-123,BELOW,client-secret-123",
        "false,access-token-123,OVER,client-secret-123",
        "true,access-token-123,client-id-123,MIN",
        "true,access-token-123,client-id-123,MAX",
        "false,access-token-123,client-id-123,NULL",
        "false,access-token-123,client-id-123,BELOW",
        "false,access-token-123,client-id-123,OVER"
    }, nullValues = "NULL")
    void testValidation(boolean isValid,
            String accessToken, String clientId, String clientSecret) {
        TokenIntrospectionRequest request = new TokenIntrospectionRequest();

        // Set value
        if ("NULL".equals(accessToken)) {
            accessToken = null;
        }

        if ("NULL".equals(clientId)) {
            clientId = null;
        }
        if ("MIN".equals(clientId)) {
            clientId = "a".repeat(Const.CLIENT_ID_LENGTH_MIN);
        }
        if ("MAX".equals(clientId)) {
            clientId = "a".repeat(Const.CLIENT_ID_LENGTH_MAX);
        }
        if ("BELOW".equals(clientId)) {
            clientId = "a".repeat(Const.CLIENT_ID_LENGTH_MIN - 1);
        }
        if ("OVER".equals(clientId)) {
            clientId = "a".repeat(Const.CLIENT_ID_LENGTH_MAX + 1);
        }

        if ("NULL".equals(clientSecret)) {
            clientSecret = null;
        }
        if ("MIN".equals(clientSecret)) {
            clientSecret = "a".repeat(Const.CLIENT_SECRET_LENGTH_MIN);
        }
        if ("MAX".equals(clientSecret)) {
            clientSecret = "a".repeat(Const.CLIENT_SECRET_LENGTH_MAX);
        }
        if ("BELOW".equals(clientSecret)) {
            clientSecret = "a".repeat(Const.CLIENT_SECRET_LENGTH_MIN - 1);
        }
        if ("OVER".equals(clientSecret)) {
            clientSecret = "a".repeat(Const.CLIENT_SECRET_LENGTH_MAX + 1);
        }

        request.setAccessToken(accessToken);
        request.setClientId(clientId);
        request.setClientSecret(clientSecret);

        // Assert
        Set<ConstraintViolation<TokenIntrospectionRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty(),
                    () -> {
                        // Only show details if violations are not empty
                        if (!violations.isEmpty()) {
                            ConstraintViolation<TokenIntrospectionRequest> v = violations.iterator().next();
                            return String.format("Value '%s' for Property '%s' failed validation test: %s",
                                    v.getInvalidValue(), v.getPropertyPath(), v.getMessage());
                        }
                        return "Unexpected validation failure";
                    });
        } else {
            assertFalse(violations.isEmpty());
        }
    }
}