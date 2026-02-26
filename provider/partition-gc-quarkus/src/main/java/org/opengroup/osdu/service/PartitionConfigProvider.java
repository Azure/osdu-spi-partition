package org.opengroup.osdu.service;

import static org.jboss.resteasy.reactive.RestResponse.Status.INTERNAL_SERVER_ERROR;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.opengroup.osdu.model.exception.AppException;
import java.util.List;

@ApplicationScoped
@Startup
@Getter
public class PartitionConfigProvider {
  private static final Logger log = Logger.getLogger(PartitionConfigProvider.class);

  @ConfigProperty(name = "partitionConfigsPaths")
  private List<String> partitionConfigsPaths;

  @ConfigProperty(name = "groupId")
  private String groupId;

  @ConfigProperty(name = "artifactId")
  private String artifactId;

  @ConfigProperty(name = "version")
  private String version;

  @ConfigProperty(name = "buildTime")
  private String buildTime;

  @ConfigProperty(name = "gitPropertiesPath", defaultValue = "/git.properties")
  private String gitPropertiesPath;

  protected void setPartitionConfigsPaths(List<String> partitionConfigsPaths) {
    this.partitionConfigsPaths = partitionConfigsPaths;
  }

  @PostConstruct
  protected void init() {
    if (partitionConfigsPaths == null || partitionConfigsPaths.isEmpty()) {
      throw new AppException(
          INTERNAL_SERVER_ERROR.getStatusCode(),
          "Environment variable is not set",
          "The environment variable PARTITION_CONFIGS_PATHS is required but not set");
    }
    log.infof("Partition Configs Paths: %s", partitionConfigsPaths);

    for (String path : partitionConfigsPaths) {
      if (!Files.exists(Paths.get(path))) {
        log.errorf("Partition Configs Path: %s does not exist", path);
        throw new AppException(
            INTERNAL_SERVER_ERROR.getStatusCode(),
            "Partition Configs Path does not exist",
            "Partition Configs Path: " + path + " does not exist");
      }
    }
  }
}
