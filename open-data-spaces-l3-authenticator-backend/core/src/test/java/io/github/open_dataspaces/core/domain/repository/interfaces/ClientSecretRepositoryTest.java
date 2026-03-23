/*
 * ClientSecretRepositoryTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for ClientSecretRepository.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.domain.repository.interfaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import io.github.open_dataspaces.core.common.consts.ConstSqlQueries;
import io.github.open_dataspaces.core.domain.entities.ClientSecretEntity;

import jakarta.transaction.Transactional;

/**
 * Unit tests for the ClientSecretRepository class.
 */
@SpringBootTest
public class ClientSecretRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ClientSecretRepository clientSecretRepository;

    // Common input parameters
    private final String commonClientUuid = "client-uuid-123";
    private final String commonCreatedUserId = "Created user";
    private final String commonId = "APIKeysEntityTest#success_findByApiKey-id";
    private final String commonApiKey = "api-key-test-4-clients-123";
    private final String commonApplicationName = "APIKeysEntityTest#success_findByApiKey";
    private final String commonRealm = "idp-realm-123";
    private final String commonUsecase = "usecase-123";

    @AfterEach
    void cleanUp() {
        // Clean up test data from both tables
        deleteTestDataTbl();
    }

    /**
     * Insert test data into tbl_client_secrets.
     */
    private void insertTestDataTbl() {
        jdbcTemplate.execute("INSERT INTO "
                + ConstSqlQueries.SCHEMA_AUTH + "." + ConstSqlQueries.TABLE_APIKEYS
                + " ("
                + ConstSqlQueries.COLUMN_APIKEYS_ID + ", "
                + ConstSqlQueries.COLUMN_APIKEYS_APIKEY + ", "
                + ConstSqlQueries.COLUMN_APIKEYS_APPLICATION_NAME + ", "
                + ConstSqlQueries.COLUMN_APIKEYS_IDP_REALM + ", "
                + ConstSqlQueries.COLUMN_APIKEYS_USECASE + ", "
                + ConstSqlQueries.COLUMN_COMMON_DELETED_FLAG + ", "
                + ConstSqlQueries.COLUMN_COMMON_EFFECTIVE_START_DATE + ", "
                + ConstSqlQueries.COLUMN_COMMON_EFFECTIVE_END_DATE + ", "
                + ConstSqlQueries.COLUMN_COMMON_CREATED_USER_ID + ","
                + ConstSqlQueries.COLUMN_COMMON_CREATED_AT
                + ") VALUES ("
                + "'" + commonId + "', "
                + "'" + commonApiKey + "', "
                + "'" + commonApplicationName + "', "
                + "'" + commonRealm + "', "
                + "'" + commonUsecase + "', "
                + "false, "
                + "'2000-01-01 00:00:00', "
                + "'2999-12-31 00:00:00', "
                + "'" + commonCreatedUserId + "', "
                + "now());");
        jdbcTemplate.execute("INSERT INTO "
                + ConstSqlQueries.SCHEMA_AUTH + "." + ConstSqlQueries.TABLE_CLIENT_SECRETS
                + " ("
                + ConstSqlQueries.COLUMN_CLIENT_SECRETS_CLIENT_UUID + ", "
                + ConstSqlQueries.COLUMN_APIKEYS_APIKEY + ", "
                + ConstSqlQueries.COLUMN_COMMON_CREATED_AT + ", "
                + ConstSqlQueries.COLUMN_COMMON_CREATED_USER_ID
                + ") VALUES ("
                + "'" + commonClientUuid + "',"
                + "'" + commonApiKey + "',"
                + "now(),"
                + "'" + commonCreatedUserId + "'"
                + ");");
    }

    /**
     * Delete test data from tbl_client_secrets.
     */
    private void deleteTestDataTbl() {
        jdbcTemplate.execute("DELETE FROM "
                + ConstSqlQueries.SCHEMA_AUTH + "." + ConstSqlQueries.TABLE_CLIENT_SECRETS
                + " WHERE "
                + ConstSqlQueries.COLUMN_CLIENT_SECRETS_CLIENT_UUID + " = '" + commonClientUuid + "';");
        jdbcTemplate.execute("DELETE FROM "
                + ConstSqlQueries.SCHEMA_AUTH + "." + ConstSqlQueries.TABLE_APIKEYS
                + " WHERE "
                + ConstSqlQueries.COLUMN_APIKEYS_ID + " = '" + commonId + "';");
    }

    @Test
    @DisplayName("Test existsByClientUuid returns true when record exists")
    public void testExistsByClientUuid_recordExists() {
        // Arrange
        insertTestDataTbl();

        // Act
        boolean exists = clientSecretRepository.existsByClientUuid(commonClientUuid);

        // Assert
        assertTrue(exists);
    }

    @Test
    @DisplayName("Test existsByClientUuid returns false when record does not exist")
    public void testExistsByClientUuid_recordDoesNotExist() {
        // Act
        boolean exists = clientSecretRepository.existsByClientUuid(commonClientUuid);

        // Assert
        assertFalse(exists);
    }

    @Test
    @Transactional
    @DisplayName("Test deleteByClientUuid deletes the record")
    public void testDeleteByClientUuid_deletesRecord() {
        // Arrange
        insertTestDataTbl();

        // Act
        clientSecretRepository.deleteByClientUuid(commonClientUuid);

        // Assert
        boolean exists = clientSecretRepository.existsByClientUuid(commonClientUuid);
        assertFalse(exists);
    }

    @Test
    @DisplayName("ClientSecretEntity - save Test")
    void testSave_success() {
        // set up prerequisite data
        insertTestDataTbl();
        // Act
        clientSecretRepository.save(commonClientUuid, commonApiKey, commonCreatedUserId);

        // Assert
        Optional<ClientSecretEntity> result = clientSecretRepository.findById(commonClientUuid);
        assertTrue(result.isPresent(), "The saved ClientSecretEntity should be retrievable.");
        ClientSecretEntity entity = result.get();
        assertEquals(commonClientUuid, entity.getClientUuid(), "clientUuid should match.");
        assertEquals(commonApiKey, entity.getApiKey(), "apiKey should match.");
        assertEquals(commonCreatedUserId, entity.getCreatedUserId(), "createdUserId should match.");
    }
}
