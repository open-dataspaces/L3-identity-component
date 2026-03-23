/*
 * UUIDUtils.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class provides utility methods for handling UUID conversions.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.common.utils;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.lang.NonNull;

import io.github.open_dataspaces.core.common.consts.Const;

/**
 * Utility class for handling UUID conversions.
 */
public class UUIDUtils {

    // Private constructor to prevent instantiation. It is for testing purposes only.
    private UUIDUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    /**
     * Converts a list of string values to a list of UUIDs.
     *
     * <p>If any value in the list is not a valid UUID, an empty list is returned.</p>
     *
     * @param paramValues the list of string values to convert
     * @return a list of UUIDs, or an empty list if any value is invalid
     */
    public static @NonNull List<UUID> getUUIDs(@NonNull List<String> paramValues) {
        List<UUID> uuids = new ArrayList<UUID>();
        for (String value : paramValues) {
            try {
                if (!value.matches(Const.REGEX_UUID)) {
                    throw new IllegalArgumentException();
                }
                uuids.add(UUID.fromString(value));
            } catch (IllegalArgumentException e) {
                // Return empty list if any value is not a valid UUID
                return new ArrayList<UUID>();
            }
        }
        return uuids;
    }
}
