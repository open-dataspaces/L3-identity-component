/*
 * ClientsControllerTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This package contains unit tests for the ClientsController class,
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.application.controller;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.github.open_dataspaces.core.application.exception.GlobalExceptionHandler;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.consts.ConstPath;
import io.github.open_dataspaces.core.domain.dto.APIResponse;
import io.github.open_dataspaces.core.domain.dto.IdpClientInfo;
import io.github.open_dataspaces.core.domain.dto.PostClientsRequest;
import io.github.open_dataspaces.core.domain.dto.PostClientsResponse;
import io.github.open_dataspaces.core.domain.dto.PutClientsAuthCodeResponse;
import io.github.open_dataspaces.core.domain.dto.PutClientsClientCredentialsResponse;
import io.github.open_dataspaces.core.domain.dto.PutClientsRequest;
import io.github.open_dataspaces.core.domain.service.interfaces.ClientSecretService;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.domain.service.interfaces.JWTVerifyService;
import io.github.open_dataspaces.core.exception.AbstractBaseException;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.ConflictException;
import io.github.open_dataspaces.core.exception.NotFoundException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnauthorizedException;
import io.github.open_dataspaces.core.exception.UnexpectedException;
import io.github.open_dataspaces.core.exception.ValidateException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Unit tests for ClientsController.
 */
