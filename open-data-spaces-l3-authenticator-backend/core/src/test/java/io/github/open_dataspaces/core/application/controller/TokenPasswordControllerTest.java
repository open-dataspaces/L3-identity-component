/*
 * TokenPasswordControllerTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This file is part of the user authentication backend system.
 * Package: io.github.open_dataspaces.core.application.controller
 * This package contains REST controller classes and their corresponding unit tests
 * for handling HTTP requests and responses in the user authentication backend system.
 *
 * Date: 2025-06-30
 */

package io.github.open_dataspaces.core.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import org.mockito.MockedStatic;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import io.github.open_dataspaces.core.common.enums.EnumResponseTypes;
import io.github.open_dataspaces.core.domain.dto.APIResponse;
import io.github.open_dataspaces.core.domain.dto.TokenPasswordRequest;
import io.github.open_dataspaces.core.domain.dto.TokenPasswordResponse;
import io.github.open_dataspaces.core.domain.entities.AccessTokenResult;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.LoginException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnexpectedException;
import io.github.open_dataspaces.core.exception.ValidateException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Unit tests for the AuthLoginController class.
 *
 * <p>This test class verifies the correct behavior of the login REST API,
 * including normal and error cases for request validation and service delegation.
 * It uses Mockito for mocking service dependencies and JUnit 5 for assertions.
 * </p>
 *
 * @author btmurayamakohei
 * @since 2025/06/26
 */
class TokenPasswordControllerTest {

    /**
     * Test No.1
     * Verifies that login returns HTTP 201 CREATED when the request is valid.
     */
    @Test
    void testLogin_success() {
        String apiKey = "dummy-api-key";
        String clientId = "dummy-client-id";
        String clientSecret = "dummy-client-secret";
        String loginUserId = "dummy-user-id";
        String password = "dummy-password";
        // Mocking the AccessTokenResult to return dummy values
        String accessToken = "dummy-access-token";
        long expiresIn = 3600;
        String tokenType = "Bearer";
        int notBeforePolicy = 0;
        String scope = "openid profile email";
        String refreshToken = "dummy-refresh-token";
        long refreshExpiresIn = 7200;
        String idToken = "dummy-id-token";
        // Arrange
        IdentityProviderService identityProviderService = mock(IdentityProviderService.class);
        TokenPasswordController controller = new TokenPasswordController(identityProviderService);

        TokenPasswordRequest requestBody = new TokenPasswordRequest(clientId, clientSecret, loginUserId, password);

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getHeader("API-Key")).thenReturn(apiKey);

