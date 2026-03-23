/*
 * IdpAccountInfo.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for IDP account parameters.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object for IDP account parameters.
 */
@Data
@AllArgsConstructor
public class IdpAccountInfo {

    /**
     * The Idp provided user ID (UUID).
     */
    private String userId;

    /**
     * The login user ID (Keycloak username).
     */
    private String loginUserId;

    /**
     * The email address.
     */
    private String email;

    /**
     * The password.
     */
    private String password;

    /**
     * The account enable status.
     */
    private boolean enabled;

    /**
     * Constructor for creating an IdpAccountInfo without a password.
     *
     * @param userId The Idp provided user ID (UUID).
     * @param loginUserId The login user ID (Keycloak username).
     * @param email The email address.
     * @param enabled The account enable status.
     */
    public IdpAccountInfo(String userId, String loginUserId, String email, boolean enabled) {
        this.userId = userId;
        this.loginUserId = loginUserId;
        this.email = email;
        this.password = null;
        this.enabled = enabled;
    }
}
