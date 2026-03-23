/*
 * OutOfServiceException.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class defines an exception for outer service/api errors.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.exception;

import io.github.open_dataspaces.core.common.consts.Const;

/**
 * Exception class for handling outer service errors.
 */
public class OutOfServiceException extends AbstractBaseException {

    /**
     * Constructs a new OutOfServiceException with the specified message.
     *
     * @param message the exception message
     */
    public OutOfServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a new OutOfServiceException with the specified message.
     *
     * @param message the exception message
     */
    public OutOfServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Sets the response message and error source for this exception.
     *
     * @param message the exception message
     */
    @Override
    public void setInfo(String message) {
        this.logMessage = message;
        this.responseMessage = ""; // Not used in this case
        this.source = Const.SOURCE_AUTH;
    }
}
