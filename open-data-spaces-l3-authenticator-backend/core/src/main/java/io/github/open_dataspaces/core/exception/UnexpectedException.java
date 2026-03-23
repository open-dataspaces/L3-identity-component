/*
 * UnexpectedException.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class defines an exception for unexpected system errors.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.exception;

import org.springframework.util.StringUtils;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;

/**
 * Exception class for handling unexpected system errors.
 */
public class UnexpectedException extends AbstractBaseException {

    /**
     * Constructs a new UnexpectedException with the specified message.
     *
     * @param message the exception message
     * @param reason  the reason for the exception (optional)
     */
    public UnexpectedException(String message, String reason) {
        super(StringUtils.hasText(reason) ? reason : message);
        this.logMessage = StringUtils.hasText(reason)
                ? String.format(ConstError.ERRLOG_500_ERROR, message, reason)
                : message;
    }

    /**
     * Constructs a new UnexpectedException with the specified message.
     *
     * @param message the exception message
     */
    public UnexpectedException(String message, String reason, Throwable cause) {
        super(StringUtils.hasText(reason) ? reason : message, cause);
        this.logMessage = StringUtils.hasText(reason)
                ? String.format(ConstError.ERRLOG_500_ERROR, message, reason)
                : message;
    }

    /**
     * Sets the log message, response message, and error source for this exception.
     *
     * @param responseMessage the message to be sent in the response
     */
    @Override
    public void setInfo(String responseMessage) {
        this.responseMessage = responseMessage;
        this.source = Const.SOURCE_AUTH;
    }
}
