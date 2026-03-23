/*
 * KeycloakUtil.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class provides utility methods and configuration access for Keycloak integration.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.infrastructure.utils;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.infrastructure.config.KeycloakProperties;

import lombok.Data;

/**
 * Utility class for Keycloak integration.
 *
 * <p>This class provides methods to build Keycloak client instances and retrieve Keycloak-related
 * configuration.</p>
 */
@Component
@Data
public class KeycloakUtil {

    private final KeycloakProperties keycloakProperties;

    /**
     * Gets the full URL for the Keycloak token endpoint with the specified realm.
     *
     * @return the token endpoint URL
     */
    public String getTokenStoreUrl(@NonNull String idpRealm) {
        return String.format(
                Const.URL_TEMPLATE_KEYCLOAK_ACCESS_TOKEN,  // Template for Keycloak access token URL
                // arguments for the URL template
                keycloakProperties.getApiEndpoint(),     // Placeholder for Keycloak server URL
                idpRealm
        );
    }

    /**
     * Gets the full URL for the Keycloak token introspection endpoint.
     *
     * @return the token introspection endpoint URL
     */
    public String getTokenIntrospectUrl(@NonNull String idpRealm) {
        return String.format(
                Const.URL_TEMPLATE_KEYCLOAK_TOKEN_INTROSPECTION,  // Template for Keycloak token introspection URL
                // arguments for the URL template
                keycloakProperties.getApiEndpoint(),     // Placeholder for Keycloak server URL
                idpRealm
        );
    }

    /**
     * Constructs the Keycloak authorization endpoint URL.
     *
     * @param idpRealm the Keycloak realm
     * @param clientId the client ID
     * @param redirectUri the redirect URI
     * @param codeChallenge the code challenge
     * @return the authorization endpoint URL
     */
    public String getAuthorizationEndpointUrl(@NonNull String idpRealm, @NonNull String clientId, @NonNull String redirectUri, @NonNull String codeChallenge) {
        // Generate the Keycloak URL based on the arguments, constants, and realm
        return String.format(
                Const.URL_TEMPLATE_KEYCLOAK_AUTHORIZATION,  // Template for Keycloak authorization URL
                // arguments for the URL template
                keycloakProperties.getAuthorization().getUrl(), // Placeholder for Keycloak server URL
                idpRealm,
                clientId,
                keycloakProperties.getAuthorization().getResponseType(),
                keycloakProperties.getAuthorization().getScope(),
                redirectUri,
                codeChallenge,
                keycloakProperties.getAuthorization().getCodeChallengeMethod());
    }

    /**
     * Constructs the Keycloak password change endpoint URL.
     *
     * @param idpRealm the Keycloak realm
     * @param clientId the client ID
     * @param redirectUri the redirect URI
     * @param codeChallenge the code challenge
     * @return the password change endpoint URL
     */
    public String getPasswordChangeEndpointUrl(@NonNull String idpRealm, @NonNull String clientId, @NonNull String redirectUri, @NonNull String codeChallenge) {
        // Construct the Keycloak password change URL using the provided parameters
        return String.format(
                Const.URL_TEMPLATE_KEYCLOAK_PASSWORD_CHANGE,  // Template for Keycloak password change URL
                // arguments for the URL template
                keycloakProperties.getAuthorization().getUrl(), // Placeholder for Keycloak server URL
                idpRealm,
                clientId,
                keycloakProperties.getAuthorization().getResponseType(),
                keycloakProperties.getAuthorization().getScope(),
                redirectUri,
                codeChallenge,
                keycloakProperties.getAuthorization().getCodeChallengeMethod());
    }

    /**
     * Generates a random password that meets complexity requirements.
     *
     * @return the generated password
     */
    public String generateRandomPassword() {
        int length = Const.DEFAULT_CREATE_PASSWORD_LENGTH;
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specials = Const.PASSWORD_SPECIAL_CHARS;
        String all = upper + lower + digits + specials;
        StringBuilder password = new StringBuilder(length);
        java.security.SecureRandom random = new java.security.SecureRandom();

        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(specials.charAt(random.nextInt(specials.length())));

        for (int i = 4; i < length; i++) {
            password.append(all.charAt(random.nextInt(all.length())));
        }

        return password.toString();
    }

    /**
     * Gets the issuer URL for the specified realm.
     *
     * @param idpRealm the Keycloak realm
     * @return the issuer URL
     */
    public @NonNull String getIssuer(@NonNull String idpRealm) {
        return String.format(Const.URL_TEMPLATE_KEYCLOAK_ISSUER, keycloakProperties.getAuthorization().getUrl(), idpRealm);
    }

    /**
     * Gets the JWKS URI for the specified realm.
     *
     * @param issuer the Keycloak issuer URL
     * @return the JWKS URI
     */
    public @NonNull String getJwksUri(@NonNull String issuer) {
        return String.format(Const.URL_TEMPLATE_KEYCLOAK_JWKS_URI, issuer);
    }

    /**
     * Gets the full URL for the Keycloak token revoke endpoint.
     *
     * @param idpRealm the Keycloak realm
     * @return the token revoke endpoint URL
     */
    public String getTokenRevoke(@NonNull String idpRealm) {
        return String.format(
            Const.URL_TEMPLATE_KEYCLOAK_REVOKE,  // Template for Keycloak token introspection URL
            // arguments for the URL template
            keycloakProperties.getApiEndpoint(),     // Placeholder for Keycloak server URL
            idpRealm
        );
    }
}
