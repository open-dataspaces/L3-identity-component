/*
 * Const.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class defines application-wide constant values.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.common.consts;

/**
 * Defines application-wide constant values.
 *
 * <p>This class contains configuration, environment, header, and other constants used throughout the
 * user authentication module.</p>
 */
public class Const {

    // Config Constants
    public static final String SPRING_PROFILES_ACTIVE_LOCAL = "local";

    public static final String DB_PARAM_POSTGRES_SSL = "ssl=%s";
    public static final String DB_PARAM_POSTGRES_SSL_MODE = "sslmode=%s";
    public static final String DB_PARAM_POSTGRES_ROOT_CERT = "sslrootcert=%s";
    public static final String DB_PARAM_POSTGRES_CERT = "sslcert=%s";
    public static final String DB_PARAM_POSTGRES_KEY = "sslkey=%s";

    // Package names
    public static final String CORE_ROOT_PACKAGE = "io.github.open_dataspaces.core";

    // Application packages
    public static final String APPLICATION_PACKAGE = CORE_ROOT_PACKAGE + ".application";
    public static final String COMMON_PACKAGE = CORE_ROOT_PACKAGE + ".common";
    public static final String DOMAIN_PACKAGE = CORE_ROOT_PACKAGE + ".domain";
    public static final String INFRASTRUCTURE_PACKAGE = CORE_ROOT_PACKAGE + ".infrastructure";
    // Controller package
    public static final String CONTROLLER_PACKAGE = CORE_ROOT_PACKAGE + ".application.controller";
    // JPA Entity package
    public static final String REPOSITORY_PACKAGE = CORE_ROOT_PACKAGE + ".domain.repository";
    public static final String REPOSITORY_ENTITIES = CORE_ROOT_PACKAGE + ".domain.entities";
    // DTO package for masking
    public static final String MASK_DTO_PACKAGE = CORE_ROOT_PACKAGE + ".domain.dto";
    // logging Aspect package
    public static final String LOGGING_ASPECT_CONTROLLER_PACKAGE = CORE_ROOT_PACKAGE + ".application.controller";
    public static final String LOGGING_ASPECT_SERVICE_PACKAGE = CORE_ROOT_PACKAGE + ".application.service";
    public static final String LOGGING_ASPECT_INFRASTRUCTURE_PACKAGE = CORE_ROOT_PACKAGE + ".infrastructure.repository";

    // Request size limit
    public static final long DEFAULT_LIMIT = 25 * 1024 * 1024; // 25MB

    // Date time Format
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String ISO_8601_UTC_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'";
    public static final String ISO_8601_UTC_MILLISECOND_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String TIMEZONE_UTC = "UTC";

    // Request parameter default values
    public static final boolean DEFAULT_CREATE_PASSWORD_FLAG = true;
    public static final boolean DEFAULT_PASSWORD_TEMPORARY_FLAG = false;
    public static final int DEFAULT_CREATE_PASSWORD_LENGTH = 12;

    // Regular Expressions for parameter validation
    public static final String REGEX_PASSWORD = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#\\$%\\^&\\*\\(\\)])[A-Za-z0-9!@#\\$%\\^&\\*\\(\\)]+$";
    public static final String REGEX_OLD_PASSWORD = "^$|^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#\\$%\\^&\\*\\(\\)])[A-Za-z0-9!@#\\$%\\^&\\*\\(\\)]+$";
    public static final String REGEX_OPEN_PLANT_ID = "^.*[0-9]{6}$";
    public static final String REGEX_UUID = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    public static final String REGEX_BOOLEAN = "^(?i)(true|false)$";
    public static final String REGEX_LOGIN_USER_ID = "^[a-z0-9_\\.\\-@]+$";
    public static final String REGEX_CLIENT_ID = "^[A-Za-z0-9_-]+$";
    public static final String REGEX_FLOW_TYPE = "^(authorization_code|client_credentials)$";
    public static final String REGEX_REDIRECT_URI = "^[^*%{}\\\\\\s\\p{Zs}\\p{Cc}]+$";

