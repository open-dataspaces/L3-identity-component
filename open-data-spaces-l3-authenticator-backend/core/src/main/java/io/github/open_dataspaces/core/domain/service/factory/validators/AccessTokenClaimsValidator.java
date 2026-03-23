/*
 * ClaimsValidator.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class provides a concrete implementation of the JwtDecoderProvider interface.
 *
 * Date: 2026/02/28
 */

package io.github.open_dataspaces.core.domain.service.factory.validators;

import java.lang.reflect.Array;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.enums.EnumClaimFormats;
import io.github.open_dataspaces.core.common.utils.UUIDUtils;

import io.micrometer.common.lang.NonNull;

/**
 * Concrete implementation of JwtDecoderProvider.
 */
public class AccessTokenClaimsValidator implements OAuth2TokenValidator<Jwt> {

    // Expected claim values
    private final String expectedAudience;
    private final String expectedType;

    /**
     * Constructor for AccessTokenClaimsValidator.
     *
     * @param expectedAudience the expected audience claim value
     * @param expectedType the expected type claim value
     */
    public AccessTokenClaimsValidator(@NonNull String expectedAudience, @NonNull String expectedType) {
        this.expectedAudience = expectedAudience;
        this.expectedType = expectedType;
    }

    /**
     * Validates the claims of the given JWT token.
     *
     * @param token the JWT token to validate
     * @return the result of the validation
     */
    @Override
    @NonNull
    public OAuth2TokenValidatorResult validate(@NonNull Jwt token) {
        OAuth2TokenValidatorResult result;

        // Validate 'iat' claim
        result = validateClaimIssuedAt(token);
        if (result.hasErrors()) {
            return result;
        }
        // Validate 'typ' claim
        result = validateClaimExpected(token, Const.JWT_CLAIM_TYP, expectedType);
        if (result.hasErrors()) {
            return result;
        }
        // Validate 'aud' claim
        result = validateClaimExpected(token, Const.JWT_CLAIM_AUD, expectedAudience);
        if (result.hasErrors()) {
            return result;
        }
        // Validate 'sub' claim format
        result = validateClaimFormat(token, Const.JWT_CLAIM_SUB, EnumClaimFormats.ClaimFormat.UUID);
        if (result.hasErrors()) {
            return result;
        }

        // Implement your claims validation logic here
        return OAuth2TokenValidatorResult.success();
    }

    /**
     * Validates that the claim in the token matches the expected format.
     *
     * @param token the JWT token to validate
     * @param claimKey the key of the claim to validate
     * @param format the expected format of the claim
     * @return the result of the validation
     */
    private @NonNull OAuth2TokenValidatorResult validateClaimFormat(
            @NonNull Jwt token,
            @NonNull String claimKey,
            @NonNull EnumClaimFormats.ClaimFormat format) {
        // Validate claim equals expected value
        Object claimObj = token.getClaims().get(claimKey);
        if (claimObj == null) {
            // Missing claim
            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    ConstError.ERR_401_INVALID_TOKEN,
                    String.format(ConstError.ERRLOG_401_INVALID_CLAIM_MISSING, claimKey),
                    null)
            );
        }

        // Extract claim values
        List<String> claimValues = extractClaimValues(claimObj);
        // Convert to UUIDs and validate format
        switch (format) {
            case UUID:
                // Validate UUID format
                List<UUID> uuids = UUIDUtils.getUUIDs(claimValues);
                if (uuids.isEmpty()) {
                    // No valid UUID found
                    return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                            ConstError.ERR_401_INVALID_TOKEN,
                            String.format(ConstError.ERRLOG_401_INVALID_CLAIM_FORMAT, claimKey, format),
                            null)
                    );
                }
                break;
            default:
                // Unsupported format
                throw new UnsupportedOperationException(
                        String.format(ConstError.ERRLOG_UNSUPPORTED_FORMAT, format));
        }

        // Implement your format validation logic here
        return OAuth2TokenValidatorResult.success();
    }

    /**
     * Validates that the claim in the token matches the expected value.
     *
     * @param token the JWT token to validate
     * @param claimKey the key of the claim to validate
     * @param expected the expected value of the claim
     * @return the result of the validation
     */
    private @NonNull OAuth2TokenValidatorResult validateClaimExpected(
            @NonNull Jwt token,
            @NonNull String claimKey,
            @NonNull String expected) {
        if (expected.isEmpty()) {
            // No expected value to validate against
            return OAuth2TokenValidatorResult.success();
        }
        // Validate claim equals expected value
        Object claimObj = token.getClaims().get(claimKey);
        if (claimObj == null) {
            // Missing claim
            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    ConstError.ERR_401_INVALID_TOKEN,
                    String.format(ConstError.ERRLOG_401_INVALID_CLAIM_MISSING, claimKey),
                    null)
            );
        }

        // Extract claim values
        List<String> claimValues = extractClaimValues(claimObj);
        boolean matched = claimValues.stream()
                .filter(v -> v != null) // filter out null values
                .map(String::trim)  // trim whitespace
                .anyMatch(v -> v.equalsIgnoreCase(expected));   // case-insensitive match
        return matched
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    ConstError.ERR_401_INVALID_TOKEN,
                    String.format(ConstError.ERRLOG_401_INVALID_CLAIM_MISMATCH, claimKey, expected, claimObj),
                    null)
        );
    }

    /**
     * Extracts string values from the claim object.
     *
     * @param claimObj the claim object from which to extract string values
     * @return a list of string values extracted from the claim object
     */
    private @NonNull List<String> extractClaimValues(@NonNull Object claimObj) {
        // Collection
        if (claimObj instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<Object> col = (Collection<Object>) claimObj;
            return col.stream().map(String::valueOf).collect(Collectors.toList());
        }

        // Array
        if (claimObj.getClass().isArray()) {
            int length = Array.getLength(claimObj);
            List<String> res = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                res.add(String.valueOf(Array.get(claimObj, i)));
            }
            return res;
        }

        // Single value
        return Collections.singletonList(String.valueOf(claimObj));
    }

    /**
     * Validates that the 'iat' claim in the token is valid.
     *
     * @param token the JWT token to validate
     * @return the result of the validation
     */
    private @NonNull OAuth2TokenValidatorResult validateClaimIssuedAt(
            @NonNull Jwt token) {
        Instant iat = token.getIssuedAt();
        if (iat == null) {
            // Missing claim
            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    ConstError.ERR_401_INVALID_TOKEN,
                    String.format(ConstError.ERRLOG_401_INVALID_CLAIM_MISSING, Const.JWT_CLAIM_IAT),
                    null)
            );
        }
        // Validate 'iat' is not in the future beyond allowance
        return iat.isAfter(Instant.now().plusSeconds(Const.JWT_CLAIM_IAT_FUTURE_ALLOWANCE_SECONDS))
                ? OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    ConstError.ERR_401_INVALID_TOKEN,
                    String.format(ConstError.ERRLOG_401_INVALID_CLAIM_INVALID, Const.JWT_CLAIM_IAT, iat.toString()),
                    null))
                : OAuth2TokenValidatorResult.success();
    }

}
