/*
 * OperatorResultTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for OperatorResult DTO.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import io.github.open_dataspaces.core.domain.entities.OperatorEntity;

/**
 * Unit tests for OperatorResult.
 */
public class OperatorResultTest {
    /**
     * case#1:
     * testNoArgsConstructor tests the no-args constructor of OperatorResult.
     */
    @Test
    void testNoArgsConstructor() {
        OperatorResult result = new OperatorResult();
        assertEquals(null, result.getOperatorId());
        assertEquals(null, result.getOperatorName());
        assertEquals(null, result.getOperatorAddress());
        assertEquals(null, result.getOpenOperatorId());
        assertEquals(null, result.getGlobalOperatorId());
        assertEquals(false, result.isDeletedFlag());
        assertEquals(null, result.getEffectiveStartDate());
        assertEquals(null, result.getEffectiveEndDate());
        assertEquals(null, result.getCreatedAt());
        assertEquals(null, result.getCreatedUserId());
        assertEquals(null, result.getUpdatedAt());
        assertEquals(null, result.getUpdatedUserId());
    }

    /**
     * case#2:
     * testParameterizedConstructor tests the parameterized constructor of OperatorResult.
     */
    @Test
    void testParameterizedConstructor() {
        OperatorResult result = new OperatorResult(
                "id456",
                "事業者A",
                "東京都千代田区1-1-1",
                "openId456",
                "globalId456",
                false,
                LocalDate.of(2024, 4, 1),
                LocalDate.of(2029, 3, 31),
                LocalDateTime.of(2024, 4, 1, 9, 0),
                "creatorA",
                LocalDateTime.of(2024, 4, 2, 10, 0),
                "updaterA"
        );
        assertEquals("id456", result.getOperatorId());
        assertEquals("事業者A", result.getOperatorName());
        assertEquals("東京都千代田区1-1-1", result.getOperatorAddress());
        assertEquals("openId456", result.getOpenOperatorId());
        assertEquals("globalId456", result.getGlobalOperatorId());
        assertEquals(false, result.isDeletedFlag());
        assertEquals(LocalDate.of(2024, 4, 1), result.getEffectiveStartDate());
        assertEquals(LocalDate.of(2029, 3, 31), result.getEffectiveEndDate());
        assertEquals(LocalDateTime.of(2024, 4, 1, 9, 0), result.getCreatedAt());
        assertEquals("creatorA", result.getCreatedUserId());
        assertEquals(LocalDateTime.of(2024, 4, 2, 10, 0), result.getUpdatedAt());
        assertEquals("updaterA", result.getUpdatedUserId());
    }

