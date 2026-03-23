/*
 * UnexpectedExceptionTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the UnexpectedException class.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.exception;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.util.StringUtils;

import io.github.open_dataspaces.core.common.consts.Const;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the {@link UnexpectedException} class.
 */
public class UnexpectedExceptionTest {

    /**
     * (Case#1,2) Tests the constructor.
     */
    @ParameterizedTest
    @CsvSource({
            "An unexpected error occurred, Database connection failed", // Case#1 All parameters provided
            "An unexpected error occurred, ''", // Case#2 Empty reason
            "An unexpected error occurred, ", // Case#3 Null reason
            "'', Database connection failed", // Case#4 Empty message
            ", Database connection failed", // Case#5 Null message
            "'','',", // Case#6 Empty message and reason
            "," // Case#7 Null message and reason
    })
    void testConstructor(String message, String reason) {
        // Act
        UnexpectedException exception = new UnexpectedException(message, reason);

        // Assert
        String exceptionMessage = StringUtils.hasText(reason) ? reason : message;
        String expectedLogMessage =  !StringUtils.hasText(reason) ? message : message + ": " + reason;
        assertEquals(exceptionMessage, exception.getMessage(),
                "The log message should match the provided message");
        assertEquals(expectedLogMessage, exception.getLogMessage(), "The exception message should match the provided message");
        assertEquals(exceptionMessage, exception.getResponseMessage(),
                "The response message should be correctly formatted");
        assertEquals(Const.SOURCE_AUTH, exception.getSource(), "The source should be set to Const.SOURCE_AUTH");
    }
}
