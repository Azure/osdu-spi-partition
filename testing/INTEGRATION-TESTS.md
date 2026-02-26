#Integration tests
The integration tests are a separate project in the same repo. This allows you to run the partition service locally and then run the integration tests against them.

This implementation offers a highly opinionated option. 

For each API method you create a new RestDesciptor and TestTemplate. The RestDescriptior describes how to call your API and you then choose a TestTemplate to inherit from which has the test suite for your descriptor.

The TestTemplates have common tests needed for common scenarios e.g. Get by Id, Delete, Create as well as the BaseTestTemplate that has common tests for all scenarios.

These classes offer the standard set of tests expected by our APIs. Overriding these classes and using descriptors means you automatically get these tests for free. As a minimum you need a Create and Delete API for any resource. This enables setup and cleanup to happen after the test is run.

You could override these classes and they will offer all the tests your API needs.

This proves your API is functioning correctly and then the unit tests provide the breadth of testing needed for different scenarios.

The BaseTest classes are 

    BaseTestTemplate.java - offers test that every API uses including access pattern tests, HTTPS enforcement test, Options method test, 20X response on a valid call etc.
    

There are obviously other API types that are not included here e.g. list, query etc. These can be created directly of the BaseTest class or you can provide test templates for these API to be added.

Using these directly or just as documentation for your own tests means that your APIs are consistent with our standards and the other APIs in the Ecosystem.
