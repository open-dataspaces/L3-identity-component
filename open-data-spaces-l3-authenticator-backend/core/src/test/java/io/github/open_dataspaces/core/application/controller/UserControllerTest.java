/*
 * UserControllerTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This package contains unit tests for the UserController class,
 *
 * Date: 2025/11/30
 */

package io.github.open_dataspaces.core.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.dto.APIResponse;
import io.github.open_dataspaces.core.domain.dto.IdpAccountInfo;
import io.github.open_dataspaces.core.domain.dto.PostUserRequest;
import io.github.open_dataspaces.core.domain.dto.PostUserResponse;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.exception.AbstractBaseException;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.ConflictException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnexpectedException;
import io.github.open_dataspaces.core.exception.ValidateException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Unit tests for UserController.
 */
public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private IdentityProviderService identityProviderService;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private BindingResult bindingResult;
    @Mock
    private PostUserRequest postUserRequest;
    @Mock
    private Validator validator;
    @Mock
    private Set<ConstraintViolation<PostUserRequest>> violations;

    // Common input parameters
    private final String commonApiKey = "apiKey123";
    private final String commonUserId = UUID.randomUUID().toString();
    private final String commonPassword = "password123";

    private final String commonLoginUserId = "login-user-id";
    private final String commonCreatePasswordFlagTrue = "true";
    private final String commonCreatePasswordFlagFalse = "false";
    private final String commonPasswordTemporaryFlagTrue = "true";
    private final String commonPasswordTemporaryFlagFalse = "false";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/test-context");
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);

        MockitoAnnotations.openMocks(this);
        userController = new UserController(identityProviderService);

        // Inject the mock validator into the controller using reflection
        try {
            Field validatorField = UserController.class.getDeclaredField("validator");
            validatorField.setAccessible(true);
            validatorField.set(userController, validator);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to inject validator mock", e);
        }
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
     * Test for postUser - Success case.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // Normal cases
        "VALUE, TRUE, TRUE",  // All parameters provided
        // Length boundary cases
        "aaa, TRUE, FALSE",     // Min length for optional fields
        "LONG, FALSE, FALSE",     // Max length for optional fields
        // Pattern boundary cases
        // -- no test case --
        // Not required values
        // -- no test case --
    }, nullValues = "EMPTY")
    @DisplayName("postUser - Success Case")
    void testPostUser_success(
            String argLoginUserId,
            String argCreatePasswordFlag,
            String argPasswordTemporaryFlag
    ) throws IOException {
        // Convert special input values
        String loginUserId =
                "LONG".equals(argLoginUserId) ? "E".repeat(Const.LOGIN_USER_ID_LENGTH_MAX) :
                "VALUE".equals(argLoginUserId) ? commonLoginUserId : argLoginUserId;
        String createPasswordFlag =
                "TRUE".equals(argCreatePasswordFlag) ? commonCreatePasswordFlagTrue :
                "FALSE".equals(argCreatePasswordFlag) ? commonCreatePasswordFlagFalse : commonCreatePasswordFlagTrue;
        String passwordTemporaryFlag =
                "TRUE".equals(argPasswordTemporaryFlag) ? commonPasswordTemporaryFlagTrue :
                "FALSE".equals(argPasswordTemporaryFlag) ? commonPasswordTemporaryFlagFalse : commonPasswordTemporaryFlagTrue;
        // Prepare raw JSON input
        PostUserRequest postUserRequest = new PostUserRequest();
        postUserRequest.setLoginUserId(loginUserId);
        postUserRequest.setCreatePasswordFlag(createPasswordFlag);
        postUserRequest.setPasswordTemporaryFlag(passwordTemporaryFlag);
        // Convert request object to JSON string
        String rawJson = objectMapper.writeValueAsString(postUserRequest);
        // Arrange
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn(commonApiKey);

        IdpAccountInfo idpAccountInfo = new IdpAccountInfo(
                commonUserId,
                loginUserId,
                null,
                commonPassword,
                true
        );
        when(identityProviderService.createAccount(any(), any(), any(), anyBoolean(), anyBoolean()))
                .thenReturn(idpAccountInfo);

        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(PostUserRequest.class))).thenReturn(violations);

        // Act
        ResponseEntity<APIResponse<PostUserResponse>> response =
                userController.postUser(httpServletRequest, rawJson);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CREATED.value(), response.getBody().getStatus());

        verify(identityProviderService).createAccount(commonApiKey, loginUserId, null, Boolean.valueOf(createPasswordFlag), Boolean.valueOf(passwordTemporaryFlag));
    }

    /**
     * Test for postUser - Success case (not required fields omitted).
     */
    @Test
    @DisplayName("postUser - Success Case (not required fields omitted)")
    void testPostUser_success_notRequired() throws IOException {
        // Prepare raw JSON input
        StringBuilder jsonSb = new StringBuilder();
        String rowJson = jsonSb.append("{")
                .append("\"").append(Const.JSON_PROPERTY_LOGIN_USER_ID).append("\":\"").append(commonLoginUserId).append("\"")
                .append("}").toString();

        // Arrange
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn(commonApiKey);

        IdpAccountInfo idpAccountInfo = new IdpAccountInfo(
                commonUserId,
                commonLoginUserId,
                null,
                commonPassword,
                true
        );
        when(identityProviderService.createAccount(any(), any(), any(), anyBoolean(), anyBoolean()))
                .thenReturn(idpAccountInfo);

        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(PostUserRequest.class))).thenReturn(violations);

        // Act
        ResponseEntity<APIResponse<PostUserResponse>> response =
                userController.postUser(httpServletRequest, rowJson);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CREATED.value(), response.getBody().getStatus());

        verify(identityProviderService).createAccount(commonApiKey, commonLoginUserId, null, Const.DEFAULT_CREATE_PASSWORD_FLAG, Const.DEFAULT_PASSWORD_TEMPORARY_FLAG);
    }

    /**
     * Test for postUser - Validation error.
     */
    @Test
    @DisplayName("postUser - Validation Error")
    void testPostUser_validationError() throws Exception {
        // Arrange
        when(violations.isEmpty()).thenReturn(false);
        when(validator.validate(any(PostUserRequest.class))).thenReturn(violations);

        // Prepare raw JSON input
        StringBuilder jsonSb = new StringBuilder();
        String rowJson = jsonSb.append("{")
                .append("\"").append(Const.JSON_PROPERTY_LOGIN_USER_ID).append("\":\"").append(commonLoginUserId).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_CREATE_PASSWORD_FLAG).append("\":").append(commonCreatePasswordFlagTrue).append(",")  // Boolean value without quotes
                .append("\"").append(Const.JSON_PROPERTY_PASSWORD_TEMPORARY_FLAG).append("\":").append(commonPasswordTemporaryFlagTrue)  // Boolean value without quotes
                .append("}").toString();

        // Act & Assert
        ValidateException exception = assertThrows(ValidateException.class, () ->
                userController.postUser(httpServletRequest, rowJson)
        );
        assertEquals(ConstError.ERRLOG_400_INVALID_REQUEST, exception.getLogMessage());
        assertNotNull(exception.getMessage());

        verify(identityProviderService, never()).createAccount(
                any(), any(), any(), anyBoolean(), anyBoolean());
    }

    /**
     * Test for postUser - throws an exception.
     */
    @ParameterizedTest
    @CsvSource({
        // Exception
        "BadParametersException",
        "ConflictException",
        "UnexpectedException",
        "OutOfServiceException",
        "RuntimeException"
    })
    @DisplayName("postUser throws exception when service fails")
    void testPostUser_serviceThrowsException(String exceptionClassName) throws Exception {
        // Prepare exception to be thrown by identityProviderService.deleteAccount
        Exception exception = null;
        Class<? extends Throwable> clazz = null;
        switch (exceptionClassName) {
            case "BadParametersException":
                exception = new BadParametersException("dummmyLogMessage", "dummyResponseMessage");
                clazz = BadParametersException.class;
                break;
            case "OutOfServiceException":
                exception = new OutOfServiceException("dummyLogMessage");
                clazz = OutOfServiceException.class;
                break;
            case "ConflictException":
                exception = new ConflictException("dummyLogMessage", "dummyResponseMessage");
                clazz = ConflictException.class;
                break;
            case "UnexpectedException":
                exception = new UnexpectedException("dummyLogMessage", "dummyResponseMessage");
                clazz = UnexpectedException.class;
                break;
            case "RuntimeException":
                exception = new RuntimeException("dummyMessage");
                clazz = RuntimeException.class;
                break;
            default:
                // Unexpected exception
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        // Arrange common mock behaviors
        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(PostUserRequest.class))).thenReturn(violations);
        when(identityProviderService.createAccount(
                any(), any(), any(), anyBoolean(), anyBoolean()))
                .thenThrow(exception);

        // Prepare raw JSON input
        StringBuilder jsonSb = new StringBuilder();
        String rowJson = jsonSb.append("{")
                .append("\"").append(Const.JSON_PROPERTY_LOGIN_USER_ID).append("\":\"").append(commonLoginUserId).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_CREATE_PASSWORD_FLAG).append("\":").append(commonCreatePasswordFlagTrue).append(",")  // Boolean value without quotes
                .append("\"").append(Const.JSON_PROPERTY_PASSWORD_TEMPORARY_FLAG).append("\":").append(commonPasswordTemporaryFlagFalse)  // Boolean value without quotes
                .append("}").toString();

        // Act & Assert
        Throwable thrownException = assertThrows(clazz, () -> {
            userController.postUser(httpServletRequest, rowJson);
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
        verify(identityProviderService).createAccount(
                any(), any(), any(), anyBoolean(), anyBoolean());
    }
}