/*
 * ListOperatorRequestTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies the behavior and validation logic
 * of the ListOperatorRequest DTO.
 *
 * Date: 2025-08-31
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstSqlQueries;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * ListOperatorRequestTest is a test class for the ListOperatorRequest.
 */
public class ListOperatorRequestTest {

    private static Validator validator;
    private ListOperatorRequest request;
    ListOperatorRequest.SortKey sortKey;

    /**
     * setUp initializes the test objects before each test.
     */
    @BeforeEach
    void setUp() {
        request = new ListOperatorRequest();
        sortKey = new ListOperatorRequest.SortKey();
    }

    /**
     * setUpValidator initializes the Validator instance before all tests.
     */
    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * case#1:
     * testGetterSetter tests the getter and setter methods of ListOperatorRequest.
     */
    @Test
    void testGetterSetter() {
        Integer count = 10;
        Integer index = 2;
        String operatorId = "123e4567-e89b-12d3-a456-426614174000";
        String operatorName = "OperatorTest";
        String operatorAddress = "Tokyo";
        String openOperatorId = "OPEN-123456";
        String globalOperatorId = "GLOBAL-123456";
        String effectiveDate = "2025-06-30";
        String deletedFlag = "true";
        sortKey.setKey(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID);
        sortKey.setOrder("asc");

        request.setCount(count);
        request.setIndex(index);
        request.setOperatorId(operatorId);
        request.setOperatorName(operatorName);
        request.setOperatorAddress(operatorAddress);
        request.setOpenOperatorId(openOperatorId);
        request.setGlobalOperatorId(globalOperatorId);
        request.setEffectiveDate(effectiveDate);
        request.setDeletedFlag(deletedFlag);
        request.setSort(sortKey);

        assertEquals(count, request.getCount());
        assertEquals(index, request.getIndex());
        assertEquals(operatorId, request.getOperatorId());
        assertEquals(operatorName, request.getOperatorName());
        assertEquals(operatorAddress, request.getOperatorAddress());
        assertEquals(openOperatorId, request.getOpenOperatorId());
        assertEquals(globalOperatorId, request.getGlobalOperatorId());
        assertEquals(effectiveDate, request.getEffectiveDate());
        assertEquals(Boolean.valueOf(deletedFlag), request.getDeletedFlag());
        assertEquals(sortKey, request.getSort());

        // Test SortKey getters/setters
        sortKey.setKey("sortKey");
        sortKey.setOrder("DESC");
        assertEquals("sortKey", sortKey.getKey());
        assertEquals("DESC", sortKey.getOrder());
    }

