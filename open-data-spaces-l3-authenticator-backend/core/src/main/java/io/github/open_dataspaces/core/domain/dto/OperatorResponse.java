/*
 * OperatorResponse.java
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
@NoArgsConstructor
@AllArgsConstructor
public class OperatorResponse {

    @JsonProperty(Const.JSON_PROPERTY_LOGIN_USER_ID)
    private String loginUserId;

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
    private LocalDate effectiveStartDate;

    @JsonProperty(Const.JSON_PROPERTY_EFFECTIVE_END_DATE)
    private LocalDate effectiveEndDate;

    @JsonProperty(Const.JSON_PROPERTY_DELETED_FLAG)
    private boolean deletedFlag;

    @JsonFormat(pattern = Const.ISO_8601_UTC_MILLISECOND_FORMAT, timezone = Const.TIMEZONE_UTC)
    @JsonProperty(Const.JSON_PROPERTY_CREATED_AT)
    private LocalDateTime createdAt;

    @JsonFormat(pattern = Const.ISO_8601_UTC_MILLISECOND_FORMAT, timezone = Const.TIMEZONE_UTC)
    @JsonProperty(Const.JSON_PROPERTY_UPDATED_AT)
    private LocalDateTime updatedAt;

    /**
     * Constructor that initializes the response from an OperatorEntity.
     *
     * @param result the OperatorEntity
     */
    public OperatorResponse(OperatorResult result) {
        this.operatorId = result.getOperatorId();
        this.loginUserId = null; // Not included in OperatorEntity;
        this.operatorName = result.getOperatorName();
        this.operatorAddress = result.getOperatorAddress();
        this.openOperatorId = result.getOpenOperatorId();
        this.globalOperatorId = result.getGlobalOperatorId();
        this.deletedFlag = result.isDeletedFlag();
        this.effectiveStartDate = result.getEffectiveStartDate();
        this.effectiveEndDate = result.getEffectiveEndDate();
        this.createdAt = result.getCreatedAt();
        this.updatedAt = result.getUpdatedAt();
    }
}
