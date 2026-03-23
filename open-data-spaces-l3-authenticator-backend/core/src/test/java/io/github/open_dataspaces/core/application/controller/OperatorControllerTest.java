/*
 * OperatorControllerTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This package contains unit tests for the OperatorController class,
 *
 * Date: 2025/08/31
 */

package io.github.open_dataspaces.core.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import io.github.open_dataspaces.core.domain.service.interfaces.AuthorizationService;
import io.github.open_dataspaces.core.domain.service.interfaces.APIKeyService;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.github.open_dataspaces.core.exception.IllegalAuthDataException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.github.open_dataspaces.core.common.enums.EnumResponseTypes;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.dto.APIResponse;
import io.github.open_dataspaces.core.domain.dto.GetOperatorResponse;
import io.github.open_dataspaces.core.domain.dto.IdpAccountInfo;
import io.github.open_dataspaces.core.domain.dto.ListOperatorRequest;
import io.github.open_dataspaces.core.domain.dto.OperatorResponse;
import io.github.open_dataspaces.core.domain.dto.OperatorResult;
import io.github.open_dataspaces.core.domain.dto.PostOperatorRequest;
import io.github.open_dataspaces.core.domain.dto.PostOperatorResponse;
import io.github.open_dataspaces.core.domain.dto.PutOperatorRequest;
import io.github.open_dataspaces.core.domain.dto.PutOperatorStatusRequest;
import io.github.open_dataspaces.core.domain.dto.StateString;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.domain.service.interfaces.JWTVerifyService;
import io.github.open_dataspaces.core.domain.service.interfaces.OperatorService;
import io.github.open_dataspaces.core.common.config.ODSProperties;
import io.github.open_dataspaces.core.exception.AbstractBaseException;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.ConflictException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnauthorizedException;
import io.github.open_dataspaces.core.exception.UnexpectedException;
import io.github.open_dataspaces.core.exception.ValidateException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Unit tests for OperatorController.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OperatorControllerTest {

    @Mock
    private OperatorService operatorService;
    @Mock
    private JWTVerifyService jwtService;
    @Mock
    private IdentityProviderService identityProviderService;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private BindingResult bindingResult;
    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private APIKeyService apiKeyService;
    @Mock
    private ODSProperties odsProperties;
    @InjectMocks
    private OperatorController operatorController;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // Register JavaTimeModule
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // Use ISO-8601 format

    // Common input parameters
    private final String commonOperatorId = UUID.randomUUID().toString();
    private final String commonDeletedFlag = "false";

    private final String commonEffectiveDate = LocalDate.now().toString();
    private final int commonCount = 0;
    private final int commonIndex = 0;
    private final String commonSortKey = Const.JSON_PROPERTY_OPERATOR_ID;
    private final String commonSortOrder = "asc";

    private final String commonOperatorName = "operatorName";
    private final String commonOperatorAddress = "operatorAddress";
    private final String commonOpenOperatorId = "openOperatorId";
    private final String commonGlobalOperatorId = "globalOperatorId";

    private final String commonApiKey = "apiKey123";
    private final String commonStoreId = "store-uuid";

    /**
     * Constructor for OperatorControllerTest.
     */
    public OperatorControllerTest() {
    }

    /**
     * setUp initializes the test environment before each test.
     */
    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/test-context");
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
        // Mock ODSProperties to return true for authorization
        when(odsProperties.isEnableUcAuthorization()).thenReturn(true);
        // Mocks are initialized by MockitoExtension; `operatorController` is injected automatically.
        // No global default stubs here. Each test must explicitly set the mocks
        // it depends on to ensure negative cases are not masked.
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
     * case#25:
     * testGetOperatorSuccess tests the successful retrieval of an operator.
     */

    // --- testGetOperatorSuccess ---
    @Test
    void testGetOperatorSuccess() {
        // Arrange
        String operatorId = "d9a38406-cae2-4679-b052-15a75f5531e6";
        String operatorName = "login_user_id_12345";
        String operatorAddress = "試験県テスト市examビル1F";
        String openOperatorId = "1234567890123";
        String globalOperatorId = "123456789TT234567890";
        LocalDate effectiveStartDate = LocalDate.parse("2025-01-01");
        LocalDate effectiveEndDate = LocalDate.parse("2030-12-31");
        LocalDateTime createdAt = LocalDateTime.parse("2030-12-31T23:59:59.999");
        String createdUserId = "creator_user_id_12345";
        LocalDateTime updatedAt = LocalDateTime.parse("2030-12-31T23:59:59.999");
        String updatedUserId = "updater_user_id_12345";

        String userId = "test-user";
        String storeId = "test-store";
        String apiKey = "test-api-key";
        when(jwtService.getOperatorOrOpenSystemId(httpServletRequest)).thenReturn(userId);
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        when(apiKeyService.getUsecaseStoreId(apiKey)).thenReturn(storeId);
        when(operatorService.getOperatorWithAuth(
                eq(operatorId), eq(userId), eq(storeId)
        )).thenReturn(new OperatorResult(
                operatorId, operatorName, operatorAddress, openOperatorId, globalOperatorId, false, effectiveStartDate, effectiveEndDate, createdAt, createdUserId, updatedAt, updatedUserId
        ));

        // Act
        ResponseEntity<Object> responseEntity = operatorController.getOperator(httpServletRequest, operatorId);

        // Assert
        assertEquals(200, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
        APIResponse<?> apiResponse = (APIResponse<?>) responseEntity.getBody();

        // Verify the service was called with expected args (helps detect stubbing mismatches)
        verify(operatorService).getOperatorWithAuth(eq(operatorId), eq(userId), eq(storeId));

        // Convert response data safely to DTO (handles case where data is a Map)
        GetOperatorResponse getOperatorResponse = objectMapper.convertValue(apiResponse.getData(), GetOperatorResponse.class);
        assertNotNull(getOperatorResponse, "Response should be convertible to GetOperatorResponse");
        assertEquals(operatorId, getOperatorResponse.getOperatorId());
        assertEquals(operatorName, getOperatorResponse.getOperatorName());
        assertEquals(operatorAddress, getOperatorResponse.getOperatorAddress());
        assertEquals(openOperatorId, getOperatorResponse.getOpenOperatorId());
        assertEquals(globalOperatorId, getOperatorResponse.getGlobalOperatorId());
        assertEquals(effectiveStartDate, getOperatorResponse.getEffectiveStartDate());
        assertEquals(effectiveEndDate, getOperatorResponse.getEffectiveEndDate());
        assertEquals(createdAt, getOperatorResponse.getCreatedAt());
        assertEquals(updatedAt, getOperatorResponse.getUpdatedAt());
    }

    // --- testGetOperatorSuccessEmpty ---
    @Test
    void testGetOperatorSuccessEmpty() {
        // Arrange
        String operatorId = "d9a38406-cae2-4679-b052-15a75f5531e6";

        String userId = "test-user";
        String storeId = "test-store";
        String apiKey = "test-api-key";
        when(jwtService.getOperatorOrOpenSystemId(httpServletRequest)).thenReturn(userId);
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        when(apiKeyService.getUsecaseStoreId(apiKey)).thenReturn(storeId);
        when(operatorService.getOperatorWithAuth(
            eq(operatorId), eq(userId), eq(storeId)
        )).thenReturn(null);

        // Act
        ResponseEntity<Object> responseEntity = operatorController.getOperator(httpServletRequest, operatorId);

        // Assert
        assertEquals(200, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
    }

    // --- testGetOperatorExceptionDuringOperatorCreation ---
    @Test
    void testGetOperatorExceptionDuringOperatorCreation() {
        // Arrange
        String operatorId = "d9a38406-cae2-4679-b052-15a75f5531e6";
        String userId = "test-user";
        String storeId = "test-store";
        String apiKey = "test-api-key";
        when(jwtService.getOperatorOrOpenSystemId(httpServletRequest)).thenReturn(userId);
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        when(apiKeyService.getUsecaseStoreId(apiKey)).thenReturn(storeId);
        when(operatorService.getOperatorWithAuth(
                eq(operatorId), eq(userId), eq(storeId)
        )).thenThrow(new ValidateException("Invalid request parameters.", ""));

        // Act & Assert
        ValidateException exception = assertThrows(ValidateException.class, () -> {
            operatorController.getOperator(httpServletRequest, operatorId);
        });
        System.out.println("[DEBUG] exception.getLogMessage(): " + exception.getLogMessage());
        assertNotNull(exception.getLogMessage(), "LogMessage is null");
        assertTrue(exception.getLogMessage().contains("Invalid request parameters."), "LogMessage: " + exception.getLogMessage());
        verify(operatorService, times(1)).getOperatorWithAuth(eq(operatorId), eq(userId), eq(storeId));
    }

    // --- testGetOperatorWithAuthorizationDisabled ---
    @Test
    void testGetOperatorWithAuthorizationDisabled() throws Exception {
        // Arrange
        String operatorId = "d9a38406-cae2-4679-b052-15a75f5531e6";
        String operatorName = "login_user_id_12345";
        String operatorAddress = "試験県テスト市examビル1F";
        String openOperatorId = "1234567890123";
        String globalOperatorId = "123456789TT234567890";
        LocalDate effectiveStartDate = LocalDate.parse("2025-01-01");
        LocalDate effectiveEndDate = LocalDate.parse("2030-12-31");
        LocalDateTime createdAt = LocalDateTime.parse("2030-12-31T23:59:59.999");
        String createdUserId = "creator_user_id_12345";
        LocalDateTime updatedAt = LocalDateTime.parse("2030-12-31T23:59:59.999");
        String updatedUserId = "updater_user_id_12345";

        String userId = "test-user";

        // Set isEnableUcAuthorization to false for this test
        when(odsProperties.isEnableUcAuthorization()).thenReturn(false);

        when(jwtService.getOperatorOrOpenSystemId(httpServletRequest)).thenReturn(userId);
        when(operatorService.getOperator(eq(operatorId)))
                .thenReturn(new OperatorResult(
                operatorId, operatorName, operatorAddress, openOperatorId, globalOperatorId, false, effectiveStartDate, effectiveEndDate, createdAt, createdUserId, updatedAt, updatedUserId
        ));

        // Act
        ResponseEntity<Object> responseEntity = operatorController.getOperator(httpServletRequest, operatorId);

        // Assert
        assertEquals(200, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
        APIResponse<?> apiResponse = (APIResponse<?>) responseEntity.getBody();

        // Verify apiKeyService.getUsecaseStoreId() is NOT called when auth is disabled (optimization)
        verify(apiKeyService, times(0)).getUsecaseStoreId(any());

        // Verify the service was called without auth
        verify(operatorService).getOperator(eq(operatorId));
        verify(operatorService, times(0)).getOperatorWithAuth(anyString(), anyString(), anyString());

        // Convert response data safely to DTO
        GetOperatorResponse getOperatorResponse = objectMapper.convertValue(apiResponse.getData(), GetOperatorResponse.class);
        assertNotNull(getOperatorResponse, "Response should be convertible to GetOperatorResponse");
        assertEquals(operatorId, getOperatorResponse.getOperatorId());
        assertEquals(operatorName, getOperatorResponse.getOperatorName());
    }

    /**
     * Test for putOperator - Success case.
     */
    @ParameterizedTest
    @CsvSource({
        "operator123,2025-08-30T10:15:30.000Z,user1,Operator Name,Address1,open123,global123",
        "operator456,2025-09-01T12:00:00.000Z,user2,Another Operator,Address2,open456,global456",
        // Boundary values
        "operator123,2025-08-30T12:00:00.000Z, us1, Operator1, Address1, OpenId1, GlobalId1",
        "operator123,2025-08-30T12:00:00.000Z, LONG, Operator1, Address1, OpenId1, GlobalId1",
        "operator123,2025-08-30T12:00:00.000Z, user1, O, Address1, OpenId1, GlobalId1",
        "operator123,2025-08-30T12:00:00.000Z, user1, LONG,  Address1, OpenId1, GlobalId1",
        "operator123,2025-08-30T12:00:00.000Z, user1, Operator1, Address1, OpenId1, GlobalId1",
        "operator123,2025-08-30T12:00:00.000Z, user1, Operator1, Address1, OpenId1, GlobalId1",
        "operator123,2025-08-30T12:00:00.000Z, user1, Operator1, Address1, OpenId1, GlobalId1",
        "operator123,2025-08-30T12:00:00.000Z, user1, Operator1, Address1, OpenId1, GlobalId1",
        "operator123,2025-08-30T12:00:00.000Z, user1, Operator1, Address1, OpenId1, GlobalId1",
        // Pattern cases
        "operator123,2025-08-30T12:00:00.000Z, az09_.@, Operator1, Address1, OpenId1, GlobalId1",
        "operator123,2025-08-30T12:00:00.000Z, user1, Operator1, Address1, AZaz09, GlobalId1",
        // Null cases, 0 length cases
        "operator123,2025-08-30T12:00:00.000Z, NULL, NULL, NULL, NULL, NULL",
        "operator123,2025-08-30T12:00:00.000Z, NULL, NULL, NULL, NULL, ''"
    })
    @DisplayName("putOperator - Success Case")
    void testPutOperatorSuccess(
            String targetOperatorId,
            String updatedAt,
            String loginUserId,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId) throws Exception {
        // Handle special cases for NULL and LONG inputs
        loginUserId = "NULL".equals(loginUserId) ? null : loginUserId;
        operatorName = "NULL".equals(operatorName) ? null : operatorName;
        operatorAddress = "NULL".equals(operatorAddress) ? null : operatorAddress;
        openOperatorId = "NULL".equals(openOperatorId) ? null : openOperatorId;
        globalOperatorId = "NULL".equals(globalOperatorId) ? null : globalOperatorId;
        loginUserId = "LONG".equals(loginUserId) ? "u".repeat(Const.LOGIN_USER_ID_LENGTH_MAX) : loginUserId;
        operatorName = "LONG".equals(operatorName) ? "O".repeat(Const.OPERATOR_NAME_LENGTH_MAX) : operatorName;
        operatorAddress = "LONG".equals(operatorAddress) ? "A".repeat(Const.OPERATOR_ADDRESS_LENGTH_MAX) : operatorAddress;
        openOperatorId = "LONG".equals(openOperatorId) ? "O".repeat(Const.OPEN_OPERATOR_ID_LENGTH_MAX) : openOperatorId;
        globalOperatorId = "LONG".equals(globalOperatorId) ? "G".repeat(Const.GLOBAL_OPERATOR_ID_LENGTH_MAX) : globalOperatorId;

        // Arrange
        String apiKey = "apiKey123";
        String storeId = "store-uuid";
        String userOperatorId = "user123";
        LocalDateTime updatedDateTime = LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        when(apiKeyService.getUsecaseStoreId(apiKey)).thenReturn(storeId);

        StringBuilder jsonSb = new StringBuilder();
        String rowJson = jsonSb.append("{")
                .append("\"").append(Const.JSON_PROPERTY_UPDATED_AT).append("\":\"").append(updatedAt).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_LOGIN_USER_ID).append("\":\"").append(loginUserId).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_OPERATOR_NAME).append("\":\"").append(operatorName).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_OPERATOR_ADDRESS).append("\":\"").append(operatorAddress).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_OPEN_OPERATOR_ID).append("\":\"").append(openOperatorId).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_GLOBAL_OPERATOR_ID).append("\":\"").append(globalOperatorId).append("\"")
                .append("}").toString();

        PutOperatorRequest requestBody = new PutOperatorRequest(
                updatedAt, loginUserId, operatorName, operatorAddress, openOperatorId, globalOperatorId);
        requestBody.setRawJson(rowJson);

        // Mock the validator to always return no violations
        Validator validator = mock(Validator.class);
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<PutOperatorRequest>> violations = mock(Set.class);
        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(PutOperatorRequest.class))).thenReturn(violations);

        // Inject the mock validator into the controller using reflection
        Field validatorField = OperatorController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(operatorController, validator);

        OperatorResult operatorResult = new OperatorResult();
        operatorResult.setOperatorId(targetOperatorId);
        operatorResult.setOperatorName(operatorName);
        operatorResult.setOperatorAddress(operatorAddress);
        operatorResult.setOpenOperatorId(openOperatorId);
        operatorResult.setGlobalOperatorId(globalOperatorId);
        operatorResult.setUpdatedAt(updatedDateTime);
        IdpAccountInfo idpResult = new IdpAccountInfo(targetOperatorId, loginUserId, null, true);

        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(userOperatorId);
        when(operatorService.updateOperatorWithAuth(eq(targetOperatorId), any(), any(), any(), any(), any(), anyString(), eq(storeId)))
                .thenReturn(operatorResult);
        when(identityProviderService.updateAccount(anyString(), any(), any(), any()))
                .thenReturn(idpResult);

        // Act
        ResponseEntity<APIResponse<Object>> responseEntity = operatorController.putOperator(
                request, targetOperatorId, rowJson);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        APIResponse<Object> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());
        OperatorResponse responseData = objectMapper.convertValue(apiResponse.getData(), OperatorResponse.class);
        assertEquals(targetOperatorId, responseData.getOperatorId());
        assertEquals(operatorName, responseData.getOperatorName());
        assertEquals(operatorAddress, responseData.getOperatorAddress());
        assertEquals(openOperatorId, responseData.getOpenOperatorId());
        assertEquals(globalOperatorId, responseData.getGlobalOperatorId());
        assertEquals(updatedDateTime, responseData.getUpdatedAt());
    }

    /**
     * Test for putOperator - Validation error.
     */
    @ParameterizedTest
    @CsvSource({
        "operator123,2025-08-30T10:15:30,user1,Operator Name,user1@example.com,Address1,open123,global123",
        "operator456,2025-09-01T12:00:00,user2,Another Operator,user2@example.com,Address2,open456,global456"
    })
    @DisplayName("putOperator - Validation Error")
    void testPutOperatorValidationError(
            String targetOperatorId,
            String updatedAt,
            String loginUserId,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId) throws Exception {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);

        StringBuilder jsonSb = new StringBuilder();
        String rowJson = jsonSb.append("{")
                .append("\"").append(Const.JSON_PROPERTY_UPDATED_AT).append("\":\"").append(updatedAt).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_LOGIN_USER_ID).append("\":\"").append(loginUserId).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_OPERATOR_NAME).append("\":\"").append(operatorName).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_OPERATOR_ADDRESS).append("\":\"").append(operatorAddress).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_OPEN_OPERATOR_ID).append("\":\"").append(openOperatorId).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_GLOBAL_OPERATOR_ID).append("\":\"").append(globalOperatorId).append("\"")
                .append("}").toString();
        PutOperatorRequest requestBody = new PutOperatorRequest(
                updatedAt, loginUserId, operatorName, operatorAddress, openOperatorId, globalOperatorId);
        requestBody.setRawJson(rowJson);

        // Mock the validator to always return violations
        Validator validator = mock(Validator.class);
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<PutOperatorRequest>> violations = mock(Set.class);

        // Mock the validator to always return no violations
        when(violations.isEmpty()).thenReturn(false);
        when(validator.validate(any(PutOperatorRequest.class))).thenReturn(violations);

        // Inject the mock validator into the controller using reflection
        Field validatorField = OperatorController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(operatorController, validator);

        // Act & Assert
        ValidateException exception = assertThrows(ValidateException.class, () ->
                operatorController.putOperator(request, targetOperatorId, rowJson)
        );
        assertEquals(ConstError.ERRLOG_400_INVALID_REQUEST, exception.getLogMessage());
    }

    /**
     * Test for putOperator - Exception during update.
     */
    @ParameterizedTest
    @CsvSource({
        "operator123,2025-08-30T10:15:30.000Z,user1,Operator Name,Address1,open123,global123",
        "operator456,2025-09-01T12:00:00.000Z,user2,Another Operator,Address2,open456,global456"
    })
    @DisplayName("putOperator - Exception During Update")
    void testPutOperatorExceptionDuringUpdate(
            String targetOperatorId,
            String updatedAt,
            String loginUserId,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId) throws Exception {
        // Arrange
        String apiKey = "apiKey123";
        String storeId = "store-uuid";
        String userOperatorId = "user123";

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        when(apiKeyService.getUsecaseStoreId(apiKey)).thenReturn(storeId);

        StringBuilder jsonSb = new StringBuilder();
        String rowJson = jsonSb.append("{")
                .append("\"").append(Const.JSON_PROPERTY_UPDATED_AT).append("\":\"").append(updatedAt).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_LOGIN_USER_ID).append("\":\"").append(loginUserId).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_OPERATOR_NAME).append("\":\"").append(operatorName).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_OPERATOR_ADDRESS).append("\":\"").append(operatorAddress).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_OPEN_OPERATOR_ID).append("\":\"").append(openOperatorId).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_GLOBAL_OPERATOR_ID).append("\":\"").append(globalOperatorId).append("\"")
                .append("}").toString();

        PutOperatorRequest requestBody = new PutOperatorRequest(
                updatedAt, loginUserId, operatorName, operatorAddress, openOperatorId, globalOperatorId);
        requestBody.setRawJson(rowJson);

        // Mock the validator to always return violations
        Validator validator = mock(Validator.class);
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<PutOperatorRequest>> violations = mock(Set.class);

        // Mock the validator to always return no violations
        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(PutOperatorRequest.class))).thenReturn(violations);

        // Inject the mock validator into the controller using reflection
        Field validatorField = OperatorController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(operatorController, validator);

        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(userOperatorId);
        doThrow(new UnexpectedException("Service error", ""))
            .when(operatorService).updateOperatorWithAuth(anyString(), any(), any(), any(), any(), any(), anyString(), anyString());

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () ->
                operatorController.putOperator(request, targetOperatorId, rowJson)
        );
        assertEquals("Service error", exception.getMessage());
    }

    /**
     * Test for putOperator when identityProviderService.updateAccount throws an exception.
     */
    @ParameterizedTest
    @CsvSource({
        // Exception
        "BadParametersException",
        "UnauthorizedException",
        "OutOfServiceException",
        "ConflictException",
        "UnexpectedException",
        "RuntimeException"
    })
    @DisplayName("putOperator throws exception when identityProviderService.updateAccount fails")
    void testPutOperatorIdentityProviderServiceThrowsException(String exceptionClassName) throws Exception {
        // Arrange
        String apiKey = "apiKey123";
        String targetOperatorId = "operator123";
        String userOperatorId = "user123";
        String updatedAt = "2025-08-30T10:15:30.000Z";
        String loginUserId = "loginUser";
        String operatorName = "Operator Name";
        String operatorAddress = "123 Main St";
        String openOperatorId = "open123";
        String globalOperatorId = "global123";

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);

        StringBuilder jsonSb = new StringBuilder();
        String rowJson = jsonSb.append("{")
                .append("\"").append(Const.JSON_PROPERTY_UPDATED_AT).append("\":\"").append(updatedAt).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_LOGIN_USER_ID).append("\":\"").append(loginUserId).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_OPERATOR_NAME).append("\":\"").append(operatorName).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_OPERATOR_ADDRESS).append("\":\"").append(operatorAddress).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_OPEN_OPERATOR_ID).append("\":\"").append(openOperatorId).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_GLOBAL_OPERATOR_ID).append("\":\"").append(globalOperatorId).append("\"")
                .append("}").toString();

        PutOperatorRequest requestBody = new PutOperatorRequest(
                updatedAt, loginUserId, operatorName, operatorAddress, openOperatorId, globalOperatorId);
        requestBody.setRawJson(rowJson);

        // Mock the validator to always return violations
        Validator validator = mock(Validator.class);
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<PutOperatorRequest>> violations = mock(Set.class);

        // Mock the validator to always return no violations
        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(PutOperatorRequest.class))).thenReturn(violations);

        // Inject the mock validator into the controller using reflection
        Field validatorField = OperatorController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(operatorController, validator);

        OperatorResult operatorResult = new OperatorResult();
        operatorResult.setOperatorId(targetOperatorId);
        operatorResult.setOperatorName(operatorName);
        operatorResult.setOperatorAddress(operatorAddress);
        operatorResult.setOpenOperatorId(openOperatorId);
        operatorResult.setGlobalOperatorId(globalOperatorId);

        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(userOperatorId);
        when(operatorService.updateOperatorWithAuth(anyString(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(operatorResult);

        // Prepare exception to be thrown by identityProviderService.updateAccount
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
            case "ConflictException":
                exceptionLogMessage = String.format(ConstError.ERR_409_CONFLICT, "dummy param", "dummy message");
                exceptionResponseMessage = ConstError.ERR_409;
                exception = new ConflictException(exceptionLogMessage, exceptionResponseMessage);
                clazz = ConflictException.class;
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
        doThrow(exception).when(identityProviderService).updateAccount(any(), any(), any(), any());

        // Act & Assert
        Throwable thrownException = assertThrows(clazz, () -> {
            operatorController.putOperator(request, targetOperatorId, rowJson);
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
        verify(violations).isEmpty();
        verify(jwtService).getOperatorOrOpenSystemId(any(HttpServletRequest.class));
        verify(operatorService).updateOperatorWithAuth(anyString(), any(), any(), any(), any(), any(), any(), any());
        verify(identityProviderService).updateAccount(any(), any(), any(), any());
    }

    /**
     * Test for putOperatorStatus - Success case.
     */
    @ParameterizedTest
    @CsvSource({
        "operator123, 2025-08-30T10:15:30.000Z, 2020-01-01, 2030-12-31, true",
        "operator123, 2025-08-30T10:15:30.000Z, 2020-01-01, 2030-12-31, false",
        // Boundary values
        // -- no pattern --
        // Pattern cases
        // -- no pattern --
        // Null cases, 0 length cases
        "operator123,2025-08-30T12:00:00.000Z, NULL, NULL, NULL",
    })
    @DisplayName("putOperatorStatus - Success Case")
    void testPutOperatorStatusSuccess(
            String targetOperatorId,
            String updatedAt,
            String strEffectiveStartDate,
            String strEffectiveEndDate,
            String strDeletedFlag
    ) throws Exception {
        // Handle special cases for NULL and LONG inputs
        strEffectiveStartDate = "NULL".equals(strEffectiveStartDate) ? null : strEffectiveStartDate;
        strEffectiveEndDate = "NULL".equals(strEffectiveEndDate) ? null : strEffectiveEndDate;
        strDeletedFlag = "NULL".equals(strDeletedFlag) ? null : strDeletedFlag;

        LocalDate resultEffectiveStartDate = strEffectiveStartDate == null ? LocalDate.now(ZoneOffset.UTC) : LocalDate.parse(strEffectiveStartDate);
        LocalDate resultEffectiveEndDate = strEffectiveEndDate == null ? LocalDate.now(ZoneOffset.UTC) : LocalDate.parse(strEffectiveEndDate);
        boolean resultDeletedFlag = strDeletedFlag == null ? false : Boolean.valueOf(strDeletedFlag);

        // Arrange
        String apiKey = "apiKey123";
        String storeId = "store-uuid";
        String userOperatorId = "user123";

        LocalDateTime updatedDateTime = LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        when(apiKeyService.getUsecaseStoreId(apiKey)).thenReturn(storeId);

        PutOperatorStatusRequest requestBody = new PutOperatorStatusRequest(
                updatedAt, strEffectiveStartDate, strEffectiveEndDate, strDeletedFlag);

        OperatorResult operatorResult = new OperatorResult();
        operatorResult.setOperatorId(targetOperatorId);
        operatorResult.setDeletedFlag(resultDeletedFlag);
        operatorResult.setEffectiveStartDate(resultEffectiveStartDate);
        operatorResult.setEffectiveEndDate(resultEffectiveEndDate);
        operatorResult.setUpdatedAt(updatedDateTime);
        IdpAccountInfo idpResult = new IdpAccountInfo(targetOperatorId, null, null, null, true);

        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(userOperatorId);
        when(operatorService.updateOperatorStatusWithAuth(anyString(), any(), any(), any(), any(), anyString(), any()))
                .thenReturn(operatorResult);
        when(identityProviderService.updateAccountStatus(anyString(), any(), any()))
                .thenReturn(idpResult);

        // Act
        ResponseEntity<APIResponse<Object>> responseEntity = operatorController.putOperatorStatus(
                request, targetOperatorId, requestBody, bindingResult);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        APIResponse<Object> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());

        OperatorResponse responseData = objectMapper.convertValue(apiResponse.getData(), OperatorResponse.class);
        assertEquals(targetOperatorId, responseData.getOperatorId());
        assertEquals(resultEffectiveStartDate, responseData.getEffectiveStartDate());
        assertEquals(resultEffectiveEndDate, responseData.getEffectiveEndDate());
        assertEquals(resultDeletedFlag, responseData.isDeletedFlag());
    }

    /**
     * Test for putOperator when identityProviderService.updateAccount throws an exception.
     */
    @ParameterizedTest
    @CsvSource({
        // Exception
        "BadParametersException",
        "UnauthorizedException",
        "OutOfServiceException",
        "ConflictException",
        "UnexpectedException",
        "RuntimeException"
    })
    @DisplayName("putOperatorStatus throws exception when identityProviderService.updateAccount fails")
    void testPutOperatorStatusServiceThrowsException(String exceptionClassName) throws Exception {
        // Arrange
        String apiKey = "apiKey123";
        String targetOperatorId = "operator123";
        String userOperatorId = "user123";
        String updatedAt = "2025-08-30T10:15:30.000Z";
        String effectiveStartDate = "2020-01-01";
        String effectiveEndDate = "2030-12-31";
        String deletedFlag = "true";

        when(bindingResult.hasErrors()).thenReturn(false);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        PutOperatorStatusRequest requestBody = new PutOperatorStatusRequest(
                updatedAt, effectiveStartDate, effectiveEndDate, deletedFlag
        );

        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(userOperatorId);

        // Prepare exception to be thrown by identityProviderService.updateAccount
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
            case "ConflictException":
                exceptionLogMessage = String.format(ConstError.ERR_409_CONFLICT, "dummy parameter", "dummy message");
                exceptionResponseMessage = ConstError.ERR_409;
                exception = new ConflictException(exceptionLogMessage, exceptionResponseMessage);
                clazz = ConflictException.class;
                break;
            case "UnexpectedException":
                exceptionLogMessage = String.format(ConstError.ERRLOG_500_ERROR, "dummy parameter", "dummy message");
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

        // Mock operatorService to throw an exception
        doThrow(exception).when(operatorService).updateOperatorStatusWithAuth(anyString(), any(), any(), any(), any(), any(), any());

        // Act & Assert
        Throwable thrownException = assertThrows(clazz, () -> {
            operatorController.putOperatorStatus(request, targetOperatorId, requestBody, bindingResult);
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
        verify(jwtService).getOperatorOrOpenSystemId(any(HttpServletRequest.class));
        verify(operatorService).updateOperatorStatusWithAuth(anyString(), any(), any(), any(), any(), any(), any());
    }

    /**
     * Test for postOperator - Success case.
     */
    @ParameterizedTest
    @CsvSource({
        "user1,Operator1,user1@example.com,Address1,OPENID,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user2,Operator2,user2@example.com,Address2,openid2,GlobalId2,2025-09-01,2025-12-31,false,true",
        // Length boundary cases
        "us1,Operator1,user1@example.com,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false",
        "LONG,Operator1,user1@example.com,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user1,o,user1@example.com,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user1,LONG,user1@example.com,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user1,Operator1,LONG,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user1,Operator1,user1@example.com,A,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user1,Operator1,user1@example.com,LONG,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user1,Operator1,user1@example.com,Address1,0,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user1,Operator1,user1@example.com,Address1,01234567890123456789,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user1,Operator1,user1@example.com,Address1,OpenId1,LONG,2025-08-30,2025-12-31,true,false",
        // Pattern boundary cases
        "az09_.@,Operator1,user1@example.com,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user1,Operator1,azAZ09._%+-@azAZ09.-.co,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user1,Operator1,user1@example.com,Address1,OpenId1,azAZ09,2025-08-30,2025-12-31,true,false",
        "user1,Operator1,user1@example.com,Address1,OpenId1,GlobalId1,0001-01-01,2025-12-31,true,false",
        "user1,Operator1,user1@example.com,Address1,OpenId1,GlobalId1,2025-08-30,9999-12-31,true,false",
        // Not required values
        "user1,Operator1,NULL,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user1,Operator1,'',Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user1,Operator1,user1@example.com,Address1,OpenId1,NULL,2025-08-30,2025-12-31,true,false",
        "user1,Operator1,user1@example.com,Address1,OpenId1,'',2025-08-30,2025-12-31,true,false",
        "user1,Operator1,user1@example.com,Address1,OpenId1,' ',2025-08-30,2025-12-31,true,false" })
    @DisplayName("postOperator - Success Case")
    void testPostOperatorSuccess(
            String loginUserId,
            String operatorName,
            String email,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId,
            String startDate,
            String endDate,
            String createPasswordFlag,
            String passwordTemporaryFlag) throws Exception {
        // Arrange
        String apiKey = "apiKey123";
        String creatorOperatorId = "creator123";
        String operatorId = "user123";
        String password = "password123";
        LocalDateTime now = LocalDateTime.now();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);

        PostOperatorRequest requestBody = new PostOperatorRequest(
                loginUserId, operatorName, operatorAddress,
                openOperatorId, globalOperatorId, startDate, endDate, createPasswordFlag, passwordTemporaryFlag
        );

        // Handle "NULL", empty, and "LONG" inputs
        email = "NULL".equals(email) || email.isEmpty() ? null : email;
        globalOperatorId = "NULL".equals(globalOperatorId) || globalOperatorId.isEmpty() ? null : globalOperatorId;
        operatorAddress = "NULL".equals(operatorAddress) || operatorAddress.isEmpty() ? null : operatorAddress;
        loginUserId = "LONG".equals(loginUserId) ? "a".repeat(255) : loginUserId;
        operatorName = "LONG".equals(operatorName) ? "a".repeat(255) : operatorName;
        email = "LONG".equals(email) ? "a".repeat(249) + "@a.com" : email;
        operatorAddress = "LONG".equals(operatorAddress) ? "a".repeat(256) : operatorAddress;
        globalOperatorId = "LONG".equals(globalOperatorId) ? "a".repeat(255) : globalOperatorId;

        IdpAccountInfo idpResult = new IdpAccountInfo(operatorId, loginUserId, email, password, true);
        OperatorResult operatorResult = new OperatorResult();
        operatorResult.setOperatorId(operatorId);
        operatorResult.setOperatorName(operatorName);
        operatorResult.setOperatorAddress(operatorAddress);
        operatorResult.setOpenOperatorId(openOperatorId);
        operatorResult.setGlobalOperatorId(globalOperatorId);
        operatorResult.setDeletedFlag(false);
        operatorResult.setEffectiveStartDate(LocalDate.parse(startDate));
        operatorResult.setEffectiveEndDate(LocalDate.parse(endDate));
        operatorResult.setCreatedAt(now);
        operatorResult.setUpdatedAt(now);
        operatorResult.setCreatedUserId(creatorOperatorId);
        operatorResult.setUpdatedUserId(creatorOperatorId);

        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(creatorOperatorId);
        when(identityProviderService.createAccount(eq(apiKey), any(), any(), anyBoolean(), anyBoolean()))
                .thenReturn(idpResult);
        when(operatorService.addOperatorWithAuth(
            anyString(), anyString(), anyString(), anyString(), any(), any(), any(), any(), any()
        )).thenReturn(operatorResult);

        // Act
        ResponseEntity<APIResponse<PostOperatorResponse>> responseEntity = operatorController.postOperator(
                request, requestBody, mock(BindingResult.class));

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        APIResponse<PostOperatorResponse> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.CREATED.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());
        assertEquals(operatorId, apiResponse.getData().getOperatorId());
        assertEquals(password, apiResponse.getData().getPassword());
        assertEquals(operatorName, apiResponse.getData().getOperatorName());
        assertEquals(operatorAddress, apiResponse.getData().getOperatorAddress());
        assertEquals(openOperatorId, apiResponse.getData().getOpenOperatorId());
        assertEquals(globalOperatorId, apiResponse.getData().getGlobalOperatorId());
        assertEquals(startDate, apiResponse.getData().getEffectiveStartDate().toString());
        assertEquals(endDate, apiResponse.getData().getEffectiveEndDate().toString());
        assertFalse(apiResponse.getData().isDeletedFlag());
        assertNotNull(apiResponse.getData().getCreatedAt());
        assertNotNull(apiResponse.getData().getUpdatedAt());
    }

    /**
     * Test for postOperator - Success case without not required values.
     */
    @Test
    @DisplayName("postOperator - Success case without not required values")
    void testPostOperatorSuccessWithoutNotRequired() throws Exception {
        // Arrange
        String apiKey = "apiKey123";
        String storeId = "test-store-id";
        String creatorOperatorId = "creator123";
        String loginUserId = "loginuserid";
        String operatorName = "Operator Name";
        String operatorAddress = "123 Main St";
        String openOperatorId = "open123";
        String operatorId = "user123";
        String password = "password123";
        String endDate = "9999-12-31";
        boolean deletedFlag = false;
        LocalDateTime now = LocalDateTime.now();
        LocalDate nowDate = now.toLocalDate();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        when(apiKeyService.getUsecaseStoreId(apiKey)).thenReturn(storeId);

        PostOperatorRequest requestBody = new PostOperatorRequest();
        requestBody.setLoginUserId(loginUserId);
        requestBody.setOperatorName(operatorName);
        requestBody.setOperatorAddress(operatorAddress);
        requestBody.setOpenOperatorId(openOperatorId);

        IdpAccountInfo idpResult = new IdpAccountInfo(operatorId, loginUserId, null, password, true);
        OperatorResult operatorResult = new OperatorResult();
        operatorResult.setOperatorId(operatorId);
        operatorResult.setOperatorName(operatorName);
        operatorResult.setOperatorAddress(operatorAddress);
        operatorResult.setOpenOperatorId(openOperatorId);
        operatorResult.setGlobalOperatorId(null);
        operatorResult.setDeletedFlag(deletedFlag);
        operatorResult.setEffectiveStartDate(nowDate);
        operatorResult.setEffectiveEndDate(LocalDate.parse(endDate));
        operatorResult.setCreatedAt(now);
        operatorResult.setUpdatedAt(now);
        operatorResult.setCreatedUserId(creatorOperatorId);
        operatorResult.setUpdatedUserId(creatorOperatorId);

        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(creatorOperatorId);
        when(identityProviderService.createAccount(eq(apiKey), any(), any(), anyBoolean(), anyBoolean()))
                .thenReturn(idpResult);
        when(operatorService.addOperatorWithAuth(
            anyString(), anyString(), anyString(), anyString(), any(), any(), any(), any(), any()
        )).thenReturn(operatorResult);

        // Act
        ResponseEntity<APIResponse<PostOperatorResponse>> responseEntity = operatorController.postOperator(
                request, requestBody, bindingResult);

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        APIResponse<PostOperatorResponse> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.CREATED.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());
        assertEquals(operatorId, apiResponse.getData().getOperatorId());
        assertEquals(password, apiResponse.getData().getPassword());
        assertEquals(operatorName, apiResponse.getData().getOperatorName());
        assertEquals(operatorAddress, apiResponse.getData().getOperatorAddress());
        assertEquals(openOperatorId, apiResponse.getData().getOpenOperatorId());
        assertEquals(null, apiResponse.getData().getGlobalOperatorId());
        assertEquals(nowDate.toString(), apiResponse.getData().getEffectiveStartDate().toString());
        assertEquals(endDate, apiResponse.getData().getEffectiveEndDate().toString());
        assertFalse(apiResponse.getData().isDeletedFlag());
        assertNotNull(apiResponse.getData().getCreatedAt());
        assertNotNull(apiResponse.getData().getUpdatedAt());
    }

    /**
     * Test for postOperator - Validation error.
     */
    @Test
    @DisplayName("postOperator - Validation Error")
    void testPostOperatorValidationError() throws Exception {
        // Arrange
        PostOperatorRequest requestBody = new PostOperatorRequest();
        when(bindingResult.hasErrors()).thenReturn(true);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn("apiKey123");

        // Act & Assert
        ValidateException exception = assertThrows(ValidateException.class, () ->
                operatorController.postOperator(request, requestBody, bindingResult)
        );
        assertEquals(ConstError.ERRLOG_400_INVALID_REQUEST, exception.getLogMessage());
        assertNotNull(exception.getMessage());
    }

    /**
     * Test for postOperator - Exception during operator creation.
     */
    @Test
    @DisplayName("postOperator - Exception During Operator Creation")
    void testPostOperatorExceptionDuringOperatorCreation() throws Exception {
        // Arrange
        String apiKey = "apiKey123";
        String creatorOperatorId = "creator123";
        String operatorId = "user123";

        when(bindingResult.hasErrors()).thenReturn(false);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn("apiKey123");
        PostOperatorRequest requestBody = new PostOperatorRequest(
                "loginUserId", "Operator Name", "123 Main St",
                "open123", "global123", "2025-08-30", "2025-12-31", "true", "false"
        );

        IdpAccountInfo idpResult = new IdpAccountInfo(operatorId, "loginUserId", "user@example.com",  null, true);

        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(creatorOperatorId);
        when(identityProviderService.createAccount(eq(apiKey), anyString(), any(), anyBoolean(), anyBoolean()))
                .thenReturn(idpResult);
        doNothing().when(identityProviderService).deleteAccount(apiKey, operatorId);
        doThrow(new RuntimeException("Service error"))
                .when(operatorService).addOperatorWithAuth(anyString(), anyString(), anyString(),
                anyString(), any(), any(), any(), any(), any());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                operatorController.postOperator(request, requestBody, bindingResult)
        );
        assertEquals("Service error", exception.getMessage());
        verify(bindingResult).hasErrors();
        verify(jwtService).getOperatorOrOpenSystemId(any(HttpServletRequest.class));
        verify(identityProviderService).createAccount(eq(apiKey), anyString(), any(), anyBoolean(), anyBoolean());
        verify(operatorService).addOperatorWithAuth(anyString(), anyString(), anyString(), anyString(), any(), any(), any(), any(), any());
        verify(identityProviderService).deleteAccount(apiKey, operatorId);
    }

    /**
     * Test for postOperator when identityProviderService.deleteAccount throws an exception.
     */
    @ParameterizedTest
    @CsvSource({
        // Exception
        "BadParametersException",
        "UnauthorizedException",
        "OutOfServiceException",
        "ConflictException",
        "UnexpectedException",
        "RuntimeException"
    })
    @DisplayName("postOperator throws exception when identityProviderService fails")
    void testPostOperatorIdentityProviderServiceThrowsException(String exceptionClassName) {
        // Arrange
        String apiKey = "apiKey123";
        String creatorOperatorId = "creator123";
        String operatorId = "user123";

        when(bindingResult.hasErrors()).thenReturn(false);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn("apiKey123");
        PostOperatorRequest requestBody = new PostOperatorRequest(
                "loginUserId", "Operator Name", "123 Main St",
                "open123", "global123", "2025-08-30", "2025-12-31", "true", "false"
        );

        IdpAccountInfo idpResult = new IdpAccountInfo(operatorId, "loginUserId", "user@example.com", null, true);

        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(creatorOperatorId);
        when(identityProviderService.createAccount(eq(apiKey), anyString(), any(), anyBoolean(), anyBoolean()))
                .thenReturn(idpResult);
        doNothing().when(identityProviderService).deleteAccount(apiKey, operatorId);
        doThrow(new RuntimeException("Service error"))
            .when(operatorService).addOperatorWithAuth(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(java.time.LocalDate.class), org.mockito.ArgumentMatchers.any(java.time.LocalDate.class), eq(creatorOperatorId), org.mockito.Mockito.any());

        // Prepare exception to be thrown by identityProviderService.deleteAccount
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
            case "ConflictException":
                exceptionLogMessage = String.format(ConstError.ERR_409_CONFLICT, "email", "user@example.com");
                exceptionResponseMessage = ConstError.ERR_409;
                exception = new ConflictException(exceptionLogMessage, exceptionResponseMessage);
                clazz = ConflictException.class;
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
        doThrow(exception).when(identityProviderService).deleteAccount(apiKey, operatorId);

        // Act & Assert
        Throwable thrownException = assertThrows(clazz, () -> {
            operatorController.postOperator(request, requestBody, bindingResult);
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
        verify(jwtService).getOperatorOrOpenSystemId(any(HttpServletRequest.class));
        verify(identityProviderService).createAccount(eq(apiKey), anyString(), any(), anyBoolean(), anyBoolean());
        verify(operatorService).addOperatorWithAuth(anyString(), anyString(), anyString(), anyString(), anyString(), any(), any(), any(), any());
        verify(identityProviderService).deleteAccount(apiKey, operatorId);
    }

    /**
     * Test for postOperator - Operator creation succeeds but Tuple write fails.
     */
    @Test
    @DisplayName("postOperator - Tuple write failure should fail creation and should be reported")
    void testPostOperatorTupleWriteFailureReported() throws Exception {
        // Arrange
        String apiKey = "apiKey123";
        String creatorOperatorId = "creator123";
        String operatorId = "user123";
        String password = "password123";
        LocalDateTime now = LocalDateTime.now();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);

        PostOperatorRequest requestBody = new PostOperatorRequest();
        requestBody.setLoginUserId("loginUserId");
        requestBody.setOperatorName("Operator Name");
        requestBody.setOperatorAddress("123 Main St");
        requestBody.setOpenOperatorId("open123");

        IdpAccountInfo idpResult = new IdpAccountInfo(operatorId, "loginUserId", null, password, true);
        OperatorResult operatorResult = new OperatorResult();
        operatorResult.setOperatorId(operatorId);
        operatorResult.setOperatorName("Operator Name");
        operatorResult.setOperatorAddress("123 Main St");
        operatorResult.setOpenOperatorId("open123");
        operatorResult.setGlobalOperatorId(null);
        operatorResult.setDeletedFlag(false);
        operatorResult.setEffectiveStartDate(now.toLocalDate());
        operatorResult.setEffectiveEndDate(now.toLocalDate());
        operatorResult.setCreatedAt(now);
        operatorResult.setUpdatedAt(now);
        operatorResult.setCreatedUserId(creatorOperatorId);
        operatorResult.setUpdatedUserId(creatorOperatorId);

        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(creatorOperatorId);
        when(identityProviderService.createAccount(eq(apiKey), any(), any(), anyBoolean(), anyBoolean()))
                .thenReturn(idpResult);
        // Simulate service failure (e.g., tuple write failure inside the service) so that transaction rolls back
        doThrow(new RuntimeException("forward failed"))
            .when(operatorService).addOperatorWithAuth(anyString(), anyString(), anyString(), anyString(), any(), any(), any(), any(), any());

        // Act & Assert: controller should delete the created IDP account and rethrow
        Throwable thrown = assertThrows(RuntimeException.class, () -> {
            operatorController.postOperator(request, requestBody, mock(BindingResult.class));
        });
        assertEquals("forward failed", thrown.getMessage());

        // Verify that the controller attempted to clean up created IDP account
        verify(identityProviderService).deleteAccount(eq(apiKey), eq(idpResult.getUserId()));
    }

    /**
     * Test for putOperatorStatus call WithAuth.
     */
    @Test
    @DisplayName("putOperatorStatus - WithAuth Call")
    void testPutOperatorStatusWithAuthCall() throws Exception {
        // Arrange
        String apiKey = "apiKey123";
        String userOperatorId = "user123";
        String targetOperatorId = "operator123";
        String updatedAt = "2025-08-30T10:15:30.000Z";
        String strEffectiveStartDate = "2020-01-01";
        String strEffectiveEndDate = "2030-12-31";
        String strDeletedFlag = "false";
        PutOperatorStatusRequest requestBody = new PutOperatorStatusRequest(updatedAt, strEffectiveStartDate, strEffectiveEndDate, strDeletedFlag);

        OperatorResult operatorResult = new OperatorResult();
        operatorResult.setOperatorId(targetOperatorId);
        operatorResult.setDeletedFlag(false);
        operatorResult.setEffectiveStartDate(LocalDate.parse(strEffectiveStartDate));
        operatorResult.setEffectiveEndDate(LocalDate.parse(strEffectiveEndDate));
        operatorResult.setUpdatedAt(LocalDateTime.parse("2025-08-30T10:15:30"));
        IdpAccountInfo idpResult = new IdpAccountInfo(targetOperatorId, null, null, null, true);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(userOperatorId);
        when(operatorService.updateOperatorStatusWithAuth(anyString(), any(), any(), any(), any(), any(), any()))
                .thenReturn(operatorResult);
        when(identityProviderService.updateAccountStatus(anyString(), any(), any())).thenReturn(idpResult);

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        // Act
        ResponseEntity<APIResponse<Object>> responseEntity = operatorController.putOperatorStatus(request, targetOperatorId, requestBody, bindingResult);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        APIResponse<Object> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());
        OperatorResponse responseData = objectMapper.convertValue(apiResponse.getData(), OperatorResponse.class);
        assertEquals(targetOperatorId, responseData.getOperatorId());

        verify(operatorService, times(1)).updateOperatorStatusWithAuth(anyString(), any(), any(), any(), any(), any(), any());
    }

    /**
     * Test for listOperator - Not required values.
     */
    @Test
    @DisplayName("testListOperator - Not required values")
    void testListOperator_notRequiredValues() throws Exception {
        // Specify only the required fields
        ListOperatorRequest listOperatorRequest = new ListOperatorRequest();
        listOperatorRequest.setCount(commonCount);
        listOperatorRequest.setIndex(commonIndex);
        // Convert request object to JSON string
        String rawJson = objectMapper.writeValueAsString(listOperatorRequest);

        List<OperatorResult> listOperatorResult = Collections.emptyList();
        HttpServletRequest request = mock(HttpServletRequest.class);

        // Mock the validator to always return no violations
        Validator validator = mock(Validator.class);
        // Suppress warnings for unchecked casts
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<ListOperatorRequest>> violations = mock(Set.class);
        // Inject the mock validator into the controller using reflection
        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(ListOperatorRequest.class))).thenReturn(violations);
        Field validatorField = OperatorController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(operatorController, validator);

        when(operatorService.searchOperatorsWithAuth(
            anyInt(), anyInt(), isNull(), isNull(),
            isNull(), isNull(), isNull(), isNull(),
            any(StateString.class), isNull(), isNull(),
            org.mockito.ArgumentMatchers.<String>nullable(String.class), org.mockito.ArgumentMatchers.<String>nullable(String.class)
        )).thenReturn(listOperatorResult);

        // Act
        ResponseEntity<APIResponse<List<GetOperatorResponse>>> responseEntity = operatorController.listOperator(request, rawJson);

        // Assert
        verify(validator).validate(any(ListOperatorRequest.class));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        APIResponse<List<GetOperatorResponse>> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertTrue(EnumResponseTypes.getResponseType(HttpStatus.OK.value()).equals(apiResponse.getType()));
        assertNotNull(apiResponse.getData());
        assertTrue(apiResponse.getData().isEmpty(), "Data list should be empty");

        verify(operatorService).searchOperatorsWithAuth(
                anyInt(), anyInt(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(),
                any(StateString.class), isNull(), isNull(),
                org.mockito.ArgumentMatchers.<String>nullable(String.class), org.mockito.ArgumentMatchers.<String>nullable(String.class)
        );
    }

    /**
     * case#6:
     * Test for listOperator when no operators are found.
     */
    @Test
    void testListOperator_returnsEmpty() throws Exception {
        // Arrange
        ListOperatorRequest listOperatorRequest = new ListOperatorRequest();
        listOperatorRequest.setCount(commonCount);
        listOperatorRequest.setIndex(commonIndex);
        listOperatorRequest.setSort(new ListOperatorRequest.SortKey(commonSortKey, commonSortOrder));
        listOperatorRequest.setOperatorId(commonOperatorId);
        listOperatorRequest.setOperatorName(commonOperatorName);
        listOperatorRequest.setOperatorAddress(commonOperatorAddress);
        listOperatorRequest.setOpenOperatorId(commonOpenOperatorId);
        listOperatorRequest.setGlobalOperatorId(commonGlobalOperatorId);
        listOperatorRequest.setEffectiveDate(commonEffectiveDate);
        listOperatorRequest.setDeletedFlag(commonDeletedFlag);
        // Convert request object to JSON string
        String rawJson = objectMapper.writeValueAsString(listOperatorRequest);

        List<OperatorResult> listOperatorResult = Collections.emptyList();

        // Mock the validator to always return no violations
        Validator validator = mock(Validator.class);
        // Suppress warnings for unchecked casts
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<ListOperatorRequest>> violations = mock(Set.class);

        // Inject the mock validator into the controller using reflection
        Field validatorField = OperatorController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(operatorController, validator);
        // Arrange
        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(ListOperatorRequest.class))).thenReturn(violations);
        when(operatorService.searchOperatorsWithAuth(
            anyInt(), anyInt(), anyString(), anyString(),
            anyString(), anyString(), anyString(), anyString(),
            any(StateString.class), anyBoolean(), any(LocalDate.class),
            org.mockito.ArgumentMatchers.<String>nullable(String.class), org.mockito.ArgumentMatchers.<String>nullable(String.class)
        )).thenReturn(listOperatorResult);

        // Act
        ResponseEntity<APIResponse<List<GetOperatorResponse>>> response = operatorController.listOperator(
                httpServletRequest, rawJson);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        APIResponse<List<GetOperatorResponse>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertNotNull(apiResponse.getData());
        assertEquals(0, apiResponse.getData().size());
        verify(operatorService, times(1)).searchOperatorsWithAuth(
                anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(StateString.class), anyBoolean(), any(LocalDate.class),
                org.mockito.ArgumentMatchers.<String>nullable(String.class), org.mockito.ArgumentMatchers.<String>nullable(String.class)
        );
    }

    /**
     * Test for listOperator - Validation error.
     */
    @Test
    @DisplayName("listOperator - Validation Error")
    void testListOperator_validationError() throws Exception {
        // Arrange
        ListOperatorRequest listOperatorRequest = new ListOperatorRequest();
        listOperatorRequest.setCount(commonCount);
        listOperatorRequest.setIndex(commonIndex);
        listOperatorRequest.setSort(new ListOperatorRequest.SortKey(commonSortKey, commonSortOrder));
        listOperatorRequest.setOperatorId(commonOperatorId);
        listOperatorRequest.setOperatorName(commonOperatorName);
        listOperatorRequest.setOperatorAddress(commonOperatorAddress);
        listOperatorRequest.setOpenOperatorId(commonOpenOperatorId);
        listOperatorRequest.setGlobalOperatorId(commonGlobalOperatorId);
        listOperatorRequest.setEffectiveDate(commonEffectiveDate);
        listOperatorRequest.setDeletedFlag(commonDeletedFlag);
        // Convert request object to JSON string
        String rawJson = objectMapper.writeValueAsString(listOperatorRequest);
        HttpServletRequest request = mock(HttpServletRequest.class);

        // Mock the validator to always return violations
        Validator validator = mock(Validator.class);
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<ListOperatorRequest>> violations = mock(Set.class);

        // Mock the validator to always return no violations
        when(violations.isEmpty()).thenReturn(false);
        when(validator.validate(any(ListOperatorRequest.class))).thenReturn(violations);

        // Inject the mock validator into the controller using reflection
        Field validatorField = OperatorController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(operatorController, validator);

        // Act & Assert
        ValidateException exception = assertThrows(ValidateException.class, () ->
                operatorController.listOperator(request, rawJson)
        );
        assertEquals(ConstError.ERRLOG_400_INVALID_REQUEST, exception.getLogMessage());
        assertNotNull(exception.getMessage());
    }

    /**
     * Parameterized test for controller failures.
     */
    @ParameterizedTest
    @CsvSource({
            // Exception
            "BadParametersException", // case#8: Invalid parameters
            "UnauthorizedException", // case#9: Authentication error
            "OutOfServiceException", // case#10: Service unavailable
            "UnexpectedException", // case#11: Unexpected error
            "RuntimeException", // case#12: General runtime error
            "IllegalArgumentException" // case#13: Illegal argument
    })
    void testListOperator_throwException(String exceptionClassName) throws Exception {
        Exception exception = null;
        Class<? extends Throwable> clazz = null;
        // Arrange
        switch (exceptionClassName) {
            case "BadParametersException":
                exception = new BadParametersException("dummmyLogMessage", "dummyResponseMessage");
                clazz = BadParametersException.class;
                break;
            case "UnauthorizedException":
                exception = new UnauthorizedException("dummmyLogMessage", "dummyResponseMessage");
                clazz = UnauthorizedException.class;
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
            case "IllegalArgumentException":
                exception = new IllegalArgumentException("dummyMessage");
                clazz = IllegalArgumentException.class;
                break;
            default:
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }
        when(operatorService.searchOperatorsWithAuth(
            anyInt(), anyInt(), anyString(), anyString(),
            anyString(), anyString(), anyString(), anyString(),
            any(StateString.class), anyBoolean(), org.mockito.ArgumentMatchers.<LocalDate>nullable(LocalDate.class),
            org.mockito.ArgumentMatchers.<String>nullable(String.class), org.mockito.ArgumentMatchers.<String>nullable(String.class)
        )).thenThrow(exception);

        ListOperatorRequest listOperatorRequest = new ListOperatorRequest();
        listOperatorRequest.setCount(commonCount);
        listOperatorRequest.setIndex(commonIndex);
        listOperatorRequest.setSort(new ListOperatorRequest.SortKey(commonSortKey, commonSortOrder));
        listOperatorRequest.setOperatorId(commonOperatorId);
        listOperatorRequest.setOperatorName(commonOperatorName);
        listOperatorRequest.setOperatorAddress(commonOperatorAddress);
        listOperatorRequest.setOpenOperatorId(commonOpenOperatorId);
        listOperatorRequest.setGlobalOperatorId(commonGlobalOperatorId);
        listOperatorRequest.setEffectiveDate(commonEffectiveDate);
        listOperatorRequest.setDeletedFlag(commonDeletedFlag);
        // Convert request object to JSON string
        String rawJson = objectMapper.writeValueAsString(listOperatorRequest);
        HttpServletRequest request = mock(HttpServletRequest.class);

        // Mock the validator to always return no violations
        Validator validator = mock(Validator.class);
        // Suppress warnings for unchecked casts
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<ListOperatorRequest>> violations = mock(Set.class);

        // Inject the mock validator into the controller using reflection
        Field validatorField = OperatorController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(operatorController, validator);

        // Prepare exception to be thrown by identityProviderService.deleteAccount
        switch (exceptionClassName) {
            case "BadParametersException":
                exception = new BadParametersException("dummmyLogMessage", "dummyResponseMessage");
                clazz = BadParametersException.class;
                break;
            case "UnauthorizedException":
                exception = new UnauthorizedException("dummmyLogMessage", "dummyResponseMessage");
                clazz = UnauthorizedException.class;
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
            case "IllegalArgumentException":
                exception = new IllegalArgumentException("dummyMessage");
                clazz = IllegalArgumentException.class;
                break;
            default:
                // Unexpected exception
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }
        // Arrange common mock behaviors
        when(operatorService.searchOperatorsWithAuth(
                anyInt(), anyInt(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                any(StateString.class), anyBoolean(), any(LocalDate.class),
                anyString(), anyString()
        )).thenThrow(exception);
        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(ListOperatorRequest.class))).thenReturn(violations);

        // Act & Assert
        Throwable thrownException = assertThrows(clazz, () -> {
            operatorController.listOperator(request, rawJson);;
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
        verify(operatorService).searchOperatorsWithAuth(
                anyInt(), anyInt(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                any(StateString.class), anyBoolean(), org.mockito.ArgumentMatchers.<LocalDate>nullable(LocalDate.class),
                org.mockito.ArgumentMatchers.<String>nullable(String.class), org.mockito.ArgumentMatchers.<String>nullable(String.class)
        );
    }

    // --- OpenFGA / authorization failure tests ---
    /**
     * Test for getOperator - Authorization false.
     */
    @Test
    void testGetOperatorAuthDenied() throws Exception {
        String operatorId = "op-deny";
        String userId = "test-user";
        String storeId = "test-store";
        when(jwtService.getOperatorOrOpenSystemId(httpServletRequest)).thenReturn(userId);
        // Controller reads the API key header and resolves storeId via apiKeyService
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn("apiKey123");
        when(apiKeyService.getUsecaseStoreId(eq("apiKey123"))).thenReturn(storeId);
        when(operatorService.getOperatorWithAuth(eq(operatorId), eq(userId), eq(storeId)))
                .thenThrow(new IllegalAuthDataException("forbidden", "Forbidden"));

        IllegalAuthDataException ex = assertThrows(
                IllegalAuthDataException.class,
                () -> operatorController.getOperator(httpServletRequest, operatorId)
        );
        assertNotNull(ex.getResponseMessage());
    }

    /**
     * Test for getOperator - OpenFGA error.
     */
    @Test
    void testGetOperatorAuthServiceError() throws Exception {
        String operatorId = "op-error";
        String userId = "test-user";
        String storeId = "test-store";
        when(jwtService.getOperatorOrOpenSystemId(httpServletRequest)).thenReturn(userId);
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn("apiKey123");
        when(apiKeyService.getUsecaseStoreId(eq("apiKey123"))).thenReturn(storeId);
        when(operatorService.getOperatorWithAuth(eq(operatorId), eq(userId), eq(storeId)))
                .thenThrow(new UnexpectedException("auth-failed", "Auth service error", new RuntimeException("openfga")));

        UnexpectedException ex = assertThrows(UnexpectedException.class,
                () -> operatorController.getOperator(httpServletRequest, operatorId)
        );
        assertNotNull(ex.getResponseMessage());
    }

    /**
     * Test for postOperator - Authorization false.
     */
    @Test
    void testPostOperatorAuthDenied() throws Exception {
        String apiKey = "apiKey123";
        String creatorOperatorId = "creator123";
        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(creatorOperatorId);
        when(identityProviderService.createAccount(eq(apiKey), any(), any(), anyBoolean(), anyBoolean())).thenReturn(new IdpAccountInfo("id", "login", null, "pwd", true));

        PostOperatorRequest requestBody = new PostOperatorRequest();
        requestBody.setLoginUserId("login");
        requestBody.setOperatorName("name");

        when(operatorService.addOperatorWithAuth(
            any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenThrow(new IllegalAuthDataException("forbidden", "Forbidden"));

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        IllegalAuthDataException ex = assertThrows(
                IllegalAuthDataException.class,
                () -> operatorController.postOperator(request, requestBody, bindingResult)
        );

        assertNotNull(ex.getResponseMessage());
    }

    /**
     * Test for putOperator - Authorization false.
     */
    @Test
    void testPutOperatorAuthDenied() throws Exception {
        String targetOperatorId = "operator123";

        String rowJson = "{}";

        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn("user123");
        // Provide a mock Validator to avoid NullPointerException inside controller
        Validator validator = mock(Validator.class);
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<PutOperatorRequest>> violations = mock(Set.class);
        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(PutOperatorRequest.class))).thenReturn(violations);
        Field validatorField = OperatorController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(operatorController, validator);

        // Ensure request has apiKey header used by controller
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader(Const.HEADER_API_KEY)).thenReturn("apiKey123");

        doThrow(new IllegalAuthDataException("forbidden", "Forbidden"))
            .when(operatorService).updateOperatorWithAuth(anyString(), any(), any(), any(), any(), any(), any(), any());

        IllegalAuthDataException ex = assertThrows(
                IllegalAuthDataException.class,
                () -> operatorController.putOperator(req, targetOperatorId, rowJson)
        );
        assertNotNull(ex.getResponseMessage());
    }

    /**
     * Test for postOperator with authorization disabled.
     */
    @Test
    void testPostOperatorWithAuthorizationDisabled() throws Exception {
        // Arrange
        String apiKey = "apiKey123";
        String storeId = "store-uuid";
        String creatorOperatorId = "creator123";
        String loginUserId = "loginuserid";
        String operatorName = "Operator Name";
        String operatorAddress = "123 Main St";
        String openOperatorId = "open123";
        String globalOperatorId = "global123";
        String startDate = "2025-08-30";
        String endDate = "2025-12-31";
        String operatorId = "user123";
        String password = "password123";
        LocalDateTime now = LocalDateTime.now();

        // Set isEnableUcAuthorization to false for this test
        when(odsProperties.isEnableUcAuthorization()).thenReturn(false);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        // Mock apiKeyService.getUsecaseStoreId() - it's called but storeId is not used when auth is disabled
        when(apiKeyService.getUsecaseStoreId(apiKey)).thenReturn(storeId);

        PostOperatorRequest requestBody = new PostOperatorRequest(
                loginUserId, operatorName, operatorAddress,
                openOperatorId, globalOperatorId, startDate, endDate, "true", "false"
        );

        IdpAccountInfo idpResult = new IdpAccountInfo(operatorId, loginUserId, null, password, true);
        OperatorResult operatorResult = new OperatorResult();
        operatorResult.setOperatorId(operatorId);
        operatorResult.setOperatorName(operatorName);
        operatorResult.setOperatorAddress(operatorAddress);
        operatorResult.setOpenOperatorId(openOperatorId);
        operatorResult.setGlobalOperatorId(globalOperatorId);
        operatorResult.setDeletedFlag(false);
        operatorResult.setEffectiveStartDate(LocalDate.parse(startDate));
        operatorResult.setEffectiveEndDate(LocalDate.parse(endDate));
        operatorResult.setCreatedAt(now);
        operatorResult.setUpdatedAt(now);
        operatorResult.setCreatedUserId(creatorOperatorId);
        operatorResult.setUpdatedUserId(creatorOperatorId);

        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(creatorOperatorId);
        when(identityProviderService.createAccount(eq(apiKey), any(), any(), anyBoolean(), anyBoolean()))
                .thenReturn(idpResult);
        when(operatorService.addOperator(anyString(), anyString(), anyString(), anyString(), any(), any(), any(), any()))
                .thenReturn(operatorResult);

        // Act
        ResponseEntity<APIResponse<PostOperatorResponse>> responseEntity = operatorController.postOperator(
                request, requestBody, mock(BindingResult.class));

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        APIResponse<PostOperatorResponse> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.CREATED.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());

        // Verify apiKeyService.getUsecaseStoreId() is NOT called when auth is disabled (optimization)
        verify(apiKeyService, times(0)).getUsecaseStoreId(any());

        // Verify the service was called without auth (no storeId parameter)
        verify(operatorService).addOperator(anyString(), anyString(), anyString(), anyString(), any(), any(), any(), any());
        verify(operatorService, times(0)).addOperatorWithAuth(anyString(), anyString(), anyString(), anyString(), any(), any(), any(), any(), any());

        assertEquals(operatorId, apiResponse.getData().getOperatorId());
        assertEquals(password, apiResponse.getData().getPassword());
        assertEquals(operatorName, apiResponse.getData().getOperatorName());
    }

    /**
     * Test for postOperator with authorization disabled and no API key.
     */
    @Test
    void testPostOperatorWithAuthorizationDisabledNoApiKey() throws Exception {
        // Arrange
        String creatorOperatorId = "creator123";
        String loginUserId = "loginuserid";
        String operatorName = "Operator Name";
        String operatorAddress = "123 Main St";
        String openOperatorId = "open123";
        String globalOperatorId = "global123";
        String startDate = "2025-08-30";
        String endDate = "2025-12-31";
        String operatorId = "user123";
        String password = "password123";
        LocalDateTime now = LocalDateTime.now();

        // Set isEnableUcAuthorization to false for this test
        when(odsProperties.isEnableUcAuthorization()).thenReturn(false);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(null); // No API key

        PostOperatorRequest requestBody = new PostOperatorRequest(
                loginUserId, operatorName, operatorAddress,
                openOperatorId, globalOperatorId, startDate, endDate, "true", "false"
        );

        IdpAccountInfo idpResult = new IdpAccountInfo(operatorId, loginUserId, null, password, true);
        OperatorResult operatorResult = new OperatorResult();
        operatorResult.setOperatorId(operatorId);
        operatorResult.setOperatorName(operatorName);
        operatorResult.setOperatorAddress(operatorAddress);
        operatorResult.setOpenOperatorId(openOperatorId);
        operatorResult.setGlobalOperatorId(globalOperatorId);
        operatorResult.setDeletedFlag(false);
        operatorResult.setEffectiveStartDate(LocalDate.parse(startDate));
        operatorResult.setEffectiveEndDate(LocalDate.parse(endDate));
        operatorResult.setCreatedAt(now);
        operatorResult.setUpdatedAt(now);
        operatorResult.setCreatedUserId(creatorOperatorId);
        operatorResult.setUpdatedUserId(creatorOperatorId);

        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(creatorOperatorId);
        when(identityProviderService.createAccount(isNull(), any(), any(), anyBoolean(), anyBoolean()))
                .thenReturn(idpResult);
        when(operatorService.addOperator(anyString(), anyString(), anyString(), anyString(), any(), any(), any(), any()))
                .thenReturn(operatorResult);

        // Act
        ResponseEntity<APIResponse<PostOperatorResponse>> responseEntity = operatorController.postOperator(
                request, requestBody, mock(BindingResult.class));

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        // Verify apiKeyService.getUsecaseStoreId() is NOT called when apiKey is null
        verify(apiKeyService, times(0)).getUsecaseStoreId(any());

        // Verify the service was called without auth
        verify(operatorService).addOperator(anyString(), anyString(), anyString(), anyString(), any(), any(), any(), any());
        verify(operatorService, times(0)).addOperatorWithAuth(anyString(), anyString(), anyString(), anyString(), any(), any(), any(), any(), any());
    }

    /**
     * Test for postOperator with authorization disabled - Exception during operator creation triggers rollback.
     */
    @Test
    void testPostOperatorWithAuthorizationDisabledRollback() throws Exception {
        // Arrange
        String apiKey = "apiKey123";
        String creatorOperatorId = "creator123";
        String loginUserId = "loginuserid";
        String operatorName = "Operator Name";
        String operatorAddress = "123 Main St";
        String openOperatorId = "open123";
        String globalOperatorId = "global123";
        String startDate = "2025-08-30";
        String endDate = "2025-12-31";
        String operatorId = "user123";
        String password = "password123";

        // Set isEnableUcAuthorization to false for this test
        when(odsProperties.isEnableUcAuthorization()).thenReturn(false);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);

        PostOperatorRequest requestBody = new PostOperatorRequest(
                loginUserId, operatorName, operatorAddress,
                openOperatorId, globalOperatorId, startDate, endDate, "true", "false"
        );

        IdpAccountInfo idpResult = new IdpAccountInfo(operatorId, loginUserId, null, password, true);

        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(creatorOperatorId);
        when(identityProviderService.createAccount(eq(apiKey), any(), any(), anyBoolean(), anyBoolean()))
                .thenReturn(idpResult);
        // Make operatorService.addOperator throw an exception
        when(operatorService.addOperator(anyString(), anyString(), anyString(), anyString(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Database error"));
        doNothing().when(identityProviderService).deleteAccount(eq(apiKey), eq(operatorId));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                operatorController.postOperator(request, requestBody, mock(BindingResult.class))
        );

        assertEquals("Database error", exception.getMessage());

        // Verify apiKeyService.getUsecaseStoreId() is NOT called when auth is disabled (optimization)
        verify(apiKeyService, times(0)).getUsecaseStoreId(any());

        // Verify that identityProviderService.deleteAccount was called to rollback
        verify(identityProviderService).deleteAccount(eq(apiKey), eq(operatorId));
        verify(operatorService).addOperator(anyString(), anyString(), anyString(), anyString(), any(), any(), any(), any());
        verify(operatorService, times(0)).addOperatorWithAuth(anyString(), anyString(), anyString(), anyString(), any(), any(), any(), any(), any());
    }

    /**
     * Test for putOperator with authorization disabled.
     */
    @Test
    void testPutOperatorWithAuthorizationDisabled() throws Exception {
        // Arrange
        String apiKey = "apiKey123";
        String storeId = "store-uuid";
        String targetOperatorId = "operator123";
        String userOperatorId = "user123";
        String updatedAt = "2025-08-30T10:15:30.000Z";
        String loginUserId = "loginUser";
        String operatorName = "Operator Name";
        String operatorAddress = "123 Main St";
        String openOperatorId = "open123";
        String globalOperatorId = "global123";

        // Set isEnableUcAuthorization to false for this test
        when(odsProperties.isEnableUcAuthorization()).thenReturn(false);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        when(apiKeyService.getUsecaseStoreId(apiKey)).thenReturn(storeId);

        StringBuilder jsonSb = new StringBuilder();
        String rowJson = jsonSb.append("{")
                .append("\"").append(Const.JSON_PROPERTY_UPDATED_AT).append("\":\"").append(updatedAt).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_LOGIN_USER_ID).append("\":\"").append(loginUserId).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_OPERATOR_NAME).append("\":\"").append(operatorName).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_OPERATOR_ADDRESS).append("\":\"").append(operatorAddress).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_OPEN_OPERATOR_ID).append("\":\"").append(openOperatorId).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_GLOBAL_OPERATOR_ID).append("\":\"").append(globalOperatorId).append("\"")
                .append("}").toString();

        PutOperatorRequest requestBody = new PutOperatorRequest(
                updatedAt, loginUserId, operatorName, operatorAddress, openOperatorId, globalOperatorId);
        requestBody.setRawJson(rowJson);

        // Mock the validator to always return no violations
        Validator validator = mock(Validator.class);
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<PutOperatorRequest>> violations = mock(Set.class);
        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(PutOperatorRequest.class))).thenReturn(violations);

        // Inject the mock validator into the controller using reflection
        Field validatorField = OperatorController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(operatorController, validator);

        OperatorResult operatorResult = new OperatorResult();
        operatorResult.setOperatorId(targetOperatorId);
        operatorResult.setOperatorName(operatorName);
        operatorResult.setOperatorAddress(operatorAddress);
        operatorResult.setOpenOperatorId(openOperatorId);
        operatorResult.setGlobalOperatorId(globalOperatorId);

        IdpAccountInfo idpResult = new IdpAccountInfo(targetOperatorId, loginUserId, null, true);

        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(userOperatorId);
        when(operatorService.updateOperator(anyString(), any(), any(), any(), any(), any(), any()))
                .thenReturn(operatorResult);
        when(identityProviderService.updateAccount(any(), any(), any(), any()))
                .thenReturn(idpResult);

        // Act
        ResponseEntity<APIResponse<Object>> responseEntity = operatorController.putOperator(
                request, targetOperatorId, rowJson);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        APIResponse<Object> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());

        // Verify apiKeyService.getUsecaseStoreId() is NOT called when auth is disabled (optimization)
        verify(apiKeyService, times(0)).getUsecaseStoreId(any());

        // Verify the service was called without auth
        verify(operatorService).updateOperator(anyString(), any(), any(), any(), any(), any(), any());
        verify(operatorService, times(0)).updateOperatorWithAuth(anyString(), any(), any(), any(), any(), any(), any(), any());

        OperatorResponse responseData = objectMapper.convertValue(apiResponse.getData(), OperatorResponse.class);
        assertEquals(targetOperatorId, responseData.getOperatorId());
        assertEquals(operatorName, responseData.getOperatorName());
    }

    /**
     * Test for putOperatorStatus with authorization disabled.
     */
    @Test
    void testPutOperatorStatusWithAuthorizationDisabled() throws Exception {
        // Arrange
        String apiKey = "apiKey123";
        String storeId = "store-uuid";
        String targetOperatorId = "operator123";
        String userOperatorId = "user123";
        String updatedAt = "2025-08-30T10:15:30.000Z";
        String effectiveStartDate = "2020-01-01";
        String effectiveEndDate = "2030-12-31";
        String deletedFlag = "true";

        // Set isEnableUcAuthorization to false for this test
        when(odsProperties.isEnableUcAuthorization()).thenReturn(false);

        when(bindingResult.hasErrors()).thenReturn(false);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        when(apiKeyService.getUsecaseStoreId(apiKey)).thenReturn(storeId);
        PutOperatorStatusRequest requestBody = new PutOperatorStatusRequest(
                updatedAt, effectiveStartDate, effectiveEndDate, deletedFlag
        );

        OperatorResult operatorResult = new OperatorResult();
        operatorResult.setOperatorId(targetOperatorId);
        operatorResult.setDeletedFlag(true);
        operatorResult.setEffectiveStartDate(LocalDate.parse(effectiveStartDate, DateTimeFormatter.ISO_DATE));
        operatorResult.setEffectiveEndDate(LocalDate.parse(effectiveEndDate, DateTimeFormatter.ISO_DATE));

        IdpAccountInfo idpResult = new IdpAccountInfo(targetOperatorId, null, null, null, true);

        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(userOperatorId);
        when(operatorService.updateOperatorStatus(anyString(), any(), any(), any(), any(), any()))
                .thenReturn(operatorResult);
        when(identityProviderService.updateAccountStatus(any(), any(), any()))
                .thenReturn(idpResult);

        // Act
        ResponseEntity<APIResponse<Object>> responseEntity = operatorController.putOperatorStatus(
                request, targetOperatorId, requestBody, bindingResult);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        APIResponse<Object> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());

        // Verify apiKeyService.getUsecaseStoreId() is NOT called when auth is disabled (optimization)
        verify(apiKeyService, times(0)).getUsecaseStoreId(any());

        // Verify the service was called without auth
        verify(operatorService).updateOperatorStatus(anyString(), any(), any(), any(), any(), any());
        verify(operatorService, times(0)).updateOperatorStatusWithAuth(anyString(), any(), any(), any(), any(), any(), any());

        OperatorResponse responseData = objectMapper.convertValue(apiResponse.getData(), OperatorResponse.class);
        assertEquals(targetOperatorId, responseData.getOperatorId());
        assertEquals(true, responseData.isDeletedFlag());
    }

    /**
     * Test for listOperator with authorization disabled.
     */
    @Test
    void testListOperatorWithAuthorizationDisabled() throws Exception {
        // Arrange
        String apiKey = "apiKey123";
        String storeId = "store-uuid";

        // Set isEnableUcAuthorization to false for this test
        when(odsProperties.isEnableUcAuthorization()).thenReturn(false);

        ListOperatorRequest listOperatorRequest = new ListOperatorRequest();
        listOperatorRequest.setCount(commonCount);
        listOperatorRequest.setIndex(commonIndex);
        listOperatorRequest.setSort(new ListOperatorRequest.SortKey(commonSortKey, commonSortOrder));
        listOperatorRequest.setOperatorId(commonOperatorId);
        listOperatorRequest.setOperatorName(commonOperatorName);
        listOperatorRequest.setOperatorAddress(commonOperatorAddress);
        listOperatorRequest.setOpenOperatorId(commonOpenOperatorId);
        listOperatorRequest.setGlobalOperatorId(commonGlobalOperatorId);
        listOperatorRequest.setEffectiveDate(commonEffectiveDate);
        listOperatorRequest.setDeletedFlag(commonDeletedFlag);
        // Convert request object to JSON string
        String rawJson = objectMapper.writeValueAsString(listOperatorRequest);

        OperatorResult operatorResult = new OperatorResult();
        operatorResult.setOperatorId(commonOperatorId);
        operatorResult.setOperatorName(commonOperatorName);
        operatorResult.setOperatorAddress(commonOperatorAddress);
        operatorResult.setOpenOperatorId(commonOpenOperatorId);
        operatorResult.setGlobalOperatorId(commonGlobalOperatorId);
        operatorResult.setDeletedFlag(false);
        operatorResult.setEffectiveStartDate(LocalDate.now(ZoneOffset.UTC));
        operatorResult.setEffectiveEndDate(LocalDate.now(ZoneOffset.UTC).plusDays(1));
        operatorResult.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        operatorResult.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        List<OperatorResult> listOperatorResult = List.of(operatorResult);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        when(apiKeyService.getUsecaseStoreId(apiKey)).thenReturn(storeId);

        // Mock the validator to always return no violations
        Validator validator = mock(Validator.class);
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<ListOperatorRequest>> violations = mock(Set.class);

        // Inject the mock validator into the controller using reflection
        Field validatorField = OperatorController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(operatorController, validator);

        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(ListOperatorRequest.class))).thenReturn(violations);
        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn("user123");
        when(operatorService.searchOperators(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), any(StateString.class), anyBoolean(), any(LocalDate.class)))
                .thenReturn(listOperatorResult);

        // Act
        ResponseEntity<APIResponse<List<GetOperatorResponse>>> responseEntity = operatorController.listOperator(
                request, rawJson);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        APIResponse<List<GetOperatorResponse>> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());
        assertFalse(apiResponse.getData().isEmpty(), "Data list should not be empty");

        GetOperatorResponse resp = apiResponse.getData().get(0);
        assertEquals(commonOperatorId, resp.getOperatorId());
        assertEquals(commonOperatorName, resp.getOperatorName());

        // Verify apiKeyService.getUsecaseStoreId() is NOT called when auth is disabled (optimization)
        verify(apiKeyService, times(0)).getUsecaseStoreId(any());

        // Verify the service was called without auth
        verify(operatorService).searchOperators(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), any(StateString.class), anyBoolean(), any(LocalDate.class));
        verify(operatorService, times(0)).searchOperatorsWithAuth(anyInt(), anyInt(), any(), any(), any(), any(), any(),
                any(), any(StateString.class), any(), any(LocalDate.class), any(), any());
    }

    /**
     * Test for listOperator with null values in request - parameterized for auth enabled/disabled.
     *
     * @param isEnableAuthz true to enable authorization, false to disable
     * @throws Exception when error occurs
     */
    @ParameterizedTest
    @CsvSource({"true", "false"})
    void testListOperator_nullValues(boolean isEnableAuthz) throws Exception {
        // Set isEnableUcAuthorization to true or false for this test
        when(odsProperties.isEnableUcAuthorization()).thenReturn(isEnableAuthz);

        ListOperatorRequest listOperatorRequest = new ListOperatorRequest();
        listOperatorRequest.setCount(commonCount);
        listOperatorRequest.setIndex(commonIndex);
        listOperatorRequest.setSort(null);
        listOperatorRequest.setOperatorId(null);
        listOperatorRequest.setOperatorName(null);
        listOperatorRequest.setOperatorAddress(null);
        listOperatorRequest.setOpenOperatorId(null);
        listOperatorRequest.setGlobalOperatorId(null);
        listOperatorRequest.setEffectiveDate(null);
        listOperatorRequest.setDeletedFlag(null);
        // Convert request object to JSON string
        String rawJson = objectMapper.writeValueAsString(listOperatorRequest);

        OperatorResult operatorResult = new OperatorResult();
        operatorResult.setOperatorId(commonOperatorId);
        operatorResult.setOperatorName(commonOperatorName);
        operatorResult.setOperatorAddress(commonOperatorAddress);
        operatorResult.setOpenOperatorId(commonOpenOperatorId);
        operatorResult.setGlobalOperatorId(commonGlobalOperatorId);
        operatorResult.setDeletedFlag(false);
        operatorResult.setEffectiveStartDate(LocalDate.now(ZoneOffset.UTC));
        operatorResult.setEffectiveEndDate(LocalDate.now(ZoneOffset.UTC).plusDays(1));
        operatorResult.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        operatorResult.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        List<OperatorResult> listOperatorResult = List.of(operatorResult);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(commonApiKey);
        when(apiKeyService.getUsecaseStoreId(commonApiKey)).thenReturn(commonStoreId);

        // Mock the validator to always return no violations
        Validator validator = mock(Validator.class);
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<ListOperatorRequest>> violations = mock(Set.class);

        // Inject the mock validator into the controller using reflection
        Field validatorField = OperatorController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(operatorController, validator);

        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(ListOperatorRequest.class))).thenReturn(violations);
        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn("user123");
        if (isEnableAuthz) {
            when(operatorService.searchOperatorsWithAuth(anyInt(), anyInt(), any(), any(), any(), any(), any(),
                    any(), any(), any(), any(), any(), any()))
                    .thenReturn(listOperatorResult);
        } else {
            when(operatorService.searchOperators(anyInt(), anyInt(), any(), any(), any(), any(), any(),
                    any(), any(), any(), any()))
                    .thenReturn(listOperatorResult);
        }

        // Act
        ResponseEntity<APIResponse<List<GetOperatorResponse>>> responseEntity = operatorController.listOperator(
                request, rawJson);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        APIResponse<List<GetOperatorResponse>> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());
        assertFalse(apiResponse.getData().isEmpty(), "Data list should not be empty");

        // Verify apiKeyService.getUsecaseStoreId() is NOT called when auth is disabled (optimization)
        verify(apiKeyService, times(isEnableAuthz ? 1 : 0)).getUsecaseStoreId(any());

        // Verify the service was called without auth
        verify(operatorService, times(isEnableAuthz ? 0 : 1)).searchOperators(anyInt(), anyInt(), any(), any(), any(), any(), any(),
                any(), any(), any(), any());
        verify(operatorService, times(isEnableAuthz ? 1 : 0)).searchOperatorsWithAuth(anyInt(), anyInt(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any());
    }
}