/*
 * ForbiddenExceptionTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the ForbiddenException class.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link ForbiddenException} class.
 */
public class ForbiddenExceptionTest {

    private static String commonMessage = "Resource not found";
    private static String commonLogMessage = "Resource not found Log Message";
    private static Exception commonException = new Exception(commonMessage);

    @Test
    @DisplayName("Test ForbiddenException constructor with message")
    void testParameterizedConstructor() {
        // Arrange
        // Act
        ForbiddenException exception = new ForbiddenException(commonMessage);

        // Assert
        assertEquals(commonMessage, exception.getMessage(), "The exception message should match the provided message");
        assertEquals(commonMessage, exception.getLogMessage(), "The log message should match the provided message");
        assertEquals(commonMessage, exception.getResponseMessage(), "The response message should match the provided message");
    }

    @Test
    @DisplayName("Test ForbiddenException constructor with log message and message")
    void testParameterizedConstructor_withLogMessage() {
        // Arrange
        // Act
        ForbiddenException exception = new ForbiddenException(commonLogMessage, commonMessage);

        // Assert
        assertEquals(commonMessage, exception.getMessage(), "The exception message should match the provided message");
        assertEquals(commonLogMessage, exception.getLogMessage(), "The log message should match the provided log message");
        assertEquals(commonMessage, exception.getResponseMessage(), "The response message should match the provided message");
    }

    @Test
    @DisplayName("Test ForbiddenException constructor with log message, message and cause")
    void testParameterizedConstructor_withCause() {
        // Arrange
        // Act
        ForbiddenException exception = new ForbiddenException(commonLogMessage, commonMessage, commonException);

        // Assert
        assertEquals(commonMessage, exception.getMessage(), "The exception message should match the provided message");
        assertEquals(commonLogMessage, exception.getLogMessage(), "The log message should match the provided message");
        assertEquals(commonMessage, exception.getResponseMessage(), "The response message should match the provided message");
        assertEquals(commonException, exception.getCause(), "The cause should match the provided exception");
    }
}
