/*
 * PostPlantRequest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for creating a new plant.
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
public class PostPlantRequest {

    /**
     * The operator ID to which the plant belongs.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Pattern(regexp = Const.REGEX_UUID, message = ConstError.ERR_VALIDATION_COMMON_PATTERN_UUID)
    @JsonProperty(Const.JSON_PROPERTY_OPERATOR_ID)
    private String operatorId;

    /**
     * The plant name.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Size(min = Const.PLANT_NAME_LENGTH_MIN, max = Const.PLANT_NAME_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_PLANT_NAME)
    private String plantName;

    /**
     * The plant address.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Size(min = Const.PLANT_ADDRESS_LENGTH_MIN, max = Const.PLANT_ADDRESS_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_PLANT_ADDRESS)
    private String plantAddress;

    /**
     * The open plant ID.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Pattern(regexp = Const.REGEX_OPEN_PLANT_ID, message = ConstError.ERR_VALIDATION_COMMON_PATTERN)
    @Size(min = Const.OPEN_PLANT_ID_LENGTH_MIN, max = Const.OPEN_PLANT_ID_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_OPEN_PLANT_ID)
    private String openPlantId;

    /**
     * The global plant ID.
     */
    @Size(min = Const.GLOBAL_PLANT_ID_LENGTH_MIN, max = Const.GLOBAL_PLANT_ID_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_GLOBAL_PLANT_ID)
    private String globalPlantId;

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

}
