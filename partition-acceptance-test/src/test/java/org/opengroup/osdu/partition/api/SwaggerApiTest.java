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

import static org.junit.Assert.assertEquals;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.partition.util.TestBase;
import org.opengroup.osdu.partition.util.TestUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

public final class SwaggerApiTest extends TestBase {

  private static final String CONTEXT_PATH = "api/partition/v1/";
  private static final String SWAGGER_API_PATH = "swagger";
  private static final String SWAGGER_API_DOCS_PATH = "api-docs";

  @Override
  @Before
  public void setup() throws Exception {
  }

  @Override
  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void shouldReturn200_whenSwaggerApiIsCalled() throws Exception {
    CloseableHttpResponse response = TestUtils.send(CONTEXT_PATH + SWAGGER_API_PATH, HttpMethod.GET.name(),false);
    assertEquals(HttpStatus.OK.value(), response.getCode());
  }

  @Test
  public void shouldReturn200_whenSwaggerApiDocsIsCalled() throws Exception {
    CloseableHttpResponse response = TestUtils.send(CONTEXT_PATH + SWAGGER_API_DOCS_PATH, HttpMethod.GET.name(),false);
    assertEquals(HttpStatus.OK.value(), response.getCode());
  }
}
