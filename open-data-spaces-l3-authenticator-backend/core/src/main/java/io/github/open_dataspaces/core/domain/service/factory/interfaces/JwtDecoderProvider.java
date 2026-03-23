/*
 * JwtDecoderProviderImpl.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class provides a concrete implementation of the JwtDecoderProvider interface.
 *
 * Date: 2026/02/28
 */

package io.github.open_dataspaces.core.domain.service.factory.interfaces;

import org.springframework.security.oauth2.jwt.JwtDecoder;

import io.micrometer.common.lang.NonNull;

/**
 * Interface for JwtDecoderProvider.
 */
public interface JwtDecoderProvider {

    /**
     * Retrieves a JwtDecoder for the given issuer from the cache.
     *
     * @param issuer the issuer
     * @return the JwtDecoder instance
     **/
    @NonNull
    JwtDecoder getDecoder(@NonNull String issuer);
}
