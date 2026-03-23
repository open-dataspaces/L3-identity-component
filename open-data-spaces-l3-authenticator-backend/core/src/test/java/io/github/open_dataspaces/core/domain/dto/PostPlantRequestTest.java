/*
 * PostPlantRequestTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies the behavior and validation logic of the PostPlantRequest DTO.
 *
 * Date: 2025/08/30
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.open_dataspaces.core.common.consts.Const;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * PostPlantRequestTest.
 */
class PostPlantRequestTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    /**
     * Test the no-args constructor.
     * Ensures that the object is created with default values.
     */
    @Test
    @DisplayName("Test no-args constructor")
    void testNoArgsConstructor() {
        // Act
        PostPlantRequest response = new PostPlantRequest();

        // Assert
        assertNotNull(response);
    }

    /**
     * Test for parameterized constructor.
     */
    @ParameterizedTest
    @CsvSource({
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId12345623456,GlobalId1,2025-08-30,2025-12-31",
        "123e4567-e89b-12d3-a456-426614174001,Plant2,Address2,OpenId234567,GlobalId2,2025-09-01,2025-12-31" })
    @DisplayName("PostPlantRequest - Valid Input")
    void testParameterizedConstructor(
            String operatorId,
            String plantName,
            String plantAddress,
            String openPlantId,
            String globalPlantId,
            String effectiveStartDate,
            String effectiveEndDate) {

        // Arrange
        PostPlantRequest request = new PostPlantRequest(
                operatorId,
                plantName,
                plantAddress,
                openPlantId,
                globalPlantId,
                effectiveStartDate,
                effectiveEndDate
        );

        // Act
        Set<ConstraintViolation<PostPlantRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertTrue(violations.isEmpty(), "Expected no validation errors, but found: " + violations);
        assertEquals(operatorId, request.getOperatorId());
        assertEquals(plantName, request.getPlantName());
        assertEquals(plantAddress, request.getPlantAddress());
        assertEquals(openPlantId, request.getOpenPlantId());
        assertEquals(globalPlantId, request.getGlobalPlantId());
        assertEquals(effectiveStartDate, request.getEffectiveStartDate());
        assertEquals(effectiveEndDate, request.getEffectiveEndDate());
    }

    /**
     * Test for valid PostPlantRequest.
     */
    @ParameterizedTest
    @CsvSource({
        // Length boundary cases
        "d9a38406-cae2-4679-b052-15a75f5531e6,o,Address1,OpenId123456,GlobalId1,2025-08-30,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,LONG,Address1,OpenId123456,GlobalId1,2025-08-30,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,A,OpenId123456,GlobalId1,2025-08-30,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,LONG,OpenId123456,GlobalId1,2025-08-30,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,012345,GlobalId1,2025-08-30,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,01234567890123456789012345,GlobalId1,2025-08-30,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,LONG,2025-08-30,2025-12-31",
        // Pattern cases
        "00000000-aaaa-9999-8fff-1234567890ab,Plant1,Address1,OpenId123456,GlobalId1,2025-08-30,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,abcd-e@123456,GlobalId1,2025-08-30,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,GlobalId1,0001-01-01,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,GlobalId1,2025-08-30,9999-12-31",
        // Not required values
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,NULL,2025-08-30,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,'',2025-08-30,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,' ',2025-08-30,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,GlobalId1,NULL,2025-12-31",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,GlobalId1,2025-08-30,NULL" })
    @DisplayName("PostPlantRequest - Valid Input")
    void testValidPostPlantRequest(
            String operatorId,
            String plantName,
            String plantAddress,
            String openPlantId,
            String globalPlantId,
            String effectiveStartDate,
            String effectiveEndDate) {

        // Handle "NULL", empty, and "LONG" inputs
        globalPlantId = "NULL".equals(globalPlantId) || globalPlantId.isEmpty() ? null : globalPlantId;
        plantAddress = "NULL".equals(plantAddress) || plantAddress.isEmpty() ? null : plantAddress;
        effectiveStartDate = "NULL".equals(effectiveStartDate) || effectiveStartDate.isEmpty() ? null : effectiveStartDate;
        effectiveEndDate = "NULL".equals(effectiveEndDate) || effectiveEndDate.isEmpty() ? null : effectiveEndDate;
        plantName = "LONG".equals(plantName) ? "a".repeat(Const.PLANT_NAME_LENGTH_MAX) : plantName;
        plantAddress = "LONG".equals(plantAddress) ? "a".repeat(Const.PLANT_ADDRESS_LENGTH_MAX) : plantAddress;
        globalPlantId = "LONG".equals(globalPlantId) ? "a".repeat(Const.GLOBAL_PLANT_ID_LENGTH_MAX) : globalPlantId;

        // Arrange
        PostPlantRequest request = new PostPlantRequest();
        request.setOperatorId(operatorId);
        request.setPlantName(plantName);
        request.setPlantAddress(plantAddress);
        request.setOpenPlantId(openPlantId);
        request.setGlobalPlantId(globalPlantId);
        request.setEffectiveStartDate(effectiveStartDate);
        request.setEffectiveEndDate(effectiveEndDate);

        // Act
        Set<ConstraintViolation<PostPlantRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertTrue(violations.isEmpty(), "Expected no validation errors, but found: " + violations);
        assertEquals(operatorId, request.getOperatorId());
        assertEquals(plantName, request.getPlantName());
        assertEquals(plantAddress, request.getPlantAddress());
        assertEquals(openPlantId, request.getOpenPlantId());
        assertEquals(globalPlantId, request.getGlobalPlantId());
        assertEquals(effectiveStartDate, request.getEffectiveStartDate());
        assertEquals(effectiveEndDate, request.getEffectiveEndDate());
    }

    /**
     * Test for invalid PostPlantRequest.
     */
    @ParameterizedTest
    @CsvSource({
        // Not blank violations
        "NULL,Plant1,Address1,OpenId123456,GlobalId1,2025-08-30,2025-12-31,1,'%s: cannot be blank'",
        "'',Plant1,Address1,OpenId123456,GlobalId1,2025-08-30,2025-12-31,1,'%s: cannot be blank'",
        "' ',Plant1,Address1,OpenId123456,GlobalId1,2025-08-30,2025-12-31,1,'%s: cannot be blank'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,NULL,Address1,OpenId123456,GlobalId1,2025-08-30,2025-12-31,1,'%s: cannot be blank'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,'',Address1,OpenId123456,GlobalId1,2025-08-30,2025-12-31,1,'%s: cannot be blank'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,' ',Address1,OpenId123456,GlobalId1,2025-08-30,2025-12-31,1,'%s: cannot be blank'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,NULL,OpenId123456,GlobalId1,2025-08-30,2025-12-31,1,'%s: cannot be blank'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,'',OpenId123456,GlobalId1,2025-08-30,2025-12-31,1,'%s: cannot be blank'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,' ',OpenId123456,GlobalId1,2025-08-30,2025-12-31,1,'%s: cannot be blank'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,OpenId123456,NULL,GlobalId1,2025-08-30,2025-12-31,1,'%s: cannot be blank'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,OpenId123456,'',GlobalId1,2025-08-30,2025-12-31,1,'%s: cannot be blank'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,OpenId123456,' ',GlobalId1,2025-08-30,2025-12-31,1,'%s: cannot be blank'",
        // Size violations
        "d9a38406-cae2-4679-b052-15a75f5531e6,LONG,Address1,OpenId123456,GlobalId1,2025-08-30,2025-12-31,1,'%s: the length must be between 1 and 256'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,LONG,OpenId123456,GlobalId1,2025-08-30,2025-12-31,1,'%s: the length must be between 1 and 256'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,12345,GlobalId1,2025-08-30,2025-12-31,1,'%s: the length must be between 6 and 26'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,LONG,GlobalId1,2025-08-30,2025-12-31,1,'%s: the length must be between 6 and 26'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,LONG,2025-08-30,2025-12-31,1,'%s: the length must be between 0 and 256'",
        // Pattern violations
        "00000000-1111-aaaa-gfff-1234567890ab,Plant1,Address1,OpenId123456,GlobalId1,2025-08-30,2025-12-31,1,'%s: invalid UUID format'",
        "d9a38406-cae2-4679-b052-15a75f5531e,Plant1,Address1,OpenId123456,GlobalId1,2025-08-30,2025-12-31,1,'%s: invalid UUID format'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId12345,GlobalId1,2025-08-30,2025-12-31,1,'%s: the length or characters are invalid'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456a,GlobalId1,2025-08-30,2025-12-31,1,'%s: the length or characters are invalid'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,GlobalId1,'',2025-12-31,1,'%s: invalid date format, expected pattern yyyy-MM-dd'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,GlobalId1,25-01-01,2028-12-31,1,'%s: invalid date format, expected pattern yyyy-MM-dd'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,GlobalId1,2024-13-20,2025-08-30,1,'%s: invalid date format, expected pattern yyyy-MM-dd'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,GlobalId1,2024-2-30,2025-08-30,1,'%s: invalid date format, expected pattern yyyy-MM-dd'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,GlobalId1,2025-12-31,'',1,'%s: invalid date format, expected pattern yyyy-MM-dd'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,GlobalId1,2025-12-31,20-12-31,1,'%s: invalid date format, expected pattern yyyy-MM-dd'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,GlobalId1,2024-01-01,2025-13-30,1,'%s: invalid date format, expected pattern yyyy-MM-dd'",
        "d9a38406-cae2-4679-b052-15a75f5531e6,Plant1,Address1,OpenId123456,GlobalId1,2024-11-30,2025-10-32,1,'%s: invalid date format, expected pattern yyyy-MM-dd'"
    })
    @DisplayName("PostPlantRequest - Invalid Input")
    void testInvalidPostPlantRequest(
            String operatorId,
            String plantName,
            String plantAddress,
            String openPlantId,
            String globalPlantId,
            String effectiveStartDate,
            String effectiveEndDate,
            int expectedViolationCount,
            String message) {

        // Handle "NULL" and "LONG" inputs
        operatorId = "NULL".equals(operatorId) ? null : operatorId;
        plantName = "NULL".equals(plantName) ? null : "LONG".equals(plantName)
                ? "a".repeat(Const.PLANT_NAME_LENGTH_MAX + 1) : plantName;
        plantAddress = "NULL".equals(plantAddress) ? null : "LONG".equals(plantAddress)
                ? "a".repeat(Const.PLANT_ADDRESS_LENGTH_MAX + 1) : plantAddress;
        openPlantId = "NULL".equals(openPlantId) ? null : "LONG".equals(openPlantId)
                ? "a".repeat(Const.OPEN_PLANT_ID_LENGTH_MAX - 5) + "123456" : openPlantId;
        globalPlantId = "NULL".equals(globalPlantId) ? null : "LONG".equals(globalPlantId)
                ? "a".repeat(Const.GLOBAL_PLANT_ID_LENGTH_MAX + 1) : globalPlantId;
        effectiveStartDate = "NULL".equals(effectiveStartDate) ? null : effectiveStartDate;
        effectiveEndDate = "NULL".equals(effectiveEndDate) ? null : effectiveEndDate;

        // Arrange
        PostPlantRequest request = new PostPlantRequest(
                operatorId,
                plantName,
                plantAddress,
                openPlantId,
                globalPlantId,
                effectiveStartDate,
                effectiveEndDate
        );

        // Act
        Set<ConstraintViolation<PostPlantRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertTrue(expectedViolationCount <= violations.size(), "Expected " + expectedViolationCount + " validation errors, but found: " + violations);
        assertTrue(violations.stream().anyMatch(v -> message.equals(v.getMessage())),
                "Expected validation message: " + message + ", but found: " + violations);
    }

    /**
     * Test to verify not required values don't raise validation errors.
     */
    @Test
    @DisplayName("PostPlantRequest - Not required Values Test")
    void testNotRequiredValues() {
        // Arrange
        PostPlantRequest request = new PostPlantRequest();
        request.setOperatorId("d9a38406-cae2-4679-b052-15a75f5531e6");
        request.setPlantName("Plant1");
        request.setPlantAddress("Address1");
        request.setOpenPlantId("OpenId123456");

        // Act
        Set<ConstraintViolation<PostPlantRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertTrue(violations.isEmpty(), "Expected no validation errors, but found: " + violations);
    }

    /**
     * Test for setters and getters of PostPlantRequest.
     */
    @Test
    @DisplayName("PostPlantRequest - Setter and Getter Test")
    void testSettersAndGetters() {
        String operatorId = "valid-operator-id";
        String plantName = "plant name1";
        String plantAddr = "plant address 1";
        String openId = "openid123456";
        String globalId = "globalid1";
        String startDate = "2025-08-30";
        String endDate = "2025-12-31";

        // Arrange
        PostPlantRequest request = new PostPlantRequest();

        // Act
        request.setOperatorId(operatorId);
        request.setPlantName(plantName);
        request.setPlantAddress(plantAddr);
        request.setOpenPlantId(openId);
        request.setGlobalPlantId(globalId);
        request.setEffectiveStartDate(startDate);
        request.setEffectiveEndDate(endDate);

        // Assert
        assertEquals(operatorId, request.getOperatorId(), "Getter for operatorId did not return the expected value.");
        assertEquals(plantName, request.getPlantName(), "Getter for plantName did not return the expected value.");
        assertEquals(plantAddr, request.getPlantAddress(), "Getter for plantAddress did not return the expected value.");
        assertEquals(openId, request.getOpenPlantId(), "Getter for openPlantId did not return the expected value.");
        assertEquals(globalId, request.getGlobalPlantId(), "Getter for globalPlantId did not return the expected value.");
        assertEquals(startDate, request.getEffectiveStartDate(), "Getter for effectiveStartDate did not return the expected value.");
        assertEquals(endDate, request.getEffectiveEndDate(), "Getter for effectiveEndDate did not return the expected value.");
    }

    /**
     * Test for the toString method of PostPlantRequest.
     */
    @Test
    @DisplayName("PostPlantRequest - toString Test")
    void testToString() {
        // Arrange
        PostPlantRequest request = new PostPlantRequest(
                "testUserId",
                "testPlantName",
                "testAddress",
                "testOpenPlantId",
                "testGlobalPlantId",
                "2025-08-30",
                "2025-12-31"
        );

        // Act
        String result = request.toString();

        // Assert
        assertTrue(result.contains("testUserId"), "toString should include operatorId");
        assertTrue(result.contains("testPlantName"), "toString should include plantName");
        assertTrue(result.contains("testAddress"), "toString should include plantAddress");
        assertTrue(result.contains("testOpenPlantId"), "toString should include openPlantId");
        assertTrue(result.contains("testGlobalPlantId"), "toString should include globalPlantId");
        assertTrue(result.contains("2025-08-30"), "toString should include effectiveStartDate");
        assertTrue(result.contains("2025-12-31"), "toString should include effectiveEndDate");
    }

    /**
     * Test for equals and hashCode methods.
     */
    @Test
    @DisplayName("PostPlantRequest - Equals and HashCode Test")
    void testEqualsAndHashCode() {
        // Arrange
        PostPlantRequest request1 = new PostPlantRequest(
                "d9a38406-cae2-4679-b052-15a75f5531e6", "Plant1", "Address1",
                "OpenId123456", "GlobalId1", "2025-08-30", "2025-12-31"
        );

        PostPlantRequest request2 = new PostPlantRequest(
                "d9a38406-cae2-4679-b052-15a75f5531e6", "Plant1", "Address1",
                "OpenId123456", "GlobalId1", "2025-08-30", "2025-12-31"
        );

        PostPlantRequest request3 = new PostPlantRequest(
                "d9a38406-cae2-4679-b052-15a75f5531e0", "Operator2", "Address2",
                "OpenId2", "GlobalId2", "2025-09-01", "2025-12-30"
        );

        // Act & Assert
        // Equals tests
        assertEquals(request1, request2, "Objects with the same values should be equal.");
        assertNotEquals(request1, request3, "Objects with different values should not be equal.");

        // HashCode tests
        assertEquals(request1.hashCode(), request2.hashCode(),
                "Objects with the same values should have the same hash code.");
        assertNotEquals(request1.hashCode(), request3.hashCode(),
                "Objects with different values should have different hash codes.");
    }

    /**
     * Tests whether the @JsonProperty annotation of PostPlantRequest is set correctly.
     */
    @Test
    void testJsonPropertyNames() throws Exception {
        // Use reflection to access the fields and their annotations
        Field fieldOperatorId = PostPlantRequest.class.getDeclaredField("operatorId");
        Field fieldPlantName = PostPlantRequest.class.getDeclaredField("plantName");
        Field fieldPlantAddress = PostPlantRequest.class.getDeclaredField("plantAddress");
        Field fieldOpenPlantId = PostPlantRequest.class.getDeclaredField("openPlantId");
        Field fieldGlobalPlantId = PostPlantRequest.class.getDeclaredField("globalPlantId");
        Field fieldEffectiveStartDate = PostPlantRequest.class.getDeclaredField("effectiveStartDate");
        Field fieldEffectiveEndDate = PostPlantRequest.class.getDeclaredField("effectiveEndDate");

        // Get the @JsonProperty annotations
        JsonProperty annotationOperatorId = fieldOperatorId.getAnnotation(JsonProperty.class);
        JsonProperty annotationPlantName = fieldPlantName.getAnnotation(JsonProperty.class);
        JsonProperty annotationPlantAddress = fieldPlantAddress.getAnnotation(JsonProperty.class);
        JsonProperty annotationOpenPlantId = fieldOpenPlantId.getAnnotation(JsonProperty.class);
        JsonProperty annotationGlobalPlantId = fieldGlobalPlantId.getAnnotation(JsonProperty.class);
        JsonProperty annotationEffectiveStartDate = fieldEffectiveStartDate.getAnnotation(JsonProperty.class);
        JsonProperty annotationEffectiveEndDate = fieldEffectiveEndDate.getAnnotation(JsonProperty.class);

        // Assert that the annotation values match the expected JSON property names
        assertNotNull(annotationOperatorId);
        assertNotNull(annotationPlantName);
        assertNotNull(annotationPlantAddress);
        assertNotNull(annotationOpenPlantId);
        assertNotNull(annotationGlobalPlantId);
        assertNotNull(annotationEffectiveStartDate);
        assertNotNull(annotationEffectiveEndDate);
        assertEquals(Const.JSON_PROPERTY_OPERATOR_ID, annotationOperatorId.value());
        assertEquals(Const.JSON_PROPERTY_PLANT_NAME, annotationPlantName.value());
        assertEquals(Const.JSON_PROPERTY_PLANT_ADDRESS, annotationPlantAddress.value());
        assertEquals(Const.JSON_PROPERTY_OPEN_PLANT_ID, annotationOpenPlantId.value());
        assertEquals(Const.JSON_PROPERTY_GLOBAL_PLANT_ID, annotationGlobalPlantId.value());
        assertEquals(Const.JSON_PROPERTY_EFFECTIVE_START_DATE, annotationEffectiveStartDate.value());
        assertEquals(Const.JSON_PROPERTY_EFFECTIVE_END_DATE, annotationEffectiveEndDate.value());
    }

    /**
     * Test JSON serialization of PostPlantRequest.
     */
    @Test
    @DisplayName("PostPlantRequest - JSON Serialization Test")
    void testJsonSerialization() throws Exception {
        // Arrange
        PostPlantRequest request = new PostPlantRequest();
        request.setOperatorId("d9a38406-cae2-4679-b052-15a75f5531e623");
        request.setPlantName("Plant Name");
        request.setPlantAddress("123 Main St");
        request.setOpenPlantId("open123");
        request.setGlobalPlantId("global123");
        request.setEffectiveStartDate("2025-08-30");
        request.setEffectiveEndDate("2025-12-31");

        // Act
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(request);

        // Assert
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_OPERATOR_ID + "\":\"d9a38406-cae2-4679-b052-15a75f5531e623\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_PLANT_NAME + "\":\"Plant Name\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_PLANT_ADDRESS + "\":\"123 Main St\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_OPEN_PLANT_ID + "\":\"open123\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_GLOBAL_PLANT_ID + "\":\"global123\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_EFFECTIVE_START_DATE + "\":\"2025-08-30\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_EFFECTIVE_END_DATE + "\":\"2025-12-31\""));
    }
}