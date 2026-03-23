#!/bin/bash

# -------------------------------------------------------------------------
# Setup script for OPEN-DATASPACES-L3 APP
# This script sets up the necessary Keycloak realm, client, and protocol mapper for API authorization,
#  as well as the OpenFGA stores, authorization models, and tuples for API authorization, realm-store binding, and operator-plant authorization.
# It also sets up the API key for the System API in PostgreSQL.
#
# Usage:
# 1. Ensure that you have PostgreSQL client installed and running, and that you have access to the Keycloak and OpenFGA instances.
# 2. Ensure that you have the required environment variables set in a .env file.
# 3. Run this script and provide the path to the .env file when prompted:
#    bash ./setup.sh <path_to_env_file>
#
# It performs the following steps:
# 1. Checks that the required environment variables and files are present, and that the psql command is available.
# 2. Creates a realm in Keycloak.
# 3. Creates a client in Keycloak for the API authorization if it does not already exist, and adds a protocol mapper to include the "open_system_id" claim in the access token.
# 4. Creates a store in OpenFGA for API authorization, creates the authorization model for it, and creates role tuples in it.
# 5. Creates a store in OpenFGA for realm-store binding and creates the authorization model for it.
# 6. Creates a store in OpenFGA for operator-plant authorization, creates the authorization model for it, and creates role tuples in it.
# 7. Creates user tuples in the API authorization store and operator-plant authorization store, substituting the user ID for the API admin client.
# 8. Sets up the System API key and OpenFGA store IDs in PostgreSQL by executing the provided SQL script with substituted variables.
#
# error codes:
# 2: <path_to_env_file> not provided
# 3: <path_to_env_file> does not exist
# 4: Required environment variable not set
# 5: 'psql' command not found
# 6: Failed to obtain access token from Keycloak
# 7: Access token not found in Keycloak response
# 8: Failed to create realm in Keycloak
# 9: Failed to retrieve UUID of created realm from Keycloak
# 10: Failed to update realm configuration JSON
# 11: Failed to update realm configuration in Keycloak
# 12: Failed to create user profile configuration JSON for Keycloak
# 13: Failed to create user profile configuration component in Keycloak
# 14: Failed to create JSON for Keycloak password policy
# 15: Failed to update password policy in Keycloak
# 16: Failed to create API Authorization client in Keycloak
# 17: Failed to create protocol mapper in Keycloak
# 18: Failed to retrieve client secret for API Authorization client from Keycloak
# 19: Failed to create API authorization store in OpenFGA
# 20: Failed to create authorization model in API authorization store in OpenFGA
# 21: Failed to create role tuples in API authorization store in OpenFGA
# 22: Failed to create realm store binding in OpenFGA
# 23: Failed to create authorization model in realm store binding in OpenFGA
# 24: Failed to create operator-plant authorization store in OpenFGA
# 25: Failed to create authorization model in operator-plant authorization store in OpenFGA
# 26: Failed to create role tuples in operator-plant authorization store in OpenFGA
# 27: Failed to create user tuples in API authorization store in OpenFGA
# 28: Failed to create user tuples in operator-plant authorization store in OpenFGA
# 29: Failed to set up database in PostgreSQL
# -------------------------------------------------------------------------


# Logging setup
# -----------------------------------------------------------------
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)

# Create a timestamp for the log file
LOG_TS=$(date +"%Y%m%d-%H%M%S")
mkdir -p "$SCRIPT_DIR/logs"
LOG_FILE="$SCRIPT_DIR/logs/setup-$LOG_TS.log"
mkdir -p "$SCRIPT_DIR/tmp"

# Redirect stdout and stderr to the log file
exec > >(tee -a "$LOG_FILE") 2>&1

# Function
# -----------------------------------------------------------------
# Function to roll back by deleting the created OpenFGA stores
finish_error() {
  # Roll back by deleting the created OpenFGA stores
  rollback_store "$API_AUTHZ_STORE_ID"
  rollback_store "$REALM_AUTHZ_STORE_ID"
  rollback_store "$OPERATOR_PLANT_AUTHZ_STORE_ID"
  echo "-------------------------------------------------------------------------"
  echo "ErrorCode: $1, Message: $2"
  exit $1
}

# Function to check if a path is provided and exists
check_path() {
  local p="$1"
  local description="$2"
  echo "Checking $description at path: $p"
  if [ -z "$p" ]; then
    finish_error 2 "$description not provided."
  fi
  if [ ! -e "$p" ]; then
    finish_error 3 "$description '$p' does not exist."
  fi
}

