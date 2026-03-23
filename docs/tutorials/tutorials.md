# 参考実装チュートリアル

ODS L3 ユーザ認証システムの参考実装・動作確認の例を示します。

## 目次
<!-- Update Index `npx doctoc docs/tutorials/README.md` -->
<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [前提条件](#%E5%89%8D%E6%8F%90%E6%9D%A1%E4%BB%B6)
- [構成図](#%E6%A7%8B%E6%88%90%E5%9B%B3)
- [1. データセットアップ](#1-%E3%83%87%E3%83%BC%E3%82%BF%E3%82%BB%E3%83%83%E3%83%88%E3%82%A2%E3%83%83%E3%83%97)
  - [1-1. チュートリアルデータ投入](#1-1-%E3%83%81%E3%83%A5%E3%83%BC%E3%83%88%E3%83%AA%E3%82%A2%E3%83%AB%E3%83%87%E3%83%BC%E3%82%BF%E6%8A%95%E5%85%A5)
- [2. ユーザ認証システム動作確認](#2-%E3%83%A6%E3%83%BC%E3%82%B6%E8%AA%8D%E8%A8%BC%E3%82%B7%E3%82%B9%E3%83%86%E3%83%A0%E5%8B%95%E4%BD%9C%E7%A2%BA%E8%AA%8D)
  - [2-1. 認証情報の作成（事業者情報/個人ユーザ/クライアントID）](#2-1-%E8%AA%8D%E8%A8%BC%E6%83%85%E5%A0%B1%E3%81%AE%E4%BD%9C%E6%88%90%E4%BA%8B%E6%A5%AD%E8%80%85%E6%83%85%E5%A0%B1%E5%80%8B%E4%BA%BA%E3%83%A6%E3%83%BC%E3%82%B6%E3%82%AF%E3%83%A9%E3%82%A4%E3%82%A2%E3%83%B3%E3%83%88id)
    - [2-1-1. アクセストークン取得（クライアントシステム認証）](#2-1-1-%E3%82%A2%E3%82%AF%E3%82%BB%E3%82%B9%E3%83%88%E3%83%BC%E3%82%AF%E3%83%B3%E5%8F%96%E5%BE%97%E3%82%AF%E3%83%A9%E3%82%A4%E3%82%A2%E3%83%B3%E3%83%88%E3%82%B7%E3%82%B9%E3%83%86%E3%83%A0%E8%AA%8D%E8%A8%BC)
    - [2-1-2. 事業者情報登録](#2-1-2-%E4%BA%8B%E6%A5%AD%E8%80%85%E6%83%85%E5%A0%B1%E7%99%BB%E9%8C%B2)
    - [2-1-3. 事業者情報取得](#2-1-3-%E4%BA%8B%E6%A5%AD%E8%80%85%E6%83%85%E5%A0%B1%E5%8F%96%E5%BE%97)
    - [2-1-4. 事業者クライアントID発行](#2-1-4-%E4%BA%8B%E6%A5%AD%E8%80%85%E3%82%AF%E3%83%A9%E3%82%A4%E3%82%A2%E3%83%B3%E3%83%88id%E7%99%BA%E8%A1%8C)
    - [2-1-5. 事業者クライアントシークレット取得](#2-1-5-%E4%BA%8B%E6%A5%AD%E8%80%85%E3%82%AF%E3%83%A9%E3%82%A4%E3%82%A2%E3%83%B3%E3%83%88%E3%82%B7%E3%83%BC%E3%82%AF%E3%83%AC%E3%83%83%E3%83%88%E5%8F%96%E5%BE%97)
    - [2-1-6. 個人ユーザ登録](#2-1-6-%E5%80%8B%E4%BA%BA%E3%83%A6%E3%83%BC%E3%82%B6%E7%99%BB%E9%8C%B2)
    - [2-1-7. 認可コードフロー用クライアントID発行](#2-1-7-%E8%AA%8D%E5%8F%AF%E3%82%B3%E3%83%BC%E3%83%89%E3%83%95%E3%83%AD%E3%83%BC%E7%94%A8%E3%82%AF%E3%83%A9%E3%82%A4%E3%82%A2%E3%83%B3%E3%83%88id%E7%99%BA%E8%A1%8C)
    - [2-1-8. 認可コードフロークライアントシークレット取得](#2-1-8-%E8%AA%8D%E5%8F%AF%E3%82%B3%E3%83%BC%E3%83%89%E3%83%95%E3%83%AD%E3%83%BC%E3%82%AF%E3%83%A9%E3%82%A4%E3%82%A2%E3%83%B3%E3%83%88%E3%82%B7%E3%83%BC%E3%82%AF%E3%83%AC%E3%83%83%E3%83%88%E5%8F%96%E5%BE%97)
  - [2-2. 事業者認証（クライアントシステム認証）](#2-2-%E4%BA%8B%E6%A5%AD%E8%80%85%E8%AA%8D%E8%A8%BC%E3%82%AF%E3%83%A9%E3%82%A4%E3%82%A2%E3%83%B3%E3%83%88%E3%82%B7%E3%82%B9%E3%83%86%E3%83%A0%E8%AA%8D%E8%A8%BC)
    - [2-2-1. アクセストークン取得（事業者クライアントID認証）](#2-2-1-%E3%82%A2%E3%82%AF%E3%82%BB%E3%82%B9%E3%83%88%E3%83%BC%E3%82%AF%E3%83%B3%E5%8F%96%E5%BE%97%E4%BA%8B%E6%A5%AD%E8%80%85%E3%82%AF%E3%83%A9%E3%82%A4%E3%82%A2%E3%83%B3%E3%83%88id%E8%AA%8D%E8%A8%BC)
    - [2-2-2. トークンイントロスペクション](#2-2-2-%E3%83%88%E3%83%BC%E3%82%AF%E3%83%B3%E3%82%A4%E3%83%B3%E3%83%88%E3%83%AD%E3%82%B9%E3%83%9A%E3%82%AF%E3%82%B7%E3%83%A7%E3%83%B3)
  - [2-3. ユーザ当人認証（認可コードフロー）](#2-3-%E3%83%A6%E3%83%BC%E3%82%B6%E5%BD%93%E4%BA%BA%E8%AA%8D%E8%A8%BC%E8%AA%8D%E5%8F%AF%E3%82%B3%E3%83%BC%E3%83%89%E3%83%95%E3%83%AD%E3%83%BC)
    - [2-3-1. ログインURL取得](#2-3-1-%E3%83%AD%E3%82%B0%E3%82%A4%E3%83%B3url%E5%8F%96%E5%BE%97)
    - [2-3-2. 認可コード取得](#2-3-2-%E8%AA%8D%E5%8F%AF%E3%82%B3%E3%83%BC%E3%83%89%E5%8F%96%E5%BE%97)
    - [2-3-3. アクセストークン取得（認可コードフロー）](#2-3-3-%E3%82%A2%E3%82%AF%E3%82%BB%E3%82%B9%E3%83%88%E3%83%BC%E3%82%AF%E3%83%B3%E5%8F%96%E5%BE%97%E8%AA%8D%E5%8F%AF%E3%82%B3%E3%83%BC%E3%83%89%E3%83%95%E3%83%AD%E3%83%BC)
    - [2-3-4. トークンイントロスペクション](#2-3-4-%E3%83%88%E3%83%BC%E3%82%AF%E3%83%B3%E3%82%A4%E3%83%B3%E3%83%88%E3%83%AD%E3%82%B9%E3%83%9A%E3%82%AF%E3%82%B7%E3%83%A7%E3%83%B3)
  - [2-4. 認可機能利用](#2-4-%E8%AA%8D%E5%8F%AF%E6%A9%9F%E8%83%BD%E5%88%A9%E7%94%A8)
    - [2-4-1. アクセストークン取得（クライアントシステム認証）](#2-4-1-%E3%82%A2%E3%82%AF%E3%82%BB%E3%82%B9%E3%83%88%E3%83%BC%E3%82%AF%E3%83%B3%E5%8F%96%E5%BE%97%E3%82%AF%E3%83%A9%E3%82%A4%E3%82%A2%E3%83%B3%E3%83%88%E3%82%B7%E3%82%B9%E3%83%86%E3%83%A0%E8%AA%8D%E8%A8%BC)
    - [2-4-2. API実行権限タプル登録](#2-4-2-api%E5%AE%9F%E8%A1%8C%E6%A8%A9%E9%99%90%E3%82%BF%E3%83%97%E3%83%AB%E7%99%BB%E9%8C%B2)
    - [2-4-3. アクセストークン取得（事業者クライアントID認証）](#2-4-3-%E3%82%A2%E3%82%AF%E3%82%BB%E3%82%B9%E3%83%88%E3%83%BC%E3%82%AF%E3%83%B3%E5%8F%96%E5%BE%97%E4%BA%8B%E6%A5%AD%E8%80%85%E3%82%AF%E3%83%A9%E3%82%A4%E3%82%A2%E3%83%B3%E3%83%88id%E8%AA%8D%E8%A8%BC)
    - [2-4-4. ストア登録](#2-4-4-%E3%82%B9%E3%83%88%E3%82%A2%E7%99%BB%E9%8C%B2)
    - [2-4-5. モデル登録](#2-4-5-%E3%83%A2%E3%83%87%E3%83%AB%E7%99%BB%E9%8C%B2)
    - [2-4-6. タプル登録](#2-4-6-%E3%82%BF%E3%83%97%E3%83%AB%E7%99%BB%E9%8C%B2)
    - [2-4-7. 認可判定](#2-4-7-%E8%AA%8D%E5%8F%AF%E5%88%A4%E5%AE%9A)
- [FAQ](#FAQ)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## 前提条件

- [1-1 docker-compose を用いたサービスの起動](../../README.md#1-1-docker-compose-を用いたサービスの起動)にて、以下のサービスの起動が完了していること。
  - PostgreSQL : データベースサービス
  - Keycloak : 認証サービス
  - OpenFGA : 認可サービス
- bash/curl/psql コマンドを実行できる環境があること
  - curlコマンドにて、Keycloak/OpenFGAへ接続できること
  - psqlコマンドにて、PostgreSQLへ接続できること

## 構成図

本チュートリアルで使用する構成図を以下に示します。

```mermaid
flowchart LR
  subgraph HostClient["ホスト"]
    Client["HTTPクライアント<br>(ブラウザ等)"]
    HostPort["*.localhost:80<br>（ポートマッピング 127.0.0.1:80 → コンテナ:80）"]
  end

  subgraph DockerNet["shared-network-ods (external)"]
    NGINX["nginx<br>(ods-reverse-proxy)<br>container:80<br>aliases: id.ods.localhost, authz.ods.localhost"]
    ODSAPP["ods-app<br>container:8080"]
    KEYCLOAK["Keycloak<br>container:8080"]
    OPENFGA["OpenFGA<br>container:8080"]
    DB_KEYCLOAK["postgres<br>(Keycloak DB)<br>container:5432"]
    DB_OPENFGA["postgres<br>(OpenFGA DB)<br>container:5432"]
  end

  %% ホスト → nginx（ポート80マッピング経由）
  Client --> HostPort
  HostPort -->|app.ods.localhost<br>id.ods.localhost<br>authz.ods.localhost| NGINX

  %% ホスト経由でのルーティング（nginx が各サービスへプロキシ）
  NGINX -->|proxy: app.ods.localhost| ODSAPP
  NGINX -->|proxy: id.ods.localhost| KEYCLOAK
  NGINX -->|proxy: authz.ods.localhost| OPENFGA

  %% コンテナ内部からのアクセス（名前解決は nginx の aliases に向く）
  ODSAPP -->|"aliases: id.ods.localhost"| NGINX
  ODSAPP -->|"aliases: authz.ods.localhost"| NGINX
  NGINX -->|"aliases: id.ods.localhost"| KEYCLOAK
  NGINX -->|"aliases: authz.ods.localhost"| OPENFGA

  %% DB 依存関係
  ODSAPP -->|depends_on| DB_KEYCLOAK
  KEYCLOAK -->|depends_on| DB_KEYCLOAK
  OPENFGA -->|depends_on| DB_OPENFGA
```

## 1. データセットアップ

### 1-1. チュートリアルデータ投入
以下コマンドを実行して、Keycloak/OpenFGA/Postgresにチュートリアルデータを投入します。

```shell
# ディレクトリ移動（リポジトリのルートディレクトリから）
cd ./scripts

# スクリプトファイルを実行
bash ./setup.sh ../docs/tutorials/env/tutorials.env
```

セットアップが完了すると、以下メッセージが表示されます。  

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

取得した `API Authorization client secret in Keycloak` `API Authorization store ID in OpenFGA` を変数に格納します。

```shell
# API Authorization client secret in Keycloak
SYSTEM_CLIENT_SECRET=BvAvAUh...
# API Authorization store ID in OpenFGA
API_AUTHZ_STORE_ID=01KJ9VEBX...
```

## 2. ユーザ認証システム動作確認

### 2-1. 認証情報の作成（事業者情報/個人ユーザ/クライアントID）

#### 2-1-1. アクセストークン取得（クライアントシステム認証）

下記の```curl```コマンドを実行し、API実行権限を持つクライアント認証用のクライアントID認証情報を取得します。

```shell
curl -i -X POST "http://app.ods.localhost/auth/token/client" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-d '{
  "client_id": "system-api-authz-client",
  "client_secret": "'$SYSTEM_CLIENT_SECRET'"
}'
```

認証が成功すると返却値としてjson web tokenが払い出されます。

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

取得したアクセストークン `access_token` を変数に格納します。

```shell
ACCESS_TOKEN=eyJhbGciOi...
```

**アクセストークンを取り扱う際の注意点**  
アクセストークンの有効期限は300秒(Keycloak標準設定)です。  
アクセストークンを必要とする手順は、本手順実施後、有効期限時間内に実施してください。  
アクセストークンの有効期限を超過した場合、再度本手順を実施してアクセストークンを再取得してください。

#### 2-1-2. 事業者情報登録

**本手順は2-1-1 アクセストークン取得（クライアントシステム認証）のアクセストークンが必要です**  
下記の```curl```コマンドを実行し、事業者情報を登録します。

```shell
curl -i -X POST "http://app.ods.localhost/account/operator" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-H "Authorization: Bearer $ACCESS_TOKEN" \
-d '{
  "login_user_id": "login_user_id_sample",
  "operator_name": "サンプル株式会社",
  "operator_address": "試験県サンプル市examビル1F",
  "open_operator_id": "1234567890120",
  "global_operator_id": "123456789TT234567890",
  "effective_start_date": "2000-01-01",
  "effective_end_date": "9999-12-31",
  "create_password_flag": true,
  "password_temporary_flag": false
}'
```

作成した事業者情報が返却されます。

```json
{
    "type":"http://app.ods.localhost/api/created",
    "title":"Request processed successfully",
    "status":201,"detail":"time_stamp:2025-12-23T05:40:16.1766437Z, method:POST",
    "data":{
        "login_user_id":"login_user_id_sample",
        "operator_id":"9fab450b-49be-4041-aad3-2c4f2f73f095",
        "operator_name":"サンプル株式会社",
        "operator_address":"試験県サンプル市examビル1F",
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

作成した事業者情報識別子 `operator_id` 、パスワード `password` を変数に格納します。  
パスワードには記号が含まれるため `'` で囲んでください。

```shell
OPERATOR_ID=db4c6c93-25bd-4117-99ac-5407c577e2c9
OPERATOR_PASSWORD='Vz2&3PrnZcXY'
```

#### 2-1-3. 事業者情報取得

**本手順は2-1-1 アクセストークン取得（クライアントシステム認証）のアクセストークンが必要です**  
下記の```curl```コマンドを実行し、事業者情報を取得します。

```shell
curl -i -X GET "http://app.ods.localhost/account/operator/$OPERATOR_ID" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-H "Authorization: Bearer $ACCESS_TOKEN"
```

パスパラメータに指定した事業者情報が返却されます。

```json
{
    "type":"http://app.ods.localhost/api/success",
    "title":"Request processed successfully",
    "status":200,
    "detail":"time_stamp:2025-12-23T05:45:48.3628139Z, method:GET",
    "data":{
        "operator_id":"9fab450b-49be-4041-aad3-2c4f2f73f095",
        "operator_name":"サンプル株式会社",
        "operator_address":"試験県サンプル市examビル1F",
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

#### 2-1-4. 事業者クライアントID発行

**本手順は2-1-1 アクセストークン取得（クライアントシステム認証）のアクセストークンが必要です**  
下記の```curl```コマンドを実行し、事業者クライアントIDを発行します。

```shell
curl -i -X POST "http://app.ods.localhost/auth/clients" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-H "Authorization: Bearer $ACCESS_TOKEN" \
-d '{
  "flow_type": "client_credentials",
  "client_id": "login_user_client_id_sample",
  "name": "サンプル株式会社クライアントID",
  "description": "サンプル株式会社クライアントID",
  "operator_id": "'$OPERATOR_ID'",
  "open_system_id": "login_user_open_system_id_sample"
}'
```

作成したクライアントIDが返却されます。

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
        "name":"サンプル株式会社クライアントID",
        "description":"サンプル株式会社クライアントID",
        "flow_type":"client_credentials",
        "open_system_id":"login_user_open_system_id_sample",
        "operator_id":"c27a9215-6412-40e8-bd3a-e559b484bf0e"
    }
}
```

作成した事業者クライアントIDのUUID `client_uuid` を変数に格納します。  

```shell
OPERATOR_CLIENT_UUID=e8ad057e-949e-4ca8-b89d-840ba1180a2f
```

#### 2-1-5. 事業者クライアントシークレット取得

**本手順は2-1-1 アクセストークン取得（クライアントシステム認証）のアクセストークンが必要です**  
下記の```curl```コマンドを実行し、事業者クライアントシークレットを取得します。  

```shell
curl -i -X POST "http://app.ods.localhost/auth/clients/secret/$OPERATOR_CLIENT_UUID" \
-H "API-Key: tutorials-system-api-key" \
-H "Authorization: Bearer $ACCESS_TOKEN"
```

パスパラメータで指定したクライアントIDのクライアントシークレットが返却されます。

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

取得した事業者クライアントシークレット `client_secret` を変数に格納します。  

```shell
OPERATOR_CLIENT_SECRET=IgH4xifxceFgvesXICRgoACFIgmo6ZpK
```

#### 2-1-6. 個人ユーザ登録

**本手順は2-1-1 アクセストークン取得（クライアントシステム認証）のアクセストークンが必要です**  
下記の```curl```コマンドを実行し、個人ユーザを登録します。

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

作成した個人ユーザの情報が返却されます。

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

個人ユーザのパスワード `password` は [2-3-2-認可コード取得](#2-3-2-認可コード取得) 手順で使用するため、控えておいてください。

#### 2-1-7. 認可コードフロー用クライアントID発行

**本手順は2-1-1 アクセストークン取得（クライアントシステム認証）のアクセストークンが必要です**  
下記の```curl```コマンドを実行し、認可コード用クライアントIDを発行します。

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

作成したクライアントIDが返却されます。

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

作成した認可コードフローのクライアントIDのUUID `client_uuid` を変数に格納します。  

```shell
AUTH_FLOW_CLIENT_UUID=1948f168-9838-4460-8c5c-ce92c33aa0d9
```


#### 2-1-8. 認可コードフロークライアントシークレット取得

**本手順は2-1-1 アクセストークン取得（クライアントシステム認証）のアクセストークンが必要です**  
下記の```curl```コマンドを実行し、認可コードフロークライアントシークレットを取得します。  

```shell
curl -i -X POST "http://app.ods.localhost/auth/clients/secret/$AUTH_FLOW_CLIENT_UUID" \
-H "API-Key: tutorials-system-api-key" \
-H "Authorization: Bearer $ACCESS_TOKEN"
```

パスパラメータで指定したクライアントIDのクライアントシークレットが返却されます。

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

取得した認可コードフロークライアントシークレット `client_secret` を変数に格納します。  

```shell
AUTH_CODE_CLIENT_SECRET=e1iLf7YY6VGWwDFJ8Gnmf4yR7y4TLQ5D
```

### 2-2. 事業者認証（クライアントシステム認証）

#### 2-2-1. アクセストークン取得（事業者クライアントID認証）

下記の```curl```コマンドを実行し、事業者認証用のクライアントID認証情報を取得します。

```shell
curl -i -X POST "http://app.ods.localhost/auth/token/client" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-d '{
  "client_id": "login_user_client_id_sample",
  "client_secret": "'$OPERATOR_CLIENT_SECRET'"
}'
```

認証が成功すると返却値としてjson web tokenが払い出されます。

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

取得したアクセストークン `access_token` します。

```shell
ACCESS_TOKEN=eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSl...
```

**アクセストークンを取り扱う際の注意点**  
アクセストークンの有効期限は300秒(Keycloak標準設定)です。  
アクセストークンを必要とする手順は、本手順実施後、有効期限時間内に実施してください。  
アクセストークンの有効期限を超過した場合、再度本手順を実施してアクセストークンを再取得してください。

#### 2-2-2. トークンイントロスペクション

**本手順は2-2-1 アクセストークン取得（事業者クライアントID認証）のアクセストークンが必要です**  
下記の```curl```コマンドを実行し、事業者情報を登録します。

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

トークン検証結果が取得できます。

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

### 2-3. ユーザ当人認証（認可コードフロー）

#### 2-3-1. ログインURL取得

下記の```curl```コマンドを実行し、認可コードフローのログインURLを取得します。

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

認可コードフローのURLが取得できます。

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

#### 2-3-2. 認可コード取得

取得したログインURLに、ブラウザでアクセスします。

```
http://id.ods.localhost/realms/tutorials-realm/protocol/openid-connect/auth?client_id=authorization_code_flow_client_id_sample&response_type=code&scope=openid&redirect_uri=urn:ietf:wg:oauth:2.0:oob&code_challenge=dbMKFEVdKH4KTyJ4F8zOIrrdnopYVAD3S22SVujWrrc&code_challenge_method=S256
```

![ログイン画面](images/2-3-2-login-1.png)

ログインIDとパスワードを入力します。  
- ログインID：login_user_id_sample  
- パスワード： [2-1-6 個人ユーザ登録](#2-1-6-個人ユーザ登録)手順で作成したユーザ情報のパスワードを参照  

![ログインIDとパスワードを入力](images/2-3-2-login-2.png)

ログインに成功すると、認可コードが取得できます。

![認可コード取得](images/2-3-2-authcode.png)

取得した認可コードを変数に設定します。

```shell
AUTH_CODE=6c4327b7-2a54-418b-b0ec-8d09a4df2863.61e2ee6e-1914-451e-9fb1-55cfbde3af33.addfa70e-f0f7-41d1-aad7-82a5d3ee257f
```

**認可コードを取り扱う際の注意点**  
認可コードの有効期限は60秒です。  
認可コードを必要とする手順は、本手順実施後、有効期限時間内に実施してください。  
認可コードの有効期限を超過した場合、再度本手順を実施して認可コードを再取得してください。

#### 2-3-3. アクセストークン取得（認可コードフロー）

**本手順は2-3-2 認可コード取得の認可コードが必要です**  
下記の```curl```コマンドを実行し、アクセストークンを取得します。

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

認証が成功すると返却値としてjson web tokenが払い出されます。

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

#### 2-3-4. トークンイントロスペクション

**本手順は2-3-3 アクセストークン取得（認可コードフロー）のアクセストークンが必要です**  
下記の```curl```コマンドを実行し、事業者情報を登録します。

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

トークン検証結果が取得できます。

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

### 2-4. 認可機能利用

#### 2-4-1. アクセストークン取得（クライアントシステム認証）

下記の```curl```コマンドを実行し、API実行権限を持つクライアント認証用のクライアントID認証情報を取得します。

```shell
curl -i -X POST "http://app.ods.localhost/auth/token/client" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-d '{
  "client_id": "system-api-authz-client",
  "client_secret": "'$SYSTEM_CLIENT_SECRET'"
}'
```

認証が成功すると返却値としてjson web tokenが払い出されます。

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

取得したアクセストークン `access_token` を変数に格納します。

```shell
ACCESS_TOKEN=eyJhbGciOi...
```

**アクセストークンを取り扱う際の注意点**  
アクセストークンの有効期限は300秒(Keycloak標準設定)です。  
アクセストークンを必要とする手順は、本手順実施後、有効期限時間内に実施してください。  
アクセストークンの有効期限を超過した場合、再度本手順を実施してアクセストークンを再取得してください。

#### 2-4-2. API実行権限タプル登録

**本手順は2-4-1 アクセストークン取得（クライアントシステム認証）のアクセストークンが必要です**  

下記の```curl```コマンドを実行し、作成した事業者にAPI実行権限タプルを登録します。  
ODS認可モデル・タプルについては[こちら](../openfga/Readme.md)

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

`"status":200` が応答すれば、登録は完了しています。

```json
{
    "type":"http://app.ods.localhost/api/success",
    "title":"Request processed successfully",
    "status":200,
    "detail":"time_stamp:2025-12-24T06:25:03.4964760Z, method:POST",
    "data":{}
}
```

#### 2-4-3. アクセストークン取得（事業者クライアントID認証）

下記の```curl```コマンドを実行し、事業者認証用のクライアントID認証情報を取得します。

```shell
curl -i -X POST "http://app.ods.localhost/auth/token/client" \
-H "Content-Type: application/json" \
-H "API-Key: tutorials-system-api-key" \
-d '{
  "client_id": "login_user_client_id_sample",
  "client_secret": "'$OPERATOR_CLIENT_SECRET'"
}'
```

認証が成功すると返却値としてjson web tokenが払い出されます。

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

取得したアクセストークン `access_token` を変数に格納します。

```shell
ACCESS_TOKEN=eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSl...
```

**アクセストークンを取り扱う際の注意点**  
アクセストークンの有効期限は300秒(Keycloak標準設定)です。  
アクセストークンを必要とする手順は、本手順実施後、有効期限時間内に実施してください。  
アクセストークンの有効期限を超過した場合、再度本手順を実施してアクセストークンを再取得してください。

#### 2-4-4. ストア登録

**本手順は2-4-3. アクセストークン取得（事業者クライアントID認証）のアクセストークンが必要です**  

下記の```curl```コマンドを実行し、ストアを登録します。  

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

登録が完了すると、ストア情報が返却されます。

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

作成したストアID `01KJA2SAVQ7WWJTMBP4QS0EEC0` を変数に格納します。

```shell
STORE_ID=01KJA2SAVQ7WWJTMBP4QS0EEC0
```

#### 2-4-5. モデル登録

**本手順は2-4-3. アクセストークン取得（事業者クライアントID認証）のアクセストークンが必要です**  

下記の```curl```コマンドを実行し、モデルを登録します。  

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

登録が完了すると、モデルバージョン情報が返却されます。

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

#### 2-4-6. タプル登録

**本手順は2-4-3. アクセストークン取得（事業者クライアントID認証）のアクセストークンが必要です**  

下記の```curl```コマンドを実行し、タプルを登録します。  

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

`"status":200` が応答すれば、タプル登録は完了しています。

```json
{
    "type":"http://app.ods.localhost/api/success",
    "title":"Request processed successfully",
    "status":200,
    "detail":"time_stamp:2025-12-24T06:25:03.4964760Z, method:POST",
    "data":{}
}
```

#### 2-4-7. 認可判定

**本手順は2-4-3. アクセストークン取得（事業者クライアントID認証）のアクセストークンが必要です**  

下記の```curl```コマンドを実行し、登録したモデル・タプルの認可判定を行います。

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

認可判定結果が返却されます。

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
参考実装チュートリアルのFAQは[こちら](./tutorialfaq.md)を参照してください。 