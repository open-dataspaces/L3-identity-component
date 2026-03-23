/*
 * PostOperatorRequest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for creating a new operator.
 *
 * Date: 2025/08/30
 */

package io.github.open_dataspaces.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.annotations.DateFormat;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object for creating a new operator.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostOperatorRequest {

    /**
     * The login user ID for the operator.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Size(min = Const.OPERATOR_LOGIN_USER_ID_LENGTH_MIN, max = Const.OPERATOR_LOGIN_USER_ID_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @Pattern(regexp = Const.REGEX_LOGIN_USER_ID, message = ConstError.ERR_VALIDATION_COMMON_PATTERN)
    @JsonProperty(Const.JSON_PROPERTY_LOGIN_USER_ID)
    private String loginUserId;

    /**
     * The operator name.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Size(min = Const.OPERATOR_NAME_LENGTH_MIN, max = Const.OPERATOR_NAME_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_OPERATOR_NAME)
    private String operatorName;

    /**
     * The operator address.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Size(min = Const.OPERATOR_ADDRESS_LENGTH_MIN, max = Const.OPERATOR_ADDRESS_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_OPERATOR_ADDRESS)
    private String operatorAddress;

    /**
     * The open operator ID.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Pattern(regexp = Const.REGEX_OPEN_OPERATOR_ID, message = ConstError.ERR_VALIDATION_COMMON_PATTERN)
    @Size(min = Const.OPERATOR_ADDRESS_LENGTH_MIN, max = Const.OPEN_OPERATOR_ID_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_OPEN_OPERATOR_ID)
    private String openOperatorId;

    /**
     * The global operator ID.
     */
    @Size(min = Const.GLOBAL_OPERATOR_ID_LENGTH_MIN, max = Const.GLOBAL_OPERATOR_ID_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_GLOBAL_OPERATOR_ID)
    private String globalOperatorId;

    /**
     * The effective start date for the operator.
     */
    @DateFormat(pattern = Const.DATE_FORMAT, message = ConstError.ERR_VALIDATION_COMMON_PATTERN_DATE)
    @JsonProperty(Const.JSON_PROPERTY_EFFECTIVE_START_DATE)
    private String effectiveStartDate;

    /**
     * The effective end date for the operator.
     */
    @DateFormat(pattern = Const.DATE_FORMAT, message = ConstError.ERR_VALIDATION_COMMON_PATTERN_DATE)
    @JsonProperty(Const.JSON_PROPERTY_EFFECTIVE_END_DATE)
    private String effectiveEndDate;

    /**
     * Flag indicating whether to create a password for the operator.
     */
    @JsonProperty(Const.JSON_PROPERTY_CREATE_PASSWORD_FLAG)
    @Pattern(regexp = Const.REGEX_BOOLEAN, message = ConstError.ERR_VALIDATION_COMMON_PATTERN_BOOLEAN)
    private String createPasswordFlag;

    /**
     * Flag indicating whether the password is temporary.
     */
    @JsonProperty(Const.JSON_PROPERTY_PASSWORD_TEMPORARY_FLAG)
    @Pattern(regexp = Const.REGEX_BOOLEAN, message = ConstError.ERR_VALIDATION_COMMON_PATTERN_BOOLEAN)
    private String passwordTemporaryFlag;

    /**
     * Get the createPasswordFlag as a Boolean.
     *
     * @return Returns the default value if null, otherwise returns the specified value.
     */
    public boolean getCreatePasswordFlag() {
        return (this.createPasswordFlag == null) ? true : Boolean.valueOf(this.createPasswordFlag);
    }

    /**
     * Get the passwordTemporaryFlag as a Boolean.
     *
     * @return Returns the default value if null, otherwise returns the specified value.
     */
    public boolean getPasswordTemporaryFlag() {
        return (this.passwordTemporaryFlag == null) ? false : Boolean.valueOf(this.passwordTemporaryFlag);
    }
}
