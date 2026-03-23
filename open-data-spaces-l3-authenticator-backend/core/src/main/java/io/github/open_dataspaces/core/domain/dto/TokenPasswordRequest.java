/*
 * TokenPasswordRequest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for user login requests.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.annotations.Masked;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object for user login requests.
 */
@Data
@AllArgsConstructor
public class TokenPasswordRequest {

    /**
     * IDP Authorization Flow Client ID.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Size(min = Const.CLIENT_ID_LENGTH_MIN, max = Const.CLIENT_ID_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_CLIENT_ID)
    private String clientId;

    /**
     * IDP Authorization Flow Client Secret.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Size(min = Const.CLIENT_SECRET_LENGTH_MIN, max = Const.CLIENT_SECRET_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_CLIENT_SECRET)
    @Masked
    private String clientSecret;

    /**
     * The loginUserId for login.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Size(min = Const.OPERATOR_ACCOUNT_ID_LENGTH_MIN, max = Const.OPERATOR_ACCOUNT_ID_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @Pattern(regexp = Const.REGEX_LOGIN_USER_ID,
            message = ConstError.ERR_VALIDATION_AUTH_LOGIN_USER_ID_OR_PASSWORD_INVALID)
    @JsonProperty(Const.JSON_PROPERTY_LOGIN_USER_ID)
    private String loginUserId;

    /**
     * The account password for login.
     *
     * <p>This field is validated for length, presence of uppercase, lowercase, digit, special
     * character, and other requirements.</p>
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Size(min = Const.PASSWORD_LENGTH_MIN, max = Const.PASSWORD_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @Pattern(regexp = Const.REGEX_PASSWORD,
            message = ConstError.ERR_VALIDATION_AUTH_LOGIN_USER_ID_OR_PASSWORD_INVALID)
    @JsonProperty(Const.JSON_PROPERTY_LOGIN_USER_PASSWORD)
    @Masked
    private String password;

}
