# OSDU Partition Service for Azure

[![Release](https://img.shields.io/github/v/release/Azure/osdu-spi-partition)](https://github.com/Azure/osdu-spi-partition/releases)
[![Validate](https://github.com/Azure/osdu-spi-partition/actions/workflows/validate.yml/badge.svg?branch=main)](https://github.com/Azure/osdu-spi-partition/actions/workflows/validate.yml)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

> [!NOTE]
> Shared service code comes from the [OSDU community upstream](https://community.opengroup.org/osdu/platform/system/partition); the [OSDU documentation](https://osdu.pages.opengroup.org/platform/system/partition/) covers the API.

Partition keeps the registry of data partitions and each partition's properties, which other services look up at request time to find that partition's Azure resources.

## At a glance

| | |
|---|---|
| API base path | `/api/partition/v1/` |
| Swagger UI | `/api/partition/v1/swagger` |
| Health | `:8081/actuator/health` |
| Depends on | None |
| Azure resources | Table Storage (partition registry and properties), Key Vault, Redis |
| Deployed by | [OSDU SPI Stack](https://github.com/Azure/osdu-spi-stack) (`software/stacks/osdu/services/partition.yaml`) |

## Repository layout

[CONTRIBUTING.md](CONTRIBUTING.md) explains where each kind of change belongs.

| Path | Owner | Contents |
|---|---|---|
| `partition-core/` | OSDU upstream | Shared service code |
| `provider/partition-azure/` | This repository | Azure provider |
| `partition-acceptance-test/` | OSDU upstream | End-to-end suite run against a deployed environment |
| `testing/partition-test-azure/` | This repository | Azure integration tests |
| `.spi/service.yaml` | This repository | How CI deploys and tests the service on SPI Stack |

## Build

Requires Java 17 and Maven 3.8+. OSDU dependencies resolve from the public community registry through the settings file in `.mvn`:

```bash
mvn --settings .mvn/community-maven.settings.xml -P core,azure clean install
```

The runnable jar lands at `provider/partition-azure/target/partition-azure-*-spring-boot.jar`.

## Configuration

SPI Stack sets the service's environment from two places: the shared `osdu-config` ConfigMap and the service's own entry in [`services/partition.yaml`](https://github.com/Azure/osdu-spi-stack/blob/main/software/stacks/osdu/services/partition.yaml). Those files are the contract; the tables below list what Partition actually reads from them.

**Shared, from `osdu-config`:**

| Variable | Purpose |
|---|---|
| `AZURE_TENANT_ID` | Entra tenant |
| `AAD_CLIENT_ID` | Application ID that caller tokens are issued for |
| `KEYVAULT_URI` | Central Key Vault |
| `SERVER_PORT` | HTTP port (`8080`) |
| `APPINSIGHTS_KEY` | Telemetry |

**Specific to Partition**, from `services/partition.yaml`:

| Variable | Value on SPI Stack | Purpose |
|---|---|---|
| `SERVER_SERVLET_CONTEXTPATH` | `/api/partition/v1/` | API base path |
| `AZURE_PAAS_WORKLOADIDENTITY_ISENABLED` | `true` | Authenticate to Azure with workload identity |
| `AZURE_ISTIOAUTH_ENABLED` | `true` | Trust the mesh's token validation |
| `REDIS_DATABASE` | `1` | Redis database index reserved for Partition |
| `PARTITION_SPRING_LOGGING_LEVEL` | `DEBUG` | Log level for Spring web |

The service authenticates to Azure with workload identity, which injects `AZURE_CLIENT_ID` and a federated token; there are no client secrets. Partition is the one service that does not resolve its storage through the Partition API: it reads the Table Storage endpoint from the Key Vault secret `tbl-storage-endpoint`. The Redis host comes from the Key Vault secret `redis-hostname`, over TLS on port `6380`.

## Test

| Suite | Where | Runs in CI | Run it yourself |
|---|---|---|---|
| Unit | `partition-core`, `provider/partition-azure` | Pull requests (Java Build) | `mvn ... install` from [Build](#build) |
| Acceptance | [`partition-acceptance-test`](partition-acceptance-test/README.md) | Pull requests, against SPI Stack (Deploy and Test) | `spi test partition` |
| Integration | `testing/partition-test-azure` | Pull requests, against SPI Stack (Deploy and Test) | `spi test partition --suite integration` |

CI runs these on pull requests from this repository that change code. Documentation-only changes skip the build, and pull requests from forks build without deploying.

**Acceptance** calls the deployed service through the gateway as a privileged test identity. **Integration** is the Azure suite; it also exercises authorization, calling as both a privileged identity and one with no data access. Both run in the lane, and the bindings in `.spi/service.yaml` supply each suite's host, partition, and tokens. Against an environment you are connected to:

```bash
spi test partition --suite all          # the image and suites the environment is running
spi test partition --suite all --source .   # this checkout's suites and descriptor
```

To call the API by hand, `spi token` mints a bearer token:

```bash
curl -H "Authorization: Bearer $(spi token)" -H "data-partition-id: <partition>" \
  https://<gateway>/api/partition/v1/partitions
```

## Deploy

For a pull request from this repository that changes code, CI publishes the service image and its test suite image, `osdu-spi-partition-acceptance`, to GHCR, and the Deploy and Test lane borrows an SPI Stack environment, runs the new image there, proves it with the declared suites, and restores the environment's own image. When that lane runs and passes, the change is proven on real infrastructure before it merges; the Validation Summary on the pull request shows whether it ran. This repository does not own infrastructure; SPI Stack does.

To try a build by hand on an environment you are connected to, pin it by digest and release the pin when done:

```bash
spi service pin partition --image ghcr.io/azure/osdu-spi-partition@sha256:<digest>
spi service reset partition
```

## Service notes

**System partition.** The Azure implementation reserves a partition named `system`. It lets entitlements govern access to system artifacts, initially the system schemas, and it has no partition-specific infrastructure. Do not create a regular partition with that name; `reserved_partition_name` sets it and defaults to `system`.

## License

Copyright © Microsoft Corporation

Licensed under the [Apache License 2.0](LICENSE).
