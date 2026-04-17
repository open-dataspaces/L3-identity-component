
# Developer Documentation

## Open Source Software (OSS) Currently Used in the Application

| Name           | Version                | Notes |
|:---------------|:-----------------------|:------|
| java           | 21                     |       |
| spring Boot    | 3.5.11                 |       |
| maven          | -                      |       |
| mockito        | 5.3.1                  |       |
| docker         | 28.1.1                 |       |
| docker Compose | 2.35.1-desktop.1       |       |

*The list of installed OSS is described in `pom.xml`.*

## Middleware (MW) Currently Used in the Application

| Name        | Version             | Notes |
|:------------|:--------------------|:------|
| PostgreSQL  | 17.x                | Version supported by GCP Cloud SQL until 2033/02/01 |
| Keycloak    | 26.2.5              |       |
| OpenFGA     | v1.9.2-authzen      | The v1.9.2-authzen version is an authzen-supported variant that branched from the midway point of the v1.8.1 and v1.8.2 releases, with v1.9.2 changes applied. This image is available at `ghcr.io/8hz/openfga:v1.9.2-authzen`. |
