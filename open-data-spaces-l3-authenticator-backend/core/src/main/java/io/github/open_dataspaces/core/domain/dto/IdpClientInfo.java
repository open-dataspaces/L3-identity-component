/*
 * IdpClientInfo.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for IDP client information.
 *
 * Date: 2025/11/30
 */

package io.github.open_dataspaces.core.domain.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for IDP client information.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdpClientInfo {

    /**
     * UUID issued by Keycloak (client id).
     */
    private String uuid;

    /**
     * Client ID (public identifier).
     */
    private String clientId;

    /**
     * Display name.
     */
    private String name;

    /**
     * Description (optional).
     */
    private String description;

    /**
     * Registered redirect URIs (authorization_code flow only).
     */
    private List<String> redirectUris;

    /**
     * Client secret (only when explicitly retrieved).
     */
    private String clientSecret;

    /**
     * Public client flag.
     */
    private boolean publicClient;

    /**
     * Enabled flag.
     */
    private boolean enabled;
}