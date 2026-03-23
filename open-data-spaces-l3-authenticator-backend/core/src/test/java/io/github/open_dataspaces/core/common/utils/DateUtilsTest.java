/*
 * DateUtilsTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the DateUtils class.
 *
 * Date: 2025/09/25
 */

package io.github.open_dataspaces.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit tests for the {@link DateUtils} class.
 */
class DateUtilsTest {

    private static final String DATE_PATTERN_HYPHEN = "yyyy-MM-dd";
    private static final String DATE_PATTERN_SLASH = "yyyy/MM/dd";
    private static final String DATE_PATTERN = "yyyyMMdd";
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String ISO_8601_UTC_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'";
    private static final String ISO_8601_UTC_MILLISECOND_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * Default constructor.
     */
    @Test
    void testConstructor() {
        DateUtils instance = new DateUtils();
        assertNotNull(instance);
    }

    /**
     * Test for parseDate.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // date, format
        "2025-09-05, DATE_PATTERN_HYPHEN",
        "2025/09/05, DATE_PATTERN_SLASH",
        "20250905, DATE_PATTERN",
    }, nullValues = "NULL")
    void testParseDate_validString(String dataStr, String datePattern) {
        LocalDate expected = LocalDate.of(2025, 9, 5);
        LocalDate actual = DateUtils.parseDate(dataStr,
                datePattern.equals("DATE_PATTERN_HYPHEN") ? DATE_PATTERN_HYPHEN
                : datePattern.equals("DATE_PATTERN_SLASH") ? DATE_PATTERN_SLASH
                : DATE_PATTERN
        );
        assertEquals(expected, actual);
    }

    /**
     * Test for parseDate when input is null.
     */
    @Test
    void testParseDate_nullString() {
        assertNull(DateUtils.parseDate(null, DATE_PATTERN));
    }

    /**
     * Test for parseDate when input is invalid.
     */
    @Test
    void testParseDate_invalidString_throwsException() {
        assertThrows(Exception.class, () -> DateUtils.parseDate("invalid-date", DATE_PATTERN));
    }

    /**
     * Test for parseDateTime.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // date, format
        "2025-09-05 12:30:45, DATETIME_PATTERN",
        "2025-09-05T12:30:45.0000000Z, ISO_8601_UTC_FORMAT",
        "2025-09-05T12:30:45.000Z, ISO_8601_UTC_MILLISECOND_FORMAT",
    }, nullValues = "NULL")
    void testParseDateTime_validString(String dateTimeStr, String dateTimePattern) {
        LocalDateTime expected = LocalDateTime.of(2025, 9, 5, 12, 30, 45);
        LocalDateTime actual = DateUtils.parseDateTime(dateTimeStr,
                dateTimePattern.equals("DATETIME_PATTERN") ? DATETIME_PATTERN
                : dateTimePattern.equals("ISO_8601_UTC_FORMAT") ? ISO_8601_UTC_FORMAT
                : ISO_8601_UTC_MILLISECOND_FORMAT
        );
        assertEquals(expected, actual);
    }

    /**
     * Test for parseDateTime when input is null.
     */
    @Test
    void testParseDateTime_nullString() {
        assertNull(DateUtils.parseDateTime(null, DATETIME_PATTERN));
    }

    /**
     * Test for parseDateTime when input is invalid.
     */
    @Test
    void testParseDateTime_invalidString_throwsException() {
        assertThrows(Exception.class, () -> DateUtils.parseDateTime("invalid-datetime", DATETIME_PATTERN));
    }

    /**
     * Test for formatDate.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // date, format
        "2025-09-05, DATE_PATTERN_HYPHEN",
        "2025/09/05, DATE_PATTERN_SLASH",
        "20250905, DATE_PATTERN",
    }, nullValues = "NULL")
    void testFormatDate_validDate(String dataStr, String datePattern) {
        LocalDate date = LocalDate.of(2025, 9, 5);
        String formatted = DateUtils.formatDate(date, datePattern.equals("DATE_PATTERN_HYPHEN") ? DATE_PATTERN_HYPHEN
                : datePattern.equals("DATE_PATTERN_SLASH") ? DATE_PATTERN_SLASH
                : DATE_PATTERN
        );
        assertEquals(dataStr, formatted);
    }

    /**
     * Test for formatDate when input is null.
     */
    @Test
    void testFormatDate_nullDate() {
        assertNull(DateUtils.formatDate(null, DATE_PATTERN));
    }

