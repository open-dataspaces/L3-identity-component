/*
 * KeycloakRepository.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This repository defines the KeycloakRepository interface, which provides methods for interacting
 * with Keycloak for authentication and token management.
 */

package io.github.open_dataspaces.core.domain.repository.interfaces;

import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import io.github.open_dataspaces.core.domain.dto.IdpAccountInfo;
import io.github.open_dataspaces.core.domain.dto.IdpClientInfo;
import io.github.open_dataspaces.core.domain.entities.AccessTokenResult;
import io.github.open_dataspaces.core.domain.entities.TokenIntrospectionResult;
import io.github.open_dataspaces.core.domain.entities.TokenRevokeResult;

/**
 * Repository interface for Keycloak authentication and token operations.
 *
 * <p>Provides methods for user and client authentication, password management, token introspection,
 * and idToken verification using Keycloak.</p>
 */
public interface KeycloakRepository {

    /**
     * Builds a Keycloak client instance using admin credentials.
     *
     * @return a Keycloak client instance
     */
    Keycloak build();

    /**
     * Builds a Keycloak client instance using username and password. Use admin-cli as clientId.
     *
     * @param realm the Keycloak realm
     * @param username the username
     * @param password the password
     * @return a Keycloak client instance
     */
    Keycloak build(@NonNull String realm, @NonNull String username, @NonNull String password);

    /**
     * Builds a Keycloak client instance using username and password.
     *
     * @param idpRealm the Keycloak realm
     * @param username the username
     * @param password the password
     * @return a Keycloak client instance
     */
    Keycloak build(@NonNull String idpRealm, @NonNull String clientId, @NonNull String clientSecret, @NonNull String username,
            @NonNull String password);

    /**
     * Builds a Keycloak client instance using clientId, clientSecret, and grantType.
     *
     * @param idpRealm the Keycloak realm
     * @param clientId the clientId
     * @param clientSecret the client secret
     * @param grantType the OAuth2 grant type
     * @return a Keycloak client instance
     */
    Keycloak build(@NonNull String idpRealm, @NonNull String clientId, @NonNull String clientSecret, @NonNull String grantType);

    /**
     * Retrieves an access token from Keycloak using the provided authorization code and client credentials.
     *
     * @param idpRealm The realm in which the Keycloak instance operates.
     * @param code The authorization code received from the authorization server.
     * @param clientId The client ID registered with Keycloak.
     * @param clientSecret The client secret associated with the client ID.
     * @param redirectUri The redirect URI used in the authorization request.
     * @param codeVerifier The code verifier used for PKCE (Proof Key for Code Exchange).
     * @return An {@link AccessTokenResult} containing the access token and related information.
     */
    AccessTokenResult getAccessToken(@NonNull String idpRealm, @NonNull  String code, @NonNull String clientId,
            @NonNull String clientSecret, @NonNull String redirectUri, @NonNull String codeVerifier);

    /**
     * Authenticates a user using operatorAccountId and password.
     *
     * @param idpRealm the Keycloak realm
     * @param clientId the clientId for authentication
     * @param clientSecret the client secret for authentication
     * @param operatorAccountId the operatorAccountId
     * @param password the user's password
     * @return the result of the login attempt
     */
    AccessTokenResult signInWithPassword(@NonNull String idpRealm, @NonNull String clientId, @NonNull String clientSecret,
            @NonNull String operatorAccountId, @NonNull String password);

    /**
     * Authenticates a client using clientId and client secret.
     *
     * @param idpRealm the Keycloak realm
     * @param clientId the clientId
     * @param clientSecret the client secret
     * @return the result of the client login attempt
     */
    AccessTokenResult signInWithClient(@NonNull String idpRealm, @NonNull String clientId, @NonNull String clientSecret);

    /**
     * Checks if the provided password is valid for the specified user.
     *
     * @param idpRealm the Keycloak realm
     * @param uid the user ID (=operatorId)
     * @param password the password to validate
     * @return true if the password is valid, false otherwise
     */
    boolean isValidPassword(@NonNull String idpRealm, @NonNull String uid, @NonNull String password);

    /**
     * Changes the password for a user.
     *
     * @param idpRealm the Keycloak realm
     * @param uid the user ID (=operatorId)
     * @param newPassword the new password to set
     */
    void changePassword(@NonNull String idpRealm, @NonNull String uid, @NonNull String newPassword);

    /**
     * Introspects a token to retrieve its validity and associated information.
     *
     * @param idpRealm the Keycloak realm
     * @param clientId the client ID
     * @param clientSecret the client secret
     * @param token the access token to introspect
     * @param tokenTypeHint the type of the token (e.g., "access_token" or "refresh_token")
     * @return a TokenResult containing the introspection result
     */
    TokenIntrospectionResult tokenIntrospection(@NonNull String idpRealm, @NonNull String clientId, @NonNull String clientSecret,
            @NonNull String token, @NonNull String tokenTypeHint);

