/*
 * KeycloakServiceImplTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for KeycloakRepositoryImpl using mocks to avoid external.
 *
 * Date: 2025/08/30
 */

package io.github.open_dataspaces.core.infrastructure.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.dto.IdpAccountInfo;
import io.github.open_dataspaces.core.domain.dto.IdpClientInfo;
import io.github.open_dataspaces.core.domain.dto.StateString;
import io.github.open_dataspaces.core.domain.entities.APIKeysEntity;
import io.github.open_dataspaces.core.domain.entities.AccessTokenResult;
import io.github.open_dataspaces.core.domain.entities.TokenIntrospectionResult;
import io.github.open_dataspaces.core.domain.entities.TokenRevokeResult;
import io.github.open_dataspaces.core.domain.repository.interfaces.APIKeysRepository;
import io.github.open_dataspaces.core.domain.repository.interfaces.KeycloakRepository;
import io.github.open_dataspaces.core.domain.service.interfaces.APIKeyService;
import io.github.open_dataspaces.core.exception.AbstractBaseException;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.ConflictException;
import io.github.open_dataspaces.core.exception.NotFoundException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnauthorizedException;
import io.github.open_dataspaces.core.exception.UnexpectedException;
import io.github.open_dataspaces.core.infrastructure.utils.KeycloakUtil;

/**
 * Unit tests for the KeycloakServiceImpl class.
 */
@ExtendWith(MockitoExtension.class)
public class KeycloakServiceImplTest {

    @Mock
    private APIKeysRepository apiKeysRepository;

    @Mock
    private KeycloakRepository keycloakRepository;

    @Mock
    private KeycloakUtil keycloakUtil;

    @InjectMocks
    private KeycloakServiceImpl keycloakService;

    @Mock
    private APIKeyService apiKeyService;

    // Common input parameters
    private final String commonApiKey = "apiKey123";
    private final String commonUuid = UUID.randomUUID().toString();
    private final String commonFlowType = "authorization_code";
    private final String commonClientId = UUID.randomUUID().toString();
    private final String commonName = "Name";
    private final String commonDescription = "Description";
    private final String commonOperatorId = UUID.randomUUID().toString();
    private final String commonOpenSystemId = "openSystemId";
    private final List<String> commonRedirectUrisList = List.of("http://redirect.uri");
    private final String commonClientSecret = "secret";
    private final boolean commonEnabled = true;
    private final boolean commonPublicClient = false;
    private final String commonIdpRealm = "test-realm";

    /**
     * Parameterized test for building AuthorizationUrl.
     */
    @ParameterizedTest
    @CsvSource({
            // apiKey, clientId, redirectUri, codeChallenge, idpRealm, expectedUrl
            "test-api-key, client-id, http://localhost/redirect, challenge, realm1, http://auth-url", // Normal parameters
            "api-key-with-special-chars-!@#$%, client-id-with-special-chars-!@#$%, https://example.com/redirect?param=value&other=data, code-challenge-with-base64-chars_123, realm-with-special-chars-!@#, http://special-auth-url" // Parameters containing special characters
    })
    void buildAuthorizationUrl_success_parameterized(
            String apiKey, String clientId, String redirectUri, String codeChallenge,
            String idpRealm, String expectedUrl) {

        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);
        when(keycloakRepository.isValidClientId(idpRealm, clientId)).thenReturn(true);
        when(keycloakUtil.getAuthorizationEndpointUrl(idpRealm, clientId, redirectUri, codeChallenge))
                .thenReturn(expectedUrl);

        String result = keycloakService.buildAuthorizationUrl(apiKey, clientId, redirectUri, codeChallenge);

