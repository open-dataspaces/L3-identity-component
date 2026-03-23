/*
 * OpenFgaProperties.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * OpenFgaProperties is a configuration class for managing OpenFGA properties.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * KeycloakProperties is a configuration class for managing Keycloak properties.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "openfga")
public class OpenFgaProperties {

    // openfga.api-endpoint
    private String apiEndpoint;

    // openfga.preshared-key
    private String presharedKey;
}
