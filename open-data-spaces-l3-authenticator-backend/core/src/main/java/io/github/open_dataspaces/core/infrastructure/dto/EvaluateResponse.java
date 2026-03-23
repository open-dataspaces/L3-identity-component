/*
 * EvaluateResponse.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * DTO for Evaluate Response to Authorization Service.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.infrastructure.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

/**
 * DTO for Evaluate Response to Authorization Service.
 */
@Data
public class EvaluateResponse {

    @NotNull
    private Boolean decision;

}
