/*
 * KeycloakServiceImpl.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This service provides the implementation for user login logic.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.infrastructure.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.dto.IdpAccountInfo;
import io.github.open_dataspaces.core.domain.dto.IdpClientInfo;
import io.github.open_dataspaces.core.domain.dto.StateString;
import io.github.open_dataspaces.core.domain.entities.AccessTokenResult;
import io.github.open_dataspaces.core.domain.entities.TokenIntrospectionResult;
import io.github.open_dataspaces.core.domain.entities.TokenRevokeResult;
import io.github.open_dataspaces.core.domain.repository.interfaces.KeycloakRepository;
import io.github.open_dataspaces.core.domain.service.interfaces.APIKeyService;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.infrastructure.utils.KeycloakUtil;

/**
 * Service implementation for user login operations.
 *
 * <p>Provides logic to authenticate a user using operatorAccountId and password via Keycloak.</p>
 */
@Service("keycloakService")
public class KeycloakServiceImpl implements IdentityProviderService {

    private final KeycloakRepository keycloakRepository;

    private final APIKeyService apiKeyService;

    @Autowired
    private KeycloakUtil keycloakUtil;

    /**
     * Constructs a new AuthUrlServiceImpl with the specified API keys repository.
     *
     * @param keycloakRepository the Keycloak repository
     * @param apiKeyService the API key service
     * @param keycloakUtil the Keycloak utility
     */
    public KeycloakServiceImpl(KeycloakRepository keycloakRepository, KeycloakUtil keycloakUtil, APIKeyService apiKeyService) {
        this.keycloakRepository = keycloakRepository;
        this.keycloakUtil = keycloakUtil;
        this.apiKeyService = apiKeyService;
    }

    /**
     * Authenticates a user using the provided operatorAccountId and password.
     *
     * @param apiKey the API key for authentication
     * @param clientId the client ID for authentication
     * @param redirectUri the redirect URI for authentication
     * @param codeChallenge the code challenge for PKCE
     * @return the login response containing access and refresh tokens
     *
     *         Assumes that the operatorAccountId and password are validated before this method is
     *         called.
     */
    @Override
    public @NonNull String buildAuthorizationUrl(@NonNull String apiKey, @NonNull String clientId,
            @NonNull String redirectUri, @NonNull String codeChallenge) {
        // Get realm from APIKey
        String idpRealm = apiKeyService.getIdpRealm(apiKey);

        // Check if Client ID exists
        if (!keycloakRepository.isValidClientId(idpRealm, clientId)) {
            throw new IllegalArgumentException(String.format(ConstError.ERRLOG_400_INVALID_REQUEST_PARAMETER, Const.JSON_PROPERTY_CLIENT_ID, clientId));
        }

        // Generate Keycloak URL based on arguments, constants, and realm
        return keycloakUtil.getAuthorizationEndpointUrl(idpRealm, clientId, redirectUri, codeChallenge);
    }

    /**
     * Retrieves an access token from Keycloak.
     *
     * @param apiKey the API key
     * @param code the authorization code
     * @param clientId the client ID
     * @param clientSecret the client secret
     * @param redirectUri the redirect URI
     * @param codeVerifier the code verifier
     * @return the access token result
     */
    @Override
    @NonNull
    public AccessTokenResult getAccessToken(@NonNull String apiKey, @NonNull String code, @NonNull String clientId,
            @NonNull String clientSecret, @NonNull String redirectUri, @NonNull String codeVerifier) {
        // Get realm from APIKey
        String idpRealm = apiKeyService.getIdpRealm(apiKey);

        // Call Keycloak repository to get access token
        AccessTokenResult response = keycloakRepository.getAccessToken(idpRealm, code, clientId, clientSecret, redirectUri, codeVerifier);

        return response;
    }

    /**
     * Authenticates a user using the provided API key, operator ID, and password.
     *
     * @param apiKey the API key for authentication
     * @param clientId the client ID for authentication
     * @param clientSecret the client secret for authentication
     * @param userId the login user ID
     * @param password the login user's password
     */
    @Override
    @NonNull
    public AccessTokenResult signInWithPassword(@NonNull String apiKey, @NonNull String clientId, @NonNull String clientSecret, @NonNull String userId, @NonNull String password) {
        // Get realm from APIKey
        String idpRealm = apiKeyService.getIdpRealm(apiKey);

        // Call Keycloak repository to get access token
        return keycloakRepository.signInWithPassword(idpRealm, clientId, clientSecret, userId, password);
    }

