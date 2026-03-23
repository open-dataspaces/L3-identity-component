/*
 * PutPlantRequest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for plant information update requests.
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
 * Data Transfer Object for plant information update requests.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PutPlantRequest extends APIJSONRequest {

    @JsonProperty(Const.JSON_PROPERTY_UPDATED_AT)
    @DateFormat(pattern = Const.ISO_8601_UTC_MILLISECOND_FORMAT, message = ConstError.ERR_VALIDATION_COMMON_PATTERN_DATE)
    @NotNull(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    private String updatedAt;

    @JsonProperty(Const.JSON_PROPERTY_PLANT_NAME)
    @Size(min = Const.PLANT_NAME_LENGTH_MIN, max = Const.PLANT_NAME_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    private String plantName;

    @JsonProperty(Const.JSON_PROPERTY_PLANT_ADDRESS)
    @Size(min = Const.PLANT_ADDRESS_LENGTH_MIN, max = Const.PLANT_ADDRESS_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    private String plantAddress;

    @JsonProperty(Const.JSON_PROPERTY_OPEN_PLANT_ID)
    @Pattern(regexp = Const.REGEX_OPEN_PLANT_ID, message = ConstError.ERR_VALIDATION_COMMON_PATTERN)
    @Size(min = Const.OPEN_PLANT_ID_LENGTH_MIN, max = Const.OPEN_PLANT_ID_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    private String openPlantId;

    @JsonProperty(Const.JSON_PROPERTY_GLOBAL_PLANT_ID)
    @Size(min = Const.GLOBAL_PLANT_ID_LENGTH_MIN, max = Const.GLOBAL_PLANT_ID_LENGTH_MAX,
            message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    private String globalPlantId;

}