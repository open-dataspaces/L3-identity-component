/*
 * IdentityProviderService.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This file is part of the user authentication module. AuthUrlService defines the interface for
 * user login logic.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.service.interfaces;

import java.util.List;

import org.springframework.lang.NonNull;

import io.github.open_dataspaces.core.domain.dto.IdpAccountInfo;
import io.github.open_dataspaces.core.domain.dto.IdpClientInfo;
import io.github.open_dataspaces.core.domain.dto.StateString;
import io.github.open_dataspaces.core.domain.entities.AccessTokenResult;
import io.github.open_dataspaces.core.domain.entities.TokenIntrospectionResult;
import io.github.open_dataspaces.core.domain.entities.TokenRevokeResult;

/**
 * Service interface for user login operations.
 */
public interface IdentityProviderService {

    /**
     * Generates the IDP login URL based on the given parameters.
     *
     * @param apiKey the API key for authentication
     * @param clientId the client ID for authentication
     * @param redirectUri the redirect URI for the login
     * @param codeChallenge the code challenge for PKCE
     * @return the url response containing access and refresh tokens
     */
    @NonNull
    String buildAuthorizationUrl(@NonNull String apiKey, @NonNull String clientId,
            @NonNull String redirectUri, @NonNull String codeChallenge);

    /**
     * Retrieves an access token from Keycloak using the provided authorization code and client credentials.
     *
     * @param apiKey the API key for authentication
     * @param code the authorization code received from the IDP after user login
     * @param clientId the client ID for authentication
     * @param clientSecret the client secret for authentication
     * @param redirectUri the redirect URI used in the authorization request
     * @param codeVerifier The code verifier used for PKCE (Proof Key for Code Exchange).
     * @return An {@link AccessTokenResult} containing the access token and related information.
     */
    @NonNull
    AccessTokenResult getAccessToken(@NonNull String apiKey, @NonNull String code, @NonNull String clientId,
            @NonNull String clientSecret, @NonNull String redirectUri, @NonNull String codeVerifier);

    /**
     * Retrieves an access token from Keycloak using the provided authorization code and client credentials.
     *
     * @param apiKey the API key for authentication
     * @param accessToken the access token to introspect
     * @param clientId the client ID for authentication
     * @param clientSecret the client secret for authentication
     * @return a TokenIntrospectionResult containing the introspection result
     */
    @NonNull
    TokenIntrospectionResult tokenIntrospection(@NonNull String apiKey, @NonNull String accessToken,
            @NonNull String clientId, @NonNull String clientSecret);

    /**
     * Refreshes an access token using the provided refresh token and client credentials.
     *
     * @param apiKey the API key for authentication
     * @param refreshToken the refresh token to use for refreshing the access token
     * @param clientId the client ID for authentication
     * @param clientSecret the client secret for authentication
     * @return an AccessTokenResult containing the new access token and related information
     */
    @NonNull
    AccessTokenResult tokenRefresh(@NonNull String apiKey, @NonNull String refreshToken,
            @NonNull String clientId, @NonNull String clientSecret);

    /**
     * Authenticates a user using the provided API key, operator ID, and password.
     *
     * @param apiKey the API key for authentication
     * @param clientId the client ID for authentication
     * @param clientSecret the client secret for authentication
     * @param userId the user ID for authentication
     * @param password the password for authentication
     * @return an AccessTokenResult containing the access token and other details
     */
    @NonNull
    public AccessTokenResult signInWithPassword(@NonNull String apiKey, @NonNull String clientId, @NonNull String clientSecret, @NonNull String userId, @NonNull String password);

    /**
     * Authenticates a client using the provided API key, client ID, and client secret.
     *
     * @param apiKey the API key for authentication
     * @param clientId the client ID for authentication
     * @param clientSecret the client secret for authentication
     * @return an AccessTokenResult containing the access token and other details
     */
    @NonNull
    public AccessTokenResult signInWithClient(@NonNull String apiKey, @NonNull String clientId, @NonNull String clientSecret);

    /**
     * Builds the password change URL for the user.
     *
     * @param apiKey the API key for authentication
     * @param clientId the client ID for authentication
     * @param redirectUri the redirect URI for the password change
     * @param codeChallenge the code challenge for PKCE
     * @return the password change URL
     */
    @NonNull
    String buildPasswordChangeUrl(@NonNull String apiKey, @NonNull String clientId, @NonNull String redirectUri, @NonNull String codeChallenge);

