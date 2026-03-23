/*
 * PostClientSecretResponse.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for client secret information responses.
 *
 * Date: 2025/12/30
 */

package io.github.open_dataspaces.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.consts.Const;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for clients information responses.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostClientSecretResponse {

    @JsonProperty(Const.JSON_PROPERTY_CLIENT_SECRET)
    private String clientSecret;
}