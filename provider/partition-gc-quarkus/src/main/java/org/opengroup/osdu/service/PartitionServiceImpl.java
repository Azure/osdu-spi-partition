package org.opengroup.osdu.service;

import static org.jboss.resteasy.reactive.RestResponse.Status.NOT_FOUND;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;
import org.opengroup.osdu.configuration.PropertyConfiguration;
import org.opengroup.osdu.model.PartitionInfo;
import org.opengroup.osdu.model.exception.AppException;

@ApplicationScoped
@Startup
@RequiredArgsConstructor
public class PartitionServiceImpl implements IPartitionService {
  private static final Logger log = Logger.getLogger(PartitionServiceImpl.class);
  private final DirectoryWatchService directoryWatchService;
  private final PartitionFileLoaderService partitionFileLoaderService;
  private final PartitionConfigProvider partitionConfigProvider;
  private final PropertyConfiguration configuration;
  private final AtomicReference<Map<String, PartitionInfo>> partitionInfoMapRef =
      new AtomicReference<>(new ConcurrentHashMap<>());

  @PostConstruct
  protected void init() {
    log.info("Initializing Partition Service");
    updatePartitionInfoMapFromFiles();
    CompletableFuture.runAsync(
        () ->
            directoryWatchService.watchDirectories(
                partitionConfigProvider.getPartitionConfigsPaths(),
                this::updatePartitionInfoMapFromFiles));
  }

  @Override
  public List<String> getPartitionList() {
    log.debug("Getting partition list");
    String systemPartitionId = configuration.getSystemPartitionId();
    return partitionInfoMapRef.get().keySet().stream().filter(id -> !id.equals(systemPartitionId)).toList();
  }

  @Override
  public PartitionInfo getPartition(String partitionId) {
    log.debugf("Getting partition with id: %s", partitionId);
    Map<String, PartitionInfo> partitionInfoMap = partitionInfoMapRef.get();
    if (!partitionInfoMap.containsKey(partitionId)) {
      log.errorf("Partition with id: %s not found", partitionId);
      throw new AppException(
          NOT_FOUND.getStatusCode(), NOT_FOUND.getReasonPhrase(), "Partition does not exist");
    }
    return partitionInfoMap.get(partitionId);
  }

  protected void updatePartitionInfoMapFromFiles() {
    Map<String, PartitionInfo> loadedPartitionInfoMap = loadPartitionInfoMapFromFiles();
    updatePartitionInfoMap(loadedPartitionInfoMap);
  }

  protected Map<String, PartitionInfo> loadPartitionInfoMapFromFiles() {
    Map<String, PartitionInfo> loadedPartitionInfoMap =
        partitionFileLoaderService.loadPartitionInfoMapFromFiles(
            partitionConfigProvider.getPartitionConfigsPaths());
    log.infof(
        "Loaded %s partitions from directories: %s",
        loadedPartitionInfoMap.size(), partitionConfigProvider.getPartitionConfigsPaths());
    return loadedPartitionInfoMap;
  }

  protected void updatePartitionInfoMap(Map<String, PartitionInfo> newPartitionInfoMap) {
    partitionInfoMapRef.set(new ConcurrentHashMap<>(newPartitionInfoMap));
    log.infof(
        "Partition info map updated. Current partitions: %s", partitionInfoMapRef.get().keySet());
  }
}
