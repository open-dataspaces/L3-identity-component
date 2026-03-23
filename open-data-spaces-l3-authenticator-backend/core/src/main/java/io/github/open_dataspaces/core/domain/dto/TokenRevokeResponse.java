/*
 * TokenRevokeResponse.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for token revoke responses.
 *
 * Date: 2026/02/10
 */

package io.github.open_dataspaces.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.consts.Const;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the response of a token revoke operation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenRevokeResponse {
    /**
     * Indicates whether the token is currently active (valid).
     */
    @JsonProperty(Const.JSON_PROPERTY_ACTIVE)
    private boolean active;
}
