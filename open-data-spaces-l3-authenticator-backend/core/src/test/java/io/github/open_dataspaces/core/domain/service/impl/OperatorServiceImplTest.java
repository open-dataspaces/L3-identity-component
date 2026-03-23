/*
 * OperatorServiceImplTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for OperatorServiceImpl.
 *
 * Date: 2025/08/30
 */

package io.github.open_dataspaces.core.domain.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import io.github.open_dataspaces.core.common.config.ODSProperties;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.utils.DateUtils;
import io.github.open_dataspaces.core.domain.dto.OperatorResult;
import io.github.open_dataspaces.core.domain.dto.StateString;
import io.github.open_dataspaces.core.domain.entities.OperatorEntity;
import io.github.open_dataspaces.core.domain.repository.interfaces.OperatorRepository;
import io.github.open_dataspaces.core.domain.service.interfaces.AuthorizationService;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.ConflictException;
import io.github.open_dataspaces.core.exception.IllegalAuthDataException;
import io.github.open_dataspaces.core.exception.UnexpectedException;

import jakarta.persistence.EntityExistsException;
import java.util.UUID;
import io.github.open_dataspaces.core.domain.dto.ListOperatorRequest;

/**
 * Unit tests for the OperatorServiceImpl class.
 */
@ExtendWith(MockitoExtension.class)
class OperatorServiceImplTest {

    @Mock
    private OperatorRepository operatorRepository;
    @InjectMocks
    private OperatorServiceImpl operatorService;
    @Captor
    private ArgumentCaptor<OperatorEntity> operatorEntityCaptor;

    @Mock
    private ODSProperties odsProperties;

    @Mock
    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() throws Exception {
        operatorService = new OperatorServiceImpl(odsProperties, operatorRepository, authorizationService);
    }

    // Common input parameters
    private final String commonOperatorId = UUID.randomUUID().toString();
    private final LocalDate commonEffectiveDate = LocalDate.now();
    private final int commonLimit = 0;
    private final int commonOffset = 0;
    private final String commonKey = Const.JSON_PROPERTY_OPERATOR_ID;
    private final String commonOrder = "asc";
    private final Boolean commonDeletedFlag = false;
    private final String commonOperatorName = "operatorName";
    private final String commonOperatorAddress = "operatorAddress";
    private final String commonOpenOperatorId = "openOperatorId";
    private final String commonGlobalOperatorId = "globalOperatorId";
    private final ListOperatorRequest.SortKey commonSortKey = new ListOperatorRequest.SortKey(commonKey, commonOrder);

    /**
     * Test updateOperator method for success cases.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // Valid case with all fields changed
        "apiKey123, operator123, 2025-08-30T10:15:30.000Z, Updated Operator, Updated Address, open123, global123, updater123",
        // Valid case with some fields unchanged
        "apiKey123, operator123, 2025-08-30T10:15:30.000Z, NULL, Updated Address, open123, global123, updater123",
        "apiKey123, operator123, 2025-08-30T10:15:30.000Z, Updated Operator, NULL, open123, global123, updater123",
        "apiKey123, operator123, 2025-08-30T10:15:30.000Z, Updated Operator, Updated Address, NULL, global123, updater123",
        "apiKey123, operator123, 2025-08-30T10:15:30.000Z, Updated Operator, Updated Address, open123, empty, updater123",
        "apiKey123, operator123, 2025-08-30T10:15:30.000Z, Updated Operator, Updated Address, open123, null, updater123",
        // Valid case with empty strings
    }, nullValues = "NULL")
    @DisplayName("updateOperator - Success Cases")
    void updateOperator_success(
            String apiKey,
            String targetOperatorId,
            String updatedAt,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId,
            String updateOperatorId) {

        StateString stateGlobalOperatorId;
        switch (globalOperatorId) {
            case "null":
                stateGlobalOperatorId = StateString.of(null);
                break;
            case "empty":
                stateGlobalOperatorId = StateString.unset();
                break;
            default:
                stateGlobalOperatorId = StateString.of(globalOperatorId);
                break;
        }

        // Arrange
        LocalDateTime updatedDateTime = LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_DATE_TIME);
        OperatorEntity existingEntity = new OperatorEntity(
                "OldOperatorId", "Old Operator", "Old Address", "oldOpenId", "oldGlobalId",
                LocalDate.now(), LocalDate.now().plusYears(1), "creator123");
        existingEntity.setUpdatedAt(updatedDateTime);

        when(operatorRepository.findById(targetOperatorId)).thenReturn(Optional.of(existingEntity));
        when(operatorRepository.saveAndFlush(any(OperatorEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OperatorResult result = operatorService.updateOperator(
                targetOperatorId, updatedDateTime,
                operatorName, operatorAddress, openOperatorId, stateGlobalOperatorId,
                updateOperatorId);

        // Assert
        assertNotNull(result);
        assertEquals(existingEntity.getOperatorId(), result.getOperatorId());
        if (operatorName == null) {
            result.getOperatorName().equals("Old Operator");
        } else {
            result.getOperatorName().equals(operatorName);
        }
        if (operatorAddress == null) {
            result.getOperatorAddress().equals("Old Address");
        } else {
            result.getOperatorAddress().equals(operatorAddress);
        }
        if (openOperatorId == null) {
            result.getOpenOperatorId().equals("oldOpenId");
        } else {
            result.getOpenOperatorId().equals(openOperatorId);
        }
        switch (globalOperatorId) {
            case "null":
                assertNull(result.getGlobalOperatorId());
                break;
            case "empty":
                assertEquals("oldGlobalId", result.getGlobalOperatorId());
                break;
            default:
                assertEquals(globalOperatorId, result.getGlobalOperatorId());
                break;
        }
        assertEquals(updateOperatorId, result.getUpdatedUserId());
        verify(operatorRepository).findById(targetOperatorId);
        if (!StringUtils.hasText(targetOperatorId)) {
            verify(operatorRepository).saveAndFlush(any(OperatorEntity.class));
        }
    }

    /**
     * Test updateOperator method when the target operator is not found.
     */
    @ParameterizedTest
    @CsvSource({
        "apiKey123, operator123, 2025-08-30T10:15:30.000Z, Updated Operator, Updated Address, open123, global123, updater123"
    })
    @DisplayName("updateOperator - Operator Not Found")
    void updateOperator_operatorNotFound(
            String apiKey,
            String targetOperatorId,
            String updatedAt,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId,
            String updateOperatorId) {

        // Arrange
        LocalDateTime updatedDateTime = LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_DATE_TIME);
        when(operatorRepository.findById(targetOperatorId)).thenReturn(Optional.empty());