    /**
     * Changes the password for the specified user.
     *
     * @param apiKey the API key for authentication
     * @param operatorId the user ID (operatorId) for which the password is being changed
     * @param oldPassword the old password to verify
     * @param newPassword the new password to set
     * @return true if the password was changed successfully
     */
    boolean changePassword(@NonNull String apiKey, @NonNull String operatorId, @NonNull String oldPassword, @NonNull String newPassword);

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
    @NonNull
    public IdpAccountInfo createAccount(@NonNull String apiKey, @NonNull String loginUserId, @NonNull String email,
            boolean createPasswordFlag, boolean passwordTemporaryFlag);

    /**
     * Changes the password for the specified user.
     *
     * @param apiKey the API key for authentication
     * @param userId the userId of the account to be updated
     * @param loginUserId the new login user ID to set
     * @param email the new email address to set
     */
    IdpAccountInfo updateAccount(@NonNull String apiKey, @NonNull String userId, @NonNull StateString loginUserId, @NonNull StateString email);

    /**
     * Updates the enabled status of a user account.
     *
     * @param apiKey the API key for authentication
     * @param userId the userId of the account to be updated
     * @param enabled the new enabled status
     * @return the updated IdpAccountInfo
     */
    IdpAccountInfo updateAccountStatus(@NonNull String apiKey, @NonNull String userId, Boolean enabled);

    /**
     * Deletes the user account with the specified userId.
     *
     * @param apiKey the API key for authentication
     * @param userId the userId of the account to be deleted
     */
    public void deleteAccount(@NonNull String apiKey, @NonNull String userId);

    /**
     * Retrieves the account information for the specified userId.
     *
     * @param apiKey the API key for authentication
     * @param userId the userId of the account to be retrieved
     * @return the IdpAccountInfo containing account details
     */
    @NonNull
    IdpAccountInfo getAccount(@NonNull String apiKey, @NonNull String userId);

    /**
     * Registers a new client in the IDP (Keycloak).
     *
     * @param apiKey API key context
     * @param clientId client identifier to create
     * @param name display name
     * @param flowType "authorization_code" or "client_credentials"
     * @param redirectUris redirect URIs for authorization_code (null otherwise)
     * @param operatorId operator_id claim value (required for client_credentials)
     * @param openSystemId open_system_id claim value (required for client_credentials)
     * @return created client info (secret not included)
     */
    @NonNull
    IdpClientInfo registerClient(@NonNull String apiKey,
            @NonNull String clientId,
            String name,
            String description,
            @NonNull String flowType,
            List<String> redirectUris,
            String operatorId,
            String openSystemId);

    /**
     * Retrieves a client secret for the given clientId.
     *
     * @param apiKey API key
     * @param clientUuid client identifier
     * @return client secret string
     */
    @NonNull
    String getClientSecret(@NonNull String apiKey, @NonNull String clientUuid);

    /**
     * Resolves Keycloak internal client UUID (id) from clientId.
     *
     * @param apiKey API key context
     * @param clientId client identifier
     * @return Keycloak internal UUID (id)
     */
    @NonNull
    String getClientUuid(@NonNull String apiKey, @NonNull String clientId);

    /**
     * Deletes the client registered in the IDP (Keycloak).
     *
     * @param apiKey API key context
     * @param clientUuid client identifier (UUID returned by registerClient)
     */
    void deleteClient(@NonNull String apiKey, @NonNull String clientUuid);

    /**
     * Updates a client registered in the IDP (Keycloak).
     *
     * @param apiKey API key context
     * @param clientUuid client identifier (UUID returned by registerClient)
     * @param name new display name (optional)
     * @param description new description (optional)
     * @param redirectUris redirect URIs (optional)
     * @param openSystemId new open_system_id (optional)
     * @param operatorId new operator_id (optional)
     * @return updated client info
     */
    @NonNull
    IdpClientInfo updateClient(
            @NonNull String apiKey,
            @NonNull String clientUuid,
            String name,
            String description,
            @NonNull String flowType,
            String openSystemId,
            String operatorId,
            List<String> redirectUris);

    /**
     * Builds the issuer URL based on the given API key.
     *
     * @param apiKey the API key for authentication
     * @return the issuer URL
     */
    @NonNull
    String buildIssuer(@NonNull String apiKey);

    /**
     * Builds the JWKS URI based on the given issuer.
     *
     * @param issuer the issuer URL
     * @return the JWKS URI
     */
    @NonNull
    String buildJwksUri(@NonNull String issuer);

    /**
     * Revokes a token in Keycloak.
     *
     * @param apiKey API key context
     * @param clientId the client ID for authentication
     * @param clientSecret the client secret for authentication
     * @param refreshToken the refresh token to revoke
     * @return revoke result
     */
    TokenRevokeResult revoke(@NonNull String apiKey, @NonNull String clientId, @NonNull String clientSecret, @NonNull String refreshToken);

}
