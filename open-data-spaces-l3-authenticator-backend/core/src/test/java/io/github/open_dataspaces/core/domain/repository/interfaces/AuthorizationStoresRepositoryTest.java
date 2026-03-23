/*
 * AuthorizationStoresRepositoryTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for AuthorizationStoresEntity.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.domain.repository.interfaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import io.github.open_dataspaces.core.common.consts.ConstSqlQueries;
import io.github.open_dataspaces.core.domain.entities.AuthorizationStoresEntity;

/**
 * Unit tests for the {@link AuthorizationStoresRepository} class.
 */
@SpringBootTest
public class AuthorizationStoresRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AuthorizationStoresRepository authorizationStoresRepository;

    // Common input parameters
    private final String commonAuthorizationStoreId = "auth-store-123";
    private final String commonAuthorizationStoreName = "auth-store-name-123";
    private final String commonAuthorizationStoreEnvironmentName = "env-name-123";
    private final String commonAuthorizationStoreIdpRealm = "idp-realm-123";
    private final boolean commonDeletedFlag = false;
    private final String commonCreatedUserId = "Created user";
    private final String commonUpdatedUserId = "Updated user";
    private final String commonAuthorizationStorePurpose = "API_EXECUTION";

    @AfterEach
    void cleanUp() {
        // Clean up test data from both tables
        deleteTestDataTblAuthorizationStore();
    }

    /**
     * Insert test data into tbl_authorization_stores.
     */
    private void insertTestDataTblAuthorizationStore() {
        jdbcTemplate.execute("INSERT INTO "
                + ConstSqlQueries.SCHEMA_AUTH + "." + ConstSqlQueries.TABLE_AUTHORIZATION_STORES
                + " ("
                + ConstSqlQueries.COLUMN_AUTHORIZATION_STORES_ID + ", "
                + ConstSqlQueries.COLUMN_AUTHORIZATION_STORES_NAME + ", "
                + ConstSqlQueries.COLUMN_AUTHORIZATION_STORES_PURPOSE + ", "
                + ConstSqlQueries.COLUMN_AUTHORIZATION_STORES_ENVIRONMENT_NAME + ", "
                + ConstSqlQueries.COLUMN_AUTHORIZATION_STORES_IDP_REALM + ", "
                + ConstSqlQueries.COLUMN_COMMON_DELETED_FLAG + ", "
                + ConstSqlQueries.COLUMN_COMMON_EFFECTIVE_START_DATE + ", "
                + ConstSqlQueries.COLUMN_COMMON_EFFECTIVE_END_DATE + ", "
                + ConstSqlQueries.COLUMN_COMMON_CREATED_AT + ", "
                + ConstSqlQueries.COLUMN_COMMON_CREATED_USER_ID + ", "
                + ConstSqlQueries.COLUMN_COMMON_UPDATED_AT + ", "
                + ConstSqlQueries.COLUMN_COMMON_UPDATED_USER_ID
                + ") VALUES ("
                + "'" + commonAuthorizationStoreId + "',"
                + "'" + commonAuthorizationStoreName + "',"
                + "'" + commonAuthorizationStorePurpose + "',"
                + "'" + commonAuthorizationStoreEnvironmentName + "',"
                + "'" + commonAuthorizationStoreIdpRealm + "',"
                + "'" + commonDeletedFlag + "',"
                + "now(),"
                + "now(),"
                + "now(),"
                + "'" + commonCreatedUserId + "',"
                + "now(),"
                + "'" + commonUpdatedUserId + "'"
                + ");");
    }

    /**
     * Delete test data from tbl_authorization_stores.
     */
    private void deleteTestDataTblAuthorizationStore() {
        jdbcTemplate.execute("DELETE FROM "
                + ConstSqlQueries.SCHEMA_AUTH + "." + ConstSqlQueries.TABLE_AUTHORIZATION_STORES
                + " WHERE "
                + ConstSqlQueries.COLUMN_AUTHORIZATION_STORES_ID + " = '" + commonAuthorizationStoreId + "';");
    }

    /**
     * Test for retrieving an active operator by specification.
     */
    @Test
    @DisplayName("findByEnvironmentNameAndIdpRealmAndPdpStorePurpose - success")
    void testFindByEnvironmentNameAndIdpRealmAndPdpStorePurpose_success() {

        // set up prerequisite data
        insertTestDataTblAuthorizationStore();

        // Act
        Optional<AuthorizationStoresEntity> result = authorizationStoresRepository.findByEnvironmentNameAndIdpRealmAndPdpStorePurpose(
                commonAuthorizationStoreEnvironmentName,
                commonAuthorizationStoreIdpRealm,
                commonAuthorizationStorePurpose);
        assertTrue(result.isPresent(), "Active authorization store should be returned.");
        AuthorizationStoresEntity authorizationStoresEntity = result.get();

        assertEquals(commonAuthorizationStoreId, authorizationStoresEntity.getPdpStoreId(), "pdpStoreId mismatch");
        assertEquals(commonAuthorizationStoreName, authorizationStoresEntity.getPdpStoreName(), "pdpStoreName mismatch");
        assertEquals(commonAuthorizationStorePurpose, authorizationStoresEntity.getPdpStorePurpose(), "pdpStorePurpose mismatch");
        assertEquals(commonAuthorizationStoreEnvironmentName, authorizationStoresEntity.getEnvironmentName(), "environmentName mismatch");
        assertEquals(commonAuthorizationStoreIdpRealm, authorizationStoresEntity.getIdpRealm(), "idpRealm mismatch");

        assertEquals(commonDeletedFlag, authorizationStoresEntity.isDeletedFlag(), "deletedFlag mismatch");
        assertNotNull(authorizationStoresEntity.getEffectiveStartDate(), "effectiveStartDate should not be null");
        assertNotNull(authorizationStoresEntity.getEffectiveEndDate(), "effectiveEndDate should not be null");
        assertNotNull(authorizationStoresEntity.getCreatedAt(), "createdAt should not be null");
        assertEquals(commonCreatedUserId, authorizationStoresEntity.getCreatedUserId(), "createdUserId mismatch");
        assertNotNull(authorizationStoresEntity.getUpdatedAt(), "updatedAt should not be null");
        assertEquals(commonUpdatedUserId, authorizationStoresEntity.getUpdatedUserId(), "updatedUserId mismatch");
    }

    /**
     * Test for retrieving an active operator by specification.
     */
    @Test
    @DisplayName("findByEnvironmentNameAndIdpRealmAndPdpStorePurpose - Not Found")
    void testFindByEnvironmentNameAndIdpRealmAndPdpStorePurpose_successNotFound() {

        // Act
        Optional<AuthorizationStoresEntity> result = authorizationStoresRepository.findByEnvironmentNameAndIdpRealmAndPdpStorePurpose(
                commonAuthorizationStoreEnvironmentName,
                commonAuthorizationStoreIdpRealm,
                commonAuthorizationStorePurpose);
        assertTrue(result.isEmpty(), "Active authorization store should not be found.");
    }
}
