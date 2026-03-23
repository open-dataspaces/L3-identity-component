/*
 * AuthzConfirmStoreIdValidatorTest.java
 *
 * Copyright (c) 2026 NTT DATA Group Corporation All rights reserved.
 *
 * This class tests the AuthzConfirmStoreIdValidator functionality.
 *
 * Date: 2026/02/17
 */

package io.github.open_dataspaces.core.application.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerMapping;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.exception.BadParametersException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unit tests for the {@link AuthzConfirmStoreIdValidator} class.
 */
@ExtendWith(MockitoExtension.class)
public class AuthzConfirmStoreIdValidatorTest {

    @InjectMocks
    AuthzConfirmStoreIdValidator validator;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private final String commonStoreId = "store-123";
    private final Map<String, String> commonUriVars = Map.of(Const.API_PATH_PARAM_STORE_ID, commonStoreId);
    private final Object commonObject = new Object();

    /**
     * preHandle - returns true when DELETE method and storeId matches X-Confirm-Store-Id header.
     */
    @Test
    @DisplayName("preHandle - success")
    void testPreHandle_validHeader() throws Exception {
        Mockito.when(request.getMethod()).thenReturn(HttpMethod.DELETE.name());
        Mockito.when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(commonUriVars);
        Mockito.when(request.getHeader(Const.HEADER_X_CONFIRM_STORE_ID)).thenReturn(commonStoreId);
        boolean result = validator.preHandle(request, response, commonObject);
        assertTrue(result);
    }

    /**
     * preHandle - throws BadParametersException when DELETE method and X-Confirm-Store-Id header is missing or unmatched.
     */
    @ParameterizedTest
    @CsvSource({
        "null",
        "mismatched-store-id"
    })
    @DisplayName("preHandle - x-confirm-store-id header missing or unmatched")
    void testPreHandle_missingHeader(String confirmStoreId) throws Exception {
        Mockito.when(request.getMethod()).thenReturn(HttpMethod.DELETE.name());
        Mockito.when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(commonUriVars);

        String actualConfirmStoreId = "null".equals(confirmStoreId) ? null : confirmStoreId;
        Mockito.when(request.getHeader(Const.HEADER_X_CONFIRM_STORE_ID)).thenReturn(actualConfirmStoreId);

        BadParametersException exception = assertThrows(BadParametersException.class, () -> {
            validator.preHandle(request, response, commonObject);
        });
        assertEquals(String.format(ConstError.ERR_400_INVALID_VALUE_OR_MISSING, Const.HEADER_X_CONFIRM_STORE_ID), exception.getMessage());
    }

    /**
     * preHandle - returns true for non-DELETE methods without header validation.
     */
    @Test
    @DisplayName("preHandle - non-DELETE method")
    void testPreHandle_nonDeleteMethod() throws Exception {
        Mockito.when(request.getMethod()).thenReturn(HttpMethod.GET.name());
        boolean result = validator.preHandle(request, response, commonObject);
        assertTrue(result);
    }
}
