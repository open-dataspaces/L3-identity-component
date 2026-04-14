
# API Execution Authorization Management Store
## Model Definition
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

### Model that Grants Permissions to All Operators and Individual Users
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
## Tuple Definition (role-api)
 | user (role) | relation | object (api) | Target API for Execution Authorization Management | 
 | --- | --- | --- | --- | 
 | authz-stores-admin | allowed_post_roles | /authz/stores | Authorization Store Registration API | 
 | authz-stores-admin | allowed_get_roles | /authz/stores/{store_id} | Authorization Store Retrieval API | 
 | authz-stores-admin | allowed_delete_roles | /authz/stores/{store_id} | Authorization Store Deletion API | 
 | authz-models-admin | allowed_post_roles | /authz/stores/{store_id}/authorization-models | Authorization Model Registration API | 
 | authz-models-admin | allowed_get_roles | /authz/stores/{store_id}/authorization-models | Authorization Model Retrieval API | 
 | authz-tuples-admin | allowed_post_roles | /authz/stores/{store_id}/write | Authorization Tuple Registration API | 
 | authz-tuples-admin | allowed_post_roles | /authz/stores/{store_id}/read | Authorization Tuple Retrieval API | 
 | authz-user-admin | allowed_post_roles | /account/user | Individual User Registration API   | 
 | authz-client-admin | allowed_post_roles | /auth/clients | Client ID Issuance API   | 
 | authz-client-admin | allowed_post_roles | /auth/clients/{client_uuid} | Client Secret Retrieval API | 
 | authz-client-admin | allowed_put_roles | /auth/clients/{client_id} | Client Information Update API | 
 | authz-client-admin | allowed_delete_roles | /auth/clients/{client_id} | Client ID Deletion API | 
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
## Tuple Definition (user-role)
| user (user) | relation | object (role) | Target API for Execution Authorization Management | 
| --- | --- | --- | --- |
| Operator Identifier (internal) or Open System ID | member | authz-stores-admin | Authorization Store Registration API, Authorization Store Retrieval API, Authorization Store Deletion API  |
| Operator Identifier (internal) or Open System ID | member | authz-models-admin | Authorization Model Registration API, Authorization Model Retrieval API  |
| Operator Identifier (internal) or Open System ID | member | authz-tuples-admin | Authorization Tuple Registration API, Authorization Tuple Retrieval API  |
| Operator Identifier (internal) or Open System ID | member | authz-user-admin | Individual User Registration API  |
| Operator Identifier (internal) or Open System ID | member | authz-client-admin | Client ID Issuance API, Client Secret Retrieval API, Client Information Update API, Client ID Deletion API  |
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
