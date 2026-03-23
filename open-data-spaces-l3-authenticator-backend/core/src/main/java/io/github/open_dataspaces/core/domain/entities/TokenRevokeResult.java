/*
 * TokenRevokeResult.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This file defines the TokenRevokeResult class, which represents the result of a token revoke process.
 */

package io.github.open_dataspaces.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the result of a token revoke operation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenRevokeResult {
    /**
     * Indicates whether the token is currently active (valid).
     */
    private boolean active;
}
