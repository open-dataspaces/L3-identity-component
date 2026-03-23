/*
 * AuthDumpFilterTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class tests the AuthDumpFilter functionality, ensuring that request and response bodies.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.application.filter;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.slf4j.LoggerFactory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.open_dataspaces.core.common.consts.Const;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * AuthDumpFilterTest.java
 *
 * <p>AuthDumpFilterTest is a test class for the AuthDumpFilter.</p>
 */
@SpringBootTest
public class AuthDumpFilterTest {

    private AuthDumpFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    private ListAppender<ILoggingEvent> listAppender;

    /**
     * Sets up the test environment by initializing a ListAppender.
     */
    @BeforeEach
    void setUp() {
        filter = new AuthDumpFilter();
        request = mock(ContentCachingRequestWrapper.class);
        response = mock(ContentCachingResponseWrapper.class);
        filterChain = mock(FilterChain.class);

        listAppender = new ListAppender<>();
        listAppender.start();
        Logger rootLogger = (Logger) LoggerFactory.getLogger(AuthDumpFilter.class);
        rootLogger.addAppender(listAppender);
    }

    @Test
    void doFilterInternal_logsMaskedRequestAndResponse() throws ServletException, IOException {
        // Arrange
        String reqJson = "{\"client_id\":\"client-id-123\","
                + "\"client_secret\":\"client-secret-123\"}";
        String resJson = "{\"type\":\"success\","
                + "\"title\":\"title-123\","
                + "\"status\": 200,"
                + "\"detail\":\"detail-123\","
                + "\"data\": {"
                + "\"client_secret\":\"client-secret-123\","
                + "\"client_id\":\"client-id-123\"}"
                + "}";

        when(((ContentCachingRequestWrapper) request).getContentAsByteArray()).thenReturn(reqJson.getBytes());
        when(((ContentCachingRequestWrapper) request).getCharacterEncoding()).thenReturn("UTF-8");
        when(((ContentCachingResponseWrapper) response).getContentAsByteArray()).thenReturn(resJson.getBytes());
        when(((ContentCachingResponseWrapper) response).getCharacterEncoding()).thenReturn("UTF-8");
        when(((ContentCachingResponseWrapper) response).getStatus()).thenReturn(HttpServletResponse.SC_OK);

        when(request.getRequestURI()).thenReturn("/api/path");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn("api-key-123");

        // Act & Assert
        assertDoesNotThrow(() -> filter.doFilterInternal(request, response, filterChain));

        // Assert: Verify that masking is applied
        boolean masked = listAppender.list.stream()
                .anyMatch(event -> event.getFormattedMessage().contains(Const.MASK));
        assertTrue(masked, "client_secret and similar fields should be masked");

        // Assert: Verify that the client_id is not masked
        boolean notMasked = listAppender.list.stream()
                .anyMatch(event -> event.getFormattedMessage().contains("client-id-123"));
        assertTrue(notMasked, "To confirm that masking is not applied, client_id should not be masked");

        // Assert: Verify that the raw value is not included in the log
        boolean rawFound = listAppender.list.stream()
                .anyMatch(event -> event.getFormattedMessage().contains("client-secret-123"));
        assertTrue(!rawFound, "The raw client-secret-123 should not be included in the log");
    }

    @Test
    void doFilterInternal_handlesJsonProcessingException() throws Exception {
        // Arrange
        String reqJson = "{\"client_id\":\"client-id-123\","
                + "\"client_secret\":\"client-secret-123\"}";
        String resJson = "{\"type\":\"success\","
                + "\"title\":\"title-123\","
                + "\"status\": 200,"
                + "\"detail\":\"detail-123\","
                + "\"data\": {"
                + "\"access_token\":\"access-token-123\","
                + "\"client_id\":\"client-id-123\"}"
                + "}";
        when(((ContentCachingRequestWrapper) request).getContentAsByteArray()).thenReturn(reqJson.getBytes());
        when(((ContentCachingRequestWrapper) request).getCharacterEncoding()).thenReturn("UTF-8");
        when(((ContentCachingResponseWrapper) response).getContentAsByteArray()).thenReturn(resJson.getBytes());
        when(((ContentCachingResponseWrapper) response).getCharacterEncoding()).thenReturn("UTF-8");
        when(((ContentCachingResponseWrapper) response).getStatus()).thenReturn(HttpServletResponse.SC_OK);
        when(request.getRequestURI()).thenReturn("/api/token");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn("api-key-123");

        // Just verify that no exception is thrown here
        assertDoesNotThrow(() -> filter.doFilterInternal(request, response, filterChain));
    }

    @Test
    void doFilterInternal_handlesJsonProcessingException_logsWarn() throws Exception {
        // Arrange
        String reqJson = "{\"unserializable\":}";
        String resJson = "{\"unserializable\":}";

        when(((ContentCachingRequestWrapper) request).getContentAsByteArray()).thenReturn(reqJson.getBytes());
        when(((ContentCachingRequestWrapper) request).getCharacterEncoding()).thenReturn("UTF-8");
        when(((ContentCachingResponseWrapper) response).getContentAsByteArray()).thenReturn(resJson.getBytes());
        when(((ContentCachingResponseWrapper) response).getCharacterEncoding()).thenReturn("UTF-8");
        when(((ContentCachingResponseWrapper) response).getStatus()).thenReturn(HttpServletResponse.SC_OK);
        when(request.getRequestURI()).thenReturn("/api/token");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader(Const.HEADER_API_KEY)).thenReturn("api-key-123");

        try (MockedConstruction<ObjectMapper> mocked = mockConstruction(ObjectMapper.class,
                (mock, context) -> {
                    when(mock.writeValueAsString(any())).thenThrow(new JsonProcessingException("forced error") {});
                })) {
            // Act
            assertDoesNotThrow(() -> filter.doFilterInternal(request, response, filterChain));

            StringBuilder sb = new StringBuilder();
            listAppender.list.stream()
                    .map(event -> event.getFormattedMessage())
                    .forEach(sb::append);
            System.out.println(sb.toString());

            // Assert: A WARN log is output
            boolean warnLogged = listAppender.list.stream()
                    .anyMatch(event -> event.getLevel().toString().equals("WARN"));
            assertTrue(warnLogged, "A WARN log should be output when a JsonProcessingException occurs");
        }

    }
}
