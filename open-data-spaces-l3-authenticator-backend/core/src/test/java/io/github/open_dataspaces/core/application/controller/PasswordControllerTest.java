/*
 * PasswordControllerTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This package contains unit tests for the PasswordController class.
 *
 * Date: 2025/08/31
 */

package io.github.open_dataspaces.core.application.controller;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.springframework.validation.BindingResult;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.dto.PasswordRequest;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.exception.AbstractBaseException;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnauthorizedException;
import io.github.open_dataspaces.core.exception.UnexpectedException;
import io.github.open_dataspaces.core.exception.ValidateException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Unit tests for PasswordController.
 */
class PasswordControllerTest {

    @Mock
    private IdentityProviderService identityProviderService;

    @InjectMocks
    private PasswordController passwordController;

    /**
     * Constructor for PasswordControllerTest.
     */
    public PasswordControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Provides test cases for changePassword method.
     */
    static Stream<TestCase> changePasswordCases() {
        return Stream.of(
            // Valid request
            new TestCase("valid-request", "valid-api-key", "valid-operator-id", "oldPass123", "newPass123", false, HttpStatus.NO_CONTENT),

            // Validation error in request body
            new TestCase("validation-request", "valid-api-key", "valid-operator-id", "oldPass123", "newPass123", true, null),
            new TestCase("validation-error", "valid-api-key", "valid-operator-id", null, "newPass123", true, null),
            new TestCase("validation-error", "valid-api-key", "valid-operator-id", "", "newPass123", true, null),
            new TestCase("validation-error", "valid-api-key", "valid-operator-id", "oldPass123", null, true, null),
            new TestCase("validation-error", "valid-api-key", "valid-operator-id", "oldPass123", "", true, null),

            // Missing API key in header
            new TestCase("missing-api-key", null, "valid-operator-id", "oldPass123", "newPass123", false, HttpStatus.NO_CONTENT)
        );
    }

    /**
     * Parameterized test for changePassword method.
     *
     * @param tc the test case containing request parameters and expected results
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("changePasswordCases")
    @DisplayName("changePassword handles various scenarios")
    void changePassword_handlesVariousScenarios(TestCase tc) {
        // Mock HttpServletRequest
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(tc.apiKey);

        // Mock BindingResult
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(tc.hasValidationErrors);

        // Prepare request body
        PasswordRequest requestBody = new PasswordRequest(tc.oldPassword, tc.newPassword);

        if (tc.hasValidationErrors) {
            // Expect ValidateException for validation errors
            ValidateException exception = assertThrows(ValidateException.class, () -> passwordController.changePassword(requestBody, bindingResult, request, tc.operatorId));
            assertTrue(tc.expectedStatus != HttpStatus.NO_CONTENT, "Expected status should be BAD_REQUEST for validation errors.");
            assertTrue(exception.getResponseMessage().contains(ConstError.ERR_400_VALIDATION_FAILED_HEADER), "Expected validation error message.");
            assertTrue(exception.getLogMessage().contains(ConstError.ERRLOG_400_INVALID_REQUEST), "Expected validation error message.");
        } else {
            // Call the method
            ResponseEntity<Object> response = passwordController.changePassword(requestBody, bindingResult, request, tc.operatorId);

            // Verify response
            assertNotNull(response);
            assertEquals(tc.expectedStatus, response.getStatusCode());
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
    void testChangePassword_identityProviderServiceThrowsException(String exceptionClassName) {
        // Arrange
        String apiKey = "test-api-key";
        String operatorId = "operator-id";
        String oldPassword = "OldPassword1!";
        String newPassword = "NewPassword1!";

        // Mock HttpServletRequest
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);

        // Mock BindingResult
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        // Prepare request body
        PasswordRequest requestBody = new PasswordRequest(oldPassword, newPassword);

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
        doThrow(exception).when(identityProviderService).changePassword(apiKey, operatorId, oldPassword, newPassword);

        // Act & Assert
        Throwable thrownException = assertThrows(clazz, () -> {
            passwordController.changePassword(requestBody, bindingResult, request, operatorId);
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
        verify(identityProviderService).changePassword(apiKey, operatorId, oldPassword, newPassword);
    }

    /**
     * Simple holder for parameterized test data.
     */
    private static class TestCase {
        final String name;
        final String apiKey;
        final String operatorId;
        final String oldPassword;
        final String newPassword;
        final boolean hasValidationErrors;
        final HttpStatus expectedStatus;

        TestCase(String name, String apiKey, String operatorId, String oldPassword, String newPassword, boolean hasValidationErrors, HttpStatus expectedStatus) {
            this.name = name;
            this.apiKey = apiKey;
            this.operatorId = operatorId;
            this.oldPassword = oldPassword;
            this.newPassword = newPassword;
            this.hasValidationErrors = hasValidationErrors;
            this.expectedStatus = expectedStatus;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}