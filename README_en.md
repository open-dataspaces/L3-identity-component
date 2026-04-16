## Overview and Purpose
This repository publishes a reference implementation example of the **Identity Component**, which plays a core role in the Identity Layer (L3), among the reference implementations of the technical reference document for Open Data Spaces (ODS), the "ODS Reference Architecture Model (ODS-RAM)".
For details on ODS-RAM, please refer to [here](https://open-dataspaces.gitbook.io/ods-docs).

## Basic Concepts
Open Data Spaces (ODS) is an open and scalable foundation for distributed data, built on organizational and national diversity by design.

## Functional Overview / List of Functions
For details on functionality, please refer to the [Open Data Spaces Protocol (ODP)](https://open-dataspaces.gitbook.io/ods-docs), which summarizes the L3 protocol specifications, and the [API specification](./docs/openapi).

## Directory Structure
```
project-root/
├── open-data-spaces-l3-authenticator-backend/ 	# Application root directory
│ ├── config/ 									# CheckStyle files
│ ├── core/src/ 								# Source code
│ │ ├── main/
│ │ │ ├── java/io/github/open_dataspaces/core/
│ │ │ │ ├── application/ 						# Application layer
│ │ │ │ ├── common/ 							# Common functionality
│ │ │ │ ├── domain/ 							# Domain layer
│ │ │ │ ├── exception/ 							# Error handling
│ │ │ │ └── infrastructure/ 					# Infrastructure layer
│ │ │ └── resources/ 							# Application configuration
│ │ │ └── META-INF/ 							# Application configuration metadata
│ │ └── test/ 									# Test code
│ │ ├── java/io/github/open_dataspaces/core/ 	# Unit tests
│ │ └── resources/ 								# Test configuration
│ └── docker/ 									# Container orchestration configuration
├── docs/ 										# Documentation
├── scripts/ 									# Setup and auxiliary scripts
├── server/ 									# Configuration for external services (docker, etc.)
└── README.md 									# This file
```

# Environment Setup Index
<!-- Update Index `npx doctoc README.md` -->
<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents** *generated with [DocToc](https://github.com/thlorenz/doctoc)*
- [Environment Setup Index](#environment-setup-index)
- [Environment Setup Procedure](#environment-setup-procedure)
	- [Verified Operating Environment](#verified-operating-environment)
	- [About Verified OSS and Middleware Versions](#about-verified-oss-and-middleware-versions)
	- [1. Service Startup](#1-service-startup)
		- [1-1. Starting Services Using docker-compose](#1-1-starting-services-using-docker-compose)
			- [1-1-1. Starting Required External Services](#1-1-1-starting-required-external-services)
			- [1-1-2. OpenFGA Migration](#1-1-2-openfga-migration)
			- [1-1-3. Database Setup](#1-1-3-database-setup)
			- [1-1-4. Starting the User Authentication System](#1-1-4-starting-the-user-authentication-system)
		- [1-2. Starting Services Using Dockerfile](#1-2-starting-services-using-dockerfile)
			- [Prerequisites](#prerequisites)
			- [1-2-1. Starting Required External Services](#1-2-1-starting-required-external-services)
				- [Keycloak](#keycloak)
				- [OpenFGA](#openfga)
			- [1-2-2. OpenFGA Migration](#1-2-2-openfga-migration)
			- [1-2-3. Database Setup](#1-2-3-database-setup)
			- [1-2-4. Starting the User Authentication System](#1-2-4-starting-the-user-authentication-system)
	- [2. Runtime Environment Setup](#2-runtime-environment-setup)
		- [2-1. Reference Implementation](#2-1-reference-implementation)
		- [2-2. Environment Setup Procedure](#2-2-environment-setup-procedure)
			- [Prerequisites](#prerequisites-1)
			- [2-2-1. Preparation of Environment Setup Files](#2-2-1-preparation-of-environment-setup-files)
			- [2-2-2. Executing Setup Scripts](#2-2-2-executing-setup-scripts)
- [License](#license)
- [Disclaimer](#disclaimer)
<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# Environment Setup Procedure
## Verified Operating Environment

| Name | Version | Notes |
| --- | --- | --- |
| JDK | JDK-21.x | Build the user authentication system application |
| Maven | Apache Maven 3.9.8 | Build the user authentication system application |
| Docker Engine / Docker Desktop | - | Execute the user authentication system application and each service |

## About Verified OSS and Middleware Versions
For the versions of OSS and middleware currently used by the application, please refer to [here](./docs/architecture/architecture.md).

## 1. Service Startup
Please execute one of the following procedures to start the required external services and the user authentication system.
- [1-1. Starting Services Using docker-compose](#1-1-starting-services-using-docker-compose)
- [1-2. Starting Services Using Dockerfile](#1-2-starting-services-using-dockerfile)

### 1-1. Starting Services Using docker-compose
#### 1-1-1. Starting Required External Services
Execute the following commands to start the external services.
- PostgreSQL : Database service
- Keycloak : Authentication service
- OpenFGA : Authorization service
```shell
# Change directory (from the repository root directory)
cd ./server
# Create docker network: execute only for the first time
docker network create shared-network-ods
# Docker Compose
docker compose -p ods-l3-auth up --build -d
```

#### 1-1-2. OpenFGA Migration
Execute the following commands to perform OpenFGA migration.
```shell
# OpenFGA migration
docker compose -p ods-l3-auth run \
--rm openfga_dev migrate \
--datastore-engine postgres \
--datastore-uri 'postgres://openfga:password@postgres_openfga_dev:5432/openfga?sslmode=disable'
```
After execution, if the following message is displayed, the migration is complete.
```
migration done
```

#### 1-1-3. Database Setup
Execute the following commands to set up the database.
- Database creation
- DB user creation
- Schema creation
- Table creation
```shell
# Change directory (from the repository root directory)
cd ./scripts/sql
# Get Docker container name
container_name=$(docker ps --format '{{.Names}}' | grep -E "postgres_dev" | head -n1)
# Copy script files to the container
docker cp 1_create_app_user.sql $container_name:/tmp/1_create_app_user.sql
docker cp 2_create_app_db.sql $container_name:/tmp/2_create_app_db.sql
docker cp 3_setup_app_db.sql $container_name:/tmp/3_setup_app_db.sql
# Execute scripts
# Create DB user
docker exec -it $container_name bash -c "PGPASSWORD=password psql -U keycloak -d keycloak -f /tmp/1_create_app_user.sql -v user_password=password"
# Create DB
docker exec -it $container_name bash -c "PGPASSWORD=password psql -U keycloak -d keycloak -f /tmp/2_create_app_db.sql"
# Create schema/tables
docker exec -it $container_name bash -c "PGPASSWORD=password psql -U app_ods -d db_ods -f /tmp/3_setup_app_db.sql"
```

#### 1-1-4. Starting the User Authentication System
Build and start the user authentication system.
```shell
# Change directory (from the repository root directory)
cd ./open-data-spaces-l3-authenticator-backend
# Build the executable
mvn clean install -DskipTests
# Docker Compose
docker compose -p ods-l3-app -f docker/docker-compose-local.yaml up --build -d
```

### 1-2. Starting Services Using Dockerfile
#### Prerequisites
A Dockerfile for starting the database service is not included in this repository.
Please prepare a PostgreSQL service in advance and ensure that it is running and accessible.

#### 1-2-1. Starting Required External Services
Refer to the following Dockerfiles and environment variables, execute `docker build` and `docker push`, and then start the external services.
- Keycloak : Authentication service
- OpenFGA : Authorization service

##### Keycloak
```shell
# Keycloak
./server/keycloak/docker/Dockerfile
```
<a href="https://www.keycloak.org/server/configuration" target="_blank">Keycloak configuration</a>

##### OpenFGA
```shell
# OpenFGA
./server/openfga/docker/Dockerfile
```
<a href="https://openfga.dev/docs/getting-started/setup-openfga/configuration" target="_blank">OpenFGA configuration</a>

#### 1-2-2. OpenFGA Migration
Execute the following command to perform OpenFGA migration.
```shell
# OpenFGA migration
/openfga migrate
```
examples:
The following is an example of performing migration on OpenFGA in a docker environment.
```shell
# Set Docker container name
container_name="<docker container name>"
# OpenFGA migration
docker exec -it $container_name /openfga migrate
```
After execution, if the following message is displayed, the migration is complete.
```
migration done
```

#### 1-2-3. Database Setup
Execute the following scripts to set up the database.
- Database creation
- DB user creation
- Schema creation
- Table creation
```shell
# Destination database configuration
DB_HOST=http://postgres.example.com
DB_PORT=5432
DB_NAME=example_db
DB_USER=example_user
DB_PASSWORD=example_password
# Password for the user to be created (if it contains symbols, enclose it in single quotes)
USER_PASSWORKD='password'
# Change directory (from the repository root directory)
cd ./scripts/sql
# Execute script (DB user creation / not required if already created)
PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f 1_create_app_user.sql -v user_password=$USER_PASSWORD
# Execute script (DB creation / not required if already created)
PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f 2_create_app_db.sql
# Execute script (schema/table creation)
PGPASSWORD=$USER_PASSWORD psql -h $DB_HOST -p $DB_PORT -U app_ods -d db_ods -f 3_setup_app_db.sql
```

#### 1-2-4. Starting the User Authentication System
Execute the following commands to build the user authentication system.
```shell
# Change directory (from the repository root directory)
cd ./open-data-spaces-l3-authenticator-backend
# Build the executable
mvn clean install -DskipTests
```
Refer to the following Dockerfile and environment variables to start the user authentication system.
```
./docker/Dockerfile.example
```
[User Authentication System configuration](./docs/configuration/environment-variables_en.md)

## 2. Runtime Environment Setup
### 2-1. Reference Implementation
Once the services are started and built successfully, you can execute the tutorial for the following business flow.
[Reference Implementation Tutorial](./docs/tutorials/tutorials_en.md)
The reference implementation tutorial assumes [1-1 Starting with docker-compose](#1-1-starting-services-using-docker-compose).

### 2-2. Environment Setup Procedure
#### Prerequisites
- In [1. Service Startup](#1-service-startup), the following external services have been started:
  - PostgreSQL : Database service
  - Keycloak : Authentication service
  - OpenFGA : Authorization service
- An environment where bash/curl/psql commands can be executed
  - Able to connect to Keycloak/OpenFGA using curl commands
  - Able to connect to PostgreSQL using psql commands

#### 2-2-1. Preparation of Environment Setup Files
Prepare environment files for Keycloak/OpenFGA/Postgres setup.
Create the environment files for your environment by referring to the following file.
example: `./scripts/env/setup.env.example`

| Name | Notes |
| --- | --- |
| KEYCLOAK_BASE_URL | Base URL for connecting to Keycloak |
| KEYCLOAK_REALM | Name of the Keycloak realm to be created |
| KEYCLOAK_MASTER_REALM | Default Keycloak realm name (usually `master`) |
| KEYCLOAK_CLIENT_ID | Client ID for issuing access tokens for executing Keycloak Admin REST API (usually `admin-cli`) |
| KEYCLOAK_USERNAME | Keycloak administrator login ID |
| KEYCLOAK_PASSWORD | Keycloak administrator password |
| KEYCLOAK_CLIENT_ID_API_ADMIN | Client ID to be created in Keycloak (API execution privilege / operator privilege) |
| KEYCLOAK_CLAIM_OPEN_SYSTEM_ID_API_ADMIN | Open system ID to be set for the created Keycloak client ID |
| OPENFGA_BASE_URL | Base URL for connecting to OpenFGA |
| POSTGRES_HOST | Hostname for connecting to Postgres |
| POSTGRES_PORT | Port for connecting to Postgres |
| POSTGRES_DB | Database name for connecting to Postgres |
| POSTGRES_USER | User for connecting to Postgres |
| POSTGRES_PASSWORD | Password for connecting to Postgres |
| L3TBL_API_KEY | API key of the user authentication system to be created |
| L3TBL_API_KEYS_ID | API key ID of the user authentication system to be created (usually an arbitrary UUID) |
| L3TBL_API_KEYS_NAME | API key name of the user authentication system to be created |
| L3TBL_API_KEYS_USECASE | Use case name for operator/attribute management of the user authentication system to be created |
| L3TBL_AUTHZ_STORES_ENVIRONMENT_NAME | Environment name of the running user authentication system (specify the value of the environment variable `ODS_APPLICATION_ENV_NAME`) |
| L3TBL_CIDRS_CIDR | IP addresses allowed to connect (when the environment variable `ODS_ENABLE_IP_RESTRICTION` is enabled) |

#### 2-2-2. Executing Setup Scripts
Specify the created .env file as an argument and execute the setup script.
```shell
# Change directory (from the repository root directory)
cd ./scripts
# Execute script file
bash ./setup.sh <path to created .env file>
```
When the setup is completed, the following message will be displayed.
```
Setup completed successfully!
```
The items output after `Summary of created resources and configuration:` are as follows.

| Name | Note |
| --- | --- |
| Keycloak realm | Keycloak realm specified by `KEYCLOAK_REALM` |
| API Authorization client ID in Keycloak | Client ID specified by `KEYCLOAK_CLIENT_ID_API_ADMIN` |
| API Authorization client secret in Keycloak | Client secret of the created Keycloak client ID |
| API Authorization store ID in OpenFGA | Created OpenFGA store ID ([API execution privilege management store](./docs/openfga/api-authz-model_en.md)) |
| Realm-store binding store ID in OpenFGA | Created OpenFGA store ID ([Realm-store binding management store](./docs/openfga/realm-store-binding-model_en.md)) |
| Operator-plant authorization store ID in OpenFGA | Created OpenFGA store ID ([Operator/Attribute (Plant) management store](./docs/openfga/operator-plant-model_en.md)) |
| System API key ID in PostgreSQL | API key specified by `L3TBL_API_KEY` |

# License
- This repository is provided under the MIT License.
- The copyright of the source code and related documentation belongs to NTT DATA Group Corporation and NTT DATA Corporation.

# Disclaimer
- The contents of this repository are subject to change or deletion without notice.
- We assume no responsibility for any losses or damages arising from the use of this repository.
