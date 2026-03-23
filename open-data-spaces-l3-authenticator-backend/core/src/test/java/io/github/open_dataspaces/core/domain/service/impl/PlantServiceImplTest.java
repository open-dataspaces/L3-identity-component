/*
 * PlantServiceImplTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for PlantServiceImpl.
 *
 * Date: 2025/09/22
 */

package io.github.open_dataspaces.core.domain.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import io.github.open_dataspaces.core.common.config.ODSProperties;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.dto.PlantResult;
import io.github.open_dataspaces.core.common.utils.DateUtils;
import io.github.open_dataspaces.core.domain.dto.StateString;
import io.github.open_dataspaces.core.domain.entities.OperatorEntity;
import io.github.open_dataspaces.core.domain.entities.PlantEntity;
import io.github.open_dataspaces.core.domain.repository.interfaces.OperatorRepository;
import io.github.open_dataspaces.core.domain.repository.interfaces.PlantRepository;
import io.github.open_dataspaces.core.domain.service.interfaces.AuthorizationService;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.UnexpectedException;

import java.util.UUID;
import io.github.open_dataspaces.core.exception.ConflictException;
import io.github.open_dataspaces.core.exception.IllegalAuthDataException;
import io.github.open_dataspaces.core.domain.dto.ListPlantRequest;

/**
 * Unit tests for the PlantServiceImpl class.
 */
@ExtendWith(MockitoExtension.class)
class PlantServiceImplTest {

    @Mock
    private PlantRepository plantRepository;
    @Mock
    private OperatorRepository operatorRepository;
    @InjectMocks
    private OperatorServiceImpl operatorService;
    @Captor
    private ArgumentCaptor<OperatorEntity> operatorEntityCaptor;
    @InjectMocks
    private PlantServiceImpl plantService;
    @Captor
    private ArgumentCaptor<PlantEntity> plantEntityCaptor;

    @Mock
    private ODSProperties odsProperties;
    // Common input parameters
    private final String commonPlantId = UUID.randomUUID().toString();
    private final Boolean commonDeletedFlag = false;
    private final String commonPlantName = "plantName";
    private final String commonPlantAddress = "plantAddress";
    private final String commonOpenPlantId = "openPlantId";
    private final String commonGlobalPlantId = "globalPlantId";
    private final String commonKey = Const.JSON_PROPERTY_PLANT_ID;
    private final String commonOrder = "asc";
    private final ListPlantRequest.SortKey commonSortKey = new ListPlantRequest.SortKey(commonKey, commonOrder);
    private final LocalDate commonEffectiveDate = LocalDate.now();
    private final String commonOperatorId = UUID.randomUUID().toString();
    private final int commonLimit = 0;
    private final int commonOffset = 0;

    @Mock
    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() throws Exception {
        odsProperties = Mockito.mock(ODSProperties.class);
        plantService = new PlantServiceImpl(odsProperties, plantRepository, operatorRepository, authorizationService);
    }

    /**
     * Test updatePlantStatus method for success cases.
     */
    @ParameterizedTest
    @CsvSource({
        // Valid case with all fields changed
        "07c52485-aef1-4bae-aaa0-80b5f6db0950, 2025-08-30T10:15:30.000Z, 2020-08-30, 2030-08-30, false",
        // Valid case with some fields unchanged
        "07c52485-aef1-4bae-aaa0-80b5f6db0951, 2025-09-01T12:00:00.000Z, null, 2030-08-30, false",
        "07c52485-aef1-4bae-aaa0-80b5f6db0951, 2025-09-01T12:00:00.000Z, 2020-08-30, null, false",
        "07c52485-aef1-4bae-aaa0-80b5f6db0950, 2025-08-30T10:15:30.000Z, 2020-08-30, 2030-08-30, null",
        // Valid case with empty strings
        "'', 2025-08-30T10:15:30.000Z, 2020-08-30, 2030-08-30, false",
    })
    @DisplayName("updatePlantStatus - Success Cases")
    void updatePlantStatus_success(
            String targetPlantId,
            String updatedAt,
            String strEffectiveStartDate,
            String strEffectiveEndDate,
            String strDeletedFlag) {

        // Handle "null" string to represent actual null values
        targetPlantId = "null".equals(targetPlantId) ? null : targetPlantId;
        LocalDate effectiveStartDate = "null".equals(strEffectiveStartDate) ? null : DateUtils.parseDate(strEffectiveStartDate, Const.DATE_FORMAT);
        LocalDate effectiveEndDate = "null".equals(strEffectiveEndDate) ? null : DateUtils.parseDate(strEffectiveEndDate, Const.DATE_FORMAT);
        Boolean deletedFlag = "null".equals(strDeletedFlag) ? null : Boolean.valueOf(strDeletedFlag);

        LocalDate orgEffectiveDate = LocalDate.now();
        Boolean orgDeletedFlag = (deletedFlag == null) ? false : !deletedFlag;
        String updateOperatorId = "d9a38406-cae2-4679-b052-15a75f5531e6";

        // Arrange
        LocalDateTime updatedDateTime = LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_DATE_TIME);
        PlantEntity existingEntity = new PlantEntity();
        existingEntity.setPlantId(targetPlantId);
        existingEntity.setUpdatedAt(updatedDateTime);
        existingEntity.setUpdatedUserId(updateOperatorId);

        existingEntity.setEffectiveStartDate(orgEffectiveDate);
        existingEntity.setEffectiveEndDate(orgEffectiveDate);
        existingEntity.setDeletedFlag(orgDeletedFlag);

