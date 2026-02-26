# Partition Service

**partition-gc-quarkus** is a [Quarkus](https://quarkus.io/) service that is responsible retrieving partition specific
properties on behalf of other services whether they are secret values or not.

## Features

*
Implements [Partition API](https://community.opengroup.org/osdu/platform/system/partition/-/blob/master/docs/api/partition_openapi.yaml?ref_type=heads)
* Reads partition configurations from a directory specified by the `PARTITION_CONFIGS_PATH` environment variable
  (configs are stored as json files in this directory).
* Automatically reloads the configuration when files in the directory are created, modified, or deleted.
* Utilizes a "debounce" delay (set via the DIRECTORY_WATCH_DEBOUNCE_DELAY_MS environment variable) to prevent redundant
  reloads when multiple changes occur in a short timeframe.
* **Note:** This service has no authentication, so it must not be exposed directly to the public or untrusted networks.

## Limitations

* No authentication or authorization — ensure the service is protected at network or platform level.

## Building and Running

These instructions will get you a copy of the project up and running on your local machine for development and testing
purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

* Maven 3.8.0+
* JDK 17
* Docker (for native image and containerization)
* Linux or macOS (for GraalVM native image; see Quarkus native build guide)

### Steps

1. **Clone the repository:**

```bash
git clone <repository_url>
```

2. **Navigate to the Quarkus Provider Module:**

```bash
cd provider/partition-gc-quarkus/
```

3. **Build the Native Executable:**
   To build a native (GraalVM) executable:

```bash
mvn clean package -Dnative
```

The resulting binary will be located at target/partition-gc-quarkus-*-runner.

4. **Build the Docker Image (Native):**

First, ensure you have GraalVM installed and configured correctly. Then, build the native image:

```bash
mvn package -Pnative
```

```bash
docker build -f src/main/docker/Dockerfile.native -t quarkus/partition .
```

5. **Run the Service (Docker, Native Mode):**

Assuming your partition config files are under /path_to_partitions:

```bash
docker run -i --rm \
 -p 8080:8080 \
 -v /path_to_partitions:/data \
 quarkus/partition \
 -DPARTITION_CONFIGS_PATH=/data
```

## Running Locally (JVM Mode)

For development and testing, you may run the service from your IDE or the command line:

```bash
cd provider/partition-gc-quarkus/
mvn quarkus:dev
```

Or to run the packaged jar:

```bash
cd provider/partition-gc-quarkus/
mvn clean package
java -jar target/quarkus-app/quarkus-run.jar \
-DPARTITION_CONFIGS_PATH=/path_to_partitions
```

Define the following environment variables.

Must have:

| name                     | value         | description                                                    | sensitive? | source |
|--------------------------|---------------|----------------------------------------------------------------|------------|--------|
| `PARTITION_CONFIGS_PATH` | ex `/configs` | Path to the directory containing partition configuration files | false      | -      |

Defined in default application property file but possible to override:

| name                                | value                      | description                                                        | sensitive? | source |
|-------------------------------------|----------------------------|--------------------------------------------------------------------|------------|--------|
| `DIRECTORY_WATCH_DEBOUNCE_DELAY_MS` | ex `300`                   | Debounce interval between directory changes and config (re)loading | no         | -      |
| `MANAGEMENT_ENDPOINTS_WEB_BASE`     | ex `/`                     | Web base for Actuator                                              | no         | -      |
| `MANAGEMENT_SERVER_PORT`            | ex `8081`                  | Port for Actuator                                                  | no         | -      |
| `OTEL_JAVAAGENT_ENABLED`            | ex `true` or `false`       | `true` - OpenTelemetry Java agent enabled, `false` - disabled      | no         |        |
| `OTEL_EXPORTER_OTLP_ENDPOINT`       | ex `http://127.0.0.1:4318` | OpenTelemetry collector endpoint                                   | no         |        |

### Running E2E Tests

Use [partition-acceptance-test](../../partition-acceptance-test)

```bash
# build + install acceptance tests
$ (cd partition-acceptance-test/ && mvn clean install)
```

## Monitoring
### OpenTelemetry Integration

The opentelemetry-javaagent.jar file is the OpenTelemetry Java agent. It is used to
automatically instrument the Java application at runtime, without requiring manual changes
to the source code.

This provides critical observability features:
* Distributed Tracing: To trace the path of requests as they travel across different
  services.
* Metrics: To capture performance indicators and application-level metrics.
* Logs: To correlate logs with traces and other telemetry data.

Enabling this agent makes it significantly easier to monitor, debug, and manage the
application in development and production environments. The agent is activated by the
startup.sh script when the OTEL_JAVAAGENT_ENABLED environment variable is set to true.

The agent is available from the official OpenTelemetry GitHub repository. It is
recommended to use the latest stable version.

Official Download Page:
https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases

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
