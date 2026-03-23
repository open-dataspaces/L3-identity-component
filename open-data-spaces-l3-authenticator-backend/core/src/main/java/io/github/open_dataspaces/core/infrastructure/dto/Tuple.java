/*
 * Tuple.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * DTO representing a tuple with user, relation, and object fields.
 *
 * Date: 2026/02/16
 */

package io.github.open_dataspaces.core.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO representing a tuple with user, relation, and object fields.
 */
@Data
@AllArgsConstructor
public class Tuple {
    private String user;
    private String relation;
    private String object;
}
