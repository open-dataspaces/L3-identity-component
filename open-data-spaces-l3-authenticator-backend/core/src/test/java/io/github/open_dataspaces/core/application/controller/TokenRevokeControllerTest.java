/*
 * TokenRevokeControllerTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * Unit tests for the TokenRevokeController class.
 *
 * Date: 2026/02/10
 */

package io.github.open_dataspaces.core.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
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

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.dto.APIResponse;
import io.github.open_dataspaces.core.domain.dto.TokenRevokeRequest;
import io.github.open_dataspaces.core.domain.dto.TokenRevokeResponse;
import io.github.open_dataspaces.core.domain.entities.TokenRevokeResult;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.exception.AbstractBaseException;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnexpectedException;
import io.github.open_dataspaces.core.exception.ValidateException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Unit tests for the TokenRevokeController class.
 */
@ExtendWith(MockitoExtension.class)
public class TokenRevokeControllerTest {

    @Mock
    private IdentityProviderService identityProviderService;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private TokenRevokeController controller;

    /**
     * Sets up the test environment before each test case.
     */
    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/test-context");
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    /**
     * Tests successful token revocation.
     *
     * @param clientId the client ID to be tested
     * @param clientSecret the client secret to be tested
     * @param refreshToken the refresh token to be tested
     */
    @ParameterizedTest
    @CsvSource({
        // clientId, clientSecret, refreshToken
        " 10,  32,  650",     // case #1: normal
        "  1,  32,  650",     // case #2: minimum clientId length
        "239,  32,  650",     // case #3: maximum clientId length
        " 10,   1,  650",     // case #4: minimum clientSecret length
        " 10, 255,  650"      // case #5: maximum clientSecret length
    })
    void testRevokeToken_success(String clientId, String clientSecret, String refreshToken) {
        String apiKey = "API-Key";
        String paramClientId = "a".repeat(Integer.parseInt(clientId.trim()));
        String paramClientSecret = "a".repeat(Integer.parseInt(clientSecret.trim()));
        String paramRefreshToken = "a".repeat(Integer.parseInt(refreshToken.trim()));

        // Implement test logic for successful token revocation
        TokenRevokeRequest requestBody = new TokenRevokeRequest();
        requestBody.setClientId(paramClientId);
        requestBody.setClientSecret(paramClientSecret);
        requestBody.setRefreshToken(paramRefreshToken);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        TokenRevokeResult serviceResult = new TokenRevokeResult();
        when(identityProviderService.revoke(
            anyString(),
            anyString(),
            anyString(),
            anyString()
        )).thenReturn(serviceResult);

        ResponseEntity<APIResponse<TokenRevokeResponse>> responseEntity = controller.revoke(requestBody, bindingResult, httpServletRequest);

        // Assert the response
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        APIResponse<TokenRevokeResponse> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());
        assertEquals(false, apiResponse.getData().isActive());
    }

    /**
     * Tests token revocation with input validation errors.
     *
     * @param clientId the client ID to be tested
     * @param clientSecret the client secret to be tested
     * @param refreshToken the refresh token to be tested
     */
    @ParameterizedTest
    @CsvSource({
        // clientId, clientSecret, refreshToken
        "  0,  32,  650",   // case # 6: clientId too short
        "240,  32,  650",   // case # 7: clientId too long
        " 10,   0,  650",   // case # 8: clientSecret too short
        " 10, 256,  650",   // case # 9: clientSecret too long
        " 10,  32,    0"    // case #10: refreshToken too short
    })
    void testTokenRevoke_inputError(String clientId, String clientSecret, String refreshToken) {
        String paramClientId = "a".repeat(Integer.parseInt(clientId.trim()));
        String paramClientSecret = "a".repeat(Integer.parseInt(clientSecret.trim()));
        String paramRefreshToken = "a".repeat(Integer.parseInt(refreshToken.trim()));

        TokenRevokeRequest requestBody = new TokenRevokeRequest();
        requestBody.setClientId(paramClientId);
        requestBody.setClientSecret(paramClientSecret);
        requestBody.setRefreshToken(paramRefreshToken);

        when(bindingResult.hasErrors()).thenReturn(true);

        ValidateException exception = assertThrows(ValidateException.class, () -> controller.revoke(requestBody, bindingResult, httpServletRequest));
        assertEquals(ConstError.ERRLOG_400_INVALID_REQUEST, exception.getLogMessage());
        assertNotNull(exception.getMessage());
    }

    /**
     * Tests token revocation when the service throws various exceptions.
     *
     * @param exceptionClassName the name of the exception class to be thrown
     * @throws Exception if an error occurs during the test
     */
    @ParameterizedTest
    @CsvSource({
        "BadParametersException",   // case #11: service throws BadParametersException
        "UnexpectedException",      // case #12: service throws UnexpectedException
        "OutOfServiceException"     // case #13: service throws OutOfServiceException
    })
    void testTokenLogout_serviceThrowsError(String exceptionClassName) throws Exception {
        String paramClientId = "a";
        String paramClientSecret = "a";
        String paramRefreshToken = "a";

        TokenRevokeRequest requestBody = new TokenRevokeRequest();
        requestBody.setClientId(paramClientId);
        requestBody.setClientSecret(paramClientSecret);
        requestBody.setRefreshToken(paramRefreshToken);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn("API-Key");

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

        // Arrange
        when(identityProviderService.revoke(anyString(), anyString(), anyString(), anyString())).thenThrow(exception);

        // Act & Assert
        assertThrows(clazz, () -> controller.revoke(requestBody, bindingResult, httpServletRequest));
        assertEquals(exceptionLogMessage, exception.getLogMessage());
        assertEquals(exceptionResponseMessage, exception.getResponseMessage());
    }
}
