/*
 * OperatorRepositoryCustomImplTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for OperatorRepositoryCustomImpl.
 *
 * Date: 2025/08/30
 */

package io.github.open_dataspaces.core.domain.repository.interfaces.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import io.github.open_dataspaces.core.domain.entities.OperatorEntity;
import io.github.open_dataspaces.core.exception.AbstractBaseException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnexpectedException;

import jakarta.persistence.EntityManager;

/**
 * Unit tests for the {@link OperatorRepositoryCustomImpl} class.
 */
@SpringBootTest
@Transactional
class OperatorRepositoryCustomImplTest {

    @Autowired
    private OperatorRepositoryCustomImpl repository;

    @Autowired
    private EntityManager entityManager;

    /**
     * setUp initializes the test environment before each test.
     */
    @BeforeEach
    void setUp() {
        // Use reflection to inject the mock EntityManager
        try {
            var field = OperatorRepositoryCustomImpl.class.getDeclaredField("entityManager");
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
        "DELETE FROM auth.tbl_plants;",
        "DELETE FROM auth.tbl_operators;"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
        "INSERT INTO auth.tbl_operators (operator_id, operator_name, operator_address, open_operator_id, global_operator_id, deleted_flag, effective_start_date, effective_end_date, created_user_id, updated_user_id, created_at, updated_at) VALUES ("
                + "'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'テスト事業者001', '東京都渋谷区xxx', '1234567890001', '1234ABCD5678EFGH0001', false, '2025-08-20', '2099-12-31', 'manualuser', 'manualuser', now(), now()),"
                + "('54708446-d761-4071-9bc3-cfcc1108aada', 'テスト株式会社', '試験県テスト市examビル1F', '1234567890123', '123456789TT234567890', true, '2025-01-01', '2030-12-31', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', now(), now());"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testFindWithLimitOffset_success() {
        // arrange
        Sort sort = SqlUtils.buildSort(OperatorEntity.class, null, null, ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID, Sort.Direction.ASC);

        Specification<OperatorEntity> spec = SqlUtils.createSpecification();
        spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, OperatorEntity.class, ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID, "b39e6248-c888-56ca-d9d0-89de1b1adc8e");
        spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.LIKE, OperatorEntity.class, ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_NAME, "テスト事業者001");
        spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.LIKE, OperatorEntity.class, ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ADDRESS, "東京都渋谷区xxx");
        spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, OperatorEntity.class, ConstSqlQueries.COLUMN_OPERATORS_OPEN_OPERATOR_ID, "1234567890001");
        spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, OperatorEntity.class, ConstSqlQueries.COLUMN_OPERATORS_GLOBAL_OPERATOR_ID, "1234ABCD5678EFGH0001");
        spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, OperatorEntity.class, ConstSqlQueries.COLUMN_COMMON_DELETED_FLAG, Boolean.parseBoolean("false"));
        spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.LESS_THAN_OR_EQUALS, OperatorEntity.class, ConstSqlQueries.COLUMN_COMMON_EFFECTIVE_START_DATE, LocalDate.parse("2025-08-20"));
        spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.GREATER_THAN_OR_EQUALS, OperatorEntity.class, ConstSqlQueries.COLUMN_COMMON_EFFECTIVE_END_DATE, LocalDate.parse("2025-08-20"));

        // act
        List<OperatorEntity> result = repository.findWithLimitOffset(spec, 0, 0, sort);

        // assert
        assertEquals(result.get(0).getOperatorId(), "b39e6248-c888-56ca-d9d0-89de1b1adc8e");
        assertEquals(result.get(0).getOperatorName(), "テスト事業者001");
        assertEquals(result.get(0).getOperatorAddress(), "東京都渋谷区xxx");
        assertEquals(result.get(0).getOpenOperatorId(), "1234567890001");
        assertEquals(result.get(0).getGlobalOperatorId(), "1234ABCD5678EFGH0001");
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
        "DELETE FROM auth.tbl_plants;",
        "DELETE FROM auth.tbl_operators;"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
        "INSERT INTO auth.tbl_operators (operator_id, operator_name, operator_address, open_operator_id, global_operator_id, deleted_flag, effective_start_date, effective_end_date, created_user_id, updated_user_id, created_at, updated_at) VALUES ("
                + "'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'テスト事業者001', '東京都渋谷区xxx', '1234567890001', '1234ABCD5678EFGH0001', false, '2025-08-20', '2099-12-31', 'manualuser', 'manualuser', now(), now()),"
                + "('54708446-d761-4071-9bc3-cfcc1108aada', 'テスト株式会社', '試験県テスト市examビル1F', '1234567890123', '123456789TT234567890', true, '2025-01-01', '2030-12-31', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', now(), now());"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testFindWithLimitOffset_withLimit() {
        // arrange
        Sort sort = SqlUtils.buildSort(OperatorEntity.class, null, null, ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID, Sort.Direction.ASC);

        Specification<OperatorEntity> spec = SqlUtils.createSpecification();

        // act
        List<OperatorEntity> result = repository.findWithLimitOffset(spec, 0, 1, sort);

        // assert
        assertEquals(result.get(0).getOperatorId(), "54708446-d761-4071-9bc3-cfcc1108aada");
        assertEquals(1, result.size());
        assertNotNull(result);
    }

    /**
     * Test for findWithLimitOffset when index is set.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "1, true", // case#3: exist
        "2, false", // case#4: no data
    }, nullValues = "NULL")
    @Sql(statements = {
        "DELETE FROM auth.tbl_plants;",
        "DELETE FROM auth.tbl_operators;"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
        "INSERT INTO auth.tbl_operators (operator_id, operator_name, operator_address, open_operator_id, global_operator_id, deleted_flag, effective_start_date, effective_end_date, created_user_id, updated_user_id, created_at, updated_at) VALUES ("
                + "'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'テスト事業者001', '東京都渋谷区xxx', '1234567890001', '1234ABCD5678EFGH0001', false, '2025-08-20', '2099-12-31', 'manualuser', 'manualuser', now(), now()),"
                + "('54708446-d761-4071-9bc3-cfcc1108aada', 'テスト株式会社', '試験県テスト市examビル1F', '1234567890123', '123456789TT234567890', true, '2025-01-01', '2030-12-31', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', now(), now());"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testFindWithLimitOffset_withIndex(
            int offset,
            boolean isExist
    ) {
        // arrange
        Sort sort = SqlUtils.buildSort(OperatorEntity.class, null, null, ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID, Sort.Direction.ASC);

        Specification<OperatorEntity> spec = SqlUtils.createSpecification();

        // act
        List<OperatorEntity> result = repository.findWithLimitOffset(spec, offset, 1, sort);

        // assert
        if (isExist) {
            assertEquals(result.get(0).getOperatorId(), "b39e6248-c888-56ca-d9d0-89de1b1adc8e");
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
        "operator_id, b39e6248-c888-56ca-d9d0-89de1b1adc8e, 1", // case#5: normal
        "operator_id, b39e6248-c888-56ca-d9d0-89de1b1adc81, 0", // case#6: not exist
        "operator_name, テスト株式会社, 1", // case#7: all
        "operator_name, テスト*, 2", // case#8: partial match
        "operator_name, *001, 1", // case#9: partial match
        "operator_name, LONG, 0", // case#10: max length
        "operator_name, *, 2", // case#11: min length
        "operator_name, '  ', 0", // case#12: blank space
        "operator_address, 試験県テスト市examビル1F, 1", // case#13: all
        "operator_address, 試験県*, 1", // case#14: partial match
        "operator_address, *xxx, 1", // case#15: partial match
        "operator_address, LONG, 0", // case#16: max length
        "operator_address, *, 2", // case#17: min length
        "operator_address, '  ', 0", // case#18: blank space
        "open_operator_id, 1234567890001, 1", // case#19: normal
        "open_operator_id, Aa123456789012345678, 0", // case#20: max length
        "open_operator_id, A, 0", // case#21: min length
        "global_operator_id, 1234ABCD5678EFGH0001, 1", // case#22: normal
        "global_operator_id, LONG, 0", // case#23: max length
        "global_operator_id, '', 0", // case#24: min length
        "global_operator_id, '  ', 0", // case#25: blank space
        "deleted_flag, true, 1", // case#26: true
        "deleted_flag, false, 1", // case#27: false
        "effective_date, 2025-08-20, 2", // case#28: on the start date of first record
        "effective_date, 2025-08-19, 1", // case#29: before the start date of first record
        "effective_date, 2024-12-31, 0", // case#30: before the start date of both records
        "effective_date, 2030-12-31, 2", // case#31: on the end date of first record
        "effective_date, 2031-01-01, 1", // case#32: after the end date of second record
        "effective_date, 2100-01-01, 0", // case#33: after the end date of both records
    }, nullValues = "NULL")
    @Sql(statements = {
        "DELETE FROM auth.tbl_plants;",
        "DELETE FROM auth.tbl_operators;"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
        "INSERT INTO auth.tbl_operators (operator_id, operator_name, operator_address, open_operator_id, global_operator_id, deleted_flag, effective_start_date, effective_end_date, created_user_id, updated_user_id, created_at, updated_at) VALUES ("
                + "'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'テスト事業者001', '東京都渋谷区xxx', '1234567890001', '1234ABCD5678EFGH0001', false, '2025-08-20', '2099-12-31', 'manualuser', 'manualuser', now(), now()),"
                + "('54708446-d761-4071-9bc3-cfcc1108aada', 'テスト株式会社', '試験県テスト市examビル1F', '1234567890123', '123456789TT234567890', true, '2025-01-01', '2030-12-31', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', now(), now());"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testFindWithLimitOffset_withSpec(
            String searchCriteriaKey,
            String searchCriteriaValue,
            int expectedSize
    ) {
        searchCriteriaValue = "LONG".equals(searchCriteriaValue) && searchCriteriaKey.equals(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_NAME)
                ? "A".repeat(Const.OPERATOR_NAME_LENGTH_MAX)
                : ("LONG".equals(searchCriteriaValue) && searchCriteriaKey.equals(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ADDRESS)
                    ? "A".repeat(Const.OPERATOR_ADDRESS_LENGTH_MAX)
                    : ("LONG".equals(searchCriteriaValue) && searchCriteriaKey.equals(ConstSqlQueries.COLUMN_OPERATORS_GLOBAL_OPERATOR_ID)
                        ? "A".repeat(Const.GLOBAL_OPERATOR_ID_LENGTH_MAX)
                        : searchCriteriaValue
                    )
                );

        // arrange
        Sort sort = SqlUtils.buildSort(OperatorEntity.class, null, null, ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID, Sort.Direction.ASC);

        Specification<OperatorEntity> spec = SqlUtils.createSpecification();
        if (searchCriteriaKey != null && searchCriteriaValue != null) {
            if (searchCriteriaKey.equals(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID)) {
                spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, OperatorEntity.class, ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID, searchCriteriaValue);
            }
            if (searchCriteriaKey.equals(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_NAME)) {
                spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.LIKE, OperatorEntity.class, ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_NAME, searchCriteriaValue);
            }
            if (searchCriteriaKey.equals(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ADDRESS)) {
                spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.LIKE, OperatorEntity.class, ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ADDRESS, searchCriteriaValue);
            }
            if (searchCriteriaKey.equals(ConstSqlQueries.COLUMN_OPERATORS_OPEN_OPERATOR_ID)) {
                spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, OperatorEntity.class, ConstSqlQueries.COLUMN_OPERATORS_OPEN_OPERATOR_ID, searchCriteriaValue);
            }
            if (searchCriteriaKey.equals(ConstSqlQueries.COLUMN_OPERATORS_GLOBAL_OPERATOR_ID)) {
                spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, OperatorEntity.class, ConstSqlQueries.COLUMN_OPERATORS_GLOBAL_OPERATOR_ID, searchCriteriaValue);
            }
            if (searchCriteriaKey.equals(ConstSqlQueries.COLUMN_COMMON_DELETED_FLAG)) {
                spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, OperatorEntity.class, ConstSqlQueries.COLUMN_COMMON_DELETED_FLAG, Boolean.parseBoolean(searchCriteriaValue));
            }
            if (searchCriteriaKey.equals("effective_date")) {
                spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.LESS_THAN_OR_EQUALS, OperatorEntity.class, ConstSqlQueries.COLUMN_COMMON_EFFECTIVE_START_DATE, LocalDate.parse(searchCriteriaValue));
                spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.GREATER_THAN_OR_EQUALS, OperatorEntity.class, ConstSqlQueries.COLUMN_COMMON_EFFECTIVE_END_DATE, LocalDate.parse(searchCriteriaValue));
            }
        }

        // act
        List<OperatorEntity> result = repository.findWithLimitOffset(spec, 0, 0, sort);

        // assert
        assertEquals(expectedSize, result.size());
        assertNotNull(result);
    }

    /**
     * Parameterized test for sort.
     */
    @ParameterizedTest
    @CsvSource(value = {
        "operator_id, NULL, operatorId", // case#34: operator_id
        "operator_name, NULL, operatorName", // case#35: operator_name
        "operator_address, NULL, operatorAddress", // case#36: operator_address
        "open_operator_id, NULL, openOperatorId", // case#37: open_operator_id
        "global_operator_id, NULL, globalOperatorId", // case#38: global_operator_id
        "deleted_flag, NULL, deletedFlag", // case#39: deleted_flag
        "effective_start_date, NULL, effectiveStartDate", // case#40: effective_start_date
        "effective_end_date, NULL, effectiveEndDate", // case#41: effective_end_date
        "created_at, NULL, createdAt", // case#42: created_at
        "updated_at, NULL, updatedAt", // case#43: updated_at
        "NULL, asc, operatorId", // case#44: default order by operator_id asc
        "NULL, desc, operatorId", // case#45: default order by operator_id desc
        "NULL, NULL, operatorId", // case#46: default order by operator_id asc
        "operator_id, asc, operatorId", // case#47: order by operator_id asc
        "operator_name, DESC, operatorName", // case#48: order by operator_name desc
    }, nullValues = "NULL")
    @Sql(statements = {
        "DELETE FROM auth.tbl_plants;",
        "DELETE FROM auth.tbl_operators;"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
        "INSERT INTO auth.tbl_operators (operator_id, operator_name, operator_address, open_operator_id, global_operator_id, deleted_flag, effective_start_date, effective_end_date, created_user_id, updated_user_id, created_at, updated_at) VALUES ("
                + "'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'テスト事業者001', '東京都渋谷区xxx', '1234567890001', '1234ABCD5678EFGH0001', false, '2025-08-20', '2099-12-31', 'manualuser', 'manualuser', now(), now()),"
                + "('54708446-d761-4071-9bc3-cfcc1108aada', 'テスト株式会社', '試験県テスト市examビル1F', '1234567890123', '123456789TT234567890', true, '2025-01-01', '2030-12-31', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', 'b39e6248-c888-56ca-d9d0-89de1b1adc8e', now(), now());"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testFindWithLimitOffset_sortByVariousFields(
            String orderByKey,
            String orderByDirection,
            String expectedFirstValue
    ) {
        // arrange
        Sort sort = SqlUtils.buildSort(OperatorEntity.class, orderByKey, orderByDirection, ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID, Sort.Direction.ASC);
        Specification<OperatorEntity> spec = SqlUtils.createSpecification();

        // act
        List<OperatorEntity> result = repository.findWithLimitOffset(spec, 0, 10, sort);

        // assert
        Object firstData = null;
        if (orderByKey != null && orderByDirection == null) {
            firstData = getFieldValue(result.get(0), expectedFirstValue);
            if (firstData instanceof String) {
                if (orderByKey.equals(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID)) {
                    assertEquals(result.get(0).getOperatorId(), "54708446-d761-4071-9bc3-cfcc1108aada");
                }
                if (orderByKey.equals(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_NAME)) {
                    assertEquals(result.get(0).getOperatorName(), "テスト事業者001");
                }
                if (orderByKey.equals(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ADDRESS)) {
                    assertEquals(result.get(0).getOperatorAddress(), "東京都渋谷区xxx");
                }
                if (orderByKey.equals(ConstSqlQueries.COLUMN_OPERATORS_OPEN_OPERATOR_ID)) {
                    assertEquals(result.get(0).getOpenOperatorId(), "1234567890001");
                }
                if (orderByKey.equals(ConstSqlQueries.COLUMN_OPERATORS_GLOBAL_OPERATOR_ID)) {
                    assertEquals(result.get(0).getGlobalOperatorId(), "123456789TT234567890");
                }
            } else if (firstData instanceof Boolean) {
                if (orderByKey.equals(ConstSqlQueries.COLUMN_COMMON_DELETED_FLAG)) {
                    assertEquals(result.get(0).isDeletedFlag(), false);
                    assertEquals(result.get(0).getOperatorId(), "b39e6248-c888-56ca-d9d0-89de1b1adc8e");
                }
            } else if (firstData instanceof LocalDate) {
                if (orderByKey.equals(ConstSqlQueries.COLUMN_COMMON_EFFECTIVE_START_DATE)) {
                    assertEquals(result.get(0).getEffectiveStartDate(), LocalDate.parse("2025-01-01"));
                }
                if (orderByKey.equals(ConstSqlQueries.COLUMN_COMMON_EFFECTIVE_END_DATE)) {
                    assertEquals(result.get(0).getEffectiveEndDate(), LocalDate.parse("2030-12-31"));
                }
            } else if (firstData instanceof LocalDateTime) {
                if (orderByKey.equals(ConstSqlQueries.COLUMN_COMMON_CREATED_AT)) {
                    // Since created_at uses DB's now(), just check it's not null
                    assertNotNull(result.get(0).getCreatedAt());
                }
                if (orderByKey.equals(ConstSqlQueries.COLUMN_COMMON_UPDATED_AT)) {
                    // Since created_at uses DB's now(), just check it's not null
                    assertNotNull(result.get(0).getUpdatedAt());
                }
            }
        } else if (orderByKey != null && orderByDirection != null) {
            firstData = getFieldValue(result.get(0), expectedFirstValue);
            if (orderByKey.equals(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID)) {
                assertEquals(result.get(0).getOperatorId(), "54708446-d761-4071-9bc3-cfcc1108aada");
            }
            if (orderByKey.equals(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_NAME)) {
                assertEquals(result.get(0).getOperatorName(), "テスト株式会社");
            }
        } else if (orderByKey == null && orderByDirection != null) {
            if ("asc".equals(orderByDirection)) {
                assertEquals(result.get(0).getOperatorId(), "54708446-d761-4071-9bc3-cfcc1108aada");
            } else {
                assertEquals(result.get(0).getOperatorId(), "b39e6248-c888-56ca-d9d0-89de1b1adc8e");
            }
        }  else if (orderByKey == null && orderByDirection == null) {
            assertEquals(result.get(0).getOperatorId(), "54708446-d761-4071-9bc3-cfcc1108aada");
        }
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    /**
     * Parameterized test for findWithLimitOffset repository failures.
     */
    @ParameterizedTest
    @CsvSource({
        "IllegalArgumentException", // case#46: IllegalArgumentException
        "OutOfServiceException", // case#47: OutOfServiceException
        "UnexpectedException", // case#48: UnexpectedException
    })
    void testFindWithLimitOffset_throwsException(
            String exceptionClassName
    ) {
        // mock
        repository = mock(OperatorRepositoryCustomImpl.class);

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

        Sort sort = SqlUtils.buildSort(OperatorEntity.class, null, null, ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID, Sort.Direction.ASC);
        Specification<OperatorEntity> spec = SqlUtils.createSpecification();

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
    * Utility to get the value of any field in OperatorEntity.
    */
    private Object getFieldValue(OperatorEntity entity, String fieldName) {
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