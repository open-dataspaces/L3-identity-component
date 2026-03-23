/*
 * ValidateExceptionTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the ValidateException class.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.util.StringUtils;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;

/**
 * Unit tests for the {@link ValidateException} class.
 */
public class ValidateExceptionTest {

    /**
     * (Case#1-7) Tests the constructor.
     */
    @ParameterizedTest
    @CsvSource({
            "Invalid data provided, operatorId is required", // Case#1 All parameters provided
            "Invalid data provided, ''", // Case#2 Empty detail message
            "Invalid data provided, ", // Case#3 Null detail message
            "'', operatorId is required", // Case#4 Empty message
            ", operatorId is required", // Case#5 Null message
            "'',''", // Case#6 Empty message and detail message
            "," // Case#7 Null message and detail message
    })
    void testConstructor(String message, String detailMessage) {
        // Act
        ValidateException exception = new ValidateException(message, detailMessage);

        // Assert
        StringBuilder sb = new StringBuilder(ConstError.ERR_400_VALIDATION_FAILED_HEADER);
        if (StringUtils.hasText(detailMessage)) {
            sb.append(detailMessage);
            sb.append(".");
        }
        String expectedResponseMessage = sb.toString();
        assertEquals(expectedResponseMessage, exception.getMessage(),
                "The log message should match the provided message");
        assertEquals(message, exception.getLogMessage(), "The exception message should match the provided message");
        assertEquals(expectedResponseMessage,
                exception.getResponseMessage(),
                "The response message should be correctly formatted");
        assertEquals(Const.SOURCE_AUTH, exception.getSource(), "The source should be set to Const.SOURCE_AUTH");
    }
}
