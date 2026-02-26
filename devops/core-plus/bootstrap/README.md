# Partition Bootstrapping Script

## Overview

This folder contains a set of Bash scripts designed to bootstrap and configure data partitions. These scripts are used to initialize the necessary settings and properties for system and additional data partitions in a given environment.

## Key Environment Variables

| Environment Variable        | Description                                      |
| --------------------------- | ------------------------------------------------ |
| `PARTITION_HOST`            | Host for the partition API.                      |
| `DATA_PARTITION_ID`         | Identifier for the data partition.               |
| `BUCKET_PREFIX`             | Prefix for bucket names used in the partition.   |
| `PARTITION_SUFFIX`          | Suffix used for partition-specific properties.   |
| `SERVICE_ACCOUNT`           | Service account                                  |
| `MINIO_ENDPOINT`            | Endpoint for the MinIO service.                  |
| `MINIO_EXTERNAL_ENDPOINT`   | External endpoint for accessing MinIO.           |
| `MINIO_IGNORE_CERT_CHECK`   | Flag to ignore SSL certificate checks for MinIO. |
| `MINIO_UI_ENDPOINT`         | Endpoint for the MinIO user interface.           |
| `INDEXER_AUGMENTER_ENABLED` | Flag to enable or disable the index augmenter.   |

## Scripts

1. **bootstrap_partition.sh**
2. **data_core.sh**
3. **helpers.sh**

### bootstrap_partition.sh

This script is responsible for bootstrapping a data partition. It performs the following tasks:

- Sources helper functions from `helpers.sh` and core data functions from `data_core.sh`.
- Defines the `bootstrap_partition` function to bootstrap a partition by making HTTP POST and PATCH requests to a specified URL.
- Exports environment variables needed for system and additional partitions.
- Calls the `bootstrap_partition` function with appropriate data for system and additional partitions.
- Creates a temporary file to signal that the bootstrap process is complete.

### data_core.sh

This script contains functions to generate JSON data for core system and additional partitions. These functions output JSON structures with various properties required for the partitions.

### helpers.sh

This script provides utility functions to support the bootstrapping process.