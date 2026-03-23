/*
 * ODSProperties.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This configuration class defines properties for the ODS application.
 *
 * Date: 2025-10-30
 */

package io.github.open_dataspaces.core.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties for ODS application.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ods")
public class ODSProperties {

    // ods.application-env-name
    private String applicationEnvName;

    // ods.enable-ip-restriction
    private boolean enableIpRestriction = true;

    // ods.database-ssl
    private boolean databaseSsl = false;

    // ods.database-ssl-mode
    private String databaseSslMode;

    // ods.database-ssl-root-cert
    private String databaseSslRootCert;

    // ods.database-ssl-cert
    private String databaseSslCert;

    // ods.database-ssl-key
    private String databaseSslKey;

    // ods.default-effective-end-date
    private String defaultEffectiveEndDate;

    // ods.enable-uc-authorization
    private boolean enableUcAuthorization;

    // ods.operator-plant-authorization.enable
    private boolean enableOperatorPlantAuthorization;

    // ods.jwt
    private Jwt jwt = new Jwt();

    // ods.cors
    private Cors cors = new Cors();

    /**
     * JWT (Access Token) settings.
     */
    @Getter
    @Setter
    public static class Jwt {
        // ods.jwt.jwks-cache-duration-second
        private long jwksCacheDurationSecond;
        // ods.jwt.jwks-cache-size
        private long jwksCacheSize;
        // ods.jwt.claim.expected-audience
        private String claimExpectedAudience;
        // ods.jwt.claim.expected-type
        private String claimExpectedType;
    }

    /**
     * CORS settings.
     */
    @Getter
    @Setter
    public static class Cors {
        // ods.cors.enabled
        private boolean propertiesEnabled;
        // ods.cors.path-patterns
        private String pathPattern;
        // ods.cors.allowed-origins
        private String[] allowedOrigins;
        // ods.cors.allowed-methods
        private String[] allowedMethods;
        // ods.cors.allowed-headers
        private String[] allowedHeaders;
        // ods.cors.allow-credentials
        private boolean allowCredentials;
        // ods.cors.max-age
        private long maxAge;
    }

}
