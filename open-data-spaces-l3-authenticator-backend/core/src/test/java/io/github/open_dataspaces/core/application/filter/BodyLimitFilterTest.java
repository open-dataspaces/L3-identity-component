/*
 * BodyLimitFilterTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the BodyLimitFilter, which restricts the maximum size of HTTP request bodies.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.application.filter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.UnexpectedException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

/**
 * Unit tests for the BodyLimitFilter.
 */
public class BodyLimitFilterTest {

    private BodyLimitFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    /**
     * Sets up the test environment before each test case.
     */
    @BeforeEach
    void setUp() {
        filter = new BodyLimitFilter(1024); // Set limit to 1024 bytes
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = Mockito.mock(FilterChain.class);
    }

    /**
     * (Case#1) Tests that a request with content length within.
     *
     * @throws ServletException General servlet exception throws by Mockito.verify
     * @throws IOException General IO exception throws by Mockito.verify
     */
    @Test
    void testRequestWithinLimit() throws ServletException, IOException {
        String content = "A".repeat(1024);
        request.setContent(content.getBytes()); // Content length within limit

        assertDoesNotThrow(() -> filter.doFilterInternal(request, response, filterChain));
        Mockito.verify(filterChain).doFilter(request, response);
    }

    /**
     * (Case#2) Tests that a request with content length exceeding the limit.
     */
    @Test
    void testRequestExceedsLimit() {
        String content = "A".repeat(1025);
        request.setContent(content.getBytes()); // Content length exceeds limit

        BadParametersException exception = assertThrows(BadParametersException.class, () -> {
            filter.doFilterInternal(request, response, filterChain);
        });

        assertEquals(ConstError.ERR_400_REQUEST_TOO_LARGE, exception.getResponseMessage());
        assertEquals(ConstError.ERR_400_REQUEST_TOO_LARGE, exception.getLogMessage());
        assertEquals(exception.getSource(), Const.SOURCE_AUTH);
    }

    /**
     * (Case#3) Tests that an unexpected exception in the filter chain.
     *
     * @throws ServletException General servlet exception throws by Mockito.doThrow
     * @throws IOException General IO exceptionthrows by Mockito.doThrow
     */
    @Test
    void testUnexpectedExceptionInFilterChain() throws ServletException, IOException {
        String content = "A".repeat(512);
        request.setContent(content.getBytes()); // Content length within limit
        Mockito.doThrow(new IOException("Test IOException")).when(filterChain).doFilter(request, response);

        UnexpectedException exception = assertThrows(UnexpectedException.class, () -> {
            filter.doFilterInternal(request, response, filterChain);
        });

        assertTrue(exception.getLogMessage().contains(ConstError.ERR_500_FILTER_CHAIN_ERROR));
        assertEquals(exception.getResponseMessage(), "Test IOException");
        assertEquals(exception.getSource(), Const.SOURCE_AUTH);
    }
}