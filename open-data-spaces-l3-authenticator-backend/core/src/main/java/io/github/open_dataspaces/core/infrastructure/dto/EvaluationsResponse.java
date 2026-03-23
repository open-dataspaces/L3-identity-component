/*
 * EvaluationaResponse.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * DTO for bulk evaluations response from OpenFGA.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.infrastructure.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

/**
 * DTO for Evaluations Response from Authorization Service.
 */
@Data
public class EvaluationsResponse {
    @NotNull
    @Valid
    private List<EvaluationDecision> evaluations = new ArrayList<>();

    /**
     * DTO for individual evaluation decision in Evaluations Response.
     */
    @Data
    public static class EvaluationDecision {
        @NotNull
        private Boolean decision;
    }
}