        assertEquals(expectedUrl, result);
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository).isValidClientId(idpRealm, clientId);
        verify(keycloakUtil).getAuthorizationEndpointUrl(idpRealm, clientId, redirectUri, codeChallenge);
    }

    /**
     * Parameterized test for buildAuthorizationUrl repository failures.
     */
    @ParameterizedTest
    @CsvSource({
            "IllegalArgumentException, invalid-client-id, http://localhost/redirect, challenge",  // Invalid client ID
            "UnauthorizedException, client-id-unauthorized, http://localhost/redirect, challenge",  // Authentication failed
            "BadParametersException, client-id, invalid-redirect-uri, challenge",  // Invalid redirect URI
            "OutOfServiceException, client-id, http://localhost/redirect, challenge",  // Keycloak service down
            "UnexpectedException, client-id, http://localhost/redirect, challenge"  // Unexpected error
    })
    void buildAuthorizationUrl_shouldThrowExpectedException_whenRepositoryFails(String exceptionClassName,
            String clientId, String redirectUri, String codeChallenge) {

        String apiKey = "test-api-key-123";
        String idpRealm = "realm-123";

        // Arrange
        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);

        String exceptionLogMessage = null;
        String exceptionResponseMessage = null;
        Exception exception = null;
        Class<? extends Throwable> clazz = null;

        switch (exceptionClassName) {
            case "IllegalArgumentException":
                // Arrange
                when(keycloakRepository.isValidClientId(idpRealm, clientId)).thenReturn(false);
                exceptionResponseMessage = "Invalid client ID: " + clientId;
                exception = new IllegalArgumentException(exceptionResponseMessage);
                clazz = IllegalArgumentException.class;
                break;
            case "BadParametersException":
                // Arrange
                when(keycloakRepository.isValidClientId(idpRealm, clientId)).thenReturn(true);
                exceptionLogMessage = String.format(ConstError.ERRLOG_400_INVALID_GRANT, "dummy");
                exceptionResponseMessage = ConstError.ERR_400_INVALID_GRANT;
                exception = new BadParametersException(exceptionLogMessage, exceptionResponseMessage);
                when(keycloakUtil.getAuthorizationEndpointUrl(idpRealm, clientId, redirectUri, codeChallenge))
                        .thenThrow(exception);
                clazz = BadParametersException.class;
                break;
            case "UnauthorizedException":
                // Arrange
                when(keycloakRepository.isValidClientId(idpRealm, clientId)).thenReturn(true);
                exceptionLogMessage = String.format(ConstError.ERRLOG_401_INVALID_CLIENT, "dummy");
                exceptionResponseMessage = ConstError.ERR_401_INVALID_CLIENT;
                exception = new UnauthorizedException(exceptionLogMessage, exceptionResponseMessage);
                when(keycloakUtil.getAuthorizationEndpointUrl(idpRealm, clientId, redirectUri, codeChallenge))
                        .thenThrow(exception);
                clazz = UnauthorizedException.class;
                break;
            case "OutOfServiceException":
                // Arrange
                when(keycloakRepository.isValidClientId(idpRealm, clientId)).thenReturn(true);
                exceptionLogMessage = ConstError.ERR_503_OUT_OF_SERVICE_KEYCLOAK;
                exceptionResponseMessage = "";
                exception = new OutOfServiceException(exceptionLogMessage);
                when(keycloakUtil.getAuthorizationEndpointUrl(idpRealm, clientId, redirectUri, codeChallenge))
                        .thenThrow(exception);
                clazz = OutOfServiceException.class;
                break;
            case "UnexpectedException":
                // Arrange
                when(keycloakRepository.isValidClientId(idpRealm, clientId)).thenReturn(true);
                exceptionLogMessage = ConstError.ERRLOG_500_KEYCLOAK_BUILD;
                exceptionResponseMessage = ConstError.ERR_500_MESSAGE;
                exception = new UnexpectedException(exceptionLogMessage, exceptionResponseMessage);
                when(keycloakUtil.getAuthorizationEndpointUrl(idpRealm, clientId, redirectUri, codeChallenge))
                        .thenThrow(exception);
                clazz = UnexpectedException.class;
                break;
            default:
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        // Act & Assert
        assertThrows(clazz, () -> keycloakService.buildAuthorizationUrl(apiKey, clientId, redirectUri, codeChallenge));

        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository).isValidClientId(idpRealm, clientId);

        // The failure occurs during client ID validation, so keycloakUtil is not called
        if ("IllegalArgumentException".equals(exceptionClassName)) {
            verify(keycloakUtil, never()).getAuthorizationEndpointUrl(any(), any(), any(), any());
        } else {
            verify(keycloakUtil).getAuthorizationEndpointUrl(idpRealm, clientId, redirectUri, codeChallenge);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "null, null",
            "'', ''"
    })
    void buildAuthorizationUrl_shouldHandleInvalidRealmFromEntity(String realmValue, String displayValue) {
        String apiKey = "test-api-key";
        String clientId = "client-id";
        String redirectUri = "http://localhost/redirect";
        String codeChallenge = "challenge";

        // Convert "null" string to actual null
        String actualRealm = "null".equals(realmValue) ? null : realmValue;

        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(actualRealm);

        // Null or empty realm is treated as an invalid client ID and should cause an exception
        assertThrows(IllegalArgumentException.class,
                () -> keycloakService.buildAuthorizationUrl(apiKey, clientId, redirectUri, codeChallenge));

        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakUtil, never()).getAuthorizationEndpointUrl(any(), any(), any(), any());
    }

    /**
     * Parameterized test for retrieving AccessToken.
     */
    @ParameterizedTest
    @CsvSource({
            // apiKey, code, clientId, clientSecret, redirectUri, codeVerifier, idpRealm
            "test-api-key, auth-code, client-id, secret, http://localhost/redirect, verifier, realm1", // Normal parameters
            "api-key-with-special-chars-!@#$%, auth-code-with-special-chars-!@#$%, client-id-with-special-chars-!@#$%, secret-with-special-chars-!@#$%, https://example.com/redirect?param=value&other=data, verifier-with-special-chars-!@#$%, realm-with-special-chars-!@#$%" // Parameters with special characters
    })
    void testGetAccessToken_success_parameterized(
            String apiKey, String code, String clientId, String clientSecret,
            String redirectUri, String codeVerifier, String idpRealm) {

        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);
        AccessTokenResult expectedResult = mock(AccessTokenResult.class);
        when(keycloakRepository.getAccessToken(idpRealm, code, clientId, clientSecret, redirectUri, codeVerifier))
                .thenReturn(expectedResult);

        // Act
        AccessTokenResult result = keycloakService.getAccessToken(apiKey, code, clientId, clientSecret, redirectUri,
                codeVerifier);

        // Assert
        assertSame(expectedResult, result);
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository).getAccessToken(idpRealm, code, clientId, clientSecret, redirectUri, codeVerifier);
    }

    /**
     * Parameterized test for getAccessToken repository failures.
     */
    @ParameterizedTest
    @CsvSource({
            "BadParametersException",
            "UnauthorizedException",
            "OutOfServiceException",
            "UnexpectedException"
    })
    void testGetAccessToken_shouldThrowExpectedException_whenRepositoryFails(String exceptionClassName) {
        String apiKey = "test-api-key-123";
        String code = "auth-code";
        String clientId = "client-id";
        String clientSecret = "secret";
        String redirectUri = "http://localhost/redirect";
        String codeVerifier = "verifier";
        String idpRealm = "realm-123";

        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);

        String exceptionLogMessage = null;
        String exceptionResponseMessage = null;
        AbstractBaseException exception = null;
        Class<? extends Throwable> clazz = null;

        switch (exceptionClassName) {
            case "BadParametersException":
                exceptionLogMessage = String.format(ConstError.ERRLOG_400_INVALID_GRANT, "dummy");
                exceptionResponseMessage = ConstError.ERR_400_INVALID_GRANT;
                exception = new BadParametersException(exceptionLogMessage, exceptionResponseMessage);
                clazz = BadParametersException.class;
                break;
            case "UnauthorizedException":
                exceptionLogMessage = String.format(ConstError.ERRLOG_401_INVALID_CLIENT, "dummy");
                exceptionResponseMessage = ConstError.ERR_401_INVALID_CLIENT;
                exception = new UnauthorizedException(exceptionLogMessage, exceptionResponseMessage);
                clazz = UnauthorizedException.class;
                break;
            case "OutOfServiceException":
                exceptionLogMessage = ConstError.ERR_503_OUT_OF_SERVICE_KEYCLOAK;
                exceptionResponseMessage = "";
                exception = new OutOfServiceException(exceptionLogMessage);
                clazz = OutOfServiceException.class;
                break;
            case "UnexpectedException":
                exceptionLogMessage = ConstError.ERRLOG_500_KEYCLOAK_BUILD;
                exceptionResponseMessage = ConstError.ERR_500_MESSAGE;
                exception = new UnexpectedException(exceptionLogMessage, exceptionResponseMessage);
                clazz = UnexpectedException.class;
                break;
            default:
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        // Act & Assert
        when(keycloakRepository.getAccessToken(idpRealm, code, clientId, clientSecret, redirectUri, codeVerifier))
                .thenThrow(exception);

        assertThrows(clazz, () -> keycloakService.getAccessToken(apiKey, code, clientId, clientSecret, redirectUri, codeVerifier));
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository).getAccessToken(idpRealm, code, clientId, clientSecret, redirectUri, codeVerifier);
    }

    @ParameterizedTest
    @CsvSource({
            "null, null",
            "'', ''"
    })
    void testGetAccessToken_shouldHandleInvalidRealmFromEntity(String realmValue, String displayValue) {
        String apiKey = "test-api-key";
        String code = "auth-code";
        String clientId = "client-id";
        String clientSecret = "secret";
        String redirectUri = "http://localhost/redirect";
        String codeVerifier = "verifier";

        // Convert "null" string to null
        String actualRealm = "null".equals(realmValue) ? null : realmValue;
        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(actualRealm);

        AccessTokenResult result = keycloakService.getAccessToken(apiKey, code, clientId, clientSecret, redirectUri,
                codeVerifier);

        assertNull(result);
        verify(apiKeyService).getIdpRealm(apiKey);
    }

    /**
     * Success test for token introspection.
     */
    @ParameterizedTest
    @CsvSource({
            // exceptionClass, apiKey
            "api-key-123, client-id-123, client-secret-123, access-token-active", // Normal pattern
            "api-key-123, client-id-123, client-secret-123, access-token-inactive" // Normal pattern
    })
    void tokenIntrospection_success(
            String apiKey,
            String clientId,
            String clientSecret,
            String accessToken
    ) {
        String idpRealm = "realm-123";

        TokenIntrospectionResult expectedResult = mock(TokenIntrospectionResult.class);

        // Arrange
        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);
        when(keycloakRepository.tokenIntrospection(idpRealm, clientId, clientSecret, accessToken,
                Const.KEYCLOAK_SEND_VALUE_TOKEN_TYPE_HINT_ACCESS_TOKEN))
                .thenReturn(expectedResult);

        // Act
        TokenIntrospectionResult result = keycloakService.tokenIntrospection(apiKey, accessToken, clientId, clientSecret);

        assertSame(expectedResult, result);
        verify(keycloakRepository).tokenIntrospection(idpRealm, clientId, clientSecret, accessToken, Const.KEYCLOAK_SEND_VALUE_TOKEN_TYPE_HINT_ACCESS_TOKEN);
    }

    /**
     * Parameterized test for retrieving IdpRealm.
     */
    @ParameterizedTest
    @CsvSource({
            // exceptionClass, apiKey
            "IllegalArgumentException, api-key-123, client-id-empty, client-secret-123, access-token-123" // Abnormal pattern
    })
    void tokenIntrospection_shouldThrowExpectedException_whenIdpRealmRetrievalFails(String exceptionClassName,
            String apiKey, String clientId, String clientSecret, String accessToken) {

        String exceptionResponseMessage = null;
        Exception exception = null;
        Class<? extends Throwable> clazz = null;

        switch (exceptionClassName) {
            case "IllegalArgumentException":
                // Arrange
                exceptionResponseMessage = ConstError.ERR_403_APIKEY_NOT_VALID;
                exception = new IllegalArgumentException(exceptionResponseMessage);
                clazz = IllegalArgumentException.class;
                break;
            default:
                // Unexpected exception
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }
        when(apiKeyService.getIdpRealm(apiKey)).thenThrow(exception);
        // Act
        // Get realm from APIKey
        assertThrows(clazz, () -> keycloakService.tokenIntrospection(apiKey, accessToken, clientId, clientSecret));
        assertEquals(exceptionResponseMessage, exception.getMessage());
    }

    /**
     * Parameterized test for retrieving TokenIntrospection.
     */
    @ParameterizedTest
    @CsvSource({
            // apiKey, clientId, redirectUri, codeChallenge, idpRealm, expectedUrl
            "UnauthorizedException, client-id-empty, client-secret-123, access-token-123",    // Client ID invalid
            "UnauthorizedException, client-id-123, client-secret-empty, access-token-123",    // Client secret invalid
            "BadParametersException, client-id-123, client-secret-123, access-token-issuer-unmatch",    // Access token issuer invalid
            "OutOfServiceException, client-id-123, client-secret-123, access-token-123",    // Parameters valid, Keycloak down
            "UnexpectedException, client-id-123, client-secret-123, access-token-123",    // Parameters valid, Keycloak unexpected error
    })
    void tokenIntrospection_shouldThrowExpectedException_whenRepositoryFails(String exceptionClassName,
            String clientId, String clientSecret, String accessToken) {

        String apiKey = "test-api-key-123";
        String idpRealm = "realm-123";

        // Arrange
        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);

        String exceptionLogMessage = null;
        String exceptionResponseMessage = null;
        AbstractBaseException exception = null;
        Class<? extends Throwable> clazz = null;

        switch (exceptionClassName) {
            case "BadParametersException":
                // Arrange
                exceptionLogMessage = String.format(ConstError.ERRLOG_400_INVALID_GRANT, "dummy");
                exceptionResponseMessage = ConstError.ERR_400_INVALID_GRANT;
                exception = new BadParametersException(exceptionLogMessage, exceptionResponseMessage);
                clazz = BadParametersException.class;
                break;
            case "UnauthorizedException":
                // Arrange
                exceptionLogMessage = String.format(ConstError.ERRLOG_401_INVALID_CLIENT, "dummy");
                exceptionResponseMessage = ConstError.ERR_401_INVALID_CLIENT;
                exception = new UnauthorizedException(exceptionLogMessage, exceptionResponseMessage);
                clazz = UnauthorizedException.class;
                break;
            case "OutOfServiceException":
                // Arrange
                exceptionLogMessage = ConstError.ERR_503_OUT_OF_SERVICE_KEYCLOAK;
                exceptionResponseMessage = "";
                exception = new OutOfServiceException(exceptionLogMessage);
                clazz = OutOfServiceException.class;
                break;
            case "UnexpectedException":
                // Arrange
                exceptionLogMessage = ConstError.ERRLOG_500_KEYCLOAK_BUILD;
                exceptionResponseMessage = ConstError.ERR_500_MESSAGE;
                exception = new UnexpectedException(exceptionLogMessage, exceptionResponseMessage);
                clazz = UnexpectedException.class;
                break;
            default:
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }
        when(apiKeyService.getIdpRealm(apiKey)).thenThrow(exception);
        // Act
        // Get realm from APIKey
        assertThrows(clazz, () -> keycloakService.tokenIntrospection(apiKey, accessToken, clientId, clientSecret));
        assertEquals(exceptionResponseMessage, exception.getMessage());
    }

    /**
     * Success test for token refresh.
     */
    @ParameterizedTest
    @CsvSource({
            "api-key-123, client-id-123, client-secret-123, refresh-token-active", // Normal pattern
    })
    void tokenRefresh_success(
            String apiKey,
            String clientId,
            String clientSecret,
            String refreshToken
    ) {
        String idpRealm = "realm-123";

        AccessTokenResult expectedResult = mock(AccessTokenResult.class);

        // Arrange
        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);
        when(keycloakRepository.tokenRefresh(idpRealm, clientId, clientSecret, refreshToken))
                .thenReturn(expectedResult);

        // Act
        AccessTokenResult result = keycloakService.tokenRefresh(apiKey, refreshToken, clientId, clientSecret);

        assertSame(expectedResult, result);
        verify(keycloakRepository).tokenRefresh(idpRealm, clientId, clientSecret, refreshToken);
    }

    /**
     * Parameterized test for retrieving IdpRealm.
     */
    @ParameterizedTest
    @CsvSource({
            "IllegalArgumentException, api-key-123, client-id-empty, client-secret-123, refresh-token-123" // Abnormal pattern
    })
    void tokenRefresh_shouldThrowExpectedException_whenIdpRealmRetrievalFails(String exceptionClassName,
            String apiKey, String clientId, String clientSecret, String refreshToken) {

        String exceptionResponseMessage = null;
        Exception exception = null;
        Class<? extends Throwable> clazz = null;

        switch (exceptionClassName) {
            case "IllegalArgumentException":
                // Arrange
                exceptionResponseMessage = ConstError.ERR_403_APIKEY_NOT_VALID;
                exception = new IllegalArgumentException(exceptionResponseMessage);
                clazz = IllegalArgumentException.class;
                break;
            default:
                // Unexpected exception
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }
        when(apiKeyService.getIdpRealm(apiKey)).thenThrow(exception);
        // Act
        // Get realm from APIKey
        assertThrows(clazz, () -> keycloakService.tokenRefresh(apiKey, refreshToken, clientId, clientSecret));
        assertEquals(exceptionResponseMessage, exception.getMessage());
    }

    /**
     * Parameterized test for retrieving TokenRefresh.
     */
    @ParameterizedTest
    @CsvSource({
            "UnauthorizedException, client-id-empty, client-secret-123, refresh-token-123",    // Client ID invalid
            "UnauthorizedException, client-id-123, client-secret-empty, refresh-token-123",    // Client secret invalid
            "BadParametersException, client-id-123, client-secret-123, refresh-token-expired",    // Refresh token expired
            "OutOfServiceException, client-id-123, client-secret-123, refresh-token-123",    // Parameters valid, Keycloak down
            "UnexpectedException, client-id-123, client-secret-123, refresh-token-123",    // Parameters valid, Keycloak unexpected error
    })
    void tokenRefresh_shouldThrowExpectedException_whenRepositoryFails(String exceptionClassName,
            String clientId, String clientSecret, String refreshToken) {

        String apiKey = "test-api-key-123";
        String idpRealm = "realm-123";

        // Arrange
        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);

        String exceptionLogMessage = null;
        String exceptionResponseMessage = null;
        AbstractBaseException exception = null;
        Class<? extends Throwable> clazz = null;

        switch (exceptionClassName) {
            case "BadParametersException":
                // Arrange
                exceptionLogMessage = String.format(ConstError.ERRLOG_400_INVALID_GRANT, "dummy");
                exceptionResponseMessage = ConstError.ERR_400_INVALID_GRANT;
                exception = new BadParametersException(exceptionLogMessage, exceptionResponseMessage);
                clazz = BadParametersException.class;
                break;
            case "UnauthorizedException":
                // Arrange
                exceptionLogMessage = String.format(ConstError.ERRLOG_401_INVALID_CLIENT, "dummy");
                exceptionResponseMessage = ConstError.ERR_401_INVALID_CLIENT;
                exception = new UnauthorizedException(exceptionLogMessage, exceptionResponseMessage);
                clazz = UnauthorizedException.class;
                break;
            case "OutOfServiceException":
                // Arrange
                exceptionLogMessage = ConstError.ERR_503_OUT_OF_SERVICE_KEYCLOAK;
                exceptionResponseMessage = "";
                exception = new OutOfServiceException(exceptionLogMessage);
                clazz = OutOfServiceException.class;
                break;
            case "UnexpectedException":
                // Arrange
                exceptionLogMessage = ConstError.ERRLOG_500_KEYCLOAK_BUILD;
                exceptionResponseMessage = ConstError.ERR_500_MESSAGE;
                exception = new UnexpectedException(exceptionLogMessage, exceptionResponseMessage);
                clazz = UnexpectedException.class;
                break;
            default:
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }
        when(apiKeyService.getIdpRealm(apiKey)).thenThrow(exception);
        // Act
        // Get realm from APIKey
        assertThrows(clazz, () -> keycloakService.tokenRefresh(apiKey, refreshToken, clientId, clientSecret));
        assertEquals(exceptionResponseMessage, exception.getMessage());
    }

    /**
     * Provides test cases for changePassword method.
     */
    static Stream<TestCase> changePasswordCases() {
        return Stream.of(
            // Valid case
            new TestCase("valid-case", "valid-api-key", "valid-operator-id", "oldPass123", "newPass123", true, null),
            // Invalid old password
            new TestCase("invalid-old-password", "valid-api-key", "valid-operator-id", "wrongOldPass", "newPass123", false, IllegalArgumentException.class)
        );
    }

    /**
     * Parameterized test for changePassword method.
     *
     * @param tc the test case containing request parameters and expected results
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("changePasswordCases")
    void changePassword_handlesVariousScenarios(TestCase tc) {
        // Mock apiKeyService to return a valid realm
        when(apiKeyService.getIdpRealm(tc.apiKey)).thenReturn("test-realm");

        // Mock KeycloakRepository behavior
        if (tc.expectSuccess) {
            when(keycloakRepository.isValidPassword("test-realm", tc.operatorId, tc.oldPassword)).thenReturn(true);
            doNothing().when(keycloakRepository).changePassword("test-realm", tc.operatorId, tc.newPassword);
        } else if (tc.expectedException == IllegalArgumentException.class) {
            when(keycloakRepository.isValidPassword("test-realm", tc.operatorId, tc.oldPassword)).thenReturn(false);
        }

        if (tc.expectedException != null) {
            // Expect exception
            RuntimeException e = (RuntimeException) assertThrows(tc.expectedException, () -> keycloakService.changePassword(tc.apiKey, tc.operatorId, tc.oldPassword, tc.newPassword));
            assertTrue(e.getMessage().contains(String.format(ConstError.ERRLOG_400_INVALID_REQUEST_PARAMETER,
                    Const.JSON_PROPERTY_OLD_PASSWORD, tc.oldPassword)), "Expected error message not found: " + e.getMessage());
        } else {
            // Expect success
            boolean result = keycloakService.changePassword(tc.apiKey, tc.operatorId, tc.oldPassword, tc.newPassword);
            assertTrue(result, "Expected changePassword to return true for valid inputs");
        }
    }

    /**
     * Test for changePassword when apiKeyService#getIdpRealm throws an exception.
     */
    @Test
    void testChangePassword_shouldThrowException_whenGetIdpRealmFails() {
        // Arrange
        String apiKey = "invalid-api-key";
        String operatorId = "operator-id";
        String oldPassword = "old-password";
        String newPassword = "new-password";

        // Mock apiKeyService to throw an exception
        when(apiKeyService.getIdpRealm(apiKey)).thenThrow(new IllegalArgumentException(ConstError.ERR_403_APIKEY_NOT_VALID));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            keycloakService.changePassword(apiKey, operatorId, oldPassword, newPassword);
        });

        assertEquals(ConstError.ERR_403_APIKEY_NOT_VALID, exception.getMessage(), "The exception message should match the expected error message.");

        // Verify that no further interactions occur
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository, never()).isValidPassword(any(), any(), any());
        verify(keycloakRepository, never()).changePassword(any(), any(), any());
    }

    /**
     * Test for changePassword when keycloakRepository#changePassword throws an exception.
     */
    @Test
    void testChangePassword_shouldThrowException_whenChangePasswordFails() {
        // Arrange
        String apiKey = "test-api-key";
        String operatorId = "operator-id";
        String oldPassword = "valid-old-password";
        String newPassword = "new-password";
        String idpRealm = "test-realm";

        // Mock apiKeyService to return a valid realm
        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);

        // Mock keycloakRepository to validate the old password
        when(keycloakRepository.isValidPassword(idpRealm, operatorId, oldPassword)).thenReturn(true);

        // Mock keycloakRepository to throw an exception during password change
        doThrow(new IllegalArgumentException("Unexpected error during password change"))
                .when(keycloakRepository).changePassword(idpRealm, operatorId, newPassword);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            keycloakService.changePassword(apiKey, operatorId, oldPassword, newPassword);
        });

        assertEquals("Unexpected error during password change", exception.getMessage(), "The exception message should match the expected error message.");

        // Verify interactions
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository).isValidPassword(idpRealm, operatorId, oldPassword);
        verify(keycloakRepository).changePassword(idpRealm, operatorId, newPassword);
    }

    /**
     * Simple holder for parameterized test data.
     */
    private static class TestCase {
        final String name;
        final String apiKey;
        final String operatorId;
        final String oldPassword;
        final String newPassword;
        final boolean expectSuccess;
        final Class<? extends Throwable> expectedException;

        TestCase(String name, String apiKey, String operatorId, String oldPassword, String newPassword, boolean expectSuccess, Class<? extends Throwable> expectedException) {
            this.name = name;
            this.apiKey = apiKey;
            this.operatorId = operatorId;
            this.oldPassword = oldPassword;
            this.newPassword = newPassword;
            this.expectSuccess = expectSuccess;
            this.expectedException = expectedException;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Parameterized test for buildPasswordChangeUrl method.
     */
    @ParameterizedTest
    @CsvSource({
        // apiKey, clientId, redirectUri, idpRealm, expectedUrl
        "test-api-key, client-id, http://localhost/redirect, abc123#, realm1, http://password-change-url",
        "special-api-key-!@#$%, special-client-id-!@#$%, https://example.com/redirect?param=value, !=abc123#, special-realm-!@#$%, https://special-password-change-url"
    })
    void testBuildPasswordChangeUrl_success(String apiKey, String clientId, String redirectUri, String codeChallenge, String idpRealm, String expectedUrl) {
        // Arrange
        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);
        when(keycloakRepository.isValidClientId(idpRealm, clientId)).thenReturn(true);
        when(keycloakUtil.getPasswordChangeEndpointUrl(idpRealm, clientId, redirectUri, codeChallenge)).thenReturn(expectedUrl);

        // Act
        String result = keycloakService.buildPasswordChangeUrl(apiKey, clientId, redirectUri, codeChallenge);

        // Assert
        assertEquals(expectedUrl, result, "The returned URL should match the expected URL.");
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository).isValidClientId(idpRealm, clientId);
        verify(keycloakUtil).getPasswordChangeEndpointUrl(idpRealm, clientId, redirectUri, codeChallenge);
    }

    /**
     * Test for buildPasswordChangeUrl when clientId is invalid.
     */
    @Test
    void testBuildPasswordChangeUrl_invalidClientId() {
        // Arrange
        String apiKey = "test-api-key";
        String clientId = "invalid-client-id";
        String redirectUri = "http://localhost/redirect";
        String codeChallenge = "challenge";
        String idpRealm = "realm1";

        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);
        when(keycloakRepository.isValidClientId(idpRealm, clientId)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            keycloakService.buildPasswordChangeUrl(apiKey, clientId, redirectUri, codeChallenge);
        });

        assertTrue(exception.getMessage().contains(String.format(ConstError.ERRLOG_400_INVALID_REQUEST_PARAMETER, Const.JSON_PROPERTY_CLIENT_ID, clientId)), "The exception message should contain the invalid parameters.");
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository).isValidClientId(idpRealm, clientId);
        verify(keycloakUtil, never()).getPasswordChangeEndpointUrl(any(), any(), any(), any());
    }

    /**
     * Test for buildPasswordChangeUrl when apiKey is invalid.
     */
    @Test
    void testBuildPasswordChangeUrl_invalidApiKey() {
        // Arrange
        String apiKey = "invalid-api-key";
        String clientId = "client-id";
        String redirectUri = "http://localhost/redirect";
        String codeChallenge = "challenge";

        when(apiKeyService.getIdpRealm(apiKey)).thenThrow(new IllegalArgumentException("Invalid API key"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            keycloakService.buildPasswordChangeUrl(apiKey, clientId, redirectUri, codeChallenge);
        });

        assertTrue(exception.getMessage().contains("Invalid API key"), "The exception message should indicate an invalid API key.");
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository, never()).isValidClientId(any(), any());
        verify(keycloakUtil, never()).getPasswordChangeEndpointUrl(any(), any(), any(), any());
    }

    /**
     * Test for BuildPasswordChangeUrl when apiKeyService#getIdpRealm throws an exception.
     */
    @Test
    void testBuildPasswordChangeUrl_shouldThrowException_whenGetIdpRealmFails() {
        // Arrange
        String apiKey = "invalid-api-key";
        String clientId = "client-id-1";
        String redirectUri = "http://redirect.uri/1";
        String codeChallenge = "challenge";

        // Mock apiKeyService to throw an exception
        when(apiKeyService.getIdpRealm(apiKey)).thenThrow(new IllegalArgumentException(ConstError.ERR_403_APIKEY_NOT_VALID));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            keycloakService.buildPasswordChangeUrl(apiKey, clientId, redirectUri, codeChallenge);
        });

        assertEquals(ConstError.ERR_403_APIKEY_NOT_VALID, exception.getMessage(), "The exception message should match the expected error message.");

        // Verify that no further interactions occur
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository, never()).isValidClientId(any(), any());
        verify(keycloakUtil, never()).getPasswordChangeEndpointUrl(any(), any(), any(), any());
    }

    /**
     * Test for changePassword when keycloakRepository#changePassword throws an exception.
     */
    @Test
    void testBuildPasswordChangeUrl_shouldThrowException_whenGetPasswordChangeEndpointUrlFails() {
        // Arrange
        String apiKey = "test-api-key";
        String idpRealm = "test-realm";
        String clientId = "client-id-1";
        String redirectUri = "http://redirect.uri/1";
        String codeChallenge = "challenge";

        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);
        when(keycloakRepository.isValidClientId(idpRealm, clientId)).thenReturn(true);

        // Mock keycloakRepository to throw an exception during password change
        when(keycloakUtil.getPasswordChangeEndpointUrl(idpRealm, clientId, redirectUri, codeChallenge)).thenThrow(new IllegalArgumentException("Unexpected error during password url build"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            keycloakService.buildPasswordChangeUrl(apiKey, clientId, redirectUri, codeChallenge);
        });

        assertEquals("Unexpected error during password url build", exception.getMessage(), "The exception message should match the expected error message.");

        // Verify interactions
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository).isValidClientId(any(), any());
        verify(keycloakUtil).getPasswordChangeEndpointUrl(any(), any(), any(), any());
    }

    /**
     * Test No.14
     * Verifies that signInWithPassword successfully returns AccessTokenResult when valid credentials are provided.
     */
    @Test
    void testSignInWithPassword_success() {
        // Arrange
        String apiKey = "test-api-key";
        String clientId = "test-client";
        String clientSecret = "test-secret";
        String operatorAccountId = "test@example.com";
        String password = "testpassword";
        String realm = "test-realm";
        String accessToken = "access-token-abc";
        String refreshToken = "refresh-token-xyz";

        // Setup API key entity
        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(realm);
        AccessTokenResult expectedResult = new AccessTokenResult(accessToken, 3600L, "Bearer", 0, "openid profile", refreshToken, 7200L, "idToken");

        when(keycloakRepository.signInWithPassword(realm, clientId, clientSecret, operatorAccountId, password)).thenReturn(expectedResult);

        // Act
        AccessTokenResult result = keycloakService.signInWithPassword(apiKey, clientId, clientSecret, operatorAccountId, password);

        // Assert
        assertNotNull(result);
        assertEquals(accessToken, result.getAccessToken());
        assertEquals(refreshToken, result.getRefreshToken());
        assertEquals(Long.valueOf(3600L), result.getExpiresIn());
        assertEquals("Bearer", result.getTokenType());
        verify(keycloakRepository, times(1)).signInWithPassword(realm, clientId, clientSecret, operatorAccountId, password);
    }

    /**
     * Test No.15
     * Verifies that signInWithPassword return login failed exception when invalid credentials are provided.
     */
    @Test
    void signInWithPassword_loginFailed_throwsException() {
        // Arrange
        String apiKey = "test-api-key";
        String clientId = "test-client";
        String clientSecret = "test-secret";
        String operatorAccountId = "test@example.com";
        String password = "wrongpassword";
        String realm = "test-realm";
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);

        // Setup API key entity
        APIKeysEntity apiKeysEntity = mock(APIKeysEntity.class);
        when(apiKeysEntity.getIdpRealm()).thenReturn(realm);
        when(apiKeysRepository.findByApiKey(apiKey, currentDate)).thenReturn(Collections.singletonList(apiKeysEntity));

        when(keycloakRepository.signInWithPassword(realm, clientId, clientSecret, operatorAccountId, password))
                .thenThrow(new RuntimeException("Login failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> keycloakService.signInWithPassword(apiKey, clientId, clientSecret, operatorAccountId, password));
    }

    /**
     * Test No.16
     * Verifies that signInWithPassword throws an unexpected exception when Keycloak build fails.
     */
    @Test
    void signInWithPassword_keycloakBuildFailed_throwsUnexpectedException() {
        // Arrange
        String apiKey = "test-api-key";
        String clientId = "test-client";
        String clientSecret = "test-secret";
        String operatorAccountId = "test@example.com";
        String password = "testpassword";
        String realm = "test-realm";
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);

        // Setup API key entity
        APIKeysEntity apiKeysEntity = mock(APIKeysEntity.class);
        when(apiKeysEntity.getIdpRealm()).thenReturn(realm);
        when(apiKeysRepository.findByApiKey(apiKey, currentDate)).thenReturn(Collections.singletonList(apiKeysEntity));

        when(keycloakRepository.signInWithPassword(realm, clientId, clientSecret, operatorAccountId, password))
                .thenThrow(new RuntimeException("Keycloak build failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> keycloakService.signInWithPassword(apiKey, clientId, clientSecret, operatorAccountId, password));
    }

    /**
     * Test No.17
     * Verifies that signInWithPassword throws an exception when apiKeyService.getIdpRealm fails.
     */
    @ParameterizedTest
    @CsvSource({
            "IllegalArgumentException, invalid-api-key, ERR_403_APIKEY_NOT_VALID", // Invalid API key
    })
    void signInWithPassword_shouldThrowExpectedException_whenIdpRealmRetrievalFails(String exceptionClassName, String apiKey, String expectedMessage) {
        // Arrange
        String clientId = "test-client";
        String clientSecret = "test-secret";
        String operatorAccountId = "test@example.com";
        String password = "testpassword";

        Exception exception = null;
        Class<? extends Throwable> expectedExceptionClass = null;

        exception = new IllegalArgumentException(expectedMessage);
        expectedExceptionClass = IllegalArgumentException.class;

        when(apiKeyService.getIdpRealm(apiKey)).thenThrow(exception);

        // Act & Assert
        Throwable thrownException = assertThrows(expectedExceptionClass, () ->
                keycloakService.signInWithPassword(apiKey, clientId, clientSecret, operatorAccountId, password));

        assertEquals(expectedMessage, thrownException.getMessage());

        // Verify that apiKeyService.getIdpRealm was called
        verify(apiKeyService, times(1)).getIdpRealm(apiKey);
        // Verify that keycloakRepository.signInWithPassword was never called due to the exception
        verify(keycloakRepository, never()).signInWithPassword(any(), any(), any(), any(), any());
    }

    @Test
    void testSignInWithClient_success() {
        // Arrange
        String apiKey = "test-api-key";
        String clientId = "test-client";
        String clientSecret = "test-secret";
        String realm = "test-realm";
        String accessToken = "access-token-abc";

        // Setup API key entity
        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(realm);
        AccessTokenResult expectedResult = new AccessTokenResult(accessToken, 3600L, "Bearer", 0, "openid profile", null, 0, null);

        when(keycloakRepository.signInWithClient(realm, clientId, clientSecret)).thenReturn(expectedResult);

        // Act
        AccessTokenResult result = keycloakService.signInWithClient(apiKey, clientId, clientSecret);

        // Assert
        assertNotNull(result);
        assertEquals(accessToken, result.getAccessToken());
        assertEquals(Long.valueOf(3600L), result.getExpiresIn());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(0, result.getNotBeforePolicy());
        assertEquals("openid profile", result.getScope());
        assertEquals(null, result.getRefreshToken());
        assertEquals(null, result.getIdToken());
        verify(keycloakRepository, times(1)).signInWithClient(realm, clientId, clientSecret);
    }

    @Test
    void signInWithClient_loginFailed_throwsException() {
        // Arrange
        String apiKey = "test-api-key";
        String clientId = "test-client";
        String clientSecret = "test-secret";
        String realm = "test-realm";
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);

        // Setup API key entity
        APIKeysEntity apiKeysEntity = mock(APIKeysEntity.class);
        when(apiKeysEntity.getIdpRealm()).thenReturn(realm);
        when(apiKeysRepository.findByApiKey(apiKey, currentDate)).thenReturn(Collections.singletonList(apiKeysEntity));

        when(keycloakRepository.signInWithClient(realm, clientId, clientSecret))
                .thenThrow(new RuntimeException("Login failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> keycloakService.signInWithClient(apiKey, clientId, clientSecret));
    }

    @Test
    void signInWithClient_keycloakBuildFailed_throwsUnexpectedException() {
        // Arrange
        String apiKey = "test-api-key";
        String clientId = "test-client";
        String clientSecret = "test-secret";
        String realm = "test-realm";
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);

        // Setup API key entity
        APIKeysEntity apiKeysEntity = mock(APIKeysEntity.class);
        when(apiKeysEntity.getIdpRealm()).thenReturn(realm);
        when(apiKeysRepository.findByApiKey(apiKey, currentDate)).thenReturn(Collections.singletonList(apiKeysEntity));

        when(keycloakRepository.signInWithClient(realm, clientId, clientSecret))
                .thenThrow(new RuntimeException("Keycloak build failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> keycloakService.signInWithClient(apiKey, clientId, clientSecret));
    }

    @ParameterizedTest
    @CsvSource({
            "IllegalArgumentException, invalid-api-key, ERR_403_APIKEY_NOT_VALID", // Invalid API key
    })
    void signInWithClient_shouldThrowExpectedException_whenIdpRealmRetrievalFails(String exceptionClassName, String apiKey, String expectedMessage) {
        // Arrange
        String clientId = "test-client";
        String clientSecret = "test-secret";

        Exception exception = null;
        Class<? extends Throwable> expectedExceptionClass = null;
        exception = new IllegalArgumentException(expectedMessage);
        expectedExceptionClass = IllegalArgumentException.class;

        when(apiKeyService.getIdpRealm(apiKey)).thenThrow(exception);

        // Act & Assert
        Throwable thrownException = assertThrows(expectedExceptionClass, () ->
                keycloakService.signInWithClient(apiKey, clientId, clientSecret));

        assertEquals(expectedMessage, thrownException.getMessage());

        // Verify that apiKeyService.getIdpRealm was called
        verify(apiKeyService, times(1)).getIdpRealm(apiKey);
        // Verify that keycloakRepository.signInWithPassword was never called due to the exception
        verify(keycloakRepository, never()).signInWithClient(any(), any(), any());
    }

    /**
     * Verifies that updateAccount successfully updates a user and returns IdpAccountInfo.
     */
    @ParameterizedTest
    @CsvSource({
        "test-api-key, user-id-123, newLoginUser, new@example.com, updated-user-id",
        "test-api-key, user-id-456, newLoginUser, '', updated-user-id"
    })
    void updateAccount_success(
            String apiKey,
            String userId,
            String loginUserId,
            String email,
            String updatedUserId) {

        // Arrange
        String idpRealm = "test-realm";

        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);
        IdpAccountInfo expectedResult = new IdpAccountInfo(updatedUserId, loginUserId, email, null, true);
        when(keycloakRepository.updateUser(eq(idpRealm), eq(userId), eq(loginUserId), eq(email)))
                .thenReturn(expectedResult);

        // Act
        IdpAccountInfo result = keycloakService.updateAccount(apiKey, userId, StateString.of(loginUserId), StateString.of(email));

        // Assert
        assertNotNull(result);
        assertEquals(updatedUserId, result.getUserId());
        assertEquals(loginUserId, result.getLoginUserId());
        assertEquals(email, result.getEmail());
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository).updateUser(eq(idpRealm), eq(userId), eq(loginUserId), eq(email));
    }

    /**
     * Test for updateAccount when apiKeyService#getIdpRealm throws an exception.
     */
    @Test
    void updateAccount_shouldThrowException_whenGetIdpRealmFails() {
        // Arrange
        String apiKey = "invalid-api-key";
        String userId = "user-id-123";
        String loginUserId = "newLoginUser";
        String email = "new@example.com";

        // Mock apiKeyService to throw an exception
        when(apiKeyService.getIdpRealm(apiKey)).thenThrow(new IllegalArgumentException(ConstError.ERR_403_APIKEY_NOT_VALID));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            keycloakService.updateAccount(apiKey, userId, StateString.of(loginUserId), StateString.of(email));
        });

        assertEquals(ConstError.ERR_403_APIKEY_NOT_VALID, exception.getMessage(), "The exception message should match the expected error message.");

        // Verify that no further interactions occur
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository, never()).updateUser(any(), any(), any(), any());
    }

    /**
     * Parameterized test for updateAccount method to handle exception scenarios.
     */
    @ParameterizedTest
    @CsvSource({
        "test-api-key, user-id-123, newLoginUser, new@example.com, RuntimeException, Unexpected error",
        "test-api-key, user-id-456, newLoginUser, '', IllegalStateException, Repository error"
    })
    void updateAccount_exception(
            String apiKey,
            String userId,
            String loginUserId,
            String email,
            String exceptionType,
            String exceptionMessage) {

        // Arrange
        String idpRealm = "test-realm";

        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);

        if ("RuntimeException".equals(exceptionType)) {
            when(keycloakRepository.updateUser(eq(idpRealm), eq(userId), eq(loginUserId), eq(email)))
                    .thenThrow(new RuntimeException(exceptionMessage));
        } else if ("IllegalStateException".equals(exceptionType)) {
            when(keycloakRepository.updateUser(eq(idpRealm), eq(userId), eq(loginUserId), eq(email)))
                    .thenThrow(new IllegalStateException(exceptionMessage));
        }

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            keycloakService.updateAccount(apiKey, userId, StateString.of(loginUserId), StateString.of(email));
        });

        assertEquals(exceptionMessage, exception.getMessage());
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository).updateUser(eq(idpRealm), eq(userId), eq(loginUserId), eq(email));
    }

    /**
     * Verifies that createAccount successfully creates a user and returns IdpAccountResult.
     */
    @ParameterizedTest
    @CsvSource({
        "test-api-key, testUser, test@example.com, true, true, randomPassword123, generated-user-id",
        "test-api-key, testUser, '', true, true, randomPassword123, generated-user-id",
        "test-api-key, testUser, null, true, true, randomPassword123, generated-user-id",
        "test-api-key, testUser, test@example.com, true, false, randomPassword123, generated-user-id",
        "test-api-key, testUser, test@example.com, false, true, randomPassword123, generated-user-id",
        "test-api-key, testUser, test@example.com, false, false, randomPassword123, generated-user-id"
    })
    @DisplayName("createAccount - Success Cases")
    void createAccount_success(
            String apiKey,
            String loginUserId,
            String email,
            boolean createPasswordFlag,
            boolean passwordTemporaryFlag,
            String generatedPassword,
            String userId) {

        // Arrange
        String idpRealm = "test-realm";
        email = (email != null) ? null : email; // Convert empty string to null for email

        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);
        if (createPasswordFlag) {
            when(keycloakUtil.generateRandomPassword()).thenReturn(generatedPassword);
        }
        when(keycloakRepository.createUser(eq(idpRealm), eq(loginUserId), any(), any(), anyBoolean()))
                .thenReturn(userId);

        // Act
        IdpAccountInfo result = keycloakService.createAccount(apiKey, loginUserId, email, createPasswordFlag, passwordTemporaryFlag);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(createPasswordFlag ? generatedPassword : null, result.getPassword());
        verify(apiKeyService).getIdpRealm(apiKey);
        if (createPasswordFlag) {
            verify(keycloakUtil).generateRandomPassword();
        } else {
            verify(keycloakUtil, never()).generateRandomPassword();
        }
        verify(keycloakRepository).createUser(any(), any(), any(), any(), anyBoolean());
    }

    /**
     * Test for createAccount when apiKeyService#getIdpRealm throws an exception.
     */
    @Test
    void createAccount_shouldThrowException_whenGetIdpRealmFails() {
        // Arrange
        String apiKey = "invalid-api-key";
        String loginUserId = "testUser";
        String email = "test@example.com";
        boolean createPasswordFlag = true;
        boolean passwordTemporaryFlag = false;

        // Mock apiKeyService to throw an exception
        when(apiKeyService.getIdpRealm(apiKey)).thenThrow(new IllegalArgumentException(ConstError.ERR_403_APIKEY_NOT_VALID));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            keycloakService.createAccount(apiKey, loginUserId, email, createPasswordFlag, passwordTemporaryFlag);
        });

        assertEquals(ConstError.ERR_403_APIKEY_NOT_VALID, exception.getMessage(), "The exception message should match the expected error message.");

        // Verify that no further interactions occur
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository, never()).createUser(any(), any(), any(), any(), anyBoolean());
    }

    /**
     * Parameterized test for createAccount method to handle exception scenarios.
     */
    @ParameterizedTest
    @CsvSource({
        "test-api-key, testUser, test@example.com, true, true, RuntimeException, Unexpected error",
        "test-api-key, testUser, '', true, true, RuntimeException, Unexpected error",
        "test-api-key, testUser, null, true, true, RuntimeException, Unexpected error",
        "test-api-key, testUser, test@example.com, true, false, IllegalStateException, Repository error",
        "test-api-key, testUser, test@example.com, true, false, RuntimeException, Unexpected error",
        "test-api-key, testUser, test@example.com, false, true, IllegalStateException, Repository error"
    })
    @DisplayName("createAccount - Exception Cases")
    void createAccount_exception(
            String apiKey,
            String loginUserId,
            String email,
            boolean createPasswordFlag,
            boolean passwordTemporaryFlag,
            String exceptionType,
            String exceptionMessage) {

        // Arrange
        String idpRealm = "test-realm";
        final String emailConverted = (email != null) ? null : email; // Convert empty string to null for email

        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);
        if (createPasswordFlag) {
            when(keycloakUtil.generateRandomPassword()).thenReturn("randomPassword123");
        }

        if ("RuntimeException".equals(exceptionType)) {
            when(keycloakRepository.createUser(eq(idpRealm), eq(loginUserId), any(), any(), anyBoolean()))
                    .thenThrow(new RuntimeException(exceptionMessage));
        } else if ("IllegalStateException".equals(exceptionType)) {
            when(keycloakRepository.createUser(eq(idpRealm), eq(loginUserId), any(), any(), anyBoolean()))
                    .thenThrow(new IllegalStateException(exceptionMessage));
        }

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            keycloakService.createAccount(apiKey, loginUserId, emailConverted, createPasswordFlag, passwordTemporaryFlag);
        });

        assertEquals(exceptionMessage, exception.getMessage());
        verify(apiKeyService).getIdpRealm(apiKey);
        if (createPasswordFlag) {
            verify(keycloakUtil).generateRandomPassword();
        }
        verify(keycloakRepository).createUser(anyString(), anyString(), any(), any(), anyBoolean());
    }

    /**
     * Verifies that deleteAccount successfully deletes a user when valid parameters are provided.
     */
    @ParameterizedTest
    @CsvSource({
        "test-api-key, test-user-id, test-realm",
        "test-api-key, another-user-id, another-realm"
    })
    @DisplayName("deleteAccount - Success Cases")
    void deleteAccount_success(String apiKey, String userId, String idpRealm) {
        // Arrange
        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);

        // Act
        assertDoesNotThrow(() -> keycloakService.deleteAccount(apiKey, userId));

        // Assert
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository).deleteUser(idpRealm, userId);
    }

    /**
     * Test for deleteAccount when apiKeyService#getIdpRealm throws an exception.
     */
    @Test
    @DisplayName("deleteAccount - Should Throw Exception When getIdpRealm Fails")
    void deleteAccount_shouldThrowException_whenGetIdpRealmFails() {
        // Arrange
        String apiKey = "invalid-api-key";
        String userId = "test-user-id";

        when(apiKeyService.getIdpRealm(apiKey)).thenThrow(new IllegalArgumentException(ConstError.ERR_403_APIKEY_NOT_VALID));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            keycloakService.deleteAccount(apiKey, userId);
        });

        assertEquals(ConstError.ERR_403_APIKEY_NOT_VALID, exception.getMessage());
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository, never()).deleteUser(anyString(), anyString());
    }

    /**
     * Parameterized test for deleteAccount method to handle exception scenarios.
     */
    @ParameterizedTest
    @CsvSource({
        "test-api-key, test-user-id, test-realm, RuntimeException, Unexpected error",
        "test-api-key, test-user-id, test-realm, IllegalStateException, Repository error"
    })
    @DisplayName("deleteAccount - Exception Cases")
    void deleteAccount_exception(
            String apiKey,
            String userId,
            String idpRealm,
            String exceptionType,
            String exceptionMessage) {

        // Arrange
        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);

        if ("RuntimeException".equals(exceptionType)) {
            doThrow(new RuntimeException(exceptionMessage))
                    .when(keycloakRepository).deleteUser(idpRealm, userId);
        } else if ("IllegalStateException".equals(exceptionType)) {
            doThrow(new IllegalStateException(exceptionMessage))
                    .when(keycloakRepository).deleteUser(idpRealm, userId);
        }

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            keycloakService.deleteAccount(apiKey, userId);
        });

        assertEquals(exceptionMessage, exception.getMessage());
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository).deleteUser(idpRealm, userId);
    }

    /**
     * Verifies that updateAccount successfully updates a user and returns IdpAccountInfo.
     */
    @ParameterizedTest
    @CsvSource({
        "test-api-key, user-id-123, false, updated-user-id",
        "test-api-key, user-id-123, true, updated-user-id",
        "test-api-key, user-id-123, null, updated-user-id"
    })
    void updateAccountStatus_success(
            String apiKey,
            String userId,
            String strEnabled,
            String updatedUserId) {

        // Arrange
        String idpRealm = "test-realm";
        String loginUserId = "loginUserId-123";
        String email = "example@email.com";
        Boolean enabled = null;
        Boolean existingEnabled = true;

        if ("null".equals(strEnabled)) {
            enabled = null;
        } else {
            enabled = Boolean.parseBoolean(strEnabled);
        }

        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);
        IdpAccountInfo expectedResult = new IdpAccountInfo(updatedUserId, loginUserId, email, null, existingEnabled);

        when(keycloakRepository.updateUserStatus(eq(idpRealm), eq(userId), eq(enabled)))
                .thenReturn(expectedResult);

        // Act
        IdpAccountInfo result = keycloakService.updateAccountStatus(apiKey, userId, enabled);

        // Assert
        assertNotNull(result);

        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository).updateUserStatus(eq(idpRealm), eq(userId), eq(enabled));
    }

    /**
     * Test for updateAccount when apiKeyService#getIdpRealm throws an exception.
     */
    @Test
    void updateAccountStatus_shouldThrowException_whenGetIdpRealmFails() {
        // Arrange
        String apiKey = "invalid-api-key";
        String userId = "user-id-123";
        Boolean enabled = true;

        // Mock apiKeyService to throw an exception
        when(apiKeyService.getIdpRealm(apiKey)).thenThrow(new IllegalArgumentException(ConstError.ERR_403_APIKEY_NOT_VALID));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            keycloakService.updateAccountStatus(apiKey, userId, enabled);
        });

        assertEquals(ConstError.ERR_403_APIKEY_NOT_VALID, exception.getMessage(), "The exception message should match the expected error message.");

        // Verify that no further interactions occur
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository, never()).updateUserStatus(any(), any(), any());
    }

    /**
     * Parameterized test for updateAccount method to handle exception scenarios.
     */
    @ParameterizedTest
    @CsvSource({
        "RuntimeException, Unexpected error",
        "UnexpectedException, Repository error"
    })
    void updateAccountStatus_exception(
            String exceptionClassName,
            String exceptionMessage) {

        // Arrange
        String apiKey = "invalid-api-key";
        String idpRealm = "test-realm";
        String userId = "user-id-123";
        Boolean enabled = true;

        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);

        Exception exception = null;
        Class<? extends Throwable> clazz = null;
        switch (exceptionClassName) {
            case "RuntimeException":
                exception = new RuntimeException(exceptionMessage);
                clazz = RuntimeException.class;
                break;
            case "UnexpectedException":
                exception = new UnexpectedException(exceptionMessage, exceptionMessage);
                clazz = UnexpectedException.class;
                break;
            default:
                // Unexpected exception
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        // Mock keycloakService to throw an exception
        doThrow(exception).when(keycloakRepository).updateUserStatus(idpRealm, userId, enabled);

        // Act & Assert
        Throwable thrownException = assertThrows(clazz, () -> {
            keycloakService.updateAccountStatus(apiKey, userId, enabled);
        });

        // Detailed verification according to exception type
        if (exception instanceof AbstractBaseException) {
            AbstractBaseException baseException = (AbstractBaseException) exception;
            AbstractBaseException thrownBaseException = (AbstractBaseException) thrownException;
            assertEquals(baseException.getLogMessage(), thrownBaseException.getLogMessage());
            assertEquals(baseException.getResponseMessage(), thrownBaseException.getResponseMessage());
        } else {
            assertEquals(exception.getMessage(), thrownException.getMessage());
        }

        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository).updateUserStatus(eq(idpRealm), eq(userId), eq(enabled));
    }

    /**
     * Verifies that deleteAccount successfully deletes a user when valid parameters are provided.
     */
    @ParameterizedTest
    @CsvSource({
        "test-api-key, test-user-id, test-realm, loginUserId-123, email@example.com, true",
        "test-api-key, another-user-id, another-realm, loginUserId-456, another@example.com, false"
    })
    @DisplayName("getAccount - Success Cases")
    void getAccount_success(String apiKey, String userId, String idpRealm, String loginUserId, String email, String strEnabled) {
        // Arrange
        Boolean enabled = Boolean.parseBoolean(strEnabled);
        IdpAccountInfo idpAccountInfo = new IdpAccountInfo(userId, loginUserId, email, enabled);

        when(keycloakRepository.getUser(idpRealm, userId)).thenReturn(idpAccountInfo);
        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);

        // Act
        IdpAccountInfo result = keycloakService.getAccount(apiKey, userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(loginUserId, result.getLoginUserId());
        assertEquals(email, result.getEmail());
        assertEquals(enabled, result.isEnabled());

        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository).getUser(idpRealm, userId);
    }

    /**
     * Test for deleteAccount when apiKeyService#getIdpRealm throws an exception.
     */
    @Test
    @DisplayName("getAccount - Should Throw Exception When getIdpRealm Fails")
    void getAccount_shouldThrowException_whenGetIdpRealmFails() {
        // Arrange
        String apiKey = "invalid-api-key";
        String userId = "test-user-id";

        when(apiKeyService.getIdpRealm(apiKey)).thenThrow(new IllegalArgumentException(ConstError.ERR_403_APIKEY_NOT_VALID));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            keycloakService.getAccount(apiKey, userId);
        });

        assertEquals(ConstError.ERR_403_APIKEY_NOT_VALID, exception.getMessage());
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository, never()).getUser(anyString(), anyString());
    }

    /**
     * Parameterized test for deleteAccount method to handle exception scenarios.
     */
    @ParameterizedTest
    @CsvSource({
        "RuntimeException, Unexpected error"
    })
    @DisplayName("getAccount - Exception Cases")
    void getAccount_exception(
            String exceptionType,
            String exceptionMessage) {
        String apiKey = "invalid-api-key";
        String idpRealm = "test-realm";
        String userId = "test-user-id";

        // Arrange
        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);

        if ("RuntimeException".equals(exceptionType)) {
            doThrow(new RuntimeException(exceptionMessage))
                    .when(keycloakRepository).getUser(idpRealm, userId);
        }

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            keycloakService.getAccount(apiKey, userId);
        });

        assertEquals(exceptionMessage, exception.getMessage());
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository).getUser(idpRealm, userId);
    }

    /**
     * Verifies that getClientSecret successfully retrieves the client secret.
     *
     * @param clientSecret the expected client secret
     */
    @ParameterizedTest
    @CsvSource({
        "test-client-secret"
    })
    @EmptySource
    void testGetClientSecret_success(String clientSecret) {
        // Arrange
        String apiKey = "test-api-key";
        String realm = "test-realm";
        String clientUuid = "test-client-uuid";

        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(realm);
        when(keycloakRepository.getClientSecret(realm, clientUuid)).thenReturn(clientSecret);

        // Act
        String result = keycloakService.getClientSecret(apiKey, clientUuid);

        // Assert
        assertNotNull(result);
        assertEquals(clientSecret, result);
        verify(keycloakRepository, times(1)).getClientSecret(realm, clientUuid);
    }

    /**
     * Test for getClientSecret when clientUuid is empty.
     */
    @Test
    void testGetClientSecret_emptyClientUuid() {
        // Arrange
        String apiKey = "test-api-key";
        String clientUuid = "";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            keycloakService.getClientSecret(apiKey, clientUuid);
        });
        assertEquals("clientUuid must not be blank", exception.getMessage());
    }

    /**
     * Test for getClientSecret when apiKeyService#getIdpRealm throws an exception.
     */
    @Test
    void getClientSecret_shouldThrowException_whenGetIdpRealmFails() {
        // Arrange
        String apiKey = "invalid-api-key";
        String clientUuid = "test-client-uuid";

        // Mock apiKeyService to throw an exception
        when(apiKeyService.getIdpRealm(apiKey)).thenThrow(new IllegalArgumentException(ConstError.ERR_403_APIKEY_NOT_VALID));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            keycloakService.getClientSecret(apiKey, clientUuid);
        });

        assertEquals(ConstError.ERR_403_APIKEY_NOT_VALID, exception.getMessage(), "The exception message should match the expected error message.");

        // Verify that no further interactions occur
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository, never()).getClientSecret(any(), any());
    }

    /**
     * Parameterized test for getClientSecret method to handle exception scenarios.
     */
    @ParameterizedTest
    @CsvSource({
        "RuntimeException, Unexpected error",
        "UnexpectedException, Repository error"
    })
    void getClientSecret_exception(
            String exceptionType,
            String exceptionMessage) {
        String apiKey = "invalid-api-key";
        String idpRealm = "test-realm";
        String clientUuid = "test-client-uuid";

        // Arrange
        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);
        if ("RuntimeException".equals(exceptionType)) {
            doThrow(new RuntimeException(exceptionMessage))
                    .when(keycloakRepository).getClientSecret(idpRealm, clientUuid);
        } else {
            doThrow(new UnexpectedException(exceptionMessage, exceptionMessage))
                    .when(keycloakRepository).getClientSecret(idpRealm, clientUuid);
        }

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            keycloakService.getClientSecret(apiKey, clientUuid);
        });
        assertEquals(exceptionMessage, exception.getMessage());
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository).getClientSecret(idpRealm, clientUuid);
    }

    /**
     * Verifies that registerClient successfully register a user and returns IdpClientInfo.
     */
    @ParameterizedTest
    @CsvSource({
        "test-api-key, client-id-123, client-name, client-desc, authorization_code, 'http://localhost/redirect', operator-001, open-system-001, test-realm"
    })
    @DisplayName("registerClient - Success Cases")
    void testRegisterClient_success(
            String apiKey,
            String clientId,
            String name,
            String description,
            String flowType,
            String redirectUri,
            String operatorId,
            String openSystemId,
            String idpRealm) {
        // Arrange
        List<String> redirectUris = (redirectUri == null || redirectUri.isEmpty()) ? null : List.of(redirectUri);
        IdpClientInfo expectedResult = new IdpClientInfo(
                commonUuid,           // uuid
                clientId,             // clientId
                name,                 // name
                description,          // description
                redirectUris,         // redirectUris
                commonClientSecret,   // clientSecret
                commonPublicClient,   // publicClient
                commonEnabled         // enabled
        );

        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);
        when(keycloakRepository.registerClient(eq(idpRealm), eq(clientId), eq(name), eq(description), eq(flowType), eq(redirectUris), eq(operatorId), eq(openSystemId)))
                .thenReturn(expectedResult);

        // Act
        IdpClientInfo result = keycloakService.registerClient(apiKey, clientId, name, description, flowType, redirectUris, operatorId, openSystemId);

        // Assert
        assertNotNull(result);
        assertEquals(commonUuid, result.getUuid());
        assertEquals(clientId, result.getClientId());
        assertEquals(name, result.getName());
        assertEquals(description, result.getDescription());
        assertEquals(redirectUris, result.getRedirectUris());
        assertEquals(commonClientSecret, result.getClientSecret());
        assertFalse(result.isPublicClient());
        assertTrue(result.isEnabled());
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository).registerClient(eq(idpRealm), eq(clientId), eq(name), eq(description), eq(flowType), eq(redirectUris), eq(operatorId), eq(openSystemId));
    }

    /**
     * Parameterized test for registerClient method to handle exception scenarios.
     */
    @ParameterizedTest
    @CsvSource({
        "ConflictException, Conflict error",
        "BadParametersException, Bad parameters error",
        "UnexpectedException, Unexpected error"
    })
    void testRegisterClient_exception(
            String exceptionClassName,
            String exceptionMessage) {

        // Arrange
        when(apiKeyService.getIdpRealm(commonApiKey)).thenReturn(commonIdpRealm);

        Exception exception = null;
        Class<? extends Throwable> clazz = null;
        switch (exceptionClassName) {
            case "ConflictException":
                exception = new ConflictException(exceptionMessage);
                clazz = ConflictException.class;
                break;
            case "BadParametersException":
                exception = new BadParametersException(exceptionMessage);
                clazz = BadParametersException.class;
                break;
            case "UnexpectedException":
                exception = new UnexpectedException(exceptionMessage, exceptionMessage);
                clazz = UnexpectedException.class;
                break;
            default:
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        // Mock repository to throw exception
        when(keycloakRepository.registerClient(eq(commonIdpRealm), eq(commonClientId), eq(commonName), eq(commonDescription), eq(commonFlowType), eq(commonRedirectUrisList), eq(commonOperatorId), eq(commonOpenSystemId)))
                .thenThrow(exception);

        // Act & Assert
        Throwable thrownException = assertThrows(clazz, () -> {
            keycloakService.registerClient(commonApiKey, commonClientId, commonName, commonDescription, commonFlowType, commonRedirectUrisList, commonOperatorId, commonOpenSystemId);
        });

        if (exception instanceof AbstractBaseException) {
            AbstractBaseException baseException = (AbstractBaseException) exception;
            AbstractBaseException thrownBaseException = (AbstractBaseException) thrownException;
            assertEquals(baseException.getLogMessage(), thrownBaseException.getLogMessage());
            assertEquals(baseException.getResponseMessage(), thrownBaseException.getResponseMessage());
        } else {
            assertEquals(exception.getMessage(), thrownException.getMessage());
        }

        verify(apiKeyService).getIdpRealm(commonApiKey);
        verify(keycloakRepository).registerClient(eq(commonIdpRealm), eq(commonClientId), eq(commonName), eq(commonDescription), eq(commonFlowType), eq(commonRedirectUrisList), eq(commonOperatorId), eq(commonOpenSystemId));
    }

    /**
     * Verifies that deleteClient successfully deletes a user.
     */
    @ParameterizedTest
    @CsvSource({
        "test-api-key, client-id-123, test-realm"
    })
    @DisplayName("deleteClient - Success Cases")
    void testDeleteClient_success(
            String apiKey,
            String clientId,
            String idpRealm
    ) {
        // Arrange
        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);
        doNothing().when(keycloakRepository).deleteClient(eq(idpRealm), eq(clientId));

        // Act
        keycloakService.deleteClient(apiKey, clientId);
        // Assert
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository).deleteClient(eq(idpRealm), eq(clientId));
    }

    /**
     * Parameterized test for deleteClient method to handle exception scenarios.
     */
    @ParameterizedTest
    @CsvSource({
        "UnexpectedException, Unexpected error"
    })
    void testDeleteClient_exception(
            String exceptionClassName,
            String exceptionMessage) {

        // Arrange
        when(apiKeyService.getIdpRealm(commonApiKey)).thenReturn(commonIdpRealm);

        Exception exception = null;
        Class<? extends Throwable> clazz = null;
        switch (exceptionClassName) {
            case "UnexpectedException":
                exception = new UnexpectedException(exceptionMessage, exceptionMessage);
                clazz = UnexpectedException.class;
                break;
            default:
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        // Mock repository to throw exception
        doThrow(exception).when(keycloakRepository).deleteClient(eq(commonIdpRealm), eq(commonClientId));

        // Act & Assert
        Throwable thrownException = assertThrows(clazz, () -> {
            keycloakService.deleteClient(commonApiKey, commonClientId);
        });

        if (exception instanceof AbstractBaseException) {
            AbstractBaseException baseException = (AbstractBaseException) exception;
            AbstractBaseException thrownBaseException = (AbstractBaseException) thrownException;
            assertEquals(baseException.getLogMessage(), thrownBaseException.getLogMessage());
            assertEquals(baseException.getResponseMessage(), thrownBaseException.getResponseMessage());
        } else {
            assertEquals(exception.getMessage(), thrownException.getMessage());
        }

        verify(apiKeyService).getIdpRealm(commonApiKey);
        verify(keycloakRepository).deleteClient(eq(commonIdpRealm), eq(commonClientId));
    }

    /**
     * Verifies that buildIssuer successfully builds the issuer URL.
     */
    @Test
    @DisplayName("buildIssuer - success")
    void testBuildIssuer_success() {
        // Arrange
        String idpRealm = "test-realm";
        String expectedIssuer = "http://keycloak-server/auth/realms/" + idpRealm;

        when(apiKeyService.getIdpRealm(anyString())).thenReturn(idpRealm);
        when(keycloakUtil.getIssuer(idpRealm)).thenReturn(expectedIssuer);

        // Act
        String actualIssuer = keycloakService.buildIssuer(anyString());

        // Assert
        assertEquals(expectedIssuer, actualIssuer);
        verify(apiKeyService).getIdpRealm(anyString());
        verify(keycloakUtil).getIssuer(idpRealm);
    }

    /**
     * Test for buildIssuer when apiKeyService#getIdpRealm throws an exception.
     */
    @Test
    @DisplayName("buildIssuer - Should Throw Exception When getIdpRealm Fails")
    void testBuildIssuer_throwExceptionGetIdpRealm() {
        // Arrange
        when(apiKeyService.getIdpRealm(anyString())).thenThrow(new IllegalArgumentException(ConstError.ERR_403_APIKEY_NOT_VALID));

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                keycloakService.buildIssuer(anyString())
        );

        // Assert
        assertEquals(ConstError.ERR_403_APIKEY_NOT_VALID, exception.getMessage(), "The exception message should match the expected error message.");

        verify(apiKeyService).getIdpRealm(anyString());
    }

    /**
     * Test for buildIssuer when keycloakUtil#getIssuer throws an exception.
     */
    @Test
    @DisplayName("buildIssuer - Should Throw Exception When getIssuer Fails")
    void testBuildIssuer_throwExceptionGetIssuer() {
        // Arrange
        String idpRealm = "test-realm";

        when(apiKeyService.getIdpRealm(anyString())).thenReturn(idpRealm);
        when(keycloakUtil.getIssuer(idpRealm)).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                keycloakService.buildIssuer(anyString())
        );

        // Assert
        assertEquals("Unexpected error", exception.getMessage());
        verify(apiKeyService).getIdpRealm(anyString());
        verify(keycloakUtil).getIssuer(idpRealm);
    }

    /**
     * Verifies that buildJwksUri successfully builds the JWKS URI.
     */
    @Test
    @DisplayName("buildJwksUri - success")
    void testBuildJwksUri_success() {
        String expectedJwksUri = "http://keycloak-server/auth/realms/test-realm/protocol/openid-connect/certs";

        when(keycloakUtil.getJwksUri(anyString())).thenReturn(expectedJwksUri);

        // Act
        String actualJwksUri = keycloakService.buildJwksUri(anyString());

        // Assert
        assertEquals(expectedJwksUri, actualJwksUri);
        verify(keycloakUtil).getJwksUri(anyString());
    }

    /**
     * Test for buildJwksUri when keycloakUtil#getJwksUri throws an exception.
     */
    @Test
    @DisplayName("buildJwksUri - Should Throw Exception When getJwksUri Fails")
    void testBuildJwksUri_throwExceptionGetJwksUri() {
        // Arrange
        when(keycloakUtil.getJwksUri(anyString())).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                keycloakService.buildJwksUri(anyString())
        );

        // Assert
        assertEquals("Unexpected error", exception.getMessage());
        verify(keycloakUtil).getJwksUri(anyString());
    }

    /**
     * Verifies that token revoke successfully revokes a token and returns TokenRevokeResult.
     */
    @Test
    void testTokenRevoke_success() {
        String apikey = "a";
        String clientId = "a";
        String clientSecret = "a";
        String refreshToken = "a";
        String idpRealm = "a";

        TokenRevokeResult expectedResult = new TokenRevokeResult();

        when(apiKeyService.getIdpRealm(anyString())).thenReturn(idpRealm);
        when(keycloakRepository.revoke(anyString(), anyString(), anyString(), anyString())).thenReturn(expectedResult);

        // Act
        TokenRevokeResult result = keycloakService.revoke(apikey, clientId, clientSecret, refreshToken);

        // Assert
        assertNotNull(result);
        assertEquals(false, result.isActive());
    }

    /**
     * Test for token revoke when apiKeyService#getIdpRealm throws an exception.
     */
    @Test
    void testTokenRevoke_realmError() {
        String apikey = "a";
        String clientId = "a";
        String clientSecret = "a";
        String refreshToken = "a";

        String exceptionResponseMessage = null;
        Exception exception = null;
        Class<? extends Throwable> clazz = null;

        exceptionResponseMessage = ConstError.ERR_403_APIKEY_NOT_VALID;
        exception = new IllegalArgumentException(exceptionResponseMessage);
        clazz = IllegalArgumentException.class;

        when(apiKeyService.getIdpRealm(anyString())).thenThrow(exception);

        // Act & Assert
        assertThrows(clazz, () -> keycloakService.revoke(apikey, clientId, clientSecret, refreshToken));
        assertEquals(exceptionResponseMessage, exception.getMessage());
    }

    /**
     * Parameterized test for token revoke method to handle exception scenarios.
     *
     * @param exceptionClassName the name of the exception class to be tested
     */
    @ParameterizedTest
    @CsvSource({
        "BadParametersException",   // case #3:
        "UnexpectedException",      // case #4:
        "OutOfServiceException"     // case #5:
    })
    void testTokenRevoke_throwsError(String exceptionClassName) {
        String apikey = "a";
        String clientId = "a";
        String clientSecret = "a";
        String refreshToken = "a";
        String idpRealm = "a";

        String exceptionLogMessage = null;
        String exceptionResponseMessage = null;
        AbstractBaseException exception = null;
        Class<? extends Throwable> clazz = null;

        switch (exceptionClassName) {
            case "BadParametersException":
                exceptionLogMessage = String.format(ConstError.ERRLOG_400_INVALID_GRANT, "dummy message");
                exceptionResponseMessage = ConstError.ERR_400_INVALID_GRANT;
                exception = new BadParametersException(exceptionLogMessage, exceptionResponseMessage);
                clazz = BadParametersException.class;
                break;
            case "OutOfServiceException":
                exceptionLogMessage = ConstError.ERR_503_OUT_OF_SERVICE_KEYCLOAK;
                exceptionResponseMessage = "";
                exception = new OutOfServiceException(exceptionLogMessage);
                clazz = OutOfServiceException.class;
                break;
            case "UnexpectedException":
                exceptionLogMessage = String.format(ConstError.ERRLOG_500_ERROR, ConstError.ERRLOG_500_KEYCLOAK_BUILD, ConstError.ERR_500_MESSAGE);
                exceptionResponseMessage = ConstError.ERR_500_MESSAGE;
                exception = new UnexpectedException(ConstError.ERRLOG_500_KEYCLOAK_BUILD, exceptionResponseMessage);
                clazz = UnexpectedException.class;
                break;
            default:
                // unknown exception
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        // Arrange
        when(apiKeyService.getIdpRealm(anyString())).thenReturn(idpRealm);
        when(keycloakRepository.revoke(anyString(), anyString(), anyString(), anyString())).thenThrow(exception);

        // Act & Assert
        assertThrows(clazz, () -> keycloakService.revoke(apikey, clientId, clientSecret, refreshToken));
        assertEquals(exceptionLogMessage, exception.getLogMessage());
        assertEquals(exceptionResponseMessage, exception.getResponseMessage());
    }

    /**
     * Verifies that getClientUuid successfully retrieves the client UUID.
     */
    @ParameterizedTest
    @CsvSource({
        "test-api-key, client-id-123, test-realm, client-uuid-123"
    })
    @DisplayName("getClientUuid - Success Cases")
    void testGetClientUuid_success(
            String apiKey,
            String clientId,
            String idpRealm,
            String clientUuid) {
        // Arrange
        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);
        when(keycloakRepository.getClientUuidByClientId(eq(idpRealm), eq(clientId))).thenReturn(clientUuid);

        // Act
        String result = keycloakService.getClientUuid(apiKey, clientId);

        // Assert
        assertNotNull(result);
        assertEquals(clientUuid, result);
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository).getClientUuidByClientId(eq(idpRealm), eq(clientId));
    }

    /**
     * Parameterized test for getClientUuid method to handle exception scenarios.
     */
    @ParameterizedTest
    @CsvSource({
        "NotFoundException, Not Found error"
    })
    void testGetClientUuid_notFoundException(
            String exceptionClassName,
            String exceptionMessage) {

        // Arrange
        when(apiKeyService.getIdpRealm(commonApiKey)).thenReturn(commonIdpRealm);

        Exception exception = null;
        Class<? extends Throwable> clazz = null;
        switch (exceptionClassName) {
            case "NotFoundException":
                exception = new NotFoundException(exceptionMessage);
                clazz = NotFoundException.class;
                break;
            default:
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        // Mock repository to throw exception
        when(keycloakRepository.getClientUuidByClientId(eq(commonIdpRealm), eq(commonClientId))).thenThrow(exception);

        // Act & Assert
        Throwable thrownException = assertThrows(clazz, () -> {
            keycloakService.getClientUuid(commonApiKey, commonClientId);
        });

        if (exception instanceof AbstractBaseException) {
            AbstractBaseException baseException = (AbstractBaseException) exception;
            AbstractBaseException thrownBaseException = (AbstractBaseException) thrownException;
            assertEquals(baseException.getLogMessage(), thrownBaseException.getLogMessage());
            assertEquals(baseException.getResponseMessage(), thrownBaseException.getResponseMessage());
        } else {
            assertEquals(exception.getMessage(), thrownException.getMessage());
        }

        verify(apiKeyService).getIdpRealm(commonApiKey);
        verify(keycloakRepository).getClientUuidByClientId(eq(commonIdpRealm), eq(commonClientId));
    }

    /**
     * Parameterized test for getClientUuid method to handle exception scenarios.
     */
    @ParameterizedTest
    @CsvSource({
        "UnexpectedException,UnexpectedException error"
    })
    void testGetClientUuid_exception(
            String exceptionClassName,
            String exceptionMessage) {

        // Arrange
        when(apiKeyService.getIdpRealm(commonApiKey)).thenReturn(commonIdpRealm);

        Exception exception = null;
        Class<? extends Throwable> clazz = null;
        switch (exceptionClassName) {
            case "UnexpectedException":
                exception = new UnexpectedException(exceptionMessage, exceptionMessage);
                clazz = UnexpectedException.class;
                break;
            default:
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }

        // Mock repository to throw exception
        when(keycloakRepository.getClientUuidByClientId(eq(commonIdpRealm), eq(commonClientId))).thenThrow(exception);

        // Act & Assert
        Throwable thrownException = assertThrows(clazz, () -> {
            keycloakService.getClientUuid(commonApiKey, commonClientId);
        });

        if (exception instanceof AbstractBaseException) {
            AbstractBaseException baseException = (AbstractBaseException) exception;
            AbstractBaseException thrownBaseException = (AbstractBaseException) thrownException;
            assertEquals(baseException.getLogMessage(), thrownBaseException.getLogMessage());
            assertEquals(baseException.getResponseMessage(), thrownBaseException.getResponseMessage());
        } else {
            assertEquals(exception.getMessage(), thrownException.getMessage());
        }

        verify(apiKeyService).getIdpRealm(commonApiKey);
        verify(keycloakRepository).getClientUuidByClientId(eq(commonIdpRealm), eq(commonClientId));
    }

    /**
     * Verifies that updateClient successfully retrieves the client.
     */
    @ParameterizedTest
    @CsvSource({
        "test-api-key, client-uuid-123, test-name, test-description, authorization_code, test-open-system-id, test-operator-id, test-realm"
    })
    @DisplayName("updateClient - Success Cases")
        void testUpdateClient_success(
            String apiKey,
            String clientUuid,
            String name,
            String description,
            String flowType,
            String openSystemId,
            String operatorId,
            String idpRealm) {
        // Arrange
        IdpClientInfo expected = new IdpClientInfo(clientUuid, commonClientId, name, description,
                commonRedirectUrisList, commonClientSecret, commonPublicClient, commonEnabled);
        when(apiKeyService.getIdpRealm(apiKey)).thenReturn(idpRealm);
        when(keycloakRepository.updateClient(eq(idpRealm), eq(clientUuid), eq(name), eq(description),
            eq(flowType), eq(openSystemId), eq(operatorId), eq(commonRedirectUrisList))).thenReturn(expected);

        // Act
        IdpClientInfo result = keycloakService.updateClient(apiKey, clientUuid, name, description,
                flowType, openSystemId, operatorId, commonRedirectUrisList);

        // Assert
        assertNotNull(result);
        assertEquals(expected, result);
        verify(apiKeyService).getIdpRealm(apiKey);
        verify(keycloakRepository).updateClient(eq(idpRealm), eq(clientUuid), eq(name), eq(description),
                eq(flowType), eq(openSystemId), eq(operatorId), eq(commonRedirectUrisList));
    }

    /**
     * Parameterized test for updateClient method to handle exception scenarios.
     */
    @ParameterizedTest
    @CsvSource({
        "NotFoundException, Not Found error"
    })
    void testUpdateClient_notFoundException(
            String exceptionClassName,
            String exceptionMessage) {

        // Arrange
        when(apiKeyService.getIdpRealm(commonApiKey)).thenReturn(commonIdpRealm);

        assertEquals("NotFoundException", exceptionClassName);
        NotFoundException exception = new NotFoundException(exceptionMessage);

        // Mock repository to throw exception
        when(keycloakRepository.updateClient(eq(commonIdpRealm), eq(commonClientId), eq(commonName), eq(commonDescription),
            eq(commonFlowType), eq(commonOpenSystemId), eq(commonOperatorId), eq(commonRedirectUrisList))).thenThrow(exception);

        // Act & Assert
        NotFoundException thrownException = assertThrows(NotFoundException.class, () -> {
            keycloakService.updateClient(commonApiKey, commonClientId, commonName, commonDescription,
                    commonFlowType, commonOpenSystemId, commonOperatorId, commonRedirectUrisList);
        });

        assertEquals(exception.getLogMessage(), thrownException.getLogMessage());
        assertEquals(exception.getResponseMessage(), thrownException.getResponseMessage());

        verify(apiKeyService).getIdpRealm(commonApiKey);
        verify(keycloakRepository).updateClient(eq(commonIdpRealm), eq(commonClientId), eq(commonName), eq(commonDescription),
                eq(commonFlowType), eq(commonOpenSystemId), eq(commonOperatorId), eq(commonRedirectUrisList));
    }

    /**
     * Parameterized test for updateClient method to handle exception scenarios.
     */
    @ParameterizedTest
    @CsvSource({
        "BadParametersException,BadParametersException error"
    })
    void testUpdateClient_badParametersException(
            String exceptionClassName,
            String exceptionMessage) {

        // Arrange
        when(apiKeyService.getIdpRealm(commonApiKey)).thenReturn(commonIdpRealm);

        assertEquals("BadParametersException", exceptionClassName);
        BadParametersException exception = new BadParametersException(exceptionMessage);

        // Mock repository to throw exception
        when(keycloakRepository.updateClient(eq(commonIdpRealm), eq(commonClientId), eq(commonName), eq(commonDescription),
            eq(commonFlowType), eq(commonOpenSystemId), eq(commonOperatorId), eq(commonRedirectUrisList))).thenThrow(exception);

        // Act & Assert
        BadParametersException thrownException = assertThrows(BadParametersException.class, () -> {
            keycloakService.updateClient(commonApiKey, commonClientId, commonName, commonDescription,
                    commonFlowType, commonOpenSystemId, commonOperatorId, commonRedirectUrisList);
        });

        assertEquals(exception.getLogMessage(), thrownException.getLogMessage());
        assertEquals(exception.getResponseMessage(), thrownException.getResponseMessage());

        verify(apiKeyService).getIdpRealm(commonApiKey);
        verify(keycloakRepository).updateClient(eq(commonIdpRealm), eq(commonClientId), eq(commonName), eq(commonDescription),
                eq(commonFlowType), eq(commonOpenSystemId), eq(commonOperatorId), eq(commonRedirectUrisList));
    }

    /**
     * Parameterized test for updateClient method to handle exception scenarios.
     */
    @ParameterizedTest
    @CsvSource({
        "UnexpectedException,UnexpectedException error"
    })
    void testUpdateClient_exception(
            String exceptionClassName,
            String exceptionMessage) {

        // Arrange
        when(apiKeyService.getIdpRealm(commonApiKey)).thenReturn(commonIdpRealm);

        assertEquals("UnexpectedException", exceptionClassName);
        UnexpectedException exception = new UnexpectedException(exceptionMessage, exceptionMessage);

        // Mock repository to throw exception
        when(keycloakRepository.updateClient(eq(commonIdpRealm), eq(commonClientId), eq(commonName), eq(commonDescription),
            eq(commonFlowType), eq(commonOpenSystemId), eq(commonOperatorId), eq(commonRedirectUrisList))).thenThrow(exception);

        // Act & Assert
        UnexpectedException thrownException = assertThrows(UnexpectedException.class, () -> {
            keycloakService.updateClient(commonApiKey, commonClientId, commonName, commonDescription,
                    commonFlowType, commonOpenSystemId, commonOperatorId, commonRedirectUrisList);
        });

        assertEquals(exception.getLogMessage(), thrownException.getLogMessage());
        assertEquals(exception.getResponseMessage(), thrownException.getResponseMessage());

        verify(apiKeyService).getIdpRealm(commonApiKey);
        verify(keycloakRepository).updateClient(eq(commonIdpRealm), eq(commonClientId), eq(commonName), eq(commonDescription),
                eq(commonFlowType), eq(commonOpenSystemId), eq(commonOperatorId), eq(commonRedirectUrisList));
    }

}
