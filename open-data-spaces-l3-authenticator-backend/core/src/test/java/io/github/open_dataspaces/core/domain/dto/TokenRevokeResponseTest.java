/*
 * TokenRevokeResponseTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies the behavior and validation logic
 * of the TokenRevokeResponse DTO.
 *
 * Date: 2026/02/10
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.open_dataspaces.core.common.consts.Const;

/**
 * Unit tests for the TokenRevokeResponse class.
 */
public class TokenRevokeResponseTest {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // Use ISO-8601 format

    private final boolean commonActive = false;

    /**
     * Tests the default constructor of TokenRevokeResponse.
     */
    @Test
    void testDefaultConstructor() {
        TokenRevokeResponse response = new TokenRevokeResponse();
        assertFalse(response.isActive());
    }

    /**
     * Tests the parameterized constructor of TokenRevokeResponse.
     */
    @Test
    void testSettersAndGetters() {
        TokenRevokeResponse entity = new TokenRevokeResponse();
        entity.setActive(commonActive);

        assertFalse(entity.isActive());
    }

    /**
     * Tests toString method of TokenRevokeResponse.
     */
    @Test
    void testToString() {
        TokenRevokeResponse dtoObject = new TokenRevokeResponse();
        String result = dtoObject.toString();
        assertTrue(result.contains(Const.JSON_PROPERTY_ACTIVE), "toString should include active");
    }

    /**
     * Tests equals and hashCode methods of TokenRevokeResponse.
     */
    @Test
    void testEqualsAndHashCode() {
        TokenRevokeResponse dtoObject1 = new TokenRevokeResponse();
        TokenRevokeResponse dtoObject2 = new TokenRevokeResponse();
        TokenRevokeResponse dtoObject3 = new TokenRevokeResponse();
        dtoObject3.setActive(!commonActive);

        assertEquals(dtoObject1, dtoObject2);
        assertEquals(dtoObject1.hashCode(), dtoObject2.hashCode());

        assertNotEquals(dtoObject1, dtoObject3);
        assertNotEquals(dtoObject1.hashCode(), dtoObject3.hashCode());
    }

    /**
     * Tests JSON serialization of TokenRevokeResponse.
     *
     * @throws Exception if serialization fails
     */
    @Test
    void testJsonSerialization() throws Exception {
        TokenRevokeResponse dtoObject = new TokenRevokeResponse();
        // Act
        String json = objectMapper.writeValueAsString(dtoObject);
        // Assert
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_ACTIVE + "\":false"));
    }

}
