/*
 * ListPlantRequest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for plant information retrieval requests.
 *
 * Date: 2025/09/11
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
 * DTO for plant information retrieval requests.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListPlantRequest extends APIJSONRequest {

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

    @Pattern(regexp = Const.REGEX_UUID, message = ConstError.ERR_VALIDATION_COMMON_PATTERN_UUID)
    @JsonProperty(Const.JSON_PROPERTY_PLANT_ID)
    private String plantId;

    @Pattern(regexp = Const.REGEX_OPEN_PLANT_ID, message = ConstError.ERR_VALIDATION_COMMON_PATTERN)
    @Size(min = Const.OPEN_PLANT_ID_LENGTH_MIN, max = Const.OPEN_PLANT_ID_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_OPEN_PLANT_ID)
    private String openPlantId;

    @Size(min = Const.GLOBAL_PLANT_ID_LENGTH_MIN, max = Const.GLOBAL_PLANT_ID_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_GLOBAL_PLANT_ID)
    private String globalPlantId;

    @Size(min = Const.PLANT_NAME_LENGTH_MIN, max = Const.PLANT_NAME_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_PLANT_NAME)
    private String plantName;

    @Size(min = Const.PLANT_ADDRESS_LENGTH_MIN, max = Const.PLANT_ADDRESS_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_PLANT_ADDRESS)
    private String plantAddress;

    @DateFormat(pattern = Const.DATE_FORMAT, message = ConstError.ERR_VALIDATION_COMMON_PATTERN_DATE)
    @JsonProperty(Const.JSON_PROPERTY_EFFECTIVE_DATE)
    private String effectiveDate;

    @JsonProperty(Const.JSON_PROPERTY_DELETED_FLAG)
    private Boolean deletedFlag;

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
}