/*
 * TupleDeleteRequest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * DTO for Tuple Delete Request to Authorization Service.
 *
 * Date: 2026/02/09
 */

package io.github.open_dataspaces.core.infrastructure.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.consts.Const;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for tuple delete request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TupleDeleteRequest {

    /**
     * DTO for deletes in tuple delete request.
     */
    @JsonProperty(Const.TUPLE_DELETE)
    private Deletes deletes;

    /**
     * DTO for tuple_keys in tuple delete request.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Deletes {
        @JsonProperty(Const.TUPLE_KEYS)
        private List<Tuple> tupleKeys;
    }
}
