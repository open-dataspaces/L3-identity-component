/*
 * BadParametersExceptionTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the BadParametersException class.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.exception;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import io.github.open_dataspaces.core.common.consts.Const;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the {@link BadParametersException} class.
 */
public class BadParametersExceptionTest {

    /**
     * (Case#1-3) Tests the constructor.
     */
    @ParameterizedTest
    @CsvSource({
            "Invalid parameters provided" // Case#1 All parameters provided
    })
    @NullAndEmptySource
    // Case#2 Empty message
    // Case#3 Null message
    void testConstructor(String message) {
        // Arrange
        // Act
        BadParametersException exception = new BadParametersException(message);

        // Assert
        assertEquals(message, exception.getMessage(), "The exception message should match the provided message");
        assertEquals(message, exception.getLogMessage(), "The log message should match the provided message");
        assertEquals(message, exception.getResponseMessage(), "The response message should match the provided message");
        assertEquals(Const.SOURCE_AUTH, exception.getSource(), "The source should be set to Const.SOURCE_AUTH");
    }
}
