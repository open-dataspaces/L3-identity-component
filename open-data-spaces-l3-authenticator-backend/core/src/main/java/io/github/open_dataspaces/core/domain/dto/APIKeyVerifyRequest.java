/*
 * APIKeyVerifyRequest.java
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
import io.github.open_dataspaces.core.common.consts.ConstError;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for requests containing API key verify parameters.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class APIKeyVerifyRequest {
    @NotNull(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Size(min = Const.VERIFY_API_KEY_LENGTH_MIN, max = Const.VERIFY_API_KEY_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_VERIFY_APIKEY)
    private String verifyAPIKey;
}
