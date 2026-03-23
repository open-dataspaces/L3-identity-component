/*
 * PathParameterValidatorTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This package contains unit tests for the PathParameterValidator class.
 *
 * Date: 2025/08/31
 */

package io.github.open_dataspaces.core.application.interceptor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.exception.ValidateException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unit tests for PathParameterValidator.
 * - Covers: preHandle(), isOperatorIdValid(), getLastPathParameter()
 * - Uses parameterized tests to validate multiple URI patterns.
 */
@ExtendWith(MockitoExtension.class)
class PathParameterValidatorTest {

    @InjectMocks
    private final PathParameterValidator sut = new PathParameterValidator();

    private List<String> pathParameterOperatorIdPaths = List.of(
            "/app/{operatorId}"
    );
    private List<String> pathParameterPlantIdPaths = List.of(
            "/app/{plantId}"
    );
    private List<String> pathParameterClientUuidPaths = List.of();
    private List<String> pathParameterClientIdPaths = List.of("/client/{clientId}");
    private String testPath = "/app/";

    @BeforeEach
    void setUp() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field operatorField = PathParameterValidator.class.getDeclaredField("pathParameterOperatorIdPaths");
        operatorField.setAccessible(true);
        operatorField.set(sut, pathParameterOperatorIdPaths);

        Field plantField = PathParameterValidator.class.getDeclaredField("pathParameterPlantIdPaths");
        plantField.setAccessible(true);
        plantField.set(sut, pathParameterPlantIdPaths);

        Field clientUuidField = PathParameterValidator.class.getDeclaredField("pathParameterClientUuidPaths");
        clientUuidField.setAccessible(true);
        clientUuidField.set(sut, pathParameterClientUuidPaths);

        Field clientIdField = PathParameterValidator.class.getDeclaredField("pathParameterClientIdPaths");
        clientIdField.setAccessible(true);
        clientIdField.set(sut, pathParameterClientIdPaths);
    }

    // Valid UUID that should be accepted by UUIDUtils (typical RFC 4122 format).
    private static final String VALID_UUID_LOWER = "123e4567-e89b-12d3-a456-426614174000";
    private static final String VALID_UUID_UPPER = "123E4567-E89B-12D3-A456-426614174000";
    private static final String INVALID_UUID_SHORT = "123e4567-e89b-12d3-a456-42661417400";
    private static final String INVALID_UUID_LONG = "123e4567-e89b-12d3-a4567-42661417400";

    /**
     * Provides URIs that MATCH the target path and their expectations.
     * lastSegment is the final path component. Building URI: targetPrefix + lastSegment
     * - When last segment is a valid UUID -> preHandle returns true.
     * - When invalid/empty -> preHandle throws ValidateException.
     */
    static Stream<TestCase> matchingPathCases() {
        return Stream.of(
            // valid UUIDs (lower/upper)
            new TestCase("valid-lower", VALID_UUID_LOWER, true),
            new TestCase("valid-upper", VALID_UUID_UPPER, true),

            // invalid last segment patterns
            new TestCase("invalid-not-uuid", "not-a-uuid", false),
            new TestCase("invalid-numbers", "123456", false),

            // empty last segment (trailing slash after prefix)
            new TestCase("empty-last-segment", "", false),

            // uuid followed by trailing slash -> last segment becomes empty
            new TestCase("uuid-with-trailing-slash", VALID_UUID_LOWER + "/", false),

            // invalid UUIDs (short/long)
            new TestCase("invalid-length-short", INVALID_UUID_SHORT, false),
            new TestCase("invalid-length-long", INVALID_UUID_LONG, false)
        );
    }

    /**
     * Parameterized test for preHandle method.
     *
     * @param tc the test case containing request parameters and expected results
     * @throws Exception if an error occurs during request handling
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("matchingPathCases")
    @DisplayName("preHandle validates last segment when URI matches target path")
    void preHandle_whenUriMatches_validatesLastSegment(TestCase tc) throws Exception {
        String uri = testPath + tc.lastSegment;

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getRequestURI()).thenReturn(uri);

        if (tc.expectSuccess) {
            boolean result = assertDoesNotThrow(() -> sut.preHandle(request, response, new Object()));
            assertTrue(result, "Expected preHandle to return true for valid UUID");
        } else {
            assertThrows(ValidateException.class, () -> sut.preHandle(request, response, new Object()));
        }

        // pathParameterClientIdPaths coverage (without adding new test method)
        if ("valid-lower".equals(tc.name)) {
            HttpServletRequest clientIdValidRequest = mock(HttpServletRequest.class);
            HttpServletResponse clientIdValidResponse = mock(HttpServletResponse.class);
            when(clientIdValidRequest.getRequestURI()).thenReturn("/client/abc123xyz_1");
            boolean clientIdValidResult = assertDoesNotThrow(() -> sut.preHandle(clientIdValidRequest, clientIdValidResponse, new Object()));
            assertTrue(clientIdValidResult, "Expected preHandle to return true for valid client_id");

            HttpServletRequest clientIdTooLongRequest = mock(HttpServletRequest.class);
            HttpServletResponse clientIdTooLongResponse = mock(HttpServletResponse.class);
            String tooLongClientId = "a".repeat(Const.CLIENT_ID_LENGTH_MAX + 1);
            when(clientIdTooLongRequest.getRequestURI()).thenReturn("/client/" + tooLongClientId);
            assertThrows(ValidateException.class, () -> sut.preHandle(clientIdTooLongRequest, clientIdTooLongResponse, new Object()));

            HttpServletRequest clientIdInvalidPatternRequest = mock(HttpServletRequest.class);
            HttpServletResponse clientIdInvalidPatternResponse = mock(HttpServletResponse.class);
            when(clientIdInvalidPatternRequest.getRequestURI()).thenReturn("/client/abc.123");
            assertThrows(ValidateException.class, () -> sut.preHandle(clientIdInvalidPatternRequest, clientIdInvalidPatternResponse, new Object()));

            Method isClientIdValid = PathParameterValidator.class.getDeclaredMethod("isClientIdValid", String.class);
            isClientIdValid.setAccessible(true);
            InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> isClientIdValid.invoke(sut, ""));
            assertTrue(thrown.getCause() instanceof ValidateException, "Expected ValidateException for empty client_id");
        }
    }

    /*
     * Tests that preHandle returns true when the URI does not match the target path.
     */
    @Test
    @DisplayName("preHandle returns true without validation when URI does not match target path")
    void preHandle_whenUriDoesNotMatchTarget_returnsTrue() throws Exception {
        // Build an unrelated URI to skip validation branch
        String unrelatedUri = "/unrelated/path/" + VALID_UUID_LOWER;

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getRequestURI()).thenReturn(unrelatedUri);

        boolean result = sut.preHandle(request, response, new Object());
        assertTrue(result, "Expected preHandle to return true for non-matching path (no validation)");
    }

    /**
     * Simple holder for parameterized test data.
     */
    private static class TestCase {
        final String name;
        final String lastSegment;
        final boolean expectSuccess;

        TestCase(String name, String lastSegment, boolean expectSuccess) {
            this.name = name;
            this.lastSegment = lastSegment;
            this.expectSuccess = expectSuccess;
        }

        @Override
        public String toString() {
            return name + " -> lastSegment='" + lastSegment + "', expectSuccess=" + expectSuccess;
        }
    }
}