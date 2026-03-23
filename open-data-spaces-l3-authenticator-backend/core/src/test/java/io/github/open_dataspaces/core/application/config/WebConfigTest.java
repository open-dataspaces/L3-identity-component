/*
 * WebConfigTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies that the various bean creations, filter registrations,
 * and interceptor registrations in WebConfig work correctly.
 *
 * Date: 2025-06-30
 */

package io.github.open_dataspaces.core.application.config;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

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
import io.github.open_dataspaces.core.common.consts.ConstPath;

/*
 * WebConfigTest is a test class for the WebConfig.
 */
class WebConfigTest {

    @Mock
    private ODSProperties odsProperties;
    @Mock
    private APIKeyValidator apiKeyValidator;
    @Mock
    private IPForAPIKeyValidator ipForAPIKeyValidator;
    @Mock
    private TokenValidator tokenValidator;
    @Mock
    private PathParameterValidator pathParameterValidator;
    @Mock
    private HttpHeaderValidator httpHeaderValidator;
    @Mock
    private AuthzConfirmStoreIdValidator authzConfirmStoreIdValidator;
    @Mock
    private ApiAuthorizationInterceptor apiAuthorizationInterceptor;
    @Mock
    private AuthzStoreValidator authzStoreValidator;
    @Mock
    private AuthzRealmStoreValidator authzRealmStoreValidator;

    private WebConfig webConfig;

    @Mock
    private List<String> apiKeyPaths;
    @Mock
    private List<String> tokenPaths;
    @Mock
    private List<String> tokenExcludePaths;
    @Mock
    private List<String> pathParameterOperatorIdPaths;
    @Mock
    private List<String> pathParameterOperatorIdExcludePaths;
    @Mock
    private List<String> pathParameterPlantIdPaths;
    @Mock
    private List<String> pathParameterPlantIdExcludePaths;
    @Mock
    private List<String> httpHeaderPaths;
    @Mock
    private List<String> authzConfirmStoreIdPaths;
    @Mock
    private List<String> apiAuthorizationPaths;
    @Mock
    private List<String> authzStoreIdPaths;
    @Mock
    private List<String> pathParameterClientUuidPaths;
    @Mock
    private List<String> authzRealmStorePaths;

    @Mock
    private List<String> pathParameterClientIdPaths;

