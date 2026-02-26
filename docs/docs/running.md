# Running

## Community Implementation

It is containing an Open-Source version with os-osm Driver containing the postgres db for consumption. As of now the code is not having any authentication for core-plus as it is not having the infra for authentication.
So, currently it's only enough to run and consume the service locally, for development and understanding purpose.

One could either spin up a postgres docker container locally, and use along with local partition service code. Or else, fetch the container image for postgres service and Partition service both and then use them. More details [here](#running-locally---partition-core-plus) 

## Running Locally - AWS

Instructions for running the AWS implementation locally can be found [here](https://community.opengroup.org/osdu/platform/system/partition/-/blob/master/provider/partition-aws/README.md)

## Running Locally - Azure

Instructions for running the Azure implementation locally can be found [here](https://community.opengroup.org/osdu/platform/system/partition/-/blob/master/provider/partition-azure/README.md)

## Running Locally - Google Cloud

Instructions for running the Google Cloud implementation locally can be found [here](https://community.opengroup.org/osdu/platform/system/partition/-/blob/master/provider/partition-gc/README.md)

## Running Locally - IBM

## Running Locally - Partition Core Plus
Instructions for running the Partion Core Plus can be found [here](https://community.opengroup.org/osdu/platform/system/partition/-/blob/master/partition-core-plus/README.md)

## Running Integration Tests

Instructions for running the integration tests can be found [here](https://community.opengroup.org/osdu/platform/system/partition/-/blob/master/testing/README.md)

### Open API 3.0 - Swagger
- Swagger UI : https://host/context-path/swagger (will redirect to https://host/context-path/swagger-ui/index.html)
- api-docs (JSON) : https://host/context-path/api-docs
- api-docs (YAML) : https://host/context-path/api-docs.yaml

All the Swagger and OpenAPI related common properties are managed here [swagger.properties](https://community.opengroup.org/osdu/platform/system/partition/-/blob/master/partition-core/src/main/resources/swagger.properties)

#### Server Url(full path vs relative path) configuration
- `api.server.fullUrl.enabled=true` It will generate full server url in the OpenAPI swagger
- `api.server.fullUrl.enabled=false` It will generate only the contextPath only
- default value is false (Currently only in Azure it is enabled)
[Reference]:(https://springdoc.org/faq.html#_how_is_server_url_generated) 