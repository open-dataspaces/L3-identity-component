/*
 * OpenFgaServiceImplTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for OpenFgaServiceImpl.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.infrastructure.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.consts.ConstPath;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.ConflictException;
import io.github.open_dataspaces.core.exception.NotFoundException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnexpectedException;
import io.github.open_dataspaces.core.infrastructure.config.OpenFgaProperties;
import io.github.open_dataspaces.core.infrastructure.dto.EvaluateRequest;
import io.github.open_dataspaces.core.infrastructure.dto.EvaluateResponse;
import io.github.open_dataspaces.core.infrastructure.dto.EvaluationsResponse;
import io.github.open_dataspaces.core.infrastructure.dto.Tuple;
import io.github.open_dataspaces.core.infrastructure.dto.TupleDeleteRequest;
import io.github.open_dataspaces.core.infrastructure.dto.TupleDeleteRequest.Deletes;
import io.github.open_dataspaces.core.infrastructure.dto.TupleWriteRequest;
import io.github.open_dataspaces.core.infrastructure.dto.TupleWriteRequest.Writes;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Test class for OpenFgaServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
public class OpenFgaServiceImplTest {

    @InjectMocks
    private OpenFgaServiceImpl openFgaServiceImpl;

    @Mock
    private OpenFgaProperties openFgaProperties;
    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;

    private Validator validator;

    // Common input parameters
    private final String commonApiBasePath = "http://openfga.api.endpoint";
    private final String commonPath = "/v1/evaluate";
    private final HttpMethod commonHttpMethod = HttpMethod.POST;
    private final String commonStoreId = "store-123";
    private final String commonAdminStoreId = "admin-store-123";
    private final String commonSubjectType = "user";
    private final String commonUser = "user-123";
    private final String commonResourceType = "resource";
    private final String commonResource = "resource-123";
    private final String commonAction = "action-123";
    private final boolean commonDecision = true;
    private final String commonForwardResponseBody = "{\"key\": \"value\"}";
    private final LocalDateTime commonCurrentTime = LocalDateTime.now(ZoneOffset.UTC);
    private final List<Tuple> commonTuples = List.of(
        new Tuple("user:1", "member", "operator:1"),
        new Tuple("user:2", "member", "operator:2")
    );

    /**
     * Sets up the test environment before each test.
     */
    @BeforeEach
    void setUp() throws Exception {
        // Initialize ObjectMapper
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // Register JavaTimeModule
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // Use ISO-8601 format

        // Initialize Validator
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();

        // Initialize OpenFgaServiceImpl with mocks
        openFgaServiceImpl = new OpenFgaServiceImpl(
                openFgaProperties,
                this.objectMapper,
                restTemplate,
                validator
        );

    }

    /**
     * Tests successful forwarding of requests and responses in forward().
     */
    @ParameterizedTest
    @CsvSource({
        "EXIST, EXIST",     // Normal case: both request and response exist
        "NULL, EXIST",     // Edge case: request does not exist, response exists
        "EXIST, NULL"     // Edge case: request exists, response does not exist
    })
    @DisplayName("forward - Success")
    void testForward_success(
            String argRequestBody,
            String argResponseBody
    ) throws Exception {
        // Convert input parameter
        String requestBody =
                "EXIST".equals(argRequestBody) ? objectMapper.writeValueAsString(new EvaluateRequest(commonSubjectType, commonUser, commonResourceType, commonResource, commonAction, commonCurrentTime)) :
                "NULL".equals(argRequestBody) ? null :
                argRequestBody;
        String responseBody =
                "EXIST".equals(argResponseBody) ? objectMapper.writeValueAsString(new EvaluateResponse()) :
                "NULL".equals(argResponseBody) ? null :
                argResponseBody;
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(responseBody);

        // Arrange
        when(openFgaProperties.getApiEndpoint()).thenReturn(commonApiBasePath);
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponseEntity);

        // Act
        ResponseEntity<Object> response = openFgaServiceImpl.forward(commonPath, commonHttpMethod, requestBody);

        // Assert
        verify(restTemplate).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        );

        assertNotNull(response);
        assertEquals(response.getStatusCode(), mockResponseEntity.getStatusCode());
        if (responseBody != null) {
            assertNotNull(response.getBody());
            assertEquals(response.getBody(), objectMapper.readValue(responseBody, Object.class));
        } else {
            assertNull(response.getBody());
        }

    }

    /**
     * Tests that OutOfServiceException is thrown when RestClientException occurs during forward().
     */
    @Test
    @DisplayName("forward - RestClientException")
    void testForward_restClientException() throws Exception {
        String requestBody = objectMapper.writeValueAsString(new EvaluateRequest(commonSubjectType, commonUser, commonResourceType, commonResource, commonAction, commonCurrentTime));

        // Arrange
        when(openFgaProperties.getApiEndpoint()).thenReturn(commonApiBasePath);
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RestClientException("dummy exception"));

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            openFgaServiceImpl.forward(commonPath, commonHttpMethod, requestBody);
        });
        assertEquals(exception.getLogMessage(), String.format(ConstError.ERRLOG_500_ERROR, ConstError.ERR_500, "dummy exception"));

        verify(restTemplate).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    /**
     * Tests that UnexpectedException is thrown when response JSON parsing fails in forward().
     */
    @ParameterizedTest
    @CsvSource({
        "HttpStatus.OK",
        "HttpStatus.NG"
    })
    @DisplayName("forward - Response JSON Parsing Failure")
    void testForward_responseJsonParsingFailure(
            String argHttpStatus
    ) throws Exception {
        String requestBody = objectMapper.writeValueAsString(new EvaluateRequest(commonSubjectType, commonUser, commonResourceType, commonResource, commonAction, commonCurrentTime));
        String responseBody = "{";  // Malformed JSON to trigger parsing exception
        ResponseEntity<String> mockResponseEntity;
        switch (argHttpStatus) {
            case "HttpStatus.OK":
                mockResponseEntity = ResponseEntity.ok(responseBody);
                break;
            case "HttpStatus.NG":
                mockResponseEntity = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
                break;
            default:
                throw new IllegalArgumentException("Unknown HttpStatus: " + argHttpStatus);
        }

        // Arrange
        when(openFgaProperties.getApiEndpoint()).thenReturn(commonApiBasePath);
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponseEntity);

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            openFgaServiceImpl.forward(commonPath, commonHttpMethod, requestBody);
        });
        assertTrue(exception.getLogMessage().contains(ConstError.ERR_500));

        verify(restTemplate).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    /**
     * Tests successful writing of tuples in writeTuples().
     */
    @Test
    @DisplayName("writeTuples - Success")
    void testWriteTuples_success() throws Exception {
        String storeId = "store-456";

        when(openFgaProperties.getApiEndpoint()).thenReturn(commonApiBasePath);
        when(openFgaProperties.getPresharedKey()).thenReturn("test-key");
        when(restTemplate.exchange(
            anyString(),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(String.class)
        )).thenReturn(ResponseEntity.ok("{}"));

        openFgaServiceImpl.writeTuples(commonTuples, storeId);

        String expectedPath = ConstPath.AUTHORIZATION_TUPLES_WRITE.replace("{" + Const.API_PATH_PARAM_STORE_ID + "}", storeId);
        String expectedUrl = String.join("", commonApiBasePath, expectedPath);
        String expectedPayload = objectMapper.writeValueAsString(new TupleWriteRequest(new Writes(commonTuples)));

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpMethod> methodCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate, times(1)).exchange(
                urlCaptor.capture(),
                methodCaptor.capture(),
                entityCaptor.capture(),
                eq(String.class)
        );

        assertEquals(expectedUrl, urlCaptor.getValue());
        assertEquals(HttpMethod.POST, methodCaptor.getValue());
        assertEquals(expectedPayload, entityCaptor.getValue().getBody());
        assertEquals(MediaType.APPLICATION_JSON, entityCaptor.getValue().getHeaders().getContentType());
    }

    /**
     * Tests successful deleting of tuples in deleteTuples().
     */
    @Test
    @DisplayName("deleteTuples - Success")
    void testDeleteTuples_success() throws Exception {
        when(openFgaProperties.getApiEndpoint()).thenReturn(commonApiBasePath);
        when(restTemplate.exchange(
            anyString(),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(String.class)
        )).thenReturn(ResponseEntity.ok("{}"));

        openFgaServiceImpl.deleteTuples(commonTuples, commonAdminStoreId);

        String expectedPath = ConstPath.AUTHORIZATION_TUPLES_WRITE.replace("{" + Const.API_PATH_PARAM_STORE_ID + "}", commonAdminStoreId);
        String expectedUrl = String.join("", commonApiBasePath, expectedPath);
        String expectedPayload = objectMapper.writeValueAsString(new TupleDeleteRequest(new Deletes(commonTuples)));

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpMethod> methodCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate, times(1)).exchange(
                urlCaptor.capture(),
                methodCaptor.capture(),
                entityCaptor.capture(),
                eq(String.class)
        );

        assertEquals(expectedUrl, urlCaptor.getValue());
        assertEquals(HttpMethod.POST, methodCaptor.getValue());
        assertEquals(expectedPayload, entityCaptor.getValue().getBody());
        assertEquals(MediaType.APPLICATION_JSON, entityCaptor.getValue().getHeaders().getContentType());
    }

    /**
     * Tests JSON parsing failure in deleteTuples().
     */
    @Test
    @DisplayName("deleteTuples - JSON Parsing Failure")
    void testDeleteTuples_jsonParsingFailure() throws Exception {
        // Arrange
        ObjectMapper mockObjectMapper = Mockito.mock(ObjectMapper.class);
        when(mockObjectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("JSON parsing error") {});
        openFgaServiceImpl = new OpenFgaServiceImpl(
            openFgaProperties,
            mockObjectMapper,
            restTemplate,
            validator
        );

        // Act & Assert
        assertThrows(UnexpectedException.class, () -> {
            openFgaServiceImpl.deleteTuples(commonTuples, commonAdminStoreId);
        });
    }

    /**
     * Tests that appropriate exceptions are thrown based on HTTP status codes in forward().
     */
    @ParameterizedTest
    @CsvSource({
        // Exception
        "BadParametersException, EXIST",
        "ConflictException, EXIST",
        "NotFoundException, EXIST",
        "OutOfServiceException, EXIST",
        "UnexpectedException, EXIST",
        "UnexpectedException, NULL"
    })
    void testForward_restTemplateThrowsException(
            String exceptionClassName,
            String argResponseBody
    ) throws Exception {
        String requestBody = objectMapper.writeValueAsString(new EvaluateRequest(commonSubjectType, commonUser, commonResourceType, commonResource, commonAction, commonCurrentTime));
        String responseBody =
                "EXIST".equals(argResponseBody) ? objectMapper.writeValueAsString(new EvaluateResponse()) :
                "NULL".equals(argResponseBody) ? null :
                argResponseBody;

        Class<? extends Exception> clazz = null;
        ResponseEntity<String> mockResponseEntity = null;
        switch (exceptionClassName) {
            case "BadParametersException":
                clazz = BadParametersException.class;
                mockResponseEntity = ResponseEntity.badRequest().body(responseBody);
                break;
            case "ConflictException":
                clazz = ConflictException.class;
                mockResponseEntity = ResponseEntity.status(HttpStatus.CONFLICT).body(responseBody);
                break;
            case "NotFoundException":
                clazz = NotFoundException.class;
                mockResponseEntity = ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
                break;
            case "OutOfServiceException":
                clazz = OutOfServiceException.class;
                mockResponseEntity = ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(responseBody);
                break;
            case "UnexpectedException":
                clazz = UnexpectedException.class;
                mockResponseEntity = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
                break;
            default:
                throw new IllegalArgumentException("Unknown exception class: " + exceptionClassName);
        }

        // Arrange
        when(openFgaProperties.getApiEndpoint()).thenReturn(commonApiBasePath);
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponseEntity);

        // Act & Assert
        assertThrows(clazz, () -> {
            openFgaServiceImpl.forward(commonPath, commonHttpMethod, requestBody);
        });

        verify(restTemplate).exchange(
                anyString(),
                eq(commonHttpMethod),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    /**
     * Tests successful evaluation in evaluate().
     */
    @Test
    @DisplayName("evaluate - Success")
    void testEvaluate_success() throws Exception {
        // Prepare mock response
        EvaluateResponse evaluateResponse = new EvaluateResponse();
        evaluateResponse.setDecision(commonDecision);
        String responseBody = objectMapper.writeValueAsString(evaluateResponse);
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(responseBody);

        // Arrange
        when(openFgaProperties.getApiEndpoint()).thenReturn(commonApiBasePath);
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponseEntity);

        // Act
        boolean decision = openFgaServiceImpl.evaluate(
                commonStoreId,
                commonUser,
                commonResource,
                commonAction
        );
        // Assert
        verify(restTemplate).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        );
        assertEquals(commonDecision, decision);
    }

    /**
     * Tests that UnexpectedException is thrown when response JSON parsing fails in evaluate().
     */
    @Test
    @DisplayName("evaluate - Response JSON Parsing Failure")
    void testEvaluate_responseJsonParsingFailure() throws Exception {
        // Prepare mock response
        String responseBody = "{}";  // Missing 'decision' field
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(responseBody);

        // Arrange
        when(openFgaProperties.getApiEndpoint()).thenReturn(commonApiBasePath);
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponseEntity);

        // Act
        assertThrows(UnexpectedException.class, () -> {
            openFgaServiceImpl.evaluate(
                    commonStoreId,
                    commonUser,
                    commonResource,
                    commonAction
            );
        });
        // Assert
        verify(restTemplate).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    /**
     * Tests findStoreById - Success.
     */
    @Test
    @DisplayName("findStoreById - Success")
    void testFindStoreById_success() {
        // Prepare mock response
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(commonForwardResponseBody);

        // Arrange
        when(openFgaProperties.getApiEndpoint()).thenReturn(commonApiBasePath);
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponseEntity);

        // Act
        ResponseEntity<Object> response = openFgaServiceImpl.findStoreById(commonStoreId);

        // Assert
        verify(restTemplate).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        );
        assertNotNull(response);
        assertEquals(response.getStatusCode(), mockResponseEntity.getStatusCode());
        assertNotNull(response.getBody());
    }

    /**
     * evaluations - Success (single batch <= max size).
     */
    @Test
    @DisplayName("evaluations - Success (single batch)")
    void testEvaluations_success_singleBatch() throws Exception {
        // Prepare mock response for single batch with 2 decisions
        EvaluationsResponse resp = new EvaluationsResponse();
        EvaluationsResponse.EvaluationDecision d1 = new EvaluationsResponse.EvaluationDecision();
        d1.setDecision(true);
        EvaluationsResponse.EvaluationDecision d2 = new EvaluationsResponse.EvaluationDecision();
        d2.setDecision(false);
        resp.getEvaluations().add(d1);
        resp.getEvaluations().add(d2);
        String responseBody = objectMapper.writeValueAsString(resp);
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(responseBody);

        // Arrange
        when(openFgaProperties.getApiEndpoint()).thenReturn(commonApiBasePath);
        when(openFgaProperties.getPresharedKey()).thenReturn("token");
        when(restTemplate.exchange(
            anyString(),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(String.class)
        )).thenReturn(mockResponseEntity);

        // Build resources (2 items)
        List<Map<String, String>> resources = new ArrayList<>();
        Map<String, String> r1 = new HashMap<>();
        r1.put(Const.OPENFGA_EVALUATION_DEFAULT_KEY, "api");
        r1.put(Const.OPENFGA_EVALUATION_DEFAULT_ID, "/bulk/1");
        resources.add(r1);
        Map<String, String> r2 = new HashMap<>();
        r2.put(Const.OPENFGA_EVALUATION_DEFAULT_KEY, "api");
        r2.put(Const.OPENFGA_EVALUATION_DEFAULT_ID, "/bulk/2");
        resources.add(r2);

        // Act
        List<Boolean> decisions = openFgaServiceImpl.evaluations(
                commonStoreId,
                commonUser,
                Const.AUTHZ_ACTION_GET,
                resources
        );

        // Assert
        verify(restTemplate, times(1)).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        );
        assertNotNull(decisions);
        assertEquals(2, decisions.size());
        assertEquals(true, decisions.get(0));
        assertEquals(false, decisions.get(1));
    }

    /**
    * evaluations - Success (multi batch > max size).
    */
    @Test
    @DisplayName("evaluations - Success (multi batch)")
    void testEvaluations_success_multiBatch() throws Exception {
        // Prepare mock responses for two batches:
        // - first returns Const.EVALUATIONS_BATCH_SIZE_MAX decisions all false
        // - second returns 10 decisions alternating true/false (to verify order across boundary)
        int max = Const.EVALUATIONS_BATCH_SIZE_MAX;
        int remainder = 10;

        EvaluationsResponse resp1 = new EvaluationsResponse();
        for (int i = 0; i < max; i++) {
            EvaluationsResponse.EvaluationDecision di = new EvaluationsResponse.EvaluationDecision();
            di.setDecision(false);
            resp1.getEvaluations().add(di);
        }
        String body1 = objectMapper.writeValueAsString(resp1);

        EvaluationsResponse resp2 = new EvaluationsResponse();
        for (int i = 0; i < remainder; i++) {
            EvaluationsResponse.EvaluationDecision di = new EvaluationsResponse.EvaluationDecision();
            di.setDecision(i % 2 == 0); // true, false, true, ...
            resp2.getEvaluations().add(di);
        }
        String body2 = objectMapper.writeValueAsString(resp2);

        ResponseEntity<String> mockResponseEntity1 = ResponseEntity.ok(body1);
        ResponseEntity<String> mockResponseEntity2 = ResponseEntity.ok(body2);

        // Arrange
        when(openFgaProperties.getApiEndpoint()).thenReturn(commonApiBasePath);
        when(openFgaProperties.getPresharedKey()).thenReturn("token");
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponseEntity1)
         .thenReturn(mockResponseEntity2);

        // Build resources (max + remainder items) -> should trigger two batches
        List<Map<String, String>> resources = new ArrayList<>();
        for (int i = 1; i <= max + remainder; i++) {
            Map<String, String> r = new HashMap<>();
            r.put(Const.OPENFGA_EVALUATION_DEFAULT_KEY, "api");
            r.put(Const.OPENFGA_EVALUATION_DEFAULT_ID, "/bulk/" + i);
            resources.add(r);
        }

        // Act
        List<Boolean> decisions = openFgaServiceImpl.evaluations(
                commonStoreId,
                commonUser,
                Const.AUTHZ_ACTION_GET,
                resources
        );

        // Assert
        verify(restTemplate, times(2)).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        );
        assertNotNull(decisions);
        assertEquals(max + remainder, decisions.size());
        // Verify first batch (0..max-1) are all false
        for (int i = 0; i < max; i++) {
            assertEquals(false, decisions.get(i));
        }
        // Verify second batch (max..max+remainder-1) alternates true/false starting with true
        for (int i = 0; i < remainder; i++) {
            assertEquals(i % 2 == 0, decisions.get(max + i));
        }
    }

    /**
     * evaluations - Success (exact max size = 50).
     */
    @Test
    @DisplayName("evaluations - Success (exact max size)")
    void testEvaluations_success_exactMax() throws Exception {
        int max = Const.EVALUATIONS_BATCH_SIZE_MAX;

        // Prepare mock response: exactly max items, all false
        EvaluationsResponse resp = new EvaluationsResponse();
        for (int i = 0; i < max; i++) {
            EvaluationsResponse.EvaluationDecision di = new EvaluationsResponse.EvaluationDecision();
            di.setDecision(false);
            resp.getEvaluations().add(di);
        }
        String responseBody = objectMapper.writeValueAsString(resp);
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(responseBody);

        // Arrange
        when(openFgaProperties.getApiEndpoint()).thenReturn(commonApiBasePath);
        when(openFgaProperties.getPresharedKey()).thenReturn("token");
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponseEntity);

        // Build resources: exactly max items
        List<Map<String, String>> resources = new ArrayList<>();
        for (int i = 1; i <= max; i++) {
            Map<String, String> r = new HashMap<>();
            r.put(Const.OPENFGA_EVALUATION_DEFAULT_KEY, "api");
            r.put(Const.OPENFGA_EVALUATION_DEFAULT_ID, "/bulk/" + i);
            resources.add(r);
        }

        // Act
        List<Boolean> decisions = openFgaServiceImpl.evaluations(
                commonStoreId,
                commonUser,
                Const.AUTHZ_ACTION_GET,
                resources
        );

        // Assert
        verify(restTemplate, times(1)).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        );
        assertNotNull(decisions);
        assertEquals(max, decisions.size());
        for (int i = 0; i < max; i++) {
            assertEquals(false, decisions.get(i));
        }
    }

    /**
     * evaluations - Success (empty list returns empty decisions and no call).
     */
    @Test
    @DisplayName("evaluations - Success (empty list)")
    void testEvaluations_success_emptyList() throws Exception {
        // Build resources: empty
        List<Map<String, String>> resources = new ArrayList<>();

        // Act
        List<Boolean> decisions = openFgaServiceImpl.evaluations(
                commonStoreId,
                commonUser,
                Const.AUTHZ_ACTION_GET,
                resources
        );

        // Assert
        verify(restTemplate, times(0)).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        );
        assertNotNull(decisions);
        assertEquals(0, decisions.size());
    }

    /**
    * evaluations - Validation error (decision=null).
    */
    @Test
    @DisplayName("evaluations - Validation error")
    void testEvaluations_validationError() throws Exception {
        // Prepare invalid response: decision=null should violate @NotNull
        String invalidBody = "{\"evaluations\":[{\"decision\":null}]}";
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(invalidBody);

        // Arrange
        when(openFgaProperties.getApiEndpoint()).thenReturn(commonApiBasePath);
        when(openFgaProperties.getPresharedKey()).thenReturn("token");
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponseEntity);

        // Build one resource
        List<Map<String, String>> resources = new ArrayList<>();
        Map<String, String> r1 = new HashMap<>();
        r1.put(Const.OPENFGA_EVALUATION_DEFAULT_KEY, "api");
        r1.put(Const.OPENFGA_EVALUATION_DEFAULT_ID, "/bulk/1");
        resources.add(r1);

        // Act & Assert
        assertThrows(UnexpectedException.class, () -> {
            openFgaServiceImpl.evaluations(commonStoreId, commonUser, Const.AUTHZ_ACTION_GET, resources);
        });
    }

    /**
    * evaluations - HTTP error propagated (400 Bad Request).
    */
    @Test
    @DisplayName("evaluations - HTTP error (400)")
    void testEvaluations_httpError() throws Exception {
        // Prepare HTTP 400 response
        ResponseEntity<String> mockResponseEntity = ResponseEntity.badRequest().body("{}");

        // Arrange
        when(openFgaProperties.getApiEndpoint()).thenReturn(commonApiBasePath);
        when(openFgaProperties.getPresharedKey()).thenReturn("token");
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponseEntity);

        // Build one resource
        List<Map<String, String>> resources = new ArrayList<>();
        Map<String, String> r1 = new HashMap<>();
        r1.put(Const.OPENFGA_EVALUATION_DEFAULT_KEY, "api");
        r1.put(Const.OPENFGA_EVALUATION_DEFAULT_ID, "/bulk/1");
        resources.add(r1);

        // Act & Assert
        assertThrows(BadParametersException.class, () -> {
            openFgaServiceImpl.evaluations(commonStoreId, commonUser, Const.AUTHZ_ACTION_GET, resources);
        });
    }

    /**
     * Tests deleteStore - Success.
     */
    @Test
    @DisplayName("deleteStore - Success")
    void testDeleteStore_success() {
        // Prepare mock response
        ResponseEntity<String> mockResponseEntity = ResponseEntity.noContent().build();

        // Arrange
        when(openFgaProperties.getApiEndpoint()).thenReturn(commonApiBasePath);
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponseEntity);

        // Act
        ResponseEntity<Object> response = openFgaServiceImpl.deleteStore(commonStoreId);

        // Assert
        verify(restTemplate).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        );
        assertNotNull(response);
        assertEquals(response.getStatusCode(), mockResponseEntity.getStatusCode());
        assertNull(response.getBody());
    }
}
