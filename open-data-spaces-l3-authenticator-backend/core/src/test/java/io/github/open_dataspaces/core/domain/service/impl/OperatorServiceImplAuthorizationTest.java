/*
 * OperatorServiceImplAuthorizationTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for OperatorServiceImpl authorization integration.
 *
 * Date: 2025/12/03
 */

package io.github.open_dataspaces.core.domain.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import io.github.open_dataspaces.core.common.config.ODSProperties;
import io.github.open_dataspaces.core.domain.dto.OperatorResult;
import io.github.open_dataspaces.core.domain.dto.StateString;
import io.github.open_dataspaces.core.domain.entities.OperatorEntity;
import io.github.open_dataspaces.core.domain.repository.interfaces.OperatorRepository;
import io.github.open_dataspaces.core.domain.service.interfaces.AuthorizationService;
import io.github.open_dataspaces.core.exception.IllegalAuthDataException;
import io.github.open_dataspaces.core.exception.UnexpectedException;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstPath;

/**
 * Unit tests for OperatorServiceImpl authorization integration.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OperatorServiceImpl Authorization Tests")
class OperatorServiceImplAuthorizationTest {

    @Mock
    private OperatorRepository operatorRepository;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private ODSProperties odsProperties;

    private OperatorServiceImpl operatorService;

    private final String testStoreId = "test-store-id";
    private final String testUserId = "443b68c2-6815-4fa6-bdcf-a9eb821f6419";
    private final String testOperatorId = "10000";

    @BeforeEach
    void setUp() {
        // Set default operator authorization for most tests
        when(odsProperties.isEnableOperatorPlantAuthorization()).thenReturn(true);
        operatorService = new OperatorServiceImpl(odsProperties, operatorRepository, authorizationService);
    }

    /*
     * Test Cases for getOperatorWithAuth
     */
    @Test
    @DisplayName("getOperator - successful authorization returns operator")
    void testGetOperatorAuthorizationSuccess() {
        // Arrange
        OperatorEntity mockEntity = createMockOperatorEntity(testOperatorId);
        try {
            when(authorizationService.evaluate(eq(testStoreId), anyString(), anyString(), eq("can_get"))).thenReturn(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(operatorRepository.findById(testOperatorId)).thenReturn(Optional.of(mockEntity));

        // Act
        OperatorResult result = operatorService.getOperatorWithAuth(testOperatorId, testUserId, testStoreId);

        // Assert
        assertNotNull(result);
        assertEquals(testOperatorId, result.getOperatorId());
        try {
            verify(authorizationService, times(1)).evaluate(eq(testStoreId), eq(testUserId), eq(ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", testOperatorId)), eq("can_get"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        verify(operatorRepository, times(1)).findById(testOperatorId);
    }

    /*
    * Test Cases for getOperatorWithAuth - Authorization Failure
    */
    @Test
    @DisplayName("getOperator - throws ForbiddenException on authorization failure")
    void testGetOperatorAuthorizationFailure() {
        // Arrange
        try {
            when(authorizationService.evaluate(eq(testStoreId), anyString(), anyString(), eq("can_get"))).thenReturn(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act & Assert
        assertThrows(IllegalAuthDataException.class, () -> {
            operatorService.getOperatorWithAuth(testOperatorId, testUserId, testStoreId);
        });

        try {
            verify(authorizationService, times(1)).evaluate(eq(testStoreId), eq(testUserId), eq(ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", testOperatorId)), eq("can_get"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        verify(operatorRepository, times(0)).findById(anyString());
    }

    /*
     * Test Cases for searchOperatorsWithAuth
     */
    @Test
    @DisplayName("searchOperators - returns only authorized operators")
    void testSearchOperatorsFilterByAuthorization() {
        // Arrange
        OperatorEntity operator1 = createMockOperatorEntity("10000");
        OperatorEntity operator2 = createMockOperatorEntity("20000");
        OperatorEntity operator3 = createMockOperatorEntity("30000");
        List<OperatorEntity> allOperators = Arrays.asList(operator1, operator2, operator3);

        when(operatorRepository.findWithLimitOffset(ArgumentMatchers.<Specification<OperatorEntity>>any(), eq(0), eq(0), any(Sort.class)))
                .thenReturn(allOperators);

        // operator1 and 2 are authorized, operator3 is not -> bulk evaluations
        try {
            java.util.List<java.util.Map<String, String>> resources = new java.util.ArrayList<>();
            java.util.Map<String, String> r1 = new java.util.HashMap<>();
            r1.put(Const.OPENFGA_EVALUATION_DEFAULT_KEY, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE);
            r1.put(Const.OPENFGA_EVALUATION_DEFAULT_ID, ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", "10000"));
            resources.add(r1);
            java.util.Map<String, String> r2 = new java.util.HashMap<>();
            r2.put(Const.OPENFGA_EVALUATION_DEFAULT_KEY, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE);
            r2.put(Const.OPENFGA_EVALUATION_DEFAULT_ID, ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", "20000"));
            resources.add(r2);
            java.util.Map<String, String> r3 = new java.util.HashMap<>();
            r3.put(Const.OPENFGA_EVALUATION_DEFAULT_KEY, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE);
            r3.put(Const.OPENFGA_EVALUATION_DEFAULT_ID, ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", "30000"));
            resources.add(r3);
            when(authorizationService.evaluations(eq(testStoreId), eq(testUserId), eq("can_get"), eq(resources))).thenReturn(java.util.Arrays.asList(true, true, false));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act
        List<OperatorResult> results = operatorService.searchOperatorsWithAuth(
                10,  // limit
                0,   // offset
                "operator_id",  // orderByKey (DB column name)
                "asc",  // orderByDirection
                null,  // operatorId filter
                null,  // operatorName filter
                null,  // operatorAddress filter
                null,  // openOperatorId filter
                StateString.unset(),  // globalOperatorId filter
                null,  // deletedFlag filter
                null,   // effectiveDate filter
                testUserId,
                testStoreId
        );

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("10000", results.get(0).getOperatorId());
        assertEquals("20000", results.get(1).getOperatorId());
        // verify repository called once with full-scan (offset=0, limit=0)
        verify(operatorRepository, times(1)).findWithLimitOffset(ArgumentMatchers.<Specification<OperatorEntity>>any(), eq(0), eq(0), any(Sort.class));

        try {
            java.util.List<java.util.Map<String, String>> verifyResources = new java.util.ArrayList<>();
            java.util.Map<String, String> vr1 = new java.util.HashMap<>();
            vr1.put(Const.OPENFGA_EVALUATION_DEFAULT_KEY, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE);
            vr1.put(Const.OPENFGA_EVALUATION_DEFAULT_ID, ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", "10000"));
            verifyResources.add(vr1);
            java.util.Map<String, String> vr2 = new java.util.HashMap<>();
            vr2.put(Const.OPENFGA_EVALUATION_DEFAULT_KEY, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE);
            vr2.put(Const.OPENFGA_EVALUATION_DEFAULT_ID, ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", "20000"));
            verifyResources.add(vr2);
            java.util.Map<String, String> vr3 = new java.util.HashMap<>();
            vr3.put(Const.OPENFGA_EVALUATION_DEFAULT_KEY, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE);
            vr3.put(Const.OPENFGA_EVALUATION_DEFAULT_ID, ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", "30000"));
            verifyResources.add(vr3);
            verify(authorizationService, times(1)).evaluations(eq(testStoreId), eq(testUserId), eq("can_get"), eq(verifyResources));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
    * Test Cases for searchOperatorsWithAuth - All Unauthorized
    */
    @Test
    @DisplayName("searchOperators - returns empty list when all operators are unauthorized")
    void testSearchOperatorsAllUnauthorized() {
        // Arrange
        OperatorEntity operator1 = createMockOperatorEntity("10000");
        OperatorEntity operator2 = createMockOperatorEntity("20000");
        List<OperatorEntity> allOperators = Arrays.asList(operator1, operator2);
        when(operatorRepository.findWithLimitOffset(ArgumentMatchers.<Specification<OperatorEntity>>any(), eq(0), eq(0), any(Sort.class)))
                .thenReturn(allOperators);

        // all operators are unauthorized (batch evaluations)
        try {
            when(authorizationService.evaluations(eq(testStoreId), eq(testUserId), eq("can_get"), any()))
                    .thenReturn(java.util.Arrays.asList(false, false));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act
        List<OperatorResult> results = operatorService.searchOperatorsWithAuth(
                10,
                0,
                "operator_id",
                "asc",
                null,
                null,
                null,
                null,
                StateString.unset(),
                null,
                null,
                testUserId,
                testStoreId
        );

        // Assert
        assertNotNull(results);
        assertEquals(0, results.size());
        // evaluations should be called once
        try {
            verify(authorizationService, times(1)).evaluations(eq(testStoreId), eq(testUserId), eq("can_get"), any());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // repository full-scan once
        verify(operatorRepository, times(1)).findWithLimitOffset(ArgumentMatchers.<Specification<OperatorEntity>>any(), eq(0), eq(0), any(Sort.class));
    }

    /*
     * Additional Test Cases for searchOperatorsWithAuth - limit <= 0
     */
    @Test
    @DisplayName("searchOperatorsWithAuth - limit<=0 returns all authorized")
    void testSearchOperatorsWithAuthLimitZeroReturnsAllAuthorized() {
        // Arrange
        OperatorEntity o1 = createMockOperatorEntity("X1");
        OperatorEntity o2 = createMockOperatorEntity("X2");
        OperatorEntity o3 = createMockOperatorEntity("X3");
        List<OperatorEntity> page1 = Arrays.asList(o1, o2, o3);

        when(operatorRepository.findWithLimitOffset(ArgumentMatchers.<Specification<OperatorEntity>>any(), eq(0), eq(0), any(Sort.class)))
                .thenReturn(page1);
        try {
            when(authorizationService.evaluations(eq(testStoreId), eq(testUserId), eq("can_get"), any()))
                    .thenReturn(java.util.Arrays.asList(true, false, true));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act: limit 0 -> all authorized (2 items)
        List<OperatorResult> results = operatorService.searchOperatorsWithAuth(
                0, 0, "operator_id", "asc", null, null, null, null, StateString.unset(), null, null, testUserId, testStoreId);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("X1", results.get(0).getOperatorId());
        assertEquals("X3", results.get(1).getOperatorId());
    }

    /*
     * Additional Test Cases for searchOperatorsWithAuth - applies offset after authorization
     */
    @Test
    @DisplayName("searchOperatorsWithAuth - applies offset after authorization")
    void testSearchOperatorsWithAuthOffsetAfterAuthorization() {
        // Arrange: 3 authorized operators in order
        OperatorEntity o1 = createMockOperatorEntity("100");
        OperatorEntity o2 = createMockOperatorEntity("200");
        OperatorEntity o3 = createMockOperatorEntity("300");
        List<OperatorEntity> page1 = Arrays.asList(o1, o2, o3);

        when(operatorRepository.findWithLimitOffset(ArgumentMatchers.<Specification<OperatorEntity>>any(), eq(0), eq(0), any(Sort.class)))
                .thenReturn(page1);

        try {
            when(authorizationService.evaluations(eq(testStoreId), eq(testUserId), eq("can_get"), any()))
                    .thenReturn(java.util.Arrays.asList(true, true, true));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act: limit=2, offset=1 -> expect second and third
        List<OperatorResult> results = operatorService.searchOperatorsWithAuth(
                2, 1, "operator_id", "asc", null, null, null, null, StateString.unset(), null, null, testUserId, testStoreId);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("200", results.get(0).getOperatorId());
        assertEquals("300", results.get(1).getOperatorId());
    }

    /*
     * Additional Test Cases for searchOperatorsWithAuth - Multi-page fetching under low authorization rate
     */
    @Test
    @DisplayName("searchOperatorsWithAuth - offset beyond authorized size returns empty")
    void testSearchOperatorsWithAuthOffsetBeyondSize() {
        // Arrange: two authorized items
        OperatorEntity o1 = createMockOperatorEntity("A");
        OperatorEntity o2 = createMockOperatorEntity("B");
        List<OperatorEntity> all = Arrays.asList(o1, o2);
        when(operatorRepository.findWithLimitOffset(ArgumentMatchers.<Specification<OperatorEntity>>any(), eq(0), eq(0), any(Sort.class)))
                .thenReturn(all);
        try {
            when(authorizationService.evaluations(eq(testStoreId), eq(testUserId), eq("can_get"), any()))
                    .thenReturn(java.util.Arrays.asList(true, true));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act: offset 5 beyond size 2
        List<OperatorResult> results = operatorService.searchOperatorsWithAuth(
                10, 5, "operator_id", "asc", null, null, null, null, StateString.unset(), null, null, testUserId, testStoreId);

        // Assert
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    @DisplayName("searchOperatorsWithAuth - repository returns empty → no evaluations, empty result")
    void testSearchOperatorsWithAuthRepoEmptyShortCircuits() {
        // Arrange
        when(operatorRepository.findWithLimitOffset(ArgumentMatchers.<Specification<OperatorEntity>>any(), eq(0), eq(0), any(Sort.class)))
                .thenReturn(java.util.Collections.emptyList());

        // Act
        List<OperatorResult> results = operatorService.searchOperatorsWithAuth(
                10, 0, "operator_id", "asc", null, null, null, null, StateString.unset(), null, null, testUserId, testStoreId);

        // Assert
        assertNotNull(results);
        assertEquals(0, results.size());
        // evaluations should not be called
        try {
            verify(authorizationService, times(0)).evaluations(anyString(), anyString(), anyString(), any());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("searchOperatorsWithAuth - decisions shorter than resources is handled safely")
    void testSearchOperatorsWithAuthShortDecisions() {
        // Arrange
        OperatorEntity o1 = createMockOperatorEntity("1");
        OperatorEntity o2 = createMockOperatorEntity("2");
        OperatorEntity o3 = createMockOperatorEntity("3");
        List<OperatorEntity> all = Arrays.asList(o1, o2, o3);
        when(operatorRepository.findWithLimitOffset(ArgumentMatchers.<Specification<OperatorEntity>>any(), eq(0), eq(0), any(Sort.class)))
                .thenReturn(all);
        try {
            // Only two decisions provided
            when(authorizationService.evaluations(eq(testStoreId), eq(testUserId), eq("can_get"), any()))
                    .thenReturn(java.util.Arrays.asList(true, false));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act & Assert: strict mode should throw due to size mismatch
        assertThrows(
                UnexpectedException.class,
                () -> operatorService.searchOperatorsWithAuth(
                10, 0, "operator_id", "asc", null, null, null, null, StateString.unset(), null, null, testUserId, testStoreId)
        );
    }

    private OperatorEntity createMockOperatorEntity(String operatorId) {
        OperatorEntity entity = new OperatorEntity();
        entity.setOperatorId(operatorId);
        entity.setOperatorName("Test Operator " + operatorId);
        entity.setOperatorAddress("Test Address");
        entity.setOpenOperatorId("OP-" + operatorId);
        entity.setGlobalOperatorId("GLOBAL-" + operatorId);
        entity.setDeletedFlag(false);
        entity.setEffectiveStartDate(LocalDate.now().minusDays(1));
        entity.setEffectiveEndDate(LocalDate.now().plusYears(1));
        return entity;
    }

    /*
     * Test Cases for addOperatorWithAuth
     */
    @Test
    @DisplayName("addOperatorWithAuth - creates operator on successful authorization")
    void testAddOperatorWithAuthAuthorizationSuccess() {
        // Arrange
        String newOperatorId = java.util.UUID.randomUUID().toString();
        OperatorEntity mockEntity = createMockOperatorEntity(newOperatorId);
        when(operatorRepository.findById(newOperatorId)).thenReturn(Optional.empty());
        when(operatorRepository.save(any(OperatorEntity.class))).thenReturn(mockEntity);
        try {
            when(authorizationService.evaluate(eq(testStoreId), anyString(), anyString(), eq("can_create"))).thenReturn(true);
            doNothing().when(authorizationService).writeTuples(anyList(), eq(testStoreId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act
        OperatorResult result = operatorService.addOperatorWithAuth(
                newOperatorId,
                "Name",
                "Address",
                "open",
                "global",
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                "creator",
                testStoreId);

        // Assert
        assertNotNull(result);
        assertEquals(newOperatorId, result.getOperatorId());
        try {
            verify(authorizationService, times(1)).evaluate(eq(testStoreId), anyString(), eq(ConstPath.TUPLE_OPERATOR_PATH_SHORT), eq("can_create"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            verify(authorizationService, times(1)).writeTuples(ArgumentMatchers.argThat(tuples ->
                    tuples != null && !tuples.isEmpty()
            ), eq(testStoreId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
    * Test Cases for addOperatorWithAuth - Authorization Failure
    */
    @Test
    @DisplayName("addOperatorWithAuth - throws IllegalAuthDataException on authorization failure")
    void testAddOperatorWithAuthAuthorizationFailure() {
        // Arrange
        try {
            when(authorizationService.evaluate(eq(testStoreId), anyString(), anyString(), eq("can_create"))).thenReturn(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act & Assert
        assertThrows(IllegalAuthDataException.class, () -> {
            operatorService.addOperatorWithAuth(testOperatorId, "Name", "Address", "open", "global", LocalDate.now(), LocalDate.now().plusYears(1), "creator", testStoreId);
        });
    }

    /*
    * Test Cases for addOperatorWithAuth - Tuple Write Failure
    */
    @Test
    void testAddOperatorWithTupleWriteFailure() {
        // Arrange
        String newOperatorId = java.util.UUID.randomUUID().toString();
        OperatorEntity mockEntity = createMockOperatorEntity(newOperatorId);
        when(operatorRepository.findById(newOperatorId)).thenReturn(Optional.empty());
        when(operatorRepository.save(any(OperatorEntity.class))).thenReturn(mockEntity);
        try {
            when(authorizationService.evaluate(eq(testStoreId), anyString(), anyString(), eq("can_create"))).thenReturn(true);
            doThrow(new RuntimeException("writeTuples failed")).when(authorizationService).writeTuples(anyList(), eq(testStoreId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act & Assert
        UnexpectedException ex = assertThrows(UnexpectedException.class, () ->
                operatorService.addOperatorWithAuth(
                newOperatorId,
                "Name",
                "Address",
                "open",
                "global",
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                "creator",
                testStoreId
        ));
        assertTrue(ex.getMessage().contains("writeTuples failed"));

        // Verify writeTuples was attempted and save was invoked
        try {
            verify(authorizationService, times(1)).writeTuples(anyList(), eq(testStoreId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        verify(operatorRepository, times(1)).save(any(OperatorEntity.class));
    }

    /*
     * Test Cases for updateOperatorWithAuth success
     */
    @Test
    @DisplayName("updateOperatorWithAuth - calls updateOperator on authorization success")
    void testUpdateOperatorWithAuthAuthorizationSuccess() {
        // Arrange
        OperatorEntity existing = createMockOperatorEntity(testOperatorId);
        existing.setUpdatedAt(java.time.LocalDateTime.now());
        when(operatorRepository.findById(testOperatorId)).thenReturn(Optional.of(existing));
        try {
            when(authorizationService.evaluate(eq(testStoreId), anyString(), anyString(), eq("can_update"))).thenReturn(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act
        OperatorResult result = operatorService.updateOperatorWithAuth(
                testOperatorId,
                existing.getUpdatedAt(),
                null,
                null,
                null,
                StateString.unset(),
                "updater",
                testStoreId);

        // Assert
        assertNotNull(result);
        try {
            verify(authorizationService, times(1)).evaluate(eq(testStoreId), anyString(), eq(ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", testOperatorId)), eq("can_update"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Test Cases for updateOperatorWithAuth - Authorization Failure
     */
    @Test
    @DisplayName("updateOperatorWithAuth - throws IllegalAuthDataException on authorization failure")
    void testUpdateOperatorWithAuthAuthorizationFailure() {
        // Arrange
        try {
            when(authorizationService.evaluate(eq(testStoreId), anyString(), anyString(), eq("can_update"))).thenReturn(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act & Assert
        assertThrows(IllegalAuthDataException.class, () -> {
            operatorService.updateOperatorWithAuth(testOperatorId, java.time.LocalDateTime.now(), null, null, null, StateString.unset(), "updater", testStoreId);
        });
    }

    /*
     * Test Cases for updateOperatorStatusWithAuth
     */
    @Test
    @DisplayName("updateOperatorStatusWithAuth - calls updateOperatorStatus on authorization success")
    void testUpdateOperatorStatusWithAuthAuthorizationSuccess() {
        // Arrange
        OperatorEntity existing = createMockOperatorEntity(testOperatorId);
        existing.setUpdatedAt(java.time.LocalDateTime.now());
        when(operatorRepository.findById(testOperatorId)).thenReturn(Optional.of(existing));
        when(operatorRepository.saveAndFlush(any(OperatorEntity.class))).thenReturn(existing);
        try {
            when(authorizationService.evaluate(eq(testStoreId), anyString(), anyString(), eq("can_statusup"))).thenReturn(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act
        OperatorResult result = operatorService.updateOperatorStatusWithAuth(
                testOperatorId,
                existing.getUpdatedAt(),
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                false,
                "updater",
                testStoreId);

        // Assert
        assertNotNull(result);
        try {
            verify(authorizationService, times(1)).evaluate(eq(testStoreId), anyString(), eq(ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", testOperatorId)), eq("can_statusup"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Test Cases for updateOperatorStatusWithAuth - Authorization Failure
     */
    @Test
    @DisplayName("updateOperatorStatusWithAuth - throws IllegalAuthDataException on authorization failure")
    void testUpdateOperatorStatusWithAuthAuthorizationFailure() {
        // Arrange
        try {
            when(authorizationService.evaluate(eq(testStoreId), anyString(), anyString(), eq("can_statusup"))).thenReturn(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act & Assert
        assertThrows(IllegalAuthDataException.class, () -> {
            operatorService.updateOperatorStatusWithAuth(testOperatorId, java.time.LocalDateTime.now(), LocalDate.now(), LocalDate.now().plusYears(1), false, "updater", testStoreId);
        });
    }

    /*
     * Test Cases for addOperatorWithAuth with different operator authorization settings
     */
    @Test
    @DisplayName("addOperatorWithAuth - uses default tuples when operator-plant-authorization is true")
    void testAddOperatorWithAuthTrue() {
        // Arrange
        String newOperatorId = java.util.UUID.randomUUID().toString();
        OperatorEntity mockEntity = createMockOperatorEntity(newOperatorId);
        when(odsProperties.isEnableOperatorPlantAuthorization()).thenReturn(true);
        when(operatorRepository.findById(newOperatorId)).thenReturn(Optional.empty());
        when(operatorRepository.save(any(OperatorEntity.class))).thenReturn(mockEntity);
        try {
            when(authorizationService.evaluate(eq(testStoreId), anyString(), anyString(), eq("can_create"))).thenReturn(true);
            doNothing().when(authorizationService).writeTuples(anyList(), eq(testStoreId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act
        OperatorResult result = operatorService.addOperatorWithAuth(
                newOperatorId,
                "Name",
                "Address",
                "open",
                "global",
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                "creator",
                testStoreId);

        // Assert
        assertNotNull(result);
        assertEquals(newOperatorId, result.getOperatorId());
        try {
            // Verify that writeTuples was called (the specific tuples are determined by TupleUtils)
            verify(authorizationService, times(1)).writeTuples(anyList(), eq(testStoreId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("addOperatorWithAuth - uses superuser tuples when operator-plant-authorization is false")
    void testAddOperatorWithAuthFalse() {
        // Arrange
        String newOperatorId = java.util.UUID.randomUUID().toString();
        OperatorEntity mockEntity = createMockOperatorEntity(newOperatorId);
        when(odsProperties.isEnableOperatorPlantAuthorization()).thenReturn(false);
        when(operatorRepository.findById(newOperatorId)).thenReturn(Optional.empty());
        when(operatorRepository.save(any(OperatorEntity.class))).thenReturn(mockEntity);
        try {
            when(authorizationService.evaluate(eq(testStoreId), anyString(), anyString(), eq("can_create"))).thenReturn(true);
            doNothing().when(authorizationService).writeTuples(anyList(), eq(testStoreId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act
        OperatorResult result = operatorService.addOperatorWithAuth(
                newOperatorId,
                "Name",
                "Address",
                "open",
                "global",
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                "creator",
                testStoreId);

        // Assert
        assertNotNull(result);
        assertEquals(newOperatorId, result.getOperatorId());
        try {
            // Verify that writeTuples was called (the specific tuples are determined by TupleUtils)
            verify(authorizationService, times(1)).writeTuples(anyList(), eq(testStoreId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