    // Create password using specified characters
    public static final String PASSWORD_SPECIAL_CHARS = "!@#$%^&*()";
    public static final String REGEX_OPEN_OPERATOR_ID = "^[a-zA-Z0-9]{1,20}$";
    public static final String REGEX_SORT_ORDER = "^(asc|desc)$";

    // Parameter length limits
    public static final int CLIENT_ID_LENGTH_MAX = 239;
    public static final int CLIENT_ID_LENGTH_MIN = 1;
    public static final int CLIENT_SECRET_LENGTH_MAX = 255;
    public static final int CLIENT_SECRET_LENGTH_MIN = 1;
    public static final int REDIRECT_URI_LENGTH_MAX = 255;
    public static final int REDIRECT_URI_LENGTH_MIN = 1;

    public static final int PASSWORD_LENGTH_MAX = 20;
    public static final int PASSWORD_LENGTH_MIN = 8;
    public static final int OLD_PASSWORD_LENGTH_MIN = 0;
    public static final int OPERATOR_ACCOUNT_ID_LENGTH_MAX = 255;
    public static final int OPERATOR_ACCOUNT_ID_LENGTH_MIN = 3;

    public static final int LOGIN_USER_ID_LENGTH_MAX = 255;
    public static final int LOGIN_USER_ID_LENGTH_MIN = 3;
    public static final int OPERATOR_LOGIN_USER_ID_LENGTH_MAX = 255;
    public static final int OPERATOR_LOGIN_USER_ID_LENGTH_MIN = 3;
    public static final int OPERATOR_NAME_LENGTH_MAX = 255;
    public static final int OPERATOR_NAME_LENGTH_MIN = 1;
    public static final int OPERATOR_ADDRESS_LENGTH_MAX = 256;
    public static final int OPERATOR_ADDRESS_LENGTH_MIN = 1;
    public static final int OPEN_OPERATOR_ID_LENGTH_MAX = 20;
    public static final int OPEN_OPERATOR_ID_LENGTH_MIN = 1;
    public static final int GLOBAL_OPERATOR_ID_LENGTH_MAX = 256;
    public static final int GLOBAL_OPERATOR_ID_LENGTH_MIN = 0;
    public static final int PLANT_NAME_LENGTH_MAX = 256;
    public static final int PLANT_NAME_LENGTH_MIN = 1;
    public static final int PLANT_ADDRESS_LENGTH_MAX = 256;
    public static final int PLANT_ADDRESS_LENGTH_MIN = 1;
    public static final int OPEN_PLANT_ID_LENGTH_MAX = 26;
    public static final int OPEN_PLANT_ID_LENGTH_MIN = 6;
    public static final int GLOBAL_PLANT_ID_LENGTH_MAX = 256;
    public static final int GLOBAL_PLANT_ID_LENGTH_MIN = 0;
    public static final int VERIFY_API_KEY_LENGTH_MIN = 1;
    public static final int VERIFY_API_KEY_LENGTH_MAX = 256;

    public static final int CLIENT_NAME_LENGTH_MAX = 255;
    public static final int CLIENT_DESCRIPTION_LENGTH_MAX = 255;
    public static final int CLIENT_OPEN_SYSTEM_ID_LENGTH_MIN  = 1;
    public static final int CLIENT_OPEN_SYSTEM_ID_LENGTH_MAX  = 255;

    // Parameter range limits
    public static final int COUNT_RANGE_MIN = 0;
    public static final int INDEX_RANGE_MIN = 0;

    // Evaluations batch size limits
    public static final int EVALUATIONS_BATCH_SIZE_MAX = 50;

    // JSON property names
    public static final String JSON_PROPERTY_ACTIVE = "active";

