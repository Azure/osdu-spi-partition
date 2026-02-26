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

package org.opengroup.osdu.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;
import static org.opengroup.osdu.util.TestUtils.NON_EXISTENT_DIRECTORY;
import static org.opengroup.osdu.util.TestUtils.TEST_FILES_PATH;
import static org.opengroup.osdu.util.TestUtils.VALID_PARTITIONS_TO_PATH_MAP;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.opengroup.osdu.model.PartitionInfo;
import org.opengroup.osdu.model.exception.AppException;
import org.opengroup.osdu.util.TestUtils;

@QuarkusTest
class PartitionFileLoaderServiceTest {

  @Inject PartitionFileLoaderService partitionFileLoaderService;

  @Inject ObjectMapper objectMapper;

  @Test
  void should_loadPartitionsFromValidFiles_and_skipNotValidFiles_when_directoryWithFilesExists(
      @TempDir Path baseTestsDir, @TempDir Path testHierarchyDir) throws Exception {
    TestUtils.resetTestFilesInDirs(baseTestsDir, testHierarchyDir);
    Map<String, PartitionInfo> partitionInfoMap =
        partitionFileLoaderService.loadPartitionInfoMapFromFiles(
            List.of(baseTestsDir.toString(), testHierarchyDir.toString()));

    assertEquals(VALID_PARTITIONS_TO_PATH_MAP.size(), partitionInfoMap.size());
    for (Map.Entry<String, String> partitionWithPath : VALID_PARTITIONS_TO_PATH_MAP.entrySet()) {
      assertThat(partitionInfoMap.keySet(), hasItem(partitionWithPath.getKey()));

      JsonNode expectedProperties =
          objectMapper.readTree(
              getClass().getResourceAsStream(TEST_FILES_PATH + partitionWithPath.getValue()));
      JsonNode actualProperties =
          objectMapper.valueToTree(
              partitionInfoMap.get(partitionWithPath.getKey()).getProperties());
      assertEquals(expectedProperties, actualProperties);
    }
  }

  @Test
  void should_loadEmptyPartitionInfoMap_when_emptyDirectoryExists(@TempDir Path emptyTempDir) {
    Map<String, PartitionInfo> partitionInfoMap =
        partitionFileLoaderService.loadPartitionInfoMapFromFiles(List.of(emptyTempDir.toString()));

    assertEquals(0, partitionInfoMap.size());
  }

  @Test
  void should_throwAppException_when_directoryDoesNotExist() {
    AppException exception =
        assertThrows(
            org.opengroup.osdu.model.exception.AppException.class,
            () ->
                partitionFileLoaderService.loadPartitionInfoMapFromFiles(
                    List.of(NON_EXISTENT_DIRECTORY)));
    assertEquals(500, exception.getError().getCode());
  }
}