        when(plantRepository.findById(targetPlantId)).thenReturn(Optional.of(existingEntity));
        when(plantRepository.saveAndFlush(any(PlantEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PlantResult result = plantService.updatePlantStatus(
                targetPlantId, updatedDateTime, effectiveStartDate, effectiveEndDate, deletedFlag, updateOperatorId);

        // Assert
        assertNotNull(result);
        assertEquals(existingEntity.getPlantId(), result.getPlantId());
        assertEquals((effectiveStartDate == null ? orgEffectiveDate : effectiveStartDate), result.getEffectiveStartDate());
        assertEquals((effectiveEndDate == null ? orgEffectiveDate : effectiveEndDate), result.getEffectiveEndDate());
        assertEquals((deletedFlag == null ? orgDeletedFlag : deletedFlag), result.isDeletedFlag());

        assertEquals(updateOperatorId, result.getUpdatedUserId());

        verify(plantRepository).findById(targetPlantId);
        if (!StringUtils.hasText(targetPlantId)) {
            verify(plantRepository).saveAndFlush(any(PlantEntity.class));
        }
    }

    /**
     * Test updatePlantStatus method when no changes are made.
     */
    @ParameterizedTest
    @CsvSource({
        "07c52485-aef1-4bae-aaa0-80b5f6db0950, org",
        "07c52485-aef1-4bae-aaa0-80b5f6db0950, null"
    })
    @DisplayName("updatePlantStatus - No Changes Made")
    void updatePlantStatus_noChangesMade(
            String targetPlantId,
            String noChangeMode) {

        LocalDate orgEffectiveDate = LocalDate.now();
        Boolean orgDeletedFlag = true;
        String updateOperatorId = "d9a38406-cae2-4679-b052-15a75f5531e6";

        LocalDate effectiveStartDate = null;
        LocalDate effectiveEndDate = null;
        Boolean deletedFlag = null;

        switch (noChangeMode) {
            case "org":
                effectiveStartDate = orgEffectiveDate;
                effectiveEndDate = orgEffectiveDate;
                deletedFlag = orgDeletedFlag;
                break;
            case "null":
                effectiveStartDate = null;
                effectiveEndDate = null;
                deletedFlag = null;
                break;
            default:
                throw new IllegalArgumentException("Invalid noChangeMode: " + noChangeMode);
        }

        // Arrange
        LocalDateTime updatedDateTime = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS);
        PlantEntity existingEntity = new PlantEntity();
        existingEntity.setPlantId(targetPlantId);
        existingEntity.setUpdatedAt(updatedDateTime);
        existingEntity.setUpdatedUserId(updateOperatorId);

        existingEntity.setEffectiveStartDate(orgEffectiveDate);
        existingEntity.setEffectiveEndDate(orgEffectiveDate);
        existingEntity.setDeletedFlag(orgDeletedFlag);

        when(plantRepository.findById(targetPlantId)).thenReturn(Optional.of(existingEntity));

        // Act
        PlantResult result = plantService.updatePlantStatus(
                targetPlantId, updatedDateTime, effectiveStartDate, effectiveEndDate, deletedFlag, updateOperatorId);

        // Assert
        assertNotNull(result);
        assertEquals(targetPlantId, result.getPlantId());

        assertEquals((effectiveStartDate == null ? orgEffectiveDate : effectiveStartDate), result.getEffectiveStartDate());
        assertEquals((effectiveEndDate == null ? orgEffectiveDate : effectiveEndDate), result.getEffectiveEndDate());
        assertEquals((deletedFlag == null ? orgDeletedFlag : deletedFlag), result.isDeletedFlag());

        assertEquals(updatedDateTime, result.getUpdatedAt());
        assertEquals(updateOperatorId, result.getUpdatedUserId());

        verify(plantRepository).findById(targetPlantId);
        verify(plantRepository, never()).saveAndFlush(any(PlantEntity.class));
    }

    /**
     * Test updatePlantStatus method when the target Plant is not found.
     */
    @Test
    @DisplayName("updatePlantStatus - Plant Not Found")
    void updatePlantStatus_plantNotFound() {

        String targetPlantId = "07c52485-aef1-4bae-aaa0-80b5f6db0950";
        String updatedAt = "2025-08-30T10:15:30.000Z";

        LocalDateTime updatedDateTime = LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_DATE_TIME);

        // Arrange
        when(plantRepository.findById(targetPlantId)).thenReturn(Optional.empty());

        // Act
        PlantResult result = plantService.updatePlantStatus(
                targetPlantId, updatedDateTime, null, null, null, null);

        // Assert
        assertEquals(null, result);
        verify(plantRepository).findById(targetPlantId);
    }

