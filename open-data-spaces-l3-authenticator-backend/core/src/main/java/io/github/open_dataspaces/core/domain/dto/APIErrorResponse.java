/*
 * APIErrorResponse.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for representing error responses.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.enums.EnumResponseTypes;

import jakarta.servlet.http.HttpServletRequest;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object for representing error responses.
 */
@Data
@AllArgsConstructor
public class APIErrorResponse {

    /**
     * The type of the response, typically indicating success or error.
     */
    @NonNull
    @JsonProperty(Const.JSON_PROPERTY_COMMON_TYPE)
    private String type;

    /**
     * The title of the response, usually a brief description of the response type.
     */
    @NonNull
    @JsonProperty(Const.JSON_PROPERTY_COMMON_TITLE)
    private String title;

    /**
     * The HTTP status code of the response.
     */
    @JsonProperty(Const.JSON_PROPERTY_COMMON_STATUS)
    private int status;

    /**
     * A detailed message providing additional context about the response.
     */
    @NonNull
    @JsonProperty(Const.JSON_PROPERTY_COMMON_DETAIL)
    private String detail;

    /**
     * Constructs an APIErrorResponse with the given parameters.
     *
     * @param request the HTTP request
     * @param title the title of the error
     * @param status the HTTP status code
     */
    public APIErrorResponse(@NonNull HttpServletRequest request, String title, int status) {
        // Build the detail string using StringBuilder for better performance
        StringBuilder detailStringBuilder = new StringBuilder();
        detailStringBuilder.append(String.format(Const.API_RESPONSE_DETAIL_SUCCESS_FORMAT_TIMESTAMP, LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(Const.ISO_8601_UTC_FORMAT))));
        detailStringBuilder.append(Const.API_RESPONSE_DETAIL_SEPARATOR);
        detailStringBuilder.append(String.format(Const.API_RESPONSE_DETAIL_SUCCESS_FORMAT_METHOD, request.getMethod()));

        // Set the response fields
        this.type = EnumResponseTypes.getResponseType(status);
        this.title = title;
        this.status = status;
        this.detail = detailStringBuilder.toString();
    }

}
