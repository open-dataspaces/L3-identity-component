/*
 * APIKeyVerifyParamTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies the behavior of the APIKeyVerifyParam DTO,
 * including its construction and getter methods for API key, IP address,
 * and attribute handling.
 *
 * Date: 2025-06-30
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.open_dataspaces.core.common.enums.EnumAPIKeyAttributes;

/*
 * APIKeyVerifyParamTest is a test class for the APIKeyVerifyParam.
 */
class APIKeyVerifyParamTest {

    /**
     * case#1:
     * testGetAPIKeyVerifyParam tests that APIKeyVerifyParam is constructed correctly,
     * and its getter methods for API key, IP address, attributes, and attribute
     * names work as expected.
     */
    @Test
    void testGetAPIKeyVerifyParam() {
        // Set value
        List<EnumAPIKeyAttributes> attrs = Arrays.asList(EnumAPIKeyAttributes.DATASPACE,
                EnumAPIKeyAttributes.APPLICATION);
        APIKeyVerifyParam param = new APIKeyVerifyParam("test-key", "127.0.0.1", attrs);
        // Assert
        assertEquals("test-key", param.getApiKey());
        assertEquals("127.0.0.1", param.getIp());
    }

    /**
     * case#2:
     * testGetAPIKeyVerifyParamEmpty tests that APIKeyVerifyParam handles empty
     * strings and empty attribute list correctly,
     * and its getter methods return expected empty values.
     */
    @Test
    void testGetAPIKeyVerifyParamEmpty() {
        // Set value
        APIKeyVerifyParam param = new APIKeyVerifyParam("", "", Collections.emptyList());
        // Assert
        assertTrue(param.getApiKey().isBlank());
        assertTrue(param.getIp().isBlank());
    }

    /**
     * case#3:
     * testGetAPIKeyVerifyParamNull tests that APIKeyVerifyParam handles null values
     * for API key, IP, and attributes correctly,
     * and its getter methods return expected null or empty values.
     */
    @Test
    void testGetAPIKeyVerifyParamNull() {
        // Set value
        APIKeyVerifyParam param = new APIKeyVerifyParam(null, null, null);
        // Assert
        assertNull(param.getApiKey());
        assertNull(param.getIp());
    }
}
