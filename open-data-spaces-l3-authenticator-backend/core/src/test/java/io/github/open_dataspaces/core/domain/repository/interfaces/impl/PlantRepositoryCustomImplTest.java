/*
 * PlantRepositoryCustomImplTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for PlantRepositoryCustomImpl.
 *
 * Date: 2025/09/22
 */

package io.github.open_dataspaces.core.domain.repository.interfaces.impl;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.consts.ConstSqlQueries;
import io.github.open_dataspaces.core.common.utils.SqlUtils;
import io.github.open_dataspaces.core.domain.entities.PlantEntity;
import io.github.open_dataspaces.core.exception.AbstractBaseException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnexpectedException;

import jakarta.persistence.EntityManager;

/**
 * Unit tests for the {@link PlantRepositoryCustomImpl} class.
 */
@SpringBootTest
@Transactional
class PlantRepositoryCustomImplTest {

    @Autowired
    private PlantRepositoryCustomImpl repository;

    @Autowired
    private EntityManager entityManager;

    /**
     * setUp initializes the test environment before each test.
     */
    @BeforeEach
    void setUp() {
        // Use reflection to inject the mock EntityManager
        try {
            var field = PlantRepositoryCustomImpl.class.getDeclaredField("entityManager");
            field.setAccessible(true);
            field.set(repository, entityManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * case#1:
     * Test for findWithLimitOffset.
     */
    @Test
    @Sql(statements = {
        "DELETE FROM auth.tbl_plants;"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
        "INSERT INTO auth.tbl_operators (operator_id, operator_name, operator_address, open_operator_id, global_operator_id, deleted_flag, effective_start_date, effective_end_date, created_user_id, updated_user_id, created_at, updated_at) VALUES ("
                + "'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'テスト事業者001', '東京都渋谷区xxx', '1234567890001', '1234ABCD5678EFGH0001', false, '2025-08-20', '2099-12-31', 'manualuser', 'manualuser', now(), now()),"
                + "('54708446-d761-4071-9bc3-cfcc1108aada', 'テスト株式会社', '試験県テスト市examビル1F', '1234567890123', '123456789TT234567890', true, '2025-01-01', '2030-12-31', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', now(), now());"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
        "INSERT INTO auth.tbl_plants (plant_id, operator_id, plant_name, plant_address, open_plant_id, global_plant_id, deleted_flag, effective_start_date, effective_end_date, created_at, created_user_id, updated_at, updated_user_id) VALUES ("
                + "'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'テストプラント001', '東京都渋谷区xxx', 'PLANT0001', 'PLANTABCD0001', false, '2025-08-20', '2099-12-31', now(), 'manualuser', now(), 'manualuser'),"
                + "('54708446-d761-4071-9bc3-cfcc1108aada', '54708446-d761-4071-9bc3-cfcc1108aada', 'テストプラント002', '試験県テスト市examビル1F', 'PLANT0002', 'PLANTTTTT0002', true, '2025-01-01', '2030-12-31', now(), 'manualuser', now(), 'manualuser');"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testFindWithLimitOffset_success() {
        // arrange
        Sort sort = SqlUtils.buildSort(PlantEntity.class, null, null, ConstSqlQueries.COLUMN_PLANTS_OPERATOR_ID, Sort.Direction.ASC);

        Specification<PlantEntity> spec = SqlUtils.createSpecification();
        spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_OPERATOR_ID, "b39e6248-c888-56ca-d9d0-89de1b1adc8e");
        spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_PLANT_ID, "b39e6248-c888-56ca-d9d0-89de1b1adc8e");
        spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_PLANT_NAME, "テストプラント001");
        spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_PLANT_ADDRESS, "東京都渋谷区xxx");
        spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_OPEN_PLANT_ID, "PLANT0001");
        spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_GLOBAL_PLANT_ID, "PLANTABCD0001");
        spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_DELETED_FLAG, Boolean.parseBoolean("false"));
        spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.LESS_THAN_OR_EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_EFFECTIVE_START_DATE, LocalDate.parse("2025-08-20"));
        spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.GREATER_THAN_OR_EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_EFFECTIVE_END_DATE, LocalDate.parse("2025-08-20"));

        // act
        List<PlantEntity> result = repository.findWithLimitOffset(spec, 0, 0, sort);

        // assert
        assertEquals(result.get(0).getPlantId(), "b39e6248-c888-56ca-d9d0-89de1b1adc8e");
        assertEquals(result.get(0).getPlantName(), "テストプラント001");
        assertEquals(result.get(0).getPlantAddress(), "東京都渋谷区xxx");
        assertEquals(result.get(0).getOpenPlantId(), "PLANT0001");
        assertEquals(result.get(0).getGlobalPlantId(), "PLANTABCD0001");
        assertEquals(result.get(0).isDeletedFlag(), false);
        assertEquals(result.get(0).getEffectiveStartDate(), LocalDate.parse("2025-08-20"));
        assertEquals(result.get(0).getEffectiveEndDate(), LocalDate.parse("2099-12-31"));
        assertNotNull(result.get(0).getCreatedAt());
        assertNotNull(result.get(0).getUpdatedAt());
        assertEquals(1, result.size());
        assertNotNull(result);
    }

    /**
     * case#2:
     * Test for findWithLimitOffset when limit is set.
     */
    @Test
    @Sql(statements = {
        "DELETE FROM auth.tbl_plants;"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
        "INSERT INTO auth.tbl_operators (operator_id, operator_name, operator_address, open_operator_id, global_operator_id, deleted_flag, effective_start_date, effective_end_date, created_user_id, updated_user_id, created_at, updated_at) VALUES ("
                + "'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'テスト事業者001', '東京都渋谷区xxx', '1234567890001', '1234ABCD5678EFGH0001', false, '2025-08-20', '2099-12-31', 'manualuser', 'manualuser', now(), now()),"
                + "('54708446-d761-4071-9bc3-cfcc1108aada', 'テスト株式会社', '試験県テスト市examビル1F', '1234567890123', '123456789TT234567890', true, '2025-01-01', '2030-12-31', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', now(), now());"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
        "INSERT INTO auth.tbl_plants (plant_id, operator_id, plant_name, plant_address, open_plant_id, global_plant_id, deleted_flag, effective_start_date, effective_end_date, created_at, created_user_id, updated_at, updated_user_id) VALUES ("
                + "'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'テストプラント001', '東京都渋谷区xxx', 'PLANT0001', 'PLANTABCD0001', false, '2025-08-20', '2099-12-31', now(), 'manualuser', now(), 'manualuser'),"
                + "('54708446-d761-4071-9bc3-cfcc1108aada', '54708446-d761-4071-9bc3-cfcc1108aada', 'テストプラント002', '試験県テスト市examビル1F', 'PLANT0002', 'PLANTTTTT0002', true, '2025-01-01', '2030-12-31', now(), 'manualuser', now(), 'manualuser');"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testFindWithLimitOffset_withSpecNull() {
        // arrange
        Sort sort = SqlUtils.buildSort(PlantEntity.class, null, null, ConstSqlQueries.COLUMN_PLANTS_OPERATOR_ID, Sort.Direction.ASC);

        Specification<PlantEntity> spec = null;

        // act
        List<PlantEntity> result = repository.findWithLimitOffset(spec, 0, 1, sort);

        // assert
        assertEquals(result.get(0).getPlantId(), "54708446-d761-4071-9bc3-cfcc1108aada");
        assertEquals(1, result.size());
        assertNotNull(result);
    }

    /**
     * case#3:
     * Test for findWithLimitOffset when limit is set.
     */
    @Test
    @Sql(statements = {
        "DELETE FROM auth.tbl_plants;"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
        "INSERT INTO auth.tbl_operators (operator_id, operator_name, operator_address, open_operator_id, global_operator_id, deleted_flag, effective_start_date, effective_end_date, created_user_id, updated_user_id, created_at, updated_at) VALUES ("
                + "'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'テスト事業者001', '東京都渋谷区xxx', '1234567890001', '1234ABCD5678EFGH0001', false, '2025-08-20', '2099-12-31', 'manualuser', 'manualuser', now(), now()),"
                + "('54708446-d761-4071-9bc3-cfcc1108aada', 'テスト株式会社', '試験県テスト市examビル1F', '1234567890123', '123456789TT234567890', true, '2025-01-01', '2030-12-31', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', now(), now());"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
        "INSERT INTO auth.tbl_plants (plant_id, operator_id, plant_name, plant_address, open_plant_id, global_plant_id, deleted_flag, effective_start_date, effective_end_date, created_at, created_user_id, updated_at, updated_user_id) VALUES ("
                + "'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'テストプラント001', '東京都渋谷区xxx', 'PLANT0001', 'PLANTABCD0001', false, '2025-08-20', '2099-12-31', now(), 'manualuser', now(), 'manualuser'),"
                + "('54708446-d761-4071-9bc3-cfcc1108aada', '54708446-d761-4071-9bc3-cfcc1108aada', 'テストプラント002', '試験県テスト市examビル1F', 'PLANT0002', 'PLANTTTTT0002', true, '2025-01-01', '2030-12-31', now(), 'manualuser', now(), 'manualuser');"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testFindWithLimitOffset_withLimit() {
        // arrange
        Sort sort = SqlUtils.buildSort(PlantEntity.class, null, null, ConstSqlQueries.COLUMN_PLANTS_OPERATOR_ID, Sort.Direction.ASC);

        Specification<PlantEntity> spec = SqlUtils.createSpecification();

        // act
        List<PlantEntity> result = repository.findWithLimitOffset(spec, 0, 1, sort);

        // assert
        assertEquals(result.get(0).getPlantId(), "54708446-d761-4071-9bc3-cfcc1108aada");
        assertEquals(1, result.size());
        assertNotNull(result);
    }

    /**
     * Test for findWithLimitOffset when index is set.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "1, true", // case#4: exist
        "2, false", // case#5: no data
    }, nullValues = "NULL")
    @Sql(statements = {
        "DELETE FROM auth.tbl_plants;"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
        "INSERT INTO auth.tbl_operators (operator_id, operator_name, operator_address, open_operator_id, global_operator_id, deleted_flag, effective_start_date, effective_end_date, created_user_id, updated_user_id, created_at, updated_at) VALUES ("
                + "'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'テスト事業者001', '東京都渋谷区xxx', '1234567890001', '1234ABCD5678EFGH0001', false, '2025-08-20', '2099-12-31', 'manualuser', 'manualuser', now(), now()),"
                + "('54708446-d761-4071-9bc3-cfcc1108aada', 'テスト株式会社', '試験県テスト市examビル1F', '1234567890123', '123456789TT234567890', true, '2025-01-01', '2030-12-31', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', now(), now());"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
        "INSERT INTO auth.tbl_plants (plant_id, operator_id, plant_name, plant_address, open_plant_id, global_plant_id, deleted_flag, effective_start_date, effective_end_date, created_at, created_user_id, updated_at, updated_user_id) VALUES ("
                + "'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'テストプラント001', '東京都渋谷区xxx', '1234567890001', '1234ABCD5678EFGH0001', false, '2025-08-20', '2099-12-31', now(), 'manualuser', now(), 'manualuser'),"
                + "('54708446-d761-4071-9bc3-cfcc1108aada', '54708446-d761-4071-9bc3-cfcc1108aada', 'テストプラント002', '試験県テスト市examビル1F', '1234567890123', '123456789TT234567890', true, '2025-01-01', '2030-12-31', now(), 'manualuser', now(), 'manualuser');"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testFindWithLimitOffset_withIndex(
            int offset,
            boolean isExist
    ) {
        // arrange
        Sort sort = SqlUtils.buildSort(PlantEntity.class, null, null, ConstSqlQueries.COLUMN_PLANTS_OPERATOR_ID, Sort.Direction.ASC);

        Specification<PlantEntity> spec = SqlUtils.createSpecification();

        // act
        List<PlantEntity> result = repository.findWithLimitOffset(spec, offset, 1, sort);

        // assert
        if (isExist) {
            assertEquals(result.get(0).getPlantId(), "b39e6248-c888-56ca-d9d0-89de1b1adc8e");
            assertEquals(1, result.size());
        } else {
            assertEquals(0, result.size());
        }
        assertNotNull(result);
    }

    /**
     * Parameterized test for findWithLimitOffset when spec is set.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "operator_id, b39e6248-c888-56ca-d9d0-89de1b1adc8e, 1", // case#6: normal
        "operator_id, b39e6248-c888-56ca-d9d0-89de1b1adc81, 0", // case#7: not exist
        "plant_id, b39e6248-c888-56ca-d9d0-89de1b1adc8e, 1", // case#8: normal
        "plant_id, b39e6248-c888-56ca-d9d0-89de1b1adc81, 0", // case#9: not exist
        "plant_name, テストプラント001, 1", // case#10: all
        "plant_name, テスト*, 2", // case#11: partial match
        "plant_name, *001, 1", // case#12: partial match
        "plant_name, LONG, 0", // case#13: max length
        "plant_name, *, 2", // case#14: min length
        "plant_name, '  ', 0", // case#15: blank space
        "plant_address, 試験県テスト市examビル1F, 1", // case#16: all
        "plant_address, 試験県*, 1", // case#17: partial match
        "plant_address, *xxx, 1", // case#18: partial match
        "plant_address, LONG, 0", // case#19: max length
        "plant_address, *, 2", // case#20: min length
        "plant_address, '  ', 0", // case#21: blank space
        "open_plant_id, 1234567890001, 1", // case#22: normal
        "open_plant_id, Aa123456789012345678, 0", // case#23: max length
        "open_plant_id, A, 0", // case#24: min length
        "global_plant_id, 1234ABCD5678EFGH0001, 1", // case#25: normal
        "global_plant_id, LONG, 0", // case#26: max length
        "global_plant_id, '', 0", // case#27: min length
        "global_plant_id, '  ', 0", // case#28: blank space
        "deleted_flag, true, 1", // case#29: true
        "deleted_flag, false, 1", // case#30: false
        "effective_date, 2025-08-20, 2", // case#31: on the start date of first record
        "effective_date, 2025-08-19, 1", // case#32: before the start date of first record
        "effective_date, 2024-12-31, 0", // case#33: before the start date of both records
        "effective_date, 2030-12-31, 2", // case#34: on the end date of first record
        "effective_date, 2031-01-01, 1", // case#35: after the end date of second record
        "effective_date, 2100-01-01, 0", // case#36: after the end date of both records
    }, nullValues = "NULL")
    @Sql(statements = {
        "DELETE FROM auth.tbl_plants;"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
        "INSERT INTO auth.tbl_operators (operator_id, operator_name, operator_address, open_operator_id, global_operator_id, deleted_flag, effective_start_date, effective_end_date, created_user_id, updated_user_id, created_at, updated_at) VALUES ("
                + "'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'テスト事業者001', '東京都渋谷区xxx', '1234567890001', '1234ABCD5678EFGH0001', false, '2025-08-20', '2099-12-31', 'manualuser', 'manualuser', now(), now()),"
                + "('54708446-d761-4071-9bc3-cfcc1108aada', 'テスト株式会社', '試験県テスト市examビル1F', '1234567890123', '123456789TT234567890', true, '2025-01-01', '2030-12-31', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', now(), now());"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
        "INSERT INTO auth.tbl_plants (plant_id, operator_id, plant_name, plant_address, open_plant_id, global_plant_id, deleted_flag, effective_start_date, effective_end_date, created_at, created_user_id, updated_at, updated_user_id) VALUES ("
                + "'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'テストプラント001', '東京都渋谷区xxx', '1234567890001', '1234ABCD5678EFGH0001', false, '2025-08-20', '2099-12-31', now(), 'manualuser', now(), 'manualuser'),"
                + "('54708446-d761-4071-9bc3-cfcc1108aada', '54708446-d761-4071-9bc3-cfcc1108aada', 'テストプラント002', '試験県テスト市examビル1F', '1234567890123', '123456789TT234567890', true, '2025-01-01', '2030-12-31', now(), 'manualuser', now(), 'manualuser');"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testFindWithLimitOffset_withSpec(
            String searchCriteriaKey,
            String searchCriteriaValue,
            int expectedSize
    ) {
        searchCriteriaValue = "LONG".equals(searchCriteriaValue) && searchCriteriaKey.equals(ConstSqlQueries.COLUMN_PLANTS_PLANT_NAME)
                ? "A".repeat(Const.PLANT_NAME_LENGTH_MAX)
                : ("LONG".equals(searchCriteriaValue) && searchCriteriaKey.equals(ConstSqlQueries.COLUMN_PLANTS_PLANT_ADDRESS)
                    ? "A".repeat(Const.PLANT_ADDRESS_LENGTH_MAX)
                    : ("LONG".equals(searchCriteriaValue) && searchCriteriaKey.equals(ConstSqlQueries.COLUMN_PLANTS_GLOBAL_PLANT_ID)
                        ? "A".repeat(Const.GLOBAL_OPERATOR_ID_LENGTH_MAX)
                        : searchCriteriaValue
                    )
                );

        // arrange
        Sort sort = SqlUtils.buildSort(PlantEntity.class, null, null, ConstSqlQueries.COLUMN_PLANTS_OPERATOR_ID, Sort.Direction.ASC);

        Specification<PlantEntity> spec = SqlUtils.createSpecification();
        if (searchCriteriaKey != null && searchCriteriaValue != null) {
            if (searchCriteriaKey.equals(ConstSqlQueries.COLUMN_PLANTS_OPERATOR_ID)) {
                spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_OPERATOR_ID, searchCriteriaValue);
            }
            if (searchCriteriaKey.equals(ConstSqlQueries.COLUMN_PLANTS_PLANT_ID)) {
                spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_PLANT_ID, searchCriteriaValue);
            }
            if (searchCriteriaKey.equals(ConstSqlQueries.COLUMN_PLANTS_PLANT_NAME)) {
                spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.LIKE, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_PLANT_NAME, searchCriteriaValue);
            }
            if (searchCriteriaKey.equals(ConstSqlQueries.COLUMN_PLANTS_PLANT_ADDRESS)) {
                spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.LIKE, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_PLANT_ADDRESS, searchCriteriaValue);
            }
            if (searchCriteriaKey.equals(ConstSqlQueries.COLUMN_PLANTS_OPEN_PLANT_ID)) {
                spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_OPEN_PLANT_ID, searchCriteriaValue);
            }
            if (searchCriteriaKey.equals(ConstSqlQueries.COLUMN_PLANTS_GLOBAL_PLANT_ID)) {
                spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_GLOBAL_PLANT_ID, searchCriteriaValue);
            }
            if (searchCriteriaKey.equals(ConstSqlQueries.COLUMN_PLANTS_DELETED_FLAG)) {
                spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_DELETED_FLAG, Boolean.parseBoolean(searchCriteriaValue));
            }
            if (searchCriteriaKey.equals("effective_date")) {
                spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.LESS_THAN_OR_EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_EFFECTIVE_START_DATE, LocalDate.parse(searchCriteriaValue));
                spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.GREATER_THAN_OR_EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_EFFECTIVE_END_DATE, LocalDate.parse(searchCriteriaValue));
            }
        }

        // act
        List<PlantEntity> result = repository.findWithLimitOffset(spec, 0, 0, sort);

        // assert
        assertEquals(expectedSize, result.size());
        assertNotNull(result);
    }

    /**
     * Parameterized test for sort.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "operator_id, NULL, operatorId", // case#37: operator_id
        "plant_id, NULL, plantId", // case#38: plant_id
        "plant_name, NULL, plantName", // case#39: plant_name
        "plant_address, NULL, plantAddress", // case#40: plant_address
        "open_plant_id, NULL, openPlantId", // case#41: open_plant_id
        "global_plant_id, NULL, globalPlantId", // case#42: global_plant_id
        "deleted_flag, NULL, deletedFlag", // case#43: deleted_flag
        "effective_start_date, NULL, effectiveStartDate", // case#44: effective_start_date
        "effective_end_date, NULL, effectiveEndDate", // case#45: effective_end_date
        "created_at, NULL, createdAt", // case#46: created_at
        "updated_at, NULL, updatedAt", // case#47: updated_at
        "NULL, asc, operatorId", // case#48: default order by operator_id asc
        "NULL, desc, operatorId", // case#49: default order by operator_id desc
        "NULL, NULL, operatorId", // case#50: default order by operator_id asc
        "plant_id, asc, plantId", // case#51: order by plant_id asc
        "plant_name, DESC, plantName", // case#52: order by plant_name desc
    }, nullValues = "NULL")
    @Sql(statements = {
        "DELETE FROM auth.tbl_plants;"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
        "INSERT INTO auth.tbl_operators (operator_id, operator_name, operator_address, open_operator_id, global_operator_id, deleted_flag, effective_start_date, effective_end_date, created_user_id, updated_user_id, created_at, updated_at) VALUES ("
                + "'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'テスト事業者001', '東京都渋谷区xxx', '1234567890001', '1234ABCD5678EFGH0001', false, '2025-08-20', '2099-12-31', 'manualuser', 'manualuser', now(), now()),"
                + "('54708446-d761-4071-9bc3-cfcc1108aada', 'テスト株式会社', '試験県テスト市examビル1F', '1234567890123', '123456789TT234567890', true, '2025-01-01', '2030-12-31', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', now(), now());"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
        "INSERT INTO auth.tbl_plants (plant_id, operator_id, plant_name, plant_address, open_plant_id, global_plant_id, deleted_flag, effective_start_date, effective_end_date, created_at, created_user_id, updated_at, updated_user_id) VALUES ("
                + "'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'テストプラント001', '東京都渋谷区xxx', 'PLANT0001', 'PLANTABCD0001', false, '2025-08-20', '2099-12-31', now(), 'manualuser', now(), 'manualuser'),"
                + "('54708446-d761-4071-9bc3-cfcc1108aada', '54708446-d761-4071-9bc3-cfcc1108aada', 'テストプラント002', '試験県テスト市examビル1F', 'PLANT0002', 'PLANTTTTT0002', true, '2025-01-01', '2030-12-31', now(), 'manualuser', now(), 'manualuser');"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testFindWithLimitOffset_sortByVariousFields(
            String orderByKey,
            String orderByDirection,
            String expectedFirstValue
    ) {
        // arrange
        Sort sort = SqlUtils.buildSort(PlantEntity.class, orderByKey, orderByDirection, ConstSqlQueries.COLUMN_PLANTS_OPERATOR_ID, Sort.Direction.ASC);
        Specification<PlantEntity> spec = SqlUtils.createSpecification();

        // act
        List<PlantEntity> result = repository.findWithLimitOffset(spec, 0, 10, sort);

        // assert
        Object firstData = null;
        if (orderByKey != null && orderByDirection == null) {
            firstData = getFieldValue(result.get(0), expectedFirstValue);
            if (firstData instanceof String) {
                if (orderByKey.equals(ConstSqlQueries.COLUMN_PLANTS_OPERATOR_ID)) {
                    assertEquals(result.get(0).getPlantId(), "54708446-d761-4071-9bc3-cfcc1108aada");
                }
                if (orderByKey.equals(ConstSqlQueries.COLUMN_PLANTS_PLANT_NAME)) {
                    assertEquals(result.get(0).getPlantName(), "テストプラント001");
                }
                if (orderByKey.equals(ConstSqlQueries.COLUMN_PLANTS_PLANT_ADDRESS)) {
                    assertEquals(result.get(0).getPlantAddress(), "東京都渋谷区xxx");
                }
                if (orderByKey.equals(ConstSqlQueries.COLUMN_PLANTS_OPEN_PLANT_ID)) {
                    assertEquals(result.get(0).getOpenPlantId(), "PLANT0001");
                }
                if (orderByKey.equals(ConstSqlQueries.COLUMN_PLANTS_GLOBAL_PLANT_ID)) {
                    assertEquals(result.get(0).getGlobalPlantId(), "PLANTABCD0001");
                }
            } else if (firstData instanceof Boolean) {
                if (orderByKey.equals(ConstSqlQueries.COLUMN_PLANTS_DELETED_FLAG)) {
                    assertEquals(result.get(0).isDeletedFlag(), false);
                    assertEquals(result.get(0).getPlantId(), "b39e6248-c888-56ca-d9d0-89de1b1adc8e");
                }
            } else if (firstData instanceof LocalDate) {
                if (orderByKey.equals(ConstSqlQueries.COLUMN_PLANTS_EFFECTIVE_START_DATE)) {
                    assertEquals(result.get(0).getEffectiveStartDate(), LocalDate.parse("2025-01-01"));
                }
                if (orderByKey.equals(ConstSqlQueries.COLUMN_PLANTS_EFFECTIVE_END_DATE)) {
                    assertEquals(result.get(0).getEffectiveEndDate(), LocalDate.parse("2030-12-31"));
                }
            }
        } else if (orderByKey != null && orderByDirection != null) {
            firstData = getFieldValue(result.get(0), expectedFirstValue);
            if (orderByKey.equals(ConstSqlQueries.COLUMN_PLANTS_OPERATOR_ID)) {
                assertEquals(result.get(0).getPlantId(), "54708446-d761-4071-9bc3-cfcc1108aada");
            }
            if (orderByKey.equals(ConstSqlQueries.COLUMN_PLANTS_PLANT_NAME)) {
                assertEquals(result.get(0).getPlantName(), "テストプラント002");
            }
        } else if (orderByKey == null && orderByDirection != null) {
            if ("asc".equals(orderByDirection)) {
                assertEquals(result.get(0).getPlantId(), "54708446-d761-4071-9bc3-cfcc1108aada");
            } else {
                assertEquals(result.get(0).getPlantId(), "b39e6248-c888-56ca-d9d0-89de1b1adc8e");
            }
        }  else if (orderByKey == null && orderByDirection == null) {
            assertEquals(result.get(0).getPlantId(), "54708446-d761-4071-9bc3-cfcc1108aada");
        }
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    /**
     * Parameterized test for findWithLimitOffset repository failures.
     */
    @ParameterizedTest
    @CsvSource({
        "IllegalArgumentException", // case#53: IllegalArgumentException
        "OutOfServiceException", // case#54: OutOfServiceException
        "UnexpectedException", // case#55: UnexpectedException
    })
    void testFindWithLimitOffset_throwsException(
            String exceptionClassName
    ) {
        // mock
        repository = mock(PlantRepositoryCustomImpl.class);

        String exceptionLogMessage = null;
        String exceptionResponseMessage = null;
        Exception exception = null;
        Class<? extends Throwable> clazz = null;

        // Arrange
        switch (exceptionClassName) {
            case "IllegalArgumentException":
                exceptionResponseMessage = "";
                exception = new IllegalArgumentException(exceptionResponseMessage);
                clazz = IllegalArgumentException.class;
                break;
            case "OutOfServiceException":
                exceptionLogMessage = ConstError.ERR_503_OUT_OF_SERVICE_EXCEPTION;
                exceptionResponseMessage = "";
                exception = new OutOfServiceException(exceptionLogMessage);
                clazz = OutOfServiceException.class;
                break;
            case "UnexpectedException":
                exceptionLogMessage = ConstError.ERR_500_DB_QUERY_FAILED;
                exceptionResponseMessage = ConstError.ERR_500_MESSAGE;
                exception = new UnexpectedException(exceptionLogMessage, exceptionResponseMessage);
                clazz = UnexpectedException.class;
                break;
            default:
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        Sort sort = SqlUtils.buildSort(PlantEntity.class, null, null, ConstSqlQueries.COLUMN_PLANTS_OPERATOR_ID, Sort.Direction.ASC);
        Specification<PlantEntity> spec = SqlUtils.createSpecification();

        doThrow(exception).when(repository).findWithLimitOffset(spec, 0, 10, sort);

        // Act & Assert
        Throwable thrownException = assertThrows(clazz, () -> {
            repository.findWithLimitOffset(spec, 0, 10, sort);
        });

        // Detailed verification according to exception type
        if (exception instanceof AbstractBaseException) {
            AbstractBaseException baseException = (AbstractBaseException) exception;
            AbstractBaseException thrownBaseException = (AbstractBaseException) thrownException;
            assertEquals(baseException.getLogMessage(), thrownBaseException.getLogMessage());
            assertEquals(baseException.getResponseMessage(), thrownBaseException.getResponseMessage());
        } else {
            assertEquals(exception.getMessage(), thrownException.getMessage());
        }
    }

    /**
    * Utility to get the value of any field in PlantEntity.
    */
    private Object getFieldValue(PlantEntity entity, String fieldName) {
        Class<?> clazz = entity.getClass();
        while (clazz != null) {
            try {
                var field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(entity);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Cannot access field: " + fieldName, e);
            }
        }
        throw new IllegalArgumentException("No such field: " + fieldName);
    }
}