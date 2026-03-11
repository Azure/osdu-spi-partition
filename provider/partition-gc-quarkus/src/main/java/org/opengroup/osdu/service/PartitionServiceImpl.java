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
