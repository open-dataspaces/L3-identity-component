/*
 * EntityUtils.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This utility class provides methods related to entity classes.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.common.utils;

import jakarta.persistence.Table;

/**
 * Utility class for entity-related operations.
 */
public class EntityUtils {

    /**
     * Get the table name from the entity class.
     *
     * @param clazz the entity class
     * @return the table name
     */
    public static String getTableName(Class<?> clazz) {
        Table tableAnno = clazz.getAnnotation(Table.class);
        return (tableAnno != null && !tableAnno.name().isEmpty())
                ? tableAnno.name()
                : clazz.getSimpleName();    // Fallback to default name
    }
}
