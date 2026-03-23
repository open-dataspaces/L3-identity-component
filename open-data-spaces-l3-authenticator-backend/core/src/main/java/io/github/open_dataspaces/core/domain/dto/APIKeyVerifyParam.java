/*
 * APIKeyVerifyParam.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for API key verification parameters.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import java.util.List;

import io.github.open_dataspaces.core.common.enums.EnumAPIKeyAttributes;

import lombok.Data;

/**
 * Data Transfer Object for API key verification parameters.
 */
@Data
public class APIKeyVerifyParam {
    private final String apiKey;
    private final String ip;

    /**
     * Constructs a new APIKeyVerifyParam.
     *
     * @param apiKey the API key to verify
     * @param ip the client IP address (nullable)
     * @param attributes the list of required API key attributes
     */
    public APIKeyVerifyParam(String apiKey, String ip, List<EnumAPIKeyAttributes> attributes) {
        this.apiKey = apiKey;
        this.ip = ip;
    }

}
