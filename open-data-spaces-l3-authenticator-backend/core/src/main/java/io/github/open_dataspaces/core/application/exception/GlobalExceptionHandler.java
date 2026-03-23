/*
 * GlobalExceptionHandler.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This ControllerAdvice provides centralized exception handling for the application.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.application.exception;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.dto.APIErrorResponse;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.ConflictException;
import io.github.open_dataspaces.core.exception.ForbiddenException;
import io.github.open_dataspaces.core.exception.IllegalAuthDataException;
import io.github.open_dataspaces.core.exception.IllegalOperatorIdException;
import io.github.open_dataspaces.core.exception.LoginException;
import io.github.open_dataspaces.core.exception.NotFoundException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnauthorizedException;
import io.github.open_dataspaces.core.exception.UnexpectedException;
import io.github.open_dataspaces.core.exception.ValidateException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.ProcessingException;

/**
 * Global exception handler for the application.
 *
 * <p>This class provides centralized handling of exceptions thrown by controllers, returning
 * standardized error responses for various error scenarios.
 * </p>
 *
 * <p>This class provides centralized handling of exceptions thrown by controllers,
 * returning
 * standardized error responses for various error scenarios.</p>
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Generates a standardized HTTP error response.
     *
     * @param message           the error message
     * @param messageDetail     the detailed error message
     * @param status            the HTTP status
     * @param source            the error source
     * @param request           the HTTP servlet request
     * @return ResponseEntity containing the error response
     */
    @NonNull
    private final ResponseEntity<APIErrorResponse> httpErrorGenerator(String message,
            String messageDetail, HttpStatus status, String source, HttpServletRequest request) {

        // Create the error response body
        String errorMsg = StringUtils.hasText(messageDetail)
                ? String.format(ConstError.ERR_RESPONSE_BODY_MESSAGE, message, messageDetail)
                : message;
        if (!errorMsg.endsWith(".")) { // Ensure the errorMessage ends with a period
            StringBuilder sb = new StringBuilder(errorMsg);
            sb.append(".");
            errorMsg = sb.toString();
        }

        // Return the error response
        return ResponseEntity.status(status).header("Content-Type", Const.CONTENT_TYPE_JSON)
                .body(new APIErrorResponse(request, errorMsg, status.value()));
    }

    /**
     * Generates a standardized HTTP error response for WebRequest.
     *
     * @param message           the error message
     * @param messageDetail     the detailed error message
     * @param status            the HTTP status
     * @param source            the error source
     * @param request           the WebRequest object
     * @param isEmptyDataTarget whether to empty the data target in the response
     * @return ResponseEntity containing the error response
     */
    @NonNull
    private final ResponseEntity<APIErrorResponse> httpErrorGenerator(String message,
            String messageDetail, HttpStatus status, String source, WebRequest request,
            boolean isEmptyDataTarget) {
        return this.httpErrorGenerator(message, messageDetail, status, source,
                ((ServletWebRequest) request).getRequest());
    }

    /**
     * Logs a warning message with the specified exception and message.
     *
     * @param e the exception to log
     * @param message the message to log
     */
    private void logWarn(Throwable e, String message) {
        LOGGER.debug("GlobalExceptionHandler logWarn Throwable Stacktrace", e);
        if (LOGGER.isWarnEnabled()) {
            String logMessage = String.format(ConstError.ERRLOG_FORMAT, e.getStackTrace()[0].getFileName(),
                    e.getStackTrace()[0].getLineNumber(), message);
            LOGGER.warn(logMessage);
        }
    }

    /**
     * Logs an error message with the specified exception and message.
     *
     * @param e the exception to log
     * @param message the message to log
     */
    private void logError(Throwable e, String message) {
        LOGGER.debug("GlobalExceptionHandler logError Throwable Stacktrace", e);
        if (LOGGER.isErrorEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(ConstError.ERRLOG_FORMAT, e.getStackTrace()[0].getFileName(),
                    e.getStackTrace()[0].getLineNumber(), message));
            sb.append(" | Exception: ").append(e.getClass().getName()).append(": ").append(e.getMessage());
            for (StackTraceElement ste : e.getStackTrace()) {
                sb.append(", at ").append(ste.toString());
            }
            LOGGER.error(sb.toString());
        }
    }

    /**
     * Handles IllegalAuthDataException.
     *
     * @param e       the exception to handle
     * @param request the HTTP servlet request
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(IllegalAuthDataException.class)
    public ResponseEntity<APIErrorResponse> handleAPIKeyMissingException(IllegalAuthDataException e,
            HttpServletRequest request) {
        // Log the warning message
        logWarn(e, e.getLogMessage());

        // Generate the error response and return it
        return httpErrorGenerator(e.getResponseMessage(), "", HttpStatus.FORBIDDEN, e.getSource(),
                request);
    }

    /**
     * Handles ValidateException.
     *
     * @param e       the exception to handle
     * @param request the HTTP servlet request
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(ValidateException.class)
    public ResponseEntity<APIErrorResponse> handleValidateException(ValidateException e,
            HttpServletRequest request) {
        // Log the warning message
        logWarn(e, e.getLogMessage());

        // Generate the error response and return it
        return httpErrorGenerator(e.getResponseMessage(), "", HttpStatus.BAD_REQUEST, e.getSource(),
                request);
    }

    /**
     * Handles BadParametersException.
     *
     * @param e       the exception to handle
     * @param request the HTTP servlet request
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(BadParametersException.class)
    public ResponseEntity<APIErrorResponse> handleBadParametersException(BadParametersException e,
            HttpServletRequest request) {
        // Log the warning message
        logWarn(e, e.getLogMessage());

        // Generate the error response and return it
        return httpErrorGenerator(e.getResponseMessage(), "", HttpStatus.BAD_REQUEST, e.getSource(),
                request);
    }

    /**
     * Handles LoginException.
     *
     * @param e       the exception to handle
     * @param request the HTTP servlet request
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(LoginException.class)
    public ResponseEntity<APIErrorResponse> handleLoginException(LoginException e,
            HttpServletRequest request) {
        // Log the warning message
        logWarn(e, e.getLogMessage());

        // Generate the error response and return it
        return httpErrorGenerator(e.getResponseMessage(), "", HttpStatus.UNAUTHORIZED,
                e.getSource(), request);
    }

    /**
     * Handles UnexpectedException.
     *
     * @param e       the exception to handle
     * @param request the HTTP servlet request
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(UnexpectedException.class)
    public ResponseEntity<APIErrorResponse> handleUnexpectedException(UnexpectedException e,
            HttpServletRequest request) {
        // Log the error message
        logError(e, e.getLogMessage());

        // Generate the error response and return it
        return httpErrorGenerator(e.getResponseMessage(), "", HttpStatus.INTERNAL_SERVER_ERROR,
                e.getSource(), request);
    }

    /**
     * Handles IllegalOperatorIdException.
     *
     * @param e       the exception to handle
     * @param request the HTTP servlet request
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(IllegalOperatorIdException.class)
    public ResponseEntity<APIErrorResponse> handleIllegalOperatorIdException(IllegalOperatorIdException e,
            HttpServletRequest request) {
        // Log the error message
        logError(e, e.getLogMessage());

        // Generate the error response and return it
        return httpErrorGenerator(e.getResponseMessage(), "", HttpStatus.INTERNAL_SERVER_ERROR,
                e.getSource(), request);
    }

    /**
     * Handles ConflictException.
     *
     * <p>This method handles ConflictException, which may occur due to conflicting
     * input parameters. Mainly used for JPA validation errors or other cases where the
     * input does not meet the expected criteria.</p>
     *
     * @param e       the exception to handle
     * @param request the HTTP servlet request
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<APIErrorResponse> handleConflictException(ConflictException e,
            HttpServletRequest request) {
        // Log the warning message
        logWarn(e, e.getLogMessage());

        // Generate the error response and return it
        return httpErrorGenerator(e.getMessage(), "", HttpStatus.CONFLICT,
                Const.SOURCE_AUTH, request);
    }

    /**
     * Handles NotFoundException.
     *
     * <p>This method handles NotFoundException, which may occur when a requested resource is not found.
     * Mainly used for cases where the input does not meet the expected criteria or the resource does not exist.</p>
     *
     * @param e       the exception to handle
     * @param request the HTTP servlet request
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<APIErrorResponse> handleNotFoundException(
            NotFoundException e,
            HttpServletRequest request) {
        // Log the warning message
        logWarn(e, e.getLogMessage());

        // Generate the error response and return it
        return httpErrorGenerator(e.getMessage(), "", HttpStatus.NOT_FOUND,
                Const.SOURCE_AUTH, request);
    }

    /**
     * Handles OutOfServiceException.
     *
     * @param e       the exception to handle
     * @param request the HTTP servlet request
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(OutOfServiceException.class)
    public ResponseEntity<APIErrorResponse> handleOutOfServiceException(OutOfServiceException e,
            HttpServletRequest request) {
        // Log the error message
        logError(e, e.getLogMessage());

        // Generate the error response and return it
        return httpErrorGenerator(ConstError.ERR_503_OUTER_SERVICE_EXCEPTION, "", HttpStatus.SERVICE_UNAVAILABLE,
                e.getSource(), request);
    }

    /**
     * Handles UnauthorizedException.
     *
     * @param e       the exception to handle
     * @param request the HTTP servlet request
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<APIErrorResponse> handleUnauthorizedException(UnauthorizedException e,
            HttpServletRequest request) {
        // Log the warning message
        logWarn(e, e.getLogMessage());

        // Generate the error response and return it
        if (StringUtils.hasText(e.getResponseMessage())) {
            return httpErrorGenerator(e.getResponseMessage(), "",
                HttpStatus.UNAUTHORIZED, e.getSource(), request);
        } else {
            return httpErrorGenerator(ConstError.ERR_401, "",
                HttpStatus.UNAUTHORIZED, e.getSource(), request);
        }
    }

    /**
     * Handles ForbiddenException.
     *
     * @param e       the exception to handle
     * @param request the HTTP servlet request
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<APIErrorResponse> handleForbiddenException(ForbiddenException e,
            HttpServletRequest request) {
        // Log the warning message
        logWarn(e, e.getLogMessage());

        // Generate the error response and return it
        return httpErrorGenerator(e.getLogMessage(), "", HttpStatus.FORBIDDEN,
                Const.SOURCE_AUTH, request);
    }

    /**
     * Handles RestClientException.
     *
     * @param e       the exception to handle
     * @param request the HTTP servlet request
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<APIErrorResponse> handleRestClientException(RestClientException e,
            HttpServletRequest request) {
        // Log the error message
        logError(e, e.getMessage());

        // Generate the error response and return it
        return httpErrorGenerator(ConstError.ERR_500_MESSAGE, "",
                HttpStatus.INTERNAL_SERVER_ERROR, Const.SOURCE_AUTH, request);
    }

    /**
     * Handles DuplicateKeyException.
     *
     * @param e       the exception to handle
     * @param request the HTTP servlet request
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<APIErrorResponse> handleDuplicateKeyException(DuplicateKeyException e,
            HttpServletRequest request) {
        // Log the error message
        logError(e, e.getMessage());

        // Generate the error response and return it
        return httpErrorGenerator(ConstError.ERR_500_MESSAGE, "", HttpStatus.INTERNAL_SERVER_ERROR,
                Const.SOURCE_AUTH, request);
    }

    /**
     * Handles IllegalArgumentException.
     *
     * <p>This method handles IllegalArgumentException, which may occur due to invalid
     * input parameters. Mainly used for JPA validation errors or other cases where the
     * input does not meet the expected criteria.</p>
     *
     * @param e       the exception to handle
     * @param request the HTTP servlet request
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<APIErrorResponse> handleIllegalArgumentException(IllegalArgumentException e,
            HttpServletRequest request) {
        // Log the error message
        logError(e, e.getMessage());

        // Generate the error response and return it
        return httpErrorGenerator(ConstError.ERR_400_ILLEGAL_ARGUMENT, "", HttpStatus.BAD_REQUEST,
                Const.SOURCE_AUTH, request);
    }

    /**
     * Handles ProcessingException thrown during external service processing.
     * (e.g. when a REST call to an external service fails)
     *
     * <p>This method logs the error and returns a standardized error response with
     * HTTP 503 (Service Unavailable).</p>
     *
     * @param e       the ProcessingException to handle
     * @param request the HTTP servlet request
     * @return ResponseEntity containing the error response with HTTP 503 status
     */
    @ExceptionHandler(ProcessingException.class)
    public ResponseEntity<APIErrorResponse> handleInvocationTargetException(ProcessingException e,
            HttpServletRequest request) {
        // Log the error message
        logError(e, e.getMessage());

        // Generate the error response and return it
        return httpErrorGenerator(ConstError.ERR_500_MESSAGE, "",
                HttpStatus.INTERNAL_SERVER_ERROR, Const.SOURCE_AUTH, request);
    }

    /**
     * Handles JsonParseException thrown when JSON parsing fails.
     */
    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<APIErrorResponse> handleJsonParseException(JsonProcessingException ex, HttpServletRequest request) {
        // Log the warn message
        logWarn(ex, ex.getMessage());

        // Generate the error response and return it
        String[] errorMessages = ex.getMessage().split("\\n");
        String errorMessage = String.format(ConstError.ERR_400_JSON_PARSE_ERROR,
                errorMessages.length >= 1 ? errorMessages[0] : ConstError.ERR_400_HTTP_MESSAGE_NOT_READABLE);
        return httpErrorGenerator(
                errorMessage,
                "",
                HttpStatus.BAD_REQUEST,
                Const.SOURCE_AUTH,
                request);
    }

    /**
     * Handles ConstraintViolationException thrown when validation constraints are violated.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<APIErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        logWarn(ex, ex.getMessage());

        // Generate the error response message
        String message = ex.getConstraintViolations().stream()
                .map(cv -> {
                    return String.format(cv.getMessage(), cv.getPropertyPath().toString());
                })
                .collect(Collectors.joining("; "));
        return httpErrorGenerator(
                ConstError.ERR_400_VALIDATION_FAILED_HEADER + message,
                "",
                HttpStatus.BAD_REQUEST,
                Const.SOURCE_AUTH,
                request);
    }

    /**
     * Handles all other exceptions.
     *
     * @param e       the exception to handle
     * @param request the HTTP servlet request
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIErrorResponse> handleException(Exception e, HttpServletRequest request) {
        // Log the error message
        logError(e, e.getMessage());

        // Generate the error response and return it
        return httpErrorGenerator(ConstError.ERR_500, "", HttpStatus.INTERNAL_SERVER_ERROR,
                Const.SOURCE_AUTH, request);
    }

    /**
     * Handles {@link HttpMediaTypeNotAcceptableException} thrown when the requested media type is not acceptable.
     *
     * <p>This method logs the error and returns a standardized error response with HTTP 400 (Bad Request).</p>
     *
     * @param ex         the {@link HttpMediaTypeNotAcceptableException} thrown when the media type is not acceptable
     * @param headers    the HTTP headers to be written to the response
     * @param statusCode the HTTP status code to be returned
     * @param request    the current web request
     * @return ResponseEntity containing the standardized error response
     */
    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
            @NonNull HttpMediaTypeNotAcceptableException ex,
            @NonNull HttpHeaders headers, @NonNull HttpStatusCode statusCode,
            @NonNull WebRequest request) {
        // Log the error message
        logWarn(ex, ex.getMessage());

        // Generate the error response and return it
        String errorMessage = ex.getMessage().split(":")[0];
        ResponseEntity<APIErrorResponse> responseEntity = httpErrorGenerator(
                StringUtils.hasText(errorMessage) ? errorMessage : ConstError.ERR_400, "",
                HttpStatus.BAD_REQUEST, Const.SOURCE_AUTH, request, true);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(responseEntity.getHeaders())
                .body(responseEntity.getBody());
    }

    /**
     * Handles HttpMessageNotReadableException thrown when the HTTP request body is missing or unreadable.
     *
     * <p>This method logs the error and returns a standardized error response with HTTP 400 (Bad Request).</p>
     *
     * @param ex         the HttpMessageNotReadableException thrown when the request body is not readable
     * @param headers    the HTTP headers to be written to the response
     * @param statusCode the HTTP status code to be returned
     * @param request    the current web request
     * @return ResponseEntity containing the standardized error response
     */
    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            @NonNull HttpMessageNotReadableException ex,
            @NonNull HttpHeaders headers, @NonNull HttpStatusCode statusCode,
            @NonNull WebRequest request) {
        // Log the warn message
        logWarn(ex, ex.getMessage());

        // Generate the error response and return it
        String[] errorMessages = ex.getMessage().split(":");
        String errorMessage = errorMessages.length >= 2
                ? new StringBuilder(errorMessages[0]).append(":").append(errorMessages[1]).toString() :
                (errorMessages.length != 0 ? errorMessages[0] : ConstError.ERR_400_HTTP_MESSAGE_NOT_READABLE);
        ResponseEntity<APIErrorResponse> responseEntity = httpErrorGenerator(
                StringUtils.hasText(errorMessage) ? errorMessage : ConstError.ERR_400_HTTP_MESSAGE_NOT_READABLE, "",
                HttpStatus.BAD_REQUEST, Const.SOURCE_AUTH, request, true);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(responseEntity.getHeaders())
                .body(responseEntity.getBody());
    }

    /**
     * Handles UnsupportedMediaTypeException.
     *
     * <p>This method handles UnsupportedMediaTypeException, which may occur when the
     * client sends a request with a media type that is not supported by the server.</p>
     *
     * @param ex         the exception to handle
     * @param headers    the HTTP headers
     * @param statusCode the HTTP status code
     * @param request    the web request
     */
    @Override
    @NonNull
    public ResponseEntity<Object> handleHttpMediaTypeNotSupported(@NonNull HttpMediaTypeNotSupportedException ex,
            @NonNull HttpHeaders headers, @NonNull HttpStatusCode statusCode, @NonNull WebRequest request) {
        // Log the error message
        logError(ex, ex.getMessage());

        // Generate the error response and return it
        String errorMessage = ex.getMessage().split(":")[0];
        ResponseEntity<APIErrorResponse> responseEntity = httpErrorGenerator(
                ConstError.ERR_415_UNSUPPORTED_MEDIA_TYPE, StringUtils.hasText(errorMessage) ? errorMessage : ConstError.ERR_415_UNSUPPORTED_MEDIA_TYPE,
                HttpStatus.UNSUPPORTED_MEDIA_TYPE, Const.SOURCE_AUTH, request, true);

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).headers(responseEntity.getHeaders())
                .body(responseEntity.getBody());
    }

    /**
     * Handles MethodValidationException.
     *
     * <p>This method customizes the handling of exceptions that occur during
     * validation or internal
     * request processing in Spring MVC, returning a standardized error response.</p>
     *
     * @param ex         the exception to handle
     * @param body       the response body
     * @param headers    the HTTP headers
     * @param statusCode the HTTP status code
     * @param request    the web request
     * @return ResponseEntity containing the standardized error response
     */
    @Override
    @Nullable
    protected ResponseEntity<Object> handleExceptionInternal(@NonNull Exception ex,
            @Nullable Object body, @NonNull HttpHeaders headers, @NonNull HttpStatusCode statusCode,
            @NonNull WebRequest request) {
        // Log the error message
        logWarn(ex, ex.getMessage());

        // Generate the error response and return it
        ResponseEntity<APIErrorResponse> responseEntity = httpErrorGenerator(ConstError.ERR_404_ERROR,
                "", HttpStatus.NOT_FOUND, Const.SOURCE_AUTH, request, true);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(responseEntity.getHeaders())
                .body(responseEntity.getBody());
    }
}
