/*
 * ConstError.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class defines constant error messages.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.common.consts;

/**
 * Constant class used in authentication and authorization APIs.
 *
 * <p>Mainly defines error message constants, categorized by HTTP status codes.<br>
 * Each constant is used in API responses and log outputs.</p>
 */

public class ConstError {
    public static final String VALIDATION_ERROR_DELIMITER = "; ";
    public static final String ERRLOG_FORMAT = "%s: %s: %s";

    // Response body message template
    public static final String ERR_RESPONSE_BODY_MESSAGE = "%s, %s";

    // Error Validation Messages Commons
    public static final String ERR_VALIDATION_COMMON_NOT_BLANK = "{field.blank}";
    public static final String ERR_VALIDATION_COMMON_SIZE = "{field.size}";
    public static final String ERR_VALIDATION_COMMON_PATTERN = "{field.pattern}";
    public static final String ERR_VALIDATION_COMMON_PATTERN_UUID = "{field.pattern.uuid}";
    public static final String ERR_VALIDATION_COMMON_PATTERN_DATE = "{field.pattern.date}";
    public static final String ERR_VALIDATION_COMMON_PATTERN_BOOLEAN = "{field.pattern.boolean}";
    public static final String ERR_VALIDATION_COMMON_REQUIRED = "{field.required}";
    public static final String ERR_VALIDATION_COMMON_RANGE_MIN = "{field.range.min}";

    // Error Validation Messages
    public static final String ERR_VALIDATION_INVALID_UUID = "%s: invalid UUID";
    public static final String ERR_VALIDATION_INVALID_PATH_PARAMETER = "%s: invalid path parameter";
    public static final String ERR_VALIDATION_ENUM_DEFAULT = "invalid value. Must match one of the enum values";
    public static final String ERR_VALIDATION_AUTH_LOGIN_USER_ID_OR_PASSWORD_INVALID = "login_user_id or password: invalid value";
    public static final String ERR_VALIDATION_PASSWORD_INVALID = "old_password or new_password: invalid value";
    public static final String ERR_EFFECTIVE_DATE_START_AFTER_END = "effectiveStartDate: must be before effectiveEndDate";
    public static final String ERR_VALIDATION_OPEN_SYSTEM_ID_REQUIRED = "open_system_id: required for client_credentials";
    public static final String ERR_VALIDATION_CLIENT_NOT_ISSUED_BY_API = "client_id: specified client_id was not issued by the client ID issuance API";

    // 400 Error Messages(Response)
    public static final String ERR_400 = "Bad Request.";
    public static final String ERR_400_ILLEGAL_ARGUMENT = "Invalid Argument.";
    public static final String ERR_400_HTTP_MESSAGE_NOT_READABLE = "Malformed Request Body, Unreadable HTTP Message.";
    public static final String ERR_400_VALIDATION_FAILED_HEADER = "Validation failed, ";
    public static final String ERR_400_REQUEST_TOO_LARGE = "Request payload too large.";
    public static final String ERR_400_JSON_PARSE_ERROR = "JSON parse error: %s";
    public static final String ERR_400_BAD_REQUEST_MESSAGE = "Bad Request, %s";
    public static final String ERR_400_INVALID_GRANT = "Bad Request, Invalid grant.";
    public static final String ERR_400_INVALID_VALUE_OR_MISSING = "%s: invalid value or missing";
    public static final String ERR_400_VALIDATION_REDIRECT_URIS_REQUIRED = "redirect_uris: required for authorization_code";

    // 400 Error Messages(LOG)
    public static final String ERRLOG_400_INVALID_REQUEST = "Invalid request parameters.";
    public static final String ERRLOG_400_INVALID_REQUEST_PARAMETER = "Invalid request parameters, %s: %s.";
    public static final String ERRLOG_400_INVALID_GRANT = "Bad Request, Invalid grant. %s.";
    public static final String ERRLOG_400_EFFECTIVE_DATE_START_AFTER_END = "Invalid request parameters, %s: effective start date '%s' is after effective end date '%s'.";

    // 401 Error Messages(Response)
    public static final String ERR_401 = "Authentication required";
    public static final String ERR_401_INVALID_CREDENTIALS = "Invalid credentials.";
    public static final String ERR_401_TOKEN_EMPTY = "Authentication required.";
    public static final String ERR_401_INVALID_TOKEN = "Invalid or expired token.";
    public static final String ERR_401_INVALID_OR_EXPIRED_TOKEN = "Invalid or expired token.";
    public static final String ERR_401_INVALID_CLIENT = "Invalid client id or client secret.";

    // 401 Error Messages(Log)
    public static final String ERRLOG_401_LOGIN_ERROR = "%s: id: %s.";
    public static final String ERRLOG_401_TOKEN_EMPTY = "Token is empty.";
    public static final String ERRLOG_401_INVALID_TOKEN = "Token is not a valid JWT format.";
    public static final String ERRLOG_401_INVALID_JWT = "Failed to parse JWT payload: %s.";
    public static final String ERRLOG_401_INVALID_CLAIM = "Token does not contain claims.";
    public static final String ERRLOG_401_INVALID_CLAIM_NO_OPERATOR_OR_OPEN_SYSTEM_ID = "Token does not contain 'operator_id' or 'open_system_id' in claims.";
    public static final String ERRLOG_401_INVALID_CLIENT = "Invalid client id or client secret. %s.";
    public static final String ERRLOG_401_USER_NOT_FOUND = "User not found: %s.";
    public static final String ERRLOG_401_INVALID_CLAIM_MISSING = "Token '%s' claim is missing.";
    public static final String ERRLOG_401_INVALID_CLAIM_MISMATCH = "Token '%s' claim is mismatched: expected: '%s', actual: '%s'.";
    public static final String ERRLOG_401_INVALID_CLAIM_INVALID = "Token '%s' claim has invalid value: %s.";
    public static final String ERRLOG_401_INVALID_CLAIM_FORMAT = "Token '%s' claim has invalid format: %s.";

