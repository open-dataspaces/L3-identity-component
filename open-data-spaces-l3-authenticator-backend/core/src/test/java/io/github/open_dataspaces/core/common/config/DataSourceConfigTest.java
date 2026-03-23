/*
 * DataSourceConfigTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This test class verifies that the data source creation process
 * in DataSourceConfig works correctly.
 *
 * Date: 2025-06-30
 */

package io.github.open_dataspaces.core.common.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.jdbc.DataSourceBuilder;

import io.github.open_dataspaces.core.common.consts.Const;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertSame;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.RETURNS_SELF;

/*
 * DataSourceConfigTest is a test class for the DataSourceConfig.
 */
class DataSourceConfigTest {

    @Mock
    private ODSProperties odsProperties;

    @Mock
    private SpringProperties springProperties;

    private DataSourceConfig dataSourceConfig;

    /**
     * setUp initializes a new DataSourceConfig instance before each test.
     */
    @BeforeEach
    void setUp() {
        odsProperties = Mockito.mock(ODSProperties.class);
        springProperties = Mockito.mock(SpringProperties.class);
        Mockito.when(springProperties.getDatasource()).thenReturn(Mockito.mock(SpringProperties.Datasource.class));
        Mockito.when(springProperties.getProfiles()).thenReturn(Mockito.mock(SpringProperties.Profiles.class));

        dataSourceConfig = new DataSourceConfig(odsProperties, springProperties);
    }

    /**
     * testDataSource_parameterized tests that the dataSource method
     * constructs the expected DataSource for various input values.
     */
    @ParameterizedTest
    @CsvSource(
            value = {
                // dbUrl, dbUsername, dbPassword, dbDriverClassName, env, isLocal, expectSSL
                "'jdbc:postgresql://localhost:5432/testdb',user,pass,org.postgresql.Driver,local,true,true,false", // case#1 LocalEnvironment NoSSLParams
                "'jdbc:postgresql://remote:5432/proddb',produser,prodpass,org.postgresql.Driver,production,true,false,true", // case#2 NonLocalEnvironment appendsActualSSLParams
                "'jdbc:postgresql://remote:5432/proddb',produser,prodpass,org.postgresql.Driver,production,false,false,false", // case#2 NonLocalEnvironment appendsActualSSLParams
                "'jdbc:postgresql://remote:5432/proddb',produser,prodpass,org.postgresql.Driver,NULL,true,false,true", // case#3 EnvIsNull treatedAsNonLocal
                "'',user,pass,org.postgresql.Driver,local,true,false,false", // case#4 Empty DbUrl
                "'jdbc:postgresql://localhost:5432/testdb','',pass,org.postgresql.Driver,local,true,true,false", // case#5 Empty DbUsername
                "'jdbc:postgresql://localhost:5432/testdb',user,'',org.postgresql.Driver,local,true,true,false", // case#6 Empty DbPassword
                "'jdbc:postgresql://localhost:5432/testdb',user,pass,'',local,true,true,false", // case#7 Empty DbDriverClassName
                "NULL,user,pass,org.postgresql.Driver,local,true,true,false", // case#8 Null DbUrl
                "'jdbc:postgresql://localhost:5432/testdb',NULL,pass,org.postgresql.Driver,local,true,true,false", // case#9 Null DbUsername
                "'jdbc:postgresql://localhost:5432/testdb',user,NULL,org.postgresql.Driver,local,true,true,false", // case#10 Null DbPassword
                "'jdbc:postgresql://localhost:5432/testdb',user,pass,NULL,local,true,true,false" // case#11 Null DbDriverClassName
            },
            nullValues = "NULL")
    void testDataSource_parameterized(
            String dbUrl,
            String dbUsername,
            String dbPassword,
            String dbDriverClassName,
            String env,
            boolean isDbSSL,
            boolean isLocal,
            boolean expectSSL
    ) throws Exception {
        // Set value
        when(springProperties.getDatasource().getUrl()).thenReturn("''".equals(dbUrl) ? "" : dbUrl);
        when(springProperties.getDatasource().getUsername()).thenReturn("''".equals(dbUsername) ? "" : dbUsername);
        when(springProperties.getDatasource().getPassword()).thenReturn("''".equals(dbPassword) ? "" : dbPassword);
        when(springProperties.getDatasource().getDriverClassName()).thenReturn("''".equals(dbDriverClassName) ? "" : dbDriverClassName);
        when(springProperties.getProfiles().getActive()).thenReturn(env);
        when(odsProperties.isDatabaseSsl()).thenReturn(isDbSSL);
        if (odsProperties.isDatabaseSsl()) {
            when(odsProperties.getDatabaseSslMode()).thenReturn("require");
            when(odsProperties.getDatabaseSslRootCert()).thenReturn("/secrets/server-ca/server-ca.pem");
            when(odsProperties.getDatabaseSslCert()).thenReturn("/secrets/client-cert/client-cert.pem");
            when(odsProperties.getDatabaseSslKey()).thenReturn("/secrets/client-key/client-key.pem");
        } else {
            when(odsProperties.getDatabaseSslMode()).thenReturn("");
            when(odsProperties.getDatabaseSslRootCert()).thenReturn("");
            when(odsProperties.getDatabaseSslCert()).thenReturn("");
            when(odsProperties.getDatabaseSslKey()).thenReturn("");
        }

        @SuppressWarnings("rawtypes") // Suppress unchecked cast warning for DataSourceBuilder
        MockedStatic<DataSourceBuilder> dsbMock = mockStatic(DataSourceBuilder.class);
        // Create a mock
        @SuppressWarnings("unchecked") // Suppress unchecked cast warning for DataSourceBuilder
        DataSourceBuilder<DataSource> builderMock = (DataSourceBuilder<DataSource>) mock(DataSourceBuilder.class, RETURNS_SELF);
        DataSource dsMock = mock(DataSource.class);

        dsbMock.when(DataSourceBuilder::create).thenReturn(builderMock);
        when(builderMock.build()).thenReturn(dsMock);

        // Act
        DataSource result = dataSourceConfig.dataSource();

        // If SSL is required, append certificate parameters to the URL
        String expectedUrl = dbUrl;
        List<String> dbParameter = new ArrayList<>();
        if (odsProperties.isDatabaseSsl()) {
            dbParameter.add(String.format(Const.DB_PARAM_POSTGRES_SSL, odsProperties.isDatabaseSsl()));
            dbParameter.add(String.format(Const.DB_PARAM_POSTGRES_SSL_MODE, odsProperties.getDatabaseSslMode()));
            dbParameter.add(String.format(Const.DB_PARAM_POSTGRES_ROOT_CERT, odsProperties.getDatabaseSslRootCert()));
            dbParameter.add(String.format(Const.DB_PARAM_POSTGRES_CERT, odsProperties.getDatabaseSslCert()));
            dbParameter.add(String.format(Const.DB_PARAM_POSTGRES_KEY, odsProperties.getDatabaseSslKey()));
            String paramString = String.join("&", dbParameter);
            expectedUrl = String.format("%s?%s", dbUrl, paramString);
        }

        // Verify
        verify(builderMock).url(expectedUrl);
        verify(builderMock).username(dbUsername);
        verify(builderMock).password(dbPassword);
        verify(builderMock).driverClassName(dbDriverClassName);
        verify(builderMock).build();
        // Assert
        assertSame(dsMock, result);

        // Close the static mock
        dsbMock.close();
    }
}