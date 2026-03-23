/*
 * CreateStoreResponse.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * DTO for Create Store Response to Authorization Service.
 *
 * Date: 2026/02/13
 */

package io.github.open_dataspaces.core.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.micrometer.common.lang.NonNull;

import io.github.open_dataspaces.core.common.consts.Const;

/**
 * DTO for Create Store Response to Authorization Service.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateStoreResponse {

    /**
     * The store ID.
     */
    @NonNull
    @JsonProperty(Const.JSON_PROPERTY_STORE_ID)
    private String id;

    /**
     * The store name.
     */
    @NonNull
    @JsonProperty(Const.JSON_PROPERTY_STORE_NAME)
    private String name;

    /**
     * The creation timestamp.
     */
    @NonNull
    @JsonProperty(Const.JSON_PROPERTY_CREATED_AT)
    private String createdAt;

    /**
     * The update timestamp.
     */
    @NonNull
    @JsonProperty(Const.JSON_PROPERTY_UPDATED_AT)
    private String updatedAt;
}
