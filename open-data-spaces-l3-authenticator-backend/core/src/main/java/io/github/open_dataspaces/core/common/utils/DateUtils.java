/*
 * DateUtils.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This controller handles operator information retrieval and update requests.
 *
 * Date: 2025/09/05
 */

package io.github.open_dataspaces.core.common.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import io.github.open_dataspaces.core.common.consts.ConstError;

/**
 * Utility class for date operations.
 */
public class DateUtils {

    /**
     * Parses a date string into a LocalDate using the specified pattern.
     *
     * @param dateStr the date string to parse
     * @param pattern the pattern to use for parsing
     * @return the parsed LocalDate, or null if parsing failed
     */
    public static LocalDate parseDate(String dateStr, String pattern) {
        if (dateStr == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDate.parse(dateStr, formatter);
    }

    /**
     * Parses a date-time string into a LocalDateTime using the specified pattern.
     *
     * @param dateStr the date-time string to parse
     * @param pattern the pattern to use for parsing
     * @return the parsed LocalDateTime, or null if parsing failed
     */
    public static LocalDateTime parseDateTime(String dateStr, String pattern) {
        if (dateStr == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(dateStr, formatter);
    }

    /**
     * Formats a LocalDate into a string using the specified pattern.
     *
     * @param date the LocalDate to format
     * @param pattern the pattern to use for formatting
     * @return the formatted date string
     */
    public static String formatDate(LocalDate date, String pattern) {
        if (date == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }

    /**
     * Formats a LocalDateTime into a string using the specified pattern.
     *
     * @param dateTime the LocalDateTime to format
     * @param pattern the pattern to use for formatting
     * @return the formatted date-time string
     */
    public static String formatDateTime(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }

    /**
     * Compares two LocalDate objects for equality based on the specified pattern.
     *
     * @param date1 the first LocalDate to compare
     * @param date2 the second LocalDate to compare
     * @param pattern the pattern to use for formatting
     * @return true if the dates are equal, false otherwise
     */
    public static boolean equals(LocalDate date1, LocalDate date2, String pattern) throws IllegalArgumentException {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException(ConstError.ERR_500_DATE_VALUE_NULL);
        }

        date1 = parseDate(formatDate(date1, pattern), pattern);
        date2 = parseDate(formatDate(date2, pattern), pattern);

        return date1.isEqual(date2);
    }

    /**
     * Compares two LocalDateTime objects for equality based on the specified pattern.
     *
     * @param dateTime1 the first LocalDateTime to compare
     * @param dateTime2 the second LocalDateTime to compare
     * @param pattern the pattern to use for formatting
     * @return true if the date-times are equal, false otherwise
     */
    public static boolean equals(LocalDateTime dateTime1, LocalDateTime dateTime2, String pattern) throws IllegalArgumentException {
        if (dateTime1 == null || dateTime2 == null) {
            throw new IllegalArgumentException(ConstError.ERR_500_DATE_VALUE_NULL);
        }

        dateTime1 = parseDateTime(formatDateTime(dateTime1, pattern), pattern);
        dateTime2 = parseDateTime(formatDateTime(dateTime2, pattern), pattern);

        return dateTime1.isEqual(dateTime2);
    }

    /**
     * Compares two LocalDate objects for equality.
     *
     * @param dateStart the first LocalDate to compare
     * @param dateEnd the second LocalDate to compare
     * @return true if the dates are equal, false otherwise
     */
    public static boolean isValidDateRange(LocalDate dateStart, LocalDate dateEnd) {
        if (dateStart == null || dateEnd == null) {
            throw new IllegalArgumentException(ConstError.ERR_500_DATE_VALUE_NULL);
        }
        return !dateStart.isAfter(dateEnd);
    }

    /**
     * Checks if a date is between two other dates (inclusive).
     *
     * @param dateBase the date to check
     * @param dateStart the start date of the range
     * @param dateEnd the end date of the range
     * @return true if dateBase is between dateStart and dateEnd, false otherwise
     */
    public static boolean isBetween(LocalDate dateBase, LocalDate dateStart, LocalDate dateEnd) {
        if (dateBase == null || dateStart == null || dateEnd == null) {
            throw new IllegalArgumentException(ConstError.ERR_500_DATE_VALUE_NULL);
        }
        // true: dateStart <= dateBase <= dateEnd
        return (!dateBase.isBefore(dateStart)) && (!dateBase.isAfter(dateEnd));
    }

}