    // JSON property names for common properties
    public static final String JSON_PROPERTY_COMMON_TYPE = "type";
    public static final String JSON_PROPERTY_COMMON_TITLE = "title";
    public static final String JSON_PROPERTY_COMMON_DETAIL = "detail";
    public static final String JSON_PROPERTY_COMMON_STATUS = "status";
    public static final String JSON_PROPERTY_COMMON_DATA = "data";

    // JSON property names snake case
    public static final String JSON_PROPERTY_CLIENT_ID = "client_id";
    public static final String JSON_PROPERTY_CLIENT_SECRET = "client_secret";
    public static final String JSON_PROPERTY_REDIRECT_URI = "redirect_uri";
    public static final String JSON_PROPERTY_CODE = "code";
    public static final String JSON_PROPERTY_CODE_CHALLENGE = "code_challenge";
    public static final String JSON_PROPERTY_CODE_VERIFIER = "code_verifier";
    public static final String JSON_PROPERTY_LOGIN_USER_ID = "login_user_id";
    public static final String JSON_PROPERTY_LOGIN_USER_PASSWORD = "password";
    public static final String JSON_PROPERTY_OLD_PASSWORD = "old_password";
    public static final String JSON_PROPERTY_NEW_PASSWORD = "new_password";

    public static final String JSON_PROPERTY_URL = "url";
    public static final String JSON_PROPERTY_TOKEN_INFO = "token_info";
    public static final String JSON_PROPERTY_ACCESS_TOKEN = "access_token";
    public static final String JSON_PROPERTY_EXPIRES_IN = "expires_in";
    public static final String JSON_PROPERTY_TOKEN_TYPE = "token_type";
    public static final String JSON_PROPERTY_NOT_BEFORE_POLICY = "not_before_policy";
    public static final String JSON_PROPERTY_SCOPE = "scope";
    public static final String JSON_PROPERTY_REFRESH_TOKEN = "refresh_token";
    public static final String JSON_PROPERTY_REFRESH_EXPIRES_IN = "refresh_expires_in";
    public static final String JSON_PROPERTY_ID_TOKEN = "id_token";
    public static final String JSON_PROPERTY_IAT = "iat";
    public static final String JSON_PROPERTY_EXP = "exp";
    public static final String JSON_PROPERTY_USERNAME = "username";     // For user login id
    public static final String JSON_PROPERTY_SUB = "sub";
    public static final String JSON_PROPERTY_TYP = "typ";
    public static final String JSON_PROPERTY_ISS = "iss";
    public static final String JSON_PROPERTY_OPERATOR_ID = "operator_id";
    public static final String JSON_PROPERTY_OPEN_SYSTEM_ID = "open_system_id";
    public static final String JSON_PROPERTY_VERIFY_APIKEY = "verify_apikey";
    public static final String JSON_PROPERTY_VERIFY_RESULT = "verify_result";
    public static final String JSON_PROPERTY_FLOW_TYPE = "flow_type";

    // JSON Property Names Operator
    public static final String JSON_PROPERTY_OPERATOR_NAME = "operator_name";
    public static final String JSON_PROPERTY_OPEN_OPERATOR_ID = "open_operator_id";
    public static final String JSON_PROPERTY_GLOBAL_OPERATOR_ID = "global_operator_id";
    public static final String JSON_PROPERTY_EFFECTIVE_DATE = "effective_date";
    public static final String JSON_PROPERTY_EFFECTIVE_START_DATE = "effective_start_date";
    public static final String JSON_PROPERTY_EFFECTIVE_END_DATE = "effective_end_date";
    public static final String JSON_PROPERTY_DELETED_FLAG = "deleted_flag";
    public static final String JSON_PROPERTY_CREATED_AT = "created_at";
    public static final String JSON_PROPERTY_UPDATED_AT = "updated_at";

    public static final String JSON_PROPERTY_EMAIL_ADDRESS = "email_address";
    public static final String JSON_PROPERTY_OPERATOR_ADDRESS = "operator_address";
    public static final String JSON_PROPERTY_CREATE_PASSWORD_FLAG = "create_password_flag";
    public static final String JSON_PROPERTY_PASSWORD_TEMPORARY_FLAG = "password_temporary_flag";

