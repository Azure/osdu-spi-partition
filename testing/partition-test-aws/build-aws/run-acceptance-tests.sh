#!/bin/bash
# Copyright © Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -e

# Store current directory
CUR_DIR=$(pwd)
SCRIPT_SOURCE_DIR=$(dirname "$0")
cd "$SCRIPT_SOURCE_DIR"


# Required variables for the tests
export CLIENT_TENANT=common
export MY_TENANT=opendes

export IDP_NAME=$(aws ssm get-parameter --name "/osdu/instances/${OSDU_INSTANCE_NAME}/config/idp/name" --query Parameter.Value --output text --region $AWS_REGION)
export CLIENT_CREDENTIALS_CLIENT_ID=$(aws ssm get-parameter --name "/osdu/idp/${IDP_NAME}/client/client-credentials/id" --query Parameter.Value --output text --region $AWS_REGION)
export CLIENT_CREDENTIALS_CLIENT_SECRET=$(aws secretsmanager get-secret-value --secret-id /osdu/idp/${IDP_NAME}/client-credentials-secret --query SecretString --output json --region $AWS_REGION | sed -e 's/\\\"/\"/g' -e 's/^.//g' -e 's/.$//g' | jq -r '.client_credentials_client_secret')

export IDP_AUTH_TOKEN_URI=$(aws ssm get-parameter --name "/osdu/idp/${IDP_NAME}/oauth/token-uri" --query Parameter.Value --output text --region $AWS_REGION)
export IDP_ALLOWED_SCOPES=$(aws ssm get-parameter --name "/osdu/idp/${IDP_NAME}/oauth/allowed-scopes" --query Parameter.Value --output text --region $AWS_REGION)
export SERVICE_PRINCIPAL_AUTHORIZATION=$(echo -n "${CLIENT_CREDENTIALS_CLIENT_ID}:${CLIENT_CREDENTIALS_CLIENT_SECRET}" | base64 | tr -d "\n")
export ROOT_USER_TOKEN=$(curl --location ${IDP_AUTH_TOKEN_URI} --header "Content-Type:application/x-www-form-urlencoded" --header "Authorization:Basic ${SERVICE_PRINCIPAL_AUTHORIZATION}" --data-urlencode "grant_type=client_credentials" --data-urlencode ${IDP_ALLOWED_SCOPES}  --http1.1 | jq -r '.access_token')



# Run the tests
mvn clean test
TEST_EXIT_CODE=$?

# Return to original directory
cd "$CUR_DIR"

# Copy test reports if output directory is specified
if [ -n "$1" ]; then
  mkdir -p "$1/partition-acceptance-test"
  cp -R "$SCRIPT_SOURCE_DIR/target/surefire-reports/"* "$1/partition-acceptance-test"
fi

exit $TEST_EXIT_CODE