# Function to delete an OpenFGA store by ID
rollback_store() {
  local store_id="$1"
  if [ -n "$store_id" ]; then
    echo "Deleting OpenFGA store with ID: $store_id"
    http_code=''
    http_code=$(curl -sS -o /dev/null -w "%{http_code}" -i -X DELETE \
      $OPENFGA_BASE_URL/stores/$store_id \
      -H "Content-Type: application/json")
    if [ "$http_code" -ne 204 ]; then
        echo "Failed to delete OpenFGA store with ID $store_id (HTTP $http_code)."
    else
        echo "OpenFGA store with ID $store_id deleted successfully."
    fi
  fi
}

# Function to create the user profile configuration component in Keycloak with the modified JSON
create_json_keycloak_userprofile() {
    # Remove any existing component payload file
    rm -f "$SCRIPT_DIR/tmp/component_payload.json"

    # Read the user profile configuration JSON file, remove newlines, and escape double quotes and backslashes for embedding in the JSON payload
    local upcfg_file="$SCRIPT_DIR/keycloak/json/user-profile-config.json"
    local up_one_line="$(tr -d '\r\n' < "$upcfg_file")"
    local up_escaped="$(printf '%s' "$up_one_line" | sed 's/\\/\\\\/g; s/"/\\"/g')"

    # Create the user profile configuration component in Keycloak with the modified JSON
    cat > "$SCRIPT_DIR/tmp/component_payload.json" <<JSON
{
    "name": "user-profile",
    "parentId": "${REALM_UUID}",
    "providerId": "declarative-user-profile",
    "providerType": "org.keycloak.userprofile.UserProfileProvider",
    "config": {
        "kc.user.profile.config": ["${UP_ESCAPED}"]
    }
}
JSON
    ls -l "$SCRIPT_DIR/tmp/component_payload.json"
    if [ $? -ne 0 ]; then
        return 1
    fi
    return 0
}

# Function to create the JSON file for setting the password policy in Keycloak
create_json_keycloak_password_policy() {
    rm -f "$SCRIPT_DIR/tmp/realm_password_policy.json"
    # Create the password policy string based on the defined constants

    local password_policy_min_len=8
    local password_policy_max_len=20
    local password_policy_regex='^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#\\$%\\^&\\*\\(\\)])[A-Za-z0-9!@#\\$%\\^&\\*\\(\\)]+$'
    local password_policy_regex_escaped=$(printf '%s' "$password_policy_regex" | sed 's/\\/\\\\/g; s/"/\\"/g')
    # Keycloak password policy string format reference: https://www.keycloak.org/docs/latest/server_admin/#_password_policies
    local policy_string="length(${password_policy_min_len}) and maxLength(${password_policy_max_len}) and regexPattern(${password_policy_regex_escaped})"

    cat > "$SCRIPT_DIR/tmp/realm_password_policy.json" <<JSON
{
    "passwordPolicy": "${policy_string}"
}
JSON
    ls -l "$SCRIPT_DIR/tmp/realm_password_policy.json"
    if [ $? -ne 0 ]; then
        return 1
    fi
    return 0
}

# Main script
# -----------------------------------------------------------------
echo "Starting setup script for OPEN-DATASPACES-L3 APP..."

# 1. Check required environment variables and files, and check if psql command is available
# ---------------------------------------------------------
echo "Start: 1-1. Checking required files..."

# Check if the path to the .env file is provided as an argument
ENV_PATH=$1
check_path "$ENV_PATH" "<path_to_env_file>"

# Check that the required files are present
check_path "$SCRIPT_DIR/keycloak/json/user-profile-config.json" "<path_to_keycloak_user_profile_config_json>"
check_path "$SCRIPT_DIR/openfga/json/01-create-store-api-authz.json" "<path_to_setup_openfga_json_files>"
check_path "$SCRIPT_DIR/openfga/json/02-create-model-api-authz.json" "<path_to_setup_openfga_json_files>"
check_path "$SCRIPT_DIR/openfga/json/03-create-tuples-api-authz-role.json" "<path_to_setup_openfga_json_files>"
check_path "$SCRIPT_DIR/openfga/json/04-create-tuples-api-authz-userid.json" "<path_to_setup_openfga_json_files>"
check_path "$SCRIPT_DIR/openfga/json/11-create-store-realm-store-binding.json" "<path_to_setup_openfga_json_files>"
check_path "$SCRIPT_DIR/openfga/json/12-create-model-realm-store-binding.json" "<path_to_setup_openfga_json_files>"
check_path "$SCRIPT_DIR/openfga/json/21-create-store-operator-plant.json" "<path_to_setup_openfga_json_files>"
check_path "$SCRIPT_DIR/openfga/json/22-create-model-operator-plant.json" "<path_to_setup_openfga_json_files>"
check_path "$SCRIPT_DIR/openfga/json/23-create-tuples-operator-plant.json" "<path_to_setup_openfga_json_files>"
check_path "$SCRIPT_DIR/openfga/json/24-create-tuples-operator-plant-admin.json" "<path_to_setup_openfga_json_files>"
check_path "$SCRIPT_DIR/sql/4_setup_app_table.sql" "<path_to_setup_database_sql>"
echo "Finish: 1-1. Required files are present."

