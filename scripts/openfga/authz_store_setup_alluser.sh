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
  -d @json/31-create-model-alluser-authz.json

echo ""
echo "Create Model completed."

# Create role tuples(role-api)
echo "Creating role tuples..."
curl -i -X POST \
  $API_ENDPOINT/stores/$AUTHZ_STORE_ID/write \
  -H "Content-Type: application/json" \
  -d @json/03-create-tuples-api-authz-role.json

echo ""
echo "Create role tuples completed."

# Create role tuples(role-alluser-authz)
echo "Creating role tuples..."
curl -i -X POST \
  $API_ENDPOINT/stores/$AUTHZ_STORE_ID/write \
  -H "Content-Type: application/json" \
  -d @json/32-create-tuples-alluser-authz.json

echo ""
echo "Create role tuples completed."
