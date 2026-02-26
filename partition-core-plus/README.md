## Running the Partition Core Plus locally
1. Run the basic postgres docker container with default values:

`docker run --name basic-postgres --rm -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=4y7sV96vA9wv46VR -e PGDATA=/var/lib/postgresql/data/pgdata -v /tmp:/var/lib/postgresql/data -p 5432:5432 -it postgres:14.1-alpine`

It will pull the image if not present locally, and will run it with local mounted tmp  folder. It retains the tables created, you can choose a folder of your choice.

`After the container is running and waiting for connections, below should be seen on console:
2023-09-28 11:31:46.372 UTC [1] LOG:  listening on IPv6 address "::", port 5432
2023-09-28 11:31:46.382 UTC [1] LOG:  listening on Unix socket "/var/run/postgresql/.s.PGSQL.5432"
2023-09-28 11:31:46.384 UTC [21] LOG:  database system was shut down at 2023-09-28 11:31:42 UTC
2023-09-28 11:31:46.386 UTC [1] LOG:  database system is ready to accept connections`

2. Table creations: After postgres container is running, one can log in to this container, and create the tables required by the partition service like below:

* Connect to the postgres container in shell `docker exec -it basic-postgres /bin/sh`

* After that, login to postgres cli: `psql --username postgres`

* To list existing tables, u can run `\l` command(it is letter l, and not number 1).

* Create the below table required for running the partition service, you can change these names and then change the env vars as well injected into the Partition Service docker container in the next step.

```CREATE USER usr_partition_pg WITH PASSWORD 'partition_pg';
GRANT usr_partition_pg TO postgres;
CREATE DATABASE partition_db OWNER usr_partition_pg;

\c "partition_db";

CREATE SCHEMA IF NOT EXISTS partition AUTHORIZATION usr_partition_pg;

CREATE TABLE IF NOT EXISTS partition."PartitionProperty"(
id text COLLATE pg_catalog."default" NOT NULL,
pk bigint NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
data jsonb NOT NULL,
CONSTRAINT PartitionProperty UNIQUE (id)
);

ALTER TABLE partition."PartitionProperty" OWNER to usr_partition_pg;

CREATE INDEX IF NOT EXISTS PartitionProperty_datagin ON partition."PartitionProperty" USING GIN (data);
```

3. After the required tables are created, launch the partition service image locally with below command:

`docker run --name partiton_service --env ./env-partition-core-plus.txt -p 8080:8080 community.opengroup.org:5555/osdu/platform/system/partition/partition-core-plus-dk-az-community-ref-impl:3a341a939fe74889c3b99a5271e2977d84925c74`

* For this command to work, create a file with below data in the same folder from where you're executing the above command (variables are in sync with what was created in point number 2):

Must have

| name                            | value                                               | description | sensitive? | source |
|---------------------------------|-----------------------------------------------------|-------------|------------|--------|
| `PARTITION_POSTGRES_URL`        | ex `jdbc:postgresql://172.17.0.1:5432/partition_db` |             |            |        |
| `PARTITION_POSTGRESQL_USERNAME` | ex `usr_partition_pg`                               |             |            |        |
| `PARTITION_POSTGRESQL_PASSWORD` | ex `partition_pg`                                   |             |            |        |
| `dataPartitionId`               | ex `test-partition`                                 |             |            |        |

Defined in default application property file but possible to override:

| name                              | value             | description              | sensitive? | source        |
|-----------------------------------|-------------------|--------------------------|------------|---------------|
| `MANAGEMENT_ENDPOINTS_WEB_BASE`   | ex `/`            | Web base for Actuator    | no         | -             |
| `MANAGEMENT_SERVER_PORT`          | ex `8081`         | Port for Actuator        | no         | -             |

* Now, you can hit the Partition service from Postman to work on partition service locally.
