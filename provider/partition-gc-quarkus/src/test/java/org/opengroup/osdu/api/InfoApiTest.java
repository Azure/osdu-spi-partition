/*
 *  Copyright 2020-2025 Google LLC
 *  Copyright 2020-2025 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
class InfoApiTest {
  private static final String INFO_ENDPOINT = "/info";
  private static final String FIELD_GROUP_ID = "groupId";
  private static final String FIELD_ARTIFACT_ID = "artifactId";
  private static final String FIELD_VERSION = "version";
  private static final String FIELD_BUILD_TIME = "buildTime";
  private static final String FIELD_BRANCH = "branch";
  private static final String FIELD_COMMIT_ID = "commitId";
  private static final String FIELD_COMMIT_MESSAGE = "commitMessage";

  @Test
  void should_returnVersionInfo_when_getInfo() {
    given()
        .when()
        .get(INFO_ENDPOINT)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body(FIELD_GROUP_ID, notNullValue())
        .body(FIELD_ARTIFACT_ID, notNullValue())
        .body(FIELD_VERSION, notNullValue())
        .body(FIELD_BUILD_TIME, notNullValue())
        .body(FIELD_BRANCH, notNullValue())
        .body(FIELD_COMMIT_ID, notNullValue())
        .body(FIELD_COMMIT_MESSAGE, notNullValue());
  }
}
