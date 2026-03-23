/*
 * TokenIntrospectionResult.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This file defines the TokenIntrospectionResult class, which represents the result of a token validation or
 * authentication process.
 */

package io.github.open_dataspaces.core.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.consts.Const;

import lombok.Data;

/**
 * Entity class representing the result of a token validation or authentication process.
 *
 * <p>Contains information about the operator, open system, and the active status of the token.</p>
 */
@Data
public class TokenIntrospectionResult {

    /**
     * Indicates whether the token is currently active (valid).
     */
    @JsonProperty(Const.JSON_PROPERTY_ACTIVE)
    private boolean active;

    /**
     * The username associated with the token, as specified by the JSON property defined in Const.JSON_PROPERTY_USERNAME.
     */
    @JsonProperty(Const.JSON_PROPERTY_USERNAME)
    private String userName;

    /**
     * The subject (user or entity) associated with the token.
     */
    @JsonProperty(Const.JSON_PROPERTY_SUB)
    private String sub;

    /**
     * The client ID associated with the token.
     */
    @JsonProperty(Const.JSON_PROPERTY_CLIENT_ID)
    private String clientId;

    /**
     * The scope(s) granted to the token.
     */
    @JsonProperty(Const.JSON_PROPERTY_SCOPE)
    private String scope;

    /**
     * The expiration time of the token (as a timestamp).
     */
    @JsonProperty(Const.JSON_PROPERTY_EXP)
    private long exp;

    /**
     * The type of the token, as specified by the JSON property defined in Const.JSON_PROPERTY_TYP.
     * This field is typically used to indicate the token's usage or context (e.g., "Bearer").
     */
    @JsonProperty(Const.JSON_PROPERTY_TYP)
    private String typ;

    /**
     * The operator ID associated with the token.
     */
    @JsonProperty(Const.JSON_PROPERTY_OPERATOR_ID)
    private String operatorId;

    /**
     * The open system ID associated with the token.
     */
    @JsonProperty(Const.JSON_PROPERTY_OPEN_SYSTEM_ID)
    private String openSystemId;

    /**
     * The issued-at time of the token (as a timestamp).
     */
    @JsonProperty(Const.JSON_PROPERTY_IAT)
    private long iat;

    /**
     * The issuer of the token.
     */
    @JsonProperty(Const.JSON_PROPERTY_ISS)
    private String iss;
}
