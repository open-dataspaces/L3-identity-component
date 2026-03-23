/*
 * AuthzRealmStoreValidatorTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the AuthzRealmStoreValidator.
 *
 * Date: 2026/02/17
 */

package io.github.open_dataspaces.core.application.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.springframework.web.servlet.HandlerMapping;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.enums.EnumStorePurpose;
import io.github.open_dataspaces.core.domain.service.interfaces.APIKeyService;
import io.github.open_dataspaces.core.domain.service.interfaces.AuthorizationService;
import io.github.open_dataspaces.core.exception.ForbiddenException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unit tests for the {@link AuthzRealmStoreValidator} class.
 */
@ExtendWith(MockitoExtension.class)
public class AuthzRealmStoreValidatorTest {

    @InjectMocks
    AuthzRealmStoreValidator validator;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @Mock
    private APIKeyService apiKeyService;
    @Mock
    private AuthorizationService authorizationService;

    private final String commonApiKey = "api-key-123";
    private final String commonAdminStoreId = "admin-store-123";
    private final String commonRealm = "test-realm";
    private final String commonPathStoreId = "store-123";
    private final boolean commonDecision = true;
    private final Object commonObject = new Object();
    private final Map<String, String> commonUriVars = Map.of(Const.API_PATH_PARAM_STORE_ID, commonPathStoreId);

    @Test
    @DisplayName("preHandle - success")
    void testPreHandle_success() throws Exception {
        // Arrange
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(commonApiKey);
        when(apiKeyService.getStoreId(commonApiKey, EnumStorePurpose.REALM_STORE_BINDING.name())).thenReturn(commonAdminStoreId);
        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(commonUriVars);
        when(apiKeyService.getIdpRealm(commonApiKey)).thenReturn(commonRealm);
        when(authorizationService.evaluate(
                commonAdminStoreId,
                Const.OPENFGA_EVALUATION_SUBJECT_TYPE_REALM,
                commonRealm,
                Const.OPENFGA_EVALUATION_RESOURCE_TYPE_STORE,
                commonPathStoreId,
                Const.AUTHZ_ACTION_BOUND_TO)).thenReturn(true);

        // Act
        boolean result = validator.preHandle(request, response, commonObject);

        // Assert
        assertEquals(result, commonDecision);
        verify(authorizationService, times(1)).evaluate(
                commonAdminStoreId,
                Const.OPENFGA_EVALUATION_SUBJECT_TYPE_REALM,
                commonRealm,
                Const.OPENFGA_EVALUATION_RESOURCE_TYPE_STORE,
                commonPathStoreId,
                Const.AUTHZ_ACTION_BOUND_TO);
    }

    @Test
    @DisplayName("preHandle - throws ForbiddenException")
    void testPreHandle_notBound_throwsForbiddenException() throws Exception {
        // Arrange
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(commonApiKey);
        when(apiKeyService.getStoreId(commonApiKey, EnumStorePurpose.REALM_STORE_BINDING.name())).thenReturn(commonAdminStoreId);
        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(commonUriVars);
        when(apiKeyService.getIdpRealm(commonApiKey)).thenReturn(commonRealm);
        when(authorizationService.evaluate(
                commonAdminStoreId,
                Const.OPENFGA_EVALUATION_SUBJECT_TYPE_REALM,
                commonRealm,
                Const.OPENFGA_EVALUATION_RESOURCE_TYPE_STORE,
                commonPathStoreId,
                Const.AUTHZ_ACTION_BOUND_TO)).thenReturn(false);

        // Act & Assert
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            validator.preHandle(request, response, commonObject);
        });

        assertTrue(exception.getLogMessage().contains(
                String.format(ConstError.ERRLOG_403_REALM_STORE_NOT_BOUND, commonRealm, commonPathStoreId)));
        verify(authorizationService, times(1)).evaluate(
                commonAdminStoreId,
                Const.OPENFGA_EVALUATION_SUBJECT_TYPE_REALM,
                commonRealm,
                Const.OPENFGA_EVALUATION_RESOURCE_TYPE_STORE,
                commonPathStoreId,
                Const.AUTHZ_ACTION_BOUND_TO);
    }
}
