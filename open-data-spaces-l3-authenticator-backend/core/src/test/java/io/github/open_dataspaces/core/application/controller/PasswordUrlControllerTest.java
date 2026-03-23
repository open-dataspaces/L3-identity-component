/*
 * PasswordUrlControllerTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This package contains unit tests for the PasswordUrlController class.
 *
 * Date: 2025/08/31
 */

package io.github.open_dataspaces.core.application.controller;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.dto.APIResponse;
import io.github.open_dataspaces.core.domain.dto.PasswordUrlRequest;
import io.github.open_dataspaces.core.domain.dto.PasswordUrlResponse;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.exception.AbstractBaseException;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnauthorizedException;
import io.github.open_dataspaces.core.exception.UnexpectedException;
import io.github.open_dataspaces.core.exception.ValidateException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Unit tests for PasswordUrlController.
 */
class PasswordUrlControllerTest {

    @Mock
    private IdentityProviderService identityProviderService;

    @InjectMocks
    private PasswordUrlController passwordUrlController;

    /**
     * Constructor for PasswordUrlControllerTest.
     */
    public PasswordUrlControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeEach
    void setUp() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        ServletRequestAttributes attributes = new ServletRequestAttributes(mockRequest);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    /**
     * Provides test cases for the url method.
     */
    static Stream<TestCase> urlTestCases() {
        return Stream.of(
            // Valid request
            new TestCase("valid-request", "api-key-123", "client-id-1", "http://redirect.uri/1", false, "http://generated.url/1", "YxjhKleYUxz6BrJPGPOdqhDEX9WbbYgmN3BcLJwMYtA", HttpStatus.OK, null),

            // Validation error in request body
            new TestCase("validation-error", "api-key-123", "client-id-1", "http://redirect.uri/1", true, null, "YxjhKleYUxz6BrJPGPOdqhDEX9WbbYgmN3BcLJwMYtA", HttpStatus.BAD_REQUEST, "%s: cannot be blank"),
            new TestCase("validation-error", "api-key-123", "", "http://redirect.uri/1", true, null, "YxjhKleYUxz6BrJPGPOdqhDEX9WbbYgmN3BcLJwMYtA", HttpStatus.BAD_REQUEST, "%s: cannot be blank"),
            new TestCase("validation-error", "api-key-123", "client-id-1", "", true, null, "YxjhKleYUxz6BrJPGPOdqhDEX9WbbYgmN3BcLJwMYtA", HttpStatus.BAD_REQUEST, "%s: cannot be blank"),
            new TestCase("validation-error", "api-key-123", null, "http://redirect.uri/1", true, null, "YxjhKleYUxz6BrJPGPOdqhDEX9WbbYgmN3BcLJwMYtA", HttpStatus.BAD_REQUEST, "%s: cannot be blank"),
            new TestCase("validation-error", "api-key-123", "client-id-1", null, true, null, "YxjhKleYUxz6BrJPGPOdqhDEX9WbbYgmN3BcLJwMYtA", HttpStatus.BAD_REQUEST, "%s: cannot be blank")
        );
    }

