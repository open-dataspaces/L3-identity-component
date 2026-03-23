/*
 * APIKeyControllerTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This package contains unit tests for the APIKeyController class,
 * which handles HTTP requests related to apikey verify in the user authentication backend system.
 *
 * Date: 2025/11/20
 */

package io.github.open_dataspaces.core.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.dto.APIKeyVerifyRequest;
import io.github.open_dataspaces.core.domain.dto.APIKeyVerifyResponse;
import io.github.open_dataspaces.core.domain.dto.APIResponse;
import io.github.open_dataspaces.core.domain.service.interfaces.APIKeyService;
import io.github.open_dataspaces.core.exception.AbstractBaseException;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnexpectedException;
import io.github.open_dataspaces.core.exception.ValidateException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * APIKeyControllerTest is a test class for the APIKeyController.
 */
@ExtendWith(MockitoExtension.class)
public class APIKeyControllerTest {

    @Mock
    private APIKeyService apiKeyService;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private APIKeyController apikeyController;

    /**
     * setUp sets up the test environment before each test.
     */
    @BeforeEach
    void setUp() {
        // Set up ServletRequestAttributes
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/test-context");
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    /**
     * tearDown cleans up the test environment after each test.
     */
    @AfterEach
    void tearDown() {
        // Clear RequestContextHolder after test
        RequestContextHolder.resetRequestAttributes();
    }

    /**
     * verifyAPIKey tests the successful.
     */
    @ParameterizedTest
    @CsvSource({
        "Sample-API-Key",
        // Boundary values
        "LONG",
        "a",
    })
    void verifyAPIKey_success(
            String verifyAPIKey
    ) throws Exception {
        // Handle special cases for LONG inputs
        verifyAPIKey = "LONG".equals(verifyAPIKey) ? "u".repeat(Const.VERIFY_API_KEY_LENGTH_MAX) : verifyAPIKey;
        // Arrange
        APIKeyVerifyRequest requestBody = new APIKeyVerifyRequest(verifyAPIKey);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(apiKeyService.verify(verifyAPIKey)).thenReturn(true);
        // Act
        ResponseEntity<APIResponse<APIKeyVerifyResponse>>  response = apikeyController.verifyAPIKey(
                httpServletRequest, requestBody, bindingResult);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        APIResponse<?> apiResponse = (APIResponse<?>) response.getBody();
        APIKeyVerifyResponse getAPIKeyVerifyResponse = (APIKeyVerifyResponse) apiResponse.getData();
        assertEquals(true, getAPIKeyVerifyResponse.isVerifyResult());
    }

    /**
     * Test for verifyAPIKey - Validation error.
     */
    @Test
    @DisplayName("verifyAPIKey - Validation Error")
    void verifyAPIKey_validationError() throws Exception {
        String verifyAPIKey = "Sample-API-Key";
        // Arrange
        APIKeyVerifyRequest requestBody = new APIKeyVerifyRequest(verifyAPIKey);
        when(bindingResult.hasErrors()).thenReturn(true);
        // Act & Assert
        ValidateException exception = assertThrows(ValidateException.class, () ->
                apikeyController.verifyAPIKey(
                httpServletRequest, requestBody, bindingResult)
        );
        assertEquals(ConstError.ERRLOG_400_INVALID_REQUEST, exception.getLogMessage());
        assertNotNull(exception.getMessage());
    }

    /**
     * Test for verifyAPIKey when APIKeyService.verifyAPIKey throws an exception.
     */
    @ParameterizedTest
    @CsvSource({
        // Exception
        "BadParametersException",
        "UnexpectedException",
        "OutOfServiceException",
        "RuntimeException"
    })
    @DisplayName("verifyAPIKey throws exception when APIKeyService.verifyAPIKey fails")
    void verifyAPIKey_serviceThrowsException(String exceptionClassName) throws Exception {
        String verifyAPIKey = "Sample-API-Key";
        // Arrange
        APIKeyVerifyRequest requestBody = new APIKeyVerifyRequest(verifyAPIKey);
        when(bindingResult.hasErrors()).thenReturn(false);
        // Prepare exception to be thrown by identityProviderService.updateAccount
        String exceptionLogMessage = null;
        String exceptionResponseMessage = null;
        Exception exception = null;
        Class<? extends Throwable> clazz = null;
        switch (exceptionClassName) {
            case "BadParametersException":
                exceptionLogMessage = String.format(ConstError.ERRLOG_400_INVALID_GRANT, "dummy message");
                exceptionResponseMessage = ConstError.ERR_400_INVALID_GRANT;
                exception = new BadParametersException(exceptionLogMessage, exceptionResponseMessage);
                clazz = BadParametersException.class;
                break;
            case "UnexpectedException":
                exceptionLogMessage = String.format(ConstError.ERRLOG_500_ERROR, "dummy parameter", "dummy message");
                exceptionResponseMessage = ConstError.ERR_500_MESSAGE;
                exception = new UnexpectedException(ConstError.ERRLOG_500_KEYCLOAK_BUILD, exceptionResponseMessage);
                clazz = UnexpectedException.class;
                break;
            case "OutOfServiceException":
                exceptionLogMessage = ConstError.ERR_503_OUT_OF_SERVICE_KEYCLOAK;
                exceptionResponseMessage = "";
                exception = new OutOfServiceException(exceptionLogMessage);
                clazz = OutOfServiceException.class;
                break;
            case "RuntimeException":
                exception = new RuntimeException("Service error");
                clazz = RuntimeException.class;
                break;
            default:
                // Unexpected exception
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        // Mock operatorService to throw an exception
        doThrow(exception).when(apiKeyService).verify(anyString());

        // Act & Assert
        Throwable thrownException = assertThrows(clazz, () -> {
            apikeyController.verifyAPIKey(
                    httpServletRequest, requestBody, bindingResult);
        });

        // Detailed verification according to exception type
        if (exception instanceof AbstractBaseException) {
            AbstractBaseException baseException = (AbstractBaseException) exception;
            AbstractBaseException thrownBaseException = (AbstractBaseException) thrownException;
            assertEquals(baseException.getLogMessage(), thrownBaseException.getLogMessage());
            assertEquals(baseException.getResponseMessage(), thrownBaseException.getResponseMessage());
        } else {
            assertEquals(exception.getMessage(), thrownException.getMessage());
        }

        // Verify interactions
        verify(bindingResult).hasErrors();
        verify(apiKeyService).verify(anyString());
    }
}
