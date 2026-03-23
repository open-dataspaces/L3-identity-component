/*
 * StateStringTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for StateString DTO.
 *
 * Date: 2025/08/30
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Test setRawJson with valid string.
 */
public class StateStringTest {
    /**
     * Test setRawJson with valid string.
     */
    @Test
    @DisplayName("Test no-args constructor")
    void testNoArgsConstructor() {
        StateString stateString = new StateString();
        assertNotNull(stateString);
        assertEquals(StateString.State.UNSET, stateString.getState());
        assertEquals(null, stateString.getValue());
    }

    /**
     * Test setRawJson with valid string.
     */
    @Test
    @DisplayName("Test parameterized constructor")
    void testParameterizedConstructor() {
        StateString stateString = new StateString("testValue");
        assertNotNull(stateString);
        assertEquals(StateString.State.VALUE, stateString.getState());
        assertEquals("testValue", stateString.getValue());
    }

    /**
     * Test setRawJson with valid string.
     */
    @Test
    @DisplayName("Test parameterized constructor")
    void testParameterizedConstructor_null() {
        StateString stateString = new StateString(null);
        assertNotNull(stateString);
        assertEquals(StateString.State.NULL, stateString.getState());
        assertEquals(null, stateString.getValue());
    }

    /**
     * Test setters and getters.
     */
    @Test
    @DisplayName("StateString - Setter and Getter Test")
    void testSettersAndGetters() {
        StateString stateString = new StateString();
        stateString.setState(StateString.State.VALUE);
        stateString.setValue("newValue");
        assertEquals(StateString.State.VALUE, stateString.getState());
        assertEquals("newValue", stateString.getValue());
    }

    /**
     * Test setRawJson with valid string.
     */
    @Test
    @DisplayName("Test parameterized constructor")
    void testOf() {
        StateString stateString = StateString.of("testValue");
        assertNotNull(stateString);
        assertEquals(StateString.State.VALUE, stateString.getState());
        assertEquals("testValue", stateString.getValue());
    }

    /**
     * Test setRawJson with valid string.
     */
    @Test
    @DisplayName("Test parameterized constructor")
    void testUnset() {
        StateString stateString = StateString.unset();
        assertNotNull(stateString);
        assertEquals(StateString.State.UNSET, stateString.getState());
        assertEquals(null, stateString.getValue());
    }

    /**
     * Test setRawJson with valid string.
     */
    @ParameterizedTest
    @CsvSource({
        "true, unset",
        "false, value",
        "false, null"
    })
    @DisplayName("Test isUnset")
    void testIsUnset(boolean expected, String state) {
        StateString stateString;
        switch (state) {
            case "unset":
                stateString = StateString.unset();
                break;
            case "value":
                stateString = StateString.of("testValue");
                break;
            case "null":
                stateString = new StateString(null);
                break;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }
        assertEquals(expected, stateString.isUnset());
    }

    /**
     * Test isValueOrNull with various states.
     */
    @ParameterizedTest
    @CsvSource({
        "true, value",
        "true, null",
        "false, unset"
    })
    @DisplayName("Test isValueOrNull")
    void testIsValueOrNull(boolean expected, String state) {
        StateString stateString;
        switch (state) {
            case "unset":
                stateString = StateString.unset();
                break;
            case "value":
                stateString = StateString.of("testValue");
                break;
            case "null":
                stateString = new StateString(null);
                break;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }
        assertEquals(expected, stateString.isValueOrNull());
    }

    /**
     * Test isNull with various states.
     */
    @ParameterizedTest
    @CsvSource({
        "true, null",
        "false, value",
        "false, unset"
    })
    @DisplayName("Test isNull")
    void testIsNull(boolean expected, String state) {
        StateString stateString;
        switch (state) {
            case "unset":
                stateString = StateString.unset();
                break;
            case "value":
                stateString = StateString.of("testValue");
                break;
            case "null":
                stateString = new StateString(null);
                break;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }
        assertEquals(expected, stateString.isNull());
    }

    /**
     * Test isValue with various states.
     */
    @ParameterizedTest
    @CsvSource({
        "true, value",
        "false, null",
        "false, unset"
    })
    @DisplayName("Test isValue")
    void testIsValue(boolean expected, String state) {
        StateString stateString;
        switch (state) {
            case "unset":
                stateString = StateString.unset();
                break;
            case "value":
                stateString = StateString.of("testValue");
                break;
            case "null":
                stateString = new StateString(null);
                break;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }
        assertEquals(expected, stateString.isValue());
    }
}
