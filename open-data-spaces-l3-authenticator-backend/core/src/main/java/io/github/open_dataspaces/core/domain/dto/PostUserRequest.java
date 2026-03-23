/*
 * PostUserRequest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for creating a personal user.
 *
 * Date: 2025/11/30
 */

package io.github.open_dataspaces.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating a personal user.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostUserRequest extends APIJSONRequest {

    /**
     * The login user ID for the personal user.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Size(min = Const.OPERATOR_LOGIN_USER_ID_LENGTH_MIN, max = Const.OPERATOR_LOGIN_USER_ID_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @Pattern(regexp = Const.REGEX_LOGIN_USER_ID, message = ConstError.ERR_VALIDATION_COMMON_PATTERN)
    @JsonProperty(Const.JSON_PROPERTY_LOGIN_USER_ID)
    private String loginUserId;

    /**
     * Flag indicating whether to create a password for the operator.
     */
    @JsonProperty(Const.JSON_PROPERTY_CREATE_PASSWORD_FLAG)
    @Pattern(regexp = Const.REGEX_BOOLEAN, message = ConstError.ERR_VALIDATION_COMMON_PATTERN_BOOLEAN)
    private String createPasswordFlag;

    /**
     * Get the createPasswordFlag as a Boolean.
     *
     * @return Returns the default value if null, otherwise returns the specified value.
     */
    public boolean getCreatePasswordFlag() {
        return (this.createPasswordFlag == null) ? true : Boolean.valueOf(this.createPasswordFlag);
    }

    /**
     * Flag indicating whether the password is temporary.
     */
    @JsonProperty(Const.JSON_PROPERTY_PASSWORD_TEMPORARY_FLAG)
    @Pattern(regexp = Const.REGEX_BOOLEAN, message = ConstError.ERR_VALIDATION_COMMON_PATTERN_BOOLEAN)
    private String passwordTemporaryFlag;

    /**
     * Get the passwordTemporaryFlag as a Boolean.
     *
     * @return Returns the default value if null, otherwise returns the specified value.
     */
    public boolean getPasswordTemporaryFlag() {
        return (this.passwordTemporaryFlag == null) ? false : Boolean.valueOf(this.passwordTemporaryFlag);
    }
}