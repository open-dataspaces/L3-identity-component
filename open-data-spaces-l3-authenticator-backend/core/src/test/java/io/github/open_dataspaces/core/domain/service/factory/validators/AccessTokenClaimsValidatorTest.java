/*
 * JwtDecoderProviderImplTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for JwtDecoderProviderImpl
 *
 * Date: 2026/02/28
 */

package io.github.open_dataspaces.core.domain.service.factory.validators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.enums.EnumClaimFormats;

/**
 * Unit tests for AccessTokenClaimsValidator.
 */
public class AccessTokenClaimsValidatorTest {

    private AccessTokenClaimsValidator accessTokenClaimsValidator;

    private String commonToken = "header.payload.signature";
    private String commonSubject = UUID.randomUUID().toString();
    private String commonExpectedAudience = "example-audience";
    private String commonExpectedType = "example-bearer";

    private Map<String, Object> commonHeader = Map.of("alg", "RS256");
    private Map<String, Object> commonClaims = Map.of(
            Const.JWT_CLAIM_SUB, commonSubject,
            Const.JWT_CLAIM_AUD, commonExpectedAudience,
            Const.JWT_CLAIM_TYP, commonExpectedType
    );

    /**
     * Test case: validate - success case.
     */
    @ParameterizedTest
    @CsvSource({
        "SINGLE",   // Single claim value
        "ARRAY",    // Claim value as array
        "LIST"      // Claim value as list
    })
    @DisplayName("validate - success case")
    void validate_success(String argClaimValueFormat) {

        accessTokenClaimsValidator = new AccessTokenClaimsValidator(commonExpectedAudience, commonExpectedType);

        Map<String, Object> modifiedClaims = new HashMap<>(commonClaims);
        if ("ARRAY".equals(argClaimValueFormat)) {
            modifiedClaims.put(Const.JWT_CLAIM_AUD, new String[] {commonExpectedAudience, "another-audience", null});
            modifiedClaims.put(Const.JWT_CLAIM_TYP, new String[] {commonExpectedType});
            modifiedClaims.put(Const.JWT_CLAIM_SUB, new String[] {commonSubject});
        } else if ("LIST".equals(argClaimValueFormat)) {
            modifiedClaims.put(Const.JWT_CLAIM_AUD, List.of(commonExpectedAudience, "another-audience"));
            modifiedClaims.put(Const.JWT_CLAIM_TYP, List.of(commonExpectedType));
            modifiedClaims.put(Const.JWT_CLAIM_SUB, List.of(commonSubject));
        }
        Instant now = Instant.now();

        Jwt jwt = new Jwt(
                commonToken,    // token value
                now.plusSeconds(Const.JWT_CLAIM_IAT_FUTURE_ALLOWANCE_SECONDS - 1),    // issued at
                now.plusSeconds(300),    // expires at
                commonHeader,   // headers
                modifiedClaims  // claims
        );

        OAuth2TokenValidatorResult result = accessTokenClaimsValidator.validate(jwt);

        assertFalse(result.hasErrors());
    }

    /**
     * Test case: validate - success case with optional claim missing.
     */
    @ParameterizedTest
    @CsvSource({
        Const.JWT_CLAIM_AUD + ",NULL",      // Null 'aud' claim
        Const.JWT_CLAIM_AUD + ",EMPTY",     // Empty 'aud' claim
        Const.JWT_CLAIM_AUD + ",INVALID",   // Invalid 'aud' claim format
        Const.JWT_CLAIM_TYP + ",NULL",      // Null 'typ' claim
        Const.JWT_CLAIM_TYP + ",EMPTY",     // Empty 'typ' claim
        Const.JWT_CLAIM_TYP + ",INVALID"    // Invalid 'typ' claim format
    })
    @DisplayName("validate - success case with optional claim missing")
    void validate_success_optionalMissing(String argClaimKey, String argClaimValue) {

        // optional claim
        accessTokenClaimsValidator = new AccessTokenClaimsValidator(
                (Const.JWT_CLAIM_AUD.equals(argClaimKey) ? "" : commonExpectedAudience),
                (Const.JWT_CLAIM_TYP.equals(argClaimKey) ? "" : commonExpectedType));

        Map<String, Object> modifiedClaims = new HashMap<>(commonClaims);
        modifiedClaims.remove(argClaimKey); // Remove the specified optional claim
        Instant now = Instant.now();

        switch (argClaimValue) {
            case "NULL":
                modifiedClaims.remove(argClaimKey);
                break;
            case "EMPTY":
            case "INVALID":
                // Set invalid claim value
                String claimValue = "EMPTY".equals(argClaimValue) ? "" : argClaimValue;
                modifiedClaims.put(argClaimKey, claimValue);
                break;
            default:
                // No action needed
                break;
        }

        Jwt jwt = new Jwt(
                commonToken,    // token value
                now.plusSeconds(Const.JWT_CLAIM_IAT_FUTURE_ALLOWANCE_SECONDS - 1),    // issued at
                now.plusSeconds(300),    // expires at
                commonHeader,   // headers
                modifiedClaims  // claims
        );

        // Act
        OAuth2TokenValidatorResult result = accessTokenClaimsValidator.validate(jwt);

        // Assert
        assertFalse(result.hasErrors());
    }

