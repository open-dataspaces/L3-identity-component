/*
 * EvaluateRequest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * DTO for Evaluate Request to Authorization Service.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.infrastructure.dto;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.consts.Const;

import lombok.Data;

/**
 * DTO for Evaluate Request to Authorization Service.
 */
@Data
public class EvaluateRequest {
    private Subject subject;
    private Resource resource;
    private Action action;
    private Context context;

    /**
     * DTO for Subject in Evaluate Request.
     */
    @Data
    public static class Subject {
        private String type;
        private String id;
    }

    /**
     * DTO for Resource in Evaluate Request.
     */
    @Data
    public static class Resource {
        private String type;
        private String id;
    }

    /**
     * DTO for Action in Evaluate Request.
     */
    @Data
    public static class Action {
        private String name;
    }

    /**
     * DTO for Context in Evaluate Request.
     */
    @Data
    public static class Context {
        @JsonIgnore
        private LocalDateTime currentTime;

        // Serialize currentTime as ISO 8601 string in UTC
        @JsonProperty(Const.JSON_PROPERTY_CURRENT_TIME)
        public String getCurrentTimeAsString() {
            return currentTime == null ? null : currentTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
    }

    /**
     * Constructor for EvaluateRequest.
     */
    public EvaluateRequest(
            String subjectType,
            String subjectId,
            String resourceType,
            String resourceId,
            String actionName,
            LocalDateTime contextCurrentTime
    ) {
        this.subject = new Subject();
        this.subject.setType(subjectType);
        this.subject.setId(subjectId);

        this.resource = new Resource();
        this.resource.setType(resourceType);
        this.resource.setId(resourceId);

        this.action = new Action();
        this.action.setName(actionName);

        this.context = new Context();
        this.context.setCurrentTime(contextCurrentTime);
    }
}