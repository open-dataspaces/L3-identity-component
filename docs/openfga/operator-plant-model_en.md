
# Operator / Attribute (Plant) Management Store
## Model Definition
```
model
  schema 1.1

type user

type admin
  relations
    define member: [user]

type operator
  relations
    define member: [user]
    define admin: [admin]

    define viewer: member or member from admin
    define updater: member or member from admin

type plant
  relations
    define parent_operator: [operator]

    define viewer: viewer from parent_operator

type group
  relations
    define admin: [admin]
    define member: [user]

    define editor: member from admin
    define viewer: member or editor

type api
  relations
    define parent: [api]

    define resource_group_get: [group]
    define resource_group_create: [group]
    define resource_group_statusup: [group]

    define resource_operator: [operator]
    define resource_plant: [plant]
    define resource_plant_operator: [operator]

    define can_get: viewer from resource_group_get or viewer from resource_plant or viewer from resource_operator or can_get from parent
    define can_create: editor from resource_group_create or updater from resource_operator or can_create from parent
    define can_update: updater from resource_operator or updater from resource_plant_operator or can_update from parent
    define can_statusup: editor from resource_group_statusup or updater from resource_plant_operator or can_statusup from parent
```

## Tuple Definition (admin/group-api)
```
{
  "writes": {
    "tuple_keys": [
        { "user": "admin:managers", "relation": "admin", "object": "group:managers" },
        { "user": "group:managers", "relation": "resource_group_create", "object": "api:operator" },
        { "user": "group:managers", "relation": "resource_group_get", "object": "api:root/operator" },
        { "user": "group:managers", "relation": "resource_group_get", "object": "api:root/plant" },
        { "user": "admin:superusers", "relation": "admin", "object": "group:superusers" },
        { "user": "group:superusers", "relation": "resource_group_create", "object": "api:operator" },
        { "user": "group:superusers", "relation": "resource_group_get", "object": "api:root/operator" },
        { "user": "group:superusers", "relation": "resource_group_statusup", "object": "api:root/operator" },
        { "user": "group:superusers", "relation": "resource_group_get", "object": "api:root/plant" },
        { "user": "group:superusers", "relation": "resource_group_statusup", "object": "api:root/plant" }
    ],
    "on_duplicate": "ignore"
  }
}
```

## Tuple Definition (user-admin/group)
```
{
  "writes": {
    "tuple_keys": [
        {"user": "user:${AUTHZ_USER_ID}", "relation": "member", "object": "operator:${AUTHZ_USER_ID}"},
        {"user": "user:${AUTHZ_USER_ID}", "relation": "member", "object": "admin:managers" },
        {"user": "operator:${AUTHZ_USER_ID}", "relation": "resource_operator", "object": "api:operator/${AUTHZ_USER_ID}"},
        {"user": "operator:${AUTHZ_USER_ID}", "relation": "resource_operator", "object": "api:${AUTHZ_USER_ID}/plant"},
        {"user": "api:root/operator", "relation": "parent", "object": "api:operator/${AUTHZ_USER_ID}"},
        {"user": "admin:superusers", "relation": "admin", "object": "operator:${AUTHZ_USER_ID}"}
    ],
    "on_duplicate": "ignore"
  }
}
```