    // JSON Property Names Plant
    public static final String JSON_PROPERTY_PLANT_ID = "plant_id";
    public static final String JSON_PROPERTY_PLANT_NAME = "plant_name";
    public static final String JSON_PROPERTY_PLANT_ADDRESS = "plant_address";
    public static final String JSON_PROPERTY_OPEN_PLANT_ID = "open_plant_id";
    public static final String JSON_PROPERTY_GLOBAL_PLANT_ID = "global_plant_id";

    // JSON Property Search
    public static final String JSON_PROPERTY_COUNT = "count";
    public static final String JSON_PROPERTY_INDEX = "index";
    public static final String JSON_PROPERTY_SORT = "sort";
    public static final String JSON_PROPERTY_SORT_KEY = "key";
    public static final String JSON_PROPERTY_SORT_ORDER = "order";

    // JSON Property
    public static final String JSON_PROPERTY_CURRENT_TIME = "current_time";
    public static final String JSON_PROPERTY_CLIENT_UUID = "client_uuid";
    public static final String JSON_PROPERTY_CLIENT_NAME = "name";
    public static final String JSON_PROPERTY_CLIENT_DESCRIPTION = "description";
    public static final String JSON_PROPERTY_REDIRECT_URIS = "redirect_uris";
    public static final String JSON_PROPERTY_CLIENT_ENABLED = "enabled";

    // JSON Property Names Store
    public static final String JSON_PROPERTY_STORE_ID = "id";
    public static final String JSON_PROPERTY_STORE_NAME = "name";

    // JWT Claim Names
    public static final String JWT_CLAIM_OPERATOR_ID = "operator_id";
    public static final String JWT_CLAIM_OPEN_SYSTEM_ID = "open_system_id";
    public static final String JWT_CLAIM_IAT = "iat";
    public static final long JWT_CLAIM_IAT_FUTURE_ALLOWANCE_SECONDS = 60L;
    public static final String JWT_CLAIM_SUB = "sub";
    public static final String JWT_CLAIM_AUD = "aud";
    public static final String JWT_CLAIM_TYP = "typ";

    // HTTP Header Constants
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_API_KEY = "API-Key";
    public static final String HEADER_X_CONFIRM_STORE_ID = "X-Confirm-Store-Id";

    public static final String HEADER_CONTENT_SECURITY_POLICY = "Content-Security-Policy";
    public static final String HEADER_X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    public static final String HEADER_STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";
    public static final String HEADER_X_TRACKING_ID = "X-TrackingID";

    public static final String HEADER_CONTENT_SECURITY_POLICY_VALUE = "default-src 'none'";
    public static final String HEADER_X_CONTENT_TYPE_OPTIONS_VALUE = "nosniff";
    public static final String HEADER_STRICT_TRANSPORT_SECURITY_VALUE = "max-age=63072000; includeSubDomains";

    public static final String HEADER_LOG_MESSAGE = "Response headers set: {}";
    public static final String HEADER_LOG_FIELD_FORMAT = "%s=%s;";

    public static final String HEADER_BEARER_TOKEN_PATTERN = "(?i)^bearer\\s+";

    // API Path Parameters
    public static final String API_PATH_PARAM_OPERATOR_ID = "operator_id";
    public static final String API_PATH_PARAM_PLANT_ID = "plant_id";
    public static final String API_PATH_PARAM_STORE_ID = "store_id";
    public static final String API_PATH_PARAM_CLIENT_ID = "client_id";

    // API Response Constants
    public static final String API_RESPONSE_TITLE_SUCCESS = "Request processed successfully";
    public static final String API_RESPONSE_DETAIL_SEPARATOR = ", ";
    public static final String API_RESPONSE_DETAIL_SUCCESS_FORMAT_TIMESTAMP = "time_stamp:%s";
    public static final String API_RESPONSE_DETAIL_SUCCESS_FORMAT_METHOD = "method:%s";

