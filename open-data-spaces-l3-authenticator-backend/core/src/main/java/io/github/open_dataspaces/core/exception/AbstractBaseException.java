/*
 * AbstractBaseException.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class provides a base class for custom runtime exceptions.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.exception;

/**
 * Abstract base class for custom runtime exceptions.
 *
 * <p>This class defines common fields and methods for logging and response messages, as well as the
 * error source. Subclasses must implement the setInfo method.</p>
 */
public abstract class AbstractBaseException extends RuntimeException {

    protected String logMessage;
    protected String responseMessage;
    protected String source;

    /**
     * Constructs a new AbstractBaseException with the specified message.
     *
     * @param message the exception message
     */
    public AbstractBaseException(String message) {
        setInfo(message);
    }

    /**
     * Constructs a new AbstractBaseException with the specified message.
     *
     * @param message the exception message
     */
    public AbstractBaseException(String message, Throwable cause) {
        super(message, cause);
        setInfo(message);
    }

    /**
     * Returns the log message for this exception.
     */
    @Override
    public String getMessage() {
        return responseMessage;
    }

    /**
     * Sets the log message, response message, and source.
     *
     * @param message the exception message
     */
    public abstract void setInfo(String message);

    /**
     * Returns the log message for this exception.
     *
     * @return the log message
     */
    public String getLogMessage() {
        return logMessage;
    }

    /**
     * Returns the response message for this exception.
     *
     * @return the response message
     */
    public String getResponseMessage() {
        return responseMessage;
    }

    /**
     * Returns the error source for this exception.
     *
     * @return the error source
     */
    public String getSource() {
        return source;
    }
}
