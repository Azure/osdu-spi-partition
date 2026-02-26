package org.opengroup.osdu.partition.api;

import org.junit.After;
import org.junit.Before;
import org.opengroup.osdu.partition.util.AzureTestUtils;

public class TestSwaggerApi extends SwaggerApiTest {

  @Before
  @Override
  public void setup() {
    this.testUtils = new AzureTestUtils();
  }

  @After
  @Override
  public void tearDown() {
    this.testUtils = null;
  }
}