    /**
     * testCountValidation tests the validation logic
     * for various count values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
            // count, expectErrorMessage, isValid
            "0, NULL, true", // case#2: count is normal
            "100, NULL, true", // case#3: count is normal
            "-1, %s: the value must be at least 0, false", // case#4: count is negative
            "NULL, %s: is required, false" // case#5: count is null
    }, nullValues = "NULL")
    void testCountValidation(Integer count, String errStr, boolean isValid) {
        request.setCount(count);
        request.setIndex(0);
        request.setOperatorId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setOperatorName("テスト株式会社");
        request.setOperatorAddress("試験県テスト市examビル1F");
        request.setOpenOperatorId("1234567890123");
        request.setGlobalOperatorId("123456789TT234567890");
        request.setEffectiveDate("2025-01-01");
        request.setDeletedFlag("true");
        sortKey.setKey(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID);
        sortKey.setOrder("asc");
        request.setSort(sortKey);

        // assert
        Set<ConstraintViolation<ListOperatorRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty());
        } else {
            assertTrue(violations.stream().anyMatch(v -> "count".equals(v.getPropertyPath().toString())));
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * testIndexValidation tests the validation logic
     * for various index values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
            // index, expectErrorMessage, isValid
            "0, NULL, true", // case#6: index is normal
            "100, NULL, true", // case#7: index is normal
            "-1, %s: the value must be at least 0, false", // case#8: index is negative
            "NULL, %s: is required, false" // case#9: index is null
    }, nullValues = "NULL")
    void testIndexValidation(Integer index, String errStr, boolean isValid) {
        request.setCount(0);
        request.setIndex(index);
        request.setOperatorId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setOperatorName("テスト株式会社");
        request.setOperatorAddress("試験県テスト市examビル1F");
        request.setOpenOperatorId("1234567890123");
        request.setGlobalOperatorId("123456789TT234567890");
        request.setEffectiveDate("2025-01-01");
        request.setDeletedFlag("true");
        sortKey.setKey(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID);
        sortKey.setOrder("asc");
        request.setSort(sortKey);

        Set<ConstraintViolation<ListOperatorRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty());
        } else {
            assertTrue(violations.stream().anyMatch(v -> "index".equals(v.getPropertyPath().toString())));
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * testSortKeyValidation tests the validation logic
     * for various sort key values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // sortKey, sortOrder, expectErrorMessage, isValid
        "operator_name, asc, NULL, true", // case#10: key is normal, order is normal
        "operatorName, NULL, NULL, true", // case#11: key is normal(invalid enum), order is null
        "operator_name, NULL, NULL, true", // case#12: key is normal, order is null
        "NULL, NULL, NULL, true", // case#13: key is null, order is null
        "'', NULL, NULL, true", // case#14: key is empty, order is null
        "'  ', NULL, NULL, true", // case#15: key is blank spaces, order is null
        "NULL, asc, NULL, true", // case#16: key is null, order is normal
        "NULL, desc, NULL, true", // case#17: key is null, order is normal
        "NULL, ASC, %s: the length or characters are invalid, false", // case#18: key is null, order is invalid enum(upper case)
        "NULL, DESC, %s: the length or characters are invalid, false", // case#19: key is null, order is invalid enum(upper case)
        "NULL, INVALID, %s: the length or characters are invalid, false", // case#20: key is null, order is invalid enum
        "NULL, '', %s: the length or characters are invalid, false", // case#21: key is null, order is empty
    }, nullValues = "NULL")
    void testSortKeyValidation(String key, String order, String errStr, boolean isValid) {
        request.setCount(0);
        request.setIndex(0);
        request.setOperatorId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setOperatorName("テスト株式会社");
        request.setOperatorAddress("試験県テスト市examビル1F");
        request.setOpenOperatorId("1234567890123");
        request.setGlobalOperatorId("123456789TT234567890");
        request.setEffectiveDate("2025-01-01");
        request.setDeletedFlag("true");
        sortKey.setKey(key);
        sortKey.setOrder(order);
        request.setSort(sortKey);

        Set<ConstraintViolation<ListOperatorRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty());
        } else {
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().startsWith("sort")));
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * testOperatorIdValidation tests the operatorId validation logic
     * for various input values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // operatorId, expectErrorMessage, isValid
        "123e4567-e89b-12d3-a456-426614174000, NULL, true", // case#22: operatorId is normal
        "NULL, NULL, true", // case#23: operatorId is null
        "'', %s: invalid UUID format, false", // case#24: operatorId is empty
        "'  ', %s: invalid UUID format, false", // case#25: operatorId is blank spaces
        "123e456G-e89b-12d3-a456-426614, %s: invalid UUID format, false", // case#26: operatorId is invalid UUID（short length）
        "123e456G-e89b-12d3-a456-426614174000, %s: invalid UUID format, false", // case#27: operatorId is invalid UUID（first group）
        "123e4567-e89G-12d3-a456-426614174000, %s: invalid UUID format, false", // case#28: operatorId is invalid UUID（second group）
        "123e4567-e89b-12dG-a456-426614174000, %s: invalid UUID format, false", // case#29: operatorId is invalid UUID（third group）
        "123e4567-e89b-12d3-a456-42661417400G, %s: invalid UUID format, false", // case#30: operatorId is invalid UUID（last group）
        "'  123e4567-e89b-12d3-a456-426614174000    ', %s: invalid UUID format, false" // case#31: operatorId includes blank spaces before and after
    }, nullValues = "NULL")
    void testOperatorIdValidation(String operatorId, String errStr, boolean isValid) {
        request.setCount(0);
        request.setIndex(0);
        request.setOperatorId(operatorId);
        request.setOperatorName("テスト株式会社");
        request.setOperatorAddress("試験県テスト市examビル1F");
        request.setOpenOperatorId("1234567890123");
        request.setGlobalOperatorId("123456789TT234567890");
        request.setEffectiveDate("2025-01-01");
        request.setDeletedFlag("true");
        sortKey.setKey(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID);
        sortKey.setOrder("asc");
        request.setSort(sortKey);

        // Assert
        Set<ConstraintViolation<ListOperatorRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty());
        } else {
            assertTrue(violations.stream().anyMatch(v -> "operatorId".equals(v.getPropertyPath().toString())));
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * testOperatorNameValidation tests the validation logic
     * for various operatorName values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // operatorName, expectErrorMessage, isValid
        "株式会社A社, NULL, true", // case#32: operatorName is normal
        "NULL, NULL, true", // case#33: operatorName is null
        "A, NULL, true", // case#34: operatorName is min length 1 characters
        "LONG, NULL, true", // case#35: operatorName is max length 255 characters
        "'  ', NULL, true", // case#36: operatorName is blank spaces
        "'', %s: the length must be between 1 and 255, false", // case#37: operatorName is empty（less than min length 0 characters）
        "MORELONG, %s: the length must be between 1 and 255, false" // case#38: operatorName is more than max length 256 characters
    }, nullValues = "NULL")
    void testOperatorNameValidation(String operatorName, String errStr, boolean isValid) {
        operatorName = "LONG".equals(operatorName) ? "A".repeat(Const.OPERATOR_NAME_LENGTH_MAX) :
                ("MORELONG".equals(operatorName) ? "A".repeat(Const.OPERATOR_NAME_LENGTH_MAX + 1) : operatorName);

        request.setCount(0);
        request.setIndex(0);
        request.setOperatorId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setOperatorName(operatorName);
        request.setOperatorAddress("試験県テスト市examビル1F");
        request.setOpenOperatorId("1234567890123");
        request.setGlobalOperatorId("123456789TT234567890");
        request.setEffectiveDate("2025-01-01");
        request.setDeletedFlag("true");
        sortKey.setKey(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID);
        sortKey.setOrder("asc");
        request.setSort(sortKey);

        Set<ConstraintViolation<ListOperatorRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty());
        } else {
            assertTrue(violations.stream().anyMatch(v -> "operatorName".equals(v.getPropertyPath().toString())));
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * testOperatorAddressValidation tests the validation logic
     * for various operatorAddress values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // operatorAddress, expectErrorMessage, isValid
        "'Tokyo, Japan', NULL, true", // case#39: operatorAddress is normal
        "NULL, NULL, true", // case#40: operatorAddress is null
        "1, NULL, true", // case#41: operatorAddress is min length 1 character
        "LONG, NULL, true", // case#42: operatorAddress is max length 256 characters
        "'  ', NULL, true", // case#43: operatorAddress is blank spaces
        "'', %s: the length must be between 1 and 256, false", // case#44: operatorAddress is empty（less than min length 0 characters）
        "MORELONG, %s: the length must be between 1 and 256, false", // case#45: operatorAddress is more than max length 256 characters
    }, nullValues = "NULL")
    void testOperatorAddressValidation(String operatorAddress, String errStr, boolean isValid) {
        operatorAddress = "LONG".equals(operatorAddress) ? "A".repeat(Const.OPERATOR_ADDRESS_LENGTH_MAX) :
                ("MORELONG".equals(operatorAddress) ? "A".repeat(Const.OPERATOR_ADDRESS_LENGTH_MAX + 1) : operatorAddress);

        request.setCount(0);
        request.setIndex(0);
        request.setOperatorId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setOperatorName("テスト株式会社");
        request.setOperatorAddress(operatorAddress);
        request.setOpenOperatorId("1234567890123");
        request.setGlobalOperatorId("123456789TT234567890");
        request.setEffectiveDate("2025-01-01");
        request.setDeletedFlag("true");
        sortKey.setKey(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID);
        sortKey.setOrder("asc");
        request.setSort(sortKey);

        Set<ConstraintViolation<ListOperatorRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty());
        } else {
            assertTrue(violations.stream().anyMatch(v -> "operatorAddress".equals(v.getPropertyPath().toString())));
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * testOpenOperatorIdValidation tests the validation logic
     * for various openOperatorId values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // openOperatorId, expectErrorMessage, isValid
        "012320000, NULL, true", // case#46: openOperatorId is normal
        "NULL, NULL, true", // case#47: openOperatorId is null
        "a, NULL, true", // case#48: openOperatorId is min length 1 character
        "openOperatorId012320, NULL, true", // case#49: openOperatorId is max length 20 characters
        "'  ', %s: the length or characters are invalid, false", // case#50: openOperatorId is blank spaces
        "'', %s: the length or characters are invalid, false", // case#51: openOperatorId is empty（less than min length 0 characters）
        "openOperatorId0123421, %s: the length or characters are invalid, false", // case#52: openOperatorId is more than max length 21 characters
    }, nullValues = "NULL")
    void testOpenOperatorIdValidation(String openOperatorId, String errStr, boolean isValid) {
        request.setCount(0);
        request.setIndex(0);
        request.setOperatorId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setOperatorName("テスト株式会社");
        request.setOperatorAddress("試験県テスト市examビル1F");
        request.setOpenOperatorId(openOperatorId);
        request.setGlobalOperatorId("123456789TT234567890");
        request.setEffectiveDate("2025-01-01");
        request.setDeletedFlag("true");
        sortKey.setKey(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID);
        sortKey.setOrder("asc");
        request.setSort(sortKey);

        Set<ConstraintViolation<ListOperatorRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty());
        } else {
            assertTrue(violations.stream().anyMatch(v -> "openOperatorId".equals(v.getPropertyPath().toString())));
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * testGlobalOperatorIdValidation tests the validation logic
     * for various globalOperatorId values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // globalOperatorId, expectErrorMessage, isValid
        "123456789TT234567890, NULL, true", // case#53: globalOperatorId is normal
        "NULL, NULL, true", // case#54: globalOperatorId is null
        "'', NULL, true", // case#55: globalOperatorId is empty（less than min length 0 characters）
        "LONG, NULL, true", // case#56: globalOperatorId is max length 256 characters
        "'  ', NULL, true", // case#57: globalOperatorId is blank spaces
        "MORELONG, %s: the length must be between 0 and 256, false", // case#58: globalOperatorId is more than max length 257 characters
    }, nullValues = "NULL")
    void testGlobalOperatorIdValidation(String globalOperatorId, String errStr, boolean isValid) {
        globalOperatorId = "LONG".equals(globalOperatorId) ? "A".repeat(Const.GLOBAL_OPERATOR_ID_LENGTH_MAX) :
                ("MORELONG".equals(globalOperatorId) ? "A".repeat(Const.GLOBAL_OPERATOR_ID_LENGTH_MAX + 1) : globalOperatorId);

        request.setCount(0);
        request.setIndex(0);
        request.setOperatorId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setOperatorName("テスト株式会社");
        request.setOperatorAddress("試験県テスト市examビル1F");
        request.setOpenOperatorId("1234567890123");
        request.setGlobalOperatorId(globalOperatorId);
        request.setEffectiveDate("2025-01-01");
        request.setDeletedFlag("true");
        sortKey.setKey(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID);
        sortKey.setOrder("asc");
        request.setSort(sortKey);

        Set<ConstraintViolation<ListOperatorRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty());
        } else {
            assertTrue(violations.stream().anyMatch(v -> "globalOperatorId".equals(v.getPropertyPath().toString())));
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * testEffectiveDateValidation tests the validation logic
     * for various effectiveDate values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // effectiveDate, expectErrorMessage, isValid
        "2025-06-30, NULL, true", // case#59: effectiveDate is normal
        "NULL, NULL, true", // case#60: effectiveDate is null
        "2025-06-35, '%s: invalid date format, expected pattern yyyy-MM-dd', false", // case#61: effectiveDate is invalid date
        "2025/06/30, '%s: invalid date format, expected pattern yyyy-MM-dd', false", // case#62: effectiveDate is invalid format
        "30-06-2025, '%s: invalid date format, expected pattern yyyy-MM-dd', false", // case#63: effectiveDate is invalid format
        "'', '%s: invalid date format, expected pattern yyyy-MM-dd', false", // case#64: effectiveDate is empty
    }, nullValues = "NULL")
    void testEffectiveDateValidation(String effectiveDate, String errStr, boolean isValid) {
        request.setCount(0);
        request.setIndex(0);
        request.setOperatorId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setOperatorName("テスト株式会社");
        request.setOperatorAddress("試験県テスト市examビル1F");
        request.setOpenOperatorId("1234567890123");
        request.setGlobalOperatorId("123456789TT234567890");
        request.setEffectiveDate(effectiveDate);
        request.setDeletedFlag("true");
        sortKey.setKey(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID);
        sortKey.setOrder("asc");
        request.setSort(sortKey);

        Set<ConstraintViolation<ListOperatorRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty());
        } else {
            assertTrue(violations.stream().anyMatch(v -> "effectiveDate".equals(v.getPropertyPath().toString())));
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * testDeletedFlagValidation tests the validation logic
     * for various deletedFlag values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // deletedFlag, expectErrorMessage, isValid
        "true, NULL, true", // case#65: deletedFlag is true
        "false, NULL, true", // case#66: deletedFlag is false
        "TRUE, NULL, true", // case#67: deletedFlag is TRUE(upper case)
        "FALSE, NULL, true", // case#68: deletedFlag is FALSE(upper case)
        "NULL, NULL, true", // case#69: deletedFlag is null
    }, nullValues = "NULL")
    void testDeletedFlagValidation(String deletedFlag, String errStr, boolean isValid) {
        request.setCount(0);
        request.setIndex(0);
        request.setOperatorId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setOperatorName("テスト株式会社");
        request.setOperatorAddress("試験県テスト市examビル1F");
        request.setOpenOperatorId("1234567890123");
        request.setGlobalOperatorId("123456789TT234567890");
        request.setEffectiveDate("2025-01-01");
        request.setDeletedFlag(deletedFlag);
        sortKey.setKey(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID);
        sortKey.setOrder("asc");
        request.setSort(sortKey);

        Set<ConstraintViolation<ListOperatorRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty());
        } else {
            assertTrue(violations.stream().anyMatch(v -> "deletedFlag".equals(v.getPropertyPath().toString())));
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * case#70:
     * Test for the toString method of PutOperatorRequest.
     */
    @Test
    void testToString() {
        // Arrange
        request.setCount(10);
        request.setIndex(1);
        request.setOperatorId("user123");
        request.setOperatorName("Operator Name");
        request.setOperatorAddress("123 Main St");
        request.setOpenOperatorId("open123");
        request.setGlobalOperatorId("global123");
        request.setEffectiveDate("2025-08-30");
        request.setDeletedFlag("false");

        sortKey.setKey("operator_id");
        sortKey.setOrder("asc");
        request.setSort(sortKey);

        // Act
        String result = request.toString();

        // Assert
        assertTrue(result.contains("10"), "toString should include count");
        assertTrue(result.contains("1"), "toString should include index");
        assertTrue(result.contains("user123"), "toString should include operatorId");
        assertTrue(result.contains("Operator Name"), "toString should include operatorName");
        assertTrue(result.contains("123 Main St"), "toString should include operatorAddress");
        assertTrue(result.contains("open123"), "toString should include openOperatorId");
        assertTrue(result.contains("global123"), "toString should include globalOperatorId");
        assertTrue(result.contains("2025-08-30"), "toString should include effectiveDate");
        assertTrue(result.contains("false"), "toString should include deletedFlag");
        assertTrue(result.contains("operator_id"), "toString should include sort key");
        assertTrue(result.contains("asc"), "toString should include sort order");
    }

