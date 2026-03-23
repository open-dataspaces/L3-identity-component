/*
 * CidrsKeyTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This file is part of the user authentication backend system.
 *
 * CidrsKeyTest provides unit tests for the CidrsKey class.
 * <p>These tests verify that CidrsKey's getter, setter, equals, and hashCode methods
 * work as expected.
 * <p>Uses Spring Boot's test context and the local profile for configuration.
 *
 * @author btmurayamakohei
 * @since 2025/06/17
 */

package io.github.open_dataspaces.core.domain.entities;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.open_dataspaces.core.domain.repository.interfaces.CidrsRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the CidrsKeyEntity class.
 *
 * <p>This test class verifies the correct behavior of the CidrsKeyEntity JPA entity,
 * including its getter and setter methods, as well as the ability to retrieve
 * CidrsKeyEntity objects from the repository.
 * </p>
 *
 * @author btmurayamakohei
 * @since 2025/06/11
 */
@SpringBootTest
public class CidrsKeyTest {
    @Autowired
    CidrsRepository repository;

    /**
     * Test No.1
     * Tests that the setter and getter methods of CidrsKey correctly set and return the property values.
     */
    @Test
    void success_setter_getter() {
        CidrsKey key = new CidrsKey();
        key.setCidr("192.168.1.0/24");
        key.setApiKey("api-key-001");
        assertEquals("192.168.1.0/24", key.getCidr());
        assertEquals("api-key-001", key.getApiKey());
    }

    /**
     * Test No.2
     * Tests that the parameterized constructor of CidrsKey correctly sets the property values.
     */
    @Test
    void testParameterizedConstructor() {
        CidrsKey key = new CidrsKey("10.0.0.0/8", "api-key-002");
        assertEquals("10.0.0.0/8", key.getCidr());
        assertEquals("api-key-002", key.getApiKey());
    }

    /**
     * Test No.3
     * Tests that the hashCode method returns the same value for equal objects and different values for different objects.
     */
    @Test
    void testHashCode() {
        CidrsKey key1 = new CidrsKey("192.168.10.0/24", "test-api-key");
        CidrsKey key2 = new CidrsKey("192.168.10.0/24", "test-api-key");
        CidrsKey key3 = new CidrsKey("10.0.0.0/8", "test-api-key");
        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
        assertNotEquals(key1.hashCode(), key3.hashCode());
    }

    /**
     * Test No.4
     * Tests that equals returns true when comparing the same instance.
     */
    @Test
    void testEquals_sameInstance() {
        CidrsKey key = new CidrsKey("192.168.1.0/24", "api-key-001");
        assertTrue(key.equals(key));
    }

    /**
     * Test No.5
     * Tests that equals returns false when comparing with null or an object of a different class.
     */
    @Test
    void testEquals_nullAndDifferentClass() {
        CidrsKey key = new CidrsKey("192.168.1.0/24", "api-key-001");
        assertFalse(key.equals(null));
        assertFalse(key.equals((Object) "Not a CidrsKey"));
    }

    /**
     * Test No.6
     * Tests that equals returns true for objects with the same field values,
     * and false for objects with different field values.
     */
    @Test
    void testEquals_fieldComparison() {
        CidrsKey key1 = new CidrsKey("192.168.1.0/24", "api-key-001");
        CidrsKey key2 = new CidrsKey("192.168.1.0/24", "api-key-001");
        CidrsKey key3 = new CidrsKey("10.0.0.0/8", "api-key-001");
        CidrsKey key4 = new CidrsKey("192.168.1.0/24", "api-key-002");

        assertTrue(key1.equals(key2));
        assertFalse(key1.equals(key3));
        assertFalse(key1.equals(key4));
    }
}
