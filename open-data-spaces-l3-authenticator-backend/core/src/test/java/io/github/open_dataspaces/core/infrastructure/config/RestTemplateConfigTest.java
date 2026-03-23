/*
 * RestTemplateConfigTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the RestTemplateConfig class.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * Unit tests for the {@link RestTemplateConfig} class.
 */
@ExtendWith(MockitoExtension.class)
public class RestTemplateConfigTest {

    @InjectMocks
    private RestTemplateConfig restTemplateConfig;

    private RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();

    /**
     * Test for restTemplate bean creation.
     */
    @Test
    @DisplayName("Test restTemplate bean creation")
    void testRestTemplate_creation() {
        // Act
        RestTemplate restTemplate = restTemplateConfig.restTemplate(restTemplateBuilder);

        // Assert
        assertNotNull(restTemplate, "RestTemplate should be created");

        // Verify that the default error handler is set
        ResponseErrorHandler handler = restTemplate.getErrorHandler();
        assertNotNull(handler, "ResponseErrorHandler should be set on RestTemplate");
        assertTrue(handler instanceof DefaultResponseErrorHandler,
                "ResponseErrorHandler should be an instance of DefaultResponseErrorHandler");
    }

    /**
     * Test for openFgaRestTemplate bean creation.
     */
    @Test
    @DisplayName("Test openFgaRestTemplate bean creation")
    void testOpenFgaRestTemplate_creation() {
        // Act
        RestTemplate restTemplate = restTemplateConfig.openFgaRestTemplate(restTemplateBuilder);

        // Assert
        assertNotNull(restTemplate, "RestTemplate should be created");
        ResponseErrorHandler handler = restTemplate.getErrorHandler();
        assertNotNull(handler, "ResponseErrorHandler should be set on RestTemplate");
    }

    /**
     * Test for openFgaRestTemplate error handler methods are no-ops.
     */
    @Test
    @DisplayName("Test openFgaRestTemplate error handler methods are no-ops")
    void testOpenFgaRestTemplate_errorHandler_methodsAreNoOp() throws Exception {
        // Act
        RestTemplate restTemplate = restTemplateConfig.openFgaRestTemplate(restTemplateBuilder);
        ResponseErrorHandler handler = restTemplate.getErrorHandler();

        // mock response to exercise hasError and handleError paths
        ClientHttpResponse mockResponse = Mockito.mock(ClientHttpResponse.class);

        assertNotNull(restTemplate, "RestTemplate should be created");
        assertNotNull(handler, "ResponseErrorHandler should be set on RestTemplate");

        // hasError should always return false
        assertFalse(handler.hasError(mockResponse), "hasError should always return false");

        // handleError should be a no-op (should not throw)
        handler.handleError(URI.create("http://example.local"), HttpMethod.GET, mockResponse);
    }
}
