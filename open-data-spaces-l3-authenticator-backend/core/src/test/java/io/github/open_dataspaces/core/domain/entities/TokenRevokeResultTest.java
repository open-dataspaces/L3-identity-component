/*
 * TokenRevokeResultTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies the behavior and validation logic
 * of the TokenRevokeResult entity.
 *
 * Date: 2026/02/10
 */

package io.github.open_dataspaces.core.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the TokenRevokeResult class.
 */
public class TokenRevokeResultTest {

    private final boolean commonActive = false;

    /**
     * Tests the default constructor of TokenRevokeResult.
     */
    @Test
    void testDefaultConstructor() {
        TokenRevokeResult entity = new TokenRevokeResult();
        // Assert
        assertFalse(entity.isActive());
    }

    /**
     * Tests the parameterized constructor of TokenRevokeResult.
     */
    @Test
    void testSettersAndGetters() {
        TokenRevokeResult entity = new TokenRevokeResult();
        entity.setActive(commonActive);

        // Assert
        assertFalse(entity.isActive());
    }

    /**
     * Tests toString method of TokenRevokeResult.
     */
    @Test
    void testToString() {
        TokenRevokeResult entity = new TokenRevokeResult();
        entity.setActive(commonActive);

        String toStringOutput = entity.toString();
        assertNotNull(toStringOutput);
        assertTrue(toStringOutput.contains(Boolean.toString(commonActive)));
    }

    /**
     * Tests equals and hashCode methods of TokenRevokeResult.
     */
    @Test
    void testEqualsAndHashCode() {
        TokenRevokeResult entity1 = new TokenRevokeResult();
        TokenRevokeResult entity2 = new TokenRevokeResult();
        TokenRevokeResult entity3 = new TokenRevokeResult(true);

        // Assert equality and hash code for entity1 and entity2
        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
        // Assert inequality and hash code for entity1 and entity3
        assertNotEquals(entity1, entity3);
        assertNotEquals(entity1.hashCode(), entity3.hashCode());
    }
}
