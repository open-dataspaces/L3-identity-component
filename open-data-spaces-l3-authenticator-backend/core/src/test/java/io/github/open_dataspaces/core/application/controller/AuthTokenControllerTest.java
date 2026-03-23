/*
 * AuthTokenControllerTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This package contains unit tests for the AuthTokenController class,
 * which handles HTTP requests related to access token generation in the user authentication backend system.
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
import io.github.open_dataspaces.core.domain.dto.AuthTokenRequest;
import io.github.open_dataspaces.core.domain.dto.AuthTokenResponse;
import io.github.open_dataspaces.core.domain.entities.AccessTokenResult;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.exception.AbstractBaseException;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnauthorizedException;
import io.github.open_dataspaces.core.exception.UnexpectedException;
import io.github.open_dataspaces.core.exception.ValidateException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * AuthTokenControllerTest is a test class for the AuthTokenController.
 */
@ExtendWith(MockitoExtension.class)
public class AuthTokenControllerTest {

    @Mock
    private IdentityProviderService identityProviderService;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthTokenController authTokenController;

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
     * testGetAccessToken_success tests the successful generation of the access token.
     */
    @Test
    void testGetAccessToken_success() {
        // Arrange
        AuthTokenRequest requestBody = new AuthTokenRequest();
        requestBody.setCode("code");
        requestBody.setClientId("clientId");
        requestBody.setClientSecret("clientSecret");
        requestBody.setRedirectUri("redirectUri");
        requestBody.setCodeVerifier("codeVerifier");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn("api-key");

        AccessTokenResult accessTokenResult = new AccessTokenResult(
                "access-token", // accessToken
                3600L, // expiresIn
                "Bearer", // tokenType
                0, // notBeforePolicy
                "openid", // scope
                "refresh-token", // refreshToken
                7200L, // refreshExpiresIn
                "id-token" // idToken
        );

        when(identityProviderService.getAccessToken(
                eq("api-key"),
                eq("code"),
                eq("clientId"),
                eq("clientSecret"),
                eq("redirectUri"),
                eq("codeVerifier"))).thenReturn(accessTokenResult);

        // Act
        ResponseEntity<APIResponse<AuthTokenResponse>> response = authTokenController.accessToken(
                requestBody, bindingResult, httpServletRequest);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        APIResponse<AuthTokenResponse> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        AuthTokenResponse data = apiResponse.getData();
        assertNotNull(data);
        assertEquals("access-token", data.getAccessToken());
        assertEquals(3600, data.getExpiresIn());
        assertEquals("Bearer", data.getTokenType());
        assertEquals(0, data.getNotBeforePolicy());
        assertEquals("openid", data.getScope());
        assertEquals("refresh-token", data.getRefreshToken());
        assertEquals(7200, data.getRefreshExpiresIn());
        assertEquals("id-token", data.getIdToken());
    }

