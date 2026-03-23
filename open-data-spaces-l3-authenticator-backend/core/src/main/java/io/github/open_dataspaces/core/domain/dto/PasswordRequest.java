/*
 * PasswordRequest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for password change requests.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.annotations.Masked;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object for password change requests.
 */
@Data
@AllArgsConstructor
public class PasswordRequest {

    /**
     * The old password to verify.
     *
     * <p>This field is validated for length, presence of uppercase, lowercase, digit, special
     * character, and other requirements.</p>
     */
    @NotNull(message = ConstError.ERR_VALIDATION_COMMON_REQUIRED)
    @Size(min = Const.OLD_PASSWORD_LENGTH_MIN, max = Const.PASSWORD_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @Pattern(regexp = Const.REGEX_OLD_PASSWORD,
            message = ConstError.ERR_VALIDATION_PASSWORD_INVALID)
    @JsonProperty(Const.JSON_PROPERTY_OLD_PASSWORD)
    @Masked
    private String oldPassword;

    /**
     * The new password to set.
     *
     * <p>This field is validated for length, presence of uppercase, lowercase, digit, special
     * character, and other requirements.</p>
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Size(min = Const.PASSWORD_LENGTH_MIN, max = Const.PASSWORD_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @Pattern(regexp = Const.REGEX_PASSWORD,
            message = ConstError.ERR_VALIDATION_PASSWORD_INVALID)
    @JsonProperty(Const.JSON_PROPERTY_NEW_PASSWORD)
    @Masked
    private String newPassword;

}
