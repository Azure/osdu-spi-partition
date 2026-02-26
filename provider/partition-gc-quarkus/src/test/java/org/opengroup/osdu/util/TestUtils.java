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

package org.opengroup.osdu.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TestUtils {

  public static final String PARTITIONS_ENDPOINT = "/partitions";
  public static final String TEST_PARTITION_1 = "osdu";
  public static final String TEST_PARTITION_2 = "second";
  public static final String INVALID_PARTITION = "invalid-partition";
  public static final String NON_EXISTENT_DIRECTORY = "non/existent/directory";
  public static final String TEST_PARTITION_ROOT = "root";
  public static final String TEST_PARTITION_LEVEL1 = "level1";
  public static final String TEST_PARTITION_LEVEL2 = "level2";
  public static final String TEST_FILES_PATH = "/testFiles/";
  public static final String JSON = ".json";

  private static final List<String> TEST_FILENAMES =
      List.of("osdu.json", "second.json", "not_valid.json", "not.json.txt", "not_a_partition.json");
  private static final String TEST_DIR_PREFIX = "partition-configs-test-";
  private static final String DIR_SUB_1 = "sub1";
  private static final String DIR_SUB_1_SUB_2 = "sub1/sub2";

  public static final Map<String, String> VALID_PARTITIONS_TO_PATH_MAP =
      Map.of(
          TEST_PARTITION_1, TEST_PARTITION_1 + JSON,
          TEST_PARTITION_2, TEST_PARTITION_2 + JSON,
          TEST_PARTITION_ROOT, TEST_PARTITION_ROOT + JSON,
          TEST_PARTITION_LEVEL1, DIR_SUB_1 + "/" + TEST_PARTITION_LEVEL1 + JSON,
          TEST_PARTITION_LEVEL2, DIR_SUB_1_SUB_2 + "/" + TEST_PARTITION_LEVEL2 + JSON);

  public static List<Path> createTestDirectories() {
    List<Path> testDirs = new ArrayList<>();
    try {
      testDirs.add(Files.createTempDirectory(TEST_DIR_PREFIX));
      testDirs.add(Files.createTempDirectory(TEST_DIR_PREFIX));
    } catch (IOException e) {
      throw new RuntimeException("Could not create temp dir for test", e);
    }
    return testDirs;
  }

  public static void resetTestFilesInDirs(Path baseTestsDir, Path testHierarchyDir) {
    resetFilesForBaseTests(baseTestsDir);
    resetFilesForTestHierarchy(testHierarchyDir);
  }

  public static void resetFilesForBaseTests(Path dir) {
    removeAllFilesInDir(dir);
    try {
      for (String filename : TEST_FILENAMES) {
        copyTestFileToDir(filename, dir);
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not copy test file to temp dir", e);
    }
  }

  public static void resetFilesForTestHierarchy(Path dir) {
    removeAllFilesInDir(dir);
    try {
      copyTestFileToDir(TEST_PARTITION_ROOT + JSON, dir);

      Files.createDirectories(dir.resolve(DIR_SUB_1));
      copyTestFileToDir(DIR_SUB_1 + "/" + TEST_PARTITION_LEVEL1 + JSON, dir);

      Files.createDirectories(dir.resolve(DIR_SUB_1_SUB_2));
      copyTestFileToDir(DIR_SUB_1_SUB_2 + "/" + TEST_PARTITION_LEVEL2 + JSON, dir);
    } catch (IOException e) {
      throw new RuntimeException("Could not copy test file to temp dir", e);
    }
  }

  public static void removeAllFilesInDir(Path dir) {
    try (Stream<Path> paths = Files.walk(dir)) {
      paths
          .sorted(Comparator.reverseOrder())
          .filter(path -> !path.equals(dir)) // Ignore the root directory
          .forEach(
              path -> {
                try {
                  Files.deleteIfExists(path);
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
              });
    } catch (IOException e) {
      throw new RuntimeException("Could not delete files in dir " + dir, e);
    }
  }

  private static void copyTestFileToDir(String testFileName, Path dir) throws IOException {
    try (InputStream is =
        EnvVarResource.class.getClassLoader().getResourceAsStream(TEST_FILES_PATH + testFileName)) {
      if (is == null) throw new IllegalArgumentException("Test file not found: " + testFileName);
      Files.copy(is, dir.resolve(testFileName));
    }
  }
}
