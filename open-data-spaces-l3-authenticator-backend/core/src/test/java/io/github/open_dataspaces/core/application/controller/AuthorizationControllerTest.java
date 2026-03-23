/*
 * AuthorizationControllerTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This package contains unit tests for the AuthorizationController class,
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.RequestContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.open_dataspaces.core.application.exception.GlobalExceptionHandler;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstPath;
import io.github.open_dataspaces.core.domain.service.interfaces.AuthorizationService;
import io.github.open_dataspaces.core.domain.service.interfaces.StoreService;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.ConflictException;
import io.github.open_dataspaces.core.exception.NotFoundException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnexpectedException;

/**
 * Unit tests for AuthorizationController.
 */
@ExtendWith(MockitoExtension.class)
public class AuthorizationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @InjectMocks
    AuthorizationController authorizationController;
    @Mock
    AuthorizationService authorizationService;
    @Mock
    StoreService storeService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // Register JavaTimeModule
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // Use ISO-8601 format

    // Common input parameters
    private final String commonStoreId = "commonStoreId-123";

    private final String commonApiKey = "api-key-123";
    private final Map<String, Object> commonRequestBody = Map.of("key", "value"); // Adjusted to match the expected response structure with
    private final Map<String, Object> commonResponseBody = Map.of("key", "value"); // Adjusted to match the expected response structure with

    // Common API response body JSON paths
    private final String commonAPIResponseBodyTitle = "$.title";
    private final String commonAPIResponseBodyType = "$.type";
    private final String commonAPIResponseBodyStatus = "$.status";
    private final String commonAPIResponseBodyDetail = "$.detail";
    private final String commonAPIResponseBodyData = "$.data";
    private final String commonAPIResponseBodyDataKey = "$.data.key";
    private final String commonAPIResponseBodyDataValueString = "value";

    /**
     * setUp initializes the test environment before each test.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authorizationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
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
     * Test for postApi - Success Case.
     */
    @ParameterizedTest
    @CsvSource({
        ConstPath.AUTHORIZATION_PATH + ConstPath.AUTHORIZATION_AUTHORIZATION_MODELS_PATH,
        ConstPath.AUTHORIZATION_PATH + ConstPath.AUTHORIZATION_TUPLES_READ,
        ConstPath.AUTHORIZATION_PATH + ConstPath.AUTHORIZATION_TUPLES_WRITE,
        ConstPath.AUTHORIZATION_PATH + ConstPath.AUTHORIZATION_EVALUATION,
        ConstPath.AUTHORIZATION_PATH + ConstPath.AUTHORIZATION_EVALUATIONS
    })
    @DisplayName("postApi - Success Case")
    void testPostApi_success(String argRequestURI) throws Exception {
        // Convert special input values
        String requestURI = argRequestURI.replace("{" + Const.API_PATH_PARAM_STORE_ID + "}", commonStoreId);

        // Prepare
        MockHttpServletRequestBuilder requestBuilder = post(requestURI)
                .contentType(Const.CONTENT_TYPE_JSON)
                .content(objectMapper.writeValueAsString(commonRequestBody));

        // Arrange
        when(authorizationService.forward(anyString(), any(), any()))
                .thenReturn(ResponseEntity.ok(commonResponseBody));

        // Act & Assert
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath(commonAPIResponseBodyType).exists())
                .andExpect(jsonPath(commonAPIResponseBodyTitle).exists())
                .andExpect(jsonPath(commonAPIResponseBodyStatus).value(HttpStatus.OK.value()))
                .andExpect(jsonPath(commonAPIResponseBodyDetail).exists())
                .andExpect(jsonPath(commonAPIResponseBodyData).exists())
                .andExpect(jsonPath(commonAPIResponseBodyDataKey).value(commonAPIResponseBodyDataValueString));
    }

    /**
     * Test for postApi when authorizationService.forward throws an exception.
     */
    @ParameterizedTest
    @CsvSource({
        // Exception
        "BadParametersException",
        "ConflictException",
        "NotFoundException",
        "UnexpectedException",
        "OutOfServiceException"
    })
    @DisplayName("postApi throws exception when service fails")
    void testPostApi_serviceThrowsException(String exceptionClassName) throws Exception {
        // Prepare request URI
        String requestURI = ConstPath.AUTHORIZATION_PATH + ConstPath.AUTHORIZATION_AUTHORIZATION_MODELS_PATH
                .replace("{" + Const.API_PATH_PARAM_STORE_ID + "}", commonStoreId);

        // Prepare
        MockHttpServletRequestBuilder requestBuilder = post(requestURI)
                .contentType(Const.CONTENT_TYPE_JSON)
                .content(objectMapper.writeValueAsString(commonRequestBody));

        // Determine exception, expected status matcher, and expected HTTP status code
        Exception exception = null;
        ResultMatcher statusMatcher = null;
        HttpStatusCode httpStatusCode = null;
        switch (exceptionClassName) {
            case "BadParametersException":
                exception = new BadParametersException("dummmyLogMessage", "dummyResponseMessage");
                statusMatcher = status().isBadRequest();
                httpStatusCode = HttpStatus.BAD_REQUEST;
                break;
            case "ConflictException":
                exception = new ConflictException("dummyLogMessage", "dummyResponseMessage");
                statusMatcher = status().isConflict();
                httpStatusCode = HttpStatus.CONFLICT;
                break;
            case "NotFoundException":
                exception = new NotFoundException("dummyLogMessage");
                statusMatcher = status().isNotFound();
                httpStatusCode = HttpStatus.NOT_FOUND;
                break;
            case "UnexpectedException":
                exception = new UnexpectedException("dummyLogMessage", "dummyResponseMessage");
                statusMatcher = status().isInternalServerError();
                httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR;
                break;
            case "OutOfServiceException":
                exception = new OutOfServiceException("dummyLogMessage");
                statusMatcher = status().isServiceUnavailable();
                httpStatusCode = HttpStatus.SERVICE_UNAVAILABLE;
                break;
            default:
                // Unexpected exception
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        // Arrange
        when(authorizationService.forward(anyString(), any(), any()))
                .thenThrow(exception);

        // Act & Assert
        mockMvc.perform(requestBuilder)
                .andExpect(statusMatcher)
                .andExpect(jsonPath(commonAPIResponseBodyType).exists())
                .andExpect(jsonPath(commonAPIResponseBodyTitle).exists())
                .andExpect(jsonPath(commonAPIResponseBodyStatus).value(httpStatusCode.value()))
                .andExpect(jsonPath(commonAPIResponseBodyDetail).exists())
                .andExpect(jsonPath(commonAPIResponseBodyData).doesNotExist());
    }

    /**
     * Test for getApi - Success Case.
     */
    @ParameterizedTest
    @CsvSource({
        ConstPath.AUTHORIZATION_PATH + ConstPath.AUTHORIZATION_STORE,
        ConstPath.AUTHORIZATION_PATH + ConstPath.AUTHORIZATION_AUTHORIZATION_MODELS_PATH
    })
    @DisplayName("getApi - Success Case")
    void testGetApi_success(String argRequestURI) throws Exception {
        // Convert special input values
        String requestURI = argRequestURI.replace("{" + Const.API_PATH_PARAM_STORE_ID + "}", commonStoreId);

        // Prepare
        MockHttpServletRequestBuilder requestBuilder = get(requestURI);

        // Arrange
        when(authorizationService.forward(anyString(), any(), any()))
                .thenReturn(ResponseEntity.ok(commonResponseBody));

        // Act & Assert
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath(commonAPIResponseBodyType).exists())
                .andExpect(jsonPath(commonAPIResponseBodyTitle).exists())
                .andExpect(jsonPath(commonAPIResponseBodyStatus).value(HttpStatus.OK.value()))
                .andExpect(jsonPath(commonAPIResponseBodyDetail).exists())
                .andExpect(jsonPath(commonAPIResponseBodyData).exists())
                .andExpect(jsonPath(commonAPIResponseBodyDataKey).value(commonAPIResponseBodyDataValueString));
    }

    /**
     * Test for getApi when authorizationService.forward throws an exception.
     */
    @ParameterizedTest
    @CsvSource({
        // Exception
        "BadParametersException",
        "ConflictException",
        "NotFoundException",
        "UnexpectedException",
        "OutOfServiceException"
    })
    @DisplayName("getApi throws exception when service fails")
    void testGetApi_serviceThrowsException(String exceptionClassName) throws Exception {
        // Prepare request URI
        String requestURI = ConstPath.AUTHORIZATION_PATH + ConstPath.AUTHORIZATION_AUTHORIZATION_MODELS_PATH
                .replace("{" + Const.API_PATH_PARAM_STORE_ID + "}", commonStoreId);

        // Prepare
        MockHttpServletRequestBuilder requestBuilder = get(requestURI);

        // Determine exception, expected status matcher, and expected HTTP status code
        Exception exception = null;
        ResultMatcher statusMatcher = null;
        HttpStatusCode httpStatusCode = null;
        switch (exceptionClassName) {
            case "BadParametersException":
                exception = new BadParametersException("dummmyLogMessage", "dummyResponseMessage");
                statusMatcher = status().isBadRequest();
                httpStatusCode = HttpStatus.BAD_REQUEST;
                break;
            case "ConflictException":
                exception = new ConflictException("dummyLogMessage", "dummyResponseMessage");
                statusMatcher = status().isConflict();
                httpStatusCode = HttpStatus.CONFLICT;
                break;
            case "NotFoundException":
                exception = new NotFoundException("dummyLogMessage");
                statusMatcher = status().isNotFound();
                httpStatusCode = HttpStatus.NOT_FOUND;
                break;
            case "UnexpectedException":
                exception = new UnexpectedException("dummyLogMessage", "dummyResponseMessage");
                statusMatcher = status().isInternalServerError();
                httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR;
                break;
            case "OutOfServiceException":
                exception = new OutOfServiceException("dummyLogMessage");
                statusMatcher = status().isServiceUnavailable();
                httpStatusCode = HttpStatus.SERVICE_UNAVAILABLE;
                break;
            default:
                // Unexpected exception
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        // Arrange
        when(authorizationService.forward(anyString(), any(), any()))
                .thenThrow(exception);

        // Act & Assert
        mockMvc.perform(requestBuilder)
                .andExpect(statusMatcher)
                .andExpect(jsonPath(commonAPIResponseBodyType).exists())
                .andExpect(jsonPath(commonAPIResponseBodyTitle).exists())
                .andExpect(jsonPath(commonAPIResponseBodyStatus).value(httpStatusCode.value()))
                .andExpect(jsonPath(commonAPIResponseBodyDetail).exists())
                .andExpect(jsonPath(commonAPIResponseBodyData).doesNotExist());
    }

    /**
     * Test for postStores - Success Case.
     */
    @Test
    @DisplayName("postStores - Success Case")
    void testPostStores_success() throws Exception {
        String requestURI = ConstPath.AUTHORIZATION_PATH + ConstPath.AUTHORIZATION_STORES_PATH;

        // Prepare
        MockHttpServletRequestBuilder requestBuilder = post(requestURI)
                .contentType(Const.CONTENT_TYPE_JSON)
                .content(objectMapper.writeValueAsString(commonRequestBody))
                .header(Const.HEADER_API_KEY, commonApiKey);

        // Arrange
        when(storeService.registerStore(anyString(), anyString()))
                .thenReturn(ResponseEntity.created(null).body(commonResponseBody));

        // Act & Assert
        mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath(commonAPIResponseBodyType).exists())
                .andExpect(jsonPath(commonAPIResponseBodyTitle).exists())
                .andExpect(jsonPath(commonAPIResponseBodyStatus).value(HttpStatus.CREATED.value()))
                .andExpect(jsonPath(commonAPIResponseBodyDetail).exists())
                .andExpect(jsonPath(commonAPIResponseBodyData).exists())
                .andExpect(jsonPath(commonAPIResponseBodyDataKey).value(commonAPIResponseBodyDataValueString));
    }

    /**
     * Test for postStores when storeService.registerStore throws an exception.
     */
    @ParameterizedTest
    @CsvSource({
        // Exception
        "BadParametersException",
        "ConflictException",
        "NotFoundException",
        "UnexpectedException",
        "OutOfServiceException"
    })
    @DisplayName("postStores throws exception when service fails")
    void testPostStores_serviceThrowsException(String exceptionClassName) throws Exception {
        // Prepare request URI
        String requestURI = ConstPath.AUTHORIZATION_PATH + ConstPath.AUTHORIZATION_STORES_PATH;

        // Prepare
        MockHttpServletRequestBuilder requestBuilder = post(requestURI)
                .contentType(Const.CONTENT_TYPE_JSON)
                .content(objectMapper.writeValueAsString(commonRequestBody))
                .header(Const.HEADER_API_KEY, commonApiKey);

        // Determine exception, expected status matcher, and expected HTTP status code
        Exception exception = null;
        ResultMatcher statusMatcher = null;
        HttpStatusCode httpStatusCode = null;
        switch (exceptionClassName) {
            case "BadParametersException":
                exception = new BadParametersException("dummmyLogMessage", "dummyResponseMessage");
                statusMatcher = status().isBadRequest();
                httpStatusCode = HttpStatus.BAD_REQUEST;
                break;
            case "ConflictException":
                exception = new ConflictException("dummyLogMessage", "dummyResponseMessage");
                statusMatcher = status().isConflict();
                httpStatusCode = HttpStatus.CONFLICT;
                break;
            case "NotFoundException":
                exception = new NotFoundException("dummyLogMessage");
                statusMatcher = status().isNotFound();
                httpStatusCode = HttpStatus.NOT_FOUND;
                break;
            case "UnexpectedException":
                exception = new UnexpectedException("dummyLogMessage", "dummyResponseMessage");
                statusMatcher = status().isInternalServerError();
                httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR;
                break;
            case "OutOfServiceException":
                exception = new OutOfServiceException("dummyLogMessage");
                statusMatcher = status().isServiceUnavailable();
                httpStatusCode = HttpStatus.SERVICE_UNAVAILABLE;
                break;
            default:
                // Unexpected exception
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        // Arrange
        when(storeService.registerStore(anyString(), anyString()))
                .thenThrow(exception);

        // Act & Assert
        mockMvc.perform(requestBuilder)
                .andExpect(statusMatcher)
                .andExpect(jsonPath(commonAPIResponseBodyType).exists())
                .andExpect(jsonPath(commonAPIResponseBodyTitle).exists())
                .andExpect(jsonPath(commonAPIResponseBodyStatus).value(httpStatusCode.value()))
                .andExpect(jsonPath(commonAPIResponseBodyDetail).exists())
                .andExpect(jsonPath(commonAPIResponseBodyData).doesNotExist());
    }

    /**
     * Test for deleteStore - Success Case.
     */
    @Test
    @DisplayName("deleteStore - Success Case")
    void testDeleteStore_success() throws Exception {
        // Convert special input values
        String requestURI = ConstPath.AUTHORIZATION_PATH + ConstPath.AUTHORIZATION_STORE
                .replace("{" + Const.API_PATH_PARAM_STORE_ID + "}", commonStoreId);

        // Prepare
        MockHttpServletRequestBuilder requestBuilder = delete(requestURI)
                .header(Const.HEADER_API_KEY, commonApiKey);

        // Act & Assert
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andExpect(jsonPath(commonAPIResponseBodyType).doesNotExist())
                .andExpect(jsonPath(commonAPIResponseBodyTitle).doesNotExist())
                .andExpect(jsonPath(commonAPIResponseBodyStatus).doesNotExist())
                .andExpect(jsonPath(commonAPIResponseBodyDetail).doesNotExist())
                .andExpect(jsonPath(commonAPIResponseBodyData).doesNotExist())
                .andExpect(jsonPath(commonAPIResponseBodyDataKey).doesNotExist());
        verify(storeService).deleteStore(anyString(), anyString());
    }

    /**
     * Test for deleteStore when storeService.deleteStore throws an exception.
     */
    @ParameterizedTest
    @CsvSource({
        // Exception
        "BadParametersException",
        "ConflictException",
        "NotFoundException",
        "UnexpectedException",
        "OutOfServiceException"
    })
    @DisplayName("deleteStore throws exception when service fails")
    void testDeleteStore_serviceThrowsException(String exceptionClassName) throws Exception {
        // Prepare request URI
        String requestURI = ConstPath.AUTHORIZATION_PATH + ConstPath.AUTHORIZATION_STORE
                .replace("{" + Const.API_PATH_PARAM_STORE_ID + "}", commonStoreId);

        // Prepare
        MockHttpServletRequestBuilder requestBuilder = delete(requestURI)
                .header(Const.HEADER_API_KEY, commonApiKey);

        // Determine exception, expected status matcher, and expected HTTP status code
        Exception exception = null;
        ResultMatcher statusMatcher = null;
        HttpStatusCode httpStatusCode = null;
        switch (exceptionClassName) {
            case "BadParametersException":
                exception = new BadParametersException("dummmyLogMessage", "dummyResponseMessage");
                statusMatcher = status().isBadRequest();
                httpStatusCode = HttpStatus.BAD_REQUEST;
                break;
            case "ConflictException":
                exception = new ConflictException("dummyLogMessage", "dummyResponseMessage");
                statusMatcher = status().isConflict();
                httpStatusCode = HttpStatus.CONFLICT;
                break;
            case "NotFoundException":
                exception = new NotFoundException("dummyLogMessage");
                statusMatcher = status().isNotFound();
                httpStatusCode = HttpStatus.NOT_FOUND;
                break;
            case "UnexpectedException":
                exception = new UnexpectedException("dummyLogMessage", "dummyResponseMessage");
                statusMatcher = status().isInternalServerError();
                httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR;
                break;
            case "OutOfServiceException":
                exception = new OutOfServiceException("dummyLogMessage");
                statusMatcher = status().isServiceUnavailable();
                httpStatusCode = HttpStatus.SERVICE_UNAVAILABLE;
                break;
            default:
                // Unexpected exception
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        // Arrange
        doThrow(exception).when(storeService).deleteStore(anyString(), anyString());

        // Act & Assert
        mockMvc.perform(requestBuilder)
                .andExpect(statusMatcher)
                .andExpect(jsonPath(commonAPIResponseBodyType).exists())
                .andExpect(jsonPath(commonAPIResponseBodyTitle).exists())
                .andExpect(jsonPath(commonAPIResponseBodyStatus).value(httpStatusCode.value()))
                .andExpect(jsonPath(commonAPIResponseBodyDetail).exists())
                .andExpect(jsonPath(commonAPIResponseBodyData).doesNotExist());
    }
}
