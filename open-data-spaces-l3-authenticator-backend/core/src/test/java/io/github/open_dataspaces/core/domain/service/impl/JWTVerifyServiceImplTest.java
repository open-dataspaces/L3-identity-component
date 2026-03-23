/*
 * JWTVerifyServiceImplTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This file is part of the user authentication backend system.
 * Package: io.github.open_dataspaces.core.domain.service.impl
 * This package contains service implementation classes and their corresponding unit tests
 * for the user authentication backend domain logic.
 */

package io.github.open_dataspaces.core.domain.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.client.ResourceAccessException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.service.factory.interfaces.JwtDecoderProvider;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.exception.UnauthorizedException;
import io.github.open_dataspaces.core.exception.UnexpectedException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Unit tests for the JWTVerifyServiceImpl class.
 *
 * <p>This test class verifies the correct behavior of JWT verification logic,
 * including extracting claims, validating tokens, and handling error cases.
 * It covers both normal and edge cases using mocked HTTP requests and various JWT payloads.
 * </p>
 *
 * @author btmurayamakohei
 * @since 2025/06/26
 */
@ExtendWith(MockitoExtension.class)
class JWTVerifyServiceImplTest {

    @InjectMocks
    private JWTVerifyServiceImpl jwtVerifyServiceImpl;

    @Mock
    private IdentityProviderService identityProviderService;
    @Mock
    private JwtDecoderProvider jwtDecoderProvider;
    @Mock
    private HttpServletRequest request;

    // Common parameter
    String commonOperatorId = "operator-123";
    String commonOpenSystemId = "open-system-456";

    String commonApiKey = "api-key-123";
    String commonIssuer = "https://example.com/issuer";
    String commonJwksUri = "https://example.com/jwks-uri";
    long commonCacheSize = 10L;
    long commonCacheDurationSecond = 60L;
    String commonClaimExpectedAudience = "expected-audience";
    String commonClaimExpecteType = "expected-type";

    String commonToken = "header.payload.signature";
    String commonAuthorizationHeader = "Bearer " + commonToken;

    /**
     * Test case: getOperatorOrOpenSystemId - success case.
     *
     * @param argOperatorId   "VALUE" to include operator_id claim, "NULL" to exclude it
     * @param argOpenSystemId "VALUE" to include open_system_id claim, "NULL" to exclude it
     * @throws JsonProcessingException if JSON processing fails
     */
    @ParameterizedTest
    @CsvSource({
        "VALUE,NULL",
        "NULL,VALUE",
        "VALUE,VALUE"
    })
    @DisplayName("getOperatorOrOpenSystemId - Success")
    void getOperatorOrOpenSystemId_success(String argOperatorId, String argOpenSystemId) throws JsonProcessingException {
        Map<String, String> claims = new HashMap<>();
        String expectedValue = null;
        if ("VALUE".equals(argOperatorId)) {
            claims.put(Const.JWT_CLAIM_OPERATOR_ID, commonOperatorId);
            expectedValue = commonOperatorId;
        }
        if ("VALUE".equals(argOpenSystemId)) {
            claims.put(Const.JWT_CLAIM_OPEN_SYSTEM_ID, commonOpenSystemId);
            if (expectedValue == null) {
                expectedValue = commonOpenSystemId;
            }
        }
        ObjectMapper objectMapper = new ObjectMapper();
        String payloadJson = objectMapper.writeValueAsString(claims);

        // Mock request to return Authorization header
        when(request.getHeader(Const.HEADER_AUTHORIZATION)).thenReturn(buildAuthorizationHeader(payloadJson));
        // act
        String result = jwtVerifyServiceImpl.getOperatorOrOpenSystemId(request);

        // assert
        assertEquals(expectedValue, result);
    }

    /**
     * Builds an Authorization header with the specified claim key and value.
     */
    String buildAuthorizationHeader(String payloadJson) {
        String payloadBase64 = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        return commonAuthorizationHeader.replace("payload", payloadBase64);
    }

