
# Partition service integration tests 

Partition integration tests are refactored so that the business logic for integration tests resides in the `partition-test-core` module and provider specific logic and execution steps reside in provider module (e.g. `partition-test-azure`). To run the integration tests, the core module is built first and then the provider module is executed. Please read further to know more details.

### Dependencies needed to run the integration tests 
* JDK8
* Maven
* Values for the following environment variables in Config.java
  
  ```
   ENVIRONMENT ('local' for local testing or 'dev' for dev testing) 
   PARTITION_BASE_URL(service base URL )
   CLIENT_TENANT (common tenant)
   MY_TENANT (OSDU Tenant)
   OAUTH2 credentials (User, Password etc)
   ```

  Above variables should be configured in the release pipeline to run integration tests. You should also replace them with proper values if you wish to run tests locally.

### Commands to run tests
* Integration tests are refactored into two pieces: Core and Provider. Core contains business logic for tests and is a dependency for executing the tests from provider module. To build the core module, simply navigate to `partition-test-core` directory and run `mvn clean install`. This will build the core module
* Next, to execute the integration tests, navigate to the provider module and execute `mvn test`
