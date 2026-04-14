# Reference Implementation Tutorial

This section provides an example of a reference implementation and operational verification for the ODS L3 User Authentication System.

## Table of Contents
<!-- Update Index `npx doctoc docs/tutorials/README.md` -->
<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [Reference Implementation Tutorial](#reference-implementation-tutorial)
  - [Table of Contents](#table-of-contents)
  - [Prerequisites](#prerequisites)
  - [System Architecture Diagram](#system-architecture-diagram)
  - [1. Data Setup](#1-data-setup)
    - [1-1. Loading Tutorial Data](#1-1-loading-tutorial-data)
  - [2. User Authentication System Verification](#2-user-authentication-system-verification)
    - [2-1. Creation of Authentication Information (Operator Information / Individual Users / Client IDs)](#2-1-creation-of-authentication-information-operator-information--individual-users--client-ids)
      - [2-1-1. Obtaining an Access Token (Client System Authentication)](#2-1-1-obtaining-an-access-token-client-system-authentication)
      - [2-1-2. Operator Information Registration](#2-1-2-operator-information-registration)
      - [2-1-3. Retrieve Operator Information](#2-1-3-retrieve-operator-information)
      - [2-1-4. Issuing an Operator Client ID](#2-1-4-issuing-an-operator-client-id)
      - [2-1-5. Retrieving the Operator Client Secret](#2-1-5-retrieving-the-operator-client-secret)
      - [2-1-6. Individual User Registration](#2-1-6-individual-user-registration)
      - [2-1-7. Issuing a Client ID for the Authorization Code Flow](#2-1-7-issuing-a-client-id-for-the-authorization-code-flow)
      - [2-1-8. Retrieving the Authorization Code Flow Client Secret](#2-1-8-retrieving-the-authorization-code-flow-client-secret)
    - [2-2. Operator Authentication (Client System Authentication)](#2-2-operator-authentication-client-system-authentication)
      - [Obtaining an Access Token (Operator Client ID Authentication)](#obtaining-an-access-token-operator-client-id-authentication)
      - [2-2-2. Token Introspection](#2-2-2-token-introspection)
    - [2-3. End-User Authentication (Authorization Code Flow)](#2-3-end-user-authentication-authorization-code-flow)
      - [2-3-1. Retrieving the Login URL](#2-3-1-retrieving-the-login-url)
      - [2-3-2. Obtaining the Authorization Code](#2-3-2-obtaining-the-authorization-code)
      - [2-3-3. Obtaining an Access Token (Authorization Code Flow)](#2-3-3-obtaining-an-access-token-authorization-code-flow)
      - [2-3-4. Token Introspection](#2-3-4-token-introspection)
    - [2-4. Using Authorization Features](#2-4-using-authorization-features)
      - [2-4-1. Obtaining an Access Token (Client System Authentication)](#2-4-1-obtaining-an-access-token-client-system-authentication)
      - [2-4-2. Registering API Execution Authorization Tuples](#2-4-2-registering-api-execution-authorization-tuples)
      - [2-4-3. Obtaining an Access Token (Operator Client ID Authentication)](#2-4-3-obtaining-an-access-token-operator-client-id-authentication)
      - [2-4-4. Store Registration](#2-4-4-store-registration)
      - [2-4-5. Model Registration](#2-4-5-model-registration)
      - [2-4-6. Tuple Registration](#2-4-6-tuple-registration)
      - [2-4-7. Authorization Decision](#2-4-7-authorization-decision)
  - [FAQ](#faq)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Prerequisites

- The following services must be up and running as described in
  [1-1 Starting Services Using docker-compose](../../README_en.md#1-1-starting-services-using-docker-compose):
  - PostgreSQL: Database service
  - Keycloak: Authentication service
  - OpenFGA: Authorization service
- An environment capable of executing the bash / curl / psql commands must be available
  - The curl command must be able to connect to Keycloak and OpenFGA
  - The psql command must be able to connect to PostgreSQL

## System Architecture Diagram

The system architecture diagram used in this tutorial is shown below.

```mermaid
flowchart LR
  subgraph HostClient["Host"]
    Client["HTTP Client<br>(Browser, etc.)"]
    HostPort["*.localhost:80<br>(Port mapping 127.0.0.1:80 → container:80)"]
  end

  subgraph DockerNet["shared-network-ods (external)"]
    NGINX["nginx<br>(ods-reverse-proxy)<br>container:80<br>aliases: id.ods.localhost, authz.ods.localhost"]
    ODSAPP["ods-app<br>container:8080"]
    KEYCLOAK["Keycloak<br>container:8080"]
    OPENFGA["OpenFGA<br>container:8080"]
    DB_KEYCLOAK["postgres<br>(Keycloak DB)<br>container:5432"]
    DB_OPENFGA["postgres<br>(OpenFGA DB)<br>container:5432"]
  end

  %% Host → nginx (via port 80 mapping)
  Client --> HostPort
  HostPort -->|app.ods.localhost<br>id.ods.localhost<br>authz.ods.localhost| NGINX

  %% Routing via host (nginx proxies requests to each service)
  NGINX -->|proxy: app.ods.localhost| ODSAPP
  NGINX -->|proxy: id.ods.localhost| KEYCLOAK
  NGINX -->|proxy: authz.ods.localhost| OPENFGA

  %% Access from within containers (name resolution points to nginx aliases)
  ODSAPP -->|"aliases: id.ods.localhost"| NGINX
  ODSAPP -->|"aliases: authz.ods.localhost"| NGINX
  NGINX -->|"aliases: id.ods.localhost"| KEYCLOAK
  NGINX -->|"aliases: authz.ods.localhost"| OPENFGA

  %% Database dependencies
  ODSAPP -->|depends_on| DB_KEYCLOAK
  KEYCLOAK -->|depends_on| DB_KEYCLOAK
  OPENFGA -->|depends_on| DB_OPENFGA
```

## 1. Data Setup

### 1-1. Loading Tutorial Data
Execute the following commands to load tutorial data into Keycloak, OpenFGA, and PostgreSQL.

```shell
# Move to the directory (from the repository root directory)
cd ./scripts

# Execute the script file
bash ./setup.sh ../docs/tutorials/env/tutorials.env
```

Once the setup is complete, the following message will be displayed.

```
Setup completed successfully!
Summary of created resources and configuration:
Keycloak realm: tutorials-realm
API Authorization client ID in Keycloak: system-api-authz-client
API Authorization client secret in Keycloak: BvAvAUh...
API Authorization store ID in OpenFGA: 01KJ9VEBX...
Realm-store binding store ID in OpenFGA: 01KJ9VEBZ...
Operator-plant authorization store ID in OpenFGA: 01KJ9VEC0...
System API key ID in PostgreSQL: tutorials-system-api-key-id
```

Store the obtained `API Authorization client secret in Keycloak` and `API Authorization store ID in OpenFGA` in variables.

```shell
# API Authorization client secret in Keycloak
SYSTEM_CLIENT_SECRET=BvAvAUh...
# API Authorization store ID in OpenFGA
API_AUTHZ_STORE_ID=01KJ9VEBX...
```

## 2. User Authentication System Verification

### 2-1. Creation of Authentication Information (Operator Information / Individual Users / Client IDs)

#### 2-1-1. Obtaining an Access Token (Client System Authentication)

Execute the following ```curl``` command to obtain client ID credentials for client authentication that has API execution permissions.

```shell
curl -i -X POST "http://app.ods.localhost/auth/token/client" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-d '{
  "client_id": "system-api-authz-client",
  "client_secret": "'$SYSTEM_CLIENT_SECRET'"
}'
```

If the authentication is successful, a JSON Web Token is issued as the response.

```json
{
    "type":"http://app.ods.localhost/api/success",
    "title":"Request processed successfully",
    "status":200,
    "detail":"time_stamp:2025-12-23T07:53:53.4617897Z, method:POST",
    "data":{
        "access_token":"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJPSm53WkFWOEVOMTQxVTlPLWxqYzNCY3l3R0ZvZlo1eGF3TGlYN2gyUUdzIn0.eyJleHAiOjE3NjY0NzgyMzMsImlhdCI6MTc2NjQ3NjQzMywianRpIjoidHJydGNjOjBiYTBmMjE5LTMwODAtNDFhNy1iYjJiLTFlMTgwZTcwNzdlMyIsImlzcyI6Imh0dHA6Ly9ob3N0LmRvY2tlci5pbnRlcm5hbDo4MDkxL3JlYWxtcy9tYXN0ZXIiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiMjI5NWU3ODctYTFmOC00NTQyLTkxZGUtYWVjM2ZjYmJhM2VkIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoic3lzdGVtLWF1dGgtc2FtcGxlIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkZWZhdWx0LXJvbGVzLW1hc3RlciIsIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6ImVtYWlsIHByb2ZpbGUiLCJvcGVuX3N5c3RlbV9pZCI6Im9wZW5fc3lzdGVtX2lkX3NhbXBsZSIsImNsaWVudEhvc3QiOiIxNzIuMjQuMC4xIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJzZXJ2aWNlLWFjY291bnQtc3lzdGVtLWF1dGgtc2FtcGxlIiwiY2xpZW50QWRkcmVzcyI6IjE3Mi4yNC4wLjEiLCJjbGllbnRfaWQiOiJzeXN0ZW0tYXV0aC1zYW1wbGUifQ.KLEm78bbr9FgIQLvoo2ufceRcTD_tK2-YWhpPnvqtZAzdkrBAP9bxuhBsfNAX4D_ML8liAgwy7Wm7KtZQ8xiycy7tkHZpKwSctBnwnRYGriSUwPNBzPj6chKG1-eM8O-_CpxWhc7Ma_w38cKwLnkHt4B0aX80-kUUQ2vsa11tF352W_9sBGBBDdADH7puQH479bDrp7D0ZHf1p80XUzNjysqiuEoZK1gmwSIxjpvfTtUcr6XvYg94ZNZqkbhl3mYh6U5_tFMUrQQsdRzVkxYwx_JajlFnKA-6I2oL32u3-0_kimI7ECVCMfzs6FtlGPu-2i8srdQgkj6RKVe6tWQpw",
        "expires_in":300,
        "token_type":"Bearer",
        "not_before_policy":0,
        "scope":"email profile"
    }
}
```

Store the obtained access token `access_token` in a variable.

```shell
ACCESS_TOKEN=eyJhbGciOi...
```

**Notes on Handling Access Tokens**
The validity period of an access token is 300 seconds (Keycloak default configuration).  
Procedures that require an access token must be executed within the validity period after obtaining the token.  
If the access token has expired, re-execute this procedure to obtain a new access token.

#### 2-1-2. Operator Information Registration

**This procedure requires the access token obtained in 2-1-1 Obtaining an Access Token (Client System Authentication).**  
Execute the following ```curl``` command to register operator information.

```shell
curl -i -X POST "http://app.ods.localhost/account/operator" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-H "Authorization: Bearer $ACCESS_TOKEN" \
-d '{
  "login_user_id": "login_user_id_sample",
  "operator_name": "Sample Corporation",
  "operator_address": "Exam Prefecture, Sample City, Exam Building 1F",
  "open_operator_id": "1234567890120",
  "global_operator_id": "123456789TT234567890",
  "effective_start_date": "2000-01-01",
  "effective_end_date": "9999-12-31",
  "create_password_flag": true,
  "password_temporary_flag": false
}'
```

The created operator information is returned.

```json
{
    "type":"http://app.ods.localhost/api/created",
    "title":"Request processed successfully",
    "status":201,
    "detail":"time_stamp:2025-12-23T05:40:16.1766437Z, method:POST",
    "data":{
        "login_user_id":"login_user_id_sample",
        "operator_id":"9fab450b-49be-4041-aad3-2c4f2f73f095",
        "operator_name":"Sample Corporation",
        "operator_address":"Exam Prefecture, Sample City, Exam Building 1F",
        "open_operator_id":"1234567890123",
        "global_operator_id":"123456789TT234567890",
        "effective_start_date":"2000-01-01",
        "effective_end_date":"9999-12-31",
        "deleted_flag":false,
        "created_at":"2025-12-23T05:40:16.076Z",
        "updated_at":"2025-12-23T05:40:16.076Z",
        "password":"Vz2&3PrnZcXY"
    }
}
```

Store the created operator identifier `operator_id` and password `password` in variables.
Since the password contains special characters, be sure to enclose it in single quotes (`'`).

```shell
OPERATOR_ID=db4c6c93-25bd-4117-99ac-5407c577e2c9
OPERATOR_PASSWORD='Vz2&3PrnZcXY'
```

#### 2-1-3. Retrieve Operator Information

**This procedure requires the access token obtained in 2-1-1 Obtaining an Access Token (Client System Authentication).**  
Execute the following ```curl``` command to retrieve operator information.

```shell
curl -i -X GET "http://app.ods.localhost/account/operator/$OPERATOR_ID" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-H "Authorization: Bearer $ACCESS_TOKEN"
```

The operator information specified in the path parameter is returned.

```json
{
    "type":"http://app.ods.localhost/api/success",
    "title":"Request processed successfully",
    "status":200,
    "detail":"time_stamp:2025-12-23T05:45:48.3628139Z, method:GET",
    "data":{
        "operator_id":"9fab450b-49be-4041-aad3-2c4f2f73f095",
        "operator_name":"Sample Corporation",
        "operator_address":"Exam Prefecture, Sample City, Exam Building 1F",
        "open_operator_id":"1234567890123",
        "global_operator_id":"123456789TT234567890",
        "effective_start_date":"2000-01-01",
        "effective_end_date":"9999-12-31",
        "deleted_flag":false,
        "created_at":"2025-12-23T05:40:16.076Z",
        "updated_at":"2025-12-23T05:40:16.076Z"
    }
}
```

#### 2-1-4. Issuing an Operator Client ID

**This procedure requires the access token obtained in 2-1-1 Obtaining an Access Token (Client System Authentication).**  
Execute the following ```curl``` command to issue an operator client ID.

```shell
curl -i -X POST "http://app.ods.localhost/auth/clients" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-H "Authorization: Bearer $ACCESS_TOKEN" \
-d '{
  "flow_type": "client_credentials",
  "client_id": "login_user_client_id_sample",
  "name": "Sample Corporation Client ID",
  "description": "Sample Corporation Client ID",
  "operator_id": "'$OPERATOR_ID'",
  "open_system_id": "login_user_open_system_id_sample"
}'
```

The created client ID is returned.

```json
{
    "type":"http://app.ods.localhost/api/created",
    "title":"Request processed successfully",
    "status":201,
    "detail":"time_stamp:2026-02-25T08:12:37.5928455Z, method:POST",
    "data": {
        "client_uuid":"e8ad057e-949e-4ca8-b89d-840ba1180a2f",
        "client_id":"login_user_client_id_sample",
        "enabled":true,
        "name":"Sample Corporation Client ID",
        "description":"Sample Corporation Client ID",
        "flow_type":"client_credentials",
        "open_system_id":"login_user_open_system_id_sample",
        "operator_id":"c27a9215-6412-40e8-bd3a-e559b484bf0e"
    }
}
```

Store the UUID `client_uuid` of the created operator client ID in a variable.

```shell
OPERATOR_CLIENT_UUID=e8ad057e-949e-4ca8-b89d-840ba1180a2f
```

#### 2-1-5. Retrieving the Operator Client Secret

**This procedure requires the access token obtained in 2-1-1 Obtaining an Access Token (Client System Authentication).**  
Execute the following ```curl``` command to retrieve the operator client secret.

```shell
curl -i -X POST "http://app.ods.localhost/auth/clients/secret/$OPERATOR_CLIENT_UUID" \
-H "API-Key: tutorials-system-api-key" \
-H "Authorization: Bearer $ACCESS_TOKEN"

The client secret of the client ID specified in the path parameter is returned.

```json
{
    "type":"http://app.ods.localhost/api/success",
    "title":"Request processed successfully",
    "status":200,
    "detail":"time_stamp:2026-02-25T08:33:04.8584672Z, method:POST",
    "data": {
        "client_secret":"IgH4xifxceFgvesXICRgoACFIgmo6ZpK"
    }
}
```

Store the obtained operator client secret `client_secret` in a variable.  

```shell
OPERATOR_CLIENT_SECRET=IgH4xifxceFgvesXICRgoACFIgmo6ZpK
```

#### 2-1-6. Individual User Registration

**This procedure requires the access token obtained in 2-1-1 Obtaining an Access Token (Client System Authentication).**  
Execute the following ```curl``` command to register an individual user.

```shell
curl -i -X POST "http://app.ods.localhost/account/user" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-H "Authorization: Bearer $ACCESS_TOKEN" \
-d '{
  "login_user_id": "personal_user_id_sample",
  "create_password_flag": true,
  "password_temporary_flag": false
}'
```

The created individual user information is returned.

```json
{
    "type":"http://app.ods.localhost/api/created",
    "title":"Request processed successfully",
    "status":201,
    "detail":"time_stamp:2026-02-25T08:17:02.3635060Z, method:POST",
    "data": {
        "login_user_id":"personal_user_id_sample",
        "operator_id":"33cc0412-5ae2-4e56-b1a8-f7d7dec1495d",
        "password":"Gp9(@Qux#wm#"
    }
}
```
The individual user's password `password` will be used in the procedure
[#2-3-2-authorization-code-retrieval](#2-3-2-authorization-code-retrieval),
so be sure to keep it for later use.。

#### 2-1-7. Issuing a Client ID for the Authorization Code Flow

**This procedure requires the access token obtained in 2-1-1 Obtaining an Access Token (Client System Authentication).**  
Execute the following ```curl``` command to issue a client ID for the authorization code flow.

```shell
curl -i -X POST "http://app.ods.localhost/auth/clients" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-H "Authorization: Bearer $ACCESS_TOKEN" \
-d '{
  "flow_type": "authorization_code",
  "client_id": "authorization_code_flow_client_id_sample",
  "name": "Authorization code flow client id",
  "description": "Authorization code flow client id.",
  "redirect_uris": [
    "urn:ietf:wg:oauth:2.0:oob"
  ]
}'
```

The created client ID is returned.

```json
{
    "type":"http://app.ods.localhost/api/created",
    "title":"Request processed successfully",
    "status":201,
    "detail":"time_stamp:2026-02-25T08:25:07.4995465Z, method:POST",
    "data": {
        "client_uuid":"1948f168-9838-4460-8c5c-ce92c33aa0d9",
        "client_id":"authorization_code_flow_client_id_sample",
        "enabled":true,
        "name":"Authorization code flow client id",
        "description":"Authorization code flow client id.",
        "flow_type":"authorization_code",
        "redirect_uris": ["urn:ietf:wg:oauth:2.0:oob"]
    }
}
```

Store the UUID `client_uuid` of the created authorization code flow client ID in a variable.

```shell
AUTH_FLOW_CLIENT_UUID=1948f168-9838-4460-8c5c-ce92c33aa0d9
```


#### 2-1-8. Retrieving the Authorization Code Flow Client Secret

**This procedure requires the access token obtained in 2-1-1 Obtaining an Access Token (Client System Authentication).**  
Execute the following```curl```command to retrieve the authorization code flow client secret.

```shell
curl -i -X POST "http://app.ods.localhost/auth/clients/secret/$AUTH_FLOW_CLIENT_UUID" \
-H "API-Key: tutorials-system-api-key" \
-H "Authorization: Bearer $ACCESS_TOKEN"
```

The client secret of the client ID specified in the path parameter is returned.

```json
{
    "type":"http://app.ods.localhost/api/success",
    "title":"Request processed successfully",
    "status":200,
    "detail":"time_stamp:2026-02-25T08:33:04.8584672Z, method:POST",
    "data": {
        "client_secret":"e1iLf7YY6VGWwDFJ8Gnmf4yR7y4TLQ5D"
    }
}
```

Store the obtained authorization code flow client secret `client_secret` in a variable.

```shell
AUTH_CODE_CLIENT_SECRET=e1iLf7YY6VGWwDFJ8Gnmf4yR7y4TLQ5D
```

### 2-2. Operator Authentication (Client System Authentication)

#### Obtaining an Access Token (Operator Client ID Authentication)

Execute the following```curl```command to obtain client ID credentials for operator authentication.

```shell
curl -i -X POST "http://app.ods.localhost/auth/token/client" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-d '{
  "client_id": "login_user_client_id_sample",
  "client_secret": "'$OPERATOR_CLIENT_SECRET'"
}'
```

If the authentication is successful, a JSON Web Token is issued as the response.

```json
{
    "type":"http://app.ods.localhost/api/success",
    "title":"Request processed successfully",
    "status":200,
    "detail":"time_stamp:2026-02-25T08:57:25.5645400Z, method:POST",
    "data": {
        "access_token":"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJPaENTd0tFRDdIMFpxRTctcEdYVU9VQVY0RjRnaE41bXJqTE5adXBZODZBIn0.eyJleHAiOjE3NzIwMTAxNDUsImlhdCI6MTc3MjAwOTg0NSwianRpIjoidHJydGNjOmZmMjFhMGZjLWRlOTEtNDljNi1iNDg4LTkwYzUyZjlmYWQ5MCIsImlzcyI6Imh0dHA6Ly9pZC5vZHMubG9jYWxob3N0L3JlYWxtcy90dXRvcmlhbHMtcmVhbG0iLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiNGY1ZDNlMjItOWI3NS00ODE5LWIxODUtMzMzMmM0MWVlMDY2IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibG9naW5fdXNlcl9jbGllbnRfaWRfc2FtcGxlIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsImRlZmF1bHQtcm9sZXMtdHV0b3JpYWxzLXJlYWxtIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6ImVtYWlsIHByb2ZpbGUiLCJvcGVuX3N5c3RlbV9pZCI6ImxvZ2luX3VzZXJfb3Blbl9zeXN0ZW1faWRfc2FtcGxlIiwiY2xpZW50SG9zdCI6IjE3Mi4yNC4wLjciLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm9wZXJhdG9yX2lkIjoiYzI3YTkyMTUtNjQxMi00MGU4LWJkM2EtZTU1OWI0ODRiZjBlIiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LWxvZ2luX3VzZXJfY2xpZW50X2lkX3NhbXBsZSIsImNsaWVudEFkZHJlc3MiOiIxNzIuMjQuMC43IiwiY2xpZW50X2lkIjoibG9naW5fdXNlcl9jbGllbnRfaWRfc2FtcGxlIn0.ugUgFLwzHPnssGlS9chOwbgVj-rZmFx3oPXQPXZmMo2UtC-xFeI5PsVH8GDIfvzIZVS4kqkctuBaRa0mMZ5AmwkDU2h2SP9ZmAz7UZnecR3upN-3zCeyWj9X27knRWGVeozWH5st6vH_s1F-waLsxXqDiWUR5ox7iKoXU86n5TOq55LVCPU9Blzl2WoUl-uW-LLigEkonoX8UlTUk9EWDfthT9OVO3Np-KKPtJJNyN7QOyuELdBVJXKfe5qs7VgKJe9nbrgQLMpTE22spu2A-f7861LoX6pq5tiDBe4-59bufzA6CyAfIEXcC_UisURi7cLIrgel-lB2skf8bvquEw",
        "expires_in":300,
        "token_type":"Bearer",
        "not_before_policy":0,
        "scope":"email profile"
    }
}
```

Store the obtained access token `access_token` in a variable.

```shell
ACCESS_TOKEN=eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSl...
```

**Notes on Handling Access Tokens**  
The validity period of an access token is 300 seconds (Keycloak default configuration).  
Procedures that require an access token must be executed within the validity period after obtaining the token.  
If the access token has expired, re-execute this procedure to obtain a new access token


#### 2-2-2. Token Introspection

**This procedure requires the access token obtained in 2-2-1 Obtaining an Access Token (Operator Client ID Authentication).**  
Execute the following ```curl``` command to register operator information.


```shell
curl -i -X POST "http://app.ods.localhost/auth/token/introspect" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-d '{
  "client_id": "login_user_client_id_sample",
  "client_secret": "'$OPERATOR_CLIENT_SECRET'",
  "access_token": "'$ACCESS_TOKEN'"
}'
```

You can retrieve the token validation result.

```json
{
    "type":"http://app.ods.localhost/api/success",
    "title":"Request processed successfully",
    "status":200,
    "detail":"time_stamp:2026-02-25T09:03:33.3658397Z, method:POST",
    "data": {
        "active":true,
        "token_info": {
            "exp":1772010479,
            "iat":1772010179,
            "operator_id":"c27a9215-6412-40e8-bd3a-e559b484bf0e",
            "open_system_id":"login_user_open_system_id_sample",
            "scope":"email profile",
            "client_id":"login_user_client_id_sample",
            "token_type":"Bearer"
        }
    }
}
```

### 2-3. End-User Authentication (Authorization Code Flow)

#### 2-3-1. Retrieving the Login URL

Execute the following ```curl``` command to retrieve the login URL for the authorization code flow.  


```shell
curl -i -X POST "http://app.ods.localhost/auth/url" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-d '{
  "client_id": "authorization_code_flow_client_id_sample",
  "redirect_uri": "urn:ietf:wg:oauth:2.0:oob",
  "code_challenge": "dbMKFEVdKH4KTyJ4F8zOIrrdnopYVAD3S22SVujWrrc"
}'
```

The authorization code flow URL is returned.

```json
{
    "type":"http://app.ods.localhost/api/success",
    "title":"Request processed successfully",
    "status":200,
    "detail":"time_stamp:2026-02-25T09:05:51.4378316Z, method:POST",
    "data": {
        "url":"http://id.ods.localhost/realms/tutorials-realm/protocol/openid-connect/auth?client_id=authorization_code_flow_client_id_sample&response_type=code&scope=openid&redirect_uri=urn:ietf:wg:oauth:2.0:oob&code_challenge=dbMKFEVdKH4KTyJ4F8zOIrrdnopYVAD3S22SVujWrrc&code_challenge_method=S256"
    }
}
```

#### 2-3-2. Obtaining the Authorization Code

Access the retrieved login URL using a web browser.

```
http://id.ods.localhost/realms/tutorials-realm/protocol/openid-connect/auth?client_id=authorization_code_flow_client_id_sample&response_type=code&scope=openid&redirect_uri=urn:ietf:wg:oauth:2.0:oob&code_challenge=dbMKFEVdKH4KTyJ4F8zOIrrdnopYVAD3S22SVujWrrc&code_challenge_method=S256
```

![Login Screen](images/2-3-2-login-1.png)

Enter the login ID and password.  
- Login ID: login_user_id_sample  
- Password: Refer to the password of the user information created in the
  [2-1-6 Individual User Registration](#2-1-6-individual-user-registration) procedure  

![Enter Login ID and Password](images/2-3-2-login-2.png)

If the login is successful, an authorization code is obtained.

![Authorization Code Retrieved](images/2-3-2-authcode.png)

Store the obtained authorization code in a variable.

```shell
AUTH_CODE=6c4327b7-2a54-418b-b0ec-8d09a4df2863.61e2ee6e-1914-451e-9fb1-55cfbde3af33.addfa70e-f0f7-41d1-aad7-82a5d3ee257f
```

**Notes on Handling Authorization Codes**  
The validity period of an authorization code is 60 seconds.  
Procedures that require an authorization code must be executed within the validity period after obtaining the code.  
If the authorization code has expired, re-execute this procedure to obtain a new authorization code.

#### 2-3-3. Obtaining an Access Token (Authorization Code Flow)

**This procedure requires the authorization code obtained in 2-3-2 Obtaining the Authorization Code.**  
Execute the following ```curl``` command to obtain an access token.


```shell
curl -i -X POST "http://app.ods.localhost/auth/token" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-d '{
  "code": "'$AUTH_CODE'",
  "client_id": "authorization_code_flow_client_id_sample",
  "client_secret": "'$AUTH_CODE_CLIENT_SECRET'",
  "redirect_uri": "urn:ietf:wg:oauth:2.0:oob",
  "code_verifier": "Vrwl56OMekmWZHlLrLw_bpbOCji8fjEg2VlztPCmSA-vGNKXGKV60FzIMjxZm1u9FN3oUFX9OyGc8BqRjiN7MQ"
}'
```

If the authentication is successful, a JSON Web Token is issued as the response.

```json
{
    "type": "http://app.ods.localhost/api/success",
    "title": "Request processed successfully",
    "status": 200,
    "detail": "time_stamp:2026-01-06T04:37:43.2288075Z, method:POST",
    "data": {
        "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJPSm53WkFWOEVOMTQxVTlPLWxqYzNCY3l3R0ZvZlo1eGF3TGlYN2gyUUdzIn0.eyJleHAiOjE3Njc2NzYwNjMsImlhdCI6MTc2NzY3NDI2MywiYXV0aF90aW1lIjoxNzY3NjcyNzc5LCJqdGkiOiJvbnJ0YWM6OTUyOTc4YjEtMDMxMS00NzBkLWIxYjYtNjA2ODViODM0ZGIwIiwiaXNzIjoiaHR0cDovL2lkLm9kcy5sb2NhbGhvc3QvcmVhbG1zL21hc3RlciIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiJkYjRjNmM5My0yNWJkLTQxMTctOTlhYy01NDA3YzU3N2UyYzkiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJ1c2VyLWF1dGgtc2FtcGxlIiwic2lkIjoiNjFlMmVlNmUtMTkxNC00NTFlLTlmYjEtNTVjZmJkZTNhZjMzIiwiYWNyIjoiMCIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwOi8vZXhhbXBsZS5jb20iXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbImRlZmF1bHQtcm9sZXMtbWFzdGVyIiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIGVtYWlsIHByb2ZpbGUiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm9wZXJhdG9yX2lkIjoiZGI0YzZjOTMtMjViZC00MTE3LTk5YWMtNTQwN2M1NzdlMmM5IiwicHJlZmVycmVkX3VzZXJuYW1lIjoibG9naW5fdXNlcl9pZF9zYW1wbGUifQ.pvP6Dd0UofghaOlkrRu4kWZFZC0gZJA0evOZ6adt7lKvlyyCV06scMz9kGsOm10Klws7hJqdP2ZOAguuMZNVU1PI0Xq-EB71DhtpVj1ZaYq7j3nj4VIrzSY7nLEYIyU6rJM1bao0IRksjLpho4NTRT-O3Bx1bZaboIZz0RgbbwwG0QlVPMCf3S0hE0H_X58WiBMALH4P4I8MF1sZoW5ZXsPAuIXTguxmVry1RgBL7KuHTXZoCZxiW1-5TGNQvhhNfpRWcnDX-i9CoD2avuwU1YOeRx9XPq-tRqK-NAgW76u1JlRUKgG2bA8IqWM8GSJsarX_h2wLMQH3lPpkFE5H4A",
        "expires_in": 300,
        "token_type": "Bearer",
        "not_before_policy": 0,
        "scope": "openid email profile",
        "refresh_token": "eyJhbGciOiJIUzUxMiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJmY2E5OGIzMi04MGY3LTRlMGMtYmI4My05MjQ1MDBhNWE2ODcifQ.eyJleHAiOjE3Njc2NzYwNjMsImlhdCI6MTc2NzY3NDI2MywianRpIjoiMDYwZGE0YzUtZDgyYi00OTJmLWFiNzItY2M3NmIzYmQ1ZDE2IiwiaXNzIjoiaHR0cDovL2lkLm9kcy5sb2NhbGhvc3QvcmVhbG1zL21hc3RlciIsImF1ZCI6Imh0dHA6Ly9pZC5vZHMubG9jYWxob3N0L3JlYWxtcy9tYXN0ZXIiLCJzdWIiOiJkYjRjNmM5My0yNWJkLTQxMTctOTlhYy01NDA3YzU3N2UyYzkiLCJ0eXAiOiJSZWZyZXNoIiwiYXpwIjoidXNlci1hdXRoLXNhbXBsZSIsInNpZCI6IjYxZTJlZTZlLTE5MTQtNDUxZS05ZmIxLTU1Y2ZiZGUzYWYzMyIsInNjb3BlIjoib3BlbmlkIHdlYi1vcmlnaW5zIGJhc2ljIGVtYWlsIGFjciByb2xlcyBwcm9maWxlIn0.aGfO2IGzq2VtiPgHVIGeTnTZsNWHymVMvCIvGbCM_0VdTvssOFe3iYRFyc0fFAEb-RhcVCXdPWXqZp9rJTQaMA",
        "refresh_expires_in": 1800,
        "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJPSm53WkFWOEVOMTQxVTlPLWxqYzNCY3l3R0ZvZlo1eGF3TGlYN2gyUUdzIn0.eyJleHAiOjE3Njc2NzYwNjMsImlhdCI6MTc2NzY3NDI2MywiYXV0aF90aW1lIjoxNzY3NjcyNzc5LCJqdGkiOiIwYmE5MjQyOC1kZWQwLTQ3NDMtOTRiYS05MTRmNWFkOGNhYmYiLCJpc3MiOiJodHRwOi8vaWQub2RzLmxvY2FsaG9zdC9yZWFsbXMvbWFzdGVyIiwiYXVkIjoidXNlci1hdXRoLXNhbXBsZSIsInN1YiI6ImRiNGM2YzkzLTI1YmQtNDExNy05OWFjLTU0MDdjNTc3ZTJjOSIsInR5cCI6IklEIiwiYXpwIjoidXNlci1hdXRoLXNhbXBsZSIsInNpZCI6IjYxZTJlZTZlLTE5MTQtNDUxZS05ZmIxLTU1Y2ZiZGUzYWYzMyIsImF0X2hhc2giOiI0S2taNTZXa0Q4TXVtRzdGR1Nza3l3IiwiYWNyIjoiMCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoibG9naW5fdXNlcl9pZF9zYW1wbGUifQ.EtPmbDsAqd1fty2qkX1kFsW9EDB644yGvo3ZYefrlT8I5MuPPCTR9MeFM7SpiKzWGC8SdMFPKswCpyUZSIT8J_Rq1p3PK0OD7bKoTZxSVMvSQRkKQY7AqjB-fTELfY-Gj5j5UWPM2GIrP8qVNo1xMAZ1RDwjxOJSJxyMYKe5CORhMMkhN8fPHOuI54DEIVBK8FiVeC9X6L0qXuoO4MFgK8O51SbQGCe0po5Xs6Jj2HCp9jrH5bIRwA6M_PYWj4trYaK3dCIhCR_U_0nQgNH0ZvtopaXWEVKgMFZc0jn_Wyy-u3W3_z7N9iFveWtdKte1LnzS3WwnzmJ18W686UaaDA"
    }
}
```

#### 2-3-4. Token Introspection

**This procedure requires the access token obtained in 2-3-3 Obtaining an Access Token (Authorization Code Flow).**  
Execute the following ```curl``` command to register operator information.


```shell
curl -i -X POST "http://app.ods.localhost/auth/token/introspect" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-d '{
  "client_id": "authorization_code_flow_client_id_sample",
  "client_secret": "'$AUTH_CODE_CLIENT_SECRET'",
  "access_token": "'$ACCESS_TOKEN'"
}'
```

You can retrieve the token validation result.

```json
{
    "type": "http://app.ods.localhost/api/success",
    "title": "Request processed successfully",
    "status": 200,
    "detail": "time_stamp:2026-01-06T05:13:55.7784388Z, method:POST",
    "data": {
        "active": true,
        "token_info": {
            "exp": 1767676690,
            "iat": 1767676390,
            "operator_id": "33cc0412-5ae2-4e56-b1a8-f7d7dec1495d",
            "scope": "openid email profile",
            "client_id": "authorization_code_flow_client_id_sample",
            "token_type": "Bearer"
        }
    }
}
```

### 2-4. Using Authorization Features

#### 2-4-1. Obtaining an Access Token (Client System Authentication)

Execute the following ```curl``` command to obtain client ID credentials for client authentication that has API execution permissions.


```shell
curl -i -X POST "http://app.ods.localhost/auth/token/client" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-d '{
  "client_id": "system-api-authz-client",
  "client_secret": "'$SYSTEM_CLIENT_SECRET'"
}'
```

If the authentication is successful, a JSON Web Token is issued as the response.

```json
{
    "type":"http://app.ods.localhost/api/success",
    "title":"Request processed successfully",
    "status":200,
    "detail":"time_stamp:2025-12-23T07:53:53.4617897Z, method:POST",
    "data":{
        "access_token":"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJPSm53WkFWOEVOMTQxVTlPLWxqYzNCY3l3R0ZvZlo1eGF3TGlYN2gyUUdzIn0.eyJleHAiOjE3NjY0NzgyMzMsImlhdCI6MTc2NjQ3NjQzMywianRpIjoidHJydGNjOjBiYTBmMjE5LTMwODAtNDFhNy1iYjJiLTFlMTgwZTcwNzdlMyIsImlzcyI6Imh0dHA6Ly9ob3N0LmRvY2tlci5pbnRlcm5hbDo4MDkxL3JlYWxtcy9tYXN0ZXIiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiMjI5NWU3ODctYTFmOC00NTQyLTkxZGUtYWVjM2ZjYmJhM2VkIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoic3lzdGVtLWF1dGgtc2FtcGxlIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkZWZhdWx0LXJvbGVzLW1hc3RlciIsIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6ImVtYWlsIHByb2ZpbGUiLCJvcGVuX3N5c3RlbV9pZCI6Im9wZW5fc3lzdGVtX2lkX3NhbXBsZSIsImNsaWVudEhvc3QiOiIxNzIuMjQuMC4xIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJzZXJ2aWNlLWFjY291bnQtc3lzdGVtLWF1dGgtc2FtcGxlIiwiY2xpZW50QWRkcmVzcyI6IjE3Mi4yNC4wLjEiLCJjbGllbnRfaWQiOiJzeXN0ZW0tYXV0aC1zYW1wbGUifQ.KLEm78bbr9FgIQLvoo2ufceRcTD_tK2-YWhpPnvqtZAzdkrBAP9bxuhBsfNAX4D_ML8liAgwy7Wm7KtZQ8xiycy7tkHZpKwSctBnwnRYGriSUwPNBzPj6chKG1-eM8O-_CpxWhc7Ma_w38cKwLnkHt4B0aX80-kUUQ2vsa11tF352W_9sBGBBDdADH7puQH479bDrp7D0ZHf1p80XUzNjysqiuEoZK1gmwSIxjpvfTtUcr6XvYg94ZNZqkbhl3mYh6U5_tFMUrQQsdRzVkxYwx_JajlFnKA-6I2oL32u3-0_kimI7ECVCMfzs6FtlGPu-2i8srdQgkj6RKVe6tWQpw",
        "expires_in":300,
        "token_type":"Bearer",
        "not_before_policy":0,
        "scope":"email profile"
    }
}
```

Store the obtained access token `access_token` in a variable.

```shell
ACCESS_TOKEN=eyJhbGciOi...
```

**Notes on Handling Access Tokens**
The validity period of an access token is 300 seconds (Keycloak default configuration).  
Procedures that require an access token must be executed within the validity period after obtaining the token.  
If the access token has expired, re-execute this procedure to obtain a new access token.

#### 2-4-2. Registering API Execution Authorization Tuples

**This procedure requires the access token obtained in 2-4-1 Obtaining an Access Token (Client System Authentication).**  

Execute the following ```curl``` command to register API execution authorization tuples for the created operator.  
For details on the ODS authorization model and tuples, see [here](../openfga/Readme_en.md).

```shell
curl -i -X POST "http://app.ods.localhost/authz/stores/$API_AUTHZ_STORE_ID/write" \
-H "Content-Type: application/json" \
-H "API-Key: API-Key-Sample" \
-H "Authorization: Bearer $ACCESS_TOKEN" \
-d '{
   "writes": {
        "tuple_keys": [
            {
                "user": "user:'$OPERATOR_ID'",
                "relation": "member",
                "object": "role:authz-stores-admin"
            },
            {
                "user": "user:'$OPERATOR_ID'",
                "relation": "member",
                "object": "role:authz-models-admin"
            },
            {
                "user": "user:'$OPERATOR_ID'",
                "relation": "member",
                "object": "role:authz-tuples-admin"
            }
        ],
        "on_duplicate": "ignore"
   }
}'
```

If `"status": 200` is returned, the registration has been completed successfully.

```json
{
    "type":"http://app.ods.localhost/api/success",
    "title":"Request processed successfully",
    "status":200,
    "detail":"time_stamp:2025-12-24T06:25:03.4964760Z, method:POST",
 

```

#### 2-4-3. Obtaining an Access Token (Operator Client ID Authentication)

Execute the following ```curl``` command to obtain client ID credentials for operator authentication.

```shell
curl -i -X POST "http://app.ods.localhost/auth/token/client" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-d '{
  "client_id": "login_user_client_id_sample",
  "client_secret": "'$OPERATOR_CLIENT_SECRET'"
}'
```

If the authentication is successful, a JSON Web Token is issued as the response.

```json
{
    "type":"http://app.ods.localhost/api/success",
    "title":"Request processed successfully",
    "status":200,
    "detail":"time_stamp:2026-02-25T08:57:25.5645400Z, method:POST",
    "data": {
        "access_token":"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJPaENTd0tFRDdIMFpxRTctcEdYVU9VQVY0RjRnaE41bXJqTE5adXBZODZBIn0.eyJleHAiOjE3NzIwMTAxNDUsImlhdCI6MTc3MjAwOTg0NSwianRpIjoidHJydGNjOmZmMjFhMGZjLWRlOTEtNDljNi1iNDg4LTkwYzUyZjlmYWQ5MCIsImlzcyI6Imh0dHA6Ly9pZC5vZHMubG9jYWxob3N0L3JlYWxtcy90dXRvcmlhbHMtcmVhbG0iLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiNGY1ZDNlMjItOWI3NS00ODE5LWIxODUtMzMzMmM0MWVlMDY2IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibG9naW5fdXNlcl9jbGllbnRfaWRfc2FtcGxlIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsImRlZmF1bHQtcm9sZXMtdHV0b3JpYWxzLXJlYWxtIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6ImVtYWlsIHByb2ZpbGUiLCJvcGVuX3N5c3RlbV9pZCI6ImxvZ2luX3VzZXJfb3Blbl9zeXN0ZW1faWRfc2FtcGxlIiwiY2xpZW50SG9zdCI6IjE3Mi4yNC4wLjciLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm9wZXJhdG9yX2lkIjoiYzI3YTkyMTUtNjQxMi00MGU4LWJkM2EtZTU1OWI0ODRiZjBlIiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LWxvZ2luX3VzZXJfY2xpZW50X2lkX3NhbXBsZSIsImNsaWVudEFkZHJlc3MiOiIxNzIuMjQuMC43IiwiY2xpZW50X2lkIjoibG9naW5fdXNlcl9jbGllbnRfaWRfc2FtcGxlIn0.ugUgFLwzHPnssGlS9chOwbgVj-rZmFx3oPXQPXZmMo2UtC-xFeI5PsVH8GDIfvzIZVS4kqkctuBaRa0mMZ5AmwkDU2h2SP9ZmAz7UZnecR3upN-3zCeyWj9X27knRWGVeozWH5st6vH_s1F-waLsxXqDiWUR5ox7iKoXU86n5TOq55LVCPU9Blzl2WoUl-uW-LLigEkonoX8UlTUk9EWDfthT9OVO3Np-KKPtJJNyN7QOyuELdBVJXKfe5qs7VgKJe9nbrgQLMpTE22spu2A-f7861LoX6pq5tiDBe4-59bufzA6CyAfIEXcC_UisURi7cLIrgel-lB2skf8bvquEw",
        "expires_in":300,
        "token_type":"Bearer",
        "not_before_policy":0,
        "scope":"email profile"
    }
}
```

Store the obtained access token `access_token` in a variable.

```shell
ACCESS_TOKEN=eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSl...
```

**Notes on Handling Access Tokens**
The validity period of an access token is 300 seconds (Keycloak default configuration).  
Procedures that require an access token must be executed within the validity period after obtaining the token.  
If the access token has expired, re-execute this procedure to obtain a new access token.

#### 2-4-4. Store Registration

**This procedure requires the access token obtained in 2-4-3. Obtaining an Access Token (Operator Client ID Authentication).**  

Execute the following ```curl``` command to register a store.

```shell
curl -i -X POST "http://app.ods.localhost/authz/stores" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-H "Authorization: Bearer $ACCESS_TOKEN" \
-d '{
  "name": "Tutorials store",
  "description": "Tutorials store"
}'
```

Once the registration is complete, the store information is returned.

```json
{
    "type":"http://app.ods.localhost/api/created",
    "title":"Request processed successfully",
    "status":201,
    "detail":"time_stamp:2026-02-25T09:41:21.4495845Z, method:POST",
    "data": {
        "id":"01KJA2SAVQ7WWJTMBP4QS0EEC0",
        "name":"Tutorials store",
        "created_at":"2026-02-25T09:41:21.401172Z",
        "updated_at":"2026-02-25T09:41:21.401172Z"
    }
}
```

Store the created store ID `01KJA2SAVQ7WWJTMBP4QS0EEC0` in a variable.

```shell
STORE_ID=01KJA2SAVQ7WWJTMBP4QS0EEC0
```

#### 2-4-5. Model Registration

**This procedure requires the access token obtained in 2-4-3. Obtaining an Access Token (Operator Client ID Authentication).**  

Execute the following ```curl``` command to register a model.


```shell
curl -i -X POST "http://app.ods.localhost/authz/stores/$STORE_ID/authorization-models" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-H "Authorization: Bearer $ACCESS_TOKEN" \
-d '{
  "schema_version": "1.1",
  "type_definitions": [
    {
      "type": "user",
      "relations": {},
      "metadata": null
    },
    {
      "type": "resource",
      "relations": {
        "can_access": {
          "this": {}
        }
      },
      "metadata": {
        "relations": {
          "can_access": {
            "directly_related_user_types": [
              {
                "type": "user"
              }
            ]
          }
        }
      }
    }
  ]
}'
```

Once the registration is complete, the model version information is returned.

```json
{
    "type":"http://app.ods.localhost/api/created",
    "title":"Request processed successfully",
    "status":201,
    "detail":"time_stamp:2026-02-25T09:47:55.0518723Z, method:POST",
    "data": {
        "authorization_model_id":"01KJA35B950KW2ZP5G601C7R2C"
    }
}
```

#### 2-4-6. Tuple Registration

**This procedure requires the access token obtained in 2-4-3. Obtaining an Access Token (Operator Client ID Authentication).**  

Execute the following ```curl``` command to register tuples.

```shell
curl -i -X POST "http://app.ods.localhost/authz/stores/$STORE_ID/write" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-H "Authorization: Bearer $ACCESS_TOKEN" \
-d '{
   "writes": {
        "tuple_keys": [
            {
                "user": "user:'$OPERATOR_ID'",
                "relation": "can_access",
                "object": "resource:tutorials-resource-sample"
            }
        ]
   }
}'
```

If `"status": 200` is returned, the tuple registration has been completed successfully

```json
{
    "type":"http://app.ods.localhost/api/success",
    "title":"Request processed successfully",
    "status":200,
    "detail":"time_stamp:2025-12-24T06:25:03.4964760Z, method:POST",
    "data":{}
}
```

#### 2-4-7. Authorization Decision

**This procedure requires the access token obtained in 2-4-3. Obtaining an Access Token (Operator Client ID Authentication).**  

Execute the following ```curl``` command to perform an authorization decision using the registered model and tuples.

```shell
curl -i -X POST "http://app.ods.localhost/authz/stores/$STORE_ID/access/v1/evaluation" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-H "Authorization: Bearer $ACCESS_TOKEN" \
-d '{
  "subject": {
    "type": "user",
    "id": "'$OPERATOR_ID'"
  },
  "resource": {
    "type": "resource",
    "id": "tutorials-resource-sample"
  },
  "action": {
    "name": "can_access"
  }
}'
```

The authorization decision result is returned.

```json
{
    "type":"http://app.ods.localhost/api/success",
    "title":"Request processed successfully",
    "status":200,
    "detail":"time_stamp:2026-02-25T09:55:49.7757046Z, method:POST",
    "data": {
        "decision":true
    }
}
```

## FAQ
For the FAQ of the reference implementation tutorial, see [here](./tutorialfaq_en.md).
