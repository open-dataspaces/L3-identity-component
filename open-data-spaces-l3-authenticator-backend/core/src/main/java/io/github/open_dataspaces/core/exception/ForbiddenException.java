/*
 * ForbiddenException.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class defines an exception for unauthorized access errors.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.exception;

import io.github.open_dataspaces.core.common.consts.Const;

/**
 * Exception class for handling unauthorized access errors.
 */
public class ForbiddenException extends AbstractBaseException {

    /**
     * Constructs a new UnauthorizedException with the specified message and log message.
     *
     * @param logMessage the log message for this exception
     * @param responseMessage the exception message
     */
    public ForbiddenException(String logMessage, String responseMessage) {
        super(responseMessage);
        this.logMessage = logMessage;
    }

    /**
     * Constructs a new UnauthorizedException with the specified message and log message.
     *
     * @param logMessage the log message for this exception
     * @param responseMessage the exception message
     */
    public ForbiddenException(String logMessage, String responseMessage, Throwable cause) {
        super(responseMessage, cause);
        this.logMessage = logMessage;
    }

    /**
     * Constructs a new UnauthorizedException with the specified message.
     *
     * @param message the log message for this exception
     */
    public ForbiddenException(String message) {
        super(message);
        this.logMessage = message;
    }

    /**
     * Sets the log message and error source for this exception.
     *
     * @param message the exception message
     */
    @Override
    public void setInfo(String message) {
        this.responseMessage = message;
        this.source = Const.SOURCE_AUTH;
    }
}
