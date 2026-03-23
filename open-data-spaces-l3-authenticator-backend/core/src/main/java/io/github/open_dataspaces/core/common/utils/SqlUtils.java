/*
 * SqlUtils.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class provides utility methods for handling SQL operations.
 *
 * Date: 2025/09/30
 */

package io.github.open_dataspaces.core.common.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.consts.ConstSqlQueries;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.UnexpectedException;

import jakarta.persistence.Column;

/**
 * Utility class for SQL-related operations.
 */
public class SqlUtils {

    private static final Map<Class<?>, Map<String, String>> ENTITY_COLUMN_TO_PROPERTY_MAP = new HashMap<>();

    /**
     * Enumeration of specification methods for query building.
     */
    public enum SpecificationMethod {
        EQUALS,
        LIKE,
        GREATER_THAN,
        GREATER_THAN_OR_EQUALS,
        LESS_THAN,
        LESS_THAN_OR_EQUALS
    }

    static {
        // Search for entity classes with the annotation
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(ConstSqlQueries.DATABASE_ENTITIES_PACKAGE))
                .setScanners(Scanners.TypesAnnotated, Scanners.FieldsAnnotated));

        // Get classes annotated with @Entity
        Set<Class<?>> entityClasses = reflections.getTypesAnnotatedWith(jakarta.persistence.Entity.class);

        for (Class<?> entityClass : entityClasses) {
            Map<String, String> columnToPropertyMap = new HashMap<>();
            Class<?> current = entityClass;
            // Traverse class hierarchy to include fields from superclasses
            while (current != null && current != Object.class) {
                for (Field field : current.getDeclaredFields()) {
                    Column column = field.getAnnotation(Column.class);
                    if (column != null) {
                        columnToPropertyMap.putIfAbsent(column.name(), field.getName());
                    }
                }
                // Move to the superclass
                current = current.getSuperclass();
            }
            if (!columnToPropertyMap.isEmpty()) {
                ENTITY_COLUMN_TO_PROPERTY_MAP.put(entityClass, columnToPropertyMap);
            }
        }
    }

    /**
     * Creates an empty Specification that can be used as a starting point for building queries.
     *
     * @param <T> the type of the entity
     * @param <V> the type of the comparable value
     * @return an empty Specification
     */
    public static <T, V extends Comparable<? super V>> Specification<T> createSpecification() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }

    /**
     * Finds the entity property name by the column name.
     *
     * @param columnName the column name
     * @return the entity property name, or null if not found
     */
    public static String getEntityPropertyNameByColumn(Class<?> entityClass, String columnName) {
        Map<String, String> columnToPropertyMap = ENTITY_COLUMN_TO_PROPERTY_MAP.get(entityClass);
        return columnToPropertyMap != null ? columnToPropertyMap.get(columnName) : null;
    }

    /**
     * Converts a string with asterisks to a SQL LIKE pattern with percent signs.
     *
     * @param input the input string
     * @return the converted string
     */
    public static String convertWildcard(String input) {
        if (input == null) {
            return null;
        }
        // Escape existing SQL wildcard characters
        input = input.replace(ConstSqlQueries.WILDCARD_SQL, String.valueOf(ConstSqlQueries.ESCAPE_CHAR) + ConstSqlQueries.WILDCARD_SQL);
        // Replace asterisks with SQL wildcard characters
        input = input.replace(ConstSqlQueries.WILDCARD_REQUEST_PARAMETER, ConstSqlQueries.WILDCARD_SQL);
        return input;
    }

    /**
     * Builds a Specification based on the provided parameters.
     *
     * @param spec the existing specification to build upon
     * @param method the specification method (e.g., EQUALS, LIKE)
     * @param entityClass the entity class
     * @param columnName the database column name
     * @param value the value to compare
     * @return the updated specification
     */
    public static <T, V extends Comparable<? super V>> Specification<T> buildSpecification(Specification<T> spec, SpecificationMethod method, Class<T> entityClass, String columnName, V value) {
        String propertyName = getEntityPropertyNameByColumn(entityClass, columnName);
        if (propertyName == null) {
            return null;
        }

        switch (method) {
            case EQUALS:
                if (value == null) {
                    spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get(propertyName)));
                } else {
                    spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(propertyName), value));
                }
                break;
            case LIKE:
                if (value instanceof String) {
                    spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.like(
                            root.get(propertyName),
                            convertWildcard((String) value),
                            ConstSqlQueries.ESCAPE_CHAR));
                } else {
                    throw new UnexpectedException(
                        ConstError.ERR_500_DB_QUERY_FAILED, String.format(ConstError.ERRLOG_500_ERROR, columnName, value));
                }
                break;
            case GREATER_THAN:
                spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get(propertyName), value));
                break;
            case GREATER_THAN_OR_EQUALS:
                spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get(propertyName), value));
                break;
            case LESS_THAN:
                spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get(propertyName), value));
                break;
            case LESS_THAN_OR_EQUALS:
                spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get(propertyName), value));
                break;
            default:
                throw new UnexpectedException(
                    ConstError.ERR_500_DB_QUERY_FAILED, String.format(ConstError.ERRLOG_500_ERROR, columnName, value));
        }
        return spec;
    }

    /**
     * Builds a Sort object based on the provided parameters.
     *
     * @param entityClass the entity class
     * @param orderByKey the column name to sort by
     * @param orderByDirection the sort direction ("ASC" or "DESC")
     * @param defaultOrderByKey the default column name to sort by if orderByKey is null
     * @param defaultOrderByDirection the default sort direction if orderByDirection is null
     * @return the Sort object
     */
    public static Sort buildSort(Class<?> entityClass, String orderByKey, String orderByDirection, String defaultOrderByKey, Sort.Direction defaultOrderByDirection) {
        // Determine the entity property name for the orderByKey
        String orderByKeyEntityProperty;
        if (orderByKey == null) {
            orderByKeyEntityProperty = getEntityPropertyNameByColumn(entityClass, defaultOrderByKey);
        } else {
            orderByKeyEntityProperty = getEntityPropertyNameByColumn(entityClass, orderByKey);
            if (!StringUtils.hasText(orderByKeyEntityProperty)) {
                throw new BadParametersException(
                    String.format(ConstError.ERRLOG_400_INVALID_REQUEST_PARAMETER, Const.JSON_PROPERTY_SORT_KEY, orderByKey));
            }
        }

        // Determine the sort direction
        Direction sortDirection = defaultOrderByDirection;
        if (orderByDirection != null) {
            try {
                sortDirection = Sort.Direction.valueOf(orderByDirection.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadParametersException(
                    String.format(ConstError.ERRLOG_400_INVALID_REQUEST_PARAMETER, Const.JSON_PROPERTY_SORT_ORDER, orderByDirection));
            }
        }

        // Build the Sort object
        return Sort.by(sortDirection, orderByKeyEntityProperty);

    }

}