    /**
     * Authenticates a client using the provided API key, client ID, and client secret.
     *
     * @param apiKey the API key for authentication
     * @param clientId the client ID for authentication
     * @param clientSecret the client secret for authentication
     * @return AccessTokenResult containing the access token and other details
     */
    @Override
    @NonNull
    public AccessTokenResult signInWithClient(@NonNull String apiKey, @NonNull String clientId, @NonNull String clientSecret) {
        // Get realm from APIKey
        String idpRealm = apiKeyService.getIdpRealm(apiKey);

        // Call Keycloak repository to get access token
        return keycloakRepository.signInWithClient(idpRealm, clientId, clientSecret);
    }

    /**
     * Introspects a token to check its validity.
     *
     * @param apiKey the API key
     * @param accessToken the access token to introspect
     * @param clientId the client ID
     * @param clientSecret the client secret
     * @return the introspection result
     */
    @Override
    @NonNull
    public TokenIntrospectionResult tokenIntrospection(@NonNull String apiKey, @NonNull String accessToken,
            @NonNull String clientId, @NonNull String clientSecret) {
        // Get realm from APIKey
        String idpRealm = apiKeyService.getIdpRealm(apiKey);

        // Call Keycloak repository to introspect token
        TokenIntrospectionResult response = keycloakRepository.tokenIntrospection(idpRealm, clientId, clientSecret, accessToken, Const.KEYCLOAK_SEND_VALUE_TOKEN_TYPE_HINT_ACCESS_TOKEN);

        return response;
    }

    /**
     * Introspects a token to check its validity.
     *
     * @param apiKey the API key
     * @param refreshToken the refresh token to use
     * @param clientId the client ID
     * @param clientSecret the client secret
     * @return the introspection result
     */
    @Override
    @NonNull
    public AccessTokenResult tokenRefresh(@NonNull String apiKey, @NonNull String refreshToken,
            @NonNull String clientId, @NonNull String clientSecret) {
        // Get realm from APIKey
        String idpRealm = apiKeyService.getIdpRealm(apiKey);

        // Call Keycloak repository to introspect token
        AccessTokenResult response = keycloakRepository.tokenRefresh(idpRealm, clientId, clientSecret, refreshToken);

        return response;
    }

    /**
     * Changes the password for the specified user.
     *
     * @param apiKey the API key for authentication
     * @param operatorId the user ID (operatorId) for which the password is being changed
     * @param oldPassword the old password to verify
     * @param newPassword the new password to set
     * @return true if the password was changed successfully
     */
    @Override
    public boolean changePassword(@NonNull String apiKey, @NonNull String operatorId, @NonNull String oldPassword, @NonNull String newPassword) {
        // Get realm from APIKey
        String idpRealm = apiKeyService.getIdpRealm(apiKey);

        // Check if oldPassword is valid
        if (!keycloakRepository.isValidPassword(idpRealm, operatorId, oldPassword)) {
            throw new IllegalArgumentException(String.format(ConstError.ERRLOG_400_INVALID_REQUEST_PARAMETER,
                    Const.JSON_PROPERTY_OLD_PASSWORD, oldPassword));
        }

        // Change password using Keycloak repository
        keycloakRepository.changePassword(idpRealm, operatorId, newPassword);
        return true;
    }

    /**
     * Builds the password change URL for the user.
     *
     * @param apiKey the API key for authentication
     * @param clientId the client ID for authentication
     * @param redirectUri the redirect URI for the password change
     * @param codeChallenge the code challenge for PKCE
     * @return the password change URL
     */
    @Override
    @NonNull
    public String buildPasswordChangeUrl(@NonNull String apiKey, @NonNull String clientId, @NonNull String redirectUri, @NonNull String codeChallenge) {
        // Get realm from APIKey
        String idpRealm = apiKeyService.getIdpRealm(apiKey);

        // Check if Client ID exists
        if (!keycloakRepository.isValidClientId(idpRealm, clientId)) {
            throw new IllegalArgumentException(String.format(ConstError.ERRLOG_400_INVALID_REQUEST_PARAMETER, Const.JSON_PROPERTY_CLIENT_ID, clientId));
        }

        // Generate Keycloak URL based on arguments, constants, and realm
        return keycloakUtil.getPasswordChangeEndpointUrl(idpRealm, clientId, redirectUri, codeChallenge);
    }

