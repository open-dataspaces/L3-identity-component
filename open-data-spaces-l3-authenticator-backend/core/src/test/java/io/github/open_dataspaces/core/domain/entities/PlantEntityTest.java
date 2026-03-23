/*
 * PlantEntityTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for the PlantEntity class.
 *
 * Date: 2025/09/22
 */

package io.github.open_dataspaces.core.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.open_dataspaces.core.domain.dto.StateString;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import java.time.LocalDate;

import io.github.open_dataspaces.core.domain.repository.interfaces.OperatorRepository;
import io.github.open_dataspaces.core.domain.repository.interfaces.PlantRepository;

import jakarta.persistence.Column;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Unit tests for PlantEntity.
 */
@SpringBootTest
public class PlantEntityTest {

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private OperatorRepository operatorRepository;

    /**
     * Test for the default constructor.
     */
    @Test
    void testDefaultConstructor() {
        PlantEntity entity = new PlantEntity();
        assertNotNull(entity);
        assertNull(entity.getPlantId());
        assertNull(entity.getPlantName());
        assertNull(entity.getPlantAddress());
        assertNull(entity.getOpenPlantId());
        assertNull(entity.getGlobalPlantId());
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
        "Plant123, Operator123, Plant Name, Address, open123, global123, 2025-08-30, 2025-12-31, creator123",
        "Plant456, Operator456, Another Plant, Another Address, open456, global456, 2025-09-01, 2025-12-31, creator456"
    })
    @DisplayName("PlantEntity - Constructor and Getters (Normal Case)")
    void testParameterizedConstructor(
            String operatorId,
            String plantId,
            String plantName,
            String plantAddress,
            String openPlantId,
            String globalPlantId,
            String effectiveStartDate,
            String effectiveEndDate,
            String creatorPlantId) {

        // Arrange
        LocalDate startDate = LocalDate.parse(effectiveStartDate);
        LocalDate endDate = LocalDate.parse(effectiveEndDate);

        // Act
        PlantEntity entity = new PlantEntity(
                plantId,
                operatorId,
                plantName,
                plantAddress,
                openPlantId,
                globalPlantId,
                startDate,
                endDate,
                creatorPlantId
        );

        // Assert
        assertEquals(operatorId, entity.getOperatorId());
        assertEquals(plantId, entity.getPlantId());
        assertEquals(plantName, entity.getPlantName());
        assertEquals(plantAddress, entity.getPlantAddress());
        assertEquals(openPlantId, entity.getOpenPlantId());
        assertEquals(globalPlantId, entity.getGlobalPlantId());
        assertEquals(startDate, entity.getEffectiveStartDate());
        assertEquals(endDate, entity.getEffectiveEndDate());
        assertEquals(creatorPlantId, entity.getCreatedUserId());
        assertEquals(creatorPlantId, entity.getUpdatedUserId());
    }

    /**
     * Test for retrieving an active plant by specification.
     */
    @Test
    @DisplayName("Repository: retrieve active plant by specification")
    @Sql(statements = {
            "INSERT INTO auth.tbl_operators (operator_id, operator_name, operator_address, open_operator_id, global_operator_id, "
                    + "deleted_flag, effective_start_date, effective_end_date, created_user_id, updated_user_id, created_at, updated_at) VALUES ("
                    + "'op-repo-active', 'Active Plant', 'Addr-A', 'open-active', 'global-active', "
                    + "false, '2000-01-01', '2999-12-31', 'creatorA', 'creatorA', now(), now());"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
            "INSERT INTO auth.tbl_plants (operator_id, plant_id, plant_name, plant_address, open_plant_id, global_plant_id, "
                    + "deleted_flag, effective_start_date, effective_end_date, created_user_id, updated_user_id, created_at, updated_at) VALUES ("
                    + "'op-repo-active', 'plant-repo-active', 'Active Plant', 'Addr-A', 'open-active', 'global-active', "
                    + "false, '2000-01-01', '2999-12-31', 'creatorA', 'creatorA', now(), now());"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
            "DELETE FROM auth.tbl_plants WHERE operator_id = 'op-repo-active';"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Sql(statements = {
            "DELETE FROM auth.tbl_operators WHERE operator_id = 'op-repo-active';"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testFindByIdSuccess() {
        Optional<PlantEntity> findByIdResult = plantRepository.findById("plant-repo-active");
        assertTrue(findByIdResult.isPresent(), "Active plant should be returned.");
        PlantEntity plantEntity = findByIdResult.get();

        assertEquals("op-repo-active", plantEntity.getOperatorId(), "operatorId mismatch");
        assertEquals("plant-repo-active", plantEntity.getPlantId(), "plantId mismatch");
        assertEquals("Active Plant", plantEntity.getPlantName(), "plantName mismatch");
        assertEquals("Addr-A", plantEntity.getPlantAddress(), "plantAddress mismatch");
        assertEquals("open-active", plantEntity.getOpenPlantId(), "openPlantId mismatch");
        assertEquals("global-active", plantEntity.getGlobalPlantId(), "globalPlantId mismatch");
        assertFalse(plantEntity.isDeletedFlag(), "deletedFlag should be false");
        assertEquals(LocalDate.parse("2000-01-01"), plantEntity.getEffectiveStartDate(), "effectiveStartDate mismatch");
        assertEquals(LocalDate.parse("2999-12-31"), plantEntity.getEffectiveEndDate(), "effectiveEndDate mismatch");
        assertEquals("creatorA", plantEntity.getCreatedUserId(), "createdUserId mismatch");
    }

    /**
     * Test for AbstractBaseEntity's onCreate method.
     */
    @Test
    @DisplayName("AbstractBaseEntity - onCreate Test")
    @Sql(statements = {
            "DELETE FROM auth.tbl_plants WHERE Plant_id = 'plant-repo-save';"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Sql(statements = {
            "DELETE FROM auth.tbl_operators WHERE operator_id = 'op-repo-save';"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testSaveSuccess() {
        // Arrange
        OperatorEntity opEntity = new OperatorEntity(
                "op-repo-save",
                "Active Operator",
                "Addr-A",
                "open-save",
                "global-save",
                LocalDate.parse("2025-08-30"),
                LocalDate.parse("2025-12-31"),
                "creator-save"
        );

        operatorRepository.save(opEntity);

        // Arrange
        PlantEntity entity = new PlantEntity(
                "plant-repo-save",
                "op-repo-save",
                "Active Plant",
                "Addr-A",
                "open-save",
                "global-save",
                LocalDate.parse("2025-08-30"),
                LocalDate.parse("2025-12-31"),
                "creator-save"
        );

        PlantEntity plantEntity = plantRepository.save(entity);

        assertNotNull(plantEntity, "Active Plant should be returned.");
        assertEquals("op-repo-save", plantEntity.getOperatorId(), "OperatorID mismatch");
        assertEquals("plant-repo-save", plantEntity.getPlantId(), "PlantId mismatch");
        assertEquals("Active Plant", plantEntity.getPlantName(), "PlantName mismatch");
        assertEquals("Addr-A", plantEntity.getPlantAddress(), "PlantAddress mismatch");
        assertEquals("open-save", plantEntity.getOpenPlantId(), "openPlantId mismatch");
        assertEquals("global-save", plantEntity.getGlobalPlantId(), "globalPlantId mismatch");
        assertFalse(plantEntity.isDeletedFlag(), "deletedFlag should be false");
        assertEquals(LocalDate.parse("2025-08-30"), plantEntity.getEffectiveStartDate(), "effectiveStartDate mismatch");
        assertEquals(LocalDate.parse("2025-12-31"), plantEntity.getEffectiveEndDate(), "effectiveEndDate mismatch");
        assertEquals("creator-save", plantEntity.getCreatedUserId(), "createdUserId mismatch");

        // Verify that the entity is actually saved in the database
        Optional<PlantEntity> findByIdResult = plantRepository.findById("plant-repo-save");
        assertTrue(findByIdResult.isPresent(), "Saved Plant should be retrievable.");
        PlantEntity retrievedEntity = findByIdResult.get();
        assertEquals("op-repo-save", plantEntity.getOperatorId(), "OperatorID mismatch");
        assertEquals("plant-repo-save", retrievedEntity.getPlantId(), "PlantId mismatch");
        assertEquals("Active Plant", retrievedEntity.getPlantName(), "PlantName mismatch");
        assertEquals("Addr-A", retrievedEntity.getPlantAddress(), "PlantAddress mismatch");
        assertEquals("open-save", retrievedEntity.getOpenPlantId(), "openPlantId mismatch");
        assertEquals("global-save", retrievedEntity.getGlobalPlantId(), "globalPlantId mismatch");
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
        PlantEntity entity = new PlantEntity();

        // Use reflection to access the protected onCreate method
        try {
            Method onCreateMethod = PlantEntity.class.getSuperclass().getDeclaredMethod("onCreate");
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
        PlantEntity entity = new PlantEntity();

        try {
            Method onCreateMethod = PlantEntity.class.getSuperclass().getDeclaredMethod("onCreate");
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
            Method onUpdateMethod = PlantEntity.class.getSuperclass().getDeclaredMethod("onUpdate");
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
    @DisplayName("PlantEntity - Equals and HashCode")
    void testEqualsAndHashCode() {
        // Arrange
        PlantEntity entity1 = new PlantEntity(
                "Plant123",
                "Operator123",
                "Plant Name",
                "Address",
                "open123",
                "global123",
                LocalDate.parse("2025-08-30"),
                LocalDate.parse("2025-12-31"),
                "creator123"
        );

        PlantEntity entity2 = new PlantEntity(
                "Plant123",
                "Operator123",
                "Plant Name",
                "Address",
                "open123",
                "global123",
                LocalDate.parse("2025-08-30"),
                LocalDate.parse("2025-12-31"),
                "creator123"
        );

        PlantEntity entity3 = new PlantEntity(
                "Plant456",
                "Operator456",
                "Another Plant",
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
    @DisplayName("PlantEntity - @Column Annotation Test")
    void testColumnAnnotations() throws NoSuchFieldException {
        // Verify @Column for plantId
        Field plantIdField = PlantEntity.class.getDeclaredField("plantId");
        Column plantIdColumn = plantIdField.getAnnotation(Column.class);
        assertNotNull(plantIdColumn, "The plantId field should have a @Column annotation.");
        assertEquals("plant_id", plantIdColumn.name(), "The @Column name for plantId is incorrect.");

        // Verify @Column for operatorId
        Field operatorIdField = PlantEntity.class.getDeclaredField("operatorId");
        Column operatorIdColumn = operatorIdField.getAnnotation(Column.class);
        assertNotNull(operatorIdColumn, "The operatorId field should have a @Column annotation.");
        assertEquals("operator_id", operatorIdColumn.name(), "The @Column name for operatorId is incorrect.");

        // Verify @Column for plantName
        Field plantNameField = PlantEntity.class.getDeclaredField("plantName");
        Column plantNameColumn = plantNameField.getAnnotation(Column.class);
        assertNotNull(plantNameColumn, "The plantName field should have a @Column annotation.");
        assertEquals("plant_name", plantNameColumn.name(), "The @Column name for plantName is incorrect.");

        // Verify @Column for plantAddress
        Field plantAddressField = PlantEntity.class.getDeclaredField("plantAddress");
        Column plantAddressColumn = plantAddressField.getAnnotation(Column.class);
        assertNotNull(plantAddressColumn, "The plantAddress field should have a @Column annotation.");
        assertEquals("plant_address", plantAddressColumn.name(), "The @Column name for plantAddress is incorrect.");

        // Verify @Column for openPlantId
        Field openPlantIdField = PlantEntity.class.getDeclaredField("openPlantId");
        Column openPlantIdColumn = openPlantIdField.getAnnotation(Column.class);
        assertNotNull(openPlantIdColumn, "The openPlantId field should have a @Column annotation.");
        assertEquals("open_plant_id", openPlantIdColumn.name(), "The @Column name for openPlantId is incorrect.");

        // Verify @Column for globalPlantId
        Field globalPlantIdField = PlantEntity.class.getDeclaredField("globalPlantId");
        Column globalPlantIdColumn = globalPlantIdField.getAnnotation(Column.class);
        assertNotNull(globalPlantIdColumn, "The globalPlantId field should have a @Column annotation.");
        assertEquals("global_plant_id", globalPlantIdColumn.name(), "The @Column name for globalPlantId is incorrect.");
    }

    /**
     * Test for isUpdated method.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // Normal cases
        "true,Plant Name,Plant Address,open123,global123",
        // Change
        "true,New PlantName,Old Address,oldOpenId,oldGlobalId",
        "true,Old PlantName,New Address,oldOpenId,oldGlobalId",
        "true,Old PlantName,Old Address,New OpenId,oldGlobalId",
        "true,Old PlantName,Old Address,oldOpenId,New GlobalId",
        // Change to null
        "true,null,Old Address,oldOpenId,oldGlobalId",
        "true,Old PlantName,null,oldOpenId,oldGlobalId",
        "true,Old PlantName,Old Address,null,oldGlobalId",
        "true,Old PlantName,Old Address,oldOpenId,null",
        // empty
        "false,Old PlantName,Old Address,oldOpenId,empty",
        // No changes
        "false,Old PlantName,Old Address,oldOpenId,oldGlobalId",
    }, nullValues = "NULL")
    @DisplayName("Test isUpdated")
    void testIsUpdated(boolean expected, String plantName, String plantAddress, String openPlantId, String globalPlantId) throws Exception {
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

        PlantEntity plantEntity = new PlantEntity(
                "OldPlantId", "OldOperatorId", "Old PlantName", "Old Address", "oldOpenId", "oldGlobalId",
                LocalDate.now(), LocalDate.now().plusYears(1), "creator123");
        // Assert
        assertEquals(expected, plantEntity.isUpdated(plantName, plantAddress, openPlantId, stateGlobalPlantId));
    }
}