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

package org.opengroup.osdu.partition.util;

import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class VersionInfoUtils {

  public VersionInfo getVersionInfoFromResponse(ClientResponse response) {
    assertTrue(response.getType().toString().contains("application/json"));
    String json = response.getEntity(String.class);
    Gson gson = new Gson();
    return gson.fromJson(json, VersionInfo.class);
  }

  public VersionInfo getVersionInfoFromResponse(CloseableHttpResponse response) {
    String json = "";
    try {
      assertTrue(response.getHeader("Content-Type").getValue().contains("application/json"));
      json = EntityUtils.toString(response.getEntity());
    } catch (ProtocolException | IOException e) {
      throw new RuntimeException(e);
    }
    Gson gson = new Gson();
    return gson.fromJson(json, VersionInfo.class);
  }

  public class VersionInfo {

    public String groupId;
    public String artifactId;
    public String version;
    public String buildTime;
    public String branch;
    public String commitId;
    public String commitMessage;
  }
}
