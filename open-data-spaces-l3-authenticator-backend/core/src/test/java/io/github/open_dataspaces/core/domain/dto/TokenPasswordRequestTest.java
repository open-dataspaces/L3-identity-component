/*
 * TokenPasswordRequestTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies the behavior and validation logic
 * of the TokenPasswordRequest DTO.
 *
 * Date: 2025-06-30
 */

package io.github.open_dataspaces.core.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/*
 * TokenPasswordRequestTest is a test class for the TokenPasswordRequest.
 */
class TokenPasswordRequestTest {

    private static Validator validator;

    /**
     * setUpValidator initializes the Validator instance before all tests.
     */
    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * case#1:
     * testGetterSetter tests the getter and setter methods of TokenPasswordRequest.
     */
    @Test
    void testGetterSetter() {
        TokenPasswordRequest request = new TokenPasswordRequest("wrongClientId", "wrongPassword", "wrongUserId", "wrongClientSecret");
        // Set value
        request.setLoginUserId("user1");
        request.setPassword("Password1!@#");
        request.setClientId("testClientId");
        request.setClientSecret("testClientSecret");

        // Assert
        assertEquals("user1", request.getLoginUserId());
        assertEquals("Password1!@#", request.getPassword());
        assertEquals("testClientId", request.getClientId());
        assertEquals("testClientSecret", request.getClientSecret());
    }