    /**
     * Test for formatDateTime.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // date, format
        "2025-09-05 12:30:45, DATETIME_PATTERN",
        "2025-09-05T12:30:45.0000000Z, ISO_8601_UTC_FORMAT",
        "2025-09-05T12:30:45.000Z, ISO_8601_UTC_MILLISECOND_FORMAT",
    }, nullValues = "NULL")
    void testFormatDateTime_validDateTime(String dataStr, String dateTimePattern) {
        LocalDateTime dateTime = LocalDateTime.of(2025, 9, 5, 12, 30, 45);
        String formatted = DateUtils.formatDateTime(dateTime, dateTimePattern.equals("DATETIME_PATTERN") ? DATETIME_PATTERN
                : dateTimePattern.equals("ISO_8601_UTC_FORMAT") ? ISO_8601_UTC_FORMAT
                : ISO_8601_UTC_MILLISECOND_FORMAT
        );
        assertEquals(dataStr, formatted);
    }

    /**
     * Test for formatDateTime when input is null.
     */
    @Test
    void testFormatDateTime_nullDateTime() {
        assertNull(DateUtils.formatDateTime(null, DATETIME_PATTERN));
    }

    /**
     * Test for equals method with LocalDate.
     */
    @Test
    void testEquals_localDate_equalDates() {
        LocalDate d1 = LocalDate.of(2025, 9, 5);
        LocalDate d2 = LocalDate.of(2025, 9, 5);
        assertTrue(DateUtils.equals(d1, d2, DATE_PATTERN));
    }

    /**
     * Test for equals method with LocalDate when dates are different.
     */
    @Test
    void testEquals_localDate_differentDates() {
        LocalDate d1 = LocalDate.of(2025, 9, 5);
        LocalDate d2 = LocalDate.of(2025, 9, 6);
        assertFalse(DateUtils.equals(d1, d2, DATE_PATTERN));
    }

    /**
     * Test for equals method with LocalDate when date is invalid.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // d1, d2
        "NULL, valid-date",
        "valid-date, NULL",
    }, nullValues = "NULL")
    void testEquals_localDate_nullDate_throwsException(String date1Str, String date2Str) {
        LocalDate d1 = date1Str == null ? null : LocalDate.of(2025, 9, 5);
        LocalDate d2 = date2Str == null ? null : LocalDate.of(2025, 9, 5);
        assertThrows(IllegalArgumentException.class, () -> DateUtils.equals(d1, d2, DATE_PATTERN));
    }

    /**
     * Test for equals method with LocalDateTime.
     */
    @Test
    void testEquals_localDateTime_equalDateTimes() {
        LocalDateTime dt1 = LocalDateTime.of(2025, 9, 5, 12, 30, 45);
        LocalDateTime dt2 = LocalDateTime.of(2025, 9, 5, 12, 30, 45);
        assertTrue(DateUtils.equals(dt1, dt2, DATETIME_PATTERN));
    }

    /**
     * Test for equals method with LocalDateTime when dates are different.
     */
    @Test
    void testEquals_localDateTime_differentDateTimes() {
        LocalDateTime dt1 = LocalDateTime.of(2025, 9, 5, 12, 30, 45);
        LocalDateTime dt2 = LocalDateTime.of(2025, 9, 5, 12, 30, 46);
        assertFalse(DateUtils.equals(dt1, dt2, DATETIME_PATTERN));
    }

    /**
     * Test for equals method with LocalDateTime when date is invalid.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // d1, d2
        "NULL, valid-date",
        "valid-date, NULL",
    }, nullValues = "NULL")
    void testEquals_localDateTime_nullDateTime_throwsException(String date1Str, String date2Str) {
        LocalDateTime dt1 = date1Str == null ? null : LocalDateTime.of(2025, 9, 5, 12, 30, 45);
        LocalDateTime dt2 = date2Str == null ? null : LocalDateTime.of(2025, 9, 5, 12, 30, 45);
        assertThrows(IllegalArgumentException.class, () -> DateUtils.equals(dt1, dt2, DATETIME_PATTERN));
    }

    /**
     * Test for isValidDateRange.
     */
    @Test
    void testIsValidDateRange_validRange() {
        LocalDate start = LocalDate.of(2025, 9, 5);
        LocalDate end = LocalDate.of(2025, 9, 6);
        assertTrue(DateUtils.isValidDateRange(start, end));
    }

    /**
     * Test for isValidDateRange when dates are equal.
     */
    @Test
    void testIsValidDateRange_equalDates() {
        LocalDate date = LocalDate.of(2025, 9, 5);
        assertTrue(DateUtils.isValidDateRange(date, date));
    }

    /**
     * Test for isValidDateRange when dates are invalid.
     */
    @Test
    void testIsValidDateRange_invalidRange() {
        LocalDate start = LocalDate.of(2025, 9, 7);
        LocalDate end = LocalDate.of(2025, 9, 6);
        assertFalse(DateUtils.isValidDateRange(start, end));
    }

    /**
     * Test for isValidDateRange when date is null.
     */
    @ParameterizedTest
    @CsvSource(value = {
        // d1, d2
        "NULL, valid-date",
        "valid-date, NULL",
    }, nullValues = "NULL")
    void testIsValidDateRange_nullDate_throwsException(String date1Str, String date2Str) {
        LocalDate start = date1Str == null ? null : LocalDate.of(2025, 9, 5);
        LocalDate end = date2Str == null ? null : LocalDate.of(2025, 9, 6);
        assertThrows(IllegalArgumentException.class, () -> DateUtils.isValidDateRange(start, end));
    }
}