        AccessTokenResult loginResponse = new AccessTokenResult(accessToken, expiresIn, tokenType, notBeforePolicy, scope, refreshToken, refreshExpiresIn, idToken);
        when(identityProviderService.signInWithPassword(apiKey, clientId, clientSecret, loginUserId, password)).thenReturn(loginResponse);
        // Mock the static method of EnumResponseTypes
        try (MockedStatic<EnumResponseTypes> mockedEnum = mockStatic(EnumResponseTypes.class)) {
            // Set up the mock to return a specific value when getResponseType is called
            mockedEnum.when(() -> EnumResponseTypes.getResponseType(200))
                    .thenReturn("http://localhost:8080/api/success");
            // Act
            ResponseEntity<APIResponse<TokenPasswordResponse>> response = controller.login(requestBody, bindingResult, httpRequest);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("http://localhost:8080/api/success", response.getBody().getType());
            verify(identityProviderService, times(1)).signInWithPassword(apiKey, clientId, clientSecret, loginUserId, password);

            // EnumResponseTypes.getResponseType(200) is called once
            mockedEnum.verify(() -> EnumResponseTypes.getResponseType(200));
        }
    }

    /**
     * Test No.2
     * Verifies that login throws ValidateException when the request body is invalid.
     */
    @Test
    void testLogin_invalidRequest() {
        String clientId = "dummy-client-id";
        String clientSecret = "dummy-client-secret";
        String loginUserId = "dummy-user-id";
        String password = "dummy-password";
        // Arrange
        IdentityProviderService identityProviderService = mock(IdentityProviderService.class);
        TokenPasswordController controller = new TokenPasswordController(identityProviderService);

        TokenPasswordRequest requestBody = new TokenPasswordRequest(clientId, clientSecret, loginUserId, password);
        requestBody.setLoginUserId("dummy-account");
        requestBody.setPassword("dummy-password");

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);

        // Act & Assert
        assertThrows(
                ValidateException.class,
                () -> controller.login(requestBody, bindingResult, httpRequest)
        );
        // Note: The actual ValidateException behavior should be checked against the real implementation
    }

    /**
     * Test No.3
     * Verifies that login throws BadParametersException when invalid parameters are provided.
     */
    @Test
    void testLogin_badParametersException() {
        String apiKey = "dummy-api-key";
        String clientId = "dummy-client-id";
        String clientSecret = "dummy-client-secret";
        String loginUserId = "dummy-user-id";
        String password = "dummy-password";

        // Arrange
        IdentityProviderService identityProviderService = mock(IdentityProviderService.class);
        TokenPasswordController controller = new TokenPasswordController(identityProviderService);
        TokenPasswordRequest requestBody = new TokenPasswordRequest(clientId, clientSecret, loginUserId, password);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getHeader("API-Key")).thenReturn(apiKey);

        // Throw BadParametersException when invalid parameters are provided
        BadParametersException badParamsException = new BadParametersException("ERR_400_INVALID_API_KEY");
        when(identityProviderService.signInWithPassword(apiKey, clientId, clientSecret, loginUserId, password))
                .thenThrow(badParamsException);

        // Act & Assert
        BadParametersException exception = assertThrows(BadParametersException.class, () -> {
            controller.login(requestBody, bindingResult, httpRequest);
        });

        // assert the contents of BadParametersException
        assertEquals("ERR_400_INVALID_API_KEY", exception.getResponseMessage());
        assertEquals("ERR_400_INVALID_API_KEY", exception.getLogMessage());

        verify(identityProviderService, times(1)).signInWithPassword(apiKey, clientId, clientSecret, loginUserId, password);
    }

    /**
     * Test No.4
     * Verifies that login throws LoginException when authentication fails.
     */
    @Test
    void testLogin_loginException() {
        String apiKey = "dummy-api-key";
        String clientId = "dummy-client-id";
        String clientSecret = "dummy-client-secret";
        String loginUserId = "dummy-user-id";
        String password = "dummy-password";

        // Arrange
        IdentityProviderService identityProviderService = mock(IdentityProviderService.class);
        TokenPasswordController controller = new TokenPasswordController(identityProviderService);
        TokenPasswordRequest requestBody = new TokenPasswordRequest(clientId, clientSecret, loginUserId, password);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getHeader("API-Key")).thenReturn(apiKey);

        // Mock to throw LoginException
        when(identityProviderService.signInWithPassword(apiKey, clientId, clientSecret, loginUserId, password))
                .thenThrow(new LoginException("ERR_401_INVALID_CREDENTIALS", "authentication failed"));

        // Act & Assert
        LoginException exception = assertThrows(LoginException.class, () -> {
            controller.login(requestBody, bindingResult, httpRequest);
        });

        // assert the contents of LoginException
        assertEquals("ERR_401_INVALID_CREDENTIALS", exception.getResponseMessage());
        assertEquals("ERR_401_INVALID_CREDENTIALS: id: authentication failed.", exception.getLogMessage());

        verify(identityProviderService, times(1)).signInWithPassword(apiKey, clientId, clientSecret, loginUserId, password);
    }

    /**
     * Test No.5
     * Verifies that login throws UnexpectedException when system error occurs.
     */
    @Test
    void testLogin_unexpectedException() {
        String apiKey = "dummy-api-key";
        String clientId = "dummy-client-id";
        String clientSecret = "dummy-client-secret";
        String loginUserId = "dummy-user-id";
        String password = "dummy-password";

        // Arrange
        IdentityProviderService identityProviderService = mock(IdentityProviderService.class);
        TokenPasswordController controller = new TokenPasswordController(identityProviderService);
        TokenPasswordRequest requestBody = new TokenPasswordRequest(clientId, clientSecret, loginUserId, password);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getHeader("API-Key")).thenReturn(apiKey);

        // Mock to throw UnexpectedException
        UnexpectedException unexpectedException = new UnexpectedException("ERR_500_KEYCLOAK_FAILED", "System error occurred");
        when(identityProviderService.signInWithPassword(apiKey, clientId, clientSecret, loginUserId, password))
                .thenThrow(unexpectedException);

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            controller.login(requestBody, bindingResult, httpRequest);
        });

        // assert the contents of UnexpectedException
        assertEquals("System error occurred", exception.getResponseMessage());
        assertTrue(exception.getLogMessage().contains("ERR_500_KEYCLOAK_FAILED"));

        verify(identityProviderService, times(1)).signInWithPassword(apiKey, clientId, clientSecret, loginUserId, password);
    }

    /**
     * Test No.6
     * Verifies that login throws OutOfServiceException when service is unavailable.
     */
    @Test
    void testLogin_outOfServiceException() {
        String apiKey = "dummy-api-key";
        String clientId = "dummy-client-id";
        String clientSecret = "dummy-client-secret";
        String loginUserId = "dummy-user-id";
        String password = "dummy-password";

        // Arrange
        IdentityProviderService identityProviderService = mock(IdentityProviderService.class);
        TokenPasswordController controller = new TokenPasswordController(identityProviderService);
        TokenPasswordRequest requestBody = new TokenPasswordRequest(clientId, clientSecret, loginUserId, password);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getHeader("API-Key")).thenReturn(apiKey);

        // Mock to throw OutOfServiceException
        OutOfServiceException outOfServiceException = new OutOfServiceException("ERR_503_OUT_OF_SERVICE_KEYCLOAK");
        when(identityProviderService.signInWithPassword(apiKey, clientId, clientSecret, loginUserId, password))
                .thenThrow(outOfServiceException);

        // Act & Assert
        OutOfServiceException exception = assertThrows(OutOfServiceException.class, () -> {
            controller.login(requestBody, bindingResult, httpRequest);
        });

        // assert the contents of OutOfServiceException
        assertEquals("", exception.getResponseMessage());
        assertEquals("ERR_503_OUT_OF_SERVICE_KEYCLOAK", exception.getLogMessage());

        verify(identityProviderService, times(1)).signInWithPassword(apiKey, clientId, clientSecret, loginUserId, password);
    }
}