    /**
     * testGetAccessToken_variousApiKeys_success tests the generation of the access token with various API keys.
     */
    @ParameterizedTest
    @CsvSource({
            // apiKey, expectedToken
            "null, access-token-null-key", // case#2: apiKey is null
            "'', access-token-empty-key", // case#3: apiKey is empty
            "a, access-token-min", // case#4: min length 1 characters
            "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345, access-token-max" // case#5: max length 255 characters
    })
    void testGetAccessToken_variousApiKeys_success(String apiKey, String expectedToken) {
        // Arrange
        AuthTokenRequest requestBody = new AuthTokenRequest();
        requestBody.setCode("auth-code");
        requestBody.setClientId("client123");
        requestBody.setClientSecret("client-secret");
        requestBody.setRedirectUri("http://redirect.example.com");
        requestBody.setCodeVerifier("code-verifier-123");

        when(bindingResult.hasErrors()).thenReturn(false);

        String actualApiKey = "null".equals(apiKey) ? null : apiKey;
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn(actualApiKey);

        AccessTokenResult accessTokenResult = new AccessTokenResult(
                expectedToken, // accessToken
                3600L, // expiresIn
                "Bearer", // tokenType
                0, // notBeforePolicy
                "openid profile", // scope
                "refresh-token", // refreshToken
                7200L, // refreshExpiresIn
                "id-token" // idToken
        );

        when(identityProviderService.getAccessToken(
                eq(actualApiKey),
                eq("auth-code"),
                eq("client123"),
                eq("client-secret"),
                eq("http://redirect.example.com"),
                eq("code-verifier-123"))).thenReturn(accessTokenResult);

        // Act
        ResponseEntity<APIResponse<AuthTokenResponse>> response = authTokenController.accessToken(
                requestBody, bindingResult, httpServletRequest);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        APIResponse<AuthTokenResponse> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        AuthTokenResponse data = apiResponse.getData();
        assertNotNull(data);
        assertEquals(expectedToken, data.getAccessToken());
        verify(identityProviderService, times(1)).getAccessToken(
                eq(actualApiKey), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    /**
     * testGetAccessToken_success_parameterized tests the generation of the access token with various request parameters.
     */
    @ParameterizedTest
    @CsvSource({
            // code, clientId, clientSecret, redirectUri, codeVerifier
            "a, a, a, http://a, a", // case#6: min length
            "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345, 12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345, 12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345, https://example.com/123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012, 12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345" // case#7: max length 255 characters
    })
    void testGetAccessToken_success_parameterized(
            String code, String clientId, String clientSecret, String redirectUri, String codeVerifier) {
        // Arrange
        AuthTokenRequest requestBody = new AuthTokenRequest();
        requestBody.setCode(code);
        requestBody.setClientId(clientId);
        requestBody.setClientSecret(clientSecret);
        requestBody.setRedirectUri(redirectUri);
        requestBody.setCodeVerifier(codeVerifier);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn("api-key-123");

        AccessTokenResult accessTokenResult = new AccessTokenResult(
                "access-token-boundary-test", // accessToken
                3600L, // expiresIn
                "Bearer", // tokenType
                0, // notBeforePolicy
                "openid profile", // scope
                "refresh-token-boundary", // refreshToken
                7200L, // refreshExpiresIn
                "id-token-boundary" // idToken
        );

        when(identityProviderService.getAccessToken(
                eq("api-key-123"), eq(code), eq(clientId), eq(clientSecret), eq(redirectUri), eq(codeVerifier)))
                .thenReturn(accessTokenResult);

        // Act
        ResponseEntity<APIResponse<AuthTokenResponse>> response = authTokenController.accessToken(
                requestBody, bindingResult, httpServletRequest);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        APIResponse<AuthTokenResponse> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        AuthTokenResponse data = apiResponse.getData();
        assertNotNull(data);
        assertEquals("access-token-boundary-test", data.getAccessToken());
        verify(identityProviderService, times(1)).getAccessToken(
                eq("api-key-123"), eq(code), eq(clientId), eq(clientSecret), eq(redirectUri), eq(codeVerifier));
    }

    /**
     * testGetAccessToken_whenServiceReturnsNullOrEmptyToken tests the behavior when the service returns null or empty token.
     */
    @ParameterizedTest
    @CsvSource({
        "null", // case#8: service returns null
        "''"    // case#9: service returns empty
    })
    void testGetAccessToken_whenServiceReturnsNullOrEmptyToken(String returnedToken) {
        // Arrange
        AuthTokenRequest requestBody = new AuthTokenRequest();
        requestBody.setCode("auth-code");
        requestBody.setClientId("client123");
        requestBody.setClientSecret("client-secret");
        requestBody.setRedirectUri("http://redirect.example.com");
        requestBody.setCodeVerifier("code-verifier-123");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn("api-key-123");

        String actualToken = "null".equals(returnedToken) ? null : returnedToken;
        AccessTokenResult accessTokenResult = new AccessTokenResult(
                actualToken, // accessToken
                3600L, // expiresIn
                "Bearer", // tokenType
                0, // notBeforePolicy
                "openid", // scope
                "refresh-token", // refreshToken
                7200L, // refreshExpiresIn
                "id-token" // idToken
        );

        when(identityProviderService.getAccessToken(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(accessTokenResult);

        // Act
        ResponseEntity<APIResponse<AuthTokenResponse>> response = authTokenController.accessToken(
                requestBody, bindingResult, httpServletRequest);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        APIResponse<AuthTokenResponse> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        AuthTokenResponse data = apiResponse.getData();
        assertNotNull(data);
        assertEquals(actualToken, data.getAccessToken());
        verify(identityProviderService, times(1)).getAccessToken(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    /**
     * case#10:
     * testGetAccessToken_whenBindingResultHasErrors tests the behavior when BindingResult has errors.
     */
    @Test
    void testGetAccessToken_whenBindingResultHasErrors() {
        // Arrange
        AuthTokenRequest requestBody = new AuthTokenRequest();
        when(bindingResult.hasErrors()).thenReturn(true);

        // Act & Assert
        ValidateException exception = assertThrows(ValidateException.class,
                () -> authTokenController.accessToken(requestBody, bindingResult, httpServletRequest));
        assertEquals(ConstError.ERRLOG_400_INVALID_REQUEST, exception.getLogMessage());
        assertNotNull(exception.getMessage());
    }

    /**
     * testGetAccessToken_shouldThrowException_whenServiceFails tests the accessToken endpoint with various exception scenarios.
     */
    @ParameterizedTest
    @CsvSource({
        // Exception
        "BadParametersException", // case#11: Invalid parameters
        "UnauthorizedException", // case#12: Authentication error
        "OutOfServiceException", // case#13: Service unavailable
        "UnexpectedException", // case#14: Unexpected error
        "RuntimeException" // case#15: General runtime error
    })
    void testGetAccessToken_shouldThrowException_whenServiceFails(String exceptionClassName) {
        // Arrange
        AuthTokenRequest requestBody = new AuthTokenRequest();
        requestBody.setCode("auth-code");
        requestBody.setClientId("client123");
        requestBody.setClientSecret("client-secret");
        requestBody.setRedirectUri("http://redirect.example.com");
        requestBody.setCodeVerifier("code-verifier-123");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn("api-key-123");

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
            case "UnauthorizedException":
                exceptionLogMessage = String.format(ConstError.ERRLOG_401_INVALID_CLIENT, "dummy message");
                exceptionResponseMessage = ConstError.ERR_401_INVALID_CLIENT;
                exception = new UnauthorizedException(exceptionLogMessage, exceptionResponseMessage);
                clazz = UnauthorizedException.class;
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
            case "RuntimeException":
                exception = new RuntimeException("Service error");
                clazz = RuntimeException.class;
                break;
            default:
                // Unexpected exception
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        when(identityProviderService.getAccessToken(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(exception);

        // Act & Assert
        Throwable thrownException = assertThrows(clazz,
                () -> authTokenController.accessToken(requestBody, bindingResult, httpServletRequest));

        // Detailed verification according to exception type
        if (exception instanceof AbstractBaseException) {
            AbstractBaseException baseException = (AbstractBaseException) exception;
            AbstractBaseException thrownBaseException = (AbstractBaseException) thrownException;
            assertEquals(baseException.getLogMessage(), thrownBaseException.getLogMessage());
            assertEquals(baseException.getResponseMessage(), thrownBaseException.getResponseMessage());
        } else {
            assertEquals(exception.getMessage(), thrownException.getMessage());
        }

        verify(identityProviderService, times(1)).getAccessToken(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }
}