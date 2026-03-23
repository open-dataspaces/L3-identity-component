/*
 * PostClientsRequest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * DTO for client creation requests with flow-dependent validation.
 *
 * Date: 2025/11/30
 */

package io.github.open_dataspaces.core.domain.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a Keycloak client.
 * Flow-type dependent rules:
 * - authorization_code: redirect_uris required; operator_id / open_system_id optional.
 * - client_credentials: operator_id (UUID) and open_system_id required; redirect_uris ignored.
 * Additional element-level checks for redirect_uris are performed in the controller.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostClientsRequest {

    @JsonProperty(Const.JSON_PROPERTY_FLOW_TYPE)
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Pattern(regexp = Const.REGEX_FLOW_TYPE, message = ConstError.ERR_VALIDATION_ENUM_DEFAULT)
    private String flowType;

    @JsonProperty(Const.JSON_PROPERTY_CLIENT_ID)
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Size(min = Const.CLIENT_ID_LENGTH_MIN, max = Const.CLIENT_ID_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @Pattern(regexp = Const.REGEX_CLIENT_ID, message = ConstError.ERR_VALIDATION_COMMON_PATTERN)
    private String clientId;

    @JsonProperty(Const.JSON_PROPERTY_CLIENT_NAME)
    @Size(max = Const.CLIENT_NAME_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    private String name;

    @JsonProperty(Const.JSON_PROPERTY_CLIENT_DESCRIPTION)
    @Size(max = Const.CLIENT_DESCRIPTION_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    private String description;

    @Pattern(regexp = Const.REGEX_UUID, message = ConstError.ERR_VALIDATION_COMMON_PATTERN_UUID)
    @JsonProperty(Const.JSON_PROPERTY_OPERATOR_ID)
    private String operatorId; // Required (UUID) when flow_type=client_credentials (validated additionally)

    @JsonProperty(Const.JSON_PROPERTY_OPEN_SYSTEM_ID)
    @Size(max = Const.CLIENT_OPEN_SYSTEM_ID_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    private String openSystemId; // Required when flow_type=client_credentials (validated additionally)

    @JsonProperty(Const.JSON_PROPERTY_REDIRECT_URIS)
    private List<
            @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
            @Size(min = Const.REDIRECT_URI_LENGTH_MIN, max = Const.REDIRECT_URI_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
            @Pattern(regexp = Const.REGEX_REDIRECT_URI, message = ConstError.ERR_VALIDATION_COMMON_PATTERN)
            String
            > redirectUris;  // Required (non-empty) when flow_type=authorization_code (additional validation)
}