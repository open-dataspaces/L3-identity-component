/*
 * PathListConfig.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This configuration class sets up web-related beans, filters, and interceptors for the
 * application's API security, request logging, and request validation.
 *
 * Date: 2025-06-30
 */

package io.github.open_dataspaces.core.application.config;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.open_dataspaces.core.common.consts.ConstPath;

/**
 * Configuration class for defining path lists used in interceptors.
 */
@Configuration
public class PathListConfig {

    /**
     * API Key path List.
     */
    @Bean
    public List<String> apiKeyPaths() {
        return new CopyOnWriteArrayList<>(List.of(
            String.join("", ConstPath.AUTH_PATH, ConstPath.ALL_PATH_PATTERN_INTERCEPTOR),
            String.join("", ConstPath.ACCOUNT_PATH, ConstPath.ALL_PATH_PATTERN_INTERCEPTOR),
            String.join("", ConstPath.AUTHORIZATION_PATH, ConstPath.ALL_PATH_PATTERN_INTERCEPTOR)
        ));
    }

    /**
     * Sets the paths for the token validator interceptor.
     */
    @Bean
    public List<String> tokenPaths() {
        return new CopyOnWriteArrayList<>(List.of(
            String.join("", ConstPath.AUTH_PATH, ConstPath.AUTH_PASSWORD_SHORT),
            String.join("", ConstPath.AUTH_PATH, ConstPath.AUTH_CLIENTS_PATH_SHORT),
            String.join("", ConstPath.AUTH_PATH, ConstPath.AUTH_CLIENTS_ID_PATH_SHORT),
            String.join("", ConstPath.AUTH_PATH, ConstPath.AUTH_CLIENTS_SECRET_PATH_SHORT),
            String.join("", ConstPath.ACCOUNT_PATH, ConstPath.ALL_PATH_PATTERN_INTERCEPTOR),
            String.join("", ConstPath.AUTHORIZATION_PATH, ConstPath.ALL_PATH_PATTERN_INTERCEPTOR)
        ));
    }

    /**
     * Sets the paths to exclude for the token validator interceptor.
     */
    @Bean
    public List<String> tokenExcludePaths() {
        return new CopyOnWriteArrayList<>(List.of(
            String.join("", ConstPath.AUTH_PATH, ConstPath.AUTH_PASSWORD_URL_PATH_SHORT)    // Exclude to avoid conflict with AUTH_PASSWORD_SHORT
        ));
    }

    /**
     * Sets the paths for the path parameter operator ID validator interceptor.
     */
    @Bean
    public List<String> pathParameterOperatorIdPaths() {
        return new CopyOnWriteArrayList<>(List.of(
            String.join("", ConstPath.AUTH_PATH, ConstPath.AUTH_PASSWORD_SHORT),
            String.join("", ConstPath.ACCOUNT_PATH, ConstPath.ACCOUNT_OPERATOR_PATH_WITH_ID_SHORT),
            String.join("", ConstPath.ACCOUNT_PATH, ConstPath.ACCOUNT_OPERATOR_STATUS_PATH_WITH_ID_SHORT)
        ));
    }

    /**
     * Sets the paths to exclude for the path parameter operator ID validator interceptor.
     */
    @Bean
    public List<String> pathParameterOperatorIdExcludePaths() {
        return new CopyOnWriteArrayList<>(List.of(
            String.join("", ConstPath.AUTH_PATH, ConstPath.AUTH_PASSWORD_URL_PATH_SHORT),    // Exclude to avoid conflict with /auth/password/{operator_id}
            String.join("", ConstPath.ACCOUNT_PATH, ConstPath.ACCOUNT_OPERATOR_LIST_PATH_SHORT),    // Exclude to avoid conflict with /account/operator/{operator_id}
            String.join("", ConstPath.ACCOUNT_PATH, ConstPath.ACCOUNT_OPERATOR_PATH_SHORT, ConstPath.ACCOUNT_OPERATOR_PLANT_PATH_SHORT),    // Exclude to avoid conflict with /account/operator/{operator_id}
            String.join("", ConstPath.ACCOUNT_PATH, ConstPath.ACCOUNT_OPERATOR_PATH_SHORT, ConstPath.ACCOUNT_OPERATOR_PLANT_LIST_PATH_SHORT)    // Exclude to avoid conflict with /account/operator/plant/{plant_id}
        ));
    }

    /**
     * Sets the paths for the path parameter plant ID validator interceptor.
     */
    @Bean
    public List<String> pathParameterPlantIdPaths() {
        return new CopyOnWriteArrayList<>(List.of(
            String.join("", ConstPath.ACCOUNT_PATH, ConstPath.ACCOUNT_OPERATOR_PATH_SHORT, ConstPath.ACCOUNT_OPERATOR_PLANT_PATH_WITH_ID_SHORT),
            String.join("", ConstPath.ACCOUNT_PATH, ConstPath.ACCOUNT_OPERATOR_PATH_SHORT, ConstPath.ACCOUNT_OPERATOR_PLANT_STATUS_PATH_WITH_ID_SHORT)
        ));
    }

