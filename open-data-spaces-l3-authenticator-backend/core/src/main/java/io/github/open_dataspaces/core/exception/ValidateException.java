/*
 * ValidateException.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class defines an exception for validation errors.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.exception;

import org.springframework.util.StringUtils;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;

/**
 * Exception class for handling validation errors.
 */
public class ValidateException extends AbstractBaseException {

    /**
     * Constructs a new ValidateException with the specified message and detail message.
     *
     * @param message the exception message
     * @param detailMessage the detailed validation error message
     */
    public ValidateException(String message, String detailMessage) {
        super(message);

        StringBuilder sb = new StringBuilder(ConstError.ERR_400_VALIDATION_FAILED_HEADER);
        if (StringUtils.hasText(detailMessage)) {
            sb.append(detailMessage);
            sb.append(".");
        }
        this.responseMessage = sb.toString();
    }

    /**
     * Constructs a new ValidateException with the specified detail message.
     *
     * @param detailMessage the detailed validation error message
     */
    public ValidateException(String detailMessage) {
        // Construct the log message
        this(detailMessage, detailMessage);
        StringBuilder sb = new StringBuilder(ConstError.ERRLOG_400_INVALID_REQUEST);
        if (StringUtils.hasText(detailMessage)) {
            sb.append(detailMessage);
            sb.append(".");
        }
        setInfo(sb.toString());
    }

    /**
     * Sets the log message and error source for this exception.
     *
     * @param message the exception message
     */
    @Override
    public void setInfo(String message) {
        this.logMessage = message;
        this.source = Const.SOURCE_AUTH;
    }
}