public class ClientsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @InjectMocks
    private ClientsController clientsController;
    @Mock
    private ClientSecretService clientSecretService;
    @Mock
    private JWTVerifyService jwtService;
    @Mock
    private IdentityProviderService identityProviderService;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private BindingResult bindingResult;
    @Mock
    private PostClientsRequest postClientsRequest;

    // common data
    private final String commonClientSecretRequestURI = ConstPath.AUTH_PATH + ConstPath.AUTH_CLIENTS_SECRET_PATH_SHORT;
    private final String commonAPIKey = "test-api-key";
    private final String commonClientUuid = "test-client-id";
    private final String commonClientSecret = "test-client-secret";
    private final String commonAPIResponseBodyTitle = "$.title";
    private final String commonAPIResponseBodyType = "$.type";
    private final String commonAPIResponseBodyStatus = "$.status";
    private final String commonAPIResponseBodyDetail = "$.detail";
    private final String commonAPIResponseBodyData = "$.data";
    private final String commonAPIResponseBodyDataClientSecret = "$.data.client_secret";
    private final String commonApiKey = "apiKey123";
    private final String commonUuid = UUID.randomUUID().toString();
    private final String commonFlowType = "authorization_code";
    private final String commonClientId = UUID.randomUUID().toString();
    private final String commonName = "Name";
    private final String commonDescription = "Description";
    private final String commonOperatorId = UUID.randomUUID().toString();
    private final String commonOpenSystemId = "openSystemId";
    private final List<String> commonRedirectUrisList = List.of("http://redirect.uri");
    private final String commonRedirectUris = "http://redirect.uri";
    private final boolean commonEnabled = true;

    /**
     * setUp initializes the test environment before each test.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(clientsController)
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
     * Constructor to initialize mocks.
     */
    public ClientsControllerTest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request = new MockHttpServletRequest();
        request.setContextPath("/test-context");
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
        MockitoAnnotations.openMocks(this);
        clientsController = new ClientsController(jwtService, identityProviderService, clientSecretService);
    }

    /**
     * testGetClientSecret_success tests the successful retrieval of client secret.
     *
     * @param argSecret the client secret to be returned by the service
     * @throws Exception if an error occurs during the test
     */
    @ParameterizedTest
    @CsvSource({
        // Client Secret Retrieval Success
        commonClientSecret,
    })
    @NullAndEmptySource
    @DisplayName("getClientSecret - Success Case")
    public void testGetClientSecret_success(String argSecret) throws Exception {
        // Prepare request URI
        String requestURI = commonClientSecretRequestURI.replace("{client_uuid}", commonClientUuid);

        // Prepare
        MockHttpServletRequestBuilder requestBuilder = post(requestURI)
                .contentType(Const.CONTENT_TYPE_JSON)
                .header("API-Key", commonAPIKey);

        // Arrange
        when(clientSecretService.existsById(anyString())).thenReturn(true);
        doNothing().when(clientSecretService).deleteById(anyString());
        when(identityProviderService.getClientSecret(anyString(), anyString())).thenReturn(argSecret);

        // Act & Assert
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath(commonAPIResponseBodyType).exists())
                .andExpect(jsonPath(commonAPIResponseBodyTitle).exists())
                .andExpect(jsonPath(commonAPIResponseBodyStatus).value(HttpStatus.OK.value()))
                .andExpect(jsonPath(commonAPIResponseBodyDetail).exists())
                .andExpect(jsonPath(commonAPIResponseBodyData).exists())
                .andExpect(jsonPath(commonAPIResponseBodyDataClientSecret).value(argSecret));
    }

    /**
     * testGetClientSecret_notFound tests the scenario where the client secret link is not found.
     *
     * @throws Exception if an error occurs during the test
     */
    @Test
    @DisplayName("getClientSecret - Not Found Case")
    public void testGetClientSecret_notFound() throws Exception {
        // Prepare request URI
        String requestURI = commonClientSecretRequestURI.replace("{client_uuid}", commonClientUuid);

        // Prepare
        MockHttpServletRequestBuilder requestBuilder = post(requestURI)
                .contentType(Const.CONTENT_TYPE_JSON)
                .header("API-Key", commonAPIKey);

        // Arrange
        when(clientSecretService.existsById(anyString())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(commonAPIResponseBodyType).exists())
                .andExpect(jsonPath(commonAPIResponseBodyTitle).exists())
                .andExpect(jsonPath(commonAPIResponseBodyStatus).value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath(commonAPIResponseBodyDetail).exists())
                .andExpect(jsonPath(commonAPIResponseBodyData).doesNotExist());
    }

    /**
     * testGetClientSecret_serviceThrowsException tests exception handling when services throw exceptions.
     *
     * @param exceptionClassName the name of the exception class to be thrown
     * @throws Exception if an error occurs during the test
     */
    @ParameterizedTest
    @CsvSource({
        // Exception
        "BadParametersException",
        "NotFoundException",
        "UnexpectedException",
        "OutOfServiceException"
    })
    @DisplayName("getClientSecret throws exception when service fails")
    void testGetClientSecret_serviceThrowsException(String exceptionClassName) throws Exception {
        // Prepare request URI
        String requestURI = commonClientSecretRequestURI.replace("{client_uuid}", commonClientUuid);

        // Prepare
        MockHttpServletRequestBuilder requestBuilder = post(requestURI)
                .contentType(Const.CONTENT_TYPE_JSON)
                .header("API-Key", commonAPIKey);

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
        when(clientSecretService.existsById(anyString())).thenThrow(exception);

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
     * Test for PostClient - Success case.
     */
    @ParameterizedTest
    @CsvSource({
        // Typical case
        "client_credentials,VALUE,VALUE,VALUE,VALUE,VALUE,VALUE",
        "VALUE,SMALL,EMPTY,EMPTY,EMPTY,EMPTY,SMALL",
        "VALUE,LONG,LONG,LONG,LONG,LONG,LONG",
        "VALUE,VALUE,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY"
    })
    @DisplayName("postClients - Success Case")
    void testPostClients_success(
            String argFlowType,
            String argClientId,
            String argName,
            String argDescription,
            String argOperatorId,
            String argOpenSystemId,
            String argRedirectUris) throws Exception {
        // Arrange
        String flowType =
                "VALUE".equals(argFlowType) ? commonFlowType : "client_credentials";
        String clientId =
                "SMALL".equals(argClientId) ? "a".repeat(Const.CLIENT_ID_LENGTH_MIN) :
                "LONG".equals(argClientId) ? "N".repeat(Const.CLIENT_ID_LENGTH_MAX) :
                "VALUE".equals(argClientId) ? commonClientId : argClientId;
        String name =
                "LONG".equals(argName) ? "N".repeat(Const.CLIENT_NAME_LENGTH_MAX) :
                "EMPTY".equals(argName) ? null :
                "VALUE".equals(argName) ? commonName : argName;
        String description =
                "LONG".equals(argDescription) ? "N".repeat(Const.CLIENT_DESCRIPTION_LENGTH_MAX) :
                "EMPTY".equals(argDescription) ? null :
                "VALUE".equals(argDescription) ? commonDescription : argDescription;
        String operatorId =
                "UUID".equals(argOperatorId) ? commonOperatorId :
                "EMPTY".equals(argOperatorId) ? null :
                "VALUE".equals(argOperatorId) ? commonOperatorId : argOperatorId;
        String openSystemId =
                "LONG".equals(argOpenSystemId) ? "N".repeat(Const.CLIENT_OPEN_SYSTEM_ID_LENGTH_MAX) :
                "EMPTY".equals(argOpenSystemId) ? null :
                "VALUE".equals(argOpenSystemId) ? commonOpenSystemId : argOpenSystemId;
        String stringRedirectUris =
                "LONG".equals(argRedirectUris) ? "N".repeat(Const.REDIRECT_URI_LENGTH_MAX) :
                "SMALL".equals(argRedirectUris) ? "a".repeat(Const.REDIRECT_URI_LENGTH_MIN) :
                "VALUE".equals(argRedirectUris) ? commonRedirectUris : argRedirectUris;
        List<String> redirectUris = List.of(stringRedirectUris);
        String creatorOperatorId = "creator123";

        HttpServletRequest request = mock(HttpServletRequest.class);
        PostClientsRequest requestBody = new PostClientsRequest(
                flowType,
                clientId,
                name,
                description,
                operatorId,
                openSystemId,
                redirectUris);

        IdpClientInfo info = new IdpClientInfo();
        info.setUuid(commonUuid);
        info.setClientId(clientId);
        info.setName(name);
        info.setDescription(description);
        info.setRedirectUris(redirectUris);
        info.setClientSecret(commonClientSecret);
        info.setEnabled(commonEnabled);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(creatorOperatorId);
        doNothing().when(clientSecretService).save(any(), any(), any());
        when(identityProviderService.registerClient(
                any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(info);

        // Act
        ResponseEntity<APIResponse<PostClientsResponse>> responseEntity = null;
        try {
            responseEntity = clientsController.postClients(
                    request, requestBody, mock(BindingResult.class));
        } catch (Exception e) {
            // Handle exception if any
            assertEquals(false, "Exception thrown during test execution: " + e.getMessage());
            return;
        }

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        APIResponse<PostClientsResponse> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.CREATED.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());
        assertEquals(commonUuid, apiResponse.getData().getClientUuid());
        assertEquals(clientId, apiResponse.getData().getClientId());
        assertEquals(commonEnabled, apiResponse.getData().getEnabled());
        assertEquals(name == null ? "" : name, apiResponse.getData().getName());
        assertEquals(description == null ? "" : description, apiResponse.getData().getDescription());
        assertEquals(flowType, apiResponse.getData().getFlowType());
        assertEquals(openSystemId, apiResponse.getData().getOpenSystemId());
        assertEquals(operatorId, apiResponse.getData().getOperatorId());
        assertEquals(redirectUris, apiResponse.getData().getRedirectUris());

    }

    /**
     * Test for postClient - Validation error.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // flowType=authorization_code, redirectUris=null/empty → ValidateException
        "authorization_code,VALUE,NULL",    // redirectUris is null
        "authorization_code,VALUE,EMPTY",   // redirectUris is empty
        // flowType=client_credentials, openSystemId=null/empty → ValidateException
        "client_credentials,NULL,VALUE",    // openSystemId is null
        "client_credentials,EMPTY,VALUE"    // openSystemId is empty
    })
    @DisplayName("postClients - validationError")
    void testPostClients_validationError(
            String flowType,
            String argOpenSystemId,
            String argRedirectUris) throws Exception {
        // Arrange
        String openSystemId =
                "NULL".equals(argOpenSystemId) ? null :
                "EMPTY".equals(argOpenSystemId) ? "" :
                "VALUE".equals(argOpenSystemId) ? commonOpenSystemId : argOpenSystemId;
        List<String> redirectUris =
                "NULL".equals(argRedirectUris) ? null :
                "EMPTY".equals(argRedirectUris) ? List.of() :
                "VALUE".equals(argRedirectUris) ? commonRedirectUrisList : List.of(argRedirectUris);

        PostClientsRequest requestBody = new PostClientsRequest(
                flowType, commonClientId, commonName, commonDescription, commonOperatorId, openSystemId, redirectUris);
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(commonOperatorId);

        // Act & Assert
        ValidateException exception = assertThrows(ValidateException.class, () ->
                clientsController.postClients(request, requestBody, bindingResult)
        );
        assertEquals(ConstError.ERRLOG_400_INVALID_REQUEST, exception.getLogMessage());
        assertNotNull(exception.getMessage());
    }

    /**
     * Test for postClients - JWT error.
     */
    @Test
    @DisplayName("postClients - JWT Service Error")
    void testPostClients_jwtServiceError() throws Exception {
        // Arrange
        PostClientsRequest requestBody = new PostClientsRequest();
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenThrow(
                new UnauthorizedException(ConstError.ERRLOG_401_INVALID_TOKEN, ConstError.ERR_401_INVALID_TOKEN));

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () ->
                clientsController.postClients(request, requestBody, bindingResult)
        );
        assertEquals(ConstError.ERRLOG_401_INVALID_TOKEN, exception.getLogMessage());
        assertNotNull(exception.getMessage());
    }

    /**
     * Test for postClients when identityProviderService.deleteAccount throws an exception.
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
    @DisplayName("postClient throws exception when identityProviderService fails")
    void testPostClients_serviceThrowsException(String exceptionClassName) {
        // Arrange
        String flowType = "flowType";
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn(commonApiKey);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(commonOperatorId);

        HttpServletRequest request = mock(HttpServletRequest.class);

        PostClientsRequest requestBody = new PostClientsRequest(
                flowType, commonClientId, commonName, commonDescription, commonOperatorId, commonOpenSystemId, commonRedirectUrisList);
        IdpClientInfo info = new IdpClientInfo();

        when(identityProviderService.registerClient(
                any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(info);

        // Prepare exception to be thrown by identityProviderService.deleteAccount
        Exception exception = null;
        Class<? extends Throwable> clazz = null;
        switch (exceptionClassName) {
            case "BadParametersException":
                exception = new BadParametersException("dummyLogMessage", "dummyResponseMessage");
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
            case "NotFoundException":
                exception = new NotFoundException("dummyLogMessage");
                clazz = NotFoundException.class;
                break;
            default:
                // Unexpected exception
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        // Mock identityProviderService to throw an exception
        doThrow(exception).when(clientSecretService).save(any(), any(), any());

        // Act & Assert
        Throwable thrownException = assertThrows(clazz, () -> {
            clientsController.postClients(request, requestBody, bindingResult);
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
        verify(bindingResult).hasErrors();
        verify(jwtService).getOperatorOrOpenSystemId(any(HttpServletRequest.class));
        verify(clientSecretService).save(any(), any(), any());
    }

    /**
     * Test for PutClient - Success case.
     */
    @ParameterizedTest
    @CsvSource({
        // Typical case
        "client_credentials,VALUE,VALUE,VALUE,VALUE,VALUE,VALUE",
        "client_credentials,VALUE,EMPTY,EMPTY,VALUE,VALUE,EMPTY",
        "authorization_code,SMALL,EMPTY,EMPTY,EMPTY,EMPTY,SMALL",
        "VALUE,LONG,LONG,LONG,LONG,LONG,LONG",
        "VALUE,VALUE,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY",
        "authorization_code,VALUE,VALUE,VALUE,EMPTY,EMPTY,NULL",
        "authorization_code,VALUE,VALUE,VALUE,EMPTY,EMPTY,EMPTY",
        "client_credentials,VALUE,VALUE,VALUE,EMPTY,EMPTY,EMPTY"
    })
    @DisplayName("putClients - Success Case")
    void testPutClients_success(
            String argFlowType,
            String argClientId,
            String argName,
            String argDescription,
            String argOperatorId,
            String argOpenSystemId,
            String argRedirectUris) throws Exception {
        // Arrange
        String flowType =
                "VALUE".equals(argFlowType) ? commonFlowType : argFlowType;
        String clientId =
                "SMALL".equals(argClientId) ? "a".repeat(Const.CLIENT_ID_LENGTH_MIN) :
                "LONG".equals(argClientId) ? "N".repeat(Const.CLIENT_ID_LENGTH_MAX) :
                "VALUE".equals(argClientId) ? commonClientId : argClientId;
        String name =
                "LONG".equals(argName) ? "N".repeat(Const.CLIENT_NAME_LENGTH_MAX) :
                "EMPTY".equals(argName) ? null :
                "VALUE".equals(argName) ? commonName : argName;
        String description =
                "LONG".equals(argDescription) ? "N".repeat(Const.CLIENT_DESCRIPTION_LENGTH_MAX) :
                "EMPTY".equals(argDescription) ? null :
                "VALUE".equals(argDescription) ? commonDescription : argDescription;
        String operatorId =
                "UUID".equals(argOperatorId) ? commonOperatorId :
                "EMPTY".equals(argOperatorId) ? null :
                "VALUE".equals(argOperatorId) ? commonOperatorId : argOperatorId;
        String openSystemId =
                "LONG".equals(argOpenSystemId) ? "N".repeat(Const.CLIENT_OPEN_SYSTEM_ID_LENGTH_MAX) :
                "EMPTY".equals(argOpenSystemId) ? null :
                "VALUE".equals(argOpenSystemId) ? commonOpenSystemId : argOpenSystemId;
        List<String> redirectUris = "NULL".equals(argRedirectUris) ? null
                : "EMPTY".equals(argRedirectUris) ? Collections.emptyList()
                : List.of("LONG".equals(argRedirectUris) ? "N".repeat(Const.REDIRECT_URI_LENGTH_MAX)
                : "SMALL".equals(argRedirectUris) ? "a".repeat(Const.REDIRECT_URI_LENGTH_MIN)
                : "VALUE".equals(argRedirectUris) ? commonRedirectUris : argRedirectUris);

        HttpServletRequest request = mock(HttpServletRequest.class);
        PutClientsRequest requestBody = new PutClientsRequest(
                flowType,
                name,
                description,
                operatorId,
                openSystemId,
                redirectUris);

        IdpClientInfo info = new IdpClientInfo();
        info.setUuid(commonUuid);
        info.setName(name);
        info.setDescription(description);
        info.setRedirectUris(redirectUris);
        info.setClientSecret(commonClientSecret);
        info.setEnabled(commonEnabled);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(commonApiKey);
        when(identityProviderService.getClientUuid(anyString(), anyString())).thenReturn(commonUuid);
        when(identityProviderService.updateClient(
                any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(info);

        // Act
        ResponseEntity<APIResponse<?>> responseEntity = clientsController.putClients(
                request, clientId, requestBody, bindingResult);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        APIResponse<?> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());
        if (Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_AUTHORIZATION_CODE.equals(flowType)) {
            List<String> expectedRedirectUris =
                    (redirectUris == null || redirectUris.isEmpty()) ? null : redirectUris;
            PutClientsAuthCodeResponse data =
                    (PutClientsAuthCodeResponse) apiResponse.getData();
            assertEquals(name, data.getName());
            assertEquals(description, data.getDescription());
            assertEquals(flowType, data.getFlowType());
            assertEquals(expectedRedirectUris, data.getRedirectUris());
        } else {
            PutClientsClientCredentialsResponse data =
                    (PutClientsClientCredentialsResponse) apiResponse.getData();
            assertEquals(name, data.getName());
            assertEquals(description, data.getDescription());
            assertEquals(flowType, data.getFlowType());
            assertEquals(openSystemId, data.getOpenSystemId());
            assertEquals(operatorId, data.getOperatorId());
        }
    }

    /**
     * Test for PutClient - Validation error case.
     */
    @Test
    @DisplayName("putClients - Validation Error Case")
    void testPutClients_validationError() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        PutClientsRequest requestBody = new PutClientsRequest(
                commonFlowType,
                commonName,
                commonDescription,
                commonOperatorId,
                commonOpenSystemId,
                commonRedirectUrisList);

        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(List.of(new ObjectError("putClientsRequest", "dummyError")));

        // Act & Assert
        assertThrows(ValidateException.class, () -> {
            clientsController.putClients(request, commonClientId, requestBody, bindingResult);
        });

        verify(bindingResult).hasErrors();
        verify(bindingResult).getAllErrors();
    }

    /**
     * testPutClients_serviceThrowsException tests exception handling when services throw exceptions.
     *
     * @param exceptionClassName the name of the exception class to be thrown
     * @throws Exception if an error occurs during the test
     */
    @ParameterizedTest
    @CsvSource({
        // Exception
        "BadParametersException",
        "NotFoundException",
        "UnexpectedException",
        "OutOfServiceException"
    })
    @DisplayName("putClients throws exception when service fails")
    void testPutClients_serviceThrowsException(String exceptionClassName) throws Exception {
        // Prepare request URI
        String requestURI = (ConstPath.AUTH_PATH + ConstPath.AUTH_CLIENTS_ID_PATH_SHORT)
                .replace("{client_id}", commonClientId);

        // Prepare
        MockHttpServletRequestBuilder requestBuilder = delete(requestURI)
                .contentType(Const.CONTENT_TYPE_JSON)
                .header("API-Key", commonAPIKey);

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
        when(identityProviderService.getClientUuid(anyString(), anyString())).thenThrow(exception);

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
     * testDeleteClient_success tests the successful deletion of a client.
     *
     * @throws Exception if an error occurs during the test
     */
    @Test
    @DisplayName("deleteClient - Success Case")
    public void testDeleteClient_success() throws Exception {
        // Prepare request URI
        String requestURI = (ConstPath.AUTH_PATH + ConstPath.AUTH_CLIENTS_ID_PATH_SHORT)
                .replace("{client_id}", commonClientId);

        // Prepare
        MockHttpServletRequestBuilder requestBuilder = delete(requestURI)
                .contentType(Const.CONTENT_TYPE_JSON)
                .header("API-Key", commonAPIKey);

        // Arrange
        when(identityProviderService.getClientUuid(anyString(), anyString())).thenReturn(commonClientUuid);
        doNothing().when(identityProviderService).deleteClient(anyString(), anyString());
        doNothing().when(clientSecretService).deleteById(anyString());

        // Act & Assert
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent());

        verify(identityProviderService).getClientUuid(anyString(), anyString());
        verify(identityProviderService).deleteClient(anyString(), anyString());
        verify(clientSecretService).deleteById(anyString());
    }

    /**
     * testDeleteClients_serviceThrowsException tests exception handling when services throw exceptions.
     *
     * @param exceptionClassName the name of the exception class to be thrown
     * @throws Exception if an error occurs during the test
     */
    @ParameterizedTest
    @CsvSource({
        // Exception
        "BadParametersException",
        "NotFoundException",
        "UnexpectedException",
        "OutOfServiceException"
    })
    @DisplayName("deleteClient throws exception when service fails")
    void testDeleteClients_serviceThrowsException(String exceptionClassName) throws Exception {
        // Prepare request URI
        String requestURI = (ConstPath.AUTH_PATH + ConstPath.AUTH_CLIENTS_ID_PATH_SHORT)
                .replace("{client_id}", commonClientId);

        // Prepare
        MockHttpServletRequestBuilder requestBuilder = put(requestURI)
                .contentType(Const.CONTENT_TYPE_JSON)
                .header("API-Key", commonAPIKey)
                .content("{\"flow_type\":\"authorization_code\",\"redirect_uris\":[\"http://redirect.uri\"]}");

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
        when(identityProviderService.getClientUuid(anyString(), anyString())).thenThrow(exception);

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
