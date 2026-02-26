## Partition Service

## Table of Contents <a name="TOC"></a>
* [Introduction](#introduction)
* [Checking Service Health](#checking-service-health)
* [Partition API access](#partition-api-access)
* [APIs](#apis)
    * [Get partition details](#get-partition)
    * [Create a new partition](#create-partition)
    * [Update an existing partition](#update-partition)
    * [Delete an existing partition](#delete-partition)
    * [List of partitions](#list-partition)
    

## Introduction <a name="introduction"></a>
Partition service is responsible for creating and retrieving the partition specific properties (secret and non-secret) on behalf of other services.

## Health Check <a name="checking-service-health"></a>
An endpoint to check if service is up and running.
```
GET api/partition/v1/_ah/liveness_check
```
<details><summary>curl</summary>

```
curl --request GET \
  --url 'https://<base_url>/api/partition/v1/_ah/liveness_check'
```
</details>

## Partition API access <a name="partition-api-access"></a>
As Partition service APIs are mostly consumed by other services, API access is limited to service accounts only.

## APIs <a name="apis"></a>
### Get partition details<a name="get-partition"></a>
Consuming services can use this API to get details of a partition. Partition details consists of a set of key-value pairs of properties.

```
GET api/partition/v1/partitions/{partitionId}
```
<details><summary>curl</summary>

```
curl --request GET \
  --url 'https://<base_url>/api/partition/v1/partitions/osdu' \
  --header 'Authorization: Bearer <JWT>' \
  --header 'Content-Type: application/json'
```
</details>

A sample output is shown below.
<details><summary>Sample response</summary>

```
{
    "projectId": {
        "sensitive": false,
        "value": "osdu"
    },
    "serviceAccount": {
        "sensitive": false,
        "value": ".iam.gserviceaccount.com"
    },
    "complianceRuleSet": {
        "sensitive": false,
        "value": "shared"
    },
    "dataPartitionId": {
        "sensitive": false,
        "value": "osdu"
    },
    "name": {
        "sensitive": false,
        "value": "osdu"
    },
    "policy-service-enabled": {
        "sensitive": false,
        "value": "false"
    },
    "bucket": {
        "sensitive": false,
        "value": "bucketName"
    },
    "crmAccountID": {
        "sensitive": false,
        "value": ["osdu","osdu"]
    }
}
```

</details>

[Back to Table of Contents](#TOC)

### List partitions <a name="list-partition"></a>
Consuming services can use this API to list all partitions Id.  
```
GET api/partition/v1/partitions
```
<details><summary>curl</summary>

```
curl --request GET \
  --url 'https://<base_url>/api/partition/v1/partitions' \
  --header 'Authorization: Bearer <JWT>' \
  --header 'Content-Type: application/json'
```
</details>

A sample output is shown below.
<details><summary>Sample response</summary>

```
[
    "default-dev",
    "opendes",
    "osdu",
    "mypartition"
]
```

</details>

[Back to Table of Contents](#TOC)