    /**
     * Test case: getOperatorOrOpenSystemId - failure case.
     */
    @ParameterizedTest
    @CsvSource({
        "EMPTY",
        "EMPTY_TOKEN",
        "INVALID_TOKEN_FORMAT_NO_DOTS",
        "INVALID_TOKEN_FORMAT_NO_JSON_PAYLOAD",
        "INVALID_TOKEN_FORMAT_NULL_CLAIMS",
        "INVALID_TOKEN_FORMAT_EMPTY_CLAIMS",
        "NO_OPERATOR_ID_NO_OPEN_SYSTEM_ID",
        "OPERATOR_ID_BLANK",
        "OPEN_SYSTEM_ID_BLANK"
    })
    @DisplayName("getOperatorOrOpenSystemId - Failure")
    void getOperatorOrOpenSystemId_failure(String argCase) {
        String authorizationHeader = null;
        String exceptionLogMessage = null;
        String exceptionResponseMessage = null;
        switch (argCase) {
            case "EMPTY":
                authorizationHeader = null;
                exceptionLogMessage = ConstError.ERRLOG_401_TOKEN_EMPTY;
                exceptionResponseMessage = ConstError.ERR_401_TOKEN_EMPTY;
                break;
            case "EMPTY_TOKEN":
                authorizationHeader = "Bearer ";
                exceptionLogMessage = ConstError.ERRLOG_401_TOKEN_EMPTY;
                exceptionResponseMessage = ConstError.ERR_401_TOKEN_EMPTY;
                break;
            case "INVALID_TOKEN_FORMAT_NO_DOTS":
                authorizationHeader = "Bearer invalidtoken";
                exceptionLogMessage = ConstError.ERRLOG_401_INVALID_TOKEN;
                exceptionResponseMessage = ConstError.ERR_401_INVALID_TOKEN;
                break;
            case "INVALID_TOKEN_FORMAT_NO_JSON_PAYLOAD":
                authorizationHeader = "Bearer header.invalid-payload.signature";
                exceptionLogMessage = ConstError.ERRLOG_401_INVALID_JWT.replace("%s.", "");  // empty payload after decoding
                exceptionResponseMessage = ConstError.ERR_401_INVALID_TOKEN;
                break;
            case "INVALID_TOKEN_FORMAT_NULL_CLAIMS":
                authorizationHeader = buildAuthorizationHeader("null");
                exceptionLogMessage = ConstError.ERRLOG_401_INVALID_CLAIM;
                exceptionResponseMessage = ConstError.ERR_401_INVALID_TOKEN;
                break;
            case "INVALID_TOKEN_FORMAT_EMPTY_CLAIMS":
                authorizationHeader = buildAuthorizationHeader("{}");
                exceptionLogMessage = ConstError.ERRLOG_401_INVALID_CLAIM;
                exceptionResponseMessage = ConstError.ERR_401_INVALID_TOKEN;
                break;
            case "NO_OPERATOR_ID_NO_OPEN_SYSTEM_ID":
                String payloadJson = String.format("{\"%s\":\"%s\"}", "invalid_key", "some_value");
                authorizationHeader = buildAuthorizationHeader(payloadJson);
                exceptionLogMessage = ConstError.ERRLOG_401_INVALID_CLAIM_NO_OPERATOR_OR_OPEN_SYSTEM_ID;
                exceptionResponseMessage = ConstError.ERR_401_INVALID_OR_EXPIRED_TOKEN;
                break;
            case "OPERATOR_ID_BLANK":
                payloadJson = String.format("{\"%s\":\"%s\"}", Const.JWT_CLAIM_OPERATOR_ID, "   ");
                authorizationHeader = buildAuthorizationHeader(payloadJson);
                exceptionLogMessage = ConstError.ERRLOG_401_INVALID_CLAIM_NO_OPERATOR_OR_OPEN_SYSTEM_ID;
                exceptionResponseMessage = ConstError.ERR_401_INVALID_OR_EXPIRED_TOKEN;
                break;
            case "OPEN_SYSTEM_ID_BLANK":
                payloadJson = String.format("{\"%s\":\"%s\"}", Const.JWT_CLAIM_OPEN_SYSTEM_ID, "   ");
                authorizationHeader = buildAuthorizationHeader(payloadJson);
                exceptionLogMessage = ConstError.ERRLOG_401_INVALID_CLAIM_NO_OPERATOR_OR_OPEN_SYSTEM_ID;
                exceptionResponseMessage = ConstError.ERR_401_INVALID_OR_EXPIRED_TOKEN;
                break;
            default:
                throw new IllegalArgumentException("Unknown case: " + argCase);
        }
        // Mock request to return Authorization header
        when(request.getHeader(Const.HEADER_AUTHORIZATION)).thenReturn(authorizationHeader);
        // act
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            jwtVerifyServiceImpl.getOperatorOrOpenSystemId(request);
        });

        // assert
        assertTrue(exception.getLogMessage().contains(exceptionLogMessage),
                String.format("Expected log message to contain '%s' but was '%s'", exceptionLogMessage, exception.getLogMessage()));    // partial match
        assertEquals(exceptionResponseMessage, exception.getResponseMessage(),
                String.format("Expected response message to be '%s' but was '%s'", exceptionResponseMessage, exception.getResponseMessage())); // exact match
    }

    /**
     * Test case: verifyToken - success case.
     */
    @Test
    @DisplayName("verifyToken - Success")
    void verifyToken_success() {
        // arrange
        // Mock identityProviderService to return issuer
        when(identityProviderService.buildIssuer(anyString())).thenReturn(commonIssuer);

        // Mock JwtDecoder and Jwt
        JwtDecoder mockDecoder = mock(JwtDecoder.class);
        when(jwtDecoderProvider.getDecoder(anyString())).thenReturn(mockDecoder);
        // Mock request to return Authorization header
        when(request.getHeader(Const.HEADER_AUTHORIZATION)).thenReturn(commonAuthorizationHeader);
        // Mock JwtDecoder to return a valid Jwt
        when(mockDecoder.decode(anyString())).thenReturn(mock(Jwt.class));

        // act
        boolean result = jwtVerifyServiceImpl.verifyToken(commonApiKey, request);

        // assert
        assertTrue(result);
        verify(identityProviderService).buildIssuer(commonApiKey);
        verify(request).getHeader(Const.HEADER_AUTHORIZATION);
        verify(mockDecoder).decode(commonToken);
    }

    /**
     * Test case: verifyToken - failure case due to invalid issuer.
     */
    @Test
    @DisplayName("verifyToken - Invalid Issuer")
    void verifyToken_invalidIssuer() {
        // arrange
        // Mock identityProviderService to return issuer
        when(identityProviderService.buildIssuer(anyString())).thenThrow(RuntimeException.class);

        // act and assert
        assertThrows(RuntimeException.class, () -> {
            jwtVerifyServiceImpl.verifyToken(commonApiKey, request);
        });

        // assert
        verify(identityProviderService).buildIssuer(commonApiKey);
    }

    /**
     * Test case: verifyToken - Decoder get throws JwtException.
     */
    @Test
    @DisplayName("verifyToken - Decoder get throws JwtException")
    void verifyToken_decoderGetThrowJwtException() {
        // arrange
        // Mock identityProviderService to return issuer
        when(identityProviderService.buildIssuer(anyString())).thenReturn(commonIssuer);

        // Mock JwtDecoder and Jwt
        when(jwtDecoderProvider.getDecoder(anyString())).thenThrow(JwtException.class);

        // act and assert
        assertThrows(JwtException.class, () -> {
            jwtVerifyServiceImpl.verifyToken(commonApiKey, request);
        });

        // assert
        verify(identityProviderService).buildIssuer(commonApiKey);
    }

    /**
     * Test case: verifyToken - Invalid Authorization header.
     */
    @ParameterizedTest
    @CsvSource({
        "EMPTY",
        "EMPTY_TOKEN"
    })
    @DisplayName("verifyToken - Invalid Authorization header")
    void verifyToken_invalidAuthorizationHeader(String argAuthorizationHeader) {
        String authorizationHeader =
                "EMPTY".equals(argAuthorizationHeader) ? null :
                "EMPTY_TOKEN".equals(argAuthorizationHeader) ? "Bearer " : argAuthorizationHeader;

        // arrange
        // Mock identityProviderService to return issuer
        when(identityProviderService.buildIssuer(anyString())).thenReturn(commonIssuer);

        // Mock JwtDecoder and Jwt
        JwtDecoder mockDecoder = mock(JwtDecoder.class);
        when(jwtDecoderProvider.getDecoder(anyString())).thenReturn(mockDecoder);

        // Mock request to return null Authorization header
        when(request.getHeader(Const.HEADER_AUTHORIZATION)).thenReturn(authorizationHeader);

        // act and assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            jwtVerifyServiceImpl.verifyToken(commonApiKey, request);
        });

        // assert
        assertEquals(exception.getLogMessage(), ConstError.ERRLOG_401_TOKEN_EMPTY);
        assertEquals(exception.getResponseMessage(), ConstError.ERR_401_TOKEN_EMPTY);
        verify(identityProviderService).buildIssuer(commonApiKey);
        verify(request).getHeader(Const.HEADER_AUTHORIZATION);
    }

    /**
     * Test case: verifyToken - Invalid Authorization header.
     */
    @Test
    @DisplayName("verifyToken - Invalid Authorization header")
    void verifyToken_invalidAuthorizationHeader() {
        // arrange
        // Mock identityProviderService to return issuer
        when(identityProviderService.buildIssuer(anyString())).thenReturn(commonIssuer);

        // Mock JwtDecoder and Jwt
        JwtDecoder mockDecoder = mock(JwtDecoder.class);
        when(jwtDecoderProvider.getDecoder(anyString())).thenReturn(mockDecoder);

        // Mock request to return null Authorization header
        when(request.getHeader(Const.HEADER_AUTHORIZATION)).thenReturn(commonAuthorizationHeader);
        // Mock JwtDecoder to return a valid Jwt
        when(mockDecoder.decode(anyString())).thenReturn(null);

        // act and assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            jwtVerifyServiceImpl.verifyToken(commonApiKey, request);
        });

        // assert
        assertEquals(exception.getLogMessage(), ConstError.ERRLOG_401_INVALID_TOKEN);
        assertEquals(exception.getResponseMessage(), ConstError.ERR_401_INVALID_OR_EXPIRED_TOKEN);
        verify(identityProviderService).buildIssuer(commonApiKey);
        verify(request).getHeader(Const.HEADER_AUTHORIZATION);
    }

    /**
     * Test case: verifyToken - Invalid Authorization header.
     */
    @ParameterizedTest
    @CsvSource({
        "JwtException",
        "JwtException_caseResourceAccessException",
        "JwtException_caseConnectException"
    })
    @DisplayName("verifyToken - Invalid Authorization header")
    void verifyToken_decodeThrowException(String argExceptionType) {

        // arrange
        // Mock identityProviderService to return issuer
        when(identityProviderService.buildIssuer(anyString())).thenReturn(commonIssuer);

        // Mock JwtDecoder and Jwt
        JwtDecoder mockDecoder = mock(JwtDecoder.class);
        when(jwtDecoderProvider.getDecoder(anyString())).thenReturn(mockDecoder);

        // Mock request to return Authorization header
        when(request.getHeader(Const.HEADER_AUTHORIZATION)).thenReturn(commonAuthorizationHeader);

        Exception exception = null;
        Class<? extends Throwable> clazz = null;
        switch (argExceptionType) {
            case "JwtException":
                exception = new JwtException(argExceptionType);
                clazz = UnauthorizedException.class;
                break;
            case "JwtException_caseResourceAccessException":
                exception = new JwtException(argExceptionType, new Exception(new ResourceAccessException(argExceptionType)));
                clazz = UnexpectedException.class;
                break;
            case "JwtException_caseConnectException":
                exception = new JwtException(argExceptionType, new Exception(new ConnectException(argExceptionType)));
                clazz = UnexpectedException.class;
                break;
            default:
                throw new IllegalArgumentException(String.format("Invalid exception type for test '%s'", argExceptionType));
        }
        // Mock JwtDecoder to return a valid Jwt
        when(mockDecoder.decode(anyString())).thenThrow(exception);

        // act and assert
        Throwable thrownException = assertThrows(clazz, () -> {
            jwtVerifyServiceImpl.verifyToken(commonApiKey, request);
        });

        // assert
        assertEquals(thrownException.getClass(), clazz);
        verify(identityProviderService).buildIssuer(commonApiKey);
        verify(request).getHeader(Const.HEADER_AUTHORIZATION);
    }

}