/*
 * ListOperatorRequest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for operator information retrieval requests.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.annotations.DateFormat;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for operator information retrieval requests.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListOperatorRequest extends APIJSONRequest {

    @NotNull(message = ConstError.ERR_VALIDATION_COMMON_REQUIRED)
    @Min(value = Const.COUNT_RANGE_MIN, message = ConstError.ERR_VALIDATION_COMMON_RANGE_MIN)
    private Integer count;

    @NotNull(message = ConstError.ERR_VALIDATION_COMMON_REQUIRED)
    @Min(value = Const.INDEX_RANGE_MIN, message = ConstError.ERR_VALIDATION_COMMON_RANGE_MIN)
    private Integer index;

    @Valid
    private SortKey sort = new SortKey();

    @Pattern(regexp = Const.REGEX_UUID, message = ConstError.ERR_VALIDATION_COMMON_PATTERN_UUID)
    @JsonProperty(Const.JSON_PROPERTY_OPERATOR_ID)
    private String operatorId;

    @Size(min = Const.OPERATOR_NAME_LENGTH_MIN, max = Const.OPERATOR_NAME_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_OPERATOR_NAME)
    private String operatorName;

    @Size(min = Const.OPERATOR_ADDRESS_LENGTH_MIN, max = Const.OPERATOR_ADDRESS_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_OPERATOR_ADDRESS)
    private String operatorAddress;

    @Pattern(regexp = Const.REGEX_OPEN_OPERATOR_ID, message = ConstError.ERR_VALIDATION_COMMON_PATTERN)
    @JsonProperty(Const.JSON_PROPERTY_OPEN_OPERATOR_ID)
    private String openOperatorId;

    @Size(min = Const.GLOBAL_OPERATOR_ID_LENGTH_MIN, max = Const.GLOBAL_OPERATOR_ID_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_GLOBAL_OPERATOR_ID)
    private String globalOperatorId;

    @DateFormat(pattern = Const.DATE_FORMAT, message = ConstError.ERR_VALIDATION_COMMON_PATTERN_DATE)
    @JsonProperty(Const.JSON_PROPERTY_EFFECTIVE_DATE)
    private String effectiveDate;   // If set as a date type, a JSON parse error will occur before validation, so it is received as a String

    @JsonProperty(Const.JSON_PROPERTY_DELETED_FLAG)
    @Pattern(regexp = Const.REGEX_BOOLEAN, message = ConstError.ERR_VALIDATION_COMMON_PATTERN_BOOLEAN)
    private String deletedFlag;

    /**
     * Sorting key information.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SortKey {
        private String key;

        @Pattern(regexp = Const.REGEX_SORT_ORDER, message = ConstError.ERR_VALIDATION_COMMON_PATTERN)
        private String order;
    }

    /**
     * Get the deletedFlag as a Boolean.
     *
     * @return Boolean representation of deletedFlag, or null if deletedFlag is null.
     */
    public Boolean getDeletedFlag() {
        return (this.deletedFlag == null) ? null : Boolean.valueOf(this.deletedFlag);
    }
}
