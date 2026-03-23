/*
 * EnumErrorCode.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class defines error code constants and utility methods for HTTP status codes.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.common.enums;

import org.springframework.http.HttpStatus;

/**
 * Provides error code constants and utility methods for HTTP status codes.
 *
 * <p>This class defines an enum for common HTTP error codes and messages, and provides a method to
 * retrieve the error code string by status code.</p>
 */
public class EnumErrorCode {

    // Private constructor to prevent instantiation. It is for testing purposes only.
    private EnumErrorCode() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    /**
     * Enum representing common HTTP error codes and messages.
     */
    public enum ErrorCodes {

        SUCCESS(HttpStatus.OK.value(), "OK"),
            _201(HttpStatus.CREATED.value(), "Created"),
            _400(HttpStatus.BAD_REQUEST.value(), "BadRequest"),
            _401(HttpStatus.UNAUTHORIZED.value(), "Unauthorized"),
            _403(HttpStatus.FORBIDDEN.value(), "AccessDenied"),
            _404(HttpStatus.NOT_FOUND.value(), "NotFound"),
            _409(HttpStatus.CONFLICT.value(), "Conflict"),
            _415(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), "UnsupportedMediaType"),
            _500(HttpStatus.INTERNAL_SERVER_ERROR.value(), "InternalServerError"),
            _503(HttpStatus.SERVICE_UNAVAILABLE.value(), "ServiceUnavailable");

        private final int statusCode;
        private final String errorCode;

        /**
         * Constructor for ErrorCodes.
         *
         * @param code the HTTP status code
         * @param message the error code string
         */
        ErrorCodes(int code, String message) {
            this.statusCode = code;
            this.errorCode = message;
        }

        /**
         * Gets the HTTP status code.
         *
         * @return the status code
         */
        public int getStatusCode() {
            return statusCode;
        }

        /**
         * Gets the error code string.
         *
         * @return the error code string
         */
        public String getErrorCode() {
            return errorCode;
        }
    }

    /**
     * Returns the error code string for a given HTTP status code. If the status code is not found,
     * returns "InternalServerError".
     *
     * @param statusCode the HTTP status code
     * @return the error code string
     */
    public static String getErrorCode(int statusCode) {
        ErrorCodes[] codes = ErrorCodes.values();
        for (ErrorCodes code : codes) {
            if (code.getStatusCode() == statusCode) {
                return code.getErrorCode();
            }
        }
        return ErrorCodes._500.getErrorCode();
    }
}
