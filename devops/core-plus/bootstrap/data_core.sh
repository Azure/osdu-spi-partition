#!/usr/bin/env bash

# FIXME (GONRG-7695): Move elastic properties to additional partition when resolved
# FIXME (GONRG-7696): Move rabbitmq properties to additional partition when resolved
core_partition_data() {
  DATA_PARTITION_ID_UPPER="${DATA_PARTITION_ID_VALUE^^}"
  cat <<EOF
{
  "properties": {
    "projectId": {
      "sensitive": false,
      "value": "${BUCKET_PREFIX}"
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
    "bucket": {
      "sensitive": false,
      "value": "${BUCKET_PREFIX}-${DATA_PARTITION_ID_VALUE}-records"
    },
    "crmAccountID": {
      "sensitive": false,
      "value": "[${DATA_PARTITION_ID_VALUE},${DATA_PARTITION_ID_VALUE}]"
    },
    "osm.postgres.datasource.url": {
      "sensitive": true,
      "value": "POSTGRES_DATASOURCE_URL${PARTITION_SUFFIX}"
    },
    "osm.postgres.datasource.username": {
      "sensitive": true,
      "value": "POSTGRES_DB_USERNAME${PARTITION_SUFFIX}"
    },
    "osm.postgres.datasource.password": {
      "sensitive": true,
      "value": "POSTGRES_DB_PASSWORD${PARTITION_SUFFIX}"
    },
    "obm.minio.endpoint": {
      "sensitive": false,
      "value": "${MINIO_ENDPOINT}"
    },
    "obm.minio.accessKey": {
      "sensitive": true,
      "value": "MINIO_ACCESS_KEY"
    },
    "obm.minio.secretKey": {
      "sensitive": true,
      "value": "MINIO_SECRET_KEY"
    },
    "obm.minio.ignoreCertCheck": {
      "sensitive": false,
      "value": "${MINIO_IGNORE_CERT_CHECK}"
    },
    "obm.minio.ui.endpoint": {
        "sensitive": false,
        "value": "${MINIO_UI_ENDPOINT}"
    },
    "kubernetes-secret-name": {
      "sensitive": false,
      "value": "eds-${DATA_PARTITION_ID_VALUE}"
    },
    "oqm.rabbitmq.amqp.host": {
      "sensitive": false,
      "value": "rabbitmq"
    },
    "oqm.rabbitmq.amqp.port": {
      "sensitive": false,
      "value": "5672"
    },
    "oqm.rabbitmq.amqp.path": {
      "sensitive": false,
      "value": ""
    },
    "oqm.rabbitmq.amqp.username": {
      "sensitive": true,
      "value": "RABBITMQ_ADMIN_USERNAME"
    },
    "oqm.rabbitmq.amqp.password": {
      "sensitive": true,
      "value": "RABBITMQ_ADMIN_PASSWORD"
    },
    "oqm.rabbitmq.admin.schema": {
      "sensitive": false,
      "value": "http"
    },
    "oqm.rabbitmq.admin.host": {
      "sensitive": false,
      "value": "rabbitmq"
    },
    "oqm.rabbitmq.admin.port": {
      "sensitive": false,
      "value": "15672"
    },
    "oqm.rabbitmq.admin.path": {
      "sensitive": false,
      "value": "/api"
    },
    "oqm.rabbitmq.admin.username": {
      "sensitive": true,
      "value": "RABBITMQ_ADMIN_USERNAME"
    },
    "oqm.rabbitmq.admin.password": {
      "sensitive": true,
      "value": "RABBITMQ_ADMIN_PASSWORD"
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
    "system.schema.bucket.name": {
      "sensitive": false,
      "value": "${BUCKET_PREFIX}-${DATA_PARTITION_ID_VALUE}-system-schema"
    },
    "schema.bucket.name": {
      "sensitive": false,
      "value": "${BUCKET_PREFIX}-${DATA_PARTITION_ID_VALUE}-schema"
    },
    "obm.minio.external.endpoint": {
      "sensitive": false,
      "value": "${MINIO_EXTERNAL_ENDPOINT}"
    },
    "wellbore-dms-bucket": {
      "sensitive": false,
      "value": "${BUCKET_PREFIX}-${DATA_PARTITION_ID_VALUE}-wellbore"
    },
    "sd.ksd.k8s.namespace": {
        "sensitive": false,
        "value": "${SECRET_SERVICE_NAMESPACE}"
    },
    "index-augmenter-enabled": {
      "sensitive": false,
      "value": "${INDEXER_AUGMENTER_ENABLED}"
    },
    "featureFlag.eds.enabled": {
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
    "collaborations-enabled" : {
      "sensitive" : false,
      "value" : "${COLLABORATIONS_ENABLED}"
    },
    "featureFlag.autocomplete.enabled": {
      "sensitive": false,
      "value": "${AUTOCOMPLETE_ENABLED}"
    },
    "reservoir-connection": {
        "sensitive": true,
        "value": "RESERVOIR_POSTGRES_CONN_STRING_OSDU"
    }
  }
}
EOF
}
