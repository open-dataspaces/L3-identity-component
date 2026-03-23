/*
 * PostClientSecretResponseTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies the behavior and validation logic of the PostClientSecretResponse DTO.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.open_dataspaces.core.common.consts.Const;

/**
 * Unit tests for the DTO PostClientSecretResponse.
 */
public class PostClientSecretResponseTest {

    private final String commonClientSecret = "example-client-secret";

    @Test
    @DisplayName("PostClientSecretResponse - Default Constructor")
    void testDefaultConstructor() {
        PostClientSecretResponse response = new PostClientSecretResponse();

        assertNotNull(response);
        assertNull(response.getClientSecret());
    }

    @Test
    @DisplayName("PostClientSecretResponse - All Args Constructor")
    void testAllArgsConstructor() {
        String testClientSecret = commonClientSecret;
        PostClientSecretResponse response = new PostClientSecretResponse(testClientSecret);

        assertNotNull(response);
        assertNotNull(response.getClientSecret());
    }

    @Test
    @DisplayName("PostClientSecretResponse - Getter and Setter")
    void testSettersAndGetters() {
        PostClientSecretResponse response = new PostClientSecretResponse();
        response.setClientSecret(commonClientSecret);

        assertNotNull(response.getClientSecret());
    }

    @Test
    @DisplayName("PostClientSecretResponse - toString Test")
    void testToString() {
        PostClientSecretResponse response = new PostClientSecretResponse(commonClientSecret);
        String toStringResult = response.toString();

        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains(commonClientSecret), "toString should include cmpPlantId");
    }

    @Test
    @DisplayName("PostClientSecretResponse - Equals and HashCode Test")
    void testEqualsAndHashCode() {
        PostClientSecretResponse response1 = new PostClientSecretResponse(commonClientSecret);
        PostClientSecretResponse response2 = new PostClientSecretResponse(commonClientSecret);
        PostClientSecretResponse response3 = new PostClientSecretResponse(commonClientSecret + "-diff");

        assertEquals(response1, response2, "Objects with same clientSecret should be equal");
        assertNotEquals(response1, response3, "Objects with different clientSecret should not be equal");
        assertEquals(response1.hashCode(), response2.hashCode(), "Hash codes should be equal for equal objects");
        assertNotEquals(response1.hashCode(), response3.hashCode(), "Objects with different clientSecret should not be equal");
    }

    @Test
    @DisplayName("PostClientSecretResponse - @JsonProperty Annotation Test")
    void testJsonPropertyNames() throws NoSuchFieldException {
        var field = PostClientSecretResponse.class.getDeclaredField("clientSecret");
        var annotation = field.getAnnotation(JsonProperty.class);
        assertNotNull(annotation, "JsonProperty annotation should be present on clientSecret field");
        assertEquals(Const.JSON_PROPERTY_CLIENT_SECRET, annotation.value(), "JsonProperty value should match expected JSON property name");
    }

    @Test
    @DisplayName("PostClientSecretResponse - JSON Serialization Test")
    void testJsonSerialization() throws Exception {
        PostClientSecretResponse response = new PostClientSecretResponse(commonClientSecret);
        ObjectMapper objectMapper = new ObjectMapper();

        String jsonString = objectMapper.writeValueAsString(response);
        assertTrue(jsonString.contains("\"" + Const.JSON_PROPERTY_CLIENT_SECRET + "\":\"" + commonClientSecret + "\""),
                "Serialized JSON should contain the correct clientSecret property");
    }
}