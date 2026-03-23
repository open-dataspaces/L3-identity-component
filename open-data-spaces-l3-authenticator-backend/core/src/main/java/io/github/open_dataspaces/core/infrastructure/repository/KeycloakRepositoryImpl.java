/*
 * KeycloakRepositoryImpl.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This repository provides Keycloak integration for authentication and token operations.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.infrastructure.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ProtocolMappersResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.dto.IdpAccountInfo;
import io.github.open_dataspaces.core.domain.dto.IdpClientInfo;
import io.github.open_dataspaces.core.domain.entities.AccessTokenResult;
import io.github.open_dataspaces.core.domain.entities.TokenIntrospectionResult;
import io.github.open_dataspaces.core.domain.entities.TokenRevokeResult;
import io.github.open_dataspaces.core.domain.repository.interfaces.KeycloakRepository;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.ConflictException;
import io.github.open_dataspaces.core.exception.LoginException;
import io.github.open_dataspaces.core.exception.NotFoundException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnauthorizedException;
import io.github.open_dataspaces.core.exception.UnexpectedException;
import io.github.open_dataspaces.core.exception.ValidateException;
import io.github.open_dataspaces.core.infrastructure.config.KeycloakProperties;
import io.github.open_dataspaces.core.infrastructure.utils.KeycloakUtil;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;

/**
 * Repository implementation for Keycloak integration.
 *
 * <p>This class provides methods for authenticating users and clients, refreshing tokens, changing
 * passwords, and performing token introspection using Keycloak.</p>
 */
