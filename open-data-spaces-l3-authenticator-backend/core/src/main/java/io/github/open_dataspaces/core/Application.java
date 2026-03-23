/*
 * Application.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This file defines the entry point for the Spring Boot application.
 */

package io.github.open_dataspaces.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.github.open_dataspaces.core.common.consts.Const;

/**
 * Main application class for the User Authentication system.
 *
 * <p>This class serves as the entry point for starting the Spring Boot application.</p>
 */
@SpringBootApplication
@EnableJpaRepositories(Const.REPOSITORY_PACKAGE)
public class Application {

    /**
     * Main method to launch the Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
