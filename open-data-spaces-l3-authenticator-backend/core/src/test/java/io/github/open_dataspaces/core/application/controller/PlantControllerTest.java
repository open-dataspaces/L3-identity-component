/*
 * PlantControllerTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This package contains unit tests for the PlantController class,
 *
 * Date: 2025/09/22
 */

package io.github.open_dataspaces.core.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import io.github.open_dataspaces.core.exception.IllegalAuthDataException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import io.github.open_dataspaces.core.exception.ValidateException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.open_dataspaces.core.common.enums.EnumResponseTypes;
import io.github.open_dataspaces.core.domain.dto.PostPlantRequest;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.dto.APIJSONRequest;
import io.github.open_dataspaces.core.domain.dto.APIResponse;
import io.github.open_dataspaces.core.domain.dto.PlantResponse;
import io.github.open_dataspaces.core.domain.dto.PlantResult;
import io.github.open_dataspaces.core.domain.dto.PutPlantRequest;
import io.github.open_dataspaces.core.domain.dto.GetPlantResponse;
import io.github.open_dataspaces.core.domain.dto.ListOperatorRequest;
import io.github.open_dataspaces.core.domain.dto.ListPlantRequest;
import io.github.open_dataspaces.core.domain.dto.PutPlantStatusRequest;
import io.github.open_dataspaces.core.domain.dto.StateString;
import io.github.open_dataspaces.core.domain.service.interfaces.JWTVerifyService;
import io.github.open_dataspaces.core.domain.service.interfaces.PlantService;
import io.github.open_dataspaces.core.domain.service.interfaces.AuthorizationService;
import io.github.open_dataspaces.core.domain.service.interfaces.APIKeyService;
import io.github.open_dataspaces.core.common.config.ODSProperties;
import io.github.open_dataspaces.core.exception.AbstractBaseException;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.ConflictException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnauthorizedException;
import io.github.open_dataspaces.core.exception.UnexpectedException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Unit tests for PlantController.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
class PlantControllerTest {

    @Mock
    private PlantService plantService;
    @Mock
    private JWTVerifyService jwtService;
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
    private PlantController plantController;

    // Common input parameters
    private final String commonPlantId = UUID.randomUUID().toString();
    private final Boolean commonDeletedFlag = false;
    private final LocalDate commonEffectiveStartDate = LocalDate.now();
    private final LocalDate commonEffectiveEndDate = LocalDate.now();
    private final LocalDateTime commonCreatedAt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    private final LocalDateTime commonUpdatedAt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    private final String commonOperatorId = UUID.randomUUID().toString();
    private final String commonPlantName = "plantName";
    private final String commonPlantAddress = "plantAddress";
    private final String commonOpenPlantId = "openPlantId";
    private final String commonGlobalPlantId = "globalPlantId";

    // private final String commonUpdatedFrom = LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(CMPConst.DATE_TIME_FORMAT_SEARCH_JST));
    // private final String commonUpdatedTo = LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(CMPConst.DATE_TIME_FORMAT_SEARCH_JST));
    private final String commonEffectiveDate = LocalDate.now().toString();
    private final int commonCount = 0;
    private final int commonIndex = 0;
    private final String commonSortKey = Const.JSON_PROPERTY_PLANT_ID;
    private final String commonSortOrder = "asc";

    private final String commonApiKey = "apiKey123";
    private final String commonStoreId = "store-uuid";

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // Register JavaTimeModule
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // Use ISO-8601 format