    /**
     * Creates a new user account with the specified login_user_id and email.
     *
     * @param apiKey the API key for authentication
     * @param loginUserId the login_user_id for the new account
     * @param email the email address for the new account
     * @param createPasswordFlag indicates whether to create a password for the account
     * @param passwordTemporaryFlag indicates whether the password is temporary
     * @return the result containing userId and password
     */
    @Override
    @NonNull
    public IdpAccountInfo createAccount(@NonNull String apiKey, @NonNull String loginUserId, @NonNull String email,
            boolean createPasswordFlag, boolean passwordTemporaryFlag) {
        // Get realm from APIKey
        String idpRealm = apiKeyService.getIdpRealm(apiKey);

        // Generate a password if createPasswordFlag is true
        String password;
        if (createPasswordFlag) {
            password = keycloakUtil.generateRandomPassword(); // Utility to generate a random password
        } else {
            password = null;
            passwordTemporaryFlag = false; // Ensure passwordTemporaryFlag is false if no password is created
        }

        // Create user in Keycloak
        String userId = keycloakRepository.createUser(idpRealm, loginUserId, email, password, passwordTemporaryFlag);

        // Return the result
        return new IdpAccountInfo(userId, loginUserId, email, password, true);
    }

    /**
     * Updates the username and email address for the specified user in Keycloak.
     *
     * @param apiKey the API key for authentication
     * @param userId the access token of the user
     * @param loginUserId the new login user ID to set
     * @param email the new email address to set
     */
    @Override
    @NonNull
    public IdpAccountInfo updateAccount(@NonNull String apiKey, @NonNull String userId, StateString loginUserId, StateString email) {
        // Get realm from APIKey
        String idpRealm = apiKeyService.getIdpRealm(apiKey);

        // Update username and email address
        IdpAccountInfo result = keycloakRepository.updateUser(idpRealm, userId, loginUserId.getValue(), email.getValue());
        return result;
    }

    /**
     * Updates the username and email address for the specified user in Keycloak.
     *
     * @param apiKey the API key for authentication
     * @param userId the access token of the user
     * @param enabled the new enabled status to set
     */
    @Override
    @NonNull
    public IdpAccountInfo updateAccountStatus(@NonNull String apiKey, @NonNull String userId, Boolean enabled) {
        // Get realm from APIKey
        String idpRealm = apiKeyService.getIdpRealm(apiKey);

        // Update username and email address
        IdpAccountInfo result = keycloakRepository.updateUserStatus(idpRealm, userId, enabled);
        return result;
    }

    /**
     * Deletes the user account with the specified userId.
     *
     * @param apiKey the API key for authentication
     * @param userId the userId of the account to be deleted
     */
    @Override
    public void deleteAccount(@NonNull String apiKey, @NonNull String userId) {
        // Get realm from APIKey
        String idpRealm = apiKeyService.getIdpRealm(apiKey);

        // Delete user in Keycloak
        keycloakRepository.deleteUser(idpRealm, userId);
    }

    /**
     * Retrieves the user account with the specified userId.
     *
     * @param apiKey the API key for authentication
     * @param userId the userId of the account to be retrieved
     */
    @Override
    @NonNull
    public IdpAccountInfo getAccount(@NonNull String apiKey, @NonNull String userId) {
        // Get realm from APIKey
        String idpRealm = apiKeyService.getIdpRealm(apiKey);

        // Retrieve user in Keycloak
        IdpAccountInfo result = keycloakRepository.getUser(idpRealm, userId);
        return result;
    }

