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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.opengroup.osdu.model.exception.AppException;
import org.opengroup.osdu.util.EnvVarResource;
import java.util.List;

@QuarkusTest
@QuarkusTestResource(EnvVarResource.class)
class DirectoryWatchServiceTest {
  @Inject DirectoryWatchService directoryWatchService;

  @Test
  void should_callRunnableOnce_when_debounceCalledRapidly() {
    Runnable runnable = mock(Runnable.class);

    // Call debounce rapidly
    directoryWatchService.debounce(runnable);
    directoryWatchService.debounce(runnable);
    directoryWatchService.debounce(runnable);

    await().atMost(1, SECONDS).untilAsserted(() -> verify(runnable, times(1)).run());
  }

  @Test
  void should_throwAppException_when_watchDirectoryIOException() {
    String nonExistentDir = "/non/existent/dir";
    Runnable dummy = mock(Runnable.class);
    AppException ex =
        assertThrows(
            AppException.class, () -> directoryWatchService.watchDirectories(List.of(nonExistentDir), dummy));
    assertEquals(500, ex.getError().getCode());
  }
}
