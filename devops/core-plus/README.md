# OSDU Core Partition Deployment

## Overview

This directory deploys two components via Helm:

| Component | Purpose | Type | Lifecycle |
|-----------|---------|------|-----------|
| **Partition Service** | REST API for partition management | Java app | Always running |
| **Bootstrap** | Initializes partition properties | Bash scripts | Runs once on startup, then idles |

## Quick Start

```bash
cd deploy/
helm install core-partition-deploy .
```

See [Deploy README](./deploy/README.md) for full installation guide.

---

## Component Details

### Bootstrap

**What it does:**
- Calls Partition API to create/update partition config
- Sets storage, database, messaging, and feature flags
- Runs once on startup, then marks ready via `/tmp/bootstrap_ready` and remains idle

> **⚠️ DEPRECATION NOTICE:** MinIO configuration will be deprecated in the next release and replaced with SeaweedFS. Please plan your migration accordingly.

**Key Files:**
- Deployment: `deploy/templates/deploy-bootstrap.yaml`
- ConfigMap: `deploy/templates/configmap-bootstrap.yaml` (extensive config)
- Scripts: `bootstrap/bootstrap_partition.sh`

**Configuration (values.yaml):**
```yaml
data:
  bootstrapImage: ""           # Bootstrap image
  bucketPrefix: "refi"
  minioExternalEndpoint: ""

  # Feature flags (used by bootstrap)
  indexerAugmenterEnabled: "true"
  asIngestedCoordinatesEnabled: "true"
  keywordLowerEnabled: "true"
  bagOfWordsEnabled: "true"
  collaborationsEnabled: "true"
  autocompleteEnabled: "false"
  policyServiceEnabled: "false"
  edsEnabled: "false"
```

**Full details:** [Bootstrap README](./bootstrap/README.md)

---

### Partition Service (Non-Bootstrap)

**What it does:**
- Provides partition CRUD API endpoints
- Runs continuously as a service
- Connects to PostgreSQL for metadata

**Key Files:**
- Deployment: `deploy/templates/deploy.yaml`
- ConfigMap: `deploy/templates/configmap.yaml` (LOG_LEVEL only)
- Service: `deploy/templates/service.yaml`
- Virtual Service: `deploy/templates/virtual-service.yaml`

**Configuration (values.yaml):**
```yaml
data:
  image: ""                    # Service image
  logLevel: "ERROR"            # Application logging
  requestsCpu: "5m"
  requestsMemory: "350Mi"

conf:
  appName: "partition"
  replicas: 1
```

**Health Endpoints:**
- `/health/liveness` (port 8081)
- `/health/readiness` (port 8081)

---

## Configuration Summary

| Setting | Affects | Used In |
|---------|---------|---------|
| `data.image` | Partition Service | deploy.yaml |
| `data.bootstrapImage` | Bootstrap | deploy-bootstrap.yaml |
| `data.logLevel` | Partition Service | configmap.yaml |
| `data.bucket*`, `data.minio*` **(deprecated)** | Bootstrap | configmap-bootstrap.yaml |
| `data.*Enabled` (feature flags) | Bootstrap | configmap-bootstrap.yaml |
| `istio.proxyCPU*` | Partition Service | deploy.yaml |
| `istio.bootstrapProxyCPU*` | Bootstrap | deploy-bootstrap.yaml |

---

## Troubleshooting

**Bootstrap issues:**
```bash
# Check bootstrap logs
kubectl logs -l app=partition-bootstrap -f

# Verify completion
kubectl exec deployment/partition-bootstrap -- cat /tmp/bootstrap_ready
```

**Service issues:**
```bash
# Check service logs
kubectl logs -l app=partition -f

# Check health
kubectl exec deployment/partition -- curl localhost:8081/health/readiness
```

---

## Documentation

- **[Bootstrap Details](./bootstrap/README.md)** - Scripts and environment variables
- **[Helm Guide](./deploy/README.md)** - Installation and configuration
- **[Testing](./test/)** - Validation scripts
