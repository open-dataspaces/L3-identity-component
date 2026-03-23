/*
 * PlantResponse.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for plant information responses.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.consts.Const;

import io.micrometer.common.lang.NonNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for plant information responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlantResponse {

    @NonNull
    @JsonProperty(Const.JSON_PROPERTY_PLANT_ID)
    private String plantId;

    @NonNull
    @JsonProperty(Const.JSON_PROPERTY_OPERATOR_ID)
    private String operatorId;

    @NonNull
    @JsonProperty(Const.JSON_PROPERTY_PLANT_NAME)
    private String plantName;

    @NonNull
    @JsonProperty(Const.JSON_PROPERTY_PLANT_ADDRESS)
    private String plantAddress;

    @NonNull
    @JsonProperty(Const.JSON_PROPERTY_OPEN_PLANT_ID)
    private String openPlantId;

    @JsonProperty(Const.JSON_PROPERTY_GLOBAL_PLANT_ID)
    private String globalPlantId;

    @NonNull
    @JsonProperty(Const.JSON_PROPERTY_EFFECTIVE_START_DATE)
    private LocalDate effectiveStartDate;

    @NonNull
    @JsonProperty(Const.JSON_PROPERTY_EFFECTIVE_END_DATE)
    private LocalDate effectiveEndDate;

    @JsonProperty(Const.JSON_PROPERTY_DELETED_FLAG)
    private boolean deletedFlag;

    @NonNull
    @JsonFormat(pattern = Const.ISO_8601_UTC_MILLISECOND_FORMAT, timezone = Const.TIMEZONE_UTC)
    @JsonProperty(Const.JSON_PROPERTY_CREATED_AT)
    private LocalDateTime createdAt;

    @NonNull
    @JsonFormat(pattern = Const.ISO_8601_UTC_MILLISECOND_FORMAT, timezone = Const.TIMEZONE_UTC)
    @JsonProperty(Const.JSON_PROPERTY_UPDATED_AT)
    private LocalDateTime updatedAt;

    /**
     * Constructs a new {@code PlantResponse} from a {@code PlantResult}.
     *
     * @param result the PlantResult containing plant information
     */
    public PlantResponse(PlantResult result) {
        this.plantId = result.getPlantId();
        this.operatorId = result.getOperatorId();
        this.plantName = result.getPlantName();
        this.plantAddress = result.getPlantAddress();
        this.openPlantId = result.getOpenPlantId();
        this.globalPlantId = result.getGlobalPlantId();
        this.deletedFlag = result.isDeletedFlag();
        this.effectiveStartDate = result.getEffectiveStartDate();
        this.effectiveEndDate = result.getEffectiveEndDate();
        this.createdAt = result.getCreatedAt();
        this.updatedAt = result.getUpdatedAt();
    }

}