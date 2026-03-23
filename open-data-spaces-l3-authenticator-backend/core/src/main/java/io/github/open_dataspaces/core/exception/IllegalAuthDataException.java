/*
 * IllegalAuthDataException.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class defines an exception for illegal authentication data.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.exception;

import io.github.open_dataspaces.core.common.consts.Const;

/**
 * Exception class for handling illegal authentication data.
 */
public class IllegalAuthDataException extends AbstractBaseException {

    /**
     * Constructs a new IllegalAuthDataException with the specified message.
     *
     * @param logMessage the message to be logged
     * @param responseMessage the message to be returned in the response
     */
    public IllegalAuthDataException(String logMessage, String responseMessage) {
        super(responseMessage);
        this.logMessage = logMessage;
    }

    /**
     * Constructs a new IllegalAuthDataException with the specified message.
     *
     * @param message the message to be logged
     */
    public IllegalAuthDataException(String message) {
        super(message);
        this.logMessage = message;
    }

    /**
     * Sets the log message, response message, and source for this exception.
     *
     * @param message the exception message
     */
    @Override
    public void setInfo(String message) {
        this.logMessage = "";
        this.responseMessage = message;
        this.source = Const.SOURCE_AUTH;
    }
}
