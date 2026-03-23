/*
 * EntityUtilsTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for EntityUtils.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import jakarta.persistence.Table;

/**
 * Unit tests for the {@link EntityUtils} class.
 */
public class EntityUtilsTest {

    /**
     * Default constructor.
     */
    @Test
    @DisplayName("EntityUtils - constructor")
    void testConstructor() {
        EntityUtils instance = new EntityUtils();
        assertNotNull(instance);
    }

    @Table(name = "tableName")
    private static class TestEntityWithTable {
    }

    private static class TestEntityWithoutTable {
    }

    @Table
    private static class TestEntityWithoutTableName {
    }

    @Table(name = "")
    private static class TestEntityWithEmptyTableName {
    }

    /**
     * Test for parseDate.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // date, format
        "TestEntityWithTable, tableName",
        "TestEntityWithoutTable, TestEntityWithoutTable",
        "TestEntityWithoutTableName, TestEntityWithoutTableName",
        "TestEntityWithEmptyTableName, TestEntityWithEmptyTableName"
    })
    @DisplayName("getTableName - success")
    void testGetTableName_success(
            String className,
            String expectedTableName
    ) {
        Class<?> clazz =
                "TestEntityWithTable".equals(className) ? TestEntityWithTable.class :
                "TestEntityWithoutTable".equals(className) ? TestEntityWithoutTable.class :
                "TestEntityWithoutTableName".equals(className) ? TestEntityWithoutTableName.class :
                "TestEntityWithEmptyTableName".equals(className) ? TestEntityWithEmptyTableName.class : null;

        // Act
        String actualTableName = EntityUtils.getTableName(clazz);

        // Assert
        assertNotNull(actualTableName);
        assertEquals(expectedTableName, actualTableName);
    }
}
