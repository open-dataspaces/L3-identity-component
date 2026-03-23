/*
 * APIKeyVerifyResponse.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for API key verify parameters.
 *
 * Date: 2025/11/30
 */

package io.github.open_dataspaces.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.consts.Const;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for response containing API key verify parameters.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class APIKeyVerifyResponse {
    @JsonProperty(Const.JSON_PROPERTY_VERIFY_RESULT)
    private boolean verifyResult;
}
