package org.opengroup.osdu.service;

import static org.jboss.resteasy.reactive.RestResponse.StatusCode.INTERNAL_SERVER_ERROR;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.jboss.logging.Logger;
import org.opengroup.osdu.model.PartitionInfo;
import org.opengroup.osdu.model.Property;
import org.opengroup.osdu.model.exception.AppException;

@ApplicationScoped
@RequiredArgsConstructor
public class PartitionFileLoaderService {
  private static final Logger log = Logger.getLogger(PartitionFileLoaderService.class);

  private static final String JSON_EXTENSION = ".json";
  private final ObjectMapper objectMapper;

  @Retry
  public Map<String, PartitionInfo> loadPartitionInfoMapFromFiles(List<String> directories) {
    log.debugf("Loading partition infos from directories: %s", directories);
    Map<String, PartitionInfo> loadedPartitions = new HashMap<>();

    for (String directory : directories) {
      Path directoryPath = Paths.get(directory);
      if (!Files.exists(directoryPath) || !Files.isDirectory(directoryPath)) {
        log.errorf("Directory does not exist or is not a directory: %s", directory);
        throw new AppException(
            INTERNAL_SERVER_ERROR,
            "Error loading partition info",
            "Directory does not exist or is not a directory: %s".formatted(directory));
      }

      try (Stream<Path> pathStream = Files.walk(directoryPath)) {
        pathStream
            .filter(this::isJsonFile)
            .forEach(pathToJsonFile -> loadPartitionInfoToMap(pathToJsonFile, loadedPartitions));
      } catch (IOException e) {
        log.errorf(e, "Error loading partition infos from directory: %s", directory);
        throw new AppException(
            INTERNAL_SERVER_ERROR, "Error loading partition info", e.getMessage(), e);
      }
    }
    return loadedPartitions;
  }

  private void loadPartitionInfoToMap(Path path, Map<String, PartitionInfo> partitionInfoMap) {
    try {
      String partitionId = getPartitionIdFromPath(path);
      if (partitionInfoMap.containsKey(partitionId)) {
        log.warnf("Partition info '%s' already exists. Skipping file: %s", partitionId, path);
        return;
      }
      partitionInfoMap.put(partitionId, loadPartitionInfoFromFile(path));
    } catch (Exception e) {
      log.errorf(
          "Error loading partition info from file: %s. Skipping file. Error: %s",
          path.getFileName(), e.getMessage());
    }
  }

  private boolean isJsonFile(Path path) {
    return Files.isRegularFile(path) && path.toString().toLowerCase().endsWith(JSON_EXTENSION);
  }

  private String getPartitionIdFromPath(Path path) {
    String fileName = path.getFileName().toString();
    if (!fileName.toLowerCase().endsWith(JSON_EXTENSION)) {
      throw new IllegalArgumentException(
          "File name '%s' does not end with expected extension '%s'"
              .formatted(fileName, JSON_EXTENSION));
    }
    return fileName.substring(0, fileName.length() - JSON_EXTENSION.length());
  }

  private PartitionInfo loadPartitionInfoFromFile(Path path) throws IOException {
    try (InputStream inputStream = Files.newInputStream(path)) {
      log.debugf("Loading partition info from file: %s", path.getFileName());
      Map<String, Property> loadedProperties =
          objectMapper.readValue(inputStream, new TypeReference<>() {});
      return new PartitionInfo(loadedProperties);
    }
  }
}
