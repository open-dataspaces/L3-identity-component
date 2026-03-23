/*
 * CidrsEntityTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This file is part of the user authentication backend system.
 *
 * CidrsEntityTest provides unit tests for the CidrsEntity JPA entity.
 * <p>
 * These tests verify that CIDR entities' getter and setter methods work as expected.
 * <p>
 * Uses only plain JUnit assertions (no Spring context required).
 *
 * @author btmurayamakohei
 * @since 2025/06/11
 */

package io.github.open_dataspaces.core.domain.entities;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;

import io.github.open_dataspaces.core.common.consts.ConstSqlQueries;
import io.github.open_dataspaces.core.domain.repository.interfaces.CidrsRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Unit tests for the CidrsEntity class.
 *
 * <p>This test class verifies the correct behavior of the CidrsEntity JPA entity,
 * including its getter and setter methods, as well as the ability to retrieve
 * CidrsEntity objects from the repository.
 * </p>
 *
 * @author btmurayamakohei
 */
@SpringBootTest
public class CidrsEntityTest {
    @Autowired
    CidrsRepository repository;

    /**
     * Test No.1
     * Tests that the setter methods of APIKeysEntity correctly set the property values,
     * and the getter methods return those values.
     */
    @Test
    void success_setter_getter() {
        String ip = "0.0.0.0/0";
        String apiKey = "e387cf3a-4584-4352-8ed3-50df0f4045bd";
        var entity = new CidrsEntity();
        entity.setCidr(ip);
        entity.setApiKey(apiKey);

        assertEquals(apiKey, entity.getApiKey());
        assertEquals(ip, entity.getCidr());
    }

    /**
     * Test No.2
     * Tests that an APIKeysEntity can be retrieved from the repository
     * and its properties match the expected values.
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
                + ");",
            "insert into " + ConstSqlQueries.SCHEMA_AUTH + "." + ConstSqlQueries.TABLE_CIDRS
                + " ("
                + "cidr, "
                + "api_key, "
                + "deleted_flag, "
                + "effective_start_date, "
                + "effective_end_date "
                + ")values("
                + "'0.0.0.0/0', "
                + "'api-key-test-123', "
                + "false, "
                + "'2000-01-01 00:00:00', "
                + "'2999-12-31 00:00:00' "
                + ");"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
            "delete from " + ConstSqlQueries.SCHEMA_AUTH + "." + ConstSqlQueries.TABLE_CIDRS
                + " where "
                + "cidr = '0.0.0.0/0' "
                + "and api_key = 'api-key-test-123';",
            "delete from " + ConstSqlQueries.SCHEMA_AUTH + "." + ConstSqlQueries.TABLE_APIKEYS
                + " where "
                + "id = 'APIKeysEntityTest#success_findByApiKey-id';"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    void success_findFromCidrsTable() {
        String ip = "0.0.0.0/0";
        String apiKey = "api-key-test-123";
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);

        var entity = repository.findCidrs(apiKey, ip, currentDate).stream().findFirst().orElse(null);

        assertNotNull(entity, "Entity should not be null");
        assertEquals(apiKey, entity.getApiKey());
        assertEquals(ip, entity.getCidr());
    }

    /**
    * Test No.3
    * Tests that findByApiKey returns an empty list when no matching apiKey exists.
    */
    @Test
    void testFindCidrs_noResult() {
        String ip = "0.0.0.0/0";
        String nonExistentApiKey = "not-exist-api-key";
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);

        List<CidrsEntity> result = repository.findCidrs(nonExistentApiKey, ip, currentDate);

        assertNotNull(result, "Result list should not be null");
        assertTrue(result.isEmpty(), "Result list should be empty when no matching apiKey exists");
    }

    /**
    * Test No.4
    * Tests that FindCidrs returns an empty list when no matching apiKey exists.
    */
    @Test
    void testFindCidrs_emptyString_shouldThrowException() {
        String ip = "";
        String nonExistentApiKey = "";
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);

        assertThrows(DataIntegrityViolationException.class, () -> {
            repository.findCidrs(nonExistentApiKey, ip, currentDate);
        }, "Should throw exception when empty string is passed for inet type");
    }

    /**
    * Test No.5
    * Tests that FindCidrs returns an empty list when no matching apiKey exists.
    */
    @Test
    void testFindCidrs_noMatch() {
        String ip = "0.0.0.0/0";
        String nonExistentApiKey = "z2e6e2e2-2c7a-4e0a-8e7b-7e2c9f6a1d3b";
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);

        List<CidrsEntity> result = repository.findCidrs(nonExistentApiKey, ip, currentDate);
        assertNotNull(result, "Result list should not be null");
        assertTrue(result.isEmpty(), "Result list should be empty when no matching apiKey exists");
    }
}
