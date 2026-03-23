/*
 * LoginException.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class defines an exception for login-related errors.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.exception;

import org.springframework.util.StringUtils;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;

/**
 * Exception class for handling login-related errors.
 */
public class LoginException extends AbstractBaseException {

    /**
     * Constructs a new LoginException with the specified message and identifier.
     *
     * @param message the exception message
     * @param id the identifier related to the login attempt (e.g., operatorId)
     */
    public LoginException(String message, String id) {
        super(message);

        this.logMessage = StringUtils.hasText(id)
                ? String.format(ConstError.ERRLOG_401_LOGIN_ERROR, message, id)
                : message;
    }

    /**
     * Sets the response message and error source for this exception.
     *
     * @param message the exception message
     */
    @Override
    public void setInfo(String message) {
        this.responseMessage = message;
        this.source = Const.SOURCE_AUTH;
    }
}
