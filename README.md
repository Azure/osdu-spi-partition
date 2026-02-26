# Introduction

The Partition service is responsible for creating and retrieving partition specific properties on behalf of other services whether they are secret values or not. It is a Maven multi-module project with each cloud implementation placed in its submodule.

## Community Implementation
It is containing an Open-Source version with os-osm Driver containing the postgres db for consumption. As of now the code is not having any authentication for core-plus as it is not having the infra for authentication.
So, currently it's only enough to run and consume the service locally, for development and understanding purpose.

One could either spin up a postgres docker container locally, and use along with local partition service code. Or else, fetch the container image for postgres service and Partition service both and then use them. More details [here](./README.md#running-locally---partition-core-plus) 

## Running Locally - AWS

Instructions for running the AWS implementation locally can be found [here](./provider/partition-aws/README.md)

## Running Locally - Azure

Instructions for running the Azure implementation locally can be found [here](./provider/partition-azure/README.md)

## Running Locally - Google Cloud

Instructions for running the Google Cloud implementation locally can be found [here](./provider/partition-gc/README.md)

## Running Locally - IBM

## Running Locally - Partition Core Plus
Instructions for running the Partion Core Plus can be found [here](./partition-core-plus/README.md)

## Running Integration Tests

Instructions for running the integration tests can be found [here](./testing/README.md)

### Open API 3.0 - Swagger
- Swagger UI : https://host/context-path/swagger (will redirect to https://host/context-path/swagger-ui/index.html)
- api-docs (JSON) : https://host/context-path/api-docs
- api-docs (YAML) : https://host/context-path/api-docs.yaml

All the Swagger and OpenAPI related common properties are managed here [swagger.properties](./partition-core/src/main/resources/swagger.properties)

#### Server Url(full path vs relative path) configuration
- `api.server.fullUrl.enabled=true` It will generate full server url in the OpenAPI swagger
- `api.server.fullUrl.enabled=false` It will generate only the contextPath only
- default value is false (Currently only in Azure it is enabled)
[Reference]:(https://springdoc.org/faq.html#_how_is_server_url_generated) 

## License

Copyright 2017-2020, Schlumberger

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
