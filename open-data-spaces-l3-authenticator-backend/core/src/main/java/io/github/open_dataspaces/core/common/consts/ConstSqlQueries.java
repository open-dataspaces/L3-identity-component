/*
 * ConstSqlQueries.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class defines constant SQL and JPQL query strings.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.common.consts;

/**
 * Constant class used in authentication and authorization APIs.
 *
 * <p>Mainly defines JPQL and SQL query string constants for repository operations.<br>
 * Each constant is used in repository classes.</p>
 *
 * <ul>
 * <li><b>API key queries:</b> For API key validation and related lookups.</li>
 * <li><b>Operator queries:</b> For operator entity lookups and updates.</li>
 * <li><b>Plant queries:</b> For plant entity creation, lookup, update, and deletion.</li>
 * </ul>
 */
public class ConstSqlQueries {
    // SQL and JPQL query string constants
    public static final String SELECT_APIKEYS =
            "SELECT a"
            + " FROM APIKeysEntity a"
            + " WHERE a.apiKey = :apiKey"
            + " AND a.deletedFlag = false"
            + " AND a.effectiveStartDate <= :currentDate"
            + " AND a.effectiveEndDate >= :currentDate";
    public static final String SELECT_CIDRS =
            "SELECT *"
            + " FROM auth.tbl_cidrs c"
            + " WHERE c.api_key = :apiKey"
            + " AND inet(:ip) <<= inet(c.cidr)"
            + " AND c.deleted_flag = false"
            + " AND c.effective_start_date <= :currentDate"
            + " AND c.effective_end_date >= :currentDate";

    // Database entities package
    public static final String DATABASE_ENTITIES_PACKAGE = "io.github.open_dataspaces.core.domain.entities";

    // Wildcard constants
    public static final String WILDCARD_REQUEST_PARAMETER = "*";
    public static final String WILDCARD_SQL = "%";
    public static final Character ESCAPE_CHAR = '\\';

    // SQL Database schema and table names
    public static final String SCHEMA_AUTH = "auth";

    // SQL Table name constants
    public static final String TABLE_APIKEYS = "tbl_api_keys";
    public static final String TABLE_CIDRS = "tbl_cidrs";
    public static final String TABLE_OPERATORS = "tbl_operators";
    public static final String TABLE_PLANTS = "tbl_plants";
    public static final String TABLE_AUTHORIZATION_STORES = "tbl_authz_stores";
    public static final String TABLE_AUTHORIZATION_UC_STORES = "tbl_authz_uc_stores";
    public static final String TABLE_CLIENT_SECRETS = "tbl_client_secrets";
    public static final String COLUMN_CLIENT_SECRETS_CLIENT_UUID = "client_uuid";

    // SQL Column name constants
    public static final String COLUMN_COMMON_DELETED_FLAG = "deleted_flag";
    public static final String COLUMN_COMMON_EFFECTIVE_START_DATE = "effective_start_date";
    public static final String COLUMN_COMMON_EFFECTIVE_END_DATE = "effective_end_date";
    public static final String COLUMN_COMMON_CREATED_AT = "created_at";
    public static final String COLUMN_COMMON_CREATED_USER_ID = "created_user_id";
    public static final String COLUMN_COMMON_UPDATED_AT = "updated_at";
    public static final String COLUMN_COMMON_UPDATED_USER_ID = "updated_user_id";

    // APIKey Attributes
    public static final String COLUMN_APIKEYS_ID = "id";
    public static final String COLUMN_APIKEYS_APIKEY = "api_key";
    public static final String COLUMN_APIKEYS_APPLICATION_NAME = "application_name";
    public static final String COLUMN_APIKEYS_IDP_REALM = "idp_realm";
    public static final String COLUMN_APIKEYS_USECASE = "usecase";

    // Cidrs Attributes
    public static final String COLUMN_CIDRS_CIDR = "cidr";
    public static final String COLUMN_CIDRS_APIKEY = "api_key";
    // Authorization Stores Attributes
    public static final String COLUMN_AUTHORIZATION_STORES_ID = "pdp_store_id";
    public static final String COLUMN_AUTHORIZATION_STORES_NAME = "pdp_store_name";
    public static final String COLUMN_AUTHORIZATION_STORES_PURPOSE = "pdp_store_purpose";
    public static final String COLUMN_AUTHORIZATION_STORES_ENVIRONMENT_NAME = "environment_name";
    public static final String COLUMN_AUTHORIZATION_STORES_IDP_REALM = "idp_realm";
    public static final String COLUMN_AUTHORIZATION_UC_STORES_ID = "uc_store_id";
    public static final String COLUMN_AUTHORIZATION_STORES_UC_NAME = "uc_store_name";
    public static final String COLUMN_AUTHORIZATION_STORES_USECASE = "usecase";

    // Operators Attributes
    public static final String COLUMN_OPERATORS_OPERATOR_ID = "operator_id";
    public static final String COLUMN_OPERATORS_OPERATOR_NAME = "operator_name";
    public static final String COLUMN_OPERATORS_OPERATOR_ADDRESS = "operator_address";
    public static final String COLUMN_OPERATORS_OPEN_OPERATOR_ID = "open_operator_id";
    public static final String COLUMN_OPERATORS_GLOBAL_OPERATOR_ID = "global_operator_id";

    // Plants Attributes
    public static final String COLUMN_PLANTS_PLANT_ID = "plant_id";
    public static final String COLUMN_PLANTS_PLANT_NAME = "plant_name";
    public static final String COLUMN_PLANTS_OPERATOR_ID = "operator_id";
    public static final String COLUMN_PLANTS_PLANT_ADDRESS = "plant_address";
    public static final String COLUMN_PLANTS_OPEN_PLANT_ID = "open_plant_id";
    public static final String COLUMN_PLANTS_GLOBAL_PLANT_ID = "global_plant_id";
    public static final String COLUMN_PLANTS_DELETED_FLAG = "deleted_flag";
    public static final String COLUMN_PLANTS_EFFECTIVE_START_DATE = "effective_start_date";
    public static final String COLUMN_PLANTS_EFFECTIVE_END_DATE = "effective_end_date";
}