    /**
     * setUp tests that mocks are initialized and a WebConfig instance is created before each test.
     */
    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
        // Create WebConfig instance
        webConfig = new WebConfig(
                odsProperties,
                apiKeyValidator,
                ipForAPIKeyValidator,
                tokenValidator,
                pathParameterValidator,
                httpHeaderValidator,
                authzConfirmStoreIdValidator,
                apiAuthorizationInterceptor,
                authzStoreValidator,
                authzRealmStoreValidator,
                apiKeyPaths,
                tokenPaths,
                tokenExcludePaths,
                pathParameterOperatorIdPaths,
                pathParameterOperatorIdExcludePaths,
                pathParameterPlantIdPaths,
                pathParameterPlantIdExcludePaths,
                pathParameterClientUuidPaths,
                pathParameterClientIdPaths,
                httpHeaderPaths,
                authzConfirmStoreIdPaths,
                apiAuthorizationPaths,
                authzStoreIdPaths,
                authzRealmStorePaths);
    }

    /**
     * bodyLimitFilter_shouldReturnInstance tests that the bodyLimitFilter method returns an instance of BodyLimitFilter.
     */
    @Test
    void bodyLimitFilter_shouldReturnInstance() {
        // Assert
        assertNotNull(webConfig.bodyLimitFilter());
        assertTrue(webConfig.bodyLimitFilter() instanceof BodyLimitFilter);
    }

    /**
     * authDumpFilter_shouldRegisterCorrectFilterAndPattern tests that the authDumpFilter method registers the correct filter and pattern.
     */
    @Test
    void authDumpFilter_shouldRegisterCorrectFilterAndPattern() {
        // Get FilterRegistrationBean for AuthDumpFilter
        FilterRegistrationBean<AuthDumpFilter> bean = webConfig.authDumpFilter();
        // Assert
        assertNotNull(bean.getFilter());
        assertTrue(bean.getFilter() instanceof AuthDumpFilter);
        assertTrue(bean.getUrlPatterns().contains(ConstPath.ALL_PATH_PATTERN_FILTER));
    }

    /**
     * bodyDpFilter_shouldRegisterCorrectFilterAndPattern tests that the bodyDpFilter method registers the correct filter and pattern.
     */
    @Test
    void bodyDpFilter_shouldRegisterCorrectFilterAndPattern() {
        // Get FilterRegistrationBean for BodyDumpFilter
        FilterRegistrationBean<BodyDumpFilter> bean = webConfig.bodyDpFilter();
        // Assert
        assertNotNull(bean.getFilter());
        assertTrue(bean.getFilter() instanceof BodyDumpFilter);
        assertTrue(bean.getUrlPatterns().contains(ConstPath.ALL_PATH_PATTERN_FILTER));
    }

    /**
     * addInterceptors_shouldRegisterAllPatterns_whenIpRestrictionEnabled tests that when IP restriction is enabled,
     * systemAPIKeyValidator and ipForAPIKeyValidator are registered, and apiKeyValidator is not registered.
     *
     * @throws Exception if a field operation fails
     */
    @Test
    @SuppressWarnings("unchecked")
    void addInterceptors_shouldRegisterAllPatterns_whenIpRestrictionEnabled() throws Exception {
        InterceptorRegistry registry = mock(InterceptorRegistry.class);
        InterceptorRegistration registration = mock(InterceptorRegistration.class);
        when(odsProperties.isEnableIpRestriction()).thenReturn(true);
        when(registry.addInterceptor(any())).thenReturn(registration);
        when(registration.addPathPatterns(any(List.class))).thenReturn(registration);
        when(registration.excludePathPatterns(any(List.class))).thenReturn(registration);

        // Act
        webConfig.addInterceptors(registry);

        // Verify
        // verify(registry).addInterceptor(systemAPIKeyValidator);
        verify(registry, atLeastOnce()).addInterceptor(ipForAPIKeyValidator);
        verify(registry, never()).addInterceptor(apiKeyValidator);
        verify(registration, atLeastOnce()).addPathPatterns(any(List.class));
        verify(registry, atLeastOnce()).addInterceptor(tokenValidator);
        verify(registry, atLeastOnce()).addInterceptor(apiAuthorizationInterceptor);
        verify(registry, atLeastOnce()).addInterceptor(pathParameterValidator);
        verify(registry, atLeastOnce()).addInterceptor(httpHeaderValidator);
        verify(registry, atLeastOnce()).addInterceptor(authzConfirmStoreIdValidator);
        verify(registry, atLeastOnce()).addInterceptor(authzStoreValidator);
        verify(registry, atLeastOnce()).addInterceptor(authzRealmStoreValidator);
    }

    /**
     * addInterceptors_shouldRegisterAllPatterns_whenIpRestrictionDisabled tests that when IP restriction is disabled,
     * systemAPIKeyValidator and apiKeyValidator are registered, and ipForAPIKeyValidator is not registered.
     *
     * @throws Exception if a field operation fails
     */
    @Test
    @SuppressWarnings("unchecked")
    void addInterceptors_shouldRegisterAllPatterns_whenIpRestrictionDisabled() throws Exception {
        InterceptorRegistry registry = mock(InterceptorRegistry.class);
        InterceptorRegistration registration = mock(InterceptorRegistration.class);
        when(odsProperties.isEnableIpRestriction()).thenReturn(false);
        when(registry.addInterceptor(any())).thenReturn(registration);
        when(registration.addPathPatterns(any(List.class))).thenReturn(registration);
        when(registration.excludePathPatterns(any(List.class))).thenReturn(registration);

        // Act
        webConfig.addInterceptors(registry);

        // Verify
        // verify(registry).addInterceptor(systemAPIKeyValidator);
        verify(registry, atLeastOnce()).addInterceptor(apiKeyValidator);
        verify(registry, never()).addInterceptor(ipForAPIKeyValidator);
        verify(registration, atLeastOnce()).addPathPatterns(any(List.class));
        verify(registry, atLeastOnce()).addInterceptor(tokenValidator);
        verify(registry, atLeastOnce()).addInterceptor(apiAuthorizationInterceptor);
        verify(registry, atLeastOnce()).addInterceptor(pathParameterValidator);
        verify(registry, atLeastOnce()).addInterceptor(httpHeaderValidator);
        verify(registry, atLeastOnce()).addInterceptor(authzConfirmStoreIdValidator);
        verify(registry, atLeastOnce()).addInterceptor(authzStoreValidator);
        verify(registry, atLeastOnce()).addInterceptor(authzRealmStoreValidator);
    }

    /**
     * addCorsMappings_propertiesEnabled tests that when CORS properties are enabled.
     */
    @Test
    @DisplayName("addCorsMappings - CORS properties enabled")
    void addCorsMappings_corsPropertiesEnabled() {
        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration corsRegistration = mock(CorsRegistration.class);

        // Arrange
        when(odsProperties.getCors()).thenReturn(mock(ODSProperties.Cors.class));
        when(odsProperties.getCors().isPropertiesEnabled()).thenReturn(true);       // properties enabled

        when(odsProperties.getCors().getPathPattern()).thenReturn("/**");
        when(odsProperties.getCors().getAllowedOrigins()).thenReturn("http://example.com,http://another.com".split(","));
        when(odsProperties.getCors().getAllowedMethods()).thenReturn("GET,POST,PUT,DELETE,OPTIONS".split(","));
        when(odsProperties.getCors().getAllowedHeaders()).thenReturn("Content-Type,Authorization".split(","));
        when(odsProperties.getCors().getMaxAge()).thenReturn(3600L);

        when(registry.addMapping(anyString())).thenReturn(corsRegistration);
        when(corsRegistration.allowedOrigins(any(String[].class))).thenReturn(corsRegistration);
        when(corsRegistration.allowedMethods(any(String[].class))).thenReturn(corsRegistration);
        when(corsRegistration.allowedHeaders(any(String[].class))).thenReturn(corsRegistration);
        when(corsRegistration.allowCredentials(anyBoolean())).thenReturn(corsRegistration);
        when(corsRegistration.maxAge(anyLong())).thenReturn(corsRegistration);

        // Act
        webConfig.addCorsMappings(registry);

        // Assert
        verify(registry).addMapping(anyString());
        verify(corsRegistration).allowedOrigins(any(String[].class));
        verify(corsRegistration).allowedMethods(any(String[].class));
        verify(corsRegistration).allowedHeaders(any(String[].class));
        verify(corsRegistration).allowCredentials(anyBoolean());
        verify(corsRegistration).maxAge(anyLong());
    }

    @Test
    @DisplayName("addCorsMappings - CORS default")
    void addCorsMappings_corsDefault() {
        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration corsRegistration = mock(CorsRegistration.class);

        // Arrange
        when(odsProperties.getCors()).thenReturn(mock(ODSProperties.Cors.class));
        when(odsProperties.getCors().isPropertiesEnabled()).thenReturn(false);       // properties disabled

        when(registry.addMapping(anyString())).thenReturn(corsRegistration);
        when(corsRegistration.allowedOriginPatterns(anyString())).thenReturn(corsRegistration);
        when(corsRegistration.allowedMethods(anyString())).thenReturn(corsRegistration);
        when(corsRegistration.allowedHeaders(anyString())).thenReturn(corsRegistration);
        when(corsRegistration.allowCredentials(anyBoolean())).thenReturn(corsRegistration);
        when(corsRegistration.maxAge(anyLong())).thenReturn(corsRegistration);

        // Act
        webConfig.addCorsMappings(registry);

        // Assert
        verify(registry).addMapping(anyString());
        verify(corsRegistration).allowedOriginPatterns(anyString());
        verify(corsRegistration).allowedMethods(anyString());
        verify(corsRegistration).allowedHeaders(anyString());
        verify(corsRegistration).allowCredentials(anyBoolean());
        verify(corsRegistration).maxAge(anyLong());
    }
}