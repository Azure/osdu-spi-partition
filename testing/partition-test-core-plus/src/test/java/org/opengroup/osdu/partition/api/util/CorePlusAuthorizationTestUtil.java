/*
 * Copyright 2020-2022 Google LLC
 * Copyright 2020-2022 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.partition.api.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.opengroup.osdu.partition.util.RestDescriptor;
import org.opengroup.osdu.partition.util.TestUtils;

@Slf4j
public class CorePlusAuthorizationTestUtil {

  private RestDescriptor descriptor;
  private TestUtils testUtils;

  public CorePlusAuthorizationTestUtil(RestDescriptor descriptor, TestUtils testUtils) {
    this.descriptor = descriptor;
    this.testUtils = testUtils;
  }

  // Test depends on an infrastructure level.
  public void should_return401or403_when_noAccessToken(String partitionId) throws Exception {
    CloseableHttpResponse response = descriptor.runOnCustomerTenant(partitionId, testUtils.getNoAccessToken());
    log.info(
        "Test should_return401or403_when_noAccessToken has a response code = {}."
            + "This test depends on an infrastructure level.",
        response.getCode());
  }

  // Test depends on an infrastructure level.
  public void should_return401or403_when_accessingWithCredentialsWithoutPermission(
      String partitionId) throws Exception {
    CloseableHttpResponse response = descriptor.run(partitionId, testUtils.getNoAccessToken());
    log.info(
        "Test should_return401or403_when_accessingWithCredentialsWithoutPermission has a response code = {}."
            + "This test depends on an infrastructure level.",
        response.getCode());
  }

  // Test depends on an infrastructure level.
  public void should_return401or403_when_makingHttpRequestWithoutToken(String partitionId)
      throws Exception {
    CloseableHttpResponse response = descriptor.run(partitionId, "");
    log.info(
        "Test should_return401or403_when_makingHttpRequestWithoutToken has a response code = {}."
            + "This test depends on an infrastructure level.",
        response.getCode());
  }

  protected String error(String body) {
    return String.format(
        "%s: %s %s %s",
        descriptor.getHttpMethod(), descriptor.getPath(), descriptor.getQuery(), body);
  }
}
