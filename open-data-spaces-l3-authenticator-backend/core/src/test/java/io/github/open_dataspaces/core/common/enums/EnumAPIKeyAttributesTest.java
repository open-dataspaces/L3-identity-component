/*
 * EnumAPIKeyAttributesTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the EnumAPIKeyAttributes class.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.common.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link EnumAPIKeyAttributes} enum.
 */
class EnumAPIKeyAttributesTest {

    /**
     * (Case#1) Tests the getValue method.
     */
    @Test
    void testGetValue() {
        assertEquals("DataSpace", EnumAPIKeyAttributes.DATASPACE.getValue(), "DATASPACE should return 'DataSpace'");
        assertEquals("Application", EnumAPIKeyAttributes.APPLICATION.getValue(), "APPLICATION should return 'Application'");
        assertEquals("Traceability", EnumAPIKeyAttributes.TRACEABILITY.getValue(), "TRACEABILITY should return 'Traceability'");
    }

    /**
     * (Case#2) Tests the enum constants.
     */
    @Test
    void testEnumConstants() {
        EnumAPIKeyAttributes[] values = EnumAPIKeyAttributes.values();
        assertEquals(3, values.length, "There should be 3 enum constants");
        assertEquals(EnumAPIKeyAttributes.DATASPACE, values[0], "First constant should be DATASPACE");
        assertEquals(EnumAPIKeyAttributes.APPLICATION, values[1], "Second constant should be APPLICATION");
        assertEquals(EnumAPIKeyAttributes.TRACEABILITY, values[2], "Third constant should be TRACEABILITY");
    }

    /**
     * (Case#3) Tests the valueOf.
     */
    @Test
    void testValueOf() {
        assertEquals(EnumAPIKeyAttributes.DATASPACE, EnumAPIKeyAttributes.valueOf("DATASPACE"), "valueOf should return DATASPACE");
        assertEquals(EnumAPIKeyAttributes.APPLICATION, EnumAPIKeyAttributes.valueOf("APPLICATION"), "valueOf should return APPLICATION");
        assertEquals(EnumAPIKeyAttributes.TRACEABILITY, EnumAPIKeyAttributes.valueOf("TRACEABILITY"), "valueOf should return TRACEABILITY");
    }

    /**
     * (Case#4) Tests that valueOf throws an exception for invalid names.
     */
    @Test
    void testValueOf_withInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> EnumAPIKeyAttributes.valueOf("INVALID"), "valueOf should throw IllegalArgumentException for invalid names");
    }
}