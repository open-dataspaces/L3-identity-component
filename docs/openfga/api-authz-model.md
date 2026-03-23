# API実行権限管理ストア

## モデル定義
```
model
  schema 1.1

type user

type role
  relations
    define member: [user]

type api
  relations
    define allowed_get_roles: [role]
    define allowed_post_roles: [role]
    define allowed_put_roles: [role]
    define allowed_delete_roles: [role]
    define GET: allowed_get_roles or member from allowed_get_roles
    define POST: allowed_post_roles or member from allowed_post_roles
    define PUT: allowed_put_roles or member from allowed_put_roles
    define DELETE: allowed_delete_roles or member from allowed_delete_roles
```

### すべての事業者・個人ユーザに権限を付与するモデル

```
model
  schema 1.1

type user

type role
  relations
    define member: [user, user:*]

type api
  relations
    define allowed_get_roles: [role]
    define allowed_post_roles: [role]
    define allowed_put_roles: [role]
    define allowed_delete_roles: [role]
    define GET: allowed_get_roles or member from allowed_get_roles
    define POST: allowed_post_roles or member from allowed_post_roles
    define PUT: allowed_put_roles or member from allowed_put_roles
    define DELETE: allowed_delete_roles or member from allowed_delete_roles
```


## タプル定義(role-api)
| user(role) | relation | object(api) | 実行権限管理対象API |
| --- | --- | --- | --- |
| authz-stores-admin | allowed_post_roles | /authz/stores | 認可ストア登録API |
| authz-stores-admin | allowed_get_roles | /authz/stores/{store_id} | 認可ストア取得API |
| authz-stores-admin | allowed_delete_roles | /authz/stores/{store_id} | 認可ストア削除API |
| authz-models-admin | allowed_post_roles | /authz/stores/{store_id}/authorization-models | 認可モデル登録API |
| authz-models-admin | allowed_get_roles | /authz/stores/{store_id}/authorization-models | 認可モデル取得API |
| authz-tuples-admin | allowed_post_roles | /authz/stores/{store_id}/write | 認可タプル登録API |
| authz-tuples-admin | allowed_post_roles | /authz/stores/{store_id}/read | 認可タプル取得API |
| authz-user-admin | allowed_post_roles | /account/user | 個人ユーザ登録API |
| authz-client-admin | allowed_post_roles | /auth/clients | クライアントID発行API |
| authz-client-admin | allowed_post_roles | /auth/clients/{client_uuid} | クライアントシークレット取得API |
| authz-client-admin | allowed_put_roles | /auth/clients/{client_id} | クライアント情報更新API |
| authz-client-admin | allowed_delete_roles | /auth/clients/{client_id} | クライアントID削除API |
```
{
  "writes": {
    "tuple_keys": [
      {
        "user": "role:authz-stores-admin",
        "relation": "allowed_post_roles",
        "object": "api:/authz/stores"
      }
      ,{
        "user": "role:authz-stores-admin",
        "relation": "allowed_get_roles",
        "object": "api:/authz/stores/{store_id}"
      }
      ,{
        "user": "role:authz-stores-admin",
        "relation": "allowed_delete_roles",
        "object": "api:/authz/stores/{store_id}"
      }
      {
        "user": "role:authz-models-admin",
        "relation": "allowed_get_roles",
        "object": "api:/authz/stores/{store_id}/authorization-models"
      }
      ,{
        "user": "role:authz-models-admin",
        "relation": "allowed_post_roles",
        "object": "api:/authz/stores/{store_id}/authorization-models"
      }
      ,{
        "user": "role:authz-tuples-admin",
        "relation": "allowed_post_roles",
        "object": "api:/authz/stores/{store_id}/write"
      }
      ,{
        "user": "role:authz-tuples-admin",
        "relation": "allowed_post_roles",
        "object": "api:/authz/stores/{store_id}/read"
      }
      ,{
        "user": "role:authz-user-admin",
        "relation": "allowed_post_roles",
        "object": "api:/account/user"
      }
      ,{
        "user": "role:authz-client-admin",
        "relation": "allowed_post_roles",
        "object": "api:/auth/clients"
      }
      ,{
        "user": "role:authz-client-admin",
        "relation": "allowed_post_roles",
        "object": "api:/auth/clients/secret/{client_uuid}"
      }
      ,{
        "user": "role:authz-client-admin",
        "relation": "allowed_put_roles",
        "object": "api:/auth/clients/{client_id}"
      }
      ,{
        "user": "role:authz-client-admin",
        "relation": "allowed_delete_roles",
        "object": "api:/auth/clients/{client_id}"
      }
    ],
    "on_duplicate": "ignore"
  }
}
```

## タプル定義(user-role)
| user(user) | relation | object(role) | 実行権限管理対象API |
| --- | --- | --- | --- |
| 事業者識別子(内部) または オープンシステムID | member | authz-stores-admin | 認可ストア登録API、認可ストア取得API、認可ストア削除API |
| 事業者識別子(内部) または オープンシステムID | member | authz-models-admin | 認可モデル登録API、認可モデル取得API |
| 事業者識別子(内部) または オープンシステムID | member | authz-tuples-admin | 認可タプル登録API、認可タプル取得API |
| 事業者識別子(内部) または オープンシステムID | member | authz-user-admin | 個人ユーザ登録API |
| 事業者識別子(内部) または オープンシステムID | member | authz-client-admin | クライアントID発行API、クライアントシークレット取得API、クライアント情報更新API、クライアントID削除API |

```
{
  "writes": {
    "tuple_keys": [
      {
        "user": "user:${AUTHZ_USER_ID}",
        "relation": "member",
        "object": "role:authz-stores-admin"
      }
      ,{
        "user": "user:${AUTHZ_USER_ID}",
        "relation": "member",
        "object": "role:authz-models-admin"
      }
      ,{
        "user": "user:${AUTHZ_USER_ID}",
        "relation": "member",
        "object": "role:authz-tuples-admin"
      }
      ,{
        "user": "user:${AUTHZ_USER_ID}",
        "relation": "member",
        "object": "role:authz-user-admin"
      }
      ,{
        "user": "user:${AUTHZ_USER_ID}",
        "relation": "member",
        "object": "role:authz-client-admin"
      }
    ],
    "on_duplicate": "ignore"
  }
}

```

