/*
 * PostOperatorRequestTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies the behavior and validation logic of the PostOperatorRequest DTO.
 *
 * Date: 2025/08/30
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
 * PostOperatorRequestTest.
 */
class PostOperatorRequestTest {

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
        PostOperatorRequest response = new PostOperatorRequest();

        // Assert
        assertNotNull(response);
    }

    /**
     * Test for parameterized constructor.
     */
    @ParameterizedTest
    @CsvSource({
        "user1,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user2,Operator2,Address2,OpenId2,GlobalId2,2025-09-01,2025-12-31,false,true" })
    @DisplayName("PostOperatorRequest - Valid Input")
    void testParameterizedConstructor(
            String loginUserId,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId,
            String effectiveStartDate,
            String effectiveEndDate,
            String createPasswordFlag,
            String passwordTemporaryFlag) {

        // Arrange
        PostOperatorRequest request = new PostOperatorRequest(
                loginUserId,
                operatorName,
                operatorAddress,
                openOperatorId,
                globalOperatorId,
                effectiveStartDate,
                effectiveEndDate,
                createPasswordFlag,
                passwordTemporaryFlag
        );

        // Act
        Set<ConstraintViolation<PostOperatorRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertTrue(violations.isEmpty(), "Expected no validation errors, but found: " + violations);
        assertEquals(loginUserId, request.getLoginUserId());
        assertEquals(operatorName, request.getOperatorName());
        assertEquals(operatorAddress, request.getOperatorAddress());
        assertEquals(openOperatorId, request.getOpenOperatorId());
        assertEquals(globalOperatorId, request.getGlobalOperatorId());
        assertEquals(effectiveStartDate, request.getEffectiveStartDate());
        assertEquals(effectiveEndDate, request.getEffectiveEndDate());
        assertEquals(Boolean.valueOf(createPasswordFlag), request.getCreatePasswordFlag());
        assertEquals(Boolean.valueOf(passwordTemporaryFlag), request.getPasswordTemporaryFlag());
    }

    /**
     * Test for valid PostOperatorRequest.
     */
    @ParameterizedTest
    @CsvSource({
        "user1,Operator1,Address1,OPENID,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user2,Operator2,Address2,openid2,GlobalId2,2025-09-01,2025-12-31,false,true",
        // Length boundary cases
        "us1,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false",
        "LONG,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user1,o,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user1,LONG,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user1,Operator1,A,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user1,Operator1,LONG,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user1,Operator1,Address1,0,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user1,Operator1,Address1,01234567890123456789,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user1,Operator1,Address1,OpenId1,LONG,2025-08-30,2025-12-31,true,false",
        // Pattern boundary cases
        "az09_.-@,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false",
        "user1,Operator1,Address1,OpenId1,azAZ09,2025-08-30,2025-12-31,true,false",
        "user1,Operator1,Address1,OpenId1,GlobalId1,0001-01-01,2025-12-31,true,false",
        "user1,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,9999-12-31,true,false",
        "user1,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,TRUE,false",
        "user1,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,True,false",
        "user1,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,FALSE,false",
        "user1,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,False,false",
        "user1,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,False,TRUE",
        "user1,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,False,True",
        "user1,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,FALSE",
        "user1,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,False",
        // Not required values
        "user1,Operator1,Address1,OpenId1,NULL,2025-08-30,2025-12-31,true,false",
        "user1,Operator1,Address1,OpenId1,'',2025-08-30,2025-12-31,true,false",
        "user1,Operator1,Address1,OpenId1,' ',2025-08-30,2025-12-31,true,false",
        "user1,Operator1,Address1,OpenId1,GlobalId1,NULL,2025-12-31,true,false",
        "user1,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,NULL,true,false",
        "user1,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,NULL,false",
        "user1,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,NULL",
    })
    @DisplayName("PostOperatorRequest - Valid Input")
    void testValidPostOperatorRequest(
            String loginUserId,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId,
            String effectiveStartDate,
            String effectiveEndDate,
            String createPasswordFlag,
            String passwordTemporaryFlag) {

        // Handle "NULL", empty, and "LONG" inputs
        globalOperatorId = "NULL".equals(globalOperatorId) || globalOperatorId.isEmpty() ? null : globalOperatorId;
        operatorAddress = "NULL".equals(operatorAddress) || operatorAddress.isEmpty() ? null : operatorAddress;
        effectiveStartDate = "NULL".equals(effectiveStartDate) || effectiveStartDate.isEmpty() ? null : effectiveStartDate;
        effectiveEndDate = "NULL".equals(effectiveEndDate) || effectiveEndDate.isEmpty() ? null : effectiveEndDate;
        loginUserId = "LONG".equals(loginUserId) ? "a".repeat(255) : loginUserId;
        operatorName = "LONG".equals(operatorName) ? "a".repeat(255) : operatorName;
        operatorAddress = "LONG".equals(operatorAddress) ? "a".repeat(256) : operatorAddress;
        globalOperatorId = "LONG".equals(globalOperatorId) ? "a".repeat(255) : globalOperatorId;
        createPasswordFlag = "NULL".equals(createPasswordFlag) ? null : createPasswordFlag;
        passwordTemporaryFlag = "NULL".equals(passwordTemporaryFlag) ? null : passwordTemporaryFlag;

        // Arrange
        PostOperatorRequest request = new PostOperatorRequest();
        request.setLoginUserId(loginUserId);
        request.setOperatorName(operatorName);
        request.setOperatorAddress(operatorAddress);
        request.setOpenOperatorId(openOperatorId);
        request.setGlobalOperatorId(globalOperatorId);
        request.setEffectiveStartDate(effectiveStartDate);
        request.setEffectiveEndDate(effectiveEndDate);
        request.setCreatePasswordFlag(createPasswordFlag);
        request.setPasswordTemporaryFlag(passwordTemporaryFlag);

        // Act
        Set<ConstraintViolation<PostOperatorRequest>> violations = VALIDATOR.validate(request);

        // Assert
        // Set default values for flags if they are null
        if (createPasswordFlag == null) {
            createPasswordFlag = "true";
        }
        if (passwordTemporaryFlag == null) {
            passwordTemporaryFlag = "false";
        }
        assertTrue(violations.isEmpty(), "Expected no validation errors, but found: " + violations);
        assertEquals(loginUserId, request.getLoginUserId());
        assertEquals(operatorName, request.getOperatorName());
        assertEquals(operatorAddress, request.getOperatorAddress());
        assertEquals(openOperatorId, request.getOpenOperatorId());
        assertEquals(globalOperatorId, request.getGlobalOperatorId());
        assertEquals(effectiveStartDate, request.getEffectiveStartDate());
        assertEquals(effectiveEndDate, request.getEffectiveEndDate());
        assertEquals(Boolean.valueOf(createPasswordFlag), request.getCreatePasswordFlag());
        assertEquals(Boolean.valueOf(passwordTemporaryFlag), request.getPasswordTemporaryFlag());
    }

    /**
     * Test for invalid PostOperatorRequest.
     */
    @ParameterizedTest
    @CsvSource({
        // Not blank violations
        "NULL,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false,1,'%s: cannot be blank'",
        "'',Operator1,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false,1,'%s: cannot be blank'",
        "' ',Operator1,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false,1,'%s: cannot be blank'",
        "user1,NULL,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false,1,'%s: cannot be blank'",
        "user1,'',Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false,1,'%s: cannot be blank'",
        "user1,' ',Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false,1,'%s: cannot be blank'",
        "user1,Operator1,NULL,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false,1,'%s: cannot be blank'",
        "user1,Operator1,'',OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false,1,'%s: cannot be blank'",
        "user1,Operator1,' ',OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false,1,'%s: cannot be blank'",
        "user1,Operator1,Address1,NULL,GlobalId1,2025-08-30,2025-12-31,true,false,1,'%s: cannot be blank'",
        "user1,Operator1,Address1,'',GlobalId1,2025-08-30,2025-12-31,true,false,1,'%s: cannot be blank'",
        "user1,Operator1,Address1,' ',GlobalId1,2025-08-30,2025-12-31,true,false,1,'%s: cannot be blank'",
        "user1,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,'',false,1,'%s: invalid boolean format'",
        "user1,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,'',1,'%s: invalid boolean format'",
        // Size violations
        "us,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false,1,'%s: the length must be between 3 and 255'",
        "LONG,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false,1,'%s: the length must be between 3 and 255'",
        "user1,LONG,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false,1,'%s: the length must be between 1 and 255'",
        "user1,Operator1,LONG,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false,1,'%s: the length must be between 1 and 256'",
        "user1,Operator1,Address1,LONG,GlobalId1,2025-08-30,2025-12-31,true,false,1,'%s: the length must be between 1 and 20'",
        "user1,Operator1,Address1,OpenId1,LONG,2025-08-30,2025-12-31,true,false,1,'%s: the length must be between 0 and 256'",
        // Pattern violations
        "user!@,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,2025-12-31,true,false,1,'%s: the length or characters are invalid'",
        "user1,Operator1,Address1,OpenId1-1,GlobalId1,2025-08-30,2025-12-31,true,false,1,'%s: the length or characters are invalid'",
        "user1,Operator1,Address1,OpenId1,GlobalId1,in-valid-date,2025-12-31,true,false,1,'%s: invalid date format, expected pattern yyyy-MM-dd'",
        "user1,Operator1,Address1,OpenId1,GlobalId1,2025-08-30,inva-li-dd,true,false,1,'%s: invalid date format, expected pattern yyyy-MM-dd'"
    })
    @DisplayName("PostOperatorRequest - Invalid Input")
    void testInvalidPostOperatorRequest(
            String loginUserId,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId,
            String effectiveStartDate,
            String effectiveEndDate,
            String createPasswordFlag,
            String passwordTemporaryFlag,
            int expectedViolationCount,
            String message) {

        // Handle "NULL" and "LONG" inputs
        loginUserId = "NULL".equals(loginUserId) ? null : "LONG".equals(loginUserId) ? "a".repeat(256) : loginUserId;
        operatorName = "NULL".equals(operatorName) ? null : "LONG".equals(operatorName) ? "a".repeat(256) : operatorName;
        operatorAddress = "NULL".equals(operatorAddress) ? null : "LONG".equals(operatorAddress) ? "a".repeat(257) : operatorAddress;
        openOperatorId = "NULL".equals(openOperatorId) ? null : "LONG".equals(openOperatorId) ? "a".repeat(21) : openOperatorId;
        globalOperatorId = "NULL".equals(globalOperatorId) ? null : "LONG".equals(globalOperatorId) ? "a".repeat(257) : globalOperatorId;
        effectiveStartDate = "NULL".equals(effectiveStartDate) ? null : effectiveStartDate;
        effectiveEndDate = "NULL".equals(effectiveEndDate) ? null : effectiveEndDate;
        createPasswordFlag = "NULL".equals(createPasswordFlag) ? null : createPasswordFlag;
        passwordTemporaryFlag = "NULL".equals(passwordTemporaryFlag) ? null : passwordTemporaryFlag;

        // Arrange
        PostOperatorRequest request = new PostOperatorRequest(
                loginUserId,
                operatorName,
                operatorAddress,
                openOperatorId,
                globalOperatorId,
                effectiveStartDate,
                effectiveEndDate,
                createPasswordFlag,
                passwordTemporaryFlag
        );

        // Act
        Set<ConstraintViolation<PostOperatorRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertTrue(expectedViolationCount <= violations.size(), "Expected " + expectedViolationCount + " validation errors, but found: " + violations);
        assertTrue(violations.stream().anyMatch(v -> message.equals(v.getMessage())),
                "Expected validation message: " + message + ", but found: " + violations);
    }

    /**
     * Test to verify default values for createPasswordFlag and passwordTemporaryFlag.
     */
    @Test
    @DisplayName("PostOperatorRequest - Default Values Test")
    void testDefaultValues() {
        // Arrange
        PostOperatorRequest request = new PostOperatorRequest();

        // Act & Assert
        assertEquals(Const.DEFAULT_CREATE_PASSWORD_FLAG, request.getCreatePasswordFlag(),
                "Expected createPasswordFlag to have the default value.");
        assertEquals(Const.DEFAULT_PASSWORD_TEMPORARY_FLAG, request.getPasswordTemporaryFlag(),
                "Expected passwordTemporaryFlag to have the default value.");
    }

    /**
     * Test to verify not required values don't raise validation errors.
     */
    @Test
    @DisplayName("PostOperatorRequest - Not required Values Test")
    void testNotRequiredValues() {
        // Arrange
        PostOperatorRequest request = new PostOperatorRequest();
        request.setLoginUserId("user1");
        request.setOperatorName("Operator1");
        request.setOperatorAddress("Address1");
        request.setOpenOperatorId("OpenId1");
        // Act
        Set<ConstraintViolation<PostOperatorRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertTrue(violations.isEmpty(), "Expected no validation errors, but found: " + violations);
        assertEquals(true, request.getCreatePasswordFlag());
        assertEquals(false, request.getPasswordTemporaryFlag());
    }

    /**
     * Test for setters and getters of PostOperatorRequest.
     */
    @Test
    @DisplayName("PostOperatorRequest - Setter and Getter Test")
    void testSettersAndGetters() {
        // Arrange
        PostOperatorRequest request = new PostOperatorRequest();

        // Act
        request.setLoginUserId("testUserId");
        request.setOperatorName("testOperatorName");
        request.setOperatorAddress("testAddress");
        request.setOpenOperatorId("testOpenOperatorId");
        request.setGlobalOperatorId("testGlobalOperatorId");
        request.setEffectiveStartDate("2025-08-30");
        request.setEffectiveEndDate("2025-12-31");
        request.setCreatePasswordFlag("true");
        request.setPasswordTemporaryFlag("false");

        // Assert
        assertEquals("testUserId", request.getLoginUserId(), "Getter for loginUserId did not return the expected value.");
        assertEquals("testOperatorName", request.getOperatorName(), "Getter for operatorName did not return the expected value.");
        assertEquals("testAddress", request.getOperatorAddress(), "Getter for operatorAddress did not return the expected value.");
        assertEquals("testOpenOperatorId", request.getOpenOperatorId(), "Getter for openOperatorId did not return the expected value.");
        assertEquals("testGlobalOperatorId", request.getGlobalOperatorId(), "Getter for globalOperatorId did not return the expected value.");
        assertEquals("2025-08-30", request.getEffectiveStartDate(), "Getter for effectiveStartDate did not return the expected value.");
        assertEquals("2025-12-31", request.getEffectiveEndDate(), "Getter for effectiveEndDate did not return the expected value.");
        assertTrue(request.getCreatePasswordFlag(), "Getter for createPasswordFlag did not return the expected value.");
        assertFalse(request.getPasswordTemporaryFlag(), "Getter for passwordTemporaryFlag did not return the expected value.");
    }

    /**
     * Test for the toString method of PostOperatorRequest.
     */
    @Test
    @DisplayName("PostOperatorRequest - toString Test")
    void testToString() {
        // Arrange
        PostOperatorRequest request = new PostOperatorRequest(
                "testUserId",
                "testOperatorName",
                "testAddress",
                "testOpenOperatorId",
                "testGlobalOperatorId",
                "2025-08-30",
                "2025-12-31",
                "true",
                "false"
        );

        // Act
        String result = request.toString();

        // Assert
        assertTrue(result.contains("testUserId"), "toString should include loginUserId");
        assertTrue(result.contains("testOperatorName"), "toString should include operatorName");
        assertTrue(result.contains("testAddress"), "toString should include operatorAddress");
        assertTrue(result.contains("testOpenOperatorId"), "toString should include openOperatorId");
        assertTrue(result.contains("testGlobalOperatorId"), "toString should include globalOperatorId");
        assertTrue(result.contains("2025-08-30"), "toString should include effectiveStartDate");
        assertTrue(result.contains("2025-12-31"), "toString should include effectiveEndDate");
        assertTrue(result.contains("true"), "toString should include createPasswordFlag");
        assertTrue(result.contains("false"), "toString should include passwordTemporaryFlag");
    }

    /**
     * Test for equals and hashCode methods.
     */
    @Test
    @DisplayName("PostOperatorRequest - Equals and HashCode Test")
    void testEqualsAndHashCode() {
        // Arrange
        PostOperatorRequest request1 = new PostOperatorRequest(
                "user1", "Operator1",  "Address1",
                "OpenId1", "GlobalId1", "2025-08-30", "2025-12-31",
                "true", "false"
        );

        PostOperatorRequest request2 = new PostOperatorRequest(
                "user1", "Operator1", "Address1",
                "OpenId1", "GlobalId1", "2025-08-30", "2025-12-31",
                "true", "false"
        );

        PostOperatorRequest request3 = new PostOperatorRequest(
                "user2", "Operator2", "Address2",
                "OpenId2", "GlobalId2", "2025-09-01", "2025-12-30",
                "false", "true"
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
     * Tests whether the @JsonProperty annotation of PostOperatorRequest is set correctly.
     */
    @Test
    void testJsonPropertyNames() throws Exception {
        // Use reflection to access the fields and their annotations
        Field fieldLoginUserId = PostOperatorRequest.class.getDeclaredField("loginUserId");
        Field fieldOperatorName = PostOperatorRequest.class.getDeclaredField("operatorName");
        Field fieldOperatorAddress = PostOperatorRequest.class.getDeclaredField("operatorAddress");
        Field fieldOpenOperatorId = PostOperatorRequest.class.getDeclaredField("openOperatorId");
        Field fieldGlobalOperatorId = PostOperatorRequest.class.getDeclaredField("globalOperatorId");
        Field fieldEffectiveStartDate = PostOperatorRequest.class.getDeclaredField("effectiveStartDate");
        Field fieldEffectiveEndDate = PostOperatorRequest.class.getDeclaredField("effectiveEndDate");
        Field fieldCreatePasswordFlag = PostOperatorRequest.class.getDeclaredField("createPasswordFlag");
        Field fieldPasswordTemporaryFlag = PostOperatorRequest.class.getDeclaredField("passwordTemporaryFlag");

        // Get the @JsonProperty annotations
        JsonProperty annotationLoginUserId = fieldLoginUserId.getAnnotation(JsonProperty.class);
        JsonProperty annotationOperatorName = fieldOperatorName.getAnnotation(JsonProperty.class);
        JsonProperty annotationOperatorAddress = fieldOperatorAddress.getAnnotation(JsonProperty.class);
        JsonProperty annotationOpenOperatorId = fieldOpenOperatorId.getAnnotation(JsonProperty.class);
        JsonProperty annotationGlobalOperatorId = fieldGlobalOperatorId.getAnnotation(JsonProperty.class);
        JsonProperty annotationEffectiveStartDate = fieldEffectiveStartDate.getAnnotation(JsonProperty.class);
        JsonProperty annotationEffectiveEndDate = fieldEffectiveEndDate.getAnnotation(JsonProperty.class);
        JsonProperty annotationCreatePasswordFlag = fieldCreatePasswordFlag.getAnnotation(JsonProperty.class);
        JsonProperty annotationPasswordTemporaryFlag = fieldPasswordTemporaryFlag.getAnnotation(JsonProperty.class);

        // Assert that the annotation values match the expected JSON property names
        assertNotNull(annotationLoginUserId);
        assertNotNull(annotationOperatorName);
        assertNotNull(annotationOperatorAddress);
        assertNotNull(annotationOpenOperatorId);
        assertNotNull(annotationGlobalOperatorId);
        assertNotNull(annotationEffectiveStartDate);
        assertNotNull(annotationEffectiveEndDate);
        assertNotNull(annotationCreatePasswordFlag);
        assertNotNull(annotationPasswordTemporaryFlag);
        assertEquals(Const.JSON_PROPERTY_LOGIN_USER_ID, annotationLoginUserId.value());
        assertEquals(Const.JSON_PROPERTY_OPERATOR_NAME, annotationOperatorName.value());
        assertEquals(Const.JSON_PROPERTY_OPERATOR_ADDRESS, annotationOperatorAddress.value());
        assertEquals(Const.JSON_PROPERTY_OPEN_OPERATOR_ID, annotationOpenOperatorId.value());
        assertEquals(Const.JSON_PROPERTY_GLOBAL_OPERATOR_ID, annotationGlobalOperatorId.value());
        assertEquals(Const.JSON_PROPERTY_EFFECTIVE_START_DATE, annotationEffectiveStartDate.value());
        assertEquals(Const.JSON_PROPERTY_EFFECTIVE_END_DATE, annotationEffectiveEndDate.value());
        assertEquals(Const.JSON_PROPERTY_CREATE_PASSWORD_FLAG, annotationCreatePasswordFlag.value());
        assertEquals(Const.JSON_PROPERTY_PASSWORD_TEMPORARY_FLAG, annotationPasswordTemporaryFlag.value());
    }

    /**
     * Test JSON serialization of PostOperatorRequest.
     */
    @Test
    @DisplayName("PostOperatorRequest - JSON Serialization Test")
    void testJsonSerialization() throws Exception {
        // Arrange
        PostOperatorRequest request = new PostOperatorRequest();
        request.setLoginUserId("user123");
        request.setOperatorName("Operator Name");
        request.setOperatorAddress("123 Main St");
        request.setOpenOperatorId("open123");
        request.setGlobalOperatorId("global123");
        request.setEffectiveStartDate("2025-08-30");
        request.setEffectiveEndDate("2025-12-31");
        request.setCreatePasswordFlag("true");
        request.setPasswordTemporaryFlag("false");

        // Act
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(request);

        // Assert
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_LOGIN_USER_ID + "\":\"user123\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_OPERATOR_NAME + "\":\"Operator Name\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_OPERATOR_ADDRESS + "\":\"123 Main St\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_OPEN_OPERATOR_ID + "\":\"open123\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_GLOBAL_OPERATOR_ID + "\":\"global123\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_EFFECTIVE_START_DATE + "\":\"2025-08-30\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_EFFECTIVE_END_DATE + "\":\"2025-12-31\""));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_CREATE_PASSWORD_FLAG + "\":true"));
        assertTrue(json.contains("\"" + Const.JSON_PROPERTY_PASSWORD_TEMPORARY_FLAG + "\":false"));
    }
}