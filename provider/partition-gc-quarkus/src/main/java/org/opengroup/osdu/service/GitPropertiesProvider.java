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
