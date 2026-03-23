/*
 * TokenIntrospectionControllerTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * Unit tests for the TokenIntrospectionController class.
 *
 * Date: 2025-06-30
 */

package io.github.open_dataspaces.core.application.controller;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.dto.APIResponse;
import io.github.open_dataspaces.core.domain.dto.TokenIntrospectionRequest;
import io.github.open_dataspaces.core.domain.dto.TokenIntrospectionResponse;
import io.github.open_dataspaces.core.domain.entities.TokenIntrospectionResult;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.exception.AbstractBaseException;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnauthorizedException;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TokenIntrospectionControllerTest is a test class for the TokenIntrospectionController.
 *
 * <p>This class tests the functionality of the token introspection endpoint, ensuring that it correctly processes
 * requests and handles various scenarios, including successful introspection and error cases.</p>
 *
 * @author YourName
 */
@ExtendWith(MockitoExtension.class)
class TokenIntrospectionControllerTest {

    @Mock
    private IdentityProviderService identityProviderService;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private TokenIntrospectionController controller;

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
    void testTokenIntrospection_success() {
        // Arrange
        TokenIntrospectionRequest requestBody = new TokenIntrospectionRequest();
        requestBody.setAccessToken("access-token-123");
        requestBody.setClientId("client-id-123");
        requestBody.setClientSecret("client-secret-123");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn("api-key-123");

        TokenIntrospectionResult introspectResult = mock(TokenIntrospectionResult.class);
        when(introspectResult.isActive()).thenReturn(true);
        when(introspectResult.getExp()).thenReturn(123L);
        when(introspectResult.getIat()).thenReturn(456L);
        when(introspectResult.getOperatorId()).thenReturn("operator-id-123");
        when(introspectResult.getOpenSystemId()).thenReturn("open-system-id-123");
        when(introspectResult.getScope()).thenReturn("openid");
        when(introspectResult.getClientId()).thenReturn("client-id-123");
        when(introspectResult.getTyp()).thenReturn("Bearer");

        when(identityProviderService.tokenIntrospection(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(introspectResult);

        // Act
        ResponseEntity<APIResponse<TokenIntrospectionResponse>> responseEntity =
                controller.tokenIntrospection(requestBody, bindingResult, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        APIResponse<TokenIntrospectionResponse> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());
        assertEquals(true, apiResponse.getData().isActive());
        assertEquals(123L, apiResponse.getData().getTokenInfo().getExp());
        assertEquals(456L, apiResponse.getData().getTokenInfo().getIat());
        assertEquals("operator-id-123", apiResponse.getData().getTokenInfo().getOperatorId());
        assertEquals("open-system-id-123", apiResponse.getData().getTokenInfo().getOpenSystemId());
        assertEquals("openid", apiResponse.getData().getTokenInfo().getScope());
        assertEquals("client-id-123", apiResponse.getData().getTokenInfo().getClientId());
        assertEquals("Bearer", apiResponse.getData().getTokenInfo().getTokenType());
    }

    /**
     * testAuthUrl_variousRequest_success tests the generation of the authorization URL with various request parameters.
     */
    @ParameterizedTest
    @CsvSource({
        // apiKey, clientId, clientSecret, accessToken
        "success, a, a, a, a", // case#2: min length 1 characters
        "success, LONG, LONG, LONG, LONG" // case#3: max length 255 characters
    })
    void testTokenIntrospection_variousRequest_success(
            String result,
            String apiKey,
            String clientId,
            String clientSecret,
            String accessToken
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
        if ("LONG".equals(accessToken)) {
            accessToken = "a".repeat(255);
        }

        // Arrange
        TokenIntrospectionRequest requestBody = new TokenIntrospectionRequest();
        requestBody.setClientId(clientId);
        requestBody.setClientSecret(clientSecret);
        requestBody.setAccessToken(accessToken);

        TokenIntrospectionResult tokenIntrospectionResult = mock(TokenIntrospectionResult.class);
        when(tokenIntrospectionResult.isActive()).thenReturn(true);
        when(tokenIntrospectionResult.getExp()).thenReturn(123L);
        when(tokenIntrospectionResult.getIat()).thenReturn(456L);
        when(tokenIntrospectionResult.getOperatorId()).thenReturn("operator-id-123");
        when(tokenIntrospectionResult.getOpenSystemId()).thenReturn("open-system-id-123");
        when(tokenIntrospectionResult.getScope()).thenReturn("openid");
        when(tokenIntrospectionResult.getClientId()).thenReturn(clientId);
        when(tokenIntrospectionResult.getTyp()).thenReturn("Bearer");

        when(bindingResult.hasErrors()).thenReturn(false);

        // success
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        when(identityProviderService.tokenIntrospection(
                eq(apiKey),
                eq(accessToken),
                eq(clientId),
                eq(clientSecret)
                )).thenReturn(tokenIntrospectionResult);

        // Act
        ResponseEntity<APIResponse<TokenIntrospectionResponse>> responseEntity = controller.tokenIntrospection(requestBody, bindingResult, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        APIResponse<TokenIntrospectionResponse> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());
        assertEquals(true, apiResponse.getData().isActive());
        assertEquals(123L, apiResponse.getData().getTokenInfo().getExp());
        assertEquals(456L, apiResponse.getData().getTokenInfo().getIat());
        assertEquals("operator-id-123", apiResponse.getData().getTokenInfo().getOperatorId());
        assertEquals("open-system-id-123", apiResponse.getData().getTokenInfo().getOpenSystemId());
        assertEquals("openid", apiResponse.getData().getTokenInfo().getScope());
        assertEquals(clientId, apiResponse.getData().getTokenInfo().getClientId());
        assertEquals("Bearer", apiResponse.getData().getTokenInfo().getTokenType());
    }

    /**
     * testAuthUrl_variousRequest_tokenActive tests the generation of the authorization URL with various request parameters.
     */
    @ParameterizedTest
    @CsvSource({
        // token is active
        "true", // case#4: Token is active
        "false" // case#5: Token is inactive
    })
    void testTokenIntrospection_variousRequest_tokenActive(
            String isActive
    ) {
        // If isActive is true, set appropriate values for each field
        boolean isActiveFlag = "true".equals(isActive);

        String apiKey = "api-key-123";

        // Arrange
        TokenIntrospectionRequest requestBody = new TokenIntrospectionRequest();
        requestBody.setClientId("client-id-123");
        requestBody.setClientSecret("client-secret-123");
        requestBody.setAccessToken("access-token-123");

        TokenIntrospectionResult tokenIntrospectionResult = mock(TokenIntrospectionResult.class);
        if (isActiveFlag) {
            when(tokenIntrospectionResult.isActive()).thenReturn(true);
            when(tokenIntrospectionResult.getExp()).thenReturn(123L);
            when(tokenIntrospectionResult.getIat()).thenReturn(456L);
            when(tokenIntrospectionResult.getOperatorId()).thenReturn("operator-id-123");
            when(tokenIntrospectionResult.getOpenSystemId()).thenReturn("open-system-id-123");
            when(tokenIntrospectionResult.getScope()).thenReturn("openid");
            when(tokenIntrospectionResult.getClientId()).thenReturn(requestBody.getClientId());
            when(tokenIntrospectionResult.getTyp()).thenReturn("Bearer");
        } else {
            when(tokenIntrospectionResult.isActive()).thenReturn(false);
        }

        when(bindingResult.hasErrors()).thenReturn(false);

        // success
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        when(identityProviderService.tokenIntrospection(
                eq(apiKey),
                eq(requestBody.getAccessToken()),
                eq(requestBody.getClientId()),
                eq(requestBody.getClientSecret())
                )).thenReturn(tokenIntrospectionResult);

        // Act
        ResponseEntity<APIResponse<TokenIntrospectionResponse>> responseEntity = controller.tokenIntrospection(requestBody, bindingResult, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        APIResponse<TokenIntrospectionResponse> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());

        if (isActiveFlag) {
            assertNotNull(apiResponse.getData());
            assertEquals(true, apiResponse.getData().isActive());
            assertEquals(123L, apiResponse.getData().getTokenInfo().getExp());
            assertEquals(456L, apiResponse.getData().getTokenInfo().getIat());
            assertEquals("operator-id-123", apiResponse.getData().getTokenInfo().getOperatorId());
            assertEquals("open-system-id-123", apiResponse.getData().getTokenInfo().getOpenSystemId());
            assertEquals("openid", apiResponse.getData().getTokenInfo().getScope());
            assertEquals("client-id-123", apiResponse.getData().getTokenInfo().getClientId());
            assertEquals("Bearer", apiResponse.getData().getTokenInfo().getTokenType());
        } else {
            assertNotNull(apiResponse.getData());
            assertEquals(false, apiResponse.getData().isActive());
            assertNull(apiResponse.getData().getTokenInfo());
        }
    }

    /**
     * case #6: BindingResult has errors.
     */
    @Test
    void testTokenIntrospection_bindingResultHasErrors_throwsValidateException() {
        // Arrange
        TokenIntrospectionRequest requestBody = new TokenIntrospectionRequest();
        when(bindingResult.hasErrors()).thenReturn(true);

        // Act & Assert
        ValidateException exception = assertThrows(ValidateException.class, () ->
                controller.tokenIntrospection(requestBody, bindingResult, httpServletRequest)
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
        "BadParametersException", // case#7: Invalid parameters
        "UnauthorizedException", // case#8: Authentication error
        "OutOfServiceException", // case#9: Service unavailable
        "UnexpectedException" // case#10: Unexpected error
    })
    void testTokenIntrospection_identityProviderService_throwsException(
            String exceptionClassName
    ) {
        // Arrange
        TokenIntrospectionRequest requestBody = new TokenIntrospectionRequest();
        requestBody.setClientId("client-id-123");
        requestBody.setClientSecret("client-secret-123");
        requestBody.setAccessToken("access-token-123");

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
            default:
                // This should not happen
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        when(identityProviderService.tokenIntrospection(
                anyString(), anyString(), anyString(), anyString()))
                .thenThrow(exception);

        // Act & Assert
        assertThrows(clazz, () ->
                controller.tokenIntrospection(requestBody, bindingResult, httpServletRequest)
        );
        assertEquals(exceptionLogMessage, exception.getLogMessage());
        assertEquals(exceptionResponseMessage, exception.getResponseMessage());
    }

}