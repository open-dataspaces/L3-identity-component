/*
 * JwtDecoderProviderImplTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for JwtDecoderProviderImpl
 *
 * Date: 2026/02/28
 */

package io.github.open_dataspaces.core.domain.service.factory.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import io.github.open_dataspaces.core.common.config.ODSProperties;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;

/**
 * Unit tests for JwtDecoderProviderImpl.
 */
@ExtendWith(MockitoExtension.class)
public class JwtDecoderProviderImplTest {

    private JwtDecoderProviderImpl jwtDecoderProviderImpl;

    @Mock
    private ODSProperties odsProperties;

    @Mock
    private IdentityProviderService identityProviderService;

    private String commonJwksUri = "https://example.com/.well-known/jwks.json";
    private String commonIssuer = "https://example.com/";
    private long commonCacheSize = 100L;
    private long commonCacheDurationSecond = 3600L;
    private String commonExpectedAudience = "example-audience";
    private String commonExpectedType = "example-client-id";

    private String commonToken = "eyJhbGciOi";
    private String commonSubject = UUID.randomUUID().toString();

    /**
     * Setup method to initialize common mocks and the class under test.
     */
    @BeforeEach
    void setUp() {
        when(odsProperties.getJwt()).thenReturn(mock(ODSProperties.Jwt.class));

        when(odsProperties.getJwt().getJwksCacheSize()).thenReturn(commonCacheSize);
        when(odsProperties.getJwt().getJwksCacheDurationSecond()).thenReturn(commonCacheDurationSecond);

        jwtDecoderProviderImpl = new JwtDecoderProviderImpl(odsProperties, identityProviderService);
    }

    /**
     * Test for buildValidator method.
     */
    @ParameterizedTest
    @CsvSource({
        "VALUE, VALUE",
        "EMPTY, VALUE",
        "VALUE, EMPTY",
        "EMPTY, EMPTY"
    })
    @DisplayName("buildValidator - success")
    void buildValidator_success(String audienceOption, String typeOption) {
        var validator = jwtDecoderProviderImpl.buildValidator(
                commonIssuer,
                "EMPTY".equals(audienceOption) ? "" : commonExpectedAudience,
                "EMPTY".equals(typeOption) ? "" : commonExpectedType);

        // Validate that the validator is not null
        assertNotNull(validator);

        // Create a mock Jwt to test the validator
        Instant now = Instant.now();
        Map<String, Object> headers = Map.of("alg", "RS256");
        Map<String, Object> claims = Map.of(
                "sub", commonSubject,
                "iss", commonIssuer,
                "aud", "EMPTY".equals(audienceOption) ? "" : commonExpectedAudience,
                "typ", "EMPTY".equals(typeOption) ? "" : commonExpectedType
        );
        Jwt jwt = new Jwt(
                commonToken,    // token value
                now.plusSeconds(Const.JWT_CLAIM_IAT_FUTURE_ALLOWANCE_SECONDS - 1),    // issued at
                now.plusSeconds(300),    // expires at
                headers, // headers
                claims   // claims
        );
        OAuth2TokenValidatorResult resultValid = validator.validate(jwt);
        assertFalse(resultValid.hasErrors());
    }

    /**
     * Test for buildValidator method with invalid claims.
     */
    @ParameterizedTest
    @CsvSource({
        "iss, invalid-issuer",
        "ext, ",
        "iat, ",
        "aud, invalid-audience",
        "typ, invalid-type",
        "sub, invalid-subject"
    })
    @DisplayName("buildValidator - invalid claims")
    void buildValidator_invalid(String claim, String invalidValue) {
        var validator = jwtDecoderProviderImpl.buildValidator(
                commonIssuer,
                commonExpectedAudience,
                commonExpectedType);

        // Create a mock Jwt with invalid audience
        Instant issuedAt = Instant.now();
        Instant expiresAt = Instant.now().plusSeconds(300);
        Map<String, Object> headers = Map.of("alg", "RS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", commonSubject);
        claims.put("iss", commonIssuer);
        claims.put("aud", commonExpectedAudience);
        claims.put("typ", commonExpectedType);

        // Modify the claim to be invalid
        switch (claim) {
            case "ext":
                issuedAt = issuedAt.minusSeconds(3600);
                expiresAt = expiresAt.minusSeconds(3600);   // already expired
                break;
            case "iat":
                issuedAt = issuedAt.plusSeconds(Const.JWT_CLAIM_IAT_FUTURE_ALLOWANCE_SECONDS + 60); // issued in the future
                expiresAt = expiresAt.plusSeconds(Const.JWT_CLAIM_IAT_FUTURE_ALLOWANCE_SECONDS + 60);
                break;
            default:
                claims.put(claim, invalidValue);
                break;
        }

        Jwt jwt = new Jwt(
                commonToken,    // token value
                issuedAt,    // issued at
                expiresAt,    // expires at
                headers, // headers
                claims   // claims
        );
        OAuth2TokenValidatorResult resultInvalid = validator.validate(jwt);
        assertTrue(resultInvalid.hasErrors());
    }

    /**
     * Test for getDecoder method.
     */
    @Test
    @DisplayName("getDecoder - success")
    void getDecoder_success() {

        when(identityProviderService.buildJwksUri(commonIssuer)).thenReturn(commonJwksUri);
        when(odsProperties.getJwt().getClaimExpectedAudience()).thenReturn(commonExpectedAudience);
        when(odsProperties.getJwt().getClaimExpectedType()).thenReturn(commonExpectedType);

        JwtDecoder jwtDecoder = jwtDecoderProviderImpl.getDecoder(commonIssuer);

        assertNotNull(jwtDecoder);
    }

    /**
     * Test for getDecoder method.
     */
    @Test
    @DisplayName("getDecoder - failure")
    void getDecoder_failure() {

        when(identityProviderService.buildJwksUri(commonIssuer)).thenThrow(JwtException.class);

        assertThrows(JwtException.class, () -> jwtDecoderProviderImpl.getDecoder(commonIssuer));
    }
}
