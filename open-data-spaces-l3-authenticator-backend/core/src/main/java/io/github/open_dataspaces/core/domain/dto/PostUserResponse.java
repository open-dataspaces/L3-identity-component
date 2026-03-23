/*
 * UserResponse.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for user information responses.
 *
 * Date: 2025/11/30
 */

package io.github.open_dataspaces.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class PostUserResponse {

    @JsonProperty(Const.JSON_PROPERTY_LOGIN_USER_ID)
    private String loginUserId;

    @JsonProperty(Const.JSON_PROPERTY_OPERATOR_ID)
    private String operatorId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(Const.JSON_PROPERTY_LOGIN_USER_PASSWORD)
    private String password;

    /**
     * Constructor that initializes the response from an OperatorEntity.
     *
     * @param result the OperatorEntity
     */
    public PostUserResponse(IdpAccountInfo result) {
        this.loginUserId = result.getLoginUserId();
        this.operatorId = result.getUserId();
        this.password = result.getPassword();
    }
}
