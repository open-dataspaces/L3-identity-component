/*
 * AuthorizationStoresEntityTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for the AuthorizationStoresEntity class.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.github.open_dataspaces.core.common.consts.ConstSqlQueries;

import jakarta.persistence.Column;

/**
 * Unit tests for AuthorizationStoresEntity.
 */
public class AuthorizationStoresEntityTest {

    // Common test data
    private final String commonPdpStoreId = "PDPStoreId123";
    private final String commonPdpStoreName = "PDP Store Name";
    private final String commonPdpStorePurpose = "API_EXECUTION";
    private final String commonEnvironmentName = "EnvName123";
    private final String commonIdpRealm = "IDPRealm123";

    private final LocalDate commonEffectiveStartDate = LocalDate.now();
    private final LocalDate commonEffectiveEndDate = LocalDate.now().plusDays(1);
    private final boolean commonDeletedFlag = false;
    private final LocalDateTime commonCreatedAt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    private final String commonCreatedUserId = "CreatedUserId";
    private final LocalDateTime commonUpdatedAt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    private final String commonUpdatedUserId = "UpdatedUserId";

    @Test
    @DisplayName("Default Constructor")
    void testDefaultConstructor() {
        AuthorizationStoresEntity entity = new AuthorizationStoresEntity();

        // Assert that the object is created successfully
        assertNotNull(entity);
        assertNull(entity.getPdpStoreId());
        assertNull(entity.getPdpStoreName());
        assertNull(entity.getPdpStorePurpose());
        assertNull(entity.getEnvironmentName());
        assertNull(entity.getIdpRealm());

        assertFalse(entity.isDeletedFlag());
        assertNull(entity.getEffectiveStartDate());
        assertNull(entity.getEffectiveEndDate());
        assertNull(entity.getCreatedUserId());
        assertNull(entity.getUpdatedUserId());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    @DisplayName("Setter and Getter Test")
    void testSettersAndGetters() {
        // Arrange
        AuthorizationStoresEntity entity = new AuthorizationStoresEntity();

        // Act
        entity.setPdpStoreId(commonPdpStoreId);
        entity.setPdpStoreName(commonPdpStoreName);
        entity.setPdpStorePurpose(commonPdpStorePurpose);
        entity.setEnvironmentName(commonEnvironmentName);
        entity.setIdpRealm(commonIdpRealm);

        // Assert
        assertEquals(entity.getPdpStoreId(), commonPdpStoreId);
        assertEquals(entity.getPdpStoreName(), commonPdpStoreName);
        assertEquals(entity.getPdpStorePurpose(), commonPdpStorePurpose);
        assertEquals(entity.getIdpRealm(), commonIdpRealm);
    }

    @Test
    @DisplayName("toString Test")
    void testToString() {
        // Arrange
        AuthorizationStoresEntity entity = new AuthorizationStoresEntity();
        entity.setPdpStoreId(commonPdpStoreId);
        entity.setPdpStoreName(commonPdpStoreName);
        entity.setPdpStorePurpose(commonPdpStorePurpose);
        entity.setEnvironmentName(commonEnvironmentName);
        entity.setIdpRealm(commonIdpRealm);

        entity.setDeletedFlag(commonDeletedFlag);
        entity.setEffectiveStartDate(commonEffectiveStartDate);
        entity.setEffectiveEndDate(commonEffectiveEndDate);
        entity.setCreatedAt(commonCreatedAt);
        entity.setCreatedUserId(commonCreatedUserId);
        entity.setUpdatedAt(commonUpdatedAt);
        entity.setUpdatedUserId(commonUpdatedUserId);

        // Act
        String result = entity.toString();

        // Assert
        assertTrue(result.contains(commonPdpStoreId), "toString should include pdpStoreId");
        assertTrue(result.contains(commonPdpStoreName), "toString should include pdpStoreName");
        assertTrue(result.contains(commonPdpStorePurpose), "toString should include pdpStorePurpose");
        assertTrue(result.contains(commonEnvironmentName), "toString should include environmentName");
        assertTrue(result.contains(commonIdpRealm), "toString should include idpRealm");

        assertTrue(result.contains(String.valueOf(commonDeletedFlag)), "toString should include deletedFlag");
        assertTrue(result.contains(commonEffectiveStartDate.toString()), "toString should include effectiveStartDate");
        assertTrue(result.contains(commonEffectiveEndDate.toString()), "toString should include effectiveEndDate");
        assertTrue(result.contains(commonCreatedAt.toString()), "toString should include createdAt");
        assertTrue(result.contains(commonCreatedUserId), "toString should include createdUserId");
        assertTrue(result.contains(commonUpdatedAt.toString()), "toString should include updatedAt");
        assertTrue(result.contains(commonUpdatedUserId), "toString should include updatedUserId");
    }

    @Test
    @DisplayName("Equals and HashCode")
    void testEqualsAndHashCode() {
        // Arrange
        AuthorizationStoresEntity entity = new AuthorizationStoresEntity();
        entity.setPdpStoreId(commonPdpStoreId);
        entity.setPdpStoreName(commonPdpStoreName);
        entity.setPdpStorePurpose(commonPdpStorePurpose);
        entity.setEnvironmentName(commonEnvironmentName);
        entity.setIdpRealm(commonIdpRealm);

        AuthorizationStoresEntity entityEquales = new AuthorizationStoresEntity();
        entityEquales.setPdpStoreId(commonPdpStoreId);
        entityEquales.setPdpStoreName(commonPdpStoreName);
        entityEquales.setPdpStorePurpose(commonPdpStorePurpose);
        entityEquales.setEnvironmentName(commonEnvironmentName);
        entityEquales.setIdpRealm(commonIdpRealm);

        AuthorizationStoresEntity entityDiff = new AuthorizationStoresEntity();
        entityDiff.setPdpStoreId(commonPdpStoreId + "diff");
        entityDiff.setPdpStoreName(commonPdpStoreName + "diff");
        entityDiff.setPdpStorePurpose(commonPdpStorePurpose + "diff");
        entityDiff.setEnvironmentName(commonEnvironmentName + "diff");
        entityDiff.setIdpRealm(commonIdpRealm + "diff");

        // Act & Assert
        assertEquals(entity, entityEquales);
        assertNotEquals(entity, entityDiff);
        assertEquals(entity.hashCode(), entityEquales.hashCode());
        assertNotEquals(entity.hashCode(), entityDiff.hashCode());
    }

    @ParameterizedTest
    @CsvSource(value = {
        "pdpStoreId, " + ConstSqlQueries.COLUMN_AUTHORIZATION_STORES_ID,
        "pdpStoreName, " + ConstSqlQueries.COLUMN_AUTHORIZATION_STORES_NAME,
        "pdpStorePurpose, " + ConstSqlQueries.COLUMN_AUTHORIZATION_STORES_PURPOSE,
        "environmentName, " + ConstSqlQueries.COLUMN_AUTHORIZATION_STORES_ENVIRONMENT_NAME,
        "idpRealm, " + ConstSqlQueries.COLUMN_AUTHORIZATION_STORES_IDP_REALM,
    })
    @DisplayName("@Column Annotation Test")
    void testColumnAnnotations(String fieldName, String columnName) throws Exception {
        Field field = AuthorizationStoresEntity.class.getDeclaredField(fieldName);
        Column annotation = field.getAnnotation(Column.class);
        assertNotNull(annotation);
        assertEquals(columnName, annotation.name());
    }

}
