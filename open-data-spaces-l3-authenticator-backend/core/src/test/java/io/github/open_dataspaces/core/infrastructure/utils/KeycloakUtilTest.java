/*
 * KeycloakUtilTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the KeycloakUtil class.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.infrastructure.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.infrastructure.config.KeycloakProperties;

/**
 * Unit tests for the {@link KeycloakUtil} class.
 */
@ExtendWith(MockitoExtension.class)
class KeycloakUtilTest {

    private KeycloakUtil keycloakUtil;

    @Mock
    private KeycloakProperties keycloakProperties;

    @BeforeEach
    void setUp() {
        keycloakUtil = new KeycloakUtil(keycloakProperties);
    }

    /**
     * (Case#7) Tests the getKeycloakAuthServerUrl method.
     */
    @Test
    void testGetTokenStoreUrl() {
        when(keycloakProperties.getApiEndpoint()).thenReturn("http://localhost:8080/auth");
        String tokenStoreUrl = keycloakUtil.getTokenStoreUrl("test-realm");
        assertEquals("http://localhost:8080/auth/realms/test-realm/protocol/openid-connect/token", tokenStoreUrl, "Token store URL should match the expected value");
    }

    /**
     * (Case#8) Tests the getTokenIntrospectUrl method.
     */
    @Test
    void testGetTokenIntrospectUrl() {
        when(keycloakProperties.getApiEndpoint()).thenReturn("http://localhost:8080/auth");
        String tokenIntrospectUrl = keycloakUtil.getTokenIntrospectUrl("test-realm");
        assertEquals("http://localhost:8080/auth/realms/test-realm/protocol/openid-connect/token/introspect", tokenIntrospectUrl, "Token introspect URL should match the expected value");
    }

    /**
     * Parameterized test for getPasswordChangeEndpointUrl method.
     */
    @ParameterizedTest
    @CsvSource({
        "realm1, client1, http://redirect1.com, http://global-url.com, response-type, scope, abc123#, http://global-url.com/realms/realm1/protocol/openid-connect/auth?client_id=client1&response_type=response-type&scope=scope&redirect_uri=http://redirect1.com&kc_action=UPDATE_PASSWORD&prompt=login&code_challenge=abc123#&code_challenge_method=S256",
        "realm2, client2, https://redirect2.com, https://global-url.com, code, openid, abc123#, https://global-url.com/realms/realm2/protocol/openid-connect/auth?client_id=client2&response_type=code&scope=openid&redirect_uri=https://redirect2.com&kc_action=UPDATE_PASSWORD&prompt=login&code_challenge=abc123#&code_challenge_method=S256"
    })
    @DisplayName("getPasswordChangeEndpointUrl constructs the correct URL")
    void testGetPasswordChangeEndpointUrl(String idpRealm, String clientId, String redirectUri, String authUrl, String responseType, String scope, String codeChallenge, String expectedUrl) {
        // Arrange
        when(keycloakProperties.getAuthorization()).thenReturn(mock(KeycloakProperties.Authorization.class));
        when(keycloakProperties.getAuthorization().getUrl()).thenReturn(authUrl);
        when(keycloakProperties.getAuthorization().getResponseType()).thenReturn(responseType);
        when(keycloakProperties.getAuthorization().getScope()).thenReturn(scope);
        when(keycloakProperties.getAuthorization().getCodeChallengeMethod()).thenReturn("S256");

        // Act
        String result = keycloakUtil.getPasswordChangeEndpointUrl(idpRealm, clientId, redirectUri, codeChallenge);

        // Assert
        assertEquals(expectedUrl, result, "The constructed URL should match the expected URL.");
    }

    /**
     * (Case#9) Tests the getAuthorizationEndpointUrl method.
     */
    @Test
    void testGetAuthorizationEndpointUrl() {
        when(keycloakProperties.getAuthorization()).thenReturn(mock(KeycloakProperties.Authorization.class));
        when(keycloakProperties.getAuthorization().getUrl()).thenReturn("http://localhost:8080/auth");
        when(keycloakProperties.getAuthorization().getResponseType()).thenReturn("code");
        when(keycloakProperties.getAuthorization().getScope()).thenReturn("openid profile");
        when(keycloakProperties.getAuthorization().getCodeChallengeMethod()).thenReturn("S256");

        String authUrl = keycloakUtil.getAuthorizationEndpointUrl("test-realm", "test-client", "http://redirect.uri", "challenge123");
        String expectedUrl = "http://localhost:8080/auth/realms/test-realm/protocol/openid-connect/auth?client_id=test-client&response_type=code&scope=openid profile&redirect_uri=http://redirect.uri&code_challenge=challenge123&code_challenge_method=S256";
        assertEquals(expectedUrl, authUrl, "Authorization endpoint URL should match the expected value");
    }

    /**
     * Tests the getIssuer method.
     */
    @Test
    void testGetIssuer() {
        String authorizationUrl = "http://keycloak.example.com";
        String idpRealm = "test-realm";
        String expectedIssuer = String.format(Const.URL_TEMPLATE_KEYCLOAK_ISSUER, authorizationUrl, idpRealm);

        when(keycloakProperties.getAuthorization()).thenReturn(mock(KeycloakProperties.Authorization.class));
        when(keycloakProperties.getAuthorization().getUrl()).thenReturn(authorizationUrl);

        String issuer = keycloakUtil.getIssuer(idpRealm);

        assertEquals(expectedIssuer, issuer, "Issuer URL should match the expected value");
    }

    /**
     * Tests the getJwksUri method.
     */
    @Test
    void testGetJwksUri() {
        String issuer = "http://keycloak.example.com/realms/test-realm";
        String expectedJwksUri = String.format(Const.URL_TEMPLATE_KEYCLOAK_JWKS_URI, issuer);

        String jwksUri = keycloakUtil.getJwksUri(issuer);

        assertEquals(expectedJwksUri, jwksUri, "JWKS URI should match the expected value");
    }

    /**
     * Tests the getTokenRevoke method.
     */
    @Test
    void testGetTokenRevoke() {
        String idpRealm = "test-realm";
        when(keycloakProperties.getApiEndpoint()).thenReturn("http://localhost:8080");

        String tokenRevokeUrl = keycloakUtil.getTokenRevoke(idpRealm);

        assertEquals("http://localhost:8080/realms/test-realm/protocol/openid-connect/revoke", tokenRevokeUrl, "Token revoke URL should match the expected value");
    }
}