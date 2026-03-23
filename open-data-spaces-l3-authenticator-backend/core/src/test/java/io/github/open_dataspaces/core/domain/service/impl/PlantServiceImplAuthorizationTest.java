/*
 * PlantServiceImplAuthorizationTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for PlantServiceImplWithAuthorization.
 *
 * Date: 2025/12/31
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Collections;

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
import io.github.open_dataspaces.core.domain.dto.PlantResult;
import io.github.open_dataspaces.core.domain.dto.StateString;
import io.github.open_dataspaces.core.domain.entities.PlantEntity;
import io.github.open_dataspaces.core.domain.entities.OperatorEntity;
import io.github.open_dataspaces.core.domain.repository.interfaces.PlantRepository;
import io.github.open_dataspaces.core.domain.repository.interfaces.OperatorRepository;
import io.github.open_dataspaces.core.domain.service.interfaces.AuthorizationService;
import io.github.open_dataspaces.core.exception.IllegalAuthDataException;
import io.github.open_dataspaces.core.common.consts.ConstPath;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnexpectedException;

/**
 * PlantServiceImpl Authorization Tests.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PlantServiceImpl Authorization Tests")
class PlantServiceImplAuthorizationTest {

    @Mock
    private PlantRepository plantRepository;

    @Mock
    private OperatorRepository operatorRepository;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private ODSProperties odsProperties;

    private PlantServiceImpl plantService;

    private final String testStoreId = "test-store-id";
    private final String testUserId = "user-uuid";
    private final String testPlantId = "plant-100";

    @BeforeEach
    void setUp() {
        plantService = new PlantServiceImpl(odsProperties, plantRepository, operatorRepository, authorizationService);
    }

    /*
     * Test Cases for getPlantWithAuth - Authorization Success
    */
    @Test
    @DisplayName("getPlant - returns plant information on authorization success")
    void testGetPlantAuthorizationSuccess() {
        PlantEntity entity = createMockPlantEntity(testPlantId);
        try {
            when(authorizationService.evaluate(eq(testStoreId), anyString(), anyString(), eq("can_get"))).thenReturn(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(plantRepository.findById(testPlantId)).thenReturn(Optional.of(entity));

        PlantResult res = plantService.getPlantWithAuth(testPlantId, testUserId, testStoreId);

        assertNotNull(res);
        assertEquals(testPlantId, res.getPlantId());
        try {
            verify(authorizationService, times(1)).evaluate(eq(testStoreId), eq(testUserId), eq(ConstPath.TUPLE_OPERATOR_PLANT_PATH_WITH_ID_SHORT.replace("{plant_id}", testPlantId)), eq("can_get"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Test Cases for getPlantWithAuth - Authorization Failure
    */
    @Test
    @DisplayName("getPlant - throws IllegalAuthDataException on authorization failure")
    void testGetPlantAuthorizationFailure() {
        try {
            when(authorizationService.evaluate(eq(testStoreId), anyString(), anyString(), eq("can_get"))).thenReturn(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertThrows(IllegalAuthDataException.class, () -> {
            plantService.getPlantWithAuth(testPlantId, testUserId, testStoreId);
        });
    }

    /*
     * Test Cases for searchPlantsWithAuth
    */
    @Test
    @DisplayName("searchPlants - filters by authorization")
    void testSearchPlantsFilterByAuthorization() {
        PlantEntity p1 = createMockPlantEntity("p1");
        PlantEntity p2 = createMockPlantEntity("p2");
        PlantEntity p3 = createMockPlantEntity("p3");
        List<PlantEntity> all = Arrays.asList(p1, p2, p3);
        when(plantRepository.findWithLimitOffset(ArgumentMatchers.<Specification<PlantEntity>>any(), eq(0), eq(0), any(Sort.class))).thenReturn(all);
        try {
            java.util.List<java.util.Map<String, String>> resources = new java.util.ArrayList<>();
            java.util.Map<String, String> r1 = new java.util.HashMap<>();
            r1.put(Const.OPENFGA_EVALUATION_DEFAULT_KEY, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE);
            r1.put(Const.OPENFGA_EVALUATION_DEFAULT_ID, ConstPath.TUPLE_OPERATOR_PLANT_PATH_WITH_ID_SHORT.replace("{plant_id}", "p1"));
            resources.add(r1);
            java.util.Map<String, String> r2 = new java.util.HashMap<>();
            r2.put(Const.OPENFGA_EVALUATION_DEFAULT_KEY, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE);
            r2.put(Const.OPENFGA_EVALUATION_DEFAULT_ID, ConstPath.TUPLE_OPERATOR_PLANT_PATH_WITH_ID_SHORT.replace("{plant_id}", "p2"));
            resources.add(r2);
            java.util.Map<String, String> r3 = new java.util.HashMap<>();
            r3.put(Const.OPENFGA_EVALUATION_DEFAULT_KEY, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE);
            r3.put(Const.OPENFGA_EVALUATION_DEFAULT_ID, ConstPath.TUPLE_OPERATOR_PLANT_PATH_WITH_ID_SHORT.replace("{plant_id}", "p3"));
            resources.add(r3);
            when(authorizationService.evaluations(eq(testStoreId), eq(testUserId), eq("can_get"), eq(resources))).thenReturn(java.util.Arrays.asList(true, true, false));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<PlantResult> results = plantService.searchPlantsWithAuth(10, 0, "plant_id", "asc", null, null, null, StateString.unset(), null, null, null, null, testUserId, testStoreId);

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    /*
     * Additional Test Cases for searchPlantsWithAuth - limit <= 0
     */
    @Test
    @DisplayName("searchPlantsWithAuth - limit<=0 returns all authorized")
    void testSearchPlantsWithAuthLimitZeroReturnsAllAuthorized() {
        // Arrange
        PlantEntity p1 = createMockPlantEntity("X1");
        PlantEntity p2 = createMockPlantEntity("X2");
        PlantEntity p3 = createMockPlantEntity("X3");
        List<PlantEntity> page1 = Arrays.asList(p1, p2, p3);

        when(plantRepository.findWithLimitOffset(ArgumentMatchers.<Specification<PlantEntity>>any(), eq(0), eq(0), any(Sort.class)))
                .thenReturn(page1);
        try {
            when(authorizationService.evaluations(eq(testStoreId), eq(testUserId), eq("can_get"), any()))
                    .thenReturn(java.util.Arrays.asList(true, false, true));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act: limit 0 -> all authorized (2 items)
        List<PlantResult> results = plantService.searchPlantsWithAuth(
                0, 0, "plant_id", "asc", null, null, null, StateString.unset(), null, null, null, null, testUserId, testStoreId);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("X1", results.get(0).getPlantId());
        assertEquals("X3", results.get(1).getPlantId());
    }

    /*
     * Additional Test Cases for searchPlantsWithAuth - applies offset after authorization
     */
    @Test
    @DisplayName("searchPlantsWithAuth - applies offset after authorization")
    void testSearchPlantsWithAuthOffsetAfterAuthorization() {
        // Arrange: 3 authorized plants in order
        PlantEntity p1 = createMockPlantEntity("100");
        PlantEntity p2 = createMockPlantEntity("200");
        PlantEntity p3 = createMockPlantEntity("300");
        List<PlantEntity> page1 = Arrays.asList(p1, p2, p3);

        when(plantRepository.findWithLimitOffset(ArgumentMatchers.<Specification<PlantEntity>>any(), eq(0), eq(0), any(Sort.class)))
                .thenReturn(page1);

        try {
            when(authorizationService.evaluations(eq(testStoreId), eq(testUserId), eq("can_get"), any()))
                    .thenReturn(java.util.Arrays.asList(true, true, true));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act: limit=2, offset=1 -> expect second and third
        List<PlantResult> results = plantService.searchPlantsWithAuth(
                2, 1, "plant_id", "asc", null, null, null, StateString.unset(), null, null, null, null, testUserId, testStoreId);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("200", results.get(0).getPlantId());
        assertEquals("300", results.get(1).getPlantId());
    }

    /*
     * Additional Test Cases for searchPlantsWithAuth - Multi-page fetching under low authorization rate
     */
    @Test
    @DisplayName("searchPlantsWithAuth - offset beyond authorized size returns empty")
    void testSearchPlantsWithAuthOffsetBeyondSize() {
        // Arrange
        PlantEntity p1 = createMockPlantEntity("A");
        PlantEntity p2 = createMockPlantEntity("B");
        List<PlantEntity> all = Arrays.asList(p1, p2);
        when(plantRepository.findWithLimitOffset(ArgumentMatchers.<Specification<PlantEntity>>any(), eq(0), eq(0), any(Sort.class)))
                .thenReturn(all);
        try {
            when(authorizationService.evaluations(eq(testStoreId), eq(testUserId), eq("can_get"), any()))
                    .thenReturn(java.util.Arrays.asList(true, true));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act
        List<PlantResult> results = plantService.searchPlantsWithAuth(
                10, 5, "plant_id", "asc", null, null, null, StateString.unset(), null, null, null, null, testUserId, testStoreId);

        // Assert
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    @DisplayName("searchPlantsWithAuth - repository returns empty → no evaluations, empty result")
    void testSearchPlantsWithAuthRepoEmptyShortCircuits() {
        // Arrange
        when(plantRepository.findWithLimitOffset(ArgumentMatchers.<Specification<PlantEntity>>any(), eq(0), eq(0), any(Sort.class)))
                .thenReturn(Collections.emptyList());

        // Act
        List<PlantResult> results = plantService.searchPlantsWithAuth(
                10, 0, "plant_id", "asc", null, null, null, StateString.unset(), null, null, null, null, testUserId, testStoreId);

        // Assert
        assertNotNull(results);
        assertEquals(0, results.size());
        try {
            verify(authorizationService, times(0)).evaluations(anyString(), anyString(), anyString(), any());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("searchPlantsWithAuth - decisions shorter than resources is handled safely")
    void testSearchPlantsWithAuthShortDecisions() {
        // Arrange
        PlantEntity p1 = createMockPlantEntity("1");
        PlantEntity p2 = createMockPlantEntity("2");
        PlantEntity p3 = createMockPlantEntity("3");
        List<PlantEntity> all = Arrays.asList(p1, p2, p3);
        when(plantRepository.findWithLimitOffset(ArgumentMatchers.<Specification<PlantEntity>>any(), eq(0), eq(0), any(Sort.class)))
                .thenReturn(all);
        try {
            when(authorizationService.evaluations(eq(testStoreId), eq(testUserId), eq("can_get"), any()))
                    .thenReturn(Arrays.asList(true, false));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act & Assert: strict mode should throw due to size mismatch
        assertThrows(
                UnexpectedException.class,
                () -> plantService.searchPlantsWithAuth(
                10, 0, "plant_id", "asc", null, null, null, StateString.unset(), null, null, null, null, testUserId, testStoreId)
        );
    }

    /*
     * Test Cases for addPlantWithAuth
     * Note: Authorization is solely based on authorizationService.evaluate() result.
     * No pre-check on creator/operator identity is performed.
    */
    @Test
    @DisplayName("addPlantWithAuth - succeeds when evaluate returns true")
    void testAddPlantWithAuthSuccess() {
        String operatorId = java.util.UUID.randomUUID().toString();
        String creatorId = java.util.UUID.randomUUID().toString();
        String resource = ConstPath.TUPLE_OPERATOR_PLANT_PATH_SHORT.replace("{operator_id}", operatorId);
        try {
            // Mock authorization check returns true
            when(authorizationService.evaluate(eq(testStoreId), eq(creatorId), eq(resource), eq("can_create"))).thenReturn(true);
            // Mock operator type checks
            when(authorizationService.evaluate(eq(testStoreId), eq(Const.OPENFGA_EVALUATION_DEFAULT_USER_TYPE), eq(operatorId), eq(Const.TUPLE_REL_ADMIN), eq(Const.TUPLE_REL_SU_GROUP_NAME), eq(Const.TUPLE_REL_MEMBER))).thenReturn(false);
            when(authorizationService.evaluate(eq(testStoreId), eq(Const.OPENFGA_EVALUATION_DEFAULT_USER_TYPE), eq(operatorId), eq(Const.TUPLE_REL_ADMIN), eq(Const.TUPLE_REL_MANAGERS_GROUP_NAME), eq(Const.TUPLE_REL_MEMBER))).thenReturn(false);
            doNothing().when(authorizationService).writeTuples(anyList(), eq(testStoreId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(operatorRepository.findById(operatorId)).thenReturn(Optional.of(new OperatorEntity()));
        PlantEntity saved = createMockPlantEntity("generated");
        when(plantRepository.save(any(PlantEntity.class))).thenReturn(saved);

        PlantResult res = plantService.addPlantWithAuth(operatorId, "name", "addr", "open", "global", LocalDate.now(), LocalDate.now().plusDays(1), creatorId, testStoreId);

        assertNotNull(res);
        assertEquals(saved.getPlantId(), res.getPlantId());
        try {
            verify(authorizationService, times(1)).evaluate(eq(testStoreId), eq(creatorId), eq(resource), eq("can_create"));
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
     * Test Cases for addPlantWithAuth - Authorization Failure
    */
    @Test
    @DisplayName("addPlantWithAuth - fails when evaluate returns false")
    void testAddPlantWithAuthFailure() {
        try {
            when(authorizationService.evaluate(eq(testStoreId), anyString(), anyString(), eq("can_create"))).thenReturn(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertThrows(IllegalAuthDataException.class, () -> {
            plantService.addPlantWithAuth("op", "n", "a", "open", "global", LocalDate.now(), LocalDate.now().plusDays(1), "creator", testStoreId);
        });
    }

    /*
     * Test Cases for addPlantWithAuth - Tuple Write Failure
    */
    @Test
    void testAddPlantWithTupleWriteFailure() {
        // Arrange
        String operatorId = java.util.UUID.randomUUID().toString();
        when(operatorRepository.findById(operatorId)).thenReturn(Optional.of(new OperatorEntity()));
        PlantEntity saved = createMockPlantEntity("generated");
        when(plantRepository.save(any(PlantEntity.class))).thenReturn(saved);
        try {
            // Mock operator type checks
            String resource = ConstPath.TUPLE_OPERATOR_PLANT_PATH_SHORT.replace("{operator_id}", operatorId);
            when(authorizationService.evaluate(eq(testStoreId), eq(operatorId), eq(resource), eq("can_create"))).thenReturn(true);
            when(authorizationService.evaluate(eq(testStoreId), eq(Const.OPENFGA_EVALUATION_DEFAULT_USER_TYPE), eq(operatorId), eq(Const.TUPLE_REL_ADMIN), eq(Const.TUPLE_REL_SU_GROUP_NAME), eq(Const.TUPLE_REL_MEMBER))).thenReturn(false);
            when(authorizationService.evaluate(eq(testStoreId), eq(Const.OPENFGA_EVALUATION_DEFAULT_USER_TYPE), eq(operatorId), eq(Const.TUPLE_REL_ADMIN), eq(Const.TUPLE_REL_MANAGERS_GROUP_NAME), eq(Const.TUPLE_REL_MEMBER))).thenReturn(false);
            doThrow(new RuntimeException("writeTuples failed")).when(authorizationService).writeTuples(anyList(), eq(testStoreId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act & Assert
        UnexpectedException ex = assertThrows(UnexpectedException.class, () ->
                plantService.addPlantWithAuth(operatorId, "name", "addr", "open", "global", LocalDate.now(), LocalDate.now().plusDays(1), operatorId, testStoreId)
        );
        assertTrue(ex.getMessage().contains("writeTuples failed"));

        // Verify save attempted and writeTuples called
        verify(plantRepository, times(1)).save(any(PlantEntity.class));
        try {
            verify(authorizationService, times(1)).writeTuples(anyList(), eq(testStoreId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Test Cases for updatePlantWithAuth - Authorization Success
    */
    @Test
    @DisplayName("updatePlantWithAuth - successfully calls updatePlant on authorization success")
    void testUpdatePlantWithAuthAuthorizationSuccess() {
        PlantEntity existing = createMockPlantEntity(testPlantId);
        existing.setUpdatedAt(LocalDateTime.now());
        when(plantRepository.findById(testPlantId)).thenReturn(Optional.of(existing));
        try {
            when(authorizationService.evaluate(eq(testStoreId), anyString(), anyString(), eq("can_update"))).thenReturn(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        PlantResult res = plantService.updatePlantWithAuth(testPlantId, existing.getUpdatedAt(), null, null, null, StateString.unset(), "updater", testStoreId);

        assertNotNull(res);
        try {
            verify(authorizationService, times(1)).evaluate(eq(testStoreId), anyString(), eq(ConstPath.TUPLE_OPERATOR_PLANT_PATH_WITH_ID_SHORT.replace("{plant_id}", testPlantId)), eq("can_update"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Test Cases for updatePlantWithAuth - Authorization Failure
    */
    @Test
    @DisplayName("updatePlantWithAuth - throws IllegalAuthDataException on authorization failure")
    void testUpdatePlantWithAuthAuthorizationFailure() {
        try {
            when(authorizationService.evaluate(eq(testStoreId), anyString(), anyString(), eq("can_update"))).thenReturn(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertThrows(IllegalAuthDataException.class, () -> {
            plantService.updatePlantWithAuth(testPlantId, LocalDateTime.now(), null, null, null, StateString.unset(), "updater", testStoreId);
        });
    }

    /*
     * Test Cases for updatePlantStatusWithAuth - Authorization Success
    */
    @Test
    @DisplayName("updatePlantStatusWithAuth - successfully calls updatePlantStatus on authorization success")
    void testUpdatePlantStatusWithAuthAuthorizationSuccess() {
        PlantEntity existing = createMockPlantEntity(testPlantId);
        existing.setUpdatedAt(LocalDateTime.now());
        when(plantRepository.findById(testPlantId)).thenReturn(Optional.of(existing));
        when(plantRepository.saveAndFlush(any(PlantEntity.class))).thenReturn(existing);
        try {
            when(authorizationService.evaluate(eq(testStoreId), anyString(), anyString(), eq("can_statusup"))).thenReturn(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        PlantResult res = plantService.updatePlantStatusWithAuth(testPlantId, existing.getUpdatedAt(), LocalDate.now(), LocalDate.now().plusDays(1), false, "updater", testStoreId);

        assertNotNull(res);
        try {
            verify(authorizationService, times(1)).evaluate(eq(testStoreId), anyString(), eq(ConstPath.TUPLE_OPERATOR_PLANT_PATH_WITH_ID_SHORT.replace("{plant_id}", testPlantId)), eq("can_statusup"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Test Cases for updatePlantStatusWithAuth - Authorization Failure
    */
    @Test
    @DisplayName("updatePlantStatusWithAuth - throws IllegalAuthDataException on authorization failure")
    void testUpdatePlantStatusWithAuthAuthorizationFailure() {
        try {
            when(authorizationService.evaluate(eq(testStoreId), anyString(), anyString(), eq("can_statusup"))).thenReturn(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertThrows(IllegalAuthDataException.class, () -> {
            plantService.updatePlantStatusWithAuth(testPlantId, LocalDateTime.now(), LocalDate.now(), LocalDate.now().plusDays(1), false, "updater", testStoreId);
        });
    }

    /*
     * Test Cases for managing operator type check exception handling
    */
    @Test
    @DisplayName("addPlantWithAuth - rethrows known exception during managers group check")
    void testAddPlantWithAuthManagersCheckKnownException() throws Exception {
        // Arrange
        String operatorId = java.util.UUID.randomUUID().toString();
        OperatorEntity mockOperator = new OperatorEntity();
        mockOperator.setOperatorId(operatorId);
        when(operatorRepository.findById(operatorId)).thenReturn(Optional.of(mockOperator));

        when(plantRepository.findById(anyString())).thenReturn(Optional.empty());
        when(plantRepository.save(any(PlantEntity.class))).thenReturn(createMockPlantEntity("plant-mgr"));

        // Authorization for can_create succeeds
        String resource = ConstPath.TUPLE_OPERATOR_PLANT_PATH_SHORT.replace("{operator_id}", operatorId);
        when(authorizationService.evaluate(eq(testStoreId), eq(operatorId), eq(resource), eq("can_create")))
                .thenReturn(true);

        // Superuser check passes
        when(authorizationService.evaluate(eq(testStoreId), eq(Const.OPENFGA_EVALUATION_DEFAULT_USER_TYPE), eq(operatorId), eq(Const.TUPLE_REL_ADMIN), eq(Const.TUPLE_REL_SU_GROUP_NAME), eq(Const.TUPLE_REL_MEMBER)))
                .thenReturn(false);

        // Managers check throws a known exception
        when(authorizationService.evaluate(eq(testStoreId), eq(Const.OPENFGA_EVALUATION_DEFAULT_USER_TYPE), eq(operatorId), eq(Const.TUPLE_REL_ADMIN), eq(Const.TUPLE_REL_MANAGERS_GROUP_NAME), eq(Const.TUPLE_REL_MEMBER)))
                .thenThrow(new OutOfServiceException("Authorization service unavailable"));

        // Act & Assert
        assertThrows(OutOfServiceException.class, () -> {
            plantService.addPlantWithAuth(
                    operatorId,
                    "Test Plant",
                    "Test Address",
                    "plant-mgr",
                    null,
                    LocalDate.now(),
                    LocalDate.parse("2099-12-31"),
                    operatorId,
                    testStoreId
            );
        });
    }

    /*
     * Test Cases for addPlantWithAuth with operator type detection
     */
    @Test
    @DisplayName("addPlantWithAuth - detects superuser operator and uses superuser tuples")
    void testAddPlantWithAuthSuperuserOperator() throws Exception {
        // Arrange
        String operatorId = java.util.UUID.randomUUID().toString();
        OperatorEntity mockOperator = new OperatorEntity();
        mockOperator.setOperatorId(operatorId);
        when(operatorRepository.findById(operatorId)).thenReturn(Optional.of(mockOperator));

        PlantEntity savedEntity = createMockPlantEntity("plant-123");
        when(plantRepository.save(any(PlantEntity.class))).thenReturn(savedEntity);
        when(plantRepository.findById(anyString())).thenReturn(Optional.empty());

        // Mock authorization responses: superuser check returns true
        String resource = ConstPath.TUPLE_OPERATOR_PLANT_PATH_SHORT.replace("{operator_id}", operatorId);
        when(authorizationService.evaluate(eq(testStoreId), eq(operatorId), eq(resource), eq("can_create"))).thenReturn(true);
        when(authorizationService.evaluate(eq(testStoreId), eq(Const.OPENFGA_EVALUATION_DEFAULT_USER_TYPE), eq(operatorId), eq(Const.TUPLE_REL_ADMIN), eq(Const.TUPLE_REL_SU_GROUP_NAME), eq(Const.TUPLE_REL_MEMBER))).thenReturn(true);
        doNothing().when(authorizationService).writeTuples(anyList(), eq(testStoreId));

        // Act
        PlantResult result = plantService.addPlantWithAuth(
                operatorId,
                "Test Plant",
                "Test Address",
                "plant123",
                null,
                LocalDate.now(),
                LocalDate.parse("2099-12-31"),
                operatorId,
                testStoreId
        );

        // Assert
        assertNotNull(result);
        // Verify superuser check was performed
        verify(authorizationService, times(1)).evaluate(eq(testStoreId), eq(Const.OPENFGA_EVALUATION_DEFAULT_USER_TYPE), eq(operatorId), eq(Const.TUPLE_REL_ADMIN), eq(Const.TUPLE_REL_SU_GROUP_NAME), eq(Const.TUPLE_REL_MEMBER));
    }

    /*
     * Test Cases for addPlantWithAuth with operator type detection - managing operator
    */
    @Test
    @DisplayName("addPlantWithAuth - detects managing operator and uses managing tuples")
    void testAddPlantWithAuthManagingOperator() throws Exception {
        // Arrange
        String operatorId = java.util.UUID.randomUUID().toString();
        OperatorEntity mockOperator = new OperatorEntity();
        mockOperator.setOperatorId(operatorId);
        when(operatorRepository.findById(operatorId)).thenReturn(Optional.of(mockOperator));

        PlantEntity savedEntity = createMockPlantEntity("plant-456");
        when(plantRepository.save(any(PlantEntity.class))).thenReturn(savedEntity);
        when(plantRepository.findById(anyString())).thenReturn(Optional.empty());

        // Mock authorization responses: superuser check returns false, managing operator check returns true
        String resource = ConstPath.TUPLE_OPERATOR_PLANT_PATH_SHORT.replace("{operator_id}", operatorId);
        when(authorizationService.evaluate(eq(testStoreId), eq(operatorId), eq(resource), eq("can_create"))).thenReturn(true);
        when(authorizationService.evaluate(eq(testStoreId), eq(Const.OPENFGA_EVALUATION_DEFAULT_USER_TYPE), eq(operatorId), eq(Const.TUPLE_REL_ADMIN), eq(Const.TUPLE_REL_SU_GROUP_NAME), eq(Const.TUPLE_REL_MEMBER))).thenReturn(false);
        when(authorizationService.evaluate(eq(testStoreId), eq(Const.OPENFGA_EVALUATION_DEFAULT_USER_TYPE), eq(operatorId), eq(Const.TUPLE_REL_ADMIN), eq(Const.TUPLE_REL_MANAGERS_GROUP_NAME), eq(Const.TUPLE_REL_MEMBER))).thenReturn(true);
        doNothing().when(authorizationService).writeTuples(anyList(), eq(testStoreId));

        // Act
        PlantResult result = plantService.addPlantWithAuth(
                operatorId,
                "Test Plant",
                "Test Address",
                "plant456",
                null,
                LocalDate.now(),
                LocalDate.parse("2099-12-31"),
                operatorId,
                testStoreId
        );

        // Assert
        assertNotNull(result);
        // Verify both checks were performed
        verify(authorizationService, times(1)).evaluate(eq(testStoreId), eq(Const.OPENFGA_EVALUATION_DEFAULT_USER_TYPE), eq(operatorId), eq(Const.TUPLE_REL_ADMIN), eq(Const.TUPLE_REL_SU_GROUP_NAME), eq(Const.TUPLE_REL_MEMBER));
        verify(authorizationService, times(1)).evaluate(eq(testStoreId), eq(Const.OPENFGA_EVALUATION_DEFAULT_USER_TYPE), eq(operatorId), eq(Const.TUPLE_REL_ADMIN), eq(Const.TUPLE_REL_MANAGERS_GROUP_NAME), eq(Const.TUPLE_REL_MEMBER));
    }

    @Test
    @DisplayName("addPlantWithAuth - defaults to user operator tuples when not superuser or managing")
    void testAddPlantWithAuthUserOperator() throws Exception {
        // Arrange
        String operatorId = java.util.UUID.randomUUID().toString();
        OperatorEntity mockOperator = new OperatorEntity();
        mockOperator.setOperatorId(operatorId);
        when(operatorRepository.findById(operatorId)).thenReturn(Optional.of(mockOperator));

        PlantEntity savedEntity = createMockPlantEntity("plant-789");
        when(plantRepository.save(any(PlantEntity.class))).thenReturn(savedEntity);
        when(plantRepository.findById(anyString())).thenReturn(Optional.empty());

        // Mock authorization responses: both superuser and managing checks return false
        String resource = ConstPath.TUPLE_OPERATOR_PLANT_PATH_SHORT.replace("{operator_id}", operatorId);
        when(authorizationService.evaluate(eq(testStoreId), eq(operatorId), eq(resource), eq("can_create"))).thenReturn(true);
        when(authorizationService.evaluate(eq(testStoreId), eq(Const.OPENFGA_EVALUATION_DEFAULT_USER_TYPE), eq(operatorId), eq(Const.TUPLE_REL_ADMIN), eq(Const.TUPLE_REL_SU_GROUP_NAME), eq(Const.TUPLE_REL_MEMBER))).thenReturn(false);
        when(authorizationService.evaluate(eq(testStoreId), eq(Const.OPENFGA_EVALUATION_DEFAULT_USER_TYPE), eq(operatorId), eq(Const.TUPLE_REL_ADMIN), eq(Const.TUPLE_REL_MANAGERS_GROUP_NAME), eq(Const.TUPLE_REL_MEMBER))).thenReturn(false);
        doNothing().when(authorizationService).writeTuples(anyList(), eq(testStoreId));

        // Act
        PlantResult result = plantService.addPlantWithAuth(
                operatorId,
                "Test Plant",
                "Test Address",
                "plant789",
                null,
                LocalDate.now(),
                LocalDate.parse("2099-12-31"),
                operatorId,
                testStoreId
        );

        // Assert
        assertNotNull(result);
        // Verify all checks were performed
        verify(authorizationService, times(1)).evaluate(eq(testStoreId), eq(Const.OPENFGA_EVALUATION_DEFAULT_USER_TYPE), eq(operatorId), eq(Const.TUPLE_REL_ADMIN), eq(Const.TUPLE_REL_SU_GROUP_NAME), eq(Const.TUPLE_REL_MEMBER));
        verify(authorizationService, times(1)).evaluate(eq(testStoreId), eq(Const.OPENFGA_EVALUATION_DEFAULT_USER_TYPE), eq(operatorId), eq(Const.TUPLE_REL_ADMIN), eq(Const.TUPLE_REL_MANAGERS_GROUP_NAME), eq(Const.TUPLE_REL_MEMBER));
    }

    @Test
    @DisplayName("addPlantWithAuth - rethrows known exception during operator type check")
    void testAddPlantWithAuthOperatorTypeCheckKnownException() throws Exception {
        // Arrange
        String operatorId = java.util.UUID.randomUUID().toString();
        OperatorEntity mockOperator = new OperatorEntity();
        mockOperator.setOperatorId(operatorId);
        when(operatorRepository.findById(operatorId)).thenReturn(Optional.of(mockOperator));

        when(plantRepository.findById(anyString())).thenReturn(Optional.empty());
        when(plantRepository.save(any(PlantEntity.class))).thenReturn(createMockPlantEntity("plant-ex"));

        // Authorization for can_create succeeds
        String resource = ConstPath.TUPLE_OPERATOR_PLANT_PATH_SHORT.replace("{operator_id}", operatorId);
        when(authorizationService.evaluate(eq(testStoreId), eq(operatorId), eq(resource), eq("can_create")))
                .thenReturn(true);

        // Operator type check throws a known exception
        when(authorizationService.evaluate(eq(testStoreId), eq(Const.OPENFGA_EVALUATION_DEFAULT_USER_TYPE), eq(operatorId), eq(Const.TUPLE_REL_ADMIN), eq(Const.TUPLE_REL_SU_GROUP_NAME), eq(Const.TUPLE_REL_MEMBER)))
                .thenThrow(new OutOfServiceException("Authorization service unavailable"));

        // Act & Assert
        assertThrows(OutOfServiceException.class, () -> {
            plantService.addPlantWithAuth(
                    operatorId,
                    "Test Plant",
                    "Test Address",
                    "plant-ex",
                    null,
                    LocalDate.now(),
                    LocalDate.parse("2099-12-31"),
                    operatorId,
                    testStoreId
            );
        });
    }

    @Test
    @DisplayName("addPlantWithAuth - wraps unknown exception during operator type check")
    void testAddPlantWithAuthOperatorTypeCheckUnknownException() throws Exception {
        // Arrange
        String operatorId = java.util.UUID.randomUUID().toString();
        OperatorEntity mockOperator = new OperatorEntity();
        mockOperator.setOperatorId(operatorId);
        when(operatorRepository.findById(operatorId)).thenReturn(Optional.of(mockOperator));

        when(plantRepository.findById(anyString())).thenReturn(Optional.empty());
        when(plantRepository.save(any(PlantEntity.class))).thenReturn(createMockPlantEntity("plant-ex2"));

        // Authorization for can_create succeeds
        String resource = ConstPath.TUPLE_OPERATOR_PLANT_PATH_SHORT.replace("{operator_id}", operatorId);
        when(authorizationService.evaluate(eq(testStoreId), eq(operatorId), eq(resource), eq("can_create")))
                .thenReturn(true);

        // Operator type check throws an unknown exception
        when(authorizationService.evaluate(eq(testStoreId), eq(Const.OPENFGA_EVALUATION_DEFAULT_USER_TYPE), eq(operatorId), eq(Const.TUPLE_REL_ADMIN), eq(Const.TUPLE_REL_SU_GROUP_NAME), eq(Const.TUPLE_REL_MEMBER)))
                .thenThrow(new RuntimeException("Unexpected authorization error"));

        // Act & Assert
        assertThrows(UnexpectedException.class, () -> {
            plantService.addPlantWithAuth(
                    operatorId,
                    "Test Plant",
                    "Test Address",
                    "plant-ex2",
                    null,
                    LocalDate.now(),
                    LocalDate.parse("2099-12-31"),
                    operatorId,
                    testStoreId
            );
        });
    }

    private PlantEntity createMockPlantEntity(String id) {
        PlantEntity e = new PlantEntity();
        e.setPlantId(id);
        e.setOperatorId("op-1");
        e.setPlantName("name");
        e.setPlantAddress("addr");
        e.setOpenPlantId("open");
        e.setGlobalPlantId("global");
        e.setEffectiveStartDate(LocalDate.now().minusDays(1));
        e.setEffectiveEndDate(LocalDate.now().plusYears(1));
        e.setDeletedFlag(false);
        return e;
    }
}
