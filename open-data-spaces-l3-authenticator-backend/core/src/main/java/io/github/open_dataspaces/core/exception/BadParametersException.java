/*
 * BadParametersException.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class represents an exception for invalid or bad parameters.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.exception;

import io.github.open_dataspaces.core.common.consts.Const;

/**
 * Exception thrown when invalid or bad parameters are detected.
 *
 * <p>This exception is used to indicate validation failures or incorrect input parameters.</p>
 */
public class BadParametersException extends AbstractBaseException {

    /**
     * Constructs a new BadParametersException with the specified message.
     *
     * @param message the exception message
     */
    public BadParametersException(String message) {
        super(message);
    }

    /**
     * Constructs a new BadParametersException with the specified message.
     *
     * @param message the exception message
     */
    public BadParametersException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new UnauthorizedException with the specified message and log message.
     *
     * @param logMessage the log message for this exception
     * @param responseMessage the exception message
     */
    public BadParametersException(String logMessage, String responseMessage) {
        super(responseMessage);
        this.logMessage = logMessage;
    }

    /**
     * Constructs a new UnauthorizedException with the specified message and log message.
     *
     * @param logMessage the log message for this exception
     * @param responseMessage the exception message
     */
    public BadParametersException(String logMessage, String responseMessage, Throwable cause) {
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
