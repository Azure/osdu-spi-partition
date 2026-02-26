/*
 * Copyright 2021 Google LLC
 * Copyright 2021 EPAM Systems, Inc
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

package org.opengroup.osdu.partition.api;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.junit.Test;
import org.opengroup.osdu.partition.util.TestUtils;
import org.opengroup.osdu.partition.util.TestBase;
import org.opengroup.osdu.partition.util.VersionInfoUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class GetVersionInfoApiTest extends TestBase {

  protected static final VersionInfoUtils VERSION_INFO_UTILS = new VersionInfoUtils();

  @Test
  public void should_returnInfo() throws Exception {
    CloseableHttpResponse response = TestUtils
        .send("api/partition/v1/info", HttpMethod.GET.name(), this.testUtils.getAccessToken(), "",
            "", false);
    assertEquals(HttpStatus.OK.value(), response.getCode());

    VersionInfoUtils.VersionInfo responseObject = VERSION_INFO_UTILS
        .getVersionInfoFromResponse(response);

    assertNotNull(responseObject.groupId);
    assertNotNull(responseObject.artifactId);
    assertNotNull(responseObject.version);
    assertNotNull(responseObject.buildTime);
    assertNotNull(responseObject.branch);
    assertNotNull(responseObject.commitId);
    assertNotNull(responseObject.commitMessage);
  }
}
