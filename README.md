## 概要・目的
本リポジトリでは、Open Data Spaces（ODS）における技術参照文書
「ODSリファレンスアーキテクチャモデル（ODS-RAM）」の参照実装のうち、アイデンティティレイヤ（L3）の中核的役割を果たすアイデンティティコンポーネントの参照実装例を公開する。

ODS-RAMの詳細については[こちら](http://open-dataspaces.gitbook.io/ods-docs/jp)を参照すること

## 基本概念
「ODS」とは、オープンで中立的なデータスペースの技術コンセプトであり、企業・業界・国境を横断したエンタープライズデータの連携と利活用を促進する。

ODSでは産業界がデータスペースの社会実装を早急に進めるためのサービスライフサイクルに焦点をおいたアーキテクチャモデルである「ODS-RAM」を公開しており、その中で認証および認可の問題を解決するレイヤとして「アイデンティティレイヤ」が定義されている。アイデンティティコンポーネントはアイデンティティレイヤにおいて中核的役割を果たすコアコンポーネントである。

## 機能概要・機能一覧
機能についてはL3のプロトコル仕様をまとめた[Open Data Spaces Protocol（ODP）](http://open-dataspaces.gitbook.io/ods-docs/jp)および[API仕様書](./docs/openapi)を参照すること。

## ディレクトリ構成

```
project-root/
├── open-data-spaces-l3-authenticator-backend/                  # アプリケーションルートディレクトリ
│   ├── config/                                                 # CheckStyleファイル
│   ├── core/src/                                               # ソースコード
│   │   ├── main/
│   │   │   ├── java/io/github/open_dataspaces/core/
│   │   │   │   ├── application/                                # アプリケーション層
│   │   │   │   ├── common/                                     # 共通機能
│   │   │   │   ├── domain/                                     # ドメイン層
│   │   │   │   ├── exception/                                  # エラーハンドリング
│   │   │   │   └── infrastructure/                             # インフラ層
│   │   │   └── resources/                                      # アプリケーション設定
│   │   │       └── META-INF/                                   # アプリケーション設定メタデータ
│   │   └── test/                                               # テストコード
│   │       ├── java/io/github/open_dataspaces/core/            # ユニットテスト
│   │       └── resources/                                      # テスト用設定
│   └── docker/                                                 # コンテナオーケストレーション設定
├── docs/                                                       # ドキュメント
├── scripts/                                                    # セットアップ・補助スクリプト
├── server/                                                     # 外部サービス用構成（docker 等）
└── README.md                                                   # 本ファイル
```

# 環境構築目次
<!-- Update Index `npx doctoc README.md` -->
<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [環境構築手順](#%E7%92%B0%E5%A2%83%E6%A7%8B%E7%AF%89%E6%89%8B%E9%A0%86)
  - [動作確認済み環境](#%E5%8B%95%E4%BD%9C%E7%A2%BA%E8%AA%8D%E6%B8%88%E3%81%BF%E7%92%B0%E5%A2%83)
  - [動作確認済みのOSSやミドルウェアのバージョンについて](#%E5%8B%95%E4%BD%9C%E7%A2%BA%E8%AA%8D%E6%B8%88%E3%81%BF%E3%81%AEoss%E3%82%84%E3%83%9F%E3%83%89%E3%83%AB%E3%82%A6%E3%82%A7%E3%82%A2%E3%81%AE%E3%83%90%E3%83%BC%E3%82%B8%E3%83%A7%E3%83%B3%E3%81%AB%E3%81%A4%E3%81%84%E3%81%A6)
  - [1. サービス起動](#1-%E3%82%B5%E3%83%BC%E3%83%93%E3%82%B9%E8%B5%B7%E5%8B%95)
    - [1-1. docker-compose を用いたサービスの起動](#1-1-docker-compose-%E3%82%92%E7%94%A8%E3%81%84%E3%81%9F%E3%82%B5%E3%83%BC%E3%83%93%E3%82%B9%E3%81%AE%E8%B5%B7%E5%8B%95)
      - [1-1-1. 必要な外部サービスの起動](#1-1-1-%E5%BF%85%E8%A6%81%E3%81%AA%E5%A4%96%E9%83%A8%E3%82%B5%E3%83%BC%E3%83%93%E3%82%B9%E3%81%AE%E8%B5%B7%E5%8B%95)
      - [1-1-2. OpenFGAマイグレーション](#1-1-2-openfga%E3%83%9E%E3%82%A4%E3%82%B0%E3%83%AC%E3%83%BC%E3%82%B7%E3%83%A7%E3%83%B3)
      - [1-1-3. データベースセットアップ](#1-1-3-%E3%83%87%E3%83%BC%E3%82%BF%E3%83%99%E3%83%BC%E3%82%B9%E3%82%BB%E3%83%83%E3%83%88%E3%82%A2%E3%83%83%E3%83%97)
      - [1-1-4. ユーザ認証システムの起動](#1-1-3-%E3%83%A6%E3%83%BC%E3%82%B6%E8%AA%8D%E8%A8%BC%E3%82%B7%E3%82%B9%E3%83%86%E3%83%A0%E3%81%AE%E8%B5%B7%E5%8B%95)
    - [1-2. Dockerfile を用いたサービスの起動](#1-2-dockerfile-%E3%82%92%E7%94%A8%E3%81%84%E3%81%9F%E3%82%B5%E3%83%BC%E3%83%93%E3%82%B9%E3%81%AE%E8%B5%B7%E5%8B%95)
      - [前提](#%E5%89%8D%E6%8F%90)
      - [1-2-1. 必要な外部サービスの起動](#1-2-1-%E5%BF%85%E8%A6%81%E3%81%AA%E5%A4%96%E9%83%A8%E3%82%B5%E3%83%BC%E3%83%93%E3%82%B9%E3%81%AE%E8%B5%B7%E5%8B%95)
        - [Keycloak](#keycloak)
        - [OpenFGA](#openfga)
      - [1-2-2. OpenFGAマイグレーション](#1-2-2-openfga%E3%83%9E%E3%82%A4%E3%82%B0%E3%83%AC%E3%83%BC%E3%82%B7%E3%83%A7%E3%83%B3)
      - [1-2-3. データベースセットアップ](#1-2-3-%E3%83%87%E3%83%BC%E3%82%BF%E3%83%99%E3%83%BC%E3%82%B9%E3%82%BB%E3%83%83%E3%83%88%E3%82%A2%E3%83%83%E3%83%97)
      - [1-2-4. ユーザ認証システムの起動](#1-2-4-%E3%83%A6%E3%83%BC%E3%82%B6%E8%AA%8D%E8%A8%BC%E3%82%B7%E3%82%B9%E3%83%86%E3%83%A0%E3%81%AE%E8%B5%B7%E5%8B%95)
  - [2. 実行環境構築](#2-%E5%AE%9F%E8%A1%8C%E7%92%B0%E5%A2%83%E6%A7%8B%E7%AF%89)
    - [2-1 参考実装](#2-1-%E5%8F%82%E8%80%83%E5%AE%9F%E8%A3%85)
    - [2-2. 環境構築手順](#2-2-%E7%92%B0%E5%A2%83%E6%A7%8B%E7%AF%89%E6%89%8B%E9%A0%86)
      - [前提条件](#%E5%89%8D%E6%8F%90%E6%9D%A1%E4%BB%B6)
      - [2-2-1. 環境構築ファイル準備](#2-2-1-%E7%92%B0%E5%A2%83%E6%A7%8B%E7%AF%89%E3%83%95%E3%82%A1%E3%82%A4%E3%83%AB%E6%BA%96%E5%82%99)
      - [2-2-2. セットアップスクリプトの実行](#2-2-2-%E3%82%BB%E3%83%83%E3%83%88%E3%82%A2%E3%83%83%E3%83%97%E3%82%B9%E3%82%AF%E3%83%AA%E3%83%97%E3%83%88%E3%81%AE%E5%AE%9F%E8%A1%8C)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# 環境構築手順

## 動作確認済み環境

| Name | Version | Notes |
| --- | --- | --- |
| JDK | JDK-21.x | ユーザ認証システムアプリのビルド |
| Maven | Apache Maven 3.9.8 | ユーザ認証システムアプリのビルド |
| Docker Engine / Docker Desktop | - | ユーザ認証システムアプリおよび各サービスの実行 |

## 動作確認済みのOSSやミドルウェアのバージョンについて

現在アプリケーションで利用しているOSSやミドルウェアのバージョンについては[こちら](./docs/architecture/architecture.md)

## 1. サービス起動

以下のいずれかの手順を実施し、必要な外部サービスおよびユーザ認証システムを起動してください。

- [1-1. docker-compose を用いたサービスの起動](#1-1-docker-composeを用いたサービスの起動)
- [1-2. dockerfile を用いたサービスの起動](#1-2-dockerfile-を用いたサービスの起動)

### 1-1. docker-compose を用いたサービスの起動

#### 1-1-1. 必要な外部サービスの起動

以下のコマンドを実行し、外部サービスを起動してください。

- PostgreSQL : データベースサービス
- Keycloak : 認証サービス
- OpenFGA : 認可サービス

```shell
# ディレクトリ移動（リポジトリのルートディレクトリから）
cd ./server

# docker network作成：初回のみ実行
docker network create shared-network-ods

# Docker Compose
docker compose -p ods-l3-auth up --build -d
```

#### 1-1-2. OpenFGAマイグレーション

以下のコマンドを実行し、OpenFGAのマイグレーションを実行してください。

```shell
# OpenFGA マイグレーション
docker compose -p ods-l3-auth run \
--rm openfga_dev migrate \
--datastore-engine postgres \
--datastore-uri 'postgres://openfga:password@postgres_openfga_dev:5432/openfga?sslmode=disable'
```

実行後、以下のメッセージが表示されていれば実行は完了です。

```
migration done
```

#### 1-1-3. データベースセットアップ

以下のコマンドを実行し、データベースセットアップを行ってください。

- データベース作成
- DBユーザ作成
- スキーマ作成
- テーブル作成

```shell
# ディレクトリ移動（リポジトリのルートディレクトリから）
cd ./scripts/sql

# Dockerコンテナ名を取得
container_name=$(docker ps --format '{{.Names}}' | grep -E "postgres_dev" | head -n1)

# スクリプトファイルをコンテナにコピー
docker cp 1_create_app_user.sql $container_name:/tmp/1_create_app_user.sql
docker cp 2_create_app_db.sql $container_name:/tmp/2_create_app_db.sql
docker cp 3_setup_app_db.sql $container_name:/tmp/3_setup_app_db.sql

# スクリプト実行
# DBユーザ作成
docker exec -it $container_name bash -c "PGPASSWORD=password psql -U keycloak -d keycloak -f /tmp/1_create_app_user.sql -v user_password=password"
# DB作成
docker exec -it $container_name bash -c "PGPASSWORD=password psql -U keycloak -d keycloak -f /tmp/2_create_app_db.sql"
# スキーマ/テーブル作成
docker exec -it $container_name bash -c "PGPASSWORD=password psql -U app_ods -d db_ods -f /tmp/3_setup_app_db.sql"
```

#### 1-1-3. ユーザ認証システムの起動

ユーザ認証システムのビルド・起動を行ってください。

```shell
# ディレクトリ移動（リポジトリのルートディレクトリから）
cd ./open-data-spaces-l3-authenticator-backend

# 実行ファイルをビルドする
mvn clean install -DskipTests

# Docker compose
docker compose -p ods-l3-app -f docker/docker-compose-local.yaml up --build -d
```

### 1-2. Dockerfile を用いたサービスの起動

#### 前提

データベースサービスを起動するためのDockerfileはリポジトリに含まれておりません。  
予めPostgresSQLサービスを用意し、起動・接続できる状態にしてください。

#### 1-2-1. 必要な外部サービスの起動

以下Dockerfile、環境変数を参考に、`docker build` `docker push` を実行後、外部サービスを起動してください。

- Keycloak : 認証サービス
- OpenFGA : 認可サービス

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

#### 1-2-2. OpenFGAマイグレーション

以下のコマンドを実行し、OpenFGAのマイグレーションを実行してください。

```shell
# OpenFGA マイグレーション
/openfga migrate
```

examples:
以下は、docker環境のOpenFGAにマイグレーションを行う場合

```shell
# Dockerコンテナ名を設定
container_name="<dockerコンテナ名>"

# OpenFGA マイグレーション
docker exec -it $container_name /openfga migrate
```

実行後、以下のメッセージが表示されていれば実行は完了です。

```
migration done
```

#### 1-2-3. データベースセットアップ

以下のスクリプトを実行し、データベースセットアップを行ってください。  

- データベース作成
- DBユーザ作成
- スキーマ作成
- テーブル作成

```shell
# 接続先データベース設定
DB_HOST=http://postgres.example.com
DB_PORT=5432
DB_NAME=example_db
DB_USER=example_user
DB_PASSWORD=example_password
# 作成するユーザのパスワード設定（記号を含む場合はシングルコーテーションで囲む）
USER_PASSWORKD='password'

# ディレクトリ移動（リポジトリのルートディレクトリから）
cd ./scripts/sql

# スクリプト実行（DBユーザ作成/DBユーザ作成済みの場合は実行不要）
PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f 1_create_app_user.sql -v user_password=$USER_PASSWORD

# スクリプト実行（DB作成/DB作成済みの場合は実行不要）
PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f 2_create_app_db.sql

# スクリプト実行（スキーマ/テーブル作成）
PGPASSWORD=$USER_PASSWORD psql -h $DB_HOST -p $DB_PORT -U app_ods -d db_ods -f 3_setup_app_db.sql
```

#### 1-2-4. ユーザ認証システムの起動

以下コマンドを実行し、ユーザ認証システムのビルドを行ってください。

```shell
# ディレクトリ移動（リポジトリのルートディレクトリから）
cd ./open-data-spaces-l3-authenticator-backend

# 実行ファイルをビルドする
mvn clean install -DskipTests
```

以下のDockerfile、環境変数を参考に、ユーザ認証システムを起動してください。

```
./docker/Dockerfile.example
```

[ユーザ認証システム configuration](./docs/configuration/environment-variables.md)

## 2. 実行環境構築

### 2-1 参考実装

サービス起動・ビルドが環境すると、下記の業務フローのチュートリアルを実行できます。  

[参考実装チュートリアル](./docs/tutorials/tutorials.md)  

参考実装チュートリアルは [1-1 docker-composeを用いた起動](#1-1-docker-compose-を用いたサービスの起動) を前提としています。  

### 2-2. 環境構築手順

#### 前提条件

- [1 サービス起動](#1-サービス起動)にて、以下の外部サービスの起動が完了していること
  - PostgreSQL : データベースサービス
  - Keycloak : 認証サービス
  - OpenFGA : 認可サービス
- bash/curl/psql コマンドを実行できる環境があること
  - curlコマンドを実行して、Keycloak/OpenFGAへ接続できること
  - psqlコマンドを実行して、PostgreSQLへ接続できること

#### 2-2-1. 環境構築ファイル準備

Keycloak/OpenFGA/Postgresセットアップ用の環境ファイルを準備します。  
以下のファイルを参考に、構築する環境ファイルを作成します。  
example： `./scripts/env/setup.env.example`  

| Name | Notes |
| --- | --- |
| KEYCLOAK_BASE_URL | Keycloakの接続先BaseURL |
| KEYCLOAK_REALM | 作成するKeycloakのレルム名 |
| KEYCLOAK_MASTER_REALM | Keycloakのデフォルトレルム名（通常は`master`） |
| KEYCLOAK_CLIENT_ID | Keycloak Admin RestAPI 実行用アクセストークン発行クライアントID（通常は`admin-cli`） |
| KEYCLOAK_USERNAME | Keycloakの管理ユーザ ログインID |
| KEYCLOAK_PASSWORD | Keycloakの管理ユーザ パスワード |
| KEYCLOAK_CLIENT_ID_API_ADMIN | 作成するKeycloakのクライアントID  (API実行権限/運用事業者権限) |
| KEYCLOAK_CLAIM_OPEN_SYSTEM_ID_API_ADMIN | 作成するKeycloakのクライアントIDに設定するオープンシステムID |
| OPENFGA_BASE_URL | OpenFGAの接続先BaseURL |
| POSTGRES_HOST | Postgresの接続先ホスト名 |
| POSTGRES_PORT | Postgresの接続先ポート |
| POSTGRES_DB | Postgresの接続先データベース名 |
| POSTGRES_USER | Postgresの接続先ユーザ |
| POSTGRES_PASSWORD | Postgresの接続先パスワード |
| L3TBL_API_KEY | 作成するユーザ認証システムのAPI-Key |
| L3TBL_API_KEYS_ID | 作成するユーザ認証システムのAPI-Key-ID（通常は任意のUUID） |
| L3TBL_API_KEYS_NAME | 作成するユーザ認証システムのAPI-Key名称 |
| L3TBL_API_KEYS_USECASE | 作成するユーザ認証システムの事業者・属性管理用usecase名 |
| L3TBL_AUTHZ_STORES_ENVIRONMENT_NAME | 起動しているユーザ認証システムの環境名（環境変数 `ODS_APPLICATION_ENV_NAME` の値を指定） |
| L3TBL_CIDRS_CIDR | 接続を許可するIPアドレス（環境変数 `ODS_ENABLE_IP_RESTRICTION` が有効な場合） |

#### 2-2-2. セットアップスクリプトの実行

作成した .env ファイルを引数に指定して、セットアップスクリプトを実行します。  

```shell
# ディレクトリ移動（リポジトリのルートディレクトリから）
cd ./scripts

# スクリプトファイルを実行
bash ./setup.sh <作成した.envファイルパス>
```

セットアップが完了すると、以下メッセージが表示されます。  

```
Setup completed successfully!
```

`Summary of created resources and configuration:` 以降に出力される項目は以下の通りです。  

| Name | Note |
| --- | --- |
| Keycloak realm | `KEYCLOAK_REALM` で指定したKeycloakのレルム |
| API Authorization client ID in Keycloak | `KEYCLOAK_CLIENT_ID_API_ADMIN` で指定したクライアントID |
| API Authorization client secret in Keycloak | 作成されたKeycloakのクライアントIDのクライアントシークレット |
| API Authorization store ID in OpenFGA | 作成されたOpenFGAのストアID [API実行権限管理ストア](./docs/openfga/api-authz-model.md) |
| Realm-store binding store ID in OpenFGA | 作成されたOpenFGAのストアID [レルム・ストア紐づけ管理ストア](./docs/openfga/realm-store-binding-model.md) |
| Operator-plant authorization store ID in OpenFGA | 作成されたOpenFGAのストアID [事業者・属性(事業所)管理ストア](./docs/openfga/operator-plant-model.md) |
| System API key ID in PostgreSQL | `L3TBL_API_KEY` で指定したAPI-Key |

# ライセンス
- 本リポジトリはMITライセンスで提供されています。
- ソースコードおよび関連ドキュメントの著作権は株式会社NTTデータグループ、株式会社NTTデータに帰属します。

# 免責事項
- 本リポジトリの内容は予告なく変更・削除する可能性があります。
- 本リポジトリの利用により生じた損失及び損害等について、いかなる責任も負わないものとします。