        // Act
        OperatorResult result = operatorService.updateOperator(
                targetOperatorId, updatedDateTime,
                operatorName, operatorAddress, openOperatorId, StateString.of(globalOperatorId),
                updateOperatorId);

        // Assert
        assertEquals(null, result);
        verify(operatorRepository).findById(targetOperatorId);
    }

    /**
     * Test updateOperator method when no changes are made.
     */
    @ParameterizedTest
    @CsvSource({
        "apiKey123, operator123, 2025-08-30T10:15:30.000Z, Old Operator, Old Address, oldOpenId, oldGlobalId, updater123"
    })
    @DisplayName("updateOperator - No Changes Made")
    void updateOperator_noChangesMade(
            String apiKey,
            String targetOperatorId,
            String updatedAt,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId,
            String updateOperatorId) {

        // Arrange
        LocalDateTime updatedDateTime = LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_DATE_TIME);
        OperatorEntity existingEntity = new OperatorEntity(
                targetOperatorId, operatorName, operatorAddress, openOperatorId, globalOperatorId,
                LocalDate.now(), LocalDate.now().plusYears(1), "creator123");
        existingEntity.setUpdatedAt(updatedDateTime);

        when(operatorRepository.findById(targetOperatorId)).thenReturn(Optional.of(existingEntity));

        // Act
        OperatorResult result = operatorService.updateOperator(
                targetOperatorId, updatedDateTime,
                operatorName, operatorAddress, openOperatorId, StateString.of(globalOperatorId),
                updateOperatorId);

        // Assert
        assertNotNull(result);
        assertEquals(targetOperatorId, result.getOperatorId());
        assertEquals(operatorName, result.getOperatorName());
        assertEquals(operatorAddress, result.getOperatorAddress());
        assertEquals(openOperatorId, result.getOpenOperatorId());
        assertEquals(globalOperatorId, result.getGlobalOperatorId());
        assertEquals(updatedAt, result.getUpdatedAt().format(DateTimeFormatter.ofPattern(Const.ISO_8601_UTC_MILLISECOND_FORMAT)));
        assertEquals("creator123", result.getUpdatedUserId());
        verify(operatorRepository).findById(targetOperatorId);
        verify(operatorRepository, never()).saveAndFlush(any(OperatorEntity.class));
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
        String operatorName = "Updated Operator";
        String operatorAddress = "Updated Address";
        String openOperatorId = "open123";
        String globalOperatorId = "global123";
        String updateOperatorId = "updater123";

        OperatorEntity existingEntity = new OperatorEntity();
        existingEntity.setDeletedFlag(deletedFlag);
        existingEntity.setEffectiveStartDate(effectiveStartDate);
        existingEntity.setEffectiveEndDate(effectiveEndDate);

        // Arrange
        when(operatorRepository.findById(targetOperatorId)).thenReturn(Optional.of(existingEntity));

        // Act & Assert
        IllegalAuthDataException exception = assertThrows(IllegalAuthDataException.class, () -> {
            operatorService.updateOperator(
                    targetOperatorId, updatedAt,
                    operatorName, operatorAddress, openOperatorId, StateString.of(globalOperatorId),
                    updateOperatorId);
        });

        assertTrue(exception.getLogMessage().contains(String.format(ConstError.ERRLOG_403_FORBIDDEN_RESOURCE_DISABLED, targetOperatorId)));
        verify(operatorRepository).findById(targetOperatorId);
    }

    /**
     * Test updateOperator method for data update conflict.
     */
    @ParameterizedTest
    @CsvSource({
        "apiKey123, operator123, 2025-08-30T10:15:30.000Z, Updated Operator, Updated Address, open123, global123, updater123"
    })
    @DisplayName("updateOperator - Data Update Conflict")
    void updateOperator_dataUpdateConflict(
            String apiKey,
            String targetOperatorId,
            String updatedAt,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId,
            String updateOperatorId) {

        // Arrange
        LocalDateTime updatedDateTime = LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_DATE_TIME);
        OperatorEntity existingEntity = new OperatorEntity(
                targetOperatorId, "Old Operator", "Old Address", "oldOpenId", "oldGlobalId",
                LocalDate.now(), LocalDate.now().plusYears(1), "creator123");
        existingEntity.setUpdatedAt(updatedDateTime.minusDays(1)); // Simulate a conflict

        when(operatorRepository.findById(targetOperatorId)).thenReturn(Optional.of(existingEntity));

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            operatorService.updateOperator(
                    targetOperatorId, updatedDateTime,
                    operatorName, operatorAddress, openOperatorId, StateString.of(globalOperatorId),
                    updateOperatorId);
        });

        assertTrue(exception.getLogMessage().contains(ConstError.ERRLOG_409_CONFLICT));
        verify(operatorRepository).findById(targetOperatorId);
    }

    /**
     * Test addOperator method for success cases.
     */
    @ParameterizedTest
    @CsvSource({
        // Normal cases with all fields provided
        "RAND, Operator Name 1, Address 1, open-id-1, global-id-1, 2025-01-01, 9999-12-31, creator-id-1",
        // Case where effectiveEndDate is empty
        "RAND, Operator Name 2, Address 2, open-id-2, global-id-2, 2025-01-01, null, creator-id-2",
        // Case where effectiveStartDate is empty
        "RAND, Operator Name 2, Address 2, open-id-2, global-id-2, null, 9999-12-31, creator-id-2",
        // Case where both effectiveStartDate and effectiveEndDate are empty
        "RAND, Operator Name 3, Address 3, open-id-3, global-id-3, null, null, creator-id-3"
    })
    @DisplayName("addOperator - Success")
    void addOperator_success(
            String operatorId,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId,
            String effectiveStartDate,
            String effectiveEndDate,
            String creatorOperatorId) {

        LocalDate effectiveEndLocalDate = (effectiveEndDate != null && effectiveEndDate.equals("null"))
                ? null : LocalDate.parse(effectiveEndDate);
        LocalDate effectiveStartLocalDate = (effectiveStartDate != null && effectiveStartDate.equals("null"))
                ? null : LocalDate.parse(effectiveStartDate);

        String randOperatorId = "RAND".equals(operatorId) ? UUID.randomUUID().toString() : operatorId;

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
        doReturn(Optional.empty()).when(operatorRepository).findById(randOperatorId);
        OperatorEntity savedEntity = new OperatorEntity(
                randOperatorId, operatorName, operatorAddress, openOperatorId, globalOperatorId,
                LocalDate.parse(expectedEffectiveStartDate), LocalDate.parse(expectedEffectiveEndDate), creatorOperatorId);
        doReturn(savedEntity).when(operatorRepository).save(any(OperatorEntity.class));

        // Act
        OperatorResult result = operatorService.addOperator(
                randOperatorId, operatorName, operatorAddress,
                openOperatorId, globalOperatorId, effectiveStartLocalDate,
                effectiveEndLocalDate, creatorOperatorId);

        // Assert
        assertNotNull(result);
        assertEquals(randOperatorId, result.getOperatorId());
        assertEquals(operatorName, result.getOperatorName());
        assertEquals(operatorAddress, result.getOperatorAddress());
        assertEquals(openOperatorId, result.getOpenOperatorId());
        assertEquals(globalOperatorId, result.getGlobalOperatorId());
        assertEquals(expectedEffectiveStartDate, result.getEffectiveStartDate().toString());
        assertEquals(expectedEffectiveEndDate, result.getEffectiveEndDate().toString());
        verify(operatorRepository).findById(randOperatorId);
        verify(operatorRepository).save(any(OperatorEntity.class));
    }

    /**
     * Test addOperator method for invalid UUID.
     */
    @ParameterizedTest
    @CsvSource({
        "NULL, Operator Name 1, Address 1, open-id-1, global-id-1, 2025-01-01, 2025-12-31, creator-id-1",
        "'', Operator Name 1, Address 1, open-id-1, global-id-1, 2025-01-01, 2025-12-31, creator-id-1",
        "invalid-uuid, Operator Name 2, Address 2, open-id-2, global-id-2, 2025-01-01, 2025-12-31, creator-id-2"
    })
    @DisplayName("addOperator - Invalid UUID")
    void addOperator_invalidUUID(
            String operatorId,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId,
            String effectiveStartDate,
            String effectiveEndDate,
            String creatorOperatorId) {

        final String invalidOperatorId = operatorId.equals("null") ? null : operatorId;

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            operatorService.addOperator(
                    invalidOperatorId, operatorName, operatorAddress,
                    openOperatorId, globalOperatorId, LocalDate.parse(effectiveStartDate),
                    LocalDate.parse(effectiveEndDate), creatorOperatorId);
        });

        assertTrue(exception.getResponseMessage().contains("invalid UUID"));
        assertTrue(exception.getLogMessage().contains("invalid UUID"));
    }

    /**
     * Test addOperator method for invalid effective dates.
     */
    @ParameterizedTest
    @CsvSource({
        "RAND, Operator Name 1, Address 1, open-id-1, global-id-1, 2025-12-31, 2025-01-01, creator-id-1",
        "RAND, Operator Name 1, Address 1, open-id-1, global-id-1, 2026-01-01, 2025-12-31, creator-id-1",
        "RAND, Operator Name 1, Address 1, open-id-1, global-id-1, 2026-02-01, 2026-01-31, creator-id-1"
    })
    @DisplayName("addOperator - Invalid Effective Dates")
    void addOperator_invalidEffectiveDates(
            String operatorId,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId,
            String effectiveStartDate,
            String effectiveEndDate,
            String creatorOperatorId) {
        final String randOperatorId = operatorId.equals("RAND") ? UUID.randomUUID().toString() : operatorId;

        // Act & Assert
        BadParametersException exception = assertThrows(BadParametersException.class, () -> {
            operatorService.addOperator(
                    randOperatorId, operatorName, operatorAddress,
                    openOperatorId, globalOperatorId, LocalDate.parse(effectiveStartDate),
                    LocalDate.parse(effectiveEndDate), creatorOperatorId);
        });

        assertEquals(ConstError.ERR_EFFECTIVE_DATE_START_AFTER_END, exception.getResponseMessage());
        assertEquals(ConstError.ERR_EFFECTIVE_DATE_START_AFTER_END, exception.getLogMessage());
    }

    /**
     * Test addOperator method for duplicate keys.
     */
    @ParameterizedTest
    @CsvSource({
        "RAND, Operator Name 1, Address 1, open-id-1, global-id-1, 2025-01-01, 2025-12-31, creator-id-1",
    })
    @DisplayName("addOperator - Duplicate by id")
    void addOperator_duplicateById(
            String operatorId,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId,
            String effectiveStartDate,
            String effectiveEndDate,
            String creatorOperatorId) {
        final String randOperatorId = operatorId.equals("RAND") ? UUID.randomUUID().toString() : operatorId;

        // Arrange
        OperatorEntity conflictOperatorEntity = new OperatorEntity();
        conflictOperatorEntity.setOperatorId(randOperatorId);
        doReturn(Optional.of(conflictOperatorEntity)).when(operatorRepository).findById(randOperatorId);

        // Act & Assert
        ConflictException conflictException = assertThrows(ConflictException.class, () -> {
            operatorService.addOperator(
                    randOperatorId, operatorName, operatorAddress,
                    openOperatorId, globalOperatorId, DateUtils.parseDate(effectiveStartDate, Const.DATE_FORMAT),
                    DateUtils.parseDate(effectiveEndDate, Const.DATE_FORMAT), creatorOperatorId);
        });

        assertTrue(conflictException.getLogMessage().contains(randOperatorId));
    }

    /**
     * Test addOperator method for duplicate key on other fields.
     */
    @ParameterizedTest
    @CsvSource({
        "RAND, Operator Name 2, Address 2, open-id-2, global-id-2, 2025-01-01, 2025-12-31, creator-id-2, uk_tbl_invalid_id"
    })
    @DisplayName("addOperator - Duplicate Keys")
    void addOperator_duplicateKeyOthers(
            String operatorId,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId,
            String effectiveStartDate,
            String effectiveEndDate,
            String creatorOperatorId,
            String duplicateKey) {
        final String randOperatorId = operatorId.equals("RAND") ? UUID.randomUUID().toString() : operatorId;

        // Arrange
        ConstraintViolationException exception = new ConstraintViolationException("Unexpected error occurred: " + duplicateKey, null, duplicateKey);
        doThrow(exception).when(operatorRepository).save(any(OperatorEntity.class));

        // Act & Assert
        UnexpectedException unexpectedException = assertThrows(UnexpectedException.class, () -> {
            operatorService.addOperator(
                    randOperatorId, operatorName, operatorAddress,
                    openOperatorId, globalOperatorId, LocalDate.parse(effectiveStartDate),
                    LocalDate.parse(effectiveEndDate), creatorOperatorId);
        });

        assertTrue(unexpectedException.getLogMessage().contains("Unexpected error occurred"));
        assertTrue(unexpectedException.getResponseMessage().contains("Unexpected error occurred"));
    }

    /**
     * Test addOperator method for handling EntityExistsException and IllegalArgumentException.
     */
    @ParameterizedTest
    @CsvSource({
        "EntityExistsException, Entity already exists",
        "IllegalArgumentException, Invalid argument provided"
    })
    @DisplayName("addOperator - Handle EntityExistsException and IllegalArgumentException")
    void addOperator_handleEntityExistsAndIllegalArgument(
            String exceptionType,
            String exceptionMessage) {

        if ("EntityExistsException".equals(exceptionType)) {
            doThrow(new EntityExistsException(exceptionMessage)).when(operatorRepository).save(any(OperatorEntity.class));
        } else if ("IllegalArgumentException".equals(exceptionType)) {
            doThrow(new IllegalArgumentException(exceptionMessage)).when(operatorRepository).save(any(OperatorEntity.class));
        }

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            operatorService.addOperator(
                    UUID.randomUUID().toString(),
                    "Operator Name",
                    "Operator Address",
                    "open-id",
                    "global-id",
                    LocalDate.parse("2025-01-01"),
                    LocalDate.parse("2025-12-31"),
                    "creator-id"
            );
        });

        // Assert
        assertTrue(exception.getMessage().contains(exceptionMessage));
        assertTrue(exception.getLogMessage().contains("Unexpected error occurred"));
    }

    /**
     * case#1:
     * Test for searchOperators.
     */
    @Test
    void searchOperators_success() {
        List<OperatorEntity> entities = Arrays.asList(mock(OperatorEntity.class), mock(OperatorEntity.class));
        when(operatorRepository.findWithLimitOffset(any(), anyInt(), anyInt(), any())).thenReturn(entities);

        List<OperatorResult> results = operatorService.searchOperators(
                commonLimit, commonOffset, commonKey, commonOrder,
                commonOperatorId, commonOperatorName, commonOperatorAddress, commonOpenOperatorId,
                StateString.of(commonGlobalOperatorId), commonDeletedFlag, commonEffectiveDate
        );
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    /**
     * Parameterized test for searchOperators.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "VALUE, VALUE, operator_id, asc, VALUE, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY", // case#2: Filter by operatorId
        "VALUE, VALUE, operator_name, desc, EMPTY, VALUE, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY", // case#3: Filter by operatorName
        "VALUE, VALUE, operator_address, asc, EMPTY, EMPTY, VALUE, EMPTY, EMPTY, EMPTY, EMPTY", // case#4: Filter by operatorAddress
        "VALUE, VALUE, open_operator_id, desc, EMPTY, EMPTY, EMPTY, VALUE, EMPTY, EMPTY, EMPTY", // case#5: Filter by openOperatorId
        "VALUE, VALUE, global_operator_id, asc, EMPTY, EMPTY, EMPTY, EMPTY, VALUE, EMPTY, EMPTY", // case#6: Filter by globalOperatorId
        "VALUE, VALUE, deleted_flag, asc, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, VALUE, EMPTY", // case#7: Filter by deletedFlag
        "VALUE, VALUE, effective_start_date, desc, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, VALUE", // case#8: Filter by effectiveStartDate
        "VALUE, VALUE, effective_end_date, desc, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, VALUE", // case#9: Filter by effectiveEndDate
        "VALUE, VALUE, created_at, desc, VALUE, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY", // case#10: Filter by created_at
        "VALUE, VALUE, updated_at , desc, VALUE, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY", // case#11: Filter by updated_at
    }, nullValues = "NULL")
    void searchOperators_variousSpecs(
            String argLimit,
            String argOffset,
            String argOrderByKey,
            String argOrderByDirection,
            String argOperatorId,
            String argOperatorName,
            String argOperatorAddress,
            String argOpenOperatorId,
            String argGlobalOperatorId,
            String argDeletedFlag,
            String argEffectiveDate
    ) {
        Integer count = "VALUE".equals(argLimit) ? commonLimit :
                Integer.valueOf(argLimit);
        Integer index = "VALUE".equals(argOffset) ? commonOffset :
                Integer.valueOf(argOffset);
        ListOperatorRequest.SortKey sort = "VALUE".equals(argOrderByKey) ? commonSortKey :
        new ListOperatorRequest.SortKey(argOrderByKey, argOrderByDirection);
        String operatorId =
                "UUID".equals(argOperatorId) ? commonOperatorId :
                "EMPTY".equals(argOperatorId) ? null :
                argOperatorId;
        String operatorName =
                "EMPTY".equals(argOperatorName) ? null :
                "VALUE".equals(argOperatorName) ? commonOperatorName :
                argOperatorName;
        String operatorAddress =
                "EMPTY".equals(argOperatorAddress) ? null :
                "VALUE".equals(argOperatorAddress) ? commonOperatorAddress :
                argOperatorAddress;
        String openOperatorId =
                "EMPTY".equals(argOpenOperatorId) ? null :
                "VALUE".equals(argOpenOperatorId) ? commonOpenOperatorId :
                argOpenOperatorId;
        StateString globalOperatorId =
                "EMPTY".equals(argGlobalOperatorId) ? StateString.unset() :
                "VALUE".equals(argGlobalOperatorId) ? StateString.of(commonGlobalOperatorId) :
                StateString.of(argGlobalOperatorId);
        Boolean deletedFlag =
                "EMPTY".equals(argDeletedFlag) ? null :
                "VALUE".equals(argDeletedFlag) ? commonDeletedFlag :
                Boolean.valueOf(argDeletedFlag);
        LocalDate effectiveDate =
                "EMPTY".equals(argEffectiveDate) ? null :
                "VALUE".equals(argEffectiveDate) ? commonEffectiveDate :
                LocalDate.parse(argEffectiveDate);
        List<OperatorEntity> entities = Arrays.asList(mock(OperatorEntity.class), mock(OperatorEntity.class));

        // Mock to return data that matches the search conditions.
        when(operatorRepository.findWithLimitOffset(any(), anyInt(), anyInt(), any())).thenReturn(entities);

        // Act
        List<OperatorResult> results = operatorService.searchOperators(
                count, index, sort.getKey(), sort.getOrder(),
                operatorId, operatorName, operatorAddress, openOperatorId, globalOperatorId, deletedFlag, effectiveDate
        );
        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(operatorRepository).findWithLimitOffset(any(), anyInt(), anyInt(), any());
    }

    /**
     * case#9:
     * Test for searchOperators when no results found.
     */
    @Test
    void searchOperators_returnsEmptyList() {
        when(operatorRepository.findWithLimitOffset(any(), anyInt(), anyInt(), any(Sort.class)))
                .thenReturn(Collections.emptyList());

        List<OperatorResult> results = operatorService.searchOperators(
                commonLimit, commonOffset, commonKey, commonOrder,
                commonOperatorId, commonOperatorName, commonOperatorAddress, commonOpenOperatorId,
                StateString.of(commonGlobalOperatorId), commonDeletedFlag, commonEffectiveDate
        );
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /**
     * case#16:
     * getOperator_Success tests the successful retrieval of an operator.
     */
    @Test
    void getOperator_success() {

        String operatorId = "d9a38406-cae2-4679-b052-15a75f5531e6";
        String operatorName = "テスト事業者";
        String operatorAddress = "テスト県テスト市テストビル1F";
        String openOperatorId = "openId123";
        String globalOperatorId = "globalId123";
        LocalDate effectiveStartDate = LocalDate.now();
        LocalDate effectiveEndDate = LocalDate.now().plusYears(1);
        LocalDateTime createAt = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime updateAt = LocalDateTime.now(ZoneOffset.UTC).plusHours(1);
        String createdUserId = "creatorOperatorId";
        String updatedUserId = "updaterOperatorId";

        OperatorEntity testEntity = new OperatorEntity(
                operatorId,
                operatorName,
                operatorAddress,
                openOperatorId,
                globalOperatorId,
                effectiveStartDate,
                effectiveEndDate,
                createdUserId
        );

        testEntity.setCreatedAt(createAt);
        testEntity.setUpdatedAt(updateAt);
        testEntity.setUpdatedUserId(updatedUserId);

        Optional<OperatorEntity> optionalOperatorEntity = Optional.of(testEntity);

        // Arrange
        when(operatorRepository.findById(
            operatorId
        )).thenReturn(optionalOperatorEntity);

        // Act
        OperatorResult responseEntity = operatorService.getOperator(operatorId);

        // Assert
        assertEquals(operatorId, responseEntity.getOperatorId());
        assertEquals(operatorName, responseEntity.getOperatorName());
        assertEquals(operatorAddress, responseEntity.getOperatorAddress());
        assertEquals(openOperatorId, responseEntity.getOpenOperatorId());
        assertEquals(globalOperatorId, responseEntity.getGlobalOperatorId());
        assertEquals(effectiveStartDate, responseEntity.getEffectiveStartDate());
        assertEquals(effectiveEndDate, responseEntity.getEffectiveEndDate());
        assertEquals(createAt, responseEntity.getCreatedAt());
        assertEquals(createdUserId, responseEntity.getCreatedUserId());
        assertEquals(updateAt, responseEntity.getUpdatedAt());
        assertEquals(updatedUserId, responseEntity.getUpdatedUserId());
    }

    /**
     * case#17:
     * getOperator_returnEmpty tests the successful retrieval of an operator when no operator is found.
     */
    @Test
    void getOperator_operatorNotFound() {
        // Arrange
        when(operatorRepository.findById(
            anyString()
        )).thenReturn(Optional.empty());

        // Act
        OperatorResult responseEntity = operatorService.getOperator(anyString());

        // Assert
        assertNull(responseEntity);
    }

    /**
     * case#18:
     * getOperator_DbException tests the handling of a database exception during operator retrieval.
     */
    @Test
    void getOperator_exception() {
        String operatorId = "d9a38406-cae2-4679-b052-15a75f5531e6";
        when(operatorRepository.findById(operatorId))
                .thenThrow(new RuntimeException("DB connection error"));

        assertThrows(RuntimeException.class, () -> {
            operatorService.getOperator(operatorId);
        });
    }

    /**
     * Test updateOperator method for success cases.
     */
    @ParameterizedTest
    @CsvSource({
        // Valid case with all fields changed
        "apiKey123, operator123, 2025-08-30T10:15:30.000Z, 2020-08-30, 2030-08-30, false",
        // Valid case with some fields unchanged
        "apiKey123, operator456, 2025-09-01T12:00:00.000Z, null, 2030-08-30, false",
        "apiKey123, operator456, 2025-09-01T12:00:00.000Z, 2020-08-30, null, false",
        "apiKey123, operator123, 2025-08-30T10:15:30.000Z, 2020-08-30, 2030-08-30, null",
        // Valid case with empty strings
        "apiKey123, '', 2025-08-30T10:15:30.000Z, 2020-08-30, 2030-08-30, false",
    })
    @DisplayName("updateOperatorStatus - Success Cases")
    void updateOperatorStatus_success(
            String apiKey,
            String targetOperatorId,
            String updatedAt,
            String strEffectiveStartDate,
            String strEffectiveEndDate,
            String strDeletedFlag) {

        // Handle "null" string to represent actual null values
        targetOperatorId = "null".equals(targetOperatorId) ? null : targetOperatorId;
        LocalDate effectiveStartDate = "null".equals(strEffectiveStartDate) ? null : DateUtils.parseDate(strEffectiveStartDate, Const.DATE_FORMAT);
        LocalDate effectiveEndDate = "null".equals(strEffectiveEndDate) ? null : DateUtils.parseDate(strEffectiveEndDate, Const.DATE_FORMAT);
        Boolean deletedFlag = "null".equals(strDeletedFlag) ? null : Boolean.valueOf(strDeletedFlag);

        LocalDate orgEffectiveDate = LocalDate.now();
        Boolean orgDeletedFlag = (deletedFlag == null) ? false : !deletedFlag;
        String updateOperatorId = "updateOperatorId-123";

        // Arrange
        LocalDateTime updatedDateTime = LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_DATE_TIME);
        OperatorEntity existingEntity = new OperatorEntity();
        existingEntity.setOperatorId(targetOperatorId);
        existingEntity.setUpdatedAt(updatedDateTime);
        existingEntity.setUpdatedUserId(updateOperatorId);

        existingEntity.setEffectiveStartDate(orgEffectiveDate);
        existingEntity.setEffectiveEndDate(orgEffectiveDate);
        existingEntity.setDeletedFlag(orgDeletedFlag);

        when(operatorRepository.findById(targetOperatorId)).thenReturn(Optional.of(existingEntity));
        when(operatorRepository.saveAndFlush(any(OperatorEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OperatorResult result = operatorService.updateOperatorStatus(
                targetOperatorId, updatedDateTime, effectiveStartDate, effectiveEndDate, deletedFlag, updateOperatorId);

        // Assert
        assertNotNull(result);
        assertEquals(existingEntity.getOperatorId(), result.getOperatorId());
        assertEquals((effectiveStartDate == null ? orgEffectiveDate : effectiveStartDate), result.getEffectiveStartDate());
        assertEquals((effectiveEndDate == null ? orgEffectiveDate : effectiveEndDate), result.getEffectiveEndDate());
        assertEquals((deletedFlag == null ? orgDeletedFlag : deletedFlag), result.isDeletedFlag());

        assertEquals(updateOperatorId, result.getUpdatedUserId());

        verify(operatorRepository).findById(targetOperatorId);
        if (!StringUtils.hasText(targetOperatorId)) {
            verify(operatorRepository).saveAndFlush(any(OperatorEntity.class));
        }
    }

    /**
     * Test updateOperator method when no changes are made.
     */
    @ParameterizedTest
    @CsvSource({
        "apiKey123, operator123, org",
        "apiKey123, operator123, null"
    })
    @DisplayName("updateOperatorStatus - No Changes Made")
    void updateOperatorStatus_noChangesMade(
            String apiKey,
            String targetOperatorId,
            String noChangeMode) {

        LocalDate orgEffectiveDate = LocalDate.now();
        Boolean orgDeletedFlag = true;
        String updateOperatorId = "updateOperatorId-123";

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
        OperatorEntity existingEntity = new OperatorEntity();
        existingEntity.setOperatorId(targetOperatorId);
        existingEntity.setUpdatedAt(updatedDateTime);
        existingEntity.setUpdatedUserId(updateOperatorId);

        existingEntity.setEffectiveStartDate(orgEffectiveDate);
        existingEntity.setEffectiveEndDate(orgEffectiveDate);
        existingEntity.setDeletedFlag(orgDeletedFlag);

        when(operatorRepository.findById(targetOperatorId)).thenReturn(Optional.of(existingEntity));

        // Act
        OperatorResult result = operatorService.updateOperatorStatus(
                targetOperatorId, updatedDateTime, effectiveStartDate, effectiveEndDate, deletedFlag, updateOperatorId);

        // Assert
        assertNotNull(result);
        assertEquals(targetOperatorId, result.getOperatorId());

        assertEquals((effectiveStartDate == null ? orgEffectiveDate : effectiveStartDate), result.getEffectiveStartDate());
        assertEquals((effectiveEndDate == null ? orgEffectiveDate : effectiveEndDate), result.getEffectiveEndDate());
        assertEquals((deletedFlag == null ? orgDeletedFlag : deletedFlag), result.isDeletedFlag());

        assertEquals(updatedDateTime, result.getUpdatedAt());
        assertEquals(updateOperatorId, result.getUpdatedUserId());

        verify(operatorRepository).findById(targetOperatorId);
        verify(operatorRepository, never()).saveAndFlush(any(OperatorEntity.class));
    }

    /**
     * Test updateOperator method when the target operator is not found.
     */
    @Test
    @DisplayName("updateOperatorStatus - Operator Not Found")
    void updateOperatorStatus_operatorNotFound() {

        String targetOperatorId = "operatorNotFound123";
        String updatedAt = "2025-08-30T10:15:30.000Z";

        LocalDateTime updatedDateTime = LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_DATE_TIME);

        // Arrange
        when(operatorRepository.findById(targetOperatorId)).thenReturn(Optional.empty());

        // Act
        OperatorResult result = operatorService.updateOperatorStatus(
                targetOperatorId, updatedDateTime, null, null, null, null);

        // Assert
        assertEquals(null, result);
        verify(operatorRepository).findById(targetOperatorId);
    }

    /**
     * Test updateOperatorStatus method for invalid effective dates.
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
    @DisplayName("updateOperatorStatus - Invalid Effective Dates")
    void updateOperatorStatus_invalidEffectiveDates(
            String strEffectiveStartDate,
            String strEffectiveEndDate,
            String orgEffectiveStartDateStr,
            String orgEffectiveEndDateStr
    ) {

        // Handle "null" string to represent actual null values
        String targetOperatorId = "07c52485-aef1-4bae-aaa0-80b5f6db0950";
        LocalDate effectiveStartDate = strEffectiveStartDate == null ? null : DateUtils.parseDate(strEffectiveStartDate, Const.DATE_FORMAT);
        LocalDate effectiveEndDate = strEffectiveEndDate == null ? null : DateUtils.parseDate(strEffectiveEndDate, Const.DATE_FORMAT);
        LocalDate orgEffectiveStartDate = orgEffectiveStartDateStr == null ? null : DateUtils.parseDate(orgEffectiveStartDateStr, Const.DATE_FORMAT);
        LocalDate orgEffectiveEndDate = orgEffectiveEndDateStr == null ? null : DateUtils.parseDate(orgEffectiveEndDateStr, Const.DATE_FORMAT);
        LocalDateTime updatedDateTime = LocalDateTime.parse("2025-08-30T10:15:30.000Z", DateTimeFormatter.ISO_DATE_TIME);
        Boolean deletedFlag = false;

        String updateOperatorId = "d9a38406-cae2-4679-b052-15a75f5531e6";

        // Arrange
        OperatorEntity existingEntity = new OperatorEntity();
        existingEntity.setOperatorId(targetOperatorId);
        existingEntity.setUpdatedAt(updatedDateTime);
        existingEntity.setUpdatedUserId(updateOperatorId);

        existingEntity.setEffectiveStartDate(orgEffectiveStartDate);
        existingEntity.setEffectiveEndDate(orgEffectiveEndDate);
        existingEntity.setDeletedFlag(deletedFlag);

        when(operatorRepository.findById(targetOperatorId)).thenReturn(Optional.of(existingEntity));

        // Act & Assert
        BadParametersException exception = assertThrows(BadParametersException.class, () -> {
            operatorService.updateOperatorStatus(
                    targetOperatorId, updatedDateTime, effectiveStartDate, effectiveEndDate, deletedFlag, updateOperatorId);
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
     * Test updateOperator method for data update conflict.
     */
    @Test
    @DisplayName("updateOperatorStatus - Data Update Conflict")
    void updateOperatorStatus_dataUpdateConflict() {

        String targetOperatorId = "operatorNotFound123";
        String updatedAt = "2025-08-30T10:15:30.000Z";

        LocalDateTime updatedDateTime = LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_DATE_TIME);

        // Arrange
        OperatorEntity existingEntity = new OperatorEntity(
                targetOperatorId, "Old Operator", "Old Address", "oldOpenId", "oldGlobalId",
                LocalDate.now(), LocalDate.now().plusYears(1), "creator123");
        existingEntity.setUpdatedAt(updatedDateTime.minusDays(1)); // Simulate a conflict

        when(operatorRepository.findById(targetOperatorId)).thenReturn(Optional.of(existingEntity));

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            operatorService.updateOperatorStatus(
                    targetOperatorId, updatedDateTime, null, null, null, null);
        });

        assertTrue(exception.getLogMessage().contains(ConstError.ERRLOG_409_CONFLICT));
        verify(operatorRepository).findById(targetOperatorId);
    }
}
