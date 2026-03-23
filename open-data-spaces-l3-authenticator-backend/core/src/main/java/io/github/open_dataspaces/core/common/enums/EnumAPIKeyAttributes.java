/*
 * EnumAPIKeyAttributes.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class defines appliacation's attribute types for API keys.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.common.enums;

/**
 * Enum representing attribute types for API keys.
 *
 * <p>This enum defines possible attributes that can be associated with API keys.</p>
 *
 * @since 2025/06/30
 */
public enum EnumAPIKeyAttributes {

    DATASPACE("DataSpace"), APPLICATION("Application"), TRACEABILITY("Traceability");

    private final String value;

    /**
     * Constructor for EnumAPIKeyAttributes.
     *
     * @param value the string value of the attribute
     */
    EnumAPIKeyAttributes(String value) {
        this.value = value;
    }

    /**
     * Gets the string value of the attribute.
     *
     * @return the attribute value
     */
    public String getValue() {
        return value;
    }
}
