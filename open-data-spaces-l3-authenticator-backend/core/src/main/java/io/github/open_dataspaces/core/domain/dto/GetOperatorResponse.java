/*
 * GetOperatorResponse.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for operator information responses.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.consts.Const;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for operator information responses.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetOperatorResponse {

    @JsonProperty(Const.JSON_PROPERTY_OPERATOR_ID)
    private String operatorId;

    @JsonProperty(Const.JSON_PROPERTY_OPERATOR_NAME)
    private String operatorName;

    @JsonProperty(Const.JSON_PROPERTY_OPERATOR_ADDRESS)
    private String operatorAddress;

    @JsonProperty(Const.JSON_PROPERTY_OPEN_OPERATOR_ID)
    private String openOperatorId;

    @JsonProperty(Const.JSON_PROPERTY_GLOBAL_OPERATOR_ID)
    private String globalOperatorId;

    @JsonProperty(Const.JSON_PROPERTY_EFFECTIVE_START_DATE)
    @JsonFormat(pattern = Const.DATE_FORMAT)
    private LocalDate effectiveStartDate;

    @JsonProperty(Const.JSON_PROPERTY_EFFECTIVE_END_DATE)
    @JsonFormat(pattern = Const.DATE_FORMAT)
    private LocalDate effectiveEndDate;

    @JsonProperty(Const.JSON_PROPERTY_DELETED_FLAG)
    private Boolean deletedFlag;

    @JsonProperty(Const.JSON_PROPERTY_CREATED_AT)
    @JsonFormat(pattern = Const.ISO_8601_UTC_MILLISECOND_FORMAT)
    private LocalDateTime createdAt;

    @JsonProperty(Const.JSON_PROPERTY_UPDATED_AT)
    @JsonFormat(pattern = Const.ISO_8601_UTC_MILLISECOND_FORMAT)
    private LocalDateTime updatedAt;

    /**
     * Constructs a new {@code GetOperatorResponse} with the specified operator result.
     *
     * @param operatorResult the operator result to copy properties from
     */
    public GetOperatorResponse(OperatorResult operatorResult) {
        this.operatorId = operatorResult.getOperatorId();
        this.operatorName = operatorResult.getOperatorName();
        this.operatorAddress = operatorResult.getOperatorAddress();
        this.openOperatorId = operatorResult.getOpenOperatorId();
        this.globalOperatorId = operatorResult.getGlobalOperatorId();
        this.effectiveStartDate = operatorResult.getEffectiveStartDate();
        this.effectiveEndDate = operatorResult.getEffectiveEndDate();
        this.deletedFlag = operatorResult.isDeletedFlag();
        this.createdAt = operatorResult.getCreatedAt();
        this.updatedAt = operatorResult.getUpdatedAt();
    }

}
