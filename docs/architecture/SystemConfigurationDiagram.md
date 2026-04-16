# Open Data Spaces L3 Authenticator System Configuration Diagram

## ODS基盤 L3ユーザ認証システム構成図

```mermaid
flowchart LR
  subgraph UC["UC"]
    APP["アプリ<br>(ブラウザ、APサーバ等)"]
  end

  subgraph ODSL3["ODS L3 ｱｲﾃﾞﾝﾃｨﾃｨｺﾝﾎﾟｰﾈﾝﾄ"]
    ODS_AUTH["認証・認可サーバ<br>(Spring Boot)<br><br>"]
    OpenFGA["認可サーバ<br>(OpenFGA)"]
    KEYCLOAK["認証・認可サーバ<br>(Keycloak)"]
    DB_ODS["DBサーバ<br>(postgres)"]
    DB_OPENFGA["OpenFGA DBサーバ<br>(postgres)<br> "]
    DB_KEYCLOAK["Keycloak DBサーバ<br>(postgres)<br>"]
  end

  %% アプリ → ODS_AUTH
  APP <-->|internet| ODS_AUTH

  %% ODS_AUTHが使用するサーバ
  ODS_AUTH <--> OpenFGA
  ODS_AUTH <--> KEYCLOAK
  ODS_AUTH <--> DB_ODS

  %% OpenFGA,Keycloakが使用するDB
  OpenFGA <--> DB_OPENFGA
  KEYCLOAK <--> DB_KEYCLOAK
```

### 用語説明
|名前                                        |意味|
|:-------------------------------------------|:----|
|UC|ユースケース（事業者・個人）|
|ODS|OPEN-DATASPACESの略称|
|L3|ODSで定められているアイデンティティレイヤ、認証・認可の問題を解決する|
