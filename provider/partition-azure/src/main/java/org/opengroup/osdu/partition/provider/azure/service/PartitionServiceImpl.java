// Copyright 2017-2020, Schlumberger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.partition.provider.azure.service;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.azure.config.AzureConfig;
import org.opengroup.osdu.partition.provider.azure.persistence.PartitionTableStore;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PartitionServiceImpl implements IPartitionService {

    private static final String SYSTEM_PARTITION_DELETE_ERROR = "System partition is reserved for system and shared usage, currently it cannot Deleted";
    private static final String NOT_ALLOWED = "Not Allowed";
    static final String PARTITION_LIST_KEY = "getAllPartitions";
    static final String PARTITION_NOT_FOUND = "partition not found";

    @Autowired
    private PartitionTableStore tableStore;

    @Inject
    @Qualifier("partitionServiceCache")
    private ICache<String, PartitionInfo> partitionServiceCache;

    @Inject
    @Qualifier("partitionListCache")
    private ICache<String, List<String>> partitionListCache;

    @Autowired
    private AzureConfig azureConfig;

    @Override
    public PartitionInfo createPartition(String partitionId, PartitionInfo partitionInfo) {
        if (partitionServiceCache.get(partitionId) != null || tableStore.partitionExists(partitionId)) {
            throw new AppException(HttpStatus.SC_CONFLICT, "partition exist", "Partition with same id exist");
        }

        tableStore.addPartition(partitionId, partitionInfo);

        partitionServiceCache.put(partitionId, partitionInfo);
        partitionListCache.clearAll();

        return partitionInfo;
    }

    @Override
    public PartitionInfo updatePartition(String partitionId, PartitionInfo partitionInfo) {
        if (!tableStore.partitionExists(partitionId)) {
            throw new AppException(HttpStatus.SC_NOT_FOUND, PARTITION_NOT_FOUND, String.format("%s partition not found", partitionId));
        }

        if(partitionInfo.getProperties().containsKey("id")) {
            throw new AppException(HttpStatus.SC_BAD_REQUEST, "can not update id", "the field id can not be updated");
        }

        tableStore.addPartition(partitionId, partitionInfo);
        PartitionInfo pi = Optional.ofNullable(tableStore.getPartition(partitionId))
                .map(map -> PartitionInfo.builder().properties(map).build())
                .orElse(null);

        if(pi != null) {
            partitionServiceCache.put(partitionId, pi);
        }

        return pi;
    }

    @Override
    public PartitionInfo getPartition(String partitionId) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        PartitionInfo pi = partitionServiceCache.get(partitionId);
        stopwatch.stop();
        log.info(String.format("Total time taken to fetch from PartitionCache: %d", stopwatch.elapsed(TimeUnit.MILLISECONDS)));

        if (pi == null) {
            stopwatch.reset();
            stopwatch.start();
            Map<String, Property> out = new HashMap<>(tableStore.getPartition(partitionId));
            stopwatch.stop();
            log.info(String.format("Total time taken to Fetch Data From Storage Table: %d", stopwatch.elapsed(TimeUnit.MILLISECONDS)));

            if (out.isEmpty()) {
                throw new AppException(HttpStatus.SC_NOT_FOUND, PARTITION_NOT_FOUND, String.format("%s partition not found", partitionId));
            }

            pi = PartitionInfo.builder().properties(out).build();

            if (pi != null) {
                partitionServiceCache.put(partitionId, pi);
            }
        }

        return pi;
    }

    @Override
    public boolean deletePartition(String partitionId) {
        if(azureConfig.isReservedPartition(partitionId))
            throw new AppException(HttpStatus.SC_FORBIDDEN, NOT_ALLOWED, SYSTEM_PARTITION_DELETE_ERROR);

        if (!tableStore.partitionExists(partitionId)) {
            throw new AppException(HttpStatus.SC_NOT_FOUND, PARTITION_NOT_FOUND, String.format("%s partition not found", partitionId));
        }

        tableStore.deletePartition(partitionId);

        if (partitionServiceCache.get(partitionId) != null) {
            partitionServiceCache.delete(partitionId);
        }
        partitionListCache.clearAll();
        return true;
    }

    @Override
    public List<String> getAllPartitions() {
        List<String> partitions = partitionListCache.get(PARTITION_LIST_KEY);

        if (partitions == null) {
            Stopwatch stopwatch = Stopwatch.createStarted();
            partitions = tableStore.getAllPartitions();
            stopwatch.stop();
            log.info(String.format("Total time taken to fetch all partition: %d", stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            if (partitions != null) {
                partitionListCache.put(PARTITION_LIST_KEY, partitions);
            }
        }

        if(partitions != null) {
            partitions = partitions.stream()
                    .filter(item-> !azureConfig.isReservedPartition(item))
                    .toList();
        }

        return partitions;
    }
}
