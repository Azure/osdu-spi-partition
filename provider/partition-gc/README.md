# Partition Service

partition-gc is a [Spring Boot](https://spring.io/projects/spring-boot) service that is responsible for creating and retrieving partition specific properties on behalf of other services whether they are secret values or not.

## Features of implementation

This is a universal solution created using EPAM OSM mapper technology. It allows you to work with various
implementations of KV stores.

## Limitations of the current version

In the current version, the mappers have been equipped with several drivers to the stores:

OSM (mapper for KV-data): Google Datastore; Postgres

## Extensibility

To use any other store or message broker, implement a driver for it. With an extensible set of drivers, the solution is
unrestrictedly universal and portable without modification to the main code.
Mappers support "multitenancy" with flexibility in how it is implemented. They switch between datasources of different
tenants due to the work of a bunch of classes that implement the following interfaces:

* Destination - takes a description of the current context, e.g., "data-partition-id = opendes";
* DestinationResolver – accepts Destination, finds the resource, connects, and returns Resolution;
* DestinationResolution – contains a ready-made connection, the mapper uses it to get the data.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

Pre-requisites

* GCloud SDK with java (latest version)
* JDK 17
* Lombok 1.18 or later
* [Maven 3.8.0+](https://maven.apache.org/download.cgi)

### Google Cloud Service Configuration

[Google Cloud service configuration](docs/gc/README.md)

### Run Locally

Check that maven is installed:

```bash
$ mvn --version
Apache Maven 3.8.0
Maven home: /usr/share/maven
Java version: 17.0.7
...
```

You may need to configure access to the remote maven repository that holds the OSDU dependencies. This file should live within `~/.mvn/community-maven.settings.xml`:

```bash
$ cat ~/.m2/settings.xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>community-maven-via-private-token</id>
            <!-- Treat this auth token like a password. Do not share it with anyone, including Microsoft support. -->
            <!-- The generated token expires on or before 11/14/2019 -->
             <configuration>
              <httpHeaders>
                  <property>
                      <name>Private-Token</name>
                      <value>${env.COMMUNITY_MAVEN_TOKEN}</value>
                  </property>
              </httpHeaders>
             </configuration>
        </server>
    </servers>
</settings>
```

* Update the Google cloud SDK to the latest version:

```bash
gcloud components update
```

* Set Google Project Id:

```bash
gcloud config set project <YOUR-PROJECT-ID>
```

* Perform a basic authentication in the selected project:

```bash
gcloud auth application-default login
```

* Navigate to partition service's root folder and run:

```bash
mvn clean install   
```

* If you wish to see the coverage report then go to target/site/jacoco/index.html and open index.html

* If you wish to build the project without running tests

```bash
mvn clean install -DskipTests
```

After configuring your environment as specified above, you can follow these steps to build and run the application. These steps should be invoked from the *repository root.*

```bash
cd provider/partition-gc/ && mvn spring-boot:run
```

## Testing

Navigate to partition service's root folder and run all the tests:

```bash
# build + install integration test core
$ (cd testing/partition-test-core/ && mvn clean install)
```

### Running E2E Tests

This section describes how to run cloud OSDU E2E tests.


### Google Cloud test configuration

[Google Cloud service configuration](docs/gc/README.md)

## Deployment

Partition Service is compatible with App Engine Flexible Environment, Cloud Run, K8s.

* To deploy into Cloud run, please, use this documentation:
  <https://cloud.google.com/run/docs/quickstarts/build-and-deploy>

* To deploy into App Engine, please, use this documentation:
  <https://cloud.google.com/appengine/docs/flexible/java/quickstart>

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
