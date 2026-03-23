/*
 * WebConfig.java
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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.github.open_dataspaces.core.application.filter.AuthDumpFilter;
import io.github.open_dataspaces.core.application.filter.BodyDumpFilter;
import io.github.open_dataspaces.core.application.filter.BodyLimitFilter;
import io.github.open_dataspaces.core.application.interceptor.APIKeyValidator;
import io.github.open_dataspaces.core.application.interceptor.ApiAuthorizationInterceptor;
import io.github.open_dataspaces.core.application.interceptor.AuthzConfirmStoreIdValidator;
import io.github.open_dataspaces.core.application.interceptor.AuthzRealmStoreValidator;
import io.github.open_dataspaces.core.application.interceptor.AuthzStoreValidator;
import io.github.open_dataspaces.core.application.interceptor.HttpHeaderValidator;
import io.github.open_dataspaces.core.application.interceptor.IPForAPIKeyValidator;
import io.github.open_dataspaces.core.application.interceptor.PathParameterValidator;
import io.github.open_dataspaces.core.application.interceptor.TokenValidator;
import io.github.open_dataspaces.core.common.config.ODSProperties;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstPath;

/**
 * Web configuration class for registering filters and interceptors. Handles API key validation, IP
 * restriction, request logging, and request body size limitation.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ODSProperties odsProperties;

    private final APIKeyValidator apiKeyValidator;
    private final IPForAPIKeyValidator ipForAPIKeyValidator;
    private final TokenValidator tokenValidator;
    private final PathParameterValidator pathParameterValidator;
    private final HttpHeaderValidator httpHeaderValidator;
    private final AuthzConfirmStoreIdValidator authzConfirmStoreIdValidator;
    private final ApiAuthorizationInterceptor apiAuthorizationInterceptor;
    private final AuthzRealmStoreValidator authzRealmStoreValidator;
    private final AuthzStoreValidator authzStoreIdValidator;

    private List<String> apiKeyPaths;
    private List<String> tokenPaths;
    private List<String> tokenExcludePaths;
    private List<String> pathParameterOperatorIdPaths;
    private List<String> pathParameterOperatorIdExcludePaths;
    private List<String> pathParameterPlantIdPaths;
    private List<String> pathParameterPlantIdExcludePaths;
    private List<String> pathParameterClientUuidPaths;
    private List<String> pathParameterClientIdPaths;
    private List<String> httpHeaderPaths;
    private List<String> authzConfirmStoreIdPaths;
    private List<String> apiAuthorizationPaths;
    private List<String> authzStoreIdPaths;
    private List<String> authzRealmStorePaths;

    /**
     * Constructor for WebConfig.
     *
     * @param apiKeyValidator the API key validator interceptor
     * @param ipForAPIKeyValidator the IP restriction validator interceptor
     * @param tokenValidator the token validator interceptor
     * @param pathParameterValidator the path parameter validator interceptor
     */
    public WebConfig(
            ODSProperties odsProperties,
            APIKeyValidator apiKeyValidator,
            IPForAPIKeyValidator ipForAPIKeyValidator,
            @Lazy TokenValidator tokenValidator,
            PathParameterValidator pathParameterValidator,
            HttpHeaderValidator httpHeaderValidator,
            AuthzConfirmStoreIdValidator authzConfirmStoreIdValidator,
            ApiAuthorizationInterceptor apiAuthorizationInterceptor,
            AuthzStoreValidator authzStoreIdValidator,
            AuthzRealmStoreValidator authzRealmStoreValidator,
            @Qualifier("apiKeyPaths") List<String> apiKeyPaths,
            @Qualifier("tokenPaths") List<String> tokenPaths,
            @Qualifier("tokenExcludePaths") List<String> tokenExcludePaths,
            @Qualifier("pathParameterOperatorIdPaths") List<String> pathParameterOperatorIdPaths,
            @Qualifier("pathParameterOperatorIdExcludePaths") List<String> pathParameterOperatorIdExcludePaths,
            @Qualifier("pathParameterPlantIdPaths") List<String> pathParameterPlantIdPaths,
            @Qualifier("pathParameterPlantIdExcludePaths") List<String> pathParameterPlantIdExcludePaths,
            @Qualifier("pathParameterClientUuidPaths") List<String> pathParameterClientUuidPaths,
            @Qualifier("pathParameterClientIdPaths") List<String> pathParameterClientIdPaths,
            @Qualifier("httpHeaderPaths") List<String> httpHeaderPaths,
            @Qualifier("authzConfirmStoreIdPaths") List<String> authzConfirmStoreIdPaths,
            @Qualifier("apiAuthorizationPaths") List<String> apiAuthorizationPaths,
            @Qualifier("authzStoreIdPaths") List<String> authzStoreIdPaths,
            @Qualifier("authzRealmStorePaths") List<String> authzRealmStorePaths
    ) {
        this.odsProperties = odsProperties;
        this.apiKeyValidator = apiKeyValidator;
        this.ipForAPIKeyValidator = ipForAPIKeyValidator;
        this.tokenValidator = tokenValidator;
        this.pathParameterValidator = pathParameterValidator;
        this.httpHeaderValidator = httpHeaderValidator;
        this.authzConfirmStoreIdValidator = authzConfirmStoreIdValidator;
        this.apiAuthorizationInterceptor = apiAuthorizationInterceptor;
        this.authzStoreIdValidator = authzStoreIdValidator;
        this.authzRealmStoreValidator = authzRealmStoreValidator;

        this.apiKeyPaths = apiKeyPaths;
        this.tokenPaths = tokenPaths;
        this.tokenExcludePaths = tokenExcludePaths;
        this.pathParameterOperatorIdPaths = pathParameterOperatorIdPaths;
        this.pathParameterOperatorIdExcludePaths = pathParameterOperatorIdExcludePaths;
        this.pathParameterPlantIdPaths = pathParameterPlantIdPaths;
        this.pathParameterPlantIdExcludePaths = pathParameterPlantIdExcludePaths;
        this.pathParameterClientUuidPaths = pathParameterClientUuidPaths;
        this.pathParameterClientIdPaths = pathParameterClientIdPaths;
        this.httpHeaderPaths = httpHeaderPaths;
        this.authzConfirmStoreIdPaths = authzConfirmStoreIdPaths;
        this.apiAuthorizationPaths = apiAuthorizationPaths;
        this.authzStoreIdPaths = authzStoreIdPaths;
        this.authzRealmStorePaths = authzRealmStorePaths;
    }

    /**
     * Registers the BodyLimitFilter bean to limit request body size.
     *
     * @return BodyLimitFilter instance
     */
    @Bean
    public BodyLimitFilter bodyLimitFilter() {
        return new BodyLimitFilter();
    }

    /**
     * Registers the AuthDumpFilter for authentication-related endpoints.
     *
     * @return FilterRegistrationBean for AuthDumpFilter
     */
    @Bean
    public FilterRegistrationBean<AuthDumpFilter> authDumpFilter() {
        FilterRegistrationBean<AuthDumpFilter> registrationBean =
                new FilterRegistrationBean<AuthDumpFilter>();
        registrationBean.setFilter(new AuthDumpFilter());
        registrationBean.addUrlPatterns(ConstPath.ALL_PATH_PATTERN_FILTER);
        return registrationBean;
    }

    /**
     * Registers the BodyDumpFilter for all endpoints.
     *
     * @return FilterRegistrationBean for BodyDumpFilter
     */
    @Bean
    public FilterRegistrationBean<BodyDumpFilter> bodyDpFilter() {
        FilterRegistrationBean<BodyDumpFilter> registrationBean =
                new FilterRegistrationBean<BodyDumpFilter>();
        registrationBean.setFilter(new BodyDumpFilter());
        registrationBean.addUrlPatterns(ConstPath.ALL_PATH_PATTERN_FILTER);
        return registrationBean;
    }

    /**
     * Adds API key and IP restriction interceptors, and token validate interceptor to
     * the application's interceptor registry.
     *
     * @param registry the InterceptorRegistry to which interceptors are added
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // apikey path list
        // -------------------------------
        // Switch the validator to use based on the condition
        HandlerInterceptor apiKeyInterceptor = odsProperties.isEnableIpRestriction() ? ipForAPIKeyValidator : apiKeyValidator;
        // registry.addInterceptor(apiKeyInterceptor)
        //         .addPathPatterns((List<String>) applicationContext.getBean("apiKeyPaths", List.class));
        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns(apiKeyPaths);

        // Token path List
        // -------------------------------
        // Add token validation for password-related paths
        registry.addInterceptor(tokenValidator)
                .addPathPatterns(tokenPaths)
                .excludePathPatterns(tokenExcludePaths);

        // Authorization path List
        // -------------------------------
        registry.addInterceptor(apiAuthorizationInterceptor)
                .addPathPatterns(apiAuthorizationPaths);

        // Realm-Store Validation path List
        // -------------------------------
        registry.addInterceptor(authzRealmStoreValidator)
                .addPathPatterns(authzRealmStorePaths);

        // HTTP Header Validation for all paths
        // -------------------------------
        registry.addInterceptor(httpHeaderValidator)
                .addPathPatterns(httpHeaderPaths);

        // Authorization Confirm Store ID path List
        // -------------------------------
        registry.addInterceptor(authzConfirmStoreIdValidator)
                .addPathPatterns(authzConfirmStoreIdPaths);

        // Path Parameter path List
        // -------------------------------
        // Add path parameter validation for password-related paths
        registry.addInterceptor(pathParameterValidator)
                .addPathPatterns(pathParameterOperatorIdPaths)
                .addPathPatterns(pathParameterPlantIdPaths)
                .addPathPatterns(pathParameterClientUuidPaths)
                .addPathPatterns(pathParameterClientIdPaths)
                .excludePathPatterns(pathParameterOperatorIdExcludePaths)
                .excludePathPatterns(pathParameterPlantIdExcludePaths);

        // Authorization store ID path List
        // -------------------------------
        registry.addInterceptor(authzStoreIdValidator)
                .addPathPatterns(authzStoreIdPaths);

    }

    /**
     * Configures CORS mappings for the application.
     *
     * @param registry the CorsRegistry to configure
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // CORS settings from ODSProperties
        if (odsProperties.getCors().isPropertiesEnabled()) {
            // Configure CORS based on properties
            registry.addMapping(odsProperties.getCors().getPathPattern())   // path pattern
                    .allowedOrigins(odsProperties.getCors().getAllowedOrigins()) // allowed origin
                    .allowedMethods(odsProperties.getCors().getAllowedMethods()) // allowed HTTP methods
                    .allowedHeaders(odsProperties.getCors().getAllowedHeaders()) // allowed headers
                    .allowCredentials(odsProperties.getCors().isAllowCredentials()) // allow credentials such as cookies
                    .maxAge(odsProperties.getCors().getMaxAge()); // max age for preflight requests
        } else {
            // Default: Allow all origins, methods, and headers
            registry.addMapping(Const.CORS_ALL_ENABLED_PATH_PATTERN)
                    .allowedOriginPatterns(Const.CORS_ALL_ENABLED)
                    .allowedMethods(Const.CORS_ALL_ENABLED)
                    .allowedHeaders(Const.CORS_ALL_ENABLED)
                    .allowCredentials(Const.CORS_ALL_ENABLED_CREDENTIALS)
                    .maxAge(Const.CORS_ALL_ENABLED_MAX_AGE);
        }
    }
}
