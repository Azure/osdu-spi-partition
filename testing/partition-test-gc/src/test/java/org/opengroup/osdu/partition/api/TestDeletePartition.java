/*
  Copyright 2002-2022 Google LLC
  Copyright 2002-2022 EPAM Systems, Inc

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.opengroup.osdu.partition.api;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.partition.util.GCPTestUtils;

@Slf4j
public class TestDeletePartition extends DeletePartitionTest {

  @Override
  @Before
  public void setup() throws Exception {
    this.testUtils = new GCPTestUtils();
  }

  @Override
  @After
  public void tearDown() throws Exception {
    deleteResource();
    this.testUtils = null;
  }

  // Test depends on an infrastructure level.
  @Override
  @Test
  public void should_return401_when_makingHttpRequestWithoutToken() throws Exception {
    CloseableHttpResponse response = descriptor.run(getId(), "");
    log.info(
        "Test should_return401_when_makingHttpRequestWithoutToken has a response code = {}."
            + "This test depends on an infrastructure level.",
        response.getCode());
  }

  // Test depends on an infrastructure level.
  @Override
  @Test
  public void should_return401_when_accessingWithCredentialsWithoutPermission() throws Exception {
    CloseableHttpResponse response = descriptor.run(getId(), testUtils.getNoAccessToken());
    log.info(
        "Test should_return401_when_accessingWithCredentialsWithoutPermission has a response code = {}."
            + "This test depends on an infrastructure level.",
        response.getCode());
  }

  // Test depends on an infrastructure level.
  @Override
  @Test
  public void should_return401_when_noAccessToken() throws Exception {
    CloseableHttpResponse response = descriptor.runOnCustomerTenant(getId(), testUtils.getNoAccessToken());
    log.info(
        "Test should_return401_when_noAccessToken has a response code = {}."
            + "This test depends on an infrastructure level.",
        response.getCode());
  }
}
