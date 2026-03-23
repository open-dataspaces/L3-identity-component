/*
 * AuthUrlResponse.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * Thi class is a DTO for user login responses.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.consts.Const;

import io.micrometer.common.lang.NonNull;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object for user login responses.
 */
@Data
@AllArgsConstructor
public class AuthUrlResponse {

    /**
     * The URL for the authorization request.
     */
    @NonNull
    @JsonProperty(Const.JSON_PROPERTY_URL)
    private String url;

}