    /**
     * case#71:
     * Test for equals and hashCode methods of ListOperatorRequest.
     */
    @Test
    void testEqualsAndHashCode() {
        // Arrange
        ListOperatorRequest request1 = new ListOperatorRequest();
        ListOperatorRequest request2 = new ListOperatorRequest();
        ListOperatorRequest request3 = new ListOperatorRequest();

        ListOperatorRequest.SortKey sortKey1 = new ListOperatorRequest.SortKey();
        sortKey1.setKey("operator_id");
        sortKey1.setOrder("asc");

        ListOperatorRequest.SortKey sortKey2 = new ListOperatorRequest.SortKey();
        sortKey2.setKey("operator_id");
        sortKey2.setOrder("asc");

        ListOperatorRequest.SortKey sortKey3 = new ListOperatorRequest.SortKey();
        sortKey3.setKey("operator_name");
        sortKey3.setOrder("desc");

        // Set same values for request1 and request2
        request1.setCount(10);
        request1.setIndex(1);
        request1.setOperatorId("user123");
        request1.setOperatorName("Operator Name");
        request1.setOperatorAddress("123 Main St");
        request1.setOpenOperatorId("open123");
        request1.setGlobalOperatorId("global123");
        request1.setEffectiveDate("2025-08-30");
        request1.setDeletedFlag("false");
        request1.setSort(sortKey1);

        request2.setCount(10);
        request2.setIndex(1);
        request2.setOperatorId("user123");
        request2.setOperatorName("Operator Name");
        request2.setOperatorAddress("123 Main St");
        request2.setOpenOperatorId("open123");
        request2.setGlobalOperatorId("global123");
        request2.setEffectiveDate("2025-08-30");
        request2.setDeletedFlag("false");
        request2.setSort(sortKey2);

        // Set different values for request3
        request3.setCount(20);
        request3.setIndex(2);
        request3.setOperatorId("user456");
        request3.setOperatorName("Operator2");
        request3.setOperatorAddress("456 Main St");
        request3.setOpenOperatorId("open456");
        request3.setGlobalOperatorId("global456");
        request3.setEffectiveDate("2025-09-01");
        request3.setDeletedFlag("true");
        request3.setSort(sortKey3);

        // Act & Assert
        // Equals tests
        assertEquals(request1, request2, "Objects with the same values should be equal.");
        assertNotEquals(request1, request3, "Objects with different values should not be equal.");

        // HashCode tests
        assertEquals(request1.hashCode(), request2.hashCode(),
                "Objects with the same values should have the same hash code.");
        assertNotEquals(request1.hashCode(), request3.hashCode(),
                "Objects with different values should have different hash codes.");
    }

