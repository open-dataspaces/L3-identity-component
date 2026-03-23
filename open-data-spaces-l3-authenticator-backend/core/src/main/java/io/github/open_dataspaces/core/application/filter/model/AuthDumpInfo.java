/*
 * AuthDumpInfo.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This file is part of the user authentication backend system.
 *
 * AuthDumpInfo is a data holder for authentication dump information,
 * used for logging authentication-related request and response details.
 *
 * @author btmurayamakohei
 * @since 2025/06/11
 */

package io.github.open_dataspaces.core.application.filter.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.consts.Const;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data holder for authentication dump information.
 */
@Data
@AllArgsConstructor
public class AuthDumpInfo {

    @JsonProperty(Const.DUMP_ITEM_EVENT)
    private String event;
    @JsonProperty(Const.DUMP_ITEM_IS_REQUEST_RESULT)
    private boolean isRequestResult;
    @JsonProperty(Const.DUMP_ITEM_REQUEST_BODY)
    private Object requestBody;
    @JsonProperty(Const.DUMP_ITEM_RESPONSE_BODY)
    private Object responseBody;
    @JsonProperty(Const.DUMP_ITEM_TIME_STAMP)
    private String timeStamp;
    @JsonProperty(Const.DUMP_ITEM_REQUEST_IP_ADDRESS)
    private String requestIpAddress;
    @JsonProperty(Const.DUMP_ITEM_REQUEST_API_KEY)
    private String requestApiKey;

}
