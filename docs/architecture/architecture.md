# 開発者向けドキュメント

## 現在アプリケーションで利用しているOSS

|Name                                        |Version |Notes|
|:-------------------------------------------|:-------|:----|
|java                                        |21||
|Spring Boot                                 |3.5.11||
|maven                                       |-||
|mockito                                     |5.3.1||
|docker                                      |28.1.1||
|docker-compose                              |2.35.1-desktop.1||

※導入されているOSS一覧は`pom.xml`内に記述

## 現在アプリケーションで利用しているMW
|Name                                        |Version |Notes|
|:-------------------------------------------|:-------|:----|
|PostgreSQL                                  |17.x|GCPが提供しているCloudSQLで2033/2/1までサポートしているバージョン|
|Keycloak                                    |26.2.5||
|OpenFGA                                     |v1.9.2-authzen|v1.9.2-authzenはv1.8.1とv1.8.2のリリースの途中から分岐したauthzen対応版にv1.9.2が反映されたものが ghcr.io/8hz/openfga:v1.9.2-authzenに格納されている|