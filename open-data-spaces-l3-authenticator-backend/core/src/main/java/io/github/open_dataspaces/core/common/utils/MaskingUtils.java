/*
 * MaskingUtils.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * Utility class for masking sensitive information.
 *
 * Date: 2025/08/12
 */

package io.github.open_dataspaces.core.common.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import io.github.open_dataspaces.core.common.annotations.Masked;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

/**
 * Utility class for masking sensitive information.
 *
 * <p>This class provides utility methods for masking sensitive information in DTOs.</p>
 */
public class MaskingUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MaskingUtils.class);

    private static final Map<String, Masked> MASKED_FIELD_MAP = new HashMap<>();

    static {
        // Search for DTO classes with the annotation
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(Const.MASK_DTO_PACKAGE))
                .setScanners(Scanners.FieldsAnnotated));

        // Get fields annotated with the Masked annotation
        Set<Field> maskedFields = reflections.getFieldsAnnotatedWith(Masked.class);

        for (Field field : maskedFields) {
            String jsonKey = field.getName();
            // Check if the field is annotated with @JsonProperty
            if (field.isAnnotationPresent(JsonProperty.class)) {
                jsonKey = field.getAnnotation(JsonProperty.class).value();
            }
            MASKED_FIELD_MAP.put(jsonKey, field.getAnnotation(Masked.class));
        }
    }

    /**
     * Masks JSON fields in the given object based on the @Masked annotation.
     *
     * @param objects the array of objects to mask
     * @return the array of masked objects
     */
    public static Object[] maskJsonFieldsByAnnotation(Object[] objects) {
        List<Object> maskedArgs = new ArrayList<>();
        for (Object object : objects) {
            maskedArgs.add(maskJsonFieldsByAnnotation(object));
        }
        return maskedArgs.toArray();
    }

    /**
     * Masks JSON fields in the given object based on the @Masked annotation.
     *
     * @param object the object to mask
     * @return the masked object
     */
    public static Object maskJsonFieldsByAnnotation(Object object) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            if (object != null && MaskingUtils.isMaskTarget(object.getClass())) {
                // Mask only if it is a DTO
                String json = MaskingUtils.maskJsonFieldsByAnnotation(mapper.writeValueAsString(object));
                return mapper.readValue(json, object.getClass());
            } else {
                return object;
            }
        } catch (IOException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(String.format(ConstError.ERRLOG_JSON_MASK_FAILED, e.getMessage()));
            }
        }
        return object;
    }

    /**
     * Masks JSON fields based on the @Masked annotation.
     *
     * @param jsonBody the JSON string to mask
     * @return the masked JSON string
     */
    public static String maskJsonFieldsByAnnotation(String jsonBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(jsonBody);

            maskFieldsRecursively(jsonNode);

            return mapper.writeValueAsString(jsonNode);
        } catch (IOException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(String.format(ConstError.ERRLOG_JSON_MASK_FAILED, e.getMessage()));
            }
            jsonBody = null;
        }
        return jsonBody;
    }

    /**
     * Masks fields in the given JSON node based on the @Masked annotation.
     *
     * @param node the JSON node to mask
     */
    private static void maskFieldsRecursively(JsonNode node) {
        // Check if the input string is a JSON object
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objNode = (ObjectNode) node;
            Iterator<String> fieldNames = objNode.fieldNames();
            // Iterate over JSON field names
            while (fieldNames.hasNext()) {
                // Check if the field name is included in the set of masked field names
                String field = fieldNames.next();
                JsonNode child = objNode.get(field);
                if (MASKED_FIELD_MAP.containsKey(field) && child.isValueNode()) {
                    // Perform masking for fields that require masking
                    String original = child.asText();
                    Masked masked = MASKED_FIELD_MAP.get(field);
                    objNode.put(field, maskValue(original, masked.unmaskedPrefixLength(), masked.unmaskedSuffixLength()));
                } else {
                    maskFieldsRecursively(child);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                maskFieldsRecursively(item);
            }
        }
    }

    /**
     * Checks if the given class is a target for masking.
     *
     * @param clazz the class to check
     * @return true if the class is a target for masking, false otherwise
     */
    public static boolean isMaskTarget(Class<?> clazz) {
        // ResponseEntity class is always a masking target
        if (clazz == ResponseEntity.class) {
            return true;
        }
        // Determine by whether the class is under the DTO package
        return clazz.getPackageName().startsWith(Const.MASK_DTO_PACKAGE);
    }

    /**
     * Masks the given value based on the specified start position.
     *
     * @param value the original string value
     * @param unmaskedPrefixLength the length of the unmasked prefix
     * @param unmaskedSuffixLength the length of the unmasked suffix
     * @return the masked string
     */
    public static String maskValue(String value, int unmaskedPrefixLength, int unmaskedSuffixLength) {
        // Process only if value is not null
        if (value == null) {
            return null;
        }

        int length = value.length();
        // If the sum of unmasked parts is greater than or equal to the original length, mask all
        if (unmaskedPrefixLength + unmaskedSuffixLength >= length) {
            return "*****";
        }

        // Get the unmasked prefix and suffix
        String prefix = value.substring(0, unmaskedPrefixLength);
        String suffix = value.substring(length - unmaskedSuffixLength);

        // Mask the middle part
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append(Const.MASK);
        sb.append(suffix);

        return sb.toString();
    }
}