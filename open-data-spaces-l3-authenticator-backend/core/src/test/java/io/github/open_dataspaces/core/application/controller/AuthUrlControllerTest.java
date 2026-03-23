/*
 * AuthUrlControllerTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This package contains unit tests for the AuthUrlController class,
 * which handles HTTP requests related to authorization URL generation in the user authentication backend system.
 *
 * Date: 2025/08/31
 */

package io.github.open_dataspaces.core.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import io.github.open_dataspaces.core.domain.dto.APIResponse;
import io.github.open_dataspaces.core.domain.dto.AuthUrlRequest;
import io.github.open_dataspaces.core.domain.dto.AuthUrlResponse;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.exception.ValidateException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * AuthUrlControllerTest is a test class for the AuthUrlController.
 */
@ExtendWith(MockitoExtension.class)
public class AuthUrlControllerTest {

    @Mock
    private IdentityProviderService identityProviderService;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthUrlController authUrlController;

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
     * case#1:
     * testAuthUrl_success tests the successful generation of the authorization URL.
     */
    @Test
    void testAuthUrl_success() {
        // Arrange
        AuthUrlRequest requestBody = new AuthUrlRequest();
        requestBody.setClientId("client123");
        requestBody.setRedirectUri("http://redirect");
        requestBody.setCodeChallenge("challenge");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn("api-key-123");
        when(identityProviderService.buildAuthorizationUrl(
                eq("api-key-123"),
                eq("client123"),
                eq("http://redirect"),
                eq("challenge")
        )).thenReturn("http://auth-url");

        // Act
        ResponseEntity<APIResponse<AuthUrlResponse>> responseEntity = authUrlController.url(requestBody, bindingResult, httpServletRequest);

        // Assert
        assertEquals(200, responseEntity.getStatusCode().value());
        APIResponse<AuthUrlResponse> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertNotNull(apiResponse.getData());
        assertEquals("http://auth-url", apiResponse.getData().getUrl());
        verify(identityProviderService, times(1)).buildAuthorizationUrl(anyString(), anyString(), anyString(), anyString());
    }

    /**
     * testAuthUrl_variousRequest tests the generation of the authorization URL with various request parameters.
     */
    @ParameterizedTest
    @CsvSource({
        // apiKey, clientId, redirectUri, codeChallenge, expectedUrl
        "null, client123, http://redirect, challenge, http://auth-url-without-api-key", // case#2: apiKey is null
        "'', client123, http://redirect, challenge, http://auth-url-empty-api-key", // case#3: apiKey is empty
        "a, a, http://a, a, http://minimal-auth-url", // case#4: min length 1 characters
        "very-long-api-key-with-special-characters-12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345, very-long-client-id-with-special-characters-1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345, https://example.com/redirect/path/with/multiple/segments/and/very/long/subpath/123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345, very-long-code-challenge-string-with-base64-url-safe-characters-1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345, http://maximal-auth-url-with-all-parameters" // case#5: max length 255 characters
    })
    void testAuthUrl_variousRequest(
            String apiKey,
            String clientId,
            String redirectUri,
            String codeChallenge,
            String expectedUrl
    ) {
        // Arrange
        AuthUrlRequest requestBody = new AuthUrlRequest();
        requestBody.setClientId(clientId);
        requestBody.setRedirectUri(redirectUri);
        requestBody.setCodeChallenge(codeChallenge);

        when(bindingResult.hasErrors()).thenReturn(false);
        if ("null".equals(apiKey)) {
            when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn(null);
            when(identityProviderService.buildAuthorizationUrl(
                    eq(null),
                    eq(clientId),
                    eq(redirectUri),
                    eq(codeChallenge)
            )).thenReturn(expectedUrl);
        } else {
            when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
            when(identityProviderService.buildAuthorizationUrl(
                    eq(apiKey),
                    eq(clientId),
                    eq(redirectUri),
                    eq(codeChallenge)
                    )).thenReturn(expectedUrl);
        }

        // Act
        ResponseEntity<APIResponse<AuthUrlResponse>> responseEntity = authUrlController.url(requestBody, bindingResult, httpServletRequest);

        // Assert
        assertEquals(200, responseEntity.getStatusCode().value());
        APIResponse<AuthUrlResponse> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertNotNull(apiResponse.getData());
        assertEquals(expectedUrl, apiResponse.getData().getUrl());
        if ("null".equals(apiKey)) {
            verify(identityProviderService, times(1)).buildAuthorizationUrl(eq(null), anyString(), anyString(), anyString());
        } else {
            verify(identityProviderService, times(1)).buildAuthorizationUrl(eq(apiKey), anyString(), anyString(), anyString());
        }
    }

    /**
     * testAuthUrl_whenServiceReturnsNullOrEmptyUrl tests the behavior when the service returns null or empty URL.
     */
    @ParameterizedTest
    @CsvSource({
        "null", // case#6: url is null
        "''"    // case#7: url is empty
    })
    void testAuthUrl_whenServiceReturnsNullOrEmptyUrl(String returnedUrl) {
        // Arrange
        AuthUrlRequest requestBody = new AuthUrlRequest();
        requestBody.setClientId("client123");
        requestBody.setRedirectUri("http://redirect");
        requestBody.setCodeChallenge("challenge");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn("api-key-123");
        String urlToReturn = "null".equals(returnedUrl) ? null : returnedUrl;
        when(identityProviderService.buildAuthorizationUrl(
                anyString(), anyString(), anyString(), anyString()
        )).thenReturn(urlToReturn);

        // Act
        ResponseEntity<APIResponse<AuthUrlResponse>> responseEntity = authUrlController.url(requestBody, bindingResult, httpServletRequest);

        // Assert
        assertEquals(200, responseEntity.getStatusCode().value());
        APIResponse<AuthUrlResponse> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertNotNull(apiResponse.getData());
        assertEquals(urlToReturn, apiResponse.getData().getUrl());
        verify(identityProviderService, times(1)).buildAuthorizationUrl(anyString(), anyString(), anyString(), anyString());
    }

    /**
     * case#8:
     * testAuthUrl_whenBindingResultHasErrors tests the behavior when BindingResult has errors.
     */
    @Test
    void testAuthUrl_whenBindingResultHasErrors() {
        // Arrange
        AuthUrlRequest requestBody = new AuthUrlRequest();
        when(bindingResult.hasErrors()).thenReturn(true);

        // Act & Assert
        ValidateException exception = assertThrows(ValidateException.class, () ->
                authUrlController.url(requestBody, bindingResult, httpServletRequest)
        );
        assertEquals(ConstError.ERRLOG_400_INVALID_REQUEST, exception.getLogMessage());
        assertNotNull(exception.getMessage());
    }

    /**
     * case#9:
     * testAuthUrl_whenServiceThrowsRuntimeException tests the behavior when the service throws a RuntimeException.
     */
    @Test
    void testAuthUrl_whenServiceThrowsRuntimeException() {
        // Arrange
        AuthUrlRequest requestBody = new AuthUrlRequest();
        requestBody.setClientId("client123");
        requestBody.setRedirectUri("http://redirect");
        requestBody.setCodeChallenge("challenge");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn("api-key-123");
        when(identityProviderService.buildAuthorizationUrl(
                anyString(), anyString(), anyString(), anyString()
        )).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authUrlController.url(requestBody, bindingResult, httpServletRequest)
        );
        assertEquals("Service error", exception.getMessage());
        verify(identityProviderService, times(1)).buildAuthorizationUrl(anyString(), anyString(), anyString(), anyString());
    }
}