    // KEYCLOAK Send Parameter Constants
    public static final String KEYCLOAK_SEND_PARAM_CLIENT_ID = "client_id";
    public static final String KEYCLOAK_SEND_PARAM_CLIENT_SECRET = "client_secret";
    public static final String KEYCLOAK_SEND_PARAM_GRANT_TYPE = "grant_type";
    public static final String KEYCLOAK_SEND_PARAM_TOKEN = "token";
    public static final String KEYCLOAK_SEND_PARAM_TOKEN_TYPE_HINT = "token_type_hint";
    public static final String KEYCLOAK_SEND_PARAM_REFRESH_TOKEN = "refresh_token";
    public static final String KEYCLOAK_SEND_PARAM_AUTHORIZATION_CODE = "code";
    public static final String KEYCLOAK_SEND_PARAM_REDIRECT_URI = "redirect_uri";
    public static final String KEYCLOAK_SEND_PARAM_CODE_VERIFIER = "code_verifier";

    // KEYCLOAK Send Value Constants
    public static final String KEYCLOAK_SEND_VALUE_GRANT_TYPE = "refresh_token";
    public static final String KEYCLOAK_SEND_VALUE_GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";        // For Authorization Code Flow
    public static final String KEYCLOAK_SEND_VALUE_TOKEN_TYPE_HINT_ACCESS_TOKEN = "access_token"; // For Access Token Introspection
    public static final String KEYCLOAK_SEND_VALUE_GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials"; // For Client Credentials Flow

    // KEYCLOAK Entity Property Names
    public static final String KEYCLOAK_ENTITY_USERNAME = "username";
    public static final String KEYCLOAK_ENTITY_EMAIL = "email";

    public static final String KEYCLOAK_USERRESOURCE_CREDENTIAL_TYPE_PASSWORD = "password";
    public static final String KEYCLOAK_USERRESOURCE_REQUIRED_ACTION_UPDATE_PASSWORD = "UPDATE_PASSWORD";

    // KEYCLOAK Protocol / client authenticator
    public static final String KEYCLOAK_PROTOCOL_OPENID_CONNECT = "openid-connect";
    public static final String KEYCLOAK_CLIENT_AUTHENTICATOR_CLIENT_SECRET = "client-secret";

    // Standard header names
    public static final String HEADER_LOCATION = "Location";

    // KEYCLOAK OIDC protocol mappers
    public static final String KEYCLOAK_MAPPER_OIDC_USERMODEL_PROPERTY = "oidc-usermodel-property-mapper";
    public static final String KEYCLOAK_MAPPER_OIDC_HARDCODED_CLAIM = "oidc-hardcoded-claim-mapper";

    // KEYCLOAK MAPPER config keys
    public static final String KEYCLOAK_MAPPER_CONFIG_USER_ATTRIBUTE = "user.attribute";
    public static final String KEYCLOAK_MAPPER_CONFIG_PKCE_ENABLED = "pkceEnabled";
    public static final String KEYCLOAK_MAPPER_CONFIG_PKCE_CODE_CHALLENGE_METHOD = "pkce.code.challenge.method";
    public static final String KEYCLOAK_MAPPER_CONFIG_S256 = "S256";
    public static final String KEYCLOAK_MAPPER_CONFIG_ID = "id";

    // KEYCLOAK public static final String KEYCLOAK_MAPPER_CONFIG_PROPERTY = "property"; // Use per Keycloak version if needed
    public static final String KEYCLOAK_MAPPER_CONFIG_CLAIM_NAME = "claim.name";
    public static final String KEYCLOAK_MAPPER_CONFIG_CLAIM_VALUE = "claim.value";
    public static final String KEYCLOAK_MAPPER_CONFIG_JSON_TYPE_LABEL = "jsonType.label";
    public static final String KEYCLOAK_MAPPER_CONFIG_ID_TOKEN_CLAIM = "id.token.claim";
    public static final String KEYCLOAK_MAPPER_CONFIG_ACCESS_TOKEN_CLAIM = "access.token.claim";
    public static final String KEYCLOAK_MAPPER_CONFIG_USERINFO_TOKEN_CLAIM = "userinfo.token.claim";
    public static final String KEYCLOAK_MAPPER_CONFIG_INTROSPECTION_TOKEN_CLAIM = "introspection.token.claim";
    public static final String KEYCLOAK_MAPPER_CONFIG_ACCESS_TOKEN_RESPONSE_CLAIM = "access.token.response.claim";

