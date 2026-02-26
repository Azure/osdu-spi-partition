Copyright 2017-2020, Schlumberger

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

# Partition service integration tests

Partition integration tests are refactored so that the business logic for integration tests resides in the `partition-test-core` module and provider specific logic and execution steps reside in provider module (e.g. `partition-test-azure`). To run the integration tests, the core module is built first and then the provider module is executed. Please read further to know more details.

### Dependencies needed to run the integration tests 
* JDK8
* Maven
* Azure Devops access to slb-des-ext-collaboration organization. You need to generate a PAT that can access dependencies held in the Azure artifacts
* Values for the following environment variables in Config.java
  
  ```
   ENVIRONMENT ('local' for local testing or 'dev' for dev testing) 
   PARTITION_BASE_URL(service base URL )
   INTEGRATION_TESTER (service account key which has full api access)
   NO_DATA_ACCESS_TESTER (service account key which has not api access)
   TESTER_SERVICEPRINCIPAL_SECRET (service principal secret)
   NO_DATA_ACCESS_TESTER_SERVICEPRINCIPAL_SECRET (service principal secret for no access)
   AZURE_AD_TENANT_ID (tenant id)
   AZURE_AD_APP_RESOURCE_ID (App resource Id) 
   AZURE_AD_OTHER_APP_RESOURCE_ID (resourse Id used for testing with no access)
   CLIENT_TENANT (common tenant)
   MY_TENANT (OSDU Tenant)
   ```

  Above variables should be configured in the release pipeline to run integration tests. You should also replace them with proper values if you wish to run tests locally.

### Commands to run tests
* Integration tests are refactored into two pieces: Core and Provider. Core contains business logic for tests and is a dependency for executing the tests from provider module. To build the core module, simply navigate to `partition-test-core` directory and run `mvn clean install`. This will build the core module
* Next, to execute the integration tests, navigate to the provider module and execute `mvn test`
