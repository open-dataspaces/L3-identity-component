/*
 * APIKeysEntityTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This file is part of the user authentication backend system.
 *
 * APIKeysEntityTest provides unit tests for the APIKeysEntity JPA entity.
 * <p>
 * These tests verify that API key entities can be retrieved from the repository,
 * and that getter/setter methods work as expected.
 * <p>
 * Uses Spring Boot's test context and the local profile for configuration.
 *
 * @author btmurayamakohei
 * @since 2025/06/11
 */

package io.github.open_dataspaces.core.domain.entities;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import io.github.open_dataspaces.core.common.consts.ConstSqlQueries;
import io.github.open_dataspaces.core.domain.repository.interfaces.APIKeysRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Unit tests for the APIKeysEntity class.
 *
 * <p>This test class verifies the correct behavior of getter and setter methods,
 * as well as the ability to retrieve API key entities from the repository.
 * </p>
 *
 * @author btmurayamakohei
 */
@SpringBootTest
public class APIKeysEntityTest {

    @Autowired
    APIKeysRepository repository;

    /**
     * Test No.1
     * Tests that the setter methods of APIKeysEntity correctly set the property values,
     * and the getter methods return those values.
     */
    @Test
    void success_setter_getter() {
        String id = "test-id-12345";
        String apiKey = "test-api-key-67890";
        String applicationName = "test-application-name";
        var entity = new APIKeysEntity();
        entity.setId(id);
        entity.setApiKey(apiKey);
        entity.setApplicationName(applicationName);

        assertEquals(id, entity.getId());
        assertEquals(apiKey, entity.getApiKey());
        assertEquals(applicationName, entity.getApplicationName());
    }

    /**
     * Test No.2
     * Tests retrieving values from the api_keys table in the database.
     */
    @Sql(statements = {
            "insert into " + ConstSqlQueries.SCHEMA_AUTH + "." + ConstSqlQueries.TABLE_APIKEYS
                + " ("
                + "id, "
                + "api_key, "
                + "application_name, "
                + "idp_realm, "
                + "usecase, "
                + "deleted_flag, "
                + "effective_start_date, "
                + "effective_end_date "
                + ")values("
                + "'APIKeysEntityTest#success_findByApiKey-id', "
                + "'api-key-test-123', "
                + "'APIKeysEntityTest#success_findByApiKey', "
                + "'idp-realm-123', "
                + "'usecase-123', "
                + "false, "
                + "'2000-01-01 00:00:00', "
                + "'2999-12-31 00:00:00' "
                + ");"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
            "delete from " + ConstSqlQueries.SCHEMA_AUTH + "." + ConstSqlQueries.TABLE_APIKEYS
                + " where "
                + "id = 'APIKeysEntityTest#success_findByApiKey-id';"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    void success_findByApiKey() {
        String id = "APIKeysEntityTest#success_findByApiKey-id";
        String apiKey = "api-key-test-123";
        String applicationName = "APIKeysEntityTest#success_findByApiKey";
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);

        APIKeysEntity entity = repository.findByApiKey(apiKey, currentDate).stream().findFirst().orElse(null);
        assertNotNull(entity, "Entity should not be null");
        assertEquals(id, entity.getId());
        assertEquals(apiKey, entity.getApiKey());
        assertEquals(applicationName, entity.getApplicationName());
    }

    /**
    * Test No.4
    * Tests that findByApiKey returns an empty list when no matching apiKey exists.
    */
    @Test
    void testFindByApiKey_noResult() {
        String nonExistentApiKey = "not-exist-api-key";
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);

        List<APIKeysEntity> result = repository.findByApiKey(nonExistentApiKey, currentDate);
        assertNotNull(result, "Result list should not be null");
        assertTrue(result.isEmpty(), "Result list should be empty when no matching apiKey exists");
    }

    /**
    * Test No.5
    * Tests that findByApiKey returns an empty list when no matching apiKey exists.
    */
    @Test
    void testFindByApiKey_emptyString() {
        String nonExistentApiKey = "";
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);

        List<APIKeysEntity> result = repository.findByApiKey(nonExistentApiKey, currentDate);
        assertNotNull(result, "Result list should not be null");
        assertTrue(result.isEmpty(), "Result list should be empty when no matching apiKey exists");
    }

    /**
    * Test No.8
    * Tests that findByApiKey returns an empty list when no matching apiKey exists.
    */
    @Test
    void testFindByApiKey_noMatch() {
        String nonExistentApiKey = "z2e6e2e2-2c7a-4e0a-8e7b-7e2c9f6a1d3b";
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);

        List<APIKeysEntity> result = repository.findByApiKey(nonExistentApiKey, currentDate);
        assertNotNull(result, "Result list should not be null");
        assertTrue(result.isEmpty(), "Result list should be empty when no matching apiKey exists");
    }

}
