/*
 * JwtDecoderProviderImpl.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class provides a concrete implementation of the JwtDecoderProvider interface.
 *
 * Date: 2026/02/28
 */

package io.github.open_dataspaces.core.domain.service.factory.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import io.github.open_dataspaces.core.common.config.ODSProperties;
import io.github.open_dataspaces.core.domain.service.factory.interfaces.JwtDecoderProvider;
import io.github.open_dataspaces.core.domain.service.factory.validators.AccessTokenClaimsValidator;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;

import io.micrometer.common.lang.NonNull;

/**
 * Concrete implementation of JwtDecoderProvider.
 *
 * <p>This class creates JwtDecoder instances based on the JWKS URI and validates them with the issuer.</p>
 */
@Component
public class JwtDecoderProviderImpl implements JwtDecoderProvider {

    // ODS application properties
    private final ODSProperties odsProperties;

    // Service to interact with identity providers
    private final IdentityProviderService identityProviderService;
    // JwtDecoder cache to improve performance
    private final LoadingCache<String, JwtDecoder> decoders;

    /**
     * Constructor for JwtDecoderProviderImpl.
     */
    public JwtDecoderProviderImpl(ODSProperties odsProperties, IdentityProviderService identityProviderService) {
        this.odsProperties = odsProperties;
        this.identityProviderService = identityProviderService;
        // Initialize the JwtDecoder cache
        this.decoders = Caffeine.newBuilder()
                .maximumSize(odsProperties.getJwt().getJwksCacheSize())   // Limit cache size
                .expireAfterWrite(Duration.ofSeconds(odsProperties.getJwt().getJwksCacheDurationSecond()))  // Expire entries after configured duration
                .build(this::getOrCreateDecoder);
    }

    /**
     * Creates a JwtDecoder for the given issuer.
     *
     * @param issuer the issuer
     * @return the JwtDecoder instance
     */
    @NonNull
    private JwtDecoder getOrCreateDecoder(@NonNull String issuer) {
        // Create NimbusJwtDecoder with the provided JWKS URI
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(identityProviderService.buildJwksUri(issuer)).build();

        // Set up the JWT validators
        jwtDecoder.setJwtValidator(buildValidator(
                issuer,
                odsProperties.getJwt().getClaimExpectedAudience(),
                odsProperties.getJwt().getClaimExpectedType()));
        return jwtDecoder;
    }

    /**
     * Builds a composite OAuth2TokenValidator for the JWT.
     *
     * @param issuer the expected issuer
     * @param expectedAudience the expected audience claim value
     * @param expectedType the expected type claim value
     * @return the composite OAuth2TokenValidator
     */
    @NonNull
    OAuth2TokenValidator<Jwt> buildValidator(String issuer, String expectedAudience, String expectedType) {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();

        // Set up validators: default with issuer + custom access token claims validator
        validators.add(JwtValidators.createDefaultWithIssuer(issuer)); // iss + exp
        validators.add(new AccessTokenClaimsValidator(expectedAudience, expectedType)); // iat/sub/aud/typ checks
        return new DelegatingOAuth2TokenValidator<>(validators);
    }

    /**
     * Retrieves a JwtDecoder for the given issuer from the cache.
     *
     * @param issuer the issuer
     * @return the JwtDecoder instance
     */
    public @NonNull JwtDecoder getDecoder(@NonNull String issuer) {
        return decoders.get(issuer);
    }
}