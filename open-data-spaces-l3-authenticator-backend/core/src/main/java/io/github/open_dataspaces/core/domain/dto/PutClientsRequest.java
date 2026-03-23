/*
 * PutClientsRequest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * DTO for client update requests.
 *
 * Date: 2026/02/12
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
 * Request DTO for updating a Keycloak client.
 *
 * <p>This API uses a path parameter (/clients/{client_id}).</p>
 *
 * <p>Request body fields are optional; when omitted they are not updated.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PutClientsRequest {
    @JsonProperty(Const.JSON_PROPERTY_FLOW_TYPE)
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Pattern(regexp = Const.REGEX_FLOW_TYPE, message = ConstError.ERR_VALIDATION_ENUM_DEFAULT)
    private String flowType;

    @JsonProperty(Const.JSON_PROPERTY_CLIENT_NAME)
    @Size(max = Const.CLIENT_NAME_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    private String name;

    @JsonProperty(Const.JSON_PROPERTY_CLIENT_DESCRIPTION)
    @Size(max = Const.CLIENT_DESCRIPTION_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    private String description;

    @Pattern(regexp = Const.REGEX_UUID, message = ConstError.ERR_VALIDATION_COMMON_PATTERN_UUID)
    @JsonProperty(Const.JSON_PROPERTY_OPERATOR_ID)
    private String operatorId;

    @JsonProperty(Const.JSON_PROPERTY_OPEN_SYSTEM_ID)
    @Size(min = Const.CLIENT_OPEN_SYSTEM_ID_LENGTH_MIN, max = Const.CLIENT_OPEN_SYSTEM_ID_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    private String openSystemId;

    @JsonProperty(Const.JSON_PROPERTY_REDIRECT_URIS)
    private List<
            @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
            @Size(min = Const.REDIRECT_URI_LENGTH_MIN, max = Const.REDIRECT_URI_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
            @Pattern(regexp = Const.REGEX_REDIRECT_URI, message = ConstError.ERR_VALIDATION_COMMON_PATTERN)
            String
            > redirectUris;
}