    /**
     * Sets the paths to exclude for the path parameter plant ID validator interceptor.
     */
    @Bean
    public List<String> pathParameterPlantIdExcludePaths() {
        return new CopyOnWriteArrayList<>(List.of(
            String.join("", ConstPath.ACCOUNT_PATH, ConstPath.ACCOUNT_OPERATOR_PATH_SHORT, ConstPath.ACCOUNT_OPERATOR_PLANT_LIST_PATH_SHORT)    // Exclude to avoid conflict with /account/operator/plant/{plant_id}
        ));
    }

    /**
     * Sets the paths for the path parameter client UUID validator interceptor.
     */
    @Bean
    public List<String> pathParameterClientUuidPaths() {
        return new CopyOnWriteArrayList<>(List.of(
            String.join("", ConstPath.AUTH_PATH, ConstPath.AUTH_CLIENTS_SECRET_PATH_SHORT)
        ));
    }

    /**
     * Sets the paths for the path parameter client ID validator interceptor.
     */
    @Bean
    public List<String> pathParameterClientIdPaths() {
        return new CopyOnWriteArrayList<>(List.of(
            String.join("", ConstPath.AUTH_PATH, ConstPath.AUTH_CLIENTS_ID_PATH_SHORT)
        ));
    }

    /**
     * Sets the paths for the HTTP header validator interceptor.
     */
    @Bean
    public List<String> httpHeaderPaths() {
        return new CopyOnWriteArrayList<>(List.of(
            String.join("", ConstPath.AUTH_PATH, ConstPath.ALL_PATH_PATTERN_INTERCEPTOR),
            String.join("", ConstPath.ACCOUNT_PATH, ConstPath.ALL_PATH_PATTERN_INTERCEPTOR),
            String.join("", ConstPath.AUTHORIZATION_PATH, ConstPath.ALL_PATH_PATTERN_INTERCEPTOR)
        ));
    }

    /**
     * Sets the paths for the API authorization interceptor.
     */
    @Bean
    public List<String> apiAuthorizationPaths() {
        return new CopyOnWriteArrayList<>(List.of(
            String.join("", ConstPath.AUTHORIZATION_PATH, ConstPath.AUTHORIZATION_AUTHORIZATION_MODELS_PATH),
            String.join("", ConstPath.AUTHORIZATION_PATH, ConstPath.AUTHORIZATION_TUPLES_WRITE),
            String.join("", ConstPath.AUTHORIZATION_PATH, ConstPath.AUTHORIZATION_TUPLES_READ),
            String.join("", ConstPath.AUTHORIZATION_PATH, ConstPath.AUTHORIZATION_STORES_PATH),
            String.join("", ConstPath.AUTHORIZATION_PATH, ConstPath.AUTHORIZATION_STORE),
            String.join("", ConstPath.ACCOUNT_PATH, ConstPath.ACCOUNT_USER_PATH_SHORT),
            String.join("", ConstPath.AUTH_PATH, ConstPath.AUTH_CLIENTS_PATH_SHORT),
            String.join("", ConstPath.AUTH_PATH, ConstPath.AUTH_CLIENTS_ID_PATH_SHORT),
            String.join("", ConstPath.AUTH_PATH, ConstPath.AUTH_CLIENTS_SECRET_PATH_SHORT)
        ));
    }

    /**
     * Sets the paths for the authorization store ID validator interceptor.
     */
    @Bean
    public List<String> authzStoreIdPaths() {
        return new CopyOnWriteArrayList<>(List.of(
            String.join("", ConstPath.AUTHORIZATION_PATH, ConstPath.AUTHORIZATION_AUTHORIZATION_MODELS_PATH)
        ));
    }

    /**
     * Sets the paths for the realm-store validator interceptor.
     */
    @Bean
    public List<String> authzRealmStorePaths() {
        return new CopyOnWriteArrayList<>(List.of(
            String.join("", ConstPath.AUTHORIZATION_PATH, ConstPath.AUTHORIZATION_STORE, ConstPath.ALL_PATH_PATTERN_INTERCEPTOR)
        ));
    }

    /**
     * Sets the paths for the confirm store ID validator interceptor.
     */
    @Bean
    public List<String> authzConfirmStoreIdPaths() {
        return new CopyOnWriteArrayList<>(List.of(
            String.join("", ConstPath.AUTHORIZATION_PATH, ConstPath.AUTHORIZATION_STORE)
        ));
    }
}