# Load environment variables from the .env file
echo "Start: 1-2. Loading environment variables from .env file..."
set -o allexport
source "$ENV_PATH"
set +o allexport
echo "Finish: 1-2. Environment variables loaded from .env file."

# Check if required environment variables are set
echo "Start: 1-3. Checking required environment variables..."
REQUIRED_VARS=(
    KEYCLOAK_BASE_URL
    KEYCLOAK_REALM
    KEYCLOAK_MASTER_REALM
    KEYCLOAK_CLIENT_ID
    KEYCLOAK_USERNAME
    KEYCLOAK_PASSWORD
    KEYCLOAK_CLIENT_ID_API_ADMIN
    KEYCLOAK_CLAIM_OPEN_SYSTEM_ID_API_ADMIN
    OPENFGA_BASE_URL
    POSTGRES_HOST
    POSTGRES_PORT
    POSTGRES_DB
    POSTGRES_USER
    POSTGRES_PASSWORD
    L3TBL_API_KEY
    L3TBL_API_KEYS_ID
    L3TBL_API_KEYS_NAME
    L3TBL_API_KEYS_USECASE
    L3TBL_AUTHZ_STORES_ENVIRONMENT_NAME
    L3TBL_CIDRS_CIDR
)
for var in "${REQUIRED_VARS[@]}"; do
    echo "Checking environment variable: $var, value: ${!var}"
    if [ -z "${!var}" ]; then
        finish_error 4 "Environment variable '$var' is not set. Please check your .env file."
    fi
done
echo "Finish: 1-3. Required environment variables are set."

# Check if PostgreSQL client is available
echo "Start: 1-4. Checking if 'psql' command is available..."
IS_PSQL_AVAILABLE=$(command -v psql)
if [ -z "$IS_PSQL_AVAILABLE" ]; then
    finish_error 5 "'psql' command not found. Please ensure you have PostgreSQL client installed and 'psql' is in your PATH."
fi
echo "Finish: 1-4. 'psql' command is available."

# 2. Set up Realm
# ---------------------------------------------------------
# Obtain an access token from Keycloak using the Resource Owner Password Credentials Grant
echo "Start: 2-1. Obtaining access token from Keycloak..."
response_json=''
response_json=$(curl -sS -f --location -X POST "$KEYCLOAK_BASE_URL/realms/$KEYCLOAK_MASTER_REALM/protocol/openid-connect/token" \
    --header "Content-Type: application/x-www-form-urlencoded" \
    --data-urlencode "grant_type=password" \
    --data-urlencode "client_id=$KEYCLOAK_CLIENT_ID" \
    --data-urlencode "username=$KEYCLOAK_USERNAME" \
    --data-urlencode "password=$KEYCLOAK_PASSWORD")
if [ $? -ne 0 ]; then
    finish_error 6 "Failed to obtain access token from Keycloak."
