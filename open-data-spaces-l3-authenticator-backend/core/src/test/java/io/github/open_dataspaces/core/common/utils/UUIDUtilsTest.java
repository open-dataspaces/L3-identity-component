/*
 * UUIDUtilsTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the UUIDUtils class.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link UUIDUtils} class.
 */
class UUIDUtilsTest {

    /**
     * (Case#1) Tests the getUUIDs method with single valid UUID string.
     */
    @Test
    void testGetUUIDs_withValidUUID() {
        // Arrange
        List<String> validUUIDs = Arrays.asList(
                "123e4567-e89b-12d3-a456-426614174000"
        );

        // Act
        List<UUID> result = UUIDUtils.getUUIDs(validUUIDs);

        // Assert
        assertEquals(1, result.size(), "The result should contain 1 UUIDs");
        assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), result.get(0));
    }

    /**
     * (Case#2) Tests the getUUIDs method with valid UUID strings.
     */
    @Test
    void testGetUUIDs_withValidUUIDs() {
        // Arrange
        List<String> validUUIDs = Arrays.asList(
                "123e4567-e89b-12d3-a456-426614174000",
                "123e4567-e89b-12d3-a456-426614174001"
        );

        // Act
        List<UUID> result = UUIDUtils.getUUIDs(validUUIDs);

        // Assert
        assertEquals(2, result.size(), "The result should contain 2 UUIDs");
        assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), result.get(0));
        assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-426614174001"), result.get(1));
    }

    /**
     * (Case#3) Tests the getUUIDs method with invalid UUID strings.
     */
    @Test
    void testGetUUIDs_withInvalidUUIDs() {
        // Arrange
        List<String> invalidUUIDs = Arrays.asList(
                "123e4567-e89b-12d3-a456-426614174000",
                "invalid-uuid-string",
                "123e4567-e89b-12d3-a456-426614174001"
        );

        // Act
        List<UUID> result = UUIDUtils.getUUIDs(invalidUUIDs);

        // Assert
        assertTrue(result.isEmpty(), "The result should be an empty list");
    }

    /**
     * (Case#4) Tests the getUUIDs method with an empty list.
     */
    @Test
    void testGetUUIDs_withEmptyList() {
        // Arrange
        List<String> emptyList = Arrays.asList();

        // Act
        List<UUID> result = UUIDUtils.getUUIDs(emptyList);

        // Assert
        assertTrue(result.isEmpty(), "The result should be an empty list");
    }

    /**
     * (Case#5) Tests the getUUIDs method with null input.
     */
    @SuppressWarnings("null")
    @Test
    void testGetUUIDs_withNullInput() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> UUIDUtils.getUUIDs(null), "Null input should throw NullPointerException");
    }

    /**
     * (Case#6) Tests the getUUIDs method with UUIDs containing whitespace.
     */
    @Test
    void testGetUUIDs_withUUIDsContainingWhitespace() {
        // Arrange
        List<String> uuidWithWhitespace = Arrays.asList(
                " 123e4567-e89b-12d3-a456-426614174000 ",
                "123e4567-e89b-12d3-a456-426614174001"
        );

        // Act
        List<UUID> result = UUIDUtils.getUUIDs(uuidWithWhitespace);

        // Assert
        assertTrue(result.isEmpty(), "The result should be an empty list when UUIDs contain whitespace");
    }

    /**
     * (Case#7) Tests the getUUIDs method with a large list of UUIDs.
     */
    @Test
    void testGetUUIDs_withLargeList() {
        // Arrange
        List<String> largeList = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            largeList.add(UUID.randomUUID().toString());
        }

        // Act
        List<UUID> result = UUIDUtils.getUUIDs(largeList);

        // Assert
        assertEquals(10000, result.size(), "The result should contain all valid UUIDs");
    }

    /**
     * (Case#8) Tests that the constructor of UUIDUtils is private.
     */
    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<UUIDUtils> constructor = UUIDUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(exception.getCause() instanceof UnsupportedOperationException, "Cause should be UnsupportedOperationException");
        assertEquals("Utility class cannot be instantiated.", exception.getCause().getMessage());
    }
}
