/*
 * GlobalExceptionHandlerTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class tests the GlobalExceptionHandler functionality
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.application.exception;

import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.dto.APIErrorResponse;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.IllegalAuthDataException;
import io.github.open_dataspaces.core.exception.IllegalOperatorIdException;
import io.github.open_dataspaces.core.exception.LoginException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnexpectedException;
import io.github.open_dataspaces.core.exception.ValidateException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterEach;
import org.mockito.Mockito;

import org.slf4j.LoggerFactory;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.http.HttpInputMessage;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.ProcessingException;

/**
 * Unit tests for the GlobalExceptionHandler.
 */
public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;
    private final Logger logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private ListAppender<ILoggingEvent> listAppender;

    /**
     * Sets up the test environment before each test.
     */
    @BeforeEach
    void setUp() {
        // Mocking the JWTVerifyService and HttpServletRequest
        exceptionHandler = new GlobalExceptionHandler();
        request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getRequestURI()).thenReturn("/test/api/uri/endpoint");
        Mockito.when(request.getMethod()).thenReturn("PUT");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        // Mocking logger to capture log events
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        logger.setLevel(Level.INFO);
    }

    /**
     * Cleans up the test environment by detaching and stopping the ListAppender.
     */
    @AfterEach
    private void cleanup() {
        RequestContextHolder.resetRequestAttributes();
        if (listAppender != null) {
            logger.detachAppender(listAppender);
            listAppender.stop();
        }
        listAppender = null;
    }

    /**
     * (Case#1) Tests handling of IllegalAuthDataException for API key missing scenario.
     */
    @Test
    void testHandleAPIKeyMissingException() {
        IllegalAuthDataException exception = new IllegalAuthDataException(
                "API key is missing.", "ERR_403_API_KEY_MISSING");

        ResponseEntity<APIErrorResponse> response = exceptionHandler.handleAPIKeyMissingException(exception, request);

        // Validate the response
        APIErrorResponse apiErrorResponse = response.getBody();
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(apiErrorResponse, "Response body should not be null");
        // assertEquals("[auth] AccessDenied", apiErrorResponse.getCode());
        assertEquals("ERR_403_API_KEY_MISSING.", apiErrorResponse.getTitle());
        // assertTrue(apiErrorResponse.getDetail().contains("id: testOperatorId, timeStamp: "));

        // Validate the log message
        boolean debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.WARN)
                .anyMatch(event -> event.getFormattedMessage()
                .contains("API key is missing."));
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);
    }

    /**
     * (Case#2) Tests handling of ValidateException for bad API request senario.
     */
    @Test
    void testHandleValidateException() {
        ValidateException exception = new ValidateException(
                "OperatorId validation failed", "operatorId is required");

        ResponseEntity<APIErrorResponse> response = exceptionHandler.handleValidateException(exception, request);

        // Validate the response
        APIErrorResponse apiErrorResponse = response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(apiErrorResponse, "Response body should not be null");
        // assertEquals("[auth] BadRequest", apiErrorResponse.getCode());
        assertEquals("Validation failed, operatorId is required.", apiErrorResponse.getTitle());
        // assertTrue(apiErrorResponse.getDetail().contains("id: testOperatorId, timeStamp: "));

        // Validate the log message
        boolean debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.WARN)
                .anyMatch(event -> event.getFormattedMessage()
                .contains("OperatorId validation failed"));
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);
    }

    /**
     * (Case#3) Tests handling of BadParametersException for invalid request parameters.
     */
    @Test
    void testHandleBadParametersException() {
        BadParametersException exception = new BadParametersException(
                "Invalid request parameters.");

        ResponseEntity<APIErrorResponse> response = exceptionHandler.handleBadParametersException(exception, request);

        // Validate the response
        APIErrorResponse apiErrorResponse = response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(apiErrorResponse, "Response body should not be null");
        // assertEquals("[auth] BadRequest", apiErrorResponse.getCode());
        assertEquals("Invalid request parameters.", apiErrorResponse.getTitle());
        // assertTrue(apiErrorResponse.getDetail().contains("id: testOperatorId, timeStamp: "));

        // Validate the log message
        boolean debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.WARN)
                .anyMatch(event -> event.getFormattedMessage()
                .contains("Invalid request parameters."));
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);
    }

    /**
     * (Case#4) Tests handling of LoginException for login failure scenario.
     */
    @Test
    void testHandleLoginException() {
        LoginException exception = new LoginException(
                "Login failed", "testOperatorId123");

        ResponseEntity<APIErrorResponse> response = exceptionHandler.handleLoginException(exception, request);

        // Validate the response
        APIErrorResponse apiErrorResponse = response.getBody();
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(apiErrorResponse, "Response body should not be null");
        // assertEquals("[auth] Unauthorized", apiErrorResponse.getCode());
        assertEquals("Login failed.", apiErrorResponse.getTitle());
        // assertTrue(apiErrorResponse.getDetail().contains("id: testOperatorId, timeStamp: "));

        // Validate the log message
        boolean debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.WARN)
                .anyMatch(event -> event.getFormattedMessage()
                .contains("Login failed: id: testOperatorId123."));
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);
    }

    /**
     * (Case#5) Tests handling of UnexpectedException for unexpected system errors.
     */
    @Test
    void testHandleUnexpectedException() {
        UnexpectedException exception = new UnexpectedException(
                "Unexpected error occurred", "ERR_500_UNEXPECTED_ERROR");

        ResponseEntity<APIErrorResponse> response = exceptionHandler.handleUnexpectedException(exception, request);
        // Validate the response
        APIErrorResponse apiErrorResponse = response.getBody();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(apiErrorResponse, "Response body should not be null");
        // assertEquals("[auth] InternalServerError", apiErrorResponse.getCode());
        assertEquals("ERR_500_UNEXPECTED_ERROR.", apiErrorResponse.getTitle());
        // assertTrue(apiErrorResponse.getDetail().contains("id: testOperatorId, timeStamp: "));

        java.util.List<String> txtLog = new java.util.ArrayList<>();
        listAppender.list.forEach(event ->
                txtLog.add(event.getLevel() + " : " + event.getFormattedMessage())
        );
        // Validate the log message
        boolean debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.ERROR)
                .anyMatch(event -> event.getFormattedMessage()
                .contains("Unexpected error occurred: ERR_500_UNEXPECTED_ERROR"));
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);
    }

    /**
     * (Case#6) Tests handling of OutOfServiceException for outer service errors.
     */
    @Test
    void testHandleOutOfServiceException() {
        OutOfServiceException exception = new OutOfServiceException(
                "Keycloak is not in service.");

        ResponseEntity<APIErrorResponse> response = exceptionHandler.handleOutOfServiceException(exception, request);

        // Validate the response
        APIErrorResponse apiErrorResponse = response.getBody();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(apiErrorResponse, "Response body should not be null");
        // assertEquals("[auth] ServiceUnavailable", apiErrorResponse.getCode());
        assertEquals("Unexpected error occurred in outer service.", apiErrorResponse.getTitle());
        // assertTrue(apiErrorResponse.getDetail().contains("id: testOperatorId, timeStamp: "));

        // Validate the log message
        boolean debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.ERROR)
                .anyMatch(event -> event.getFormattedMessage()
                .contains("Keycloak is not in service."));
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);
    }

    /**
     * (Case#8) Tests handling of RestClientException for connection errors.
     */
    @Test
    void testHandleRestClientException() {
        RestClientException exception = new RestClientException("Connection error");
        logger.setLevel(Level.DEBUG);

        ResponseEntity<APIErrorResponse> response = exceptionHandler.handleRestClientException(exception, request);

        // Validate the response
        APIErrorResponse apiErrorResponse = response.getBody();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(apiErrorResponse, "Response body should not be null");
        // assertEquals("[auth] InternalServerError", apiErrorResponse.getCode());
        assertEquals(ConstError.ERR_500_MESSAGE, apiErrorResponse.getTitle());
        // assertTrue(apiErrorResponse.getDetail().contains("id: testOperatorId, timeStamp: "));

        // Validate the log message
        boolean debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.ERROR)
                .anyMatch(event -> event.getFormattedMessage()
                .contains("Connection error"));
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);

        long logCount = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.ERROR).count();

        // Set logger level to INFO for subsequent tests
        logger.setLevel(Level.OFF);
        response = exceptionHandler.handleRestClientException(exception, request);

        // Validate the log message
        debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.ERROR)
                .count() == logCount; // Ensure no additional INFO logs were added
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);
    }

    /**
     * (Case#9) Tests handling of DuplicateKeyException for database key conflicts.
     */
    @Test
    void testHandleDuplicateKeyException() {
        DuplicateKeyException exception = new DuplicateKeyException("key already exists");

        ResponseEntity<APIErrorResponse> response = exceptionHandler.handleDuplicateKeyException(exception, request);

        APIErrorResponse apiErrorResponse = response.getBody();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(apiErrorResponse, "Response body should not be null");
        // assertEquals("[auth] InternalServerError", apiErrorResponse.getCode());
        assertEquals("Unexpected error occurred.", apiErrorResponse.getTitle());
        // assertTrue(apiErrorResponse.getDetail().contains("id: testOperatorId, timeStamp: "));

        // Validate the log message
        boolean debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.ERROR)
                .anyMatch(event -> event.getFormattedMessage()
                .contains("key already exists"));
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);

        long logCount = listAppender.list.stream().count();

        // Set logger level to INFO for subsequent tests
        logger.setLevel(Level.OFF);
        response = exceptionHandler.handleDuplicateKeyException(exception, request);

        // Validate the log message
        debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.ERROR)
                .count() == logCount; // Ensure no additional INFO logs were added
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);
    }

    /**
     * (Case#10) Tests handling of IllegalArgumentException for invalid input parameters.
     */
    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("invalid input parameters");

        ResponseEntity<APIErrorResponse> response = exceptionHandler.handleIllegalArgumentException(exception, request);

        APIErrorResponse apiErrorResponse = response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(apiErrorResponse, "Response body should not be null");
        // assertEquals("[auth] BadRequest", apiErrorResponse.getCode());
        assertEquals("Invalid Argument.", apiErrorResponse.getTitle());
        // assertTrue(apiErrorResponse.getDetail().contains("id: testOperatorId, timeStamp: "));

        // Validate the log message
        boolean debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.ERROR)
                .anyMatch(event -> event.getFormattedMessage()
                .contains("invalid input parameters"));
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);

        long logCount = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.ERROR).count();

        // Set logger level to INFO for subsequent tests
        logger.setLevel(Level.OFF);
        response = exceptionHandler.handleIllegalArgumentException(exception, request);

        // Validate the log message
        debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.ERROR)
                .count() == logCount; // Ensure no additional INFO logs were added
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);
    }

    /**
     * (Case#11) Tests handling of ProcessingException for outer service errors.
     */
    @Test
    void testHandleInvocationTargetException() {
        ProcessingException exception = new ProcessingException("outer service error");

        ResponseEntity<APIErrorResponse> response = exceptionHandler.handleInvocationTargetException(exception, request);

        APIErrorResponse apiErrorResponse = response.getBody();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(apiErrorResponse, "Response body should not be null");
        // assertEquals("[auth] InternalServerError", apiErrorResponse.getCode());
        assertEquals("Unexpected error occurred.", apiErrorResponse.getTitle());
        // assertTrue(apiErrorResponse.getDetail().contains("id: testOperatorId, timeStamp: "));

        // Validate the log message
        boolean debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.ERROR)
                .anyMatch(event -> event.getFormattedMessage()
                .contains("outer service error"));
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);

        long logCount = listAppender.list.stream().count();

        // Set logger level to INFO for subsequent tests
        logger.setLevel(Level.OFF);
        response = exceptionHandler.handleInvocationTargetException(exception, request);

        // Validate the log message
        debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.ERROR)
                .count() == logCount; // Ensure no additional INFO logs were added
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);
    }

    /**
     * (Case#12) Tests handling of generic Exception for unexpected errors.
     */
    @Test
    void testHandleException() {
        Exception exception = new Exception("Generic error");

        ResponseEntity<APIErrorResponse> response = exceptionHandler.handleException(exception, request);

        APIErrorResponse apiErrorResponse = response.getBody();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(apiErrorResponse, "Response body should not be null");
        // assertEquals("[auth] InternalServerError", apiErrorResponse.getCode());
        assertEquals("Unexpected error occurred.", apiErrorResponse.getTitle());
        // assertTrue(apiErrorResponse.getDetail().contains("id: testOperatorId, timeStamp: "));

        // Validate the log message
        boolean debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.ERROR)
                .anyMatch(event -> event.getFormattedMessage()
                .contains("Generic error"));
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);

        long logCount = listAppender.list.stream().count();

        // Set logger level to INFO for subsequent tests
        logger.setLevel(Level.OFF);
        response = exceptionHandler.handleException(exception, request);

        // Validate the log message
        debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.ERROR)
                .count() == logCount; // Ensure no additional INFO logs were added
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);
    }

    /**
     * (Case#13) Tests handling of HttpMediaTypeNotAcceptableException for unsupported media types.
     */
    @ParameterizedTest
    @CsvSource(value = {"Media type not acceptable"})
    @EmptySource
    void testHandleHttpMediaTypeNotAcceptable(String message) {
        // Mocking the WebRequest and HttpServletRequest
        WebRequest webRequest = Mockito.mock(ServletWebRequest.class);
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(httpServletRequest.getRequestURI()).thenReturn("/test/api/uri/endpoint");
        Mockito.when(httpServletRequest.getMethod()).thenReturn("PUT");

        // webRequest is a ServletWebRequest, so we need to cast it
        Mockito.when(((ServletWebRequest) webRequest).getRequest()).thenReturn(httpServletRequest);

        // Creating the exception and headers
        HttpMediaTypeNotAcceptableException exception = new HttpMediaTypeNotAcceptableException(message);
        HttpHeaders headers = new HttpHeaders();
        HttpStatusCode statusCode = HttpStatus.BAD_REQUEST;

        // Calling the exception handler
        ResponseEntity<Object> response = exceptionHandler.handleHttpMediaTypeNotAcceptable(exception, headers, statusCode, webRequest);

        // Validating the response
        String expectedMessage = StringUtils.hasText(message) ? message + "." : ConstError.ERR_400;
        APIErrorResponse apiErrorResponse = (APIErrorResponse) response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), apiErrorResponse.getStatus());
        // assertNotNull(apiErrorResponse, "Response body should not be null");
        // assertEquals("[auth] BadRequest", apiErrorResponse.getCode());
        assertEquals(expectedMessage, apiErrorResponse.getTitle());
        // assertTrue(apiErrorResponse.getDetail().contains("id: testOperatorId333, timeStamp: "));

        // Validate the log message
        boolean debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.WARN)
                .anyMatch(event -> event.getFormattedMessage()
                .contains(message));
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);

        long logCount = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.WARN).count();

        // Set logger level to ERROR for subsequent tests
        logger.setLevel(Level.ERROR);
        // response = exceptionHandler.handleHttpMediaTypeNotAcceptable(exception, headers, statusCode, webRequest);

        // Validate the log message
        debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.WARN)
                .count() == logCount; // Ensure no additional INFO logs were added
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);
    }

    /**
     * (Case#14) Tests handling of HttpMessageNotReadableException for unreadable messages.
     */
    @ParameterizedTest
    @CsvSource(value = {"1,Message not readable:aaa:ddd", "2,Message not readable", "3,:", "4,''"})
    void testHandleHttpMessageNotReadable(int caseNo, String message) {
        // Mocking the WebRequest and HttpServletRequest
        WebRequest webRequest = Mockito.mock(ServletWebRequest.class);
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(httpServletRequest.getRequestURI()).thenReturn("/test/api/uri/endpoint");
        Mockito.when(httpServletRequest.getMethod()).thenReturn("PUT");

        // webRequest is a ServletWebRequest, so we need to cast it
        Mockito.when(((ServletWebRequest) webRequest).getRequest()).thenReturn(httpServletRequest);

        // Creating the exception and headers
        HttpInputMessage dummyInputMessage = Mockito.mock(HttpInputMessage.class);
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(message, dummyInputMessage);
        HttpHeaders headers = new HttpHeaders();
        HttpStatusCode statusCode = HttpStatus.BAD_REQUEST;

        // Calling the exception handler
        ResponseEntity<Object> response = exceptionHandler.handleHttpMessageNotReadable(exception, headers, statusCode, webRequest);

        // Validating the response
        APIErrorResponse apiErrorResponse = (APIErrorResponse) response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), apiErrorResponse.getStatus());
        assertNotNull(apiErrorResponse, "Response body should not be null");
        // assertEquals("[auth] BadRequest", apiErrorResponse.getCode());
        final String expectedMessage;
        switch (caseNo) {
            case 1:
                expectedMessage = "Message not readable:aaa.";
                break;
            case 2:
                expectedMessage = "Message not readable.";
                break;
            case 3:
                expectedMessage = "Malformed Request Body, Unreadable HTTP Message.";
                break;
            case 4:
                expectedMessage = "Malformed Request Body, Unreadable HTTP Message.";
                break;
            default:
                expectedMessage = "Unexpected case number: " + caseNo;
                break;
        }
        assertEquals(expectedMessage, apiErrorResponse.getTitle());
        // assertTrue(apiErrorResponse.getDetail().contains("id: testOperatorId, timeStamp: "));

        // Validate the log message
        boolean debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.WARN)
                .anyMatch(event -> event.getFormattedMessage()
                .contains(message));
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);

        long logCount = listAppender.list.stream().count();

        // Set logger level to ERROR for subsequent tests
        logger.setLevel(Level.ERROR);
        // response = exceptionHandler.handleHttpMessageNotReadable(exception, headers, statusCode, webRequest);

        // Validate the log message
        debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.WARN)
                .count() == logCount; // Ensure no additional INFO logs were added
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);
    }

    /**
     * (Case#15) Tests handling of HttpMediaTypeNotAcceptableException for internal errors.
     */
    @Test
    void testHandleExceptionInternal() {
        // Mocking the WebRequest and HttpServletRequest
        WebRequest webRequest = Mockito.mock(ServletWebRequest.class);
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(httpServletRequest.getRequestURI()).thenReturn("/test/api/uri/endpoint");
        Mockito.when(httpServletRequest.getMethod()).thenReturn("PUT");

        // webRequest is a ServletWebRequest, so we need to cast it
        Mockito.when(((ServletWebRequest) webRequest).getRequest()).thenReturn(httpServletRequest);

        // Creating the exception and headers
        Exception exception = new Exception("Endpoint error");
        HttpHeaders headers = new HttpHeaders();
        HttpStatusCode statusCode = HttpStatus.BAD_REQUEST;

        // Calling the exception handler
        ResponseEntity<Object> response = exceptionHandler.handleExceptionInternal(exception, null, headers, statusCode, webRequest);

        // Validating the response
        assertNotNull(response, "Response should not be null");
        APIErrorResponse apiErrorResponse = (APIErrorResponse) response.getBody();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        // assertNotNull(apiErrorResponse, "Response body should not be null");
        // assertEquals("[auth] NotFound", apiErrorResponse.getCode());
        assertEquals("Endpoint not found.", apiErrorResponse.getTitle());
        // assertTrue(apiErrorResponse.getDetail().contains("id: testOperatorId, timeStamp: "));

        // Validate the log message
        boolean debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.WARN)
                .anyMatch(event -> event.getFormattedMessage()
                .contains("Endpoint error"));
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);

        long logCount = listAppender.list.stream().count();

        // Set logger level to OFF for subsequent tests
        logger.setLevel(Level.OFF);
        response = exceptionHandler.handleExceptionInternal(exception, null, headers, statusCode, webRequest);

        // Validate the log message
        debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.WARN)
                .count() == logCount; // Ensure no additional WARN logs were added
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);
    }

    /**
     * (Case#16) Tests the private method httpErrorGenerator of GlobalExceptionHandler.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testHttpErrorGenerator() {
        // Invode private method authDump with a mock request
        Method method = null;
        ResponseEntity<APIErrorResponse> response = null;
        try {
            method = GlobalExceptionHandler.class.getDeclaredMethod("httpErrorGenerator",
            String.class, String.class, HttpStatus.class, String.class, HttpServletRequest.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            assertTrue(false, "Method not found.");
            return;
        }
        method.setAccessible(true);
        try {
            response = (ResponseEntity<APIErrorResponse>) method.invoke(new GlobalExceptionHandler(),
                    "main message", "this is detail message", HttpStatus.BAD_REQUEST, "test", request);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            assertTrue(false, "Method cannot be invoked.");
            return;
        }

        // Validating the response
        assertNotNull(response, "Response should not be null");
        APIErrorResponse apiErrorResponse = (APIErrorResponse) response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(apiErrorResponse, "Response body should not be null");
        // assertEquals("[test] BadRequest", apiErrorResponse.getCode());
        // assertEquals("main message, this is detail message.", apiErrorResponse.getMessage());
        //assertTrue(apiErrorResponse.getDetail().contains("id: testOperatorId, timeStamp: "));
    }

    /**
     * (Case#17) Tests handling of IllegalOperatorIdException for invalid operatorId.
     */
    @Test
    void handleIllegalOperatorIdException_returnsInternalServerAPIErrorResponse() {
        IllegalOperatorIdException exception = new IllegalOperatorIdException();

        ResponseEntity<APIErrorResponse> response = exceptionHandler.handleIllegalOperatorIdException(exception, request);
        // Validate the response
        APIErrorResponse apiErrorResponse = response.getBody();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(apiErrorResponse, "Response body should not be null");
        // assertEquals("[auth] InternalServerError", apiErrorResponse.getCode());
        assertEquals(ConstError.ERR_500_OPERATORID_INVALID, apiErrorResponse.getTitle());
        // assertTrue(apiErrorResponse.getDetail().contains("id: testOperatorId, timeStamp: "));

        // Validate the log message
        boolean debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.ERROR)
                .anyMatch(event -> event.getFormattedMessage()
                .contains(ConstError.ERR_500_OPERATORID_INVALID));
        assertTrue(debugLogFound, "Wrong logged messages" + listAppender.list);
    }
}
