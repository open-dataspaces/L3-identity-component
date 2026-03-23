/*
 * ApiAuthorizationInterceptorTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the ApiAuthorizationInterceptor.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.application.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.HandlerMapping;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.service.interfaces.APIKeyService;
import io.github.open_dataspaces.core.domain.service.interfaces.AuthorizationService;
import io.github.open_dataspaces.core.domain.service.interfaces.JWTVerifyService;
import io.github.open_dataspaces.core.exception.ForbiddenException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unit tests for the {@link ApiAuthorizationInterceptor} class.
 */
@ExtendWith(MockitoExtension.class)
public class ApiAuthorizationInterceptorTest {

    @InjectMocks
    ApiAuthorizationInterceptor interceptor;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @Mock
    private JWTVerifyService jwtService;
    @Mock
    private APIKeyService apiKeyService;
    @Mock
    private AuthorizationService authorizationService;

    // Common test data
    private final String commonApiKey = "api-key-123";
    private final String commonStoreId = "store-id-123";
    private final String commonUserId = "user-id-123";
    private final String commonResourcePath = "/api/v1/resource";
    private final String commonMethod = "GET";
    private final boolean commonDecision = true;

    @Test
    @DisplayName("preHandle - success")
    void testPreHandle_success() throws Exception {
        // Arrange
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(commonApiKey);
        when(apiKeyService.getStoreId(anyString(), anyString())).thenReturn(commonStoreId);
        when(jwtService.getOperatorOrOpenSystemId(request)).thenReturn(commonUserId);
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(commonResourcePath);
        when(request.getMethod()).thenReturn(commonMethod);
        when(authorizationService.evaluate(
                anyString(),
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(commonDecision);

        // Act
        boolean result = interceptor.preHandle(request, response, new Object());

        // Assert
        assertEquals(result, commonDecision);
        verify(authorizationService, times(1)).evaluate(
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );
    }

    @Test
    @DisplayName("preHandle - throws ForbiddenException")
    void testPreHandle_throwsForbiddenException() throws Exception {
        // Arrange
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(commonApiKey);
        when(apiKeyService.getStoreId(anyString(), anyString())).thenReturn(commonStoreId);
        when(jwtService.getOperatorOrOpenSystemId(request)).thenReturn(commonUserId);
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(commonResourcePath);
        when(request.getMethod()).thenReturn(commonMethod);
        when(authorizationService.evaluate(
                anyString(),
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(false);   // Authorization denied

        // Act & Assert
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
                    interceptor.preHandle(request, response, new Object());
                }
        );

        assertEquals(exception.getLogMessage(),
                String.format(ConstError.ERR_403_AUTHORIZATION_FAILED, commonUserId, commonResourcePath, commonMethod));
        verify(authorizationService, times(1)).evaluate(
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );
    }
}
