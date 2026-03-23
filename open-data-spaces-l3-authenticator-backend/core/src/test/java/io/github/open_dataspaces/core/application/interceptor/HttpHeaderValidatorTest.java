/*
 * HttpHeaderValidatorTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class tests the HttpHeaderValidator functionality,
 *
 * Date: 2025/09/30
 */

package io.github.open_dataspaces.core.application.interceptor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import io.github.open_dataspaces.core.common.consts.ConstError;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unit tests for HttpHeaderValidator.
 * - Covers: preHandle()
 * - Uses parameterized tests to validate multiple Content-Type headers.
 */
public class HttpHeaderValidatorTest {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private final HttpHeaderValidator sut = new HttpHeaderValidator();

    /**
     * Sets up the test environment before each test.
     */
    @BeforeEach
    void setUp() {
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
    }

    /**
     * Tests that preHandle returns true for valid Content-Type headers.
     */
    @Test
    void testPreHandle_applicationJson() throws Exception {
        Mockito.when(request.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(MediaType.APPLICATION_JSON_VALUE);
        Mockito.when(request.getContentLength()).thenReturn(1);
        boolean result = sut.preHandle(request, response, new Object());
        assertTrue(result);
    }

    /**
     * Tests that preHandle returns true for valid Content-Type headers with charset.
     */
    @Test
    void testPreHandle_applicationJsonWithCharset() throws Exception {
        Mockito.when(request.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn("application/json;charset=UTF-8");
        Mockito.when(request.getContentLength()).thenReturn(1);
        boolean result = sut.preHandle(request, response, new Object());
        assertTrue(result);
    }

    /**
     * Tests that preHandle returns true when request body is empty.
     * In this case, Content-Type validation is skipped.
     */
    @Test
    void testPreHandle_emptyRequestBody() throws Exception {
        Mockito.when(request.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(null);
        Mockito.when(request.getContentLength()).thenReturn(0);
        boolean result = sut.preHandle(request, response, new Object());
        assertTrue(result);
    }

    /**
     * Tests that preHandle throws HttpMediaTypeNotSupportedException for null Content-Type header.
     */
    @Test
    void testPreHandle_nullContentType() throws Exception {
        Mockito.when(request.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(null);
        Mockito.when(request.getContentLength()).thenReturn(1);
        HttpMediaTypeNotSupportedException exception = assertThrows(HttpMediaTypeNotSupportedException.class, () -> {
            sut.preHandle(request, response, new Object());
        });
        assertEquals(ConstError.ERR_415_UNSUPPORTED_MEDIA_TYPE_REQUIRED, exception.getMessage());
    }

    /**
     * Tests that preHandle throws HttpMediaTypeNotSupportedException for unsupported Content-Type headers without charset.
     */
    @ParameterizedTest
    @CsvSource({
        "text/plain, " + "Content-Type 'text/plain;charset=UTF-8' is not supported.",
        "application/xml, " + "Content-Type 'application/xml;charset=UTF-8' is not supported.",
        "application/javascript, " + "Content-Type 'application/javascript;charset=UTF-8' is not supported.",
        "text/html, " + "Content-Type 'text/html;charset=UTF-8' is not supported.",
        "application/x-yaml, " + "Content-Type 'application/x-yaml;charset=UTF-8' is not supported.",
        "application/xhtml+xml, " + "Content-Type 'application/xhtml+xml;charset=UTF-8' is not supported.",
        "text/xml, " + "Content-Type 'text/xml;charset=UTF-8' is not supported.",
        "text/xml;charset=UTF-8, " + "Content-Type 'text/xml;charset=UTF-8' is not supported."
    })
    void testPreHandle_notApplicationJson(String contentType, String expectedErrorMessage) throws Exception {
        Mockito.when(request.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(contentType);
        Mockito.when(request.getContentLength()).thenReturn(1);
        HttpMediaTypeNotSupportedException exception = assertThrows(HttpMediaTypeNotSupportedException.class, () -> {
            sut.preHandle(request, response, new Object());
        });
        assertEquals(expectedErrorMessage, exception.getMessage());
    }

}
