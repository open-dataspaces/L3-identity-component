# !/bin/bash
read -r -p "OpenFGA BaseURLを設定する(https://example.com):" API_ENDPOINT
export API_ENDPOINT
read -r -p "作成するユーザ用ストアID名を設定する:" AUTHZ_USER_STORE_NAME
export AUTHZ_USER_STORE_NAME

# Create User Store
echo "Creating User Store..."
envsubst < json/41-create-user-store.json | curl -i -X POST \
  $API_ENDPOINT/stores \
  -H "Content-Type: application/json" \
  -d @-

echo ""
echo "Create User Store completed."
