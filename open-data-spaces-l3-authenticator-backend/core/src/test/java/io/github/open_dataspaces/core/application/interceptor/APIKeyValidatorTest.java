/*
 * APIKeyValidatorTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class tests the APIKeyValidator functionality,
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.application.interceptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.any;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.service.interfaces.APIKeyService;
import io.github.open_dataspaces.core.exception.IllegalAuthDataException;
import io.github.open_dataspaces.core.domain.dto.APIKeyVerifyParam;

/**
 * Test class for APIKeyValidator.
 */
public class APIKeyValidatorTest {

    private APIKeyService apiKeyService;
    private APIKeyValidator validator;
    private HttpServletRequest request;
    private HttpServletResponse response;

    /**
     * Sets up the test environment before each test.
     */
    @BeforeEach
    void setUp() {
        apiKeyService = Mockito.mock(APIKeyService.class);
        validator = new APIKeyValidator(apiKeyService);
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
    }

    /**
     * (Case#1) Tests if a valid API key is correctly identified.
     *
     * @throws Exception if an error occurs during the test
     */
    @Test
    void testIsValid_validKey() throws Exception {
        String validKey = "VALID-KEY";
        Mockito.when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(validKey);
        Mockito.when(apiKeyService.verify(any(APIKeyVerifyParam.class))).thenReturn(true);

        assertTrue(validator.preHandle(request, response, new Object()), "A valid API key should return true");
    }

    /**
     * (Case#2) Tests if an invalid API key throws an exception.
     */
    @Test
    void testIsValid_invalidKey() {
        String invalidKey = "INVALID";
        Mockito.when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(invalidKey);
        Mockito.when(apiKeyService.verify(any(APIKeyVerifyParam.class))).thenReturn(false);

        IllegalAuthDataException exception = assertThrows(IllegalAuthDataException.class, () -> {
            validator.preHandle(request, response, new Object());
        });
        assertEquals(ConstError.ERR_403_APIKEY_NOT_VALID, exception.getResponseMessage());
        assertEquals(ConstError.ERRLOG_403_APIKEY_NOT_VALID, exception.getLogMessage());
        assertEquals(Const.SOURCE_AUTH, exception.getSource());
    }

    /**
     * (Case#3) Tests if a null API key throws an exception.
     */
    @Test
    void testIsValid_nullKey() {
        Mockito.when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(null);

        IllegalAuthDataException exception = assertThrows(IllegalAuthDataException.class, () -> {
            validator.preHandle(request, response, new Object());
        });
        assertEquals(ConstError.ERR_403_APIKEY_NOT_PROVIDED, exception.getResponseMessage());
        assertEquals(ConstError.ERRLOG_403_APIKEY_NOT_PROVIDED, exception.getLogMessage());
        assertEquals(Const.SOURCE_AUTH, exception.getSource());
    }

    /**
     * (Case#4) Tests if an empty API key throws an exception.
     */
    @Test
    void testIsValid_emptyKey() {
        Mockito.when(request.getHeader(Const.HEADER_API_KEY)).thenReturn("");

        IllegalAuthDataException exception = assertThrows(IllegalAuthDataException.class, () -> {
            validator.preHandle(request, response, new Object());
        });
        assertEquals(ConstError.ERR_403_APIKEY_NOT_PROVIDED, exception.getResponseMessage());
        assertEquals(ConstError.ERRLOG_403_APIKEY_NOT_PROVIDED, exception.getLogMessage());
        assertEquals(Const.SOURCE_AUTH, exception.getSource());
    }
}