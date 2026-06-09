# Partition Bootstrapping Script

## Overview

This folder contains Bash scripts and a Dockerfile to bootstrap and configure a data partition. The scripts initialize partition properties by calling the Partition API, creating or updating the partition as needed.

## Key Environment Variables

| Environment Variable              | Description                                                |
| --------------------------------- | ---------------------------------------------------------- |
| `PARTITION_HOST`                  | Host for the partition API.                                |
| `DATA_PARTITION_ID`               | Identifier for the data partition.                         |
| `BUCKET_PREFIX`                   | Prefix for bucket names used in the partition.             |
| `PARTITION_SUFFIX`                | Suffix used for partition-specific secret references.      |
| `SERVICE_ACCOUNT`                 | Service account associated with the partition.             |
| `SECRET_SERVICE_NAMESPACE`        | Kubernetes namespace for the secret service.               |
| `MINIO_ENDPOINT`                  | Endpoint for the MinIO service.                            |
| `MINIO_EXTERNAL_ENDPOINT`         | External endpoint for accessing MinIO.                     |
| `MINIO_IGNORE_CERT_CHECK`         | Flag to ignore SSL certificate checks for MinIO.           |
| `MINIO_UI_ENDPOINT`               | Endpoint for the MinIO user interface.                     |
| `ELASTIC_HTTPS`                   | Flag to enable HTTPS for Elasticsearch connections.        |
| `INDEXER_AUGMENTER_ENABLED`       | Flag to enable or disable the index augmenter.             |
| `EDS_ENABLED`                     | Feature flag to enable or disable EDS.                     |
| `POLICY_SERVICE_ENABLED`          | Feature flag to enable or disable OPA/policy enforcement.  |
| `AUTOCOMPLETE_ENABLED`            | Feature flag to enable or disable autocomplete.            |
| `AS_INGESTED_COORDINATES_ENABLED` | Feature flag to enable or disable as-ingested coordinates. |
| `KEYWORD_LOWER_ENABLED`           | Feature flag to enable or disable lowercase keywords.      |
| `BAG_OF_WORDS_ENABLED`            | Feature flag to enable or disable bag-of-words indexing.   |
| `COLLABORATIONS_ENABLED`          | Feature flag to enable or disable collaborations.          |

## Scripts

1. **bootstrap_partition.sh**
2. **data_core.sh**

### bootstrap_partition.sh

This script bootstraps the data partition. It performs the following tasks:

- Sources core data functions from `data_core.sh`.
- Defines the `bootstrap_partition` function that makes an HTTP POST request to create the partition; if the partition already exists (HTTP 409), it falls back to a PATCH request to update its properties.
- Calls `bootstrap_partition` with the JSON payload produced by `core_partition_data`.
- Creates `/tmp/bootstrap_ready` to signal that the bootstrap process is complete.

### data_core.sh

This script defines the `core_partition_data` function, which outputs the full JSON payload for the partition. The payload includes properties for storage buckets, PostgreSQL, MinIO, SeaweedFS, RabbitMQ, Elasticsearch, Entitlements, and feature flags.

## Docker

The `Dockerfile` builds an Alpine-based image that runs `bootstrap_partition.sh` on startup and then sleeps to keep the container alive. Required environment variables must be supplied at runtime (e.g. via Kubernetes environment or Docker `--env`/`--env-file`).