#!/usr/bin/env bash

set -ex

source ./data_core.sh

DATA_PARTITION_URL="http://${PARTITION_HOST}/api/partition/v1/partitions/${DATA_PARTITION_ID}"

bootstrap_partition() {

  local DATA_PARTITION_ID=$1
  local BOOTSTRAP_DATA=$2
  local PARTITION_URL=$3

  echo "Bootstrapping partition: $DATA_PARTITION_ID"
  echo "$BOOTSTRAP_DATA" | jq

  status_code=$(curl -X POST \
     --url "$PARTITION_URL" --write-out "%{http_code}" --silent --output "/dev/null" \
     -H "Content-Type: application/json" \
     --data-raw "$BOOTSTRAP_DATA")

  # shellcheck disable=SC2002
  if [[ "${status_code}" == 201 ]]; then
    echo "Partition bootstrap finished successfully!"
  elif [[ "${status_code}" == 409 ]]; then

    patch_status_code=$(curl -X PATCH \
    --url "$PARTITION_URL" --write-out "%{http_code}" --silent --output "/dev/null" \
    -H "Content-Type: application/json" \
    --data-raw "$BOOTSTRAP_DATA")

    echo "Partition was patched because partition $DATA_PARTITION_ID already has properties! Status code: ${patch_status_code}"
  else
    echo "Exiting with status code: ${status_code}"
    exit 1
  fi
}

# Bootstrap additional partition
export DATA_PARTITION_ID_VALUE="${DATA_PARTITION_ID}"
bootstrap_partition "${DATA_PARTITION_ID}" "$(core_partition_data)" "${DATA_PARTITION_URL}"

touch /tmp/bootstrap_ready
