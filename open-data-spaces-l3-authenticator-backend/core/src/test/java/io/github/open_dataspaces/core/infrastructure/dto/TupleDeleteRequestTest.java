/*
 * TupleDeleteRequestTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the TupleDeleteRequest DTO.
 *
 * Date: 2026/02/20
 */

package io.github.open_dataspaces.core.infrastructure.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.infrastructure.dto.TupleDeleteRequest.Deletes;

/**
 * Unit tests for TupleDeleteRequest DTO.
 */
public class TupleDeleteRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    // Common test data
    private final Tuple tuple1 = new Tuple("user1", "relation1", "object1");
    private final Tuple tuple2 = new Tuple("user2", "relation2", "object2");
    private final List<Tuple> tuples = Arrays.asList(tuple1, tuple2);

    @Test
    @DisplayName("Default Constructor")
    void testDefaultConstructor() {
        TupleDeleteRequest dtoObject = new TupleDeleteRequest();
        assertNotNull(dtoObject);
        assertNull(dtoObject.getDeletes());
    }

    @Test
    @DisplayName("AllArgsConstructor Test")
    void testAllArgsConstructor() {
        Deletes deletes = new Deletes(tuples);
        TupleDeleteRequest dtoObject = new TupleDeleteRequest(deletes);
        assertEquals(deletes, dtoObject.getDeletes());
        assertEquals(tuples, dtoObject.getDeletes().getTupleKeys());
    }

    @Test
    @DisplayName("Setters and Getters Test")
    void testSettersAndGetters() {
        Deletes deletes = new Deletes();
        deletes.setTupleKeys(tuples);
        TupleDeleteRequest dtoObject = new TupleDeleteRequest();
        dtoObject.setDeletes(deletes);
        assertEquals(deletes, dtoObject.getDeletes());
        assertEquals(tuples, dtoObject.getDeletes().getTupleKeys());
    }

    @Test
    @DisplayName("toString Test")
    void testToString() {
        Deletes deletes = new Deletes(tuples);
        TupleDeleteRequest dtoObject = new TupleDeleteRequest(deletes);
        String result = dtoObject.toString();
        assertTrue(result.contains("Deletes"), "toString should include 'Deletes'");
        assertTrue(result.contains("tupleKeys"), "toString should include 'tupleKeys'");
        assertTrue(result.contains("user1"), "toString should include tuple user1");
        assertTrue(result.contains("user2"), "toString should include tuple user2");
    }

    @Test
    @DisplayName("Equals and HashCode Test")
    void testEqualsAndHashCode() {
        Deletes deletes1 = new Deletes(tuples);
        Deletes deletes2 = new Deletes(tuples);
        Deletes deletesDiff = new Deletes(Collections.singletonList(tuple1));
        TupleDeleteRequest dtoObject = new TupleDeleteRequest(deletes1);
        TupleDeleteRequest dtoObjectSame = new TupleDeleteRequest(deletes2);
        TupleDeleteRequest dtoObjectDiff = new TupleDeleteRequest(deletesDiff);
        assertEquals(dtoObject, dtoObjectSame, "Objects with same values should be equal");
        assertNotEquals(dtoObject, dtoObjectDiff, "Objects with different values should not be equal");
        assertEquals(dtoObject.hashCode(), dtoObjectSame.hashCode(), "Hash codes should be equal for same objects");
        assertNotEquals(dtoObject.hashCode(), dtoObjectDiff.hashCode(), "Hash codes should differ for different objects");
    }

    @Test
    @DisplayName("TupleDeleteRequest - @JsonProperty Annotation Test")
    void testJsonPropertyNames() throws Exception {
        Field field = TupleDeleteRequest.class.getDeclaredField(Const.TUPLE_DELETE);
        JsonProperty annotation = field.getAnnotation(JsonProperty.class);
        assertNotNull(annotation);
        assertEquals(Const.TUPLE_DELETE, annotation.value());
    }

    @Test
    @DisplayName("Deletes - @JsonProperty Annotation Test")
    void testDeletesJsonPropertyNames() throws Exception {
        Field field = Deletes.class.getDeclaredField("tupleKeys");
        JsonProperty annotation = field.getAnnotation(JsonProperty.class);
        assertNotNull(annotation);
        assertEquals(Const.TUPLE_KEYS, annotation.value());
    }

    @Test
    @DisplayName("JSON serialization")
    void testJsonSerialization() throws Exception {
        Deletes deletes = new Deletes(tuples);
        TupleDeleteRequest dtoObject = new TupleDeleteRequest(deletes);
        String json = objectMapper.writeValueAsString(dtoObject);
        assertTrue(json.contains("\"" + Const.TUPLE_DELETE + "\""), "JSON should contain deletes property: " + json);
        assertTrue(json.contains("\"" + Const.TUPLE_KEYS + "\""), "JSON should contain tupleKeys property: " + json);
        assertTrue(json.contains("user1"), "JSON should contain user1: " + json);
        assertTrue(json.contains("user2"), "JSON should contain user2: " + json);
    }
}