    /**
    * Registers a new confidential OpenID Connect client.
    *
    * @param apiKey the API key (currently not used for realm resolution)
    * @param clientId the unique client identifier
    * @param name the display name
    * @param flowType the client flow type ("authorization_code" or "client_credentials")
    * @param redirectUris the redirect URI list (required if flowType is "authorization_code")
    * @param operatorId the value placed into operator_id claim (nullable)
    * @param openSystemId the value placed into open_system_id claim for client_credentials flow (optional)
    */
    @Override
    @NonNull
        public IdpClientInfo registerClient(@NonNull String apiKey,
            @NonNull String clientId,
            String name,
            String description,
            @NonNull String flowType,
            List<String> redirectUris,
            String operatorId,
            String openSystemId) {

        // Get realm from APIKey
        String idpRealm = apiKeyService.getIdpRealm(apiKey);

        // Delegate to repository; let exceptions propagate
        return keycloakRepository.registerClient(
                idpRealm, clientId, name, description, flowType, redirectUris, operatorId, openSystemId);
    }

    /**
     * Retrieves client secret for given client_id.
     * Returns 200 if confidential, 400 if public or invalid, 500 on unexpected error.
     *
     * @param apiKey the API key for authentication
     * @param clientUuid target client uuid
     * @return API response with secret
     */
    @Override
    @NonNull
    public String getClientSecret(@NonNull String apiKey, @NonNull String clientUuid) {
        if (clientUuid.isBlank()) {
            throw new IllegalArgumentException("clientUuid must not be blank");
        }

        // Get realm from APIKey
        String idpRealm = apiKeyService.getIdpRealm(apiKey);
        // Get client secret from Keycloak
        return keycloakRepository.getClientSecret(idpRealm, clientUuid);
    }

    @Override
    @NonNull
    public String getClientUuid(@NonNull String apiKey, @NonNull String clientId) {
        String idpRealm = apiKeyService.getIdpRealm(apiKey);
        return keycloakRepository.getClientUuidByClientId(idpRealm, clientId);
    }

    /**
     * Deletes a client by UUID using the admin realm context.
     *
     * @param apiKey API key context (not used for realm resolution)
     * @param clientUuid UUID returned by registerClient
     */
    @Override
    public void deleteClient(@NonNull String apiKey, @NonNull String clientUuid) {
        // Get realm from APIKey
        String idpRealm = apiKeyService.getIdpRealm(apiKey);
        // Delete client in Keycloak
        keycloakRepository.deleteClient(idpRealm, clientUuid);
    }

    /**
     * Updates a client by UUID.
     */
    @Override
    @NonNull
    public IdpClientInfo updateClient(
            @NonNull String apiKey,
            @NonNull String clientUuid,
            String name,
            String description,
            @NonNull String flowType,
            String openSystemId,
            String operatorId,
            List<String> redirectUris) {
        // Get realm from APIKey
        String idpRealm = apiKeyService.getIdpRealm(apiKey);
        return keycloakRepository.updateClient(idpRealm, clientUuid, name, description, flowType, openSystemId, operatorId, redirectUris);
    }

    /**
     * Builds the issuer URL based on the API key.
     *
     * @param apiKey the API key.
     * @return the issuer URL
     */
    @Override
    @NonNull
    public String buildIssuer(@NonNull String apiKey) {
        // Get realm from APIKey
        String idpRealm = apiKeyService.getIdpRealm(apiKey);

        // Construct and return the issuer URL
        return keycloakUtil.getIssuer(idpRealm);
    }

    /**
     * Builds the JWKS URI based on the issuer.
     *
     * @param issuer the issuer URL
     * @return the JWKS URI
     */
    @Override
    @NonNull
    public String buildJwksUri(@NonNull String issuer) {
        // Construct and return the JWKS URI
        return keycloakUtil.getJwksUri(issuer);
    }

    /**
     * Revokes a token.
     *
     * @param apiKey the API key for authentication
     * @param clientId the client ID for authentication
     * @param clientSecret the client secret for authentication
     * @param refreshToken the refresh token to be revoked
     */
    @Override
    public TokenRevokeResult revoke(@NonNull String apiKey, @NonNull String clientId, @NonNull String clientSecret, @NonNull String refreshToken) {
        // Get realm from APIKey
        String idpRealm = apiKeyService.getIdpRealm(apiKey);
        // Revoke token in Keycloak
        return keycloakRepository.revoke(idpRealm, clientId, clientSecret, refreshToken);
    }
}