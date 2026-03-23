/*
 * KeycloakProperties.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * KeycloakProperties is a configuration class for managing Keycloak properties.
 *
 * Date: 2025/06/30
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
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    // keycloak.realm
    private String realm;
    // keycloak.api-endpoint
    private String apiEndpoint;

    private Credentials credentials = new Credentials();

    /**
     * Credentials inner class to hold Keycloak credential properties.
     */
    @Getter
    @Setter
    public static class Credentials {
        // keycloak.credentials.admin-realm
        private String adminRealm;
        // keycloak.credentials.admin-username
        private String adminUserName;
        // keycloak.credentials.admin-password
        private String adminPassword;
        // keycloak.credentials.admin-client-id
        private String adminClientId;
    }

    private Authorization authorization = new Authorization();

    /**
     * Authorization inner class to hold Keycloak authorization properties.
     */
    @Getter
    @Setter
    public static class Authorization {
        // keycloak.authorization.url
        private String url;
        // keycloak.authorization.response-type
        private String responseType;
        // keycloak.authorization.scope
        private String scope;
        // keycloak.authorization.code-challenge-method
        private String codeChallengeMethod;
    }
}
