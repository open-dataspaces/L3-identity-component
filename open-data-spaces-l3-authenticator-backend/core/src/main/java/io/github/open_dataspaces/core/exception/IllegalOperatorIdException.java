/*
 * IllegalOperatorIdException.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class defines an exception for illegal operatorId errors.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.exception;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;

/**
 * Exception class for handling illegal operatorId errors.
 */
public class IllegalOperatorIdException extends AbstractBaseException {

    /**
     * Constructs a new IllegalOperatorIdException with the specified operatorId.
     */
    public IllegalOperatorIdException() {
        super(ConstError.ERR_500_OPERATORID_INVALID);
    }

    /**
     * Sets the log message, response message, and error source for this exception.
     *
     * @param message the exception message
     */
    @Override
    public void setInfo(String message) {
        this.responseMessage = message;
        this.source = Const.SOURCE_AUTH;
    }
}
