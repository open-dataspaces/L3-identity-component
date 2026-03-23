/*
 * PostClientsResponse.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * Response DTO for creating a Keycloak client via /auth/clients.
 *
 * Date: 2025/11/30
 */

package io.github.open_dataspaces.core.domain.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.consts.Const;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for client creation.
 * Contains core client attributes required by API spec.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostClientsResponse {

    @JsonProperty(Const.JSON_PROPERTY_CLIENT_UUID)
    private String clientUuid;

    @JsonProperty(Const.JSON_PROPERTY_CLIENT_ID)
    private String clientId;

    @JsonProperty(Const.JSON_PROPERTY_CLIENT_ENABLED)
    private Boolean enabled;

    @JsonProperty(Const.JSON_PROPERTY_CLIENT_NAME)
    private String name;

    @JsonProperty(Const.JSON_PROPERTY_CLIENT_DESCRIPTION)
    private String description;

    @JsonProperty(Const.JSON_PROPERTY_FLOW_TYPE)
    private String flowType;

    @JsonProperty(Const.JSON_PROPERTY_OPEN_SYSTEM_ID)
    private String openSystemId;

    @JsonProperty(Const.JSON_PROPERTY_OPERATOR_ID)
    private String operatorId;

    @JsonProperty(Const.JSON_PROPERTY_REDIRECT_URIS)
    private List<String> redirectUris;

    public String getName() {
        return name == null ? "" : name;
    }

    public String getDescription() {
        return description == null ? "" : description;
    }
}