    // Mapper config values
    public static final String KEYCLOAK_MAPPER_JSON_TYPE_STRING = "String";
    public static final String KEYCLOAK_MAPPER_CONFIG_TRUE = "true";
    public static final String KEYCLOAK_MAPPER_CONFIG_FALSE = "false";

    // KEYCLOAK Property
    public static final String KEYCLOAK_PROPERTY_OPERATOR_ID = "operator_id";
    public static final String KEYCLOAK_PROPERTY_OPEN_SYSTEM_ID = "open_system_id";
    public static final String KEYCLOAK_PROPERTY_CLIENT_ID = "client_id";
    public static final String KEYCLOAK_PROPERTY_CLIENT_UUID = "client_uuid";

    // URL Template Constants
    // example: https://keycloak.example.com/realms/{realm}/protocol/openid-connect/auth?client_id={clientId}&response_type=code&scope=openid&redirect_uri={redirectUri}&code_challenge={codeChallenge}&code_challenge_method=S256
    public static final String URL_TEMPLATE_KEYCLOAK_AUTHORIZATION =
            "%s/realms/%s/protocol/openid-connect/auth?client_id=%s&response_type=%s&scope=%s&redirect_uri=%s&code_challenge=%s&code_challenge_method=%s";
    // example: https://keycloak.example.com/realms/{realm}/protocol/openid-connect/token
    public static final String URL_TEMPLATE_KEYCLOAK_ACCESS_TOKEN =
            "%s/realms/%s/protocol/openid-connect/token";
    // example: https://keycloak.example.com/realms/{realm}/protocol/openid-connect/token/introspect
    public static final String URL_TEMPLATE_KEYCLOAK_TOKEN_INTROSPECTION =
            "%s/realms/%s/protocol/openid-connect/token/introspect";
    // example: http://keycloak.example.com/realms/{realm}/protocol/openid-connect/auth?client_id={clientId}&response_type=code&scope=openid&redirect_uri={redirectUri}&kc_action=UPDATE_PASSWORD&code_challenge={codeChallenge}&code_challenge_method=S256&prompt=login
    public static final String URL_TEMPLATE_KEYCLOAK_PASSWORD_CHANGE =
            "%s/realms/%s/protocol/openid-connect/auth?client_id=%s&response_type=%s&scope=%s&redirect_uri=%s&kc_action=UPDATE_PASSWORD&prompt=login&code_challenge=%s&code_challenge_method=%s";
    // example: https://keycloak.example.com/realms/{realm}/protocol/openid-connect/revoke
    public static final String URL_TEMPLATE_KEYCLOAK_REVOKE =
            "%s/realms/%s/protocol/openid-connect/revoke";

    // example: https://keycloak.example.com/realms/{realm}
    public static final String URL_TEMPLATE_KEYCLOAK_ISSUER =
            "%s/realms/%s";
    // example: https://keycloak.example.com/realms/{realm}/protocol/openid-connect/certs
    public static final String URL_TEMPLATE_KEYCLOAK_JWKS_URI =
            "%s/protocol/openid-connect/certs";

