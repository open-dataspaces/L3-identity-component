/*
 * StateString.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class represents a string that can be in one of three states: unset, explicitly null, or a concrete value.
 *
 * Date: 2025/10/30
 */

package io.github.open_dataspaces.core.domain.dto;

import lombok.Data;

/**
 * A string that can be in one of three states: unset, explicitly null, or a concrete value.
 */
@Data
public class StateString {

    /**
     * The possible states of the StateString.
     */
    public enum State { UNSET, NULL, VALUE }

    /**
     * The current state.
     */
    private State state;

    /**
     * The string value, if state is VALUE; null otherwise.
     */
    private String value;

    /**
     * Constructors and getters.
     */
    public StateString() {
        this.state = State.UNSET;
    }

    /**
     * Constructor to set the value.
     *
     * @param value the string value, or null to represent explicit null
     */
    public StateString(String value) {
        if (value == null) {
            this.state = State.NULL;
            this.value = null;
        } else {
            this.state = State.VALUE;
            this.value = value;
        }
    }

    /**
     * Factory method to create a StateString with a concrete value.
     */
    public static StateString of(String value) {
        return new StateString(value);
    }

    /**
     * Factory method to create a StateString in the UNSET state.
     */
    public static StateString unset() {
        return new StateString();
    }

    /**
     * Checks if the state is SET.
     */
    public boolean isUnset() {
        return this.state == State.UNSET;
    }

    /**
     * Checks if the state is SET.
     */
    public boolean isValueOrNull() {
        return this.state == State.VALUE || this.state == State.NULL;
    }

    /**
     * Checks if the state is NULL.
     */
    public boolean isNull() {
        return this.state == State.NULL;
    }

    /**
     * Checks if the state is VALUE.
     */
    public boolean isValue() {
        return this.state == State.VALUE;
    }
}