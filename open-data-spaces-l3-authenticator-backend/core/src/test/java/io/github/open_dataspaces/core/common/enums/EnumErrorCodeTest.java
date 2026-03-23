/*
 * EnumErrorCodeTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the EnumErrorCode class.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.common.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link EnumErrorCode} class.
 */
class EnumErrorCodeTest {

    /**
     * (Case#1) Tests the getStatusCode method.
     */
    @Test
    void testGetStatusCode() {
        assertEquals(200, EnumErrorCode.ErrorCodes.SUCCESS.getStatusCode(), "SUCCESS should return status code 200");
        assertEquals(400, EnumErrorCode.ErrorCodes._400.getStatusCode(), "_400 should return status code 400");
        assertEquals(401, EnumErrorCode.ErrorCodes._401.getStatusCode(), "_401 should return status code 401");
        assertEquals(403, EnumErrorCode.ErrorCodes._403.getStatusCode(), "_403 should return status code 403");
        assertEquals(404, EnumErrorCode.ErrorCodes._404.getStatusCode(), "_404 should return status code 404");
        assertEquals(409, EnumErrorCode.ErrorCodes._409.getStatusCode(), "_409 should return status code 409");
        assertEquals(500, EnumErrorCode.ErrorCodes._500.getStatusCode(), "_500 should return status code 500");
        assertEquals(503, EnumErrorCode.ErrorCodes._503.getStatusCode(), "_503 should return status code 503");
    }

    /**
     * (Case#2) Tests the getErrorCode method.
     */
    @Test
    void testGetErrorCode() {
        assertEquals("OK", EnumErrorCode.ErrorCodes.SUCCESS.getErrorCode(), "SUCCESS should return error code 'OK'");
        assertEquals("BadRequest", EnumErrorCode.ErrorCodes._400.getErrorCode(), "_400 should return error code 'BadRequest'");
        assertEquals("Unauthorized", EnumErrorCode.ErrorCodes._401.getErrorCode(), "_401 should return error code 'Unauthorized'");
        assertEquals("AccessDenied", EnumErrorCode.ErrorCodes._403.getErrorCode(), "_403 should return error code 'AccessDenied'");
        assertEquals("NotFound", EnumErrorCode.ErrorCodes._404.getErrorCode(), "_404 should return error code 'NotFound'");
        assertEquals("Conflict", EnumErrorCode.ErrorCodes._409.getErrorCode(), "_409 should return error code 'Conflict'");
        assertEquals("InternalServerError", EnumErrorCode.ErrorCodes._500.getErrorCode(), "_500 should return error code 'InternalServerError'");
        assertEquals("ServiceUnavailable", EnumErrorCode.ErrorCodes._503.getErrorCode(), "_503 should return error code 'ServiceUnavailable'");
    }

    /**
     * (Case#3) Tests the getErrorCode method with valid status codes.
     */
    @Test
    void testGetErrorCode_withValidStatusCodes() {
        assertEquals("OK", EnumErrorCode.getErrorCode(200), "Status code 200 should return 'OK'");
        assertEquals("BadRequest", EnumErrorCode.getErrorCode(400), "Status code 400 should return 'BadRequest'");
        assertEquals("Unauthorized", EnumErrorCode.getErrorCode(401), "Status code 401 should return 'Unauthorized'");
        assertEquals("AccessDenied", EnumErrorCode.getErrorCode(403), "Status code 403 should return 'AccessDenied'");
        assertEquals("NotFound", EnumErrorCode.getErrorCode(404), "Status code 404 should return 'NotFound'");
        assertEquals("Conflict", EnumErrorCode.getErrorCode(409), "Status code 409 should return 'Conflict'");
        assertEquals("InternalServerError", EnumErrorCode.getErrorCode(500), "Status code 500 should return 'InternalServerError'");
        assertEquals("ServiceUnavailable", EnumErrorCode.getErrorCode(503), "Status code 503 should return 'ServiceUnavailable'");
    }

    /**
     * (Case#4) Tests the getErrorCode method with an invalid status code.
     */
    @Test
    void testGetErrorCode_withInvalidStatusCode() {
        assertEquals("InternalServerError", EnumErrorCode.getErrorCode(999));
        assertEquals("InternalServerError", EnumErrorCode.getErrorCode(-1));
        assertEquals("InternalServerError", EnumErrorCode.getErrorCode(0));
    }

    /**
     * (Case#5) Tests that the constructor of EnumErrorCode is private.
     */
    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<EnumErrorCode> constructor = EnumErrorCode.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(exception.getCause() instanceof UnsupportedOperationException, "Cause should be UnsupportedOperationException");
        assertEquals("Utility class cannot be instantiated.", exception.getCause().getMessage());
    }
}