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
