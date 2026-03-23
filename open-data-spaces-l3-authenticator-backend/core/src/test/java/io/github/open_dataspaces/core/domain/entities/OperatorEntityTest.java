/*
 * OperatorEntityTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for the OperatorEntity class.
 *
 * Date: 2025/08/30
 */

package io.github.open_dataspaces.core.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.open_dataspaces.core.domain.repository.interfaces.OperatorRepository;

import jakarta.persistence.Column;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Unit tests for OperatorEntity.
 */
@SpringBootTest
public class OperatorEntityTest {

    @Autowired
    private OperatorRepository operatorRepository;

    /**
     * Test for the default constructor.
     */
    @Test
    void testDefaultConstructor() {
        OperatorEntity entity = new OperatorEntity();
        assertNotNull(entity);
        assertNull(entity.getOperatorId());
        assertNull(entity.getOperatorName());
        assertNull(entity.getOperatorAddress());
        assertNull(entity.getOpenOperatorId());
        assertNull(entity.getGlobalOperatorId());
        assertNull(entity.getEffectiveStartDate());
        assertNull(entity.getEffectiveEndDate());
        assertNull(entity.getCreatedUserId());
        assertNull(entity.getUpdatedUserId());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    /**
     * Test for the constructor and getters (normal case).
     */
    @ParameterizedTest
    @CsvSource({
        "operator123, Operator Name, Address, open123, global123, 2025-08-30, 2025-12-31, creator123",
        "operator456, Another Operator, Another Address, open456, global456, 2025-09-01, 2025-12-31, creator456"
    })
    @DisplayName("OperatorEntity - Constructor and Getters (Normal Case)")
    void testParameterizedConstructor(
            String operatorId,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId,
            String effectiveStartDate,
            String effectiveEndDate,
            String creatorOperatorId) {

        // Arrange
        LocalDate startDate = LocalDate.parse(effectiveStartDate);
        LocalDate endDate = LocalDate.parse(effectiveEndDate);

        // Act
        OperatorEntity entity = new OperatorEntity(
                operatorId,
                operatorName,
                operatorAddress,
                openOperatorId,
                globalOperatorId,
                startDate,
                endDate,
                creatorOperatorId
        );

        // Assert
        assertEquals(operatorId, entity.getOperatorId());
        assertEquals(operatorName, entity.getOperatorName());
        assertEquals(operatorAddress, entity.getOperatorAddress());
        assertEquals(openOperatorId, entity.getOpenOperatorId());
        assertEquals(globalOperatorId, entity.getGlobalOperatorId());
        assertEquals(startDate, entity.getEffectiveStartDate());
        assertEquals(endDate, entity.getEffectiveEndDate());
        assertEquals(creatorOperatorId, entity.getCreatedUserId());
        assertEquals(creatorOperatorId, entity.getUpdatedUserId());
    }

    /**
     * Test for retrieving an active operator by specification.
     */
    @Test
    @DisplayName("Repository: retrieve active operator by specification")
    @Sql(statements = {
            "INSERT INTO auth.tbl_operators (operator_id, operator_name, operator_address, open_operator_id, global_operator_id, "
                    + "deleted_flag, effective_start_date, effective_end_date, created_user_id, updated_user_id, created_at, updated_at) VALUES ("
                    + "'op-repo-active', 'Active Operator', 'Addr-A', 'open-active', 'global-active', "
                    + "false, '2000-01-01', '2999-12-31', 'creatorA', 'creatorA', now(), now());"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
            "DELETE FROM auth.tbl_operators WHERE operator_id = 'op-repo-active';"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testFindByIdSuccess() {
        Optional<OperatorEntity> findByIdResult = operatorRepository.findById("op-repo-active");
        assertTrue(findByIdResult.isPresent(), "Active operator should be returned.");
        OperatorEntity operatorEntity = findByIdResult.get();

        assertEquals("op-repo-active", operatorEntity.getOperatorId(), "operatorId mismatch");
        assertEquals("Active Operator", operatorEntity.getOperatorName(), "operatorName mismatch");
        assertEquals("Addr-A", operatorEntity.getOperatorAddress(), "operatorAddress mismatch");
        assertEquals("open-active", operatorEntity.getOpenOperatorId(), "openOperatorId mismatch");
        assertEquals("global-active", operatorEntity.getGlobalOperatorId(), "globalOperatorId mismatch");
        assertFalse(operatorEntity.isDeletedFlag(), "deletedFlag should be false");
        assertEquals(LocalDate.parse("2000-01-01"), operatorEntity.getEffectiveStartDate(), "effectiveStartDate mismatch");
        assertEquals(LocalDate.parse("2999-12-31"), operatorEntity.getEffectiveEndDate(), "effectiveEndDate mismatch");
        assertEquals("creatorA", operatorEntity.getCreatedUserId(), "createdUserId mismatch");
    }

    /**
     * Test for AbstractBaseEntity's onCreate method.
     */
    @Test
    @DisplayName("OperatorEntity - saveAndFlush Test")
    @Sql(statements = {
            "INSERT INTO auth.tbl_operators (operator_id, operator_name, operator_address, open_operator_id, global_operator_id, "
                    + "deleted_flag, effective_start_date, effective_end_date, created_user_id, updated_user_id, created_at, updated_at) VALUES ("
                    + "'op-repo-active', 'Active Operator', 'Addr-A', 'open-active', 'global-active', "
                    + "false, '2000-01-01', '2999-12-31', 'creatorA', 'creatorA', now(), now());"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
            "DELETE FROM auth.tbl_operators WHERE operator_id = 'op-repo-active';"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testSaveAndFlushSuccess() {
        // Arrange
        OperatorEntity entity = new OperatorEntity(
                "op-repo-active",
                "update Operator",
                "Addr-update",
                "open-update",
                "global-update",
                LocalDate.parse("2099-08-30"),
                LocalDate.parse("2099-12-31"),
                "creatorA"
        );
        entity.setUpdatedUserId("updatorA");

        OperatorEntity operatorEntity = operatorRepository.saveAndFlush(entity);

        assertNotNull(operatorEntity, "Active operator should be returned.");
        assertEquals("op-repo-active", operatorEntity.getOperatorId(), "operatorId mismatch");
        assertEquals("update Operator", operatorEntity.getOperatorName(), "operatorName mismatch");
        assertEquals("Addr-update", operatorEntity.getOperatorAddress(), "operatorAddress mismatch");
        assertEquals("open-update", operatorEntity.getOpenOperatorId(), "openOperatorId mismatch");
        assertEquals("global-update", operatorEntity.getGlobalOperatorId(), "globalOperatorId mismatch");
        assertFalse(operatorEntity.isDeletedFlag(), "deletedFlag should be false");
        assertEquals(LocalDate.parse("2099-08-30"), operatorEntity.getEffectiveStartDate(), "effectiveStartDate mismatch");
        assertEquals(LocalDate.parse("2099-12-31"), operatorEntity.getEffectiveEndDate(), "effectiveEndDate mismatch");
        assertEquals("creatorA", operatorEntity.getCreatedUserId(), "createdUserId mismatch");
        assertEquals("updatorA", operatorEntity.getUpdatedUserId(), "updatedUserId mismatch");

        // Verify that the entity is actually saved in the database
        Optional<OperatorEntity> findByIdResult = operatorRepository.findById("op-repo-active");

        assertTrue(findByIdResult.isPresent(), "Saved operator should be retrievable.");
        OperatorEntity retrievedEntity = findByIdResult.get();
        assertEquals("op-repo-active", retrievedEntity.getOperatorId(), "operatorId mismatch");
        assertEquals("update Operator", retrievedEntity.getOperatorName(), "operatorName mismatch");
        assertEquals("Addr-update", retrievedEntity.getOperatorAddress(), "operatorAddress mismatch");
        assertEquals("open-update", retrievedEntity.getOpenOperatorId(), "openOperatorId mismatch");
        assertEquals("global-update", retrievedEntity.getGlobalOperatorId(), "globalOperatorId mismatch");
        assertFalse(retrievedEntity.isDeletedFlag(), "deletedFlag should be false");
        assertEquals(LocalDate.parse("2099-08-30"), retrievedEntity.getEffectiveStartDate(), "effectiveStartDate mismatch");
        assertEquals(LocalDate.parse("2099-12-31"), retrievedEntity.getEffectiveEndDate(), "effectiveEndDate mismatch");
        assertEquals("creatorA", retrievedEntity.getCreatedUserId(), "createdUserId mismatch");
        assertEquals("updatorA", retrievedEntity.getUpdatedUserId(), "updatedUserId mismatch");
    }

    /**
     * Test for AbstractBaseEntity's onCreate method.
     */
    @Test
    @DisplayName("AbstractBaseEntity - onCreate Test")
    @Sql(statements = {
            "DELETE FROM auth.tbl_operators WHERE operator_id = 'op-repo-save';"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testSaveSuccess() {
        // Arrange
        OperatorEntity entity = new OperatorEntity(
                "op-repo-save",
                "Active Operator",
                "Addr-A",
                "open-save",
                "global-save",
                LocalDate.parse("2025-08-30"),
                LocalDate.parse("2025-12-31"),
                "creator-save"
        );

        OperatorEntity operatorEntity = operatorRepository.save(entity);

        assertNotNull(operatorEntity, "Active operator should be returned.");
        assertEquals("op-repo-save", operatorEntity.getOperatorId(), "operatorId mismatch");
        assertEquals("Active Operator", operatorEntity.getOperatorName(), "operatorName mismatch");
        assertEquals("Addr-A", operatorEntity.getOperatorAddress(), "operatorAddress mismatch");
        assertEquals("open-save", operatorEntity.getOpenOperatorId(), "openOperatorId mismatch");
        assertEquals("global-save", operatorEntity.getGlobalOperatorId(), "globalOperatorId mismatch");
        assertFalse(operatorEntity.isDeletedFlag(), "deletedFlag should be false");
        assertEquals(LocalDate.parse("2025-08-30"), operatorEntity.getEffectiveStartDate(), "effectiveStartDate mismatch");
        assertEquals(LocalDate.parse("2025-12-31"), operatorEntity.getEffectiveEndDate(), "effectiveEndDate mismatch");
        assertEquals("creator-save", operatorEntity.getCreatedUserId(), "createdUserId mismatch");

        // Verify that the entity is actually saved in the database
        Optional<OperatorEntity> findByIdResult = operatorRepository.findById("op-repo-save");
        assertTrue(findByIdResult.isPresent(), "Saved operator should be retrievable.");
        OperatorEntity retrievedEntity = findByIdResult.get();
        assertEquals("op-repo-save", retrievedEntity.getOperatorId(), "operatorId mismatch");
        assertEquals("Active Operator", retrievedEntity.getOperatorName(), "operatorName mismatch");
        assertEquals("Addr-A", retrievedEntity.getOperatorAddress(), "operatorAddress mismatch");
        assertEquals("open-save", retrievedEntity.getOpenOperatorId(), "openOperatorId mismatch");
        assertEquals("global-save", retrievedEntity.getGlobalOperatorId(), "globalOperatorId mismatch");
        assertFalse(retrievedEntity.isDeletedFlag(), "deletedFlag should be false");
        assertEquals(LocalDate.parse("2025-08-30"), retrievedEntity.getEffectiveStartDate(), "effectiveStartDate mismatch");
        assertEquals(LocalDate.parse("2025-12-31"), retrievedEntity.getEffectiveEndDate(), "effectiveEndDate mismatch");
        assertEquals("creator-save", retrievedEntity.getCreatedUserId(), "createdUserId mismatch");
    }

    /**
     * Test for AbstractBaseEntity's onCreate method.
     */
    @Test
    @DisplayName("AbstractBaseEntity - onCreate Test")
    void testOnCreate() {
        // Arrange
        OperatorEntity entity = new OperatorEntity();

        // Use reflection to access the protected onCreate method
        try {
            Method onCreateMethod = OperatorEntity.class.getSuperclass().getDeclaredMethod("onCreate");
            onCreateMethod.setAccessible(true);
            // Act
            onCreateMethod.invoke(entity);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("onCreate method not found", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to access onCreate method", e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException("Error invoking onCreate method", e);
        }

        // Assert
        assertNotNull(entity.getCreatedAt(), "createdAt should not be null after onCreate.");
        assertNotNull(entity.getUpdatedAt(), "updatedAt should not be null after onCreate.");
        assertTrue(entity.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)),
                "createdAt should be set to a time before the current time.");
    }

    /**
     * Test for AbstractBaseEntity's onUpdate method.
     */
    @Test
    @DisplayName("AbstractBaseEntity - onUpdate Test")
    void testOnUpdate() throws InterruptedException {
        // Arrange
        OperatorEntity entity = new OperatorEntity();

        try {
            Method onCreateMethod = OperatorEntity.class.getSuperclass().getDeclaredMethod("onCreate");
            onCreateMethod.setAccessible(true);
            // Act
            onCreateMethod.invoke(entity);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("onCreate method not found", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to access onCreate method", e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException("Error invoking onCreate method", e);
        }

        // Simulate some delay to ensure updatedAt is different
        Thread.sleep(10);

        try {
            Method onUpdateMethod = OperatorEntity.class.getSuperclass().getDeclaredMethod("onUpdate");
            onUpdateMethod.setAccessible(true);
            // Act
            onUpdateMethod.invoke(entity);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("onUpdate method not found", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to access onUpdate method", e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException("Error invoking onUpdate method", e);
        }

        // Assert
        assertNotNull(entity.getUpdatedAt(), "updatedAt should not be null after onUpdate.");
        assertTrue(entity.getUpdatedAt().isAfter(entity.getCreatedAt()), "updatedAt should be after createdAt.");
        assertTrue(entity.getUpdatedAt().isBefore(LocalDateTime.now().plusSeconds(1)),
                "updatedAt should be set to a time before the current time.");
    }

    /**
     * Test for equals and hashCode methods.
     */
    @Test
    @DisplayName("OperatorEntity - Equals and HashCode")
    void testEqualsAndHashCode() {
        // Arrange
        OperatorEntity entity1 = new OperatorEntity(
                "operator123",
                "Operator Name",
                "Address",
                "open123",
                "global123",
                LocalDate.parse("2025-08-30"),
                LocalDate.parse("2025-12-31"),
                "creator123"
        );

        OperatorEntity entity2 = new OperatorEntity(
                "operator123",
                "Operator Name",
                "Address",
                "open123",
                "global123",
                LocalDate.parse("2025-08-30"),
                LocalDate.parse("2025-12-31"),
                "creator123"
        );

        OperatorEntity entity3 = new OperatorEntity(
                "operator456",
                "Another Operator",
                "Another Address",
                "open456",
                "global456",
                LocalDate.parse("2025-09-01"),
                LocalDate.parse("2025-12-31"),
                "creator456"
        );

        // Act & Assert
        assertEquals(entity1, entity2);
        assertNotEquals(entity1, entity3);
        assertEquals(entity1.hashCode(), entity2.hashCode());
        assertNotEquals(entity1.hashCode(), entity3.hashCode());
    }

    /**
     * Test to verify that @Column annotations are correctly set on fields.
     */
    @Test
    @DisplayName("OperatorEntity - @Column Annotation Test")
    void testColumnAnnotations() throws NoSuchFieldException {
        // Verify @Column for operatorId
        Field operatorIdField = OperatorEntity.class.getDeclaredField("operatorId");
        Column operatorIdColumn = operatorIdField.getAnnotation(Column.class);
        assertNotNull(operatorIdColumn, "The operatorId field should have a @Column annotation.");
        assertEquals("operator_id", operatorIdColumn.name(), "The @Column name for operatorId is incorrect.");

        // Verify @Column for operatorName
        Field operatorNameField = OperatorEntity.class.getDeclaredField("operatorName");
        Column operatorNameColumn = operatorNameField.getAnnotation(Column.class);
        assertNotNull(operatorNameColumn, "The operatorName field should have a @Column annotation.");
        assertEquals("operator_name", operatorNameColumn.name(), "The @Column name for operatorName is incorrect.");

        // Verify @Column for operatorAddress
        Field operatorAddressField = OperatorEntity.class.getDeclaredField("operatorAddress");
        Column operatorAddressColumn = operatorAddressField.getAnnotation(Column.class);
        assertNotNull(operatorAddressColumn, "The operatorAddress field should have a @Column annotation.");
        assertEquals("operator_address", operatorAddressColumn.name(), "The @Column name for operatorAddress is incorrect.");

        // Verify @Column for openOperatorId
        Field openOperatorIdField = OperatorEntity.class.getDeclaredField("openOperatorId");
        Column openOperatorIdColumn = openOperatorIdField.getAnnotation(Column.class);
        assertNotNull(openOperatorIdColumn, "The openOperatorId field should have a @Column annotation.");
        assertEquals("open_operator_id", openOperatorIdColumn.name(), "The @Column name for openOperatorId is incorrect.");

        // Verify @Column for globalOperatorId
        Field globalOperatorIdField = OperatorEntity.class.getDeclaredField("globalOperatorId");
        Column globalOperatorIdColumn = globalOperatorIdField.getAnnotation(Column.class);
        assertNotNull(globalOperatorIdColumn, "The globalOperatorId field should have a @Column annotation.");
        assertEquals("global_operator_id", globalOperatorIdColumn.name(), "The @Column name for globalOperatorId is incorrect.");
    }
}