    /**
     * case#3:
     * testParameterizedConstructor_operatorEntity tests the constructor of OperatorResult that takes an OperatorEntity.
     */
    @Test
    void testParameterizedConstructor_operatorEntity() {
        // Arrange
        OperatorEntity entity = new OperatorEntity();
        entity.setOperatorId("id123");
        entity.setOperatorName("テスト事業者");
        entity.setOperatorAddress("テスト県テスト市テストビル1F");
        entity.setOpenOperatorId("openId123");
        entity.setGlobalOperatorId("globalId123");
        entity.setDeletedFlag(true);
        entity.setEffectiveStartDate(LocalDate.of(2025, 1, 1));
        entity.setEffectiveEndDate(LocalDate.of(2030, 12, 31));
        entity.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        entity.setCreatedUserId("creatorId");
        entity.setUpdatedAt(LocalDateTime.of(2025, 1, 2, 11, 0));
        entity.setUpdatedUserId("updaterId");

        // Act
        OperatorResult result = new OperatorResult(entity);

        // Assert
        assertEquals("id123", result.getOperatorId());
        assertEquals("テスト事業者", result.getOperatorName());
        assertEquals("テスト県テスト市テストビル1F", result.getOperatorAddress());
        assertEquals("openId123", result.getOpenOperatorId());
        assertEquals("globalId123", result.getGlobalOperatorId());
        assertTrue(result.isDeletedFlag());
        assertEquals(LocalDate.of(2025, 1, 1), result.getEffectiveStartDate());
        assertEquals(LocalDate.of(2030, 12, 31), result.getEffectiveEndDate());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), result.getCreatedAt());
        assertEquals("creatorId", result.getCreatedUserId());
        assertEquals(LocalDateTime.of(2025, 1, 2, 11, 0), result.getUpdatedAt());
        assertEquals("updaterId", result.getUpdatedUserId());
    }

    /**
     * case#4:
     * testSettersAndGetters tests the setter and getter methods of OperatorResult.
     */
    @Test
    void testSettersAndGetters() {
        OperatorResult result = new OperatorResult();

        result.setOperatorId("id999");
        result.setOperatorName("事業者B");
        result.setOperatorAddress("大阪府大阪市1-2-3");
        result.setOpenOperatorId("openId999");
        result.setGlobalOperatorId("globalId999");
        result.setDeletedFlag(true);
        result.setEffectiveStartDate(LocalDate.of(2026, 5, 1));
        result.setEffectiveEndDate(LocalDate.of(2031, 4, 30));
        result.setCreatedAt(LocalDateTime.of(2026, 5, 1, 8, 0));
        result.setCreatedUserId("creatorB");
        result.setUpdatedAt(LocalDateTime.of(2026, 5, 2, 9, 0));
        result.setUpdatedUserId("updaterB");

        assertEquals("id999", result.getOperatorId());
        assertEquals("事業者B", result.getOperatorName());
        assertEquals("大阪府大阪市1-2-3", result.getOperatorAddress());
        assertEquals("openId999", result.getOpenOperatorId());
        assertEquals("globalId999", result.getGlobalOperatorId());
        assertTrue(result.isDeletedFlag());
        assertEquals(LocalDate.of(2026, 5, 1), result.getEffectiveStartDate());
        assertEquals(LocalDate.of(2031, 4, 30), result.getEffectiveEndDate());
        assertEquals(LocalDateTime.of(2026, 5, 1, 8, 0), result.getCreatedAt());
        assertEquals("creatorB", result.getCreatedUserId());
        assertEquals(LocalDateTime.of(2026, 5, 2, 9, 0), result.getUpdatedAt());
        assertEquals("updaterB", result.getUpdatedUserId());
    }

    /**
     * case#5:
     * testToString tests the toString method of OperatorResult.
     */
    @Test
    void testToString() {
        OperatorResult response = new OperatorResult();
        response.setOperatorId("id123");
        response.setOperatorName("テスト事業者");
        response.setOperatorAddress("テスト県テスト市テストビル1F");
        response.setOpenOperatorId("openId123");
        response.setGlobalOperatorId("globalId123");
        response.setEffectiveStartDate(LocalDate.of(2025, 1, 1));
        response.setEffectiveEndDate(LocalDate.of(2025, 12, 31));
        response.setDeletedFlag(true);
        response.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        response.setUpdatedAt(LocalDateTime.of(2025, 1, 2, 11, 0));

        String str = response.toString();
        assertTrue(str.contains("id123"));
        assertTrue(str.contains("テスト事業者"));
        assertTrue(str.contains("テスト県テスト市テストビル1F"));
        assertTrue(str.contains("openId123"));
        assertTrue(str.contains("globalId123"));
        assertTrue(str.contains("2025-01-01"));
        assertTrue(str.contains("2025-12-31"));
        assertTrue(str.contains("true"));
        assertTrue(str.contains("2025-01-01T10:00"));
        assertTrue(str.contains("2025-01-02T11:00"));
    }

    /**
     * case#6:
     * testEqualsAndHashCode tests the equals and hashCode methods of OperatorResult.
     */
    @Test
    void testEqualsAndHashCode() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2030, 12, 31);
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2025, 1, 2, 11, 0);
        OperatorResult response1 =  new OperatorResult(
                "id123",
                "テスト事業者",
                "テスト県テスト市テストビル1F",
                "openId123",
                "globalId123",
                true,
                startDate,
                endDate,
                createdAt,
                "creatorId",
                updatedAt,
                "updaterId"
        );

        OperatorResult response2 =  new OperatorResult(
                "id123",
                "テスト事業者",
                "テスト県テスト市テストビル1F",
                "openId123",
                "globalId123",
                true,
                startDate,
                endDate,
                createdAt,
                "creatorId",
                updatedAt,
                "updaterId"
        );

        OperatorResult response3 =  new OperatorResult(
                "id789",
                "テスト事業者3",
                "テスト県テスト市テストビル3F",
                "openId789",
                "globalId789",
                true,
                startDate,
                endDate,
                createdAt,
                "creatorId",
                updatedAt,
                "updaterId"
        );

        // Test equals
        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
        assertNotEquals(response2, response3);

        // Test hashCode
        assertEquals(response1.hashCode(), response2.hashCode());
    }
}
