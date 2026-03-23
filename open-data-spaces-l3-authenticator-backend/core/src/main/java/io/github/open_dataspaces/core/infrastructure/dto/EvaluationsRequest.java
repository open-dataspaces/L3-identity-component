/*
 * EvaluationaRequest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * DTO for bulk evaluations request to OpenFGA.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.infrastructure.dto;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.consts.Const;

import lombok.Data;

/**
* DTO for Evaluations Request to Authorization Service.
*/
@Data
public class EvaluationsRequest {
    private Subject subject;
    private Action action;
    private List<EvaluationItem> evaluations = new ArrayList<>();
    private Context context;

    /**
     * DTO for Subject in Evaluations Request.
     */
    @Data
    public static class Subject {
        private String type;
        private String id;
    }

    /**
     * DTO for Action in Evaluations Request.
     */
    @Data
    public static class Action {
        private String name;
    }

    /**
     * DTO for individual evaluation item in Evaluations Request.
     */
    @Data
    public static class EvaluationItem {
        private Resource resource;
    }

    /**
     * DTO for Resource in Evaluation Item.
     */
    @Data
    public static class Resource {
        private String type;
        private String id;
    }

    /**
     * DTO for Context in Evaluations Request.
     */
    @Data
    public static class Context {
        @JsonIgnore
        private LocalDateTime currentTime;

        @JsonProperty(Const.JSON_PROPERTY_CURRENT_TIME)
        public String getCurrentTimeAsString() {
            return currentTime == null ? null : currentTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
    }

    /**
     * Constructor for EvaluationsRequest.
     */
    public EvaluationsRequest(String user, String actionName) {
        this.subject = new Subject();
        this.subject.setType(Const.OPENFGA_EVALUATION_DEFAULT_USER_TYPE);
        this.subject.setId(user);

        this.action = new Action();
        this.action.setName(actionName);

        this.context = new Context();
        this.context.setCurrentTime(LocalDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Adds an evaluation item to the evaluations list.
     *
     * @param resourceType The type of the resource
     * @param resourceId The ID of the resource
     */
    public void addEvaluation(String resourceType, String resourceId) {
        EvaluationItem item = new EvaluationItem();
        Resource r = new Resource();
        r.setType(resourceType);
        r.setId(resourceId);
        item.setResource(r);
        this.evaluations.add(item);
    }
}
