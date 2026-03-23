/*
 * TokenRefreshControllerTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * Unit tests for the TokenRefreshController class.
 *
 * Date: 2025-06-30
 */

package io.github.open_dataspaces.core.application.controller;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.dto.APIResponse;
import io.github.open_dataspaces.core.domain.dto.TokenRefreshRequest;
import io.github.open_dataspaces.core.domain.dto.TokenRefreshResponse;
import io.github.open_dataspaces.core.domain.entities.AccessTokenResult;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.exception.AbstractBaseException;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnexpectedException;
import io.github.open_dataspaces.core.exception.ValidateException;
import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TokenRefreshControllerTest is a test class for the TokenRefreshController.
 * This class tests the functionality of the token introspection endpoint, ensuring that it correctly processes
 * requests and handles various scenarios, including successful introspection and error cases.
 *
 * @author YourName
 */
@ExtendWith(MockitoExtension.class)
class TokenRefreshControllerTest {

    @Mock
    private IdentityProviderService identityProviderService;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private TokenRefreshController controller;

    /**
     * setUp initializes the test environment before each test.
     */
    @BeforeEach
    void setUp() {
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
     * case#1:
     * testAuthUrl_success tests the successful generation of the authorization URL.
     */
    @Test
    void testTokenRefresh_success() {
        // Arrange
        TokenRefreshRequest requestBody = new TokenRefreshRequest();
        requestBody.setRefreshToken("refresh-token-123");
        requestBody.setClientId("client-id-123");
        requestBody.setClientSecret("client-secret-123");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn("api-key-123");

        AccessTokenResult serviceResult = mock(AccessTokenResult.class);
        when(serviceResult.getAccessToken()).thenReturn("access-token-456");
        when(serviceResult.getExpiresIn()).thenReturn(123L);
        when(serviceResult.getTokenType()).thenReturn("Bearer");
        when(serviceResult.getNotBeforePolicy()).thenReturn(0);
        when(serviceResult.getScope()).thenReturn("scope-123");
        when(serviceResult.getRefreshToken()).thenReturn("refresh-token-456");
        when(serviceResult.getRefreshExpiresIn()).thenReturn(456L);
        when(serviceResult.getIdToken()).thenReturn("id-token-456");

        when(identityProviderService.tokenRefresh(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(serviceResult);

        // Act
        ResponseEntity<APIResponse<TokenRefreshResponse>> responseEntity =
                controller.refresh(requestBody, bindingResult, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        APIResponse<TokenRefreshResponse> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());
        assertEquals("access-token-456", apiResponse.getData().getAccessToken());
        assertEquals(123L, apiResponse.getData().getExpiresIn());
        assertEquals("Bearer", apiResponse.getData().getTokenType());
        assertEquals(0, apiResponse.getData().getNotBeforePolicy());
        assertEquals("scope-123", apiResponse.getData().getScope());
        assertEquals("refresh-token-456", apiResponse.getData().getRefreshToken());
        assertEquals(456L, apiResponse.getData().getRefreshExpiresIn());
        assertEquals("id-token-456", apiResponse.getData().getIdToken());
    }

    /**
     * testAuthUrl_variousRequest_success tests the generation of the authorization URL with various request parameters.
     */
    @ParameterizedTest
    @CsvSource({
        // apiKey, clientId, clientSecret, accessToken
        "success, a, a, a, refresh-token-123", // case#2: min length 1 characters
        "success, LONG, LONG, LONG, refresh-token-123" // case#3: max length 255 characters
    })
    void testTokenRefresh_variousRequest_success(
            String result,
            String apiKey,
            String clientId,
            String clientSecret,
            String refreshToken
    ) {
        if ("LONG".equals(apiKey)) {
            apiKey = "a".repeat(255);
        }
        if ("LONG".equals(clientId)) {
            clientId = "a".repeat(255);
        }
        if ("LONG".equals(clientSecret)) {
            clientSecret = "a".repeat(255);
        }

        // Arrange
        TokenRefreshRequest requestBody = new TokenRefreshRequest();
        requestBody.setClientId(clientId);
        requestBody.setClientSecret(clientSecret);
        requestBody.setRefreshToken(refreshToken);

        AccessTokenResult serviceResult = mock(AccessTokenResult.class);
        when(serviceResult.getAccessToken()).thenReturn("access-token-456");
        when(serviceResult.getExpiresIn()).thenReturn(123L);
        when(serviceResult.getTokenType()).thenReturn("Bearer");
        when(serviceResult.getNotBeforePolicy()).thenReturn(0);
        when(serviceResult.getScope()).thenReturn("scope-123");
        when(serviceResult.getRefreshToken()).thenReturn("refresh-token-456");
        when(serviceResult.getRefreshExpiresIn()).thenReturn(456L);
        when(serviceResult.getIdToken()).thenReturn("id-token-456");

        when(bindingResult.hasErrors()).thenReturn(false);

        // success
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        when(identityProviderService.tokenRefresh(
                eq(apiKey),
                eq(refreshToken),
                eq(clientId),
                eq(clientSecret)
                )).thenReturn(serviceResult);

        // Act
        ResponseEntity<APIResponse<TokenRefreshResponse>> responseEntity = controller.refresh(requestBody, bindingResult, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        APIResponse<TokenRefreshResponse> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());
        assertEquals("access-token-456", apiResponse.getData().getAccessToken());
        assertEquals(123L, apiResponse.getData().getExpiresIn());
        assertEquals("Bearer", apiResponse.getData().getTokenType());
        assertEquals(0, apiResponse.getData().getNotBeforePolicy());
        assertEquals("scope-123", apiResponse.getData().getScope());
        assertEquals("refresh-token-456", apiResponse.getData().getRefreshToken());
        assertEquals(456L, apiResponse.getData().getRefreshExpiresIn());
        assertEquals("id-token-456", apiResponse.getData().getIdToken());
    }

    /**
     * case #6: BindingResult has errors.
     */
    @Test
    void testTokenRefresh_bindingResultHasErrors_throwsValidateException() {
        // Arrange
        TokenRefreshRequest requestBody = new TokenRefreshRequest();
        when(bindingResult.hasErrors()).thenReturn(true);

        // Act & Assert
        ValidateException exception = assertThrows(ValidateException.class, () ->
                controller.refresh(requestBody, bindingResult, httpServletRequest)
        );
        assertEquals(ConstError.ERRLOG_400_INVALID_REQUEST, exception.getLogMessage());
        assertNotNull(exception.getMessage());
    }

    /**
     * Tests the token introspection endpoint with various exception scenarios.
     */
    @ParameterizedTest
    @CsvSource({
        // Exception
        "BadParametersException", // Invalid parameters
        "OutOfServiceException", // Service unavailable
        "UnexpectedException" // Unexpected error
    })
    void testTokenRefresh_identityProviderService_throwsException(
            String exceptionClassName
    ) {
        // Arrange
        TokenRefreshRequest requestBody = new TokenRefreshRequest();
        requestBody.setClientId("client-id-123");
        requestBody.setClientSecret("client-secret-123");
        requestBody.setRefreshToken("refresh-token-123");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn("api-key-123");

        String exceptionLogMessage = null;
        String exceptionResponseMessage = null;
        AbstractBaseException exception = null;
        Class<? extends Throwable> clazz = null;

        switch (exceptionClassName) {
            case "BadParametersException":
                exceptionLogMessage = String.format(ConstError.ERRLOG_400_INVALID_GRANT, "dummy message");
                exceptionResponseMessage = ConstError.ERR_400_INVALID_GRANT;
                exception = new BadParametersException(exceptionLogMessage, exceptionResponseMessage);
                clazz = BadParametersException.class;
                break;
            case "OutOfServiceException":
                exceptionLogMessage = ConstError.ERR_503_OUT_OF_SERVICE_KEYCLOAK;
                exceptionResponseMessage = "";
                exception = new OutOfServiceException(exceptionLogMessage);
                clazz = OutOfServiceException.class;
                break;
            case "UnexpectedException":
                exceptionLogMessage = String.format(ConstError.ERRLOG_500_ERROR, ConstError.ERRLOG_500_KEYCLOAK_BUILD, ConstError.ERR_500_MESSAGE);
                exceptionResponseMessage = ConstError.ERR_500_MESSAGE;
                exception = new UnexpectedException(ConstError.ERRLOG_500_KEYCLOAK_BUILD, exceptionResponseMessage);
                clazz = UnexpectedException.class;
                break;
            default:
                // unknown exception
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        when(identityProviderService.tokenRefresh(
                anyString(), anyString(), anyString(), anyString()))
                .thenThrow(exception);

        // Act & Assert
        assertThrows(clazz, () ->
                controller.refresh(requestBody, bindingResult, httpServletRequest)
        );
        assertEquals(exceptionLogMessage, exception.getLogMessage());
        assertEquals(exceptionResponseMessage, exception.getResponseMessage());
    }

}