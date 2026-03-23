/*
 * EnumResponseTypes.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class defines error code constants and utility methods for HTTP status codes.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.common.enums;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Provides error code constants and utility methods for HTTP status codes.
 *
 * <p>This class defines an enum for common HTTP error codes and messages, and provides a method to
 * retrieve the error code string by status code.</p>
 */
public class EnumResponseTypes {

    // Private constructor to prevent instantiation. It is for testing purposes only.
    private EnumResponseTypes() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    /**
     * Enum representing common HTTP error codes and messages.
     */
    public enum ResponseTypes {

        SUCCESS(HttpStatus.OK.value(), "%s/api/success"),
            _201(HttpStatus.CREATED.value(), "%s/api/created"),
            _400(HttpStatus.BAD_REQUEST.value(), "%s/api/bad-request"),
            _401(HttpStatus.UNAUTHORIZED.value(), "%s/api/unauthorized"),
            _403(HttpStatus.FORBIDDEN.value(), "%s/api/forbidden"),
            _404(HttpStatus.NOT_FOUND.value(), "%s/api/not-found"),
            _409(HttpStatus.CONFLICT.value(), "%s/api/conflict"),
            _415(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), "%s/api/unsupported-media-type"),
            _500(HttpStatus.INTERNAL_SERVER_ERROR.value(), "%s/api/internal-server-error"),
            _503(HttpStatus.SERVICE_UNAVAILABLE.value(), "%s/api/service-unavailable");

        private final int statusCode;
        private final String errorCode;

        /**
         * Constructor for ErrorCodes.
         *
         * @param code the HTTP status code
         * @param message the error code string
         */
        ResponseTypes(int code, String message) {
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
        public String getResponseType() {
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
    public static String getResponseType(int statusCode) {
        ResponseTypes[] codes = ResponseTypes.values();
        for (ResponseTypes code : codes) {
            if (code.getStatusCode() == statusCode) {
                return String.format(code.getResponseType(), ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString());
            }
        }
        return String.format(ResponseTypes._500.getResponseType(), ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString());
    }
}
