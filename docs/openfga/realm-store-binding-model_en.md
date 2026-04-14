
# Realm and Store Binding Management Store
## Model Definition
```
model
  schema 1.1

type realm

type store
  relations
    define bound_to: [realm]
```
## Tuple Definition
| user(realm) | relation | object(store) | Description |
| --- | --- | --- | --- |
| realm | bound_to | store_id | Binding between a realm and a store  |
```
{
  "writes": {
    "tuple_keys": [
      {
        "user": "realm:${REALM}",
        "relation": "bound_to",
        "object": "store:${STORE_ID}"
      }
    ],
    "on_duplicate": "ignore"
  }
}
```
