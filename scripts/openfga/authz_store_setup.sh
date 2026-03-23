# !/bin/bash
read -r -p "OpenFGA BaseURLを設定する(https://example.com):" API_ENDPOINT
export API_ENDPOINT
#export API_ENDPOINT=http://localhost:8080
#export API_ENDPOINT=https://dts-ods-openfga-backend-608541294538.asia-northeast1.run.app
#export API_TOKEN=

# Create a new store
echo "Creating Store..."
curl -i -X POST \
  $API_ENDPOINT/stores \
  -H "Content-Type: application/json" \
  -d @json/01-create-store-api-authz.json

echo ""
echo "Create Store completed."

read -r -p "作成されたストアIDを設定する:" AUTHZ_STORE_ID
export AUTHZ_STORE_ID

# Create Model
echo "Creating Model..."
curl -i -X POST \
  $API_ENDPOINT/stores/$AUTHZ_STORE_ID/authorization-models \
  -H "Content-Type: application/json" \
  -d @json/02-create-model-api-authz.json

echo ""
echo "Create Model completed."

# Create role tuples
echo "Creating role tuples..."
curl -i -X POST \
  $API_ENDPOINT/stores/$AUTHZ_STORE_ID/write \
  -H "Content-Type: application/json" \
  -d @json/03-create-tuples-api-authz-role.json

echo ""
echo "Create role tuples completed."

read -r -p "API実行権限を付与するユーザID(OperatorId)を設定する:" AUTHZ_USER_ID
export AUTHZ_USER_ID

# Create user tuples
echo "Creating user tuples..."
envsubst < json/04-create-tuples-api-authz-userid.json | curl -i -X POST \
  $API_ENDPOINT/stores/$AUTHZ_STORE_ID/write \
  -H "Content-Type: application/json" \
  -d @-

echo ""
echo "Create user tuples completed."
