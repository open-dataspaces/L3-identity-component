# レルム・ストア紐づけ管理ストア

## モデル定義
```
model
  schema 1.1

type realm

type store
  relations
    define bound_to: [realm]
```

## タプル定義
| user(realm) | relation | object(store) | 説明 |
| --- | --- | --- | --- |
| realm | bound_to | store_id | レルムとストアの紐づけ |

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
