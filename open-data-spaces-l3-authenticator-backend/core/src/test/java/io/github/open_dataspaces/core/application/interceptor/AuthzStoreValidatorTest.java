/*
 * AuthzStoreValidatorTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the AuthzStoreValidator.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.application.interceptor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.HandlerMapping;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.service.interfaces.APIKeyService;
import io.github.open_dataspaces.core.domain.service.interfaces.AuthorizationService;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.ForbiddenException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unit tests for the {@link AuthzStoreValidator} class.
 */
@ExtendWith(MockitoExtension.class)
public class AuthzStoreValidatorTest {

    @InjectMocks
    AuthzStoreValidator validator;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @Mock
    private APIKeyService apiKeyService;
    @Mock
    private AuthorizationService authorizationService;

    // Common test data
    private final String commonApiKey = "api-key-123";
    private final String commonRealmStoreId = "store-id-realm-123";
    private final Map<String, String> commonUriVars = Map.of(Const.API_PATH_PARAM_STORE_ID, "store-id-usecase-456");

    @Test
    @DisplayName("preHandle - success")
    void testPreHandle_success() throws Exception {
        ResponseEntity<Object> storeResponse = ResponseEntity.ok().build();

        // Arrange
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(commonApiKey);
        when(apiKeyService.getStoreId(anyString(), anyString())).thenReturn(commonRealmStoreId);

        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(commonUriVars);
        when(authorizationService.findStoreById(anyString())).thenReturn(storeResponse);

        // Act
        boolean result = validator.preHandle(request, response, new Object());

        // Assert
        assertTrue(result, "Valid store ID should return true");

        verify(authorizationService, times(1)).findStoreById(anyString());
    }

    @Test
    @DisplayName("preHandle - store not found - throws BadParametersException")
    void testPreHandle_storeNotFound_throwsBadParametersException() throws Exception {
        ResponseEntity<Object> storeResponse = ResponseEntity.notFound().build(); // Mock store not found

        // Arrange
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(commonApiKey);
        when(apiKeyService.getStoreId(anyString(), anyString())).thenReturn(commonRealmStoreId);

        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(commonUriVars);
        when(authorizationService.findStoreById(anyString())).thenReturn(storeResponse);

        // Act & Assert
        BadParametersException exception = assertThrows(BadParametersException.class, () -> {
            validator.preHandle(request, response, new Object());
        });

        // Assert
        assertTrue(exception.getLogMessage().contains(
                String.format(ConstError.ERRLOG_400_INVALID_REQUEST_PARAMETER, Const.API_PATH_PARAM_STORE_ID, commonUriVars.get(Const.API_PATH_PARAM_STORE_ID))));

        verify(authorizationService, times(1)).findStoreById(anyString());
    }

    @Test
    @DisplayName("preHandle - matching realm store ID - throws ForbiddenException")
    void testPreHandle_matchingRealmStoreId_throwsForbiddenException() throws Exception {
        ResponseEntity<Object> storeResponse = ResponseEntity.ok().build();

        // Arrange
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(commonApiKey);
        when(apiKeyService.getStoreId(anyString(), anyString())).thenReturn(commonRealmStoreId);

        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
                .thenReturn(Map.of(Const.API_PATH_PARAM_STORE_ID, commonRealmStoreId));     // Matching store ID
        when(authorizationService.findStoreById(anyString())).thenReturn(storeResponse);

        // Act & Assert
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            validator.preHandle(request, response, new Object());
        });

        // Assert
        assertTrue(exception.getLogMessage().contains(
                String.format(ConstError.ERRLOG_403_INVALID_AUTHORIZATION, commonRealmStoreId)));
        verify(authorizationService, times(1)).findStoreById(anyString());
    }

}