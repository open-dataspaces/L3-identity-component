/*
 * PostOperatorResponse.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for the response of creating a new operator.
 *
 * Date: 2025/08/30
 */

package io.github.open_dataspaces.core.domain.dto;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.consts.Const;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Data Transfer Object for the response of creating a new operator.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
public class PostOperatorResponse extends OperatorResponse {

    /**
     * Constructor with no parameters.
     */
    @Builder
    public PostOperatorResponse() {
        super();
    }

    /**
     * Populates this response object from an OperatorEntity.
     *
     * @param operatorResult the OperatorResult
     * @param loginUserId the login user ID
     * @param password the password
     */
    public PostOperatorResponse(OperatorResult operatorResult, String loginUserId, String password) {
        super(operatorResult);
        this.setLoginUserId(loginUserId);
        if (StringUtils.hasText(password)) {
            this.setPassword(password);
        }
    }

    /**
     * The Password for the operator.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(Const.JSON_PROPERTY_LOGIN_USER_PASSWORD)
    private String password;
}
