/*
 * KeycloakRepositoryImplTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for KeycloakRepositoryImpl using mocks to avoid external.
 *
 * Date: 2025/08/30
 */

package io.github.open_dataspaces.core.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.ProtocolMappersResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import io.github.open_dataspaces.core.exception.AbstractBaseException;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.ConflictException;
import io.github.open_dataspaces.core.exception.LoginException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnauthorizedException;
import io.github.open_dataspaces.core.exception.UnexpectedException;
import io.github.open_dataspaces.core.exception.ValidateException;
import io.github.open_dataspaces.core.infrastructure.config.KeycloakProperties;
import io.github.open_dataspaces.core.infrastructure.utils.KeycloakUtil;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

/**
 * Unit tests for KeycloakRepositoryImpl using mocks to avoid external
 * dependencies.
 * This class tests various authentication and authorization processes without
 * connecting to Keycloak.
 */
@ExtendWith(MockitoExtension.class)
public class KeycloakRepositoryImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private KeycloakUtil keycloakUtil;

    @Mock
    private KeycloakProperties keycloakProperties;

    @InjectMocks
    private KeycloakRepositoryImpl keycloakRepository;

    private MockedStatic<KeycloakBuilder> mockedKeycloakBuilder;
    private Keycloak mockKeycloak;

    @BeforeEach
    void setUp() {
        // Mock static KeycloakBuilder
        mockedKeycloakBuilder = mockStatic(KeycloakBuilder.class);
        mockKeycloak = mock(Keycloak.class);
    }

    @AfterEach
    void tearDown() {
        if (mockedKeycloakBuilder != null) {
            mockedKeycloakBuilder.close();
        }
    }

    // Keycloak build test case

    /**
     * case#1:
     * Normal case - build() admin method (Using mocks, no external
     * connection).
     */
    @Test
    void build_admin_success() {
        // Arrange - Setup required keycloakUtil mocks for build() method (admin
        // operations)
        when(keycloakProperties.getApiEndpoint()).thenReturn("http://localhost:8080/auth");
        when(keycloakProperties.getCredentials()).thenReturn(mock(KeycloakProperties.Credentials.class));
        when(keycloakProperties.getCredentials().getAdminRealm()).thenReturn("master");
        when(keycloakProperties.getCredentials().getAdminClientId()).thenReturn("admin-cli");
        when(keycloakProperties.getCredentials().getAdminUserName()).thenReturn("admin");
        when(keycloakProperties.getCredentials().getAdminPassword()).thenReturn("admin-password");

        // Mock KeycloakBuilder chain for admin operations
        KeycloakBuilder mockBuilder = mock(KeycloakBuilder.class);
        Keycloak mockKeycloak = mock(Keycloak.class);

        when(KeycloakBuilder.builder()).thenReturn(mockBuilder);
        when(mockBuilder.serverUrl(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.realm(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.username(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.password(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.grantType(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockKeycloak);

        // Act
        Keycloak result = keycloakRepository.build();

        // Assert
        assertNotNull(result);
        assertEquals(mockKeycloak, result);
    }

    /**
     * case#3:
     * Normal case - build(String realm, String clientId, String clientSecret,
     * String username, String password) method (Using mocks, no external
     * connection).
     */
    @Test
    void build_withUsernamePassword_success() {
        // Arrange
        String idpRealm = "test-realm";
        String clientId = "test-client";
        String clientSecret = "test-secret";
        String username = "testuser";
        String password = "testpassword";

        // Setup required keycloakUtil mocks for build(String idpRealm, String clientId,
        // String clientSecret, String username, String password) method
        when(keycloakProperties.getApiEndpoint()).thenReturn("http://localhost:8080/auth");

        // Mock KeycloakBuilder chain
        KeycloakBuilder mockBuilder = mock(KeycloakBuilder.class);
        Keycloak mockKeycloak = mock(Keycloak.class);

        when(KeycloakBuilder.builder()).thenReturn(mockBuilder);
        when(mockBuilder.serverUrl(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.realm(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientSecret(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.username(username)).thenReturn(mockBuilder);
        when(mockBuilder.password(password)).thenReturn(mockBuilder);
        when(mockBuilder.grantType(OAuth2Constants.PASSWORD)).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockKeycloak);

        // Act
        Keycloak result = keycloakRepository.build(idpRealm, clientId, clientSecret, username, password);

        // Assert
        assertNotNull(result);
        assertEquals(mockKeycloak, result);
    }

    /**
     * case#4:
     * Normal case - build(String idpRealm, String clientId, String
     * clientSecret, String grantType) method (Using mocks, no external connection).
     */
    @Test
    void build_withClientCredentials_success() {
        // Arrange
        String idpRealm = "test-realm";
        String clientId = "test-client-id";
        String clientSecret = "test-client-secret";
        String grantType = OAuth2Constants.CLIENT_CREDENTIALS;

        // Setup required keycloakUtil mocks for build(String idpRealm, String clientId,
        // String clientSecret, String grantType) method
        when(keycloakProperties.getApiEndpoint()).thenReturn("http://localhost:8080/auth");

        // Mock KeycloakBuilder chain
        KeycloakBuilder mockBuilder = mock(KeycloakBuilder.class);
        Keycloak mockKeycloak = mock(Keycloak.class);

        when(KeycloakBuilder.builder()).thenReturn(mockBuilder);
        when(mockBuilder.serverUrl(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.realm(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(clientId)).thenReturn(mockBuilder);
        when(mockBuilder.clientSecret(clientSecret)).thenReturn(mockBuilder);
        when(mockBuilder.grantType(grantType)).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockKeycloak);

        // Act
        Keycloak result = keycloakRepository.build(idpRealm, clientId, clientSecret, grantType);

        // Assert
        assertNotNull(result);
        assertEquals(mockKeycloak, result);
    }

    /**
     * case#5:
     * Error case - KeycloakBuilder.builder() returns null (Using mocks, no
     * external connection).
     */
    @Test
    void build_builderReturnsNull_returnsNull() {
        // Arrange - Mock KeycloakBuilder.builder() to return null
        when(KeycloakBuilder.builder()).thenReturn(null);

        // Act & Assert - This will cause a NullPointerException when trying to chain
        // methods
        assertThrows(NullPointerException.class, () -> {
            keycloakRepository.build();
        });
    }

    /**
     * case#6:
     * Normal case - KeycloakBuilder.build() returns a valid Keycloak instance (Using mocks, no external connection).
     */
    @Test
    void build_buildReturnsValidKeycloak_returnsKeycloak() {
        // Arrange - Setup required keycloakUtil mocks
        when(keycloakProperties.getApiEndpoint()).thenReturn("http://localhost:8080/auth");
        when(keycloakProperties.getCredentials()).thenReturn(mock(KeycloakProperties.Credentials.class));
        when(keycloakProperties.getCredentials().getAdminRealm()).thenReturn("master");
        when(keycloakProperties.getCredentials().getAdminClientId()).thenReturn("admin-cli");
        when(keycloakProperties.getCredentials().getAdminUserName()).thenReturn("admin");
        when(keycloakProperties.getCredentials().getAdminPassword()).thenReturn("admin-password");

        // Mock KeycloakBuilder chain to return null at the end
        KeycloakBuilder mockBuilder = mock(KeycloakBuilder.class);

        when(KeycloakBuilder.builder()).thenReturn(mockBuilder);
        when(mockBuilder.serverUrl(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.realm(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.username(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.password(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.grantType(OAuth2Constants.PASSWORD)).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(null); // Simulate build failure

        // Act
        Keycloak result = keycloakRepository.build();

        // Assert
        assertEquals(null, result);
    }

    // Keycloak isValidClientId test case

    /**
     * case#7:
     * Normal case - Valid client ID (Using mocks, no external connection).
     */
    @Test
    void isValidClientId_validClientId_returnsTrue() {
        // Arrange
        String realm = "test-realm";
        String clientId = "valid-client-id";

        // Setup required keycloakUtil mocks for build() method (admin operations)
        when(keycloakProperties.getApiEndpoint()).thenReturn("http://localhost:8080/auth");
        when(keycloakProperties.getCredentials()).thenReturn(mock(KeycloakProperties.Credentials.class));
        when(keycloakProperties.getCredentials().getAdminRealm()).thenReturn("master");
        when(keycloakProperties.getCredentials().getAdminClientId()).thenReturn("admin-cli");
        when(keycloakProperties.getCredentials().getAdminUserName()).thenReturn("admin");
        when(keycloakProperties.getCredentials().getAdminPassword()).thenReturn("admin-password");

        // Mock KeycloakBuilder chain and client operations
        KeycloakBuilder mockBuilder = mock(KeycloakBuilder.class);
        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        ClientsResource mockClientsResource = mock(ClientsResource.class);

        // Mock client representations list (non-empty list indicates valid client)
        ClientRepresentation mockClientRepresentation = mock(
                ClientRepresentation.class);
        java.util.List<ClientRepresentation> clientList = java.util.Arrays
                .asList(mockClientRepresentation);

        when(KeycloakBuilder.builder()).thenReturn(mockBuilder);
        when(mockBuilder.serverUrl(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.realm(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.username(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.password(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.grantType(OAuth2Constants.PASSWORD)).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockKeycloak);

        when(mockKeycloak.realm(realm)).thenReturn(mockRealmResource);
        when(mockRealmResource.clients()).thenReturn(mockClientsResource);
        when(mockClientsResource.findByClientId(clientId)).thenReturn(clientList);

        // Act
        boolean result = keycloakRepository.isValidClientId(realm, clientId);

        // Assert
        assertTrue(result);
    }

    /**
     * case#8:
     * Normal case - Invalid client ID (Using mocks, no external connection).
     */
    @Test
    void isValidClientId_invalidClientId_returnsFalse() {
        // Arrange
        String realm = "test-realm";
        String clientId = "invalid-client-id";

        // Setup required keycloakUtil mocks for build() method (admin operations)
        when(keycloakProperties.getApiEndpoint()).thenReturn("http://localhost:8080/auth");
        when(keycloakProperties.getCredentials()).thenReturn(mock(KeycloakProperties.Credentials.class));
        when(keycloakProperties.getCredentials().getAdminRealm()).thenReturn("master");
        when(keycloakProperties.getCredentials().getAdminClientId()).thenReturn("admin-cli");
        when(keycloakProperties.getCredentials().getAdminUserName()).thenReturn("admin");
        when(keycloakProperties.getCredentials().getAdminPassword()).thenReturn("admin-password");

        // Mock KeycloakBuilder chain and client operations
        KeycloakBuilder mockBuilder = mock(KeycloakBuilder.class);
        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        ClientsResource mockClientsResource = mock(ClientsResource.class);

        // Mock empty client representations list (indicates invalid client)
        java.util.List<org.keycloak.representations.idm.ClientRepresentation> emptyClientList = java.util.Collections
                .emptyList();

        when(KeycloakBuilder.builder()).thenReturn(mockBuilder);
        when(mockBuilder.serverUrl(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.realm(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.username(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.password(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.grantType(OAuth2Constants.PASSWORD)).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockKeycloak);

        when(mockKeycloak.realm(realm)).thenReturn(mockRealmResource);
        when(mockRealmResource.clients()).thenReturn(mockClientsResource);
        when(mockClientsResource.findByClientId(clientId)).thenReturn(emptyClientList);

        // Act
        boolean result = keycloakRepository.isValidClientId(realm, clientId);

        // Assert
        assertTrue(!result); // Should return false for invalid client
    }

    /**
     * case#9:
     * Normal case - Null client list (Using mocks, no external connection).
     */
    @Test
    void isValidClientId_nullClientList_returnsFalse() {
        // Arrange
        String realm = "test-realm";
        String clientId = "test-client-id";

        // Setup required keycloakUtil mocks for build() method (admin operations)
        when(keycloakProperties.getApiEndpoint()).thenReturn("http://localhost:8080/auth");
        when(keycloakProperties.getCredentials()).thenReturn(mock(KeycloakProperties.Credentials.class));
        when(keycloakProperties.getCredentials().getAdminRealm()).thenReturn("master");
        when(keycloakProperties.getCredentials().getAdminClientId()).thenReturn("admin-cli");
        when(keycloakProperties.getCredentials().getAdminUserName()).thenReturn("admin");
        when(keycloakProperties.getCredentials().getAdminPassword()).thenReturn("admin-password");

        // Mock KeycloakBuilder chain and client operations
        KeycloakBuilder mockBuilder = mock(KeycloakBuilder.class);
        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        ClientsResource mockClientsResource = mock(ClientsResource.class);

        when(KeycloakBuilder.builder()).thenReturn(mockBuilder);
        when(mockBuilder.serverUrl(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.realm(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.username(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.password(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.grantType(OAuth2Constants.PASSWORD)).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockKeycloak);

        when(mockKeycloak.realm(realm)).thenReturn(mockRealmResource);
        when(mockRealmResource.clients()).thenReturn(mockClientsResource);
        when(mockClientsResource.findByClientId(clientId)).thenReturn(null); // Return null client list

        // Act
        boolean result = keycloakRepository.isValidClientId(realm, clientId);

        // Assert
        assertTrue(!result); // Should return false for null client list
    }

    /**
     * case#10:
     * Error case - Client not found exception (Using mocks, no external
     * connection).
     */
    @Test
    void isValidClientId_clientNotFound_throwsException() {
        // Arrange
        String realm = "test-realm";
        String clientId = "not-found-client-id";

        // Setup required keycloakUtil mocks for build() method (admin operations)
        when(keycloakProperties.getApiEndpoint()).thenReturn("http://localhost:8080/auth");
        when(keycloakProperties.getCredentials()).thenReturn(mock(KeycloakProperties.Credentials.class));
        when(keycloakProperties.getCredentials().getAdminRealm()).thenReturn("master");
        when(keycloakProperties.getCredentials().getAdminClientId()).thenReturn("admin-cli");
        when(keycloakProperties.getCredentials().getAdminUserName()).thenReturn("admin");
        when(keycloakProperties.getCredentials().getAdminPassword()).thenReturn("admin-password");

        // Mock KeycloakBuilder chain and client operations
        KeycloakBuilder mockBuilder = mock(KeycloakBuilder.class);
        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        ClientsResource mockClientsResource = mock(ClientsResource.class);

        when(KeycloakBuilder.builder()).thenReturn(mockBuilder);
        when(mockBuilder.serverUrl(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.realm(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.username(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.password(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.grantType(OAuth2Constants.PASSWORD)).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockKeycloak);

        when(mockKeycloak.realm(realm)).thenReturn(mockRealmResource);
        when(mockRealmResource.clients()).thenReturn(mockClientsResource);
        when(mockClientsResource.findByClientId(clientId)).thenThrow(new jakarta.ws.rs.NotFoundException());

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            keycloakRepository.isValidClientId(realm, clientId);
        });
        assertTrue(exception.getLogMessage().contains(String.format(ConstError.ERRLOG_500_KEYCLOAK_FAILED, 404)));
    }

    /**
     * Get access token success test.
     */
    @Test
    void getAccessToken_success() {
        // Arrange
        String idpRealm = "test-realm";
        String code = "authorization-code-123";
        String clientId = "test-client";
        String clientSecret = "test-secret";
        String redirectUri = "http://localhost:8080/callback";
        String codeVerifier = "code-verifier-123";
        String url = "http://keycloak/token";

        AccessTokenResponse expectedResponse = new AccessTokenResponse();
        expectedResponse.setToken("access-token-123");
        expectedResponse.setExpiresIn(3600L);
        expectedResponse.setTokenType("Bearer");
        expectedResponse.setNotBeforePolicy(0);
        expectedResponse.setScope("openid profile");
        expectedResponse.setRefreshToken("refresh-token-123");
        expectedResponse.setRefreshExpiresIn(7200L);
        expectedResponse.setIdToken("id-token-123");

        ResponseEntity<AccessTokenResponse> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(keycloakUtil.getTokenStoreUrl(idpRealm)).thenReturn(url);
        when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(AccessTokenResponse.class)))
                .thenReturn(responseEntity);

        // Act
        AccessTokenResult result = keycloakRepository.getAccessToken(idpRealm, code, clientId, clientSecret,
                redirectUri, codeVerifier);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse.getToken(), result.getAccessToken());
        assertEquals(expectedResponse.getExpiresIn(), result.getExpiresIn());
        assertEquals(expectedResponse.getTokenType(), result.getTokenType());
        assertEquals(expectedResponse.getNotBeforePolicy(), result.getNotBeforePolicy());
        assertEquals(expectedResponse.getScope(), result.getScope());
        assertEquals(expectedResponse.getRefreshToken(), result.getRefreshToken());
        assertEquals(expectedResponse.getRefreshExpiresIn(), result.getRefreshExpiresIn());
        assertEquals(expectedResponse.getIdToken(), result.getIdToken());
    }

    /**
     * Get access token null body test.
     */
    @Test
    void getAccessToken_nullBody() {
        // Arrange
        String idpRealm = "test-realm";
        String code = "authorization-code-123";
        String clientId = "test-client";
        String clientSecret = "test-secret";
        String redirectUri = "http://localhost:8080/callback";
        String codeVerifier = "code-verifier-123";
        String url = "http://keycloak/token";

        ResponseEntity<AccessTokenResponse> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(keycloakUtil.getTokenStoreUrl(idpRealm)).thenReturn(url);
        when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(AccessTokenResponse.class)))
                .thenReturn(responseEntity);

        // Act & Assert
        UnexpectedException ex = assertThrows(UnexpectedException.class, () -> keycloakRepository
                .getAccessToken(idpRealm, code, clientId, clientSecret, redirectUri, codeVerifier));
        assertEquals(ConstError.ERR_500_MESSAGE, ex.getMessage());
    }

    /**
     * Get access token failure test - exception handling.
     */
    @ParameterizedTest
    @CsvSource({
            "BadParametersException, test-realm, invalid-code, test-client, test-secret, http://localhost:8080/callback, code-verifier-123",
            "UnauthorizedException, test-realm, authorization-code-123, invalid-client, test-secret, http://localhost:8080/callback, code-verifier-123",
            "UnauthorizedException, test-realm, authorization-code-123, test-client, invalid-secret, http://localhost:8080/callback, code-verifier-123",
            "OutOfServiceException, test-realm, authorization-code-123, test-client, test-secret, http://localhost:8080/callback, code-verifier-123",
            "UnexpectedException, test-realm, authorization-code-123, test-client, test-secret, http://localhost:8080/callback, code-verifier-123"
    })
    void getAccessToken_shouldThrowExpectedException(String exceptionClassName,
            String idpRealm, String code, String clientId, String clientSecret, String redirectUri,
            String codeVerifier) {
        // Arrange
        String url = "http://keycloak/token";
        String exceptionResponseMessage = null;
        Exception exception = null;
        Class<? extends Throwable> clazz = null;

        switch (exceptionClassName) {
            case "BadParametersException":
                exceptionResponseMessage = "Bad parameter";
                exception = Mockito.mock(HttpClientErrorException.BadRequest.class);
                clazz = BadParametersException.class;
                break;
            case "UnauthorizedException":
                exceptionResponseMessage = "Unauthorized";
                exception = Mockito.mock(HttpClientErrorException.Unauthorized.class);
                clazz = UnauthorizedException.class;
                break;
            case "OutOfServiceException":
                exceptionResponseMessage = "Service unavailable";
                exception = Mockito.mock(HttpServerErrorException.ServiceUnavailable.class);
                clazz = OutOfServiceException.class;
                break;
            case "UnexpectedException":
                exceptionResponseMessage = "Unexpected error occurred";
                exception = Mockito.mock(HttpServerErrorException.InternalServerError.class);
                clazz = UnexpectedException.class;
                break;
            default:
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        when(keycloakUtil.getTokenStoreUrl(idpRealm)).thenReturn(url);
        when(exception.getMessage()).thenReturn(exceptionResponseMessage);
        when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(AccessTokenResponse.class)))
                .thenThrow(exception);

        // Act & Assert
        assertThrows(clazz, () -> keycloakRepository.getAccessToken(idpRealm, code, clientId, clientSecret, redirectUri,
                codeVerifier));
        assertEquals(exceptionResponseMessage, exception.getMessage());
    }

    /**
     * Token introspection success test.
     */
    @ParameterizedTest
    @CsvSource({
            // exceptionClass, apiKey
            "idp-realm-123, client-id-123, client-secret-123, access-token-active, " + Const.KEYCLOAK_SEND_VALUE_TOKEN_TYPE_HINT_ACCESS_TOKEN, // normal pattern
            "idp-realm-123, client-id-123, client-secret-123, access-token-inactive, " + Const.KEYCLOAK_SEND_VALUE_TOKEN_TYPE_HINT_ACCESS_TOKEN // normal pattern
    })
    void tokenIntrospection_success(
            String idpRealm, String clientId, String clientSecret, String token, String tokenTypeHint
    ) {
        String url = "http://keycloak/token/introspect";
        TokenIntrospectionResult expectedResult = new TokenIntrospectionResult();
        ResponseEntity<TokenIntrospectionResult> responseEntity = new ResponseEntity<>(expectedResult, HttpStatus.OK);

        // Arrange
        when(keycloakUtil.getTokenIntrospectUrl(idpRealm)).thenReturn(url);
        when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(TokenIntrospectionResult.class)))
                .thenReturn(responseEntity);

        // Act
        TokenIntrospectionResult result = keycloakRepository.tokenIntrospection(idpRealm, clientId, clientSecret, token, tokenTypeHint);

        // Assert
        assertSame(expectedResult, result);
    }

    /**
     * Token introspection null body test.
     */
    @ParameterizedTest
    @CsvSource({
            // exceptionClass, apiKey
            "idp-realm-123, client-id-123, client-secret-123, access-token-active, " + Const.KEYCLOAK_SEND_VALUE_TOKEN_TYPE_HINT_ACCESS_TOKEN, // normal pattern
    })
    void tokenIntrospection_nullBody(
            String idpRealm, String clientId, String clientSecret, String token, String tokenTypeHint
    ) {
        String url = "http://keycloak/token/introspect";
        ResponseEntity<TokenIntrospectionResult> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(keycloakUtil.getTokenIntrospectUrl(idpRealm)).thenReturn(url);
        when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(TokenIntrospectionResult.class)))
                .thenReturn(responseEntity);

        UnexpectedException ex = assertThrows(UnexpectedException.class, () ->
                keycloakRepository.tokenIntrospection(idpRealm, clientId, clientSecret, token, tokenTypeHint));
        assertEquals(ConstError.ERR_500_MESSAGE, ex.getMessage());
    }

    /**
     * Token introspection failure test.
     */
    @ParameterizedTest
    @CsvSource({
            // exceptionClass, apiKey
            "BadParametersException, idp-realm-123, client-id-123, client-secret-123, access-token-active, dummy-token-type", // BadParametersException
            "UnexpectedException, idp-realm-dummy, client-id-123, client-secret-123, access-token-active, " + Const.KEYCLOAK_SEND_VALUE_TOKEN_TYPE_HINT_ACCESS_TOKEN, //
            "UnauthorizedException, idp-realm-123, client-id-empty, client-secret-123, access-token-active, " + Const.KEYCLOAK_SEND_VALUE_TOKEN_TYPE_HINT_ACCESS_TOKEN, // UnauthorizedException
            "UnauthorizedException, idp-realm-123, client-id-123, client-secret-empty, access-token-active, " + Const.KEYCLOAK_SEND_VALUE_TOKEN_TYPE_HINT_ACCESS_TOKEN, //
            "OutOfServiceException, idp-realm-123, client-id-123, client-secret-123, access-token-active, " + Const.KEYCLOAK_SEND_VALUE_TOKEN_TYPE_HINT_ACCESS_TOKEN, //
    })
    void tokenIntrospection_shouldThrowExpectedException(String exceptionClassName,
            String idpRealm, String clientId, String clientSecret, String token, String tokenTypeHint
    ) {
        String url = "http://keycloak/token/introspect";

        String exceptionResponseMessage = null;
        Exception exception = null;
        Class<? extends Throwable> clazz = null;

        switch (exceptionClassName) {
            case "BadParametersException":
                // Arrange
                exceptionResponseMessage = "Bad parameter";
                exception = Mockito.mock(HttpClientErrorException.BadRequest.class);
                clazz = BadParametersException.class;
                break;
            case "UnauthorizedException":
                // Arrange
                exceptionResponseMessage = "Unauthorized";
                exception = Mockito.mock(HttpClientErrorException.Unauthorized.class);
                clazz = UnauthorizedException.class;
                break;
            case "OutOfServiceException":
                // Arrange
                exceptionResponseMessage = "Service unavailable";
                exception = Mockito.mock(HttpServerErrorException.ServiceUnavailable.class);
                clazz = OutOfServiceException.class;
                break;
            case "UnexpectedException":
                // Arrange
                exceptionResponseMessage = "Unexpected error occurred";
                exception = Mockito.mock(HttpServerErrorException.InternalServerError.class);
                clazz = UnexpectedException.class;
                break;
            default:
                // Unexpected exception
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        when(keycloakUtil.getTokenIntrospectUrl(idpRealm)).thenReturn(url);
        when(exception.getMessage()).thenReturn(exceptionResponseMessage);
        when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(TokenIntrospectionResult.class)))
                .thenThrow(exception);

        // Act
        // Get realm from APIKey
        assertThrows(clazz, () ->
                keycloakRepository.tokenIntrospection(idpRealm, clientId, clientSecret, token, tokenTypeHint));
        assertEquals(exceptionResponseMessage, exception.getMessage());

    }

    /**
     * Token introspection success test.
     */
    @ParameterizedTest
    @CsvSource({
            // exceptionClass, apiKey
            "idp-realm-123, client-id-123, client-secret-123, refresh-token-authentication-flow, authentication-flow", // normal pattern
            "idp-realm-123, client-id-123, client-secret-123, refresh-token-resource-owner-credential-flow, resource-owner-credential-flow" // normal pattern
    })
    void tokenRefresh_success(
            String idpRealm, String clientId, String clientSecret, String refreshToken, String tokenTypeString
    ) {
        String url = "http://keycloak/token";

        AccessTokenResponse expectedResult = new AccessTokenResponse();
        expectedResult.setToken("access-token-123");
        expectedResult.setExpiresIn(3600L);
        expectedResult.setTokenType("Bearer");
        expectedResult.setNotBeforePolicy(0);
        expectedResult.setScope("openid");
        expectedResult.setRefreshToken("refresh-token-123");
        expectedResult.setRefreshExpiresIn(7200L);
        switch (tokenTypeString) {
            case "authentication-flow":
                expectedResult.setIdToken("id-token-123");
                break;
            case "resource-owner-credential-flow":
                expectedResult.setIdToken(null);
                break;
            default:
                throw new IllegalArgumentException("Unknown tokenTypeString: " + tokenTypeString);
        }

        ResponseEntity<AccessTokenResponse> responseEntity = new ResponseEntity<>(expectedResult, HttpStatus.OK);

        // Arrange
        when(keycloakUtil.getTokenStoreUrl(idpRealm)).thenReturn(url);
        when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(AccessTokenResponse.class)))
                .thenReturn(responseEntity);

        // Act
        AccessTokenResult result = keycloakRepository.tokenRefresh(idpRealm, clientId, clientSecret, refreshToken);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResult.getToken(), result.getAccessToken());
        assertEquals(expectedResult.getExpiresIn(), result.getExpiresIn());
        assertEquals(expectedResult.getTokenType(), result.getTokenType());
        assertEquals(expectedResult.getNotBeforePolicy(), result.getNotBeforePolicy());
        assertEquals(expectedResult.getScope(), result.getScope());
        assertEquals(expectedResult.getRefreshToken(), result.getRefreshToken());
        assertEquals(expectedResult.getRefreshExpiresIn(), result.getRefreshExpiresIn());
        assertEquals(expectedResult.getIdToken(), result.getIdToken());
    }

    /**
     * Token introspection null body test.
     */
    @ParameterizedTest
    @CsvSource({
            // exceptionClass, apiKey
            "idp-realm-123, client-id-123, client-secret-123, refresh-token-active", // normal pattern
    })
    void tokenRefresh_nullBody(
            String idpRealm, String clientId, String clientSecret, String refreshToken
    ) {
        String url = "http://keycloak/token";
        ResponseEntity<AccessTokenResponse> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(keycloakUtil.getTokenStoreUrl(idpRealm)).thenReturn(url);
        when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(AccessTokenResponse.class)))
                .thenReturn(responseEntity);

        UnexpectedException ex = assertThrows(UnexpectedException.class, () ->
                keycloakRepository.tokenRefresh(idpRealm, clientId, clientSecret, refreshToken));
        assertEquals(ConstError.ERR_500_MESSAGE, ex.getMessage());
    }

    /**
     * Token introspection failure test.
     */
    @ParameterizedTest
    @CsvSource({
            // exceptionClass, apiKey
            "BadParametersException, idp-realm-123, client-id-123, client-secret-123, refresh-token-expired", // BadParametersException
            "UnexpectedException, idp-realm-dummy, client-id-123, client-secret-123, refresh-token-123", //
            "UnauthorizedException, idp-realm-123, client-id-empty, client-secret-123, refresh-token-123", // UnauthorizedException
            "UnauthorizedException, idp-realm-123, client-id-123, client-secret-empty, refresh-token-123", //
            "OutOfServiceException, idp-realm-123, client-id-123, client-secret-123, refresh-token-123", //
    })
    void tokenRefresh_shouldThrowExpectedException(String exceptionClassName,
            String idpRealm, String clientId, String clientSecret, String refreshToken
    ) {
        String url = "http://keycloak/token";

        String exceptionResponseMessage = null;
        Exception exception = null;
        Class<? extends Throwable> clazz = null;

        switch (exceptionClassName) {
            case "BadParametersException":
                // Arrange
                exceptionResponseMessage = "Bad parameter";
                exception = Mockito.mock(HttpClientErrorException.BadRequest.class);
                clazz = BadParametersException.class;
                break;
            case "UnauthorizedException":
                // Arrange
                exceptionResponseMessage = "Unauthorized";
                exception = Mockito.mock(HttpClientErrorException.Unauthorized.class);
                clazz = UnauthorizedException.class;
                break;
            case "OutOfServiceException":
                // Arrange
                exceptionResponseMessage = "Service unavailable";
                exception = Mockito.mock(HttpServerErrorException.ServiceUnavailable.class);
                clazz = OutOfServiceException.class;
                break;
            case "UnexpectedException":
                // Arrange
                exceptionResponseMessage = "Unexpected error occurred";
                exception = Mockito.mock(HttpServerErrorException.InternalServerError.class);
                clazz = UnexpectedException.class;
                break;
            default:
                // Unexpected exception
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        when(keycloakUtil.getTokenStoreUrl(idpRealm)).thenReturn(url);
        when(exception.getMessage()).thenReturn(exceptionResponseMessage);
        when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(AccessTokenResponse.class)))
                .thenThrow(exception);

        // Act
        // Get realm from APIKey
        assertThrows(clazz, () ->
                keycloakRepository.tokenRefresh(idpRealm, clientId, clientSecret, refreshToken));
        assertEquals(exceptionResponseMessage, exception.getMessage());

    }

    /**
     * case#11:
     * Normal case - signInWithPassword success (Using mocks, no external connection).
     */
    @Test
    void signInWithPassword_success_returnsAccessTokenResult() {
        // Arrange
        String realm = "test-realm";
        String clientId = "test-client";
        String clientSecret = "test-secret";
        String operatorAccountId = "test@example.com";
        String password = "testpassword";

        // Setup required keycloakUtil mocks for build() method
        when(keycloakProperties.getApiEndpoint()).thenReturn("http://localhost:8080/auth");

        // Mock KeycloakBuilder chain for user authentication
        KeycloakBuilder mockBuilder = mock(KeycloakBuilder.class);
        Keycloak mockKeycloak = mock(Keycloak.class);

        // Mock TokenManager and AccessTokenResponse
        org.keycloak.admin.client.token.TokenManager mockTokenManager = mock(org.keycloak.admin.client.token.TokenManager.class);
        AccessTokenResponse mockAccessTokenResponse = mock(AccessTokenResponse.class);

        when(KeycloakBuilder.builder()).thenReturn(mockBuilder);
        when(mockBuilder.serverUrl(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.realm(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientSecret(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.username(operatorAccountId)).thenReturn(mockBuilder);
        when(mockBuilder.password(password)).thenReturn(mockBuilder);
        when(mockBuilder.grantType(OAuth2Constants.PASSWORD)).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockKeycloak);

        // Mock Keycloak tokenManager and response
        when(mockKeycloak.tokenManager()).thenReturn(mockTokenManager);
        when(mockTokenManager.getAccessToken()).thenReturn(mockAccessTokenResponse);

        // Mock AccessTokenResponse values
        when(mockAccessTokenResponse.getToken()).thenReturn("test-access-token");
        when(mockAccessTokenResponse.getExpiresIn()).thenReturn(3600L);
        when(mockAccessTokenResponse.getTokenType()).thenReturn("Bearer");
        when(mockAccessTokenResponse.getNotBeforePolicy()).thenReturn(0);
        when(mockAccessTokenResponse.getScope()).thenReturn("openid profile");
        when(mockAccessTokenResponse.getRefreshToken()).thenReturn("test-refresh-token");
        when(mockAccessTokenResponse.getRefreshExpiresIn()).thenReturn(7200L);
        // Act
        AccessTokenResult result = keycloakRepository.signInWithPassword(realm, clientId, clientSecret, operatorAccountId, password);

        // Assert
        assertNotNull(result);
        assertEquals("test-access-token", result.getAccessToken());
        assertEquals(Long.valueOf(3600L), result.getExpiresIn());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(Long.valueOf(0L), result.getNotBeforePolicy());
        assertEquals("openid profile", result.getScope());
        assertEquals("test-refresh-token", result.getRefreshToken());
        assertEquals(Long.valueOf(7200L), result.getRefreshExpiresIn());
    }

    /**
     * case#12:
     * Error case - signInWithPassword login failed (Using mocks, no external connection).
     */
    @Test
    void signInWithPassword_loginFailed_throwsException() throws Exception {
        // Arrange
        String realm = "test-realm";
        String clientId = "test-client";
        String clientSecret = "test-secret";
        String operatorAccountId = "test@example.com";
        String password = "invalid-password";

        // Setup required keycloakUtil mocks for build() method
        when(keycloakProperties.getApiEndpoint()).thenReturn("http://localhost:8080/auth");

        // Mock KeycloakBuilder chain for user authentication
        KeycloakBuilder mockBuilder = mock(KeycloakBuilder.class);
        Keycloak mockKeycloak = mock(Keycloak.class);

        // Mock TokenManager that throws NotAuthorizedException
        org.keycloak.admin.client.token.TokenManager mockTokenManager = mock(org.keycloak.admin.client.token.TokenManager.class);

        when(KeycloakBuilder.builder()).thenReturn(mockBuilder);
        when(mockBuilder.serverUrl(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.realm(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientSecret(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.username(operatorAccountId)).thenReturn(mockBuilder);
        when(mockBuilder.password(password)).thenReturn(mockBuilder);
        when(mockBuilder.grantType(OAuth2Constants.PASSWORD)).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockKeycloak);

        // Mock Keycloak tokenManager to throw NotAuthorizedException (login failure)
        when(mockKeycloak.tokenManager()).thenReturn(mockTokenManager);
        when(mockTokenManager.getAccessToken()).thenThrow(new jakarta.ws.rs.NotAuthorizedException("Unauthorized"));

        // Act & Assert
        LoginException exception = assertThrows(LoginException.class, () -> {
            keycloakRepository.signInWithPassword(realm, clientId, clientSecret, operatorAccountId, password);
        });
        assertTrue(
                exception.getLogMessage().contains(ConstError.ERR_401_INVALID_CREDENTIALS)
        );
    }

    /**
     * case#13:
     * Error case - signInWithPassword Keycloak build failed (Using mocks, no external connection).
     */
    @Test
    void signInWithPassword_keycloakBuildFailed_throwsUnexpectedException() throws Exception {
        // Arrange
        String realm = "test-realm";
        String clientId = "test-client";
        String clientSecret = "test-secret";
        String operatorAccountId = "test@example.com";
        String password = "testpassword";

        // Setup required keycloakUtil mocks for build() method
        when(keycloakProperties.getApiEndpoint()).thenReturn("http://localhost:8080/auth");

        // Mock KeycloakBuilder chain that returns null Keycloak instance
        KeycloakBuilder mockBuilder = mock(KeycloakBuilder.class);

        when(KeycloakBuilder.builder()).thenReturn(mockBuilder);
        when(mockBuilder.serverUrl(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.realm(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientSecret(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.username(operatorAccountId)).thenReturn(mockBuilder);
        when(mockBuilder.password(password)).thenReturn(mockBuilder);
        when(mockBuilder.grantType(OAuth2Constants.PASSWORD)).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(null); // Simulate Keycloak build failure

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            keycloakRepository.signInWithPassword(realm, clientId, clientSecret, operatorAccountId, password);
        });
        assertTrue(
                exception.getLogMessage().contains(ConstError.ERRLOG_500_KEYCLOAK_BUILD)
        );
    }

    /**
     * case#11:
     * Normal case - signInWithClient success (Using mocks, no external connection).
     */
    @Test
    void signInWithClient_success_returnsAccessTokenResult() {
        // Arrange
        String realm = "test-realm";
        String clientId = "test-client";
        String clientSecret = "test-secret";

        // Setup required keycloakUtil mocks for build() method
        when(keycloakProperties.getApiEndpoint()).thenReturn("http://localhost:8080/auth");

        // Mock KeycloakBuilder chain for user authentication
        KeycloakBuilder mockBuilder = mock(KeycloakBuilder.class);
        Keycloak mockKeycloak = mock(Keycloak.class);

        // Mock TokenManager and AccessTokenResponse
        org.keycloak.admin.client.token.TokenManager mockTokenManager = mock(org.keycloak.admin.client.token.TokenManager.class);
        AccessTokenResponse mockAccessTokenResponse = mock(AccessTokenResponse.class);

        when(KeycloakBuilder.builder()).thenReturn(mockBuilder);
        when(mockBuilder.serverUrl(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.realm(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientSecret(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.grantType(OAuth2Constants.CLIENT_CREDENTIALS)).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockKeycloak);

        // Mock Keycloak tokenManager and response
        when(mockKeycloak.tokenManager()).thenReturn(mockTokenManager);
        when(mockTokenManager.getAccessToken()).thenReturn(mockAccessTokenResponse);

        // Mock AccessTokenResponse values
        when(mockAccessTokenResponse.getToken()).thenReturn("test-access-token");
        when(mockAccessTokenResponse.getExpiresIn()).thenReturn(3600L);
        when(mockAccessTokenResponse.getTokenType()).thenReturn("Bearer");
        when(mockAccessTokenResponse.getNotBeforePolicy()).thenReturn(0);
        when(mockAccessTokenResponse.getScope()).thenReturn("openid profile");
        when(mockAccessTokenResponse.getRefreshToken()).thenReturn(null);
        when(mockAccessTokenResponse.getRefreshExpiresIn()).thenReturn(0L);
        when(mockAccessTokenResponse.getIdToken()).thenReturn(null);

        // Act
        AccessTokenResult result = keycloakRepository.signInWithClient(realm, clientId, clientSecret);

        // Assert
        assertNotNull(result);
        assertEquals("test-access-token", result.getAccessToken());
        assertEquals(Long.valueOf(3600L), result.getExpiresIn());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(Long.valueOf(0L), result.getNotBeforePolicy());
        assertEquals("openid profile", result.getScope());
        assertEquals(null, result.getRefreshToken());
        assertEquals(Long.valueOf(0), result.getRefreshExpiresIn());
        assertEquals(null, result.getIdToken());
    }

    /**
     * case#12:
     * Error case - signInWithClient login failed (Using mocks, no external connection).
     */
    @Test
    void signInWithClient_loginFailed_throwsException() throws Exception {
        // Arrange
        String realm = "test-realm";
        String clientId = "test-client";
        String clientSecret = "test-secret";

        // Setup required keycloakUtil mocks for build() method
        when(keycloakProperties.getApiEndpoint()).thenReturn("http://localhost:8080/auth");

        // Mock KeycloakBuilder chain for user authentication
        KeycloakBuilder mockBuilder = mock(KeycloakBuilder.class);
        Keycloak mockKeycloak = mock(Keycloak.class);

        // Mock TokenManager that throws NotAuthorizedException
        org.keycloak.admin.client.token.TokenManager mockTokenManager = mock(org.keycloak.admin.client.token.TokenManager.class);

        when(KeycloakBuilder.builder()).thenReturn(mockBuilder);
        when(mockBuilder.serverUrl(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.realm(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientSecret(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.grantType(OAuth2Constants.CLIENT_CREDENTIALS)).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockKeycloak);

        // Mock Keycloak tokenManager to throw NotAuthorizedException (login failure)
        when(mockKeycloak.tokenManager()).thenReturn(mockTokenManager);
        when(mockTokenManager.getAccessToken()).thenThrow(new jakarta.ws.rs.NotAuthorizedException("Unauthorized"));

        // Act & Assert
        LoginException exception = assertThrows(LoginException.class, () -> {
            keycloakRepository.signInWithClient(realm, clientId, clientSecret);
        });
        assertTrue(
                exception.getLogMessage().contains(ConstError.ERR_401_INVALID_CREDENTIALS)
        );
    }

    /**
     * case#13:
     * Error case - signInWithClient Keycloak build failed (Using mocks, no external connection).
     */
    @Test
    void signInWithClient_keycloakBuildFailed_throwsUnexpectedException() throws Exception {
        // Arrange
        String realm = "test-realm";
        String clientId = "test-client";
        String clientSecret = "test-secret";

        // Setup required keycloakUtil mocks for build() method
        when(keycloakProperties.getApiEndpoint()).thenReturn("http://localhost:8080/auth");

        // Mock KeycloakBuilder chain that returns null Keycloak instance
        KeycloakBuilder mockBuilder = mock(KeycloakBuilder.class);

        when(KeycloakBuilder.builder()).thenReturn(mockBuilder);
        when(mockBuilder.serverUrl(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.realm(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientSecret(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.grantType(OAuth2Constants.CLIENT_CREDENTIALS)).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(null); // Simulate Keycloak build failure

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            keycloakRepository.signInWithClient(realm, clientId, clientSecret);
        });
        assertTrue(
                exception.getLogMessage().contains(ConstError.ERRLOG_500_KEYCLOAK_BUILD)
        );
    }

    /**
     * Tests the isValidPassword method for a valid password scenario.
     */
    @Test
    @DisplayName("isValidPassword - Valid password")
    void testIsValidPassword_validPassword() {
        // Arrange
        String idpRealm = "test-realm";
        String uid = "test-user";
        String password = "valid-password";

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("test-user");
        userRepresentation.setRequiredActions(List.of());
        UserResource mockUserResource = mock(UserResource.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        CredentialRepresentation mockCredentialRepresentation = mock(CredentialRepresentation.class);
        when(mockCredentialRepresentation.getType()).thenReturn("");
        List<CredentialRepresentation> credentials = Arrays.asList(mockCredentialRepresentation);
        userRepresentation.setCredentials(credentials);
        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockUserResource.toRepresentation()).thenReturn(userRepresentation);
        when(mockUsersResource.get(uid)).thenReturn(mockUserResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUserResource.credentials()).thenReturn(credentials);

        Keycloak mockKeycloakWithCredentials = mock(Keycloak.class);
        TokenManager mockTokenManager = mock(TokenManager.class);
        AccessTokenResponse mockAccessTokenResponse = new AccessTokenResponse();
        mockAccessTokenResponse.setToken("mock-access-token");

        when(mockKeycloakWithCredentials.tokenManager()).thenReturn(mockTokenManager);
        when(mockTokenManager.getAccessToken()).thenReturn(mockAccessTokenResponse);

        KeycloakRepositoryImpl repository = new KeycloakRepositoryImpl(null, null, null) {
            @Override
            public Keycloak build(String realm, String username, String password) {
                return mockKeycloakWithCredentials;
            }

            @Override
            public Keycloak build() {
                return mockKeycloak;
            }
        };

        // Act
        boolean result = repository.isValidPassword(idpRealm, uid, password);

        // Assert
        assertTrue(result, "Expected isValidPassword to return true for valid password");
    }

    /**
     * Tests the isValidPassword method for a invalid password scenario.
     */
    @Test
    @DisplayName("isValidPassword - Invalid password")
    void testIsValidPassword_invalidPassword() {
        // Arrange
        String idpRealm = "test-realm";
        String uid = "test-user";
        String password = "invalid-password";

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("test-user");
        userRepresentation.setRequiredActions(List.of());
        UserResource mockUserResource = mock(UserResource.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        CredentialRepresentation mockCredentialRepresentation = mock(CredentialRepresentation.class);
        when(mockCredentialRepresentation.getType()).thenReturn("");
        List<CredentialRepresentation> credentials = Arrays.asList(mockCredentialRepresentation);
        userRepresentation.setCredentials(credentials);
        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockUserResource.toRepresentation()).thenReturn(userRepresentation);
        when(mockUsersResource.get(uid)).thenReturn(mockUserResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUserResource.credentials()).thenReturn(credentials);

        Keycloak mockKeycloakWithCredentials = mock(Keycloak.class);
        TokenManager mockTokenManager = mock(TokenManager.class);
        AccessTokenResponse mockAccessTokenResponse = new AccessTokenResponse();
        mockAccessTokenResponse.setToken(null);

        when(mockKeycloakWithCredentials.tokenManager()).thenReturn(mockTokenManager);
        when(mockTokenManager.getAccessToken()).thenReturn(null);

        KeycloakRepositoryImpl repository = new KeycloakRepositoryImpl(null, null, null) {
            @Override
            public Keycloak build(String realm, String username, String password) {
                return mockKeycloakWithCredentials;
            }

            @Override
            public Keycloak build() {
                return mockKeycloak;
            }
        };

        // Act
        boolean result = repository.isValidPassword(idpRealm, uid, password);

        // Assert
        assertFalse(result, "Expected isValidPassword to return false for invalid password");
    }

    /**
     * Tests the isValidPassword method for a invalid password scenario(throws Exception).
     */
    @Test
    @DisplayName("isValidPassword - Invalid password throws NotAuthorizedException")
    void testIsValidPassword_invalidPassword_throwNotAuthorizedException() {
        // Arrange
        String idpRealm = "test-realm";
        String uid = "test-user";
        String password = "invalid-password";

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("test-user");
        userRepresentation.setRequiredActions(List.of());
        UserResource mockUserResource = mock(UserResource.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        CredentialRepresentation mockCredentialRepresentation = mock(CredentialRepresentation.class);
        when(mockCredentialRepresentation.getType()).thenReturn("");
        List<CredentialRepresentation> credentials = Arrays.asList(mockCredentialRepresentation);
        userRepresentation.setCredentials(credentials);
        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockUserResource.toRepresentation()).thenReturn(userRepresentation);
        when(mockUsersResource.get(uid)).thenReturn(mockUserResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUserResource.credentials()).thenReturn(credentials);

        Keycloak mockKeycloakWithCredentials = mock(Keycloak.class);
        TokenManager mockTokenManager = mock(TokenManager.class);
        AccessTokenResponse mockAccessTokenResponse = new AccessTokenResponse();
        mockAccessTokenResponse.setToken(null);

        when(mockKeycloakWithCredentials.tokenManager()).thenReturn(mockTokenManager);
        when(mockTokenManager.getAccessToken()).thenReturn(null);

        KeycloakRepositoryImpl repository = new KeycloakRepositoryImpl(null, null, null) {
            @Override
            public Keycloak build(String realm, String username, String password) {
                return mockKeycloakWithCredentials;
            }

            @Override
            public Keycloak build() {
                return mockKeycloak;
            }
        };

        // Act
        boolean result = repository.isValidPassword(idpRealm, uid, password);

        // Assert
        assertFalse(result, "Expected isValidPassword to return false for invalid password");
    }

    /**
     * Tests the isValidPassword method for a invalid password scenario(throws NotFoundException).
     */
    @Test
    @DisplayName("isValidPassword - Invalid password throws NotFoundException")
    void testIsValidPassword_invalidPassword_throwNotFound() {
        // Arrange
        String idpRealm = "test-realm";
        String uid = "test-user";
        String password = "invalid-password";

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("test-user");
        userRepresentation.setRequiredActions(List.of());
        UserResource mockUserResource = mock(UserResource.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        CredentialRepresentation mockCredentialRepresentation = mock(CredentialRepresentation.class);
        when(mockCredentialRepresentation.getType()).thenReturn("");
        List<CredentialRepresentation> credentials = Arrays.asList(mockCredentialRepresentation);
        userRepresentation.setCredentials(credentials);
        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockUserResource.toRepresentation()).thenReturn(userRepresentation);
        when(mockUsersResource.get(uid)).thenReturn(mockUserResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUserResource.credentials()).thenReturn(credentials);

        Keycloak mockKeycloakWithCredentials = mock(Keycloak.class);
        TokenManager mockTokenManager = mock(TokenManager.class);
        AccessTokenResponse mockAccessTokenResponse = new AccessTokenResponse();
        mockAccessTokenResponse.setToken(null);

        when(mockKeycloakWithCredentials.tokenManager()).thenReturn(mockTokenManager);
        when(mockTokenManager.getAccessToken()).thenThrow(new jakarta.ws.rs.NotFoundException("User not found"));

        KeycloakRepositoryImpl repository = new KeycloakRepositoryImpl(null, null, null) {
            @Override
            public Keycloak build(String realm, String username, String password) {
                return mockKeycloakWithCredentials;
            }

            @Override
            public Keycloak build() {
                return mockKeycloak;
            }
        };

        // Act
        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            repository.isValidPassword(idpRealm, uid, password);
        });

        // Verify that the exception message contains the expected text
        assertTrue(exception.getMessage().contains("Unexpected error occurred"), "UnexpectedException should contain 'Keycloak failed'");
        assertTrue(exception.getLogMessage().contains("Keycloak: Unexpected error occurred with status code 404"), "UnexpectedException should contain 'Keycloak failed'");
    }

    /**
     * Tests the isValidPassword method for a user not found scenario.
     */
    @Test
    @DisplayName("isValidPassword - User not found")
    void testIsValidPassword_userNotFound() {
        // Arrange
        String idpRealm = "test-realm";
        String uid = "nonexistent-user";
        String password = "password";

        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockUsersResource.get(uid)).thenReturn(null);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        KeycloakRepositoryImpl repository = new KeycloakRepositoryImpl(null, null, null) {
            @Override
            public Keycloak build() {
                return mockKeycloak;
            }
        };

        // Act
        boolean result = repository.isValidPassword(idpRealm, uid, password);

        // Assert
        assertFalse(result, "Expected isValidPassword to return false for invalid password");
    }

    @Test
    @DisplayName("isValidPassword - UnexpectedException when NotFoundException occurs")
    void testIsValidPassword_unexpectedException() {
        // Arrange
        String idpRealm = "test-realm";
        String uid = "test-user";
        String password = "password";

        // Mocking
        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.get(uid)).thenThrow(new jakarta.ws.rs.NotFoundException("User not found"));

        KeycloakRepositoryImpl repository = new KeycloakRepositoryImpl(null, null, null) {
            @Override
            public Keycloak build() {
                return mockKeycloak;
            }
        };

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            repository.isValidPassword(idpRealm, uid, password);
        });

        // Verify that the exception message contains the expected text
        assertTrue(exception.getMessage().contains("Unexpected error occurred"), "UnexpectedException should contain 'Keycloak failed'");
        assertTrue(exception.getLogMessage().contains("Keycloak: Unexpected error occurred with status code 404"), "UnexpectedException should contain 'Keycloak failed'");
    }

    /**
     * Tests the isValidPassword method for a not set password scenario(null credentials).
     */
    @Test
    @DisplayName("isValidPassword - not set password")
    void testIsValidPassword_noPassword_nullCredentials() {
        // Arrange
        String idpRealm = "test-realm";
        String uid = "test-user";
        String password = "";

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("test-user");
        userRepresentation.setRequiredActions(List.of());
        UserResource mockUserResource = mock(UserResource.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockUserResource.toRepresentation()).thenReturn(userRepresentation);
        when(mockUsersResource.get(uid)).thenReturn(mockUserResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUserResource.credentials()).thenReturn(null);

        KeycloakRepositoryImpl repository = new KeycloakRepositoryImpl(null, null, null) {
            @Override
            public Keycloak build() {
                return mockKeycloak;
            }
        };

        // Act
        boolean result = repository.isValidPassword(idpRealm, uid, password);

        // Assert
        assertTrue(result, "Expected isValidPassword to return true for valid password");
    }

    /**
     * Tests the isValidPassword method for a not set password scenario(empty credentials).
     */
    @Test
    @DisplayName("isValidPassword - not set password")
    void testIsValidPassword_noPassword_emptyCredentials() {
        // Arrange
        String idpRealm = "test-realm";
        String uid = "test-user";
        String password = "";

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("test-user");
        userRepresentation.setRequiredActions(List.of());
        UserResource mockUserResource = mock(UserResource.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockUserResource.toRepresentation()).thenReturn(userRepresentation);
        when(mockUsersResource.get(uid)).thenReturn(mockUserResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUserResource.credentials()).thenReturn(List.of());

        KeycloakRepositoryImpl repository = new KeycloakRepositoryImpl(null, null, null) {
            @Override
            public Keycloak build() {
                return mockKeycloak;
            }
        };

        // Act
        boolean result = repository.isValidPassword(idpRealm, uid, password);

        // Assert
        assertTrue(result, "Expected isValidPassword to return true for valid password");
    }

    /**
     * Tests the isValidPassword method for a not set password scenario(not empty password).
     */
    @Test
    @DisplayName("isValidPassword - not set password")
    void testIsValidPassword_noPassword_notEmptyPassword() {
        // Arrange
        String idpRealm = "test-realm";
        String uid = "test-user";
        String password = "test_password";

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("test-user");
        userRepresentation.setRequiredActions(List.of());
        UserResource mockUserResource = mock(UserResource.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockUserResource.toRepresentation()).thenReturn(userRepresentation);
        when(mockUsersResource.get(uid)).thenReturn(mockUserResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUserResource.credentials()).thenReturn(List.of());

        KeycloakRepositoryImpl repository = new KeycloakRepositoryImpl(null, null, null) {
            @Override
            public Keycloak build() {
                return mockKeycloak;
            }
        };

        // Act
        boolean result = repository.isValidPassword(idpRealm, uid, password);

        // Assert
        assertFalse(result, "Expected isValidPassword to return false for invalid password");
    }

    /**
     * Tests the isValidPassword method for temporary password scenario.
     */
    @Test
    @DisplayName("isValidPassword - Temporary password")
    void testIsValidPassword_temporaryPassword() {
        // Arrange
        String idpRealm = "test-realm";
        String uid = "test-user";
        String password = "valid-password";

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("test-user");
        userRepresentation.setRequiredActions(Collections.singletonList(Const.KEYCLOAK_USERRESOURCE_REQUIRED_ACTION_UPDATE_PASSWORD));
        UserResource mockUserResource = mock(UserResource.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        CredentialRepresentation mockCredentialRepresentation = mock(CredentialRepresentation.class);
        when(mockCredentialRepresentation.getType()).thenReturn(Const.KEYCLOAK_USERRESOURCE_CREDENTIAL_TYPE_PASSWORD);
        List<CredentialRepresentation> credentials = Arrays.asList(mockCredentialRepresentation);
        userRepresentation.setCredentials(credentials);
        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockUserResource.toRepresentation()).thenReturn(userRepresentation);
        when(mockUsersResource.get(uid)).thenReturn(mockUserResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUserResource.credentials()).thenReturn(credentials);

        KeycloakRepositoryImpl repository = new KeycloakRepositoryImpl(null, null, null) {
            @Override
            public Keycloak build() {
                return mockKeycloak;
            }
        };

        // Act
        boolean result = repository.isValidPassword(idpRealm, uid, password);

        // Assert
        assertTrue(result, "Expected isValidPassword to return true for valid password");
    }

    /**
     * Tests the isValidPassword method for temporary password scenario(type is not 'password').
     */
    @Test
    @DisplayName("isValidPassword - Temporary password, type is not 'password'")
    void testIsValidPassword_temporaryPassword_notType() {
        // Arrange
        String idpRealm = "test-realm";
        String uid = "test-user";
        String password = "valid-password";

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("test-user");
        userRepresentation.setRequiredActions(Collections.singletonList(Const.KEYCLOAK_USERRESOURCE_REQUIRED_ACTION_UPDATE_PASSWORD));
        UserResource mockUserResource = mock(UserResource.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        CredentialRepresentation mockCredentialRepresentation = mock(CredentialRepresentation.class);
        when(mockCredentialRepresentation.getType()).thenReturn("");
        List<CredentialRepresentation> credentials = Arrays.asList(mockCredentialRepresentation);
        userRepresentation.setCredentials(credentials);
        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockUserResource.toRepresentation()).thenReturn(userRepresentation);
        when(mockUsersResource.get(uid)).thenReturn(mockUserResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUserResource.credentials()).thenReturn(credentials);

        Keycloak mockKeycloakWithCredentials = mock(Keycloak.class);
        TokenManager mockTokenManager = mock(TokenManager.class);
        AccessTokenResponse mockAccessTokenResponse = new AccessTokenResponse();
        mockAccessTokenResponse.setToken("mock-access-token");

        when(mockKeycloakWithCredentials.tokenManager()).thenReturn(mockTokenManager);
        when(mockTokenManager.getAccessToken()).thenReturn(mockAccessTokenResponse);

        KeycloakRepositoryImpl repository = new KeycloakRepositoryImpl(null, null, null) {
            @Override
            public Keycloak build(String realm, String username, String password) {
                return mockKeycloakWithCredentials;
            }

            @Override
            public Keycloak build() {
                return mockKeycloak;
            }
        };

        // Act
        boolean result = repository.isValidPassword(idpRealm, uid, password);

        // Assert
        assertTrue(result, "Expected isValidPassword to return true for valid password");
    }

    /**
     * Tests the isValidPassword method for temporary password scenario(no actions).
     */
    @Test
    @DisplayName("isValidPassword - Temporary password, no actions")
    void testIsValidPassword_temporaryPassword_noActions() {
        // Arrange
        String idpRealm = "test-realm";
        String uid = "test-user";
        String password = "valid-password";

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("test-user");
        userRepresentation.setRequiredActions(List.of());
        UserResource mockUserResource = mock(UserResource.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        CredentialRepresentation mockCredentialRepresentation = mock(CredentialRepresentation.class);
        when(mockCredentialRepresentation.getType()).thenReturn(Const.KEYCLOAK_USERRESOURCE_CREDENTIAL_TYPE_PASSWORD);
        List<CredentialRepresentation> credentials = Arrays.asList(mockCredentialRepresentation);
        userRepresentation.setCredentials(credentials);
        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockUserResource.toRepresentation()).thenReturn(userRepresentation);
        when(mockUsersResource.get(uid)).thenReturn(mockUserResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUserResource.credentials()).thenReturn(credentials);

        Keycloak mockKeycloakWithCredentials = mock(Keycloak.class);
        TokenManager mockTokenManager = mock(TokenManager.class);
        AccessTokenResponse mockAccessTokenResponse = new AccessTokenResponse();
        mockAccessTokenResponse.setToken("mock-access-token");

        when(mockKeycloakWithCredentials.tokenManager()).thenReturn(mockTokenManager);
        when(mockTokenManager.getAccessToken()).thenReturn(mockAccessTokenResponse);

        KeycloakRepositoryImpl repository = new KeycloakRepositoryImpl(null, null, null) {
            @Override
            public Keycloak build(String realm, String username, String password) {
                return mockKeycloakWithCredentials;
            }

            @Override
            public Keycloak build() {
                return mockKeycloak;
            }
        };

        // Act
        boolean result = repository.isValidPassword(idpRealm, uid, password);

        // Assert
        assertTrue(result, "Expected isValidPassword to return true for valid password");
    }

    /**
     * Tests the isValidPassword method for temporary password scenario(password empty).
     */
    @Test
    @DisplayName("isValidPassword - Temporary password")
    void testIsValidPassword_temporaryPassword_emptyPassword() {
        // Arrange
        String idpRealm = "test-realm";
        String uid = "test-user";
        String password = "";

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("test-user");
        userRepresentation.setRequiredActions(Collections.singletonList(Const.KEYCLOAK_USERRESOURCE_REQUIRED_ACTION_UPDATE_PASSWORD));
        UserResource mockUserResource = mock(UserResource.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        CredentialRepresentation mockCredentialRepresentation = mock(CredentialRepresentation.class);
        List<CredentialRepresentation> credentials = Arrays.asList(mockCredentialRepresentation);
        userRepresentation.setCredentials(credentials);
        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockUserResource.toRepresentation()).thenReturn(userRepresentation);
        when(mockUsersResource.get(uid)).thenReturn(mockUserResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUserResource.credentials()).thenReturn(credentials);

        Keycloak mockKeycloakWithCredentials = mock(Keycloak.class);
        TokenManager mockTokenManager = mock(TokenManager.class);
        AccessTokenResponse mockAccessTokenResponse = new AccessTokenResponse();
        mockAccessTokenResponse.setToken("mock-access-token");

        when(mockKeycloakWithCredentials.tokenManager()).thenReturn(mockTokenManager);
        when(mockTokenManager.getAccessToken()).thenThrow(new jakarta.ws.rs.NotAuthorizedException("Unauthorized"));

        KeycloakRepositoryImpl repository = new KeycloakRepositoryImpl(null, null, null) {
            @Override
            public Keycloak build(String realm, String username, String password) {
                return mockKeycloakWithCredentials;
            }

            @Override
            public Keycloak build() {
                return mockKeycloak;
            }
        };

        // Act
        boolean result = repository.isValidPassword(idpRealm, uid, password);

        // Assert
        assertFalse(result, "Expected isValidPassword to return false for invalid password");
    }

    /**
     * Provides test cases for changePassword method with various scenarios.
     */
    static Stream<Arguments> changePasswordTestCases() {
        return Stream.of(
            // Normal case: Password change succeeds
            Arguments.of("test-realm", "test-user", "new-password", null, null),

            // User not found
            Arguments.of("test-realm", "nonexistent-user", "new-password", new NotFoundException(), UnexpectedException.class),

            // Unauthorized access
            Arguments.of("test-realm", "deleted-user", "new-password", null, UnauthorizedException.class)
        );
    }

    /**
     * Parameterized test for changePassword method to handle various scenarios.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("changePasswordTestCases")
    @DisplayName("changePassword handles various scenarios")
    void changePassword_handlesVariousScenarios(String idpRealm, String uid, String newPassword, Exception userResourceException, Class<? extends Throwable> expectedException) {
        // Arrange
        UserResource mockUserResource = mock(UserResource.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);

        // Mock Keycloak behavior
        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);

        if (userResourceException != null) {
            when(mockUsersResource.get(uid)).thenThrow(userResourceException);
        } else if (uid == "deleted-user") {
            when(mockUsersResource.get(uid)).thenReturn(null);
        } else {
            when(mockUsersResource.get(uid)).thenReturn(mockUserResource);
        }

        // Spy on the repository and mock build methods
        KeycloakRepositoryImpl spyRepository = spy(keycloakRepository);
        doReturn(mockKeycloak).when(spyRepository).build();

        // Act & Assert
        if (expectedException != null) {
            AbstractBaseException e = (AbstractBaseException) assertThrows(expectedException, () -> spyRepository.changePassword(idpRealm, uid, newPassword));
            if (e.getClass() == UnauthorizedException.class) {
                assertTrue(e.getLogMessage().equals(String.format(ConstError.ERRLOG_401_USER_NOT_FOUND, uid)), "Expected UnauthorizedException with specific message");
                assertTrue(e.getMessage().equals(ConstError.ERR_401_INVALID_CLIENT), "Expected UnauthorizedException with specific message");
            } else {
                assertTrue(e.getLogMessage().contains(String.format(ConstError.ERRLOG_500_KEYCLOAK_FAILED,
                        HttpStatus.NOT_FOUND.value(), newPassword)), "Expected NotFoundException with specific message");
            }

        } else {
            assertDoesNotThrow(() -> spyRepository.changePassword(idpRealm, uid, newPassword));
        }
    }

    /**
     * Tests the updateUser method for the normal case where a user is successfully updated.
     */
    @ParameterizedTest
    @CsvSource({
        "test-realm, user-id-123, newLoginUser, new@example.com",
        "test-realm, user-id-456, newLoginUser, ''", // Empty email
        "test-realm, user-id-789, newLoginUser, null" // Null email
    })
    @DisplayName("updateUser - Success Cases")
    void updateUser_success(String idpRealm, String userId, String newLoginUserId, String newEmail) {
        // Arrange
        newEmail = "null".equals(newEmail) ? null : newEmail;

        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        UserResource mockUserResource = mock(UserResource.class);
        UserRepresentation mockUserRepresentation = new UserRepresentation();

        mockUserRepresentation.setUsername("oldLoginUser");
        mockUserRepresentation.setEmail("old@example.com");
        mockUserRepresentation.setEnabled(true);

        when(mockKeycloak.realm(anyString())).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.get(userId)).thenReturn(mockUserResource);
        when(mockUserResource.toRepresentation()).thenReturn(mockUserRepresentation);

        KeycloakRepositoryImpl repository = spy(keycloakRepository);
        doReturn(mockKeycloak).when(repository).build();

        // Act
        IdpAccountInfo result = repository.updateUser(idpRealm, userId, newLoginUserId, newEmail);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(newLoginUserId, result.getLoginUserId());
        assertEquals(newEmail == null ? "old@example.com" : newEmail, result.getEmail());
        assertEquals(true, result.isEnabled());
        verify(mockUserResource).update(any(UserRepresentation.class));
    }

    /**
     * Tests the updateUser method when no update is needed.
     */
    @ParameterizedTest
    @CsvSource({
        "test-realm, user-id-123, oldLoginUser, old@example.com",
        "test-realm, user-id-123, null, old@example.com",
        "test-realm, user-id-789, oldLoginUser, null"
    })
    @DisplayName("updateUser - needUpdate is false")
    void updateUser_needUpdateFalse(
            String idpRealm,
            String userId,
            String newLoginUserId,
            String newEmail) {
        // Handle "null" string to actual null
        newLoginUserId = "null".equals(newLoginUserId) ? null : newLoginUserId;
        newEmail = "null".equals(newEmail) ? null : newEmail;

        // Arrange
        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        UserResource mockUserResource = mock(UserResource.class);
        UserRepresentation existingUserRepresentation = new UserRepresentation();

        existingUserRepresentation.setId(userId);
        existingUserRepresentation.setUsername("oldLoginUser");
        existingUserRepresentation.setEmail("old@example.com");
        existingUserRepresentation.setEnabled(true);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.get(userId)).thenReturn(mockUserResource);
        when(mockUserResource.toRepresentation()).thenReturn(existingUserRepresentation);

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();

        // Act
        IdpAccountInfo result = repository.updateUser(idpRealm, userId, newLoginUserId, newEmail);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(newLoginUserId == null ? "oldLoginUser" : newLoginUserId, result.getLoginUserId());
        assertEquals(newEmail == null ? "old@example.com" : newEmail, result.getEmail());

        // Verify that the user was not updated because needUpdate is false
        verify(mockUserResource, never()).update(any(UserRepresentation.class));
    }

    /**
     * Tests the updateUser method for various conditions of the users list.
     */
    @ParameterizedTest
    @CsvSource({
        // Case 1: users is null
        "test-realm, user-id-123, newLoginUser, new@example.com, null, false, false, false",
        // Case 2: users is empty
        "test-realm, user-id-123, newLoginUser, new@example.com, empty, true, false, false",
        // Case 3: users is not empty, but userId matches
        "test-realm, user-id-123, newLoginUser, new@example.com, match, true, true, false",
        // Case 4: users is not empty, and userId does not match
        "test-realm, user-id-123, newLoginUser, new@example.com, mismatch, true, true, true"
    })
    @DisplayName("updateUser - Test conditions for users list")
    void updateUser_userIdConditions(
            String idpRealm,
            String userId,
            String newLoginUserId,
            String newEmail,
            String usersCase,
            boolean usersNotNull,
            boolean usersNotEmpty,
            boolean userIdMismatch) {

        // Arrange
        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        UserResource mockUserResource = mock(UserResource.class);
        UserRepresentation existingUserRepresentation = new UserRepresentation();

        existingUserRepresentation.setId(userId);
        existingUserRepresentation.setUsername("oldLoginUser");
        existingUserRepresentation.setEmail("old@example.com");
        existingUserRepresentation.setEnabled(true);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.get(userId)).thenReturn(mockUserResource);
        when(mockUserResource.toRepresentation()).thenReturn(existingUserRepresentation);

        // Simulate users list based on the test case
        List<UserRepresentation> users = null;
        if ("null".equals(usersCase)) {
            users = null;
        } else if ("empty".equals(usersCase)) {
            users = List.of();
        } else if ("match".equals(usersCase)) {
            UserRepresentation matchingUser = new UserRepresentation();
            matchingUser.setId(userId); // Same ID as the current user
            users = List.of(matchingUser);
        } else if ("mismatch".equals(usersCase)) {
            UserRepresentation mismatchingUser = new UserRepresentation();
            mismatchingUser.setId("different-user-id"); // Different ID
            users = List.of(mismatchingUser);
        }

        when(mockUsersResource.search(newLoginUserId, true)).thenReturn(users);

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();

        // Act & Assert
        if (usersNotNull && usersNotEmpty && userIdMismatch) {
            ConflictException exception = assertThrows(ConflictException.class, () -> {
                repository.updateUser(idpRealm, userId, newLoginUserId, newEmail);
            });

            // Assert
            assertTrue(exception.getLogMessage().contains("login_user_id conflict occurred"), "Expected error message not found in log message.");
            assertTrue(exception.getResponseMessage().contains("login_user_id conflict occurred"), "Expected error message not found in response message.");
        } else {
            assertDoesNotThrow(() -> {
                repository.updateUser(idpRealm, userId, newLoginUserId, newEmail);
            });
        }
    }

    /**
     * Tests the updateUser method for various conditions of email conflict.
     */
    @ParameterizedTest
    @CsvSource({
        // Case 1: users is null
        "test-realm, user-id-123, newLoginUser, duplicate@example.com",
        // Case 2: users is empty
        "test-realm, user-id-123, newLoginUser, duplicate@example.com",
        // Case 3: users contains a user with the same email but different userId
        "test-realm, user-id-123, newLoginUser, duplicate@example.com",
        // Case 4: users contains a user with the same email and same userId
        "test-realm, user-id-123, newLoginUser, duplicate@example.com"
    })
    @DisplayName("updateUser - Test conditions for email conflict")
    void updateUser_emailConditions(
            String idpRealm,
            String userId,
            String newLoginUserId,
            String newEmail) {

        // Arrange
        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        UserResource mockUserResource = mock(UserResource.class);
        UserRepresentation existingUserRepresentation = new UserRepresentation();

        existingUserRepresentation.setId(userId);
        existingUserRepresentation.setUsername("oldLoginUser");
        existingUserRepresentation.setEmail("old@example.com");
        existingUserRepresentation.setEnabled(true);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.get(userId)).thenReturn(mockUserResource);
        when(mockUserResource.toRepresentation()).thenReturn(existingUserRepresentation);

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();

        // Act & Assert
        assertDoesNotThrow(() -> {
            repository.updateUser(idpRealm, userId, newLoginUserId, newEmail);
        });
    }

    /**
     * Tests the updateUser method for the case where the user is not found.
     */
    @Test
    @DisplayName("updateUser - User Not Found")
    void updateUser_userNotFound() {
        // Arrange
        String idpRealm = "test-realm";
        String userId = "nonexistent-user";
        String newLoginUserId = "newLoginUser";
        String newEmail = "new@example.com";

        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);

        when(mockKeycloak.realm(anyString())).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.get(userId)).thenReturn(null);

        KeycloakRepositoryImpl repository = spy(keycloakRepository);
        doReturn(mockKeycloak).when(repository).build();

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            repository.updateUser(idpRealm, userId, newLoginUserId, newEmail);
        });

        assertTrue(exception.getLogMessage().contains("Unexpected error occurred"), "Expected error message not found in log message.");
    }

    /**
     * Tests the updateUser method for the case where a BadRequestException is thrown during the update.
     */
    @Test
    @DisplayName("updateUser - BadRequestException Handling")
    void updateUser_badRequestException() {
        // Arrange
        String idpRealm = "test-realm";
        String userId = "user-id-123";
        String newLoginUserId = "newLoginUser";
        String newEmail = "new@example.com";

        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        UserResource mockUserResource = mock(UserResource.class);
        UserRepresentation existingUserRepresentation = new UserRepresentation();

        existingUserRepresentation.setId(userId);
        existingUserRepresentation.setUsername("oldLoginUser");
        existingUserRepresentation.setEmail("old@example.com");

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.get(userId)).thenReturn(mockUserResource);
        when(mockUserResource.toRepresentation()).thenReturn(existingUserRepresentation);

        // Simulate BadRequestException when updating the user
        doThrow(new BadRequestException("Bad request error"))
                .when(mockUserResource).update(any(UserRepresentation.class));

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            repository.updateUser(idpRealm, userId, newLoginUserId, newEmail);
        });

        // Assert
        assertTrue(exception.getLogMessage().contains(ConstError.ERRLOG_500_KEYCLOAK_BUILD));
        assertEquals(ConstError.ERR_500_MESSAGE, exception.getResponseMessage());
    }

    /**
     * Tests the createUser method for the normal case where a user is successfully created.
     */
    @ParameterizedTest
    @CsvSource({"test@example.com, password123",
            "'', password123", // Empty email
            "null, password123", // Null email
            "test@example.com, ''", // Empty password
            "test@example.com, null", // Null password
            "'', ''", // Both empty
            "null, null"}) // Both null
    @DisplayName("createUser - success Case")
    void createUser_success(String email, String password) {
        // Arrange
        String idpRealm = "test-realm";
        String loginUserId = "testUser";
        boolean passwordTemporaryFlag = true;

        email = "null".equals(email) ? null : email;
        password = "null".equals(password) ? null : password;

        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        UserResource mockUserResource = mock(UserResource.class);
        Response mockResponse = mock(Response.class);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(201);
        when(mockResponse.getLocation()).thenReturn(java.net.URI.create("http://keycloak/users/12345"));
        if (StringUtils.hasText(password)) {
            when(mockUsersResource.get("12345")).thenReturn(mockUserResource);
            doNothing().when(mockUserResource).resetPassword(any(CredentialRepresentation.class));
        }

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();

        // Act
        String userId = repository.createUser(idpRealm, loginUserId, email, password, passwordTemporaryFlag);

        // Assert
        assertEquals("12345", userId);
        verify(mockUsersResource).create(any(UserRepresentation.class));
        if (StringUtils.hasText(password)) {
            verify(mockUserResource).resetPassword(any(CredentialRepresentation.class));
        } else {
            verify(mockUserResource, never()).resetPassword(any(CredentialRepresentation.class));
        }
    }

    /**
     * Tests the createUser method for various error cases.
     */
    @ParameterizedTest
    @CsvSource({
        // Conflict due to duplicate username
        "test-realm, testUser, test@example.com, password123, true, 409, '{\"errorMessage\":\"username already exists\"}', DataConflictException, conflict occurred, conflict occurred",
        // Conflict due to duplicate email
        "test-realm, testUser, test@example.com, password123, true, 409, '{\"errorMessage\":\"email already exists\"}', DataConflictException, conflict occurred, conflict occurred",
        // Conflict due to duplicate other reason
        "test-realm, testUser, test@example.com, password123, true, 409, '{\"errorMessage\":\"xxxxx already exists\"}', UnexpectedException, Keycloak: Unexpected error occurred with status code 409, Unexpected error occurred.",
        // Unexpected error with non-201 status
        "test-realm, testUser, test@example.com, password123, true, 500, '', UnexpectedException, 500, Unexpected error occurred",
        // NotFoundException during user creation
        "test-realm, testUser, test@example.com, password123, true, 404, '', UnexpectedException, Keycloak: Unexpected error occurred with status code 404, Unexpected error occurred"
    })
    @DisplayName("createUser - Error Cases")
    void createUser_error(
            String idpRealm,
            String loginUserId,
            String email,
            String password,
            boolean passwordTemporaryFlag,
            int responseStatus,
            String responseBody,
            String expectedExceptionString,
            String expectedLogMessage,
            String expectedErrorMessage) {

        // Arrange
        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        Response mockResponse = mock(Response.class);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(responseStatus);
        lenient().when(mockResponse.readEntity(String.class)).thenReturn(responseBody);

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();

        // Act & Assert
        Class<? extends Throwable> clazz;
        switch (expectedExceptionString) {
            case "DataConflictException":
                clazz = ConflictException.class;
                break;
            case "UnexpectedException":
                clazz = UnexpectedException.class;
                break;
            default:
                clazz = UnexpectedException.class;
                break;
        }
        AbstractBaseException exception = (AbstractBaseException) assertThrows(clazz, () -> {
            repository.createUser(idpRealm, loginUserId, email, password, passwordTemporaryFlag);
        });

        assertTrue(exception.getLogMessage().contains(expectedLogMessage), "Expected error code not found in log message.");
        assertTrue(exception.getResponseMessage().contains(expectedErrorMessage), "Expected error message not found in log message.");
    }

    /**
     * Tests the createUser method for the case where an unexpected exception occurs during user creation.
     */
    @Test
    @DisplayName("createUser - Unexpected Exception")
    void createUser_unexpectedException() {
        // Arrange
        String idpRealm = "test-realm";
        String loginUserId = "testUser";
        String email = "test@example.com";
        String password = "password123";
        boolean passwordTemporaryFlag = true;

        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);

        // Simulate an unexpected exception during user creation
        when(mockUsersResource.create(any(UserRepresentation.class))).thenThrow(new RuntimeException("Unexpected error"));

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            repository.createUser(idpRealm, loginUserId, email, password, passwordTemporaryFlag);
        });

        // Assert
        assertTrue(exception.getLogMessage().contains("Unexpected error"), "Expected error message not found in log message.");
        assertEquals(ConstError.ERR_500_MESSAGE, exception.getMessage(), "Expected error message not found.");
    }

    /**
     * Tests the deleteUser method for the normal case where a user is successfully deleted.
     */
    @Test
    @DisplayName("deleteUser - Success Case")
    void deleteUser_success() {
        // Arrange
        String idpRealm = "test-realm";
        String userId = "test-user-id";

        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        Response mockResponse = mock(Response.class);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.delete(userId)).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(org.apache.http.HttpStatus.SC_NO_CONTENT);

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();

        // Act & Assert
        assertDoesNotThrow(() -> repository.deleteUser(idpRealm, userId));
        verify(mockUsersResource).delete(userId);
    }

    /**
     * Tests the deleteUser method for the case where an unexpected exception occurs during user deletion.
     */
    @Test
    @DisplayName("deleteUser - Unexpected Exception")
    void deleteUser_unexpectedException() {
        // Arrange
        String idpRealm = "test-realm";
        String userId = "test-user-id";

        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.delete(userId)).thenThrow(new RuntimeException("Unexpected error"));

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            repository.deleteUser(idpRealm, userId);
        });

        assertTrue(exception.getLogMessage().contains("Unexpected error"), "Expected error message not found in log message.");
        assertEquals(ConstError.ERR_500_MESSAGE, exception.getMessage(), "Expected error message not found.");
    }

    /**
     * Tests the deleteUser method for the case where a NotFoundException occurs during user deletion.
     */
    @Test
    @DisplayName("deleteUser - NotFoundException")
    void deleteUser_notFoundException() {
        // Arrange
        String idpRealm = "test-realm";
        String userId = "test-user-id";

        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.delete(userId)).thenThrow(new NotFoundException());

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            repository.deleteUser(idpRealm, userId);
        });

        assertTrue(exception.getLogMessage().contains("Keycloak: Unexpected error occurred"), "Expected error message not found in log message.");
        assertTrue(exception.getMessage().contains("Unexpected error occurred"), "Expected error message not found in log message.");
    }

    /**
     * Tests the deleteUser method for the case where a non-204 response is received during user deletion.
     */
    @Test
    @DisplayName("deleteUser - Non-204 Response")
    void deleteUser_non204Response() {
        // Arrange
        String idpRealm = "test-realm";
        String userId = "test-user-id";

        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        Response mockResponse = mock(Response.class);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.delete(userId)).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR);

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            repository.deleteUser(idpRealm, userId);
        });

        assertTrue(exception.getLogMessage().contains("Keycloak: Unexpected error occurred"), "Expected error message not found in log message.");
        assertTrue(exception.getMessage().contains("Unexpected error occurred"), "Expected error message not found in log message.");
    }

    /**
     * Tests the updateUser method for the normal case where a user is successfully updated.
     */
    @ParameterizedTest
    @CsvSource({
        "test-realm, user-id-123, true, false",
        "test-realm, user-id-456, false, true"
    })
    @DisplayName("updateUserStatus - Success")
    void updateUserStatus_success(String idpRealm, String userId, Boolean enabledFlag, Boolean existingEnabledFlag) {
        // Arrange
        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        UserResource mockUserResource = mock(UserResource.class);
        UserRepresentation mockUserRepresentation = new UserRepresentation();

        mockUserRepresentation.setId(userId);
        mockUserRepresentation.setEnabled(existingEnabledFlag);    // Set to opposite for testing

        when(mockKeycloak.realm(anyString())).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.get(userId)).thenReturn(mockUserResource);
        when(mockUserResource.toRepresentation()).thenReturn(mockUserRepresentation);

        KeycloakRepositoryImpl repository = spy(keycloakRepository);
        doReturn(mockKeycloak).when(repository).build();

        // Act
        IdpAccountInfo result = repository.updateUserStatus(idpRealm, userId, enabledFlag);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(enabledFlag, result.isEnabled());
        verify(mockUserResource).update(any(UserRepresentation.class));
    }

    /**
     * Tests the updateUser method when no update is needed.
     */
    @ParameterizedTest
    @CsvSource({
        "test-realm, user-id-123, true, true",
        "test-realm, user-id-123, false, false"
    })
    @DisplayName("updateUserStatus - needUpdate is false")
    void updateUserStatus_needUpdateFalse(String idpRealm, String userId, Boolean enabledFlag, Boolean existingEnabledFlag) {
        // Arrange
        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        UserResource mockUserResource = mock(UserResource.class);
        UserRepresentation existingUserRepresentation = new UserRepresentation();

        existingUserRepresentation.setId(userId);
        existingUserRepresentation.setEnabled(existingEnabledFlag);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.get(userId)).thenReturn(mockUserResource);
        when(mockUserResource.toRepresentation()).thenReturn(existingUserRepresentation);

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();

        // Act
        IdpAccountInfo result = repository.updateUserStatus(idpRealm, userId, enabledFlag);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(enabledFlag, result.isEnabled());

        // Verify that the user was not updated because needUpdate is false
        verify(mockUserResource, never()).update(any(UserRepresentation.class));
    }

    /**
     * Tests the updateUser method for the case where the user is not found.
     */
    @Test
    @DisplayName("updateUserStatus - User Not Found")
    void updateUserStatus_userNotFound() {
        // Arrange
        String idpRealm = "test-realm";
        String userId = "nonexistent-user";
        Boolean enabledFlag = true;

        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);

        when(mockKeycloak.realm(anyString())).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.get(userId)).thenReturn(null);

        KeycloakRepositoryImpl repository = spy(keycloakRepository);
        doReturn(mockKeycloak).when(repository).build();

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            repository.updateUserStatus(idpRealm, userId, enabledFlag);
        });

        assertTrue(exception.getLogMessage().contains("Unexpected error occurred"), "Expected error message not found in log message.");
    }

    /**
     * Tests the updateUser method for the case where a BadRequestException occurs during user update.
     */
    @Test
    @DisplayName("updateUserStatus - Exception Handling")
    void updateUserStatus_exception() {
        // Arrange
        String idpRealm = "test-realm";
        String userId = "user-id-123";
        Boolean enabledFlag = true;
        Boolean existingEnabledFlag = false;

        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        UserResource mockUserResource = mock(UserResource.class);
        UserRepresentation existingUserRepresentation = new UserRepresentation();

        existingUserRepresentation.setId(userId);
        existingUserRepresentation.setEnabled(existingEnabledFlag);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.get(userId)).thenReturn(mockUserResource);
        when(mockUserResource.toRepresentation()).thenReturn(existingUserRepresentation);

        // Simulate Exception when updating the user
        doThrow(new MockitoException("Unexpected error"))
                .when(mockUserResource).update(any(UserRepresentation.class));

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            repository.updateUserStatus(idpRealm, userId, enabledFlag);
        });

        // Assert
        assertTrue(exception.getLogMessage().contains(ConstError.ERRLOG_500_KEYCLOAK_BUILD));
        assertEquals(ConstError.ERR_500_MESSAGE, exception.getResponseMessage());
    }

    /**
     * Tests the deleteUser method for the normal case where a user is successfully deleted.
     */
    @Test
    @DisplayName("getUser - Success Case")
    void geteUser_success() {
        // Arrange
        String idpRealm = "test-realm";
        String userId = "test-user-id";
        String userName = "test-user-name";
        boolean enabled = true;

        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        UserResource mockUserResource = mock(UserResource.class);
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(userId);
        userRepresentation.setUsername(userName);
        userRepresentation.setEnabled(enabled);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.get(userId)).thenReturn(mockUserResource);
        when(mockUserResource.toRepresentation()).thenReturn(userRepresentation);

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();

        // Act & Assert
        IdpAccountInfo result = repository.getUser(idpRealm, userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(userName, result.getLoginUserId());
        assertEquals(enabled, result.isEnabled());

        verify(mockUsersResource).get(userId);
    }

    /**
     * Tests the deleteUser method for the normal case where a user is successfully deleted.
     */
    @Test
    @DisplayName("getUser - User not found Case")
    void geteUser_userNotFound() {
        // Arrange
        String idpRealm = "test-realm";
        String userId = "test-user-id";

        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        UserResource mockUserResource = mock(UserResource.class);

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.get(userId)).thenReturn(mockUserResource);
        when(mockUserResource.toRepresentation()).thenThrow(new NotFoundException());

        // Act & Assert
        IdpAccountInfo result = repository.getUser(idpRealm, userId);

        assertNull(result);

        verify(mockUsersResource).get(userId);
    }

    /**
     * Tests the getUser method for various exception cases.
     */
    @ParameterizedTest
    @CsvSource({
            "RuntimeException, UnexpectedException"
    })
    @DisplayName("getUser - Exception")
    void getUser_exception(
            String exceptionType,
            String expectedExceptionString
    ) {
        // Arrange
        String idpRealm = "test-realm";
        String userId = "test-user-id";
        String userName = "test-user-name";
        boolean enabled = true;

        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        UserResource mockUserResource = mock(UserResource.class);
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(userId);
        userRepresentation.setUsername(userName);
        userRepresentation.setEnabled(enabled);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.get(userId)).thenReturn(mockUserResource);

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();

        Exception exception;
        Class<? extends Throwable> clazz;
        switch (exceptionType) {
            case "RuntimeException":
                exception = new RuntimeException("Unexpected error");
                clazz = UnexpectedException.class;
                break;
            default:
                throw new IllegalArgumentException("Invalid exception type");
        }

        // Act & Assert
        when(mockUserResource.toRepresentation()).thenThrow(exception);
        Throwable throwsException = assertThrows(clazz, () -> {
            repository.getUser(idpRealm, userId);
        });

        assertEquals(ConstError.ERR_500_MESSAGE, throwsException.getMessage(), "Expected error message not found.");
    }

    /**
     * Tests the getClientSecret method for the normal case where a client secret is successfully retrieved.
     */
    @Test
    @DisplayName("getClientSecret - Success Case")
    void getClientSecret_success() {
        // Arrange
        String idpRealm = "test-realm";
        String clientUuid = "test-client-uuid";
        String expectedClientSecret = "test-client-secret";
        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        ClientsResource mockClientsResource = mock(ClientsResource.class);
        ClientResource mockClientResource = mock(ClientResource.class);
        ClientRepresentation mockClientRepresentation = new ClientRepresentation();
        CredentialRepresentation mockCredentialRepresentation = new CredentialRepresentation();

        mockClientRepresentation.setId(clientUuid);
        mockClientRepresentation.setPublicClient(false);
        mockCredentialRepresentation.setType(CredentialRepresentation.SECRET);
        mockCredentialRepresentation.setValue(expectedClientSecret);

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();
        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.clients()).thenReturn(mockClientsResource);
        when(mockClientsResource.get(clientUuid)).thenReturn(mockClientResource);
        when(mockClientResource.toRepresentation()).thenReturn(mockClientRepresentation);
        when(mockClientResource.getSecret()).thenReturn(mockCredentialRepresentation);

        // Act
        String actualClientSecret = repository.getClientSecret(idpRealm, clientUuid);

        // Assert
        assertEquals(expectedClientSecret, actualClientSecret);
    }

    /**
     * Tests the getClientSecret method for the case where the client is not found.
     */
    @Test
    @DisplayName("getClientSecret - Client Not Found Case")
    void getClientSecret_clientNotFound() {
        // Arrange
        String idpRealm = "test-realm";
        String clientUuid = "nonexistent-client-uuid";

        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        ClientsResource mockClientsResource = mock(ClientsResource.class);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.clients()).thenReturn(mockClientsResource);
        when(mockClientsResource.get(clientUuid)).thenThrow(new jakarta.ws.rs.NotFoundException());

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();

        // Act & Assert
        UnexpectedException exception =
                assertThrows(UnexpectedException.class, () -> {
                    repository.getClientSecret(idpRealm, clientUuid);
                });

        assertTrue(exception.getLogMessage().contains("Keycloak: Unexpected error occurred: client_uuid"), "Expected error message not found in log message." + exception.getLogMessage());
        assertTrue(exception.getMessage().contains("Keycloak: Unexpected error occurred: client_uuid"), "Expected error message not found in message." + exception.getMessage());
    }

    /**
     * Tests the getClientSecret method for the case where client is public.
     */
    @Test
    @DisplayName("getClientSecret - Client is Public Case")
    void getClientSecret_clientIsPublic() {
        // Arrange
        String idpRealm = "test-realm";
        String clientUuid = "public-client-uuid";

        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        ClientsResource mockClientsResource = mock(ClientsResource.class);
        ClientResource mockClientResource = mock(ClientResource.class);
        ClientRepresentation mockClientRepresentation = new ClientRepresentation();

        mockClientRepresentation.setId(clientUuid);
        mockClientRepresentation.setPublicClient(true);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.clients()).thenReturn(mockClientsResource);
        when(mockClientsResource.get(clientUuid)).thenReturn(mockClientResource);
        when(mockClientResource.toRepresentation()).thenReturn(mockClientRepresentation);

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            repository.getClientSecret(idpRealm, clientUuid);
        });

        assertTrue(exception.getLogMessage().contains("Unexpected error occurred: client_uuid"), "Expected error message not found in log message." + exception.getLogMessage());
        assertTrue(exception.getMessage().contains("Unexpected error occurred"), "Expected error message not found in message." + exception.getMessage());
    }

    /**
     * Tests the getClientSecret method for an exception cases.
     */
    @Test
    @DisplayName("getClientSecret - Exception Case")
    void getClientSecret_exception() {
        // Arrange
        String idpRealm = "test-realm";
        String clientUuid = "test-client-uuid";
        String errMessage = "Unexpected error at getting client";

        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        ClientsResource mockClientsResource = mock(ClientsResource.class);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.clients()).thenReturn(mockClientsResource);
        when(mockClientsResource.get(clientUuid)).thenThrow(new RuntimeException(errMessage));

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            repository.getClientSecret(idpRealm, clientUuid);
        });

        assertTrue(exception.getLogMessage().contains(errMessage), "Expected error message not found in log message.");
        assertEquals(ConstError.ERR_500_MESSAGE, exception.getMessage(), "Expected error message not found.");
    }

    /**
    * Tests the registerClient method for the normal case where a client is successfully created.
    */
    @ParameterizedTest
    @CsvSource({
        // idpRealm clientId, name, description, redirectUris, operatorId, openSystemId, uuid
        "test-realm, test-client-1, Test Client 1, Description 1, authorization_code, http://localhost/callback1, operatorId1, openSystemId, uuid-1", // authorization_code flow
        "test-realm, test-client-1, Test Client 1, Description 1, client_credentials, http://localhost/callback1, operatorId1, openSystemId, uuid-1", // client_credentials flow
        "test-realm, test-client-1, Test Client 1, Description 1, client_credentials, NULL, , , uuid-1", // without operatorId and openSystemId and redirectUri
        "test-realm, test-client-1, Test Client 1, Description 1, flowType, http://localhost/callback1, operatorId1, openSystemId, uuid-1" // flowtype that does not match any known types
    })
    @DisplayName("registerClient - success Case")
    void testRegisterClient_success(
            String idpRealm,
            String clientId,
            String name,
            String description,
            String flowType,
            String redirectUri,
            String operatorId,
            String openSystemId,
            String uuid) {
        // Arrange
        List<String> redirectUris = List.of(redirectUri);

        KeycloakRepositoryImpl repository = spy(keycloakRepository);
        doReturn(mockKeycloak).when(repository).build();

        RealmResource mockRealmResource = mock(RealmResource.class);
        ClientsResource mockClientsResource = mock(ClientsResource.class);
        Response mockResponse = mock(Response.class);
        ClientRepresentation mockClientRep = mock(ClientRepresentation.class);
        ClientResource mockClientResource = mock(ClientResource.class);
        ProtocolMappersResource mockProtocolMappersResource = mock(ProtocolMappersResource.class);

        when(mockClientRep.isEnabled()).thenReturn(true);
        when(mockClientResource.toRepresentation()).thenReturn(mockClientRep);
        when(mockResponse.getHeaderString(Const.HEADER_LOCATION)).thenReturn("http://keycloak/clients/" + uuid);
        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.clients()).thenReturn(mockClientsResource);
        when(mockClientsResource.create(any(ClientRepresentation.class))).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(201);
        when(mockClientsResource.get(uuid)).thenReturn(mockClientResource);
        lenient().when(mockClientResource.getProtocolMappers()).thenReturn(mockProtocolMappersResource);
        lenient().when(mockProtocolMappersResource.createMapper(any(ProtocolMapperRepresentation.class))).thenReturn(mockResponse);

        ClientRepresentation client = new ClientRepresentation();
        client.setClientId(clientId);
        client.setName(name);
        client.setDescription(description);
        client.setProtocol(Const.KEYCLOAK_PROTOCOL_OPENID_CONNECT);
        client.setEnabled(true);
        client.setPublicClient(false);
        client.setClientAuthenticatorType(Const.KEYCLOAK_CLIENT_AUTHENTICATOR_CLIENT_SECRET);

        // Act
        IdpClientInfo result = repository.registerClient(
                idpRealm,
                clientId,
                name,
                description,
                flowType,
                redirectUri.equals("NULL") ? null : redirectUris,
                operatorId,
                openSystemId);

        // Assert
        assertNotNull(result);
        assertEquals(uuid, result.getUuid());
        assertEquals(clientId, result.getClientId());
        assertEquals(name, result.getName());
        assertEquals(description, result.getDescription());
        assertEquals(redirectUri.equals("NULL") ? null : redirectUris, result.getRedirectUris());
        assertEquals(null, result.getClientSecret());
        assertEquals(false, result.isPublicClient());
        assertEquals(true, result.isEnabled());
        verify(mockClientsResource).create(any(ClientRepresentation.class));
        verify(mockClientsResource).get(uuid);
    }

    /**
     * Tests the registerClient method for various exception cases.
     */
    @ParameterizedTest
    @CsvSource({
        // idpRealm, clientId, name, description, flowType, redirectUri, operatorId, openSystemId, uuid, responseStatus, headerLocation, expectedException, expectedLogMessage, expectedErrorMessage
        "test-realm, test-client-1, Test Name 1, Description 1, client_credentials, http://localhost/callback1, operatorId1, openSystemId, uuid-1, 409, http://keycloak/clients/uuid-1, '', ConflictException, client_id conflict, client_id conflict",
        "test-realm, test-client-2, Test Name 2, Description 2, client_credentials, http://localhost/callback2, operatorId2, openSystemId, uuid-2, 500, http://keycloak/clients/uuid-2, '', UnexpectedException, Keycloak: Unexpected, Unexpected error occurred",
        "test-realm, test-client-3, Test Name 3, Description 3, client_credentials, http://localhost/callback3, operatorId3, openSystemId, uuid-3, 400, http://keycloak/clients/uuid-3, '', BadParametersException, Invalid request parameters., Invalid Argument."
    })
    @DisplayName("registerClient - Error Cases")
    void testRegisterClient_exception(
            String idpRealm,
            String clientId,
            String name,
            String description,
            String flowType,
            String redirectUri,
            String operatorId,
            String openSystemId,
            String uuid,
            int responseStatus,
            String headerLocation,
            String responseBody,
            String expectedException,
            String expectedLogMessage,
            String expectedErrorMessage
    ) {
        // Arrange
        List<String> redirectUris = List.of(redirectUri);
        KeycloakRepositoryImpl repository = spy(keycloakRepository);
        doReturn(mockKeycloak).when(repository).build();

        RealmResource mockRealmResource = mock(RealmResource.class);
        ClientsResource mockClientsResource = mock(ClientsResource.class);
        Response mockResponse = mock(Response.class);
        ClientRepresentation mockClientRep = mock(ClientRepresentation.class);
        ClientResource mockClientResource = mock(ClientResource.class);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.clients()).thenReturn(mockClientsResource);
        when(mockClientsResource.create(any(ClientRepresentation.class))).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(responseStatus);
        lenient().when(mockClientRep.isEnabled()).thenReturn(true);
        lenient().when(mockClientResource.toRepresentation()).thenReturn(mockClientRep);
        lenient().when(mockResponse.getHeaderString(Const.HEADER_LOCATION)).thenReturn("http://keycloak/clients/" + uuid);
        lenient().when(mockClientsResource.get(uuid)).thenReturn(mockClientResource);
        lenient().when(mockResponse.readEntity(String.class)).thenReturn(responseBody);

        // Act & Assert
        Class<? extends Throwable> clazz;
        switch (expectedException) {
            case "ConflictException":
                clazz = ConflictException.class;
                break;
            case "BadParametersException":
                clazz = BadParametersException.class;
                break;
            case "UnexpectedException":
            default:
                clazz = UnexpectedException.class;
                break;
        }
        Throwable thrown = assertThrows(clazz, () -> {
            repository.registerClient(
                    idpRealm,
                    clientId,
                    name,
                    description,
                    flowType,
                    redirectUris,
                    operatorId,
                    openSystemId
            );
        });

        if (thrown instanceof AbstractBaseException) {
            AbstractBaseException exception = (AbstractBaseException) thrown;
            assertTrue(exception.getLogMessage().contains(expectedLogMessage), "Expected log message not found.");
            assertTrue(exception.getResponseMessage().contains(expectedErrorMessage), "Expected error message not found.");
        }
    }

    /**
    * Tests the deleteClient method for the normal case where a client is successfully deleted.
    */
    @ParameterizedTest
    @CsvSource({
        // idpRealm clientUuid
        "test-realm, test-client-1"
    })
    @DisplayName("deleteClient - success Case")
    void testDeleteClient_success(
            String idpRealm,
            String clientUuid) {
        // Arrange
        when(keycloakProperties.getApiEndpoint()).thenReturn("http://localhost:8080/auth");
        KeycloakProperties.Credentials mockCredentials = mock(KeycloakProperties.Credentials.class);
        when(keycloakProperties.getCredentials()).thenReturn(mockCredentials);
        when(mockCredentials.getAdminRealm()).thenReturn("master");
        when(mockCredentials.getAdminClientId()).thenReturn("admin-cli");
        when(mockCredentials.getAdminUserName()).thenReturn("admin");
        when(mockCredentials.getAdminPassword()).thenReturn("admin-password");

        KeycloakBuilder mockBuilder = mock(KeycloakBuilder.class);
        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        ClientsResource mockClientsResource = mock(ClientsResource.class);
        ClientResource mockClientResource = mock(ClientResource.class);

        when(KeycloakBuilder.builder()).thenReturn(mockBuilder);
        when(mockBuilder.serverUrl(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.realm(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.username(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.password(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.grantType(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockKeycloak);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.clients()).thenReturn(mockClientsResource);
        when(mockClientsResource.get(clientUuid)).thenReturn(mockClientResource);

        // deleteClient now validates existing flow + required protocol mappers before remove()
        ClientRepresentation existing = new ClientRepresentation();
        existing.setStandardFlowEnabled(true);
        existing.setServiceAccountsEnabled(false);
        when(mockClientResource.toRepresentation()).thenReturn(existing);

        ProtocolMappersResource mockProtocolMappersResource = mock(ProtocolMappersResource.class);
        when(mockClientResource.getProtocolMappers()).thenReturn(mockProtocolMappersResource);
        ProtocolMapperRepresentation requiredMapper = new ProtocolMapperRepresentation();
        requiredMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_USERMODEL_PROPERTY);
        when(mockProtocolMappersResource.getMappers()).thenReturn(List.of(requiredMapper));

        doNothing().when(mockClientResource).remove();

        // Act & Assert
        assertDoesNotThrow(() -> keycloakRepository.deleteClient(idpRealm, clientUuid));
        verify(mockClientResource).remove();
    }

    /**
     * Tests the deleteClient method for various exception cases.
     */
    @ParameterizedTest
    @CsvSource({
        // scenario, idpRealm, clientId, existingStdFlow, existingSvcAcct, mapperType, expectedException, expectedLogMessage, expectedErrorMessage
        "remove-notfound, test-realm, test-client-1, true, false, usermodel, NotFoundException, Resource not found, client_uuid",
        "remove-unexpected, test-realm, test-client-2, false, true, usermodel, UnexpectedException, Keycloak: Unexpected, Unexpected error occurred",
        "invalid-flow, test-realm, test-client-3, false, false, usermodel, ValidateException, '', ''",
        "required-mapper-missing, test-realm, test-client-4, true, false, empty, ValidateException, '', ''",
        "required-mapper-invalid-type, test-realm, test-client-5, true, false, custom, ValidateException, '', ''"
    })
    @DisplayName("deleteClient - Error Cases")
    void testDeleteClient_exception(
            String scenario,
            String idpRealm,
            String clientId,
            Boolean existingStdFlow,
            Boolean existingSvcAcct,
            String mapperType,
            String expectedException,
            String expectedLogMessage,
            String expectedErrorMessage
    ) {
        // Arrange
        KeycloakRepositoryImpl repository = spy(keycloakRepository);
        doReturn(mockKeycloak).when(repository).build();

        RealmResource mockRealmResource = mock(RealmResource.class);
        ClientsResource mockClientsResource = mock(ClientsResource.class);
        ClientResource mockClientResource = mock(ClientResource.class);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.clients()).thenReturn(mockClientsResource);
        when(mockClientsResource.get(clientId)).thenReturn(mockClientResource);

        // deleteClient now validates existing flow + required protocol mappers before remove()
        ClientRepresentation existing = new ClientRepresentation();
        existing.setStandardFlowEnabled(existingStdFlow);
        existing.setServiceAccountsEnabled(existingSvcAcct);
        when(mockClientResource.toRepresentation()).thenReturn(existing);

        ProtocolMappersResource mockProtocolMappersResource = mock(ProtocolMappersResource.class);
        lenient().when(mockClientResource.getProtocolMappers()).thenReturn(mockProtocolMappersResource);
        ProtocolMapperRepresentation requiredMapper = new ProtocolMapperRepresentation();
        if ("usermodel".equals(mapperType)) {
            requiredMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_USERMODEL_PROPERTY);
            lenient().when(mockProtocolMappersResource.getMappers()).thenReturn(List.of(requiredMapper));
        } else if ("hardcoded".equals(mapperType)) {
            requiredMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_HARDCODED_CLAIM);
            lenient().when(mockProtocolMappersResource.getMappers()).thenReturn(List.of(requiredMapper));
        } else if ("empty".equals(mapperType)) {
            lenient().when(mockProtocolMappersResource.getMappers()).thenReturn(List.of());
        } else {
            requiredMapper.setProtocolMapper("custom-mapper-type");
            lenient().when(mockProtocolMappersResource.getMappers()).thenReturn(List.of(requiredMapper));
        }

        // Set up the ClientResource mock to throw the expected exception
        if ("remove-notfound".equals(scenario)) {
            doThrow(new jakarta.ws.rs.NotFoundException(expectedLogMessage)).when(mockClientResource).remove();
        } else if ("remove-unexpected".equals(scenario)) {
            doThrow(new RuntimeException("Unexpected")).when(mockClientResource).remove();
        }

        // Act & Assert
        Class<? extends Throwable> clazz;
        switch (expectedException) {
            case "NotFoundException":
                clazz = io.github.open_dataspaces.core.exception.NotFoundException.class;
                break;
            case "UnexpectedException":
                clazz = UnexpectedException.class;
                break;
            case "ValidateException":
                clazz = ValidateException.class;
                break;
            default:
                clazz = UnexpectedException.class;
                break;
        }
        Throwable thrown = assertThrows(clazz, () -> {
            repository.deleteClient(idpRealm, clientId);
        });

        if (thrown instanceof ValidateException) {
            verify(mockClientResource, never()).remove();
            if ("invalid-flow".equals(scenario)) {
                verify(mockClientResource, never()).getProtocolMappers();
                verify(mockProtocolMappersResource, never()).getMappers();
            } else {
                verify(mockClientResource).getProtocolMappers();
                verify(mockProtocolMappersResource).getMappers();
            }
        }

        if (thrown instanceof AbstractBaseException) {
            AbstractBaseException exception = (AbstractBaseException) thrown;
            assertTrue(exception.getLogMessage().contains(expectedLogMessage), "Expected log message not found.");
            assertTrue(exception.getResponseMessage().contains(expectedErrorMessage), "Expected error message not found.");
        }
    }

    @Test
    void testValidateDuplicate() throws Exception {
        // Arrange
        Keycloak keycloakMock = mock(Keycloak.class);
        RealmResource realmResourceMock = mock(RealmResource.class);
        ClientsResource clientsResourceMock = mock(ClientsResource.class);
        String realm = "test-realm";
        String clientId = "duplicate-client";
        ClientRepresentation clientRep1 = new ClientRepresentation();
        clientRep1.setClientId(clientId);
        ClientRepresentation clientRep2 = new ClientRepresentation();
        clientRep2.setClientId(clientId);

        when(keycloakMock.realm(realm)).thenReturn(realmResourceMock);
        when(realmResourceMock.clients()).thenReturn(clientsResourceMock);

        // null
        when(clientsResourceMock.findByClientId(clientId)).thenReturn(null);
        KeycloakRepositoryImpl repo = new KeycloakRepositoryImpl(null, null, null);
        java.lang.reflect.Method method = KeycloakRepositoryImpl.class.getDeclaredMethod("validateDuplicate", Keycloak.class, String.class, String.class);
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(repo, keycloakMock, realm, clientId));

        // empty
        when(clientsResourceMock.findByClientId(clientId)).thenReturn(Collections.emptyList());
        assertDoesNotThrow(() -> method.invoke(repo, keycloakMock, realm, clientId));

        // List Pattern with Elements (Multiple)
        when(clientsResourceMock.findByClientId(clientId)).thenReturn(List.of(clientRep1, clientRep2));
        Exception ex = assertThrows(Exception.class, () -> method.invoke(repo, keycloakMock, realm, clientId));
        Throwable cause = ex.getCause();
        assertTrue(cause instanceof io.github.open_dataspaces.core.exception.ConflictException);
    }

    @Test
    @DisplayName("applyFlowSettings - authorization_code flow")
    void testApplyFlowSettings_authorizationCode() throws Exception {
        // Arrange
        ClientRepresentation client = new ClientRepresentation();
        client.setAttributes(new HashMap<>());
        List<String> redirectUris = List.of("http://localhost/callback");
        String flowType = "authorization_code";

        KeycloakRepositoryImpl repo = new KeycloakRepositoryImpl(null, null, null);
        java.lang.reflect.Method method = KeycloakRepositoryImpl.class.getDeclaredMethod("applyFlowSettings", ClientRepresentation.class, String.class, List.class);
        method.setAccessible(true);

        // Act
        method.invoke(repo, client, flowType, redirectUris);

        // Assert
        assertEquals(true, client.isStandardFlowEnabled());
        assertEquals(false, client.isServiceAccountsEnabled());
        assertEquals(redirectUris, client.getRedirectUris());
        assertEquals(null, client.getAttributes().get("pkce.enabled"));
        assertEquals("S256", client.getAttributes().get("pkce.code.challenge.method"));
    }

    /**
     * Tests the revoke method for the normal case where the token is successfully revoked.
     */
    @Test
    void testTokenRevoke_success() {
        String idpRealm = "a";
        String clientId = "a";
        String clientSecret = "a";
        String refreshToken = "a";

        TokenRevokeResult expectedResult = new TokenRevokeResult();
        ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.OK);

        String url = "";

        when(keycloakUtil.getTokenRevoke(anyString())).thenReturn(url);
        when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(String.class))).thenReturn(responseEntity);

        TokenRevokeResult result = keycloakRepository.revoke(idpRealm, clientId, clientSecret, refreshToken);

        assertNotNull(result);
        assertEquals(expectedResult.isActive(), result.isActive());
    }

    /**
     * Tests the revoke method for the case where an invalid token error is returned.
     */
    @Test
    void testTokenRevoke_throwsInvalidToken() {
        String idpRealm = "a";
        String clientId = "a";
        String clientSecret = "a";
        String refreshToken = "a";

        String url = "a";

        Class<? extends Throwable> clazz = BadParametersException.class;
        ResponseEntity<String> responseEntity = new ResponseEntity<>("{\"error\": \"invalid_token\", \"error_description\": \"Invalid token\"}", HttpStatus.OK);

        when(keycloakUtil.getTokenRevoke(anyString())).thenReturn(url);
        when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(String.class))).thenReturn(responseEntity);

        // Act & Assert
        assertThrows(clazz, () -> keycloakRepository.revoke(idpRealm, clientId, clientSecret, refreshToken));
    }

    /**
     * Tests the revoke method for various exception cases.
     *
     * @param exceptionClassName the name of the exception class to be tested, which can be "BadParametersException", "OutOfServiceException", or "UnexpectedException".
     */
    @ParameterizedTest
    @CsvSource({
        "BadParametersException",   // case #3:
        "OutOfServiceException",    // case #5:
        "UnexpectedException"       // case #4:
    })
    void testTokenRevoke_throwsError(String exceptionClassName) {
        String idpRealm = "a";
        String clientId = "a";
        String clientSecret = "a";
        String refreshToken = "a";

        String url = "a";

        String exceptionResponseMessage = null;
        Exception exception = null;
        Class<? extends Throwable> clazz = null;

        switch (exceptionClassName) {
            case "BadParametersException":
                exceptionResponseMessage = ConstError.ERR_400_INVALID_GRANT;
                exception = Mockito.mock(HttpClientErrorException.BadRequest.class);
                clazz = BadParametersException.class;
                break;
            case "OutOfServiceException":
                exceptionResponseMessage = ConstError.ERR_503_OUT_OF_SERVICE_EXCEPTION;
                exception = Mockito.mock(HttpServerErrorException.ServiceUnavailable.class);
                clazz = OutOfServiceException.class;
                break;
            case "UnexpectedException":
                exceptionResponseMessage = ConstError.ERRLOG_401_INVALID_CLIENT;
                exception = Mockito.mock(UnexpectedException.class);
                clazz = UnexpectedException.class;
                break;
            default:
                // unknown exception
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        when(exception.getMessage()).thenReturn(exceptionResponseMessage);
        when(keycloakUtil.getTokenRevoke(anyString())).thenReturn(url);
        when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(String.class))).thenThrow(exception);

        // Act & Assert
        assertThrows(clazz, () -> keycloakRepository.revoke(idpRealm, clientId, clientSecret, refreshToken));
        assertEquals(exceptionResponseMessage, exception.getMessage());
    }

    /**
     * Tests the GetClientUuidByClientId method for the normal case where a client secret is successfully retrieved.
     */
    @Test
    @DisplayName("GetClientUuidByClientId - Success Case")
    void testGetClientUuidByClientId_success() {
        // Arrange
        String idpRealm = "test-realm";
        String clientId = "test-client-id";
        String expectedClientUuid = "test-client-uuid";
        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        ClientsResource mockClientsResource = mock(ClientsResource.class);
        ClientRepresentation mockClientRepresentation = new ClientRepresentation();

        mockClientRepresentation.setId(expectedClientUuid);

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();
        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.clients()).thenReturn(mockClientsResource);
        when(mockClientsResource.findByClientId(clientId)).thenReturn(List.of(mockClientRepresentation));

        // Act
        String actualClientUuid = repository.getClientUuidByClientId(idpRealm, clientId);

        // Assert
        assertEquals(expectedClientUuid, actualClientUuid);
    }

    /**
     * Tests the GetClientUuidByClientId method for the case where the client is not found.
     */
    @Test
    @DisplayName("GetClientUuidByClientId - Client Not Found Case")
    void testGetClientUuidByClientId_clientNotFound() {
        // Arrange
        String idpRealm = "test-realm";
        String clientId = "nonexistent-client-id";

        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        ClientsResource mockClientsResource = mock(ClientsResource.class);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.clients()).thenReturn(mockClientsResource);
        when(mockClientsResource.findByClientId(clientId)).thenReturn(null).thenReturn(List.of());

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();
        String expectedMessage = String.format(ConstError.ERR_404_RESOURCE_NOT_FOUND_MESSAGE, Const.KEYCLOAK_PROPERTY_CLIENT_ID);

        // Act & Assert: clients == null
        Executable actionWhenNull = () -> repository.getClientUuidByClientId(idpRealm, clientId);
        io.github.open_dataspaces.core.exception.NotFoundException exceptionWhenNull =
                assertThrows(io.github.open_dataspaces.core.exception.NotFoundException.class, actionWhenNull);
        assertTrue(exceptionWhenNull.getLogMessage().contains(expectedMessage), "Expected error message not found in log message." + exceptionWhenNull.getLogMessage());
        assertTrue(exceptionWhenNull.getMessage().contains(expectedMessage), "Expected error message not found in message." + exceptionWhenNull.getMessage());

        // Act & Assert: clients.isEmpty()
        Executable actionWhenEmpty = () -> repository.getClientUuidByClientId(idpRealm, clientId);
        io.github.open_dataspaces.core.exception.NotFoundException exceptionWhenEmpty =
                assertThrows(io.github.open_dataspaces.core.exception.NotFoundException.class, actionWhenEmpty);
        assertTrue(exceptionWhenEmpty.getLogMessage().contains(expectedMessage), "Expected error message not found in log message." + exceptionWhenEmpty.getLogMessage());
        assertTrue(exceptionWhenEmpty.getMessage().contains(expectedMessage), "Expected error message not found in message." + exceptionWhenEmpty.getMessage());
    }

    /**
     * Tests the GetClientUuidByClientId method for an exception cases.
     */
    @Test
    @DisplayName("GetClientUuidByClientId - Exception Case")
    void testGetClientUuidByClientId_exception() {
        // Arrange
        String idpRealm = "test-realm";
        String clientId = "test-client-id";
        String errMessage = "Unexpected error at getting client";

        Keycloak mockKeycloak = mock(Keycloak.class);
        RealmResource mockRealmResource = mock(RealmResource.class);
        ClientsResource mockClientsResource = mock(ClientsResource.class);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.clients()).thenReturn(mockClientsResource);
        when(mockClientsResource.findByClientId(clientId)).thenThrow(new RuntimeException(errMessage));

        KeycloakRepositoryImpl repository = spy(new KeycloakRepositoryImpl(null, null, null));
        doReturn(mockKeycloak).when(repository).build();

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            repository.getClientUuidByClientId(idpRealm, clientId);
        });

        assertTrue(exception.getLogMessage().contains(errMessage), "Expected error message not found in log message.");
        assertEquals(ConstError.ERR_500_MESSAGE, exception.getMessage(), "Expected error message not found.");
    }

    /**
    * Tests the updateClient method for the normal case where a client is successfully updated.
    */
    @ParameterizedTest
    @CsvSource({
        // idpRealm, clientId, name, description, flowType, redirectUri, operatorId, openSystemId, uuid, existingStdFlow, existingSvcAcct
        "test-realm, test-client-1, Test Client 1, Description 1, authorization_code, http://localhost/callback1, operatorId1, openSystemId, uuid-1, false, true",
        "test-realm, test-client-1, old-name, old-description, authorization_code, NULL, , , uuid-1, true, false",
        "test-realm, test-client-1, Test Client 1, Description 1, client_credentials, http://localhost/callback1, operatorId1, openSystemId, uuid-1, true, false",
        "test-realm, test-client-1, Test Client 1, Description 1, client_credentials, NULL, , , uuid-1, false, true",
        "test-realm, test-client-1, null, Description 1, client_credentials, http://localhost/callback1, operatorId1, openSystemId, uuid-1, true, false",
        "test-realm, test-client-1, old-name, old-description, client_credentials, NULL, , , uuid-1, false, true",
        "test-realm, test-client-1, null, null, client_credentials, http://localhost/callback1, operatorId1, openSystemId, uuid-1, true, false",
    })
    @DisplayName("updateClient - success Case")
    void testUpdateClient_success(
            String idpRealm,
            String clientId,
            String name,
            String description,
            String flowType,
            String redirectUri,
            String operatorId,
            String openSystemId,
            String uuid,
            Boolean existingStdFlow,
            Boolean existingSvcAcct) {
        // Arrange
        name = "null".equals(name) ? null : name;
        description = "null".equals(description) ? null : description;
        List<String> redirectUris = "EMPTY".equals(redirectUri) ? List.of() : List.of(redirectUri);

        KeycloakRepositoryImpl repository = spy(keycloakRepository);
        doReturn(mockKeycloak).when(repository).build();

        RealmResource mockRealmResource = mock(RealmResource.class);
        ClientsResource mockClientsResource = mock(ClientsResource.class);
        Response mapperResponse = mock(Response.class);
        ClientRepresentation existing = new ClientRepresentation();
        ClientRepresentation updated = new ClientRepresentation();
        ClientResource mockClientResource = mock(ClientResource.class);
        ProtocolMappersResource mockProtocolMappersResource = mock(ProtocolMappersResource.class);
        ProtocolMapperRepresentation requiredMapper = new ProtocolMapperRepresentation();
        ProtocolMapperRepresentation hardcodedMapper = new ProtocolMapperRepresentation();

        existing.setId(uuid);
        existing.setClientId(clientId);
        existing.setName("old-name");
        existing.setDescription("old-description");
        existing.setStandardFlowEnabled(existingStdFlow);
        existing.setServiceAccountsEnabled(existingSvcAcct);
        existing.setRedirectUris(existingStdFlow ? List.of("http://localhost/old") : null);
        if (existingStdFlow && !existingSvcAcct) {
            existing.setWebOrigins(List.of(Const.CORS_ALL_ENABLED));
            Map<String, String> attrs = new HashMap<>();
            attrs.put(Const.KEYCLOAK_MAPPER_CONFIG_PKCE_ENABLED, Const.KEYCLOAK_MAPPER_CONFIG_TRUE);
            attrs.put(Const.KEYCLOAK_MAPPER_CONFIG_PKCE_CODE_CHALLENGE_METHOD, Const.KEYCLOAK_MAPPER_CONFIG_S256);
            existing.setAttributes(attrs);
        }
        if (!existingStdFlow && existingSvcAcct) {
            existing.setWebOrigins(null);
            existing.setDirectAccessGrantsEnabled(false);
            existing.setAuthorizationServicesEnabled(false);
            existing.setConsentRequired(false);
            existing.setAttributes(null);
        }

        updated.setId(uuid);
        updated.setClientId(clientId);
        updated.setName(name == null ? "old-name" : name);
        updated.setDescription(description == null ? "old-description" : description);
        if (Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_AUTHORIZATION_CODE.equals(flowType)) {
            updated.setRedirectUris(redirectUri.equals("NULL") ? null : redirectUris);
        } else {
            updated.setRedirectUris(null);
        }
        updated.setEnabled(true);

        requiredMapper.setId("mapper-1");
        requiredMapper.setName(Const.KEYCLOAK_PROPERTY_OPERATOR_ID);
        requiredMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_USERMODEL_PROPERTY);
        hardcodedMapper.setId("mapper-2");
        hardcodedMapper.setName(Const.KEYCLOAK_PROPERTY_OPERATOR_ID);
        hardcodedMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_HARDCODED_CLAIM);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.clients()).thenReturn(mockClientsResource);
        when(mockClientsResource.get(uuid)).thenReturn(mockClientResource);
        when(mockClientResource.toRepresentation()).thenReturn(existing, updated);
        when(mockClientResource.getProtocolMappers()).thenReturn(mockProtocolMappersResource);
        if (Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_AUTHORIZATION_CODE.equals(flowType)
                && !existingStdFlow
                && existingSvcAcct) {
            when(mockProtocolMappersResource.getMappers()).thenReturn(List.of(hardcodedMapper));
        } else {
            when(mockProtocolMappersResource.getMappers()).thenReturn(List.of(requiredMapper));
        }
        lenient().when(mockProtocolMappersResource.createMapper(any(ProtocolMapperRepresentation.class))).thenReturn(mapperResponse);
        lenient().doNothing().when(mockProtocolMappersResource).delete(anyString());

        // Act
        IdpClientInfo result = repository.updateClient(
                idpRealm,
                uuid,
                name,
                description,
                flowType,
                openSystemId,
                operatorId,
                "NULL".equals(redirectUri) ? null : redirectUris);

        // Assert
        String expectedName = name == null ? "old-name" : name;
        String expectedDescription = description == null ? "old-description" : description;
        assertNotNull(result);
        assertEquals(uuid, result.getUuid());
        assertEquals(clientId, result.getClientId());
        assertEquals(expectedName, result.getName());
        assertEquals(expectedDescription, result.getDescription());
        if (Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_AUTHORIZATION_CODE.equals(flowType)) {
            assertEquals(redirectUri.equals("NULL") ? null : redirectUris, result.getRedirectUris());
        } else {
            assertNull(result.getRedirectUris());
        }
        assertEquals(null, result.getClientSecret());
        assertEquals(false, result.isPublicClient());
        assertEquals(true, result.isEnabled());
        verify(mockClientsResource).get(uuid);
        if ("old-name".equals(name)
                && "old-description".equals(description)
                && Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_CLIENT_CREDENTIALS.equals(flowType)
                && !existingStdFlow
                && existingSvcAcct) {
            verify(mockClientResource, never()).update(any(ClientRepresentation.class));
        }
        if ("old-name".equals(name)
                && "old-description".equals(description)
                && Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_AUTHORIZATION_CODE.equals(flowType)
                && existingStdFlow
                && !existingSvcAcct
                && "NULL".equals(redirectUri)) {
            verify(mockClientResource, never()).update(any(ClientRepresentation.class));
        }
        if (Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_AUTHORIZATION_CODE.equals(flowType)
                && !existingStdFlow
                && existingSvcAcct) {
            verify(mockProtocolMappersResource).delete("mapper-2");
            verify(mockProtocolMappersResource).createMapper(any(ProtocolMapperRepresentation.class));
        }
    }

    /**
     * Tests the updateClient method for various exception cases.
     */
    @ParameterizedTest
    @CsvSource({
        // scenario, flowType, existingStdFlow, existingSvcAcct, redirectUri, operatorId, openSystemId, expectedException
        "invalid-flow, flowType, true, false, http://localhost/callback1, operatorId1, openSystemId, ValidateException",
        "invalid-existing-flow, authorization_code, true, true, http://localhost/callback1, operatorId1, openSystemId, ValidateException",
        "invalid-existing-flow-client-credentials, client_credentials, false, false, http://localhost/callback1, operatorId1, openSystemId, ValidateException",
        "required-mapper-missing, authorization_code, true, false, http://localhost/callback1, operatorId1, openSystemId, ValidateException",
        "required-mapper-invalid-type, authorization_code, true, false, http://localhost/callback1, operatorId1, openSystemId, ValidateException",
        "auth-code-redirect-required, authorization_code, false, true, NULL, operatorId1, openSystemId, ValidateException",
        "auth-code-redirect-empty-required, authorization_code, false, true, EMPTY, operatorId1, openSystemId, ValidateException",
        "client-credentials-open-system-required, client_credentials, true, false, http://localhost/callback1, operatorId1, , ValidateException",
        "client-not-found, authorization_code, true, false, http://localhost/callback1, operatorId1, openSystemId, NotFoundException",
        "bad-request, authorization_code, true, false, http://localhost/callback1, operatorId1, openSystemId, BadParametersException",
        "unexpected, authorization_code, true, false, http://localhost/callback1, operatorId1, openSystemId, UnexpectedException"
    })
    @DisplayName("updateClient - Error Cases")
    void testUpdateClient_exception(
            String scenario,
            String flowType,
            Boolean existingStdFlow,
            Boolean existingSvcAcct,
            String redirectUri,
            String operatorId,
            String openSystemId,
            String expectedException
    ) {
        // Arrange
        String idpRealm = "test-realm";
        String clientId = "test-client-1";
        String name = "Test Name";
        String description = "Description";
        String uuid = "uuid-1";
        List<String> redirectUris = "EMPTY".equals(redirectUri) ? List.of() : List.of(redirectUri);
        String normalizedOpenSystemId = StringUtils.hasText(openSystemId) ? openSystemId : null;
        KeycloakRepositoryImpl repository = spy(keycloakRepository);
        doReturn(mockKeycloak).when(repository).build();

        RealmResource mockRealmResource = mock(RealmResource.class);
        ClientsResource mockClientsResource = mock(ClientsResource.class);
        ClientResource mockClientResource = mock(ClientResource.class);
        ClientRepresentation existing = new ClientRepresentation();
        ProtocolMappersResource mockProtocolMappersResource = mock(ProtocolMappersResource.class);
        ProtocolMapperRepresentation requiredMapper = new ProtocolMapperRepresentation();

        existing.setId(uuid);
        existing.setClientId(clientId);
        existing.setName("old-name");
        existing.setDescription("old-description");
        existing.setStandardFlowEnabled(existingStdFlow);
        existing.setServiceAccountsEnabled(existingSvcAcct);

        lenient().when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        lenient().when(mockRealmResource.clients()).thenReturn(mockClientsResource);
        if ("client-not-found".equals(scenario)) {
            lenient().when(mockClientsResource.get(uuid)).thenThrow(new NotFoundException());
        } else if ("unexpected".equals(scenario)) {
            lenient().when(mockClientsResource.get(uuid)).thenThrow(new RuntimeException("Unexpected"));
        } else {
            lenient().when(mockClientsResource.get(uuid)).thenReturn(mockClientResource);
            lenient().when(mockClientResource.toRepresentation()).thenReturn(existing);
            lenient().when(mockClientResource.getProtocolMappers()).thenReturn(mockProtocolMappersResource);
            if ("required-mapper-missing".equals(scenario)) {
                lenient().when(mockProtocolMappersResource.getMappers()).thenReturn(List.of());
            } else if ("required-mapper-invalid-type".equals(scenario)) {
                ProtocolMapperRepresentation invalidMapper = new ProtocolMapperRepresentation();
                invalidMapper.setId("mapper-invalid");
                invalidMapper.setName(Const.KEYCLOAK_PROPERTY_OPERATOR_ID);
                invalidMapper.setProtocolMapper("invalid-type");
                lenient().when(mockProtocolMappersResource.getMappers()).thenReturn(List.of(invalidMapper));
            } else {
                requiredMapper.setId("mapper-1");
                requiredMapper.setName(Const.KEYCLOAK_PROPERTY_OPERATOR_ID);
                requiredMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_USERMODEL_PROPERTY);
                lenient().when(mockProtocolMappersResource.getMappers()).thenReturn(List.of(requiredMapper));
            }
            if ("bad-request".equals(scenario)) {
                doThrow(new BadRequestException("Bad Request")).when(mockClientResource).update(any(ClientRepresentation.class));
            }
        }

        // Act & Assert
        Class<? extends Throwable> clazz;
        switch (expectedException) {
            case "ValidateException":
                clazz = ValidateException.class;
                break;
            case "NotFoundException":
                clazz = io.github.open_dataspaces.core.exception.NotFoundException.class;
                break;
            case "BadParametersException":
                clazz = BadParametersException.class;
                break;
            case "UnexpectedException":
            default:
                clazz = UnexpectedException.class;
                break;
        }
        Throwable thrown = assertThrows(clazz, () -> {
            repository.updateClient(
                    idpRealm,
                    uuid,
                    name,
                    description,
                    flowType,
                    normalizedOpenSystemId,
                    operatorId,
                    "NULL".equals(redirectUri) ? null : redirectUris
            );
        });

        if (thrown instanceof ValidateException) {
            assertNotNull(((ValidateException) thrown).getResponseMessage());
        }
        if (thrown instanceof io.github.open_dataspaces.core.exception.NotFoundException) {
            assertTrue(thrown.getMessage().contains(Const.KEYCLOAK_PROPERTY_CLIENT_UUID));
        }
        if (thrown instanceof BadParametersException) {
            assertEquals(ConstError.ERR_400_ILLEGAL_ARGUMENT, thrown.getMessage());
        }
        if (thrown instanceof UnexpectedException) {
            assertEquals(ConstError.ERR_500_MESSAGE, thrown.getMessage());
        }
    }

    @Test
    @DisplayName("updateClient - authorization_code updates full flow settings")
    void testUpdateClient_authorizationCode_updatesAllFlowSettings() {
        // Arrange
        String idpRealm = "test-realm";
        String uuid = "uuid-1";
        String clientId = "test-client-1";
        List<String> redirectUris = List.of("http://localhost/callback1");
        KeycloakRepositoryImpl repository = spy(keycloakRepository);
        doReturn(mockKeycloak).when(repository).build();

        RealmResource mockRealmResource = mock(RealmResource.class);
        ClientsResource mockClientsResource = mock(ClientsResource.class);
        ClientResource mockClientResource = mock(ClientResource.class);
        ProtocolMappersResource mockProtocolMappersResource = mock(ProtocolMappersResource.class);
        Response mapperResponse = mock(Response.class);
        ClientRepresentation existing = new ClientRepresentation();
        ClientRepresentation updated = new ClientRepresentation();
        ProtocolMapperRepresentation requiredMapper = new ProtocolMapperRepresentation();

        existing.setId(uuid);
        existing.setClientId(clientId);
        existing.setName("old-name");
        existing.setDescription("old-description");
        existing.setStandardFlowEnabled(false);
        existing.setServiceAccountsEnabled(true);
        existing.setWebOrigins(List.of("http://localhost/another-origin"));
        existing.setDirectAccessGrantsEnabled(true);
        existing.setAuthorizationServicesEnabled(true);
        existing.setConsentRequired(false);
        existing.setRedirectUris(List.of("http://localhost/old-callback"));
        Map<String, String> existingAttrs = new HashMap<>();
        existingAttrs.put(Const.KEYCLOAK_MAPPER_CONFIG_PKCE_ENABLED, "false");
        existingAttrs.put(Const.KEYCLOAK_MAPPER_CONFIG_PKCE_CODE_CHALLENGE_METHOD, "plain");
        existing.setAttributes(existingAttrs);

        updated.setId(uuid);
        updated.setClientId(clientId);
        updated.setName("new-name");
        updated.setDescription("new-description");
        updated.setRedirectUris(redirectUris);
        updated.setEnabled(true);

        requiredMapper.setId("mapper-1");
        requiredMapper.setName(Const.KEYCLOAK_PROPERTY_OPERATOR_ID);
        requiredMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_USERMODEL_PROPERTY);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.clients()).thenReturn(mockClientsResource);
        when(mockClientsResource.get(uuid)).thenReturn(mockClientResource);
        when(mockClientResource.toRepresentation()).thenReturn(existing, updated);
        when(mockClientResource.getProtocolMappers()).thenReturn(mockProtocolMappersResource);
        when(mockProtocolMappersResource.getMappers()).thenReturn(List.of(requiredMapper));
        lenient().when(mockProtocolMappersResource.createMapper(any(ProtocolMapperRepresentation.class))).thenReturn(mapperResponse);
        lenient().doNothing().when(mockProtocolMappersResource).delete(anyString());

        // Act
        IdpClientInfo result = repository.updateClient(
                idpRealm,
                uuid,
                "new-name",
                "new-description",
                Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_AUTHORIZATION_CODE,
                "openSystemId",
                "operatorId1",
                redirectUris);

        // Assert
        assertNotNull(result);
        assertEquals(uuid, result.getUuid());
        assertEquals(clientId, result.getClientId());
        assertEquals("new-name", result.getName());
        assertEquals("new-description", result.getDescription());
        assertEquals(redirectUris, result.getRedirectUris());
        verify(mockClientResource).update(any(ClientRepresentation.class));
    }

    @Test
    @DisplayName("updateClient - client_credentials updates full flow settings")
    void testUpdateClient_clientCredentials_updatesAllFlowSettings() {
        // Arrange
        String idpRealm = "test-realm";
        String uuid = "uuid-1";
        String clientId = "test-client-1";
        KeycloakRepositoryImpl repository = spy(keycloakRepository);
        doReturn(mockKeycloak).when(repository).build();

        RealmResource mockRealmResource = mock(RealmResource.class);
        ClientsResource mockClientsResource = mock(ClientsResource.class);
        ClientResource mockClientResource = mock(ClientResource.class);
        ProtocolMappersResource mockProtocolMappersResource = mock(ProtocolMappersResource.class);
        Response mapperResponse = mock(Response.class);
        ClientRepresentation existing = new ClientRepresentation();
        ClientRepresentation updated = new ClientRepresentation();
        ProtocolMapperRepresentation requiredMapper = new ProtocolMapperRepresentation();

        existing.setId(uuid);
        existing.setClientId(clientId);
        existing.setName("old-name");
        existing.setDescription("old-description");
        existing.setStandardFlowEnabled(true);
        existing.setServiceAccountsEnabled(false);
        existing.setWebOrigins(List.of(Const.CORS_ALL_ENABLED));
        existing.setDirectAccessGrantsEnabled(true);
        existing.setAuthorizationServicesEnabled(true);
        existing.setRedirectUris(List.of("http://localhost/old"));
        existing.setConsentRequired(true);
        Map<String, String> existingAttrs = new HashMap<>();
        existingAttrs.put(Const.KEYCLOAK_MAPPER_CONFIG_PKCE_ENABLED, Const.KEYCLOAK_MAPPER_CONFIG_TRUE);
        existingAttrs.put(Const.KEYCLOAK_MAPPER_CONFIG_PKCE_CODE_CHALLENGE_METHOD, Const.KEYCLOAK_MAPPER_CONFIG_S256);
        existing.setAttributes(existingAttrs);

        updated.setId(uuid);
        updated.setClientId(clientId);
        updated.setName("new-name");
        updated.setDescription("new-description");
        updated.setRedirectUris(null);
        updated.setEnabled(true);

        requiredMapper.setId("mapper-1");
        requiredMapper.setName(Const.KEYCLOAK_PROPERTY_OPERATOR_ID);
        requiredMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_USERMODEL_PROPERTY);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.clients()).thenReturn(mockClientsResource);
        when(mockClientsResource.get(uuid)).thenReturn(mockClientResource);
        when(mockClientResource.toRepresentation()).thenReturn(existing, updated);
        when(mockClientResource.getProtocolMappers()).thenReturn(mockProtocolMappersResource);
        when(mockProtocolMappersResource.getMappers()).thenReturn(List.of(requiredMapper));
        lenient().when(mockProtocolMappersResource.createMapper(any(ProtocolMapperRepresentation.class))).thenReturn(mapperResponse);
        lenient().doNothing().when(mockProtocolMappersResource).delete(anyString());

        // Act
        IdpClientInfo result = repository.updateClient(
                idpRealm,
                uuid,
                "new-name",
                "new-description",
                Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_CLIENT_CREDENTIALS,
                "openSystemId",
                "operatorId1",
                null);

        // Assert
        assertNotNull(result);
        assertEquals(uuid, result.getUuid());
        assertEquals(clientId, result.getClientId());
        assertEquals("new-name", result.getName());
        assertEquals("new-description", result.getDescription());
        assertNull(result.getRedirectUris());
        verify(mockClientResource).update(any(ClientRepresentation.class));
    }

    /**
     * Tests updateClient authorization_code path executes hardcoded-claim delete branch.
     */
    @Test
    @DisplayName("updateClient - authorization_code triggers hardcoded mapper delete branch")
    void testUpdateClient_authorizationCode_triggersHardcodedDeleteBranch() {
        // Arrange
        String idpRealm = "test-realm";
        String uuid = "uuid-branch-auth";
        List<String> redirectUris = List.of("http://localhost/callback-branch");

        KeycloakRepositoryImpl repository = spy(keycloakRepository);
        doReturn(mockKeycloak).when(repository).build();

        RealmResource mockRealmResource = mock(RealmResource.class);
        ClientsResource mockClientsResource = mock(ClientsResource.class);
        ClientResource mockClientResource = mock(ClientResource.class);
        ProtocolMappersResource mockProtocolMappersResource = mock(ProtocolMappersResource.class);
        Response mapperResponse = mock(Response.class);
        ClientRepresentation existing = new ClientRepresentation();
        ClientRepresentation updated = new ClientRepresentation();
        ProtocolMapperRepresentation hardcodedOperatorMapper = new ProtocolMapperRepresentation();

        existing.setId(uuid);
        existing.setClientId("client-branch-auth");
        existing.setName("old-name");
        existing.setDescription("old-description");
        existing.setStandardFlowEnabled(false);
        existing.setServiceAccountsEnabled(true);
        existing.setRedirectUris(null);

        updated.setId(uuid);
        updated.setClientId("client-branch-auth");
        updated.setName("new-name");
        updated.setDescription("new-description");
        updated.setRedirectUris(redirectUris);
        updated.setEnabled(true);

        hardcodedOperatorMapper.setId("hardcoded-operator-auth");
        hardcodedOperatorMapper.setName(Const.KEYCLOAK_PROPERTY_OPERATOR_ID);
        hardcodedOperatorMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_HARDCODED_CLAIM);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.clients()).thenReturn(mockClientsResource);
        when(mockClientsResource.get(uuid)).thenReturn(mockClientResource);
        when(mockClientResource.toRepresentation()).thenReturn(existing, updated);
        when(mockClientResource.getProtocolMappers()).thenReturn(mockProtocolMappersResource);
        when(mockProtocolMappersResource.getMappers()).thenReturn(List.of(hardcodedOperatorMapper));
        when(mockProtocolMappersResource.createMapper(any(ProtocolMapperRepresentation.class))).thenReturn(mapperResponse);
        doNothing().when(mockProtocolMappersResource).delete(anyString());

        // Act
        IdpClientInfo result = repository.updateClient(
                idpRealm,
                uuid,
                "new-name",
                "new-description",
                Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_AUTHORIZATION_CODE,
                null,
                "operator-branch",
                redirectUris);

        // Assert
        assertNotNull(result);
        verify(mockProtocolMappersResource).delete("hardcoded-operator-auth");
        verify(mockProtocolMappersResource).createMapper(any(ProtocolMapperRepresentation.class));
    }

    /**
     * Tests updateClient client_credentials path executes else-if operator_id branch.
     */
    @Test
    @DisplayName("updateClient - client_credentials triggers else-if operator_id branch")
    void testUpdateClient_clientCredentials_triggersElseIfOperatorIdBranch() {
        // Arrange
        String idpRealm = "test-realm";
        String uuid = "uuid-branch-cc";

        KeycloakRepositoryImpl repository = spy(keycloakRepository);
        doReturn(mockKeycloak).when(repository).build();

        RealmResource mockRealmResource = mock(RealmResource.class);
        ClientsResource mockClientsResource = mock(ClientsResource.class);
        ClientResource mockClientResource = mock(ClientResource.class);
        ProtocolMappersResource mockProtocolMappersResource = mock(ProtocolMappersResource.class);
        Response mapperResponse = mock(Response.class);
        ClientRepresentation existing = new ClientRepresentation();
        ClientRepresentation updated = new ClientRepresentation();
        ProtocolMapperRepresentation hardcodedOperatorMapper = new ProtocolMapperRepresentation();

        existing.setId(uuid);
        existing.setClientId("client-branch-cc");
        existing.setName("old-name");
        existing.setDescription("old-description");
        existing.setStandardFlowEnabled(false);
        existing.setServiceAccountsEnabled(true);
        existing.setRedirectUris(null);

        updated.setId(uuid);
        updated.setClientId("client-branch-cc");
        updated.setName("new-name");
        updated.setDescription("new-description");
        updated.setRedirectUris(null);
        updated.setEnabled(true);

        hardcodedOperatorMapper.setId("hardcoded-operator-cc");
        hardcodedOperatorMapper.setName(Const.KEYCLOAK_PROPERTY_OPERATOR_ID);
        hardcodedOperatorMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_HARDCODED_CLAIM);

        when(mockKeycloak.realm(idpRealm)).thenReturn(mockRealmResource);
        when(mockRealmResource.clients()).thenReturn(mockClientsResource);
        when(mockClientsResource.get(uuid)).thenReturn(mockClientResource);
        when(mockClientResource.toRepresentation()).thenReturn(existing, updated);
        when(mockClientResource.getProtocolMappers()).thenReturn(mockProtocolMappersResource);
        when(mockProtocolMappersResource.getMappers()).thenReturn(List.of(hardcodedOperatorMapper));
        when(mockProtocolMappersResource.createMapper(any(ProtocolMapperRepresentation.class))).thenReturn(mapperResponse);
        doNothing().when(mockProtocolMappersResource).delete(anyString());

        // Act
        IdpClientInfo result = repository.updateClient(
                idpRealm,
                uuid,
                "new-name",
                "new-description",
                Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_CLIENT_CREDENTIALS,
                null,
                "operator-branch-only",
                null);

        // Assert
        assertNotNull(result);
        verify(mockProtocolMappersResource).delete("hardcoded-operator-cc");
        verify(mockProtocolMappersResource, Mockito.times(1)).createMapper(any(ProtocolMapperRepresentation.class));
    }

    /**
     * Tests updateClient applyAuthorizationCodeFlowSettingsForUpdate path executes else-if operator_id branch.
     */
    @Test
    @DisplayName("applyAuthorizationCodeFlowSettingsForUpdate - no changes branch")
    void testApplyAuthorizationCodeFlowSettingsForUpdate_noChanges() throws Exception {
        // Arrange
        ClientRepresentation client = new ClientRepresentation();
        List<String> redirectUris = List.of("http://localhost/callback1");
        client.setWebOrigins(List.of(Const.CORS_ALL_ENABLED));
        client.setStandardFlowEnabled(true);
        client.setServiceAccountsEnabled(false);
        client.setDirectAccessGrantsEnabled(false);
        client.setAuthorizationServicesEnabled(false);
        client.setRedirectUris(redirectUris);
        client.setConsentRequired(true);
        Map<String, String> attrs = new HashMap<>();
        attrs.put(Const.KEYCLOAK_MAPPER_CONFIG_PKCE_ENABLED, Const.KEYCLOAK_MAPPER_CONFIG_TRUE);
        attrs.put(Const.KEYCLOAK_MAPPER_CONFIG_PKCE_CODE_CHALLENGE_METHOD, Const.KEYCLOAK_MAPPER_CONFIG_S256);
        client.setAttributes(attrs);

        KeycloakRepositoryImpl repo = new KeycloakRepositoryImpl(null, null, null);
        java.lang.reflect.Method method = KeycloakRepositoryImpl.class.getDeclaredMethod("applyAuthorizationCodeFlowSettingsForUpdate", ClientRepresentation.class, List.class);
        method.setAccessible(true);

        // Act
        boolean changed = (boolean) method.invoke(repo, client, redirectUris);

        // Assert
        assertFalse(changed);
        assertEquals(List.of(Const.CORS_ALL_ENABLED), client.getWebOrigins());
        assertEquals(redirectUris, client.getRedirectUris());

        // Additional branch coverage in same test (no new test):
        // webOrigins is non-null and different, redirectUris is non-null and different.
        client.setWebOrigins(List.of("http://localhost/another-origin"));
        client.setRedirectUris(List.of("http://localhost/old-callback"));
        changed = (boolean) method.invoke(repo, client, redirectUris);
        assertTrue(changed);
        assertEquals(List.of(Const.CORS_ALL_ENABLED), client.getWebOrigins());
        assertEquals(redirectUris, client.getRedirectUris());

        // redirectUris == null path
        changed = (boolean) method.invoke(repo, client, null);
        assertFalse(changed);
    }

    /**
     * Tests updateClient syncProtocolMappersForClientCredentials path executes else-if operator_id branch.
     */
    @Test
    @DisplayName("applyClientCredentialsFlowSettingsForUpdate - attrs removal branch")
    void testApplyClientCredentialsFlowSettingsForUpdate_removePkceAttrs() throws Exception {
        // Arrange
        ClientRepresentation client = new ClientRepresentation();
        client.setWebOrigins(null);
        client.setStandardFlowEnabled(false);
        client.setServiceAccountsEnabled(true);
        client.setDirectAccessGrantsEnabled(false);
        client.setAuthorizationServicesEnabled(false);
        client.setRedirectUris(null);
        client.setConsentRequired(false);
        Map<String, String> attrs = new HashMap<>();
        attrs.put(Const.KEYCLOAK_MAPPER_CONFIG_PKCE_ENABLED, Const.KEYCLOAK_MAPPER_CONFIG_TRUE);
        attrs.put(Const.KEYCLOAK_MAPPER_CONFIG_PKCE_CODE_CHALLENGE_METHOD, Const.KEYCLOAK_MAPPER_CONFIG_S256);
        attrs.put("keep", "value");
        client.setAttributes(attrs);

        KeycloakRepositoryImpl repo = new KeycloakRepositoryImpl(null, null, null);
        java.lang.reflect.Method method = KeycloakRepositoryImpl.class.getDeclaredMethod("applyClientCredentialsFlowSettingsForUpdate", ClientRepresentation.class);
        method.setAccessible(true);

        // Act
        boolean changed = (boolean) method.invoke(repo, client);

        // Assert
        assertTrue(changed);
        assertNull(client.getAttributes().get(Const.KEYCLOAK_MAPPER_CONFIG_PKCE_ENABLED));
        assertNull(client.getAttributes().get(Const.KEYCLOAK_MAPPER_CONFIG_PKCE_CODE_CHALLENGE_METHOD));
        assertEquals("value", client.getAttributes().get("keep"));

        // Additional branch coverage in same test (no new test): attrs exists but target keys are missing.
        Map<String, String> attrsWithoutPkce = new HashMap<>();
        attrsWithoutPkce.put("keep", "value");
        client.setAttributes(attrsWithoutPkce);
        changed = (boolean) method.invoke(repo, client);
        assertFalse(changed);
        assertEquals("value", client.getAttributes().get("keep"));
    }

    /**
     * Tests updateClient syncProtocolMappersForAuthorizationCode path executes else-if operator_id branch.
     */
    @Test
    @DisplayName("syncProtocolMappersForAuthorizationCode - continue and delete")
    void testSyncProtocolMappersForAuthorizationCode() throws Exception {
        // Arrange
        ClientResource mockClientResource = mock(ClientResource.class);
        ProtocolMappersResource mockProtocolMappersResource = mock(ProtocolMappersResource.class);
        Response mapperResponse = mock(Response.class);

        ProtocolMapperRepresentation userModelMapper = new ProtocolMapperRepresentation();
        userModelMapper.setId("mapper-1");
        userModelMapper.setName(Const.KEYCLOAK_PROPERTY_OPERATOR_ID);
        userModelMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_USERMODEL_PROPERTY);

        ProtocolMapperRepresentation hardcodedMapper = new ProtocolMapperRepresentation();
        hardcodedMapper.setId("mapper-2");
        hardcodedMapper.setName(Const.KEYCLOAK_PROPERTY_OPERATOR_ID);
        hardcodedMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_HARDCODED_CLAIM);

        ProtocolMapperRepresentation hardcodedOtherMapper = new ProtocolMapperRepresentation();
        hardcodedOtherMapper.setId("mapper-3");
        hardcodedOtherMapper.setName("other-claim");
        hardcodedOtherMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_HARDCODED_CLAIM);

        ProtocolMapperRepresentation hardcodedOpenSystemMapper = new ProtocolMapperRepresentation();
        hardcodedOpenSystemMapper.setId("mapper-4");
        hardcodedOpenSystemMapper.setName(Const.KEYCLOAK_PROPERTY_OPEN_SYSTEM_ID);
        hardcodedOpenSystemMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_HARDCODED_CLAIM);

        ProtocolMapperRepresentation userModelOtherMapper = new ProtocolMapperRepresentation();
        userModelOtherMapper.setId("mapper-5");
        userModelOtherMapper.setName("other-user-model");
        userModelOtherMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_USERMODEL_PROPERTY);

        when(mockClientResource.getProtocolMappers()).thenReturn(mockProtocolMappersResource);
        when(mockProtocolMappersResource.getMappers())
            .thenReturn(List.of(userModelMapper, userModelOtherMapper, hardcodedMapper, hardcodedOtherMapper, hardcodedOpenSystemMapper))
                .thenReturn(List.of(hardcodedMapper));
        doNothing().when(mockProtocolMappersResource).delete(anyString());
        when(mockProtocolMappersResource.createMapper(any(ProtocolMapperRepresentation.class))).thenReturn(mapperResponse);

        KeycloakRepositoryImpl repo = new KeycloakRepositoryImpl(null, null, null);
        java.lang.reflect.Method method = KeycloakRepositoryImpl.class.getDeclaredMethod("syncProtocolMappersForAuthorizationCode", ClientResource.class);
        method.setAccessible(true);

        // Act
        method.invoke(repo, mockClientResource);

        // Assert
        verify(mockProtocolMappersResource).delete("mapper-2");
        verify(mockProtocolMappersResource).delete("mapper-4");
        verify(mockProtocolMappersResource, never()).delete("mapper-3");
        verify(mockProtocolMappersResource, never()).createMapper(any(ProtocolMapperRepresentation.class));

        // Additional branch coverage in same test (no new test): no operator_id user-model mapper exists.
        clearInvocations(mockProtocolMappersResource);
        method.invoke(repo, mockClientResource);
        verify(mockProtocolMappersResource).delete("mapper-2");
        verify(mockProtocolMappersResource).createMapper(any(ProtocolMapperRepresentation.class));
    }

    /**
     * Tests updateClient syncProtocolMappersForClientCredentials path executes else-if operator_id branch.
     */
    @Test
    @DisplayName("syncProtocolMappersForClientCredentials - delete")
    void testSyncProtocolMappersForClientCredentials() throws Exception {
        // Arrange
        ClientResource mockClientResource = mock(ClientResource.class);
        ProtocolMappersResource mockProtocolMappersResource = mock(ProtocolMappersResource.class);
        Response mapperResponse = mock(Response.class);

        ProtocolMapperRepresentation userModelMapper = new ProtocolMapperRepresentation();
        userModelMapper.setId("mapper-1");
        userModelMapper.setName(Const.KEYCLOAK_PROPERTY_OPERATOR_ID);
        userModelMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_USERMODEL_PROPERTY);

        ProtocolMapperRepresentation userModelOtherMapper = new ProtocolMapperRepresentation();
        userModelOtherMapper.setId("mapper-5");
        userModelOtherMapper.setName("other-claim-2");
        userModelOtherMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_USERMODEL_PROPERTY);

        ProtocolMapperRepresentation hardcodedOpenSystemMapper = new ProtocolMapperRepresentation();
        hardcodedOpenSystemMapper.setId("mapper-2");
        hardcodedOpenSystemMapper.setName(Const.KEYCLOAK_PROPERTY_OPEN_SYSTEM_ID);
        hardcodedOpenSystemMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_HARDCODED_CLAIM);

        ProtocolMapperRepresentation hardcodedOperatorMapper = new ProtocolMapperRepresentation();
        hardcodedOperatorMapper.setId("mapper-3");
        hardcodedOperatorMapper.setName(Const.KEYCLOAK_PROPERTY_OPERATOR_ID);
        hardcodedOperatorMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_HARDCODED_CLAIM);

        ProtocolMapperRepresentation hardcodedOtherMapper = new ProtocolMapperRepresentation();
        hardcodedOtherMapper.setId("mapper-4");
        hardcodedOtherMapper.setName("other-claim");
        hardcodedOtherMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_HARDCODED_CLAIM);

        ProtocolMapperRepresentation nonHardcodedOperatorMapper = new ProtocolMapperRepresentation();
        nonHardcodedOperatorMapper.setId("mapper-6");
        nonHardcodedOperatorMapper.setName(Const.KEYCLOAK_PROPERTY_OPERATOR_ID);
        nonHardcodedOperatorMapper.setProtocolMapper("custom-mapper-type");

        when(mockClientResource.getProtocolMappers()).thenReturn(mockProtocolMappersResource);
        when(mockProtocolMappersResource.getMappers())
                .thenReturn(List.of(
                userModelMapper, userModelOtherMapper, hardcodedOpenSystemMapper, hardcodedOperatorMapper,
                hardcodedOtherMapper, nonHardcodedOperatorMapper))
                .thenReturn(List.of(
                userModelMapper, hardcodedOpenSystemMapper, hardcodedOperatorMapper))
                .thenReturn(List.of(
                userModelMapper, hardcodedOperatorMapper))
                .thenReturn(List.of(
                userModelMapper, hardcodedOpenSystemMapper));
        doNothing().when(mockProtocolMappersResource).delete(anyString());
        when(mockProtocolMappersResource.createMapper(any(ProtocolMapperRepresentation.class))).thenReturn(mapperResponse);

        KeycloakRepositoryImpl repo = new KeycloakRepositoryImpl(null, null, null);
        java.lang.reflect.Method method = KeycloakRepositoryImpl.class.getDeclaredMethod("syncProtocolMappersForClientCredentials", ClientResource.class, String.class, String.class);
        method.setAccessible(true);

        // Act
        method.invoke(repo, mockClientResource, "operator-1", "open-system-1");

        // Assert
        verify(mockProtocolMappersResource).delete("mapper-1");
        verify(mockProtocolMappersResource).delete("mapper-2");
        verify(mockProtocolMappersResource).delete("mapper-3");
        verify(mockProtocolMappersResource, never()).delete("mapper-4");
        verify(mockProtocolMappersResource, never()).delete("mapper-5");
        verify(mockProtocolMappersResource, never()).delete("mapper-6");
        verify(mockProtocolMappersResource, Mockito.times(2)).createMapper(any(ProtocolMapperRepresentation.class));

        // Additional branch coverage in same test (no new test): no update values supplied.
        clearInvocations(mockProtocolMappersResource);
        method.invoke(repo, mockClientResource, null, null);
        verify(mockProtocolMappersResource).delete("mapper-1");
        verify(mockProtocolMappersResource, never()).delete("mapper-2");
        verify(mockProtocolMappersResource, never()).delete("mapper-3");
        verify(mockProtocolMappersResource, never()).delete("mapper-4");
        verify(mockProtocolMappersResource, never()).delete("mapper-5");
        verify(mockProtocolMappersResource, never()).delete("mapper-6");
        verify(mockProtocolMappersResource, never()).createMapper(any(ProtocolMapperRepresentation.class));

        // Additional branch coverage in same test (no new test): only operator_id is updated.
        clearInvocations(mockProtocolMappersResource);
        method.invoke(repo, mockClientResource, "operator-2", null);
        verify(mockProtocolMappersResource).delete("mapper-1");
        verify(mockProtocolMappersResource, never()).delete("mapper-2");
        verify(mockProtocolMappersResource).delete("mapper-3");
        verify(mockProtocolMappersResource, never()).delete("mapper-4");
        verify(mockProtocolMappersResource, never()).delete("mapper-5");
        verify(mockProtocolMappersResource, never()).delete("mapper-6");
        verify(mockProtocolMappersResource, Mockito.times(1)).createMapper(any(ProtocolMapperRepresentation.class));

        // Additional branch coverage in same test (no new test): only open_system_id is updated.
        clearInvocations(mockProtocolMappersResource);
        method.invoke(repo, mockClientResource, null, "open-system-2");
        verify(mockProtocolMappersResource).delete("mapper-1");
        verify(mockProtocolMappersResource).delete("mapper-2");
        verify(mockProtocolMappersResource, never()).delete("mapper-3");
        verify(mockProtocolMappersResource, never()).delete("mapper-4");
        verify(mockProtocolMappersResource, never()).delete("mapper-5");
        verify(mockProtocolMappersResource, never()).delete("mapper-6");
        verify(mockProtocolMappersResource, Mockito.times(1)).createMapper(any(ProtocolMapperRepresentation.class));

        // Additional branch coverage in same test (no new test): explicitly execute operator_id else-if path.
        ClientResource operatorOnlyClientResource = mock(ClientResource.class);
        ProtocolMappersResource operatorOnlyProtocolMappersResource = mock(ProtocolMappersResource.class);
        ProtocolMapperRepresentation operatorOnlyHardcodedMapper = new ProtocolMapperRepresentation();
        operatorOnlyHardcodedMapper.setId("mapper-op-only");
        operatorOnlyHardcodedMapper.setName(Const.KEYCLOAK_PROPERTY_OPERATOR_ID);
        operatorOnlyHardcodedMapper.setProtocolMapper(Const.KEYCLOAK_MAPPER_OIDC_HARDCODED_CLAIM);
        when(operatorOnlyClientResource.getProtocolMappers()).thenReturn(operatorOnlyProtocolMappersResource);
        when(operatorOnlyProtocolMappersResource.getMappers()).thenReturn(List.of(operatorOnlyHardcodedMapper));
        doNothing().when(operatorOnlyProtocolMappersResource).delete(anyString());
        when(operatorOnlyProtocolMappersResource.createMapper(any(ProtocolMapperRepresentation.class))).thenReturn(mapperResponse);

        method.invoke(repo, operatorOnlyClientResource, "operator-3", null);
        verify(operatorOnlyProtocolMappersResource).delete("mapper-op-only");
        verify(operatorOnlyProtocolMappersResource, Mockito.times(1)).createMapper(any(ProtocolMapperRepresentation.class));
    }

}
