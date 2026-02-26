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
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.model.info.VersionInfo;

@QuarkusTest
class VersionInfoBuilderTest {

  private static final String TEST_GROUP_ID = "org.opengroup.osdu";
  private static final String TEST_ARTIFACT_ID = "partition-artifact";
  private static final String TEST_VERSION = "1.0";
  private static final String TEST_BUILD_TIME = "2025-04-28T00:00:00Z";

  private static final String TEST_BRANCH = "test-branch";
  private static final String TEST_COMMIT_ID = "a6b5aee7348318201355adf1c7b0ad3e6caa035d";
  private static final String TEST_COMMIT_MSG = "Test short commit message";

  @InjectMock PartitionConfigProvider mockPartitionConfigProvider;

  @InjectMock GitPropertiesProvider gitPropertiesProvider;

  @Inject VersionInfoBuilder versionInfoBuilder;

  @BeforeEach
  void setUp() {
    when(mockPartitionConfigProvider.getGroupId()).thenReturn(TEST_GROUP_ID);
    when(mockPartitionConfigProvider.getArtifactId()).thenReturn(TEST_ARTIFACT_ID);
    when(mockPartitionConfigProvider.getVersion()).thenReturn(TEST_VERSION);
    when(mockPartitionConfigProvider.getBuildTime()).thenReturn(TEST_BUILD_TIME);

    when(gitPropertiesProvider.getBranch()).thenReturn(TEST_BRANCH);
    when(gitPropertiesProvider.getCommitId()).thenReturn(TEST_COMMIT_ID);
    when(gitPropertiesProvider.getCommitMessage()).thenReturn(TEST_COMMIT_MSG);
  }

  @Test
  void should_buildVersionInfo_when_gitPropertiesFilePresent() {
    VersionInfo versionInfo = versionInfoBuilder.buildVersionInfo();

    assertNotNull(versionInfo);

    assertEquals(TEST_GROUP_ID, versionInfo.getGroupId());
    assertEquals(TEST_ARTIFACT_ID, versionInfo.getArtifactId());
    assertEquals(TEST_VERSION, versionInfo.getVersion());
    assertEquals(TEST_BUILD_TIME, versionInfo.getBuildTime());

    assertEquals(TEST_BRANCH, versionInfo.getBranch());
    assertEquals(TEST_COMMIT_ID, versionInfo.getCommitId());
    assertEquals(TEST_COMMIT_MSG, versionInfo.getCommitMessage());

    assertNotNull(versionInfo.getConnectedOuterServices());
    assertTrue(versionInfo.getConnectedOuterServices().isEmpty());
  }
}
