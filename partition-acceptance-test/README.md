### Running E2E Tests

You will need to have the following environment variables defined.

| name                 | value                      | description                | sensitive? | source | required |
|----------------------|----------------------------|----------------------------|------------|--------|----------|
| `HOST`               | ex `http://localhost:8080`  | Partition service base URL | no         | the deployment's external URL (gateway/ingress route) for the Partition service | yes      |
| `DATA_PARTITION_ID`  | ex `osdu`                  | Data partition identifier  | no         | deployment configuration: the partition the environment was bootstrapped with | yes      |

Authentication can be provided as OIDC config:

| name                                            | value                                      | description                                | sensitive? | source |
|-------------------------------------------------|--------------------------------------------|--------------------------------------------|------------|--------|
| `PRIVILEGED_USER_OPENID_PROVIDER_CLIENT_ID`     | `********`                                 | Privileged User Client Id                  | yes        | an OIDC client-credentials client provisioned by the deployment for testing |
| `PRIVILEGED_USER_OPENID_PROVIDER_CLIENT_SECRET` | `********`                                 | Privileged User Client secret              | yes        | the same client's secret, from the deployment's secret store |
| `TEST_OPENID_PROVIDER_URL`                      | ex `https://keycloak.com/auth/realms/osdu` | OpenID provider url                        | yes        | the deployment's OIDC issuer URL, the same issuer the services validate tokens against |
| `PRIVILEGED_USER_OPENID_PROVIDER_SCOPE`         | ex `api://my-app/.default`                 | OAuth2 scope (optional, defaults to openid)| no         | identity-provider dependent; omit for the `openid` default (Keycloak), set for e.g. Entra ID |

Or a static bearer token can be supplied directly:

| name                        | value      | description                | sensitive? | source |
|-----------------------------|------------|----------------------------|------------|--------|
| `PRIVILEGED_USER_TOKEN`     | `********` | Privileged User Token      | yes        | a token minted for the same identity, when OIDC client configuration is not available |

Authentication configuration is optional and can be omitted when the service allows unauthenticated access locally.

No specific Entitlements roles are required for the test user. The acceptance tests only exercise read endpoints (`GET /partitions`, `GET /partitions/{id}`, liveness check, swagger) which require authentication but no entitlement group membership.

Environment variables can also be placed in a `.env` file in the working directory or in
`src/test/resources/.env` (working directory takes precedence).

Execute the following command to build code and run all the integration tests:

 ```bash
 # Note: this assumes that the environment variables for integration tests as outlined
 #       above are already exported in your environment.
 $ (cd partition-acceptance-test && mvn clean test)
 ```

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
