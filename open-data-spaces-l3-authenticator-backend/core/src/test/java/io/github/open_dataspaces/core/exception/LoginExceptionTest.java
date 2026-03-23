/*
 * LoginExceptionTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the LoginException class.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.exception;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.github.open_dataspaces.core.common.consts.Const;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the {@link LoginException} class.
 */
public class LoginExceptionTest {

    /**
     * (Case#1-5) Tests the constructor.
     */
    @ParameterizedTest
    @CsvSource({
            "Login failed due to invalid credentials, operatorId123, Login failed due to invalid credentials: id: operatorId123.", // Case#1 All parameters provided
            "Login failed due to invalid credentials, '', Login failed due to invalid credentials", // Case#2 Empty id
            "Login failed due to invalid credentials, , Login failed due to invalid credentials", // Case#3 null id
            "'', operatorId123, : id: operatorId123.", // Case#4 empty message
            ", operatorId123, null: id: operatorId123.", // Case#5 Null message
            "'', '', ''", // Case#6 Empty message and id
            ", , " // Case#7 Null message and id
    })
    void testConstructor(String message, String id, String expectedLogMessage) {
        // Act
        LoginException exception = new LoginException(message, id);

        // Assert
        assertEquals(message, exception.getMessage(),
                "The log message should match the provided message");
        assertEquals(expectedLogMessage, exception.getLogMessage(), "The exception message should match the provided message");
        assertEquals(message, exception.getResponseMessage(),
                "The response message should be correctly formatted");
        assertEquals(Const.SOURCE_AUTH, exception.getSource(), "The source should be set to Const.SOURCE_AUTH");
    }
}