    // 403 Error Messages(Response)
    public static final String ERR_403_APIKEY_NOT_PROVIDED = "You do not have the necessary privileges.";
    public static final String ERR_403_APIKEY_NOT_VALID = "Invalid API key.";
    public static final String ERR_403_IP_NOT_PROVIDED = "You do not have the necessary privileges.";
    public static final String ERR_403_IP_NOT_AUTHORIZED_FOR_KEY = "IP address not authorized for this API key.";
    public static final String ERR_403_AUTHORIZATION_FAILED = "Authorization failed for user '%s' on resource '%s' with action '%s'.";

    // 403 Error Messages(Log)
    public static final String ERRLOG_403_APIKEY_NOT_PROVIDED = "No API key is provided.";
    public static final String ERRLOG_403_APIKEY_NOT_VALID = "Invalid API key.";
    public static final String ERRLOG_403_IP_NOT_PROVIDED = "No IP address is provided.";
    public static final String ERRLOG_403_IP_NOT_AUTHORIZED_FOR_KEY = "IP address not authorized for API key.";
    public static final String ERRLOG_403_FORBIDDEN_RESOURCE_DISABLED = "This resource is disabled and cannot be updated '%s'.";
    public static final String ERRLOG_403_INVALID_AUTHORIZATION = "Invalid authorization to access the resource '%s'.";
    public static final String ERRLOG_403_REALM_STORE_NOT_BOUND = "Realm '%s' is not bound to store '%s'.";

    // 404 Error Messages
    public static final String ERR_404_ERROR = "Endpoint not found.";
    public static final String ERR_404_RESOURCE_NOT_FOUND_MESSAGE = "Resource not found, %s";

    // 409 Error Messages(Response)
    public static final String ERR_409 = "Conflict.";
    public static final String ERR_409_CONFLICT = "%s conflict occurred '%s'.";
    public static final String ERR_409_CONFLICT_MESSAGE = "Conflict, %s";

    // 409 Error Messages(Log)
    public static final String ERRLOG_409_CONFLICT = "%s conflict: expected %s, but current is %s.";
    public static final String ERRLOG_409_STORE_CANNOT_BE_DELETED = "Store '%s' is reserved for administration and cannot be deleted.";

    // 415 Error Messages
    public static final String ERR_415_UNSUPPORTED_MEDIA_TYPE = "Unsupported media type";
    public static final String ERR_415_UNSUPPORTED_MEDIA_TYPE_REQUIRED = "Content-Type is required.";
    public static final String ERR_415_UNSUPPORTED_MEDIA_TYPE_NOT_SUPPORTED = "Content-Type '%s' is not supported.";

    // 500 Error Messages
    public static final String ERRLOG_500_ERROR = "%s: %s";
    public static final String ERRLOG_500_FAILED_TO_GENERATE_UUID = "%s: Failed to generate UUID.";
    public static final String ERRLOG_500_DB_TABLE_INVALID = "Invalid database table configuration '%s'";
    public static final String ERRLOG_500_KEYCLOAK_BUILD = "Keycloak client build failed.";
    public static final String ERRLOG_500_KEYCLOAK_NO_RESPONSE = "Keycloak: Unexpected error occurred(no response body).";
    public static final String ERRLOG_500_KEYCLOAK_FAILED = "Keycloak: Unexpected error occurred with status code %d.";
    public static final String ERRLOG_500_KEYCLOAK_FAILED_WITH_MESSAGE = "Keycloak: Unexpected error occurred: %s.";
    public static final String ERRLOG_500_KEYCLOAK_PARSE_FAILED = "Keycloak: Unexpected error occurred: Failed to parse response.";

    public static final String ERR_500 = "Unexpected error occurred";
    public static final String ERR_500_MESSAGE = "Unexpected error occurred.";
    public static final String ERR_500_DB_QUERY_FAILED = "Unexpected error occurred: DB query failed.";
    public static final String ERR_500_FILTER_CHAIN_ERROR = "Filterchain failed.";
    public static final String ERR_500_OPERATORID_INVALID = "Unexpected error occurred, operatorId: invalid UUID.";
    public static final String ERR_500_DATE_VALUE_NULL = "Unexpected error occurred, date value is null.";
    public static final String ERR_500_OPENFGA_FAILED_WITH_MESSAGE = "OpenFGA: Unexpected error occurred: %s.";

    // 503 Error Messages
    public static final String ERR_503_OUTER_SERVICE_EXCEPTION = "Unexpected error occurred in outer service.";
    public static final String ERR_503_OUT_OF_SERVICE_EXCEPTION = "Service unavailable.";
    public static final String ERR_503_OUT_OF_SERVICE_KEYCLOAK = "Keycloak: Service unavailable.";

    // Dump error messages
    public static final String ERRLOG_DUMP_SERIALIZE_FAILED = "Failed to serialize AuthDumpInfo: %s";
    // JSON Mask error messages
    public static final String ERRLOG_JSON_MASK_FAILED = "Failed to mask in JSON: %s";
    // Response body has no data
    public static final String ERRLOG_RESPONSE_BODY_NO_DATA = "Response body has no data";
    // Unsupported format
    public static final String ERRLOG_UNSUPPORTED_FORMAT = "Unsupported format: %s";
}