@Repository
public class KeycloakRepositoryImpl implements KeycloakRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakRepositoryImpl.class);

    private KeycloakProperties keycloakProperties;

    private RestTemplate restTemplate;

    private KeycloakUtil keycloakUtil;

    /**
     * Constructor for KeycloakRepositoryImpl. This constructor is used by Spring to create an
     * instance of this repository.
     *
     * @param restTemplate the RestTemplate for making HTTP requests
     * @param keycloakUtil the Keycloak utility for building Keycloak client instances
     */
    public KeycloakRepositoryImpl(RestTemplate restTemplate, KeycloakUtil keycloakUtil, KeycloakProperties keycloakProperties) {
        // Default constructor for Spring
        this.restTemplate = restTemplate;
        this.keycloakUtil = keycloakUtil;
        this.keycloakProperties = keycloakProperties;
    }

    /**
     * Builds a Keycloak client instance using admin credentials.
     *
     * @return a Keycloak client instance
     */
    @Override
    public Keycloak build() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.getApiEndpoint())
                .realm(keycloakProperties.getCredentials().getAdminRealm())
                .clientId(keycloakProperties.getCredentials().getAdminClientId())
                .username(keycloakProperties.getCredentials().getAdminUserName())
                .password(keycloakProperties.getCredentials().getAdminPassword())
                .grantType(OAuth2Constants.PASSWORD)
                .build();
    }

    /**
     * Builds a Keycloak client instance using username and password. Use admin-cli as clientId.
     *
     * @param realm the Keycloak realm
     * @param username the username
     * @param password the password
     * @return a Keycloak client instance
     */
    @Override
    public Keycloak build(@NonNull String realm, @NonNull String username, @NonNull String password) {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.getApiEndpoint())
                .realm(realm)
                .clientId(keycloakProperties.getCredentials().getAdminClientId())
                .username(username)
                .password(password)
                .grantType(OAuth2Constants.PASSWORD)
                .build();
    }

    /**
     * Builds a Keycloak client instance using the specified username and password.
     *
     * @param idpRealm the Keycloak realm
     * @param username the username
     * @param password the password
     * @return a Keycloak client instance
     */
    @Override
    public Keycloak build(@NonNull String idpRealm, @NonNull String clientId, @NonNull String clientSecret, @NonNull String username,
            @NonNull String password) {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.getApiEndpoint())
                .realm(idpRealm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .username(username)
                .password(password)
                .grantType(OAuth2Constants.PASSWORD)
                .build();
    }

    /**
     * Builds a Keycloak client instance using the specified client ID, client secret, and grant
     * type.
     *
     * @param idpRealm the Keycloak realm
     * @param clientId the client ID
     * @param clientSecret the client secret
     * @param grantType the OAuth2 grant type
     * @return a Keycloak client instance
     */
    @Override
    public Keycloak build(@NonNull String idpRealm, @NonNull String clientId, @NonNull String clientSecret,
            @NonNull String grantType) {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.getApiEndpoint())
                .realm(idpRealm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType(grantType)
                .build();
    }

    /**
     * Authenticates a user using a Keycloak instance and returns the login result.
     *
     * @param newKeycloak the Keycloak client instance
     * @param id the user or client ID
     * @return the result of the login attempt
     * @throws UnexpectedException if the Keycloak client is null or login fails
     * @throws LoginException if login fails
     */
    private AccessTokenResult signIn(Keycloak newKeycloak, String id) {
        if (newKeycloak == null) {
            throw new UnexpectedException(ConstError.ERRLOG_500_KEYCLOAK_BUILD, ConstError.ERR_500_MESSAGE);
        }
        // Login to Keycloak and obtain access token
        AccessTokenResponse response = newKeycloak.tokenManager().getAccessToken();
        return new AccessTokenResult(
                response.getToken(), response.getExpiresIn(), response.getTokenType(),
                response.getNotBeforePolicy(), response.getScope(), response.getRefreshToken(),
                response.getRefreshExpiresIn(), response.getIdToken());
    }

    /**
     * Authenticates a user using operatorAccountId and password.
     *
     * @param operatorAccountId the operatorAccountId
     * @param password the user's password
     * @return the result of the login attempt
     */
    @Override
    public AccessTokenResult signInWithPassword(@NonNull String idpRealm, @NonNull String clientId, @NonNull String clientSecret,
            @NonNull String operatorAccountId, @NonNull String password) {
        try (Keycloak newKeycloak = build(idpRealm, clientId, clientSecret, operatorAccountId, password)) {
            // Login to Keycloak and obtain access token
            return signIn(newKeycloak, operatorAccountId);
        } catch (jakarta.ws.rs.NotAuthorizedException  e) {
            throw new LoginException(ConstError.ERR_401_INVALID_CREDENTIALS, "");
        }
    }

    /**
     * Authenticates a client using clientId and client secret.
     *
     * @param idpRealm the Keycloak realm
     * @param clientId the clientId
     * @param clientSecret the client secret
     * @return the result of the login attempt
     */
    @Override
    public AccessTokenResult signInWithClient(@NonNull String idpRealm, @NonNull String clientId, @NonNull String clientSecret) {
        try (Keycloak newKeycloak = build(idpRealm, clientId, clientSecret, OAuth2Constants.CLIENT_CREDENTIALS)) {
            // Login to Keycloak and obtain access token
            return signIn(newKeycloak, clientId);
        } catch (jakarta.ws.rs.NotAuthorizedException  e) {
            throw new LoginException(ConstError.ERR_401_INVALID_CREDENTIALS, "");
        }
    }

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
    @Override
    public AccessTokenResult getAccessToken(@NonNull String idpRealm, @NonNull String code, @NonNull String clientId,
            @NonNull String clientSecret, @NonNull String redirectUri, @NonNull String codeVerifier) {

        // Make request URL
        String url = keycloakUtil.getTokenStoreUrl(idpRealm);

        // Make request header
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", Const.CONTENT_TYPE_FORM);

        // Make request body
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(Const.KEYCLOAK_SEND_PARAM_GRANT_TYPE, Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_AUTHORIZATION_CODE);
        params.add(Const.KEYCLOAK_SEND_PARAM_AUTHORIZATION_CODE, code);
        params.add(Const.KEYCLOAK_SEND_PARAM_CLIENT_ID, clientId);
        params.add(Const.KEYCLOAK_SEND_PARAM_CLIENT_SECRET, clientSecret);
        params.add(Const.KEYCLOAK_SEND_PARAM_REDIRECT_URI, redirectUri);
        params.add(Const.KEYCLOAK_SEND_PARAM_CODE_VERIFIER, codeVerifier);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<AccessTokenResponse> responseEntity;
        try {
            // Send request and get response
            responseEntity =
                    restTemplate.postForEntity(url, request, AccessTokenResponse.class);
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            // Keycloak is unavailable
            LOGGER.debug("Keycloak is unavailable: {}", e.getMessage(), e);
            throw new OutOfServiceException(ConstError.ERR_503_OUT_OF_SERVICE_KEYCLOAK, e);
        } catch (HttpClientErrorException.BadRequest e) {
            LOGGER.debug("Bad request while getting access token: {}", e.getMessage(), e);
            throw new BadParametersException(
                String.format(ConstError.ERRLOG_400_INVALID_GRANT, e.getMessage()), ConstError.ERR_400_INVALID_GRANT, e);
        } catch (HttpClientErrorException.Unauthorized e) {
            LOGGER.debug("Unauthorized request while getting access token: {}", e.getMessage(), e);
            throw new UnauthorizedException(
                String.format(ConstError.ERRLOG_401_INVALID_CLIENT, e.getMessage()), ConstError.ERR_401_INVALID_CLIENT, e);
        } catch (Exception e) {
            LOGGER.debug("Unexpected error while getting access token: {}", e.getMessage(), e);
            throw new UnexpectedException(ConstError.ERRLOG_500_KEYCLOAK_BUILD, ConstError.ERR_500_MESSAGE, e);
        }

        // Check response body
        AccessTokenResponse response = responseEntity.getBody();
        if (response == null) {
            throw new UnexpectedException(ConstError.ERRLOG_500_KEYCLOAK_NO_RESPONSE, ConstError.ERR_500_MESSAGE);
        }

        // Get access token and return it
        AccessTokenResult accessTokenResult = new AccessTokenResult(
                response.getToken(), response.getExpiresIn(), response.getTokenType(),
                response.getNotBeforePolicy(), response.getScope(), response.getRefreshToken(),
                response.getRefreshExpiresIn(), response.getIdToken());

        return accessTokenResult;
    }

    /**
     * Checks if the provided password is valid for the specified user.
     *
     * @param idpRealm the Keycloak realm
     * @param uid the user ID (=operatorId)
     * @param password the password to validate
     * @return true if the password is valid, false otherwise
     */
    @Override
    public boolean isValidPassword(@NonNull String idpRealm, @NonNull String uid, @NonNull String password) {
        // Get the user resource from Keycloak
        String loginId = "";
        try (Keycloak newKeycloak = build()) {
            UserResource userResource = getUserResource(newKeycloak, idpRealm, uid);
            if (userResource == null) {
                return false; // User not found
            }
            // Get the login ID (username) of the user
            loginId = userResource.toRepresentation().getUsername();

            // Check if the password is valid, in case of temporary password or not set password, assume valid
            List<CredentialRepresentation> credentials = userResource.credentials();
            if (credentials == null || credentials.isEmpty()) {
                if (password.isEmpty()) {
                    return true; // Password is not set, so it is considered valid.(old password can be empty)
                }
                return false; // Password is not set, so it is considered invalid.
            } else {
                // If the user has a temporary password, it is considered valid.
                if (!password.isEmpty()
                        && credentials.getFirst().getType().equals(Const.KEYCLOAK_USERRESOURCE_CREDENTIAL_TYPE_PASSWORD)
                        && userResource.toRepresentation().getRequiredActions()
                        .contains(Const.KEYCLOAK_USERRESOURCE_REQUIRED_ACTION_UPDATE_PASSWORD)) {
                    return true; // Password is temporary, so it is considered valid.(old password can be empty)
                }
            }

        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new UnexpectedException(String.format(ConstError.ERRLOG_500_KEYCLOAK_FAILED, e.getResponse().getStatus()), ConstError.ERR_500_MESSAGE, e);
        }

        // Validate the password by trying to build a Keycloak client with the provided credentials
        try (Keycloak newKeycloak = build(idpRealm, loginId, password)) {
            if (newKeycloak.tokenManager().getAccessToken() != null) {
                return true; // Password is valid
            } else {
                return false; // Password is invalid
            }
        } catch (jakarta.ws.rs.NotAuthorizedException e) {
            return false; // Password is invalid
        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new UnexpectedException(String.format(ConstError.ERRLOG_500_KEYCLOAK_FAILED, e.getResponse().getStatus()), ConstError.ERR_500_MESSAGE, e);
        }
    }

    /**
     * Changes the password for a user.
     *
     * @param uid the user ID
     * @param newPassword the new password to set
     * @throws LoginException if the user is not found
     */
    @Override
    public void changePassword(@NonNull String idpRealm, @NonNull String uid, @NonNull String newPassword) {
        // Create a Credentilal object for the new password
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(newPassword);

        // Get the user resource from Keycloak
        try (Keycloak newKeycloak = build()) {
            UserResource userResource = getUserResource(newKeycloak, idpRealm, uid);
            if (userResource == null) {
                throw new UnauthorizedException(
                    String.format(ConstError.ERRLOG_401_USER_NOT_FOUND, uid), ConstError.ERR_401_INVALID_CLIENT);
            }
            // Change the password
            userResource.resetPassword(credential);
        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new UnexpectedException(String.format(ConstError.ERRLOG_500_KEYCLOAK_FAILED, e.getResponse().getStatus()), ConstError.ERR_500_MESSAGE, e);
        }
    }

    /**
     * Retrieves the user resource from Keycloak.
     *
     * @param keycloak the Keycloak client instance
     * @param idpRealm the Keycloak realm
     * @param uid the user ID
     * @return the UserResource for the specified user
     */
    private UserResource getUserResource(@NonNull Keycloak keycloak, @NonNull String idpRealm, @NonNull String uid) {
        // Get the user resource from Keycloak
        return keycloak.realm(idpRealm).users().get(uid);
    }

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
    @Override
    public TokenIntrospectionResult tokenIntrospection(@NonNull String idpRealm, @NonNull String clientId, @NonNull String clientSecret,
            @NonNull String token, @NonNull String tokenTypeHint) {
        // Make request URL
        String url = keycloakUtil.getTokenIntrospectUrl(idpRealm);

        // Make request header
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", Const.CONTENT_TYPE_FORM);

        // Make request body
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add(Const.KEYCLOAK_SEND_PARAM_CLIENT_ID, clientId);
        param.add(Const.KEYCLOAK_SEND_PARAM_CLIENT_SECRET, clientSecret);
        param.add(Const.KEYCLOAK_SEND_PARAM_TOKEN, token);
        param.add(Const.KEYCLOAK_SEND_PARAM_TOKEN_TYPE_HINT, tokenTypeHint);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(param, headers);

        // Send request and get response
        ResponseEntity<TokenIntrospectionResult> responseEntity;
        try {
            responseEntity = restTemplate.postForEntity(url, request, TokenIntrospectionResult.class);
        } catch (HttpClientErrorException.BadRequest e) {
            LOGGER.debug("Bad request while introspecting token: {}", e.getMessage(), e);
            throw new BadParametersException(
                String.format(ConstError.ERRLOG_400_INVALID_GRANT, e.getMessage()), ConstError.ERR_400_INVALID_GRANT, e);
        } catch (HttpClientErrorException.Unauthorized e) {
            LOGGER.debug("Unauthorized request while introspecting token: {}", e.getMessage(), e);
            throw new UnauthorizedException(
                String.format(ConstError.ERRLOG_401_INVALID_CLIENT, e.getMessage()), ConstError.ERR_401_INVALID_CLIENT, e);
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            // Keycloak is unavailable
            LOGGER.debug("Unavailable error while introspecting token: {}", e.getMessage(), e);
            throw new OutOfServiceException(ConstError.ERR_503_OUT_OF_SERVICE_KEYCLOAK, e);
        } catch (Exception e) {
            LOGGER.debug("Unexpected error while introspecting token: {}", e.getMessage(), e);
            throw new UnexpectedException(ConstError.ERRLOG_500_KEYCLOAK_BUILD, ConstError.ERR_500_MESSAGE, e);
        }

        // Get and check response body
        TokenIntrospectionResult response = responseEntity.getBody();
        if (response == null) {
            throw new UnexpectedException(ConstError.ERRLOG_500_KEYCLOAK_PARSE_FAILED, ConstError.ERR_500_MESSAGE);
        }

        return response;
    }

    /**
     * Refreshes an access token using a refresh token.
     *
     * @param refreshToken the refresh token
     * @return the result of the login attempt with new tokens
     * @throws OutOfServiceException if Keycloak is unavailable
     * @throws UnexpectedException if an unexpected error occurs
     */
    @Override
    public AccessTokenResult tokenRefresh(@NonNull String idpRealm, @NonNull String clientId, @NonNull String clientSecret, @NonNull String refreshToken) {
        // Make request URL
        String url = keycloakUtil.getTokenStoreUrl(idpRealm);

        // Make request header
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", Const.CONTENT_TYPE_FORM);

        // Make request body
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(Const.KEYCLOAK_SEND_PARAM_GRANT_TYPE, Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE);
        params.add(Const.KEYCLOAK_SEND_PARAM_REFRESH_TOKEN, refreshToken);
        params.add(Const.KEYCLOAK_SEND_PARAM_CLIENT_ID, clientId);
        params.add(Const.KEYCLOAK_SEND_PARAM_CLIENT_SECRET, clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<AccessTokenResponse> responseEntity;
        try {
            // Send request and get response
            responseEntity =
                    restTemplate.postForEntity(url, request, AccessTokenResponse.class);
        } catch (HttpClientErrorException.BadRequest e) {
            LOGGER.debug("Bad request while refreshing token: {}", e.getMessage(), e);
            throw new BadParametersException(
                String.format(ConstError.ERRLOG_400_INVALID_GRANT, e.getMessage()), ConstError.ERR_400_INVALID_GRANT, e);
        } catch (HttpClientErrorException.Unauthorized e) {
            LOGGER.debug("Unauthorized request while refreshing token: {}", e.getMessage(), e);
            throw new UnauthorizedException(
                String.format(ConstError.ERRLOG_401_INVALID_CLIENT, e.getMessage()), ConstError.ERR_401_INVALID_CLIENT, e);
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            // Keycloak is unavailable
            LOGGER.debug("Unavailable error while refreshing token: {}", e.getMessage(), e);
            throw new OutOfServiceException(ConstError.ERR_503_OUT_OF_SERVICE_KEYCLOAK, e);
        } catch (Exception e) {
            LOGGER.debug("Unexpected error while refreshing token: {}", e.getMessage(), e);
            throw new UnexpectedException(ConstError.ERRLOG_500_KEYCLOAK_BUILD, ConstError.ERR_500_MESSAGE, e);
        }

        // Check response body
        AccessTokenResponse response = responseEntity.getBody();
        if (response == null) {
            throw new UnexpectedException(ConstError.ERRLOG_500_KEYCLOAK_NO_RESPONSE, ConstError.ERR_500_MESSAGE);
        }

        // Get access token and return it
        AccessTokenResult accessTokenResult = new AccessTokenResult(
                response.getToken(), response.getExpiresIn(), response.getTokenType(),
                response.getNotBeforePolicy(), response.getScope(), response.getRefreshToken(),
                response.getRefreshExpiresIn(), response.getIdToken());

        LOGGER.trace("Get authentication access token: {}", accessTokenResult);

        return accessTokenResult;
    }

    /**
     * Changes the password for a user.
     *
     * @param realm the Keycloak realm
     * @param clientId the client ID to validate
     * @throws LoginException if the user is not found
     */
    @Override
    public boolean isValidClientId(@NonNull String realm, @NonNull String clientId) {

        // Get the user resource from Keycloak
        try (Keycloak newKeycloak = build()) {
            List<ClientRepresentation> clients = newKeycloak.realm(realm).clients().findByClientId(clientId);
            if (clients == null || clients.isEmpty()) {
                return false; // Client ID is not valid
            }
        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new UnexpectedException(
                    String.format(ConstError.ERRLOG_500_KEYCLOAK_FAILED, e.getResponse().getStatus()), ConstError.ERR_500_MESSAGE, e);
        }
        return true;
    }

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
    @Override
    public String createUser(@NonNull String idpRealm, @NonNull String loginUserId, String email, String password, boolean passwordTemporaryFlag) {
        // Create a new user representation
        UserRepresentation user = new UserRepresentation();
        user.setUsername(loginUserId);
        user.setEnabled(true);
        if (StringUtils.hasText(email)) {
            user.setEmail(email);
        }

        // Create the user in Keycloak
        String userId = "";
        try (Keycloak newKeycloak = build()) {
            Response res = newKeycloak.realm(idpRealm).users().create(user);
            // Check the response status. If it's 409, it means the user already exists.
            if (res.getStatus() == HttpStatus.SC_CONFLICT) {
                String errorMessage = res.readEntity(String.class);
                if (errorMessage.contains(Const.KEYCLOAK_ENTITY_USERNAME)) {
                    throw new ConflictException(
                            String.format(ConstError.ERR_409_CONFLICT,
                            Const.JSON_PROPERTY_LOGIN_USER_ID, loginUserId));
                } else if (errorMessage.contains(Const.KEYCLOAK_ENTITY_EMAIL)) {
                    throw new ConflictException(
                            String.format(ConstError.ERR_409_CONFLICT,
                            Const.JSON_PROPERTY_EMAIL_ADDRESS, email));
                }
            }
            // If the response status is not 201 (Created), throw an exception
            if (res.getStatus() != HttpStatus.SC_CREATED) {
                throw new UnexpectedException(
                        String.format(ConstError.ERRLOG_500_KEYCLOAK_FAILED, res.getStatus()), ConstError.ERR_500_MESSAGE);
            }

            // Extract the user ID from the URI
            userId = res.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            // Set the password if provided
            if (StringUtils.hasText(password)) {
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(password);
                credential.setTemporary(passwordTemporaryFlag);
                newKeycloak.realm(idpRealm).users().get(userId).resetPassword(credential);
            }
        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new UnexpectedException(
                    String.format(ConstError.ERRLOG_500_KEYCLOAK_FAILED, e.getResponse().getStatus()), ConstError.ERR_500_MESSAGE, e);
        } catch (ConflictException | UnexpectedException e) {
            throw e;
        } catch (Exception e) {
            throw new UnexpectedException(
                String.format(ConstError.ERRLOG_500_KEYCLOAK_FAILED_WITH_MESSAGE, e.getMessage()), ConstError.ERR_500_MESSAGE, e);
        }

        return userId;
    }

    /**
     * Updates the account information for a user.
     *
     * @param idpRealm the Keycloak realm
     * @param userId the user ID of the account to be updated
     * @param newLoginUserId the new login user id to set (optional)
     * @param newEmail the new email address to set (optional)
     * @return an UpdateAccountResult containing the updated username and email
     * @throws ConflictException if the new username or email already exists
     * @throws UnexpectedException if an unexpected error occurs
     */
    public IdpAccountInfo updateUser(@NonNull String idpRealm, @NonNull String userId, String newLoginUserId, String newEmail) {
        try (Keycloak keycloak = build()) {
            UserResource userResource = getUserResource(keycloak, idpRealm, userId);
            UserRepresentation existingUser = userResource.toRepresentation();
            // Check for changes
            boolean needUpdate = false;

            if (newLoginUserId != null && !newLoginUserId.equals(existingUser.getUsername())) {
                // username duplicate check
                List<UserRepresentation> users = keycloak.realm(idpRealm).users().search(newLoginUserId, true);
                if (users != null && !users.isEmpty() && !users.get(0).getId().equals(userId)) {
                    throw new ConflictException(
                            String.format(ConstError.ERR_409_CONFLICT, Const.JSON_PROPERTY_LOGIN_USER_ID, newLoginUserId));
                }
                existingUser.setUsername(newLoginUserId);
                needUpdate = true;
            }
            if (newEmail != null && !newEmail.equals(existingUser.getEmail())) {
                existingUser.setEmail(newEmail);
                needUpdate = true;
            }
            // Update the user if there are changes
            if (needUpdate) {
                userResource.update(existingUser);
            }
            UserRepresentation userUpdated = userResource.toRepresentation();
            return new IdpAccountInfo(userId, userUpdated.getUsername(), userUpdated.getEmail(), userUpdated.isEnabled());
        } catch (ConflictException e) {
            throw e;
        } catch (BadRequestException e) {
            LOGGER.debug("Bad request error occurred while updating account, there may be an issue with the Keycloak realm configuration: {}", e.getMessage(), e);
            throw new UnexpectedException(ConstError.ERRLOG_500_KEYCLOAK_BUILD, ConstError.ERR_500_MESSAGE, e);
        } catch (Exception e) {
            LOGGER.debug("Unexpected error occurred while updating account: {}", e.getMessage(), e);
            throw new UnexpectedException(ConstError.ERRLOG_500_KEYCLOAK_BUILD, ConstError.ERR_500_MESSAGE, e);
        }
    }

    /**
     * Updates the enabled status of a user.
     *
     * @param idpRealm the Keycloak realm
     * @param userId the user ID of the account to be updated
     * @param enabledFlag the new enabled status to set
     */
    @Override
    public IdpAccountInfo updateUserStatus(@NonNull String idpRealm, @NonNull String userId, Boolean enabledFlag) {
        // If enabledFlag is null, no update is needed
        try (Keycloak keycloak = build()) {
            // Get the user resource for the specified userId
            UserResource userResource = getUserResource(keycloak, idpRealm, userId);
            UserRepresentation existingUser = userResource.toRepresentation();

            // Update the enabled status if it is different from the current status
            if (enabledFlag != null && !enabledFlag.equals(existingUser.isEnabled())) {
                existingUser.setEnabled(enabledFlag);
                userResource.update(existingUser);

                // Return the updated user information
                UserRepresentation userUpdated = userResource.toRepresentation();
                return new IdpAccountInfo(userId, userUpdated.getUsername(), userUpdated.getEmail(), userUpdated.isEnabled());
            }
            return new IdpAccountInfo(userId, existingUser.getUsername(), existingUser.getEmail(), existingUser.isEnabled());
        } catch (BadRequestException e) {
            LOGGER.debug("Bad request error occurred while updating account, there may be an issue with the Keycloak realm configuration: {}", e.getMessage(), e);
            throw new UnexpectedException(ConstError.ERRLOG_500_KEYCLOAK_BUILD, ConstError.ERR_500_MESSAGE, e);
        } catch (Exception e) {
            LOGGER.debug("Unexpected error occurred while updating account: {}", e.getMessage(), e);
            throw new UnexpectedException(ConstError.ERRLOG_500_KEYCLOAK_BUILD, ConstError.ERR_500_MESSAGE, e);
        }
    }

    /**
     * Deletes the user with the specified userId.
     *
     * @param idpRealm the Keycloak realm
     * @param userId the userId of the account to be deleted
     */
    @Override
    public void deleteUser(@NonNull String idpRealm, @NonNull String userId) {
        try (Keycloak newKeycloak = build()) {
            Response res = newKeycloak.realm(idpRealm).users().delete(userId);
            // If the response status is not 204 (No Content), throw an exception
            if (res.getStatus() != HttpStatus.SC_NO_CONTENT) {
                throw new UnexpectedException(
                        String.format(ConstError.ERRLOG_500_KEYCLOAK_FAILED, res.getStatus()), ConstError.ERR_500_MESSAGE);
            }
        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new UnexpectedException(
                    String.format(ConstError.ERRLOG_500_KEYCLOAK_FAILED, e.getResponse().getStatus()), ConstError.ERR_500_MESSAGE, e);
        } catch (Exception e) {
            throw new UnexpectedException(
                    String.format(ConstError.ERRLOG_500_KEYCLOAK_FAILED_WITH_MESSAGE, e.getMessage()), ConstError.ERR_500_MESSAGE, e);
        }
    }

    /**
     * Retrieves the user with the specified userId.
     *
     * @param idpRealm the Keycloak realm
     * @param userId the userId of the account to be retrieved
     */
    @Override
    public @Nullable IdpAccountInfo getUser(@NonNull String idpRealm, @NonNull String userId) {
        try (Keycloak newKeycloak = build()) {
            UserResource userResource = getUserResource(newKeycloak, idpRealm, userId);
            UserRepresentation userRepresentation = userResource.toRepresentation();
            return new IdpAccountInfo(userId, userRepresentation.getUsername(), userRepresentation.getEmail(), userRepresentation.isEnabled());
        } catch (jakarta.ws.rs.NotFoundException e) {
            // User not found in Keycloak
            return null;
        } catch (Exception e) {
            throw new UnexpectedException(
                    String.format(ConstError.ERRLOG_500_KEYCLOAK_FAILED_WITH_MESSAGE, e.getMessage()), ConstError.ERR_500_MESSAGE, e);
        }
    }

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
     * @return created client info (secret not included)
     */
    @Override
    public IdpClientInfo registerClient(
            @NonNull String idpRealm,
            @NonNull String clientId,
            String name,
            String description,
            @NonNull String flowType,
            List<String> redirectUris,
            String operatorId,
            String openSystemId) {

        try (Keycloak kc = build()) {

            validateDuplicate(kc, idpRealm, clientId);

            ClientRepresentation client = buildBaseClient(clientId, name, description);
            applyFlowSettings(client, flowType, redirectUris);

            String uuid = createClient(kc, idpRealm, client);

            ClientResource clientResource = kc.realm(idpRealm).clients().get(uuid);
            ClientRepresentation rep = clientResource.toRepresentation();
            boolean enabled = Boolean.TRUE.equals(rep.isEnabled());

            addProtocolMappers(clientResource, flowType, operatorId, openSystemId);

            return toDto(uuid, clientId, name, description, redirectUris, enabled);
        } catch (ConflictException e) {
            throw e;
        } catch (BadParametersException e) {
            throw e;
        } catch (Exception e) {
            throw new UnexpectedException(
                String.format(ConstError.ERRLOG_500_KEYCLOAK_FAILED_WITH_MESSAGE, e.getMessage()),
                ConstError.ERR_500_MESSAGE,
                e
            );
        }
    }

    /**
     * Retrieves client secret for a confidential client in the given realm.
     *
     * @param idpRealm the Keycloak realm
     * @param clientUuid target client identifier
     * @return client secret value
     * @throws IllegalArgumentException when not found or public client
     * @throws IllegalStateException when Keycloak operation fails unexpectedly
     */
    @Override
    public String getClientSecret(@NonNull String idpRealm, @NonNull String clientUuid) {
        try (Keycloak kc = build()) {
            ClientResource clientResource;
            ClientRepresentation rep;

            try {
                clientResource = kc.realm(idpRealm).clients().get(clientUuid);
                rep = clientResource.toRepresentation(); // may throw 404 if UUID is invalid
            } catch (jakarta.ws.rs.NotFoundException e) {
                // Client does not exist in Keycloak.
                throw new UnexpectedException(
                        String.format(ConstError.ERRLOG_500_KEYCLOAK_FAILED_WITH_MESSAGE, Const.JSON_PROPERTY_CLIENT_UUID),
                        null
                    );
            }

            if (Boolean.TRUE.equals(rep.isPublicClient())) {
                // Secret cannot be retrieved for public clients.
                throw new UnexpectedException(
                        String.format(ConstError.ERRLOG_500_KEYCLOAK_FAILED_WITH_MESSAGE, Const.JSON_PROPERTY_CLIENT_UUID),
                        null
                    );
            }

            return clientResource.getSecret().getValue();
        } catch (io.github.open_dataspaces.core.exception.NotFoundException e) {
            // Propagate controlled client errors.
            throw e;
        } catch (UnexpectedException e) {
            // Propagate controlled client errors.
            throw e;
        } catch (Exception e) {
            // Normalize any unexpected failure.
            throw new UnexpectedException(
                    String.format(ConstError.ERRLOG_500_KEYCLOAK_FAILED_WITH_MESSAGE, e.getMessage()),
                    ConstError.ERR_500_MESSAGE,
                    e
            );
        }
    }

    /**
     * Resolves Keycloak internal client UUID (id) from clientId.
     *
     * @param idpRealm the Keycloak realm
     * @param clientId client identifier
     * @return Keycloak internal UUID (id)
     */
    @Override
    @NonNull
    public String getClientUuidByClientId(@NonNull String idpRealm, @NonNull String clientId) {
        try (Keycloak kc = build()) {
            List<ClientRepresentation> clients = kc.realm(idpRealm).clients().findByClientId(clientId);
            if (clients == null || clients.isEmpty()) {
                throw new NotFoundException(
                        String.format(ConstError.ERR_404_RESOURCE_NOT_FOUND_MESSAGE, Const.KEYCLOAK_PROPERTY_CLIENT_ID));
            }
            return clients.getFirst().getId();
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new UnexpectedException(
                    String.format(ConstError.ERRLOG_500_KEYCLOAK_FAILED_WITH_MESSAGE, e.getMessage()),
                    ConstError.ERR_500_MESSAGE,
                    e);
        }
    }

    /**
     * Deletes a confidential client in the given realm.
     * Uses the admin client to locate the client resource by UUID and invokes remove()
     *
     * @param idpRealm the Keycloak realm
     * @param clientUuid the internal client UUID to delete
     */
    @Override
    public void deleteClient(@NonNull String idpRealm, @NonNull String clientUuid) {
        try (Keycloak newKeycloak = build()) {
            ClientResource clientResource = newKeycloak.realm(idpRealm).clients().get(clientUuid);
            ClientRepresentation existing = clientResource.toRepresentation();

            if (!isAuthorizationCodeFlow(existing) && !isClientCredentialsFlow(existing)) {
                throw new ValidateException(
                        ConstError.ERRLOG_400_INVALID_REQUEST,
                        ConstError.ERR_VALIDATION_CLIENT_NOT_ISSUED_BY_API);
            }
            if (!hasRequiredProtocolMappers(clientResource)) {
                throw new ValidateException(
                        ConstError.ERRLOG_400_INVALID_REQUEST,
                        ConstError.ERR_VALIDATION_CLIENT_NOT_ISSUED_BY_API);
            }

            // remove() returns void; if no exception is thrown, consider it successful
            clientResource.remove();
        } catch (jakarta.ws.rs.NotFoundException e) {
            // The target client is not found in Keycloak
            throw new NotFoundException(
                   String.format(ConstError.ERR_404_RESOURCE_NOT_FOUND_MESSAGE, Const.JSON_PROPERTY_CLIENT_UUID));
        } catch (ValidateException e) {
            // Keep validation failures as 400; do not wrap into 500.
            throw e;
        } catch (Exception e) {
            throw new UnexpectedException(
                    String.format(ConstError.ERRLOG_500_KEYCLOAK_FAILED_WITH_MESSAGE, e.getMessage()),
                    ConstError.ERR_500_MESSAGE, e);
        }
    }

    /**
     * Updates a confidential client in the given realm by UUID.
     *
     * @param idpRealm the Keycloak realm
     * @param clientUuid the internal client UUID to update
     * @param name new display name (optional)
     * @param description new description (optional)
     * @param flowType OAuth2 flow type: {@code authorization_code} or {@code client_credentials}
     * @param openSystemId claim value for {@code open_system_id} (required only when switching to client_credentials)
     * @param operatorId claim value for {@code operator_id} (optional)
     * @param redirectUris redirect URIs (required only when switching to authorization_code)
     * @return updated client info
     */
    @Override
    public IdpClientInfo updateClient(
            @NonNull String idpRealm,
            @NonNull String clientUuid,
            String name,
            String description,
            @NonNull String flowType,
            String openSystemId,
            String operatorId,
            List<String> redirectUris) {

        try (Keycloak keycloak = build()) {
            ClientResource clientResource;
            ClientRepresentation existing;
            try {
                clientResource = keycloak.realm(idpRealm).clients().get(clientUuid);
                existing = clientResource.toRepresentation();
            } catch (jakarta.ws.rs.NotFoundException e) {
                throw new NotFoundException(
                        String.format(ConstError.ERR_404_RESOURCE_NOT_FOUND_MESSAGE, Const.KEYCLOAK_PROPERTY_CLIENT_UUID));
            }

            if (!isAuthorizationCodeFlow(existing) && !isClientCredentialsFlow(existing)) {
                throw new ValidateException(
                        ConstError.ERRLOG_400_INVALID_REQUEST,
                        ConstError.ERR_VALIDATION_CLIENT_NOT_ISSUED_BY_API);
            }
            if (!hasRequiredProtocolMappers(clientResource)) {
                throw new ValidateException(
                        ConstError.ERRLOG_400_INVALID_REQUEST,
                        ConstError.ERR_VALIDATION_CLIENT_NOT_ISSUED_BY_API);
            }

            boolean needUpdate = false;
            if (name != null && !name.equals(existing.getName())) {
                existing.setName(name);
                needUpdate = true;
            }
            if (description != null && !description.equals(existing.getDescription())) {
                existing.setDescription(description);
                needUpdate = true;
            }

            String normalizedOpenSystemId = StringUtils.hasText(openSystemId) ? openSystemId : null;
            String normalizedOperatorId = StringUtils.hasText(operatorId) ? operatorId : null;

            // Apply flow settings, persist client representation, and sync protocol mappers.
            // Keep the flow-specific logic within a single switch to make the full behavior easy to follow.
            switch (flowType) {
                case Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_AUTHORIZATION_CODE:
                {
                    // Require redirectUris only when switching flow to authorization_code.
                    if (!isAuthorizationCodeFlow(existing)
                            && (redirectUris == null || redirectUris.isEmpty())) {
                        throw new ValidateException(
                                ConstError.ERRLOG_400_INVALID_REQUEST,
                                ConstError.ERR_400_VALIDATION_REDIRECT_URIS_REQUIRED);
                    }

                    needUpdate |= applyAuthorizationCodeFlowSettingsForUpdate(existing, redirectUris);
                    if (needUpdate) {
                        clientResource.update(existing);
                    }

                    syncProtocolMappersForAuthorizationCode(clientResource);
                    break;
                }
                case Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_CLIENT_CREDENTIALS:
                {
                    // Require openSystemId only when switching flow to client_credentials.
                    if (!isClientCredentialsFlow(existing)
                            && normalizedOpenSystemId == null) {
                        throw new ValidateException(
                                ConstError.ERRLOG_400_INVALID_REQUEST,
                                ConstError.ERR_VALIDATION_OPEN_SYSTEM_ID_REQUIRED);
                    }

                    needUpdate |= applyClientCredentialsFlowSettingsForUpdate(existing);
                    if (needUpdate) {
                        clientResource.update(existing);
                    }

                    syncProtocolMappersForClientCredentials(clientResource, normalizedOperatorId, normalizedOpenSystemId);
                    break;
                }
                default:
                    throw new ValidateException(
                            ConstError.ERRLOG_400_INVALID_REQUEST,
                            ConstError.ERR_VALIDATION_CLIENT_NOT_ISSUED_BY_API);
            }

            ClientRepresentation updated = clientResource.toRepresentation();
            return toDto(
                    clientUuid,
                    updated.getClientId(),
                    updated.getName(),
                    updated.getDescription(),
                    updated.getRedirectUris(),
                    Boolean.TRUE.equals(updated.isEnabled()));
        } catch (BadRequestException e) {
            LOGGER.debug("Bad request error occurred while updating client, there may be an issue with the Keycloak realm configuration: {}", e.getMessage(), e);
            throw new BadParametersException(
                String.format(ConstError.ERRLOG_400_INVALID_REQUEST, e.getMessage()), ConstError.ERR_400_ILLEGAL_ARGUMENT, e);
        } catch (ValidateException e) {
            // Keep validation failures as 400; do not wrap into 500.
            throw e;
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.debug("Unexpected error occurred while updating client: {}", e.getMessage(), e);
            throw new UnexpectedException(ConstError.ERRLOG_500_KEYCLOAK_BUILD, ConstError.ERR_500_MESSAGE, e);
        }
    }

    /**
     * Revokes a token in Keycloak.
     *
     * @param idpRealm the Keycloak realm
     * @param clientId the client ID
     * @param clientSecret the client secret
     * @param refreshToken the refresh token to revoke
     * @return a TokenRevokeResult indicating the result of the revocation
     */
    @Override
    public TokenRevokeResult revoke(@NonNull String idpRealm, @NonNull String clientId, @NonNull String clientSecret, @NonNull String refreshToken) {
        // Make request URL
        String url = keycloakUtil.getTokenRevoke(idpRealm);

        // Make request header
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", Const.CONTENT_TYPE_FORM);

        // Make request body
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(Const.KEYCLOAK_SEND_PARAM_CLIENT_ID, clientId);
        params.add(Const.KEYCLOAK_SEND_PARAM_CLIENT_SECRET, clientSecret);
        params.add(Const.KEYCLOAK_SEND_PARAM_TOKEN, refreshToken);
        params.add(Const.KEYCLOAK_SEND_PARAM_TOKEN_TYPE_HINT, Const.KEYCLOAK_SEND_PARAM_REFRESH_TOKEN);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> responseEntity;
        try {
            // Send request and get response
            responseEntity = restTemplate.postForEntity(url, request, String.class);
        } catch (HttpClientErrorException.BadRequest e) {
            LOGGER.debug("Bad request while revoke: {}", e.getMessage(), e);
            throw new BadParametersException(
                String.format(ConstError.ERRLOG_400_INVALID_REQUEST, e.getMessage()), ConstError.ERR_400_ILLEGAL_ARGUMENT, e);
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            // Keycloak is unavailable
            LOGGER.debug("Keycloak revoke request failed due to service unavailability: {}", e.getMessage(), e);
            throw new OutOfServiceException(ConstError.ERR_503_OUT_OF_SERVICE_KEYCLOAK, e);
        } catch (HttpClientErrorException.Unauthorized e) {
            LOGGER.debug("Received HTTP 401 from Keycloak token revocation endpoint.: {}", e.getMessage(), e);
            throw new UnauthorizedException(
                String.format(ConstError.ERRLOG_401_INVALID_CLIENT, e.getMessage()), ConstError.ERR_401_INVALID_CLIENT, e);
        } catch (Exception e) {
            LOGGER.debug("Unexpected error occurred during Keycloak revoke request: {}", e.getMessage(), e);
            throw new UnexpectedException(ConstError.ERRLOG_500_KEYCLOAK_BUILD, ConstError.ERR_500_MESSAGE, e);
        }

        // Check response status
        String response = responseEntity.getBody();
        if (!(responseEntity.getStatusCode().value() == HttpStatus.SC_OK && (response == null || response.isEmpty()))) {
            // Keycloak's token revocation endpoint always returns HTTP 200,
            // even when the token is already invalid or the request itself is incorrect.
            // In such cases, the error is provided only in the JSON response body.
            //
            // This wrapper converts those body-level errors into HTTP 400
            // to provide clearer failure signals and consistent client-side handling.
            LOGGER.debug("Keycloak responded with HTTP 200, but the token is reported as invalid in the response body. Converting to HTTP 400.: {}", null, null);
            throw new BadParametersException(
                ConstError.ERRLOG_400_INVALID_REQUEST,
                ConstError.ERR_400_ILLEGAL_ARGUMENT
            );
        }
        // Return empty result as Keycloak does not return a body
        TokenRevokeResult tokenRevokeResult = new TokenRevokeResult();
        return tokenRevokeResult;
    }

    /**
     * Applies authorization_code flow-specific settings during update.
     *
     * <p>This is an "update" variant of {@link #applyFlowSettings(ClientRepresentation, String, List)}.
     * When redirectUris is supplied, we align related client settings (webOrigins, standard flow,
     * consent, PKCE attributes, etc.) to match authorization_code expectations.</p>
     *
     * @param client existing client representation to mutate
     * @param redirectUris redirect URIs supplied by the update request
     * @return true if any property was changed
     */
    private boolean applyAuthorizationCodeFlowSettingsForUpdate(ClientRepresentation client, List<String> redirectUris) {
        boolean changed = false;

        List<String> desiredWebOrigins = List.of(Const.CORS_ALL_ENABLED);
        if (client.getWebOrigins() == null || !client.getWebOrigins().equals(desiredWebOrigins)) {
            client.setWebOrigins(desiredWebOrigins);
            changed = true;
        }
        if (Boolean.FALSE.equals(client.isStandardFlowEnabled())) {
            client.setStandardFlowEnabled(true);
            changed = true;
        }
        if (Boolean.TRUE.equals(client.isServiceAccountsEnabled())) {
            client.setServiceAccountsEnabled(false);
            changed = true;
        }
        if (Boolean.TRUE.equals(client.isDirectAccessGrantsEnabled())) {
            client.setDirectAccessGrantsEnabled(false);
            changed = true;
        }
        if (Boolean.TRUE.equals(client.getAuthorizationServicesEnabled())) {
            client.setAuthorizationServicesEnabled(false);
            changed = true;
        }
        if (redirectUris != null) {
            if (client.getRedirectUris() == null || !client.getRedirectUris().equals(redirectUris)) {
                client.setRedirectUris(redirectUris);
                changed = true;
            }
        }
        if (Boolean.FALSE.equals(client.isConsentRequired())) {
            client.setConsentRequired(true);
            changed = true;
        }

        Map<String, String> attrs = client.getAttributes();
        if (attrs == null) {
            client.setAttributes(new HashMap<>());
            attrs = client.getAttributes();
        }
        if (!Const.KEYCLOAK_MAPPER_CONFIG_TRUE.equals(attrs.get(Const.KEYCLOAK_MAPPER_CONFIG_PKCE_ENABLED))) {
            attrs.put(Const.KEYCLOAK_MAPPER_CONFIG_PKCE_ENABLED, Const.KEYCLOAK_MAPPER_CONFIG_TRUE);
            changed = true;
        }
        if (!Const.KEYCLOAK_MAPPER_CONFIG_S256.equals(attrs.get(Const.KEYCLOAK_MAPPER_CONFIG_PKCE_CODE_CHALLENGE_METHOD))) {
            attrs.put(Const.KEYCLOAK_MAPPER_CONFIG_PKCE_CODE_CHALLENGE_METHOD, Const.KEYCLOAK_MAPPER_CONFIG_S256);
            changed = true;
        }

        return changed;
    }

    /**
     * Determines whether the given Keycloak client representation is configured for the
     * {@code authorization_code} flow.
     *
     * <p>In this project we treat authorization_code as: standard flow enabled AND service accounts
     * disabled.</p>
     *
     * @param client Keycloak client representation (non-null)
     * @return true if the client matches the authorization_code flow configuration
     */
    private boolean isAuthorizationCodeFlow(ClientRepresentation client) {
        return Boolean.TRUE.equals(client.isStandardFlowEnabled())
                && Boolean.FALSE.equals(client.isServiceAccountsEnabled());
    }

    /**
     * Determines whether the given Keycloak client representation is configured for the
     * {@code client_credentials} flow.
     *
     * <p>In this project we treat client_credentials as: standard flow disabled AND service accounts
     * enabled.</p>
     *
     * @param client Keycloak client representation (non-null)
     * @return true if the client matches the client_credentials flow configuration
     */
    private boolean isClientCredentialsFlow(ClientRepresentation client) {
        return Boolean.FALSE.equals(client.isStandardFlowEnabled())
                && Boolean.TRUE.equals(client.isServiceAccountsEnabled());
    }

    /**
     * Checks whether the given client already has at least one of the protocol mapper types
     * required by this service.
     *
     * @param clientResource Keycloak client resource
     * @return true if the required protocol mapper type exists; otherwise false
     */
    private boolean hasRequiredProtocolMappers(ClientResource clientResource) {
        ProtocolMappersResource protocolMappers = clientResource.getProtocolMappers();
        List<ProtocolMapperRepresentation> mappers = protocolMappers.getMappers();
        if (mappers.isEmpty()) {
            return false;
        }

        for (ProtocolMapperRepresentation mapper : mappers) {
            String mapperType = mapper.getProtocolMapper();
            if (Const.KEYCLOAK_MAPPER_OIDC_USERMODEL_PROPERTY.equals(mapperType)
                    || Const.KEYCLOAK_MAPPER_OIDC_HARDCODED_CLAIM.equals(mapperType)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Applies client_credentials flow-specific settings during update.
     *
     * <p>This is an "update" variant of {@link #applyFlowSettings(ClientRepresentation, String, List)}.
     * It clears authorization_code-specific settings such as redirect URIs, consent, and PKCE attributes,
     * and enables service accounts.</p>
     */
    private boolean applyClientCredentialsFlowSettingsForUpdate(ClientRepresentation client) {
        boolean changed = false;

        if (client.getWebOrigins() != null) {
            client.setWebOrigins(null);
            changed = true;
        }
        if (Boolean.TRUE.equals(client.isStandardFlowEnabled())) {
            client.setStandardFlowEnabled(false);
            changed = true;
        }
        if (Boolean.FALSE.equals(client.isServiceAccountsEnabled())) {
            client.setServiceAccountsEnabled(true);
            changed = true;
        }
        if (Boolean.TRUE.equals(client.isDirectAccessGrantsEnabled())) {
            client.setDirectAccessGrantsEnabled(false);
            changed = true;
        }
        if (Boolean.TRUE.equals(client.getAuthorizationServicesEnabled())) {
            client.setAuthorizationServicesEnabled(false);
            changed = true;
        }
        if (client.getRedirectUris() != null) {
            client.setRedirectUris(null);
            changed = true;
        }
        if (Boolean.TRUE.equals(client.isConsentRequired())) {
            client.setConsentRequired(false);
            changed = true;
        }

        Map<String, String> attrs = client.getAttributes();
        if (attrs != null) {
            if (attrs.remove(Const.KEYCLOAK_MAPPER_CONFIG_PKCE_ENABLED) != null) {
                changed = true;
            }
            if (attrs.remove(Const.KEYCLOAK_MAPPER_CONFIG_PKCE_CODE_CHALLENGE_METHOD) != null) {
                changed = true;
            }
        }

        return changed;
    }

    /**
     * Update variant of for authorization_code.
     *
     * <p>Ensures required mappers exist and removes hardcoded claim mappers that would conflict with
     * the authorization_code flow token shape.</p>
     *
     * @param clientResource target client
     */
    private void syncProtocolMappersForAuthorizationCode(ClientResource clientResource) {
        ProtocolMappersResource protocolMappers = clientResource.getProtocolMappers();
        List<ProtocolMapperRepresentation> existing = protocolMappers.getMappers();

        boolean hasOperatorUserModelMapper = false;
        for (ProtocolMapperRepresentation mapper : existing) {
            String mapperName = mapper.getName();
            String mapperType = mapper.getProtocolMapper();

            if (Const.KEYCLOAK_PROPERTY_OPERATOR_ID.equals(mapperName)
                    && Const.KEYCLOAK_MAPPER_OIDC_USERMODEL_PROPERTY.equals(mapperType)) {
                hasOperatorUserModelMapper = true;
                continue;
            }

            if (Const.KEYCLOAK_MAPPER_OIDC_HARDCODED_CLAIM.equals(mapperType)
                    && isOperatorOrOpenSystemId(mapperName)) {
                protocolMappers.delete(mapper.getId());
            }
        }

        if (!hasOperatorUserModelMapper) {
            protocolMappers.createMapper(buildOperatorIdUserModelMapper()).close();
        }
    }

    /**
     * Update variant of for client_credentials.
     *
     * <p>Ensures hardcoded claim mappers exist for operator_id/open_system_id and removes the
     * authorization_code specific operator_id user model mapper.</p>
     */
    private void syncProtocolMappersForClientCredentials(ClientResource clientResource, String operatorId, String openSystemId) {
        ProtocolMappersResource protocolMappers = clientResource.getProtocolMappers();
        List<ProtocolMapperRepresentation> existing = protocolMappers.getMappers();

        boolean updateOpenSystemId = StringUtils.hasText(openSystemId);
        boolean updateOperatorId = StringUtils.hasText(operatorId);

        for (ProtocolMapperRepresentation mapper : existing) {
            String mapperName = mapper.getName();
            String mapperType = mapper.getProtocolMapper();

            if (Const.KEYCLOAK_MAPPER_OIDC_USERMODEL_PROPERTY.equals(mapperType)
                    && Const.KEYCLOAK_PROPERTY_OPERATOR_ID.equals(mapperName)) {
                protocolMappers.delete(mapper.getId());
                continue;
            }

            if (Const.KEYCLOAK_MAPPER_OIDC_HARDCODED_CLAIM.equals(mapperType)
                    && isOperatorOrOpenSystemId(mapperName)) {
                // Only update the mapper when the corresponding value is explicitly supplied.
                if (Const.KEYCLOAK_PROPERTY_OPEN_SYSTEM_ID.equals(mapperName)) {
                    if (updateOpenSystemId) {
                        protocolMappers.delete(mapper.getId());
                    }
                } else {
                    if (updateOperatorId) {
                        protocolMappers.delete(mapper.getId());
                    }
                }
            }
        }

        if (updateOpenSystemId) {
            ProtocolMapperRepresentation openSystemMapper = buildHardcodedClaimMapperBase();
            openSystemMapper.setName(Const.KEYCLOAK_PROPERTY_OPEN_SYSTEM_ID);
            openSystemMapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_CLAIM_NAME, Const.KEYCLOAK_PROPERTY_OPEN_SYSTEM_ID);
            openSystemMapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_CLAIM_VALUE, openSystemId);
            protocolMappers.createMapper(openSystemMapper).close();
        }

        if (updateOperatorId) {
            ProtocolMapperRepresentation operatorMapper = buildHardcodedClaimMapperBase();
            operatorMapper.setName(Const.KEYCLOAK_PROPERTY_OPERATOR_ID);
            operatorMapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_CLAIM_NAME, Const.KEYCLOAK_PROPERTY_OPERATOR_ID);
            operatorMapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_CLAIM_VALUE, operatorId);
            protocolMappers.createMapper(operatorMapper).close();
        }
    }

    /**
     * Checks whether the given mapper name is one of the claim mappers managed by this service.
     *
     * @param mapperName protocol mapper name
     * @return true if the mapper name is {@code operator_id} or {@code open_system_id}
     */
    private boolean isOperatorOrOpenSystemId(String mapperName) {
        return Const.KEYCLOAK_PROPERTY_OPERATOR_ID.equals(mapperName) || Const.KEYCLOAK_PROPERTY_OPEN_SYSTEM_ID.equals(mapperName);
    }

    /**
     * Builds a protocol mapper representation for {@code operator_id} using Keycloak's user model
     * property mapper.
     *
     * @return protocol mapper representation to be created in Keycloak
     */
    private ProtocolMapperRepresentation buildOperatorIdUserModelMapper() {
        ProtocolMapperRepresentation operatorMapper = new ProtocolMapperRepresentation();
        operatorMapper.setName(Const.KEYCLOAK_PROPERTY_OPERATOR_ID);
        operatorMapper.setProtocol(Const.KEYCLOAK_PROTOCOL_OPENID_CONNECT);
        operatorMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_USERMODEL_PROPERTY);
        operatorMapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_USER_ATTRIBUTE, Const.KEYCLOAK_MAPPER_CONFIG_ID);
        operatorMapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_CLAIM_NAME, Const.KEYCLOAK_PROPERTY_OPERATOR_ID);
        operatorMapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_JSON_TYPE_LABEL, Const.KEYCLOAK_MAPPER_JSON_TYPE_STRING);
        operatorMapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_ID_TOKEN_CLAIM, Const.KEYCLOAK_MAPPER_CONFIG_TRUE);
        operatorMapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_ACCESS_TOKEN_CLAIM, Const.KEYCLOAK_MAPPER_CONFIG_TRUE);
        operatorMapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_USERINFO_TOKEN_CLAIM, Const.KEYCLOAK_MAPPER_CONFIG_TRUE);
        operatorMapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_INTROSPECTION_TOKEN_CLAIM, Const.KEYCLOAK_MAPPER_CONFIG_TRUE);
        operatorMapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_ACCESS_TOKEN_RESPONSE_CLAIM, Const.KEYCLOAK_MAPPER_CONFIG_FALSE);
        return operatorMapper;
    }

    /**
     * Ensures the clientId is not already registered in the target realm.
     *
     * @param kc Keycloak admin client
     * @param realm target realm
     * @param clientId unique client identifier to check
     * @throws ConflictException when a client with the same clientId already exists
     */
    private void validateDuplicate(Keycloak kc, String realm, String clientId) {
        List<ClientRepresentation> existing = kc.realm(realm).clients().findByClientId(clientId);
        if (existing != null && !existing.isEmpty()) {
            throw new ConflictException(String.format(
                    ConstError.ERR_409_CONFLICT, Const.JSON_PROPERTY_CLIENT_ID, clientId));
        }
    }

    /**
     * Builds a base ClientRepresentation with common fields set.
     *
     * @param clientId client identifier
     * @param name display name
     * @param description client description
     * @return a base representation (protocol, enabled, confidential etc.)
     */
    private ClientRepresentation buildBaseClient(String clientId, String name, String description) {
        ClientRepresentation client = new ClientRepresentation();
        client.setClientId(clientId);
        client.setName(name);
        client.setDescription(description);
        client.setProtocol(Const.KEYCLOAK_PROTOCOL_OPENID_CONNECT);
        client.setEnabled(true);
        client.setPublicClient(false);
        client.setClientAuthenticatorType(Const.KEYCLOAK_CLIENT_AUTHENTICATOR_CLIENT_SECRET);
        return client;
    }

    /**
     * Applies flow-specific settings to the client representation.
     * For authorization_code: enables standard flow, sets redirect URIs, consent and PKCE(S256).
     * For client_credentials: enables service accounts and clears redirect URIs.
     *
     * @param client client representation to mutate
     * @param flowType "authorization_code" or "client_credentials"
     * @param redirectUris list of redirect URIs (auth_code only)
     */
    private void applyFlowSettings(ClientRepresentation client, String flowType, List<String> redirectUris) {
        if (Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_AUTHORIZATION_CODE.equals(flowType)) {
            client.setWebOrigins(List.of(Const.CORS_ALL_ENABLED));
            client.setStandardFlowEnabled(true);
            client.setServiceAccountsEnabled(false);
            client.setDirectAccessGrantsEnabled(false);
            client.setAuthorizationServicesEnabled(false);
            client.setRedirectUris(redirectUris);
            client.setConsentRequired(true);
            java.util.Map<String, String> attrs = client.getAttributes();
            if (attrs == null) {
                client.setAttributes(new HashMap<>());
                attrs = client.getAttributes();
            }
            attrs.put("pkceEnabled", "true");
            attrs.put("pkce.code.challenge.method", "S256");
        } else if (Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_CLIENT_CREDENTIALS.equals(flowType)) {
            client.setWebOrigins(null);
            client.setStandardFlowEnabled(false);
            client.setServiceAccountsEnabled(true);
            client.setDirectAccessGrantsEnabled(false);
            client.setAuthorizationServicesEnabled(false);
            client.setRedirectUris(null);
        }
    }

    /**
     * Creates the client in Keycloak and validates the response status.
     *
     * @param kc Keycloak admin client
     * @param realm target realm
     * @param client prepared client representation to create
     * @return newly created internal client UUID
     * @throws ConflictException when creation returns conflict
     * @throws UnexpectedException for any non-created status
     */
    private String createClient(Keycloak kc, String realm, ClientRepresentation client) {
        Response res = kc.realm(realm).clients().create(client);
        if (res.getStatus() == HttpStatus.SC_CONFLICT) {
            res.close();
            throw new ConflictException(String.format(
                    ConstError.ERR_409_CONFLICT, Const.JSON_PROPERTY_CLIENT_ID, client.getClientId()));
        }
        if (res.getStatus() == HttpStatus.SC_BAD_REQUEST) {
            res.close();
            throw new BadParametersException(
                String.format(ConstError.ERRLOG_400_INVALID_REQUEST, "Invalid client configuration: " + client.getClientId()),
                ConstError.ERR_400_ILLEGAL_ARGUMENT
            );
        }
        if (res.getStatus() != HttpStatus.SC_CREATED) {
            res.close();
            throw new UnexpectedException(
                String.format(ConstError.ERRLOG_500_KEYCLOAK_FAILED, res.getStatus()),
                ConstError.ERR_500_MESSAGE
            );
        }
        String location = res.getHeaderString(Const.HEADER_LOCATION);
        String uuid = location.substring(location.lastIndexOf('/') + 1);
        res.close();
        return uuid;
    }

    /**
     * Adds protocol mappers required by the selected flow.
     * - authorization_code: adds operator_id from user internal property.
     * - client_credentials: adds hardcoded open_system_id and operator_id when provided.
     *
     * @param clientResource client resource to add protocol mappers to
     * @param flowType flow type determining which mappers to add
     * @param operatorId optional operator_id claim (hardcoded for client_credentials)
     * @param openSystemId optional open_system_id claim (hardcoded for client_credentials)
     */
    private void addProtocolMappers(ClientResource clientResource, String flowType, String operatorId, String openSystemId) {
        if (Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_AUTHORIZATION_CODE.equals(flowType)) {
            ProtocolMapperRepresentation operatorMapper = new ProtocolMapperRepresentation();
            operatorMapper.setName(Const.JSON_PROPERTY_OPERATOR_ID);
            operatorMapper.setProtocol(Const.KEYCLOAK_PROTOCOL_OPENID_CONNECT);
            operatorMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_USERMODEL_PROPERTY);
            operatorMapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_USER_ATTRIBUTE, "id");
            operatorMapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_CLAIM_NAME, Const.JSON_PROPERTY_OPERATOR_ID);
            operatorMapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_JSON_TYPE_LABEL, Const.KEYCLOAK_MAPPER_JSON_TYPE_STRING);
            operatorMapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_ID_TOKEN_CLAIM, Const.KEYCLOAK_MAPPER_CONFIG_TRUE);
            operatorMapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_ACCESS_TOKEN_CLAIM, Const.KEYCLOAK_MAPPER_CONFIG_TRUE);
            operatorMapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_USERINFO_TOKEN_CLAIM, Const.KEYCLOAK_MAPPER_CONFIG_TRUE);
            operatorMapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_INTROSPECTION_TOKEN_CLAIM, Const.KEYCLOAK_MAPPER_CONFIG_TRUE);
            operatorMapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_ACCESS_TOKEN_RESPONSE_CLAIM, Const.KEYCLOAK_MAPPER_CONFIG_FALSE);
            clientResource.getProtocolMappers().createMapper(operatorMapper).close();
        }

        if (Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_CLIENT_CREDENTIALS.equals(flowType)) {
            // Prepare common base for hardcoded claim mappers once (used in client_credentials)
            ProtocolMapperRepresentation hardcodedBase = buildHardcodedClaimMapperBase();
            // When openSystemId is present, add open_system_id hardcoded claim
            if (StringUtils.hasText(openSystemId)) {
                hardcodedBase.setName(Const.JSON_PROPERTY_OPEN_SYSTEM_ID);
                hardcodedBase.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_CLAIM_NAME, Const.JSON_PROPERTY_OPEN_SYSTEM_ID);
                hardcodedBase.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_CLAIM_VALUE, openSystemId);
                clientResource.getProtocolMappers().createMapper(hardcodedBase).close();
            }

            // When operatorId is present, add operator_id hardcoded claim
            if (StringUtils.hasText(operatorId)) {
                hardcodedBase.setName(Const.JSON_PROPERTY_OPERATOR_ID);
                hardcodedBase.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_CLAIM_NAME, Const.JSON_PROPERTY_OPERATOR_ID);
                hardcodedBase.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_CLAIM_VALUE, operatorId);
                clientResource.getProtocolMappers().createMapper(hardcodedBase).close();
            }
        }
    }

    /**
     * Builds a base ProtocolMapperRepresentation for OIDC hardcoded claim mappers
     * with common token inclusion flags preset.
     *
     * @return a mapper with common OIDC settings and flags applied
     */
    private ProtocolMapperRepresentation buildHardcodedClaimMapperBase() {
        ProtocolMapperRepresentation mapper = new ProtocolMapperRepresentation();
        mapper.setProtocol(Const.KEYCLOAK_PROTOCOL_OPENID_CONNECT);
        mapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_HARDCODED_CLAIM);

        // Common flags: include in ID, access, userinfo, introspection; exclude from access token response
        mapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_JSON_TYPE_LABEL, Const.KEYCLOAK_MAPPER_JSON_TYPE_STRING);
        mapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_ID_TOKEN_CLAIM, Const.KEYCLOAK_MAPPER_CONFIG_TRUE);
        mapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_ACCESS_TOKEN_CLAIM, Const.KEYCLOAK_MAPPER_CONFIG_TRUE);
        mapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_USERINFO_TOKEN_CLAIM, Const.KEYCLOAK_MAPPER_CONFIG_TRUE);
        mapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_INTROSPECTION_TOKEN_CLAIM, Const.KEYCLOAK_MAPPER_CONFIG_TRUE);
        mapper.getConfig().put(Const.KEYCLOAK_MAPPER_CONFIG_ACCESS_TOKEN_RESPONSE_CLAIM, Const.KEYCLOAK_MAPPER_CONFIG_FALSE);

        return mapper;
    }

    /**
     * Builds the DTO returned to callers after client creation.
     *
     * @param uuid internal client UUID
     * @param clientId registered client identifier
     * @param name display name
     * @param description description
     * @param redirectUris redirect URIs (auth_code only)
     * @param enabled current enabled flag in Keycloak
     * @return IdpClientInfo DTO without the client secret
     */
    private IdpClientInfo toDto(String uuid, String clientId, String name, String description, List<String> redirectUris, boolean enabled) {
        return new IdpClientInfo(
                uuid,
                clientId,
                name,
                description,
                redirectUris == null ? null : new java.util.ArrayList<>(redirectUris),
                null,
                false,
                enabled
        );
    }
}
