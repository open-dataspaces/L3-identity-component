/*
 * PutOperatorRequest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for operator information update requests.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.annotations.DateFormat;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for operator information update requests.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PutOperatorRequest extends APIJSONRequest {
    @JsonProperty(Const.JSON_PROPERTY_UPDATED_AT)
    @DateFormat(pattern = Const.ISO_8601_UTC_MILLISECOND_FORMAT, message = ConstError.ERR_VALIDATION_COMMON_PATTERN_DATE)
    @NotNull(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    private String updatedAt;

    @JsonProperty(Const.JSON_PROPERTY_LOGIN_USER_ID)
    @Pattern(regexp = Const.REGEX_LOGIN_USER_ID, message = ConstError.ERR_VALIDATION_COMMON_PATTERN)
    @Size(min = Const.LOGIN_USER_ID_LENGTH_MIN, max = Const.LOGIN_USER_ID_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    private String loginUserId;

    @JsonProperty(Const.JSON_PROPERTY_OPERATOR_NAME)
    @Size(min = Const.OPERATOR_NAME_LENGTH_MIN, max = Const.OPERATOR_NAME_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    private String operatorName;

    @JsonProperty(Const.JSON_PROPERTY_OPERATOR_ADDRESS)
    @Size(min = Const.OPERATOR_ADDRESS_LENGTH_MIN, max = Const.OPERATOR_ADDRESS_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    private String operatorAddress;

    @JsonProperty(Const.JSON_PROPERTY_OPEN_OPERATOR_ID)
    @Pattern(regexp = Const.REGEX_OPEN_OPERATOR_ID, message = ConstError.ERR_VALIDATION_COMMON_PATTERN)
    @Size(min = Const.OPEN_OPERATOR_ID_LENGTH_MIN, max = Const.OPEN_OPERATOR_ID_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    private String openOperatorId;

    @JsonProperty(Const.JSON_PROPERTY_GLOBAL_OPERATOR_ID)
    @Size(min = Const.GLOBAL_OPERATOR_ID_LENGTH_MIN, max = Const.GLOBAL_OPERATOR_ID_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    private String globalOperatorId;
}