    /**
     * Default constructor.
     */
    public PlantControllerTest() {
        MockitoAnnotations.openMocks(this);
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

        MockitoAnnotations.openMocks(this);
        when(odsProperties.isEnableUcAuthorization()).thenReturn(true);
        plantController = new PlantController(plantService, jwtService, apiKeyService, odsProperties);
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
     * Test for putPlant - Success case.
     */
    @ParameterizedTest
    @CsvSource({
        // Valid case with all fields changed
        "plant123,2025-08-30T10:15:30.000Z,Plant Name,Address1,open123,global123",
        // Valid case with all fields changed Another case
        "plant456,2025-09-01T12:00:00.000Z,Another Name,Address2,open456,global456",
        // Valid case with only required fields
        "plant123,2025-08-30T10:15:30.000Z,null,null,null,null",
        // Boundary values
        "plant123,2025-08-30T10:15:30.000Z,p,Address1,open123,global123",
        "plant123,2025-08-30T10:15:30.000Z,LONG,Address1,open123,global123",
        "plant123,2025-08-30T10:15:30.000Z,Plant Name,a,open123,global123",
        "plant123,2025-08-30T10:15:30.000Z,Plant Name,LONG,open123,global123",
        "plant123,2025-08-30T10:15:30.000Z,Plant Name,Address1,123456,global123",
        "plant123,2025-08-30T10:15:30.000Z,Plant Name,Address1,LONG,global123",
        "plant123,2025-08-30T10:15:30.000Z,Plant Name,Address1,open123,''",
        "plant123,2025-08-30T10:15:30.000Z,Plant Name,Address1,open123,LONG",
        // Pattern cases
        "plant123,2025-08-30T10:15:30.000Z,Plant Name,Address1,open123456,global123",
    })
    @DisplayName("putPlant - Success Case")
    void testPutPlantSuccess(
            String targetPlantId,
            String updatedAt,
            String plantName,
            String plantAddress,
            String openPlantId,
            String globalPlantId) throws Exception {
        // Handle special cases for NULL and LONG inputs
        plantName = "null".equals(plantName) ? null : plantName;
        plantAddress = "null".equals(plantAddress) ? null : plantAddress;
        openPlantId = "null".equals(openPlantId) ? null : openPlantId;
        globalPlantId = "null".equals(globalPlantId) ? null : globalPlantId;
        plantName = "LONG".equals(plantName) ? "u".repeat(Const.PLANT_NAME_LENGTH_MAX) : plantName;
        plantAddress = "LONG".equals(plantAddress) ? "O".repeat(Const.PLANT_ADDRESS_LENGTH_MAX) : plantAddress;
        openPlantId = "LONG".equals(openPlantId) ? "e".repeat(Const.OPEN_PLANT_ID_LENGTH_MAX - 6) + "1".repeat(6)  : openPlantId;
        globalPlantId = "LONG".equals(globalPlantId) ? "A".repeat(Const.GLOBAL_PLANT_ID_LENGTH_MAX) : globalPlantId;
        // Arrange
        String apiKey = "apiKey123";
        String storeId = "test-store-id";
        String userOperatorId = "user123";
        LocalDateTime updatedDateTime = LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        when(apiKeyService.getUsecaseStoreId(apiKey)).thenReturn(storeId);
        StringBuilder jsonSb = new StringBuilder();
        String rowJson = jsonSb.append("{")
                .append("\"").append(Const.JSON_PROPERTY_UPDATED_AT).append("\":\"").append(updatedAt).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_PLANT_NAME).append("\":\"").append(plantName).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_PLANT_ADDRESS).append("\":\"").append(plantAddress).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_OPEN_PLANT_ID).append("\":\"").append(openPlantId).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_GLOBAL_PLANT_ID).append("\":\"").append(globalPlantId).append("\"")
                .append("}").toString();
        PutPlantRequest requestBody = new PutPlantRequest(
                updatedAt, plantName, plantAddress, openPlantId, globalPlantId);
        requestBody.setRawJson(rowJson);
        // Mock the validator to always return no violations
        Validator validator = mock(Validator.class);
        // Suppress warnings for unchecked casts
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<PutPlantRequest>> violations = mock(Set.class);
        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(PutPlantRequest.class))).thenReturn(violations);
        // Inject the mock validator into the controller using reflection
        Field validatorField = PlantController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(plantController, validator);
        PlantResult plantResult = new PlantResult();
        plantResult.setPlantId(targetPlantId);
        plantResult.setPlantName(plantName);
        plantResult.setPlantAddress(plantAddress);
        plantResult.setOpenPlantId(openPlantId);
        plantResult.setGlobalPlantId(globalPlantId);
        plantResult.setUpdatedAt(updatedDateTime);
        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(userOperatorId);
        when(plantService.updatePlantWithAuth(anyString(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(plantResult);
        // Act
        ResponseEntity<APIResponse<Object>> responseEntity = plantController.putPlant(
                request, targetPlantId, rowJson);
        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        APIResponse<Object> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());
        PlantResponse responseData = (PlantResponse) apiResponse.getData();
        assertEquals(targetPlantId, responseData.getPlantId());
        assertEquals(plantAddress, responseData.getPlantAddress());
        assertEquals(openPlantId, responseData.getOpenPlantId());
        assertEquals(globalPlantId, responseData.getGlobalPlantId());
        assertEquals(updatedDateTime, responseData.getUpdatedAt());
    }

    /**
     * Test for putPlant - Authorization false.
     */
    @Test
    void testPutPlantAuthDenied() throws Exception {
        String targetPlantId = "plant123";

        String rowJson = "{}";

        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn("user123");
        // Provide a mock Validator to avoid NPE
        Validator validator = mock(Validator.class);
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<PutPlantRequest>> violations = mock(Set.class);
        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(PutPlantRequest.class))).thenReturn(violations);
        Field validatorField = PlantController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(plantController, validator);

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader(Const.HEADER_API_KEY)).thenReturn("apiKey123");

        doThrow(new IllegalAuthDataException("forbidden", "Forbidden"))
            .when(plantService).updatePlantWithAuth(anyString(), any(), any(), any(), any(), any(), any(), any());

        IllegalAuthDataException ex = assertThrows(
                IllegalAuthDataException.class,
                () -> plantController.putPlant(req, targetPlantId, rowJson)
        );
        assertNotNull(ex.getResponseMessage());
    }

    /**
     * Test for putPlant - Empty response from service (Success case).
     */
    @ParameterizedTest
    @CsvSource({
        // Valid case with all fields changed
        "plant123,2025-08-30T10:15:30.000Z,Plant Name,Address1,open123,global123",
        // Valid case with all fields changed Another case
        // Valid case with all fields changed another case
        "plant456,2025-09-01T12:00:00.000Z,Another Name,Address2,open456,global456"
    })
    @DisplayName("putPlant - Empty Response Case")
    void testPutPlantEmptyResponse(
            String targetPlantId,
            String updatedAt,
            String plantName,
            String plantAddress,
            String openPlantId,
            String globalPlantId) throws Exception {
        // Arrange
        String apiKey = "apiKey123";
        String userOperatorId = "user123";
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        StringBuilder jsonSb = new StringBuilder();
        String rowJson = jsonSb.append("{")
                .append("\"").append(Const.JSON_PROPERTY_UPDATED_AT).append("\":\"").append(updatedAt).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_PLANT_NAME).append("\":\"").append(plantName).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_PLANT_ADDRESS).append("\":\"").append(plantAddress).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_OPEN_PLANT_ID).append("\":\"").append(openPlantId).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_GLOBAL_PLANT_ID).append("\":\"").append(globalPlantId).append("\"")
                .append("}").toString();
        PutPlantRequest requestBody = new PutPlantRequest(
                updatedAt, plantName, plantAddress, openPlantId, globalPlantId);
        requestBody.setRawJson(rowJson);
        // Mock the validator to always return no violations
        Validator validator = mock(Validator.class);
        // Suppress warnings for unchecked casts
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<PutPlantRequest>> violations = mock(Set.class);
        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(PutPlantRequest.class))).thenReturn(violations);
        // Inject the mock validator into the controller using reflection
        Field validatorField = PlantController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(plantController, validator);
        // Simulate plantService.updatePlant returning null
        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(userOperatorId);
        when(plantService.updatePlantWithAuth(anyString(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(null);
        // Act
        ResponseEntity<APIResponse<Object>> responseEntity = plantController.putPlant(
                request, targetPlantId, rowJson);
        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        APIResponse<Object> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());
        assertEquals(new HashMap<>(), apiResponse.getData()); // Ensure the response data is an empty object
    }

    /**
     * Parameterized test for validation errors.
     */
    @ParameterizedTest
    @CsvSource({
        "plant123,2025-08-30T10:15:30.000Z,Plant Name,Address1,open123,global123",
        "plant456,2025-09-01T12:00:00.000Z,Another Name,Address2,open456,global456"
    })
    @DisplayName("putPlant - Validation Error")
    void testPutValidationError(
            String targetPlantId,
            String updatedAt,
            String plantName,
            String plantAddress,
            String openPlantId,
            String globalPlantId) throws Exception {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        StringBuilder jsonSb = new StringBuilder();
        String rowJson = jsonSb.append("{")
                .append("\"").append(Const.JSON_PROPERTY_UPDATED_AT).append("\":\"").append(updatedAt).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_PLANT_NAME).append("\":\"").append(plantName).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_PLANT_ADDRESS).append("\":\"").append(plantAddress).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_OPEN_PLANT_ID).append("\":\"").append(openPlantId).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_GLOBAL_PLANT_ID).append("\":\"").append(globalPlantId).append("\"")
                .append("}").toString();
        PutPlantRequest requestBody = new PutPlantRequest(
                updatedAt, plantName, plantAddress, openPlantId, globalPlantId);
        requestBody.setRawJson(rowJson);
        // Mock the validator to always return violations
        Validator validator = mock(Validator.class);
        // Suppress warnings for unchecked casts
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<PutPlantRequest>> violations = mock(Set.class);
        // Mock the validator to always return no violations
        when(violations.isEmpty()).thenReturn(false);
        when(validator.validate(any(PutPlantRequest.class))).thenReturn(violations);
        // Inject the mock validator into the controller using reflection
        Field validatorField = PlantController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(plantController, validator);
        // Act & Assert
        ValidateException exception = assertThrows(ValidateException.class, () ->
                plantController.putPlant(request, targetPlantId, rowJson)
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
        "BadParametersException",
        "UnauthorizedException",
        "OutOfServiceException",
        "ConflictException",
        "UnexpectedException",
        "RuntimeException"
    })
    void testPutPlant_throwException(String exceptionClassName)throws Exception {
        // Arrange
        String targetPlantId = "operator123";
        String userOperatorId = "user123";
        String updatedAt = "2025-08-30T10:15:30.000Z";
        String plantName = "Plant Name";
        String plantAddress = "Address1";
        String openPlantId = "open123";
        String globalPlantId = "global123";
        HttpServletRequest request = mock(HttpServletRequest.class);
        StringBuilder jsonSb = new StringBuilder();
        String rowJson = jsonSb.append("{")
                .append("\"").append(Const.JSON_PROPERTY_UPDATED_AT).append("\":\"").append(updatedAt).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_PLANT_NAME).append("\":\"").append(plantName).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_PLANT_ADDRESS).append("\":\"").append(plantAddress).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_OPEN_PLANT_ID).append("\":\"").append(openPlantId).append("\",")
                .append("\"").append(Const.JSON_PROPERTY_GLOBAL_PLANT_ID).append("\":\"").append(globalPlantId).append("\"")
                .append("}").toString();
        PutPlantRequest requestBody = new PutPlantRequest(
                updatedAt, plantName, plantAddress, openPlantId, globalPlantId);
        requestBody.setRawJson(rowJson);
        // Mock the validator to always return violations
        Validator validator = mock(Validator.class);
        // Suppress warnings for unchecked casts
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<PutPlantRequest>> violations = mock(Set.class);
        // Mock the validator to always return no violations
        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(PutPlantRequest.class))).thenReturn(violations);
        // Inject the mock validator into the controller using reflection
        Field validatorField = PlantController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(plantController, validator);
        PlantResult plantResult = new PlantResult();
        plantResult.setPlantId(targetPlantId);
        plantResult.setPlantName(plantName);
        plantResult.setPlantAddress(plantAddress);
        plantResult.setOpenPlantId(openPlantId);
        plantResult.setGlobalPlantId(globalPlantId);
        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(userOperatorId);
        // Prepare exception to be thrown
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
                exceptionLogMessage = String.format(ConstError.ERR_409_CONFLICT, "updatedAt", updatedAt);
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
        when(plantService.updatePlantWithAuth(anyString(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(exception);
        // Act & Assert
        Throwable thrownException = assertThrows(clazz, () -> {
            plantController.putPlant(request, targetPlantId, rowJson);
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
        verify(violations).isEmpty();
        verify(jwtService).getOperatorOrOpenSystemId(any(HttpServletRequest.class));
        verify(plantService).updatePlantWithAuth(anyString(), any(), any(), any(), any(), any(), any(), any());
    }

    /**
     * Test for putPlant with authorization disabled.
     */
    @Test
    void testPutPlant_withAuthorizationDisabled() throws Exception {
        // Arrange
        String apiKey = "apiKey123";
        String storeId = "store-uuid";
        String targetPlantId = "28919692-AD5F-410E-9567-D99555BBDA61";
        String userOperatorId = "123e4567-e89b-12d3-a456-426614174000";
        String updatedAt = "2025-08-30T10:15:30.000Z";
        String plantName = "Updated Plant Name";
        String plantAddress = "456 New St";
        String openPlantId = "NEWOPEN12345";
        String globalPlantId = "NEW-GLOBAL-ID";

        // Set isEnableUcAuthorization to false for this test
        when(odsProperties.isEnableUcAuthorization()).thenReturn(false);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        when(apiKeyService.getUsecaseStoreId(apiKey)).thenReturn(storeId);
        PutPlantRequest requestBody = new PutPlantRequest();
        requestBody.setUpdatedAt(updatedAt);
        requestBody.setPlantName(plantName);
        requestBody.setPlantAddress(plantAddress);
        requestBody.setOpenPlantId(openPlantId);
        requestBody.setGlobalPlantId(globalPlantId);

        // Convert request object to JSON string
        String rawJson = objectMapper.writeValueAsString(requestBody);

        PlantResult plantResult = new PlantResult();
        plantResult.setOperatorId(userOperatorId);
        plantResult.setPlantId(targetPlantId);
        plantResult.setPlantName(plantName);
        plantResult.setPlantAddress(plantAddress);
        plantResult.setOpenPlantId(openPlantId);
        plantResult.setGlobalPlantId(globalPlantId);
        plantResult.setDeletedFlag(false);
        plantResult.setEffectiveStartDate(LocalDate.now(ZoneOffset.UTC));
        plantResult.setEffectiveEndDate(LocalDate.now(ZoneOffset.UTC).plusDays(1));
        plantResult.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        plantResult.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));

        // Mock the validator to always return no violations
        Validator validator = mock(Validator.class);
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<PutPlantRequest>> violations = mock(Set.class);

        // Inject the mock validator into the controller using reflection
        Field validatorField = PlantController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(plantController, validator);

        // Arrange
        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(PutPlantRequest.class))).thenReturn(violations);
        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(userOperatorId);
        when(plantService.updatePlant(anyString(), any(), any(), any(), any(), any(), any()))
                .thenReturn(plantResult);

        // Act
        ResponseEntity<APIResponse<Object>> responseEntity = plantController.putPlant(
                request, targetPlantId, rawJson);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        APIResponse<Object> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());

        // Verify apiKeyService.getUsecaseStoreId() is NOT called when auth is disabled (optimization)
        verify(apiKeyService, times(0)).getUsecaseStoreId(any());

        // Verify the service was called without auth
        verify(plantService).updatePlant(anyString(), any(), any(), any(), any(), any(), any());
        verify(plantService, times(0)).updatePlantWithAuth(anyString(), any(), any(), any(), any(), any(), any(), any());

        PlantResponse response = (PlantResponse) apiResponse.getData();
        assertEquals(targetPlantId, response.getPlantId());
        assertEquals(plantName, response.getPlantName());
    }

    /**
     * Test for putPlantStatus - Success case.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "07c52485-aef1-4bae-aaa0-80b5f6db0950, 2025-08-30T10:15:30.000Z, 2020-01-01, 2030-12-31, true",
        "07c52485-aef1-4bae-aaa0-80b5f6db0950, 2025-08-30T10:15:30.000Z, 2020-01-01, 2030-12-31, false",
    }, nullValues = "NULL")
    @DisplayName("putPlantStatus - Success Case")
    void testPutPlantStatus_success(
            String targetPlantId,
            String updatedAt,
            String strEffectiveStartDate,
            String strEffectiveEndDate,
            String strDeletedFlag
    ) throws Exception {
        // Handle special cases for NULL and LONG inputs
        LocalDate resultEffectiveStartDate = strEffectiveStartDate == null ? LocalDate.now(ZoneOffset.UTC) : LocalDate.parse(strEffectiveStartDate);
        LocalDate resultEffectiveEndDate = strEffectiveEndDate == null ? LocalDate.now(ZoneOffset.UTC) : LocalDate.parse(strEffectiveEndDate);
        boolean resultDeletedFlag = strDeletedFlag == null ? false : Boolean.valueOf(strDeletedFlag);

        // Arrange
        String userOperatorId = "123e4567-e89b-12d3-a456-426614174000";

        LocalDateTime updatedDateTime = LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        HttpServletRequest request = mock(HttpServletRequest.class);

        PutPlantStatusRequest requestBody = new PutPlantStatusRequest(
                updatedAt, strEffectiveStartDate, strEffectiveEndDate, strDeletedFlag);

        PlantResult plantResult = new PlantResult();
        plantResult.setOperatorId(targetPlantId);
        plantResult.setDeletedFlag(resultDeletedFlag);
        plantResult.setEffectiveStartDate(resultEffectiveStartDate);
        plantResult.setEffectiveEndDate(resultEffectiveEndDate);
        plantResult.setUpdatedAt(updatedDateTime);

        String apiKey = "test-api-key";
        String storeId = "test-store-id";
        when(bindingResult.hasErrors()).thenReturn(false);
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        when(apiKeyService.getUsecaseStoreId(apiKey)).thenReturn(storeId);
        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(userOperatorId);
        when(plantService.updatePlantStatusWithAuth(anyString(), any(), any(), any(), any(), any(), any()))
                .thenReturn(plantResult);

        // Act
        ResponseEntity<APIResponse<Object>> responseEntity = plantController.putPlantStatus(
                request, targetPlantId, requestBody, bindingResult);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        APIResponse<Object> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());

        PlantResponse responseData = (PlantResponse) apiResponse.getData();
        assertEquals(targetPlantId, responseData.getOperatorId());
        assertEquals(resultEffectiveStartDate, responseData.getEffectiveStartDate());
        assertEquals(resultEffectiveEndDate, responseData.getEffectiveEndDate());
        assertEquals(resultDeletedFlag, responseData.isDeletedFlag());
    }

    /**
     * Test for putPlantStatus - Authorization false.
     */
    @Test
    void testPutPlantStatusAuthDenied() throws Exception {
        String targetPlantId = "plant123";
        String updatedAt = "2025-08-30T10:15:30.000Z";
        String effectiveStartDate = "2020-01-01";
        String effectiveEndDate = "2030-12-31";
        String deletedFlag = "true";

        when(bindingResult.hasErrors()).thenReturn(false);
        HttpServletRequest request = mock(HttpServletRequest.class);
        PutPlantStatusRequest requestBody = new PutPlantStatusRequest(
                updatedAt, effectiveStartDate, effectiveEndDate, deletedFlag
        );

        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn("user123");

        doThrow(new IllegalAuthDataException("forbidden", "Forbidden"))
                .when(plantService).updatePlantStatusWithAuth(anyString(), any(), any(), any(), any(), any(), any());

        IllegalAuthDataException ex = assertThrows(
                IllegalAuthDataException.class,
                () -> plantController.putPlantStatus(request, targetPlantId, requestBody, bindingResult)
        );
        assertNotNull(ex.getResponseMessage());
    }

    /**
     * Test for putPlantStatus - Empty response from service (Success case).
     */
    @ParameterizedTest
    @CsvSource(value = {
        "07c52485-aef1-4bae-aaa0-80b5f6db0950, 2025-08-30T10:15:30.000Z, NULL, NULL, NULL"
    }, nullValues = "NULL")
    @DisplayName("putPlantStatus - Empty Response Case")
    void testPutPlantStatusEmptyResponse(
            String targetPlantId,
            String updatedAt,
            String strEffectiveStartDate,
            String strEffectiveEndDate,
            String strDeletedFlag
    ) throws Exception {

        // Arrange
        String userOperatorId = "123e4567-e89b-12d3-a456-426614174000";
        HttpServletRequest request = mock(HttpServletRequest.class);
        PutPlantStatusRequest requestBody = new PutPlantStatusRequest(
                updatedAt, strEffectiveStartDate, strEffectiveEndDate, strDeletedFlag
        );

        // Simulate plantService.updatePlant returning null
        PlantResult plantResult = null;

        when(bindingResult.hasErrors()).thenReturn(false);
        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(userOperatorId);
        when(plantService.updatePlantStatusWithAuth(anyString(), any(), any(), any(), any(), any(), any()))
                .thenReturn(plantResult);

        // Act
        ResponseEntity<APIResponse<Object>> responseEntity = plantController.putPlantStatus(
                request, targetPlantId, requestBody, bindingResult);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        APIResponse<Object> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());
        assertEquals(new HashMap<>(), apiResponse.getData()); // Ensure the response data is an empty object
    }

    /**
     * Test for putPlantStatus - Validation error.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "NULL, 2025-08-30 10:15:30.000, 2020-01-01, 2030-12-31, true", // targetPlantId is null (required case)
        "'', 2025-08-30 10:15:30.000, 2020-01-01, 2030-12-31, true", // targetPlantId is empty (required case)
        "07c52485-aef1-4bae-aaa0-80b5f6db0950, NULL, 2020-01-01, 2030-12-31, true", // updatedAt is null (required case)
        "07c52485-aef1-4bae-aaa0-80b5f6db0950, '', 2020-01-01, 2030-12-31, true", // updatedAt is empty (required case)
        "NULL, NULL, 2020-01-01, 2030-12-31, true", // targetPlantId and updatedAt are null (required case)
        "'', '', 2020-01-01, 2030-12-31, true", // targetPlantId and updatedAt are empty (required case)
        "07c52485-aef1-4bae-aaa0-80b5f6db0950, 2025-08-30 10:15:30.000, NULL, 2030-12-31, true", // effectiveStartDate is null
        "07c52485-aef1-4bae-aaa0-80b5f6db0950, 2025-08-30 10:15:30.000, '', 2030-12-31, true", // effectiveStartDate is empty
        "07c52485-aef1-4bae-aaa0-80b5f6db0950, 2025-08-30 10:15:30.000, 2020-01-01, NULL, true", // effectiveEndDate is null
        "07c52485-aef1-4bae-aaa0-80b5f6db0950, 2025-08-30 10:15:30.000, 2020-01-01, '', true", // effectiveEndDate is empty
        "07c52485-aef1-4bae-aaa0-80b5f6db0950, 2025-08-30 10:15:30.000, 2020-01-01, 2030-12-31, NULL", // deletedFlag is null
        "07c52485-aef1-4bae-aaa0-80b5f6db0950, 2025-08-30 10:15:30.000, 2020-01-01, 2030-12-31, ''", // deletedFlag is empty
    }, nullValues = "NULL")
    @DisplayName("putPlantStatus - Validation Error")
    void testPutPlantStatusValidationError(
            String targetPlantId,
            String updatedAt,
            String strEffectiveStartDate,
            String strEffectiveEndDate,
            String strDeletedFlag
    ) throws Exception {
        // Arrange
        PutPlantStatusRequest requestBody = new PutPlantStatusRequest(
                updatedAt, strEffectiveStartDate, strEffectiveEndDate, strDeletedFlag
        );
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(bindingResult.hasErrors()).thenReturn(true);

        // Act & Assert
        ValidateException exception = assertThrows(ValidateException.class, () ->
                plantController.putPlantStatus(request, targetPlantId, requestBody, bindingResult)
        );
        assertEquals(ConstError.ERRLOG_400_INVALID_REQUEST, exception.getLogMessage());
        assertNotNull(exception.getMessage());
    }

    /**
     * Test for putPlantStatus throws an exception.
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
    @DisplayName("putPlantStatus - Exception During Update")
    void testPutPlantStatusServiceThrowsException(String exceptionClassName) throws Exception {
        // Arrange
        String targetPlantId = "07c52485-aef1-4bae-aaa0-80b5f6db0950";
        String userOperatorId = "123e4567-e89b-12d3-a456-426614174000";
        String updatedAt = "2025-08-30T10:15:30.000Z";
        String effectiveStartDate = "2020-01-01";
        String effectiveEndDate = "2030-12-31";
        String deletedFlag = "true";

        when(bindingResult.hasErrors()).thenReturn(false);
        HttpServletRequest request = mock(HttpServletRequest.class);
        PutPlantStatusRequest requestBody = new PutPlantStatusRequest(
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

        // Mock plantService to throw an exception
        doThrow(exception).when(plantService).updatePlantStatusWithAuth(anyString(), any(), any(), any(), any(), any(), any());

        // Act & Assert
        Throwable thrownException = assertThrows(clazz, () -> {
            plantController.putPlantStatus(request, targetPlantId, requestBody, bindingResult);
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
        verify(plantService).updatePlantStatusWithAuth(anyString(), any(), any(), any(), any(), any(), any());
    }

    /**
     * Test for putPlantStatus with authorization disabled.
     */
    @Test
    void testPutPlantStatus_withAuthorizationDisabled() throws Exception {
        // Arrange
        String apiKey = "apiKey123";
        String storeId = "store-uuid";
        String targetPlantId = "07c52485-aef1-4bae-aaa0-80b5f6db0950";
        String userOperatorId = "123e4567-e89b-12d3-a456-426614174000";
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
        PutPlantStatusRequest requestBody = new PutPlantStatusRequest(
                updatedAt, effectiveStartDate, effectiveEndDate, deletedFlag
        );

        PlantResult plantResult = new PlantResult();
        plantResult.setOperatorId(userOperatorId);
        plantResult.setPlantId(targetPlantId);
        plantResult.setPlantName("Test Plant");
        plantResult.setPlantAddress("Test Address");
        plantResult.setOpenPlantId("OPENID12345");
        plantResult.setGlobalPlantId("GLOBAL-ID");
        plantResult.setDeletedFlag(true);
        plantResult.setEffectiveStartDate(LocalDate.parse(effectiveStartDate, DateTimeFormatter.ISO_DATE));
        plantResult.setEffectiveEndDate(LocalDate.parse(effectiveEndDate, DateTimeFormatter.ISO_DATE));
        plantResult.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        plantResult.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));

        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(userOperatorId);
        when(plantService.updatePlantStatus(anyString(), any(), any(), any(), any(), any()))
                .thenReturn(plantResult);

        // Act
        ResponseEntity<APIResponse<Object>> responseEntity = plantController.putPlantStatus(
                request, targetPlantId, requestBody, bindingResult);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        APIResponse<Object> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());

        // Verify apiKeyService.getUsecaseStoreId() is NOT called when auth is disabled (optimization)
        verify(apiKeyService, times(0)).getUsecaseStoreId(any());

        // Verify the service was called without auth
        verify(plantService).updatePlantStatus(anyString(), any(), any(), any(), any(), any());
        verify(plantService, times(0)).updatePlantStatusWithAuth(anyString(), any(), any(), any(), any(), any(), any());

        PlantResponse response = (PlantResponse) apiResponse.getData();
        assertEquals(targetPlantId, response.getPlantId());
        assertEquals(true, response.isDeletedFlag());
    }

    /**
     * case#01:
     * testGetPlantSuccess tests the successful retrieval of plant.
     */
    @Test
    void testGetPlantSuccess() {

        // Arrange
        String plantId = "28919692-AD5F-410E-9567-D99555BBDA61";

        PlantResult serviceResult = new PlantResult(
                plantId,
                "operatorId123",
                "plantName123",
                "plantAddr123",
                "openOperatorId123",
                "globalOperatorId123",
                false,
                LocalDate.now(ZoneOffset.UTC),
                LocalDate.now(ZoneOffset.UTC).plusDays(1),
                LocalDateTime.now(ZoneOffset.UTC),
                "createOperatorId123",
                LocalDateTime.now(ZoneOffset.UTC),
                "updateOperatorId123"
        );

        String apiKey = "test-api-key";
        String storeId = "test-store-id";
        when(httpServletRequest.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        when(apiKeyService.getUsecaseStoreId(apiKey)).thenReturn(storeId);
        when(plantService.getPlantWithAuth(eq(plantId), any(), any())).thenReturn(serviceResult);

        // Act
        ResponseEntity<Object> responseEntity = plantController.getPlant(httpServletRequest, plantId);

        // Assert
        assertEquals(200, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
        APIResponse<?> apiResponse = (APIResponse<?>) responseEntity.getBody();
        PlantResponse response = (PlantResponse) apiResponse.getData();
        assertEquals(serviceResult.getOperatorId(), response.getOperatorId());
        assertEquals(plantId, response.getPlantId());
        assertEquals(serviceResult.getPlantName(), response.getPlantName());
        assertEquals(serviceResult.getPlantAddress(), response.getPlantAddress());
        assertEquals(serviceResult.getOpenPlantId(), response.getOpenPlantId());
        assertEquals(serviceResult.getGlobalPlantId(), response.getGlobalPlantId());
        assertEquals(serviceResult.getEffectiveStartDate(), response.getEffectiveStartDate());
        assertEquals(serviceResult.getEffectiveEndDate(), response.getEffectiveEndDate());
        assertEquals(serviceResult.getCreatedAt(), response.getCreatedAt());
        assertEquals(serviceResult.getUpdatedAt(), response.getUpdatedAt());
    }

    /**
     * Test for getPlant - Authorization false.
     */
    @Test
    void testGetPlantAuthDenied() throws Exception {
        String plantId = "op-deny";
        String userId = "test-user";
        when(jwtService.getOperatorOrOpenSystemId(httpServletRequest)).thenReturn(userId);
        when(plantService.getPlantWithAuth(eq(plantId), any(), any()))
                .thenThrow(new IllegalAuthDataException("forbidden", "Forbidden"));

        IllegalAuthDataException ex = assertThrows(
                IllegalAuthDataException.class,
                () -> plantController.getPlant(httpServletRequest, plantId)
        );
        assertNotNull(ex.getResponseMessage());
    }

    /**
     * case#02:
     * testGetPlantSuccessEmpty tests the successful retrieval of an plant.
     */
    @Test
    void testGetPlantSuccessEmpty() {
        // Arrange
        String plantId = "123e4567-e89b-12d3-a456-426614174000";

        when(plantService.getPlantWithAuth(
            eq(plantId), any(), any()
        )).thenReturn(null);

        // Act
        ResponseEntity<Object> responseEntity = plantController.getPlant(httpServletRequest, plantId);

        // Assert
        assertEquals(200, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
    }

    /**
     * case#03:
     * testGetPlantExceptionDuringOperatorCreation tests the exception handling during operator creation.
     */
    @Test
    void testGetPlantExceptionDuringPlantCreation() {
        // Arrange
        String plantId = "28919692-AD5F-410E-9567-D99555BBDA61";

        when(plantService.getPlantWithAuth(
            eq(plantId), any(), any()
        )).thenThrow(new RuntimeException("DB connection error"));

        // Act
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            plantController.getPlant(httpServletRequest, plantId);
        });
        // Assert: controller wraps original message; verify it contains original cause message
        assertTrue(exception.getMessage().contains("DB connection error"));
        verify(plantService, times(1)).getPlantWithAuth(eq(plantId), any(), any());
    }

    /**
     * Test for getPlant with authorization disabled.
     */
    @Test
    void testGetPlantWithAuthorizationDisabled() throws Exception {
        // Arrange
        String plantId = "28919692-AD5F-410E-9567-D99555BBDA61";
        String userId = "test-user";

        // Set isEnableUcAuthorization to false for this test
        when(odsProperties.isEnableUcAuthorization()).thenReturn(false);

        PlantResult serviceResult = new PlantResult(
                plantId,
                "operatorId123",
                "plantName123",
                "plantAddr123",
                "openOperatorId123",
                "globalOperatorId123",
                false,
                LocalDate.now(ZoneOffset.UTC),
                LocalDate.now(ZoneOffset.UTC).plusDays(1),
                LocalDateTime.now(ZoneOffset.UTC),
                "createOperatorId123",
                LocalDateTime.now(ZoneOffset.UTC),
                "updateOperatorId123"
        );

        when(jwtService.getOperatorOrOpenSystemId(httpServletRequest)).thenReturn(userId);
        when(plantService.getPlant(eq(plantId))).thenReturn(serviceResult);

        // Act
        ResponseEntity<Object> responseEntity = plantController.getPlant(httpServletRequest, plantId);

        // Assert
        assertEquals(200, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
        APIResponse<?> apiResponse = (APIResponse<?>) responseEntity.getBody();
        PlantResponse response = (PlantResponse) apiResponse.getData();

        // Verify the service was called without auth
        verify(plantService).getPlant(eq(plantId));
        verify(plantService, times(0)).getPlantWithAuth(anyString(), anyString(), anyString());

        assertEquals(serviceResult.getOperatorId(), response.getOperatorId());
        assertEquals(plantId, response.getPlantId());
        assertEquals(serviceResult.getPlantName(), response.getPlantName());
    }

    /**
     * ParameterizedTest for listPlant.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // operatorId, plantId, plantName, plantAddress, openPlantId, globalPlantId, deletedFlag, effectiveDate, key, order, expectedCount
        "VALUE,VALUE,UUID,UUID, VALUE, VALUE, VALUE, VALUE, VALUE, VALUE, VALUE, VALUE", // case#1: normal case
        "VALUE,VALUE,UUID,UUID, a, a, a, '', VALUE, VALUE, VALUE, VALUE", // case#2: min length
        "VALUE,VALUE,UUID,UUID, LONG, LONG, LONG, LONG, VALUE, LONG, VALUE, VALUE", // case#3: max length
        "VALUE,VALUE,UUID,UUID, VALUE, VALUE, VALUE, NULL, VALUE, VALUE, VALUE, VALUE", // case#1: normal case
    }, nullValues = "NULL")
    void testListPlant_success(
            String argCount,
            String argIndex,
            String argOperatorId,
            String argPlantId,
            String argPlantName,
            String argPlantAddress,
            String argOpenPlantId,
            String argGlobalPlantId,
            String argDeletedFlag,
            String argEffectiveDate,
            String argSortKey,
            String argSortOrder
    ) throws Exception {
        // Convert special input values
        Integer count = "VALUE".equals(argCount) ? commonCount :
                Integer.valueOf(argCount);
        Integer index = "VALUE".equals(argIndex) ? commonIndex :
                Integer.valueOf(argIndex);
        String operatorId =
                "UUID".equals(argOperatorId) ? commonOperatorId :
                argOperatorId;
        String plantId =
                "UUID".equals(argPlantId) ? commonPlantId :
                argPlantId;
        String plantName =
                "LONG".equals(argPlantName) ? "E".repeat(Const.OPERATOR_NAME_LENGTH_MAX) :
                "VALUE".equals(argPlantName) ? commonPlantName :
                argPlantName;
        String plantAddress =
                "LONG".equals(argPlantAddress) ? "E".repeat(Const.OPERATOR_ADDRESS_LENGTH_MAX) :
                "VALUE".equals(argPlantAddress) ? commonPlantAddress :
                argPlantAddress;
        String openPlantId =
                "LONG".equals(argOpenPlantId) ? "E".repeat(Const.OPEN_OPERATOR_ID_LENGTH_MAX) :
                "VALUE".equals(argOpenPlantId) ? commonOpenPlantId :
                argOpenPlantId;
        String globalPlantId =
                "LONG".equals(argGlobalPlantId) ? "E".repeat(Const.GLOBAL_OPERATOR_ID_LENGTH_MAX) :
                "VALUE".equals(argGlobalPlantId) ? commonGlobalPlantId :
                argGlobalPlantId;
        Boolean deletedFlag =
                "VALUE".equals(argDeletedFlag) ? commonDeletedFlag :
                Boolean.valueOf(argDeletedFlag);
        String effectiveDate =
                "VALUE".equals(argEffectiveDate) ? commonEffectiveDate :
                "LONG".equals(argGlobalPlantId) ? "9999-12-31" :
                argEffectiveDate;
        String sortKey = "VALUE".equals(argSortKey) ? commonSortKey :
                argSortKey;
        String sortOrder = "VALUE".equals(argSortOrder) ? commonSortOrder :
                argSortOrder;

        // Arrange
        ListPlantRequest listPlantRequest = new ListPlantRequest();
        listPlantRequest.setCount(count);
        listPlantRequest.setIndex(index);
        listPlantRequest.setSort(new ListPlantRequest.SortKey(sortKey, sortOrder));
        listPlantRequest.setOperatorId(operatorId);
        listPlantRequest.setPlantId(plantId);
        listPlantRequest.setPlantName(plantName);
        listPlantRequest.setPlantAddress(plantAddress);
        listPlantRequest.setOpenPlantId(openPlantId);
        listPlantRequest.setGlobalPlantId(globalPlantId);
        listPlantRequest.setEffectiveDate(effectiveDate);
        listPlantRequest.setDeletedFlag(deletedFlag);
        // Convert request object to JSON string
        String rawJson = objectMapper.writeValueAsString(listPlantRequest);

        PlantResult plantResult = new PlantResult();
        plantResult.setOperatorId(commonOperatorId);
        plantResult.setPlantId(commonPlantId);
        plantResult.setPlantName(commonPlantName);
        plantResult.setPlantAddress(commonPlantAddress);
        plantResult.setOpenPlantId(commonOpenPlantId);
        plantResult.setGlobalPlantId(commonGlobalPlantId);
        plantResult.setDeletedFlag(commonDeletedFlag);
        plantResult.setEffectiveStartDate(commonEffectiveStartDate);
        plantResult.setEffectiveEndDate(commonEffectiveEndDate);
        plantResult.setCreatedAt(commonCreatedAt);
        plantResult.setUpdatedAt(commonUpdatedAt);
        List<PlantResult> listPlantResult = List.of(plantResult);

        HttpServletRequest request = mock(HttpServletRequest.class);
        // Mock the validator to always return no violations
        Validator validator = mock(Validator.class);
        // Suppress warnings for unchecked casts
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<ListPlantRequest>> violations = mock(Set.class);

        // Inject the mock validator into the controller using reflection
        Field validatorField = PlantController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(plantController, validator);
        // Arrange
        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(ListPlantRequest.class))).thenReturn(violations);
        when(plantService.searchPlantsWithAuth(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(), anyString(),
                any(StateString.class), anyString(), anyString(), anyBoolean(), any(LocalDate.class), any(), any()))
                .thenReturn(listPlantResult);

        // Act
        ResponseEntity<APIResponse<List<GetPlantResponse>>> responseEntity = plantController.listPlant(
                request, rawJson);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        APIResponse<List<GetPlantResponse>> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertTrue(EnumResponseTypes.getResponseType(HttpStatus.OK.value()).equals(apiResponse.getType()));

        assertNotNull(apiResponse.getData());

        assertFalse(apiResponse.getData().isEmpty(), "Data list should not be empty");

        GetPlantResponse resp = apiResponse.getData().get(0);
        assertEquals(commonPlantId, resp.getPlantId());
        assertEquals(commonPlantName, resp.getPlantName());
        assertEquals(commonPlantAddress, resp.getPlantAddress());
        assertEquals(commonOpenPlantId, resp.getOpenPlantId());
        assertEquals(commonGlobalPlantId, resp.getGlobalPlantId());
        assertEquals(commonEffectiveStartDate, resp.getEffectiveStartDate());
        assertEquals(commonEffectiveEndDate, resp.getEffectiveEndDate());
        assertEquals(commonCreatedAt, resp.getCreatedAt());
        assertEquals(commonUpdatedAt, resp.getUpdatedAt());

        verify(plantService).searchPlantsWithAuth(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(), anyString(),
                any(StateString.class), anyString(), anyString(), anyBoolean(), any(LocalDate.class), any(), any());
    }

    /**
     * Test for listOperator - Not required values.
     */
    @Test
    @DisplayName("testListPlant - Not required values")
    void testListPlant_notRequiredValues() throws Exception {
        // Specify only the required fields
        ListPlantRequest listPlantRequest = new ListPlantRequest();
        listPlantRequest.setCount(commonCount);
        listPlantRequest.setIndex(commonIndex);
        // Convert request object to JSON string
        String rawJson = objectMapper.writeValueAsString(listPlantRequest);

        PlantResult plantResult = new PlantResult();
        plantResult.setPlantId(commonPlantId);
        plantResult.setPlantName(commonPlantName);
        plantResult.setPlantAddress(commonPlantAddress);
        plantResult.setOpenPlantId(commonOpenPlantId);
        plantResult.setGlobalPlantId(commonGlobalPlantId);
        plantResult.setDeletedFlag(commonDeletedFlag);
        plantResult.setEffectiveStartDate(commonEffectiveStartDate);
        plantResult.setEffectiveEndDate(commonEffectiveEndDate);
        plantResult.setCreatedAt(commonCreatedAt);
        plantResult.setUpdatedAt(commonUpdatedAt);
        List<PlantResult> listPlantResult = List.of(plantResult);
        HttpServletRequest request = mock(HttpServletRequest.class);
        String apiKey = "test-api-key";
        String storeId = "test-store-id";
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        when(apiKeyService.getUsecaseStoreId(apiKey)).thenReturn(storeId);

        // Mock the validator to always return no violations
        Validator validator = mock(Validator.class);
        // Suppress warnings for unchecked casts
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<ListPlantRequest>> violations = mock(Set.class);

        // Inject the mock validator into the controller using reflection
        Field validatorField = PlantController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(plantController, validator);

        // Arrange
        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(ListPlantRequest.class))).thenReturn(violations);
        when(plantService.searchPlantsWithAuth(anyInt(), anyInt(), isNull(), isNull(), isNull(), isNull(), isNull(),
                any(StateString.class), isNull(), isNull(), isNull(), isNull(), any(), any()))
                .thenReturn(listPlantResult);

        // Act
        ResponseEntity<APIResponse<List<GetPlantResponse>>> responseEntity = plantController.listPlant(
                request, rawJson);
        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        APIResponse<List<GetPlantResponse>> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertTrue(EnumResponseTypes.getResponseType(HttpStatus.OK.value()).equals(apiResponse.getType()));
        assertNotNull(apiResponse.getData());
        assertFalse(apiResponse.getData().isEmpty(), "Data list should not be empty");

        GetPlantResponse resp = apiResponse.getData().get(0);
        assertEquals(commonPlantId, resp.getPlantId());
        assertEquals(commonPlantName, resp.getPlantName());
        assertEquals(commonPlantAddress, resp.getPlantAddress());
        assertEquals(commonOpenPlantId, resp.getOpenPlantId());
        assertEquals(commonGlobalPlantId, resp.getGlobalPlantId());
        assertEquals(commonEffectiveStartDate, resp.getEffectiveStartDate());
        assertEquals(commonEffectiveEndDate, resp.getEffectiveEndDate());
        assertEquals(commonCreatedAt, resp.getCreatedAt());
        assertEquals(commonUpdatedAt, resp.getUpdatedAt());

        verify(plantService).searchPlantsWithAuth(anyInt(), anyInt(), isNull(), isNull(), isNull(), isNull(), isNull(),
                any(StateString.class), isNull(), isNull(), isNull(), isNull(), any(), any());
    }

    /**
     * case#6:
     * Test for listPlant when no plants are found.
     */
    @Test
    void testListPlant_returnsEmpty() throws Exception {
        // Arrange
        ListPlantRequest listPlantRequest = new ListPlantRequest();
        listPlantRequest.setCount(commonCount);
        listPlantRequest.setIndex(commonIndex);
        listPlantRequest.setSort(new ListPlantRequest.SortKey(commonSortKey, commonSortOrder));
        listPlantRequest.setOperatorId(commonOperatorId);
        listPlantRequest.setPlantId(commonPlantId);
        listPlantRequest.setPlantName(commonPlantName);
        listPlantRequest.setPlantAddress(commonPlantAddress);
        listPlantRequest.setOpenPlantId(commonOpenPlantId);
        listPlantRequest.setGlobalPlantId(commonGlobalPlantId);
        listPlantRequest.setEffectiveDate(commonEffectiveDate);
        listPlantRequest.setDeletedFlag(commonDeletedFlag);
        // Convert request object to JSON string
        String rawJson = objectMapper.writeValueAsString(listPlantRequest);

        List<PlantResult> listPlantResult = Collections.emptyList();

        HttpServletRequest request = mock(HttpServletRequest.class);
        // Mock the validator to always return no violations
        Validator validator = mock(Validator.class);
        // Suppress warnings for unchecked casts
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<ListPlantRequest>> violations = mock(Set.class);

        // Inject the mock validator into the controller using reflection
        Field validatorField = PlantController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(plantController, validator);
        // Arrange
        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(ListPlantRequest.class))).thenReturn(violations);
        when(plantService.searchPlantsWithAuth(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(), anyString(),
                any(StateString.class), anyString(), anyString(), anyBoolean(), any(LocalDate.class), any(), any()))
                .thenReturn(listPlantResult);

        // Act
        ResponseEntity<APIResponse<List<GetPlantResponse>>> response = plantController.listPlant(
                request, rawJson);
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        APIResponse<List<GetPlantResponse>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertNotNull(apiResponse.getData());
        assertEquals(0, apiResponse.getData().size());

        verify(plantService).searchPlantsWithAuth(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(), anyString(),
                any(StateString.class), anyString(), anyString(), anyBoolean(), any(LocalDate.class), any(), any());
    }

    /**
     * Test for listPlant - Validation error.
     */
    @Test
    @DisplayName("listPlant - Validation Error")
    void testListPlant_validationError() throws Exception {
        // Arrange
        ListPlantRequest listPlantRequest = new ListPlantRequest();
        listPlantRequest.setCount(commonCount);
        listPlantRequest.setIndex(commonIndex);
        listPlantRequest.setSort(new ListPlantRequest.SortKey(commonSortKey, commonSortOrder));
        listPlantRequest.setPlantId(commonPlantId);
        listPlantRequest.setPlantId(commonPlantId);
        listPlantRequest.setPlantName(commonPlantName);
        listPlantRequest.setPlantAddress(commonPlantAddress);
        listPlantRequest.setOpenPlantId(commonOpenPlantId);
        listPlantRequest.setGlobalPlantId(commonGlobalPlantId);
        listPlantRequest.setEffectiveDate(commonEffectiveDate);
        listPlantRequest.setDeletedFlag(commonDeletedFlag);
        // Convert request object to JSON string
        String rawJson = objectMapper.writeValueAsString(listPlantRequest);
        HttpServletRequest request = mock(HttpServletRequest.class);

        // Mock the validator to always return violations
        Validator validator = mock(Validator.class);
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<ListPlantRequest>> violations = mock(Set.class);

        // Mock the validator to always return no violations
        when(violations.isEmpty()).thenReturn(false);
        when(validator.validate(any(ListPlantRequest.class))).thenReturn(violations);

        // Inject the mock validator into the controller using reflection
        Field validatorField = PlantController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(plantController, validator);

        // Act & Assert
        ValidateException exception = assertThrows(ValidateException.class, () ->
                plantController.listPlant(request, rawJson)
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
    void testListPlant_throwException(String exceptionClassName) throws Exception {
        // Arrange
        ListPlantRequest listPlantRequest = new ListPlantRequest();
        listPlantRequest.setCount(commonCount);
        listPlantRequest.setIndex(commonIndex);
        listPlantRequest.setSort(new ListPlantRequest.SortKey(commonSortKey, commonSortOrder));
        listPlantRequest.setOperatorId(commonOperatorId);
        listPlantRequest.setPlantId(commonPlantId);
        listPlantRequest.setPlantName(commonPlantName);
        listPlantRequest.setPlantAddress(commonPlantAddress);
        listPlantRequest.setOpenPlantId(commonOpenPlantId);
        listPlantRequest.setGlobalPlantId(commonGlobalPlantId);
        listPlantRequest.setEffectiveDate(commonEffectiveDate);
        listPlantRequest.setDeletedFlag(commonDeletedFlag);
        // Convert request object to JSON string
        String rawJson = objectMapper.writeValueAsString(listPlantRequest);
        HttpServletRequest request = mock(HttpServletRequest.class);

        // Mock the validator to always return no violations
        Validator validator = mock(Validator.class);
        // Suppress warnings for unchecked casts
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<ListOperatorRequest>> violations = mock(Set.class);

        // Inject the mock validator into the controller using reflection
        Field validatorField = PlantController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(plantController, validator);

        // Prepare exception to be thrown by identityProviderService.deleteAccount
        Exception exception = null;
        Class<? extends Throwable> clazz = null;
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

        when(plantService.searchPlantsWithAuth(anyInt(), anyInt(), any(), any(), any(), any(), any(),
            any(StateString.class), any(), any(), any(), any(LocalDate.class), any(), any()
            )).thenThrow(exception);
        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(ListOperatorRequest.class))).thenReturn(violations);

        // Act & Assert
        Throwable thrownException = assertThrows(clazz, () -> {
            plantController.listPlant(request, rawJson);;
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
        verify(plantService).searchPlantsWithAuth(anyInt(), anyInt(), any(), any(), any(), any(), any(),
                any(StateString.class), any(), any(), any(), any(LocalDate.class), any(), any());
    }

    /**
     * Test for listPlant with authorization disabled.
     */
    @Test
    @DisplayName("listPlant with authorization disabled")
    void testListPlant_withAuthorizationDisabled() throws Exception {
        // Set isEnableUcAuthorization to false for this test
        when(odsProperties.isEnableUcAuthorization()).thenReturn(false);

        ListPlantRequest listPlantRequest = new ListPlantRequest();
        listPlantRequest.setCount(commonCount);
        listPlantRequest.setIndex(commonIndex);
        listPlantRequest.setSort(new ListPlantRequest.SortKey(commonSortKey, commonSortOrder));
        listPlantRequest.setOperatorId(commonOperatorId);
        listPlantRequest.setPlantId(commonPlantId);
        listPlantRequest.setPlantName(commonPlantName);
        listPlantRequest.setPlantAddress(commonPlantAddress);
        listPlantRequest.setOpenPlantId(commonOpenPlantId);
        listPlantRequest.setGlobalPlantId(commonGlobalPlantId);
        listPlantRequest.setEffectiveDate(commonEffectiveDate);
        listPlantRequest.setDeletedFlag(commonDeletedFlag);
        // Convert request object to JSON string
        String rawJson = objectMapper.writeValueAsString(listPlantRequest);

        PlantResult plantResult = new PlantResult();
        plantResult.setOperatorId(commonOperatorId);
        plantResult.setPlantId(commonPlantId);
        plantResult.setPlantName(commonPlantName);
        plantResult.setPlantAddress(commonPlantAddress);
        plantResult.setOpenPlantId(commonOpenPlantId);
        plantResult.setGlobalPlantId(commonGlobalPlantId);
        plantResult.setDeletedFlag(commonDeletedFlag);
        plantResult.setEffectiveStartDate(commonEffectiveStartDate);
        plantResult.setEffectiveEndDate(commonEffectiveEndDate);
        plantResult.setCreatedAt(commonCreatedAt);
        plantResult.setUpdatedAt(commonUpdatedAt);
        List<PlantResult> listPlantResult = List.of(plantResult);

        HttpServletRequest request = mock(HttpServletRequest.class);
        // Mock the validator to always return no violations
        Validator validator = mock(Validator.class);
        // Suppress warnings for unchecked casts
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<ListPlantRequest>> violations = mock(Set.class);

        // Inject the mock validator into the controller using reflection
        Field validatorField = PlantController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(plantController, validator);
        // Arrange
        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(ListPlantRequest.class))).thenReturn(violations);
        when(plantService.searchPlants(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(), anyString(),
                any(StateString.class), anyString(), anyString(), anyBoolean(), any(LocalDate.class)))
                .thenReturn(listPlantResult);

        // Act
        ResponseEntity<APIResponse<List<GetPlantResponse>>> responseEntity = plantController.listPlant(
                request, rawJson);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        APIResponse<List<GetPlantResponse>> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());
        assertFalse(apiResponse.getData().isEmpty(), "Data list should not be empty");

        GetPlantResponse resp = apiResponse.getData().get(0);
        assertEquals(commonPlantId, resp.getPlantId());
        assertEquals(commonPlantName, resp.getPlantName());

        // Verify apiKeyService.getUsecaseStoreId() is NOT called when auth is disabled (optimization)
        verify(apiKeyService, times(0)).getUsecaseStoreId(any());

        // Verify the service was called without auth
        verify(plantService).searchPlants(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(), anyString(),
                any(StateString.class), anyString(), anyString(), anyBoolean(), any(LocalDate.class));
        verify(plantService, times(0)).searchPlantsWithAuth(anyInt(), anyInt(), any(), any(), any(), any(), any(),
                any(StateString.class), any(), any(), any(), any(LocalDate.class), any(), any());
    }

    /**
     * Test setRawJson with valid JSON string.
     */
    @Test
    @DisplayName("Test setRawJson Success")
    void testSetRawJson_success() {
        String rowJson = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
        APIJSONRequest apiJsonRequest = new APIJSONRequest();
        // Arrange
        apiJsonRequest.setRawJson(rowJson);
        // Assert
        assertNotNull(apiJsonRequest.getJsonProperties());
        assertEquals(2, apiJsonRequest.getJsonProperties().size());
        assertTrue(apiJsonRequest.getJsonProperties().contains("key1"));
        assertTrue(apiJsonRequest.getJsonProperties().contains("key2"));
    }

    /**
     * Test setRawJson with null, empty, or invalid JSON strings.
     */
    @Test
    @DisplayName("Test setRawJson null")
    void testSetRawJson_null() {
        String rowJson = null;
        APIJSONRequest apiJsonRequest = new APIJSONRequest();
        // Arrange
        apiJsonRequest.setRawJson(rowJson);
        // Assert
        assertNotNull(apiJsonRequest.getJsonProperties());
        assertEquals(0, apiJsonRequest.getJsonProperties().size());
    }

    /**
     * Test setRawJson with empty string.
     */
    @Test
    @DisplayName("Test setRawJson empty")
    void testSetRawJson_emptyRawJson() {
        String rowJson = "";
        APIJSONRequest apiJsonRequest = new APIJSONRequest();
        // Arrange
        apiJsonRequest.setRawJson(rowJson);
        // Assert
        assertNotNull(apiJsonRequest.getJsonProperties());
        assertEquals(0, apiJsonRequest.getJsonProperties().size());
    }

    /**
     * Test setRawJson with invalid JSON format.
     */
    @Test
    @DisplayName("Test setRawJson not json format")
    void testSetRawJson_notJsonFormat() {

        // Missing ending brace }
        String rowJson = "{\"key1\":\"value1\",\"key2\":\"value2\"";
        APIJSONRequest apiJsonRequest = new APIJSONRequest();
        // Arrange
        apiJsonRequest.setRawJson(rowJson);
        // Assert
        assertNotNull(apiJsonRequest.getJsonProperties());
        assertEquals(0, apiJsonRequest.getJsonProperties().size());
    }

    /**
    * Test hasProperty with existing and non-existing properties.
    */
    @Test
    @DisplayName("Test hasProperty Success")
    void testHasProperty_success() {
        APIJSONRequest apiJsonRequest = new APIJSONRequest();
        apiJsonRequest.setRawJson("{\"key1\":\"value1\",\"key2\":\"value2\"}");
        // Arrange & Assert
        assertTrue(apiJsonRequest.hasProperty("key1"));
        assertTrue(apiJsonRequest.hasProperty("key2"));
        assertFalse(apiJsonRequest.hasProperty("notExistKey"));
    }

    /**
     * Test hasProperty with null jsonProperties.
     */
    @Test
    @DisplayName("Test hasProperty null")
    void testHasProperty_null()throws Exception {
        APIJSONRequest req = new APIJSONRequest();
        var field = APIJSONRequest.class.getDeclaredField("jsonProperties");
        field.setAccessible(true);
        field.set(req, null);
        assertFalse(req.hasProperty("anyKey"));
    }

    @Test
    @DisplayName("Test buildStateStringForProperty Success")
    void testBuildStateStringForProperty_success() {
        APIJSONRequest req = new APIJSONRequest();
        req.setRawJson("{\"key1\":\"value1\",\"key2\":\"value2\"}");
        StateString expected = StateString.of("value1");
        // Arrange
        StateString stateString = req.buildStateStringForProperty("key1", "value1");
        // Assert
        assertNotNull(stateString);
        assertEquals(expected, stateString);
    }

    @Test
    @DisplayName("Test buildStateStringForProperty Success")
    void testBuildStateStringForProperty_emptyStateString() {
        APIJSONRequest req = new APIJSONRequest();
        StateString expected = new StateString();
        // Arrange
        StateString stateString = req.buildStateStringForProperty("key1", "value1");
        // Assert
        assertEquals(expected, stateString);
    }

    /**
     * Test for PostPlant - Success case.
     */
    @ParameterizedTest
    @CsvSource({
        // Typical case
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OPENID123456,GlobalId1,2025-08-30,2025-12-31",
        // Length boundary cases
        "d9a38406-cae2-4679-b052-15a75f5531e6,o,Address1,OpenId123456,GlobalId1,2025-08-30,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,LONG,Address1,OpenId123456,GlobalId1,2025-08-30,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,A,OpenId123456,GlobalId1,2025-08-30,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,LONG,OpenId123456,GlobalId1,2025-08-30,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,012345,GlobalId1,2025-08-30,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,01234567890123456789012345,GlobalId1,2025-08-30,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,LONG,2025-08-30,2025-12-31",
        // Pattern boundary cases
        "d9a38406-cae2-4679-b052-15a75f5531e7,Plant1,Address1,OpenId123456,GlobalId1,2025-08-30,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId012345,GlobalId1,2025-08-30,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,GlobalId1,0001-01-01,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,GlobalId1,2025-08-30,9999-12-31",
        // Not required values
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,NULL,2025-08-30,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,'',2025-08-30,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,' ',2025-08-30,2025-12-31" })
    @DisplayName("postPlant - Success Case")
    void testPostPlantSuccess(
            String operatorId,
            String plantName,
            String plantAddress,
            String openPlantId,
            String globalPlantId,
            String startDate,
            String endDate) throws Exception {
        // Arrange
        String creatorOperatorId = "creator123";
        LocalDateTime now = LocalDateTime.now();

        HttpServletRequest request = mock(HttpServletRequest.class);
        PostPlantRequest requestBody = new PostPlantRequest(
                operatorId, plantName, plantAddress, openPlantId, globalPlantId, startDate, endDate);

        // Handle "NULL", empty, and "LONG" inputs
        globalPlantId = "NULL".equals(globalPlantId) || globalPlantId.isEmpty() ? null : globalPlantId;
        plantAddress = "NULL".equals(plantAddress) || plantAddress.isEmpty() ? null : plantAddress;
        plantName = "LONG".equals(plantName) ? "a".repeat(Const.PLANT_NAME_LENGTH_MAX) : plantName;
        plantAddress = "LONG".equals(plantAddress) ? "a".repeat(Const.PLANT_ADDRESS_LENGTH_MAX) : plantAddress;
        globalPlantId = "LONG".equals(globalPlantId) ? "a".repeat(Const.GLOBAL_PLANT_ID_LENGTH_MAX) : globalPlantId;

        PlantResult plantResult = new PlantResult();
        plantResult.setPlantId("generated-plant-id");
        plantResult.setOperatorId(operatorId);
        plantResult.setPlantName(plantName);
        plantResult.setPlantAddress(plantAddress);
        plantResult.setOpenPlantId(openPlantId);
        plantResult.setGlobalPlantId(globalPlantId);
        plantResult.setEffectiveStartDate(LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE));
        plantResult.setEffectiveEndDate(LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE));
        plantResult.setDeletedFlag(false);
        plantResult.setCreatedAt(now);
        plantResult.setUpdatedAt(now);
        plantResult.setCreatedUserId(creatorOperatorId);
        plantResult.setUpdatedUserId(creatorOperatorId);

        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(creatorOperatorId);
        when(plantService.addPlantWithAuth(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(plantResult);

        // Act
        ResponseEntity<APIResponse<PlantResponse>> responseEntity = null;
        try {
            responseEntity = plantController.postPlant(
                    request, requestBody, mock(BindingResult.class));
        } catch (Exception e) {
            // Handle exception if any
            assertEquals(false, "Exception thrown during test execution: " + e.getMessage());
            return;
        }

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        APIResponse<PlantResponse> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.CREATED.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());
        assertEquals("generated-plant-id", apiResponse.getData().getPlantId());
        assertEquals(operatorId, apiResponse.getData().getOperatorId());
        assertEquals(plantName, apiResponse.getData().getPlantName());
        assertEquals(plantAddress, apiResponse.getData().getPlantAddress());
        assertEquals(openPlantId, apiResponse.getData().getOpenPlantId());
        assertEquals(globalPlantId, apiResponse.getData().getGlobalPlantId());
        assertEquals(LocalDate.parse(startDate).toString(), apiResponse.getData().getEffectiveStartDate().toString());
        assertEquals(LocalDate.parse(endDate).toString(), apiResponse.getData().getEffectiveEndDate().toString());
        assertFalse(apiResponse.getData().isDeletedFlag());
        assertNotNull(apiResponse.getData().getCreatedAt());
        assertNotNull(apiResponse.getData().getUpdatedAt());
    }

    /**
     * Test for postPlant - Success case without not required values.
     */
    @Test
    @DisplayName("postPlant - Success case without not required values")
    void testPostPlantSuccessWithoutNotRequired() throws Exception {
        // Arrange
        String creatorOperatorId = "creator123";
        String plantName = "Plant Name";
        String plantAddress = "123 Main St";
        String openPlantId = "open123";
        String operatorId = "user123";
        String endDate = "9999-12-31";
        LocalDateTime now = LocalDateTime.now();
        LocalDate nowDate = now.toLocalDate();

        HttpServletRequest request = mock(HttpServletRequest.class);
        PostPlantRequest requestBody = new PostPlantRequest();
        requestBody.setOperatorId(operatorId);
        requestBody.setPlantName(plantName);
        requestBody.setPlantAddress(plantAddress);
        requestBody.setOpenPlantId(openPlantId);

        PlantResult plantResult = new PlantResult();
        plantResult.setPlantId("generated-plant-id");
        plantResult.setOperatorId(operatorId);
        plantResult.setPlantName(plantName);
        plantResult.setPlantAddress(plantAddress);
        plantResult.setOpenPlantId(openPlantId);
        plantResult.setGlobalPlantId(null);
        plantResult.setEffectiveStartDate(nowDate);
        plantResult.setEffectiveEndDate(LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE));
        plantResult.setDeletedFlag(false);
        plantResult.setCreatedAt(now);
        plantResult.setUpdatedAt(now);
        plantResult.setCreatedUserId(creatorOperatorId);
        plantResult.setUpdatedUserId(creatorOperatorId);

        String apiKey = "test-api-key";
        String storeId = "test-store-id";
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        when(apiKeyService.getUsecaseStoreId(apiKey)).thenReturn(storeId);
        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(creatorOperatorId);
        when(plantService.addPlantWithAuth(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(plantResult);

        // Act
        ResponseEntity<APIResponse<PlantResponse>> responseEntity = plantController.postPlant(
                request, requestBody, mock(BindingResult.class));

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        APIResponse<PlantResponse> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.CREATED.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());
        assertNotNull(apiResponse.getData());
        assertEquals("generated-plant-id", apiResponse.getData().getPlantId());
        assertEquals(operatorId, apiResponse.getData().getOperatorId());
        assertEquals(plantName, apiResponse.getData().getPlantName());
        assertEquals(plantAddress, apiResponse.getData().getPlantAddress());
        assertEquals(openPlantId, apiResponse.getData().getOpenPlantId());
        assertEquals(null, apiResponse.getData().getGlobalPlantId());
        assertEquals(nowDate.toString(), apiResponse.getData().getEffectiveStartDate().toString());
        assertEquals(LocalDate.parse(endDate).toString(), apiResponse.getData().getEffectiveEndDate().toString());
        assertFalse(apiResponse.getData().isDeletedFlag());
        assertNotNull(apiResponse.getData().getCreatedAt());
        assertNotNull(apiResponse.getData().getUpdatedAt());
    }

    /**
     * Test for postPlant - Validation error.
     */
    @Test
    @DisplayName("postPlant - Validation Error")
    void testPostPlantValidationError() throws Exception {
        // Arrange
        PostPlantRequest requestBody = new PostPlantRequest();
        when(bindingResult.hasErrors()).thenReturn(true);
        HttpServletRequest request = mock(HttpServletRequest.class);

        // Act & Assert
        ValidateException exception = assertThrows(ValidateException.class, () ->
                plantController.postPlant(request, requestBody, bindingResult)
        );
        assertEquals(ConstError.ERRLOG_400_INVALID_REQUEST, exception.getLogMessage());
        assertNotNull(exception.getMessage());
    }

    /**
     * Test for postPlant when plantService throws an exception.
     */
    @ParameterizedTest
    @CsvSource({
        // Exception
        "BadParametersException",
        "OutOfServiceException",
        "UnexpectedException",
        "RuntimeException"
    })
    @DisplayName("postPlant throws exception when plantService fails")
    void testPostPlantServiceThrowsException(String exceptionClassName) {
        // Arrange
        String creatorOperatorId = "creator123";

        when(bindingResult.hasErrors()).thenReturn(false);
        HttpServletRequest request = mock(HttpServletRequest.class);
        PostPlantRequest requestBody = new PostPlantRequest(
                "d9a38406-cae2-4679-b052-15a75f5531e6", "Plant Name", "123 Main St",
                "open123456", "global123", "2025-08-30", "2025-12-31");
        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(creatorOperatorId);

        // Prepare exception to be thrown by plantService
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
        // Mock plantService to throw an exception
        doThrow(exception).when(plantService).addPlantWithAuth(any(), any(), any(), any(), any(), any(), any(), any(), any());

        // Act & Assert
        Throwable thrownException = assertThrows(clazz, () -> {
            plantController.postPlant(request, requestBody, bindingResult);
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
        verify(plantService).addPlantWithAuth(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    /**
     * Test for postPlant - Authorization false.
     */
    @Test
    void testPostPlantAuthDenied() throws Exception {
        String apiKey = "apiKey123";
        String creatorOperatorId = "creator123";
        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(creatorOperatorId);

        PostPlantRequest requestBody = new PostPlantRequest();
        requestBody.setOperatorId("op123");
        requestBody.setPlantName("name");

        when(plantService.addPlantWithAuth(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalAuthDataException("forbidden", "Forbidden"));

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);

        IllegalAuthDataException ex = assertThrows(
                IllegalAuthDataException.class,
                () -> plantController.postPlant(request, requestBody, mock(BindingResult.class))
        );
        assertNotNull(ex.getResponseMessage());
    }

    /**
     * Test for postPlant with authorization disabled.
     */
    @Test
    void testPostPlantWithAuthorizationDisabled() throws Exception {
        // Arrange
        String apiKey = "apiKey123";
        String storeId = "store-uuid";
        String creatorOperatorId = "creator123";
        String operatorId = "d9a38406-cae2-4679-b052-15a75f5531e6";
        String plantName = "Plant Name";
        String plantAddress = "123 Main St";
        String openPlantId = "open123456";
        String globalPlantId = "global123";
        String startDate = "2025-08-30";
        String endDate = "2025-12-31";
        LocalDateTime now = LocalDateTime.now();

        // Set isEnableUcAuthorization to false for this test
        when(odsProperties.isEnableUcAuthorization()).thenReturn(false);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(apiKey);
        when(apiKeyService.getUsecaseStoreId(apiKey)).thenReturn(storeId);

        PostPlantRequest requestBody = new PostPlantRequest(
                operatorId, plantName, plantAddress, openPlantId, globalPlantId, startDate, endDate);

        PlantResult plantResult = new PlantResult();
        plantResult.setPlantId("generated-plant-id");
        plantResult.setOperatorId(operatorId);
        plantResult.setPlantName(plantName);
        plantResult.setPlantAddress(plantAddress);
        plantResult.setOpenPlantId(openPlantId);
        plantResult.setGlobalPlantId(globalPlantId);
        plantResult.setEffectiveStartDate(LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE));
        plantResult.setEffectiveEndDate(LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE));
        plantResult.setDeletedFlag(false);
        plantResult.setCreatedAt(now);
        plantResult.setUpdatedAt(now);
        plantResult.setCreatedUserId(creatorOperatorId);
        plantResult.setUpdatedUserId(creatorOperatorId);

        when(jwtService.getOperatorOrOpenSystemId(any(HttpServletRequest.class))).thenReturn(creatorOperatorId);
        when(plantService.addPlant(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(plantResult);

        // Act
        ResponseEntity<APIResponse<PlantResponse>> responseEntity = plantController.postPlant(
                request, requestBody, mock(BindingResult.class));

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        APIResponse<PlantResponse> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.CREATED.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());

        // Verify apiKeyService.getUsecaseStoreId() is NOT called when auth is disabled (optimization)
        verify(apiKeyService, times(0)).getUsecaseStoreId(any());

        // Verify the service was called without auth
        verify(plantService).addPlant(any(), any(), any(), any(), any(), any(), any(), any());
        verify(plantService, times(0)).addPlantWithAuth(any(), any(), any(), any(), any(), any(), any(), any(), any());

        assertEquals("generated-plant-id", apiResponse.getData().getPlantId());
        assertEquals(operatorId, apiResponse.getData().getOperatorId());
        assertEquals(plantName, apiResponse.getData().getPlantName());
    }

    /**
     * Test for listPlant with null values and both authorization enabled and disabled.
     *
     * @param isEnableAuthz true to test with authorization enabled, false for disabled
     * @throws Exception if any error occurs during test execution
     */
    @ParameterizedTest
    @CsvSource({"true", "false"})
    void testListPlant_nullValues(boolean isEnableAuthz) throws Exception {
        // Set isEnableUcAuthorization to true or false for this test
        when(odsProperties.isEnableUcAuthorization()).thenReturn(isEnableAuthz);

        ListPlantRequest listPlantRequest = new ListPlantRequest();
        listPlantRequest.setCount(commonCount);
        listPlantRequest.setIndex(commonIndex);
        listPlantRequest.setSort(null);
        listPlantRequest.setOperatorId(null);
        listPlantRequest.setPlantId(null);
        listPlantRequest.setPlantName(null);
        listPlantRequest.setPlantAddress(null);
        listPlantRequest.setOpenPlantId(null);
        listPlantRequest.setGlobalPlantId(null);
        listPlantRequest.setEffectiveDate(null);
        listPlantRequest.setDeletedFlag(null);
        // Convert request object to JSON string
        String rawJson = objectMapper.writeValueAsString(listPlantRequest);

        PlantResult plantResult = new PlantResult();
        plantResult.setOperatorId(commonOperatorId);
        plantResult.setPlantId(commonPlantId);
        plantResult.setPlantName(commonPlantName);
        plantResult.setPlantAddress(commonPlantAddress);
        plantResult.setOpenPlantId(commonOpenPlantId);
        plantResult.setGlobalPlantId(commonGlobalPlantId);
        plantResult.setDeletedFlag(commonDeletedFlag);
        plantResult.setEffectiveStartDate(commonEffectiveStartDate);
        plantResult.setEffectiveEndDate(commonEffectiveEndDate);
        plantResult.setCreatedAt(commonCreatedAt);
        plantResult.setUpdatedAt(commonUpdatedAt);
        List<PlantResult> listPlantResult = List.of(plantResult);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn(commonApiKey);
        when(apiKeyService.getUsecaseStoreId(commonApiKey)).thenReturn(commonStoreId);
        // Mock the validator to always return no violations
        Validator validator = mock(Validator.class);
        // Suppress warnings for unchecked casts
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<ListPlantRequest>> violations = mock(Set.class);

        // Inject the mock validator into the controller using reflection
        Field validatorField = PlantController.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(plantController, validator);
        // Arrange
        when(violations.isEmpty()).thenReturn(true);
        when(validator.validate(any(ListPlantRequest.class))).thenReturn(violations);
        if (isEnableAuthz) {
            when(plantService.searchPlantsWithAuth(anyInt(), anyInt(), any(), any(), any(), any(), any(),
                    any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(listPlantResult);
        } else {
            when(plantService.searchPlants(anyInt(), anyInt(), any(), any(), any(), any(), any(),
                    any(), any(), any(), any(), any()))
                    .thenReturn(listPlantResult);
        }

        // Act
        ResponseEntity<APIResponse<List<GetPlantResponse>>> responseEntity = plantController.listPlant(
                request, rawJson);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        APIResponse<List<GetPlantResponse>> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertNotNull(apiResponse.getData());
        assertFalse(apiResponse.getData().isEmpty(), "Data list should not be empty");

        // Verify apiKeyService.getUsecaseStoreId() is NOT called when auth is disabled (optimization)
        verify(apiKeyService, times(isEnableAuthz ? 1 : 0)).getUsecaseStoreId(any());

        // Verify the service was called without auth
        verify(plantService, times(isEnableAuthz ? 0 : 1)).searchPlants(anyInt(), anyInt(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any());
        verify(plantService, times(isEnableAuthz ? 1 : 0)).searchPlantsWithAuth(anyInt(), anyInt(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any());
    }
}
