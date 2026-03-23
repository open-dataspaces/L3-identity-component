/*
 * BodyDumpFilterTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class tests the BodyDumpFilter functionality, ensuring that request and response bodies.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.application.filter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.LoggerFactory;

/**
 * BodyDumpFilterTest.java
 *
 * <p>BodyDumpFilterTest is a test class for the BodyDumpFilter.</p>
 */
@SpringBootTest
public class BodyDumpFilterTest {

    /*
     * CustomFilterChain is a mock implementation of FilterChain
     */
    class CustomFilterChain implements FilterChain {

        private final String responseBody;

        public CustomFilterChain(String responseBody) {
            this.responseBody = responseBody;
        }

        /**
         * doFilter is a mock implementation of the doFilter method
         * that writes a predefined response body.
         *
         * @param req the ServletRequest
         * @param res the ServletResponse
         * @throws IOException if an I/O error occurs
         * @throws ServletException if a servlet error occurs
         */
        @Override
        public void doFilter(ServletRequest req, ServletResponse res) throws IOException {
            res.setContentType("application/json");
            res.getWriter().write(responseBody);
        }
    }

    // Constants for the test
    private final String requestUri = "/api/unit/test";
    private final String expectedInfoLogSubstring = "Request Body: ******, Response Body: ******";
    private final String expectedHeaderString = "Content-Type: application/json;charset=UTF-8;";

    private final Logger logger = (Logger) LoggerFactory.getLogger(BodyDumpFilter.class);
    private ListAppender<ILoggingEvent> listAppender;

    /**
     * Sets up the test environment by initializing a ListAppender.
     */
    @BeforeEach
    void setUp() {
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        logger.setLevel(Level.DEBUG);
    }

    /**
     * Cleans up the test environment by detaching and stopping the ListAppender.
     */
    @AfterEach
    private void cleanup() {
        if (listAppender != null) {
            logger.detachAppender(listAppender);
            listAppender.stop();
        }
        listAppender = null;

    }

    /**
     * postRequest is a helper method that simulates a POST request.
     *
     * @param req the request body as a String
     * @param res the expected response body as a String
     * @param caseNo the case number
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    private void postRequest(String req, String res, int caseNo) throws ServletException, IOException {
        BodyDumpFilter filter = new BodyDumpFilter();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(requestUri);
        request.setMethod("POST");
        request.setContent(req.getBytes(StandardCharsets.UTF_8));
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        request.setContentType("application/json;charset=UTF-8");
        request.addHeader("authorization", "this-is-secret");

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setCharacterEncoding(StandardCharsets.UTF_8.name()); // Ensure response writer uses UTF-8

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        // Use CustomFilterChain from the outer class, assuming it's defined there
        FilterChain filterChain = new CustomFilterChain(res);

        // Act
        // Reading the input stream is necessary to populate the ContentCachingRequestWrapper's cache
        if (caseNo == 2) {
            // Case 2: Empty request body, ensure we read the input stream to cache it
            wrappedRequest.getInputStream().readAllBytes();
        }
        filter.doFilter(wrappedRequest, wrappedResponse, filterChain);
        wrappedResponse.copyBodyToResponse(); // Ensure response is written to the actual response
    }

    /**
     * Tests BodyDumpFilter with a generic API path, ensuring both INFO (masked) and DEBUG (full)
     * logs are generated correctly for request and response bodies.
     *
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @ParameterizedTest
    @CsvSource(
            // requestBody, responseBody
            {"1, {\"requestKey\":\"requestValue\"}, {\"responseKey\":\"responseValue\"}",  // case#1 Generic
            "2, \"\", {\"responseKey\":\"responseValue\"}", // case#2 Empty request body
            "3, {\"requestKey\":\"requestValue\"}, \"\"", // case#3 Empty response body
            "4, \"\", \"\"" }) // case#4 Both request and response bodies are empty
    void doFilterInternalTest(int caseNo, String requestBody, String responseBody) throws ServletException, IOException {
        // Arrange & Act
        postRequest(requestBody, responseBody, caseNo);

        // Assert
        // Check INFO log (masked bodies)
        // This assertion assumes Const.MASK is "******" and the log format includes "Request Body: %s, Response Body: %s\""
        boolean infoLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.INFO)
                .anyMatch(event -> event.getFormattedMessage().contains("Auth Access Path: " + requestUri)
                && event.getFormattedMessage().contains(expectedHeaderString)
                && event.getFormattedMessage().contains(expectedInfoLogSubstring));
        assertTrue(infoLogFound, "Masked request and response bodies should be logged at INFO level. Logged messages: " + listAppender.list);

        // Check DEBUG log (full bodies)
        // This assertion assumes the log format includes "Request Body: %s, Response Body: %s\""
        final String expectedDebugLogSubstring = "Request Body: " + requestBody + ", Response Body: " + responseBody;
        boolean debugLogFound = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.DEBUG)
                .anyMatch(event -> event.getFormattedMessage().contains("Auth Access Path: " + requestUri)
                && event.getFormattedMessage().contains(expectedHeaderString)
                && event.getFormattedMessage().contains(expectedDebugLogSubstring));
        assertTrue(debugLogFound, "Actual request and response bodies should be logged at DEBUG level. Logged messages: " + listAppender.list);

        long logInfoCount = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.INFO).count();
        long logDebugCount = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.DEBUG).count();

        // Set logger level to INFO for subsequent tests
        logger.setLevel(Level.INFO);

        // Re-run the postRequest to ensure it works with INFO level logging
        postRequest(requestBody, responseBody, caseNo);

        // Ensure no additional DEBUG logs were added
        debugLogFound = listAppender.list.stream()
        .filter(event -> event.getLevel() == Level.DEBUG)
        .count() == logDebugCount;
        assertTrue(debugLogFound, "Wrong DEBUG logged messages");

        // Ensure one additional INFO log was added
        debugLogFound = listAppender.list.stream()
        .filter(event -> event.getLevel() == Level.INFO)
        .count() == logInfoCount + 1;;
        assertTrue(debugLogFound, "Wrong INFO logged messages");
        ++logInfoCount;

        // Set logger level to OFF for subsequent tests
        logger.setLevel(Level.OFF);

        // Re-run the postRequest to ensure it works with OFF level logging
        postRequest(requestBody, responseBody, caseNo);

        // Ensure no additional DEBUG logs were added
        debugLogFound = listAppender.list.stream()
        .filter(event -> event.getLevel() == Level.DEBUG)
        .count() == logDebugCount;
        assertTrue(debugLogFound, "Wrong DEBUG logged messages");

        // Ensure no additional INFO log was added
        debugLogFound = listAppender.list.stream()
        .filter(event -> event.getLevel() == Level.INFO)
        .count() == logInfoCount;
        assertTrue(debugLogFound, "Wrong DEBUG logged messages");
    }
}
