# Open Data Spaces L3 Authenticator System Configuration Diagram

## ODS Platform L3 User Authentication System Configuration Diagram

```mermaid
flowchart LR
  subgraph UC["UC"]
    APP["Application<br>(Browser, Application Server, etc.)"]
  end

  subgraph ODSL3["ODS L3 Identity Component"]
    ODS_AUTH["Authentication / Authorization Server<br>(Spring Boot)<br><br>"]
    OpenFGA["Authorization Server<br>(OpenFGA)"]
    KEYCLOAK["Authentication / Authorization Server<br>(Keycloak)"]
    DB_ODS["DB Server<br>(PostgreSQL)"]
    DB_OPENFGA["OpenFGA DB Server<br>(PostgreSQL)<br> "]
    DB_KEYCLOAK["Keycloak DB Server<br>(PostgreSQL)<br>"]
  end

  %% Application → ODS_AUTH
  APP <-->|internet| ODS_AUTH

  %% Servers used by ODS_AUTH
  ODS_AUTH <--> OpenFGA
  ODS_AUTH <--> KEYCLOAK
  ODS_AUTH <--> DB_ODS

  %% Databases used by OpenFGA and Keycloak
  OpenFGA <--> DB_OPENFGA
  KEYCLOAK <--> DB_KEYCLOAK
```

### Terminology
|Name                                        |Description|
|:-------------------------------------------|:----|
|UC|Use Case (Business Operator or Individual)|
|ODS|Abbreviation for OPEN-DATASPACES|
|L3|Identity layer defined in ODS that resolves authentication and authorization issues|