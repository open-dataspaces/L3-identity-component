/*
 * NotFoundException.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class represents an exception for not found resources.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.exception;

import io.github.open_dataspaces.core.common.consts.Const;

/**
 * Exception thrown when invalid or bad parameters are detected.
 *
 * <p>This exception is used to indicate validation failures or incorrect input parameters.</p>
 */
public class NotFoundException extends AbstractBaseException {

    /**
     * Constructs a new NotFoundException with the specified message.
     *
     * @param message the exception message
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new NotFoundException with the specified message.
     *
     * @param message the exception message
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
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
