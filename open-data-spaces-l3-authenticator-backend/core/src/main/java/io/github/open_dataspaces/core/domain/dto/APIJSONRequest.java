/*
 * APIJSONRequest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class represents a request containing raw JSON data.
 *
 * Date: 2025/10/30
 */

package io.github.open_dataspaces.core.domain.dto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

/**
 * Data Transfer Object for requests containing raw JSON data.
 */
@Data
public class APIJSONRequest {

    @JsonIgnore
    private String rawJson;

    @JsonIgnore
    private List<String> jsonProperties = List.of();

    /**
     * Sets the raw JSON string and parses it into a Map.
     *
     * @param rawJson the raw JSON string
     */
    public void setRawJson(String rawJson) {
        this.rawJson = rawJson;
        if (rawJson != null && !rawJson.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(rawJson);

                this.jsonProperties = new ArrayList<>();
                // Extract field names
                Iterator<String> iterator = node.fieldNames();
                iterator.forEachRemaining(this.jsonProperties::add);
            } catch (Exception e) {
                this.jsonProperties = List.of();
            }
        }
    }

    /**
     * Checks if the specified property exists in the rawJson.
     *
     * @param jsonPropertyName the name of the JSON property to check
     * @return true if the property exists, false otherwise
     */
    public boolean hasProperty(String jsonPropertyName) {
        return jsonProperties != null && jsonProperties.contains(jsonPropertyName);
    }

    /**
     * Returns a StateString based on whether the specified property exists in the rawJson.
     *
     * @param jsonPropertyName the name of the JSON property to check
     * @param value the value to wrap in StateString if the property exists
     * @return StateString containing the value if the property exists, otherwise an unset StateString
     */
    public StateString buildStateStringForProperty(String jsonPropertyName, String value) {
        if (hasProperty(jsonPropertyName)) {
            return StateString.of(value);
        } else {
            return StateString.unset();
        }
    }
}
