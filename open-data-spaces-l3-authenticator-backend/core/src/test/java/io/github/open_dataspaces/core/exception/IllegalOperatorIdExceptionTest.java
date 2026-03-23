/*
 * IllegalOperatorIdExceptionTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the IllegalOperatorIdException class.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the {@link IllegalOperatorIdException} class.
 */
class IllegalOperatorIdExceptionTest {

    /**
     * (Case#1) Tests the default constructor.
     */
    @Test
    void testDefaultConstructor_setsErrorMessage() {
        IllegalOperatorIdException ex = new IllegalOperatorIdException();
        assertEquals(ConstError.ERR_500_OPERATORID_INVALID, ex.getMessage());
    }

    /**
     * (Case#2) Tests the constructor with a custom message.
     */
    @ParameterizedTest
    @CsvSource(value = {"Invalid operatorId format"})
    @NullAndEmptySource
    void testSetInfo_setsResponseMessageAndSource(String testMessage) {
        IllegalOperatorIdException ex = new IllegalOperatorIdException();
        ex.setInfo(testMessage);

        assertEquals(testMessage, ex.getResponseMessage());
        assertEquals(null, ex.getLogMessage());
        assertEquals(Const.SOURCE_AUTH, ex.getSource());
    }
}