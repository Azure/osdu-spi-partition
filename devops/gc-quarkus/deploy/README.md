<!--- Deploy -->

# Deploy helm chart for Partition Quarkus

## Introduction

This chart bootstraps a deployment of the Quarkus-based Partition service on a [Kubernetes](https://kubernetes.io) cluster using [Helm](https://helm.sh) package manager.

The Quarkus implementation offers better startup times, lower memory consumption, and uses JSON files mounted from a ConfigMap for partition configuration.

## Prerequisites

The code was tested on **Kubernetes cluster** (v1.21.11) with **Istio** (1.12.6)
  > Istio is installed with Istio Ingress Gateway

- Kubernetes cluster version can be checked with the command:

    `kubectl version --short | grep Server`

    The output will be similar to the following:

  ```console
  Server Version: v1.21.11-gke.1100
  ```

- Istio version can be checked in different ways, it is out of scope for this README. You can find more information [here](https://istio.io/latest/docs/setup/install/).

    The following command shows how to check version if Anthos Service Mesh is used:

    `kubectl -n istio-system get pods -lapp=istiod -o=jsonpath='{.items[0].metadata.labels.istio\.io/rev}'`

    The output will be similar to the following:

  ```console
  asm-1132-5
  ```

> It is possible to use other versions, but it hasn't been tested

This example describes installation in **Development mode**:

- In this mode helm chart is installed to the namespace **not labeled with Istio**.
  > More information about labeling can be found [here](https://istio.io/latest/docs/setup/additional-setup/sidecar-injection) (Istio) or [here](https://cloud.google.com/service-mesh/docs/managed/select-a-release-channel#default-injection-labels) (Anthos Service Mesh)

    You can find all labels for your namespace with the command:

     `kubectl get namespace <namespace> -o jsonpath={.metadata.labels}`

    The output shows that there are no any labels related to Istio:

    ```console
    {"kubernetes.io/metadata.name":"default"}
    ```

    When the namespace is labeled with Istio, the output could be:

    ```console
    {"istio-injection":"enabled","kubernetes.io/metadata.name":"default"}
    ```

### Operation system

The code works in Debian-based Linux (Debian 10 and Ubuntu 20.04) and Windows WSL 2. Also, it works but is not guaranteed in Google Cloud Shell. All other operating systems, including macOS, are not verified and supported.

### Packages

Packages are only needed for installation from a local computer.

- **HELM** (version: v3.7.1 or higher) [helm](https://helm.sh/docs/intro/install/)

    Helm version can be checked with the command:

    `helm version --short`

    The output will be similar to the following:

  ```console
  v3.7.1+gd141386
  ```

- **Kubectl** (version: v1.21.0 or higher) [kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl)

    Kubectl version can be checked with the command:

    `kubectl version --short | grep Client`

    The output will be similar to the following:

  ```console
  Client Version: v1.21.0
  ```

## Installation

First you need to set variables in **values.yaml** file using any code editor. Some of the values are prefilled, but you need to specify some values as well. You can find more information about them below.

### Global variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**global.domain** | your domain | string | - | yes
**global.limitsEnabled** | whether CPU and memory limits are enabled | boolean | `true` | yes
**global.dataPartitionId** | data partition id | string | - | yes
**global.logLevel** | severity of logging level | string | `ERROR` | yes
**global.tier** | Only PROD must be used to enable autoscaling | string | "" | no
**global.autoscaling** | enables horizontal pod autoscaling, when tier=PROD | boolean | true | yes

### Configmap variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**data.logLevel** | logging severity level for this service only  | string | - | yes, only if differs from the `global.logLevel`
**data.projectId** | your Google Cloud project id | string | - | yes

### System Partition variables

| Name | Description | Type | Default |Required |
| ---- | ----------- | ---- | ------- | ------- |
**data.tenantServiceAccount** | partition admin service account | string | - | yes
**data.databaseId** | datastore database id | string | - | yes
**data.policyServiceEnabled** | indicates that Policy service is available in an OSDU installation | bool | true | yes
**data.edsEnabled** | indicates that EDS services are available in an OSDU installation | bool | false | yes
**data.autoCompleteEnabled** | enables autoComplete function | bool | true | yes
**data.asIngestedCoordinatesEnabled** | enables asIngestedCoordinates function | bool | true | yes
**data.keyWordLowerEnabled** | enables keyWordLower function | bool | true | yes
**data.bagOfWordsEnabled** | enables bagOfWords function | bool | true | yes
**data.xCollaborationEnabled** | enables xCollaboration function | bool | false | yes
**data.customSystemPartitionProperties** | user defined properties for configuring partition | list of objects | no

### Deployment variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**data.requestsCpu** | amount of requests CPU | string | `5m` | yes
**data.requestsMemory** | amount of requests memory | string | `350Mi` | yes
**data.limitsCpu** | CPU limit | string | `500m` | only if `global.limitsEnabled` is true
**data.limitsMemory** | memory limit | string | `1G` | only if `global.limitsEnabled` is true
**data.serviceAccountName** | name of your service account | string | `partition` | yes
**data.image** | path to the image in a registry | string | - | yes
**data.imagePullPolicy** | when to pull the image | string | `IfNotPresent` | yes
**data.systemPartitionConfigPath** | mount path for system partition json | string | `/mnt/system-partition` | yes
**data.dataPartitionsConfigPath** | mount path for data partitions json | string | `/mnt/data-partitions` | yes

### Configuration variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**conf.appName** | name of the app | string | `partition-quarkus` | yes
**conf.configmap** | configmap to be used | string | `partition-quarkus-config` | yes
**conf.configmapPartitions** | configmap with data partitions configuration | string | `quarkus-partitions` | yes
**conf.replicas** | number of replicas | integer | `1` | yes

### ISTIO variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**istio.proxyCPU** | CPU request for Envoy sidecars | string | `5m` | yes
**istio.proxyCPULimit** | CPU limit for Envoy sidecars | string | `500m` | yes
**istio.proxyMemory** | memory request for Envoy sidecars | string | `64Mi` | yes
**istio.proxyMemoryLimit** | memory limit for Envoy sidecars | string | `512Mi` | yes
**istio.sidecarInject** | whether Istio sidecar will be injected. Setting to `false` reduces security, because disables authorization policy. | boolean | `true` | yes

## Partition JSON Configuration

The Quarkus implementation reads partition configurations from JSON files mounted as a ConfigMap. The default configuration includes an "osdu.json" file with the following structure:

```json
{
  "properties": {
    "projectId": {
      "sensitive": false,
      "value": "your-project-id"
    },
    "dataPartitionId": {
      "sensitive": false,
      "value": "partition-id"
    },
    "featureFlag.eds.enabled": {
      "sensitive": false,
      "value": "true"
    },
    "featureFlag.policy.enabled": {
      "sensitive": false,
      "value": "true"
    },
    // More properties...
  }
}
```

The partition JSON files have the feature flags hardcoded with appropriate values. You can add more partition files by adding them to the configmap-partitions.yaml template.

## API Endpoints

The Quarkus implementation exposes the following endpoints:

- API Root: `/api/partition-quarkus/v1`
- Health Check: `/liveness` and `/readiness` on port 8081
- API Documentation: `/api/partition-quarkus/v1/swagger` and `/api/partition-quarkus/v1/api-docs`

## Quarkus Native Executable

This deployment uses a native executable built with GraalVM, which provides:

- Significantly faster startup times
- Lower memory consumption
- Improved security through reduced attack surface

### Install the helm chart

Run this command from within this directory:

```console
helm install gc-quarkus-partition-deploy .
```

## Uninstalling the Chart

To uninstall the helm deployment:

```console
helm uninstall gc-quarkus-partition-deploy
```

To delete secrets and PVCs:

```console
kubectl delete secret --all; kubectl delete pvc --all
```

[Move-to-Top](#deploy-helm-chart-for-partition-quarkus)
