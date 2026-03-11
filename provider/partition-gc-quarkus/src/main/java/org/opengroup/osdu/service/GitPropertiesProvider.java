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

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;

@ApplicationScoped
@RequiredArgsConstructor
public class GitPropertiesProvider {
  private static final Logger log = Logger.getLogger(GitPropertiesProvider.class);

  private static final String GIT_BRANCH = "git.branch";
  private static final String GIT_COMMIT_ID = "git.commit.id.full";
  private static final String GIT_COMMIT_MESSAGE_SHORT = "git.commit.message.short";
  private final PartitionConfigProvider partitionConfigProvider;
  @Getter private String branch;
  @Getter private String commitId;
  @Getter private String commitMessage;

  @PostConstruct
  public void init() {
    Properties gitProperties = new Properties();
    String gitPropertiesPath = partitionConfigProvider.getGitPropertiesPath();
    try (InputStream gitStream = this.getClass().getResourceAsStream(gitPropertiesPath)) {
      if (gitStream != null) {
        gitProperties.load(gitStream);
      } else {
        log.warnf("Git properties file not found by path: %s", gitPropertiesPath);
      }
    } catch (IOException e) {
      log.warnf("Error loading git properties: %s", e);
    }
    branch = gitProperties.getProperty(GIT_BRANCH, "N/A");
    commitId = gitProperties.getProperty(GIT_COMMIT_ID, "N/A");
    commitMessage = gitProperties.getProperty(GIT_COMMIT_MESSAGE_SHORT, "N/A");
  }
}
