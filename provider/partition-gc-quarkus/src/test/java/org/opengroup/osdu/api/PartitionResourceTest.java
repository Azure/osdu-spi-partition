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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opengroup.osdu.util.TestUtils.INVALID_PARTITION;
import static org.opengroup.osdu.util.TestUtils.JSON;
import static org.opengroup.osdu.util.TestUtils.PARTITIONS_ENDPOINT;
import static org.opengroup.osdu.util.TestUtils.TEST_FILES_PATH;
import static org.opengroup.osdu.util.TestUtils.TEST_PARTITION_1;
import static org.opengroup.osdu.util.TestUtils.TEST_PARTITION_2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.util.TestUtils;

@QuarkusTest
class PartitionResourceTest {
  private static final String CODE = "code";
  @Inject private ObjectMapper objectMapper;

  @ConfigProperty(name = "directory-watch.debounce-delay-ms")
  private int debounceDelayMs;

  @ConfigProperty(name = "partitionConfigsPaths")
  private List<String> partitionConfigsPaths;

  @BeforeEach
  void setup() {
    assertThat(
        "Two paths must be set in PARTITION_CONFIGS_PATHS", partitionConfigsPaths.size(), is(2));
    TestUtils.resetTestFilesInDirs(
        Path.of(partitionConfigsPaths.get(0)), Path.of(partitionConfigsPaths.get(1)));
    waitDebounceDelay();
  }

  @Test
  void should_returnPartitionList_when_listEndpointCalled() {
    given()
        .when()
        .get(PARTITIONS_ENDPOINT)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("$", hasItems(TEST_PARTITION_1, TEST_PARTITION_2));
  }

  @Test
  void should_returnPartitionProperties_when_getEndpointCalledWithValidId() throws IOException {
    JsonNode expectedProperties =
        objectMapper.readTree(
            getClass().getResourceAsStream(TEST_FILES_PATH + TEST_PARTITION_1 + JSON));

    String response =
        given()
            .when()
            .get(PARTITIONS_ENDPOINT + "/" + TEST_PARTITION_1)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .extract()
            .asString();
    JsonNode actualProperties = objectMapper.readTree(response);

    assertEquals(expectedProperties, actualProperties);
  }

  @Test
  void should_return404_when_getEndpointCalledWithInvalidPartitionId() {
    given()
        .when()
        .get(PARTITIONS_ENDPOINT + "/" + INVALID_PARTITION)
        .then()
        .statusCode(404)
        .contentType(ContentType.JSON)
        .body(CODE, equalTo(404));
  }

  @Test
  void should_returnEmptyPartitionList_when_allPartitionsDeleted_and_listEndpointCalled() {
    given()
        .when()
        .get(PARTITIONS_ENDPOINT)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("", hasItems(TEST_PARTITION_1, TEST_PARTITION_2));

    for (String path : partitionConfigsPaths) {
      TestUtils.removeAllFilesInDir(Path.of(path));
    }
    waitDebounceDelay();

    given()
        .when()
        .get(PARTITIONS_ENDPOINT)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("", empty());
  }

  private void waitDebounceDelay() {
    try {
      Thread.sleep(debounceDelayMs * 2L);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