    /**
     * case#72:
     * testJsonSerialization tests the JSON serialization and deserialization of ListOperatorRequest.
     */
    @Test
    void testJsonSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Set value
        request.setCount(0);
        request.setIndex(0);
        request.setOperatorId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setOperatorName("テスト株式会社");
        request.setOperatorAddress("試験県テスト市examビル1F");
        request.setOpenOperatorId("1234567890123");
        request.setGlobalOperatorId("123456789TT234567890");
        request.setEffectiveDate("2025-01-01");
        request.setDeletedFlag("true");
        sortKey.setKey(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID);
        sortKey.setOrder("asc");
        request.setSort(sortKey);

        String inputJson = "{"
                + "\"" + Const.JSON_PROPERTY_COUNT + "\":0,"
                + "\"" + Const.JSON_PROPERTY_INDEX + "\":0,"
                + "\"" + Const.JSON_PROPERTY_SORT + "\":{"
                + "\"" + Const.JSON_PROPERTY_SORT_KEY + "\":\"operator_id\","
                + "\"" + Const.JSON_PROPERTY_SORT_ORDER + "\":\"asc\""
                + "},"
                + "\"" + Const.JSON_PROPERTY_OPERATOR_ID + "\":\"d9a38406-cae2-4679-b052-15a75f5531e6\","
                + "\"" + Const.JSON_PROPERTY_OPERATOR_NAME + "\":\"テスト株式会社\","
                + "\"" + Const.JSON_PROPERTY_OPERATOR_ADDRESS + "\":\"試験県テスト市examビル1F\","
                + "\"" + Const.JSON_PROPERTY_OPEN_OPERATOR_ID + "\":\"1234567890123\","
                + "\"" + Const.JSON_PROPERTY_GLOBAL_OPERATOR_ID + "\":\"123456789TT234567890\","
                + "\"" + Const.JSON_PROPERTY_EFFECTIVE_DATE + "\":\"2025-01-01\","
                + "\"" + Const.JSON_PROPERTY_DELETED_FLAG + "\":true"
                + "}";

        // Serialize
        String json = mapper.writeValueAsString(request);
        assertTrue(json.contentEquals(inputJson));

        // Deserialize
        ListOperatorRequest input = mapper.readValue(inputJson, ListOperatorRequest.class);

        // Assert
        assertEquals(input.getCount(), request.getCount());
        assertEquals(input.getIndex(), request.getIndex());
        assertEquals(input.getOperatorId(), request.getOperatorId());
        assertEquals(input.getOperatorName(), request.getOperatorName());
        assertEquals(input.getOperatorAddress(), request.getOperatorAddress());
        assertEquals(input.getOpenOperatorId(), request.getOpenOperatorId());
        assertEquals(input.getGlobalOperatorId(), request.getGlobalOperatorId());
        assertEquals(input.getEffectiveDate(), request.getEffectiveDate());
        assertEquals(input.getDeletedFlag(), request.getDeletedFlag());
        assertEquals(input.getSort().getKey(), request.getSort().getKey());
        assertEquals(input.getSort().getOrder(), request.getSort().getOrder());
    }
}