/*
 * ListPlantRequestTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies the behavior and validation logic
 * of the ListPlantRequest DTO.
 *
 * Date: 2025/09/22
 */

package io.github.open_dataspaces.core.domain.dto;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
 * ListPlantRequestTest is a test class for the ListPlantRequest DTO.
 */
public class ListPlantRequestTest {

    private static Validator validator;
    private ListPlantRequest request;
    ListPlantRequest.SortKey sortKey;

    /**
     * setUp initializes the test objects before each test.
     */
    @BeforeEach
    void setUp() {
        request = new ListPlantRequest();
        sortKey = new ListPlantRequest.SortKey();
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
     * testGetterSetter tests the getter and setter methods of ListPlantRequest.
     */
    @Test
    void testGetterSetter() {
        Integer count = 10;
        Integer index = 2;
        String operatorId = "123e4567-e89b-12d3-a456-426614174000";
        String plantId = "123e4567-e89b-12d3-a456-426614174000";
        String plantName = "PlantTest";
        String plantAddress = "Tokyo";
        String effectiveDate = "2025-06-30";
        Boolean deletedFlag = Boolean.TRUE;
        sortKey.setKey(ConstSqlQueries.COLUMN_PLANTS_PLANT_ID);
        sortKey.setOrder("asc");

        request.setCount(count);
        request.setIndex(index);
        request.setOperatorId(operatorId);
        request.setPlantId(plantId);
        request.setPlantName(plantName);
        request.setPlantAddress(plantAddress);
        request.setEffectiveDate(effectiveDate);
        request.setDeletedFlag(deletedFlag);
        request.setSort(sortKey);

        assertEquals(count, request.getCount());
        assertEquals(index, request.getIndex());
        assertEquals(plantId, request.getPlantId());
        assertEquals(plantName, request.getPlantName());
        assertEquals(plantAddress, request.getPlantAddress());
        assertEquals(effectiveDate, request.getEffectiveDate());
        assertEquals(deletedFlag, request.getDeletedFlag());
        assertEquals(sortKey, request.getSort());

        sortKey.setKey("sortKey");
        sortKey.setOrder("DESC");
        assertEquals("sortKey", sortKey.getKey());
        assertEquals("DESC", sortKey.getOrder());
    }

    /**
     * Verifying the Default Constructor of ListPlantRequest.
     */
    @Test
    void testDefaultConstructor() {
        ListPlantRequest request = new ListPlantRequest();
        assertNull(request.getCount());
        assertNull(request.getIndex());
        assertNull(request.getOperatorId());
        assertNull(request.getPlantId());
        assertNull(request.getOpenPlantId());
        assertNull(request.getGlobalPlantId());
        assertNull(request.getPlantName());
        assertNull(request.getPlantAddress());
        assertNull(request.getEffectiveDate());
        assertNull(request.getDeletedFlag());
        assertNotEquals(null, request.getSort());
        assertNull(request.getSort().getKey());
        assertNull(request.getSort().getOrder());
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
        request.setOperatorId("0bbb8463-cd21-4b91-8e5e-2535ea27ab1b");
        request.setPlantId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setPlantName("テストプラント");
        request.setPlantAddress("試験県テスト市examビル1F");
        request.setEffectiveDate("2025-01-01");
        request.setDeletedFlag(Boolean.TRUE);
        sortKey.setKey(ConstSqlQueries.COLUMN_PLANTS_PLANT_ID);
        sortKey.setOrder("asc");
        request.setSort(sortKey);

        // assert
        Set<ConstraintViolation<ListPlantRequest>> violations = validator.validate(request);
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
        request.setOperatorId("0bbb8463-cd21-4b91-8e5e-2535ea27ab1b");
        request.setPlantId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setPlantName("テストプラント");
        request.setPlantAddress("試験県テスト市examビル1F");
        request.setEffectiveDate("2025-01-01");
        request.setDeletedFlag(Boolean.TRUE);
        sortKey.setKey(ConstSqlQueries.COLUMN_PLANTS_PLANT_ID);
        sortKey.setOrder("asc");
        request.setSort(sortKey);

        Set<ConstraintViolation<ListPlantRequest>> violations = validator.validate(request);
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
        "plant_name, asc, NULL, true", // case#10: key is normal, order is normal
        "plantName, NULL, NULL, true", // case#11: key is normal(invalid enum), order is null
        "plant_name, NULL, NULL, true", // case#12: key is normal, order is null
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
        request.setOperatorId("0bbb8463-cd21-4b91-8e5e-2535ea27ab1b");
        request.setPlantId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setPlantName("テストプラント");
        request.setPlantAddress("試験県テスト市examビル1F");
        request.setEffectiveDate("2025-01-01");
        request.setDeletedFlag(Boolean.TRUE);
        sortKey.setKey(key);
        sortKey.setOrder(order);
        request.setSort(sortKey);

        Set<ConstraintViolation<ListPlantRequest>> violations = validator.validate(request);
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
        request.setPlantId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setPlantName("テストプラント");
        request.setPlantAddress("試験県テスト市examビル1F");
        request.setEffectiveDate("2025-01-01");
        request.setDeletedFlag(Boolean.TRUE);
        sortKey.setKey(ConstSqlQueries.COLUMN_PLANTS_PLANT_ID);
        sortKey.setOrder("asc");
        request.setSort(sortKey);

        // Assert
        Set<ConstraintViolation<ListPlantRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty());
        } else {
            assertTrue(violations.stream().anyMatch(v -> "operatorId".equals(v.getPropertyPath().toString())));
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * testPlantIdValidation tests the plantId validation logic
     * for various input values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // plantId, expectErrorMessage, isValid
        "123e4567-e89b-12d3-a456-426614174000, NULL, true", // case#32: plantId is normal
        "NULL, NULL, true", // case#33: plantId is null
        "'', %s: invalid UUID format, false", // case#34: plantId is empty
        "'  ', %s: invalid UUID format, false", // case#35: plantId is blank spaces
        "123e456G-e89b-12d3-a456-426614, %s: invalid UUID format, false", // case#36: plantId is invalid UUID（short length）
        "123e456G-e89b-12d3-a456-426614174000, %s: invalid UUID format, false", // case#37: plantId is invalid UUID（first group）
        "123e4567-e89G-12d3-a456-426614174000, %s: invalid UUID format, false", // case#38: plantId is invalid UUID（second group）
        "123e4567-e89b-12dG-a456-426614174000, %s: invalid UUID format, false", // case#39: plantId is invalid UUID（third group）
        "123e4567-e89b-12d3-a456-42661417400G, %s: invalid UUID format, false", // case#40: plantId is invalid UUID（last group）
        "'  123e4567-e89b-12d3-a456-426614174000    ', %s: invalid UUID format, false" // case#41: plantId includes blank spaces before and after
    }, nullValues = "NULL")
    void testPlantIdValidation(String plantId, String errStr, boolean isValid) {
        request.setCount(0);
        request.setIndex(0);
        request.setOperatorId("0bbb8463-cd21-4b91-8e5e-2535ea27ab1b");
        request.setPlantId(plantId);
        request.setPlantName("テストプラント");
        request.setPlantAddress("試験県テスト市examビル1F");
        request.setEffectiveDate("2025-01-01");
        request.setDeletedFlag(Boolean.TRUE);
        sortKey.setKey(ConstSqlQueries.COLUMN_PLANTS_PLANT_ID);
        sortKey.setOrder("asc");
        request.setSort(sortKey);

        Set<ConstraintViolation<ListPlantRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty());
        } else {
            assertTrue(violations.stream().anyMatch(v -> "plantId".equals(v.getPropertyPath().toString())));
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
        "012320000, NULL, true", // case#42: openOperatorId is normal
        "NULL, NULL, true", // case#43: openPlantId is null
        "123456, NULL, true", // case#44: openPlantId is min length 6 character
        "openPlantId012456780101226, NULL, true", // case#45: openPlantId is max length 26 characters
        "'  ', %s: the length or characters are invalid, false", // case#46: openPlantId is blank spaces
        "'', %s: the length or characters are invalid, false", // case#47: openPlantId is empty（less than min length 0 characters）
        "openPlantId0124567801012327, %s: the length must be between 6 and 26, false", // case#48: openPlantId is more than max length 26 characters
    }, nullValues = "NULL")
    void testOpenPlantIdValidation(String openPlantId, String errStr, boolean isValid) {
        request.setCount(0);
        request.setIndex(0);
        request.setOperatorId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setPlantId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setPlantName("テスト株式会社");
        request.setPlantAddress("試験県テスト市examビル1F");
        request.setOpenPlantId(openPlantId);
        request.setGlobalPlantId("123456789TT234567890");
        request.setEffectiveDate("2025-01-01");
        request.setDeletedFlag(Boolean.TRUE);
        sortKey.setKey(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID);
        sortKey.setOrder("asc");
        request.setSort(sortKey);

        Set<ConstraintViolation<ListPlantRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty());
        } else {
            assertTrue(violations.stream().anyMatch(v -> "openPlantId".equals(v.getPropertyPath().toString())));
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * testGlobalPlantIdValidation tests the validation logic
     * for various globalPlantId values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // globalPlantId, expectErrorMessage, isValid
        "123456789TT234567890, NULL, true", // case#49: globalPlantId is normal
        "NULL, NULL, true", // case#50: globalPlantId is null
        "'', NULL, true", // case#51: globalPlantId is empty（less than min length 0 characters）
        "LONG, NULL, true", // case#52: globalPlantId is max length 256 characters
        "'  ', NULL, true", // case#53: globalPlantId is blank spaces
        "MORELONG, %s: the length must be between 0 and 256, false", // case#54: globalPlantId is more than max length 257 characters
    }, nullValues = "NULL")
    void testGlobalPlantIdValidation(String globalPlantId, String errStr, boolean isValid) {
        globalPlantId = "LONG".equals(globalPlantId) ? "A".repeat(Const.GLOBAL_PLANT_ID_LENGTH_MAX) :
                ("MORELONG".equals(globalPlantId) ? "A".repeat(Const.GLOBAL_PLANT_ID_LENGTH_MAX + 1) : globalPlantId);

        request.setCount(0);
        request.setIndex(0);
        request.setOperatorId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setPlantId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setPlantName("テスト株式会社");
        request.setPlantAddress("試験県テスト市examビル1F");
        request.setOpenPlantId("1234567890123");
        request.setGlobalPlantId(globalPlantId);
        request.setEffectiveDate("2025-01-01");
        request.setDeletedFlag(Boolean.TRUE);
        sortKey.setKey(ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID);
        sortKey.setOrder("asc");
        request.setSort(sortKey);

        Set<ConstraintViolation<ListPlantRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty());
        } else {
            assertTrue(violations.stream().anyMatch(v -> "globalPlantId".equals(v.getPropertyPath().toString())));
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * testPlantNameValidation tests the plantName validation logic
     * for various input values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // plantName, expectErrorMessage, isValid
        "株式会社A社,, true", // case#55: plantName is normal
        "NULL,, true", // case#56: plantName is null
        "A,, true", // case#57: plantName is min length 1 characters
        "LONG,, true", // case#58: plantName is max length 258 characters
        "'  ',, true", // case#59: plantName is blank spaces
        "'', %s: the length must be between 1 and 256, false", // case#60: plantName is empty（less than min length 0 characters）
        "MORELONG, %s: the length must be between 1 and 256, false"  // case#61: plantName is more than max length 256 characters
    }, nullValues = "NULL")
    void testPlantNameValidation(String plantName, String errStr, boolean isValid) {
        plantName = "LONG".equals(plantName) ? "A".repeat(Const.PLANT_NAME_LENGTH_MAX) :
                ("MORELONG".equals(plantName) ? "A".repeat(Const.PLANT_NAME_LENGTH_MAX + 1) : plantName);
        request.setCount(0);
        request.setIndex(0);
        request.setOperatorId("0bbb8463-cd21-4b91-8e5e-2535ea27ab1b");
        request.setPlantId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setPlantName(plantName);
        request.setPlantAddress("試験県テスト市examビル1F");
        request.setEffectiveDate("2025-01-01");
        request.setDeletedFlag(Boolean.TRUE);
        sortKey.setKey(ConstSqlQueries.COLUMN_PLANTS_PLANT_ID);
        sortKey.setOrder("asc");
        request.setSort(sortKey);
        Set<ConstraintViolation<ListPlantRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty());
        } else {
            assertTrue(violations.stream().anyMatch(v -> "plantName".equals(v.getPropertyPath().toString())));
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * testPlantAddressValidation tests the plantAddress validation logic
     * for various input values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // plantAddress, expectErrorMessage, isValid
        "'Tokyo, Japan',, true", // case#62: plantAddress is normal
        "NULL,, true", // case#63: plantAddress is null
        "1,, true", // case#64: plantAddress is min length 1 character
        "LONG,, true", // case#65: plantAddress is max length 256 characters
        "'  ',, true", // case#66: plantAddress is blank spaces
        "'', %s: the length must be between 1 and 256, false",  // case#67: plantAddress is empty（less than min length 0 characters）
        "MORELONG, %s: the length must be between 1 and 256, false", // case#68: plantAddress is more than max length 256 characters
    }, nullValues = "NULL")
    void testPlantAddressValidation(String plantAddress, String errStr, boolean isValid) {
        plantAddress = "LONG".equals(plantAddress) ? "A".repeat(Const.PLANT_ADDRESS_LENGTH_MAX) :
                ("MORELONG".equals(plantAddress) ? "A".repeat(Const.PLANT_ADDRESS_LENGTH_MAX + 1) : plantAddress);
        request.setCount(0);
        request.setIndex(0);
        request.setOperatorId("0bbb8463-cd21-4b91-8e5e-2535ea27ab1b");
        request.setPlantId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setPlantName("テストプラント");
        request.setPlantAddress(plantAddress);
        request.setEffectiveDate("2025-01-01");
        request.setDeletedFlag(Boolean.TRUE);
        sortKey.setKey(ConstSqlQueries.COLUMN_PLANTS_PLANT_ID);
        sortKey.setOrder("asc");
        request.setSort(sortKey);
        Set<ConstraintViolation<ListPlantRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty());
        } else {
            assertTrue(violations.stream().anyMatch(v -> "plantAddress".equals(v.getPropertyPath().toString())));
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
        "2025-06-30, NULL, true", // case#69: effectiveDate is normal
        "NULL, NULL, true", // case#70: effectiveDate is null
        "2025-06-35, '%s: invalid date format, expected pattern yyyy-MM-dd', false", // case#71: effectiveDate is invalid date
        "2025/06/30, '%s: invalid date format, expected pattern yyyy-MM-dd', false", // case#72: effectiveDate is invalid format
        "30-06-2025, '%s: invalid date format, expected pattern yyyy-MM-dd', false", // case#73: effectiveDate is invalid format
        "'', '%s: invalid date format, expected pattern yyyy-MM-dd', false", // case#74: effectiveDate is empty
    }, nullValues = "NULL")
    void testEffectiveDateValidation(String effectiveDate, String errStr, boolean isValid) {
        request.setCount(0);
        request.setIndex(0);
        request.setOperatorId("0bbb8463-cd21-4b91-8e5e-2535ea27ab1b");
        request.setPlantId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setPlantName("テストプラント");
        request.setPlantAddress("試験県テスト市examビル1F");
        request.setEffectiveDate(effectiveDate);
        request.setDeletedFlag(Boolean.TRUE);
        sortKey.setKey(ConstSqlQueries.COLUMN_PLANTS_PLANT_ID);
        sortKey.setOrder("asc");
        request.setSort(sortKey);
        Set<ConstraintViolation<ListPlantRequest>> violations = validator.validate(request);
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
        "true, NULL, true", // case#75: deletedFlag is true
        "false, NULL, true", // case#76: deletedFlag is false
        "TRUE, NULL, true", // case#77: deletedFlag is TRUE(upper case)
        "FALSE, NULL, true", // case#78: deletedFlag is FALSE(upper case)
        "NULL, NULL, true", // case#79: deletedFlag is null
    }, nullValues = "NULL")
    void testDeletedFlagValidation(Boolean deletedFlag, String errStr, boolean isValid) {
        request.setCount(0);
        request.setIndex(0);
        request.setOperatorId("0bbb8463-cd21-4b91-8e5e-2535ea27ab1b");
        request.setPlantId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setPlantName("テストプラント");
        request.setPlantAddress("試験県テスト市examビル1F");
        request.setEffectiveDate("2025-01-01");
        request.setDeletedFlag(deletedFlag);
        sortKey.setKey(ConstSqlQueries.COLUMN_PLANTS_PLANT_ID);
        sortKey.setOrder("asc");
        request.setSort(sortKey);
        Set<ConstraintViolation<ListPlantRequest>> violations = validator.validate(request);
        if (isValid) {
            assertTrue(violations.isEmpty());
        } else {
            assertTrue(violations.stream().anyMatch(v -> "deletedFlag".equals(v.getPropertyPath().toString())));
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * case#80:
     * Test for the toString method of PutPlantRequest.
     */
    @Test
    void testToString() {
        // Arrange
        request.setCount(10);
        request.setIndex(1);
        request.setOperatorId("user123");
        request.setPlantId("user123");
        request.setPlantName("Operator Name");
        request.setPlantAddress("123 Main St");
        request.setEffectiveDate("2025-08-30");
        request.setDeletedFlag(Boolean.FALSE);

        sortKey.setKey("operator_id");
        sortKey.setOrder("asc");
        request.setSort(sortKey);

        // Act
        String result = request.toString();

        // Assert
        assertTrue(result.contains("10"), "toString should include count");
        assertTrue(result.contains("1"), "toString should include index");
        assertTrue(result.contains("user123"), "toString should include operatorId");
        assertTrue(result.contains("user123"), "toString should include plantId");
        assertTrue(result.contains("Operator Name"), "toString should include operatorName");
        assertTrue(result.contains("123 Main St"), "toString should include operatorAddress");
        assertTrue(result.contains("2025-08-30"), "toString should include effectiveDate");
        assertTrue(result.contains("false"), "toString should include deletedFlag");
        assertTrue(result.contains("operator_id"), "toString should include sort key");
        assertTrue(result.contains("asc"), "toString should include sort order");
    }

    /**
     * case#81:
     * Test for equals and hashCode methods of ListPlantRequest.
     */
    @Test
    void testEqualsAndHashCode() {
        // Arrange
        ListPlantRequest request1 = new ListPlantRequest();
        ListPlantRequest request2 = new ListPlantRequest();
        ListPlantRequest request3 = new ListPlantRequest();

        ListPlantRequest.SortKey sortKey1 = new ListPlantRequest.SortKey();
        sortKey1.setKey("operator_id");
        sortKey1.setOrder("asc");

        ListPlantRequest.SortKey sortKey2 = new ListPlantRequest.SortKey();
        sortKey2.setKey("operator_id");
        sortKey2.setOrder("asc");

        ListPlantRequest.SortKey sortKey3 = new ListPlantRequest.SortKey();
        sortKey3.setKey("operator_name");
        sortKey3.setOrder("desc");

        // Set same values for request1 and request2
        request1.setCount(10);
        request1.setIndex(1);
        request1.setOperatorId("user123");
        request1.setPlantId("user123");
        request1.setPlantName("Operator Name");
        request1.setPlantAddress("123 Main St");
        request1.setEffectiveDate("2025-08-30");
        request1.setDeletedFlag(Boolean.FALSE);
        request1.setSort(sortKey1);

        request2.setCount(10);
        request2.setIndex(1);
        request2.setOperatorId("user123");
        request2.setPlantId("user123");
        request2.setPlantName("Operator Name");
        request2.setPlantAddress("123 Main St");
        request2.setEffectiveDate("2025-08-30");
        request2.setDeletedFlag(Boolean.FALSE);
        request2.setSort(sortKey2);

        // Set different values for request3
        request3.setCount(20);
        request3.setIndex(2);
        request3.setOperatorId("user456");
        request3.setPlantId("user456");
        request3.setPlantName("Operator2");
        request3.setPlantAddress("456 Main St");
        request3.setEffectiveDate("2025-09-01");
        request3.setDeletedFlag(Boolean.TRUE);
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
     * case#82:
     * testJsonSerialization tests the JSON serialization and deserialization of ListPlantRequest.
     */
    @Test
    void testJsonSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Set value
        request.setCount(0);
        request.setIndex(0);
        request.setOperatorId("0bbb8463-cd21-4b91-8e5e-2535ea27ab1b");
        request.setPlantId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setOpenPlantId("open123456");
        request.setGlobalPlantId("globalPlantId");
        request.setPlantName("テストプラント");
        request.setPlantAddress("試験県テスト市examビル1F");
        request.setEffectiveDate("2025-01-01");
        request.setDeletedFlag(Boolean.TRUE);
        sortKey.setKey(ConstSqlQueries.COLUMN_PLANTS_PLANT_ID);
        sortKey.setOrder("asc");
        request.setSort(sortKey);

        String inputJson = "{"
                + "\"" + Const.JSON_PROPERTY_COUNT + "\":0,"
                + "\"" + Const.JSON_PROPERTY_INDEX + "\":0,"
                + "\"" + Const.JSON_PROPERTY_SORT + "\":{"
                + "\"" + Const.JSON_PROPERTY_SORT_KEY + "\":\"plant_id\","
                + "\"" + Const.JSON_PROPERTY_SORT_ORDER + "\":\"asc\""
                + "},"
                + "\"" + Const.JSON_PROPERTY_OPERATOR_ID + "\":\"0bbb8463-cd21-4b91-8e5e-2535ea27ab1b\","
                + "\"" + Const.JSON_PROPERTY_PLANT_ID + "\":\"d9a38406-cae2-4679-b052-15a75f5531e6\","
                + "\"" + Const.JSON_PROPERTY_OPEN_PLANT_ID + "\":\"open123456\","
                + "\"" + Const.JSON_PROPERTY_GLOBAL_PLANT_ID + "\":\"globalPlantId\","
                + "\"" + Const.JSON_PROPERTY_PLANT_NAME + "\":\"テストプラント\","
                + "\"" + Const.JSON_PROPERTY_PLANT_ADDRESS + "\":\"試験県テスト市examビル1F\","
                + "\"" + Const.JSON_PROPERTY_EFFECTIVE_DATE + "\":\"2025-01-01\","
                + "\"" + Const.JSON_PROPERTY_DELETED_FLAG + "\":true"
                + "}";

        // Serialize
        String json = mapper.writeValueAsString(request);
        assertTrue(json.contentEquals(inputJson));

        // Deserialize
        ListPlantRequest input = mapper.readValue(inputJson, ListPlantRequest.class);

        // Assert
        assertEquals(input.getCount(), request.getCount());
        assertEquals(input.getIndex(), request.getIndex());
        assertEquals(input.getOperatorId(), request.getOperatorId());
        assertEquals(input.getPlantId(), request.getPlantId());
        assertEquals(input.getPlantName(), request.getPlantName());
        assertEquals(input.getPlantAddress(), request.getPlantAddress());
        assertEquals(input.getEffectiveDate(), request.getEffectiveDate());
        assertEquals(input.getDeletedFlag(), request.getDeletedFlag());
        assertEquals(input.getSort().getKey(), request.getSort().getKey());
        assertEquals(input.getSort().getOrder(), request.getSort().getOrder());
    }
}