    /**
     * testAccountIdValidation tests the accountId validation logic
     * for various input values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
            // accountId, expectErrorMessage, isValid
            "user1,, true", // case#3: accountId is normal
            "___,, true", // case: #3: accountId contains '_' character
            "...,, true", // case#3: accountId contains '.' character
            "---,, true", // case#3: accountId contains '-' character
            "@@@,, true", // case#3: accountId contains '@' character
            "1us,,true", // case#4: accountId is min length 3 characters
            "LONG,, true", // case#5: accountId is max length 255 characters
            "'   User1   ', 'login_user_id or password: invalid value', false", // case#6: accountId includes blank spaces before and after
            "NULL, '%s: cannot be blank', false", // case#7: accountId is null
            "'', '%s: cannot be blank', false", // case#8: accountId is empty
            "'  ', '%s: cannot be blank', false", // case#9: accountId is blank spaces
            "aa, '%s: the length must be between 3 and 255', false", // case#10: accountId is less than min length 2 characters
            "OVER, '%s: the length must be between 3 and 255', false" // case#11: accountId is more than max length 256 characters
    }, nullValues = "NULL")
    void testAccountIdValidation(String accountId, String errStr, boolean isValid) {

        accountId = ("LONG".equals(accountId) ? "a".repeat(255) :
                "OVER".equals(accountId) ? "a".repeat(256) : accountId);

        TokenPasswordRequest request = new TokenPasswordRequest("wrongClientId", "wrongPassword", "wrongUserId", "wrongClientSecret");
        // Set value
        request.setClientId("valid-client-id-12345");
        request.setClientSecret("valid-client-secret-12345");
        request.setLoginUserId(accountId);
        request.setPassword("Password1!@#");

        // Assert
        Set<ConstraintViolation<TokenPasswordRequest>> violations = validator.validate(request);

        if (isValid) {
            assertTrue(violations.isEmpty(), "Expected no violations for valid accountId: " + accountId);
        } else {
            assertFalse(violations.isEmpty(), "Expected violations for invalid accountId: " + accountId);
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * testPasswordValidation tests the password validation logic
     * for various input values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
            // password, expectErrorMessage, isValid
            "Password1!@#,, true", // case#12: password is normal
            "1!Passwd,,true", // case#13: password is min length 8 characters
            "Password1234567890!@,, true", // case#14: password is max length 20 characters
            "Pass1!@#$%^&*(),, true", // case#15: password includes all special character
            "NULL, %s: cannot be blank, false", // case#16: password is null
            "'', %s: cannot be blank, false", // case#17: password is empty
            "'  ', %s: cannot be blank, false", // case#18: password is blank spaces
            "A1b!aaa, %s: the length must be between 8 and 20, false", // case#19: password is less than min length 7 characters
            "a1%AAAAAAAAAAAAAAAAAA, %s: the length must be between 8 and 20, false", // case#20: password is more than max length 21 characters
            "password1!@#, login_user_id or password: invalid value, false", // case#21: password not includes upper case letter
            "PASSWORD1!@#, login_user_id or password: invalid value, false", // case#22: password not includes lower case letter
            "Password!@#, login_user_id or password: invalid value, false", // case#23: password not includes digit
            "Password1, login_user_id or password: invalid value, false", // case#24: password not includes special character
            "Password1!@#_, login_user_id or password: invalid value, false", // case#25: password includes any other special character
            "Passwor d1!@#, login_user_id or password: invalid value, false", // case#26: password includes empty（special character）
            "'   Password1!@#   ', login_user_id or password: invalid value, false" // case#27: password includes blank spaces before and after（special character）
    }, nullValues = "NULL")
    void testPasswordValidation(String password, String errStr, boolean isValid) {
        TokenPasswordRequest request = new TokenPasswordRequest("testClientId", password, "user1", "testClientSecret");
        // Set value
        request.setClientId("testClientId");
        request.setClientSecret("testClientSecret");
        request.setLoginUserId("user1");
        request.setPassword(password); // test item

        // Assert
        Set<ConstraintViolation<TokenPasswordRequest>> violations = validator.validate(request);

        if (isValid) {
            assertTrue(violations.isEmpty(), "Expected no violations for valid password: " + password);
        } else {
            assertFalse(violations.isEmpty(), "Expected violations for invalid password: " + password);
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * testClientIdValidation tests the clientId validation logic
     * for various input values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
            // clientId, expectErrorMessage, isValid
            "User1!@#,, true", // case#28: clientId is normal
            "U,,true", // case#29: clientId is min length 1 characters
            "clientmaxt1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789,, true", // case#30: clientId is max length 239 characters
            "'   User1!@#   ',, true", // case#31: clientId includes blank spaces before and after
            "NULL, %s: cannot be blank, false", // case#32: clientId is null
            "'', %s: cannot be blank, false", // case#33: clientId is empty
            "'  ', %s: cannot be blank, false", // case#34: clientId is blank spaces
            "clientmaxt12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890, %s: the length must be between 1 and 239, false" // case#35: accountId is more than max length 240 characters

    }, nullValues = "NULL")
    void testClientIdValidation(String clientId, String errStr, boolean isValid) {
        TokenPasswordRequest request = new TokenPasswordRequest("testClientId", "testPassword", "user1", "testClientSecret");
        // Set value
        request.setClientSecret("testClientSecret");
        request.setLoginUserId("user1");
        request.setPassword("Password1!@#");
        request.setClientId(clientId);

        // Assert
        Set<ConstraintViolation<TokenPasswordRequest>> violations = validator.validate(request);

        if (isValid) {
            assertTrue(violations.isEmpty(), "Expected no violations for valid clientId: " + clientId);
        } else {
            assertFalse(violations.isEmpty(), "Expected violations for invalid clientId: " + clientId);
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }

    /**
     * testclientSecretValidation tests the clientSecret validation logic
     * for various input values using parameterized tests.
     */
    @ParameterizedTest
    @CsvSource(value = {
            // clientSecret, expectErrorMessage, isValid
            "User1!@#,, true", // case#36: clientSecret is normal
            "U,,true", // case#37: clientSecret is min length 1 characters
            "User!@#$%^&*()_-01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345255,, true", // case#38: clientSecret is max length 255 characters
            "'   User1!@#   ',, true", // case#39: clientSecret includes blank spaces before and after
            "NULL, %s: cannot be blank, false", // case#40: clientSecret is null
            "'', %s: cannot be blank, false", // case#41: clientSecret is empty
            "'  ', %s: cannot be blank, false", // case#42: clientSecret is blank spaces
            "User!@#$%^&*()_-012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456256, %s: the length must be between 1 and 255, false" // case#43: clientSecret is more than max length 256 characters

    }, nullValues = "NULL")
    void testClientSecretValidation(String clientSecret, String errStr, boolean isValid) {
        TokenPasswordRequest request = new TokenPasswordRequest("testClientId", "testPassword", "user1", "testClientSecret");
        // Set value
        request.setClientId("testClientId");
        request.setLoginUserId("user1");
        request.setPassword("Password1!@#");
        request.setClientSecret(clientSecret);

        // Assert
        Set<ConstraintViolation<TokenPasswordRequest>> violations = validator.validate(request);

        if (isValid) {
            assertTrue(violations.isEmpty(), "Expected no violations for valid clientId: " + clientSecret);
        } else {
            assertFalse(violations.isEmpty(), "Expected violations for invalid clientId: " + clientSecret);
            assertTrue(violations.stream().anyMatch(v -> errStr.equals(v.getMessage())));
        }
    }
}
