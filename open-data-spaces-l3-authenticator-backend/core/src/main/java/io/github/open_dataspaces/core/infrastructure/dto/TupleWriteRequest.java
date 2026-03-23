/*
 * TupleWriteRequest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * DTO for Tuple Write Request to Authorization Service.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.infrastructure.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.consts.Const;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO for tuple write request.
 */
@Data
@AllArgsConstructor
public class TupleWriteRequest {

    /**
     * DTO for writes in tuple write request.
     */
    @JsonProperty(Const.TUPLE_WRITE)
    private Writes writes;

    /**
     * DTO for tuple_keys in tuple write request.
     */
    @Data
    @AllArgsConstructor
    public static class Writes {
        @JsonProperty(Const.TUPLE_KEYS)
        private List<Tuple> tupleKeys;
    }
}