    /**
     * Refreshes an access token using the provided refresh token.
     *
     * @param idpRealm the Keycloak realm
     * @param clientId the client ID
     * @param clientSecret the client secret
     * @param refreshToken the refresh token to use for refreshing the access token
     * @return an AccessTokenResult containing the new access token and related information
     */
    AccessTokenResult tokenRefresh(@NonNull String idpRealm, @NonNull String clientId, @NonNull String clientSecret, @NonNull String refreshToken);

    /**
     * Verifies the clientId.
     *
     * @param realm the realm
     * @param clientId the clientId to verify
     * @return true if the clientId is valid, false otherwise
     */
    boolean isValidClientId(@NonNull String realm, @NonNull String clientId);

    /**
     * Creates a new user in Keycloak.
     *
     * @param idpRealm the Keycloak realm
     * @param loginUserId the login user id
     * @param email the email address for the new account
     * @param password the password for the new account
     * @param passwordTemporaryFlag indicates whether the password is temporary
     * @return the ID of the newly created user
     */
    String createUser(@NonNull String idpRealm, @NonNull String loginUserId, String email, String password, boolean passwordTemporaryFlag);

    /**
     * Updates the user account with the specified userId.
     *
     * @param idpRealm the Keycloak realm
     * @param userId the user ID
     * @param newLoginUserId the new login user ID
     * @param newEmail the new email address
     * @return the updated IdpAccountInfo
     */
    IdpAccountInfo updateUser(@NonNull String idpRealm, @NonNull String userId, String newLoginUserId, String newEmail);

    /**
     * Updates the user account status (enabled/disabled) with the specified userId.
     *
     * @param idpRealm the Keycloak realm
     * @param userId the user ID
     * @param enabledFlag the new enabled status
     * @return the updated IdpAccountInfo
     */
    IdpAccountInfo updateUserStatus(@NonNull String idpRealm, @NonNull String userId, Boolean enabledFlag);

    /**
     * Deletes the user with the specified userId.
     *
     * @param idpRealm the Keycloak realm
     * @param userId the userId of the account to be deleted
     */
    void deleteUser(@NonNull String idpRealm, @NonNull String userId);

    /**
     * Retrieves user information for the specified userId.
     *
     * @param idpRealm the Keycloak realm
     * @param userId the userId of the account to be retrieved
     * @return the IdpAccountInfo containing user information
     */
    @Nullable
    IdpAccountInfo getUser(@NonNull String idpRealm, @NonNull String userId);

    /**
     * Registers a new confidential OpenID Connect client in the given realm.
     *
     * @param idpRealm the Keycloak realm
     * @param clientId unique client identifier
     * @param name display name
     * @param flowType "authorization_code" or "client_credentials"
     * @param redirectUris redirect URIs for authorization_code flow (ignored otherwise)
     * @param operatorId claim value for operator_id (nullable)
     * @param openSystemId claim value for open_system_id (client_credentials only; optional)
     */
    IdpClientInfo registerClient(@NonNull String idpRealm,
            @NonNull String clientId,
            String name,
            String description,
            @NonNull String flowType,
            List<String> redirectUris,
            String operatorId,
            String openSystemId);

    /**
     * Retrieves client secret for a confidential client in the given realm.
     *
     * @param realm admin realm
     * @param clientId target client identifier
     */
    String getClientSecret(@NonNull String realm, @NonNull String clientId);

    /**
     * Resolves Keycloak internal client UUID (id) from clientId.
     *
     * @param idpRealm admin realm
     * @param clientId unique client identifier
     * @return Keycloak internal UUID (id)
     */
    @NonNull
    String getClientUuidByClientId(@NonNull String idpRealm, @NonNull String clientId);

    /**
     * Deletes the confidential client in the given realm by client UUID.
     *
     * @param idpRealm admin realm
     * @param clientUuid target client UUID
     */
    void deleteClient(@NonNull String idpRealm, @NonNull String clientUuid);

    /**
     * Updates a confidential client in the given realm by UUID.
     *
     * @param idpRealm the Keycloak realm
     * @param clientUuid target client UUID
     * @param name new display name (optional)
     * @param description new description (optional)
     * @param redirectUris redirect URIs (optional)
     * @return updated client info
     */
    @NonNull
    IdpClientInfo updateClient(
            @NonNull String idpRealm,
            @NonNull String clientUuid,
            String name,
            String description,
            @NonNull String flowType,
            String openSystemId,
            String operatorId,
            List<String> redirectUris);

    /**
     * Revokes a token by invalidating the provided token.
     *
     * @param idpRealm the Keycloak realm
     * @param clientId the client ID
     * @param clientSecret the client secret
     * @param refreshToken the refresh token to revoke
     * @return the result of the token revoke operation
     */
    TokenRevokeResult revoke(@NonNull String idpRealm, @NonNull String clientId, @NonNull String clientSecret, @NonNull String refreshToken);
}
