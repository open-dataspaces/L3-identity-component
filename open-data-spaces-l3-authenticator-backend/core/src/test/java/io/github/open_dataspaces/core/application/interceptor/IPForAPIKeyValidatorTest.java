/*
 * IPForAPIKeyValidatorTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class tests the IPForAPIKeyValidator functionality
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.application.interceptor;

import io.github.open_dataspaces.core.common.config.SpringProperties;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.dto.APIKeyVerifyParam;
import io.github.open_dataspaces.core.domain.service.interfaces.APIKeyService;
import io.github.open_dataspaces.core.exception.IllegalAuthDataException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.mockito.Mock;
import org.mockito.Mockito;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Unit tests for the IPForAPIKeyValidator.
 */
public class IPForAPIKeyValidatorTest {

    @Mock
    private SpringProperties springProperties;

    private APIKeyService apiKeyService;
    private IPForAPIKeyValidator validator;
    private HttpServletRequest request;
    private HttpServletResponse response;

    /**
     * Sets up the test environment before each test.
     */
    @BeforeEach
    void setUp() {
        springProperties = Mockito.mock(SpringProperties.class);
        apiKeyService = Mockito.mock(APIKeyService.class);
        validator = new IPForAPIKeyValidator(springProperties, apiKeyService);
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);

        Mockito.when(springProperties.getProfiles()).thenReturn(Mockito.mock(SpringProperties.Profiles.class));
    }

    /**
     * Retrieves the private method getIP from the IPForAPIKeyValidator class.
     *
     * @return Method object representing the getIP method
     * @throws NoSuchMethodException if the method does not exist
     */
    private Method getIPMethod() throws NoSuchMethodException {
        Method method = IPForAPIKeyValidator.class.getDeclaredMethod("getIP", HttpServletRequest.class);
        method.setAccessible(true);
        return method;
    }

    /**
     * (Case#1-2) Tests if the getIP method returns the correct IP address.
     *
     * @throws NoSuchMethodException if the method does not exist
     * @throws IllegalAccessException if the method cannot be accessed
     * @throws InvocationTargetException if the method invocation fails
     */
    @ParameterizedTest
    @ValueSource(strings = {
        "192.168.1.1, 10.0.0.1", // Case#1: Two IPs
        "10.0.0.2, 192.168.1.1, 10.0.0.1" // Case#2: Three IPs
    })
    void testGetIP_withXForwardedForHeader() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        when(springProperties.getProfiles().getActive()).thenReturn("dummy");
        Mockito.when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");

        Method getIPMethod = getIPMethod();
        String ip = (String) getIPMethod.invoke(validator, request);
        assertEquals("192.168.1.1", ip, "The IP should be extracted from the X-Forwarded-For header");
    }

    /**
     * (Case#3) Tests if the getIP method returns the remote IP address.
     *
     * @throws NoSuchMethodException if the method does not exist
     * @throws IllegalAccessException if the method cannot be accessed
     * @throws InvocationTargetException if the method invocation fails
     * @throws NoSuchFieldException if the field does not exist
     * @throws SecurityException if a security violation occurs
     */
    @Test
    void testGetIP_local() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException {
        when(springProperties.getProfiles().getActive()).thenReturn(Const.SPRING_PROFILES_ACTIVE_LOCAL);

        // Mock the request to return a valid local IP
        Mockito.when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        Method getIPMethod = getIPMethod();
        String ip = (String) getIPMethod.invoke(validator, request);
        assertEquals("127.0.0.1", ip, "The IP should be remote ip when environment is local");
    }

    /**
     * (Case#4) Tests if a valid API key.
     */
    @Test
    void testValidateAPIKey_valid() {
        when(springProperties.getProfiles().getActive()).thenReturn(Const.SPRING_PROFILES_ACTIVE_LOCAL);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        Mockito.when(apiKeyService.verify(Mockito.any(APIKeyVerifyParam.class))).thenReturn(true);

        assertDoesNotThrow(() -> validator.validateAPIKey(request, response, "VALID_API_KEY"));
    }

    /**
     * (Case#5-9) Tests if an exception is thrown when the wrong IP is provided.
     */
    @ParameterizedTest
    @ValueSource(strings = {
        "10.0.0.1", // Case#5: Single IP
        ",10.0.0.1", // Case#6: Leading comma
        "10.0.0.1," // Case#7: Trailing comma
    })
    @NullAndEmptySource
    // Case#8: Null IP
    // Case#9: Empty IP
    void testValidateAPIKey_missingIP(String xff) {
        Mockito.when(request.getHeader("X-Forwarded-For")).thenReturn(xff);

        IllegalAuthDataException exception = assertThrows(IllegalAuthDataException.class, () -> {
            validator.validateAPIKey(request, response, "VALID_API_KEY");
        });

        assertEquals(ConstError.ERR_403_IP_NOT_PROVIDED, exception.getResponseMessage());
        assertEquals(ConstError.ERRLOG_403_IP_NOT_PROVIDED, exception.getLogMessage());
        assertEquals(Const.SOURCE_AUTH, exception.getSource());
    }

    /**
     * (Case#10-11) Tests if an exception is thrown when the API key is missing.
     *
     * @param apiKey the API key to test, can be null or empty
     */
    @ParameterizedTest
    @NullAndEmptySource
    // Case#10: Null API key
    // Case#11: Empty API key
    void testValidateAPIKey_missingAPIKey(String apiKey) {
        Mockito.when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");

        IllegalAuthDataException exception = assertThrows(IllegalAuthDataException.class, () -> {
            validator.validateAPIKey(request, response, apiKey);
        });

        assertEquals(ConstError.ERR_403_APIKEY_NOT_PROVIDED, exception.getResponseMessage());
        assertEquals(ConstError.ERRLOG_403_APIKEY_NOT_PROVIDED, exception.getLogMessage());
        assertEquals(Const.SOURCE_AUTH, exception.getSource());
    }

    /**
     * (Case#12) Tests if an exception is thrown when the IP is not authorized for the API key.
     */
    @Test
    void testValidateAPIKey_invalidIPForAPIKey() {
        Mockito.when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");
        Mockito.when(apiKeyService.verify(Mockito.any(APIKeyVerifyParam.class))).thenReturn(false);

        IllegalAuthDataException exception = assertThrows(IllegalAuthDataException.class, () -> {
            validator.validateAPIKey(request, response, "VALID_API_KEY");
        });

        assertEquals(ConstError.ERR_403_IP_NOT_AUTHORIZED_FOR_KEY, exception.getResponseMessage());
        assertTrue(exception.getLogMessage().contains("IP address not authorized for API key"));
        assertEquals(Const.SOURCE_AUTH, exception.getSource());
    }
}