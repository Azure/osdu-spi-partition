## Service Configuration for Google Cloud

## Environment variables

Define the following environment variables.

Must have:

| name                     | value       | description                                                                     | sensitive? | source                              |
|--------------------------|-------------|---------------------------------------------------------------------------------|------------|-------------------------------------|
| `SPRING_PROFILES_ACTIVE` | ex `gcp`    | Spring profile that activate default configuration for Google Cloud environment | false      | -                                   |
| `GOOGLE_CLOUD_PROJECT`   | ex `google` | Google Cloud Project Id                                                         | false      | <https://console.cloud.google.com/> |

Defined in default application property file but possible to override:

| name                            | value                   | description                        | sensitive? | source |
|---------------------------------|-------------------------|------------------------------------|------------|--------|
| `LOG_LEVEL`                     | `****`                  | Logging level                      | no         | -      |
| `LOG_PREFIX`                    | `service`               | Logging prefix                     | no         | -      |
| `SERVER_SERVLET_CONTEXPATH`     | `/api/partition/v1`     | Servlet context path               | no         | -      |
| `PARTITION_PROPERTY_KIND`       | ex `PartitionProperty`  | Kind name to store the properties. | no         | -      |
| `PARTITION_NAMESPACE`           | ex `partition`          | Namespace for database.            | no         | -      |
| `SYSTEM_PARTITION_ID`           | ex `syspartition`       | Name of system partition.          | yes        | -      |
| `SYSTEM_TENANT_API`             | ex `true`               | Enables system API                 | no         | -      |
| `MANAGEMENT_ENDPOINTS_WEB_BASE` | ex `/`                  | Web base for Actuator              | no         | -      |
| `MANAGEMENT_SERVER_PORT`        | ex `8081`               | Port for Actuator                  | no         | -      |

These variables define service behavior, and are used to switch between `baremetal` or `gc` environments, their overriding and usage in mixed mode was not tested.
Usage of spring profiles is preferred.

| `ENVIRONMENT` | `gcp` or `anthos` | If `anthos` then authorization is disabled | no | - |

### Running E2E Tests

You will need to have the following environment variables defined.

| name                    | value                                    | description                                                                                                              | sensitive? | source                                                       |
|-------------------------|------------------------------------------|--------------------------------------------------------------------------------------------------------------------------|------------|--------------------------------------------------------------|
| `ENVIRONMENT`           | ex `dev`                                 |                                                                                                                          | no         |                                                              |
| `PARTITION_BASE_URL`    | ex `http://localhost:8080/`              | service base URL                                                                                                         | yes        |                                                              |
| `CLIENT_TENANT`         | ex `opendes`                             | name of the client partition                                                                                             | yes        |                                                              |
| `MY_TENANT`             | ex `opendes`                             | name of the OSDU partition                                                                                               | yes        |                                                              |
| `INTEGRATION_TESTER`    | `ewogICJ0....` or `tmp/service-acc.json` | Service account base64 encoded string or path to a file for API calls. Note: this user must be `PARTITION_ADMIN_ACCOUNT` | yes        | <https://console.cloud.google.com/iam-admin/serviceaccounts> |
| `NO_DATA_ACCESS_TESTER` | `ewogICJ0....` or `tmp/service-acc.json` | Service account base64 encoded string or path to a file for API calls. Without data access                               | yes        | <https://console.cloud.google.com/iam-admin/serviceaccounts> |

Execute following command to build code and run all the integration tests:

```bash
# Note: this assumes that the environment variables for integration tests as outlined
#       above are already exported in your environment.
$ (cd testing/partition-test-gc/ && mvn clean test)
```

## Google Cloud service account configuration

TBD

| Required roles |
|----------------|
| -              |

## License

Copyright © Google LLC
Copyright © EPAM Systems

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
