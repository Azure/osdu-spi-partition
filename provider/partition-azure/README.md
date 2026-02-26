# Running Locally - Azure

## Requirements

In order to run this service locally, you will need the following:
- JDK 17
- [Maven 3.8.0+](https://maven.apache.org/download.cgi)
- Lombok 1.18 or later

## General Tips

**Environment Variable Management**
The following tools make environment variable configuration simpler
 - [direnv](https://direnv.net/) - for a shell/terminal environment
 - [EnvFile](https://plugins.jetbrains.com/plugin/7861-envfile) - for [Intellij IDEA](https://www.jetbrains.com/idea/)

**Lombok**
This project uses [Lombok](https://projectlombok.org/) for code generation. You may need to configure your IDE to take advantage of this tool.
 - [Intellij configuration](https://projectlombok.org/setup/intellij)
 - [VSCode configuration](https://projectlombok.org/setup/vscode)
 
### Environment Variables

In order to run the service locally, you will need to have the following environment variables defined.

**Note** The following command can be useful to pull secrets from keyvault:
```bash
az keyvault secret show --vault-name $KEY_VAULT_NAME --name $KEY_VAULT_SECRET_NAME --query value -otsv
```

**Required to run service**

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `AZURE_TENANT_ID` | `********` | AD tenant to authenticate users from | yes | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-tenant-id` |
| `AZURE_CLIENT_ID` | `********` | Identity to run the service locally. This enables access to Azure resources. You only need this if running locally | yes | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-username` |
| `AZURE_CLIENT_SECRET` | `********` | Secret for `$AZURE_CLIENT_ID` | yes | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-password` |
| `KEYVAULT_URI` | (non-secret) | KeyVault URI | no | variable `AZURE_KEYVAULT_URI` from GitLab variable group `Azure Target Env - {{env}}` |
| `azure.activedirectory.app-resource-id` | `********` | AAD client application ID | yes | output of infrastructure deployment |
| `azure.activedirectory.client-id` | `********` | AAD client application ID | yes | keyvault secret: `$KEYVAULT_URI/secrets/aad-client-id` |
| `azure.activedirectory.AppIdUri` | `api://${azure.activedirectory.client-id}` | URI for AAD Application | no | -- |
| `azure.activedirectory.session-stateless` | `true` | Flag run in stateless mode (needed by AAD dependency) | no | -- |
| `appinsights_key` | `********` | Application Insights Instrumentation Key, required to hook AppInsights with Partition application | yes | keyvault secret: `$KEYVAULT_URI/secrets/appinsights-key` |
| `cache.provider` | (non-secret) | Cache to be used (can use `vm` for local testing) | no | - |
| `redis.ssl.enabled` | (non-secret) | `true` if connecting to redis cache with SSL enabled, `false` otherwise | no | -

**Required to run integration tests**

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `PARTITION_BASE_URL` | ex `http://localhost:8080/` | The host where the service is running. NO CONTEXT! | no | -- |
| `ENVIRONMENT` | ex `LOCAL` | The environment name | no | LOCAL/HOSTED |
| `MY_TENANT` | ex `opendes` | OSDU tenant used for testing | no | -- |
| `CLIENT_TENANT` | ex `common` | Client tenant used for testing | no | -- |
| `DEFAULT_PARTITION` | ex `opendes` | Default Tenant Name used bypasses Data Preperation and Teardown of tests | no | -- |
| `AZURE_AD_TENANT_ID` | `********` | AD tenant to authenticate users from | yes | -- |
| `INTEGRATION_TESTER` | `********` | System identity to assume for API calls. Note: this user must have entitlements configured already | no | -- |
| `AZURE_TESTER_SERVICEPRINCIPAL_SECRET` | `********` | Secret for `$INTEGRATION_TESTER` | yes | -- |
| `AZURE_AD_APP_RESOURCE_ID` | `********` | AAD client application ID | yes | output of infrastructure deployment |
| `AZURE_AD_OTHER_APP_RESOURCE_ID` | `********` | AAD client application ID for another application | yes | -- |
| `NO_DATA_ACCESS_TESTER` | `********` | Service principal ID of a service principal without entitlements | yes | `aad-no-data-access-tester-client-id` secret from keyvault |
| `NO_DATA_ACCESS_TESTER_SERVICEPRINCIPAL_SECRET` | `********` | Secret for `$NO_DATA_ACCESS_TESTER` | yes | `aad-no-data-access-tester-secret` secret from keyvault |



### Configure Maven

Check that maven is installed:
```bash
$ mvn --version
Apache Maven 3.8.0
Maven home: /usr/share/maven
Java version: 17.0.7
...
```


### Build and run the application

After configuring your environment as specified above, you can follow these steps to build and run the application. These steps should be invoked from the *repository root.*

```bash
# build + test + install core service code
$ mvn clean install

# build + test + package azure service code
$ (cd provider/partition-azure/ && mvn clean package)

# run service
#
# Note: this assumes that the environment variables for running the service as outlined
#       above are already exported in your environment.
$ java -jar $(find provider/partition-azure/target/ -name '*-spring-boot.jar')
```


### Test the application

After the service has started it should be accessible via a web browser by visiting [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html). If the request does not fail, you can then run the integration tests.

```bash
# build + install integration test core
$ (cd testing/partition-test-core/ && mvn clean install)

# build + run Azure integration tests.
#
# Note: this assumes that the environment variables for integration tests as outlined
#       above are already exported in your environment.
$ (cd testing/partition-test-azure/ && mvn clean test)
```

A liveness check can also be performed at `http://localhost:8080/api/partition/v1/actuator/health`. Other apis can be found on the swagger page

## Debugging

Jet Brains - the authors of Intellij IDEA, have written an [excellent guide](https://www.jetbrains.com/help/idea/debugging-your-first-java-application.html) on how to debug java programs.

## Notes

### System Partition

The Azure implementation of OSDU has a special partition called the system partition. This partition allows entitlements to be used to provide access to system artifacts, initially system schemas. This partition has no partition-specific infrastructure.

The partition name "system" is reserved for the system partition and should NOT be used for a regular partition.

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
