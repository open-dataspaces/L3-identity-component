
# User Authentication System Environment Variable List
## External Service Connection Settings
### Database Connection Settings
Environment Variable Name | Default Value | Description
-- | -- | -------------------------------------------
SPRING_DATASOURCE_DRIVER_CLASS_NAME | org.postgresql.Driver | JDBC driver class name
SPRING_DATASOURCE_URL | jdbc:postgresql://localhost:5432/db_ods | Database connection URL
SPRING_DATASOURCE_USERNAME | app_ods | Database username
SPRING_DATASOURCE_PASSWORD | *** | Database password
ODS_DATABASE_SSL | false | SSL enable/disable flag for database connections (ssl=true)
ODS_DATABASE_SSL_MODE | disable | SSL mode enable/disable flag for database connections (sslmode=require / disable / verify-full)
ODS_DATABASE_SSL_ROOT_CERT |  | Server certificate path for database SSL connections (/secrets/server-ca/server-ca.pem)
ODS_DATABASE_SSL_CERT |  | Client certificate path for database SSL connections (/secrets/client-cert/client-cert.pem)
ODS_DATABASE_SSL_KEY |  | Client key path for database SSL connections (/secrets/client-key/client-key.pem)
### Keycloak Connection Settings
Environment Variable Name | Default Value | Description
-- | -- | --
KEYCLOAK_REALM | master | Keycloak realm name
KEYCLOAK_API_ENDPOINT | http://localhost:8081 | Keycloak API endpoint
KEYCLOAK_CREDENTIALS_ADMIN_REALM | master | Administrator realm name
KEYCLOAK_CREDENTIALS_ADMIN_USERNAME | admin | Administrator username
KEYCLOAK_CREDENTIALS_ADMIN_PASSWORD | *** | Administrator password
KEYCLOAK_CREDENTIALS_ADMIN_CLIENT_ID | admin-cli | Administrator client ID
KEYCLOAK_AUTHORIZATION_URL | http://localhost:8081 | Authorization Code Flow endpoint URL
KEYCLOAK_AUTHORIZATION_RESPONSE_TYPE | code | Authorization Code Flow response type
KEYCLOAK_AUTHORIZATION_SCOPE | openid | Authorization Code Flow scope
KEYCLOAK_AUTHORIZATION_CODE_CHALLENGE_METHOD | S256 | Authorization Code Flow PKCE code challenge method
### OpenFGA Connection Settings
Environment Variable Name | Default Value | Description
-- | -- | --
OPENFGA_API_ENDPOINT | http://localhost:8080 | OpenFGA connection base URL
OPENFGA_PRESHARED_KEY | preshared | Authorization preshared key for OpenFGA connections
## Application Runtime Settings
### Application Environment Settings
Environment Variable Name | Default Value | Description
-- | -- | --
ODS_APPLICATION_ENV_NAME | local | Application runtime environment name
ODS_IS_ENABLE_IP_RESTRICTION | true | Enable/disable IP restriction flag (true/false)
ODS_DEFAULT_EFFECTIVE_END_DATE | 9999-12-31 | Default value for the data effective end date
ODS_ENABLE_UC_AUTHORIZATION | true | Enable/disable use-case operator management feature (true/false)
ODS_ENABLE_OPERATOR_PLANT_AUTHORIZATION | true | Enable/disable common operator management feature (true/false)
### Logging Settings
Environment Variable Name | Default Value | Description
-- | -- | --
LOGGING_FILE_NAME | logs/user_authentication.log | Log file path
LOGGING_LEVEL_COM_ODS | INFO | Application log level
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_WEB | info | Spring Web log level
LOGGING_LEVEL_ORG_POSTGRESQL | INFO | PostgreSQL JDBC log level
SPRING_JPA_SHOW_SQL | false | Enable SQL log output (true/false)
SPRING_MVC_LOG_REQUEST_DETAILS | false | Enable detailed request logging (true/false)
SPRING_OUTPUT_ANSI_ENABLED | always | Enable ANSI-colored log output
### API-JWT Verification Settings
Environment Variable Name | Default Value | Description
-- | -- | --
ODS_JWT_JWKS_CACHE_DURATION_SECOND | 3600 | JWKS certificate cache duration for Keycloak
ODS_JWT_JWKS_CACHE_SIZE | 10 | Number of cached JWKS certificates for Keycloak
ODS_JWT_CLAIM_EXPECTED_AUDIENCE |  | Claim `AUD` string to be validated during access token verification
ODS_JWT_CLAIM_EXPECTED_TYPE | Bearer | Claim `TYP` string to be validated during access token verification
### CORS Settings
Environment Variable Name | Default Value | Description
-- | -- | --
ODS_CORS_PROPERTIES_ENABLED | false | Control CORS settings via properties (true/false)
ODS_CORS_PATH_PATTERN | /** | Target path pattern for CORS
ODS_CORS_ALLOWED_ORIGINS | http://localhost:3000,https://example.com | Allowed origins
ODS_CORS_ALLOWED_METHODS | GET,POST,PUT,OPTIONS | Allowed HTTP methods
ODS_CORS_ALLOWED_HEADERS | Content-Type,Authorization,API-Key,User-Agent,Accept,Accept-Language,X-TrackingID | Allowed headers
ODS_CORS_ALLOW_CREDENTIALS | true | Allow credentialed requests (true/false)
ODS_CORS_MAX_AGE | 5 | Cache duration (seconds) for preflight requests
### Actuator CORS Settings
Environment Variable Name | Default Value | Description
-- | -- | --
MANAGEMENT_ENDPOINTS_WEB_CORS_ALLOWED_ORIGINS | http://localhost:3000 | Allowed origins for Actuator endpoints
MANAGEMENT_ENDPOINTS_WEB_CORS_ALLOW_CREDENTIALS | true | Allow credentials for Actuator endpoints (true/false)
MANAGEMENT_ENDPOINTS_WEB_CORS_ALLOWED_ORIGIN_PATTERNS | /** | Allowed origin patterns for Actuator endpoints
