/*
 * ApplicationTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the Application class.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link Application} class.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ApplicationTest {

    /**
     * (Case#1) Tests if the main method runs without throwing exceptions.
     */
    @Test
    void contextLoads() {
        // Empty test to verify that the Spring application context loads successfully.
    }

    /**
     * (Case#2) Tests if the Application class is annotated with @SpringBootApplication.
     */
    @Test
    void applicationClassHasSpringBootApplicationAnnotation() {
        assertTrue(Application.class.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class));
    }
}