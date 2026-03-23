/*
 * ConflictException.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This exception is thrown when a conflict occurs, typically for HTTP 409 responses.
 *
 * Date: 2025/08/19
 */

package io.github.open_dataspaces.core.exception;

import io.github.open_dataspaces.core.common.consts.Const;

/**
 * Exception thrown when a conflict occurs.
 *
 * <p>This exception is typically used for HTTP 409 (Conflict) responses,
 * such as when optimistic locking fails or when there are concurrent modifications.</p>
 */
public class ConflictException extends AbstractBaseException {

    /**
     * Constructs a new ConflictException with the specified detail message.
     *
     * @param message the detail message
     */
    public ConflictException(String message) {
        super(message);
    }

    /**
     * Constructs a new ConflictException with the specified message and log message.
     *
     * @param logMessage the log message for this exception
     * @param responseMessage the exception message
     */
    public ConflictException(String logMessage, String responseMessage) {
        super(responseMessage);
        this.logMessage = logMessage;
    }

    /**
     * Sets the log message, response message, and source for this exception.
     *
     * @param message the exception message
     */
    @Override
    public void setInfo(String message) {
        this.logMessage = message;
        this.responseMessage = message;
        this.source = Const.SOURCE_AUTH;
    }
}