    /**
     * Parameterized test for the url method.
     *
     * @param tc the test case containing request parameters and expected results
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("urlTestCases")
    @DisplayName("url handles various scenarios")
    void url_handlesVariousScenarios(TestCase tc) {
        // Mock HttpServletRequest
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(tc.apiKey);

        // Mock BindingResult
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(tc.hasValidationErrors);

        // Prepare request body
        PasswordUrlRequest requestBody = new PasswordUrlRequest(tc.clientId, tc.redirectUri, tc.codeChallenge);

        if (tc.hasValidationErrors) {
            // Expect ValidateException for validation errors
            ValidateException exception = assertThrows(ValidateException.class, () -> passwordUrlController.url(requestBody, bindingResult, request));
            assertTrue(tc.expectedStatus != HttpStatus.OK, "Expected status should be BAD_REQUEST for validation errors.");
            assertTrue(exception.getResponseMessage().contains(ConstError.ERR_400_VALIDATION_FAILED_HEADER), "Expected validation error message.");
            assertTrue(exception.getLogMessage().contains(ConstError.ERRLOG_400_INVALID_REQUEST), "Expected validation error message.");
        } else {
            // Mock service response
            when(identityProviderService.buildPasswordChangeUrl(tc.apiKey, tc.clientId, tc.redirectUri, tc.codeChallenge)).thenReturn(tc.expectedUrl);

            // Call the method
            ResponseEntity<APIResponse<PasswordUrlResponse>> response = passwordUrlController.url(requestBody, bindingResult, request);

            // Verify response
            assertNotNull(response);
            assertEquals(tc.expectedStatus, response.getStatusCode());
            APIResponse<PasswordUrlResponse> apiResponse = response.getBody();
            assertNotNull(apiResponse);
            assertEquals(tc.expectedUrl, apiResponse.getData().getUrl());
        }
    }

    /**
     * Test for changePassword when identityProviderService.changePassword throws an exception.
     */
    @ParameterizedTest
    @CsvSource({
        // Exception
        "BadParametersException",
        "UnauthorizedException",
        "OutOfServiceException",
        "UnexpectedException",
        "RuntimeException"
    })
    @DisplayName("changePassword throws exception when identityProviderService fails")
    void test_identityProviderServiceThrowsException(String exceptionClassName) {
        // Arrange
        String apiKey = "test-api-key";
        String clientId = "client-id-1";
        String redirectUri = "http://redirect.uri/1";
        String codeChallenge = "YxjhKleYUxz6BrJPGPOdqhDEX9WbbYgmN3BcLJwMYtA";

        // Mock HttpServletRequest
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);

        // Mock BindingResult
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        // Prepare request body
        PasswordUrlRequest requestBody = new PasswordUrlRequest(clientId, redirectUri, codeChallenge);

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

        // Mock identityProviderService to throw an exception
        doThrow(exception).when(identityProviderService).buildPasswordChangeUrl(apiKey, clientId, redirectUri, codeChallenge);

        // Act & Assert
        Throwable thrownException = assertThrows(clazz, () -> {
            passwordUrlController.url(requestBody, bindingResult, request);
        });

        // Detailed verification according to exception type
        if (exception instanceof AbstractBaseException) {
            AbstractBaseException baseException = (AbstractBaseException) exception;
            AbstractBaseException thrownBaseException = (AbstractBaseException) thrownException;
            assertEquals(baseException.getLogMessage(), thrownBaseException.getLogMessage());
            assertEquals(baseException.getResponseMessage(), thrownBaseException.getResponseMessage());
        } else {
            assertEquals(exception.getMessage(), thrownException.getMessage());
        }

        // Verify interactions
        verify(request).getHeader(Const.HEADER_API_KEY);
        verify(bindingResult).hasErrors();
        verify(identityProviderService).buildPasswordChangeUrl(apiKey, clientId, redirectUri, codeChallenge);
    }

    /**
     * Simple holder for parameterized test data.
     */
    private static class TestCase {
        final String name;
        final String apiKey;
        final String clientId;
        final String redirectUri;
        final String codeChallenge;
        final boolean hasValidationErrors;
        final String expectedUrl;
        final HttpStatus expectedStatus;

        TestCase(String name, String apiKey, String clientId, String redirectUri, boolean hasValidationErrors, String expectedUrl, String codeChallenge, HttpStatus expectedStatus, String expectedMessage) {
            this.name = name;
            this.apiKey = apiKey;
            this.clientId = clientId;
            this.redirectUri = redirectUri;
            this.hasValidationErrors = hasValidationErrors;
            this.expectedUrl = expectedUrl;
            this.codeChallenge = codeChallenge;
            this.expectedStatus = expectedStatus;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
