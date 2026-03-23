/*
 * IllegalAuthDataExceptionTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the IllegalAuthDataException class.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.exception;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.github.open_dataspaces.core.common.consts.Const;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the {@link IllegalAuthDataException} class.
 */
public class IllegalAuthDataExceptionTest {

    /**
     * (Case#1-7) Tests the constructor.
     */
    @ParameterizedTest
    @CsvSource({
            "Access denied due to invalid credentials, Access denied", // Case#1 All parameters provided
            "Access denied due to invalid credentials, ''", // Case#2 Empty response message
            "Access denied due to invalid credentials, ", // Case#3 Null response message
            "'', Access denied due to invalid credentials", // Case#4 Empty log message
            ", Access denied due to invalid credentials", // Case#5 Null log message
            "'','", // Case#6 Empty log and response messages
            "," // Case#7 Null log and response messages
    })
    // Test cases for the constructor of IllegalAuthDataException
    void testConstructor(String logMessage, String responseMessage) {
        // Act
        IllegalAuthDataException exception = new IllegalAuthDataException(logMessage, responseMessage);

        // Assert
        assertEquals(responseMessage, exception.getMessage(),
                "The log message should match the provided message");
        assertEquals(logMessage, exception.getLogMessage(), "The exception message should match the provided message");
        assertEquals(responseMessage, exception.getResponseMessage(),
                "The response message should be correctly formatted");
        assertEquals(Const.SOURCE_AUTH, exception.getSource(), "The source should be set to Const.SOURCE_AUTH");
    }
}
