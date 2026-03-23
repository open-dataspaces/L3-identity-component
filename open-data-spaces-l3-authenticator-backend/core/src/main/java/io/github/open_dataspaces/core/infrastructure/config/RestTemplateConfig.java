/*
 * RestTemplateConfig.java
 *
 * Copyright (c) 2025 NTT DATA Group All rights reserved.
 *
 * Configuration class for RestTemplate beans.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.infrastructure.config;

import java.io.IOException;
import java.net.URI;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for RestTemplate beans.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a default RestTemplate bean.
     *
     * @return a new RestTemplate instance
     */
    @Bean
    @Primary
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder.build();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
        return restTemplate;
    }

    /**
     * Creates and configures a RestTemplate bean for OpenFGA communication.
     *
     * @param builder the RestTemplateBuilder
     * @return the configured RestTemplate
     */
    @Bean
    public RestTemplate openFgaRestTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder.build();

        // Configure RestTemplate to not throw exceptions on HTTP error codes
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                // Treat all responses as non-errors to handle them manually
                return false;
            }

            @Override
            public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
                // no-op: we handle status codes explicitly after exchange()
            }
        });
        return restTemplate;
    }
}
