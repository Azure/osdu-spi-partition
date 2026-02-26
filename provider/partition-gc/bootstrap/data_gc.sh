#!/usr/bin/env bash

gc_partition_data() {
  DATA_PARTITION_ID_UPPER=$(echo "${DATA_PARTITION_ID_VALUE//-/_}" | tr '[:lower:]' '[:upper:]')
  cat <<EOF
{
  "properties": {
    "projectId": {
      "sensitive": false,
      "value": "${PROJECT_ID}"
    },
    "serviceAccount": {
      "sensitive": false,
      "value": "${SERVICE_ACCOUNT}"
    },
    "complianceRuleSet": {
      "sensitive": false,
      "value": "shared"
    },
    "dataPartitionId": {
      "sensitive": false,
      "value": "${DATA_PARTITION_ID_VALUE}"
    },
    "name": {
      "sensitive": false,
      "value": "${DATA_PARTITION_ID_VALUE}"
    },
    "crmAccountID": {
      "sensitive": false,
      "value": "[${DATA_PARTITION_ID_VALUE},${DATA_PARTITION_ID_VALUE}]"
    },
    "entitlements.datasource.url": {
      "sensitive": true,
      "value": "ENT_PG_URL${PARTITION_SUFFIX}"
    },
    "entitlements.datasource.username": {
      "sensitive": true,
      "value": "ENT_PG_USER${PARTITION_SUFFIX}"
    },
    "entitlements.datasource.password": {
      "sensitive": true,
      "value": "ENT_PG_PASS${PARTITION_SUFFIX}"
    },
    "entitlements.datasource.schema": {
      "sensitive": true,
      "value": "ENT_PG_SCHEMA_${DATA_PARTITION_ID_UPPER}"
    },
    "bucket": {
      "sensitive": false,
      "value": "${PROJECT_ID}-${DATA_PARTITION_ID_VALUE}-records"
    },
    "reservoir-connection": {
      "sensitive": true,
      "value": "RESERVOIR_POSTGRES_CONN_STRING${PARTITION_SUFFIX}"
    },
    "kubernetes-secret-name": {
      "sensitive": false,
      "value": "eds-${DATA_PARTITION_ID_VALUE}"
    },
    "index-augmenter-enabled": {
      "sensitive": false,
      "value": "${INDEXER_AUGMENTER_ENABLED}"
    },
    "osm.datastore.database.id": {
      "sensitive": false,
      "value": "${DATABASE_ID}"
    },
    "wellbore-dms-bucket": {
      "sensitive": false,
      "value": "${PROJECT_ID}-${DATA_PARTITION_ID_VALUE}-wellbore"
    },
    "schema.bucket.name": {
      "sensitive": false,
      "value": "${PROJECT_ID}-${DATA_PARTITION_ID_VALUE}-schema"
    },
    "eds.enabled": {
      "sensitive": false,
      "value": "${EDS_ENABLED}"
    },
    "featureFlag.opa.enabled": {
      "sensitive": false,
      "value": "${POLICY_SERVICE_ENABLED}"
    },
    "featureFlag.policy.enabled": {
      "sensitive": false,
      "value": "${POLICY_SERVICE_ENABLED}"
    },
    "featureFlag.autocomplete.enabled": {
      "sensitive": false,
      "value": "${AUTOCOMPLETE_ENABLED}"
    },
    "featureFlag.asIngestedCoordinates.enabled": {
      "sensitive": false,
      "value": "${AS_INGESTED_COORDINATES_ENABLED}"
    },
    "featureFlag.keywordLower.enabled": {
      "sensitive": false,
      "value": "${KEYWORD_LOWER_ENABLED}"
    },
    "featureFlag.bagOfWords.enabled": {
      "sensitive": false,
      "value": "${BAG_OF_WORDS_ENABLED}"
    },
    "featureFlag.mapBooleanToString.enabled": {
      "sensitive": false,
      "value": true
    },
    "featureFlag.xCollaboration.enabled": {
      "sensitive": false,
      "value": "${X_COLLABORATION_ENABLED}"
    },
    "sd.ksd.k8s.namespace": {
        "sensitive": false,
        "value": "secret-admin"
    },
    "seismicBucket": {
      "sensitive": false,
      "value": "${PROJECT_ID}-${DATA_PARTITION_ID_VALUE}-ss-seismic"
    },
    "elasticsearch.8.host": {
      "sensitive": true,
      "value": "ELASTIC_HOST${PARTITION_SUFFIX}"
    },
    "elasticsearch.8.port": {
      "sensitive": true,
      "value": "ELASTIC_PORT${PARTITION_SUFFIX}"
    },
    "elasticsearch.8.user": {
      "sensitive": true,
      "value": "ELASTIC_USER${PARTITION_SUFFIX}"
    },
    "elasticsearch.8.password": {
      "sensitive": true,
      "value": "ELASTIC_PASS${PARTITION_SUFFIX}"
    },
    "elasticsearch.8.https": {
      "sensitive": false,
      "value": "${ELASTIC_HTTPS}"
    },
    "elasticsearch.8.tls": {
      "sensitive": false,
      "value": "${ELASTIC_HTTPS}"
    }
  }
}
EOF
}