    /**
     * Test updatePlantStatus method for invalid effective dates.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // requested effectiveStartDate, requested effectiveEndDate, existing effectiveStartDate, existing effectiveEndDate
        "2025-12-31, 2025-01-01, 2025-12-30, 9999-12-31",
        "2026-01-01, 2025-12-31, 2026-01-01, 9999-12-31",
        "2026-02-01, 2026-01-31, 2026-02-01, 9999-12-31",
        "2025-12-31, NULL, 2025-01-01, 2025-12-30",
        "2026-01-02, NULL, 2025-12-31, 2026-01-01",
        "2026-02-02, NULL, 2026-01-31, 2026-02-01",
        "NULL, 2025-01-01, 2025-12-30, 9999-12-31",
        "NULL, 2025-12-31, 2026-01-01, 9999-12-31",
        "NULL, 2026-01-31, 2026-02-01, 9999-12-31",
    }, nullValues = "NULL")
    @DisplayName("updatePlantStatus - Invalid Effective Dates")
    void updatePlantStatus_invalidEffectiveDates(
            String strEffectiveStartDate,
            String strEffectiveEndDate,
            String orgEffectiveStartDateStr,
            String orgEffectiveEndDateStr
    ) {

        // Handle "null" string to represent actual null values
        String targetPlantId = "07c52485-aef1-4bae-aaa0-80b5f6db0950";
        LocalDate effectiveStartDate = strEffectiveStartDate == null ? null : DateUtils.parseDate(strEffectiveStartDate, Const.DATE_FORMAT);
        LocalDate effectiveEndDate = strEffectiveEndDate == null ? null : DateUtils.parseDate(strEffectiveEndDate, Const.DATE_FORMAT);
        LocalDate orgEffectiveStartDate = orgEffectiveStartDateStr == null ? null : DateUtils.parseDate(orgEffectiveStartDateStr, Const.DATE_FORMAT);
        LocalDate orgEffectiveEndDate = orgEffectiveEndDateStr == null ? null : DateUtils.parseDate(orgEffectiveEndDateStr, Const.DATE_FORMAT);
        LocalDateTime updatedDateTime = LocalDateTime.parse("2025-08-30T10:15:30.000Z", DateTimeFormatter.ISO_DATE_TIME);
        Boolean deletedFlag = false;

        String updateOperatorId = "d9a38406-cae2-4679-b052-15a75f5531e6";

        // Arrange
        PlantEntity existingEntity = new PlantEntity();
        existingEntity.setPlantId(targetPlantId);
        existingEntity.setUpdatedAt(updatedDateTime);
        existingEntity.setUpdatedUserId(updateOperatorId);

        existingEntity.setEffectiveStartDate(orgEffectiveStartDate);
        existingEntity.setEffectiveEndDate(orgEffectiveEndDate);
        existingEntity.setDeletedFlag(deletedFlag);

        when(plantRepository.findById(targetPlantId)).thenReturn(Optional.of(existingEntity));

        // Act & Assert
        BadParametersException exception = assertThrows(BadParametersException.class, () -> {
            plantService.updatePlantStatus(
                    targetPlantId, updatedDateTime, effectiveStartDate, effectiveEndDate, deletedFlag, updateOperatorId);
        });

        if (effectiveStartDate == null) {
            assertEquals(String.format(ConstError.ERRLOG_400_EFFECTIVE_DATE_START_AFTER_END,
                    Const.JSON_PROPERTY_EFFECTIVE_END_DATE,
                    DateUtils.formatDate(orgEffectiveStartDate, Const.DATE_FORMAT),
                    DateUtils.formatDate(effectiveEndDate, Const.DATE_FORMAT)), exception.getResponseMessage());
            assertEquals(String.format(ConstError.ERRLOG_400_EFFECTIVE_DATE_START_AFTER_END,
                    Const.JSON_PROPERTY_EFFECTIVE_END_DATE,
                    DateUtils.formatDate(orgEffectiveStartDate, Const.DATE_FORMAT),
                    DateUtils.formatDate(effectiveEndDate, Const.DATE_FORMAT)), exception.getLogMessage());
        } else if (strEffectiveEndDate == null) {
            assertEquals(String.format(ConstError.ERRLOG_400_EFFECTIVE_DATE_START_AFTER_END,
                    Const.JSON_PROPERTY_EFFECTIVE_START_DATE,
                    DateUtils.formatDate(effectiveStartDate, Const.DATE_FORMAT),
                    DateUtils.formatDate(orgEffectiveEndDate, Const.DATE_FORMAT)), exception.getResponseMessage());
            assertEquals(String.format(ConstError.ERRLOG_400_EFFECTIVE_DATE_START_AFTER_END,
                    Const.JSON_PROPERTY_EFFECTIVE_START_DATE,
                    DateUtils.formatDate(effectiveStartDate, Const.DATE_FORMAT),
                    DateUtils.formatDate(orgEffectiveEndDate, Const.DATE_FORMAT)), exception.getLogMessage());
        } else {
            assertEquals(String.format(ConstError.ERRLOG_400_EFFECTIVE_DATE_START_AFTER_END,
                    Const.JSON_PROPERTY_EFFECTIVE_START_DATE,
                    DateUtils.formatDate(effectiveStartDate, Const.DATE_FORMAT),
                    DateUtils.formatDate(effectiveEndDate, Const.DATE_FORMAT)), exception.getResponseMessage());
            assertEquals(String.format(ConstError.ERRLOG_400_EFFECTIVE_DATE_START_AFTER_END,
                    Const.JSON_PROPERTY_EFFECTIVE_START_DATE,
                    DateUtils.formatDate(effectiveStartDate, Const.DATE_FORMAT),
                    DateUtils.formatDate(effectiveEndDate, Const.DATE_FORMAT)), exception.getLogMessage());
        }
    }

    /**
     * Test updatePlantStatus method for data update conflict.
     */
    @Test
    @DisplayName("updatePlantStatus - Data Update Conflict")
    void updatePlantStatus_dataUpdateConflict() {

        String targetPlantId = "07c52485-aef1-4bae-aaa0-80b5f6db0950";
        String updatedAt = "2025-08-30T10:15:30.000Z";
        String updateOperatorId = "d9a38406-cae2-4679-b052-15a75f5531e6";

        LocalDateTime updatedDateTime = LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_DATE_TIME);

        // Arrange
        PlantEntity existingEntity = new PlantEntity(
                targetPlantId,
                updateOperatorId,
                "Old Plant",
                "Old Address",
                "oldOpenId",
                "oldGlobalId",
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                "creator123");
        existingEntity.setUpdatedAt(updatedDateTime.minusDays(1)); // Simulate a conflict

