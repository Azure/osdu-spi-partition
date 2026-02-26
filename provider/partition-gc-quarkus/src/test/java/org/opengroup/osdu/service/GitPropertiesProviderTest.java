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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class GitPropertiesProviderTest {

  private static final String TEST_GIT_PROPERTIES = "/test.git.properties";
  private static final String NOT_EXISTING_GIT_PROPERTIES = "/not-existing.properties";
  @InjectMock PartitionConfigProvider partitionConfigProvider;

  GitPropertiesProvider gitPropertiesProvider;

  void initGitPropertiesProvider(String gitPropertiesPath) {
    when(partitionConfigProvider.getGitPropertiesPath()).thenReturn(gitPropertiesPath);

    gitPropertiesProvider = new GitPropertiesProvider(partitionConfigProvider);
    gitPropertiesProvider.init();
  }

  @Test
  void should_loadGitProperties_when_initWithValidPath() {
    initGitPropertiesProvider(TEST_GIT_PROPERTIES);

    assertEquals("test-branch", gitPropertiesProvider.getBranch());
    assertEquals("a6b5aee7348318201355adf1c7b0ad3e6caa035d", gitPropertiesProvider.getCommitId());
    assertEquals("Test short commit message", gitPropertiesProvider.getCommitMessage());
  }

  @Test
  void should_setDefaults_when_fileNotFound() {
    initGitPropertiesProvider(NOT_EXISTING_GIT_PROPERTIES);

    assertEquals("N/A", gitPropertiesProvider.getBranch());
    assertEquals("N/A", gitPropertiesProvider.getCommitId());
    assertEquals("N/A", gitPropertiesProvider.getCommitMessage());
  }
}