fi
# Extract the access token from the response JSON
ACCESS_TOKEN=$(printf '%s' "$response_json" \
  | tr -d '\n' \
  | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p')
if [ -z "$ACCESS_TOKEN" ]; then
    finish_error 7 "Access token not found in Keycloak response."
fi
echo "Finish: 2-1. Access token obtained from Keycloak: $(printf '%.20s' "$ACCESS_TOKEN")..."

# Check if the realm already exists, and create it if it does not exist
echo "Start: 2-2. Checking if realm '$KEYCLOAK_REALM' exists in Keycloak..."
REALM_UUID=$(curl -sS -f -X GET "$KEYCLOAK_BASE_URL/admin/realms/$KEYCLOAK_REALM" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  | tr -d '\r' \
  | grep -Po '"id":"\K[^"]+' \
  | head -1
)
if [ -z "$REALM_UUID" ]; then
    # Realm does not exist, create it
    echo "Create: 2-2. Realm '$KEYCLOAK_REALM' does not exist in Keycloak. Creating realm..."
    http_code=''
    http_code=$(curl -sS -o /dev/null -w "%{http_code}" -X POST "$KEYCLOAK_BASE_URL/admin/realms" \
        -H "Authorization: Bearer $ACCESS_TOKEN" \
        -H "Content-Type: application/json" \
        -d '{"realm":"'"$KEYCLOAK_REALM"'","enabled":true}')
    if [ "$http_code" -ne 201 ]; then
        finish_error 8 "Failed to create realm '$KEYCLOAK_REALM' (HTTP $http_code)"
    fi

    # Retrieve the UUID of the created realm
    REALM_UUID=$(curl -sS -f -X GET "$KEYCLOAK_BASE_URL/admin/realms/$KEYCLOAK_REALM" \
    -H "Authorization: Bearer $ACCESS_TOKEN" \
    -H "Content-Type: application/json" \
    | tr -d '\r' \
    | grep -Po '"id":"\K[^"]+' \
    | head -1
    )
    if [ -z "$REALM_UUID" ]; then
        finish_error 9 "Failed to retrieve UUID of realm '$KEYCLOAK_REALM' from Keycloak after creation."
    fi
fi
echo "Finish: 2-2. Realm '$KEYCLOAK_REALM' already exists in Keycloak with UUID: $REALM_UUID"

# Retrieve the current realm configuration, modify it to set the user profile configuration, and update the realm configuration in Keycloak
echo "Start: 2-3. Checking if 'editUsernameAllowed' is set to true in realm configuration in Keycloak realm '$KEYCLOAK_REALM'..."
# Retrieve the current realm configuration from Keycloak and save it to a temporary file
curl -s -X GET "$KEYCLOAK_BASE_URL/admin/realms/$KEYCLOAK_REALM" \
     -H "Authorization: Bearer $ACCESS_TOKEN" > "$SCRIPT_DIR/tmp/realm.json"
if [ $? -ne 0 ]; then
    finish_error 10 "Failed to retrieve realm configuration from Keycloak."
fi
check_path "$SCRIPT_DIR/tmp/realm.json" "Temporary realm configuration file"

# Check if "editUsernameAllowed" is set to true in the realm configuration, and update it if it is not set to true
IS_EDIT_USERNAME_ALLOWED=$(sed -n 's/.*"editUsernameAllowed":\([^,}]*\).*/\1/p ' "$SCRIPT_DIR/tmp/realm.json")
if [ "$IS_EDIT_USERNAME_ALLOWED" != "true" ]; then
    echo "Update: 2-3. 'editUsernameAllowed' is not set to true in realm configuration. Updating realm configuration to set 'editUsernameAllowed' to true..."
    # Update the realm configuration to set "editUsernameAllowed" to true
    sed -E 's/("editUsernameAllowed":)([^,}]*)([},])/\1true\3/' "$SCRIPT_DIR/tmp/realm.json" > "$SCRIPT_DIR/tmp/realm_updated.json"

    # Update the realm configuration in Keycloak with the modified JSON
    http_code=''
    http_code=$(curl -sS -o /dev/null -w "%{http_code}" -X PUT "$KEYCLOAK_BASE_URL/admin/realms/$KEYCLOAK_REALM" \
        -H "Authorization: Bearer $ACCESS_TOKEN" \
        -H "Content-Type: application/json" \
        --data-binary @"$SCRIPT_DIR/tmp/realm_updated.json")
    if [ "$http_code" -ne 204 ]; then
        finish_error 11 "Failed to update realm configuration in Keycloak (HTTP $http_code)."
    fi
fi
echo "Finish: 2-3. Realm configuration in Keycloak realm '$KEYCLOAK_REALM' is updated to set 'editUsernameAllowed' to true if it was not already set."

# Set up user profile configuration in the realm by creating the user profile configuration component if it does not already exist, and configuring it with the provided JSON configuration
echo "Start: 2-4. Setting up user profile configuration in Keycloak realm '$KEYCLOAK_REALM'..."
# Check if the user profile configuration component already exists in the realm, and create it if it does not exist
UPCONF_UUID=$(curl -s -X GET "$KEYCLOAK_BASE_URL/admin/realms/$KEYCLOAK_REALM/components?type=org.keycloak.userprofile.UserProfileProvider" \
    -H "Authorization: Bearer $ACCESS_TOKEN" \
    | tr -d '\r' \
    | grep -Po '"id":"\K[^"]+' \
    | head -1
)
echo "Checked for user profile configuration component in Keycloak realm '$KEYCLOAK_REALM'. UUID: $UPCONF_UUID"

if [ -z "$UPCONF_UUID" ]; then
    echo "Put: 2-4. User profile configuration component does not exist in Keycloak realm '$KEYCLOAK_REALM'. Setting up user profile configuration..."

    # Create the user profile configuration component in Keycloak with the modified JSON
    create_json_keycloak_userprofile
    if [ $? -ne 0 ]; then
        finish_error 12 "Failed to create user profile configuration JSON for Keycloak."
    fi

    # Update the realm configuration in Keycloak with the user profile configuration component
    http_code=''
    http_code=$(curl -sS -o /dev/null -w "%{http_code}" -X POST "$KEYCLOAK_BASE_URL/admin/realms/$KEYCLOAK_REALM/components" \
        -H "Authorization: Bearer $ACCESS_TOKEN" \
        -H "Content-Type: application/json" \
        --data-binary @"$SCRIPT_DIR/tmp/component_payload.json")
    if [ "$http_code" -ne 201 ]; then
        finish_error 13 "Failed to create user profile configuration component in Keycloak (HTTP $http_code)."
    fi
fi

echo "Finish: 2-4. User profile configuration is set up in Keycloak realm '$KEYCLOAK_REALM' if it was not already set up."

echo "Start: 2-5. Setting up password policy in Keycloak realm '$KEYCLOAK_REALM'..."
# Create the JSON file for setting the password policy in Keycloak
create_json_keycloak_password_policy
if [ $? -ne 0 ]; then
    finish_error 14 "Failed to create JSON for Keycloak password policy."
fi
# Update the realm configuration in Keycloak with the password policy
http_code=''
http_code=$(curl -sS -o /dev/null -w "%{http_code}" -X PUT "$KEYCLOAK_BASE_URL/admin/realms/$KEYCLOAK_REALM" \
    -H "Authorization: Bearer $ACCESS_TOKEN" \
    -H "Content-Type: application/json" \
    --data-binary @"$SCRIPT_DIR/tmp/realm_password_policy.json")
if [ "$http_code" -ne 204 ]; then
    finish_error 15 "Failed to update password policy in Keycloak (HTTP $http_code)."
fi
echo "Finish: 2-5. Password policy is set up in Keycloak realm '$KEYCLOAK_REALM'."

# 3. Create the API Authorization client in Keycloak
# ---------------------------------------------------------
# Check if the client already exists, and create it if it does not exist
echo "Start: 3-1. Checking if API Authorization client '$KEYCLOAK_CLIENT_ID_API_ADMIN' exists in Keycloak realm '$KEYCLOAK_REALM'..."
API_ADMIN_CLIENT_UUID=$(curl -sS -i -X GET "$KEYCLOAK_BASE_URL/admin/realms/$KEYCLOAK_REALM/clients?clientId=$KEYCLOAK_CLIENT_ID_API_ADMIN" \
    -H "Authorization: Bearer $ACCESS_TOKEN" \
    -H "Content-Type: application/json" \
    | tr -d '\r' \
    | sed -n 's/.*"id":"\([^"]*\)".*/\1/p')
echo "Checked for API Authorization client '$KEYCLOAK_CLIENT_ID_API_ADMIN' in Keycloak realm '$KEYCLOAK_REALM'. UUID: $API_ADMIN_CLIENT_UUID"

if [ -z "$API_ADMIN_CLIENT_UUID" ]; then
    # Client does not exist, create it
    echo "Create: 3-1. API Authorization client '$KEYCLOAK_CLIENT_ID_API_ADMIN' does not exist in Keycloak realm '$KEYCLOAK_REALM'. Creating client..."
    API_ADMIN_CLIENT_UUID=$(curl -sS -f -i -X POST "$KEYCLOAK_BASE_URL/admin/realms/$KEYCLOAK_REALM/clients" \
    -H "Authorization: Bearer $ACCESS_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "clientId": "'"$KEYCLOAK_CLIENT_ID_API_ADMIN"'",
        "protocol": "openid-connect",
        "name": "API Authorization Client",
        "enabled": true,
        "publicClient": false,
        "serviceAccountsEnabled": true,
        "standardFlowEnabled": false,
        "directAccessGrantsEnabled": false,
        "implicitFlowEnabled": false,
        "attributes": {
            "client_credentials.use_refresh_token": "false"
        }
    }' \
    | tr -d '\r' \
    | sed -n 's/^Location: .*\/\([0-9a-fA-F-]\{36\}\)$/\1/p')
    if [ -z "$API_ADMIN_CLIENT_UUID" ]; then
        finish_error 16 "Failed to create API Authorization client in Keycloak."
    fi
fi
echo "Finish: 3-1. API Authorization client '$KEYCLOAK_CLIENT_ID_API_ADMIN' already exists in Keycloak realm '$KEYCLOAK_REALM' with UUID: $API_ADMIN_CLIENT_UUID"

echo "Start: 3-2. Checking if protocol mapper for 'open_system_id' claim exists in API Authorization client '$KEYCLOAK_CLIENT_ID_API_ADMIN'..."
response_json=''
response_json=$(curl -sS -f -X GET "$KEYCLOAK_BASE_URL/admin/realms/$KEYCLOAK_REALM/clients?clientId=$KEYCLOAK_CLIENT_ID_API_ADMIN" \
    -H "Authorization: Bearer $ACCESS_TOKEN" \
    -H "Content-Type: application/json" \
    | tr -d '\r' \
    | grep $KEYCLOAK_CLAIM_OPEN_SYSTEM_ID_API_ADMIN)
echo "Checked for protocol mapper for 'open_system_id' claim in API Authorization client '$KEYCLOAK_CLIENT_ID_API_ADMIN' in Keycloak realm '$KEYCLOAK_REALM'."

# Create a protocol mapper to include the "open_system_id" claim in the access token for the API admin client
if [ -z "$response_json" ]; then
    echo "Create: 3-2. Protocol mapper for 'open_system_id' claim does not exist in API Authorization client '$KEYCLOAK_CLIENT_ID_API_ADMIN'. Creating protocol mapper..."
    http_code=''
    http_code=$(curl -sS -o /dev/null -w "%{http_code}" -i -X POST "$KEYCLOAK_BASE_URL/admin/realms/$KEYCLOAK_REALM/clients/$API_ADMIN_CLIENT_UUID/protocol-mappers/models" \
        -H "Authorization: Bearer $ACCESS_TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "name": "open_system_id_mapper",
            "protocol": "openid-connect",
            "protocolMapper": "oidc-hardcoded-claim-mapper",
            "config": {
                "claim.name": "open_system_id",
                "claim.value": "'"$KEYCLOAK_CLAIM_OPEN_SYSTEM_ID_API_ADMIN"'",
                "jsonType.label": "String",
                "id.token.claim": "false",
                "access.token.claim": "true",
                "userinfo.token.claim": "false",
                "introspection.token.claim": "true"
            }
        }')
    if [ "$http_code" -ne 201 ]; then
        finish_error 17 "Failed to create protocol mapper in Keycloak (HTTP $http_code)."
    fi
fi
echo "Finish: 3-2. Protocol mapper for 'open_system_id' claim created in API Authorization client '$KEYCLOAK_CLIENT_ID_API_ADMIN'."

# 3-3. Retrieve the client secret for the API admin client
echo "Start: 3-3. Retrieving client secret for API Authorization client '$KEYCLOAK_CLIENT_ID_API_ADMIN' from Keycloak..."
KEYCLOAK_CLIENT_SECRET_API_ADMIN=$(curl -sS -f -i -X GET "$KEYCLOAK_BASE_URL/admin/realms/$KEYCLOAK_REALM/clients?clientId=$KEYCLOAK_CLIENT_ID_API_ADMIN" \
    -H "Authorization: Bearer $ACCESS_TOKEN" \
    -H "Content-Type: application/json" \
    | tr -d '\r' \
    | sed -n 's/.*"secret":"\([^"]*\)".*/\1/p')
if [ -z "$KEYCLOAK_CLIENT_SECRET_API_ADMIN" ]; then
    finish_error 18 "Failed to retrieve client secret for API Authorization client from Keycloak."
fi
echo "Finish: 3-3. Retrieved client secret for API Authorization client from Keycloak: $(printf '%.20s' "$KEYCLOAK_CLIENT_SECRET_API_ADMIN")..."

# 4. Set up API authorization store in OpenFGA
# ---------------------------------------------------------
# Create Store in OpenFGA for API authorization and retrieve the store ID
echo "Start: 4-1. Creating API authorization store in OpenFGA..."
response_json=''
response_json=$(curl -sS -f -i -X POST \
  $OPENFGA_BASE_URL/stores \
  -H "Content-Type: application/json" \
  -d @"$SCRIPT_DIR/openfga/json/01-create-store-api-authz.json"
)
if [ $? -ne 0 ]; then
    finish_error 19 "Failed to create API authorization store in OpenFGA."
fi
API_AUTHZ_STORE_ID=$(printf '%s' "$response_json" \
  | tr -d '\n' \
  | sed -n 's/.*"id":"\([^"]*\)".*/\1/p')
echo "Finish: 4-1. API authorization store created in OpenFGA with ID: $API_AUTHZ_STORE_ID"

# Create the authorization model in the API authorization store
echo "Start: 4-2. Creating authorization model in API authorization store in OpenFGA..."
http_code=''
http_code=$(curl -sS -o /dev/null -w "%{http_code}" -i -X POST \
  $OPENFGA_BASE_URL/stores/$API_AUTHZ_STORE_ID/authorization-models \
  -H "Content-Type: application/json" \
  -d @"$SCRIPT_DIR/openfga/json/02-create-model-api-authz.json")
if [ "$http_code" -ne 201 ]; then
    finish_error 20 "Failed to create authorization model in OpenFGA (HTTP $http_code)."
fi
echo "Finish: 4-2. Authorization model created in API authorization store in OpenFGA."

# Create role tuples in the API authorization store
echo "Start: 4-3. Creating role tuples in API authorization store in OpenFGA..."
http_code=''
http_code=$(curl -sS -o /dev/null -w "%{http_code}" -i -X POST \
  $OPENFGA_BASE_URL/stores/$API_AUTHZ_STORE_ID/write \
  -H "Content-Type: application/json" \
  -d @"$SCRIPT_DIR/openfga/json/03-create-tuples-api-authz-role.json")
if [ "$http_code" -ne 200 ]; then
    finish_error 21 "Failed to create role tuples in OpenFGA (HTTP $http_code)."
fi
echo "Finish: 4-3. Role tuples created in API authorization store in OpenFGA."

# 5. Set up realm store binding in OpenFGA
# ---------------------------------------------------------
# Create Store in OpenFGA for realm-store binding and retrieve the store ID
echo "Start: 5-1. Creating realm store binding in OpenFGA..."
response_json=''
response_json=$(curl -sS -f -i -X POST \
  $OPENFGA_BASE_URL/stores \
  -H "Content-Type: application/json" \
  -d @"$SCRIPT_DIR/openfga/json/11-create-store-realm-store-binding.json"
)
if [ $? -ne 0 ]; then
    finish_error 22 "Failed to create realm store binding in OpenFGA."
fi
REALM_AUTHZ_STORE_ID=$(printf '%s' "$response_json" \
  | tr -d '\n' \
  | sed -n 's/.*"id":"\([^"]*\)".*/\1/p')
echo "Finish: 5-1. Realm store binding created in OpenFGA with ID: $REALM_AUTHZ_STORE_ID"

# Create the authorization model in the realm store binding
echo "Start: 5-2. Creating authorization model in realm store binding in OpenFGA..."
http_code=''
http_code=$(curl -sS -o /dev/null -w "%{http_code}" -i -X POST \
  $OPENFGA_BASE_URL/stores/$REALM_AUTHZ_STORE_ID/authorization-models \
  -H "Content-Type: application/json" \
  -d @"$SCRIPT_DIR/openfga/json/12-create-model-realm-store-binding.json")
if [ "$http_code" -ne 201 ]; then
    finish_error 23 "Failed to create authorization model in realm store binding in OpenFGA (HTTP $http_code)."
fi
echo "Finish: 5-2. Authorization model created in realm store binding in OpenFGA."

# 6. Set up operator-plant authorization in OpenFGA
# ---------------------------------------------------------
# Create Store in OpenFGA for operator-plant authorization and retrieve the store ID
echo "Start: 6-1. Creating operator-plant authorization store in OpenFGA..."
response_json=''
response_json=$(curl -sS -f -i -X POST \
  $OPENFGA_BASE_URL/stores \
  -H "Content-Type: application/json" \
  -d @"$SCRIPT_DIR/openfga/json/21-create-store-operator-plant.json"
)
if [ $? -ne 0 ]; then
    finish_error 24 "Failed to create operator-plant authorization store in OpenFGA."
fi
OPERATOR_PLANT_AUTHZ_STORE_ID=$(printf '%s' "$response_json" \
  | tr -d '\n' \
  | sed -n 's/.*"id":"\([^"]*\)".*/\1/p')
echo "Finish: 6-1. Operator-plant authorization store created in OpenFGA with ID: $OPERATOR_PLANT_AUTHZ_STORE_ID"

# Create the authorization model in the operator-plant authorization store
echo "Start: 6-2. Creating authorization model in operator-plant authorization store in OpenFGA..."
http_code=''
http_code=$(curl -sS -o /dev/null -w "%{http_code}" -i -X POST \
  $OPENFGA_BASE_URL/stores/$OPERATOR_PLANT_AUTHZ_STORE_ID/authorization-models \
  -H "Content-Type: application/json" \
  -d @"$SCRIPT_DIR/openfga/json/22-create-model-operator-plant.json")
if [ "$http_code" -ne 201 ]; then
    finish_error 25 "Failed to create authorization model in operator-plant authorization store in OpenFGA (HTTP $http_code)."
fi
echo "Finish: 6-2. Authorization model created in operator-plant authorization store in OpenFGA."

# Create role tuples in the operator-plant authorization store
echo "Start: 6-3. Creating role tuples in operator-plant authorization store in OpenFGA..."
http_code=''
http_code=$(curl -sS -o /dev/null -w "%{http_code}" -i -X POST \
  $OPENFGA_BASE_URL/stores/$OPERATOR_PLANT_AUTHZ_STORE_ID/write \
  -H "Content-Type: application/json" \
  -d @"$SCRIPT_DIR/openfga/json/23-create-tuples-operator-plant.json")
if [ "$http_code" -ne 200 ]; then
    finish_error 26 "Failed to create role tuples in operator-plant authorization store in OpenFGA (HTTP $http_code)."
fi
echo "Finish: 6-3. Role tuples created in operator-plant authorization store in OpenFGA."

# 7. Create user tuples in the API authorization store, substituting the user ID for the API admin client
# ----------------------------------------------------------
# Substitute the user ID for the API admin client in the JSON file and create user tuples in the API authorization store
echo "Start: 7-1. Creating user tuples in API authorization store in OpenFGA..."
http_code=''
http_code=$(AUTHZ_USER_ID=$KEYCLOAK_CLAIM_OPEN_SYSTEM_ID_API_ADMIN envsubst < "$SCRIPT_DIR/openfga/json/04-create-tuples-api-authz-userid.json" | curl -sS -o /dev/null -w "%{http_code}" -X POST \
  $OPENFGA_BASE_URL/stores/$API_AUTHZ_STORE_ID/write \
  -H "Content-Type: application/json" \
  -d @-)
if [ "$http_code" -ne 200 ]; then
    finish_error 27 "Failed to create user tuples in OpenFGA (HTTP $http_code)."
fi
echo "Finish: 7-1. User tuples created in API authorization store in OpenFGA."

echo "Start: 7-2. Creating user tuples in operator-plant authorization store in OpenFGA..."
http_code=''
http_code=$(AUTHZ_USER_ID=$KEYCLOAK_CLAIM_OPEN_SYSTEM_ID_API_ADMIN envsubst < "$SCRIPT_DIR/openfga/json/24-create-tuples-operator-plant-admin.json" | curl -sS -o /dev/null -w "%{http_code}" -X POST \
  $OPENFGA_BASE_URL/stores/$OPERATOR_PLANT_AUTHZ_STORE_ID/write \
  -H "Content-Type: application/json" \
  -d @-)
if [ "$http_code" -ne 200 ]; then
    finish_error 28 "Failed to create user tuples in OpenFGA (HTTP $http_code)."
fi
echo "Finish: 7-2. User tuples created in operator-plant authorization store in OpenFGA."

# 8. Set up OPEN-DATASPACES database in PostgreSQL
# ---------------------------------------------------------
# Substitute the variables in the SQL script and execute it using psql
echo "Start: 8-1. Setting up OPEN-DATASPACES database in PostgreSQL..."
PGPASSWORD="$POSTGRES_PASSWORD" psql \
    -U $POSTGRES_USER \
    -d $POSTGRES_DB \
    -h $POSTGRES_HOST \
    -p $POSTGRES_PORT \
    -v ON_ERROR_STOP=1 \
    -f "$SCRIPT_DIR/sql/4_setup_app_table.sql" \
    -v idp_realm="$KEYCLOAK_REALM" \
    -v pdp_store_id_api_authz="$API_AUTHZ_STORE_ID" \
    -v pdp_store_id_realm_store_binding="$REALM_AUTHZ_STORE_ID" \
    -v pdp_store_id_operator_plant_authz="$OPERATOR_PLANT_AUTHZ_STORE_ID" \
    -v api_key="$L3TBL_API_KEY" \
    -v api_key_id="$L3TBL_API_KEYS_ID" \
    -v api_key_name="$L3TBL_API_KEYS_NAME" \
    -v usecase="$L3TBL_API_KEYS_USECASE" \
    -v environment_name="$L3TBL_AUTHZ_STORES_ENVIRONMENT_NAME" \
    -v cidr="$L3TBL_CIDRS_CIDR"
if [ $? -ne 0 ]; then
    finish_error 29 "Failed to set up database in PostgreSQL."
fi
echo "Finish: 8-1. Database setup in PostgreSQL completed."

# Final output
# ---------------------------------------------------------
echo "-------------------------------------------------------------------------"
echo "Setup completed successfully!"
echo "Summary of created resources and configuration:"
echo "Keycloak realm: $KEYCLOAK_REALM"
echo "API Authorization client ID in Keycloak: $KEYCLOAK_CLIENT_ID_API_ADMIN"
echo "API Authorization client secret in Keycloak: $KEYCLOAK_CLIENT_SECRET_API_ADMIN"
echo "API Authorization store ID in OpenFGA: $API_AUTHZ_STORE_ID"
echo "Realm-store binding store ID in OpenFGA: $REALM_AUTHZ_STORE_ID"
echo "Operator-plant authorization store ID in OpenFGA: $OPERATOR_PLANT_AUTHZ_STORE_ID"
echo "System API key ID in PostgreSQL: $L3TBL_API_KEYS_ID"
