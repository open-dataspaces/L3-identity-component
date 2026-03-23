/*
 * SpringProperties.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This configuration class defines Spring profile properties.
 *
 * Date: 2025-10-30
 */

package io.github.open_dataspaces.core.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * SpringProfilesConfig is a configuration class for managing Spring profiles.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring")
public class SpringProperties {

    private Profiles profiles = new Profiles();
    private Datasource datasource = new Datasource();

    /**
     * Profiles inner class to hold active profile information.
     */
    @Getter
    @Setter
    public static class Profiles {
        // spring.profiles.active
        private String active;
    }

    /**
     * Datasource inner class to hold datasource configuration properties.
     */
    @Getter
    @Setter
    public static class Datasource {
        // spring.datasource.url
        private String url;

        // spring.datasource.username
        private String username;

        // spring.datasource.password
        private String password;

        // spring.datasource.driver-class-name
        private String driverClassName;
    }
}
