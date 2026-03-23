/*
 * JSONUtil.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class provides utility methods for JSON operations.
 *
 * Date: 2025-08-06
 */

package io.github.open_dataspaces.core.common.utils;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JavaType;

/**
 * Utility class for JSON operations.
 */
@Component
public class JSONUtil {

    /**
     * Retrieves the JSON property name for a given field in a class.
     *
     * @param classType the class type containing the field
     * @param fieldName the name of the field
     * @return the JSON property name, or the field name if not found
     */
    public static String getJsonPropertyName(Class<?> classType, String fieldName) {
        ObjectMapper mapper = new ObjectMapper();   // Create a new ObjectMapper instance
        JavaType javaType = mapper.constructType(classType);    // Construct JavaType from the class
        for (BeanPropertyDefinition prop : mapper.getSerializationConfig()
                .introspect(javaType)
                .findProperties()) {
            if (prop.getInternalName().equals(fieldName)) {
                return prop.getName(); // JSON property name
            }
        }
        return fieldName; // If not found, return Java name
    }
}
