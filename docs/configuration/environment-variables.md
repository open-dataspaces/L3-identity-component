# ユーザ認証システム 環境変数一覧
## 外部サービス接続設定
### データベース接続設定
環境変数名 | デフォルト値 | 説明　　　　　　　　　　　　　　　　　　　　
-- | -- | -------------------------------------------
SPRING_DATASOURCE_DRIVER_CLASS_NAME | org.postgresql.Driver | JDBCドライバクラス名
SPRING_DATASOURCE_URL | jdbc:postgresql://localhost:5432/db_ods | DB接続URL
SPRING_DATASOURCE_USERNAME | app_ods | DBユーザー名
SPRING_DATASOURCE_PASSWORD | *** | DBパスワード
ODS_DATABASE_SSL | false | データベース接続時のssl有効/無効フラグ(ssl=true)
ODS_DATABASE_SSL_MODE | disable | データベース接続時のsslmode有効/無効フラグ(sslmode=require / disable / verify-full)
ODS_DATABASE_SSL_ROOT_CERT | | データベースSSL接続時のサーバ証明書パス(/secrets/server-ca/server-ca.pem)
ODS_DATABASE_SSL_CERT |  | データベースSSL接続時のクライアント証明書パス(/secrets/client-cert/client-cert.pem)
ODS_DATABASE_SSL_KEY |  | データベースSSL接続時のクライアントキーパス(/secrets/client-key/client-key.pem)

### Keycloak接続設定
環境変数名 | デフォルト値 | 説明　　　　　　　　　　　　　　　　　　　　
-- | -- | --
KEYCLOAK_REALM | master | KeycloakのRealm名
KEYCLOAK_API_ENDPOINT | http://localhost:8081 | Keycloak APIエンドポイント
KEYCLOAK_CREDENTIALS_ADMIN_REALM | master | 管理者Realm名
KEYCLOAK_CREDENTIALS_ADMIN_USERNAME | admin | 管理者ユーザー名
KEYCLOAK_CREDENTIALS_ADMIN_PASSWORD | *** | 管理者パスワード
KEYCLOAK_CREDENTIALS_ADMIN_CLIENT_ID | admin-cli | 管理者クライアントID
KEYCLOAK_AUTHORIZATION_URL | http://localhost:8081 | 認可コードフローエンドポイントURL
KEYCLOAK_AUTHORIZATION_RESPONSE_TYPE | code | 認可コードフローレスポンスタイプ
KEYCLOAK_AUTHORIZATION_SCOPE | openid | 認可コードフロースコープ
KEYCLOAK_AUTHORIZATION_CODE_CHALLENGE_METHOD | S256 | 認可コードフローPKCEコードチャレンジ方式

### OpenFGA接続設定
環境変数名 | デフォルト値 | 説明　　　　　　　　　　　　　　　　　　　　
-- | -- | --
OPENFGA_API_ENDPOINT | http://localhost:8080 | OpenFGA接続先BaseURL
OPENFGA_PRESHARED_KEY | preshared | OpenFGA接続時のAutorization Preshared Key

## アプリ動作設定
### アプリ動作環境設定
環境変数名 | デフォルト値 | 説明　　　　　　　　　　　　　　　　　　　　
-- | -- | --
ODS_APPLICATION_ENV_NAME | local | アプリ実行環境名
ODS_IS_ENABLE_IP_RESTRICTION | true | IP制限の有効/無効フラグ(true/false)
ODS_DEFAULT_EFFECTIVE_END_DATE | 9999-12-31 | データ有効終了日のデフォルト値
ODS_ENABLE_UC_AUTHORIZATION | true | ユースケース事業者管理機能の有効/無効フラグ(true/false)
ODS_ENABLE_OPERATOR_PLANT_AUTHORIZATION | true | 共通事業者管理機能の有効/無効フラグ(true/false)

### ログ設定
環境変数名 | デフォルト値 | 説明　　　　　　　　　　　　　　　　　　　　
-- | -- | --
LOGGING_FILE_NAME | logs/user_authentication.log | ログファイルパス
LOGGING_LEVEL_COM_ODS | INFO | アプリケーションログレベル
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_WEB | info | Spring Webログレベル
LOGGING_LEVEL_ORG_POSTGRESQL | INFO | PostgreSQL JDBCログレベル
SPRING_JPA_SHOW_SQL | false | SQLログ出力の有効化(true/false)
SPRING_MVC_LOG_REQUEST_DETAILS | false | 詳細なリクエストログの有効化(true/false)
SPRING_OUTPUT_ANSI_ENABLED | always | ANSIカラー付きログ出力

### API-JWT検証設定
環境変数名 | デフォルト値 | 説明　　　　　　　　　　　　　　　　　　　　
-- | -- | --
ODS_JWT_JWKS_CACHE_DURATION_SECOND | 3600 | KeycloakのJWKS証明書キャッシュ時間
ODS_JWT_JWKS_CACHE_SIZE | 10 | KeycloakのJWKS証明書キャッシュ数
ODS_JWT_CLAIM_EXPECTED_AUDIENCE |  | アクセストークン検証時に検証するクレーム `AUD` 文字列
ODS_JWT_CLAIM_EXPECTED_TYPE | Bearer | アクセストークン検証時に検証するクレーム `TYP` 文字列

### CORS設定
環境変数名 | デフォルト値 | 説明　　　　　　　　　　　　　　　　　　　　
-- | -- | --
ODS_CORS_PROPERTIES_ENABLED | false | CORS設定をプロパティで制御(true/false)
ODS_CORS_PATH_PATTERN | /** | CORS対象パスパターン
ODS_CORS_ALLOWED_ORIGINS | http://localhost:3000,https://example.com | 許可オリジン
ODS_CORS_ALLOWED_METHODS | GET,POST,PUT,OPTIONS | 許可HTTPメソッド
ODS_CORS_ALLOWED_HEADERS | Content-Type,Authorization,API-Key,User-Agent,Accept,Accept-Language,X-TrackingID | 許可ヘッダー
ODS_CORS_ALLOW_CREDENTIALS | true | 認証情報付きリクエストの許可(true/false)
ODS_CORS_MAX_AGE | 5 | プリフライトリクエストのキャッシュ秒数

### Actuator CORS設定
環境変数名 | デフォルト値 | 説明　　　　　　　　　　　　　　　　　　　　
-- | -- | --
MANAGEMENT_ENDPOINTS_WEB_CORS_ALLOWED_ORIGINS | http://localhost:3000 | Actuatorエンドポイント許可オリジン
MANAGEMENT_ENDPOINTS_WEB_CORS_ALLOW_CREDENTIALS | true | Actuatorエンドポイント認証情報許可(true/false)
MANAGEMENT_ENDPOINTS_WEB_CORS_ALLOWED_ORIGIN_PATTERNS | /** | Actuatorエンドポイント許可オリジンパターン
