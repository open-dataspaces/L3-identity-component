/*
 * ConstPath.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class defines constant values for API request and mapping paths.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.common.consts;

/**
 * Defines constant values for API request and mapping paths used in the authentication and
 * authorization system.
 */
public class ConstPath {

    // Mapping path constants ODS
    // Request path constants
    public static final String AUTH_PATH = "/auth";

    // Sub-path constants under /auth
    public static final String AUTH_URL_PATH_SHORT = "/url";
    public static final String AUTH_TOKEN_PATH_SHORT = "/token";
    public static final String AUTH_TOKEN_PASSWORD_SHORT = "/token/password";
    public static final String AUTH_TOKEN_CLIENT_SHORT = "/token/client";
    public static final String AUTH_TOKEN_INTROSPECT_PATH_SHORT = "/token/introspect";
    public static final String AUTH_TOKEN_REFRESH_PATH_SHORT = "/token/refresh";
    public static final String AUTH_TOKEN_REVOKE_PATH_SHORT = "/token/revoke";
    public static final String AUTH_PASSWORD_SHORT = "/password/{operator_id}";
    public static final String AUTH_PASSWORD_URL_PATH_SHORT = "/password/url";
    public static final String AUTH_APIKEY_VERIFY_PATH_SHORT = "/apikey/verify";
    public static final String AUTH_CLIENTS_PATH_SHORT = "/clients";
    public static final String AUTH_CLIENTS_ID_PATH_SHORT = "/clients/{client_id}";
    public static final String AUTH_CLIENTS_SECRET_PATH_SHORT = "/clients/secret/{client_uuid}";

    // Mapping path constants Account
    // Request path constants
    public static final String ACCOUNT_PATH = "/account";

    // Sub-path constants for operator under /account
    public static final String ACCOUNT_OPERATOR_PATH_SHORT = "/operator";
    public static final String ACCOUNT_OPERATOR_PATH_WITH_ID_SHORT = "/operator/{operator_id}";
    public static final String ACCOUNT_OPERATOR_STATUS_PATH_WITH_ID_SHORT = "/operator/status/{operator_id}";
    public static final String ACCOUNT_OPERATOR_LIST_PATH_SHORT = "/operator/list";

    // Sub-path constants for plant under /account
    public static final String ACCOUNT_OPERATOR_PLANT_PATH_SHORT = "/plant";
    public static final String ACCOUNT_OPERATOR_PLANT_PATH_WITH_ID_SHORT = "/plant/{plant_id}";
    public static final String ACCOUNT_OPERATOR_PLANT_STATUS_PATH_WITH_ID_SHORT = "/plant/status/{plant_id}";
    public static final String ACCOUNT_OPERATOR_PLANT_LIST_PATH_SHORT = "/plant/list";

    // Sub-path constants for user under /account
    public static final String ACCOUNT_USER_PATH_SHORT = "/user";

    // Mapping path constants Authorization
    public static final String AUTHORIZATION_PATH = "/authz";

    // Sub-path constants under /authz
    public static final String AUTHORIZATION_STORES_PATH = "/stores";
    public static final String AUTHORIZATION_STORE = "/stores/{store_id}";
    public static final String AUTHORIZATION_AUTHORIZATION_MODELS_PATH = "/stores/{store_id}/authorization-models";
    public static final String AUTHORIZATION_TUPLES_READ = "/stores/{store_id}/read";
    public static final String AUTHORIZATION_TUPLES_WRITE = "/stores/{store_id}/write";
    public static final String AUTHORIZATION_EVALUATION = "/stores/{store_id}/access/v1/evaluation";
    public static final String AUTHORIZATION_EVALUATIONS = "/stores/{store_id}/access/v1/evaluations";

    // Path patterns
    public static final String ALL_PATH_PATTERN_INTERCEPTOR = "/**";
    public static final String ALL_PATH_PATTERN_FILTER = "/*";

    // Path patterns in write tuples
    public static final String TUPLE_OPERATOR_PATH_SHORT = "operator";
    public static final String TUPLE_OPERATOR_PATH_WITH_ID_SHORT = "operator/{operator_id}";
    public static final String TUPLE_OPERATOR_PLANT_PATH_SHORT = "{operator_id}/plant";
    public static final String TUPLE_OPERATOR_PLANT_PATH_WITH_ID_SHORT = "plant/{plant_id}";
}
