/*
 * Copyright 2020-2021 Google LLC
 * Copyright 2020-2021 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.partition.coreplus.service;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.logging.AuditLogger;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.coreplus.model.PartitionPropertyEntity;
import org.opengroup.osdu.partition.coreplus.osm.repository.OsmPartitionPropertyRepository;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PartitionServiceImpl implements IPartitionService {

  static final String UNKNOWN_ERROR_REASON = "unknown error";
  static final String PARTITION_LIST_KEY = "getAllPartitions";

  private final OsmPartitionPropertyRepository partitionPropertyEntityRepository;

  private final AuditLogger auditLogger;

  private final ICache<String, PartitionInfo> partitionServiceCache;

  private final ICache<String, List<String>> partitionListCache;

  @Override
  public PartitionInfo createPartition(String partitionId, PartitionInfo partitionInfo) {
    if (partitionServiceCache.get(partitionId) != null)
      throw new AppException(HttpStatus.SC_CONFLICT, "partition exist", "Partition with same id exist");

    if (this.partitionPropertyEntityRepository.findByPartitionId(partitionId).isPresent()) {
      this.auditLogger.createPartitionFailure(Collections.singletonList(partitionId));
      throw new AppException(HttpStatus.SC_CONFLICT, UNKNOWN_ERROR_REASON,
              "Partition already exists.");
    }
    List<PartitionPropertyEntity> partitionProperties = new ArrayList<>();
    for (Map.Entry<String, Property> entry : partitionInfo.getProperties().entrySet()) {
      PartitionPropertyEntity entity = new PartitionPropertyEntity(partitionId,
              entry.getKey(), entry.getValue());
      partitionProperties.add(entity);
    }
    this.partitionPropertyEntityRepository.saveAll(partitionProperties);
    PartitionInfo pi = getPartition(partitionId);

    if (Objects.nonNull(pi)) {
      partitionListCache.clearAll();
    }

    return pi;
  }

  @Override
  public PartitionInfo updatePartition(String partitionId, PartitionInfo partitionInfo) {
    if (partitionInfo.getProperties().containsKey("id")) {
      this.auditLogger.updatePartitionSecretFailure(Collections.singletonList(partitionId));
      throw new AppException(HttpStatus.SC_BAD_REQUEST, "can not update id",
              "the field id can not be updated");
    }

    if (!this.partitionPropertyEntityRepository.findByPartitionId(partitionId).isPresent()) {
      this.auditLogger.updatePartitionSecretFailure(Collections.singletonList(partitionId));
      throw new AppException(HttpStatus.SC_NOT_FOUND, UNKNOWN_ERROR_REASON,
              "An attempt to update not existing partition.");
    }

    List<PartitionPropertyEntity> partitionProperties = new ArrayList<>();
    for (Map.Entry<String, Property> entry : partitionInfo.getProperties().entrySet()) {
      PartitionPropertyEntity entity = this.partitionPropertyEntityRepository
              .findByPartitionIdAndName(partitionId, entry.getKey());
      if (Objects.nonNull(entity)) {
        entity.setSensitive(entry.getValue().isSensitive());
        entity.setValue(entry.getValue().getValue());
      } else {
        entity = new PartitionPropertyEntity(partitionId, entry.getKey(), entry.getValue());
      }
      partitionProperties.add(entity);
    }
    this.partitionPropertyEntityRepository.saveAll(partitionProperties);
    if (Objects.nonNull(partitionServiceCache.get(partitionId))) {
      partitionServiceCache.delete(partitionId);
    }

    return getPartition(partitionId);
  }

  @Override
  public PartitionInfo getPartition(String partitionId) {
    PartitionInfo pi = partitionServiceCache.get(partitionId);

    if (Objects.isNull(pi)) {
      pi = getEncryptedPartition(partitionId);

      if (Objects.nonNull(pi)) {
        partitionServiceCache.put(partitionId, pi);
      }
    }

    return pi;
  }

  private PartitionInfo getEncryptedPartition(String partitionId) {
    Optional<List<PartitionPropertyEntity>> partitionPropertyEntitiesOptional = partitionPropertyEntityRepository
            .findByPartitionId(partitionId);
    if (!partitionPropertyEntitiesOptional.isPresent()) {
      this.auditLogger.readPartitionFailure(Collections.singletonList(partitionId));
      throw new AppException(HttpStatus.SC_NOT_FOUND, UNKNOWN_ERROR_REASON,
          "Partition does not exist.");
    }
    List<PartitionPropertyEntity> partitionPropertiesList = partitionPropertyEntitiesOptional.get();
    PartitionInfo partitionInfo = new PartitionInfo();
    Map<String, Property> partitionInfoProperties = new HashMap<>();
    for (PartitionPropertyEntity entity : partitionPropertiesList) {
      partitionInfoProperties
          .put(entity.getName(), new Property(entity.getSensitive(), entity.getValue()));
    }
    partitionInfo.setProperties(partitionInfoProperties);

    return partitionInfo;
  }

  @Override
  public boolean deletePartition(String partitionId) {
    if (!this.partitionPropertyEntityRepository.findByPartitionId(partitionId).isPresent()) {
      this.auditLogger.deletePartitionFailure(Collections.singletonList(partitionId));
      throw new AppException(HttpStatus.SC_NOT_FOUND, UNKNOWN_ERROR_REASON,
              "An attempt to delete not existing partition.");
    }
    this.partitionPropertyEntityRepository.deleteByPartitionId(partitionId);

    if (Objects.nonNull(partitionServiceCache.get(partitionId))) {
      partitionServiceCache.delete(partitionId);
    }
    partitionListCache.clearAll();
    return true;
  }

  @Override
  public List<String> getAllPartitions() {
    List<String> partitions = partitionListCache.get(PARTITION_LIST_KEY);

    if (Objects.isNull(partitions)) {
      List<String> allPartitions = this.partitionPropertyEntityRepository.getAllPartitions();
      partitions = (allPartitions.isEmpty() ? Collections.emptyList() : allPartitions);

      if (!CollectionUtils.isEmpty(partitions)) {
        partitionListCache.put(PARTITION_LIST_KEY, partitions);
      }
    }
    return partitions;
  }
}