        when(plantRepository.findById(targetPlantId)).thenReturn(Optional.of(existingEntity));

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            plantService.updatePlantStatus(
                    targetPlantId, updatedDateTime, null, null, null, null);
        });

        assertTrue(exception.getLogMessage().contains(ConstError.ERRLOG_409_CONFLICT));
        verify(plantRepository).findById(targetPlantId);
    }

    /**
     * case#01:
     * getPlant_Success tests the successful retrieval of a plant.(not null case).
     */
    @Test
    void getPlant_success() {
        String plantId = "d9a38406-cae2-4679-b052-15a75f5531e6";
        String operatorId = "d9a38406-cae2-4679-b052-15a75f5531e6";
        String plantName = "テスト事業所";
        String plantAddress = "テスト県テスト市テストビル1F";
        String openPlantId = "openId123";
        String globalPlantId = "globalId123";
        boolean deletedFlag = false;
        LocalDate effectiveStartDate = LocalDate.now(ZoneOffset.UTC);
        LocalDate effectiveEndDate = LocalDate.now(ZoneOffset.UTC).plusYears(1);
        LocalDateTime createdAt = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime updatedAt = LocalDateTime.now(ZoneOffset.UTC).plusHours(1);
        String createdUserId = "createUserId123";
        String updatedUserId = "updateUserId123";

        PlantEntity testEntity = new PlantEntity();
        testEntity.setPlantId(plantId);
        testEntity.setOperatorId(operatorId);
        testEntity.setPlantName(plantName);
        testEntity.setPlantAddress(plantAddress);
        testEntity.setOpenPlantId(openPlantId);
        testEntity.setGlobalPlantId(globalPlantId);
        testEntity.setDeletedFlag(deletedFlag);
        testEntity.setEffectiveStartDate(effectiveStartDate);
        testEntity.setEffectiveEndDate(effectiveEndDate);
        testEntity.setCreatedAt(createdAt);
        testEntity.setUpdatedAt(updatedAt);
        testEntity.setCreatedUserId(createdUserId);
        testEntity.setUpdatedUserId(updatedUserId);

        Optional<PlantEntity> optionalPlantEntity = Optional.of(testEntity);
        // Arrange

        when(plantRepository.findById(
            plantId
        )).thenReturn(optionalPlantEntity);

        // Act
        PlantResult serviceResult = plantService.getPlant(plantId);

        // Assert
        assertEquals(plantId, serviceResult.getPlantId());
        assertEquals(operatorId, serviceResult.getOperatorId());
        assertEquals(plantName, serviceResult.getPlantName());
        assertEquals(plantAddress, serviceResult.getPlantAddress());
        assertEquals(openPlantId, serviceResult.getOpenPlantId());
        assertEquals(globalPlantId, serviceResult.getGlobalPlantId());
        assertEquals(effectiveStartDate, serviceResult.getEffectiveStartDate());
        assertEquals(effectiveEndDate, serviceResult.getEffectiveEndDate());
        assertEquals(createdAt, serviceResult.getCreatedAt());
        assertEquals(updatedAt, serviceResult.getUpdatedAt());
        assertEquals(deletedFlag, serviceResult.isDeletedFlag());
    }

    /**
     * case#04:
     * getPlantEntity_returnEmpty tests the successful retrieval of a plant when no plant is found.
     */
    @Test
    void getPlant_returnEmpty() {
        String plantId = "emptyPlantId";

        // Arrange
        when(plantRepository.findById(
            plantId
        )).thenReturn(Optional.empty());

        // Act
        PlantResult serviceResult = plantService.getPlant(plantId);

        // Assert
        assertNull(serviceResult);
    }

    /**
     * case#06:
     * getPlant_DbException tests the handling of a database exception during plant retrieval.
     */
    @Test
    void getPlant_exception() {
        String plantId = "d9a38406-cae2-4679-b052-15a75f5531e6";

        when(plantRepository.findById(plantId))
                .thenThrow(new RuntimeException("DB connection error"));
        assertThrows(RuntimeException.class, () -> {
            plantService.getPlant(plantId);
        });
    }

    /**
     * case#1:
     * Test for searchPlants.
     */
    @Test
    void searchPlants_success() {
        List<PlantEntity> entities = Arrays.asList(mock(PlantEntity.class), mock(PlantEntity.class));
        when(plantRepository.findWithLimitOffset(any(), anyInt(), anyInt(), any())).thenReturn(entities);

        List<PlantResult> results = plantService.searchPlants(
                commonLimit, commonOffset, commonKey, commonOrder,
                commonOperatorId, commonPlantId, commonOpenPlantId, StateString.of(commonGlobalPlantId),  commonPlantName,
                commonPlantAddress, commonDeletedFlag, commonEffectiveDate
        );
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    /**
     * Parameterized test for searchPlants.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "VALUE, VALUE, operator_id, asc, VALUE, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY", // Filter by operatorId
        "VALUE, VALUE, plant_id, asc, EMPTY, VALUE, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY", // Filter by plantId
        "VALUE, VALUE, open_plant_id, asc, EMPTY, EMPTY, VALUE, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY", // Filter by openPlantId
        "VALUE, VALUE, global_plant_id, asc, EMPTY, EMPTY, EMPTY, VALUE, EMPTY, EMPTY, EMPTY, EMPTY", // Filter by globalPlantId
        "VALUE, VALUE, plant_name, asc, EMPTY, EMPTY, EMPTY, EMPTY, VALUE, EMPTY, EMPTY, EMPTY", // Filter by plantName
        "VALUE, VALUE, plant_name, asc, EMPTY, EMPTY, EMPTY, EMPTY, Test*, EMPTY, EMPTY, EMPTY", // PartialPrefix by plantName
        "VALUE, VALUE, plant_name, asc, EMPTY, EMPTY, EMPTY, EMPTY, *Name, EMPTY, EMPTY, EMPTY", // PartialSuffix by plantName
        "VALUE, VALUE, plant_address, asc, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, VALUE, EMPTY, EMPTY", // Filter by plantAddress
        "VALUE, VALUE, plant_address, asc, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, To*, EMPTY, EMPTY", // PartialPrefix by plantAddress
        "VALUE, VALUE, plant_address, asc, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, *kyo, EMPTY, EMPTY", // PartialSuffix by plantAddress
        "VALUE, VALUE, deleted_flag, asc, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, VALUE, EMPTY", // Filter by deletedFlag
        "VALUE, VALUE, operator_id, asc, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, VALUE", // Filter by effectiveDate
        "VALUE, VALUE, plant_id, asc, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, VALUE", // Filter by effectiveDate
    }, nullValues = "NULL")
    void searchPlants_variousSpecs(
            String argLimit,
            String argOffset,
            String argOrderByKey,
            String argOrderByDirection,
            String argOperatorId,
            String argPlantId,
            String argPlantName,
            String argPlantAddress,
            String argOpenPlantId,
            String argGlobalPlantId,
            String argDeletedFlag,
            String argEffectiveDate
    ) {
        Integer limit = "VALUE".equals(argLimit) ? commonLimit :
                Integer.valueOf(argLimit);
        Integer offset = "VALUE".equals(argOffset) ? commonOffset :
                Integer.valueOf(argOffset);
        ListPlantRequest.SortKey sort = "VALUE".equals(argOrderByKey) ? commonSortKey :
        new ListPlantRequest.SortKey(argOrderByKey, argOrderByDirection);
        String operatorId =
                "UUID".equals(argOperatorId) ? commonOperatorId :
                "EMPTY".equals(argOperatorId) ? null :
                argOperatorId;
        String plantId =
                "UUID".equals(argPlantId) ? commonPlantId :
                "EMPTY".equals(argPlantId) ? null :
                argPlantId;
        String plantName =
                "EMPTY".equals(argPlantName) ? null :
                "VALUE".equals(argPlantName) ? commonPlantName :
                argPlantName;
        String plantAddress =
                "EMPTY".equals(argPlantAddress) ? null :
                "VALUE".equals(argPlantAddress) ? commonPlantAddress :
                argPlantAddress;
        String openPlantId =
                "EMPTY".equals(argOpenPlantId) ? null :
                "VALUE".equals(argOpenPlantId) ? commonOpenPlantId :
                argOpenPlantId;
        StateString globalPlantId =
                "EMPTY".equals(argGlobalPlantId) ? StateString.unset() :
                "VALUE".equals(argGlobalPlantId) ? StateString.of(commonGlobalPlantId) :
                StateString.of(argGlobalPlantId);
        Boolean deletedFlag =
                "EMPTY".equals(argDeletedFlag) ? null :
                "VALUE".equals(argDeletedFlag) ? commonDeletedFlag :
                Boolean.valueOf(argDeletedFlag);
        LocalDate effectiveDate =
                "EMPTY".equals(argEffectiveDate) ? null :
                "VALUE".equals(argEffectiveDate) ? commonEffectiveDate :
                LocalDate.parse(argEffectiveDate);
        List<PlantEntity> entities = Arrays.asList(mock(PlantEntity.class), mock(PlantEntity.class));

        // Mock to return data that matches the search conditions.
        when(plantRepository.findWithLimitOffset(any(), anyInt(), anyInt(), any())).thenReturn(entities);

        // Act
        List<PlantResult> results = plantService.searchPlants(
                limit, offset, sort.getKey(), sort.getOrder(), operatorId,
                plantId, openPlantId, globalPlantId, plantName, plantAddress, deletedFlag, effectiveDate
        );
        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(plantRepository).findWithLimitOffset(any(), anyInt(), anyInt(), any());
    }

    /**
     * case#2:
     * Test for searchPlants when no results found.
     */
    @Test
    void searchPlants_returnsEmptyList() {
        when(plantRepository.findWithLimitOffset(any(), anyInt(), anyInt(), any(Sort.class)))
                .thenReturn(Collections.emptyList());

        List<PlantResult> results = plantService.searchPlants(
                commonLimit, commonOffset, commonKey, commonOrder,
                commonOperatorId, commonPlantId, commonOpenPlantId, StateString.of(commonGlobalPlantId),  commonPlantName,
                commonPlantAddress, commonDeletedFlag, commonEffectiveDate
        );

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /**
     * Test updateOperator method for success cases.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // Valid case with all fields changed
        "apiKey123,plant123,operator123,2025-08-30T10:15:30.000Z,Plant Name,Address1,open123,global123,updater123",
        // Valid case with some fields null(no change)
        "apiKey123,plant123,operator123,2025-08-30T10:15:30.000Z,NULL,Address1,open123,global123,updater123",
        "apiKey123,plant123,operator123,2025-08-30T10:15:30.000Z,Plant Name,NULL,open123,global123,updater123",
        "apiKey123,plant123,operator123,2025-08-30T10:15:30.000Z,Plant Name,Address1,NULL,global123,updater123",
        "apiKey123,plant123,operator123,2025-08-30T10:15:30.000Z,Plant Name,Address1,open123,null,updater123",
        // Valid case with some fields empty(no change)
        "apiKey123,plant123,operator123,2025-08-30T10:15:30.000Z,Plant Name,Address1,open123,empty,updater123",
    }, nullValues = "NULL")
    @DisplayName("updatePlant - Success Cases")
    void updatePlant_success(
            String apiKey,
            String targetPlantId,
            String targetOperatorId,
            String updatedAt,
            String plantName,
            String plantAddress,
            String openPlantId,
            String globalPlantId,
            String updateOperatorId) {
        // Handle "null" string to represent actual null values
        StateString stateGlobalPlantId;
        switch (globalPlantId) {
            case "null":
                stateGlobalPlantId = StateString.of(null);
                break;
            case "empty":
                stateGlobalPlantId = StateString.unset();
                break;
            default:
                stateGlobalPlantId = StateString.of(globalPlantId);
                break;
        }

        // Arrange
        LocalDateTime updatedDateTime = LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_DATE_TIME);
        PlantEntity existingEntity = new PlantEntity(
                "OldPlantId", "OldOperatorId", "Old PlantName", "Old Address", "oldOpenId", "oldGlobalId",
                LocalDate.now(), LocalDate.now().plusYears(1), "creator123");
        existingEntity.setUpdatedAt(updatedDateTime);

        when(plantRepository.findById(targetPlantId)).thenReturn(Optional.of(existingEntity));
        when(plantRepository.saveAndFlush(any(PlantEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PlantResult result = plantService.updatePlant(
                targetPlantId, updatedDateTime,
                plantName, plantAddress, openPlantId, stateGlobalPlantId,
                updateOperatorId);

        // Assert
        assertNotNull(result);
        assertEquals(existingEntity.getOperatorId(), result.getOperatorId());
        // If the value is "empty", use the existing value; otherwise, use the input value
        if (plantName == null) {
            result.getPlantName().equals("Old PlantName");
        } else {
            result.getPlantName().equals(plantName);
        }
        if (plantAddress == null) {
            result.getPlantAddress().equals("Old Address");
        } else {
            result.getPlantAddress().equals(plantAddress);
        }
        if (openPlantId == null) {
            result.getOpenPlantId().equals("oldOpenId");
        } else {
            result.getOpenPlantId().equals(openPlantId);
        }
        // Only globalPlantId is allowed to be null
        switch (globalPlantId) {
            case "null":
                assertNull(result.getGlobalPlantId());
                break;
            case "empty":
                assertEquals("oldGlobalId", result.getGlobalPlantId());
                break;
            default:
                assertEquals(globalPlantId, result.getGlobalPlantId());
                break;
        }
        assertEquals(updateOperatorId, result.getUpdatedUserId());
        verify(plantRepository).findById(targetPlantId);
        verify(plantRepository).saveAndFlush(any(PlantEntity.class));
    }

    /**
     * Test updateOperator method when the target operator is not found.
     */
    @ParameterizedTest
    @CsvSource({
        "apiKey123,plant123,operator123,2025-08-30T10:15:30.000Z,Plant Name,Address1,open123,global123,updater123"
    })
    @DisplayName("updatePlant - Plant Not Found")
    void updatePlant_plantNotFound(
            String apiKey,
            String targetPlantId,
            String targetOperatorId,
            String updatedAt,
            String plantName,
            String plantAddress,
            String openPlantId,
            String globalPlantId,
            String updateOperatorId) {

        // Arrange
        LocalDateTime updatedDateTime = LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_DATE_TIME);
        when(plantRepository.findById(targetPlantId)).thenReturn(Optional.empty());

        // Act
        PlantResult result = plantService.updatePlant(
                targetPlantId, updatedDateTime,
                plantName, plantAddress, openPlantId, StateString.of(globalPlantId),
                updateOperatorId);

        // Assert
        assertEquals(null, result);
        verify(plantRepository).findById(targetPlantId);
    }

    /**
     * Test updateOperator method when no changes are made.
     */
    @ParameterizedTest
    @CsvSource({
        "apiKey123, plant123, operator123, 2025-08-30T10:15:30.000Z, Old Operator, Old Address, oldOpenId, oldGlobalId, updater123"
    })
    @DisplayName("updateOperator - No Changes Made")
    void updateOperator_noChangesMade(
            String apiKey,
            String targetPlantId,
            String targetOperatorId,
            String updatedAt,
            String plantName,
            String plantAddress,
            String openPlantId,
            String globalPlantId,
            String updateOperatorId) {

        // Arrange
        LocalDateTime updatedDateTime = LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_DATE_TIME);
        PlantEntity existingEntity = new PlantEntity(
                targetPlantId, targetOperatorId, plantName, plantAddress, openPlantId, globalPlantId,
                LocalDate.now(), LocalDate.now().plusYears(1), "creator123");
        existingEntity.setUpdatedAt(updatedDateTime);

        when(plantRepository.findById(targetPlantId)).thenReturn(Optional.of(existingEntity));

        // Act
        PlantResult result = plantService.updatePlant(
                targetPlantId, updatedDateTime,
                plantName, plantAddress, openPlantId, StateString.of(globalPlantId),
                updateOperatorId);

        // Assert
        assertNotNull(result);
        assertEquals(targetPlantId, result.getPlantId());
        assertEquals(targetOperatorId, result.getOperatorId());
        assertEquals(plantName, result.getPlantName());
        assertEquals(plantAddress, result.getPlantAddress());
        assertEquals(openPlantId, result.getOpenPlantId());
        assertEquals(globalPlantId, result.getGlobalPlantId());
        assertEquals(updatedAt, result.getUpdatedAt().format(DateTimeFormatter.ofPattern(Const.ISO_8601_UTC_MILLISECOND_FORMAT)));
        assertEquals("creator123", result.getUpdatedUserId());
        verify(plantRepository).findById(targetPlantId);
        verify(plantRepository, never()).saveAndFlush(any(PlantEntity.class));
    }

    /**
     * Test updateOperator method for data disabled scenarios.
     */
    @ParameterizedTest
    @CsvSource({
        "true, VALUE, VALUE",   // deletedFlag is true
        "false, AFTER, VALUE",  // effectiveStartDate is after today
        "false, VALUE, BEFORE"  // effectiveEndDate is before today
    })
    @DisplayName("updateOperator - Data disabled")
    void updateOperator_dataDisabled(
            String argDeletedFlag,
            String argEffectiveStartDate,
            String argEffectiveEndDate) {

        boolean deletedFlag = Boolean.parseBoolean(argDeletedFlag);
        LocalDate effectiveStartDate = ("AFTER".equals(argEffectiveStartDate) ? LocalDate.now().plusDays(1) :
                LocalDate.now());
        LocalDate effectiveEndDate = ("BEFORE".equals(argEffectiveEndDate) ? LocalDate.now().minusDays(1) :
                LocalDate.now());

        String targetOperatorId = "operator123";
        LocalDateTime updatedAt = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS);
        String plantName = "Plant Name";
        String plantAddress = "Address1";
        String openPlantId = "open123";
        String globalPlantId = "global123";
        String updateOperatorId = "updater123";

        PlantEntity existingEntity = new PlantEntity();
        existingEntity.setDeletedFlag(deletedFlag);
        existingEntity.setEffectiveStartDate(effectiveStartDate);
        existingEntity.setEffectiveEndDate(effectiveEndDate);

        // Arrange
        when(plantRepository.findById(targetOperatorId)).thenReturn(Optional.of(existingEntity));

        // Act & Assert
        IllegalAuthDataException exception = assertThrows(IllegalAuthDataException.class, () -> {
            plantService.updatePlant(
                    targetOperatorId, updatedAt,
                    plantName, plantAddress, openPlantId, StateString.of(globalPlantId),
                    updateOperatorId);
        });

        assertTrue(exception.getLogMessage().contains(String.format(ConstError.ERRLOG_403_FORBIDDEN_RESOURCE_DISABLED, targetOperatorId)));
        verify(plantRepository).findById(targetOperatorId);
    }

    /**
     * Test updatePlant method for data update conflict.
     */
    @ParameterizedTest
    @CsvSource({
        "apiKey123,plant123,operator123,2025-08-30T10:15:30.000Z,Plant Name,Address1,open123,global123,updater123"
    })
    @DisplayName("updatePlant - Data Update Conflict")
    void updatePlant_dataUpdateConflict(
            String apiKey,
            String targetPlantId,
            String targetOperatorId,
            String updatedAt,
            String plantName,
            String plantAddress,
            String openPlantId,
            String globalPlantId,
            String updateOperatorId) {

        // Arrange
        LocalDateTime updatedDateTime = LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_DATE_TIME);
        PlantEntity existingEntity = new PlantEntity(
                targetPlantId, targetOperatorId, plantName, plantAddress, openPlantId, globalPlantId,
                LocalDate.now(), LocalDate.now().plusYears(1), "creator123");
        existingEntity.setUpdatedAt(updatedDateTime.minusDays(1)); // Simulate a conflict

        when(plantRepository.findById(targetPlantId)).thenReturn(Optional.of(existingEntity));

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            plantService.updatePlant(
                    targetPlantId, updatedDateTime,
                    plantName, plantAddress, openPlantId, StateString.of(globalPlantId),
                    updateOperatorId);
        });

        assertTrue(exception.getLogMessage().contains(ConstError.ERRLOG_409_CONFLICT));
        verify(plantRepository).findById(targetPlantId);
    }

    /**
     * Test addPlant method for success cases.
     */
    @ParameterizedTest
    @CsvSource({
        // Normal cases with all fields provided
        "RAND, Plant Name 1, Address 1, open-id-123456, global-id-1, 2025-01-01, 9999-12-31, creator-id-1",
        // Case where effectiveEndDate is empty
        "RAND, Plant Name 2, Address 2, open-id-234567, global-id-2, 2025-01-01, null, creator-id-2",
        // Case where effectiveStartDate is empty
        "RAND, Plant Name 2, Address 2, open-id-234567, global-id-2, null, 9999-12-31, creator-id-2",
        // Case where both effectiveStartDate and effectiveEndDate are empty
        "RAND, Plant Name 3, Address 3, open-id-345678, global-id-3, null, null, creator-id-3"
    })
    @DisplayName("addPlant - Success")
    void addPlant_success(
            String operatorId,
            String plantName,
            String plantAddress,
            String openPlantId,
            String globalPlantId,
            String effectiveStartDate,
            String effectiveEndDate,
            String creatorOperatorId) {

        LocalDate effectiveEndLocalDate = (effectiveEndDate != null && effectiveEndDate.equals("null"))
                ? null : LocalDate.parse(effectiveEndDate);
        LocalDate effectiveStartLocalDate = (effectiveStartDate != null && effectiveStartDate.equals("null"))
                ? null : LocalDate.parse(effectiveStartDate);

        String randOperatorId = UUID.randomUUID().toString();
        String plantId = "generated-plant-id";

        // Arrange
        String expectedEffectiveStartDate = effectiveStartDate;
        String expectedEffectiveEndDate = effectiveEndDate;

        // If effectiveStartDate is empty, it should default to the current date in UTC
        if (effectiveStartLocalDate == null) {
            expectedEffectiveStartDate = LocalDateTime.now(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern(Const.DATE_FORMAT));
        }

        // If effectiveEndDate is empty, it should default to the configured defaultEffectiveEndDate
        if (effectiveEndLocalDate == null) {
            expectedEffectiveEndDate = LocalDateTime.now(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern(Const.DATE_FORMAT));
            when(odsProperties.getDefaultEffectiveEndDate()).thenReturn(DateUtils.formatDate(LocalDate.now(ZoneOffset.UTC), Const.DATE_FORMAT));
        }

        // Mock operatorRepository to simulate persistence
        OperatorEntity operatorEntity = new OperatorEntity();
        operatorEntity.setOperatorId(randOperatorId);
        doReturn(Optional.empty()).when(plantRepository).findById(anyString());
        doReturn(Optional.of(operatorEntity)).when(operatorRepository).findById(anyString());
        PlantEntity savedEntity = new PlantEntity(
                plantId, randOperatorId, plantName, plantAddress, openPlantId, globalPlantId,
                LocalDate.parse(expectedEffectiveStartDate), LocalDate.parse(expectedEffectiveEndDate), creatorOperatorId);
        doReturn(savedEntity).when(plantRepository).save(any(PlantEntity.class));

        // Act
        PlantResult result = plantService.addPlant(
                randOperatorId, plantName, plantAddress,
                openPlantId, globalPlantId, effectiveStartLocalDate,
                effectiveEndLocalDate, creatorOperatorId);

        // Assert
        assertNotNull(result);
        assertEquals(randOperatorId, result.getOperatorId());
        assertEquals(plantId, result.getPlantId());
        assertEquals(plantName, result.getPlantName());
        assertEquals(plantAddress, result.getPlantAddress());
        assertEquals(openPlantId, result.getOpenPlantId());
        assertEquals(globalPlantId, result.getGlobalPlantId());
        assertEquals(false, result.isDeletedFlag());
        assertEquals(creatorOperatorId, result.getCreatedUserId());
        assertEquals(creatorOperatorId, result.getUpdatedUserId());
        assertEquals(expectedEffectiveStartDate, result.getEffectiveStartDate().toString());
        assertEquals(expectedEffectiveEndDate, result.getEffectiveEndDate().toString());
        verify(operatorRepository).findById(randOperatorId);
        verify(plantRepository).findById(anyString());
        verify(plantRepository).save(any(PlantEntity.class));
    }

    /**
     * Test addPlant method for invalid UUID.
     */
    @ParameterizedTest
    @CsvSource({
        "NULL, Operator Name 1, Address 1, open-id-1, global-id-1, 2025-01-01, 2025-12-31, creator-id-1",
        "'', Operator Name 1, Address 1, open-id-1, global-id-1, 2025-01-01, 2025-12-31, creator-id-1",
        "invalid-uuid, Operator Name 2, Address 2, open-id-2, global-id-2, 2025-01-01, 2025-12-31, creator-id-2"
    })
    @DisplayName("addPlant - Invalid UUID")
    void addPlant_invalidUUID(
            String operatorId,
            String plantName,
            String plantAddress,
            String openPlantId,
            String globalPlantId,
            String effectiveStartDate,
            String effectiveEndDate,
            String creatorOperatorId) {

        final String invalidOperatorId = operatorId.equals("NULL") ? null : operatorId;

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            plantService.addPlant(
                    invalidOperatorId, plantName, plantAddress,
                    openPlantId, globalPlantId, LocalDate.parse(effectiveStartDate),
                    LocalDate.parse(effectiveEndDate), creatorOperatorId);
        });

        assertTrue(exception.getResponseMessage().contains("invalid UUID"));
        assertTrue(exception.getLogMessage().contains("invalid UUID"));
    }

    /**
     * Test addPlant method for invalid effective dates.
     */
    @ParameterizedTest
    @CsvSource({
        "RAND, Operator Name 1, Address 1, open-id-1, global-id-1, 2025-12-31, 2025-01-01, creator-id-1",
        "RAND, Operator Name 1, Address 1, open-id-1, global-id-1, 2026-01-01, 2025-12-31, creator-id-1",
        "RAND, Operator Name 1, Address 1, open-id-1, global-id-1, 2026-02-01, 2026-01-31, creator-id-1"
    })
    @DisplayName("addPlant - Invalid Effective Dates")
    void addPlant_invalidEffectiveDates(
            String operatorId,
            String plantName,
            String plantAddress,
            String openPlantId,
            String globalPlantId,
            String effectiveStartDate,
            String effectiveEndDate,
            String creatorOperatorId) {
        final String randOperatorId = UUID.randomUUID().toString();

        // Act & Assert
        BadParametersException exception = assertThrows(BadParametersException.class, () -> {
            plantService.addPlant(
                    randOperatorId, plantName, plantAddress,
                    openPlantId, globalPlantId, LocalDate.parse(effectiveStartDate),
                    LocalDate.parse(effectiveEndDate), creatorOperatorId);
        });

        assertEquals(ConstError.ERR_EFFECTIVE_DATE_START_AFTER_END, exception.getResponseMessage());
        assertEquals(ConstError.ERR_EFFECTIVE_DATE_START_AFTER_END, exception.getLogMessage());
    }

    /**
     * Test addPlant method when plantId already exists.
     */
    @Test
    @DisplayName("addPlant - plantId is already exist")
    void addPlant_plantId_exists() {
        String operatorId = UUID.randomUUID().toString();
        String plantName = "Plant Name 1";
        String plantAddress = "Address 1";
        String openPlantId = "open-id-123456";
        String globalPlantId = "global-id-1";
        String effectiveStartDate = "2025-01-01";
        String effectiveEndDate = "2025-12-31";
        String creatorOperatorId = "creator-id-1";

        String plantId = "generated-plant-id";

        // Arrange
        PlantEntity savedEntity = new PlantEntity();
        savedEntity.setPlantId(plantId);
        doReturn(Optional.of(savedEntity)).when(plantRepository).findById(anyString());

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            plantService.addPlant(
                    operatorId, plantName, plantAddress,
                    openPlantId, globalPlantId, LocalDate.parse(effectiveStartDate),
                    LocalDate.parse(effectiveEndDate), creatorOperatorId);
        });

        // Assert
        assertTrue(exception.getResponseMessage().contains("plant_id: Failed to generate UUID"));
        assertTrue(exception.getLogMessage().contains("Unexpected error occurred."));
        assertTrue(exception.getLogMessage().contains("plant_id: Failed to generate UUID"));
        verify(operatorRepository, never()).findById(operatorId);
        verify(plantRepository).findById(anyString());
        verify(plantRepository, never()).save(any(PlantEntity.class));
    }

    /**
     * Test addPlant method when operatorId does not exist.
     */
    @Test
    @DisplayName("addPlant - operatorId is not exist")
    void addPlant_operatorId_notExists() {
        String operatorId = UUID.randomUUID().toString();
        String plantName = "Plant Name 1";
        String plantAddress = "Address 1";
        String openPlantId = "open-id-123456";
        String globalPlantId = "global-id-1";
        String effectiveStartDate = "2025-01-01";
        String effectiveEndDate = "2025-12-31";
        String creatorOperatorId = "creator-id-1";

        // Arrange
        doReturn(Optional.empty()).when(plantRepository).findById(anyString());
        doReturn(Optional.empty()).when(operatorRepository).findById(anyString());

        // Act & Assert
        BadParametersException exception = assertThrows(BadParametersException.class, () -> {
            plantService.addPlant(
                    operatorId, plantName, plantAddress,
                    openPlantId, globalPlantId, LocalDate.parse(effectiveStartDate),
                    LocalDate.parse(effectiveEndDate), creatorOperatorId);
        });

        // Assert
        assertTrue(exception.getMessage().contains("Invalid request parameters"));
        assertTrue(exception.getMessage().contains(operatorId));
        verify(operatorRepository).findById(operatorId);
        verify(plantRepository).findById(anyString());
        verify(plantRepository, never()).save(any(PlantEntity.class));
    }
}
