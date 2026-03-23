/*
 * TokenValidatorTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class tests the TokenValidator functionality
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.application.interceptor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.domain.service.interfaces.JWTVerifyService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unit tests for the TokenValidator.
 */
@ExtendWith(MockitoExtension.class)
class TokenValidatorTest {

    @InjectMocks
    private TokenValidator tokenValidator;

    @Mock
    private JWTVerifyService jwtVerifyService;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    // Common test parameters
    private String commonApiKey = "test-api-key-123";

    /**
     * preHandle success case.
     */
    @ParameterizedTest
    @CsvSource({
        "TRUE",
        "FALSE"
    })
    @DisplayName("preHandle - success case")
    void preHandle_success(String argVerifyResult) throws Exception  {
        boolean expectedVerifyResult = Boolean.valueOf(argVerifyResult);

        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(commonApiKey);
        when(jwtVerifyService.verifyToken(anyString(), any(HttpServletRequest.class))).thenReturn(expectedVerifyResult);

        boolean result = tokenValidator.preHandle(request, response, null);

        // Verify the result
        if (expectedVerifyResult) {
            assertTrue(result);
        } else {
            assertFalse(result);
        }
    }

    /**
     * Tests if getting API key header throws an exception.
     */
    @Test
    @DisplayName("preHandle - throw exception when getting API key header")
    void preHandle_throwExceptionGetApiKeyHeader() throws Exception {
        // Setup mock to throw exception when getting header
        String exceptionMessage = "Header error";

        // Arrange
        when(request.getHeader(Const.HEADER_API_KEY)).thenThrow(new RuntimeException(exceptionMessage));

        // Act & Assert
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            tokenValidator.preHandle(request, response, null);
        });

        // Verify the result
        assertTrue(exception.getMessage().contains(exceptionMessage));
    }

    @Test
    @DisplayName("preHandle - throw exception when verifying token")
    void preHandle_throwExceptionVerifyToken() throws Exception {
        String exceptionMessage = "Invalid token";

        // Arrange
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(commonApiKey);
        when(jwtVerifyService.verifyToken(anyString(), any(HttpServletRequest.class)))
                .thenThrow(new RuntimeException(exceptionMessage));

        // Act & Assert
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            tokenValidator.preHandle(request, response, null);
        });

        // Verify the result
        assertTrue(exception.getMessage().contains(exceptionMessage));
    }
}
