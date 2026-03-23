/*
 * ClientSecretEntityTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for the ClientSecretEntity class.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.github.open_dataspaces.core.common.consts.ConstSqlQueries;

/**
 * Unit tests for ClientSecretEntity.
 */
public class ClientSecretEntityTest {

    private final String commonClientUuid = "test-client-uuid";
    private final String commonApiKey = "test-api-key";
    private final String commonCreatedUserId = "test-user-id";
    private final LocalDateTime commonCreatedAt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

    @Test
    @DisplayName("Default Constructor")
    void testDefaultConstructor() {
        ClientSecretEntity entity = new ClientSecretEntity();

        // Assert that the object is created successfully
        assertNotNull(entity);
        assertNull(entity.getClientUuid());
        assertNull(entity.getApiKey());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getCreatedUserId());
    }

    @Test
    @DisplayName("Constructor with parameters")
    void testArgsConstructor() {
        ClientSecretEntity entity = new ClientSecretEntity(commonClientUuid, commonApiKey, commonCreatedUserId);

        // Assert that the fields are set correctly
        assertNotNull(entity);
        assertEquals(entity.getClientUuid(), commonClientUuid);
        assertEquals(entity.getApiKey(), commonApiKey);
        assertEquals(entity.getCreatedUserId(), commonCreatedUserId);
        assertNull(entity.getCreatedAt());
    }

    @Test
    @DisplayName("Getter and Setter methods")
    void testSettersAndGetters() {
        ClientSecretEntity entity = new ClientSecretEntity();
        entity.setClientUuid(commonClientUuid);
        entity.setApiKey(commonApiKey);
        entity.setCreatedUserId(commonCreatedUserId);
        entity.setCreatedAt(commonCreatedAt);

        // Assert that the fields are set correctly
        assertEquals(entity.getClientUuid(), commonClientUuid);
        assertEquals(entity.getApiKey(), commonApiKey);
        assertEquals(entity.getCreatedUserId(), commonCreatedUserId);
        assertEquals(entity.getCreatedAt(), commonCreatedAt);
    }

    @Test
    @DisplayName("toString method")
    void testToString() {
        ClientSecretEntity entity = new ClientSecretEntity();
        entity.setClientUuid(commonClientUuid);
        entity.setApiKey(commonApiKey);
        entity.setCreatedUserId(commonCreatedUserId);
        entity.setCreatedAt(commonCreatedAt);

        String toStringOutput = entity.toString();

        // Assert that toString contains all field values
        assertNotNull(toStringOutput);
        assertTrue(toStringOutput.contains(commonClientUuid));
        assertTrue(toStringOutput.contains(commonApiKey));
        assertTrue(toStringOutput.contains(commonCreatedUserId));
        assertTrue(toStringOutput.contains(commonCreatedAt.toString()));
    }

    @Test
    @DisplayName("Equals and HashCode methods")
    void testEqualsAndHashCode() {
        ClientSecretEntity entity1 = new ClientSecretEntity(commonClientUuid, commonApiKey, commonCreatedUserId);
        ClientSecretEntity entity2 = new ClientSecretEntity(commonClientUuid, commonApiKey, commonCreatedUserId);
        ClientSecretEntity entity3 = new ClientSecretEntity(commonClientUuid + "-diff", commonApiKey + "-diff", commonCreatedUserId + "-diff");

        // Assert that two entities with the same values are equal
        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
        // Assert that entities with different values are not equal
        assertNotEquals(entity1, entity3);
        assertNotEquals(entity1.hashCode(), entity3.hashCode());
    }

    @Test
    @DisplayName("@CreationTimestamp annotation on createdAt")
    void testCreationTimestampAnnotation() throws NoSuchFieldException {
        var field = ClientSecretEntity.class.getDeclaredField("createdAt");
        var annotation = field.getAnnotation(org.hibernate.annotations.CreationTimestamp.class);
        assertNotNull(annotation);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "clientUuid, " + ConstSqlQueries.COLUMN_CLIENT_SECRETS_CLIENT_UUID,
        "apiKey, " + ConstSqlQueries.COLUMN_APIKEYS_APIKEY,
        "createdAt, " + ConstSqlQueries.COLUMN_COMMON_CREATED_AT,
        "createdUserId, " + ConstSqlQueries.COLUMN_COMMON_CREATED_USER_ID
    })
    @DisplayName("@Column annotation")
    void testColumnAnnotations(String propertyName, String columnName) throws NoSuchFieldException {
        var field = ClientSecretEntity.class.getDeclaredField(propertyName);
        var annotation = field.getAnnotation(jakarta.persistence.Column.class);
        assertNotNull(annotation);
        assertEquals(columnName, annotation.name());
    }
}