    // Dump messages
    public static final String DUMP_BODY = "Auth Access Path: %s, Header: %s, Request Body: %s, Response Body: %s";
    public static final String DUMP_IP_FOR_APIKEY = "{\"event\":\"apiKey\",\"isRequestResult\":%s,\"timeStamp\":\"%s\",\"requestIpAddress\":\"%s\",\"requestApiKey\":\"%s\"}";
    public static final String DUMP_ITEM_EVENT = "event";
    public static final String DUMP_ITEM_IS_REQUEST_RESULT = "isRequestResult";
    public static final String DUMP_ITEM_REQUEST_BODY = "requestBody";
    public static final String DUMP_ITEM_RESPONSE_BODY = "responseBody";
    public static final String DUMP_ITEM_TIME_STAMP = "timeStamp";
    public static final String DUMP_ITEM_REQUEST_IP_ADDRESS = "requestIpAddress";
    public static final String DUMP_ITEM_REQUEST_API_KEY = "requestApiKey";

    // Mask Constants
    public static final String MASK = "******";

    // OpenFGA Constants
    public static final String OPENFGA_EVALUATION_DEFAULT_USER_TYPE = "user";
    public static final String OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE = "api";
    public static final String OPENFGA_EVALUATION_DEFAULT_KEY = "type";
    public static final String OPENFGA_EVALUATION_DEFAULT_ID = "id";
    public static final String OPENFGA_EVALUATION_SUBJECT_TYPE_REALM = "realm";
    public static final String OPENFGA_EVALUATION_RESOURCE_TYPE_STORE = "store";

    // Tuple prefixes and relations for authorization model
    public static final String TUPLE_PREFIX_USER = "user:";
    public static final String TUPLE_PREFIX_OPERATOR = "operator:";
    public static final String TUPLE_PREFIX_PLANT = "plant:";
    public static final String TUPLE_PREFIX_GROUP = "group:";
    public static final String TUPLE_REL_MEMBER = "member";
    public static final String TUPLE_REL_ADMIN = "admin";
    public static final String TUPLE_PREFIX_ADMIN = "admin:";
    public static final String TUPLE_PREFIX_ROOT_OPERATOR_API = "api:root/operator";
    public static final String TUPLE_PREFIX_ROOT_PLANT_API = "api:root/plant";
    public static final String TUPLE_REL_RESOURCE_OPERATOR = "resource_operator";
    public static final String TUPLE_REL_PARENT_OPERATOR = "parent_operator";
    public static final String TUPLE_REL_RESOURCE_PLANT = "resource_plant";
    public static final String TUPLE_REL_RESOURCE_PLANT_OPERATOR = "resource_plant_operator";
    public static final String TUPLE_REL_RESOURCE_GROUP_GET = "resource_group_get";
    public static final String TUPLE_REL_RESOURCE_GROUP_STATUSUP = "resource_group_statusup";
    public static final String TUPLE_REL_PARENT = "parent";
    public static final String TUPLE_REL_USERS_GROUP_NAME = "users";
    public static final String TUPLE_REL_MANAGERS_GROUP_NAME = "managers";
    public static final String TUPLE_REL_SU_GROUP_NAME = "superusers";

    //Tuple write request relations
    public static final String TUPLE_WRITE = "writes";
    public static final String TUPLE_DELETE = "deletes";
    public static final String TUPLE_KEYS = "tuple_keys";

    // Authorization action constants
    public static final String AUTHZ_ACTION_CREATE = "can_create";
    public static final String AUTHZ_ACTION_UPDATE = "can_update";
    public static final String AUTHZ_ACTION_STATUSUP = "can_statusup";
    public static final String AUTHZ_ACTION_GET = "can_get";
    public static final String AUTHZ_ACTION_BOUND_TO = "bound_to";

    // Other Constants
    public static final String SOURCE_AUTH = "auth";
    public static final int PROXY_IP_POS = 2;

    // CORS All Enable
    public static final String CORS_ALL_ENABLED_PATH_PATTERN = "/**";
    public static final String CORS_ALL_ENABLED = "*";
    public static final Boolean CORS_ALL_ENABLED_CREDENTIALS = true;
    public static final Long CORS_ALL_ENABLED_MAX_AGE = 5L;
}
