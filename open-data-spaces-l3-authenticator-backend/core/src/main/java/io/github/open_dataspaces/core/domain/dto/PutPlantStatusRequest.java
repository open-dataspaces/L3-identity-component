/*
 * PutPlantStatusRequest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for plant information update requests.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.annotations.DateFormat;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for operator information update requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PutPlantStatusRequest {
    @JsonProperty(Const.JSON_PROPERTY_UPDATED_AT)
    @DateFormat(pattern = Const.ISO_8601_UTC_MILLISECOND_FORMAT, message = ConstError.ERR_VALIDATION_COMMON_PATTERN_DATE)
    @NotNull(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    private String updatedAt;

    @JsonProperty(Const.JSON_PROPERTY_EFFECTIVE_START_DATE)
    @DateFormat(pattern = Const.DATE_FORMAT, message = ConstError.ERR_VALIDATION_COMMON_PATTERN_DATE)
    private String effectiveStartDate;

    @JsonProperty(Const.JSON_PROPERTY_EFFECTIVE_END_DATE)
    @DateFormat(pattern = Const.DATE_FORMAT, message = ConstError.ERR_VALIDATION_COMMON_PATTERN_DATE)
    private String effectiveEndDate;

    @JsonProperty(Const.JSON_PROPERTY_DELETED_FLAG)
    @Pattern(regexp = Const.REGEX_BOOLEAN, message = ConstError.ERR_VALIDATION_COMMON_PATTERN_BOOLEAN)
    private String deletedFlag;

    /**
     * Get the deletedFlag as a Boolean.
     *
     * @return Boolean representation of deletedFlag, or null if deletedFlag is null.
     */
    public Boolean getDeletedFlag() {
        return (this.deletedFlag == null) ? null : Boolean.valueOf(this.deletedFlag);
    }
}
