/*
 * TokenClientRequest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for client authentication requests.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.annotations.Masked;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

/**
 * Data Transfer Object for client authentication requests.
 */
@Data
public class TokenClientRequest {

    /**
     * The clientId for authentication.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Size(min = Const.CLIENT_ID_LENGTH_MIN, max = Const.CLIENT_ID_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_CLIENT_ID)
    private String clientId;

    /**
     * The clientSecret for authentication.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Size(min = Const.CLIENT_SECRET_LENGTH_MIN, max = Const.CLIENT_SECRET_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_CLIENT_SECRET)
    @Masked
    private String clientSecret;
}
