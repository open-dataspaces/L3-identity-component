/*
 * DataSourceConfig.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This configuration class sets up the application's primary DataSource bean, customizing the
 * connection URL and SSL parameters based on the environment.
 *
 * Date: 2025-06-30
 */

package io.github.open_dataspaces.core.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import io.github.open_dataspaces.core.common.consts.Const;

import org.springframework.boot.jdbc.DataSourceBuilder;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

/**
 * Configuration class for setting up the application's DataSource. Adjusts the database connection
 * URL and SSL parameters depending on the environment.
 */
@Configuration
public class DataSourceConfig {

    private final ODSProperties odsProperties;

    private final SpringProperties springProperties;

    /**
     * Constructor for DataSourceConfig.
     */
    public DataSourceConfig(ODSProperties odsProperties, SpringProperties springProperties) {
        this.odsProperties = odsProperties;
        this.springProperties = springProperties;
    }

    /**
     * Creates and configures the primary DataSource bean. If the environment is not local, SSL
     * parameters are appended to the database URL.
     *
     * @return the configured DataSource
     */
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {

        String url = springProperties.getDatasource().getUrl();

        List<String> dbParameter = new ArrayList<>();
        // Append SSL parameters if SSL is enabled
        if (odsProperties.isDatabaseSsl()) {
            dbParameter.add(String.format(Const.DB_PARAM_POSTGRES_SSL, odsProperties.isDatabaseSsl()));
        }
        // Append SSL mode if specified
        if (StringUtils.hasText(odsProperties.getDatabaseSslMode())) {
            dbParameter.add(String.format(Const.DB_PARAM_POSTGRES_SSL_MODE, odsProperties.getDatabaseSslMode()));
        }
        // Append root cert if specified
        if (StringUtils.hasText(odsProperties.getDatabaseSslRootCert())) {
            dbParameter.add(String.format(Const.DB_PARAM_POSTGRES_ROOT_CERT, odsProperties.getDatabaseSslRootCert()));
        }
        // Append client cert if specified
        if (StringUtils.hasText(odsProperties.getDatabaseSslCert())) {
            dbParameter.add(String.format(Const.DB_PARAM_POSTGRES_CERT, odsProperties.getDatabaseSslCert()));
        }
        // Append client key if specified
        if (StringUtils.hasText(odsProperties.getDatabaseSslKey())) {
            dbParameter.add(String.format(Const.DB_PARAM_POSTGRES_KEY, odsProperties.getDatabaseSslKey()));
        }
        // Construct final URL with parameters
        if (dbParameter.size() > 0) {
            String paramString = String.join("&", dbParameter);
            url = String.format("%s?%s", url, paramString);
        }

        return DataSourceBuilder.create()
                .url(url)
                .username(springProperties.getDatasource().getUsername())
                .password(springProperties.getDatasource().getPassword())
                .driverClassName(springProperties.getDatasource().getDriverClassName())
                .build();
    }
}
