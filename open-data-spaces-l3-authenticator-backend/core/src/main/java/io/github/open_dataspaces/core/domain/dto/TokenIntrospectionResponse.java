/*
 * TokenIntrospectionResponse.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for token introspection responses.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.consts.Const;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object for token introspection responses.
 */
@Data
public class TokenIntrospectionResponse {

    /**
     * Indicates whether the token is active.
     */
    @JsonProperty(Const.JSON_PROPERTY_ACTIVE)
    private boolean active;

    /**
     * Contains information about the token.
     */
    @JsonIgnore
    private TokenInfo tokenInfo;

    /**
     * Returns the token information for JSON serialization.
     * If the token is not active, returns an empty map.
     *
     * @return the token information or an empty map if inactive
     */
    @JsonProperty(Const.JSON_PROPERTY_TOKEN_INFO)
    public Object getTokenInfoForJson() {
        if (!this.active) {
            return new HashMap<>();
        }
        return this.tokenInfo;
    }

    /**
     * Default constructor initializing the response with inactive status.
     */
    public TokenIntrospectionResponse() {
        this.active = false;
        this.tokenInfo = null;
    }

    /**
     * Constructor for TokenIntrospectionResponse.
     *
     * @param exp the expiration time of the token
     * @param iat the issued at time of the token
     * @param operatorId the operator ID associated with the token
     * @param openSystemId the open system ID associated with the token
     * @param scope the scope of the token
     * @param clientId the client ID of the token
     * @param tokenType the token type of the token
     */
    public TokenIntrospectionResponse(
            long exp, long iat, String operatorId, String openSystemId,
            String scope, String clientId, String tokenType) {
        this.active = true;
        this.tokenInfo = new TokenInfo(exp, iat, operatorId, openSystemId, scope, clientId, tokenType);
    }

    /**
     * Inner class representing the token information.
     */
    @Data
    @AllArgsConstructor
    public class TokenInfo {
        /**
         * The expiration time of the token.
         */
        @JsonProperty(Const.JSON_PROPERTY_EXP)
        private Long exp;

        /**
         * The issued at time of the token.
         */
        @JsonProperty(Const.JSON_PROPERTY_IAT)
        private Long iat;

        /**
         * The operator ID associated with the token.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonProperty(Const.JSON_PROPERTY_OPERATOR_ID)
        private String operatorId;

        /**
         * The open system ID associated with the token.
         * Expected to be obtained only when authenticated with a specific scope.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonProperty(Const.JSON_PROPERTY_OPEN_SYSTEM_ID)
        private String openSystemId;

        /**
         * The scope of the token.
         */
        @JsonProperty(Const.JSON_PROPERTY_SCOPE)
        private String scope;

        /**
         * The client ID of the token.
         */
        @JsonProperty(Const.JSON_PROPERTY_CLIENT_ID)
        private String clientId;

        /**
         * The token type of the token.
         */
        @JsonProperty(Const.JSON_PROPERTY_TOKEN_TYPE)
        private String tokenType;
    }
}