    /**
     * Test case: validate - failure cases for various invalid claims.
     */
    @ParameterizedTest
    @CsvSource({
        Const.JWT_CLAIM_IAT + ",NULL",      // Null 'iat' claim
        Const.JWT_CLAIM_IAT + ",FUTURE",    // 'iat' claim set in the future
        Const.JWT_CLAIM_SUB + ",NULL",      // Null 'sub' claim
        Const.JWT_CLAIM_SUB + ",EMPTY",     // Empty 'sub' claim
        Const.JWT_CLAIM_SUB + ",INVALID",   // Invalid 'sub' claim format
        Const.JWT_CLAIM_AUD + ",NULL",      // Null 'aud' claim
        Const.JWT_CLAIM_AUD + ",EMPTY",     // Empty 'aud' claim
        Const.JWT_CLAIM_AUD + ",INVALID",   // Invalid 'aud' claim format
        Const.JWT_CLAIM_TYP + ",NULL",      // Null 'typ' claim
        Const.JWT_CLAIM_TYP + ",EMPTY",     // Empty 'typ' claim
        Const.JWT_CLAIM_TYP + ",INVALID"    // Invalid 'typ' claim format
    })
    @DisplayName("validate - invalid claims")
    void validate_invalidClaims(String claimKey, String argInvalidValue) {

        accessTokenClaimsValidator = new AccessTokenClaimsValidator(commonExpectedAudience, commonExpectedType);

        Instant now = Instant.now();
        Map<String, Object> modifiedClaims = new HashMap<>(commonClaims);
        Instant issuedAt = now.plusSeconds(Const.JWT_CLAIM_IAT_FUTURE_ALLOWANCE_SECONDS - 1);
        Instant expiresAt = now.plusSeconds(300);

        String expectedErrorMessage = null;
        if (claimKey.equals(Const.JWT_CLAIM_IAT)) {
            // Validate 'iat' claim
            switch (argInvalidValue) {
                case "NULL":
                    issuedAt = null;
                    expectedErrorMessage = String.format(
                            ConstError.ERRLOG_401_INVALID_CLAIM_MISSING,
                            Const.JWT_CLAIM_IAT);
                    break;
                case "FUTURE":
                    issuedAt = now.plusSeconds(Const.JWT_CLAIM_IAT_FUTURE_ALLOWANCE_SECONDS + 10);
                    expiresAt = issuedAt.plusSeconds(300);
                    expectedErrorMessage = String.format(
                            ConstError.ERRLOG_401_INVALID_CLAIM_INVALID,
                            Const.JWT_CLAIM_IAT,
                            issuedAt.toString());
                    break;
                default:
                    // No action needed
                    break;
            }
        } else {
            // Validate other claims
            switch (argInvalidValue) {
                case "NULL":
                    modifiedClaims.remove(claimKey);
                    expectedErrorMessage = String.format(
                            ConstError.ERRLOG_401_INVALID_CLAIM_MISSING,
                            claimKey);
                    break;
                case "EMPTY":
                case "INVALID":
                    // Set invalid claim value
                    String claimValue = "EMPTY".equals(argInvalidValue) ? "" : argInvalidValue;
                    modifiedClaims.put(claimKey, claimValue);
                    if (claimKey.equals(Const.JWT_CLAIM_SUB)) {
                        expectedErrorMessage = String.format(
                                ConstError.ERRLOG_401_INVALID_CLAIM_FORMAT,
                                claimKey,
                                EnumClaimFormats.ClaimFormat.UUID);
                    } else {
                        expectedErrorMessage = String.format(
                                ConstError.ERRLOG_401_INVALID_CLAIM_MISMATCH,
                                claimKey,
                                (claimKey.equals(Const.JWT_CLAIM_AUD) ? commonExpectedAudience : commonExpectedType),
                                claimValue);
                    }
                    break;
                default:
                    // No action needed
                    break;
            }
        }
        Jwt jwt = new Jwt(
                commonToken,    // token value
                issuedAt,       // issued at
                expiresAt,      // expires at
                commonHeader,   // headers
                modifiedClaims  // claims
        );

        OAuth2TokenValidatorResult result = accessTokenClaimsValidator.validate(jwt);

        assertTrue(result.hasErrors());
        for (OAuth2Error error : result.getErrors()) {
            assertEquals(ConstError.ERR_401_INVALID_TOKEN, error.getErrorCode());
            assertEquals(expectedErrorMessage, error.getDescription());
        }
    }
}
