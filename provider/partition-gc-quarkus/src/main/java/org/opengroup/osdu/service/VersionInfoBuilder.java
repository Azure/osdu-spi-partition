/*
 * Copyright 2017-2025, Google
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.model.info.VersionInfo;

@ApplicationScoped
@RequiredArgsConstructor
public class VersionInfoBuilder {
  private final PartitionConfigProvider partitionConfigProvider;
  private final GitPropertiesProvider gitPropertiesProvider;

  public VersionInfo buildVersionInfo() {
    return VersionInfo.builder()
        .groupId(partitionConfigProvider.getGroupId())
        .artifactId(partitionConfigProvider.getArtifactId())
        .version(partitionConfigProvider.getVersion())
        .buildTime(partitionConfigProvider.getBuildTime())
        .branch(gitPropertiesProvider.getBranch())
        .commitId(gitPropertiesProvider.getCommitId())
        .commitMessage(gitPropertiesProvider.getCommitMessage())
        .connectedOuterServices(Collections.emptyList())
        .build();
  }
}
