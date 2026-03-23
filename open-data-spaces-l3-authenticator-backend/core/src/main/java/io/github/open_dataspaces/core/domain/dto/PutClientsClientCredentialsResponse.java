/*
 * PutClientsClientCredentialsResponse.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * DTO for client update responses.
 *
 * Date: 2026/02/12
 */

package io.github.open_dataspaces.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.consts.Const;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for updating a Keycloak client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PutClientsClientCredentialsResponse {

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

}
