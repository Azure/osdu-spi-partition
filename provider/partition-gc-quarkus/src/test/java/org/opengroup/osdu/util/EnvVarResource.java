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

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jboss.logging.Logger;

public class EnvVarResource implements QuarkusTestResourceLifecycleManager {
  private static final Logger log = Logger.getLogger(EnvVarResource.class);

  private List<Path> tempDirs;

  @Override
  public Map<String, String> start() {
    tempDirs = TestUtils.createTestDirectories();
    return Map.of(
        "PARTITION_CONFIGS_PATHS",
        tempDirs.stream()
            .map(Path::toAbsolutePath)
            .map(Path::toString)
            .map(s -> s.replace("\\", "\\\\"))
            .collect(Collectors.joining(",")));
  }

  @Override
  public void stop() {
    tempDirs.forEach(this::deleteTempDir);
  }

  private void deleteTempDir(Path tempDir) {
    if (tempDir != null && Files.exists(tempDir)) {
      try (Stream<Path> walk = Files.walk(tempDir)) {
        walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
      } catch (IOException e) {
        log.warnf(e, "Could not delete temp file or directory: %s", tempDir);
      }
    }
  }
}
