/*
 * IdpClientInfoTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for IdpClientInfo DTO.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.domain.dto;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for IdpClientInfo DTO.
 * This class tests constructors, getters/setters, equals/hashCode, toString, and field existence.
 */
class IdpClientInfoTest {

    // Common input parameters
    private final String uuid = "uuid-123";
    private final String clientId = "client-id-abc";
    private final String name = "Test Client";
    private final String description = "Description";
    private final List<String> redirectUris = Arrays.asList("http://localhost/callback1", "http://localhost/callback2");
    private final String clientSecret = "secret-xyz";
    private final boolean publicClient = true;
    private final boolean enabled = false;

    /**
     * Test for the default constructor.
     */
    @Test
    @DisplayName("IdpClientInfo - Default Constructor")
    void testDefaultConstructor() {
        IdpClientInfo dto = new IdpClientInfo();
        assertNotNull(dto);
        assertNull(dto.getUuid());
        assertNull(dto.getClientId());
        assertNull(dto.getName());
        assertNull(dto.getDescription());
        assertNull(dto.getRedirectUris());
        assertNull(dto.getClientSecret());
        assertFalse(dto.isPublicClient());
        assertFalse(dto.isEnabled());
    }

    /**
     * Test for the all args constructor.
     */
    @Test
    @DisplayName("IdpClientInfo - All Args Constructor")
    void testAllArgsConstructor() {
        IdpClientInfo dto = new IdpClientInfo(uuid, clientId, name, description, redirectUris, clientSecret, publicClient, enabled);
        assertEquals(uuid, dto.getUuid());
        assertEquals(clientId, dto.getClientId());
        assertEquals(name, dto.getName());
        assertEquals(description, dto.getDescription());
        assertEquals(redirectUris, dto.getRedirectUris());
        assertEquals(clientSecret, dto.getClientSecret());
        assertEquals(publicClient, dto.isPublicClient());
        assertEquals(enabled, dto.isEnabled());
    }

    /**
     * Test for the setters and getters.
     */
    @Test
    @DisplayName("IdpClientInfo - Setter and Getter Test")
    void testSettersAndGetters() {
        IdpClientInfo dto = new IdpClientInfo();
        dto.setUuid(uuid);
        dto.setClientId(clientId);
        dto.setName(name);
        dto.setDescription(description);
        dto.setRedirectUris(redirectUris);
        dto.setClientSecret(clientSecret);
        dto.setPublicClient(publicClient);
        dto.setEnabled(enabled);

        assertEquals(uuid, dto.getUuid());
        assertEquals(clientId, dto.getClientId());
        assertEquals(name, dto.getName());
        assertEquals(description, dto.getDescription());
        assertEquals(redirectUris, dto.getRedirectUris());
        assertEquals(clientSecret, dto.getClientSecret());
        assertEquals(publicClient, dto.isPublicClient());
        assertEquals(enabled, dto.isEnabled());
    }

    /**
     * Test for the toString method.
     */
    @Test
    @DisplayName("IdpClientInfo - toString Test")
    void testToString() {
        IdpClientInfo dto = new IdpClientInfo(uuid, clientId, name, description, redirectUris, clientSecret, publicClient, enabled);
        String result = dto.toString();
        assertTrue(result.contains(uuid), "toString should include uuid");
        assertTrue(result.contains(clientId), "toString should include clientId");
        assertTrue(result.contains(name), "toString should include name");
        assertTrue(result.contains(description), "toString should include description");
        assertTrue(result.contains(clientSecret), "toString should include clientSecret");
        assertTrue(result.contains(String.valueOf(publicClient)), "toString should include publicClient");
        assertTrue(result.contains(String.valueOf(enabled)), "toString should include enabled");
    }

    /**
     * Test for equals and hashCode methods.
     */
    @Test
    @DisplayName("IdpClientInfo - Equals and HashCode Test")
    void testEqualsAndHashCode() {
        IdpClientInfo dtoBase = new IdpClientInfo(uuid, clientId, name, description, redirectUris, clientSecret, publicClient, enabled);
        IdpClientInfo dtoEquals = new IdpClientInfo(uuid, clientId, name, description, redirectUris, clientSecret, publicClient, enabled);
        IdpClientInfo dtoNotEquals = new IdpClientInfo("diff", clientId, name, description, redirectUris, clientSecret, publicClient, enabled);

        assertEquals(dtoBase, dtoEquals, "Objects with the same values should be equal.");
        assertNotEquals(dtoBase, dtoNotEquals, "Objects with different values should not be equal.");
        assertEquals(dtoBase.hashCode(), dtoEquals.hashCode(), "Objects with the same values should have the same hash code.");
        assertNotEquals(dtoBase.hashCode(), dtoNotEquals.hashCode(), "Objects with different values should have different hash codes.");
    }
}